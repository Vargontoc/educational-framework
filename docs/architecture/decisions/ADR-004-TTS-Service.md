# ADR-007 — TTS Service: Coqui TTS on-premise via Docker

## Status

Accepted

## Fecha

2026-04-26

## Contexto

El flujo de la aplicación es:

```
UI → Backend → Agente → TTS → UI
```

El servicio TTS convierte el `content_text` devuelto por el agente en audio para el niño. Para el rango de edad objetivo (3–8 años), la voz sintetizada es parte de la identidad del personaje (FEAT-005): debe ser estable, consistente entre sesiones e independiente de la conectividad externa.

El proyecto es una **aplicación privada monousuario**, desplegada en entorno doméstico con Docker Compose junto a Ollama y PostgreSQL.

### Opciones evaluadas

| Opción | Coste | Privacidad | Consistencia de voz | Complejidad operativa |
|---|---|---|---|---|
| Coqui TTS (Docker local) | 0 | Total | Alta — voz única permanente | Mínima — un servicio más en docker-compose |
| Google Cloud TTS | Free tier | PII sale del servidor | Depende de conectividad | Media — API key, DPA |
| Amazon Polly | Free tier | PII sale del servidor | Depende de conectividad | Media — AWS account |
| ElevenLabs | 10k chars/mes | PII sale del servidor | Depende de conectividad | Media |

### Factores decisivos

**Consistencia de voz.** Un cambio de voz entre sesiones (por fallback cloud o pérdida de conectividad) es percibido por niños de 3–8 años como un cambio de personaje. La voz debe ser siempre la misma.

**Privacidad sin configuración adicional.** El `content_text` puede contener el `sanitized_agent_name` del niño (FEAT-002). Con Coqui on-premise, ningún dato sale de la red local.

**Homogeneidad del stack.** El proyecto ya usa Ollama como contenedor local para el agente. Coqui sigue exactamente el mismo patrón: imagen Docker oficial, volumen persistente para modelos, comunicación interna por HTTP dentro de `educational-network`.

## Decisión

Se añade **Coqui TTS** como un servicio adicional en el `docker-compose.yml` existente, siguiendo el mismo patrón que `ollama-educational`.

```yaml
coqui-educational:
  image: ghcr.io/coqui-ai/tts
  command: --model_name tts_models/es/css10/vits --use_cuda true
  container_name: coqui-educational
  networks:
    - educational-network
  volumes:
    - coqui_models:/root/.local/share/tts
  restart: unless-stopped
```

El modelo `tts_models/es/css10/vits` se descarga al volumen `coqui_models` en el primer arranque y persiste entre reinicios, igual que los modelos de Ollama en `ollama_models`.

La lógica de comunicación con Coqui (construcción de la petición, parámetros de prosodia, caché de audio) se implementa **dentro del backend Spring Boot existente**, sin microservicios adicionales. El backend llama a Coqui por HTTP en `http://educational-coqui:5002/api/tts`, que es un endpoint interno de `educational-network` no expuesto al host.

Los errores de TTS (servicio no alcanzable, fallo de síntesis) se gestionan en la **capa de UI** mediante animaciones y texto de fallback, sin lógica de reintento ni voz alternativa en el backend.

## Consecuencias

### Positivas

- Stack homogéneo: Coqui sigue el mismo patrón operativo que Ollama (imagen oficial, volumen persistente, red interna).
- Voz del personaje consistente en todas las sesiones y condiciones de red.
- Ningún dato sale de la red local; privacidad total sin configuración adicional.
- Coste operativo cero.
- La lógica TTS vive en el backend existente — no hay nuevos servicios que mantener, desplegar ni monitorizar.

### Negativas / trade-offs

- Calidad de voz inferior a servicios cloud premium. Aceptado dado el contexto privado y monousuario.
- Sin GPU, la latencia de síntesis para textos cortos (≤300 chars) es asumible pero mayor que un servicio cloud. Mitigable con caché de audio para contenido de catálogo repetible (curiosidades, muletillas, motivación).
- El mantenimiento del modelo (pronunciación, actualizaciones) es responsabilidad del equipo.

## Relación con otras decisiones

- **FEAT-001**: El límite de 300 chars en `content_text` acota la latencia de síntesis.
- **FEAT-002**: Al ser on-premise, el `sanitized_agent_name` nunca sale del servidor.
- **FEAT-003**: Los parámetros de prosodia por tono y edad se pasan como parámetros a la API de Coqui desde el backend.
- **FEAT-004, FEAT-005, FEAT-006**: El contenido de catálogo finito y curado es candidato a pre-síntesis y caché de audio.