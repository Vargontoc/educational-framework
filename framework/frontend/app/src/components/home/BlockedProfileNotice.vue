<template>
  <div class="blocked-profile-notice" aria-live="polite">
    <div class="blocked-profile-notice__icon" aria-hidden="true">
      <NubiIcon name="lock" :size="48" />
    </div>
    <p class="blocked-profile-notice__text">
      {{ t('views.game.blockedProfile.notice') }}
    </p>
    <span class="blocked-profile-notice__sr-only">{{ t('views.game.blockedProfile.iconLabel') }}</span>
    <NubiButton
      variant="secondary"
      class="blocked-profile-notice__close"
      :aria-label="t('views.game.blockedProfile.close')"
      @click="$emit('close')"
    >
      {{ t('views.game.blockedProfile.close') }}
    </NubiButton>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import NubiIcon from '../base/NubiIcon.vue'
import NubiButton from '../base/NubiButton.vue'

defineEmits<{
  close: []
}>()

const { t } = useI18n()
</script>

<style scoped>
.blocked-profile-notice {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--nubi-spacing-lg);
  padding: var(--nubi-spacing-xl) var(--nubi-spacing-lg);
  text-align: center;
}

.blocked-profile-notice__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  border-radius: var(--nubi-radius-full);
  background-color: var(--nubi-bg-surface-secondary);
  color: var(--nubi-text-secondary);
}

.blocked-profile-notice__text {
  font-size: var(--nubi-font-size-lg);
  font-weight: var(--nubi-font-weight-medium);
  color: var(--nubi-text-primary);
  margin: 0;
  line-height: var(--nubi-line-height-relaxed);
  max-width: 320px;
}

.blocked-profile-notice__sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border-width: 0;
}

.blocked-profile-notice__close {
  min-width: 44px;
  min-height: 44px;
}

@media (max-width: 480px) {
  .blocked-profile-notice {
    padding: var(--nubi-spacing-lg) var(--nubi-spacing-md);
    gap: var(--nubi-spacing-md);
  }

  .blocked-profile-notice__icon {
    width: 64px;
    height: 64px;
  }

  .blocked-profile-notice__text {
    font-size: var(--nubi-font-size-base);
  }
}
</style>
