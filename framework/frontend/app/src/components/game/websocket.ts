import { AuthGameEvent, HeartbeatEvent } from './GameEvent'

const WS_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080')
  .replace(/^http:/, 'ws:')
  .replace(/^https:/, 'wss:')

export function connectWebSocket(sessionId: number): Promise<WebSocket> {
  return new Promise((resolve, reject) => {
    const ws = new WebSocket(`${WS_BASE_URL}/ws/game?childSessionId=${sessionId}`)

    ws.onopen = () => {
      const authEvent = new AuthGameEvent()
      authEvent.childSessionId = sessionId
      ws.send(JSON.stringify(authEvent))

      const heartbeatId = setInterval(() => {
        if (ws.readyState === WebSocket.OPEN) {
          ws.send(JSON.stringify(new HeartbeatEvent()))
        }
      }, 30000)

      ;(ws as WebSocket & { heartbeatId: number }).heartbeatId = heartbeatId

      resolve(ws)
    }

    ws.onerror = (err) => reject(err)
  })
}

export function clearWebSocketHeartbeat(ws: WebSocket) {
  const heartbeatId = (ws as WebSocket & { heartbeatId?: number }).heartbeatId
  if (heartbeatId !== undefined) {
    clearInterval(heartbeatId)
  }
}
