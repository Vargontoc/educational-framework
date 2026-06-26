// ---------------------------------------------------------------------------
// API contract types — derived from docs/contracts/api/openapi.json
// ---------------------------------------------------------------------------

/** Generic API envelope used by every endpoint. */
export interface ApiResponse<T> {
  success: boolean
  data: T
  message?: string | null
  errors?: string[] | null
}

// ── Family ────────────────────────────────────────────────────────────────

export interface CreateFamilyRequest {
  name: string
  pin: string
  ttsEnabled: boolean
  agentEnabled: boolean
}

export interface UpdateFamilyRequest {
  name: string
  pin?: string | null
  ttsEnabled?: boolean | null
  agentEnabled?: boolean | null
}

export interface FamilyResponse {
  id: number
  name: string
  ttsEnabled: boolean
  agentEnabled: boolean
  createdAt: string
  updatedAt?: string | null
}

// ── Auth ──────────────────────────────────────────────────────────────────

export interface LoginRequest {
  pin: string
}

export interface LoginResponse {
  token: string
  sessionId: number
  familyId: number
  createdAt: string
}

// ── Child profiles ────────────────────────────────────────────────────────

export interface CreateChildProfileRequest {
  name: string
  birthday: string
  avatar?: string | null
  ttsEnabled: boolean
  agentEnabled: boolean
}

export interface UpdateChildProfileRequest {
  name: string
  birthday: string
  avatar?: string | null
  ttsEnabled?: boolean | null
  agentEnabled?: boolean | null
}

export interface ChildProfileResponse {
  id: number
  familyId: number
  name: string
  active: boolean
  birthday: string
  avatar: string
  ttsEnabled: boolean
  agentEnabled: boolean
  createdAt: string
  updatedAt?: string | null
}

// ── Child sessions ────────────────────────────────────────────────────────

export interface OpenChildSessionRequest {
  childProfileId: number
  heartbeatIntervalSeconds?: number | null
}

export interface ChildSessionResponse {
  id: number
  childProfileId: number
  familyId: number
  status: 'ACTIVE' | 'EXPIRED' | 'EXPELLED' | 'CLOSED'
  startedAt: string
  endedAt?: string | null
  durationSeconds?: number | null
  lastActivityAt: string
}

// ── Adult profiles ────────────────────────────────────────────────────────

export interface CreateAdultProfileRequest {
  name: string
  birthday: string
  avatar?: string | null
}

export interface UpdateAdultProfileRequest {
  name: string
  birthday: string
  avatar?: string | null
}

export interface AdultProfileResponse {
  id: number
  familyId: number
  name: string
  birthday: string
  avatar: string
  createdAt: string
  updatedAt?: string | null
}

// ── Dev Content: Categories ──────────────────────────────────────────────

export type ContentStatus = 'ACTIVE' | 'INACTIVE' | 'DRAFT'

export interface CreateCategoryRequest {
  name: string
  description?: string | null
  status: ContentStatus
  displayOrder?: number | null
  iconUrl?: string | null
}

export interface UpdateCategoryRequest {
  name: string
  description?: string | null
  status: ContentStatus
  displayOrder?: number | null
  iconUrl?: string | null
}

export interface CategoryResponse {
  id: number
  name: string
  description?: string | null
  status: ContentStatus
  displayOrder?: number | null
  iconUrl?: string | null
  createdAt: string
  updatedAt?: string | null
}

// ── Dev Content: Topics ──────────────────────────────────────────────────

export interface CreateTopicRequest {
  name: string
  description?: string | null
  categoryId: number
  status: ContentStatus
  minAge?: number | null
  maxAge?: number | null
  compatibleVariants?: string[] | null
}

export interface UpdateTopicRequest {
  name: string
  description?: string | null
  categoryId: number
  status: ContentStatus
  minAge?: number | null
  maxAge?: number | null
  compatibleVariants?: string[] | null
}

export interface TopicResponse {
  id: number
  name: string
  description?: string | null
  categoryId: number
  status: ContentStatus
  minAge?: number | null
  maxAge?: number | null
  compatibleVariants: string[]
  createdAt: string
  updatedAt?: string | null
}

// ── Dev Content: Activities ──────────────────────────────────────────────

export type DifficultyCode = 'EASY' | 'MEDIUM' | 'HARD'

export type ResourceType = 'IMAGE' | 'AUDIO' | 'VIDEO'

export interface CreateActivityRequest {
  name: string
  description?: string | null
  gameEngineType?: string | null
  status: ContentStatus
  minAge?: number | null
  maxAge?: number | null
  topicIds?: number[] | null
}

export interface UpdateActivityRequest {
  name: string
  description?: string | null
  gameEngineType?: string | null
  status: ContentStatus
  minAge?: number | null
  maxAge?: number | null
  topicIds?: number[] | null
}

export interface ActivityResponse {
  id: number
  name: string
  description?: string | null
  gameEngineType?: string | null
  status: ContentStatus
  minAge?: number | null
  maxAge?: number | null
  topicIds: number[]
  createdAt: string
  updatedAt?: string | null
}

// ── Dev Content: Difficulty Levels ───────────────────────────────────────

export interface CreateDifficultyLevelRequest {
  activityId: number
  difficultyCode: DifficultyCode
  engineParams?: string | null
  adaptiveThresholdConfig?: string | null
}

export interface UpdateDifficultyLevelRequest {
  difficultyCode: DifficultyCode
  engineParams?: string | null
  adaptiveThresholdConfig?: string | null
}

export interface DifficultyLevelResponse {
  id: number
  activityId: number
  difficultyCode: DifficultyCode
  engineParams?: string | null
  adaptiveThresholdConfig?: string | null
  createdAt: string
  updatedAt?: string | null
}

