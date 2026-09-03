export type TYPE_SEND_EVENT = 'auth' | 'heartbeat' | 'world_discovery_interacted' |  'game_start' | 'world_heartbeat'
export type SERVER_EVENT =
    'AUTH_ACK' |
    'HEARTBEAT_ACK' |
    'WORLD_STATE_SYNC' |
    'GAME_AVATAR_EVENT'
export type AVATAR_TYPE_EVENT = 'SESSION_CONNECTED' | 'SESSION_DISCONNECTED'
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

// Añade aquí un nuevo BaseServerGameEvent<'NUEVO_EVENTO', PayloadType> por cada evento del servidor
// y súmalo a esta unión: el resto del código estrechará payload automáticamente por event.event
export type ServerGameEvent = AuthAckEvent | HeartbeatAckEvent | WorldStateSyncEvent



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

