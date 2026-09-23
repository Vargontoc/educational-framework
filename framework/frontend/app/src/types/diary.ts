export type DiaryPeriod = 'TODAY' | 'WEEK' | 'MONTH' | 'ALL'

export type DifficultyLevel = 'EASY' | 'MEDIUM' | 'HARD'

export interface DiarySummaryResponse {
  playedTimeMinutes: number
  uniqueActivitiesCompleted: number
}

export interface DiaryActivityResponse {
  activityId: number
  name: string
  category: string
  /** Solo cuando category === 'RECOGNITION' (LETTER/NUMBER/SHAPE/COLOR/ANIMAL); null en el resto. */
  subcategory: string | null
  engine: string
  currentDifficulty: DifficultyLevel
}

export interface AbandonmentSignalResponse {
  activityId: number
  abandonmentCount: number
}
