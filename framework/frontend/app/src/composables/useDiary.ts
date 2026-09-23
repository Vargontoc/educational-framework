import { ref, type Ref } from 'vue'
import { getAbandonmentSignal, getActivities, getSummary } from '../services/diaryService'
import type { AbandonmentSignalResponse, DiaryActivityResponse, DiaryPeriod, DiarySummaryResponse } from '../types/diary'

export interface UseDiaryReturn {
  period: Ref<DiaryPeriod>
  summary: Ref<DiarySummaryResponse | null>
  activities: Ref<DiaryActivityResponse[]>
  abandonmentSignals: Ref<Record<number, AbandonmentSignalResponse | null>>
  loading: Ref<boolean>
  error: Ref<boolean>
  load: (childProfileId: number) => Promise<void>
  setPeriod: (childProfileId: number, period: DiaryPeriod) => Promise<void>
}

export function useDiary(): UseDiaryReturn {
  const period = ref<DiaryPeriod>('WEEK')
  const summary = ref<DiarySummaryResponse | null>(null)
  const activities = ref<DiaryActivityResponse[]>([])
  const abandonmentSignals = ref<Record<number, AbandonmentSignalResponse | null>>({})
  const loading = ref(false)
  const error = ref(false)

  async function load(childProfileId: number): Promise<void> {
    loading.value = true
    error.value = false

    try {
      const [summaryData, activitiesData] = await Promise.all([
        getSummary(childProfileId, period.value),
        getActivities(childProfileId, period.value)
      ])
      summary.value = summaryData
      activities.value = activitiesData

      // Una señal fallida no debe tirar todo el diario: cada actividad se resuelve por separado.
      const signalEntries = await Promise.all(
        activitiesData.map(async (activity) => {
          try {
            const signal = await getAbandonmentSignal(childProfileId, activity.activityId)
            return [activity.activityId, signal] as const
          } catch {
            return [activity.activityId, null] as const
          }
        })
      )
      abandonmentSignals.value = Object.fromEntries(signalEntries)
    } catch {
      error.value = true
      summary.value = null
      activities.value = []
      abandonmentSignals.value = {}
    } finally {
      loading.value = false
    }
  }

  async function setPeriod(childProfileId: number, newPeriod: DiaryPeriod): Promise<void> {
    period.value = newPeriod
    await load(childProfileId)
  }

  return { period, summary, activities, abandonmentSignals, loading, error, load, setPeriod }
}
