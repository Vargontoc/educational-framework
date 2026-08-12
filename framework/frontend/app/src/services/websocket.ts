/**
 * Cliente WebSocket base con reconexión exponencial
 * Servicio centralizado para comunicación WebSocket con el backend
 * 
 * Según ADR-010:
 * - Dos canales independientes: GameChannel y ParentChannel
 * - Reconexión automática con backoff exponencial
 * - Sin cola de eventos offline (eventos se descartan)
 * - Estado de conexión reactivo expuesto al store
 */

const WS_BASE_URL = import.meta.env.VITE_WS_BASE_URL || 'ws://localhost:8080'

export type ConnectionStatus = 'disconnected' | 'connecting' | 'connected' | 'reconnecting'

export interface WebSocketClientOptions {
  /** URL del endpoint WebSocket (se concatena con WS_BASE_URL) */
  endpoint: string
  /** Callback cuando se recibe un mensaje */
  onMessage?: (data: unknown) => void
  /** Callback cuando cambia el estado de conexión */
  onStatusChange?: (status: ConnectionStatus) => void
  /** Callback cuando ocurre un error */
  onError?: (error: Event) => void
  /** Número máximo de intentos de reconexión (0 = infinito) */
  maxReconnectAttempts?: number
  /** Intervalo base para backoff exponencial en ms */
  reconnectBaseDelay?: number
  /** Intervalo máximo para backoff exponencial en ms */
  reconnectMaxDelay?: number
}

/**
 * Cliente WebSocket con reconexión exponencial
 */
export class WebSocketClient {
  private ws: WebSocket | null = null
  private options: Required<WebSocketClientOptions>
  private reconnectAttempts = 0
  private reconnectTimeout: ReturnType<typeof setTimeout> | null = null
  private status: ConnectionStatus = 'disconnected'
  private shouldReconnect = true

  constructor(options: WebSocketClientOptions) {
    this.options = {
      endpoint: options.endpoint,
      onMessage: options.onMessage || (() => {}),
      onStatusChange: options.onStatusChange || (() => {}),
      onError: options.onError || (() => {}),
      maxReconnectAttempts: options.maxReconnectAttempts || 0, // 0 = infinito
      reconnectBaseDelay: options.reconnectBaseDelay || 1000,
      reconnectMaxDelay: options.reconnectMaxDelay || 30000,
    }
  }

  /**
   * Conecta al WebSocket
   */
  connect(): void {
    if (this.ws && (this.ws.readyState === WebSocket.CONNECTING || this.ws.readyState === WebSocket.OPEN)) {
      return
    }

    this.setStatus('connecting')

    const url = `${WS_BASE_URL}${this.options.endpoint}`
    this.ws = new WebSocket(url)

    this.ws.onopen = () => {
      this.setStatus('connected')
      this.reconnectAttempts = 0
    }

    this.ws.onmessage = (event: MessageEvent) => {
      try {
        const data = JSON.parse(event.data)
        this.options.onMessage(data)
      } catch {
        // Si no es JSON, pasar el dato crudo
        this.options.onMessage(event.data)
      }
    }

    this.ws.onclose = () => {
      this.setStatus('disconnected')
      if (this.shouldReconnect) {
        this.attemptReconnect()
      }
    }

    this.ws.onerror = (error: Event) => {
      this.options.onError(error)
    }
  }

  /**
   * Envía un mensaje al servidor
   */
  send(data: unknown): void {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      console.warn('WebSocket no está conectado. Mensaje no enviado.')
      return
    }

    const message = typeof data === 'string' ? data : JSON.stringify(data)
    this.ws.send(message)
  }

  /**
   * Desconecta el WebSocket
   */
  disconnect(): void {
    this.shouldReconnect = false
    this.clearReconnectTimeout()

    if (this.ws) {
      this.ws.close()
      this.ws = null
    }

    this.setStatus('disconnected')
  }

  /**
   * Obtiene el estado actual de conexión
   */
  getStatus(): ConnectionStatus {
    return this.status
  }

  /**
   * Intenta reconectar con backoff exponencial
   */
  private attemptReconnect(): void {
    if (this.options.maxReconnectAttempts > 0 && this.reconnectAttempts >= this.options.maxReconnectAttempts) {
      console.warn('Máximo de intentos de reconexión alcanzado')
      return
    }

    this.setStatus('reconnecting')
    this.reconnectAttempts++

    const delay = computeBackoffDelay(
      this.reconnectAttempts,
      this.options.reconnectBaseDelay,
      this.options.reconnectMaxDelay
    )

    this.reconnectTimeout = setTimeout(() => {
      this.connect()
    }, delay)
  }

  /**
   * Limpia el timeout de reconexión
   */
  private clearReconnectTimeout(): void {
    if (this.reconnectTimeout) {
      clearTimeout(this.reconnectTimeout)
      this.reconnectTimeout = null
    }
  }

  /**
   * Actualiza el estado y notifica al callback
   */
  private setStatus(status: ConnectionStatus): void {
    this.status = status
    this.options.onStatusChange(status)
  }
}

/**
 * Factory para crear clientes WebSocket preconfigurados
 */
export function createGameWebSocket(options: Omit<WebSocketClientOptions, 'endpoint'>): WebSocketClient {
  return new WebSocketClient({
    ...options,
    endpoint: '/ws/game',
  })
}

/**
 * Backoff exponencial con jitter, compartido entre WebSocketClient (GameChannel)
 * y StompParentClient (ParentChannel) para no duplicar la fórmula.
 */
export function computeBackoffDelay(attempt: number, baseDelay: number, maxDelay: number): number {
  return Math.min(
    baseDelay * Math.pow(2, attempt - 1) + Math.random() * 1000,
    maxDelay
  )
}

export default WebSocketClient
