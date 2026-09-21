import { Scene } from "phaser";
import router from "@/router";
import { AvatarEvent, ServerGameEvent, WorldHeartbeatEvent, GameErrorPayload, WorldDiscoveryElements, WorldDiscoveryElementInteractiveEvent } from "./GameEvent";
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
import { BiomeSelectorLayer, DESTINATION_SELECTED_EVENT, SELECTOR_CLOSED_EVENT, BiomeHostInfo } from "./worldmap/layers/BiomeSelectorLayer";
import { EdgeHintLayer } from "./worldmap/layers/EdgeHintLayer";
import { ExitPortalLayer, PORTAL_TOUCHED_EVENT, PORTAL_MARGIN, PORTAL_SIZE } from "./worldmap/layers/ExitPortalLayer";
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

const BIOME_ORDER = ['meadow', 'farm', 'woods', 'beach', 'space', 'prehistory']

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
    private exitPortalLayer?: ExitPortalLayer
    private scroller?: GradualScroller
    private environmentReaction?: EnvironmentReaction
    private currentBiome?: string
    private activeWorldWidth: number = WORLD_MAP_CONFIG.worldWidth
    private pendingBiomeLoad?: string
    private arrivalInProgress = false
    private sessionResumed = false
    private transitioningToMinigame = false
    private pendingMinigameElement?: WorldDiscoveryElements

    constructor() { super({ key: 'world-map', active: false}) }

    init(data: { websocket: WebSocket; sessionId?: number; childId?: number }) {
        this.websocket = data.websocket
        this.sessionId = data.sessionId
        this.childId = data.childId
        this.transitioningToMinigame = false
        this.pendingMinigameElement = undefined
    }

    create() {
        const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
        const npcEnabled = this.registry.get('npcEnabled') as boolean

        // Verify WebSocket state when returning from minigame
        if (this.websocket) {
            console.log('[WorldMapScene] WebSocket state on create:', this.websocket.readyState, '(0=CONNECTING, 1=OPEN, 2=CLOSING, 3=CLOSED)')
        }

        // create() se re-ejecuta sobre la MISMA instancia de escena al volver
        // de un minijuego (Phaser reutiliza la instancia con scene.start(),
        // no crea una nueva) — los campos de clase sobreviven. Sin este
        // reset, arrivalInProgress se quedaba en `true` para siempre desde
        // enterMinigame() (que lo pone a true pero nunca lo revierte al
        // volver, a diferencia de beginBiomeArrival), bloqueando en
        // silencio el portal, el selector de biomas Y cualquier
        // reconstrucción futura de discoveryElements — el bug crítico de
        // "parece que pierde la conexión".
        this.arrivalInProgress = false
        this.transitioningToMinigame = false

        // currentBiome ya tiene valor si esto es un restart tras volver de
        // un minijuego (no la primera vez que se ejecuta create() en esta
        // instancia) — en ese caso NO se reinicia a INITIAL_BIOME ni se
        // reconstruye como si el niño acabara de "llegar": seguía en el
        // mismo bioma antes de entrar al minijuego, así que se reconstruye
        // tal cual, sin fundido ni animación de arribo.
        const isReturningFromMinigame = this.currentBiome !== undefined
        const bootBiome = isReturningFromMinigame ? this.currentBiome! : INITIAL_BIOME

        if (!isReturningFromMinigame) {
            this.currentBiome = INITIAL_BIOME
            this.activeWorldWidth = WORLD_MAP_CONFIG.worldWidth
        }

        this.backgroundLayer = new BackgroundLayer(this)
        this.backgroundLayer.buildFrom(bootBiome)

        this.groundLayer = new GroundLayer(this)
        const groundContainer = this.groundLayer.create(bootBiome, this.activeWorldWidth)

        this.parallaxLayer = new ParallaxLayer(this)
        const parallaxContainer = this.parallaxLayer.create(this.groundLayer.getBandHeight(), bootBiome, this.activeWorldWidth)

        this.nubiLayer?.create(npcEnabled, this.getGroundTopY())

        this.interactiveLayer = new InteractiveLayer(this)
        const interactiveContainer = this.interactiveLayer.create()
        this.environmentReaction = new EnvironmentReaction(this)
        this.interactiveLayer.setOnTouch((element, shape, pointer) => {
            this.environmentReaction?.play(shape)
            this.nubiLayer?.walkTo(this.clampToWorldWidth(pointer.x + (this.scroller?.getOffset() ?? 0)))

            if (element.hasActivity && !this.arrivalInProgress) {
                this.enterMinigame(element)
            }
        })

        this.transportLayer = new TransportLayer(this)
        const transportContainer = this.transportLayer.create(bootBiome, this.getGroundTopY(), WORLD_MAP_CONFIG.viewportWidth / 6)

        this.biomeSelectorLayer = new BiomeSelectorLayer(this)

        this.edgeHintLayer = new EdgeHintLayer(this)
        this.edgeHintLayer.create()
        this.edgeHintLayer.setRightEdgeSuppressed(bootBiome === 'prehistory')

        this.exitPortalLayer = new ExitPortalLayer(this)
        const exitPortalContainer = this.exitPortalLayer.create(bootBiome, this.activeWorldWidth, this.getGroundTopY())

        this.scroller = new GradualScroller(this, reducedMotion)
        this.scroller.attach()
        this.scroller.setMaxScrollOffset(this.computeMaxScrollOffset(this.activeWorldWidth, bootBiome))
        if (!reducedMotion) {
            this.scroller.registerLayer(parallaxContainer, WORLD_MAP_CONFIG.parallaxFactor)
        }
        this.scroller.registerLayer(groundContainer, WORLD_MAP_CONFIG.groundFactor)
        this.scroller.registerLayer(interactiveContainer, 1)
        this.scroller.registerLayer(transportContainer, 1)
        if (exitPortalContainer) {
            this.scroller.registerLayer(exitPortalContainer, 1)
        }

        this.events.on(WORLDMAP_TAP_EVENT, (x: number) => this.nubiLayer?.walkTo(this.clampToWorldWidth(x + (this.scroller?.getOffset() ?? 0))))

        this.events.on(TRANSPORT_TOUCHED_EVENT, () => this.handleTransportTouched())
        this.events.on(DESTINATION_SELECTED_EVENT, (biome: string) => this.handleDestinationSelected(biome))
        this.events.on(PORTAL_TOUCHED_EVENT, () => this.handlePortalTouched())
        this.events.on(SELECTOR_CLOSED_EVENT, () => this.nubiLayer?.setSelectorOpen(false))
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

        // Don't close WebSocket when transitioning to minigame — RecognitionGameScene needs it
        if (!this.transitioningToMinigame) {
            this.cleanupWebSocket()
        }
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
        this.exitPortalLayer?.destroy()
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
        console.log('[WorldMapScene] Setting up WebSocket handlers, readyState:', ws.readyState)
        
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
            console.log('[WorldMapScene] WebSocket closed')
            clearWebSocketHeartbeat(ws)
            this.connectionMonitor?.handleWebSocketClose()
        }

        ws.onerror = (error) => {
            console.error('[WorldMapScene] WebSocket error:', error)
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
                            event.payload.destination.discoveryElements,
                            event.payload.positionX,
                            event.payload.positionY
                        )
                    } else {
                        this.interactiveLayer?.render([])
                    }
                    break;
                case 'WORLD_ACTIVITY_STARTED':
                    if (event.payload?.activityId && this.pendingMinigameElement) {
                        this.startMinigameTransition(event.payload.activityId)
                    }
                    break;
                case 'CHILD_AGENT_ACTIVATED':
                    this.registry.set('npcEnabled', true)
                    this.registry.events.emit('npc-state-changed', true)
                    break;
                case 'CHILD_AGENT_DEACTIVATED':
                    this.registry.set('npcEnabled', false)
                    this.registry.events.emit('npc-state-changed', false)
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
        discoveryElements: WorldDiscoveryElements[],
        positionX?: number,
        _positionY?: number
    ) {
        const normalizedBiome = biome.toLowerCase()
        const effectiveWorldWidth = hostWorldWidth ?? WORLD_MAP_CONFIG.worldWidth
        const biomeChanged = normalizedBiome !== this.currentBiome
        const worldWidthChanged = effectiveWorldWidth !== this.activeWorldWidth
        const isResume = !this.sessionResumed && positionX != null
        this.sessionResumed = true

        if (isResume) {
            // No hay "mundo viejo" visible que proteger todavía (es la primera
            // sincronización de la sesión) — aplicar de inmediato es seguro.
            this.activeWorldWidth = effectiveWorldWidth
            this.scroller?.setMaxScrollOffset(this.computeMaxScrollOffset(effectiveWorldWidth, normalizedBiome))
            const resumeOffset = positionX! * this.computeMaxScrollOffset(effectiveWorldWidth, normalizedBiome)
            if (biomeChanged) {
                this.pendingBiomeLoad = normalizedBiome
                this.ensureBiomeAssetsLoaded(normalizedBiome, () => {
                    if (this.pendingBiomeLoad !== normalizedBiome) return
                    this.pendingBiomeLoad = undefined
                    this.currentBiome = normalizedBiome
                    this.rebuildLayersForBiome(normalizedBiome, effectiveWorldWidth)
                    this.interactiveLayer?.render(discoveryElements, effectiveWorldWidth)
                    this.scroller?.setOffset(resumeOffset)
                    this.nubiLayer?.setWorldX(resumeOffset + WORLD_MAP_CONFIG.viewportWidth / 6)
                })
            } else if (worldWidthChanged) {
                this.currentBiome = normalizedBiome
                this.rebuildLayersForBiome(normalizedBiome, effectiveWorldWidth)
                this.interactiveLayer?.render(discoveryElements, effectiveWorldWidth)
                this.scroller?.setOffset(resumeOffset)
                this.nubiLayer?.setWorldX(resumeOffset + WORLD_MAP_CONFIG.viewportWidth / 6)
            } else {
                this.interactiveLayer?.render(discoveryElements, effectiveWorldWidth)
                this.scroller?.setOffset(resumeOffset)
                this.nubiLayer?.setWorldX(resumeOffset + WORLD_MAP_CONFIG.viewportWidth / 6)
            }
            return
        }

        if (biomeChanged) {
            // A propósito NO se toca activeWorldWidth/maxScrollOffset todavía:
            // hacerlo aquí reclamparía el offset actual contra el ancho del
            // NUEVO bioma mientras el bioma VIEJO sigue visible en pantalla —
            // un salto de cámara instantáneo antes de que el fundido a negro
            // lo cubra (bug: "se muestra el mundo antes de que se coloquen
            // los elementos"). Se aplica dentro de beginBiomeArrival, ya bajo
            // el fundido, junto con el reseteo de cámara/Nubi al punto de
            // inicio del nuevo bioma.
            this.pendingBiomeLoad = normalizedBiome
            this.ensureBiomeAssetsLoaded(normalizedBiome, () => {
                if (this.pendingBiomeLoad !== normalizedBiome) return
                this.pendingBiomeLoad = undefined
                this.beginBiomeArrival(normalizedBiome, effectiveWorldWidth, () => {
                    this.activeWorldWidth = effectiveWorldWidth
                    this.scroller?.setMaxScrollOffset(this.computeMaxScrollOffset(effectiveWorldWidth, normalizedBiome))
                    this.currentBiome = normalizedBiome
                    this.rebuildLayersForBiome(normalizedBiome, effectiveWorldWidth)
                    this.interactiveLayer?.render(discoveryElements, effectiveWorldWidth)
                    this.scroller?.setOffset(0)
                    this.nubiLayer?.resetToStart()
                })
            })
        } else if (worldWidthChanged) {
            this.activeWorldWidth = effectiveWorldWidth
            this.scroller?.setMaxScrollOffset(this.computeMaxScrollOffset(effectiveWorldWidth, normalizedBiome))
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
    // los elementos interactivos nunca superan worldWidth) — SALVO en los
    // biomas con portal de salida (todos menos 'prehistory'), donde ese
    // margen extra sí contiene algo: el propio portal, colocado en
    // worldWidth + PORTAL_MARGIN (ver ExitPortalLayer). Ahí ampliamos el
    // límite justo lo necesario para revelarlo entero, reutilizando el mismo
    // colchón de Ground/ParallaxLayer (ya construidas con ancho
    // worldWidth + viewportWidth) sin tocar esas capas.
    private computeMaxScrollOffset(worldWidth: number, biome: string): number {
        const hasExitPortal = biome !== 'prehistory'
        const exitZoneMargin = hasExitPortal ? PORTAL_MARGIN + PORTAL_SIZE : 0
        return Math.max(0, worldWidth + exitZoneMargin - WORLD_MAP_CONFIG.viewportWidth)
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
        this.exitPortalLayer?.destroy()

        const groundContainer = this.groundLayer?.create(biome, worldWidth)
        const parallaxContainer = this.parallaxLayer?.create(this.groundLayer?.getBandHeight() ?? 0, biome, worldWidth)

        this.backgroundLayer?.buildFrom(biome)

        this.edgeHintLayer?.setRightEdgeSuppressed(biome === 'prehistory')

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

        this.exitPortalLayer = new ExitPortalLayer(this)
        const exitPortalContainer = this.exitPortalLayer.create(biome, worldWidth, this.getGroundTopY())
        if (exitPortalContainer) {
            this.scroller?.registerLayer(exitPortalContainer, 1)
        }

        this.nubiLayer?.adjustToGroundTopY(this.getGroundTopY())
    }

    private handleTransportTouched() {
        const hosts: BiomeHostInfo[] = ALL_BIOME_HOSTS.filter(
            host => host.biome.toLowerCase() !== this.currentBiome
        )
        this.biomeSelectorLayer?.open(hosts, ALL_BIOME_HOSTS.length)
        this.nubiLayer?.setSelectorOpen(true)
    }

    private handleDestinationSelected(biome: string) {
        const normalizedBiome = biome.toLowerCase()
        if (normalizedBiome === this.currentBiome) return
        if (this.arrivalInProgress) return

        this.sendWorldTravel(biome)
    }

    private handlePortalTouched() {
        if (this.arrivalInProgress) return
        if (!this.currentBiome) return

        const currentIndex = BIOME_ORDER.indexOf(this.currentBiome)
        if (currentIndex === -1) return

        const nextIndex = (currentIndex + 1) % BIOME_ORDER.length
        const nextBiome = BIOME_ORDER[nextIndex]

        // Nubi camina hasta el portal (fuera de [0, activeWorldWidth] a
        // propósito, sin pasar por clampToWorldWidth) y solo al llegar se
        // dispara el viaje — antes se transicionaba al instante, sin que
        // Nubi se moviera hacia el portal primero. El offset/posición de
        // Nubi para el bioma nuevo se resetean ya dentro de
        // beginBiomeArrival (bajo el fundido), no aquí.
        const portalWorldX = this.activeWorldWidth + PORTAL_MARGIN
        this.nubiLayer?.walkTo(portalWorldX, () => {
            this.sendWorldTravel(nextBiome)
        })
    }

    private enterMinigame(element: WorldDiscoveryElements): void {
        if (this.arrivalInProgress) return
        if (!element.hasActivity) return
        
        // Store the element for later transition
        this.pendingMinigameElement = element
        
        // Send world_discovery_interacted to backend
        if (this.websocket && this.websocket.readyState === WebSocket.OPEN) {
            const event = new WorldDiscoveryElementInteractiveEvent()
            event.proposalRuntimeId = element.proposalRuntimeId
            event.discoveryElementId = element.discoveryElementId
            this.websocket.send(JSON.stringify(event))
        }
    }

    private startMinigameTransition(activityId: number): void {
        if (this.arrivalInProgress) return
        this.arrivalInProgress = true
        this.transitioningToMinigame = true
        this.pendingMinigameElement = undefined  // Clear pending element

        const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
        const duration = reducedMotion ? 200 : 400

        const overlay = this.add.rectangle(
            WORLD_MAP_CONFIG.viewportWidth / 2,
            WORLD_MAP_CONFIG.viewportHeight / 2,
            WORLD_MAP_CONFIG.viewportWidth,
            WORLD_MAP_CONFIG.viewportHeight,
            0x000000,
            0
        )
        overlay.setDepth(200)
        overlay.setScrollFactor(0)

        this.tweens.add({
            targets: overlay,
            alpha: 1,
            duration,
            ease: 'Linear',
            onComplete: () => {
                this.scene.start('recognition-game', {
                    websocket: this.websocket,
                    activityId: activityId,
                    biome: this.currentBiome,
                    sessionId: this.sessionId,
                    childId: this.childId,
                })
            }
        })
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
                this.edgeHintLayer.setRightEdgeSuppressed(targetBiome === 'prehistory')
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
            case 'BIOME_TRANSITION':
                this.handleBiomeTransitionEvent(event)
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

    handleBiomeTransitionEvent(event: AvatarEvent) {
        if (!this.arrivalInProgress) return

        const npcEnabled = this.registry.get('npcEnabled') as boolean ?? false
        const ttsEnabled = this.registry.get('ttsEnabled') as boolean ?? false
        const voiceEnabled = this.registry.get('voiceEnabled') as boolean ?? false
        const expectsVoice = npcEnabled && ttsEnabled && voiceEnabled

        if (!expectsVoice) return

        const audioService = this.registry.get('audioService') as AudioService | undefined
        if (!audioService) return

        if (event.audioAvailable && event.audioId) {
            const audioId = event.audioId

            const handleAudioReceived = (receivedId: string) => {
                if (receivedId === audioId) {
                    audioService.playDynamic(audioId)
                    audioService.off('audio-received', handleAudioReceived)
                }
            }
            audioService.on('audio-received', handleAudioReceived)

            this.time.delayedCall(WORLD_MAP_CONFIG.arrivalAudioTimeout, () => {
                if (!audioService.isCurrentlyPlaying()) {
                    audioService.off('audio-received', handleAudioReceived)
                }
            })
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
