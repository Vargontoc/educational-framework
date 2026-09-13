import { Scene } from "phaser";
import router from "@/router";
import { AvatarEvent, ServerGameEvent, WorldHeartbeatEvent, GameErrorPayload, WorldDiscoveryElements } from "./GameEvent";
import { connectWebSocket, clearWebSocketHeartbeat } from "./websocket";
import { ConnectionMonitor } from "./ConnectionMonitor";
import { ErrorClassifier } from "./ErrorClassifier";
import { MessageRouter } from "@/services/MessageRouter";
import { AudioService } from "@/services/AudioService";
import { BackgroundLayer } from "./worldmap/layers/BackgroundLayer";
import { ParallaxLayer } from "./worldmap/layers/ParallaxLayer";
import { GroundLayer } from "./worldmap/layers/GroundLayer";
import { NubiLayer } from "./worldmap/layers/NubiLayer";
import { InteractiveLayer } from "./worldmap/layers/InteractiveLayer";
import { TransportLayer, TRANSPORT_TOUCHED_EVENT } from "./worldmap/layers/TransportLayer";
import { BiomeSelectorLayer, DESTINATION_SELECTED_EVENT, BiomeHostInfo } from "./worldmap/layers/BiomeSelectorLayer";
import { EdgeHintLayer } from "./worldmap/layers/EdgeHintLayer";
import { GradualScroller, WORLDMAP_TAP_EVENT } from "./worldmap/scroll/GradualScroller";
import { EnvironmentReaction } from "./worldmap/reactions/EnvironmentReaction";
import { WORLD_MAP_CONFIG } from "./worldmap/config/worldMapConfig";
import { fadeToBlack, fadeFromBlack } from "./worldmap/transitions/BiomeTransition";
import { WorldTravelEvent } from "./GameEvent";

const INITIAL_BIOME = 'meadow'

const ALL_BIOME_HOSTS: BiomeHostInfo[] = [
    { biome: 'MEADOW', sequenceOrder: 1 },
    { biome: 'FARM', sequenceOrder: 2 },
    { biome: 'WOODS', sequenceOrder: 3 },
    { biome: 'BEACH', sequenceOrder: 4 },
    { biome: 'SPACE', sequenceOrder: 5 },
    { biome: 'PREHISTORY', sequenceOrder: 6 }
]

export class WorldMapScene extends Scene {
    websocket?: WebSocket
    sessionId?: number
    childId?: number
    connectionMonitor?: ConnectionMonitor

    private backgroundLayer?: BackgroundLayer
    private parallaxLayer?: ParallaxLayer
    private groundLayer?: GroundLayer
    private nubiLayer?: NubiLayer
    private interactiveLayer?: InteractiveLayer
    private transportLayer?: TransportLayer
    private biomeSelectorLayer?: BiomeSelectorLayer
    private edgeHintLayer?: EdgeHintLayer
    private scroller?: GradualScroller
    private environmentReaction?: EnvironmentReaction
    private currentBiome?: string
    private activeWorldWidth: number = WORLD_MAP_CONFIG.worldWidth
    private pendingBiomeLoad?: string
    private arrivalInProgress = false

    constructor() { super({ key: 'world-map', active: false}) }

    init(data: { websocket: WebSocket; sessionId?: number; childId?: number }) {
        this.websocket = data.websocket
        this.sessionId = data.sessionId
        this.childId = data.childId
    }

