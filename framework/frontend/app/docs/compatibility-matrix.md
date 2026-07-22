# Matriz de Compatibilidad - My Friend Nubi Frontend

## Dispositivos Objetivo

### Primario (Pruebas Obligatorias)

| Dispositivo | SO | Navegador | Resolución | Estado |
|-------------|-----|-----------|------------|--------|
| Samsung Galaxy A15 | Android 16 | Chrome 143.0.7499.193 | 1080x2340 | ✅ Obligatorio |

### Secundario (Pruebas Complementarias)

| Dispositivo | SO | Navegador | Resolución | Estado |
|-------------|-----|-----------|------------|--------|
| Samsung Galaxy Tab S4 | Android 10+ | Chrome | 2560x1600 | 🔄 Emulado |
| Google Pixel 8 | Android 14+ | Chrome | 1080x2400 | 🔄 Emulado |
| Samsung Galaxy S20 | Android 12+ | Chrome | 1440x3200 | 🔄 Emulado |

### Fuera de Alcance

| Plataforma | Razón |
|------------|-------|
| iOS / iPadOS | Restricciones de Screen Orientation API y comportamiento PWA divergente |
| APK nativo | No es objetivo en esta versión |
| Navegadores distintos a Chrome | Android/Chrome es la base del piloto |

## Requisitos de Navegador

### APIs Utilizadas

| API | Soporte Mínimo | Fallback |
|-----|----------------|----------|
| WebSocket | Chrome 5+ | Ninguno (requerido) |
| Fetch API | Chrome 42+ | Ninguno (requerido) |
| Screen Orientation API | Chrome 38+ | Detección manual via resize |
| Service Worker | Chrome 45+ | Sin caché offline |
| Web App Manifest | Chrome 39+ | Sin PWA |
| sessionStorage | Chrome 4+ | Estado no persiste entre recargas |
| CSS Transform | Chrome 4+ | Sin escalado en vertical |

### Versiones Mínimas

- **Chrome**: 143.0.7499.193 (versión del piloto)
- **Android**: 16 (versión del piloto)

## Orientación y PWA

### Estrategia de Orientación

1. **PWA Manifest** (`orientation: landscape`)
   - Fuerza orientación horizontal a nivel de SO en Android
   - Efectivo en la mayoría de dispositivos Android

2. **Screen Orientation API** (complementaria)
   - Se invoca programáticamente cuando está disponible
   - Refuerza el manifiesto en contextos fullscreen

3. **Escalado CSS** (fallback)
   - Cuando el dispositivo está en vertical física, el contenido se escala
   - Mantiene la composición horizontal sin mostrar mensaje de giro
   - Puede reducir objetivos táctiles (ver riesgos)

### PWA

- **Instalación**: Opcional, solo para adultos
- **Prompt automático**: Desactivado
- **Acceso**: Via URL en el navegador (no requiere instalación)
- **Caché**: Service Worker con estrategia stale-while-revalidate
- **Offline**: Limitado (solo recursos estáticos, API requiere conexión)

## Riesgos Conocidos

### Orientación Vertical Física

- **Problema**: El escalado CSS puede reducir objetivos táctiles por debajo del umbral aceptable para 3-4 años
- **Mitigación**: Monitorear en pruebas reales; ajustar factor de escala si es necesario
- **Dispositivos afectados**: Todos cuando se sostienen en vertical

### Bloqueo de Orientación

- **Limitación**: La aplicación web no puede garantizar bloqueo total de orientación en todos los dispositivos Android
- **Impacto**: El usuario puede rotar manualmente a vertical
- **Mitigación**: Escalado CSS como fallback

### Caché PWA

- **Limitación**: URL HTTPS final necesaria para validar comportamiento de caché en entorno de entrega
- **Impacto**: No se puede validar completamente en desarrollo local
- **Mitigación**: Pruebas en staging con HTTPS

## Estrategia de Pruebas

### Pruebas Obligatorias

1. **Samsung Galaxy A15 físico**
   - Pruebas de orientación (horizontal/vertical)
   - Pruebas de PWA (instalación, caché)
   - Pruebas de WebSocket (conexión, reconexión)
   - Pruebas de rendimiento

### Pruebas Complementarias

1. **Emuladores**
   - Galaxy Tab S4 (tablet)
   - Pixel 8 (móvil)
   - Galaxy S20 (móvil)

2. **Escenarios**
   - Cambio de orientación durante uso
   - Aplicación en segundo plano y retorno
   - Pérdida de conexión y reconexión
   - Recarga de página

### Criterios de Aceptación

- [ ] Renderizado horizontal en orientación física vertical
- [ ] No se muestran mensajes de giro al usuario
- [ ] Estado preservado ante giro y segundo plano
- [ ] PWA instalable desde menú del navegador
- [ ] WebSocket reconecta automáticamente tras pérdida de conexión
- [ ] Guards de navegación protegen rutas correctamente

## Notas

- iOS/iPadOS están explícitamente fuera del alcance para esta versión
- Android/Chrome es la única plataforma soportada
- Las pruebas en emuladores complementan pero no sustituyen las pruebas en dispositivo físico
- La URL HTTPS final es necesaria para validar PWA completamente
