import { Scene } from 'phaser'
import router from '@/router'
import { AudioService } from '@/services/AudioService'
import { AudioCache } from '@/services/AudioCache'

export class FarewellScene extends Scene {
  private farewellDuration = 5000

  constructor() {
    super({ key: 'farewell', active: false })
  }

  create() {
    const cx = 640
    const cy = 360

    this.add.rectangle(cx, cy, 1280, 720, 0xfef3c7)

    const nubiPlaceholder = this.add.circle(cx, cy - 72, 80, 0x7ec8e3)
    this.tweens.add({
      targets: nubiPlaceholder,
      alpha: 0.5,
      duration: 1500,
      yoyo: true,
      repeat: -1,
      ease: 'Sine.easeInOut'
    })

    const farewellText = this.add.text(cx, cy + 96, '\u00a1Hasta pronto!', {
      fontSize: '32px',
      color: '#111827',
      fontFamily: 'Nunito, sans-serif',
      fontStyle: '600'
    })
    farewellText.setOrigin(0.5, 0.5)

    this.playFarewellVoice()

    this.time.delayedCall(this.farewellDuration, () => {
      router.replace({ name: 'Home' })
    })
  }

  private playFarewellVoice() {
    const npcEnabled = this.registry.get('npcEnabled') as boolean ?? true
    const ttsEnabled = this.registry.get('ttsEnabled') as boolean ?? true
    const voiceEnabled = this.registry.get('voiceEnabled') as boolean ?? true

    if (npcEnabled && ttsEnabled && voiceEnabled) {
      const audioService = this.registry.get('audioService') as AudioService | undefined
      if (audioService) {
        audioService.playStatic('farewell')
      }
    }
  }

  shutdown() {
    const audioService = this.registry.get('audioService') as AudioService
    if (audioService) {
      audioService.stop()
    }
  }

  destroy() {
    const audioService = this.registry.get('audioService') as AudioService
    if (audioService) {
      audioService.dispose()
    }

    const audioCache = this.registry.get('audioCache') as AudioCache
    if (audioCache) {
      audioCache.clear()
    }
  }
}
