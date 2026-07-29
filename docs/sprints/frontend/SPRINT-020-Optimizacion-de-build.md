# SPRINT-003 — Optimización de build en Vite

## Estado

- **Estado:** planificado
- **Responsable principal:** frontend
- **Prioridad:** MEDIA
- **Dependencias:** SPRINT-001 y SPRINT-002 deben estar completados
- **Impacto estimado:** Reducción de ~2-3 MB (cacheable) y mejora de tiempos de carga repetidos

## Objetivo

Configurar Vite para generar un build de producción optimizado con code splitting explícito, compresión de assets y separación de dependencias vendor para maximizar el cacheo del navegador.

## Problema actual

### Archivo: `framework/frontend/app/vite.config.ts`

```typescript
build: {
  outDir: 'dist',
  sourcemap: false
}
```

No hay configuración de:
- `manualChunks` para separar dependencias vendor
- Compresión gzip/brotli
- Optimización de CSS
- Límite de tamaño de chunks
- Minificación avanzada con eliminación de console.log/debug

## Tareas

### Tarea 3.1: Configurar manualChunks para vendor splitting

**Descripción:** Separar las dependencias en chunks independientes para que el navegador pueda cachearlas eficientemente.

**Archivo:** `framework/frontend/app/vite.config.ts`

**Configuración esperada:**
```typescript
build: {
  outDir: 'dist',
  sourcemap: false,
  rollupOptions: {
    output: {
      manualChunks: {
        // Core de Vue
        'vendor-vue': ['vue', 'vue-router', 'pinia'],
        
        // Internacionalización
        'vendor-i18n': ['vue-i18n'],
        
        // Iconos (después de SPRINT-001, solo los usados)
        'vendor-icons': ['@lucide/vue'],
        
        // NOTA: Phaser NO se incluye aquí.
        // Phaser (~1 MB) se mantiene como dependencia pero se cargará
        // dinámicamente en GameView cuando se implemente el juego.
        // Cuando se use, añadir: 'vendor-phaser': ['phaser']
      }
    }
  }
}
```

**Criterios de aceptación:**
- El build genera chunks separados: `vendor-vue.js`, `vendor-i18n.js`, `vendor-icons.js`
- Cada chunk tiene un hash en el nombre para cache busting
- Los chunks vendor no cambian entre builds si las dependencias no cambian
- El chunk de la aplicación (código propio) es independiente de vendor

---

### Tarea 3.2: Configurar minificación avanzada con Terser

**Descripción:** Reemplazar la minificación por defecto (esbuild) por Terser con opciones avanzadas para eliminar console.log, console.debug y reducir tamaño.

**Archivo:** `framework/frontend/app/vite.config.ts`

**Configuración esperada:**
```typescript
build: {
  // ... configuración anterior
  minify: 'terser',
  terserOptions: {
    compress: {
      drop_console: true,
      drop_debugger: true,
      pure_funcs: ['console.log', 'console.debug', 'console.info']
    },
    format: {
      comments: false
    }
  }
}
```

**Nota:** Esto eliminará todos los `console.log`, `console.debug` y `console.info` del código de producción. Los `console.warn` y `console.error` se mantienen para debugging de errores críticos.

**Criterios de aceptación:**
- El build de producción no contiene `console.log` ni `console.debug`
- Los `console.warn` y `console.error` se mantienen
- El tamaño del bundle es menor que con esbuild por defecto
- No hay errores de compilación

---

### Tarea 3.3: Configurar compresión gzip y brotli

**Descripción:** Generar archivos pre-comprimidos en el build para que el servidor los sirva directamente sin comprimir en tiempo real.

**Dependencia:** Instalar `vite-plugin-compression`

```bash
npm install -D vite-plugin-compression
```

**Archivo:** `framework/frontend/app/vite.config.ts`

**Configuración esperada:**
```typescript
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'
import { compression } from 'vite-plugin-compression'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const port = parseInt(env.VITE_PORT || '80', 10)
  
  return {
    plugins: [
      vue(),
      // Generar archivos .gz
      compression({
        algorithm: 'gzip',
        ext: '.gz',
        threshold: 10240, // Solo comprimir archivos >10KB
        deleteOriginalAssets: false
      }),
      // Generar archivos .br (brotli)
      compression({
        algorithm: 'brotliCompress',
        ext: '.br',
        threshold: 10240,
        deleteOriginalAssets: false
      })
    ],
    // ... resto de configuración
  }
})
```

**Criterios de aceptación:**
- El directorio `dist/` contiene archivos `.gz` y `.br` para cada asset >10KB
- Los archivos comprimidos son significativamente menores que los originales
- El servidor puede configurar `Content-Encoding: gzip` o `br` para servir los archivos pre-comprimidos

