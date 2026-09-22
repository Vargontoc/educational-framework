export type TYPE_SEND_EVENT = 'auth' | 'heartbeat' | 'world_discovery_interacted' |  'game_start' | 'game_ready' |  'game_action' | 'game_abandon' | 'world_heartbeat' | 'world_travel'
export type SERVER_EVENT =
    'AUTH_ACK' |
    'HEARTBEAT_ACK' |
    'CHILD_EXPELLED' |
    'SESSION_EXPIRED' |
    'SESSION_INVALIDATED' |
    'CHILD_TTS_ACTIVATED' |
    'CHILD_TTS_DEACTIVATED' |
    'CHILD_AGENT_ACTIVATED' |
    'CHILD_AGENT_DEACTIVATED' |
    'GAME_ERROR' |
    'WORLD_STATE_SYNC' |
    'GAME_AVATAR_EVENT' |
    `WORLD_ACTIVITY_STARTED` |
    'GAME_STARTED' |
    'GAME_READY' |
    'GAME_ACTION_RESULT'
export type GAME_RESULT_TYPE = 'CORRECT' | 'INCORRECT' | 'TIMEOUT'
export type AVATAR_TYPE_EVENT = 'WELCOME' | 'FAREWELL' | 'BIOME_TRANSITION' | 'ROUND_PROMPT'
export type GAME_ENGINE = 'RECOGNITION' | 'MEMORY' | 'ASSOCIATION' | 'COUNT' | 'COMPARE' | 'PUZZLE'
export type RECOGNITION_TYPE = 'LETTER' | 'NUMBER' | 'SHAPE' | 'COLOR' | 'ANIMAL' | 'COMPARISON' | 'MEMORY'

class GameEvent {

    constructor(type: TYPE_SEND_EVENT) {
        this.type = type
    }
    type?: TYPE_SEND_EVENT
}

interface BaseServerGameEvent<E extends Exclude<SERVER_EVENT, 'GAME_AVATAR_EVENT'>, P> {
    event: E
    sessionId: number
    payload: P
}

export interface GameErrorPayload {
  errorCode?: string
  message?: string
}

export type AuthAckEvent = BaseServerGameEvent<'AUTH_ACK', null>
export type HeartbeatAckEvent = BaseServerGameEvent<'HEARTBEAT_ACK', null>
export type ChildExpelledEvent = BaseServerGameEvent<'CHILD_EXPELLED', null>
export type SessionExpiredEvent = BaseServerGameEvent<'SESSION_EXPIRED', null>
export type SessionInvalidatedEvent = BaseServerGameEvent<'SESSION_INVALIDATED', null>
export type ChildTTSActivatedEvent = BaseServerGameEvent<'CHILD_TTS_ACTIVATED', null>
export type ChildTTSDeactivatedEvent = BaseServerGameEvent<'CHILD_TTS_DEACTIVATED', null>
export type ChildAgentActivatedEvent = BaseServerGameEvent<'CHILD_AGENT_ACTIVATED', null>
export type ChildAgentDeactivatedEvent = BaseServerGameEvent<'CHILD_AGENT_DEACTIVATED', null>
export type GameErrorEvent = BaseServerGameEvent<'GAME_ERROR', GameErrorPayload | null>
export type WorldStateSyncEvent = BaseServerGameEvent<'WORLD_STATE_SYNC', WorldSync>
export type GameStateEvent = BaseServerGameEvent<'WORLD_ACTIVITY_STARTED', GameState>
export type GameStartedEvent = BaseServerGameEvent<'GAME_STARTED', RecognitionEnginePayload | MemoryEnginePayload>
export type GameSetEvent = BaseServerGameEvent<'GAME_READY', RecognitionEnginePayload | MemoryEnginePayload>
export type GameResultEvent = BaseServerGameEvent<'GAME_ACTION_RESULT', BaseGameActionResult<RecognitionEnginePayload | MemoryEnginePayload>>

// Añade aquí un nuevo BaseServerGameEvent<'NUEVO_EVENTO', PayloadType> por cada evento del servidor
// y súmalo a esta unión: el resto del código estrechará payload automáticamente por event.event
export type ServerGameEvent = AuthAckEvent
    | HeartbeatAckEvent 
    | ChildExpelledEvent
    | SessionExpiredEvent
    | SessionInvalidatedEvent
    | ChildTTSActivatedEvent
    | ChildTTSDeactivatedEvent
    | ChildAgentActivatedEvent
    | ChildAgentDeactivatedEvent
    | GameErrorEvent
    | WorldStateSyncEvent 
    | GameStateEvent
    | GameStartedEvent
    | GameSetEvent
    | GameResultEvent



export class AvatarEvent {
    event: 'GAME_AVATAR_EVENT' = 'GAME_AVATAR_EVENT'
    sessionId: number = 0
    eventType: AVATAR_TYPE_EVENT = 'WELCOME'
    audioAvailable: boolean = false
    audioId?: string
    text: string = ''
}

export class HeartbeatEvent extends GameEvent {
    constructor() { super('heartbeat') }
}

export class WorldHeartbeatEvent extends GameEvent {
    constructor() { super('world_heartbeat') }
}

export class WorldTravelEvent extends GameEvent {
    constructor(biome: string) {
        super('world_travel')
        this.biome = biome
    }
    biome: string
}

export class AuthGameEvent extends GameEvent {
    constructor() { super('auth') }

    childSessionId?: number
}

export class GameStartEvent extends GameEvent {
    constructor() { super('game_start') }
    activityId?: number
}

export class GameReadyEvent extends GameEvent {
    constructor() { super('game_ready') }
}

