<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { ChildProfileResponse, ChildSessionResponse } from '@/shared/types/api'
import childAvatarsSvg from '@/assets/images/child-avatars.svg?url'

const { t } = useI18n()

const props = defineProps<{
  child: ChildProfileResponse
  activeSession: ChildSessionResponse | null
  familyTtsEnabled: boolean
  familyAgentEnabled: boolean
}>()

const emit = defineEmits<{
  edit: []
  block: []
  unblock: []
  closeSession: []
}>()

function handleBlockClick() {
  if (props.child.active) {
    emit('block')
  } else {
    emit('unblock')
  }
}

const sessionDuration = computed(() => {
  if (!props.activeSession?.startedAt) return null
  const started = new Date(props.activeSession.startedAt)
  if (isNaN(started.getTime())) return null
  const now = new Date()
  const diffMs = now.getTime() - started.getTime()
  const totalSeconds = Math.floor(diffMs / 1000)
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  if (minutes > 0) {
    return t('panel.children.durationMinutes', { m: minutes, s: seconds })
  }
  return t('panel.children.durationSeconds', { s: seconds })
})
</script>

<template>
  <article class="child-card">
    <div class="card-header">
      <div class="avatar-wrap" aria-hidden="true">
        <svg class="avatar" width="64" height="64" viewBox="0 0 100 100">
          <use :href="`${childAvatarsSvg}#${child.avatar}`" />
        </svg>
      </div>
      <div class="card-info">
        <h3 class="child-name">{{ child.name }}</h3>
        <div class="status-badges">
          <span
            class="status-badge"
            :class="child.active ? 'status-badge--active' : 'status-badge--blocked'"
            :aria-label="child.active ? t('panel.children.card.activeLabel') : t('panel.children.card.blockedLabel')"
          >
            <svg width="12" height="12" viewBox="0 0 12 12" fill="none" aria-hidden="true">
              <circle cx="6" cy="6" r="5" stroke="currentColor" stroke-width="1.5"/>
              <circle v-if="child.active" cx="6" cy="6" r="3" fill="currentColor"/>
            </svg>
            {{ child.active ? t('panel.children.card.activeLabel') : t('panel.children.card.blockedLabel') }}
          </span>
        </div>
      </div>
    </div>

    <div class="card-flags">
      <div class="flag-item">
        <span class="flag-icon" aria-hidden="true">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <path d="M3 8c0-1.1.9-2 2-2s2 .9 2 2-.9 2-2 2-2-.9-2-2z" fill="currentColor"/>
            <path d="M1 14c1.5-1 3-1.5 5-1.5s3.5.5 5 1.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </span>
        <span class="flag-label">{{ t('panel.children.card.tts') }}</span>
        <span class="flag-value" :class="child.ttsEnabled ? 'flag-value--on' : 'flag-value--off'">
          {{ child.ttsEnabled ? t('panel.children.card.on') : t('panel.children.card.off') }}
        </span>
      </div>
      <div class="flag-item">
        <span class="flag-icon" aria-hidden="true">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <circle cx="8" cy="6" r="3" stroke="currentColor" stroke-width="1.5"/>
            <path d="M2 14c2-1 4-1.5 6-1.5s4 .5 6 1.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </span>
        <span class="flag-label">{{ t('panel.children.card.agent') }}</span>
        <span class="flag-value" :class="child.agentEnabled ? 'flag-value--on' : 'flag-value--off'">
          {{ child.agentEnabled ? t('panel.children.card.on') : t('panel.children.card.off') }}
        </span>
      </div>
    </div>

    <div v-if="activeSession" class="session-row">
      <div class="session-duration">
        <svg class="duration-icon" width="14" height="14" viewBox="0 0 14 14" fill="none" aria-hidden="true">
          <circle cx="7" cy="7" r="6" stroke="currentColor" stroke-width="1.5"/>
          <path d="M7 4v3l2 1" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
        <span>{{ sessionDuration }}</span>
      </div>
      <button
        class="close-session-btn"
        type="button"
        :aria-label="t('panel.children.actions.closeSessionAria', { name: child.name })"
        @click="emit('closeSession')"
      >
        <svg width="14" height="14" viewBox="0 0 14 14" fill="none" aria-hidden="true">
          <path d="M3 10.5L7 4l4 6.5H3z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M5.5 8.5h3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
      </button>
    </div>

    <div class="card-actions">
      <button
        type="button"
        class="action-btn action-btn--edit"
        :aria-label="t('panel.children.actions.editAria', { name: child.name })"
        @click="emit('edit')"
      >
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none" aria-hidden="true">
          <path d="M11 2l3 3-9 9H2v-3l9-9z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
        </svg>
        {{ t('panel.children.actions.edit') }}
      </button>
      <button
        type="button"
        class="action-btn"
        :class="child.active ? 'action-btn--block' : 'action-btn--unblock'"
        :aria-label="child.active
          ? t('panel.children.actions.blockAria', { name: child.name })
          : t('panel.children.actions.unblockAria', { name: child.name })"
        @click="handleBlockClick"
      >
        <svg v-if="child.active" width="16" height="16" viewBox="0 0 16 16" fill="none" aria-hidden="true">
          <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.5"/>
          <path d="M5 5l6 6M11 5l-6 6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
        <svg v-else width="16" height="16" viewBox="0 0 16 16" fill="none" aria-hidden="true">
          <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.5"/>
          <path d="M5 8h6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
        {{ child.active ? t('panel.children.actions.block') : t('panel.children.actions.unblock') }}
      </button>
    </div>
  </article>