---

### Tarea 3.4: Configurar optimización de CSS

**Descripción:** Extraer CSS en archivos separados y minificarlo para mejorar el cacheo y reducir el tamaño.

**Archivo:** `framework/frontend/app/vite.config.ts`

**Configuración esperada:**
```typescript
build: {
  // ... configuración anterior
  cssCodeSplit: true, // Extraer CSS por chunk
  assetsInlineLimit: 4096, // Inline assets <4KB
}
```

**Criterios de aceptación:**
- El CSS se extrae en archivos `.css` separados (no inline en JS)
- Cada chunk tiene su CSS correspondiente
- El CSS está minificado
- No hay FOUC (Flash of Unstyled Content) al cargar la aplicación

---

### Tarea 3.5: Configurar chunkSizeWarningLimit y análisis de bundle

**Descripción:** Aumentar el límite de advertencia de tamaño de chunks y generar un reporte visual del bundle.

**Archivo:** `framework/frontend/app/vite.config.ts`

**Configuración esperada:**
```typescript
build: {
  // ... configuración anterior
  chunkSizeWarningLimit: 1000, // Advertir solo si un chunk >1MB
}
```

**Script de análisis (package.json):**
```json
{
  "scripts": {
    "build:analyze": "vite build --mode production && npx vite-bundle-visualizer"
  }
}
```

**Criterios de aceptación:**
- No hay advertencias de tamaño de chunk en el build
- El comando `npm run build:analyze` genera un reporte visual
- El reporte muestra la composición de cada chunk

---

### Tarea 3.6: Pruebas de build y carga

**Descripción:** Verificar que el build optimizado funciona correctamente y mejora los tiempos de carga.

**Pasos:**
1. Ejecutar `npm run build`
2. Verificar que no hay errores de compilación
3. Ejecutar `npm run preview` para servir el build de producción
4. Medir tiempos de carga con DevTools en modo 3G
5. Verificar que los archivos `.gz` y `.br` se sirven correctamente

**Métricas a capturar:**
- Tamaño total de `dist/`
- Número de archivos generados
- Tamaño de cada chunk (vendor-vue, vendor-i18n, vendor-icons, main)
- Tiempo de carga en 3G
- Número de requests

**Criterios de aceptación:**
- El build se genera sin errores
- La aplicación funciona correctamente en preview
- Los tiempos de carga son menores que antes
- Los chunks vendor tienen hash y son cacheables

---

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|---------------|
| `framework/frontend/app/vite.config.ts` | Configuración completa de build |
| `framework/frontend/app/package.json` | Añadir script `build:analyze` y dependencia `vite-plugin-compression` |

## Estimación

- **Duración:** 1 día
- **Complejidad:** Media
- **Riesgo:** Bajo (cambios de configuración, no de lógica)

## Dependencias

- **SPRINT-001** debe estar completado (para que el chunk `vendor-icons` contenga solo los iconos usados)
- **SPRINT-002** debe estar completado (para que los chunks asíncronos de modales se generen correctamente)

## Métricas esperadas

| Métrica | Antes | Después (esperado) |
|---------|-------|-------------------|
| Tamaño chunk inicial | ~9 MB | ~1-2 MB |
| Chunks vendor cacheables | No | Sí (~2-3 MB total) |
| Archivos pre-comprimidos | No | Sí (.gz y .br) |
| Tiempo de carga repetido | Sin mejora | ~50-70% más rápido (cache) |
| Tiempo de carga inicial (3G) | 18.31s | ~4-6s |

## Plan de rollback

Si se detectan problemas, revertir el commit del sprint. Los cambios son de configuración y completamente reversibles.

## Notas adicionales

### Configuración de servidor para archivos pre-comprimidos

Para que los archivos `.gz` y `.br` se sirvan correctamente, el servidor debe configurar:

**Nginx:**
```nginx
gzip_static on;
brotli_static on;
```

**Apache:**
```apache
<IfModule mod_headers.c>
  <FilesMatch "\.gz$">
    Header set Content-Encoding gzip
    Header set Content-Type application/javascript
  </FilesMatch>
  <FilesMatch "\.br$">
    Header set Content-Encoding br
    Header set Content-Type application/javascript
  </FilesMatch>
</IfModule>
```

**Vercel/Netlify:** Detectan automáticamente los archivos pre-comprimidos.

### Cache headers recomendados

```nginx
location /assets/ {
  expires 1y;
  add_header Cache-Control "public, immutable";
}

location / {
  expires 1h;
  add_header Cache-Control "public, must-revalidate";
}
```
