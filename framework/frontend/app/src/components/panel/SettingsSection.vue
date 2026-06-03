<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useFamilyStore } from '@/stores/useFamilyStore'
import * as familyService from '@/services/familyService'
import SettingsCard from './SettingsCard.vue'
import ToggleWithConfirm from './ToggleWithConfirm.vue'
import PinUpdateModal from './PinUpdateModal.vue'

const { t } = useI18n()
const familyStore = useFamilyStore()

const loading = ref(false)
const errorMsg = ref<string | null>(null)

onMounted(async () => {
  loading.value = true
  errorMsg.value = null
  try {
    await familyStore.fetchFamily()
  } catch {
    errorMsg.value = t('panel.settings.error')
  } finally {
    loading.value = false
  }
})

const ttsToggleOpen = ref(false)
const agentToggleOpen = ref(false)
const pinModalOpen = ref(false)

const ttsEnabled = computed(() => familyStore.family?.ttsEnabled ?? false)
const agentEnabled = computed(() => familyStore.family?.agentEnabled ?? false)

function openTtsToggle() {
  ttsToggleOpen.value = true
}

function openAgentToggle() {
  agentToggleOpen.value = true
}

function openPinModal() {
  pinModalOpen.value = true
}

async function handleTtsConfirm() {
  if (loading.value) return
  loading.value = true
  errorMsg.value = null
  try {
    await familyService.updateFamily({
      name: familyStore.family?.name ?? '',
      pin: null,
      ttsEnabled: !ttsEnabled.value,
      agentEnabled: familyStore.family?.agentEnabled ?? false
    })
    await familyStore.fetchFamily()
    ttsToggleOpen.value = false
  } catch {
    errorMsg.value = t('panel.settings.tts.updateError')
  } finally {
    loading.value = false
  }
}

async function handleAgentConfirm() {
  if (loading.value) return
  loading.value = true
  errorMsg.value = null
  try {
    await familyService.updateFamily({
      name: familyStore.family?.name ?? '',
      pin: null,
      ttsEnabled: familyStore.family?.ttsEnabled ?? false,
      agentEnabled: !agentEnabled.value
    })
    await familyStore.fetchFamily()
    agentToggleOpen.value = false
  } catch {
    errorMsg.value = t('panel.settings.agent.updateError')
  } finally {
    loading.value = false
  }
}

function closeTtsToggle() {
  if (!loading.value) ttsToggleOpen.value = false
}

function closeAgentToggle() {
  if (!loading.value) agentToggleOpen.value = false
}

function closePinModal() {
  pinModalOpen.value = false
}
</script>

<template>
  <div class="settings-section">
    <div v-if="loading && !familyStore.family" class="loading-state">
      <p>{{ t('panel.settings.loading') }}</p>
    </div>

    <div v-else-if="errorMsg && !familyStore.family" class="error-state">
      <p role="alert">{{ errorMsg }}</p>
      <button type="button" class="retry-btn" @click="familyStore.fetchFamily()">
        {{ t('panel.settings.retry') }}
      </button>
    </div>

    <div v-else class="settings-list">
      <SettingsCard
        :title="t('panel.settings.tts.label')"
        :description="t('panel.settings.tts.description')"
      >
        <div class="toggle-control">
          <button
            type="button"
            class="toggle-btn"
            :class="{ 'toggle-btn--active': ttsEnabled }"
            :aria-label="ttsEnabled ? t('panel.settings.tts.enabled') : t('panel.settings.tts.disabled')"
            @click="openTtsToggle"
          >
            <span class="toggle-indicator" aria-hidden="true" />
            <span class="toggle-label">{{ ttsEnabled ? t('panel.settings.tts.enabled') : t('panel.settings.tts.disabled') }}</span>
          </button>
        </div>
      </SettingsCard>

      <SettingsCard
        :title="t('panel.settings.agent.label')"
        :description="t('panel.settings.agent.description')"
      >
        <div class="toggle-control">
          <button
            type="button"
            class="toggle-btn"
            :class="{ 'toggle-btn--active': agentEnabled }"
            :aria-label="agentEnabled ? t('panel.settings.agent.enabled') : t('panel.settings.agent.disabled')"
            @click="openAgentToggle"
          >
            <span class="toggle-indicator" aria-hidden="true" />
            <span class="toggle-label">{{ agentEnabled ? t('panel.settings.agent.enabled') : t('panel.settings.agent.disabled') }}</span>
          </button>
        </div>
      </SettingsCard>

      <SettingsCard
        :title="t('panel.settings.pin.label')"
        :description="t('panel.settings.pin.description')"
      >
        <button
          type="button"
          class="pin-change-btn"
          :aria-label="t('panel.settings.pin.label')"
          @click="openPinModal"
        >
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none" aria-hidden="true">
            <rect x="3" y="8" width="14" height="10" rx="2" stroke="currentColor" stroke-width="1.5"/>
            <path d="M7 8V5a3 3 0 016 0v3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <span>{{ t('panel.settings.pin.label') }}</span>
        </button>
      </SettingsCard>
    </div>

    <ToggleWithConfirm
      :open="ttsToggleOpen"
      type="tts"
      :current-value="ttsEnabled"
      :loading="loading"
      @confirm="handleTtsConfirm"
      @cancel="closeTtsToggle"
    />

    <ToggleWithConfirm
      :open="agentToggleOpen"
      type="agent"
      :current-value="agentEnabled"
      :loading="loading"
      @confirm="handleAgentConfirm"
      @cancel="closeAgentToggle"
    />

    <PinUpdateModal
      v-if="pinModalOpen"
      @close="closePinModal"
    />
  </div>
