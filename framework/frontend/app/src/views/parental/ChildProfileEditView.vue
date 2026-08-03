<template>
  <div class="child-profile-edit-view">
    <NubiBreadcrumb :items="breadcrumbItems" />

    <div v-if="loading" class="child-profile-edit-view__loading">
      <NubiSpinner size="lg" :show-label="true" />
    </div>

    <div v-else class="child-profile-edit-view__content">
      <section class="child-profile-edit-view__section">
        <h2>{{ t('views.ninos.edit.sections.basicData.title') }}</h2>

        <NubiTextInput
          :model-value="draft.name"
          @update:model-value="draft.name = $event"
          :label="t('views.ninos.edit.sections.basicData.nameLabel')"
          :required="true"
        />

        <div class="child-profile-edit-view__field">
          <label for="birthday-input" class="child-profile-edit-view__field-label">
            {{ t('views.ninos.edit.sections.basicData.birthdayLabel') }}
          </label>
          <input
            id="birthday-input"
            type="date"
            :value="draft.birthday"
            @input="draft.birthday = ($event.target as HTMLInputElement).value"
            :max="today"
            class="child-profile-edit-view__date-input"
          />
        </div>

        <div class="child-profile-edit-view__field">
          <span class="child-profile-edit-view__field-label">
            {{ t('views.ninos.edit.sections.basicData.avatarLabel') }}
          </span>
          <AvatarSelector
            :model-value="draft.avatar"
            @update:model-value="draft.avatar = $event"
          />
        </div>
      </section>

      <section class="child-profile-edit-view__section">
        <h2>{{ t('views.ninos.edit.sections.audio.title') }}</h2>

        <ToggleWithPercentage
          :model-enabled="draft.npcVoiceEnabled"
          :model-percentage="draft.npcVoiceVolume"
          :label="t('views.ninos.edit.sections.audio.voiceLabel')"
          :disabled="isNpcVoiceDisabledByFamily"
          @update:enabled="draft.npcVoiceEnabled = $event"
          @update:percentage="draft.npcVoiceVolume = $event"
        />
        <p v-if="isNpcVoiceDisabledByFamily" class="child-profile-edit-view__disabled-hint">
          {{ t('views.ninos.edit.disabledByFamily') }}
        </p>

        <NubiToggle
          :model-value="draft.npcEnabled"
          @update:model-value="draft.npcEnabled = $event"
          :label="t('views.ninos.edit.sections.audio.npcLabel')"
          :disabled="isNpcDisabledByFamily"
        />
        <p v-if="isNpcDisabledByFamily" class="child-profile-edit-view__disabled-hint">
          {{ t('views.ninos.edit.disabledByFamily') }}
        </p>
      </section>

      <section class="child-profile-edit-view__section">
        <h2>{{ t('views.ninos.edit.sections.visualAccessibility.title') }}</h2>

        <NubiToggle
          :model-value="visualAccessibilityActive"
          @update:model-value="visualAccessibilityActive = $event"
          :label="t('views.ninos.edit.sections.visualAccessibility.toggleLabel')"
        />

        <div v-if="visualAccessibilityActive" class="child-profile-edit-view__visual-options">
          <ColorVisionCardSelector
            :model-value="draft.colorVisionMode"
            :modes="colorVisionModes"
            @update:model-value="draft.colorVisionMode = $event"
          />
        </div>
      </section>

      <div class="child-profile-edit-view__actions">
        <NubiButton
          @click="handleSave"
          :disabled="!hasChanges || saving"
          :loading="saving"
        >
          {{ t('views.ninos.edit.saveButton') }}
        </NubiButton>

        <NubiButton
          variant="destructive"
          @click="showDeleteModal = true"
        >
          {{ t('views.ninos.edit.deleteButton') }}
        </NubiButton>

        <NubiButton
          variant="secondary"
          @click="handleDashboard"
        >
          {{ t('views.ninos.edit.dashboardButton') }}
        </NubiButton>
      </div>
    </div>

    <NubiConfirmModal
      v-model="showDeleteModal"
      :title="t('views.ninos.edit.deleteModal.title')"
      :message="t('views.ninos.edit.deleteModal.message', { name: profile?.name })"
      confirm-variant="destructive"
      :close-on-overlay="false"
      @confirm="confirmDelete"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { useChildProfileEdit } from '../../composables/useChildProfileEdit'
import { useToast } from '../../composables/useToast'
import { ColorVisionMode, COLOR_VISION_LABELS, COLOR_VISION_DESCRIPTIONS } from '../../types/colorVision'

