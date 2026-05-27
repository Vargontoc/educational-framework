import api from '@/shared/api/axios'
import type {
  ApiResponse,
  CategoryResponse,
  CreateCategoryRequest,
  UpdateCategoryRequest,
  TopicResponse,
  CreateTopicRequest,
  UpdateTopicRequest,
  ActivityResponse,
  CreateActivityRequest,
  UpdateActivityRequest,
  DifficultyLevelResponse,
  CreateDifficultyLevelRequest,
  UpdateDifficultyLevelRequest,
  ActivityResourceResponse,
  CreateActivityResourceRequest,
  UpdateActivityResourceRequest
} from '@/shared/types/api'

// ── Categories ───────────────────────────────────────────────────────────

export async function listCategories(): Promise<CategoryResponse[]> {
  const { data } = await api.get<ApiResponse<CategoryResponse[]>>('/api/v1/dev/content/categories')
  return data.data
}

export async function getCategoryById(id: number): Promise<CategoryResponse> {
  const { data } = await api.get<ApiResponse<CategoryResponse>>(`/api/v1/dev/content/categories/${id}`)
  return data.data
}

export async function createCategory(payload: CreateCategoryRequest): Promise<CategoryResponse> {
  const { data } = await api.post<ApiResponse<CategoryResponse>>('/api/v1/dev/content/categories', payload)
  return data.data
}

export async function updateCategory(id: number, payload: UpdateCategoryRequest): Promise<CategoryResponse> {
  const { data } = await api.put<ApiResponse<CategoryResponse>>(`/api/v1/dev/content/categories/${id}`, payload)
  return data.data
}

// ── Topics ───────────────────────────────────────────────────────────────

export async function listTopics(categoryId?: number): Promise<TopicResponse[]> {
  const params = categoryId != null ? { categoryId } : undefined
  const { data } = await api.get<ApiResponse<TopicResponse[]>>('/api/v1/dev/content/topics', { params })
  return data.data
}

export async function getTopicById(id: number): Promise<TopicResponse> {
  const { data } = await api.get<ApiResponse<TopicResponse>>(`/api/v1/dev/content/topics/${id}`)
  return data.data
}

export async function createTopic(payload: CreateTopicRequest): Promise<TopicResponse> {
  const { data } = await api.post<ApiResponse<TopicResponse>>('/api/v1/dev/content/topics', payload)
  return data.data
}

export async function updateTopic(id: number, payload: UpdateTopicRequest): Promise<TopicResponse> {
  const { data } = await api.put<ApiResponse<TopicResponse>>(`/api/v1/dev/content/topics/${id}`, payload)
  return data.data
}

// ── Activities ───────────────────────────────────────────────────────────

export async function listActivities(topicId?: number): Promise<ActivityResponse[]> {
  const params = topicId != null ? { topicId } : undefined
  const { data } = await api.get<ApiResponse<ActivityResponse[]>>('/api/v1/dev/content/activities', { params })
  return data.data
}

export async function getActivityById(id: number): Promise<ActivityResponse> {
  const { data } = await api.get<ApiResponse<ActivityResponse>>(`/api/v1/dev/content/activities/${id}`)
  return data.data
}

export async function createActivity(payload: CreateActivityRequest): Promise<ActivityResponse> {
  const { data } = await api.post<ApiResponse<ActivityResponse>>('/api/v1/dev/content/activities', payload)
  return data.data
}

export async function updateActivity(id: number, payload: UpdateActivityRequest): Promise<ActivityResponse> {
  const { data } = await api.put<ApiResponse<ActivityResponse>>(`/api/v1/dev/content/activities/${id}`, payload)
  return data.data
}

// ── Difficulty Levels ────────────────────────────────────────────────────

export async function listDifficultyLevels(activityId: number): Promise<DifficultyLevelResponse[]> {
  const { data } = await api.get<ApiResponse<DifficultyLevelResponse[]>>('/api/v1/dev/content/difficulty-levels', {
    params: { activityId }
  })
  return data.data
}

export async function createDifficultyLevel(payload: CreateDifficultyLevelRequest): Promise<DifficultyLevelResponse> {
  const { data } = await api.post<ApiResponse<DifficultyLevelResponse>>('/api/v1/dev/content/difficulty-levels', payload)
  return data.data
}

export async function updateDifficultyLevel(id: number, payload: UpdateDifficultyLevelRequest): Promise<DifficultyLevelResponse> {
  const { data } = await api.put<ApiResponse<DifficultyLevelResponse>>(`/api/v1/dev/content/difficulty-levels/${id}`, payload)
  return data.data
}

// ── Activity Resources ───────────────────────────────────────────────────

export async function listActivityResources(activityId: number): Promise<ActivityResourceResponse[]> {
  const { data } = await api.get<ApiResponse<ActivityResourceResponse[]>>('/api/v1/dev/content/activity-resources', {
    params: { activityId }
  })
  return data.data
}

export async function createActivityResource(payload: CreateActivityResourceRequest): Promise<ActivityResourceResponse> {
  const { data } = await api.post<ApiResponse<ActivityResourceResponse>>('/api/v1/dev/content/activity-resources', payload)
  return data.data
}

export async function updateActivityResource(id: number, payload: UpdateActivityResourceRequest): Promise<ActivityResourceResponse> {
  const { data } = await api.put<ApiResponse<ActivityResourceResponse>>(`/api/v1/dev/content/activity-resources/${id}`, payload)
  return data.data
}
