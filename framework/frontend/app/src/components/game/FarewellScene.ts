import { Scene } from 'phaser'
import router from '@/router'

export class FarewellScene extends Scene {
  private farewellDuration = 5000

  constructor() {
    super({ key: 'farewell', active: false })
  }

  create() {
    this.add.rectangle(400, 300, 800, 600, 0xfef3c7)

    const nubiPlaceholder = this.add.circle(400, 240, 80, 0x7ec8e3)
    this.tweens.add({
      targets: nubiPlaceholder,
      alpha: 0.5,
      duration: 1500,
      yoyo: true,
      repeat: -1,
      ease: 'Sine.easeInOut'
    })

    const farewellText = this.add.text(400, 380, '\u00a1Hasta pronto!', {
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
      console.log('Farewell voice would play here')
    }
  }
}
