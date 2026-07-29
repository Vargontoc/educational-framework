import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUIStore = defineStore('ui', () => {
  const isLoading = ref(false)
  const errorMessage = ref<string | null>(null)
  const modalOpen = ref(false)
  const sidebarOpen = ref(false)

  function toggleSidebar() {
    sidebarOpen.value = !sidebarOpen.value
  }

  function closeSidebar() {
    sidebarOpen.value = false
  }

  return {
    isLoading,
    errorMessage,
    modalOpen,
    sidebarOpen,
    toggleSidebar,
    closeSidebar
  }
})