// ── Dev Content: Activity Resources ──────────────────────────────────────

export interface CreateActivityResourceRequest {
  activityId: number
  topicId?: number | null
  resourceType: ResourceType
  path: string
  metadata?: string | null
}

export interface UpdateActivityResourceRequest {
  topicId?: number | null
  resourceType: ResourceType
  path: string
  metadata?: string | null
}

export interface ActivityResourceResponse {
  id: number
  activityId: number
  topicId?: number | null
  resourceType: ResourceType
  path: string
  metadata?: string | null
  createdAt: string
  updatedAt?: string | null
}

// ── Dev Content: Content Locales ─────────────────────────────────────────

export type LocaleEntityType = 'CATEGORY' | 'TOPIC' | 'ACTIVITY' | 'DIFFICULTY_LEVEL' | 'ACTIVITY_RESOURCE'

export interface CreateContentLocaleRequest {
  entityType: LocaleEntityType
  entityId: number
  localeCode: string
  name: string
  description?: string | null
}

export interface UpdateContentLocaleRequest {
  name: string
  description?: string | null
}

export interface ContentLocaleResponse {
  id: number
  entityType: string
  entityId: number
  localeCode: string
  name: string
  description?: string | null
  createdAt: string
  updatedAt?: string | null
}

// ── Dev Content: Curiosities ─────────────────────────────────────────────

export interface CreateCuriosityRequest {
  text: string
  topicId?: number | null
  minAge?: number | null
  maxAge?: number | null
  tags?: string[] | null
  locale: string
  phoneticHint?: string | null
  status: ContentStatus
}

export interface UpdateCuriosityRequest {
  text: string
  topicId?: number | null
  minAge?: number | null
  maxAge?: number | null
  tags?: string[] | null
  locale: string
  phoneticHint?: string | null
  status: ContentStatus
}

export interface CuriosityResponse {
  id: number
  text: string
  topicId?: number | null
  minAge?: number | null
  maxAge?: number | null
  tags: string[]
  locale: string
  phoneticHint?: string | null
  status: ContentStatus
  createdAt: string
  updatedAt?: string | null
}

// ── Dev Content: Avatar Event Catalog ────────────────────────────────────

export type AvatarEventType = 'ACTIVITY_COMPLETED' | 'ACTIVITY_STARTED' | 'ACTIVITY_FAILED' | 'HELP_REQUESTED' | 'OUT_OF_SCOPE_QUERY' | 'CURIOSITY_REQUESTED'

export type AvatarTone = 'CALM' | 'JOYFUL' | 'ENTHUSIASTIC' | 'SERIOUS' | 'NEUTRAL'

export interface CreateAvatarEventCatalogRequest {
  eventType: AvatarEventType
  tone: AvatarTone
  locale: string
  messageText: string
  status: ContentStatus
}

export interface UpdateAvatarEventCatalogRequest {
  eventType: AvatarEventType
  tone: AvatarTone
  locale: string
  messageText: string
  status: ContentStatus
}

export interface AvatarEventCatalogResponse {
  id: number
  eventType: AvatarEventType
  tone: AvatarTone
  locale: string
  messageText: string
  status: ContentStatus
  createdAt: string
  updatedAt?: string | null
}

// ── WebSocket Contract Types ──────────────────────────────────────────────

export type GameAvatarEventType = 'SESSION_CONNECTED' | 'SESSION_DISCONNECTED'

export interface GameAvatarEvent {
  event: 'GAME_AVATAR_EVENT'
  sessionId: number
  eventType: GameAvatarEventType
  audioAvailable: boolean
  audioId?: string
  text: string
}

export type SessionEventType =
  | 'GAME_STATE_UPDATE'
  | 'SESSION_EXPIRED'
  | 'SESSION_INVALIDATED'
  | 'CHILD_EXPELLED'
  | 'PARENT_BLOCK'
  | 'HEARTBEAT_ACK'
  | 'AUTH_ACK'
  | 'CHILD_TTS_ACTIVATED'
  | 'CHILD_TTS_DEACTIVATED'
  | 'CHILD_AGENT_ACTIVATED'
  | 'CHILD_AGENT_DEACTIVATED'
  | 'GAME_AVATAR_EVENT'
  | 'WORLD_DESTINATION_READY'
  | 'WORLD_STATE_SYNC'
  | 'WORLD_ACTIVITY_STARTED'
  | 'GAME_COMPLETED'
  | 'GAME_ABANDONED'

export interface SessionEvent {
  event: SessionEventType
  sessionId?: number
  payload?: unknown
}

// ── World Map Types ─────────────────────────────────────────────────────────

export interface WorldHostPayload {
  id: number
  code: string
  displayName: string
  visualAssetKey: string | null
}

export interface WorldNarrativeSituationPayload {
  id: number
  code: string
  displayText: string | null
  tone: string | null
}

export interface WorldDiscoveryElementPayload {
  proposalRuntimeId: string
  discoveryElementId: number
  code: string
  displayName: string
  elementType: string
  visualAssetKey: string | null
  interactionCueType: string | null
  hasActivity: boolean
}

export interface WorldDestinationPayload {
  destinationId: string
  host: WorldHostPayload
  narrativeSituation: WorldNarrativeSituationPayload
  biome: string
  discoveryElements: WorldDiscoveryElementPayload[]
}

export interface WorldStateSyncPayload {
  status: 'ACTIVE' | 'INACTIVE_CLOSED' | 'NO_WORLD_STATE'
  destination: WorldDestinationPayload | null
}

export interface WorldActivityStartedPayload {
  gameId: number
  activityId: number
  transition: 'START_GAME'
}