import NubiBreadcrumb from '../../components/base/NubiBreadcrumb.vue'
import NubiSpinner from '../../components/base/NubiSpinner.vue'
import NubiTextInput from '../../components/base/NubiTextInput.vue'
import NubiToggle from '../../components/base/NubiToggle.vue'
import NubiButton from '../../components/base/NubiButton.vue'
import NubiConfirmModal from '../../components/base/NubiConfirmModal.vue'
import AvatarSelector from '../../components/home/AvatarSelector.vue'
import ToggleWithPercentage from '../../components/config/ToggleWithPercentage.vue'
import ColorVisionCardSelector from '../../components/ninos/ColorVisionCardSelector.vue'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const toast = useToast()

const childId = computed(() => Number(route.params.id))

const {
  profile,
  draft,
  loading,
  saving,
  hasChanges,
  isNpcVoiceDisabledByFamily,
  isNpcDisabledByFamily,
  loadProfile,
  saveChanges,
  deleteProfile
} = useChildProfileEdit()

const today = new Date().toISOString().split('T')[0]

const breadcrumbItems = computed(() => [
  { label: t('views.ninos.title'), to: '/panel/ninos' },
  { label: profile.value?.name || '' }
])

const visualAccessibilityActive = computed({
  get: () => draft.value.colorVisionMode !== 'NONE',
  set: (val: boolean) => {
    draft.value.colorVisionMode = val ? ColorVisionMode.DEUTERANOPIA : ColorVisionMode.NONE
  }
})

const colorVisionModes = computed(() =>
  Object.values(ColorVisionMode).map((value) => ({
    value,
    label: COLOR_VISION_LABELS[value],
    description: COLOR_VISION_DESCRIPTIONS[value]
  }))
)

const showDeleteModal = ref(false)

onMounted(async () => {
  await loadProfile(childId.value)
})

async function handleSave() {
  const success = await saveChanges()
  if (success) {
    toast.success(t('views.ninos.edit.saveSuccess'))
  } else {
    toast.error(t('views.ninos.edit.saveError'))
  }
}

async function confirmDelete() {
  const success = await deleteProfile()
  if (success) {
    toast.success(t('views.ninos.edit.deleteSuccess'))
    router.push({ name: 'PanelNinos' })
  } else {
    toast.error(t('views.ninos.edit.deleteError'))
  }
  showDeleteModal.value = false
}

function handleDashboard() {
  router.push({
    name: 'PanelNinoDashboard',
    params: { id: String(childId.value) },
    state: { name: profile.value?.name }
  })
}
</script>

<style scoped>
.child-profile-edit-view {
  max-width: 720px;
  margin: 0 auto;
  padding: var(--nubi-spacing-lg);
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-lg);
}

.child-profile-edit-view__loading {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 40vh;
}

.child-profile-edit-view__content {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-lg);
}

.child-profile-edit-view__section {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-md);
  padding-bottom: var(--nubi-spacing-md);
  border-bottom: 1px solid var(--nubi-border-default);
}

.child-profile-edit-view__section h2 {
  font-size: var(--nubi-font-size-lg);
  font-weight: var(--nubi-font-weight-semibold);
  color: var(--nubi-text-primary);
  margin: 0;
}

.child-profile-edit-view__field {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-xs);
}

.child-profile-edit-view__field-label {
  font-size: var(--nubi-font-size-sm);
  font-weight: var(--nubi-font-weight-medium);
  color: var(--nubi-text-primary);
}

.child-profile-edit-view__date-input {
  min-height: 48px;
  padding: var(--nubi-spacing-sm) var(--nubi-spacing-md);
  border: var(--nubi-border-width-thick) solid var(--nubi-border-default);
  border-radius: var(--nubi-radius-md);
  background-color: var(--nubi-bg-surface);
  font-size: var(--nubi-font-size-base);
  font-family: var(--nubi-font-family-base);
  color: var(--nubi-text-primary);
  outline: none;
  transition: border-color var(--nubi-duration-fast) var(--nubi-ease-in-out),
              box-shadow var(--nubi-duration-fast) var(--nubi-ease-in-out);
}

.child-profile-edit-view__date-input:focus-visible {
  border-color: var(--nubi-border-focus);
  box-shadow: 0 0 0 3px var(--nubi-color-focus);
}

.child-profile-edit-view__disabled-hint {
  font-size: var(--nubi-font-size-sm);
  color: var(--nubi-text-tertiary);
  font-style: italic;
  margin: 0;
}

.child-profile-edit-view__visual-options {
  padding-left: var(--nubi-spacing-md);
}

.child-profile-edit-view__actions {
  display: flex;
  gap: var(--nubi-spacing-sm);
  flex-wrap: wrap;
  padding-top: var(--nubi-spacing-md);
}

@media (max-width: 640px) {
  .child-profile-edit-view {
    padding: var(--nubi-spacing-md);
  }

  .child-profile-edit-view__actions {
    flex-direction: column;
  }
}
</style>
