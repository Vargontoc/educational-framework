import { ref } from 'vue'
import { defineStore } from 'pinia'
import type {
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
import * as devContentService from '@/services/devContentService'

export const useDevContentStore = defineStore('devContent', () => {
  // ── State ──────────────────────────────────────────────────────────────

  const categories = ref<CategoryResponse[]>([])
  const topics = ref<TopicResponse[]>([])
  const activities = ref<ActivityResponse[]>([])
  const difficultyLevels = ref<DifficultyLevelResponse[]>([])
  const activityResources = ref<ActivityResourceResponse[]>([])

  const selectedCategoryId = ref<number | null>(null)
  const selectedTopicId = ref<number | null>(null)
  const selectedActivityId = ref<number | null>(null)

  const categoriesLoading = ref(false)
  const topicsLoading = ref(false)
  const activitiesLoading = ref(false)
  const difficultyLevelsLoading = ref(false)
  const activityResourcesLoading = ref(false)

  const categoriesError = ref<string | null>(null)
  const topicsError = ref<string | null>(null)
  const activitiesError = ref<string | null>(null)
  const difficultyLevelsError = ref<string | null>(null)
  const activityResourcesError = ref<string | null>(null)

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

  // ── Activity Actions ───────────────────────────────────────────────────

  async function fetchActivities(topicId?: number) {
    activitiesLoading.value = true
    activitiesError.value = null
    try {
      activities.value = await devContentService.listActivities(topicId)
    } catch (error: unknown) {
      activitiesError.value = getApiErrorMessage(error)
      activities.value = []
    } finally {
      activitiesLoading.value = false
    }
  }

  async function getActivityById(id: number): Promise<ActivityResponse> {
    return devContentService.getActivityById(id)
  }

  async function createActivity(payload: CreateActivityRequest): Promise<ActivityResponse> {
    const activity = await devContentService.createActivity(payload)
    await fetchActivities(selectedTopicId.value ?? undefined)
    return activity
  }

  async function updateActivity(id: number, payload: UpdateActivityRequest): Promise<ActivityResponse> {
    const activity = await devContentService.updateActivity(id, payload)
    await fetchActivities(selectedTopicId.value ?? undefined)
    return activity
  }

  function setSelectedTopicId(id: number | null) {
    selectedTopicId.value = id
    fetchActivities(id ?? undefined)
  }

  // ── Difficulty Level Actions ────────────────────────────────────────────

  async function fetchDifficultyLevels(activityId: number) {
    difficultyLevelsLoading.value = true
    difficultyLevelsError.value = null
    try {
      difficultyLevels.value = await devContentService.listDifficultyLevels(activityId)
    } catch (error: unknown) {
      difficultyLevelsError.value = getApiErrorMessage(error)
      difficultyLevels.value = []
    } finally {
      difficultyLevelsLoading.value = false
    }
  }

  async function createDifficultyLevel(payload: CreateDifficultyLevelRequest): Promise<DifficultyLevelResponse> {
    const level = await devContentService.createDifficultyLevel(payload)
    if (selectedActivityId.value != null) {
      await fetchDifficultyLevels(selectedActivityId.value)
    }
    return level
  }

  async function updateDifficultyLevel(id: number, payload: UpdateDifficultyLevelRequest): Promise<DifficultyLevelResponse> {
    const level = await devContentService.updateDifficultyLevel(id, payload)
    if (selectedActivityId.value != null) {
      await fetchDifficultyLevels(selectedActivityId.value)
    }
    return level
  }

  // ── Activity Resource Actions ───────────────────────────────────────────

  async function fetchActivityResources(activityId: number) {
    activityResourcesLoading.value = true
    activityResourcesError.value = null
    try {
      activityResources.value = await devContentService.listActivityResources(activityId)
    } catch (error: unknown) {
      activityResourcesError.value = getApiErrorMessage(error)
      activityResources.value = []
    } finally {
      activityResourcesLoading.value = false
    }
  }

  async function createActivityResource(payload: CreateActivityResourceRequest): Promise<ActivityResourceResponse> {
    const resource = await devContentService.createActivityResource(payload)
    if (selectedActivityId.value != null) {
      await fetchActivityResources(selectedActivityId.value)
    }
    return resource
  }

  async function updateActivityResource(id: number, payload: UpdateActivityResourceRequest): Promise<ActivityResourceResponse> {
    const resource = await devContentService.updateActivityResource(id, payload)
    if (selectedActivityId.value != null) {
      await fetchActivityResources(selectedActivityId.value)
    }
    return resource
  }

  function setSelectedActivityId(id: number | null) {
    selectedActivityId.value = id
    if (id != null) {
      fetchDifficultyLevels(id)
      fetchActivityResources(id)
    } else {
      difficultyLevels.value = []
      activityResources.value = []
    }
  }

  // ── Return ─────────────────────────────────────────────────────────────

  return {
    categories,
    topics,
    activities,
    difficultyLevels,
    activityResources,
    selectedCategoryId,
    selectedTopicId,
    selectedActivityId,
    categoriesLoading,
    topicsLoading,
    activitiesLoading,
    difficultyLevelsLoading,
    activityResourcesLoading,
    categoriesError,
    topicsError,
    activitiesError,
    difficultyLevelsError,
    activityResourcesError,
    fetchCategories,
    getCategoryById,
    createCategory,
    updateCategory,
    fetchTopics,
    getTopicById,
    createTopic,
    updateTopic,
    setSelectedCategoryId,
    fetchActivities,
    getActivityById,
    createActivity,
    updateActivity,
    setSelectedTopicId,
    fetchDifficultyLevels,
    createDifficultyLevel,
    updateDifficultyLevel,
    fetchActivityResources,
    createActivityResource,
    updateActivityResource,
    setSelectedActivityId
  }
})
