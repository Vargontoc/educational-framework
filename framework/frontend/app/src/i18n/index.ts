import { createI18n } from 'vue-i18n'
import es from './locales/es'

/**
 * Configuración de internacionalización según ADR-010
 * 
 * - Idioma activo: Español (es)
 * - Arquitectura preparada para añadir nuevos idiomas sin refactoring
 * - Todos los textos visibles deben pasar por i18n
 * - No se permiten literales en templates
 */

const i18n = createI18n({
  legacy: false,
  locale: 'es',
  fallbackLocale: 'es',
  messages: {
    es
  }
})

export default i18n
