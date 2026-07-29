# SPRINT-005 — Optimización de refresco de página

## Estado

- **Estado:** planificado
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** Ninguna
- **Impacto estimado:** Reducción del tiempo de refresco de >7s a <2s en 3G

## Objetivo

Optimizar el refresco de la página eliminando el Service Worker innecesario, configurando headers de cache HTTP, optimizando la imagen del avatar y verificando que TailwindCSS genera CSS mínimo.

## Problema actual

### 1. Service Worker innecesario

**Archivo:** `framework/frontend/app/public/sw.js`

La app se comunica con backend y no necesita funcionar offline. El SW añade complejidad sin beneficio y puede estar causando problemas de cache.

**Impacto:**
- Intercepta todas las peticiones
- Añade latencia en la gestión de cache
- No aporta valor para una app que requiere conexión

### 2. Falta de headers de cache HTTP

Los recursos estáticos (JS, CSS, imágenes) no tienen headers `Cache-Control` configurados, por lo que el navegador los re-descarga en cada refresco.

**Impacto:**
- En un refresco (F5), el navegador re-descarga todos los recursos
- En 3G, esto genera tiempos de carga muy largos

### 3. Avatar-bot.png sin optimizar

**Archivo:** `framework/frontend/app/src/assets/images/avatar-bot.png`

La imagen PNG del avatar de Nubi probablemente no esté comprimida ni convertida a WebP.

**Impacto:**
- Imagen grande que se descarga en cada refresco
- No se aprovechan formatos modernos como WebP

### 4. TailwindCSS v4 generando CSS grande

**Archivo:** `framework/frontend/app/src/styles/main.css`

TailwindCSS v4 importa 8 archivos CSS adicionales. Si no se purgan correctamente los estilos no usados, el CSS final puede ser muy grande.

**Impacto:**
- CSS grande que bloquea el renderizado
- Múltiples peticiones CSS en lugar de un único archivo

## Tareas

### Tarea 5.1: Eliminar Service Worker

**Descripción:** Eliminar el Service Worker ya que la app no necesita funcionar offline.

**Archivos a modificar:**
1. `framework/frontend/app/public/sw.js` → Eliminar archivo
2. `framework/frontend/app/src/main.ts` → Eliminar registro del SW (líneas 36-46)
3. `framework/frontend/app/public/manifest.webmanifest` → Mantener (para PWA básica sin SW)

**Cambios en main.ts:**
```typescript
// ANTES (líneas 36-46)
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js')
      .then((registration) => {
        console.debug('Service Worker registered:', registration.scope)
      })
      .catch((error) => {
        console.warn('Service Worker registration failed:', error)
      })
  })
}

// DESPUÉS: Eliminar completamente este bloque
```

**Criterios de aceptación:**
- El archivo `sw.js` se elimina
- El registro del SW se elimina de `main.ts`
- La app funciona correctamente sin SW
- No hay errores en consola relacionados con SW

---

### Tarea 5.2: Configurar headers de cache HTTP en Vite

**Descripción:** Configurar Vite para generar archivos con hashes en el nombre y configurar headers de cache para el servidor de producción.

**Archivo:** `framework/frontend/app/vite.config.ts`

**Configuración esperada:**
```typescript
build: {
  outDir: 'dist',
  sourcemap: false,
  rollupOptions: {
    output: {
      // Añadir hash a todos los assets para cache busting
      entryFileNames: 'assets/[name]-[hash].js',
      chunkFileNames: 'assets/[name]-[hash].js',
      assetFileNames: 'assets/[name]-[hash].[ext]'
    }
  }
}
```

**Archivo de configuración de servidor (crear):** `framework/frontend/app/server-config/nginx.conf`

```nginx
# Configuración de cache para recursos estáticos
location /assets/ {
  # Recursos con hash: cache por 1 año
  expires 1y;
  add_header Cache-Control "public, immutable";
}

location / {
  # HTML: no cachear (siempre verificar actualizaciones)
  expires 1h;
  add_header Cache-Control "public, must-revalidate";
}
```

