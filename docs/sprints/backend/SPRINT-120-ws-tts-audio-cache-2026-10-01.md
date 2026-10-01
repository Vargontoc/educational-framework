# SPRINT-120: Rendimiento WebSocket de juego - Audio de Nubi (TTS y caché)

## Objetivo
Que el audio de Nubi esté listo cuando el niño lo necesita, sin bloquear el juego ni fallar silenciosamente por la caché.

## Contexto
Hallazgos de la auditoría 2026-10-01:
- Un fallo de caché devuelve sin audio y lanza la síntesis en segundo plano: la primera petición de cada texto sale sin audio.
- `AudioCacheStorage.put` lista y ordena el directorio en cada escritura (`enforceDiskCapacity`).
- La ruta de caché se cablea a `classpath:/stories` (no escribible en un jar empaquetado).
- La clave usa `text.hashCode()` + parámetros de tono (riesgo de colisión: audio equivocado).
- `ChatterboxAdapter` hace `GET /ping` antes de cada síntesis, usa `SimpleClientHttpRequestFactory` (sin pool) y timeouts de 20 s.
- El nombre de fichero `.mp3` no coincide con el WAV solicitado.
- El audio viaja en un único frame binario.

## Relación con SPRINT-116
SPRINT-116 garantiza que ningún mensaje espera al TTS (se envía el audio cacheado si existe y la síntesis va en paralelo). Este sprint reduce los fallos de caché y endurece la caché y el cliente TTS.

## Tareas
- [ ] Clave de caché con hash fuerte (SHA-256 de texto normalizado + tono + voz); migración de la caché existente (invalidar las entradas antiguas)
- [ ] Ruta de caché en disco real y configurable (`application.yml`), con comprobación de escritura al arrancar y fallo claro si no es válida
- [ ] `enforceDiskCapacity` periódico (tarea programada) en lugar de por escritura
- [ ] Pre-calentamiento: sintetizar en segundo plano los textos fijos (bienvenida, despedida, transición de bioma, prompts de ronda frecuentes) al arrancar y al cambiar el catálogo, con límite de concurrencia
- [ ] Cliente HTTP con pool y reutilización de conexiones; eliminar o cachear unos segundos el resultado de `/ping`
- [ ] Timeouts de conexión y lectura acordes al juego del niño (p. ej. conexión 2-3 s, lectura configurable) con degradación limpia a "sin audio" y mensaje WS sin audio como hoy
- [ ] Corregir extensión/tipo del fichero de caché según el formato real
- [ ] Opcional según medición: enviar audio grande en varios frames o comprimir; solo si el tamaño medido lo justifica y sin cambiar el protocolo de cabecera (`audioId` + bytes) sin acuerdo con frontend
- [ ] Métricas de acierto/fallo y de tiempo de síntesis (SPRINT-114)

### Tests
- [ ] Dos textos con el mismo `hashCode` distinto no comparten audio
- [ ] La ruta de caché inválida produce error de arranque explícito
- [ ] Caída del servicio TTS: el juego continúa sin audio y el tiempo de `game_ready`/`game_action` no sube del timeout configurado
- [ ] Pre-calentamiento: tras arrancar, los textos fijos se sirven desde caché
- [ ] Capacidad en disco respetada sin listar el directorio en cada escritura

### Pruebas manuales
- [ ] Arrancar el backend en frío y jugar la primera partida: Nubi habla en bienvenida y primera ronda
- [ ] Parar el servicio TTS durante una partida: el niño no ve errores y el juego sigue
- [ ] Reiniciar el backend y comprobar que la caché en disco se reutiliza

## Criterios de Aceptación
1. La bienvenida y las rondas habituales tienen audio desde la primera partida tras el arranque
2. Ninguna colisión de clave de caché es posible en la práctica
3. Un fallo del TTS nunca bloquea ni degrada la latencia de acciones por encima del timeout configurado
4. La caché funciona en el jar empaquetado
5. Sin cambios en el protocolo WS del audio

## Contratos y dependencias
- Servicio TTS (Chatterbox, ADR-013): sin cambios en su API; cualquier ajuste del servicio es handoff a `analyser-tts`.
- Infraestructura: volumen persistente para la caché de audio (handoff).

## Riesgos
- Pre-calentar demasiado puede saturar el TTS en el arranque: concurrencia limitada.
- Cambiar la clave invalida la caché existente: asumido, se regenera con el pre-calentamiento.

## Dependencias
- SPRINT-114 completado; independiente de 116-119.

## Estimación
- **Tamaño:** M | **Riesgo:** Medio
