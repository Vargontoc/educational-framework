export class ConnectionMonitor {
  private reconnectAttempts = 0
  private maxReconnectAttempts = 3
  private reconnectIntervalMs = 5000
  private reconnectTimer: ReturnType<typeof setTimeout> | null = null

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

  private scheduleReconnect() {
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
