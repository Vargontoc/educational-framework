import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import i18n from './i18n'
import './style.css'

/**
 * Configuración principal de la aplicación según ADR-010
 * 
 * - Vue 3 como framework base
 * - Pinia para gestión de estado (3 stores: session, ws, ui)
 * - Vue Router para navegación (history mode, sin historial)
 * - vue-i18n para internacionalización (español por defecto)
 */

const app = createApp(App)

// Plugins
app.use(createPinia())
app.use(router)
app.use(i18n)

// Montaje de la aplicación
app.mount('#app')
