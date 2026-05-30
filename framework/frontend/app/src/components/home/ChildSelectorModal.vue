<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useFamilyStore } from '@/stores/useFamilyStore'
import Modal from '@/components/ui/Modal.vue'

const { t } = useI18n()
const familyStore = useFamilyStore()

const TITLE_ID = 'child-selector-title'

function handleAddChild() {
  familyStore.setActiveModal('addChild')
}

function handleClose() {
  familyStore.setActiveModal(null)
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
          role="option"
          :aria-label="t('modal.children.cardAriaLabel', { name: child.name })"
        >
          <div class="child-avatar">
            <img
              v-if="child.avatar"
              :src="child.avatar"
              :alt="child.name"
              class="child-avatar-img"
            />
            <span v-else class="child-avatar-placeholder">
              {{ child.name.charAt(0).toUpperCase() }}
            </span>
          </div>
          <span class="child-name">{{ child.name }}</span>
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
</style>