import { ref, type Ref } from 'vue'
import { toggleChildActivation } from '../services/familyService'

export interface UseChildActivationReturn {
  toggling: Ref<boolean>
  toggleActivation: (childId: number) => Promise<boolean>
}

export function useChildActivation(): UseChildActivationReturn {
  const toggling = ref(false)

  async function toggleActivation(childId: number): Promise<boolean> {
    toggling.value = true
    try {
      return await toggleChildActivation(childId)
    } finally {
      toggling.value = false
    }
  }

  return {
    toggling,
    toggleActivation
  }
}
