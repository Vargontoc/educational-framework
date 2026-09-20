import type { GameObjects } from "phaser"

export const LETTER_PALETTE = [
    0xE57373, // rojo suave
    0x64B5F6, // azul suave
    0x81C784, // verde suave
    0xFFF176, // amarillo suave
    0xFFB74D, // naranja suave
    0xBA68C8  // morado suave
] as const

const TINT_DATA_KEY = 'letterTint'

export class LetterColorizer {
    /**
     * Colores distintos para las opciones de una ronda. Si hay mas opciones que
     * colores en la paleta se recicla, pero nunca dos consecutivos iguales.
     */
    assignColors(optionCount: number): number[] {
        if (optionCount <= 0) return []

        const colors: number[] = []
        while (colors.length < optionCount) {
            const batch = this.shuffle<number>([...LETTER_PALETTE])
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
        const free = LETTER_PALETTE.filter(c => !usedColors.includes(c))
        if (free.length === 0) return undefined
        return free[Math.floor(Math.random() * free.length)]
    }

    applyTint(image: GameObjects.Image, color: number): void {
        image.setTint(color)
        image.setData(TINT_DATA_KEY, color)
    }

    clearTint(image: GameObjects.Image): void {
        image.clearTint()
        image.setData(TINT_DATA_KEY, undefined)
    }

    /** Tint asignado a la ronda (para restaurarlo tras un feedback temporal). */
    getTint(image: GameObjects.GameObject): number | undefined {
        return image.getData(TINT_DATA_KEY) as number | undefined
    }

    private shuffle<T>(items: T[]): T[] {
        for (let i = items.length - 1; i > 0; i--) {
            const j = Math.floor(Math.random() * (i + 1))
            ;[items[i], items[j]] = [items[j], items[i]]
        }
        return items
    }
}
