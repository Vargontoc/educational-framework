import { openSession } from "@/services/sessionService";
import { Scene } from "phaser";
import router from "@/router";

export class LoadingScene extends Scene {
    constructor() {
        super({ key: 'loading', active: true })
    }

    create() {
        const childId = this.registry.get('childId') as number

        this.showLoadingPlaceholder()

        openSession(childId)
            .then(session => {
                if (!session) {
                    this.handleBlockedProfile()
                    return
                }
                this.loadAssetsSilently()
            })
            .catch(error => {
                console.error('Error opening session:', error)
                this.handleSessionError()
            })
    }

    showLoadingPlaceholder() {
        this.add.rectangle(400, 300, 800, 600, 0xf0f4f8)

        const icon = this.add.circle(400, 280, 40, 0x4a90e2)
        this.tweens.add({
            targets: icon,
            alpha: 0.3,
            duration: 1000,
            yoyo: true,
            repeat: -1
        })
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
        this.scene.start('base-state')
    }

    handleBlockedProfile() {
        router.replace({ name: 'Home' })
    }

    handleSessionError() {
        router.replace({ name: 'Home' })
    }
}
