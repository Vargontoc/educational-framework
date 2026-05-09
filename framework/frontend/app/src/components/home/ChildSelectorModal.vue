<script setup lang="ts">
import { onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useFamilyStore } from '@/stores/useFamilyStore'
import * as sessionService from '@/services/sessionService'
import Modal from '@/components/ui/Modal.vue'

const { t } = useI18n()
const router = useRouter()
const familyStore = useFamilyStore()

const TITLE_ID = 'child-selector-title'

onMounted(() => {
  familyStore.fetchChildren()
})

function handleSelectChild(childId: number) {
  sessionService.openChildSession(childId)
  router.push('/game/' + childId)
}

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
    <h2 :id="TITLE_ID" class="modal-title">{{ t('modal.children.title') }}</h2>

    <div v-if="familyStore.children.length === 0" class="empty-state">
      <p>{{ t('modal.children.empty') }}</p>
    </div>

    <div v-else class="children-grid">
      <button
        v-for="child in familyStore.children"
        :key="child.id"
        class="child-card"
        @click="handleSelectChild(child.id)"
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
      </button>
    </div>

    <button class="add-child-btn" @click="handleAddChild">
      + {{ t('modal.children.addChild') }}
    </button>
  </Modal>
</template>

<style scoped>
.modal-title {
  font-size: var(--font-size-lg);
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 var(--space-md);
}

.empty-state {
  text-align: center;
  color: #6b7280;
  padding: var(--space-lg) 0;
}

.children-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: var(--space-md);
  margin-bottom: var(--space-md);
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
  cursor: pointer;
  transition: border-color var(--transition-base), transform var(--transition-base);
  min-height: var(--touch-target-min);
}

.child-card:hover {
  border-color: var(--color-primary);
  transform: translateY(-2px);
}

.child-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  overflow: hidden;
  background-color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
}

.child-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.child-avatar-placeholder {
  font-size: var(--font-size-lg);
  font-weight: 700;
  color: var(--color-text-on-primary);
}

.child-name {
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: #1f2937;
  text-align: center;
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
  transition: border-color var(--transition-base), color var(--transition-base);
}

.add-child-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}
</style>
