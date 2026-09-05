import { Scene } from "phaser";
import { AvatarEvent, GAME_RESULT_TYPE, GameReadyEvent, GameRecognitionActionEvent, GameStartEvent, RECOGNITION_TYPE, RecognitionElement, RecognitionEnginePayload, ServerGameEvent } from "./GameEvent";

export class RecognitionGameScene extends Scene {
    websocket?: WebSocket
    activityId?: number 

    startingGame: boolean = true;
    bloackActions: boolean = false
    dateClick: number = Date.now()

    images: Phaser.GameObjects.Image[] = []
    constructor() { super({ key: 'recognition-game', active: false })}

    init(data: {
        websocket: WebSocket,
        activityId: number
    }) {
        this.websocket = data.websocket
        this.activityId = data.activityId
    }

    preload() {
        if(this.websocket) {
            this.manageWs(this.websocket)
        }
    }


    manageWs(ws: WebSocket) {
        ws.onmessage = (msg) => {
            this.readEvent(JSON.parse(msg.data))
        }

        if(this.activityId && this.startingGame == true){
            this.startingGame = false
            let startEvent = new GameStartEvent();
            startEvent.activityId = this.activityId
            ws.send(JSON.stringify(startEvent))
        }
    }

    renderElements(items: RecognitionElement[]) {
        items.forEach((e, i) => {
            if(e.resourceRefs){
                if(!this.textures.exists(e.resourceRefs.image)) {
                    console.error('Texture not in cache after load complete:', e.resourceRefs.image)
                    return
                }

                
                
                this.images.push(this.add.image((i* 100) + 20, 400, e.resourceRefs.image).setScale(.1, .1).setInteractive()
                    .on('pointerdown', () => {
                        if(this.startingGame) return
                        if(this.bloackActions) return
                        this.bloackActions = true
                        if(this.websocket){
                            let ev = new GameRecognitionActionEvent()
                            let diff = Date.now() - this.dateClick
                            ev.setAction(e.id, diff)

                            this.websocket.send(JSON.stringify(ev))
                            this.dateClick = Date.now()
                        }
                    }))
            }
        })
    }

    loadResources(type: RECOGNITION_TYPE | null, items: RecognitionElement[]) {
        if(!type) return;
        if(!items || items.length <= 0) return;

        const allCached = items.every((e) => !e.resourceRefs || this.textures.exists(e.resourceRefs.image))
        if(allCached) {
            this.renderElements(items) 
            return
        }

        this.load.setBaseURL('/')
        switch(type) {
            case 'LETTER':
                this.load.pack('packManifestRecognitionLetters', 'assets-manifest.json', 'recognition-letters');
                this.load.on('loaderror', (file: Phaser.Loader.File) => { console.error('Failed to load asset', file.key, file.url) })
                this.load.once('complete', () => { 
                    this.renderElements(items)
                    this.startingGame = false
                })
                this.load.start();
            break;
            default:
                console.log('No implementado tipo: ' + type)
                break;
        }
    }
    

    readEvent(event: ServerGameEvent | AvatarEvent) {
        if(!this.websocket) return;
        if(!event) return;
        if(event.event === 'GAME_AVATAR_EVENT' ){
    
        }else{
            switch(event.event) {
                case 'GAME_STARTED':
                    if(event.payload.engine == "RECOGNITION") {
                        this.websocket.send(JSON.stringify(new GameReadyEvent()))
                    }
                    break;
                case 'GAME_ACTION_RESULT':
                    if(event.payload.resultType && event.payload.updatedState){
                        console.log(event.payload.updatedState)
                        this.applyActionToResultType(event.payload.resultType, event.payload.gameCompleted, event.payload.updatedState)
                    }
                    break
                case 'GAME_READY':
                    console.log(event.payload)
                    if(event.payload.engine == "RECOGNITION" && event.payload.recognitionState?.recognitionCategory) {
                        this.loadResources(event.payload.recognitionState.recognitionCategory, event.payload.recognitionState.elements)
                    }
                    break;
            }
        }
    }

    applyActionToResultType(result: GAME_RESULT_TYPE, complete: boolean, state: RecognitionEnginePayload){
        if(complete) {
            // Animacion y sonido
            // Ir a escena world map
            this.scene.start('world-map')
        }else {
            switch(result) {
                case 'CORRECT':
                    if(state.recognitionState && state.recognitionState.recognitionCategory) {
                        this.images.forEach((i) => { i.destroy(true) })
                        
                        this.renderElements(state.recognitionState.elements)
                    }
                    break;
                case 'INCORRECT': 
                    break;
                case 'TIMEOUT':
                    break;
            }
    
            this.bloackActions = false
        }
    }
}