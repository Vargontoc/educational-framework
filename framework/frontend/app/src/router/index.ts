import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useParentalAuthStore } from '../stores/parentalAuth'
import { useSessionStore } from '../stores/session'

/**
 * Configuración de rutas según ADR-010
 * 
 * Rutas definidas:
 * - / : Home (pública)
 * - /panel : PanelCover (protegida por autenticación parental)
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
    component: () => import('../layouts/ParentPanelLayout.vue'),
    meta: { requiresParentalAuth: true },
    children: [
      {
        path: '',
        name: 'PanelCover',
        component: () => import('../views/PanelCoverView.vue')
      },
      {
        path: 'configuracion',
        name: 'PanelConfiguracion',
        component: () => import('../views/ConfiguracionView.vue')
      },
      {
        path: 'ninos',
        name: 'PanelNinos',
        component: () => import('../views/NinosView.vue')
      },
      {
        path: 'chatbot',
        name: 'PanelChatbot',
        component: () => import('../views/ChatbotView.vue')
      },
      {
        path: 'documentacion',
        name: 'PanelDocumentacion',
        component: () => import('../views/DocumentationView.vue')
      },
      {
        path: 'lectura-familiar',
        name: 'PanelLecturaFamiliar',
        component: () => import('../views/LecturaFamiliarView.vue')
      },
      {
        path: 'relajacion-familiar',
        name: 'PanelRelajacionFamiliar',
        component: () => import('../views/RelajacionFamiliarView.vue')
      }
    ]
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

// Rutas del catálogo de componentes solo en desarrollo
if (import.meta.env.DEV) {
  const catalogRoutes: RouteRecordRaw[] = [
    {
      path: '/dev/components',
      name: 'ComponentCatalog',
      component: () => import('../views/CatalogView.vue')
    },
    {
      path: '/dev/components/tokens/colors',
      name: 'CatalogColors',
      component: () => import('../views/catalog/ColorsView.vue')
    },
    {
      path: '/dev/components/tokens/typography',
      name: 'CatalogTypography',
      component: () => import('../views/catalog/TypographyView.vue')
    },
    {
      path: '/dev/components/tokens/spacing',
      name: 'CatalogSpacing',
      component: () => import('../views/catalog/SpacingView.vue')
    },
    {
      path: '/dev/components/tokens/borders',
      name: 'CatalogBorders',
      component: () => import('../views/catalog/BordersView.vue')
    },
    {
      path: '/dev/components/button',
      name: 'CatalogButton',
      component: () => import('../views/catalog/ButtonView.vue')
    },
    {
      path: '/dev/components/icon-button',
      name: 'CatalogIconButton',
      component: () => import('../views/catalog/IconButtonView.vue')
    },
    {
      path: '/dev/components/spinner',
      name: 'CatalogSpinner',
      component: () => import('../views/catalog/SpinnerView.vue')
    },
    {
      path: '/dev/components/skeleton',
      name: 'CatalogSkeleton',
      component: () => import('../views/catalog/SkeletonView.vue')
    },
    {
      path: '/dev/components/empty-state',
      name: 'CatalogEmptyState',
      component: () => import('../views/catalog/EmptyStateView.vue')
    },
    {
      path: '/dev/components/error-state',
      name: 'CatalogErrorState',
      component: () => import('../views/catalog/ErrorStateView.vue')
    },
    {
      path: '/dev/components/text-input',
      name: 'CatalogTextInput',
      component: () => import('../views/catalog/TextInputView.vue')
    },
    {
      path: '/dev/components/number-input',
      name: 'CatalogNumberInput',
      component: () => import('../views/catalog/NumberInputView.vue')
    },
    {
      path: '/dev/components/pin-input',
      name: 'CatalogPinInput',
      component: () => import('../views/catalog/PinInputView.vue')
    },
    {
      path: '/dev/components/checkbox',
      name: 'CatalogCheckbox',
      component: () => import('../views/catalog/CheckboxView.vue')
    },
    {
      path: '/dev/components/toggle',
      name: 'CatalogToggle',
      component: () => import('../views/catalog/ToggleView.vue')
    },
    {
      path: '/dev/components/select',
      name: 'CatalogSelect',
      component: () => import('../views/catalog/SelectView.vue')
    },
    {
      path: '/dev/components/radio-group',
      name: 'CatalogRadioGroup',
      component: () => import('../views/catalog/RadioGroupView.vue')
    },
    {
      path: '/dev/components/sidebar',
      name: 'CatalogSidebar',
      component: () => import('../views/catalog/SidebarView.vue')
    },
    {
      path: '/dev/components/tabs',
      name: 'CatalogTabs',
      component: () => import('../views/catalog/TabsView.vue')
    },
    {
      path: '/dev/components/breadcrumb',
      name: 'CatalogBreadcrumb',
      component: () => import('../views/catalog/BreadcrumbView.vue')
    },
    {
      path: '/dev/components/back-button',
      name: 'CatalogBackButton',
      component: () => import('../views/catalog/BackButtonView.vue')
    },
    {
      path: '/dev/components/confirm-modal',
      name: 'CatalogConfirmModal',
      component: () => import('../views/catalog/ConfirmModalView.vue')
    },
    {
      path: '/dev/components/info-modal',
      name: 'CatalogInfoModal',
      component: () => import('../views/catalog/InfoModalView.vue')
    },
    {
      path: '/dev/components/toast',
      name: 'CatalogToast',
      component: () => import('../views/catalog/ToastView.vue')
    },
    {
      path: '/dev/components/alert',
      name: 'CatalogAlert',
      component: () => import('../views/catalog/AlertView.vue')
    },
    {
      path: '/dev/components/tooltip',
      name: 'CatalogTooltip',
      component: () => import('../views/catalog/TooltipView.vue')
    },
    {
      path: '/dev/components/card',
      name: 'CatalogCard',
      component: () => import('../views/catalog/CardView.vue')
    },
    {
      path: '/dev/components/avatar',
      name: 'CatalogAvatar',
      component: () => import('../views/catalog/AvatarView.vue')
    },
    {
      path: '/dev/components/badge',
      name: 'CatalogBadge',
      component: () => import('../views/catalog/BadgeView.vue')
    },
    {
      path: '/dev/components/list',
      name: 'CatalogList',
      component: () => import('../views/catalog/ListView.vue')
    },
    {
      path: '/dev/components/grid',
      name: 'CatalogGrid',
      component: () => import('../views/catalog/GridView.vue')
    },
    {
      path: '/dev/components/progress-bar',
      name: 'CatalogProgressBar',
      component: () => import('../views/catalog/ProgressBarView.vue')
    },
    {
      path: '/dev/components/stepper',
      name: 'CatalogStepper',
      component: () => import('../views/catalog/StepperView.vue')
    },
    {
      path: '/dev/components/counter',
      name: 'CatalogCounter',
      component: () => import('../views/catalog/CounterView.vue')
    },
    {
      path: '/dev/components/auth-screen',
      name: 'CatalogAuthScreen',
      component: () => import('../views/catalog/AuthScreenView.vue')
    },
    {
      path: '/dev/components/session-indicator',
      name: 'CatalogSessionIndicator',
      component: () => import('../views/catalog/SessionIndicatorView.vue')
    },
    {
      path: '/dev/components/inactivity-overlay',
      name: 'CatalogInactivityOverlay',
      component: () => import('../views/catalog/InactivityOverlayView.vue')
    }
  ]
  routes.unshift(...catalogRoutes)
}

const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * Guard global de navegación
 * 
 * Protege rutas según meta:
 * - requiresParentalAuth: requiere isAuthenticated en parentalAuthStore (para /panel)
 * - requiresChildSession: requiere selectedChildId en sessionStore (para /game/:childId)
 * 
 * Si no se cumple la condición, redirige a Home con router.replace()
 */
router.beforeEach((to, _from, next) => {
  const sessionStore = useSessionStore()
  const parentalAuthStore = useParentalAuthStore()

  if (to.meta.requiresParentalAuth) {
    if (!parentalAuthStore.isAuthenticated) {
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
