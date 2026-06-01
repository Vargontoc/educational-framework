import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import { useSessionStore } from '@/stores/useSessionStore'
import { isDevContentEnabled } from '@/utils/devContentEnabled'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/panel',
      name: 'panel',
      component: () => import('@/views/PanelControlView.vue'),
      beforeEnter: () => {
        const session = useSessionStore()
        if (!session.isAuthenticated()) {
          return '/'
        }
      }
    },
    {
      path: '/game',
      name: 'game',
      component: () => import('@/views/GameView.vue'),
      props: true,
      beforeEnter: () => {
        const session = useSessionStore()
        if (!session.hasActiveChildSession()) {
          return '/'
        }
      }
    },
    {
      path: '/design-system',
      name: 'design-system',
      component: () => import('@/views/DesignSystemView.vue'),
      beforeEnter: () => import.meta.env.DEV || '/'
    },
    {
      path: '/dev/content',
      name: 'dev-content',
      component: () => import('@/views/DevContentView.vue'),
      beforeEnter: () => {
        if (!isDevContentEnabled()) {
          return '/'
        }
      }
    },
    {
      path: '/docs',
      name: 'docs',
      component: () => import('@/views/DocsView.vue')
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/'
    }
  ]
})

router.beforeEach((to) => {
  if (import.meta.env.DEV) {
    console.log(`[router] -> ${String(to.name ?? to.path)}`)
  }
})

export default router