</template>

<style scoped>
.settings-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
}

.loading-state,
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-md);
  min-height: 200px;
  padding: var(--space-xl);
  background: var(--color-surface);
  border-radius: var(--radius-md);
  text-align: center;
}

.loading-state p,
.error-state p {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: var(--font-size-body);
}

.error-state p {
  color: var(--color-error-adult);
}

.retry-btn {
  min-height: var(--touch-target-min);
  padding: var(--space-sm) var(--space-lg);
  border: 2px solid var(--color-error-adult);
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--color-error-adult);
  font-size: var(--font-size-button);
  font-family: var(--font-family-base);
  font-weight: 600;
  cursor: pointer;
  transition: background-color var(--transition-base);
}

.retry-btn:hover {
  background-color: color-mix(in srgb, var(--color-error-adult) 8%, transparent);
}

.settings-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

.toggle-control {
  display: flex;
  align-items: center;
}

.toggle-btn {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  min-height: var(--touch-target-min);
  padding: var(--space-sm) var(--space-md);
  border: 2px solid var(--color-neutral);
  border-radius: var(--radius-md);
  background: transparent;
  cursor: pointer;
  transition: border-color var(--transition-base), background-color var(--transition-base);
}

.toggle-btn:hover {
  border-color: var(--color-primary);
}

.toggle-btn--active {
  border-color: var(--color-success-adult);
  background-color: color-mix(in srgb, var(--color-success-adult) 8%, transparent);
}

.toggle-indicator {
  width: 36px;
  height: 20px;
  border-radius: 10px;
  background-color: var(--color-neutral);
  position: relative;
  transition: background-color var(--transition-base);
}

.toggle-indicator::after {
  content: '';
  position: absolute;
  top: 2px;
  left: 2px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background-color: var(--color-text-secondary);
  transition: transform var(--transition-base), background-color var(--transition-base);
}

.toggle-btn--active .toggle-indicator {
  background-color: color-mix(in srgb, var(--color-success-adult) 30%, transparent);
}

.toggle-btn--active .toggle-indicator::after {
  transform: translateX(16px);
  background-color: var(--color-success-adult);
}

.toggle-label {
  font-size: var(--font-size-button);
  font-family: var(--font-family-base);
  font-weight: 600;
  color: var(--color-text-primary);
}

.pin-change-btn {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  min-height: var(--touch-target-min);
  padding: var(--space-sm) var(--space-md);
  border: 2px solid var(--color-primary);
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--color-primary);
  font-size: var(--font-size-button);
  font-family: var(--font-family-base);
  font-weight: 600;
  cursor: pointer;
  transition: background-color var(--transition-base), color var(--transition-base);
}

.pin-change-btn:hover {
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
}
</style>