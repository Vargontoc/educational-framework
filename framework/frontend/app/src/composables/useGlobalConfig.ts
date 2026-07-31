/**
 * Composable useGlobalConfig
 * 
 * Gestiona el estado local de configuración global familiar según FEAT-005:
 * - persisted: configuración actual desde el backend
 * - draft: borrador en edición local
 * - lastNonZero: últimos valores ≠ 0 para recuperación al reactivar
 * 
 * Lógica de conservación:
 * - Al apagar un control con porcentaje, se conserva el último valor ≠ 0
 * - Al reactivar, se restaura ese valor
 * - Establecer porcentaje a 0 apaga el toggle automáticamente (acción rápida)
 * 
 * Independencia de controles:
 * - audioGeneral NO afecta a npcVoice ni narrativeVoice
 * - npc NO afecta a npcVoice
 * - Cada control es ortogonal
 */

import { ref, computed, type Ref, type ComputedRef } from 'vue'
import type { FamilyData } from './useFamilyStatus'

interface FamilyGlobalConfig {
  audioGeneralEnabled: boolean
  audioGeneralVolume: number
  npcEnabled: boolean
  npcVoiceEnabled: boolean
  npcVoiceVolume: number
  narrativeVoiceEnabled: boolean
  narrativeVoiceVolume: number
}

interface LastNonZeroVolumes {
  audioGeneralVolume: number
  npcVoiceVolume: number
  narrativeVoiceVolume: number
}

/**
 * Secciones con toggle y porcentaje
 */
type PercentageSection = 'audioGeneral' | 'npcVoice' | 'narrativeVoice'

/**
 * Secciones solo con toggle (sin porcentaje)
 */
type ToggleOnlySection = 'npc'

interface UseGlobalConfigReturn {
  /** Configuración persistida desde el backend */
  persisted: Ref<FamilyGlobalConfig>
  /** Borrador en edición local */
  draft: Ref<FamilyGlobalConfig>
  /** Últimos valores ≠ 0 para recuperación */
  lastNonZero: Ref<LastNonZeroVolumes>
  /** Indica si hay cambios pendientes de guardar */
  hasChanges: ComputedRef<boolean>
  /** Inicializa el estado desde datos de familia */
  initialize: (data: FamilyData) => void
  /** Maneja cambio de toggle en secciones con porcentaje */
  onToggleChange: (section: PercentageSection, enabled: boolean) => void
  /** Maneja cambio de toggle en secciones solo toggle */
  onToggleOnlyChange: (section: ToggleOnlySection, enabled: boolean) => void
  /** Maneja cambio de porcentaje */
  onPercentageChange: (section: PercentageSection, value: number) => void
  /** Obtiene solo los campos modificados para envío parcial */
  getModifiedFields: () => Partial<FamilyGlobalConfig>
}

