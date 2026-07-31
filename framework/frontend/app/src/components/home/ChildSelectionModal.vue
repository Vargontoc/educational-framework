<template>
  <NubiInfoModal
    :model-value="modelValue"
    :title="modalTitle"
    :close-on-overlay="currentView === 'selection'"
    @update:model-value="handleModalUpdate"
    @close="handleModalClose"
  >
    <div class="child-selection-modal__content">
      <Transition name="nubi-fade" mode="out-in">
        <div v-if="currentView === 'selection'" key="selection">
          <Transition name="nubi-fade" mode="out-in">
            <div v-if="familyLoading || profilesLoading" key="loading" class="child-selection-modal__state">
              <NubiSpinner size="lg" :label="t('views.home.childSelection.loading')" show-label />
            </div>

            <div v-else-if="familyError || profilesError" key="error" class="child-selection-modal__state">
              <NubiErrorState
                :title="t('views.home.childSelection.errorTitle')"
                :message="familyError || profilesErrorMessage"
                :show-retry="true"
                :retry-label="t('common.retry')"
                @retry="loadData"
              />
            </div>

            <div v-else-if="profiles.length === 0" key="empty" class="child-selection-modal__state">
              <p class="child-selection-modal__empty-text">
                {{ t('views.home.childSelection.noProfiles') }}
              </p>
            </div>

            <div v-else key="content" class="child-selection-modal__grid-wrapper">
              <NubiGrid :items="profiles" :cols="3" :empty-text="t('views.home.childSelection.noProfiles')">
                <template #item="{ item }">
                  <ChildProfileCard
                    :profile="item"
                    :selected="selectedProfileId === item.id"
                    @select="handleSelectProfile(item)"
                  />
                </template>
              </NubiGrid>
            </div>
          </Transition>
        </div>

        <div v-else-if="currentView === 'pin-verification'" key="pin-verification" class="child-selection-modal__pin-view">
          <p class="child-selection-modal__description">
            {{ t('views.home.childSelection.pinVerification.description') }}
          </p>
          <NubiPinInput
            ref="pinInputRef"
            v-model="pin"
            :masked="true"
            :error="pinError"
            :disabled="pinVerifying"
            @complete="handleVerifyPin"
          />
          <div v-if="pinError" class="child-selection-modal__error-message" role="alert" aria-live="assertive">
            {{ pinError }}
          </div>
        </div>

        <div v-else-if="currentView === 'registration'" key="registration" class="child-selection-modal__registration-view">
          <ChildRegistrationStepper
            :family-id="familyId"
            @child-created="handleChildCreated"
            @cancel="handleCancelRegistration"
          />
        </div>
      </Transition>
    </div>

    <template v-if="currentView !== 'registration'" #footer>
      <div v-if="currentView === 'selection'" class="child-selection-modal__footer">
        <NubiButton variant="secondary" @click="handleRegisterChild">
          {{ t('views.home.childSelection.registerChild') }}
        </NubiButton>
      </div>

      <div v-else-if="currentView === 'pin-verification'" class="child-selection-modal__footer child-selection-modal__footer--actions">
        <NubiButton variant="secondary" @click="handleCancelPin">
          {{ t('common.cancel') }}
        </NubiButton>
        <NubiButton
          variant="primary"
          :disabled="pin.length < 4"
          :loading="pinVerifying"
          @click="handleVerifyPin"
        >
          {{ t('views.home.childSelection.pinVerification.verify') }}
        </NubiButton>
      </div>
    </template>
  </NubiInfoModal>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import NubiInfoModal from '../base/NubiInfoModal.vue'
import NubiGrid from '../base/NubiGrid.vue'
import NubiButton from '../base/NubiButton.vue'
import NubiSpinner from '../base/NubiSpinner.vue'
import NubiErrorState from '../base/NubiErrorState.vue'
import NubiPinInput from '../base/NubiPinInput.vue'
import ChildProfileCard from './ChildProfileCard.vue'
import ChildRegistrationStepper from '../ninos/ChildRegistrationStepper.vue'
import { getFamily, verifyPin, type ChildProfile, type ChildProfileExtended } from '../../services/familyService'
import { useChildProfiles } from '../../composables/useChildProfiles'
import { useSessionStore } from '../../stores/session'
import { useParentalAuthStore } from '../../stores/parentalAuth'
import { useToast } from '../../composables/useToast'
import type { ApiError } from '../../services/api'

type ModalView = 'selection' | 'pin-verification' | 'registration'

interface Props {
  modelValue: boolean
}

defineProps<Props>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  close: []
}>()

const { t } = useI18n()
const router = useRouter()
const sessionStore = useSessionStore()
const parentalAuthStore = useParentalAuthStore()
const toast = useToast()
const { profiles, loading: profilesLoading, error: profilesError, errorMessage: profilesErrorMessage, fetchProfiles } = useChildProfiles()

const familyName = ref('')
const familyLoading = ref(false)
const familyError = ref('')
const selectedProfileId = ref<number | null>(null)

const familyId = computed(() => parentalAuthStore.familyId ?? 0)

