import { ref, computed, type Ref, type ComputedRef } from 'vue'
import {
  getChild,
  updateChild,
  deleteChild,
  getFamily,
  type ChildProfileExtended,
  type UpdateChildProfileRequest
} from '../services/familyService'
import type { FamilyData } from './useFamilyStatus'

export interface ChildProfileDraft {
  name: string
  birthday: string
  avatar: string
  npcVoiceEnabled: boolean
  npcVoiceVolume: number
  npcEnabled: boolean
  colorVisionMode: string
}

export interface UseChildProfileEditReturn {
  profile: Ref<ChildProfileExtended | null>
  draft: Ref<ChildProfileDraft>
  loading: Ref<boolean>
  saving: Ref<boolean>
  error: Ref<boolean>
  errorMessage: Ref<string>
  hasChanges: ComputedRef<boolean>
  isNpcVoiceDisabledByFamily: ComputedRef<boolean>
  isNpcDisabledByFamily: ComputedRef<boolean>
  loadProfile: (id: number) => Promise<void>
  saveChanges: () => Promise<boolean>
  deleteProfile: () => Promise<boolean>
}

function profileToDraft(profile: ChildProfileExtended): ChildProfileDraft {
  return {
    name: profile.name,
    birthday: profile.birthday,
    avatar: profile.avatar,
    npcVoiceEnabled: profile.npcVoiceEnabled,
    npcVoiceVolume: profile.npcVoiceVolume,
    npcEnabled: profile.npcEnabled,
    colorVisionMode: profile.colorVisionMode
  }
}

export function useChildProfileEdit(): UseChildProfileEditReturn {
  const profile = ref<ChildProfileExtended | null>(null)
  const draft = ref<ChildProfileDraft>({
    name: '',
    birthday: '',
    avatar: 'avatar-1',
    npcVoiceEnabled: true,
    npcVoiceVolume: 100,
    npcEnabled: true,
    colorVisionMode: 'NONE'
  })
  const loading = ref(false)
  const saving = ref(false)
  const error = ref(false)
  const errorMessage = ref('')
  const familyConfig = ref<FamilyData | null>(null)

  const hasChanges = computed(() => {
    if (!profile.value) return false
    const p = profile.value
    const d = draft.value
    return (
      d.name !== p.name ||
      d.birthday !== p.birthday ||
      d.avatar !== p.avatar ||
      d.npcVoiceEnabled !== p.npcVoiceEnabled ||
      d.npcVoiceVolume !== p.npcVoiceVolume ||
      d.npcEnabled !== p.npcEnabled ||
      d.colorVisionMode !== p.colorVisionMode
    )
  })

  const isNpcVoiceDisabledByFamily = computed(() => {
    return familyConfig.value?.npcVoiceEnabled === false
  })

  const isNpcDisabledByFamily = computed(() => {
    return familyConfig.value?.npcEnabled === false
  })

  async function loadProfile(id: number): Promise<void> {
    loading.value = true
    error.value = false
    errorMessage.value = ''

    try {
      const [childData, familyData] = await Promise.all([
        getChild(id),
        getFamily()
      ])
      profile.value = childData
      draft.value = profileToDraft(childData)
      familyConfig.value = familyData
    } catch (err) {
      error.value = true
      errorMessage.value = (err as { message?: string }).message || 'Error al cargar el perfil'
    } finally {
      loading.value = false
    }
  }

  async function saveChanges(): Promise<boolean> {
    if (!profile.value) return false

    saving.value = true
    error.value = false
    errorMessage.value = ''

    try {
      const p = profile.value
      const d = draft.value
      const payload: UpdateChildProfileRequest = {}

      if (d.name !== p.name) payload.name = d.name
      if (d.birthday !== p.birthday) payload.birthday = d.birthday
      if (d.avatar !== p.avatar) payload.avatar = d.avatar
      if (d.npcVoiceEnabled !== p.npcVoiceEnabled) payload.npcVoiceEnabled = d.npcVoiceEnabled
      if (d.npcVoiceVolume !== p.npcVoiceVolume) payload.npcVoiceVolume = d.npcVoiceVolume
      if (d.npcEnabled !== p.npcEnabled) payload.npcEnabled = d.npcEnabled
      if (d.colorVisionMode !== p.colorVisionMode) payload.colorVisionMode = d.colorVisionMode

      if (Object.keys(payload).length === 0) return true

      const updated = await updateChild(profile.value.id, payload)
      profile.value = updated
      draft.value = profileToDraft(updated)
      return true
    } catch (err) {
      error.value = true
      errorMessage.value = (err as { message?: string }).message || 'Error al guardar los cambios'
      return false
    } finally {
      saving.value = false
    }
  }

  async function deleteProfile(): Promise<boolean> {
    if (!profile.value) return false

    saving.value = true
    error.value = false
    errorMessage.value = ''

    try {
      const success = await deleteChild(profile.value.id)
      if (success) {
        profile.value = null
      }
      return success
    } catch (err) {
      error.value = true
      errorMessage.value = (err as { message?: string }).message || 'Error al eliminar el perfil'
      return false
    } finally {
      saving.value = false
    }
  }

  return {
    profile,
    draft,
    loading,
    saving,
    error,
    errorMessage,
    hasChanges,
    isNpcVoiceDisabledByFamily,
    isNpcDisabledByFamily,
    loadProfile,
    saveChanges,
    deleteProfile
  }
}
