<template>
  <div
    :class="['parental-child-card', { 'parental-child-card--blocked': isBlocked }]"
    role="button"
    tabindex="0"
    :aria-label="cardAriaLabel"
    @click="$emit('edit', profile.id)"
    @keydown.enter="$emit('edit', profile.id)"
    @keydown.space.prevent="$emit('edit', profile.id)"
  >
    <div class="parental-child-card__avatar">
      <svg viewBox="0 0 100 100" aria-hidden="true">
        <use :href="avatarHref" />
      </svg>
    </div>

    <span class="parental-child-card__name" :title="profile.name">{{ profile.name }}</span>

    <span v-if="activeSession" class="parental-child-card__duration" aria-live="polite">
      <span class="parental-child-card__duration-label">{{ t('views.ninos.card.sessionDuration') }}:</span>
      <span class="parental-child-card__duration-value">{{ formattedDuration }}</span>
    </span>

    <span v-if="isBlocked" class="parental-child-card__blocked-badge" aria-hidden="true">
      {{ t('views.ninos.card.blocked') }}
    </span>

    <div class="parental-child-card__actions">
      <NubiButton
        v-if="activeSession"
        variant="destructive"
        size="sm"
        :aria-label="t('views.ninos.card.expel')"
        @click.stop="$emit('expel', profile.id)"
      >
        {{ t('views.ninos.card.expel') }}
      </NubiButton>
      <NubiButton
        :variant="isBlocked ? 'primary' : 'secondary'"
        size="sm"
        :aria-label="isBlocked ? t('views.ninos.card.unblock') : t('views.ninos.card.block')"
        @click.stop="$emit('toggleBlock', profile.id)"
      >
        {{ isBlocked ? t('views.ninos.card.unblock') : t('views.ninos.card.block') }}
      </NubiButton>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import type { ChildProfileExtended } from '../../services/familyService'
import type { ChildSession } from '../../services/sessionService'
import NubiButton from '../base/NubiButton.vue'

interface Props {
  profile: ChildProfileExtended
  activeSession: ChildSession | null
  isBlocked: boolean
}

const props = defineProps<Props>()

defineEmits<{
  edit: [profileId: number]
  expel: [profileId: number]
  toggleBlock: [profileId: number]
}>()

const { t } = useI18n()

const VALID_AVATARS = ['avatar-1', 'avatar-2', 'avatar-3', 'avatar-4', 'avatar-5', 'avatar-6']

const avatarHref = computed(() => {
  const avatar = VALID_AVATARS.includes(props.profile.avatar) ? props.profile.avatar : 'avatar-1'
  return `/src/assets/icons/custom/child-avatars.svg#${avatar}`
})

const cardAriaLabel = computed(() => {
  const status = props.isBlocked ? ` (${t('views.ninos.card.blocked')})` : ''
  return `${props.profile.name}${status}`
})

const now = ref(Date.now())
let tickTimer: ReturnType<typeof setInterval> | null = null

onMounted(() => {
  tickTimer = setInterval(() => {
    now.value = Date.now()
  }, 1000)
})

onUnmounted(() => {
  if (tickTimer !== null) {
    clearInterval(tickTimer)
    tickTimer = null
  }
})

const formattedDuration = computed(() => {
  if (!props.activeSession) return ''
  const start = new Date(props.activeSession.startedAt).getTime()
  const elapsedSeconds = Math.floor((now.value - start) / 1000)
  const minutes = Math.floor(elapsedSeconds / 60)
  const seconds = elapsedSeconds % 60
  return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
})
</script>

<style scoped>
.parental-child-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--nubi-spacing-sm);
  padding: var(--nubi-spacing-md);
  border: 2px solid var(--nubi-border-default);
  border-radius: var(--nubi-radius-lg);
  background-color: var(--nubi-bg-surface);
  cursor: pointer;
  transition: border-color var(--nubi-duration-fast) var(--nubi-ease-in-out),
              box-shadow var(--nubi-duration-fast) var(--nubi-ease-in-out);
  outline: none;
  min-width: 0;
}

.parental-child-card:hover {
  border-color: var(--nubi-color-primary-light);
  box-shadow: var(--nubi-shadow-sm);
}

.parental-child-card:focus-visible {
  box-shadow: 0 0 0 3px var(--nubi-color-focus);
}

.parental-child-card--blocked {
  opacity: 0.6;
  border-style: dashed;
}

.parental-child-card__avatar {
  width: 64px;
  height: 64px;
  flex-shrink: 0;
}

.parental-child-card__avatar svg {
  width: 100%;
  height: 100%;
  display: block;
}

.parental-child-card__name {
  font-size: var(--nubi-font-size-base);
  font-weight: var(--nubi-font-weight-medium);
  color: var(--nubi-text-primary);
  text-align: center;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: block;
}

.parental-child-card__duration {
  display: flex;
  align-items: center;
  gap: var(--nubi-spacing-xs);
  font-size: var(--nubi-font-size-sm);
  color: var(--nubi-text-secondary);
}

.parental-child-card__duration-label {
  font-weight: var(--nubi-font-weight-normal);
}

.parental-child-card__duration-value {
  font-weight: var(--nubi-font-weight-semibold);
  font-variant-numeric: tabular-nums;
}

.parental-child-card__blocked-badge {
  font-size: var(--nubi-font-size-xs);
  font-weight: var(--nubi-font-weight-semibold);
  color: var(--nubi-text-error);
  background-color: var(--nubi-bg-error);
  padding: 2px 8px;
  border-radius: var(--nubi-radius-full);
}

.parental-child-card__actions {
  display: flex;
  gap: var(--nubi-spacing-xs);
  flex-wrap: wrap;
  justify-content: center;
  margin-top: var(--nubi-spacing-xs);
}
</style>