export function useGlobalConfig(): UseGlobalConfigReturn {
  const persisted = ref<FamilyGlobalConfig>({
    audioGeneralEnabled: true,
    audioGeneralVolume: 100,
    npcEnabled: true,
    npcVoiceEnabled: true,
    npcVoiceVolume: 100,
    narrativeVoiceEnabled: true,
    narrativeVoiceVolume: 100
  })

  const draft = ref<FamilyGlobalConfig>({
    audioGeneralEnabled: true,
    audioGeneralVolume: 100,
    npcEnabled: true,
    npcVoiceEnabled: true,
    npcVoiceVolume: 100,
    narrativeVoiceEnabled: true,
    narrativeVoiceVolume: 100
  })

  const lastNonZero = ref<LastNonZeroVolumes>({
    audioGeneralVolume: 100,
    npcVoiceVolume: 100,
    narrativeVoiceVolume: 100
  })

  /**
   * Inicializa el estado desde datos de familia (GET /api/v1/family)
   * Aplica valores por defecto si faltan campos
   */
  function initialize(data: FamilyData) {
    const config: FamilyGlobalConfig = {
      audioGeneralEnabled: data.audioGeneralEnabled ?? true,
      audioGeneralVolume: data.audioGeneralVolume ?? 100,
      npcEnabled: data.npcEnabled ?? true,
      npcVoiceEnabled: data.npcVoiceEnabled ?? true,
      npcVoiceVolume: data.npcVoiceVolume ?? 100,
      narrativeVoiceEnabled: data.narrativeVoiceEnabled ?? true,
      narrativeVoiceVolume: data.narrativeVoiceVolume ?? 100
    }

    persisted.value = { ...config }
    draft.value = { ...config }

    // Inicializar lastNonZero con valores ≠ 0
    lastNonZero.value = {
      audioGeneralVolume: config.audioGeneralVolume !== 0 ? config.audioGeneralVolume : 100,
      npcVoiceVolume: config.npcVoiceVolume !== 0 ? config.npcVoiceVolume : 100,
      narrativeVoiceVolume: config.narrativeVoiceVolume !== 0 ? config.narrativeVoiceVolume : 100
    }
  }

  /**
   * Maneja cambio de toggle en secciones con porcentaje
   * Conserva/restaura valores según las reglas de FEAT-005
   */
  function onToggleChange(section: PercentageSection, enabled: boolean) {
    if (section === 'audioGeneral') {
      draft.value.audioGeneralEnabled = enabled
      if (!enabled) {
        if (draft.value.audioGeneralVolume !== 0) {
          lastNonZero.value.audioGeneralVolume = draft.value.audioGeneralVolume
        }
      } else {
        if (lastNonZero.value.audioGeneralVolume) {
          draft.value.audioGeneralVolume = lastNonZero.value.audioGeneralVolume
        }
      }
    } else if (section === 'npcVoice') {
      draft.value.npcVoiceEnabled = enabled
      if (!enabled) {
        if (draft.value.npcVoiceVolume !== 0) {
          lastNonZero.value.npcVoiceVolume = draft.value.npcVoiceVolume
        }
      } else {
        if (lastNonZero.value.npcVoiceVolume) {
          draft.value.npcVoiceVolume = lastNonZero.value.npcVoiceVolume
        }
      }
    } else if (section === 'narrativeVoice') {
      draft.value.narrativeVoiceEnabled = enabled
      if (!enabled) {
        if (draft.value.narrativeVoiceVolume !== 0) {
          lastNonZero.value.narrativeVoiceVolume = draft.value.narrativeVoiceVolume
        }
      } else {
        if (lastNonZero.value.narrativeVoiceVolume) {
          draft.value.narrativeVoiceVolume = lastNonZero.value.narrativeVoiceVolume
        }
      }
    }
  }

  /**
   * Maneja cambio de toggle en secciones solo toggle (sin porcentaje)
   * No afecta a otros controles (independencia)
   */
  function onToggleOnlyChange(section: ToggleOnlySection, enabled: boolean) {
    if (section === 'npc') {
      draft.value.npcEnabled = enabled
    }
  }

  /**
   * Maneja cambio de porcentaje
   * - Si value === 0: apaga el toggle (acción rápida)
   * - Si value !== 0: actualiza lastNonZero
   */
  function onPercentageChange(section: PercentageSection, value: number) {
    if (section === 'audioGeneral') {
      draft.value.audioGeneralVolume = value
      if (value === 0) {
        draft.value.audioGeneralEnabled = false
      } else {
        lastNonZero.value.audioGeneralVolume = value
      }
    } else if (section === 'npcVoice') {
      draft.value.npcVoiceVolume = value
      if (value === 0) {
        draft.value.npcVoiceEnabled = false
      } else {
        lastNonZero.value.npcVoiceVolume = value
      }
    } else if (section === 'narrativeVoice') {
      draft.value.narrativeVoiceVolume = value
      if (value === 0) {
        draft.value.narrativeVoiceEnabled = false
      } else {
        lastNonZero.value.narrativeVoiceVolume = value
      }
    }
  }

  /**
   * Obtiene solo los campos modificados (diferencias entre draft y persisted)
   * Para envío parcial en PATCH /api/v1/family
   */
  function getModifiedFields(): Partial<FamilyGlobalConfig> {
    const modified: Partial<FamilyGlobalConfig> = {}

    if (draft.value.audioGeneralEnabled !== persisted.value.audioGeneralEnabled) {
      modified.audioGeneralEnabled = draft.value.audioGeneralEnabled
    }
    if (draft.value.audioGeneralVolume !== persisted.value.audioGeneralVolume) {
      modified.audioGeneralVolume = draft.value.audioGeneralVolume
    }
    if (draft.value.npcEnabled !== persisted.value.npcEnabled) {
      modified.npcEnabled = draft.value.npcEnabled
    }
    if (draft.value.npcVoiceEnabled !== persisted.value.npcVoiceEnabled) {
      modified.npcVoiceEnabled = draft.value.npcVoiceEnabled
    }
    if (draft.value.npcVoiceVolume !== persisted.value.npcVoiceVolume) {
      modified.npcVoiceVolume = draft.value.npcVoiceVolume
    }
    if (draft.value.narrativeVoiceEnabled !== persisted.value.narrativeVoiceEnabled) {
      modified.narrativeVoiceEnabled = draft.value.narrativeVoiceEnabled
    }
    if (draft.value.narrativeVoiceVolume !== persisted.value.narrativeVoiceVolume) {
      modified.narrativeVoiceVolume = draft.value.narrativeVoiceVolume
    }

    return modified
  }

  /**
   * Indica si hay cambios pendientes de guardar
   */
  const hasChanges = computed(() => {
    return Object.keys(getModifiedFields()).length > 0
  })

  return {
    persisted,
    draft,
    lastNonZero,
    hasChanges,
    initialize,
    onToggleChange,
    onToggleOnlyChange,
    onPercentageChange,
    getModifiedFields
  }
}
