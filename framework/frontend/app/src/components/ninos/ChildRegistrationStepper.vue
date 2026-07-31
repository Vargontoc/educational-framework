<template>
  <div class="child-registration-stepper">
    <NubiStepper
      :steps="stepLabels"
      :model-value="currentStep"
      class="child-registration-stepper__stepper"
      @update:model-value="currentStep = $event"
    >
      <template #default="{ currentStep: step }">
        <div v-if="step === 0" class="child-registration-stepper__step-content">
          <NubiTextInput
            ref="nameInputRef"
            v-model="name"
            :label="t('views.ninos.stepper.step1.nameLabel')"
            :placeholder="t('views.ninos.stepper.step1.namePlaceholder')"
            :required="true"
          />
        </div>
        <div v-else-if="step === 1" class="child-registration-stepper__step-content">
          <div class="child-registration-stepper__field">
            <label class="child-registration-stepper__field-label" for="child-birthday">
              {{ t('views.ninos.stepper.step2.birthdayLabel') }}
              <span class="child-registration-stepper__field-required" aria-hidden="true">*</span>
            </label>
            <input
              id="child-birthday"
              ref="dateInputRef"
              v-model="birthday"
              type="date"
              class="child-registration-stepper__date-input"
              :max="todayDate"
              :aria-label="t('views.ninos.stepper.step2.birthdayLabel')"
            />
          </div>
          <AvatarSelector v-model="avatar" />
        </div>
      </template>
    </NubiStepper>

    <div v-if="submitError" class="child-registration-stepper__error" role="alert" aria-live="assertive">
      {{ submitError }}
    </div>

    <div class="child-registration-stepper__actions">
      <NubiButton variant="secondary" @click="$emit('cancel')">
        {{ t('common.cancel') }}
      </NubiButton>
      <div class="child-registration-stepper__actions-right">
        <NubiButton
          v-if="currentStep > 0"
          variant="secondary"
          @click="currentStep--"
        >
          {{ t('common.back') }}
        </NubiButton>
        <NubiButton
          v-if="currentStep < stepLabels.length - 1"
          :disabled="!canAdvance"
          @click="currentStep++"
        >
          {{ t('common.next') }}
        </NubiButton>
        <NubiButton
          v-if="currentStep === stepLabels.length - 1"
          :disabled="!canSubmit || creating"
          :loading="creating"
          @click="handleCreate"
        >
          {{ t('views.ninos.stepper.createButton') }}
        </NubiButton>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import NubiStepper from '../base/NubiStepper.vue'
import NubiButton from '../base/NubiButton.vue'
import NubiTextInput from '../base/NubiTextInput.vue'
import AvatarSelector from '../home/AvatarSelector.vue'
import { createChild, type CreateChildRequest, type ChildProfileExtended } from '../../services/familyService'

interface Props {
  familyId: number
}

interface Emits {
  childCreated: [profile: ChildProfileExtended]
  cancel: []
}

defineProps<Props>()
const emit = defineEmits<Emits>()

const { t } = useI18n()

const stepLabels = computed(() => [
  t('views.ninos.stepper.step1.title'),
  t('views.ninos.stepper.step2.title')
])

const currentStep = ref(0)
const name = ref('')
const birthday = ref('')
const avatar = ref('avatar-1')
const creating = ref(false)
const submitError = ref('')
const nameInputRef = ref<InstanceType<typeof NubiTextInput> | null>(null)
const dateInputRef = ref<HTMLInputElement | null>(null)

const todayDate = computed(() => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
})

const canAdvance = computed(() => {
  if (currentStep.value === 0) {
    return name.value.trim().length > 0
  }
  return true
})

const canSubmit = computed(() => {
  return name.value.trim().length > 0 &&
         birthday.value.length > 0 &&
         avatar.value.length > 0
})

async function handleCreate(): Promise<void> {
  if (!canSubmit.value) return

  creating.value = true
  submitError.value = ''

  try {
    const request: CreateChildRequest = {
      name: name.value.trim(),
      birthday: birthday.value,
      avatar: avatar.value,
      npcVoiceEnabled: true,
      npcEnabled: true,
      npcVoiceVolume: 100,
      colorVisionMode: 'NONE'
    }

    const result = await createChild(request)

    if (result.success && result.data) {
      emit('childCreated', result.data)
    } else {
      if (result.errorKey === 'validation') {
        submitError.value = result.errorMessage || t('views.ninos.stepper.createError')
      } else if (result.errorKey === 'conflict') {
        submitError.value = t('views.home.childSelection.registration.errorConflict')
      } else if (result.errorKey === 'connection') {
        submitError.value = t('views.home.childSelection.registration.errorConnection')
      } else {
        submitError.value = t('views.ninos.stepper.createError')
      }
    }
  } catch {
    submitError.value = t('views.ninos.stepper.createError')
  } finally {
    creating.value = false
  }
}

watch(currentStep, async () => {
  await nextTick()
  if (currentStep.value === 0) {
    nameInputRef.value?.focus()
  } else if (currentStep.value === 1) {
    dateInputRef.value?.focus()
  }
})
</script>

<style scoped>
.child-registration-stepper {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-md);
}

.child-registration-stepper__stepper {
  width: 100%;
}

:deep(.nubi-stepper__footer) {
  display: none;
}

.child-registration-stepper__step-content {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-lg);
  padding: var(--nubi-spacing-md) 0;
}

.child-registration-stepper__field {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-xs);
}

.child-registration-stepper__field-label {
  font-size: var(--nubi-font-size-sm);
  font-weight: var(--nubi-font-weight-medium);
  color: var(--nubi-text-primary);
}

.child-registration-stepper__field-required {
  color: var(--nubi-color-error);
  margin-left: var(--nubi-spacing-xs);
}

.child-registration-stepper__date-input {
  width: 100%;
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

.child-registration-stepper__date-input:focus {
  border-color: var(--nubi-border-focus);
  box-shadow: 0 0 0 3px var(--nubi-color-focus);
}

.child-registration-stepper__error {
  font-size: var(--nubi-font-size-sm);
  color: var(--nubi-text-error);
  text-align: center;
  padding: var(--nubi-spacing-sm) 0;
}

.child-registration-stepper__actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--nubi-spacing-sm);
  padding-top: var(--nubi-spacing-md);
}

.child-registration-stepper__actions-right {
  display: flex;
  gap: var(--nubi-spacing-sm);
}

@media (max-width: 480px) {
  .child-registration-stepper__actions {
    flex-direction: column;
    gap: var(--nubi-spacing-sm);
  }

  .child-registration-stepper__actions-right {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
