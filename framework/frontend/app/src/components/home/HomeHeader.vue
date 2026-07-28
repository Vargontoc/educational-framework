<template>
  <header class="home-header">
    <div class="home-header__actions">
      <!-- Acceso a documentacion (siempre visible) -->
      <NubiTooltip :text="t('views.home.documentation')" position="bottom">
        <button
          class="home-header__button"
          :aria-label="t('views.home.documentation')"
          @click="goToDocs"
        >
          <NubiIcon name="help-circle" :size="24" />
          <span class="home-header__label">{{ t('views.home.documentation') }}</span>
        </button>
      </NubiTooltip>

      <!-- Acceso a configuracion (solo si hay familia) -->
      <NubiTooltip v-if="hasFamily" :text="t('views.home.settings')" position="bottom">
        <button
          class="home-header__button"
          :aria-label="t('views.home.settings')"
          @click="goToPanel"
        >
          <NubiIcon name="settings" :size="24" />
          <span class="home-header__label">{{ t('views.home.settings') }}</span>
        </button>
      </NubiTooltip>
    </div>
  </header>
</template>

<script setup lang="ts">
/**
 * HomeHeader - Accesos superiores de Home
 * 
 * Segun FEAT-002 y SPRINT-008:
 * - Acceso a documentacion siempre visible (esquina superior derecha)
 * - Acceso a configuracion solo si hay familia registrada
 * - Navegacion a /docs y /panel respectivamente
 * - Identificables sin depender solo de color o iconos (icono + label)
 * - Objetivo tactil minimo 48x48dp (accesibilidad)
 * - i18n completo
 */

import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import NubiIcon from '../base/NubiIcon.vue'
import NubiTooltip from '../base/NubiTooltip.vue'

interface Props {
  /** Indica si hay una familia registrada */
  hasFamily: boolean
}

defineProps<Props>()

const { t } = useI18n()
const router = useRouter()

/**
 * Navega a la vista de documentacion
 */
function goToDocs() {
  router.replace({ name: 'Documentation' })
}

/**
 * Navega al panel de control parental
 */
function goToPanel() {
  router.replace({ name: 'PanelControl' })
}
</script>

<style scoped>
.home-header {
  position: absolute;
  top: 0;
  right: 0;
  left: 0;
  padding: var(--nubi-spacing-md);
  display: flex;
  justify-content: flex-end;
  z-index: 10;
}

.home-header__actions {
  display: flex;
  gap: var(--nubi-spacing-sm);
  align-items: center;
}

.home-header__button {
  /* Reset */
  border: none;
  background: transparent;
  cursor: pointer;
  font-family: inherit;
  
  /* Layout */
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--nubi-spacing-xs);
  
  /* Tamano minimo tactil (48x48dp) */
  min-width: 48px;
  min-height: 48px;
  padding: var(--nubi-spacing-xs) var(--nubi-spacing-sm);
  
  /* Estilo visual - transparente con sombra sutil */
  color: var(--nubi-text-primary);
  border-radius: var(--nubi-radius-lg);
  box-shadow: var(--nubi-shadow-sm);
  
  /* Transitions - solo propiedades especificas para evitar reflow */
  transition: background-color var(--nubi-duration-fast) var(--nubi-ease-in-out),
              color var(--nubi-duration-fast) var(--nubi-ease-in-out),
              box-shadow var(--nubi-duration-fast) var(--nubi-ease-in-out);
  
  /* Focus visible */
  outline: none;
  
  /* Prevenir desplazamiento */
  will-change: background-color, box-shadow;
}

.home-header__button:hover {
  background-color: rgba(255, 255, 255, 0.3);
  color: var(--nubi-color-primary-dark);
  box-shadow: var(--nubi-shadow-md);
}

.home-header__button:active {
  background-color: rgba(255, 255, 255, 0.4);
  color: var(--nubi-color-primary-dark);
  box-shadow: var(--nubi-shadow-sm);
}

.home-header__button:focus-visible {
  box-shadow: 0 0 0 3px var(--nubi-color-focus);
}

.home-header__label {
  font-size: var(--nubi-font-size-xs);
  font-weight: var(--nubi-font-weight-medium);
  line-height: 1;
  white-space: nowrap;
}

/* Responsive: movil */
@media (max-width: 640px) {
  .home-header {
    padding: var(--nubi-spacing-sm);
  }
  
  .home-header__button {
    min-width: 48px;
    min-height: 48px;
  }
  
  .home-header__label {
    font-size: 0.625rem;
  }
}

/* Responsive: tablet */
@media (min-width: 641px) and (max-width: 1024px) {
  .home-header {
    padding: var(--nubi-spacing-md);
  }
}

/* Portrait: ajustar padding y tamaño para mejor accesibilidad */
@media (orientation: portrait) {
  .home-header {
    padding: var(--nubi-spacing-sm);
  }
  
  .home-header__button {
    min-width: 44px;
    min-height: 44px;
  }
  
  .home-header__label {
    font-size: 0.625rem;
  }
}
</style>
