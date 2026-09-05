import { Scene } from "phaser";
import { AvatarEvent, ServerGameEvent, WorldDiscoveryElementInteractiveEvent, WorldHeartbeatEvent } from "./GameEvent";

export class WorldMapScene extends Scene {
    websocket?: WebSocket

    constructor() { super({ key: 'world-map', active: false}) }

    init(data: { websocket: WebSocket }) {
        this.websocket = data.websocket
    }

    create() {
        if(this.websocket) {
            this.manageWebsocket(this.websocket)
        }
    }
    
    preload() {
        
    }

    manageWebsocket(ws: WebSocket) {
        const heartbeatId = setInterval(() => {
            if(ws.readyState == WebSocket.OPEN){
                ws.send(JSON.stringify(new WorldHeartbeatEvent()))
            }
        }, 1000)
        this.registry.set('wsWorldbeat', heartbeatId)

        ws.onmessage = (msg) => {
            this.readEvent(JSON.parse(msg.data))
        }

        const cleanup = () => {
            clearInterval(heartbeatId)
            ws.onmessage = null
        }
        this.events.once('shutdown', cleanup)
        this.events.once('destroy', cleanup)
    }

    readEvent(event: ServerGameEvent | AvatarEvent) {
        if(!event) return;
        if(event.event === 'GAME_AVATAR_EVENT' ){

        }else {
            switch(event.event) {
                case 'WORLD_STATE_SYNC' :
                    if(event.payload?.status && event.payload.status == 'ACTIVE' && event.payload.destination)  {
                        // TODO: Cargar mapa de bioma (event.payload.destination.biome)
                        event.payload.destination.discoveryElements.forEach((de)  => {
                            this.add.image(100, 100, de.visualAssetKey)
                                .setScale(.2, 0.2)
                                .setInteractive()
                                .on('pointerdown', () => {
                                    if(this.websocket && de.hasActivity == true){
                                        let ev = new WorldDiscoveryElementInteractiveEvent()
                                        ev.discoveryElementId = de.discoveryElementId
                                        ev.proposalRuntimeId = de.proposalRuntimeId
                                        this.websocket.send(JSON.stringify(ev))
                                    }
                                })
                        })
                    }
                    break;
                case 'WORLD_ACTIVITY_STARTED':
                    if(event.payload) {
                        switch(event.payload.engine){
                            case 'RECOGNITION':
                                this.scene.start('recognition-game', {
                                    websocket: this.websocket,
                                    activityId: event.payload.activityId
                                })
                                break;
                            default:
                                console.log('Aun no esta la escena para este engine: ' + event.payload.engine)
                                break;
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

}