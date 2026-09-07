import { Scene } from 'phaser'
import router from "@/router";
import { ServerGameEvent, AvatarEvent, GameErrorPayload } from './GameEvent'
import { ConnectionMonitor } from './ConnectionMonitor'
import { connectWebSocket, clearWebSocketHeartbeat } from './websocket'
import { ErrorClassifier } from './ErrorClassifier'
import { MessageRouter } from '@/services/MessageRouter'
import { AudioService } from '@/services/AudioService'
import { AudioCache } from '@/services/AudioCache'

export class BaseStateScene extends Scene {
  websocket?: WebSocket
  sessionId?: number
  childId?: number
  connectionMonitor?: ConnectionMonitor

  constructor() {
    super({ key: 'base-state', active: false })
  }

  init(data: { websocket: WebSocket; sessionId: number; childId: number }) {
    this.websocket = data.websocket
    this.sessionId = data.sessionId
    this.childId = data.childId
  }

  create() {
    const cx = 640
    const cy = 360

    this.add.rectangle(cx, cy, 1280, 720, 0xe8f4f8)

    const nubiPlaceholder = this.add.circle(cx, cy - 72, 60, 0x7ec8e3)
    
    this.tweens.add({
      targets: nubiPlaceholder,
      scaleX: 1.05,
      scaleY: 1.05,
      duration: 2000,
      yoyo: true,
      repeat: -1,
      ease: 'Sine.easeInOut'
    })

    const statusText = this.add.text(cx, cy + 48, 'Listo para jugar', {
      fontSize: '28px',
      color: '#111827',
      fontFamily: 'Nunito, sans-serif',
      fontStyle: '600'
    })
    statusText.setOrigin(0.5, 0.5)

    this.connectionMonitor = new ConnectionMonitor(
      () => {
        this.attemptReconnect()
      },
      () => {
        this.goToFarewell()
      }
    )

    if (this.websocket) {
      this.manageWebSocket(this.websocket)
    }
  }

  manageWebSocket(ws: WebSocket) {
    ws.onmessage = (msg) => {
      MessageRouter.route(
        msg.data,
        (jsonData) => {
          this.readEvent(jsonData as ServerGameEvent | AvatarEvent)
        },
        (binaryData) => {
          const audioService = this.registry.get('audioService') as AudioService
          void audioService.handleBinaryFrame(binaryData)
        }
      )
    }

    ws.onclose = () => {
      clearWebSocketHeartbeat(ws)
      this.connectionMonitor?.handleWebSocketClose()
    }

    ws.onerror = () => {
      clearWebSocketHeartbeat(ws)
    }

    const cleanup = () => {
      ws.onmessage = null
      ws.onclose = null
      ws.onerror = null
    }
    this.events.once('shutdown', cleanup)
    this.events.once('destroy', cleanup)
  }

  async attemptReconnect() {
    if (this.sessionId === undefined) {
      this.goToFarewell()
      return
    }

    this.cleanupWebSocket()

    try {
      const ws = await connectWebSocket(this.sessionId)
      this.websocket = ws
      this.setupWebSocketHandlers(ws)
      this.connectionMonitor?.notifyReconnectSuccess()
    } catch {
      this.connectionMonitor?.notifyReconnectFailure()
    }
  }

  setupWebSocketHandlers(ws: WebSocket) {
    ws.onmessage = (msg) => {
      MessageRouter.route(
        msg.data,
        (jsonData) => {
          this.readEvent(jsonData as ServerGameEvent | AvatarEvent)
        },
        (binaryData) => {
          const audioService = this.registry.get('audioService') as AudioService
          void audioService.handleBinaryFrame(binaryData)
        }
      )
    }

    ws.onclose = () => {
      clearWebSocketHeartbeat(ws)
      this.connectionMonitor?.handleWebSocketClose()
    }

    ws.onerror = () => {
      clearWebSocketHeartbeat(ws)
    }
  }

  readEvent(event: ServerGameEvent | AvatarEvent) {
    console.log(event)
    if (!event) return

    if (event.event === 'GAME_AVATAR_EVENT') {
      this.handleAvatarEvent(event)
      return
    }
    switch (event.event) {
      case 'AUTH_ACK':
      case 'HEARTBEAT_ACK':
        break

      case 'CHILD_EXPELLED':
        this.handleExpulsion()
        break

      case 'SESSION_EXPIRED':
      case 'SESSION_INVALIDATED':
        this.handleSessionExpired()
        break

      case 'CHILD_TTS_ACTIVATED':
        this.handleTTSActivated()
        break

      case 'CHILD_TTS_DEACTIVATED':
        this.handleTTSDeactivated()
        break

      case 'CHILD_AGENT_ACTIVATED':
        this.handleAgentActivated()
        break

      case 'CHILD_AGENT_DEACTIVATED':
        this.handleAgentDeactivated()
        break

      case 'GAME_ERROR':
        this.handleGameError(event.payload)
        break

      default:
        break
    }
  }

