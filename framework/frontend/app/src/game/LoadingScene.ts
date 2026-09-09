import { openSession } from "@/services/sessionService";
import { Scene } from "phaser";
import router from "@/router";
import { ServerGameEvent, AvatarEvent } from "./GameEvent";
import { connectWebSocket, clearWebSocketHeartbeat } from "./websocket";
import { AudioService } from "@/services/AudioService";
import { AudioCache } from "@/services/AudioCache";
import { AudioDecoder } from "@/services/AudioDecoder";
import { MessageRouter } from "@/services/MessageRouter";

export class LoadingScene extends Scene {
    websocket?: WebSocket
    sessionId?: number
    childId?: number
    private assetsLoaded = false
    private welcomeAudioCompleted = false
    private welcomeEventReceived = false
    private transferredWebSocket = false

    constructor() {
        super({ key: 'loading', active: true })
    }

    create() {
        const childId = this.registry.get('childId') as number
        this.childId = childId

        if (this.registry.get('npcEnabled') === undefined) {
            this.registry.set('npcEnabled', false)
        }
        if (this.registry.get('ttsEnabled') === undefined) {
            this.registry.set('ttsEnabled', false)
        }
        if (this.registry.get('voiceEnabled') === undefined) {
            this.registry.set('voiceEnabled', false)
        }

        const audioDecoder = new AudioDecoder()
        const audioCache = new AudioCache()
        const audioService = new AudioService({
            scene: this,
            audioCache,
            audioDecoder
        })

        this.registry.set('audioService', audioService)
        this.registry.set('audioCache', audioCache)

        this.showLoadingPlaceholder()

        openSession(childId)
            .then(session => {
                if (!session) {
                    this.handleBlockedProfile()
                    return
                }

                this.sessionId = session.id

                connectWebSocket(session.id)
                    .then(ws => {
                        this.websocket = ws
                        this.setupWebSocketHandlers(ws)
                        this.loadAssetsSilently()
                    })
                    .catch(error => {
                        console.error('WebSocket connection failed:', error)
                        this.handleSessionError()
                    })
            })
            .catch(error => {
                console.error('Error opening session:', error)
                this.handleSessionError()
            })
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
        }
    }

    readEvent(event: ServerGameEvent | AvatarEvent) {
        if (!event) return

        if (event.event === 'GAME_AVATAR_EVENT') {
            this.handleAvatarEvent(event)
            return
        }
    
        switch (event.event) {
            case 'AUTH_ACK':
                console.log('Perfil identificado')
                break

            case 'HEARTBEAT_ACK':
                console.log('Heartbeat recibido')
                break

            case 'CHILD_EXPELLED':
                this.handleExpulsion()
                break

            case 'SESSION_EXPIRED':
            case 'SESSION_INVALIDATED':
                this.handleSessionExpired()
                break

            default:
                break
        }
    }

    handleAvatarEvent(event: AvatarEvent) {
        switch(event.eventType){
            case 'WELCOME':
                this.handleWelcomeEvent(event)
                break
            case 'FAREWELL':
                this.handleFarewellEvent(event)
                break
            default:
                break
        }
    }

    handleWelcomeEvent(event: AvatarEvent) {
        this.welcomeEventReceived = true
        const audioService = this.registry.get('audioService') as AudioService

        // Escuchar cuando el audio termine (cualquier audio: estático o dinámico)
        audioService.once('audio-completed', () => {
            this.welcomeAudioCompleted = true
            // Esperar 1 segundo después de que termine el audio
            this.time.delayedCall(1000, () => {
                this.tryGoToBaseState()
            })
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
                    audioService.playStatic('welcome')
                }
            }, 3000)
        } else {
            // No hay audio dinámico, reproducir estático directamente
            audioService.playStatic('welcome')
        }
    }

    handleFarewellEvent(event: AvatarEvent) {
        const audioService = this.registry.get('audioService') as AudioService

        // Escuchar cuando el audio termine
        audioService.once('audio-completed', () => {
            // Esperar 1 segundo después de que termine el audio
            this.time.delayedCall(1000, () => {
                this.goToWorldMap()
            })
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
        this.cleanupWebSocket()
        router.replace({ name: 'Home' })
    }

    handleSessionExpired() {
        this.cleanupWebSocket()
        router.replace({ name: 'Home' })
    }

    cleanupWebSocket() {
        if (this.websocket) {
            clearWebSocketHeartbeat(this.websocket)
            this.websocket.close()
            this.websocket = undefined
        }
    }

    showLoadingPlaceholder() {
        const cx = 640
        const cy = 360

        this.add.rectangle(cx, cy, 1280, 720, 0xf0f4f8)

        const icon = this.add.circle(cx, cy - 48, 40, 0x4a90e2)
        this.tweens.add({
            targets: icon,
            alpha: 0.3,
            duration: 1000,
            yoyo: true,
            repeat: -1
        })

        const loadingText = this.add.text(cx, cy + 48, 'Preparando...', {
            fontSize: '24px',
            color: '#111827',
            fontFamily: 'Nunito, sans-serif',
            fontStyle: '600'
        })
        loadingText.setOrigin(0.5, 0.5)
    }

    loadAssetsSilently() {
        this.load.setBaseURL('/')
        this.load.pack('packManifest', 'assets-manifest.json', 'dev')

        this.load.audio('welcome', 'audio/welcome.wav')
        this.load.audio('farewell', 'audio/farewell.wav')

        this.load.on('complete', () => {
            this.assetsLoaded = true
            // Esperar hasta 5 segundos para recibir el evento WELCOME
            // Si no se recibe, transicionar de todos modos
            setTimeout(() => {
                if (!this.welcomeEventReceived) {
                    console.warn('Evento WELCOME no recibido en 5s, transicionando a base-state')
                    this.tryGoToBaseState()
                }
            }, 5000)
        })

        this.load.on('loaderror', (file: Phaser.Loader.File) => {
            console.error('Failed to load asset:', file.key, file.url)
        })

        this.load.start()
    }

    tryGoToBaseState() {
        // Solo transicionar si los assets están cargados Y el audio de bienvenida ha terminado
        // O si no se recibió evento WELCOME (fallback)
        if (this.assetsLoaded && (this.welcomeAudioCompleted || !this.welcomeEventReceived)) {
            this.goToWorldMap()
        }
    }

    goToWorldMap() {
        if (this.websocket && this.sessionId !== undefined && this.childId !== undefined) {
            this.transferredWebSocket = true
            this.scene.start('world-map', {
                websocket: this.websocket,
                sessionId: this.sessionId,
                childId: this.childId
            })
        } else {
            this.handleSessionError()
        }
    }

    handleBlockedProfile() {
        router.replace({ name: 'Home' })
    }

    handleSessionError() {
        router.replace({ name: 'Home' })
    }

    shutdown() {
        const audioService = this.registry.get('audioService') as AudioService
        if (audioService) {
            audioService.stop()
        }
        if (!this.transferredWebSocket) {
            this.cleanupWebSocket()
        }
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
