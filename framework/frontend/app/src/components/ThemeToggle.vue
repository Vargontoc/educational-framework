<template>
  <button
    v-if="isDev"
    class="theme-toggle"
    :aria-label="isDarkMode ? 'Cambiar a modo claro' : 'Cambiar a modo oscuro'"
    @click="toggleTheme"
  >
    <NubiIcon :name="isDarkMode ? 'sun' : 'moon'" :size="20" />
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useTheme } from '../composables/useTheme'
import NubiIcon from './base/NubiIcon.vue'

const isDev = import.meta.env.DEV
const { isDark, toggleTheme } = useTheme()
const isDarkMode = computed(() => isDark())
</script>

<style scoped>
.theme-toggle {
  position: fixed;
  bottom: var(--nubi-spacing-md);
  right: var(--nubi-spacing-md);
  z-index: 99999;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 48px;
  min-height: 48px;
  padding: var(--nubi-spacing-sm);
  border: 1px solid var(--nubi-border-default);
  border-radius: var(--nubi-radius-full);
  background-color: var(--nubi-bg-surface-secondary);
  color: var(--nubi-text-primary);
  cursor: pointer;
  box-shadow: var(--nubi-shadow-md);
  transition: background-color var(--nubi-duration-fast) var(--nubi-ease-in-out),
              box-shadow var(--nubi-duration-fast) var(--nubi-ease-in-out);
}

.theme-toggle:hover {
  background-color: var(--nubi-bg-surface-tertiary);
  box-shadow: var(--nubi-shadow-lg);
}

.theme-toggle:focus-visible {
  outline: none;
  box-shadow: 0 0 0 3px var(--nubi-color-focus);
}
</style>
