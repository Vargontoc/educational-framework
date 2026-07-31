/**
 * Tipos de configuración global de familia
 * 
 * Definidos según FEAT-005 y ADR-021:
 * - audioGeneral: estado y volumen del audio general
 * - npc: estado del NPC
 * - npcVoice: estado y volumen de la voz del NPC
 * - narrativeVoice: estado y volumen de la voz narrativa
 * 
 * Contrato: docs/contracts/api/openapi/schemas/family/update-family-request.yaml
 * Respuesta: docs/contracts/api/openapi/schemas/family/family-response.yaml
 */

/**
 * Configuración global de familia para envío parcial (PATCH)
 * Todos los campos son opcionales para permitir actualización parcial
 */
export interface FamilyGlobalConfig {
  audioGeneralEnabled?: boolean
  audioGeneralVolume?: number
  npcEnabled?: boolean
  npcVoiceEnabled?: boolean
  npcVoiceVolume?: number
  narrativeVoiceEnabled?: boolean
  narrativeVoiceVolume?: number
}

/**
 * Payload de actualización de configuración familiar
 * Incluye configuración global y campos opcionales de nombre y PIN
 */
export interface FamilyUpdatePayload extends FamilyGlobalConfig {
  name?: string
  pin?: string
}

/**
 * Estado de configuración global persistida (desde GET)
 * Todos los campos son requeridos (el backend provee valores por defecto)
 */
export interface FamilyGlobalConfigPersisted {
  audioGeneralEnabled: boolean
  audioGeneralVolume: number
  npcEnabled: boolean
  npcVoiceEnabled: boolean
  npcVoiceVolume: number
  narrativeVoiceEnabled: boolean
  narrativeVoiceVolume: number
}

/**
 * Estado local de la configuración familiar
 * - persisted: configuración actual desde el backend
 * - draft: borrador en edición
 * - lastNonZero: últimos valores no-cero para recuperación al reactivar
 */
export interface FamilyConfigState {
  persisted: FamilyGlobalConfigPersisted
  draft: FamilyGlobalConfigPersisted
  lastNonZero: {
    audioGeneralVolume: number
    npcVoiceVolume: number
    narrativeVoiceVolume: number
  }
}

/**
 * Valores por defecto para configuración global
 * Según FEAT-005: todo activo al 100%
 */
export const DEFAULT_FAMILY_CONFIG: FamilyGlobalConfigPersisted = {
  audioGeneralEnabled: true,
  audioGeneralVolume: 100,
  npcEnabled: true,
  npcVoiceEnabled: true,
  npcVoiceVolume: 100,
  narrativeVoiceEnabled: true,
  narrativeVoiceVolume: 100,
}