  handleAvatarEvent(event: AvatarEvent) {
    switch(event.eventType){
        case 'FAREWELL':
            this.handleFarewellEvent(event)
            break
        default:
            break
    }
  }

  handleFarewellEvent(event: AvatarEvent) {
    // El backend cierra este socket tras el farewell; evitar que ConnectionMonitor
    // intente reconectar una sesión que ya va a quedar inactiva/expulsada
    this.connectionMonitor?.disable()

    const audioService = this.registry.get('audioService') as AudioService

    // Escuchar cuando el audio termine y mandar a home
    audioService.once('audio-completed', () => {
      this.cleanupWebSocket()
      setTimeout(() => {
        router.replace({ name: 'Home' })
      }, 1000);
    })

    if (event.audioAvailable && event.audioId) {
      let fallbackTimeout: ReturnType<typeof setTimeout> | null = null

      const handleAudioReceived = (audioId: string) => {
        if (audioId === event.audioId) {
          // Audio dinámico recibido, cancelar fallback
          if (fallbackTimeout) {
            clearTimeout(fallbackTimeout)
            fallbackTimeout = null
          }
          audioService.playDynamic(audioId)
          audioService.off('audio-received', handleAudioReceived)
        }
      }
      audioService.on('audio-received', handleAudioReceived)

      // Timeout de 3 segundos para fallback a estático si no llega el dinámico
      fallbackTimeout = setTimeout(() => {
        if (!audioService.isCurrentlyPlaying()) {
          console.warn('Audio dinámico no recibido en 3s, usando fallback estático')
          audioService.off('audio-received', handleAudioReceived)
          audioService.playStatic('farewell')
        }
      }, 3000)
    } else {
      // No hay audio dinámico, reproducir estático directamente
      audioService.playStatic('farewell')
    }
  }

  handleExpulsion() {
    this.connectionMonitor?.disable()
    this.cleanupWebSocket()
    this.goToFarewell()
  }

  handleSessionExpired() {
    this.cleanupWebSocket()
    this.connectionMonitor?.handleSessionExpired()
  }

  handleTTSActivated() {
    this.registry.set('ttsEnabled', true)
    this.events.emit('tts-state-changed', true)
    console.log('TTS activated during session')
  }

  handleTTSDeactivated() {
    this.registry.set('ttsEnabled', false)
    this.events.emit('tts-state-changed', false)
    console.log('TTS deactivated during session')
  }

  handleAgentActivated() {
    this.registry.set('npcEnabled', true)
    this.events.emit('npc-state-changed', true)
    console.log('NPC activated during session')
  }

  handleAgentDeactivated() {
    this.registry.set('npcEnabled', false)
    this.events.emit('npc-state-changed', false)
    console.log('NPC deactivated during session')
  }

  handleGameError(payload: GameErrorPayload | null) {
    const classified = ErrorClassifier.classifyFromBackendEvent({
      event: 'GAME_ERROR',
      payload: payload ?? undefined
    })

    if (classified.severity === 'RECOVERABLE') {
      console.log('Recoverable error:', classified.message)
      return
    }

    console.log('Critical error:', classified.message)
    this.handleSessionExpired()
  }

  goToFarewell() {
    this.cleanupWebSocket()
    this.scene.start('farewell')
  }

  cleanupWebSocket() {
    if (this.websocket) {
      clearWebSocketHeartbeat(this.websocket)
      this.websocket.onmessage = null
      this.websocket.onclose = null
      this.websocket.onerror = null
      this.websocket.close()
      this.websocket = undefined
    }
  }

  shutdown() {
    const audioService = this.registry.get('audioService') as AudioService
    if (audioService) {
      audioService.stop()
    }
    this.cleanupWebSocket()
  }

  destroy() {
    const audioService = this.registry.get('audioService') as AudioService
    if (audioService) {
      audioService.dispose()
    }

    const audioCache = this.registry.get('audioCache') as AudioCache
    if (audioCache) {
      audioCache.clear()
    }
  }
}
