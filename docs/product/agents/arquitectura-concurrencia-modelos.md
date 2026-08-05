# Arquitectura de concurrencia de modelos

## Estado

Documento de decision tecnica.

## Contexto

My Friend Nubi utiliza dos agentes de lenguaje que pueden operar simultaneamente:

- **npc-game**: agente infantil de juego, basado en `qwen2.5:7b-Q5_K_M`, permanente durante la sesion de juego del nino.
- **agent-educational-parent**: chatbot parental, basado en `qwen3:8b-Q3_K_M`, cargado bajo demanda cuando un padre realiza una consulta.

Ambos agentes pueden coexistir en una GPU NVIDIA RTX 4070 SUPER con 12 GB de VRAM, junto con el sistema de voz Chatterbox.

## Configuracion de modelos

| Agente | Modelo base | Cuantificacion | VRAM estimada | Carga |
|--------|-------------|----------------|----------------|-------|
| npc-game | `qwen2.5:7b` | `Q5_K_M` | ~6 GB | Permanente durante sesion de juego |
| agent-educational-parent | `qwen3:8b` | `Q3_K_M` | ~5.5 GB | Bajo demanda (latencia ~15-20 s) |
| Chatterbox (TTS) | N/A | N/A | ~2-3 GB | Temporal (ver gestion abajo) |

**VRAM total estimada en concurrencia maxima**: ~11.5 GB (npc-game + parent), dejando ~0.5 GB de margen para overhead de Ollama y sistema.

## Gestion de Chatterbox en concurrencia

Chatterbox es el sistema de texto a voz utilizado por npc-game para generar audio no cacheado. En escenario de concurrencia maxima (npc-game + parent activos), Chatterbox no puede permanecer cargado simultaneamente sin exceder los 12 GB de VRAM.

### Estrategia: descarga temporal

Cuando un padre realiza una consulta al chatbot:

1. El backend detecta que agent-educational-parent necesita cargarse.
2. Si Chatterbox esta activo en memoria, se descarga temporalmente.
3. Se carga agent-educational-parent (`qwen3:8b-Q3_K_M`).
4. El padre recibe su respuesta.
5. Tras la consulta, se recarga Chatterbox si npc-game sigue activo.

### Impacto

- **Para el nino**: el audio cacheado de npc-game sigue disponible. Solo el audio no cacheado (nuevas frases generadas) puede verse afectado durante la descarga/recarga de Chatterbox (~5-10 s adicionales).
- **Para el padre**: latencia total de ~15-20 s (carga del modelo) + ~5-10 s (gestion de Chatterbox si aplica) = ~20-30 s en el peor caso. Aceptable para un chatbot no urgente.

## Escenarios de concurrencia

### Escenario 1: Solo nino jugando

- **Modelos cargados**: npc-game (`qwen2.5:7b-Q5_K_M`) + Chatterbox.
- **VRAM**: ~8-9 GB.
- **Latencia**: minima, todo en memoria.

### Escenario 2: Solo padre consultando

- **Modelos cargados**: agent-educational-parent (`qwen3:8b-Q3_K_M`).
- **VRAM**: ~5.5 GB.
- **Latencia**: ~15-20 s (carga bajo demanda).

### Escenario 3: Nino jugando + padre consultando (concurrencia maxima)

- **Modelos cargados**: npc-game + agent-educational-parent. Chatterbox se descarga temporalmente.
- **VRAM**: ~11.5 GB.
- **Latencia padre**: ~20-30 s (carga de parent + gestion de Chatterbox).
- **Latencia nino**: audio cacheado disponible; audio nuevo con ~5-10 s de retraso durante la transicion.

### Escenario 4: Sin actividad

- **Modelos cargados**: ninguno (Ollama puede liberar memoria tras inactividad).
- **VRAM**: minima.

## Alternativas descartadas

| Alternativa | Motivo de descarte |
|-------------|-------------------|
| Mantener los tres modelos cargados (npc-game + parent + Chatterbox) | Excede 12 GB VRAM (~14 GB estimados). |
| Usar `qwen3:14b` para parent | No cabe junto con npc-game 7b (~16 GB total). |
| Limitar chatbot parental cuando Chatterbox esta activo | Viola requisito de disponibilidad para padres. |
| Usar CPU para parent en concurrencia | Latencia inaceptable (>60 s). |
| Fine-tuning de un modelo unico para ambos agentes | Viola principio de separacion estricta (ADR-002). |

## Decisiones tecnicas

1. **Modelo parent**: `qwen3:8b-Q3_K_M` (bajo demanda, latencia aceptable para padres).
2. **Modelo npc-game**: `qwen2.5:7b-Q5_K_M` (permanente durante sesion de juego).
3. **Chatterbox**: descarga temporal cuando padre consulta si npc-game esta activo.
4. **Gestion de memoria**: responsabilidad de Ollama + backend (descarga/recarga automatica).

## Riesgos

- La descarga/recarga de Chatterbox anade complejidad de infraestructura y latencia adicional.
- La latencia de ~20-30 s en concurrencia maxima puede ser percibida como lenta por el padre.
- `qwen3:8b-Q3_K_M` puede no tener capacidad suficiente para todos los guardrails de FEAT-003 (a validar en Sprint 003).

## Referencias

- SPRINT-001-system-prompt-guardrails-corpus.md
- FEAT-003: Chatbot parental conversacional de Nubi
- ADR-002: Agent education-framework-agent-child (principio de separacion estricta)
- ADR-003: Chatbot parental conversacional de Nubi