    create() {
        const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
        const npcEnabled = this.registry.get('npcEnabled') as boolean

        this.currentBiome = INITIAL_BIOME
        this.activeWorldWidth = WORLD_MAP_CONFIG.worldWidth

        this.backgroundLayer = new BackgroundLayer(this)
        this.backgroundLayer.buildFrom(INITIAL_BIOME)

        this.groundLayer = new GroundLayer(this)
        const groundContainer = this.groundLayer.create(INITIAL_BIOME, this.activeWorldWidth)

        this.parallaxLayer = new ParallaxLayer(this)
        const parallaxContainer = this.parallaxLayer.create(this.groundLayer.getBandHeight(), INITIAL_BIOME, this.activeWorldWidth)

        this.nubiLayer?.create(npcEnabled, this.getGroundTopY())

        this.interactiveLayer = new InteractiveLayer(this)
        const interactiveContainer = this.interactiveLayer.create()
        this.environmentReaction = new EnvironmentReaction(this)
        this.interactiveLayer.setOnTouch((_element, shape, pointer) => {
            this.environmentReaction?.play(shape)
            this.nubiLayer?.walkTo(this.clampToWorldWidth(pointer.x + (this.scroller?.getOffset() ?? 0)))
        })

        this.transportLayer = new TransportLayer(this)
        const transportContainer = this.transportLayer.create(INITIAL_BIOME, this.getGroundTopY(), WORLD_MAP_CONFIG.viewportWidth / 6)

        this.biomeSelectorLayer = new BiomeSelectorLayer(this)

        this.edgeHintLayer = new EdgeHintLayer(this)
        this.edgeHintLayer.create()

        this.scroller = new GradualScroller(this, reducedMotion)
        this.scroller.attach()
        this.scroller.setMaxScrollOffset(this.computeMaxScrollOffset(this.activeWorldWidth))
        if (!reducedMotion) {
            this.scroller.registerLayer(parallaxContainer, WORLD_MAP_CONFIG.parallaxFactor)
        }
        this.scroller.registerLayer(groundContainer, WORLD_MAP_CONFIG.groundFactor)
        this.scroller.registerLayer(interactiveContainer, 1)
        this.scroller.registerLayer(transportContainer, 1)

        this.events.on(WORLDMAP_TAP_EVENT, (x: number) => this.nubiLayer?.walkTo(this.clampToWorldWidth(x + (this.scroller?.getOffset() ?? 0))))

        this.events.on(TRANSPORT_TOUCHED_EVENT, () => this.handleTransportTouched())
        this.events.on(DESTINATION_SELECTED_EVENT, (biome: string) => this.handleDestinationSelected(biome))
        this.events.on('nubi-double-tap', () => this.biomeSelectorLayer?.close())

        this.connectionMonitor = new ConnectionMonitor(
            () => { this.attemptReconnect() },
            () => { this.goToFarewell() }
        )

        if(this.websocket) {
            this.manageWebsocket(this.websocket)
        }

        this.events.on('pause', () => {
            clearInterval(this.registry.get('wsWorldbeat'))
        })

        this.events.on('resume', () => {
            this.startWorldHeartbeat()
        })

        this.events.once('shutdown', () => this.handleSceneTeardown())
        this.events.once('destroy', () => this.handleSceneTeardown())
    }

    update(_time: number, delta: number) {
        this.scroller?.update(delta)
        this.backgroundLayer?.update(delta)

        const nubiStep = this.nubiLayer?.update(delta) ?? 0
        if (nubiStep !== 0) {
            this.scroller?.followStep(nubiStep)
        }
        this.nubiLayer?.syncScreenPosition(this.scroller?.getOffset() ?? 0)

        if (this.edgeHintLayer && this.scroller) {
            this.edgeHintLayer.updateProximity(this.scroller.getOffset(), this.scroller.getMaxScrollOffset())
        }
    }

    handleSceneTeardown() {
        this.cleanupLayers()

        const audioService = this.registry.get('audioService') as AudioService | undefined
        audioService?.stop()

        this.cleanupWebSocket()
    }

    cleanupLayers() {
        this.scroller?.destroy()
        this.transportLayer?.destroy()
        this.nubiLayer?.destroy()
        this.interactiveLayer?.destroy()
        this.groundLayer?.destroy()
        this.parallaxLayer?.destroy()
        this.backgroundLayer?.destroy()
        this.edgeHintLayer?.destroy()
    }

    preload() {
        this.nubiLayer = new NubiLayer(this)
        this.nubiLayer.preload()
        this.load.pack(`biome-${INITIAL_BIOME}`, '/assets-manifest.json', `biome-${INITIAL_BIOME}`)
        this.load.on('loaderror', (file: Phaser.Loader.File) => { console.error('Failed to load asset', file.key, file.url) })
        this.load.once('complete', () => { console.log("Larga....")})
        this.load.start();
    }

