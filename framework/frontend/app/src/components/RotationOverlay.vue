<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const isPortrait = ref(false)

const mq = window.matchMedia('(orientation: portrait)')

function onOrientationChange(e: MediaQueryListEvent | MediaQueryList) {
  isPortrait.value = e.matches
}

onMounted(() => {
  onOrientationChange(mq)
  mq.addEventListener('change', onOrientationChange)
})

onUnmounted(() => {
  mq.removeEventListener('change', onOrientationChange)
})
</script>

<template>
  <slot />
  <div v-if="isPortrait" class="rotation-overlay">
    <p>{{ t('rotation.message') }}</p>
  </div>
</template>

<style scoped>
.rotation-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  font-size: 1.5rem;
  text-align: center;
  padding: 2rem;
}
</style>
