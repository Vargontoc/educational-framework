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
| `/panel` | PanelControl | PIN (pendiente) |
| `/game/:childId` | GameView | Sesión activa (pendiente) |
| `/docs` | Documentation | Pública |

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

## Configuración PWA

- **Display**: standalone (oculta barra del navegador)
- **Orientación**: landscape (forzada)
- **Iconos**: SVG placeholders (192x192, 512x512)

## Stores de Pinia

### useSessionStore
Estado de la sesión familiar: familia activa, hijo seleccionado, autenticación.

### useWSStore
Estado de conexión WebSocket: GameChannel, ParentChannel, canal activo.

### useUIStore
Estado de UI: modales, pantallas de carga, mensajes de error.

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
- Guards de rutas pendientes de implementación
- Clientes API y WebSocket pendientes de implementación