export class GameRecognitionActionEvent extends GameEvent {
    constructor() { super('game_action') }
    action: string = ''

    // NOTE (OBS-070-4): Pre-existing contract discrepancy.
    // AsyncAPI contract (game-client-message.yaml) defines `action` as a plain
    // string and `responseTimeMs` as a separate integer field. This implementation
    // serializes both into a single JSON string in `action`. Deferred to a future
    // sprint for alignment — do not change without backend coordination.
    setAction(id: string, time: number, scalePercent?: number) {
        // COMPARISON: every option is the same element, so the tapped option is told apart by its size.
        const scale = scalePercent === undefined ? '' : `, "selectedScalePercent" : ${scalePercent}`
        this.action = `{"selectedOptionId" : "${id}", "responseTimeMs" : ${time}${scale}}`
    }
}

/** Accion del juego de memoria: la carta que el nino ha tocado. */
export class GameMemoryActionEvent extends GameEvent {
    constructor() { super('game_action') }
    action: string = ''

    setAction(cardId: string, time: number) {
        this.action = JSON.stringify({ cardId, responseTimeMs: time })
    }
}

export class GameAbandonEvent extends GameEvent {
    constructor() { super('game_abandon') }
}

export class RecognitionAction {
    selectedOptionId: string = ''
    responseTimeMs: number = 0
}

export class WorldDiscoveryElementInteractiveEvent extends GameEvent {
    constructor() { super('world_discovery_interacted')}
    proposalRuntimeId: string = ''
    discoveryElementId: number = 0
}

export class WorldSync {
    status?: string
    destination?: WorldDestination
    positionX?: number
    positionY?: number
}

export class WorldDestination {
    destinationId: string = ''
    biome: string = ''
    host?: WorldHost
    discoveryElements: WorldDiscoveryElements[] = []
}

export class WorldHost {
    id: number = 0
    code: string = ''
    displayName: string = ''
    visualAssetKey: string = ''
    worldWidth?: number
    sequenceOrder?: number
}

export class WorldDiscoveryElements {
    proposalRuntimeId: string = ''
    discoveryElementId: number = 0
    elementType: string = ''
    visualAssetKey: string = ''
    interactionCueType: string = ''
    hasActivity: boolean = false
    positionX?: number
    positionY?: number
}

export class GameState {
    gameId: number = 0
    activityId: number = 0
    transition: string = ''
    engine?:  GAME_ENGINE
}

export class BaseEnginePayload {
    engine: GAME_ENGINE

    constructor(engine: GAME_ENGINE) {
        this.engine = engine
    }
    activityId: number = 0
    gameId: number  = 0
    difficultyLevelId: number = 1
    status: string = ''

}

export class BaseGameActionResult<P extends BaseEnginePayload> {
    resultType?: GAME_RESULT_TYPE
    difficultyChanged: boolean = false
    newDifficultyLevelId: number = 0
    gameCompleted: boolean = false
    attemptContext: string = ''
    updatedState?: P
}

export class AccessibleColor {
    value: string = ''
    shapeIcon: string = ''
    labelKey: string = ''
}

export class RecognitionElement {
    id: string = ''
    code: string = ''
    displayValue: string = ''
    resourceRefs?: Record<string, string>
    accessibleColor?: AccessibleColor
}

/** Una opcion de una ronda de comparacion: siempre el mismo elemento, a distinto tamano relativo. */
export class ComparisonOption {
    elementId: string = ''
    scalePercent: number = 100
}

export class RecognitionState {
    elements: RecognitionElement[] = []
    recognitionCategory?: RECOGNITION_TYPE
    roundIndex: number = 0
    totalRounds: number = 0
    hintActive: boolean = false
    targetElementId: string = ''
    optionIds: string[] = []
    guideChromEnabled: boolean = false
    touchEnableDelayMs: number = 0
    nonChromaticKeyRequired: boolean = false
    /** COLOR: muestra el item de referencia (EASY/MEDIUM). Los payloads antiguos sin el campo cuentan como true. */
    showIcon: boolean = true
    /** COMPARISON (grande/pequeno): todas las opciones son el mismo elemento a distinto tamano. */
    comparisonMode: boolean = false
    comparisonOptions: ComparisonOption[] = []
}

export class RecognitionEnginePayload extends BaseEnginePayload {
    declare engine: 'RECOGNITION'
    constructor() { super('RECOGNITION') }
    recognitionState?: RecognitionState
}

/**
 * Una carta del tablero de memoria. El servidor solo envia `elementId` mientras la carta esta boca arriba o
 * emparejada: una carta boca abajo no revela que hay debajo.
 */
export class MemoryCard {
    cardId: string = ''
    elementId?: string | null = null
    faceUp: boolean = false
    matched: boolean = false
    row: number = 0
    column: number = 0
}

export class MemoryState {
    rows: number = 0
    columns: number = 0
    totalPairs: number = 0
    matchedPairs: number = 0
    /** Tiempo que una pareja que no coincide permanece a la vista antes de volver boca abajo. */
    flipBackDelayMs: number = 0
    /** Hay una pareja que no coincide a la vista; `flipBackCardIds` son sus cartas. */
    waitingForFlipBack: boolean = false
    flipBackCardIds: string[] = []
    cards: MemoryCard[] = []
    /** Todos los elementos del tablero (sin decir donde esta cada uno), para cargar sus imagenes. */
    elements: RecognitionElement[] = []
}

export class MemoryEnginePayload extends BaseEnginePayload {
    declare engine: 'MEMORY'
    constructor() { super('MEMORY') }
    memoryState?: MemoryState
}


