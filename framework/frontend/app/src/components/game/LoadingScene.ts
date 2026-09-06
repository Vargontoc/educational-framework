import { openSession } from "@/services/sessionService";
import { Scene } from "phaser";
import router from "@/router";
import { ServerGameEvent, AvatarEvent } from "./GameEvent";
import { connectWebSocket, clearWebSocketHeartbeat } from "./websocket";

export class LoadingScene extends Scene {
    websocket?: WebSocket
    sessionId?: number
    childId?: number

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
            if (msg.data) {
                this.readEvent(JSON.parse(msg.data))
            }
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
        console.log(event)
        switch (event.event) {
            case 'AUTH_ACK':
                break

            case 'HEARTBEAT_ACK':
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
            case 'SESSION_DISCONNECTED':
                if(event.audioAvailable){

                }else {
                    this.handleSessionExpired()
                }
                break;
            default:
                break;
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
        this.add.rectangle(400, 300, 800, 600, 0xf0f4f8)

        const icon = this.add.circle(400, 260, 40, 0x4a90e2)
        this.tweens.add({
            targets: icon,
            alpha: 0.3,
            duration: 1000,
            yoyo: true,
            repeat: -1
        })

        const loadingText = this.add.text(400, 340, 'Preparando...', {
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

        this.load.on('complete', () => {
            this.goToBaseState()
        })

        this.load.on('loaderror', (file: Phaser.Loader.File) => {
            console.error('Failed to load asset:', file.key, file.url)
        })

        this.load.start()
    }

    goToBaseState() {
        if (this.websocket && this.sessionId !== undefined && this.childId !== undefined) {
            this.scene.start('base-state', {
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
}
