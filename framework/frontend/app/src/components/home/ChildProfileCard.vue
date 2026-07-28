<template>
  <div
    :class="['child-profile-card', { 'child-profile-card--selected': selected }]"
    role="button"
    tabindex="0"
    :aria-label="t('views.home.childSelection.selectProfile', { name: profile.name })"
    @click="$emit('select')"
    @keydown.enter="$emit('select')"
    @keydown.space.prevent="$emit('select')"
  >
    <div class="child-profile-card__avatar">
      <svg viewBox="0 0 100 100" aria-hidden="true">
        <use :href="avatarHref" />
      </svg>
    </div>
    <span class="child-profile-card__name" :title="profile.name">{{ profile.name }}</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { ChildProfile } from '../../services/familyService'

interface Props {
  profile: ChildProfile
  selected?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  selected: false
})

defineEmits<{
  select: []
}>()

const { t } = useI18n()

const VALID_AVATARS = ['avatar-1', 'avatar-2', 'avatar-3', 'avatar-4', 'avatar-5', 'avatar-6']

const avatarHref = computed(() => {
  const avatar = VALID_AVATARS.includes(props.profile.avatar) ? props.profile.avatar : 'avatar-1'
  return `/src/assets/icons/custom/child-avatars.svg#${avatar}`
})
</script>

<style scoped>
.child-profile-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--nubi-spacing-sm);
  padding: var(--nubi-spacing-lg);
  min-width: 48px;
  min-height: 48px;
  border: 2px solid var(--nubi-border-default);
  border-radius: var(--nubi-radius-lg);
  background-color: var(--nubi-bg-surface);
  cursor: pointer;
  transition: border-color var(--nubi-duration-normal) var(--nubi-ease-in-out),
              background-color var(--nubi-duration-normal) var(--nubi-ease-in-out),
              box-shadow var(--nubi-duration-normal) var(--nubi-ease-in-out);
  outline: none;
}

.child-profile-card:hover {
  border-color: var(--nubi-color-primary-light);
  background-color: var(--nubi-bg-surface-secondary);
}

.child-profile-card:focus-visible {
  box-shadow: 0 0 0 3px var(--nubi-color-focus);
}

.child-profile-card--selected {
  border-color: var(--nubi-color-primary);
  box-shadow: 0 0 0 2px var(--nubi-color-primary-light);
}

.child-profile-card__avatar {
  width: 80px;
  height: 80px;
  flex-shrink: 0;
}

.child-profile-card__avatar svg {
  width: 100%;
  height: 100%;
  display: block;
}

.child-profile-card__name {
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
</style>
