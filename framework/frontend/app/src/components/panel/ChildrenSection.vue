<script setup lang="ts">
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useFamilyStore } from '@/stores/useFamilyStore'
import ChildCard from './ChildCard.vue'
import EditChildModal from './EditChildModal.vue'
import ConfirmModal from '@/components/shared/ConfirmModal.vue'
import { getActiveChildSessions, expelSession } from '@/services/childSessionService'
import { toggleChildActivation } from '@/services/childService'
import type { ChildProfileResponse, ChildSessionResponse } from '@/shared/types/api'

const { t } = useI18n()
const familyStore = useFamilyStore()

const children = computed(() => familyStore.children)
const family = computed(() => familyStore.family)

const activeSessions = ref<ChildSessionResponse[]>([])
const sessionsError = ref(false)

const editingChild = ref<ChildProfileResponse | null>(null)
const editModalOpen = ref(false)

type ConfirmModalType = 'block' | 'unblock' | 'close'
const confirmOpen = ref(false)
const confirmType = ref<ConfirmModalType>('block')
const confirmingChild = ref<ChildProfileResponse | null>(null)
const actionLoading = ref(false)

let pollTimer: ReturnType<typeof setInterval> | null = null
let pollInFlight = false

function getChildActiveSession(childId: number): ChildSessionResponse | null {
  return activeSessions.value.find(s => s.childProfileId === childId && s.status === 'ACTIVE') ?? null
}

async function loadSessions() {
  if (pollInFlight || !family.value) return
  pollInFlight = true
  sessionsError.value = false
  try {
    const all = await getActiveChildSessions(family.value.id)
    activeSessions.value = all.filter(s => s.status === 'ACTIVE')
  } catch {
    sessionsError.value = true
  } finally {
    pollInFlight = false
  }
}

function startPolling() {
  loadSessions()
  pollTimer = setInterval(loadSessions, 5000)
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
  pollInFlight = false
}

onMounted(async () => {
  await familyStore.fetchFamily()
  await familyStore.fetchChildren()
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})

function handleEdit(child: ChildProfileResponse) {
  editingChild.value = child
  nextTick(() => {
    editModalOpen.value = true
  })
}

function handleBlock(child: ChildProfileResponse) {
  confirmingChild.value = child
  confirmType.value = 'block'
  confirmOpen.value = true
}

function handleUnblock(child: ChildProfileResponse) {
  confirmingChild.value = child
  confirmType.value = 'unblock'
  confirmOpen.value = true
}

function handleCloseSession(child: ChildProfileResponse) {
  confirmingChild.value = child
  confirmType.value = 'close'
  confirmOpen.value = true
}

async function handleConfirmAction() {
  if (!confirmingChild.value || actionLoading.value) return
  actionLoading.value = true
  try {
    if (confirmType.value === 'block' || confirmType.value === 'unblock') {
      await toggleChildActivation(confirmingChild.value.id)
      await familyStore.fetchChildren()
      await loadSessions()
    } else if (confirmType.value === 'close') {
      const session = getChildActiveSession(confirmingChild.value.id)
      if (session) {
        await expelSession(session.id)
        await loadSessions()
      }
    }
    confirmOpen.value = false
  } catch {
    // error handled by service interceptor or shown inline
  } finally {
    actionLoading.value = false
  }
}

function handleChildUpdated(updated: ChildProfileResponse) {
  const idx = children.value.findIndex(c => c.id === updated.id)
  if (idx !== -1) {
    children.value[idx] = updated
  }
  editingChild.value = null
  familyStore.fetchChildren()
  loadSessions()
}

function handleChildDeleted(childId: number) {
  const idx = children.value.findIndex(c => c.id === childId)
  if (idx !== -1) {
    children.value.splice(idx, 1)
  }
  editingChild.value = null
  familyStore.fetchChildren()
  loadSessions()
}

function closeEditModal() {
  editModalOpen.value = false
  editingChild.value = null
}

