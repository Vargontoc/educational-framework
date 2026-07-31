<template>
  <div class="ninos-view">
    <NubiBreadcrumb :items="breadcrumbItems" />

    <h1 class="ninos-view__title">{{ t('views.ninos.title') }}</h1>

    <div v-if="loading" class="ninos-view__loading">
      <NubiSpinner size="lg" :show-label="true" />
    </div>

    <div v-else class="ninos-view__content">
      <div class="ninos-view__grid">
        <ParentalChildCard
          v-for="profile in profiles"
          :key="profile.id"
          :profile="profile"
          :active-session="activeSessionByChildId.get(profile.id) ?? null"
          :is-blocked="!profile.active"
          @edit="handleEdit"
          @expel="handleExpel"
          @toggle-block="handleToggleBlock"
        />
      </div>

      <div v-if="profiles.length === 0" class="ninos-view__empty">
        <p>{{ t('views.ninos.noProfiles') }}</p>
      </div>

      <div class="ninos-view__register">
        <NubiButton @click="showRegisterModal = true">
          {{ t('views.ninos.registerButton') }}
        </NubiButton>
      </div>
    </div>

    <NubiConfirmModal
      v-model="expelModalVisible"
      :title="t('views.ninos.expelModal.title')"
      :message="t('views.ninos.expelModal.message', { name: expelTargetName })"
      confirm-variant="destructive"
      @confirm="confirmExpel"
    />

    <NubiInfoModal
      v-model="showRegisterModal"
      :title="t('views.ninos.registerButton')"
      :close-on-overlay="false"
    >
      <ChildRegistrationStepper
        :family-id="familyId"
        @child-created="handleChildCreated"
        @cancel="closeRegisterModal"
      />
    </NubiInfoModal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useChildProfiles } from '../composables/useChildProfiles'
import { useChildSessions } from '../composables/useChildSessions'
import { useChildActivation } from '../composables/useChildActivation'
import { useToast } from '../composables/useToast'
import { useParentalAuthStore } from '../stores/parentalAuth'
import NubiBreadcrumb from '../components/base/NubiBreadcrumb.vue'
import NubiSpinner from '../components/base/NubiSpinner.vue'
import NubiButton from '../components/base/NubiButton.vue'
import NubiConfirmModal from '../components/base/NubiConfirmModal.vue'
import NubiInfoModal from '../components/base/NubiInfoModal.vue'
import ParentalChildCard from '../components/ninos/ParentalChildCard.vue'
import ChildRegistrationStepper from '../components/ninos/ChildRegistrationStepper.vue'
import type { ChildProfileExtended } from '../services/familyService'

const { t } = useI18n()
const router = useRouter()
const parentalAuthStore = useParentalAuthStore()
const toast = useToast()

const { profiles, loading, fetchProfiles } = useChildProfiles()
const { activeSessionByChildId, startPolling, stopPolling, expelChild } = useChildSessions()
const { toggleActivation } = useChildActivation()

const breadcrumbItems = computed(() => [
  { label: t('views.panel.title'), to: '/panel' },
  { label: t('views.ninos.title') }
])

const showRegisterModal = ref(false)
const expelModalVisible = ref(false)
const expelTargetId = ref<number | null>(null)
const expelTargetName = ref('')

const familyId = computed(() => parentalAuthStore.familyId ?? 0)

onMounted(() => {
  fetchProfiles()
  const fid = parentalAuthStore.familyId
  if (fid !== null) {
    startPolling(fid, 5000)
  }
})

onUnmounted(() => {
  stopPolling()
})

function handleEdit(profileId: number): void {
  router.push({ name: 'PanelNinoEdit', params: { id: String(profileId) } })
}

function handleExpel(profileId: number): void {
  const profile = profiles.value.find(p => p.id === profileId)
  if (!profile) return
  expelTargetId.value = profileId
  expelTargetName.value = profile.name
  expelModalVisible.value = true
}

async function confirmExpel(): Promise<void> {
  if (expelTargetId.value === null) return
  const session = activeSessionByChildId.value.get(expelTargetId.value)
  if (!session) return

  const success = await expelChild(session.id)
  if (success) {
    toast.success(t('views.ninos.expelSuccess'))
  } else {
    toast.error(t('views.ninos.expelError'))
  }
  expelModalVisible.value = false
}

async function handleToggleBlock(profileId: number): Promise<void> {
  const success = await toggleActivation(profileId)
  if (success) {
    await fetchProfiles()
    toast.success(t('views.ninos.blockSuccess'))
  } else {
    toast.error(t('views.ninos.blockError'))
  }
}

function closeRegisterModal(): void {
  showRegisterModal.value = false
}

function handleChildCreated(profile: ChildProfileExtended): void {
  toast.success(t('views.ninos.registerSuccess', { name: profile.name }))
  closeRegisterModal()
  fetchProfiles()
}
</script>

<style scoped>
.ninos-view {
  padding: var(--nubi-spacing-lg);
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-lg);
}

.ninos-view__title {
  font-size: var(--nubi-font-size-xl);
  font-weight: var(--nubi-font-weight-bold);
  color: var(--nubi-text-primary);
  margin: 0;
}

.ninos-view__loading {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 40vh;
}

.ninos-view__content {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-lg);
}

.ninos-view__grid {
  display: grid;
  gap: var(--nubi-spacing-md);
  grid-template-columns: repeat(2, 1fr);
}

@media (min-width: 768px) {
  .ninos-view__grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (min-width: 1024px) {
  .ninos-view__grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

.ninos-view__empty {
  text-align: center;
  padding: var(--nubi-spacing-xl);
  color: var(--nubi-text-secondary);
  font-size: var(--nubi-font-size-base);
}

.ninos-view__register {
  display: flex;
  justify-content: center;
  padding-top: var(--nubi-spacing-md);
}
</style>