const currentView = ref<ModalView>('selection')

const pin = ref('')
const pinError = ref('')
const pinVerifying = ref(false)
const pinInputRef = ref<InstanceType<typeof NubiPinInput> | null>(null)

const modalTitle = computed(() => {
  if (currentView.value === 'pin-verification') {
    return t('views.home.childSelection.pinVerification.title')
  }
  if (currentView.value === 'registration') {
    return t('views.home.childSelection.registration.title')
  }
  if (familyName.value) {
    return t('views.home.childSelection.familyTitle', { name: familyName.value })
  }
  return t('views.home.childSelection.familyTitleDefault')
})

function resetFormState(): void {
  pin.value = ''
  pinError.value = ''
  pinVerifying.value = false
}

function resetToSelection(): void {
  currentView.value = 'selection'
  resetFormState()
}

async function loadData(): Promise<void> {
  familyLoading.value = true
  familyError.value = ''

  try {
    const family = await getFamily()
    if (family) {
      familyName.value = family.name
    }
  } catch (err) {
    const apiError = err as ApiError
    familyError.value = apiError.message || 'Error al obtener la familia'
  } finally {
    familyLoading.value = false
  }

  await fetchProfiles()
}

function handleSelectProfile(profile: ChildProfile): void {
  selectedProfileId.value = profile.id
  sessionStore.selectChild(String(profile.id))
  router.replace({ name: 'GameView', params: { childId: String(profile.id) } })
}

function handleRegisterChild(): void {
  currentView.value = 'pin-verification'
}

function handleModalUpdate(value: boolean): void {
  if (!value && currentView.value !== 'selection') {
    resetToSelection()
    return
  }
  emit('update:modelValue', value)
}

function handleModalClose(): void {
  if (currentView.value === 'selection') {
    emit('close')
  }
}

async function handleVerifyPin(): Promise<void> {
  if (pin.value.length < 4) return

  pinVerifying.value = true
  pinError.value = ''

  try {
    const result = await verifyPin(pin.value)
    if (result.success) {
      pin.value = ''
      pinError.value = ''
      currentView.value = 'registration'
    } else {
      if (result.errorKey === 'invalid-pin') {
        pinError.value = t('views.home.childSelection.pinVerification.errorInvalid')
      } else if (result.errorKey === 'connection') {
        pinError.value = t('views.home.childSelection.pinVerification.errorConnection')
      } else {
        pinError.value = t('views.home.childSelection.pinVerification.errorServer')
      }
      pinInputRef.value?.clear()
    }
  } catch {
    pinError.value = t('views.home.childSelection.pinVerification.errorServer')
    pinInputRef.value?.clear()
  } finally {
    pinVerifying.value = false
  }
}

function handleCancelPin(): void {
  resetToSelection()
}

function handleCancelRegistration(): void {
  resetToSelection()
}

function handleChildCreated(profile: ChildProfileExtended): void {
  toast.success(t('views.ninos.registerSuccess', { name: profile.name }))
  resetToSelection()
  fetchProfiles()
}

watch(currentView, async () => {
  await nextTick()
  if (currentView.value === 'pin-verification') {
    pinInputRef.value?.focus()
  }
})

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.child-selection-modal__content {
  min-height: 200px;
}

.child-selection-modal__state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--nubi-spacing-xl) 0;
}

.child-selection-modal__empty-text {
  font-size: var(--nubi-font-size-base);
  color: var(--nubi-text-secondary);
  text-align: center;
  margin: 0;
  line-height: var(--nubi-line-height-relaxed);
}

.child-selection-modal__grid-wrapper {
  padding: var(--nubi-spacing-sm) 0;
}

.child-selection-modal__pin-view {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--nubi-spacing-lg);
  padding: var(--nubi-spacing-md) 0;
}

.child-selection-modal__description {
  font-size: var(--nubi-font-size-base);
  color: var(--nubi-text-secondary);
  text-align: center;
  margin: 0;
  line-height: var(--nubi-line-height-relaxed);
}

.child-selection-modal__registration-view {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-md);
}

.child-selection-modal__error-message {
  font-size: var(--nubi-font-size-sm);
  color: var(--nubi-text-error);
  text-align: center;
  padding: var(--nubi-spacing-sm) 0;
}

.child-selection-modal__footer {
  display: flex;
  justify-content: center;
}

.child-selection-modal__footer--actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--nubi-spacing-sm);
}

.child-selection-modal__footer-right {
  display: flex;
  gap: var(--nubi-spacing-sm);
}

.nubi-fade-enter-active {
  transition: opacity var(--nubi-duration-normal) var(--nubi-ease-out);
}

.nubi-fade-leave-active {
  transition: opacity var(--nubi-duration-fast) var(--nubi-ease-in);
}

.nubi-fade-enter-from,
.nubi-fade-leave-to {
  opacity: 0;
}

@media (max-width: 480px) {
  .child-selection-modal__footer--actions {
    flex-direction: column;
    gap: var(--nubi-spacing-sm);
  }

  .child-selection-modal__footer-right {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
