# Propuesta de corpus oficial para My Friend Nubi

## Estado

**Propuesta pendiente de aprobacion de producto.**

Este documento define la estructura, formato y mecanismo de alimentacion del corpus oficial que utilizara el chatbot parental (`agent-educational-parent`) como fuente de verdad para sus respuestas.

## Formato propuesto

Cada archivo del corpus sera un documento Markdown con la siguiente estructura:

```markdown
# <Titulo del tema>

## Descripcion
Breve descripcion del tema (2-3 parrafos maximo).

## Limites
Que no cubre este tema. Que queda fuera de alcance.

## Mensajes clave
- Mensaje clave 1
- Mensaje clave 2
- Mensaje clave 3

## Derivaciones
Cuando deriva a profesional y a que tipo de profesional.
```

Cada archivo debe ser:
- Autocontenido: comprensible sin necesidad de otros archivos.
- Breve: maximo 500 palabras por archivo.
- Preciso: sin ambiguedades ni interpretaciones abiertas.
- En espanol sin tildes (convencion del proyecto).

## Ubicacion propuesta

```
docs/official-corpus/
├── que-es-my-friend-nubi.md
├── progreso-orientativo.md
├── panel-parental.md
├── perfiles-infantiles.md
└── limites-y-exclusiones.md
```

## Archivos iniciales propuestos

| Archivo | Contenido |
|---------|-----------|
| `que-es-my-friend-nubi.md` | Descripcion general de la app, publico objetivo, filosofia de acompanamiento, qué es y que no es. |
| `progreso-orientativo.md` | Explicacion del progreso como orientativo para la familia, nunca como evaluacion, diagnostico o clasificacion del menor. |
| `panel-parental.md` | Funcionalidades del panel de control parental: perfiles, actividades, acompanhamento. |
| `perfiles-infantiles.md` | Creacion y gestion de perfiles infantiles, comandos de seleccion, proteccion de datos del menor. |
| `limites-y-exclusiones.md` | Temas excluidos, frase exacta de rechazo, derivaciones profesionales, prohibiciones especificas. |

## Mecanismo de alimentacion RAG

El corpus oficial se alimentara mediante RAG (Retrieval-Augmented Generation):

1. **Embeddings**: cada archivo del corpus se convierte en vectores mediante un modelo de embeddings (responsabilidad de backend/infraestructura).
2. **Vector store**: los vectores se almacenan en una base de datos vectorial (responsabilidad de backend/infraestructura).
3. **Recuperacion**: en cada turno, el backend recupera los fragmentos mas relevantes (top-K) segun la consulta del usuario.
4. **Inyeccion**: los fragmentos recuperados se inyectan en el prompt como `corpus_context`.
5. **Generacion**: el agente genera su respuesta basandose en el `corpus_context`, no en conocimiento general.

### Responsabilidades

| Capa | Responsabilidad |
|------|-----------------|
| Producto | Aprobar contenido del corpus, revisar archivos de ejemplo. |
| Backend/Infraestructura | Implementar embeddings, vector store, recuperacion RAG, inyeccion de `corpus_context` e `conversation_history`. |
| Agents | Consumir `corpus_context` y `conversation_history` respetando los guardrails del system prompt. |

### Parametros RAG (pendientes de definicion tecnica)

- **Top-K**: numero de fragmentos a recuperar por consulta (propuesta inicial: 3-5).
- **Longitud maxima de `corpus_context`**: a definir segun capacidad del modelo y limites de tokens.
- **Modelo de embeddings**: a seleccionar por backend/infraestructura.
- **Base de datos vectorial**: a seleccionar por backend/infraestructura.

## Siguiente pasos

1. Revision y aprobacion de producto del formato y archivos iniciales.
2. Creacion de contenido real para cada archivo del corpus.
3. Implementacion de RAG en backend/infraestructura (sprint posterior).
4. Integracion de `corpus_context` en el flujo de invocacion del agente.
5. Validacion de calidad de respuestas con corpus real (Sprint 003).
