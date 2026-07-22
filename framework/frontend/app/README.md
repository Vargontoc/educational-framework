# My Friend Nubi - Frontend

Aplicación web educativa para niños de 3-4 años, diseñada para tabletas y móviles.

## Tecnologías

- **Vue 3** - Framework reactivo
- **TypeScript** - Tipado estático
- **Vite** - Build tool y dev server
- **Pinia** - Gestión de estado
- **Vue Router 4** - Enrutamiento
- **vue-i18n** - Internacionalización
- **Phaser** - Motor de juegos (instalado, sin configurar)

## Estructura de Directorios

```
framework/frontend/app/
├── public/                 # Archivos estáticos
│   ├── icons/             # Iconos PWA
│   │   ├── icon-192x192.svg
│   │   └── icon-512x512.svg
│   └── manifest.webmanifest  # PWA Manifest
├── src/
│   ├── assets/            # Recursos estáticos (imágenes, fuentes)
│   ├── components/        # Componentes reutilizables
│   ├── i18n/              # Internacionalización
│   │   ├── index.ts       # Configuración vue-i18n
│   │   └── locales/       # Archivos de traducción
│   │       └── es.ts      # Español
│   ├── router/            # Configuración de rutas
│   │   └── index.ts       # Vue Router
│   ├── services/          # Servicios (API, WebSocket)
│   ├── stores/            # Stores de Pinia
│   │   ├── session.ts     # Estado de sesión familiar
│   │   ├── ui.ts          # Estado UI (modales, loading)
│   │   └── ws.ts          # Estado WebSocket
│   ├── views/             # Vistas de la aplicación
│   │   ├── HomeView.vue
│   │   ├── PanelControlView.vue
│   │   ├── GameView.vue
│   │   ├── DocumentationView.vue
│   │   └── NotFoundView.vue
│   ├── App.vue            # Componente raíz
│   ├── main.ts            # Punto de entrada
│   ├── style.css          # Estilos globales
│   └── vite-env.d.ts      # Declaraciones de tipos
├── .env                   # Variables de entorno comunes
├── .env.development       # Variables de entorno desarrollo
├── .env.production        # Variables de entorno producción
├── .dockerignore          # Archivos excluidos de Docker
├── Dockerfile             # Configuración Docker multi-stage
├── nginx.conf             # Configuración nginx para producción
├── package.json           # Dependencias y scripts
├── tsconfig.json          # Configuración TypeScript
├── tsconfig.node.json     # Configuración TypeScript para Node
└── vite.config.ts         # Configuración Vite
```

## Rutas

| Ruta | Vista | Protección |
|------|-------|------------|
| `/` | Home | Pública |
| `/panel` | PanelControl | Requiere autenticación (PIN validado) |
| `/game/:childId` | GameView | Requiere sesión de niño activa |
| `/docs` | Documentation | Pública |

### Guards de Navegación

- **requiresAuth**: Protege `/panel`, requiere `isAuthenticated` en sessionStore
- **requiresChildSession**: Protege `/game/:childId`, requiere `selectedChildId` en sessionStore
- **Navegación**: Todas las navegaciones usan `router.replace()` para eliminar historial
- **Recarga**: El estado de sesión persiste en sessionStorage, permitiendo continuar en la misma ruta después de recargas accidentales

## Instalación

```bash
# Instalar dependencias
npm install

# Desarrollo
npm run dev

# Build de producción
npm run build

# Preview de producción
npm run preview
```

## Docker

### Construir imagen

```bash
docker build -t nubi-frontend .
```

### Ejecutar contenedor

```bash
docker run -p 80:80 nubi-frontend
```

La aplicación estará disponible en `http://localhost`

### Variables de entorno

Las variables de entorno se definen en archivos `.env`:

- `VITE_API_BASE_URL` - URL base de la API REST
- `VITE_WS_BASE_URL` - URL base del WebSocket
- `VITE_APP_MODE` - Modo de la aplicación (development/production)

## Servicios

### Cliente API (src/services/api.ts)

Cliente HTTP centralizado para comunicación REST con el backend.

- **Base URL**: Configurada desde `VITE_API_BASE_URL`
- **Métodos**: `get()`, `post()`, `put()`, `delete()`
- **Errores**: Manejo centralizado con `ApiError`
- **Headers**: Content-Type JSON por defecto

**Uso:**
```typescript
import apiClient from './services/api'

// GET
const data = await apiClient.get<ResponseType>('/api/v1/endpoint')

// POST
const result = await apiClient.post<ResponseType>('/api/v1/endpoint', payload)
```

### Cliente WebSocket (src/services/websocket.ts)

Cliente WebSocket con reconexión exponencial según ADR-010.

- **Canales**: GameChannel (`/ws/game`) y ParentChannel (`/ws/parent`)
- **Reconexión**: Backoff exponencial con jitter (1s base, 30s máximo)
- **Estado**: Reactivo, expuesto via `useWSStore`
- **Sin cola offline**: Eventos durante desconexión se descartan

