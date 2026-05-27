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
