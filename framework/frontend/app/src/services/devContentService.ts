import api from '@/shared/api/axios'
import type {
  ApiResponse,
  CategoryResponse,
  CreateCategoryRequest,
  UpdateCategoryRequest,
  TopicResponse,
  CreateTopicRequest,
  UpdateTopicRequest
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
