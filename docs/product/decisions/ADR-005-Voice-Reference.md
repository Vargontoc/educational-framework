# ADR-005 — Voice Reference Generation: eSpeak NG as robot character voice designer

## Status

status: accepted
date: 2026-04-28


## Contexto

El avatar del proyecto es un robot kawaii — aspecto infantil y simpático — dirigido a niños de 3-8 años. La identidad sonora del personaje debe ser coherente con su identidad visual: una voz ligeramente sintética, aguda y con carácter robótico amigable.

XTTS v2 (ADR-007, FEAT-007) requiere un audio de referencia WAV de al menos 6 segundos para clonar la voz en cada síntesis. Sin ese audio de referencia, XTTS v2 usa speakers genéricos que no encajan con la identidad del personaje.

### El problema con las alternativas

**Grabar una voz humana** — ningún hablante humano captura de forma natural el carácter robótico del personaje. Intentar aproximarlo con modulación de voz humana produce resultados forzados e inconsistentes entre tonos emocionales.

**Usar speakers predefinidos de XTTS v2** — son voces humanas genéricas, incompatibles con la identidad visual del robot kawaii.

**Usar otro TTS neuronal** — los modelos neuronales están entrenados para sonar naturales y humanos, que es exactamente lo contrario de lo que necesita el personaje.

### Por qué eSpeak NG es la herramienta adecuada

eSpeak NG es un sintetizador de voz formante, no neuronal. Su naturaleza sintética, que sería un defecto en cualquier otro contexto, es aquí una ventaja directa: produce exactamente el sonido robótico característico que define al personaje. Tiene soporte completo de español peninsular, control total de pitch, velocidad y amplitud mediante parámetros numéricos, y corre completamente en local sin dependencias externas.

Los parámetros numéricos de eSpeak NG (`-p` pitch, `-s` velocidad, `-a` amplitud) permiten diseñar cada tono emocional como una variación del mismo robot, garantizando coherencia de identidad entre estados. Esto es imposible de conseguir con grabaciones humanas.

## Decisión

Se usa **eSpeak NG** para generar los audios WAV de referencia que XTTS v2 usa como base de clonación de voz.

El flujo es:

```
eSpeak NG (parámetros versionados) → WAV de referencia → XTTS v2 (voice cloning) → audio final
```

Cada tono emocional del personaje se define como un conjunto de parámetros eSpeak NG versionados en el repositorio. Los WAVs generados se commitean en `framework/agents/tts/references/` porque son pequeños, deterministas y reproducibles — si cambia un parámetro, se regenera el WAV y se reconstruye la caché de audio.

### Parámetros por tono

Los parámetros siguientes son el punto de partida. Se ajustarán durante la fase de validación con el usuario final antes de fijarlos como definitivos.

```bash
# Calmado — ages 3-4: lento, pitch neutro, pausas largas
espeak-ng -v es -p 55 -s 130 -a 100 "<texto_referencia>" -w references/calm.wav

# Alegre — ages 5-6: ritmo fluido, pitch ligeramente alto
espeak-ng -v es -p 70 -s 160 -a 100 "<texto_referencia>" -w references/joyful.wav

# Entusiasta — ages 7-8: rápido, pitch alto, más energía
espeak-ng -v es -p 75 -s 175 -a 120 "<texto_referencia>" -w references/enthusiastic.wav

# Juguetón — muletillas personaje (FEAT-005): dinámico, pitch variable
espeak-ng -v es -p 68 -s 165 -a 110 "<texto_referencia>" -w references/playful.wav

# Serio — safety override (FEAT-003): lento, pitch bajo, sin variaciones
espeak-ng -v es -p 45 -s 125 -a 100 "<texto_referencia>" -w references/serious.wav
```

Rango de parámetros de referencia:
- `-p` (pitch): 0–99, default 50. Valores altos para tonos infantiles y alegres, bajos para serio.
- `-s` (velocidad palabras/min): 80–350, default 175. Reducir para ages 3-4, aumentar para 7-8.
- `-a` (amplitud): 0–200, default 100. Aumentar ligeramente para entusiasta.

### Texto de referencia para generación

El texto usado para generar cada WAV de referencia debe ser fonéticamente diverso y representativo del vocabulario de la app. Se usará la siguiente frase estándar para todos los tonos, garantizando comparabilidad:

```
"Hola, soy Nubi, tu amigo explorador. ¿Sabes que hay cosas increíbles que descubrir?"
```

### Estructura de ficheros

```
framework/agents/tts/
  references/
    calm.wav            ← commiteado en git
    joyful.wav          ← commiteado en git
    enthusiastic.wav    ← commiteado en git
    playful.wav         ← commiteado en git
    serious.wav         ← commiteado en git
  scripts/
    generate_references.sh  ← script reproducible con los parámetros exactos
```

`generate_references.sh` es la fuente de verdad de los parámetros. Cualquier cambio en la voz del personaje se hace modificando este script y regenerando los WAVs.

### Integración con docker-compose

eSpeak NG no necesita un contenedor propio. Se ejecuta como herramienta de generación en el entorno de desarrollo, no en runtime. Los WAVs generados se montan en el contenedor `educational-coqui` como volumen read-only:

```yaml
educational-coqui:
  volumes:
    - coqui_models:/root/.local/share/tts
    - ./framework/agents/tts/references:/references:ro
```

## Consecuencias

### Positivas

- Identidad sonora coherente con el avatar visual del robot kawaii.
- Voz del personaje reproducible y versionada en git — cualquier miembro del equipo puede regenerarla con un script.
- Cada tono emocional es una variación paramétrica del mismo robot, garantizando coherencia entre estados.
- Sin dependencias externas ni costes adicionales — eSpeak NG es open source y corre en cualquier entorno.
- Los WAVs de referencia son pequeños (~200KB cada uno) y seguros para commitear.
- Cambiar la voz del personaje en el futuro es tan simple como ajustar parámetros en `generate_references.sh` y regenerar.

### Negativas / trade-offs

- La calidad final depende de cómo XTTS v2 clona una voz sintética — el resultado puede ser menos natural que clonar una voz humana. Aceptado: para un robot kawaii, "menos natural" es el objetivo.
- Los parámetros requieren validación manual con el usuario final antes de fijarse como definitivos. Esto es trabajo de ajuste fino, no de implementación.
- eSpeak NG debe estar instalado en el entorno de desarrollo para regenerar los WAVs. No es una dependencia de runtime.

## Relación con otras decisiones

- **ADR-005**: Los WAVs generados por eSpeak NG son los audios de referencia que el backend pasa como parámetro `voice` en las llamadas a Coqui TTS.
- **FEAT-007**: XTTS v2 usa estos WAVs para voice cloning en cada síntesis.
- **FEAT-003**: Cada tono emocional del contrato del agente (`calm`, `joyful`, `enthusiastic`, `serious`, `neutral`) tiene su WAV de referencia correspondiente.
- **FEAT-005**: El tono `playful` da voz a las muletillas del personaje manteniendo la identidad robótica.
- **FEAT-008**: Si se decide hacer fine-tuning, los WAVs de eSpeak NG pueden usarse como base para generar el dataset sintético de entrenamiento, eliminando la necesidad de grabaciones humanas.