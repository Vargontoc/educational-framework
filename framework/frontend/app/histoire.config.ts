import { defineConfig } from 'histoire'
import { HstVue } from '@histoire/plugin-vue'

/**
 * Configuración de Histoire
 * 
 * Herramienta de catálogo de componentes accesible solo en desarrollo
 * URL: /dev/components
 */

export default defineConfig({
  plugins: [HstVue()],
  setupFile: './src/histoire.setup.ts',
  outDir: '.histoire/dist',
  vite: {
    base: '/dev/components/',
  },
  tree: {
    groups: [
      {
        id: 'foundation',
        title: 'Foundation',
      },
      {
        id: 'components',
        title: 'Components',
      },
    ],
  },
})
