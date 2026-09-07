# Convención de zona esencial — Phaser canvas 1280x720

## Canvas de referencia

- **Resolución:** 1280x720px (16:9)
- **Modo de escala:** `Phaser.Scale.FIT` con `CENTER_BOTH`
- **Color de fondo Phaser:** `#028af8`

## Comportamiento de FIT

Con `Phaser.Scale.FIT`, el canvas completo de 1280x720 se escala proporcionalmente para caber en el viewport. **Todo el canvas es visible en todos los viewports; no hay recortes.**

| Viewport | Resultado |
|----------|-----------|
| 667x375 (móvil 16:9) | Canvas ocupa toda la pantalla, sin márgenes |
| 1024x768 (tableta 4:3) | Márgenes decorativos arriba/abajo (~96px cada uno) |
| 1280x800 (tableta 16:10) | Márgenes mínimos arriba/abajo |

## Diagrama de zonas

```
┌──────────────────────────────────────────────────────────────────┐
│                    CANVAS 1280x720 (todo visible)                │
│                                                                  │
│  Y=0 ┌─────────────────────────────────────────────────────┐     │
│      │  Borde superior del canvas                           │     │
│      │  ┌─────────────────────────────────────────────┐     │     │
│      │  │  Zona segura superior (Y: 60–160)            │     │     │
│      │  │  Indicadores, HUD, iconos de estado          │     │     │
│      │  ├─────────────────────────────────────────────┤     │     │
│      │  │                                             │     │     │
│      │  │       ZONA ESENCIAL (1080px centrales)       │     │     │
│      │  │                                             │     │     │
│      │  │   Nubi, elementos interactivos, controles,  │     │     │
│      │  │   objetivos táctiles, contenido de juego     │     │     │
│      │  │                                             │     │     │
│      │  │   X: [100, 1180]  |  Y: [160, 560]         │     │     │
│      │  │                                             │     │     │
│      │  ├─────────────────────────────────────────────┤     │     │
│      │  │  Zona segura inferior (Y: 560–660)           │     │     │
│      │  │  Controles, botones de acción                │     │     │
│      │  └─────────────────────────────────────────────┘     │     │
│      │  Borde inferior del canvas                           │     │
│  Y=720└─────────────────────────────────────────────────────┘     │
│                                                                  │
│  X=0                                                         X=1280│
└──────────────────────────────────────────────────────────────────┘

ZONA SEGURA PARA ELEMENTOS CRÍTICOS:
  X: [160, 1120]  →  960px centrales (75% del ancho)
  Y: [60, 660]    →  600px centrales (83% del alto)
```

## Definiciones

| Zona | Extensión | Propósito |
|------|-----------|-----------|
| Canvas completo | 1280x720 | Área total renderizada por Phaser. Visible al 100% con FIT. |
| Zona esencial | 1080px centrales (X: [100, 1180]) | Contenido principal del juego. Garantiza buena presentación en todos los aspect ratios. |
| Zona segura (elementos críticos) | X: [160, 1120], Y: [60, 660] | Elementos que deben ser legibles y alcanzables sin ambigüedad en cualquier viewport. |
| Márgenes decorativos | Fuera del canvas (viewport 4:3) | Espacio entre el canvas escalado y los bordes del viewport. Color `#028af8`. |

## Reglas de diseño

1. **Todo contenido esencial debe caber en los 1080px centrales** del canvas (84% del ancho).
2. **Los elementos interactivos (botones, objetivos táctiles) deben estar dentro de la zona segura** (X: [160, 1120], Y: [60, 660]).
3. **Los textos infantiles deben ser legibles** a 375px de ancho de viewport.
4. **Los márgenes decorativos** (visibles en tablets 4:3) son parte del diseño; el color de fondo del contenedor `.game-view` es `#028af8`, coherente con el fondo de Phaser.

## Es una guía de diseño, no un mecanismo técnico

- Con `Phaser.Scale.FIT`, **todo el canvas 1280x720 es siempre visible**. No existe recorte automático.
- La zona esencial es una **convención de posicionamiento** para garantizar que el contenido crítico esté bien centrado y sea usable en todos los aspect ratios.
- No hay ningún sistema de runtime que recorte o limite el contenido a estas zonas. El desarrollador posiciona los elementos siguiendo esta guía.

## Cómo aplicar en futuras escenas

1. Al crear una nueva escena, posiciona los elementos esenciales dentro de los 1080px centrales (X: [100, 1180]).
2. Los elementos interactivos (botones, iconos tocables) deben estar en la zona segura (X: [160, 1120], Y: [60, 660]).
3. Los fondos de escena deben cubrir el canvas completo (1280x720).
4. Los elementos puramente decorativos pueden extenderse hasta los bordes del canvas.
5. Valida siempre en viewport 1024x768 (tableta 4:3) para comprobar que los márgenes no ocultan contenido.
6. Usa las coordenadas del centro del canvas (640, 360) como referencia para elementos centrados.

## Ejemplo de posicionamiento

```typescript
// Elemento centrado en la zona esencial
this.add.image(640, 360, 'personaje')

// Botón de acción en zona segura inferior
this.add.image(640, 620, 'boton-accion')

// Indicador de estado en zona segura superior
this.add.image(640, 100, 'indicador-estrellas')

// Fondo que cubre todo el canvas
this.add.image(640, 360, 'fondo-escena')
```
