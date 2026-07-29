<template>
  <div
    class="inactivity-overlay"
    role="alert"
    aria-live="assertive"
    aria-modal="true"
    @click="handleClick"
  >
    <div class="inactivity-overlay__content">
      <p class="inactivity-overlay__message">
        {{ t('inactivity.message') }}
      </p>
      <p class="inactivity-overlay__countdown" aria-atomic="true">
        {{ t('inactivity.redirecting', { seconds: countdown }) }}
      </p>
      <p class="inactivity-overlay__hint">
        {{ t('inactivity.clickToContinue') }}
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const router = useRouter()

const emit = defineEmits<{
  (e: 'cancel'): void
  (e: 'expired'): void
}>()

const countdown = ref(5)

let interval: ReturnType<typeof setInterval> | null = null

function handleClick() {
  if (interval) {
    clearInterval(interval)
    interval = null
  }
  emit('cancel')
}

onMounted(() => {
  interval = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      if (interval) clearInterval(interval)
      interval = null
      emit('expired')
      router.replace('/')
    }
  }, 1000)
})

onUnmounted(() => {
  if (interval) {
    clearInterval(interval)
    interval = null
  }
})
</script>

<style scoped>
.inactivity-overlay {
  position: fixed;
  inset: 0;
  z-index: 10000;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--nubi-overlay-bg);
  pointer-events: auto;
  cursor: pointer;
}

.inactivity-overlay__content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--nubi-spacing-lg);
  padding: var(--nubi-spacing-xl);
  text-align: center;
  max-width: 90vw;
}

.inactivity-overlay__message {
  font-size: var(--nubi-font-size-xl);
  font-weight: var(--nubi-font-weight-bold);
  color: var(--nubi-overlay-text);
  margin: 0;
  line-height: var(--nubi-line-height-tight);
}

.inactivity-overlay__countdown {
  font-size: var(--nubi-font-size-2xl);
  font-weight: var(--nubi-font-weight-bold);
  color: var(--nubi-overlay-text);
  margin: 0;
  min-width: 3ch;
  text-align: center;
}

.inactivity-overlay__hint {
  font-size: var(--nubi-font-size-md);
  font-weight: var(--nubi-font-weight-normal);
  color: var(--nubi-overlay-text-secondary);
  margin: 0;
}

@media (max-width: 1023px) {
  .inactivity-overlay__message {
    font-size: var(--nubi-font-size-lg);
  }

  .inactivity-overlay__countdown {
    font-size: var(--nubi-font-size-xl);
  }
}

@media (orientation: landscape) and (max-height: 500px) {
  .inactivity-overlay__content {
    gap: var(--nubi-spacing-md);
    padding: var(--nubi-spacing-lg);
  }
}
</style>
