export type TYPE_SEND_EVENT = 'auth' | 'heartbeat' | 'world_discovery_interacted' |  'game_start' | 'game_ready' |  'game_action' | 'world_heartbeat'
export type SERVER_EVENT =
    'AUTH_ACK' |
    'HEARTBEAT_ACK' |
    'WORLD_STATE_SYNC' |
    'GAME_AVATAR_EVENT' |
    `WORLD_ACTIVITY_STARTED` |
    'GAME_STARTED' |
    'GAME_READY' |
    'GAME_ACTION_RESULT'
export type GAME_RESULT_TYPE = 'CORRECT' | 'INCORRECT' | 'TIMEOUT'
export type AVATAR_TYPE_EVENT = 'SESSION_CONNECTED' | 'SESSION_DISCONNECTED'
export type GAME_ENGINE = 'RECOGNITION' | 'MEMORY' | 'ASSOCIATION' | 'COUNT' | 'COMPARE' | 'PUZZLE'
export type RECOGNITION_TYPE = 'LETTER' | 'NUMBER'

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

export type AuthAckEvent = BaseServerGameEvent<'AUTH_ACK', null>
export type HeartbeatAckEvent = BaseServerGameEvent<'HEARTBEAT_ACK', null>
export type WorldStateSyncEvent = BaseServerGameEvent<'WORLD_STATE_SYNC', WorldSync>
export type GameStateEvent = BaseServerGameEvent<'WORLD_ACTIVITY_STARTED', GameState>
export type GameStartedEvent = BaseServerGameEvent<'GAME_STARTED', RecognitionEnginePayload>
export type GameSetEvent = BaseServerGameEvent<'GAME_READY', RecognitionEnginePayload>
export type GameResultEvent = BaseServerGameEvent<'GAME_ACTION_RESULT', BaseGameActionResult<RecognitionEnginePayload>>

// Añade aquí un nuevo BaseServerGameEvent<'NUEVO_EVENTO', PayloadType> por cada evento del servidor
// y súmalo a esta unión: el resto del código estrechará payload automáticamente por event.event
export type ServerGameEvent = AuthAckEvent
    | HeartbeatAckEvent 
    | WorldStateSyncEvent 
    | GameStateEvent
    | GameStartedEvent
    | GameSetEvent
    | GameResultEvent



export class AvatarEvent {
    event: 'GAME_AVATAR_EVENT' = 'GAME_AVATAR_EVENT'
    sessionId: number = 0
    eventType: AVATAR_TYPE_EVENT = 'SESSION_CONNECTED'
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

    setAction(id: number, time: number) {
        this.action = `{"selectedOptionId" : "${id}", "responseTimeMs" : ${time}}`
    }
}

export class RecognitionAction {
    selectedOptionId: number = 0
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
}

export class WorldDiscoveryElements {
    proposalRuntimeId: string = ''
    discoveryElementId: number = 0
    elementType: string = ''
    visualAssetKey: string = ''
    interactionCueType: string = ''
    hasActivity: boolean = false
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

export class RecognitionElementResource {
    image: string = ''
    audio: string = ''
}

export class RecognitionElement {
    id: number = 0
    resourceRefs?: RecognitionElementResource
}

export class RecognitionState {
    elements: RecognitionElement[] = []
    recognitionCategory?: RECOGNITION_TYPE
}

export class RecognitionEnginePayload extends BaseEnginePayload {
    constructor() { super('RECOGNITION') }
    recognitionState?: RecognitionState
}


