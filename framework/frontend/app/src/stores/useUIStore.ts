import { ref } from 'vue'
import { defineStore } from 'pinia'

export const useUIStore = defineStore('ui', () => {
  const isLoading = ref(false)
  const errorMessage = ref<string | null>(null)
  const modalOpen = ref(false)

  return { isLoading, errorMessage, modalOpen }
})
