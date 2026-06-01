<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useFamilyStore } from '@/stores/useFamilyStore'
import Modal from '@/components/ui/Modal.vue'
import childAvatarsSvg from '@/assets/images/child-avatars.svg?url'
import type { ChildProfileResponse } from '@/shared/types/api'

const { t } = useI18n()
const familyStore = useFamilyStore()

const TITLE_ID = 'child-selector-title'
const BLOCKED_WARNING_ID = 'blocked-warning-title'

const emit = defineEmits<{
  select: [child: ChildProfileResponse]
}>()

const PLACEHOLDER_AVATARS = ['avatar-1', 'avatar-2', 'avatar-3', 'avatar-4', 'avatar-5', 'avatar-6']

const blockedWarningOpen = ref(false)
const blockedChildName = ref('')

const isOpen = computed(() => familyStore.activeModal === 'childSelector')

function isPlaceholderAvatar(avatar: string): boolean {
  return PLACEHOLDER_AVATARS.includes(avatar)
}

function handleSelectChild(child: ChildProfileResponse) {
  if (!child.active) return
  emit('select', child)
}

function handleAddChild() {
  familyStore.setActiveModal('addChild')
}

function handleClose() {
  familyStore.setActiveModal(null)
}

watch(isOpen, (open) => {
  if (open) {
    blockedWarningOpen.value = false
    blockedChildName.value = ''
    const blockedChild = familyStore.children.find(c => !c.active)
    if (blockedChild) {
      blockedChildName.value = blockedChild.name
      blockedWarningOpen.value = true
    }
  }
})

watch(() => familyStore.children, (children, oldChildren) => {
  if (!isOpen.value) return

  children.forEach((child, idx) => {
    const oldChild = oldChildren[idx]
    if (oldChild && !oldChild.active && child.active) {
      if (blockedChildName.value === child.name) {
        blockedWarningOpen.value = false
        blockedChildName.value = ''
      }
    }
    if (oldChild && oldChild.active && !child.active) {
      if (isOpen.value) {
        blockedChildName.value = child.name
        blockedWarningOpen.value = true
      }
    }
  })
}, { deep: true })

function handleBlockedWarningClose() {
  blockedWarningOpen.value = false
  blockedChildName.value = ''
}
</script>

<template>
  <Modal
    :open="familyStore.activeModal === 'childSelector'"
    :title-id="TITLE_ID"
    @close="handleClose"
  >
    <div class="modal-inner">
      <h2 :id="TITLE_ID" class="modal-title">{{ t('modal.children.title') }}</h2>

      <div v-if="familyStore.children.length === 0" class="empty-state">
        <p>{{ t('modal.children.empty') }}</p>
      </div>

      <div v-else class="children-grid" role="listbox" :aria-label="t('modal.children.title')">
        <div
          v-for="child in familyStore.children"
          :key="child.id"
          class="child-card"
          :class="{ 'child-card--blocked': !child.active }"
          role="option"
          :aria-label="t('modal.children.cardAriaLabel', { name: child.name })"
          :aria-selected="child.active"
          :aria-disabled="!child.active"
          @click="handleSelectChild(child)"
        >
          <div class="child-avatar">
            <svg
              v-if="isPlaceholderAvatar(child.avatar)"
              class="child-avatar-svg"
              width="96"
              height="96"
              viewBox="0 0 100 100"
              aria-hidden="true"
            >
              <use :href="`${childAvatarsSvg}#${child.avatar}`" />
            </svg>
            <img
              v-else-if="child.avatar"
              :src="child.avatar"
              :alt="child.name"
              class="child-avatar-img"
            />
            <span v-else class="child-avatar-placeholder">
              {{ child.name.charAt(0).toUpperCase() }}
            </span>
          </div>
          <span class="child-name">{{ child.name }}</span>
          <span v-if="!child.active" class="child-blocked-label">
            {{ t('modal.children.blocked') }}
          </span>
        </div>
      </div>

      <button
        class="add-child-btn"
        @click="handleAddChild"
      >
        <span class="add-icon" aria-hidden="true">+</span>
        <span>{{ t('modal.children.addChild') }}</span>
      </button>
    </div>
  </Modal>

    <Modal
      :open="blockedWarningOpen"
      :title-id="BLOCKED_WARNING_ID"
      @close="handleBlockedWarningClose"
    >
      <div class="blocked-warning">
        <div class="blocked-warning-icon" aria-hidden="true">
          <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
            <circle cx="20" cy="20" r="18" stroke="#e53935" stroke-width="2"/>
            <path d="M13 13l14 14M27 13L13 27" stroke="#e53935" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <h2 :id="BLOCKED_WARNING_ID" class="blocked-warning-title">
          {{ t('modal.children.blockedWarningTitle') }}
        </h2>
        <p class="blocked-warning-message">
          {{ t('modal.children.blockedWarningMessage', { name: blockedChildName }) }}
        </p>
        <button
          type="button"
          class="blocked-warning-btn"
          @click="handleBlockedWarningClose"
        >
          {{ t('modal.children.blockedWarningClose') }}
        </button>
      </div>
    </Modal>
