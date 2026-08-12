/**
 * Cliente STOMP nativo para el canal parental (`/ws/parent`)
 *
 * Según SPRINT-034 / ADR-010:
 * - Autenticación vía connectHeaders.Authorization en el frame STOMP CONNECT
 *   (el backend valida en StompConnectAuthInterceptor, no en el handshake HTTP)
 * - Backoff exponencial con jitter gestionado manualmente (reconnectDelay: 0),
 *   reutilizando computeBackoffDelay() de services/websocket.ts
 * - Sin cola de eventos offline
 */
import { Client, type IMessage, type StompSubscription } from '@stomp/stompjs'
import { computeBackoffDelay, type ConnectionStatus } from './websocket'

const WS_BASE_URL = import.meta.env.VITE_WS_BASE_URL || 'ws://localhost:8080'

const RECONNECT_BASE_DELAY = 1000
const RECONNECT_MAX_DELAY = 30000

export interface StompParentClientOptions {
  onStatusChange?: (status: ConnectionStatus) => void
}

export class StompParentClient {
  private client: Client
  private subscriptions = new Map<string, StompSubscription>()
  private token: string | null = null
  private reconnectAttempts = 0
  private reconnectTimeout: ReturnType<typeof setTimeout> | null = null
  private shouldReconnect = true

  constructor(private options: StompParentClientOptions = {}) {
    this.client = new Client({
      brokerURL: `${WS_BASE_URL}/ws/parent/websocket`,
      reconnectDelay: 0,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000
    })

    this.client.onConnect = () => {
      this.reconnectAttempts = 0
      this.options.onStatusChange?.('connected')
    }

    this.client.onWebSocketClose = () => {
      this.options.onStatusChange?.('disconnected')
      this.attemptReconnect()
    }

    this.client.onStompError = () => {
      this.options.onStatusChange?.('disconnected')
    }
  }

  connect(token: string): void {
    this.shouldReconnect = true
    this.token = token
    this.client.connectHeaders = { Authorization: `Bearer ${token}` }
    this.options.onStatusChange?.('connecting')
    this.client.activate()
  }

  disconnect(): void {
    this.shouldReconnect = false
    this.clearReconnectTimeout()
    this.subscriptions.forEach((sub) => sub.unsubscribe())
    this.subscriptions.clear()
    this.client.deactivate()
    this.options.onStatusChange?.('disconnected')
  }

  subscribe(destination: string, callback: (message: IMessage) => void): void {
    if (this.subscriptions.has(destination)) return
    const sub = this.client.subscribe(destination, callback)
    this.subscriptions.set(destination, sub)
  }

  unsubscribe(destination: string): void {
    this.subscriptions.get(destination)?.unsubscribe()
    this.subscriptions.delete(destination)
  }

  publish(destination: string, body: unknown): void {
    this.client.publish({ destination, body: JSON.stringify(body) })
  }

  get connected(): boolean {
    return this.client.connected
  }

  private attemptReconnect(): void {
    if (!this.shouldReconnect || !this.token) return

    this.reconnectAttempts++
    this.options.onStatusChange?.('reconnecting')

    const delay = computeBackoffDelay(this.reconnectAttempts, RECONNECT_BASE_DELAY, RECONNECT_MAX_DELAY)

    this.clearReconnectTimeout()
    this.reconnectTimeout = setTimeout(() => {
      if (!this.shouldReconnect) return
      this.client.connectHeaders = { Authorization: `Bearer ${this.token}` }
      this.client.activate()
    }, delay)
  }

  private clearReconnectTimeout(): void {
    if (this.reconnectTimeout) {
      clearTimeout(this.reconnectTimeout)
      this.reconnectTimeout = null
    }
  }
}
