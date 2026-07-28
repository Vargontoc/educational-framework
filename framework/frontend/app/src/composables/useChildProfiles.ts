import { ref } from 'vue'
import { getChildren, type ChildProfile } from '../services/familyService'
import type { ApiError } from '../services/api'

export function useChildProfiles() {
  const profiles = ref<ChildProfile[]>([])
  const loading = ref(false)
  const error = ref(false)
  const errorMessage = ref('')

  async function fetchProfiles(): Promise<void> {
    loading.value = true
    error.value = false
    errorMessage.value = ''
    profiles.value = []

    try {
      profiles.value = await getChildren()
    } catch (err) {
      const apiError = err as ApiError
      error.value = true
      errorMessage.value = apiError.message || 'Error al obtener los perfiles'
    } finally {
      loading.value = false
    }
  }

  return {
    profiles,
    loading,
    error,
    errorMessage,
    fetchProfiles
  }
}
