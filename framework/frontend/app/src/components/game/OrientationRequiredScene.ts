import { Scene } from 'phaser'

export class OrientationRequiredScene extends Scene {
  constructor() {
    super({ key: 'orientation-required', active: false })
  }

  create() {
    const cx = 640
    const cy = 360

    this.add.rectangle(cx, cy, 1280, 720, 0xf0f4f8)

    const deviceIcon = this.add.rectangle(cx, cy - 60, 80, 120, 0x4a90e2)
    deviceIcon.setStrokeStyle(4, 0x2563eb)

    const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches

    if (!prefersReducedMotion) {
      this.tweens.add({
        targets: deviceIcon,
        angle: 90,
        duration: 1500,
        yoyo: true,
        repeat: -1,
        ease: 'Sine.easeInOut'
      })
    }

    const hintText = this.add.text(cx, cy + 120, 'Gira tu tablet', {
      fontSize: '28px',
      color: '#111827',
      fontFamily: 'Nunito, sans-serif',
      fontStyle: '600',
      align: 'center'
    })
    hintText.setOrigin(0.5, 0.5)
  }
}