**Nota:** Esta configuración es para nginx. Si se usa otro servidor, adaptar la configuración.

**Criterios de aceptación:**
- Los archivos JS/CSS generados tienen hash en el nombre
- El servidor puede configurar headers de cache según el tipo de recurso
- Los recursos con hash se cachean por 1 año
- El HTML se cachea por 1 hora con revalidación

---

### Tarea 5.3: Optimizar avatar-bot.png

**Descripción:** Convertir la imagen PNG a WebP y optimizarla para reducir el tamaño.

**Herramientas recomendadas:**
- `sharp` (npm package) para conversión y optimización
- O herramientas online como Squoosh.app

**Comando de conversión (con sharp):**
```bash
npm install -D sharp
```

**Script de optimización (crear):** `framework/frontend/app/scripts/optimize-images.js`

```javascript
import sharp from 'sharp'
import { fileURLToPath } from 'url'
import { dirname, join } from 'path'

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

const inputPath = join(__dirname, '../src/assets/images/avatar-bot.png')
const outputPath = join(__dirname, '../src/assets/images/avatar-bot.webp')

sharp(inputPath)
  .webp({ quality: 80 })
  .toFile(outputPath)
  .then(() => console.log('Image optimized:', outputPath))
  .catch((err) => console.error('Error:', err))
```

**Cambios en HomeView.vue:**
```vue
<!-- ANTES -->
<img
  src="../assets/images/avatar-bot.png"
  :alt="t('views.home.nubiAvatar')"
  class="home-view__avatar"
/>

<!-- DESPUÉS -->
<img
  src="../assets/images/avatar-bot.webp"
  :alt="t('views.home.nubiAvatar')"
  class="home-view__avatar"
/>
```

**Criterios de aceptación:**
- La imagen se convierte a WebP con calidad 80%
- El tamaño del archivo se reduce al menos 50%
- HomeView usa la imagen WebP
- La imagen se ve correctamente en todos los navegadores modernos

---

### Tarea 5.4: Verificar purgado de TailwindCSS

**Descripción:** Verificar que TailwindCSS está purgando correctamente los estilos no usados y generando un CSS mínimo.

**Archivo:** `framework/frontend/app/tailwind.config.js`

**Verificación:**
```javascript
module.exports = {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  // ... resto de configuración
}
```

**Comando para analizar el CSS generado:**
```bash
npm run build
# Analizar el tamaño del CSS en dist/assets/
ls -lh dist/assets/*.css
```

**Si el CSS es muy grande (>100KB):**
1. Verificar que `content` incluye todos los archivos Vue
2. Considerar usar `@tailwindcss/vite` plugin para TailwindCSS v4
3. Revisar si se están usando muchas utilidades de Tailwind

**Criterios de aceptación:**
- El CSS generado es <50KB (comprimido)
- Todos los estilos usados están presentes
- No hay estilos no usados en el CSS final

---

### Tarea 5.5: Pruebas de refresco

**Descripción:** Medir el tiempo de refresco antes y después de las optimizaciones.

**Proceso de medición:**
1. Ejecutar `npm run build`
2. Ejecutar `npm run preview`
3. Abrir Chrome DevTools → Network
4. Seleccionar throttling "Fast 3G"
5. Cargar la página (primera vez)
6. Hacer refresco (F5)
7. Capturar métricas de la pestaña Network
8. Repetir 3 veces y promediar

**Métricas a capturar:**

| Métrica | Antes | Después (objetivo) |
|---------|-------|-------------------|
| Requests en refresco | ~50-100 | <20 |
| Resources en refresco | ~1-2 MB | <500 KB |
| Tiempo de refresco (3G) | >7s | <2s |
| Cache hits | 0% | >80% |

**Criterios de aceptación:**
- El tiempo de refresco en 3G es <2s
- La mayoría de recursos se cargan desde cache
- No hay regresiones funcionales

