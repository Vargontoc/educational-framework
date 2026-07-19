---
name: pruebas-tts
description: "Usar para diseñar y ejecutar pruebas manuales de validación de la API del TTS service, incluyendo calidad de audio, timeout y fallback."
---

# Pruebas Manuales del TTS Service

## Propósito

Diseñar y ejecutar pruebas manuales para validar el correcto funcionamiento de la API del TTS service, incluyendo comunicación con Chatterbox, calidad de audio, timeout y fallback.

## Requisito previo

La API del TTS service y el contenedor Chatterbox deben estar levantados y accesibles.

## Scope exclusivo

Este skill gestiona ÚNICAMENTE:
- Diseño de casos de prueba manuales
- Ejecución de pruebas de validación
- Documentación de resultados

**No gestiona:**
- Pruebas automatizadas (eso corresponde al agente técnico)
- Pruebas de frontend
- Pruebas de almacenamiento (responsabilidad de la capa backend)

## Casos de prueba

### CP-001: Comunicación con Chatterbox

| Dato | Valor |
|------|-------|
| **Objetivo** | Validar que la API se conecta correctamente con Chatterbox |
| **Precondición** | Contenedor Chatterbox levantado |
| **Pasos** | 1. Enviar petición de audio con texto válido<br>2. Verificar que se recibe respuesta WAV |
| **Resultado esperado** | Se recibe audio WAV válido |
| **Criterio de éxito** | Audio reproducible, sin errores |

### CP-002: Conversión WAV a MP3

| Dato | Valor |
|------|-------|
| **Objetivo** | Validar que la conversión de formato funciona correctamente |
| **Precondición** | Audio WAV recibido de Chatterbox |
| **Pasos** | 1. Convertir WAV a MP3<br>2. Reproducir MP3 resultante |
| **Resultado esperado** | Audio MP3 reproducible, sin artefactos |
| **Criterio de éxito** | Calidad de audio aceptable, nombre claro |

### CP-003: Texto dinámico con nombre del niño

| Dato | Valor |
|------|-------|
| **Objetivo** | Validar que el nombre del niño se inserta correctamente en el audio |
| **Precondición** | Nombre de prueba: "Lucía" |
| **Pasos** | 1. Formatear frase: "Hola Lucía, vamos a jugar"<br>2. Generar audio<br>3. Escuchar resultado |
| **Resultado esperado** | Nombre "Lucía" se pronuncia claramente |
| **Criterio de éxito** | Nombre identificable, pronunciación correcta |

### CP-004: Timeout de generación

| Dato | Valor |
|------|-------|
| **Objetivo** | Validar que el timeout se activa correctamente |
| **Precondición** | Chatterbox con respuesta lenta o no disponible |
| **Pasos** | 1. Enviar petición<br>2. Esperar timeout (5 seg)<br>3. Verificar fallback |
| **Resultado esperado** | Se entrega audio genérico tras timeout |
| **Criterio de éxito** | No hay bloqueo, audio genérico coherente |

### CP-005: Fallback con audio genérico

| Dato | Valor |
|------|-------|
| **Objetivo** | Validar que el audio genérico es apropiado para la animación |
| **Precondición** | Timeout activado |
| **Pasos** | 1. Verificar que el audio genérico corresponde a la animación<br>2. Validar que el niño no notaría la diferencia |
| **Resultado esperado** | Audio genérico coherente con la animación |
| **Criterio de éxito** | Audio natural, apropiado para la edad |

### CP-006: Dos modelos de voz

| Dato | Valor |
|------|-------|
| **Objetivo** | Validar que las dos voces (npc-voice y narrative-voice) suenan diferente |
| **Precondición** | Ambos modelos disponibles |
| **Pasos** | 1. Generar audio con npc-voice<br>2. Generar audio con narrative-voice<br>3. Comparar |
| **Resultado esperado** | Voces claramente diferenciadas |
| **Criterio de éxito** | Distinción auditiva clara entre NPC y narrador |

### CP-007: Manejo de errores de conexión

| Dato | Valor |
|------|-------|
| **Objetivo** | Validar que la API maneja errores sin crashes |
| **Precondición** | Chatterbox no disponible |
| **Pasos** | 1. Detener contenedor Chatterbox<br>2. Enviar petición<br>3. Verificar respuesta de error |
| **Resultado esperado** | Respuesta de error controlada, API sigue funcionando |
| **Criterio de éxito** | Sin crashes, error registrado |

## Procedimiento de prueba

### Preparación
1. Verificar que la API del TTS service está levantada
2. Verificar que Chatterbox está accesible
3. Preparar textos de prueba con nombres variados
4. Tener audios pregrabados genéricos disponibles

### Ejecución
1. Ejecutar cada caso de prueba secuencialmente
2. Documentar resultados inmediatamente
3. Tomar notas de cualquier comportamiento inesperado
4. Repetir pruebas fallidas hasta confirmar

### Documentación
1. Registrar resultado de cada caso (Pasa/Falla)
2. Adjuntar evidencias (logs, capturas)
3. Describir problemas encontrados
4. Proponer correcciones

## Métricas a monitorear

| Métrica | Objetivo | Alerta si |
|---------|----------|-----------|
| Tiempo de respuesta Chatterbox | < 5 segundos | > 5 segundos frecuentemente |
| Tasa de éxito de conversión | 100% | < 95% |
| Frecuencia de timeouts | < 5% | > 10% |
| Calidad de audio (subjetiva) | Aceptable | Nombre incomprensible |

## Plantilla de reporte

```markdown
## Reporte de Pruebas TTS - [Fecha]

### Resumen
- Total de pruebas: X
- Pasaron: X
- Fallaron: X

### Detalle
| Caso | Resultado | Observaciones |
|------|-----------|---------------|
| CP-001 | Pasa | ... |
| CP-002 | Pasa | ... |
| ... | ... | ... |

### Problemas encontrados
1. [Descripción del problema]

### Recomendaciones
1. [Acción sugerida]
```

## Dependencias

- **integracion-chatterbox**: Para validar la comunicación
- **conversion-audio**: Para validar la conversión de formato
- **timeout-fallback**: Para validar el timeout

## Notas

- Las pruebas son manuales, no automatizadas
- El objetivo es validar la experiencia del usuario final (niño)
- Los resultados deben documentarse para referencia futura
