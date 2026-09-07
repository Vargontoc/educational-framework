export class ConnectionMonitor {
  private reconnectAttempts = 0
  private maxReconnectAttempts = 3
  private reconnectIntervalMs = 5000
  private reconnectTimer: ReturnType<typeof setTimeout> | null = null
  private disabled = false

  private onConnectionLost: () => void
  private onRecoveryFailed: () => void

  constructor(
    onConnectionLost: () => void,
    onRecoveryFailed: () => void
  ) {
    this.onConnectionLost = onConnectionLost
    this.onRecoveryFailed = onRecoveryFailed
  }

  handleWebSocketClose() {
    this.scheduleReconnect()
  }

  handleHeartbeatTimeout() {
    this.scheduleReconnect()
  }

  handleSessionExpired() {
    this.clearTimer()
    this.onRecoveryFailed()
  }

  // Llamar cuando el cierre del socket es esperado (farewell/expulsión) para no reintentar reconectar una sesión ya finalizada
  disable() {
    this.disabled = true
    this.clearTimer()
  }

  private scheduleReconnect() {
    if (this.disabled) {
      return
    }

    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      this.clearTimer()
      this.onRecoveryFailed()
      return
    }

    this.reconnectAttempts++

    this.clearTimer()
    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = null
      this.onConnectionLost()
    }, this.reconnectIntervalMs)
  }

  notifyReconnectSuccess() {
    this.clearTimer()
    this.reset()
  }

  notifyReconnectFailure() {
    this.scheduleReconnect()
  }

  private clearTimer() {
    if (this.reconnectTimer !== null) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
  }

  reset() {
    this.reconnectAttempts = 0
  }
}
