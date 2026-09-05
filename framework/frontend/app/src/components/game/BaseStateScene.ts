import { Scene } from 'phaser'

export class BaseStateScene extends Scene {
  constructor() {
    super({ key: 'base-state', active: false })
  }

  create() {
    this.add.rectangle(400, 300, 800, 600, 0xe8f4f8)

    const nubiPlaceholder = this.add.circle(400, 260, 60, 0x7ec8e3)
    
    this.tweens.add({
      targets: nubiPlaceholder,
      scaleX: 1.05,
      scaleY: 1.05,
      duration: 2000,
      yoyo: true,
      repeat: -1,
      ease: 'Sine.easeInOut'
    })
  }
}