**Uso:**
```typescript
import { createGameWebSocket } from './services/websocket'

const ws = createGameWebSocket({
  onMessage: (data) => console.log(data),
  onStatusChange: (status) => console.log(status)
})

ws.connect()
ws.send({ type: 'heartbeat' })
ws.disconnect()
```

**Fábricas preconfiguradas:**
- `createGameWebSocket()` - Canal de juego
- `createParentWebSocket()` - Canal parental

## Orientación y Renderizado

### Estrategia

Según ADR-010 y SPRINT-002, la aplicación mantiene composición horizontal permanente:

1. **PWA Manifest** (`orientation: landscape`) - Estrategia primaria
2. **Screen Orientation API** - Complementaria cuando está disponible
3. **Escalado CSS** - Fallback para orientación vertical física

### Componente OrientationManager

`src/components/OrientationManager.vue` gestiona la orientación:

- Detecta orientación del dispositivo
- Aplica escalado CSS proporcional en vertical
- No muestra mensajes de "gire el dispositivo"
- Preserva estado ante giro y segundo plano

### Preservación de Estado

- **sessionStorage**: Estado de sesión persiste entre recargas
- **visibilitychange**: Detecta segundo plano/retorno
- **WebSocket**: Reconexión automática tras pérdida de conexión

## Configuración PWA

### Manifest

- **Display**: standalone (oculta barra del navegador)
- **Orientación**: landscape (forzada)
- **Iconos**: SVG placeholders (192x192, 512x512)
- **Idioma**: es (español)
- **Categorías**: education, family

### Service Worker

- **Caché**: stale-while-revalidate para recursos estáticos
- **API calls**: network-first con fallback offline
- **Actualización**: Automática cuando hay nueva versión
- **Registro**: En `main.ts` solo si el navegador lo soporta

### Instalación

- **Opcional**: Solo para adultos, no promocionada en flujo infantil
- **Sin prompt automático**: Instalación manual desde menú del navegador
- **Acceso**: Via URL en el navegador (no requiere instalación)

## Stores de Pinia

### useSessionStore

Estado de la sesión familiar con persistencia en sessionStorage:
- `familyId`: Identificador de familia activa
- `selectedChildId`: Identificador del niño seleccionado
- `isAuthenticated`: Estado de autenticación (PIN validado)
- `setSession()`: Establecer sesión familiar
- `selectChild()`: Seleccionar niño para juego
- `logout()`: Cerrar sesión y limpiar estado

### useWSStore

Estado de conexión WebSocket:
- `gameChannelStatus`: Estado del canal de juego
- `parentChannelStatus`: Estado del canal parental
- `activeChannel`: Canal actualmente en uso
- `isConnected()`: Verificar si algún canal está conectado
- `isReconnecting()`: Verificar si algún canal está reconectando

### useUIStore

Estado de UI:
- `isLoading`: Pantalla de carga activa
- `errorMessage`: Mensaje de error actual
- `modalOpen`: Modal abierto

## Internacionalización

- **Idioma activo**: Español (es)
- **Configuración**: vue-i18n v9
- **Archivos de traducción**: `src/i18n/locales/es.ts`

Todos los textos visibles deben pasar por i18n. No se permiten literales en templates.

## Dispositivos objetivo

- **Android**: Samsung Galaxy A15 (Chrome)
- **PC**: Navegadores modernos

iOS está fuera del alcance para esta fase.

## Referencias

- [ADR-010 Frontend Layer Architecture](../../../docs/product/decisions/ADR-010-Frontend-layer.md)
- [OpenAPI Contract](../../../docs/contracts/api/openapi.json)
- [WebSocket Contract](../../../docs/contracts/api/websocket.json)
- [Matriz de Compatibilidad](./docs/compatibility-matrix.md)
- [Estrategia de Pruebas](./docs/testing-strategy.md)
- [Vue 3](https://vuejs.org)
- [Pinia](https://pinia.vuejs.org)
- [Vue Router 4](https://router.vuejs.org)
- [vue-i18n v9](https://vue-i18n.intlify.dev)
- [Vite](https://vitejs.dev)
- [Phaser](https://phaser.io)

## Notas de implementación

- Frontend consume exclusivamente backend (nunca TTS ni agents directamente)
- Navegación usa `router.replace()` para eliminar historial
- Recarga de página siempre redirige a Home
- Phaser instalado pero sin configurar (se implementará en sprints posteriores)
- iOS/iPadOS están explícitamente fuera del alcance para esta versión
- Android/Chrome es la única plataforma soportada
- Las pruebas en dispositivo físico (Samsung Galaxy A15) son obligatorias
- Las pruebas en emuladores complementan pero no sustituyen las pruebas reales
- La URL HTTPS final es necesaria para validar PWA completamente
