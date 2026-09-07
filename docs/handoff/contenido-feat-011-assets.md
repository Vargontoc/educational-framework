# Handoff a contenido — FEAT-011: Especificaciones de assets

## Contexto

FEAT-011 adapta la experiencia jugable de My Friend Nubi a viewports horizontales (móviles y tablets). El canvas de referencia es **1280x720px (16:9)** y se escala con `Phaser.Scale.FIT`, lo que garantiza que todo el canvas es visible sin recortes en cualquier dispositivo.

Este documento proporciona las especificaciones necesarias para que el equipo de contenido y diseño proporcione los assets visuales.

## Canvas y zonas

```
┌──────────────────────────────────────────────────────────────────┐
│  CANVAS 1280x720 (todo visible, sin recortes)                    │
│                                                                  │
│  ┌─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐    │
│      Zona segura superior (Y: 60–160)                        │    │
│  │  ┌──────────────────────────────────────────────────┐   │    │
│      │  ZONA ESENCIAL (1080px centrales)               │   │    │
│      │  Contenido principal del juego                  │   │    │
│      │  X: [100, 1180]  |  Y: [160, 560]              │   │    │
│  │  └──────────────────────────────────────────────────┘   │    │
│      Zona segura inferior (Y: 560–660)                       │    │
│  └─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘    │
│                                                                  │
│  ZONA SEGURA (elementos críticos):                               │
│    X: [160, 1120]  →  960px centrales (75% del ancho)           │
│    Y: [60, 660]    →  600px centrales (83% del alto)            │
└──────────────────────────────────────────────────────────────────┘
```

**Regla general:** Todo contenido esencial debe caber en los **1080px centrales** del canvas. Los elementos interactivos deben estar dentro de la **zona segura**.

## Especificaciones de assets

### Fondos de escena

| Propiedad | Valor |
|-----------|-------|
| Dimensiones | **1280x720px** |
| Zona | Canvas completo |
| Formato | PNG o WebP |
| Notas | Debe cubrir todo el canvas. Los bordes laterales pueden quedar como márgenes decorativos en tablets 4:3. |

### Nubi (sprite / placeholder)

| Propiedad | Valor |
|-----------|-------|
| Dimensiones máximas | **200x200px** |
| Zona | Zona esencial (X: [100, 1180], Y: [160, 560]) |
| Formato | PNG con transparencia |
| Notas | Posición recomendada: centrado o ligeramente bajo el centro del canvas. |

### Icono de giro (OrientationRequiredScene)

| Propiedad | Valor |
|-----------|-------|
| Dimensiones | **120x120px** |
| Zona | Centro del canvas: **(640, 360)** |
| Formato | PNG con transparencia o sprite sheet |
| Posición | Centrada horizontal y verticalmente en el canvas |

**Descripción del asset:** Representación visual de un dispositivo siendo girado a horizontal. Debe comprenderse sin necesidad de lectura. Estilo amable, coherente con la estética de Nubi.

**Alternativa con sprite sheet:** Si se proporciona como animación, indicar número de frames y dimensiones de cada frame.

### Elementos interactivos

| Propiedad | Valor |
|-----------|-------|
| Dimensiones mínimas | **48x48px** (objetivo táctil) |
| Zona | Zona segura (X: [160, 1120], Y: [60, 660]) |
| Formato | PNG con transparencia |
| Notas | Deben ser fácilmente tocables en pantallas táctiles. Tamaño mínimo 48x48 para accesibilidad. |

### Textos infantiles

| Propiedad | Valor |
|-----------|-------|
| Dimensiones | Variable |
| Zona | Zona esencial |
| Formato | Renderizado por Phaser Text (no como imagen) |
| Notas | Debe ser legible a 375px de ancho de viewport. Tamaño mínimo recomendado: 24px en canvas. |

## OrientationRequiredScene — Especificaciones completas

Esta escena se muestra cuando el dispositivo está en orientación vertical (portrait). Su propósito es indicar al usuario que gire el dispositivo a horizontal.

### Requisitos visuales

| Elemento | Especificación |
|----------|----------------|
| **Fondo** | Color plano o textura suave. Coherente con la paleta existente (`#028af8` o degradado suave). No debe distraer. |
| **Icono central** | Representación visual de un dispositivo girando. 120x120px. Centrada en (640, 360). Debe comprenderse sin lectura. |
| **Animación del icono** | Rotación suave 360° en 2 segundos, en loop. Si el usuario tiene activado `prefers-reduced-motion`, el icono debe mostrarse estático. |
| **Texto de apoyo** | 2-4 palabras, amable, no evaluativo. Ejemplo: "Gira tu tablet". Posición: debajo del icono, centrado. |

### No incluir

- Temporizadores o cuentas atrás
- Barras de progreso
- Mensajes de error
- Nombre del niño ni datos de sesión
- Elementos que induzcan prisa o ansiedad

### Estado actual (placeholders)

Actualmente la escena usa placeholders para el icono y el texto. El equipo de contenido debe proporcionar:

1. **Asset visual del icono de giro** (PNG 120x120 o sprite sheet)
2. **Texto i18n de indicación de giro** (2-4 palabras en español)

## Viewports objetivo

| Dispositivo | Resolución landscape | Comportamiento |
|-------------|---------------------|----------------|
| Móvil estándar | 667x375 (~16:9) | Canvas ocupa toda la pantalla, sin márgenes |
| Tableta estándar | 1024x768 (~4:3) | Márgenes decorativos arriba/abajo (~96px) |
| Tableta 16:10 | 1280x800 | Márgenes mínimos arriba/abajo |

**Nota:** Los márgenes visibles en tablets 4:3 son del color de fondo del contenedor (`#028af8`). No requieren assets decorativos adicionales, aunque se pueden proporcionar si se desea.

## Formatos aceptados

| Tipo | Formatos |
|------|----------|
| Imágenes estáticas | PNG (preferido), WebP |
| Sprites animados | Sprite sheet (PNG) con configuración de frames |
| Textos | No como imagen; renderizados por Phaser Text con fuentes del design system |

## Entrega de assets

Los assets deben entregarse en la carpeta del proyecto que indique el equipo de frontend, preferiblemente en:

```
framework/frontend/app/public/assets/game/
```

Nombrar los archivos con kebab-case y prefijo descriptivo:

- `fondo-escena-[nombre].png`
- `nubi-[accion].png`
- `icono-giro.png`
- `btn-[accion].png`

## Checklist de contenido pendiente

- [ ] Asset visual de Nubi girando dispositivo (reemplazar placeholder de OrientationRequiredScene)
- [ ] Texto i18n de indicación de giro (2-4 palabras, amable, no evaluativo)
- [ ] Fondos de escena definitivos (actualmente usando colores planos / placeholders)
- [ ] Assets decorativos de relleno para márgenes (opcional; actualmente color plano `#028af8`)
- [ ] Sprites de Nubi para futuras escenas (WorldMapScene, minijuegos)

## Referencias

- Convención de zona esencial: `docs/frontend/convencion-zona-esencial.md`
- Sprint de implementación: `docs/sprints/frontend/SPRINT-058-validacion-cross-viewport-documentacion-contenido.md`
- Feature: FEAT-011