    startWorldHeartbeat() {
        if (!this.websocket) return

        clearInterval(this.registry.get('wsWorldbeat'))

        const ws = this.websocket
        const sendHeartbeat = () => {
            if (ws.readyState === WebSocket.OPEN) {
                ws.send(JSON.stringify(new WorldHeartbeatEvent()))
            }
        }

        sendHeartbeat()
        const heartbeatId = setInterval(sendHeartbeat, 1000)
        this.registry.set('wsWorldbeat', heartbeatId)
    }

    manageWebsocket(ws: WebSocket) {
        this.startWorldHeartbeat()
        this.setupWebSocketHandlers(ws)
    }

    setupWebSocketHandlers(ws: WebSocket) {
        ws.onmessage = (msg) => {
            MessageRouter.route(
                msg.data,
                (jsonData) => {
                    this.readEvent(jsonData as ServerGameEvent | AvatarEvent)
                },
                (binaryData) => {
                    const audioService = this.registry.get('audioService') as AudioService | undefined
                    void audioService?.handleBinaryFrame(binaryData)
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

    async attemptReconnect() {
        if (this.sessionId === undefined) {
            this.goToFarewell()
            return
        }

        this.cleanupWebSocket()

        try {
            const ws = await connectWebSocket(this.sessionId)
            this.websocket = ws
            this.manageWebsocket(ws)
            this.connectionMonitor?.notifyReconnectSuccess()
        } catch {
            this.connectionMonitor?.notifyReconnectFailure()
        }
    }

    readEvent(event: ServerGameEvent | AvatarEvent) {
        if(!event) return;
        if(event.event === 'GAME_AVATAR_EVENT' ){
            this.handleAvatarEvent(event)
        }else {
            switch(event.event) {
                case 'WORLD_STATE_SYNC' :
                    if(event.payload?.status && event.payload.status == 'ACTIVE' && event.payload.destination)  {
                        this.handleWorldStateActive(
                            event.payload.destination.biome,
                            event.payload.destination.host?.worldWidth,
                            event.payload.destination.discoveryElements
                        )
                    } else {
                        this.interactiveLayer?.render([])
                    }
                    break;
                case 'WORLD_ACTIVITY_STARTED':
                    console.log('WORLD_ACTIVITY_STARTED recibido, sin acción en esta fase')
                    break;
                case 'CHILD_AGENT_ACTIVATED':
                    this.registry.set('npcEnabled', true)
                    this.events.emit('npc-state-changed', true)
                    break;
                case 'CHILD_AGENT_DEACTIVATED':
                    this.registry.set('npcEnabled', false)
                    this.events.emit('npc-state-changed', false)
                    break;
                case 'CHILD_TTS_ACTIVATED':
                    this.registry.set('ttsEnabled', true)
                    this.events.emit('tts-state-changed', true)
                    break;
                case 'CHILD_TTS_DEACTIVATED':
                    this.registry.set('ttsEnabled', false)
                    this.events.emit('tts-state-changed', false)
                    break;
                case 'CHILD_EXPELLED':
                    this.handleExpulsion()
                    break;
                case 'SESSION_EXPIRED':
                case 'SESSION_INVALIDATED':
                    this.handleSessionExpired()
                    break;
                case 'GAME_ERROR':
                    this.handleGameError(event.payload)
                    break;
                default:
                    break;
            }
        }
    }

    private handleWorldStateActive(
        biome: string,
        hostWorldWidth: number | undefined,
        discoveryElements: WorldDiscoveryElements[]
    ) {
        const normalizedBiome = biome.toLowerCase()
        const effectiveWorldWidth = hostWorldWidth ?? WORLD_MAP_CONFIG.worldWidth
        const biomeChanged = normalizedBiome !== this.currentBiome
        const worldWidthChanged = effectiveWorldWidth !== this.activeWorldWidth

        this.activeWorldWidth = effectiveWorldWidth

        this.scroller?.setMaxScrollOffset(this.computeMaxScrollOffset(this.activeWorldWidth))

        if (biomeChanged) {
            this.pendingBiomeLoad = normalizedBiome
            this.ensureBiomeAssetsLoaded(normalizedBiome, () => {
                if (this.pendingBiomeLoad !== normalizedBiome) return
                this.pendingBiomeLoad = undefined
                this.beginBiomeArrival(normalizedBiome, effectiveWorldWidth, () => {
                    this.currentBiome = normalizedBiome
                    this.rebuildLayersForBiome(normalizedBiome, effectiveWorldWidth)
                    this.interactiveLayer?.render(discoveryElements, effectiveWorldWidth)
                })
            })
        } else if (worldWidthChanged) {
            this.currentBiome = normalizedBiome
            this.rebuildLayersForBiome(normalizedBiome, effectiveWorldWidth)
            this.interactiveLayer?.render(discoveryElements, effectiveWorldWidth)
        } else {
            this.interactiveLayer?.render(discoveryElements, effectiveWorldWidth)
        }
    }

    // Nubi camina en el mismo espacio de mundo que el ground/interactive layer
    // (factor 1), pero pointer.x + offset puede superar activeWorldWidth porque
    // maxScrollOffset deja ver el suelo (tileado) hasta el borde de pantalla,
    // es decir hasta worldWidth + viewportWidth. Sin este clamp Nubi podía
    // caminar más allá del ancho real del mapa activo.
    private clampToWorldWidth(worldX: number): number {
        return Math.min(Math.max(worldX, 0), this.activeWorldWidth)
    }

    // El offset de scroll representa el x de mundo que queda en el borde
    // izquierdo de pantalla, así que la cámara debe detenerse cuando el borde
    // derecho del mapa (worldWidth) queda a ras del borde derecho de pantalla,
    // no cuando offset == worldWidth (eso dejaría ver hasta worldWidth +
    // viewportWidth: una franja de suelo repetido sin ningún elemento, ya que
    // los elementos interactivos nunca superan worldWidth).
    private computeMaxScrollOffset(worldWidth: number): number {
        return Math.max(0, worldWidth - WORLD_MAP_CONFIG.viewportWidth)
    }

    private getGroundTopY(): number {
        return WORLD_MAP_CONFIG.viewportHeight - (this.groundLayer?.getBandHeight() ?? 0)
    }

    private ensureBiomeAssetsLoaded(biome: string, onComplete: () => void) {
        const skyboxKey = `skybox-${biome}`
        if (this.textures.exists(skyboxKey)) {
            onComplete()
            return
        }

        this.load.pack(`biome-${biome}`, '/assets-manifest.json', `biome-${biome}`)
        this.load.once('complete', () => {
            onComplete()
        })
        this.load.start()
    }

    private rebuildLayersForBiome(biome: string, worldWidth: number) {
        this.groundLayer?.destroy()
        this.parallaxLayer?.destroy()
        this.transportLayer?.destroy()

        const groundContainer = this.groundLayer?.create(biome, worldWidth)
        const parallaxContainer = this.parallaxLayer?.create(this.groundLayer?.getBandHeight() ?? 0, biome, worldWidth)

        this.backgroundLayer?.buildFrom(biome)

        this.scroller?.clearLayers()
        if (groundContainer) {
            this.scroller?.registerLayer(groundContainer, WORLD_MAP_CONFIG.groundFactor)
        }
        if (parallaxContainer) {
            const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
            if (!reducedMotion) {
                this.scroller?.registerLayer(parallaxContainer, WORLD_MAP_CONFIG.parallaxFactor)
            }
        }
        const interactiveContainer = this.interactiveLayer?.create()
        if (interactiveContainer) {
            this.scroller?.registerLayer(interactiveContainer, 1)
        }

        this.transportLayer = new TransportLayer(this)
        const transportContainer = this.transportLayer.create(biome, this.getGroundTopY(), WORLD_MAP_CONFIG.viewportWidth / 6)
        this.scroller?.registerLayer(transportContainer, 1)

        this.nubiLayer?.adjustToGroundTopY(this.getGroundTopY())
    }

    private handleTransportTouched() {
        const hosts: BiomeHostInfo[] = ALL_BIOME_HOSTS.filter(
            host => host.biome.toLowerCase() !== this.currentBiome
        )
        this.biomeSelectorLayer?.open(hosts)
    }

    private handleDestinationSelected(biome: string) {
        const normalizedBiome = biome.toLowerCase()
        if (normalizedBiome === this.currentBiome) return
        if (this.arrivalInProgress) return

        this.sendWorldTravel(biome)
    }

    beginBiomeArrival(targetBiome: string, _targetWorldWidth: number, onMidpoint: () => void): void {
        if (this.arrivalInProgress) return
        this.arrivalInProgress = true

        const fromBiome = this.currentBiome ?? 'meadow'

        const { overlay, duration } = fadeToBlack(
            { scene: this, fromBiome, toBiome: targetBiome },
            () => {
                onMidpoint()
                this.edgeHintLayer?.destroy()
                this.edgeHintLayer = new EdgeHintLayer(this)
                this.edgeHintLayer.create()
                this.waitForArrivalAudio(() => {
                    fadeFromBlack(this, overlay, duration, () => {
                        this.arrivalInProgress = false
                    })
                })
            }
        )
    }

    private waitForArrivalAudio(onReady: () => void): void {
        const npcEnabled = this.registry.get('npcEnabled') as boolean ?? false
        const ttsEnabled = this.registry.get('ttsEnabled') as boolean ?? false
        const voiceEnabled = this.registry.get('voiceEnabled') as boolean ?? false
        const expectsVoice = npcEnabled && ttsEnabled && voiceEnabled

        if (!expectsVoice) {
            this.time.delayedCall(WORLD_MAP_CONFIG.arrivalNoVoiceDelay, onReady)
            return
        }

        const audioService = this.registry.get('audioService') as AudioService | undefined
        let settled = false

        const settle = () => {
            if (settled) return
            settled = true
            audioService?.off('audio-completed', onAudioCompleted)
            onReady()
        }

        const onAudioCompleted = () => settle()

        if (audioService) {
            audioService.once('audio-completed', onAudioCompleted)
        }

        this.time.delayedCall(WORLD_MAP_CONFIG.arrivalAudioTimeout, settle)
    }

    private sendWorldTravel(biome: string) {
        if (!this.websocket || this.websocket.readyState !== WebSocket.OPEN) return
        this.websocket.send(JSON.stringify(new WorldTravelEvent(biome)))
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
        this.connectionMonitor?.disable()

        const audioService = this.registry.get('audioService') as AudioService

        audioService.once('audio-completed', () => {
            this.cleanupWebSocket()
            setTimeout(() => {
                router.replace({ name: 'Home' })
            }, 1000)
        })

        if (event.audioAvailable && event.audioId) {
            let fallbackTimeout: ReturnType<typeof setTimeout> | null = null

            const handleAudioReceived = (audioId: string) => {
                if (audioId === event.audioId) {
                    if (fallbackTimeout) {
                        clearTimeout(fallbackTimeout)
                        fallbackTimeout = null
                    }
                    audioService.playDynamic(audioId)
                    audioService.off('audio-received', handleAudioReceived)
                }
            }
            audioService.on('audio-received', handleAudioReceived)

            fallbackTimeout = setTimeout(() => {
                if (!audioService.isCurrentlyPlaying()) {
                    console.warn('Audio dinámico no recibido en 3s, usando fallback estático')
                    audioService.off('audio-received', handleAudioReceived)
                    audioService.playStatic('farewell')
                }
            }, 3000)
        } else {
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
            clearInterval(this.registry.get('wsWorldbeat'))
            clearWebSocketHeartbeat(this.websocket)
            this.websocket.onmessage = null
            this.websocket.onclose = null
            this.websocket.onerror = null
            this.websocket.close()
            this.websocket = undefined
        }
    }

}
