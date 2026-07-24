<template>
  <Teleport to="body">
    <Transition name="nubi-overlay">
      <div v-if="visible" class="nubi-inactivity-overlay">
        <div class="nubi-inactivity-overlay__content">
          <NubiIcon name="clock" :size="64" class="nubi-inactivity-overlay__icon" />
          
          <h2 class="nubi-inactivity-overlay__title">
            {{ t('components.inactivityOverlay.title') }}
          </h2>
          
          <p class="nubi-inactivity-overlay__message">
            {{ t('components.inactivityOverlay.message', { time: formattedTime }) }}
          </p>
          
          <div class="nubi-inactivity-overlay__actions">
            <NubiButton variant="primary" @click="handleExtend">
              {{ t('components.inactivityOverlay.extend') }}
            </NubiButton>
            <NubiButton variant="secondary" @click="handleLogout">
              {{ t('components.inactivityOverlay.logout') }}
            </NubiButton>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
/**
 * NubiInactivityOverlay - Overlay de inactividad antes del logout
 * 
 * Características:
 * - Aparece antes del logout automático
 * - Cuenta atrás visual
 * - Permite extender sesión o cerrar
 * - Teleport al body
 * - Animación de entrada/salida
 */

import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import NubiIcon from './NubiIcon.vue'
import NubiButton from './NubiButton.vue'

interface Props {
  /** Si el overlay es visible */
  visible: boolean
  /** Tiempo restante en segundos */
  timeLeft: number
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  timeLeft: 0
})

const emit = defineEmits<{
  extend: []
  logout: []
}>()

const { t } = useI18n()

const formattedTime = computed(() => {
  const minutes = Math.floor(props.timeLeft / 60)
  const seconds = props.timeLeft % 60
  return `${minutes}:${seconds.toString().padStart(2, '0')}`
})

function handleExtend() {
  emit('extend')
}

function handleLogout() {
  emit('logout')
}
</script>

<style scoped>
.nubi-inactivity-overlay {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--nubi-overlay-bg);
  z-index: 9500;
  padding: var(--nubi-spacing-lg);
}

.nubi-inactivity-overlay__content {
  background-color: var(--nubi-bg-surface);
  border-radius: var(--nubi-radius-xl);
  padding: var(--nubi-spacing-2xl);
  max-width: 400px;
  width: 100%;
  text-align: center;
  box-shadow: var(--nubi-shadow-xl);
}

.nubi-inactivity-overlay__icon {
  color: var(--nubi-color-warning);
  margin-bottom: var(--nubi-spacing-lg);
}

.nubi-inactivity-overlay__title {
  font-size: var(--nubi-font-size-2xl);
  font-weight: var(--nubi-font-weight-bold);
  color: var(--nubi-text-primary);
  margin: 0 0 var(--nubi-spacing-md) 0;
}

.nubi-inactivity-overlay__message {
  font-size: var(--nubi-font-size-base);
  color: var(--nubi-text-secondary);
  margin: 0 0 var(--nubi-spacing-xl) 0;
  line-height: var(--nubi-line-height-relaxed);
}

.nubi-inactivity-overlay__actions {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-sm);
}

/* Transition */
.nubi-overlay-enter-active {
  transition: opacity var(--nubi-duration-normal) var(--nubi-ease-out);
}

.nubi-overlay-enter-active .nubi-inactivity-overlay__content {
  transition: transform var(--nubi-duration-normal) var(--nubi-ease-bounce);
}

.nubi-overlay-leave-active {
  transition: opacity var(--nubi-duration-fast) var(--nubi-ease-in);
}

.nubi-overlay-leave-active .nubi-inactivity-overlay__content {
  transition: transform var(--nubi-duration-fast) var(--nubi-ease-in);
}

.nubi-overlay-enter-from {
  opacity: 0;
}

.nubi-overlay-enter-from .nubi-inactivity-overlay__content {
  transform: scale(0.9);
}

.nubi-overlay-leave-to {
  opacity: 0;
}

.nubi-overlay-leave-to .nubi-inactivity-overlay__content {
  transform: scale(0.95);
}
</style>
