import { ref, computed, type Ref, type ComputedRef } from 'vue'
import { getActiveSessions, expelSession, type ChildSession } from '../services/sessionService'

export interface UseChildSessionsReturn {
  sessions: Ref<ChildSession[]>
  loading: Ref<boolean>
  error: Ref<boolean>
  errorMessage: Ref<string>
  activeSessionByChildId: ComputedRef<Map<number, ChildSession>>
  getSessionDuration: (childProfileId: number) => number | null
  startPolling: (familyId: number, intervalMs?: number) => void
  stopPolling: () => void
  expelChild: (sessionId: number) => Promise<boolean>
}

export function useChildSessions(): UseChildSessionsReturn {
  const sessions = ref<ChildSession[]>([])
  const loading = ref(false)
  const error = ref(false)
  const errorMessage = ref('')
  let pollingTimer: ReturnType<typeof setInterval> | null = null

  const activeSessionByChildId = computed(() => {
    const map = new Map<number, ChildSession>()
    for (const session of sessions.value) {
      if (session.status === 'ACTIVE') {
        map.set(session.childProfileId, session)
      }
    }
    return map
  })

  function getSessionDuration(childProfileId: number): number | null {
    const session = activeSessionByChildId.value.get(childProfileId)
    if (!session) return null
    const start = new Date(session.startedAt).getTime()
    const now = Date.now()
    return Math.floor((now - start) / 1000)
  }

  async function fetchSessions(familyId: number): Promise<void> {
    loading.value = true
    error.value = false
    errorMessage.value = ''
    try {
      sessions.value = await getActiveSessions(familyId)
    } catch (err) {
      error.value = true
      errorMessage.value = err instanceof Error ? err.message : 'Error al obtener sesiones'
    } finally {
      loading.value = false
    }
  }

  function startPolling(familyId: number, intervalMs: number = 5000): void {
    stopPolling()
    fetchSessions(familyId)
    pollingTimer = setInterval(() => {
      fetchSessions(familyId)
    }, intervalMs)
  }

  function stopPolling(): void {
    if (pollingTimer !== null) {
      clearInterval(pollingTimer)
      pollingTimer = null
    }
  }

  async function expelChild(sessionId: number): Promise<boolean> {
    const success = await expelSession(sessionId)
    if (success) {
      sessions.value = sessions.value.filter(s => s.id !== sessionId)
    }
    return success
  }

  return {
    sessions,
    loading,
    error,
    errorMessage,
    activeSessionByChildId,
    getSessionDuration,
    startPolling,
    stopPolling,
    expelChild
  }
}
