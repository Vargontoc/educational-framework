<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import childAvatarsSvg from '@/assets/images/child-avatars.svg?url'

const { t } = useI18n()

const props = defineProps<{
  modelValue: string | null
  disabled?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string | null]
}>()

const AVATAR_IDS = ['avatar-1', 'avatar-2', 'avatar-3', 'avatar-4', 'avatar-5', 'avatar-6']

function selectAvatar(avatarId: string) {
  if (props.disabled) return
  emit('update:modelValue', avatarId)
}

function handleAvatarKeydown(event: KeyboardEvent, avatarId: string, index: number) {
  if (props.disabled) return
  if (event.key === 'Enter' || event.key === ' ') {
    event.preventDefault()
    selectAvatar(avatarId)
    return
  }
  if (event.key === 'ArrowRight' || event.key === 'ArrowDown') {
    event.preventDefault()
    const nextIndex = (index + 1) % AVATAR_IDS.length
    const nextEl = document.getElementById(`avatar-option-${nextIndex}`)
    nextEl?.focus()
    return
  }
  if (event.key === 'ArrowLeft' || event.key === 'ArrowUp') {
    event.preventDefault()
    const prevIndex = (index - 1 + AVATAR_IDS.length) % AVATAR_IDS.length
    const prevEl = document.getElementById(`avatar-option-${prevIndex}`)
    prevEl?.focus()
  }
}
</script>

<template>
  <div
    class="avatar-grid"
    role="listbox"
    :aria-label="t('avatarPicker.sectionLabel')"
    :aria-disabled="disabled"
  >
    <div
      v-for="(id, index) in AVATAR_IDS"
      :key="id"
      :id="`avatar-option-${index}`"
      class="avatar-option"
      :class="{ 'avatar-option--selected': modelValue === id, 'avatar-option--disabled': disabled }"
      role="option"
      :aria-selected="modelValue === id"
      :aria-label="t('avatarPicker.optionAria', { name: id, index: index + 1, total: AVATAR_IDS.length })"
      :tabindex="disabled ? -1 : 0"
      :aria-disabled="disabled"
      @click="selectAvatar(id)"
      @keydown="(e) => handleAvatarKeydown(e, id, index)"
    >
      <svg
        class="avatar-svg"
        width="64"
        height="64"
        viewBox="0 0 100 100"
        aria-hidden="true"
      >
        <use :href="`${childAvatarsSvg}#${id}`" />
      </svg>
      <span
        v-if="modelValue === id"
        class="avatar-check"
        aria-hidden="true"
      >
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <circle cx="8" cy="8" r="8" fill="#1F2937" fill-opacity="0.6"/>
          <path d="M5 8L7 10L11 6" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </span>
    </div>
  </div>
</template>

<style scoped>
.avatar-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-sm);
  width: 100%;
  max-width: 320px;
}

.avatar-option {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: var(--space-sm);
  border: 2px solid transparent;
  border-radius: var(--radius-md);
  background: transparent;
  cursor: pointer;
  outline: none;
  transition: border-color var(--transition-base), background-color var(--transition-base);
}

.avatar-option:focus-visible {
  border-color: var(--color-primary);
  background-color: color-mix(in srgb, var(--color-primary) 6%, transparent);
}

.avatar-option--selected {
  border-color: var(--color-primary);
  background-color: color-mix(in srgb, var(--color-primary) 8%, transparent);
}

.avatar-option--disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.avatar-svg {
  display: block;
  filter: drop-shadow(0 3px 0 rgba(0,0,0,0.12)) drop-shadow(0 1px 4px rgba(0,0,0,0.08));
}

.avatar-check {
  position: absolute;
  bottom: -2px;
  right: -2px;
}

@media (max-width: 360px) {
  .avatar-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 6px;
  }

  .avatar-svg {
    width: 56px;
    height: 56px;
  }
}
</style>