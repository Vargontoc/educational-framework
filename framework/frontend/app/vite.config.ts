import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'
import compression from 'vite-plugin-compression'
import { VitePWA } from 'vite-plugin-pwa'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const port = parseInt(env.VITE_PORT || '80', 10)

  return {
    plugins: [
      vue(),
      compression({
        algorithm: 'gzip',
        ext: '.gz',
        threshold: 10240,
        deleteOriginalAssets: false
      }),
      compression({
        algorithm: 'brotliCompress',
        ext: '.br',
        threshold: 10240,
        deleteOriginalAssets: false
      }),
      VitePWA({
        registerType: 'autoUpdate',
        // El manifest ya existe en public/manifest.webmanifest y está enlazado en index.html
        manifest: false,
        includeManifestIcons: false,
        devOptions: {
          enabled: true
        },
        workbox: {
          globPatterns: ['**/*.{js,css,html,svg,png,ico}']
        }
      })
    ],
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
      sourcemap: false,
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
      },
      cssCodeSplit: true,
      assetsInlineLimit: 4096,
      chunkSizeWarningLimit: 1000,
      rollupOptions: {
        output: {
          entryFileNames: 'assets/[name]-[hash].js',
          chunkFileNames: 'assets/[name]-[hash].js',
          assetFileNames: 'assets/[name]-[hash].[ext]',
          manualChunks(id) {
            if (id.includes('node_modules')) {
              if (id.includes('vue-i18n')) {
                return 'vendor-i18n'
              }
              if (id.includes('@lucide/vue')) {
                return 'vendor-icons'
              }
              if (id.includes('vue') || id.includes('vue-router') || id.includes('pinia')) {
                return 'vendor-vue'
              }
            }
          }
        }
      }
    }
  }
})
