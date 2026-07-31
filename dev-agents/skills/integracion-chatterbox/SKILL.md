---
name: integracion-chatterbox
description: "Usar para conectar la API del TTS service con el contenedor Docker de Chatterbox, gestionando comunicación, formatos y errores de conexión."
---

# Integración con Chatterbox

## Propósito

Establecer y mantener la comunicación entre la API del TTS service y el contenedor Docker que ejecuta Chatterbox. Gestionar el descubrimiento del servicio, el formato de peticiones/respuestas y el manejo de errores de conexión.

## Requisito previo

El contenedor Docker de Chatterbox debe estar levantado y accesible desde la red Docker.

## Scope exclusivo

Este skill gestiona ÚNICAMENTE la comunicación con Chatterbox:
- Descubrimiento de IP/puerto del contenedor
- Formato de petición a Chatterbox
- Formato de respuesta (WAV)
- Manejo de errores de conexión

**No gestiona:**
- Almacenamiento de audio (responsabilidad de la capa backend de la aplicación)
- Frontend ni reproducción de audio
- Conversión de formatos (ver skill `conversion-audio`)

## Flujo de comunicación

```
API TTS → [Texto formateado] → Chatterbox → [Audio WAV] → API TTS
```

## Elementos a gestionar

### 1. Descubrimiento del servicio
- IP y puerto del contenedor Docker
- Protocolo de comunicación (HTTP/REST)
- Health check del servicio

### 2. Formato de petición
- Texto de entrada ya formateado (con nombre del niño insertado)
- Parámetros de voz (npc-voice o narrative-voice)
- Configuración de tonalidad (si aplica)

### 3. Formato de respuesta
- Audio en formato WAV
- Metadatos del audio (duración, calidad)
- Estados de respuesta (éxito, error, timeout)

### 4. Manejo de errores
- Conexión rechazada
- Servicio no disponible
- Timeout de respuesta
- Formato de respuesta inválido

## Criterios de validación

- [ ] La API puede descubrir el contenedor Chatterbox automáticamente
- [ ] La petición se envía con el formato correcto
- [ ] La respuesta WAV se recibe correctamente
- [ ] Los errores de conexión se manejan sin crashes
- [ ] El health check verifica que Chatterbox está operativo

## Preguntas para el agente técnico

1. ¿Qué IP/puerto usa el contenedor Chatterbox por defecto?
2. ¿Qué endpoint expone Chatterbox para generación de audio?
3. ¿Qué parámetros acepta la petición (texto, voz, tonalidad)?
4. ¿Cómo se implementa el health check?
5. ¿Cuál es el timeout de conexión recomendado?

## Dependencias

- **Infra Agent**: Debe proporcionar la configuración del contenedor Docker
- **Backend Agent**: Usa esta comunicación para generar audio

## Notas

- Chatterbox genera audio en formato WAV
- La API del TTS service debe convertir a MP3 después de recibir la respuesta
- El almacenamiento del audio no es responsabilidad de este skill
