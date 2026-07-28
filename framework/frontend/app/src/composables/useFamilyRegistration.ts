import { ref, computed, readonly } from 'vue'
import { createFamily, type CreateFamilyResult } from '../services/familyService'

export type RegistrationStep = 1 | 2
export type RegistrationStatus = 'idle' | 'submitting' | 'success' | 'error'
export type ErrorKey = 'validation' | 'conflict' | 'server' | 'connection' | 'pin-mismatch' | null

export function useFamilyRegistration() {
  const step = ref<RegistrationStep>(1)
  const familyName = ref('')
  const pin = ref('')
  const confirmPin = ref('')
  const status = ref<RegistrationStatus>('idle')
  const errorKey = ref<ErrorKey>(null)

  const isNameValid = computed(() => familyName.value.trim().length > 0)

  const isPinComplete = computed(() => pin.value.length === 4 && /^\d{4}$/.test(pin.value))

  const isConfirmPinComplete = computed(() => confirmPin.value.length === 4 && /^\d{4}$/.test(confirmPin.value))

  const pinsMatch = computed(() => {
    if (!isPinComplete.value || !isConfirmPinComplete.value) return true
    return pin.value === confirmPin.value
  })

  const canContinueStep1 = computed(() => isNameValid.value)

  const canSubmit = computed(() => {
    return isPinComplete.value && isConfirmPinComplete.value && pinsMatch.value && status.value !== 'submitting'
  })

  const pinError = computed(() => {
    if (!pinsMatch.value) return 'pin-mismatch'
    return null
  })

  const isSubmitting = computed(() => status.value === 'submitting')

  function goToStep2() {
    if (!canContinueStep1.value) return
    step.value = 2
    errorKey.value = null
  }

  function goToStep1() {
    step.value = 1
    errorKey.value = null
  }

  function reset() {
    step.value = 1
    familyName.value = ''
    pin.value = ''
    confirmPin.value = ''
    status.value = 'idle'
    errorKey.value = null
  }

  async function submit(): Promise<CreateFamilyResult> {
    if (!canSubmit.value) {
      if (!pinsMatch.value) {
        errorKey.value = 'pin-mismatch'
      }
      return { success: false, errorKey: 'validation' }
    }

    status.value = 'submitting'
    errorKey.value = null

    const result = await createFamily({
      name: familyName.value.trim(),
      pin: pin.value
    })

    if (result.success) {
      status.value = 'success'
    } else {
      status.value = 'error'
      errorKey.value = result.errorKey ?? 'server'
    }

    return result
  }

  return {
    step: readonly(step),
    familyName: readonly(familyName),
    pin: readonly(pin),
    confirmPin: readonly(confirmPin),
    status: readonly(status),
    errorKey: readonly(errorKey),
    isNameValid,
    isPinComplete,
    isConfirmPinComplete,
    pinsMatch,
    canContinueStep1,
    canSubmit,
    pinError,
    isSubmitting,
    setFamilyName: (value: string) => { familyName.value = value },
    setPin: (value: string) => { pin.value = value },
    setConfirmPin: (value: string) => { confirmPin.value = value },
    goToStep2,
    goToStep1,
    reset,
    submit
  }
}
