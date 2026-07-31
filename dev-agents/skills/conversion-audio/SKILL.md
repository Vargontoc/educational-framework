---
name: conversion-audio
description: "Usar para convertir audio de WAV (salida de Chatterbox) a MP3 (para transmisión vía WebSocket) en la API del TTS service."
---

# Conversión de Audio WAV a MP3

## Propósito

Transformar el audio generado por Chatterbox (formato WAV) a formato MP3 para una transmisión eficiente vía WebSocket desde la API del TTS service hacia la capa backend de la aplicación.

## Requisito previo

La API del TTS service debe haber recibido correctamente el audio WAV de Chatterbox.

## Scope exclusivo

Este skill gestiona ÚNICAMENTE la conversión de formato:
- Conversión de WAV a MP3
- Configuración de calidad de conversión
- Optimización para transmisión

**No gestiona:**
- Comunicación con Chatterbox (ver skill `integracion-chatterbox`)
- Almacenamiento del audio convertido
- Transmisión vía WebSocket (eso es responsabilidad de la capa backend)
- Reproducción en frontend

## Flujo de conversión

```
Audio WAV (de Chatterbox) → [Conversión] → Audio MP3 → [Envío directo]
```

## Elementos a gestionar

### 1. Configuración de conversión
- Calidad de bitrate MP3 (ej: 128kbps, 192kbps, 256kbps)
- Frecuencia de muestreo (ej: 44.1kHz)
- Canales (mono/estéreo)

### 2. Procesamiento de conversión
- Lectura del buffer WAV
- Codificación a MP3
- Generación del buffer MP3

### 3. Optimización para WebSocket
- Tamaño de chunks para transmisión
- Buffer de streaming (si aplica)
- Headers de Content-Type para el cliente

## Parámetros de calidad recomendados

| Parámetro | Valor recomendado | Justificación |
|-----------|-------------------|---------------|
| Bitrate | 128-192 kbps | Buen equilibrio calidad/tamaño para voz |
| Frecuencia | 44.1kHz | Estándar para audio de voz |
| Canales | Mono | Voz hablada, sin necesidad de estéreo |

## Criterios de validación

- [ ] El audio WAV se convierte a MP3 correctamente
- [ ] El tamaño del MP3 es menor que el WAV original
- [ ] La calidad de audio es aceptable (el nombre del niño se pronuncia claramente)
- [ ] La conversión no introduce artefactos audibles
- [ ] El buffer MP3 se genera correctamente para transmisión

## Preguntas para el agente técnico

1. ¿Qué librería de conversión usar (ffmpeg, lame, etc.)?
2. ¿Qué bitrate óptimo para voz infantil?
3. ¿Se necesita streaming en tiempo real o buffer completo?
4. ¿Cómo validar la calidad de audio convertido?

## Dependencias

- **integracion-chatterbox**: Proporciona el audio WAV de entrada
- **Backend Agent**: Usa el MP3 resultante para enviarlo al cliente

## Notas

- La conversión debe ser rápida para no añadir latencia perceptible
- El audio MP3 se envía directamente, no se almacena en el TTS service
- La calidad debe permitir que el nombre del niño se pronuncie claramente