</template>

<style scoped>
.child-card {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  padding: var(--space-lg);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  box-shadow: 0 8px 32px rgba(26, 35, 64, 0.08);
}

.card-header {
  display: flex;
  align-items: center;
  gap: var(--space-md);
}

.avatar-wrap {
  flex-shrink: 0;
}

.avatar {
  display: block;
  filter: drop-shadow(0 3px 0 rgba(0,0,0,0.12)) drop-shadow(0 1px 4px rgba(0,0,0,0.08));
}

.card-info {
  flex: 1;
  min-width: 0;
}

.child-name {
  margin: 0 0 var(--space-xs);
  font-size: var(--font-size-md);
  font-weight: 700;
  color: var(--color-text-primary);
}

.status-badges {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-xs);
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px var(--space-sm);
  border-radius: var(--radius-pill);
  font-size: var(--font-size-caption);
  font-weight: 600;
}

.status-badge--active {
  background: color-mix(in srgb, #43a047 12%, transparent);
  color: #2e7d32;
}

.status-badge--blocked {
  background: color-mix(in srgb, #e53935 12%, transparent);
  color: #c62828;
}

.card-flags {
  display: flex;
  gap: var(--space-lg);
}

.flag-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.flag-icon {
  display: flex;
  color: var(--color-text-secondary);
}

.flag-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.flag-value {
  font-size: var(--font-size-sm);
  font-weight: 600;
}

.flag-value--on {
  color: #2e7d32;
}

.flag-value--off {
  color: var(--color-text-secondary);
}

.session-row {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.session-duration {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: var(--color-primary);
}

.close-session-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: background-color var(--transition-base), color var(--transition-base);
}

.close-session-btn:hover {
  background-color: color-mix(in srgb, #e53935 12%, transparent);
  color: #e53935;
}

.card-actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-sm);
  margin-top: auto;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: var(--touch-target-min);
  padding: var(--space-xs) var(--space-md);
  border: 2px solid var(--color-neutral);
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  font-family: var(--font-family-base);
  font-weight: 600;
  cursor: pointer;
  transition: border-color var(--transition-base), color var(--transition-base), background-color var(--transition-base);
}

.action-btn:hover:not(:disabled) {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.action-btn--edit {
  color: var(--color-primary);
  border-color: color-mix(in srgb, var(--color-primary) 30%, transparent);
}

.action-btn--edit:hover:not(:disabled) {
  background-color: color-mix(in srgb, var(--color-primary) 8%, transparent);
}

.action-btn--block {
  color: #e53935;
  border-color: color-mix(in srgb, #e53935 30%, transparent);
}

.action-btn--block:hover:not(:disabled) {
  background-color: color-mix(in srgb, #e53935 8%, transparent);
}

.action-btn--unblock {
  color: #2e7d32;
  border-color: color-mix(in srgb, #2e7d32 30%, transparent);
}

.action-btn--unblock:hover:not(:disabled) {
  background-color: color-mix(in srgb, #2e7d32 8%, transparent);
}
</style>