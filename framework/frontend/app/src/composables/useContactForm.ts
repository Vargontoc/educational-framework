import { ref, computed } from 'vue'
import { contactService, type ContactMessageType } from '../services/contactService'
import type { ApiError } from '../services/api'

export function useContactForm() {
  const messageType = ref<ContactMessageType>('COMMENT')
  const message = ref('')
  const isAdultConfirmed = ref(false)
  const isSubmitting = ref(false)
  const submitError = ref<string | null>(null)
  const submitSuccess = ref(false)

  const isValid = computed(() =>
    message.value.trim().length > 0 &&
    message.value.length <= 2000 &&
    isAdultConfirmed.value
  )

  const messageError = computed(() => {
    if (message.value.length > 2000) return 'El mensaje es demasiado largo (máximo 2000 caracteres)'
    return null
  })

  async function submit(): Promise<void> {
    if (!isValid.value) return
    isSubmitting.value = true
    submitError.value = null
    submitSuccess.value = false

    try {
      await contactService.sendMessage(messageType.value, message.value.trim())
      submitSuccess.value = true
      message.value = ''
      messageType.value = 'COMMENT'
      isAdultConfirmed.value = false
    } catch (error) {
      const apiError = error as ApiError
      if (apiError.status === 429) {
        submitError.value = 'Has realizado demasiados intentos. Inténtalo más tarde.'
      } else if (apiError.status === 400) {
        submitError.value = 'El mensaje no es válido. Revisa el contenido e inténtalo de nuevo.'
      } else {
        submitError.value = 'No se ha podido enviar el mensaje. Inténtalo más tarde.'
      }
    } finally {
      isSubmitting.value = false
    }
  }

  return {
    messageType,
    message,
    isAdultConfirmed,
    isSubmitting,
    submitError,
    submitSuccess,
    isValid,
    messageError,
    submit
  }
}
