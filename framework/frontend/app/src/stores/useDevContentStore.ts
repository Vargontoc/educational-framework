import { ref } from 'vue'
import { defineStore } from 'pinia'
import type {
  CategoryResponse,
  CreateCategoryRequest,
  UpdateCategoryRequest,
  TopicResponse,
  CreateTopicRequest,
  UpdateTopicRequest
} from '@/shared/types/api'
import * as devContentService from '@/services/devContentService'

export const useDevContentStore = defineStore('devContent', () => {
  // ── State ──────────────────────────────────────────────────────────────

  const categories = ref<CategoryResponse[]>([])
  const topics = ref<TopicResponse[]>([])
  const selectedCategoryId = ref<number | null>(null)

  const categoriesLoading = ref(false)
  const topicsLoading = ref(false)
  const categoriesError = ref<string | null>(null)
  const topicsError = ref<string | null>(null)

  // ── Helpers ────────────────────────────────────────────────────────────

  function getApiErrorMessage(error: unknown): string {
    const maybeAxiosError = error as {
      response?: { data?: { message?: string; errors?: string[] } }
    }
    const apiErrors = maybeAxiosError.response?.data?.errors
    if (apiErrors && apiErrors.length > 0) {
      return apiErrors[0]
    }
    return maybeAxiosError.response?.data?.message ?? 'Unknown error'
  }

  // ── Category Actions ───────────────────────────────────────────────────

  async function fetchCategories() {
    categoriesLoading.value = true
    categoriesError.value = null
    try {
      categories.value = await devContentService.listCategories()
    } catch (error: unknown) {
      categoriesError.value = getApiErrorMessage(error)
      categories.value = []
    } finally {
      categoriesLoading.value = false
    }
  }

  async function getCategoryById(id: number): Promise<CategoryResponse> {
    return devContentService.getCategoryById(id)
  }

  async function createCategory(payload: CreateCategoryRequest): Promise<CategoryResponse> {
    const category = await devContentService.createCategory(payload)
    await fetchCategories()
    return category
  }

  async function updateCategory(id: number, payload: UpdateCategoryRequest): Promise<CategoryResponse> {
    const category = await devContentService.updateCategory(id, payload)
    await fetchCategories()
    return category
  }

  // ── Topic Actions ──────────────────────────────────────────────────────

  async function fetchTopics(categoryId?: number) {
    topicsLoading.value = true
    topicsError.value = null
    try {
      topics.value = await devContentService.listTopics(categoryId)
    } catch (error: unknown) {
      topicsError.value = getApiErrorMessage(error)
      topics.value = []
    } finally {
      topicsLoading.value = false
    }
  }

  async function getTopicById(id: number): Promise<TopicResponse> {
    return devContentService.getTopicById(id)
  }

  async function createTopic(payload: CreateTopicRequest): Promise<TopicResponse> {
    const topic = await devContentService.createTopic(payload)
    await fetchTopics(selectedCategoryId.value ?? undefined)
    return topic
  }

  async function updateTopic(id: number, payload: UpdateTopicRequest): Promise<TopicResponse> {
    const topic = await devContentService.updateTopic(id, payload)
    await fetchTopics(selectedCategoryId.value ?? undefined)
    return topic
  }

  function setSelectedCategoryId(id: number | null) {
    selectedCategoryId.value = id
    fetchTopics(id ?? undefined)
  }

  // ── Return ─────────────────────────────────────────────────────────────

  return {
    categories,
    topics,
    selectedCategoryId,
    categoriesLoading,
    topicsLoading,
    categoriesError,
    topicsError,
    fetchCategories,
    getCategoryById,
    createCategory,
    updateCategory,
    fetchTopics,
    getTopicById,
    createTopic,
    updateTopic,
    setSelectedCategoryId
  }
})
