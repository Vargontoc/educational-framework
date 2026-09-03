import { openSession } from "@/services/sessionService";
import { Scene } from "phaser";
import { AuthGameEvent, AvatarEvent, HeartbeatEvent, ServerGameEvent } from "./GameEvent";
import router from "@/router";

export class LoadingScene extends Scene {
    assetsLoaded: boolean = false
    greetAvatar: boolean = false
    websocket?: WebSocket
    worldMapPending: boolean = false

    constructor() {
        super({ 'key': "loading", active: true })
    }

    create() {
        
        const child = this.registry.get('childId') as number

        openSession(child).then((session) => {
            if(session != null) {
                this.connectWebSocket(session.id).then((ws) => {
                    this.websocket = ws
                })
            }
        }).catch(error => {
            console.log(error)
        })
    }


    connectWebSocket(child: number): Promise<WebSocket> {
        return new Promise((resolve, reject) => {
            const ws = new WebSocket(`ws://localhost:8080/ws/game?childSessionId=${child}`)
            ws.onopen = () =>  {
                console.log('⚡ Conexion establecida')
                const authEvent = new AuthGameEvent()
                authEvent.childSessionId = child
                ws.send(JSON.stringify(authEvent))

                const heartbeatId = setInterval(() => {
                    if(ws.readyState == WebSocket.OPEN){
                        ws.send(JSON.stringify(new HeartbeatEvent()))
                    }
                }, 6000)
                this.registry.set('wsHeartbeat', heartbeatId)

                // Solo limpiamos nuestro propio intervalo: el websocket se
                // entrega a la siguiente escena (world-map), que sigue
                // necesitando recibir mensajes por él.
                const cleanup = () => {
                    clearInterval(heartbeatId)
                }
                this.events.once('shutdown', cleanup)
                this.events.once('destroy', cleanup)

                resolve(ws)
            }
            ws.onmessage = (msg) => {
                if(msg.data){
                    this.readEvent(JSON.parse(msg.data))
                }
            }
            ws.onclose = () => {
                console.log('⚡ Sesión cerrada')
                clearInterval(this.registry.get('wsHeartbeat'))
            }
            ws.onerror = (err) => reject(err)
        });
    }
    
    preload() {
        const loadingText = this.add.text(400, 300, 'Descargando manifest de assets...', { fontSize: '20px' }).setOrigin(0.5);

       this.load.setBaseURL('/')
       this.load.pack('packManifest', 'assets-manifest.json', 'dev')

        this.load.on('progress', (value: number) => {
            loadingText.setText(`Cargando recursos: ${Math.round(value * 100)}%`);
        })

        this.load.on('complete', () => {
            this.assetsLoaded = true
            if(this.worldMapPending) {
                this.goToWorldMap()
            }
        })
    }

    readEvent(event: ServerGameEvent | AvatarEvent)
    {
        console.log(event)
        if(!event) return

        if(event.event === 'GAME_AVATAR_EVENT') {
            
            switch(event.eventType) {
                case 'SESSION_CONNECTED':
                    if(event.audioAvailable == true &&  event.audioId) {
                        // Play audio

                    }else {
                        this.greetAvatar = true
                        this.goToWorldMap()
                    }
                    break;
                case 'SESSION_DISCONNECTED':
                    if(event.audioAvailable == true &&  event.audioId) {
                        // Play audio

                    }else {
                        router.replace({name: 'Home'})
                    } 
                    break;
            }
            return
        }else {
            // event queda estrechado a ServerGameEvent
            if(event.payload){
                console.log(event.payload)
            }
        }

    }

    goToWorldMap() {
        if(this.assetsLoaded && this.websocket) {
            this.worldMapPending = false
            this.scene.start('world-map', { websocket: this.websocket })
        } else {
            this.worldMapPending = true
        }
    }
}