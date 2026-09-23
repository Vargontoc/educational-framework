import { TintModes, type GameObjects } from "phaser"
import type { RECOGNITION_TYPE } from "../GameEvent"

export const RECOGNITION_PALETTE = [
    0xE57373, // rojo suave
    0x64B5F6, // azul suave
    0x81C784, // verde suave
    0xFFF176, // amarillo suave
    0xFFB74D, // naranja suave
    0xBA68C8  // morado suave
] as const

const TINT_DATA_KEY = 'recognitionTint'
const TINT_FILL_DATA_KEY = 'recognitionTintFill'

/** Categorias cuyas imagenes son glifos claros (trazo negro, sin relleno) pensados para recibir tint. */
const TINTED_CATEGORIES: readonly RECOGNITION_TYPE[] = ['LETTER', 'NUMBER', 'SHAPE']

export class RecognitionColorizer {
    static appliesTo(category: RECOGNITION_TYPE | null | undefined): boolean {
        return !!category && TINTED_CATEGORIES.includes(category)
    }

    /**
     * Colores distintos para las opciones de una ronda. Si hay mas opciones que
     * colores en la paleta se recicla, pero nunca dos consecutivos iguales.
     */
    assignColors(optionCount: number): number[] {
        if (optionCount <= 0) return []

        const colors: number[] = []
        while (colors.length < optionCount) {
            const batch = this.shuffle<number>([...RECOGNITION_PALETTE])
            if (colors.length > 0 && batch[0] === colors[colors.length - 1]) {
                batch.push(batch.shift() as number)
            }
            colors.push(...batch)
        }
        return colors.slice(0, optionCount)
    }

    /**
     * Color para la tarjeta del estimulo: uno que no use ninguna opcion, para que
     * el color no sirva de pista para emparejar estimulo y respuesta.
     */
    pickStimulusColor(usedColors: number[]): number | undefined {
        const free = RECOGNITION_PALETTE.filter(c => !usedColors.includes(c))
        if (free.length === 0) return undefined
        return free[Math.floor(Math.random() * free.length)]
    }

    /**
     * `fill=true` (SHAPE) sustituye el color de cada pixel opaco por el tint (modo `FILL`), en vez
     * de multiplicarlo (modo `MULTIPLY`, el normal). Los glifos de forma son solo trazo negro sobre
     * transparente (sin relleno claro como letras/numeros), y negro * cualquier tint sigue siendo
     * negro: en modo MULTIPLY no se ve ningun color.
     */
    applyTint(image: GameObjects.Image, color: number, fill: boolean = false): void {
        image.setTint(color)
        image.setTintMode(fill ? TintModes.FILL : TintModes.MULTIPLY)
        image.setData(TINT_DATA_KEY, color)
        image.setData(TINT_FILL_DATA_KEY, fill)
    }

    clearTint(image: GameObjects.Image): void {
        image.clearTint()
        image.setData(TINT_DATA_KEY, undefined)
        image.setData(TINT_FILL_DATA_KEY, undefined)
    }

    /** Tint asignado a la ronda (para restaurarlo tras un feedback temporal). */
    getTint(image: GameObjects.GameObject): number | undefined {
        return image.getData(TINT_DATA_KEY) as number | undefined
    }

    /** Reaplica el tint asignado a la ronda (o lo limpia si no habia ninguno), respetando su modo. */
    restoreTint(image: GameObjects.Image): void {
        const color = this.getTint(image)
        if (color === undefined) {
            image.clearTint()
            return
        }
        this.applyTint(image, color, !!image.getData(TINT_FILL_DATA_KEY))
    }

    private shuffle<T>(items: T[]): T[] {
        for (let i = items.length - 1; i > 0; i--) {
            const j = Math.floor(Math.random() * (i + 1))
            ;[items[i], items[j]] = [items[j], items[i]]
        }
        return items
    }
}