---

### Tarea 5.6: Pruebas de regresión completa

**Descripción:** Verificar que toda la aplicación funciona correctamente tras las optimizaciones.

**Flujos críticos a probar:**

**1. HomeView:**
- [ ] Carga correctamente en primera visita
- [ ] Refresco (F5) carga rápidamente
- [ ] Muestra avatar de Nubi (WebP)
- [ ] Iconos se renderizan correctamente
- [ ] Responsive en móvil y tablet

**2. Navegación:**
- [ ] Navegación entre vistas funciona
- [ ] Refresco en cada vista carga rápidamente
- [ ] No hay errores en consola

**3. Modales:**
- [ ] FamilyRegistrationModal se abre y funciona
- [ ] ChildSelectionModal se abre y funciona
- [ ] ParentalAuthModal se abre y funciona

**4. Panel parental:**
- [ ] Acceso al panel funciona
- [ ] Navegación entre secciones funciona
- [ ] Refresco en panel carga rápidamente

**5. GameView:**
- [ ] Acceso a GameView funciona
- [ ] Refresco en GameView carga rápidamente

**Criterios de aceptación:**
- Todos los flujos funcionan correctamente
- El refresco es rápido en todas las vistas
- No hay errores en consola
- La experiencia de usuario no se degrada

---

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|---------------|
| `framework/frontend/app/public/sw.js` | Eliminar |
| `framework/frontend/app/src/main.ts` | Eliminar registro de SW |
| `framework/frontend/app/vite.config.ts` | Configurar hashes en assets |
| `framework/frontend/app/src/assets/images/avatar-bot.png` | Optimizar/convertir a WebP |
| `framework/frontend/app/src/views/HomeView.vue` | Usar imagen WebP |
| `framework/frontend/app/server-config/nginx.conf` | Nuevo archivo (configuración de servidor) |
| `framework/frontend/app/scripts/optimize-images.js` | Nuevo archivo (script de optimización) |

## Estimación

- **Duración:** 1 día
- **Complejidad:** Media
- **Riesgo:** Bajo (cambios de configuración y optimización)

## Métricas esperadas

| Métrica | Antes (refresco) | Después (esperado) |
|---------|------------------|-------------------|
| Requests | ~50-100 | <20 |
| Resources | ~1-2 MB | <500 KB |
| Tiempo (3G) | >7s | <2s |
| Cache hits | 0% | >80% |

## Plan de rollback

Si se detectan problemas:
1. Restaurar `sw.js` y el registro en `main.ts`
2. Revertir cambios en `vite.config.ts`
3. Restaurar `avatar-bot.png` original
4. Revertir cambios en `HomeView.vue`

Todos los cambios son reversibles mediante git.

## Notas adicionales

### Compatibilidad de WebP

WebP es soportado por:
- Chrome 32+ (2014)
- Firefox 65+ (2019)
- Safari 14+ (2020)
- Edge 79+ (2020)

Para navegadores antiguos que no soportan WebP, se puede usar el elemento `<picture>`:

```vue
<picture>
  <source srcset="../assets/images/avatar-bot.webp" type="image/webp">
  <img
    src="../assets/images/avatar-bot.png"
    :alt="t('views.home.nubiAvatar')"
    class="home-view__avatar"
  />
</picture>
```

**Decisión:** Usar WebP directamente (sin fallback) ya que los navegadores antiguos no son objetivo de My Friend Nubi (app moderna para tabletas/móviles).

### Configuración de servidor

La configuración de cache HTTP depende del servidor de producción. Este sprint incluye configuración para nginx. Si se usa otro servidor (Apache, Caddy, etc.), adaptar la configuración.

### Service Worker y PWA

Al eliminar el SW, la app pierde la capacidad de funcionar offline. Sin embargo, esto es aceptable porque:
1. La app se comunica con backend
2. No se promociona como PWA
3. El `manifest.webmanifest` se mantiene para instalación básica
