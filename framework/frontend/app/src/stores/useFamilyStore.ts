import { ref } from 'vue'
import { defineStore } from 'pinia'
import type { FamilyResponse, ChildProfileResponse, CreateFamilyRequest, CreateChildProfileRequest } from '@/shared/types/api'
import * as familyService from '@/services/familyService'
import * as childService from '@/services/childService'

export type ViewState = 'loading' | 'noFamily' | 'familyReady' | 'error'

export const useFamilyStore = defineStore('family', () => {
  const viewState = ref<ViewState>('loading')
  const family = ref<FamilyResponse | null>(null)
  const children = ref<ChildProfileResponse[]>([])
  const activeModal = ref<string | null>(null)
  const errorMessage = ref<string | null>(null)

  function setActiveModal(modal: string | null) {
    activeModal.value = modal
  }

  function getHttpStatus(error: unknown): number | undefined {
    const maybeAxiosError = error as {
      status?: number
      response?: { status?: number }
      request?: { status?: number }
    }

    return maybeAxiosError.response?.status ?? maybeAxiosError.status ?? maybeAxiosError.request?.status
  }

  async function fetchFamily() {
    viewState.value = 'loading'
    errorMessage.value = null
    try {
      family.value = await familyService.getFamily()
      viewState.value = 'familyReady'
    } catch (error: unknown) {
      const status = getHttpStatus(error)
      if (status === 404) {
        family.value = null
        viewState.value = 'noFamily'
      } else {
        errorMessage.value = (error as Error).message ?? 'Error loading family'
        viewState.value = 'error'
      }
    }
  }

  async function registerFamily(payload: CreateFamilyRequest) {
    family.value = await familyService.createFamily(payload)
    viewState.value = 'familyReady'
    activeModal.value = null
  }

  async function fetchChildren() {
    try {
      children.value = await childService.getChildren()
    } catch {
      children.value = []
    }
  }

  async function addChild(payload: CreateChildProfileRequest) {
    await childService.createChild(payload)
    await fetchChildren()
  }

  return {
    viewState,
    family,
    children,
    activeModal,
    errorMessage,
    setActiveModal,
    fetchFamily,
    registerFamily,
    fetchChildren,
    addChild
  }
})
