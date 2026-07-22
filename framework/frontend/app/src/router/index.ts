import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

/**
 * Configuración de rutas según ADR-010
 * 
 * Rutas definidas:
 * - / : Home (pública)
 * - /panel : PanelControl (protegida por PIN - se implementará en sprint posterior)
 * - /game/:childId : GameView (requiere sesión activa - se implementará en sprint posterior)
 * - /docs : Documentation (pública)
 * 
 * Navegación: Todas las navegaciones internas usan router.replace() para eliminar historial
 * Guards: No implementados en este sprint (se implementarán en sprint posterior)
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
    component: () => import('../views/PanelControlView.vue')
    // Guards se implementarán en sprint posterior
  },
  {
    path: '/game/:childId',
    name: 'GameView',
    component: () => import('../views/GameView.vue')
    // Guards se implementarán en sprint posterior
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

export default router
