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
