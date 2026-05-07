import '@/styles/variables.css'
import '@/styles/global.css'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import { createI18n } from 'vue-i18n'
import router from '@/router'
import { messages } from '@/i18n/es'
import App from './App.vue'

const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

const i18n = createI18n({
  legacy: false,
  locale: 'es',
  fallbackLocale: 'es',
  messages: { es: messages }
})

createApp(App)
  .use(pinia)
  .use(router)
  .use(i18n)
  .mount('#app')
