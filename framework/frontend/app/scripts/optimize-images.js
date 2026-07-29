import sharp from 'sharp'
import { fileURLToPath } from 'url'
import { dirname, join } from 'path'

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

const inputPath = join(__dirname, '../src/assets/images/avatar-bot.png')
const outputPath = join(__dirname, '../src/assets/images/avatar-bot.webp')

sharp(inputPath)
  .webp({ quality: 80 })
  .toFile(outputPath)
  .then((info) => {
    console.log('Image optimized:', outputPath)
    console.log('Output size:', info.size, 'bytes')
  })
  .catch((err) => console.error('Error:', err))
