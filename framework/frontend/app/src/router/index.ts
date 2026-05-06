import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'

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
      // guard stub — real PIN auth added in the auth sprint
      component: () => import('@/views/PanelControlView.vue')
    },
    {
      path: '/game/:childId',
      name: 'game',
      // guard stub — active child session check added in the auth sprint
      component: () => import('@/views/GameView.vue')
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/'
    }
  ]
})

router.beforeEach((to) => {
  if (import.meta.env.DEV) {
    console.log(`[router] → ${String(to.name ?? to.path)}`)
  }
})

export default router
