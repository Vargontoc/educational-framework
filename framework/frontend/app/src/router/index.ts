import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useSessionStore } from '../stores/session'

/**
 * Configuración de rutas según ADR-010
 * 
 * Rutas definidas:
 * - / : Home (pública)
 * - /panel : PanelControl (protegida por PIN)
 * - /game/:childId : GameView (requiere sesión activa)
 * - /docs : Documentation (pública)
 * 
 * Navegación: Todas las navegaciones internas usan router.replace() para eliminar historial
 * Guards: Protección global que redirige a Home si no hay sesión/PIN
 * Recarga: El estado persiste en sessionStorage, permitiendo continuar en la misma ruta
 */

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/HomeView.vue')
  },
  {
    path: '/panel',
    name: 'PanelControl',
    component: () => import('../views/PanelControlView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/game/:childId',
    name: 'GameView',
    component: () => import('../views/GameView.vue'),
    meta: { requiresChildSession: true }
  },
  {
    path: '/docs',
    name: 'Documentation',
    component: () => import('../views/DocumentationView.vue')
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFoundView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * Guard global de navegación
 * 
 * Protege rutas según meta:
 * - requiresAuth: requiere isAuthenticated en sessionStore (para /panel)
 * - requiresChildSession: requiere selectedChildId en sessionStore (para /game/:childId)
 * 
 * Si no se cumple la condición, redirige a Home con router.replace()
 */
router.beforeEach((to, _from, next) => {
  const sessionStore = useSessionStore()

  // Verificar si la ruta requiere autenticación (PIN validado)
  if (to.meta.requiresAuth) {
    if (!sessionStore.isAuthenticated) {
      // No hay sesión autenticada, redirigir a Home
      return next({ name: 'Home', replace: true })
    }
  }

  // Verificar si la ruta requiere sesión de niño activa
  if (to.meta.requiresChildSession) {
    if (!sessionStore.selectedChildId) {
      // No hay niño seleccionado, redirigir a Home
      return next({ name: 'Home', replace: true })
    }
  }

  next()
})

/**
 * Helper para navegación interna
 * Todas las navegaciones usan replace() para eliminar historial
 */
export function navigateTo(name: string, params?: Record<string, string>): void {
  router.replace({ name, params })
}

export default router