</template>

<style scoped>
.modal-inner {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

.modal-title {
  font-size: var(--font-size-lg);
  font-weight: 700;
  color: #1f2937;
  margin: 0;
  text-align: center;
}

.empty-state {
  text-align: center;
  color: #6b7280;
  padding: var(--space-lg) 0;
}

.empty-state p {
  margin: 0;
  font-size: var(--font-size-body);
}

.children-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: var(--space-md);
}

.child-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-md);
  border: 2px solid var(--color-neutral);
  border-radius: var(--radius-md);
  background: #ffffff;
  min-height: 140px;
  cursor: pointer;
}

.child-card--blocked {
  background: color-mix(in srgb, var(--color-neutral) 20%, #f9f9f9);
  cursor: not-allowed;
  opacity: 0.7;
}

.child-card--blocked .child-avatar {
  filter: grayscale(60%);
}

.child-blocked-label {
  font-size: var(--font-size-caption);
  font-weight: 600;
  color: #e53935;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.child-avatar {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  overflow: hidden;
  background-color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.child-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.child-avatar-svg {
  display: block;
  width: 96px;
  height: 96px;
}

.child-avatar-placeholder {
  font-size: var(--font-size-xl);
  font-weight: 700;
  color: var(--color-text-on-primary);
  font-family: var(--font-family-base);
}

.child-name {
  font-size: var(--font-size-sm);
  font-weight: 700;
  color: #1f2937;
  text-align: center;
  font-family: var(--font-family-base);
}

.add-child-btn {
  width: 100%;
  min-height: var(--touch-target-min);
  padding: var(--space-sm) var(--space-md);
  border: 2px dashed var(--color-neutral);
  border-radius: var(--radius-md);
  background: transparent;
  font-size: var(--font-size-md);
  font-family: var(--font-family-base);
  color: #6b7280;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-xs);
  transition: border-color var(--transition-base), color var(--transition-base);
}

.add-child-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.add-icon {
  font-size: var(--font-size-lg);
  line-height: 1;
}

.blocked-warning {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-lg) var(--space-md);
  text-align: center;
}

.blocked-warning-icon {
  display: flex;
}

.blocked-warning-title {
  margin: 0;
  font-size: var(--font-size-lg);
  font-weight: 700;
  color: var(--color-text-primary);
}

.blocked-warning-message {
  margin: 0;
  font-size: var(--font-size-body);
  color: var(--color-text-secondary);
  max-width: 300px;
  line-height: 1.5;
}

.blocked-warning-btn {
  min-width: 200px;
  min-height: var(--touch-target-min);
  padding: var(--space-sm) var(--space-lg);
  border: none;
  border-radius: var(--radius-md);
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
  font-size: var(--font-size-button);
  font-family: var(--font-family-base);
  font-weight: 700;
  cursor: pointer;
  transition: background-color var(--transition-base);
  box-shadow: 0 4px 0 var(--color-primary-dark), 0 18px 28px rgba(43, 91, 224, 0.22);
}

.blocked-warning-btn:hover {
  background-color: var(--color-primary-dark);
}
</style>