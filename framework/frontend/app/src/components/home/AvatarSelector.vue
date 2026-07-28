<template>
  <div class="avatar-selector" role="radiogroup" :aria-label="label">
    <button
      v-for="avatar in avatars"
      :key="avatar.id"
      type="button"
      role="radio"
      :aria-checked="modelValue === avatar.id"
      :aria-label="avatar.label"
      :class="['avatar-selector__item', { 'avatar-selector__item--selected': modelValue === avatar.id }]"
      @click="$emit('update:modelValue', avatar.id)"
    >
      <svg viewBox="0 0 100 100" aria-hidden="true" class="avatar-selector__svg">
        <use :href="avatar.href" />
      </svg>
    </button>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

interface Props {
  modelValue: string
}

defineProps<Props>()

defineEmits<{
  'update:modelValue': [value: string]
}>()

const { t } = useI18n()

const label = t('views.home.childSelection.registration.avatarLabel')

const avatars = [
  { id: 'avatar-1', href: '/src/assets/icons/custom/child-avatars.svg#avatar-1', label: t('views.home.childSelection.registration.avatar1') },
  { id: 'avatar-2', href: '/src/assets/icons/custom/child-avatars.svg#avatar-2', label: t('views.home.childSelection.registration.avatar2') },
  { id: 'avatar-3', href: '/src/assets/icons/custom/child-avatars.svg#avatar-3', label: t('views.home.childSelection.registration.avatar3') },
  { id: 'avatar-4', href: '/src/assets/icons/custom/child-avatars.svg#avatar-4', label: t('views.home.childSelection.registration.avatar4') },
  { id: 'avatar-5', href: '/src/assets/icons/custom/child-avatars.svg#avatar-5', label: t('views.home.childSelection.registration.avatar5') },
  { id: 'avatar-6', href: '/src/assets/icons/custom/child-avatars.svg#avatar-6', label: t('views.home.childSelection.registration.avatar6') },
]
</script>

<style scoped>
.avatar-selector {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--nubi-spacing-md);
  justify-items: center;
  padding: var(--nubi-spacing-sm) 0;
}

.avatar-selector__item {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  min-width: 48px;
  min-height: 48px;
  padding: var(--nubi-spacing-sm);
  border: 3px solid var(--nubi-border-default);
  border-radius: var(--nubi-radius-full);
  background: none;
  cursor: pointer;
  transition: border-color var(--nubi-duration-fast) var(--nubi-ease-in-out),
              box-shadow var(--nubi-duration-fast) var(--nubi-ease-in-out),
              transform var(--nubi-duration-fast) var(--nubi-ease-in-out);
  outline: none;
}

.avatar-selector__item:hover {
  border-color: var(--nubi-color-primary-light);
  transform: scale(1.05);
}

.avatar-selector__item:focus-visible {
  box-shadow: 0 0 0 3px var(--nubi-color-focus);
}

.avatar-selector__item--selected {
  border-color: var(--nubi-color-primary);
  box-shadow: 0 0 0 3px var(--nubi-color-primary-light);
}

.avatar-selector__svg {
  width: 100%;
  height: 100%;
  display: block;
}

@media (min-width: 768px) {
  .avatar-selector {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
