---
name: timeout-fallback
description: "Usar para gestionar el timeout de generación de audio dinámico y el fallback a audio pregrabado genérico en la API del TTS service."
---

# Timeout y Fallback de Generación de Audio

## Propósito

Garantizar que la generación de audio personalizado (con nombre del niño) no bloquee la experiencia, implementando un timeout configurable y un mecanismo de fallback a audio pregrabado genérico.

## Requisito previo

La API del TTS service debe tener acceso a los audios pregrabados genéricos como fallback.

## Scope exclusivo

Este skill gestiona ÚNICAMENTE:
- Timer de timeout para generación de audio
- Selección de audio pregrabado genérico como fallback
- Logging del evento de timeout

**No gestiona:**
- Comunicación con Chatterbox (ver skill `integracion-chatterbox`)
- Almacenamiento de audios generados
- Reproducción de audio en frontend

## Flujo de timeout

```
Petición de audio con nombre
        ↓
Iniciar timer (timeout configurable)
        ↓
    ¿Chatterbox responde a tiempo?
        ↓ SI                    ↓ NO
Audio personalizado        Seleccionar fallback
        ↓                    ↓
Enviar resultado          Enviar audio genérico
        ↓                    ↓
    Logging del evento
```

## Elementos a gestionar

### 1. Configuración de timeout
- Tiempo máximo de espera (recomendado: 5 segundos)
- Configuración por tipo de audio (npc-voice vs narrative-voice)
- Posibilidad de ajustar sin reiniciar el servicio

### 2. Mecanismo de fallback
- Selección del audio pregrabado genérico apropiado
- Mapeo de animaciones a audios genéricos
- Garantizar coherencia con la animación del NPC

### 3. Logging
- Registrar cuando se activa el fallback
- Tiempo de respuesta de Chatterbox
- Tipo de audio solicitado vs entregado

## Tiempos de timeout recomendados

| Escenario | Timeout | Justificación |
|-----------|---------|---------------|
| Audio dinámico (nombre) | 5 segundos | Generación bajo demanda, no durante juego |
| Audio predefinido | Inmediato | Ya generado, sin llamada a Chatterbox |

## Audios de fallback

| Animación | Audio genérico sugerido |
|-----------|------------------------|
| Saludo | "Hola, vamos a jugar" |
| Despedida | "Hasta luego, nos vemos pronto" |
| Celebración | "¡Muy bien, lo lograste!" |
| Pista | "Mira aquí, intenta esto" |
| Transición | "Vamos a继续" |

## Criterios de validación

- [ ] El timeout se activa correctamente cuando Chatterbox no responde a tiempo
- [ ] Se selecciona el audio genérico apropiado según la animación
- [ ] El fallback no bloquea la API
- [ ] Se registra el evento de timeout para monitoreo
- [ ] El usuario final (niño) no percibe la activación del fallback

## Preguntas para el agente técnico

1. ¿Cómo se implementa el timer de timeout en el lenguaje elegido?
2. ¿Dónde se almacenan los audios pregrabados genéricos?
3. ¿Cómo se mapea cada tipo de animación a su audio genérico?
4. ¿Qué métricas registrar para monitorear la frecuencia de timeouts?

## Dependencias

- **integracion-chatterbox**: Proporciona la respuesta de Chatterbox (o timeout)
- **Content**: Define los audios pregrabados genéricos disponibles

## Notas

- El fallback es una medida de emergencia, no el flujo normal
- Si los timeouts son frecuentes, se debe investigar Chatterbox
- El audio genérico debe ser coherente con la animación del NPC
- El niño no debe notar que se usó un audio genérico
