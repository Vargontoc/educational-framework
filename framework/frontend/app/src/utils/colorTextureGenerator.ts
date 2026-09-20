import Phaser from 'phaser'

/**
 * Shapes available for color recognition
 */
export type ColorShape = 'circle' | 'square' | 'triangle' | 'star'

/**
 * Parse a backend-supplied accessible color value into a Phaser color number.
 * Accepts hex strings (e.g. "#FF0000") and the "GRAY" sentinel used for fully
 * achromatic color vision modes (e.g. ACHROMATOPSIA/ACHROMATOMALY).
 */
export function parseColorValue(value: string): number {
  if (value.toUpperCase() === 'GRAY') {
    return 0x808080
  }
  const hex = value.startsWith('#') ? value.slice(1) : value
  const parsed = parseInt(hex, 16)
  return Number.isNaN(parsed) ? 0x808080 : parsed
}

/**
 * Generate a unique texture key for a color value + shape combination
 */
export function getColorTextureKey(colorValue: string, shape: ColorShape): string {
  return `color_${colorValue.replace('#', '')}_${shape}`
}

/**
 * Draw a shape on a Phaser Graphics object
 */
function drawShape(
  graphics: Phaser.GameObjects.Graphics,
  shape: ColorShape,
  centerX: number,
  centerY: number,
  size: number
): void {
  switch (shape) {
    case 'circle':
      graphics.fillCircle(centerX, centerY, size / 2)
      break

    case 'square':
      graphics.fillRect(
        centerX - size / 2,
        centerY - size / 2,
        size,
        size
      )
      break

    case 'triangle':
      graphics.fillTriangle(
        centerX, centerY - size / 2,
        centerX - size / 2, centerY + size / 2,
        centerX + size / 2, centerY + size / 2
      )
      break

    case 'star':
      // Draw a 5-pointed star
      const points = 5
      const outerRadius = size / 2
      const innerRadius = outerRadius * 0.4

      for (let i = 0; i < points * 2; i++) {
        const radius = i % 2 === 0 ? outerRadius : innerRadius
        const angle = (i * Math.PI) / points - Math.PI / 2
        const x = centerX + radius * Math.cos(angle)
        const y = centerY + radius * Math.sin(angle)

        if (i === 0) {
          graphics.moveTo(x, y)
        } else {
          graphics.lineTo(x, y)
        }
      }
      graphics.closePath()
      graphics.fillPath()
      break
  }
}

/**
 * Generate a color texture from an already-resolved accessible color value and shape.
 * Backend (accessible_color / accessible_color_palette) is the single source of truth
 * for which color/shape to render for the child's colorVisionMode — this function only
 * turns that resolved data into a Phaser texture.
 * This creates the texture in Phaser's texture manager if it doesn't exist.
 */
export function generateColorTexture(
  scene: Phaser.Scene,
  colorValue: string,
  shape: ColorShape,
  size: number = 120
): string {
  const textureKey = getColorTextureKey(colorValue, shape)

  // Don't regenerate if texture already exists
  if (scene.textures.exists(textureKey)) {
    return textureKey
  }

  const color = parseColorValue(colorValue)

  // Create texture using Phaser's texture manager
  const texture = scene.textures.createCanvas(textureKey, size, size)

  if (!texture) {
    console.error(`Failed to create texture for ${textureKey}`)
    return textureKey
  }

  const graphics = scene.add.graphics()

  // Draw the shape
  graphics.fillStyle(color, 1)
  drawShape(graphics, shape, size / 2, size / 2, size * 0.8)

  // Render graphics to canvas
  graphics.generateTexture(textureKey, size, size)
  graphics.destroy()

  texture.refresh()

  return textureKey
}
