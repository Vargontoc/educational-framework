import sharp from 'sharp'
import { fileURLToPath } from 'url'
import { dirname, join } from 'path'

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)
const iconsDir = join(__dirname, '../public/icons')

const sizes = [192, 512]

for (const size of sizes) {
  const inputPath = join(iconsDir, `icon-${size}x${size}.svg`)
  const outputPath = join(iconsDir, `icon-${size}x${size}.png`)
  await sharp(inputPath, { density: 384 })
    .resize(size, size)
    .png()
    .toFile(outputPath)
  console.log('Generated:', outputPath)
}
