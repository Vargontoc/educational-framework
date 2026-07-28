import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

/**
 * Configuración de Vite para Vue 3
 * 
 * - Plugin Vue para soporte de SFC (Single File Components)
 * - Alias @ para importar desde src/
 * - Configuración de puertos para desarrollo desde variables de entorno
 */

export default defineConfig(({ mode }) => {
  // Cargar variables de entorno según el modo
  const env = loadEnv(mode, process.cwd(), '')
  
  // Puerto del servidor de desarrollo (por defecto 80)
  const port = parseInt(env.VITE_PORT || '80', 10)
  
  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    server: {
      port,
      host: '0.0.0.0'
    },
    build: {
      outDir: 'dist',
      sourcemap: false
    }
  }
})