const confirmTitle = computed(() => {
  if (!confirmingChild.value) return ''
  if (confirmType.value === 'block') return t('panel.children.confirm.blockTitle', { name: confirmingChild.value.name })
  if (confirmType.value === 'unblock') return t('panel.children.confirm.unblockTitle', { name: confirmingChild.value.name })
  return t('panel.children.confirm.closeTitle', { name: confirmingChild.value.name })
})

const confirmMessage = computed(() => {
  if (confirmType.value === 'block') return t('panel.children.confirm.blockMessage')
  if (confirmType.value === 'unblock') return t('panel.children.confirm.unblockMessage')
  return t('panel.children.confirm.closeMessage')
})

const confirmTypeForModal = computed(() => confirmType.value as 'block' | 'unblock' | 'delete' | 'close')
</script>

<template>
  <div class="children-section">
    <div v-if="sessionsError" class="polling-error" role="alert">
      <p>{{ t('panel.children.pollingError') }}</p>
      <button type="button" class="retry-btn" @click="loadSessions">
        {{ t('panel.children.retry') }}
      </button>
    </div>

    <div v-if="children.length === 0" class="empty-state">
      <div class="empty-icon" aria-hidden="true">
        <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
          <circle cx="24" cy="24" r="24" fill="#E8EDF5"/>
          <circle cx="16" cy="18" r="5" stroke="#6B7A99" stroke-width="2"/>
          <path d="M6 38c0-3.31 4.48-6 10-6s10 2.69 10 6" stroke="#6B7A99" stroke-width="2" stroke-linecap="round"/>
          <circle cx="32" cy="20" r="4" stroke="#6B7A99" stroke-width="2"/>
          <path d="M36 38c0-2.21-2.24-4-5-4" stroke="#6B7A99" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </div>
      <p class="empty-text">{{ t('panel.children.empty') }}</p>
    </div>

    <div v-else class="cards-grid">
      <ChildCard
        v-for="child in children"
        :key="child.id"
        :child="child"
        :active-session="getChildActiveSession(child.id)"
        :family-tts-enabled="family?.ttsEnabled ?? true"
        :family-agent-enabled="family?.agentEnabled ?? true"
        @edit="handleEdit(child)"
        @block="handleBlock(child)"
        @unblock="handleUnblock(child)"
        @close-session="handleCloseSession(child)"
      />
    </div>

    <EditChildModal
      v-if="editingChild"
      :open="editModalOpen"
      :child="editingChild"
      :family-tts-enabled="family?.ttsEnabled ?? true"
      :family-agent-enabled="family?.agentEnabled ?? true"
      @close="closeEditModal"
      @updated="handleChildUpdated"
      @deleted="handleChildDeleted"
    />

    <ConfirmModal
      :open="confirmOpen"
      :type="confirmTypeForModal"
      :title="confirmTitle"
      :message="confirmMessage"
      :loading="actionLoading"
      @confirm="handleConfirmAction"
      @cancel="confirmOpen = false"
    />
  </div>
</template>

<style scoped>
.children-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
}

.polling-error {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-md);
  padding: var(--space-sm) var(--space-md);
  border-radius: var(--radius-md);
  background: color-mix(in srgb, #e53935 8%, transparent);
  border: 1px solid color-mix(in srgb, #e53935 20%, transparent);
}

.polling-error p {
  margin: 0;
  color: #e53935;
  font-size: var(--font-size-sm);
}

.retry-btn {
  min-height: var(--touch-target-min);
  padding: var(--space-xs) var(--space-md);
  border: 2px solid #e53935;
  border-radius: var(--radius-md);
  background: transparent;
  color: #e53935;
  font-size: var(--font-size-sm);
  font-family: var(--font-family-base);
  font-weight: 600;
  cursor: pointer;
  transition: background-color var(--transition-base);
}

.retry-btn:hover {
  background-color: color-mix(in srgb, #e53935 8%, transparent);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-md);
  min-height: 320px;
  padding: var(--space-xl);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  box-shadow: 0 8px 32px rgba(26, 35, 64, 0.08);
  text-align: center;
}

.empty-icon {
  display: flex;
}

.empty-text {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: var(--font-size-body);
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: var(--space-lg);
}

@media (max-width: 768px) {
  .cards-grid {
    grid-template-columns: 1fr;
  }
}
</style>