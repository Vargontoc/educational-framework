import type { DeviceProfile } from "./DeviceProfile"

/**
 * Tamaños del minijuego calculados a partir de lo que mide realmente el canvas en pantalla.
 *
 * El canvas lógico es fijo (p. ej. 1280x720) y Phaser lo escala por CSS, así que un
 * porcentaje del ancho lógico daría siempre el mismo resultado en cualquier dispositivo.
 * Por eso los porcentajes y los mínimos/máximos se expresan en píxeles CSS (los que
 * percibe el niño) y luego se convierten a unidades lógicas con `displayScale`.
 */

export interface LayoutInput {
    /** Tamaño lógico del juego (`scale.width` / `scale.height`). */
    viewportWidth: number
    viewportHeight: number
    /** Unidades lógicas por píxel CSS (`scale.displayScale.x`). */
    displayScale: number
}

export interface Point {
    x: number
    y: number
}

export interface OptionSlot extends Point {
    size: number
}

/** Opcion de comparacion: `size` es el lado dibujado (segun su escala) y `hitSize` el lado tactil. */
export interface ComparisonSlot extends OptionSlot {
    hitSize: number
}

/** Tablero de memoria: tamaño de cada carta y esquina superior izquierda de la rejilla (unidades logicas). */
export interface MemoryGrid {
    rows: number
    columns: number
    cardWidth: number
    cardHeight: number
    gap: number
    left: number
    top: number
}

export interface LayoutSizes {
    viewportWidth: number
    viewportHeight: number
    displayScale: number
    hitSize: number
    stimulusSize: number
    stimulusCenter: Point
    buttonSize: number
    buttonMargin: number
    nubiSize: number
    nubiMargin: number
    progressBarWidth: number
    progressBarHeight: number
    progressBarTop: number
    optionsY: number
    /** Tamaño mínimo de fuente (unidades lógicas) para que el texto sea legible. */
    minFontSize: number
}

interface Range {
    min: number
    max: number
}

const ABSOLUTE_MIN_TOUCH_CSS = 44
const HIT: Range = { min: ABSOLUTE_MIN_TOUCH_CSS, max: 150 }
const STIMULUS: Range = { min: 100, max: 200 }
const BUTTON: Range = { min: ABSOLUTE_MIN_TOUCH_CSS, max: 80 }
const NUBI: Range = { min: 80, max: 120 }
const PROGRESS_BAR_WIDTH: Range = { min: 120, max: 300 }
const PROGRESS_BAR_HEIGHT: Range = { min: 8, max: 14 }

const HIT_WIDTH_RATIO = 0.10
const STIMULUS_WIDTH_RATIO = 0.15
const BUTTON_WIDTH_RATIO = 0.05
const NUBI_WIDTH_RATIO = 0.10
const MARGIN_WIDTH_RATIO = 0.03
const PROGRESS_BAR_WIDTH_RATIO = 0.20
const PROGRESS_BAR_HEIGHT_RATIO = 0.02
const PROGRESS_BAR_TOP_RATIO = 0.03

const STIMULUS_ZONE_X = 0.5
const STIMULUS_ZONE_Y = 0.25
const OPTIONS_ZONE_Y = 0.65
const OPTION_FILL_OF_SLOT = 0.9
const ELEMENT_GAP_CSS = 8
/** Lado maximo (px CSS) de la opcion mas grande (100 %) de una ronda de comparacion. */
const COMPARISON_BASE_MAX_CSS = 240
const MIN_FONT_CSS = 14
/** Relacion ancho/alto de la carta (297x451). */
export const MEMORY_CARD_ASPECT = 297 / 451
/** Lado mayor maximo (px CSS) de una carta de memoria. */
const MEMORY_CARD_MAX_HEIGHT_CSS = 230
const MEMORY_GAP_CSS = 12
/** Hueco inferior (fraccion del alto) que deja el borde de madera del tablero. */
const MEMORY_BOTTOM_MARGIN_RATIO = 0.07

const clamp = (value: number, range: Range) => Math.min(Math.max(value, range.min), range.max)

export class ResponsiveLayout {
    private profile: DeviceProfile
    private sizes: LayoutSizes

    constructor(profile: DeviceProfile, initial: LayoutInput = { viewportWidth: 1280, viewportHeight: 720, displayScale: 1 }) {
        this.profile = profile
        this.sizes = this.calculateSizes(initial)
    }

    calculateSizes(input: LayoutInput): LayoutSizes {
        const { viewportWidth, viewportHeight } = input
        const scale = input.displayScale > 0 ? input.displayScale : 1
        const cssWidth = viewportWidth / scale
        const toLogical = (css: number) => css * scale

        const hitCss = clamp(cssWidth * HIT_WIDTH_RATIO, {
            min: Math.max(HIT.min, this.profile.minHitCss),
            max: HIT.max
        })
        const stimulusCss = clamp(cssWidth * STIMULUS_WIDTH_RATIO, STIMULUS)
        const buttonCss = clamp(cssWidth * BUTTON_WIDTH_RATIO, BUTTON)
        const nubiCss = clamp(cssWidth * NUBI_WIDTH_RATIO, NUBI)
        const marginCss = cssWidth * MARGIN_WIDTH_RATIO

        const hitSize = toLogical(hitCss)
        const stimulusSize = toLogical(stimulusCss)
        const nubiSize = toLogical(nubiCss)
        const nubiMargin = toLogical(marginCss)
        const gap = toLogical(ELEMENT_GAP_CSS)

        const progressBarTop = viewportHeight * PROGRESS_BAR_TOP_RATIO
        const stimulusCenter = {
            x: viewportWidth * STIMULUS_ZONE_X,
            y: viewportHeight * STIMULUS_ZONE_Y
        }

        // Las opciones bajan hasta su zona natural, pero nunca solapan al estimulo
        // (limite inferior) ni a Nubi (limite superior); si ambos chocan, gana el estimulo.
        const nubiTop = viewportHeight - nubiMargin - nubiSize
        const lowest = stimulusCenter.y + stimulusSize / 2 + gap + hitSize / 2
        const highest = nubiTop - gap - hitSize / 2
        const optionsY = Math.max(lowest, Math.min(viewportHeight * OPTIONS_ZONE_Y, highest))

        return this.sizes = {
            viewportWidth,
            viewportHeight,
            displayScale: scale,
            hitSize,
            stimulusSize,
            stimulusCenter,
            buttonSize: toLogical(buttonCss),
            buttonMargin: toLogical(marginCss),
            nubiSize,
            nubiMargin,
            progressBarWidth: toLogical(clamp(cssWidth * PROGRESS_BAR_WIDTH_RATIO, PROGRESS_BAR_WIDTH)),
            progressBarHeight: toLogical(clamp(cssWidth * PROGRESS_BAR_HEIGHT_RATIO, PROGRESS_BAR_HEIGHT)),
            progressBarTop,
            optionsY,
            minFontSize: toLogical(MIN_FONT_CSS)
        }
    }

    getSizes(): LayoutSizes {
        return this.sizes
    }

    getElementHitSize(): number {
        return this.sizes.hitSize
    }

    getStimulusCardSize(): number {
        return this.sizes.stimulusSize
    }

    getButtonSize(): number {
        return this.sizes.buttonSize
    }

    getNubiSize(): number {
        return this.sizes.nubiSize
    }

    /**
     * Opciones de una ronda de comparacion: el mismo objeto a `scales` (porcentajes) distintos.
     *
     * Van repartidas en la franja que deja libre Nubi (esquina inferior derecha) y bajo el estimulo,
     * asi la opcion mas grande nunca la tapa ni la solapa. El lado del 100 % se ajusta al hueco y el
     * resto se deriva de su porcentaje. El area tactil de cada opcion es al menos `hitSize` (44 px CSS
     * o mas segun el dispositivo) aunque la opcion se vea mas pequena, sin invadir a sus vecinas.
     */
    getComparisonSlots(scales: number[]): ComparisonSlot[] {
        const count = scales.length
        if (count <= 0) return []

        const s = this.sizes
        const gap = s.displayScale * ELEMENT_GAP_CSS
        const regionLeft = s.nubiMargin
        const regionRight = s.viewportWidth - s.nubiSize - s.nubiMargin * 2
        const spacing = (regionRight - regionLeft) / count

        const bandTop = s.stimulusCenter.y + s.stimulusSize / 2 + gap
        const bandBottom = s.viewportHeight - s.nubiMargin
        const base = Math.min(
            spacing * OPTION_FILL_OF_SLOT,
            bandBottom - bandTop,
            COMPARISON_BASE_MAX_CSS * s.displayScale
        )

        const centerY = Math.min(Math.max(s.optionsY, bandTop + base / 2), bandBottom - base / 2)
        const maxHit = spacing * OPTION_FILL_OF_SLOT

        return scales.map((scale, i) => {
            const size = base * scale / 100
            return {
                x: regionLeft + spacing * (i + 0.5),
                y: centerY,
                size,
                hitSize: Math.min(Math.max(size, s.hitSize), Math.max(maxHit, size))
            }
        })
    }

    /**
     * Rejilla del juego de memoria: las cartas (mismo aspecto que la imagen) se reparten centradas entre la
     * barra de progreso y el borde inferior, con el mismo margen a cada lado (el de Nubi) para que la
     * rejilla quede centrada y la esquina de Nubi libre. Ninguna carta baja de `hitSize` de ancho salvo que la
     * pantalla no de mas de si; en los dispositivos objetivo (movil apaisado) siempre pasa de 44 px CSS.
     */
    getMemoryGrid(rows: number, columns: number): MemoryGrid {
        const s = this.sizes
        const gap = s.displayScale * MEMORY_GAP_CSS
        const sideMargin = s.nubiSize + s.nubiMargin * 2
        const top = s.progressBarTop + s.progressBarHeight + gap * 2
        const bottom = s.viewportHeight * (1 - MEMORY_BOTTOM_MARGIN_RATIO)
        const regionWidth = s.viewportWidth - sideMargin * 2
        const regionHeight = bottom - top

        const safeRows = Math.max(rows, 1)
        const safeColumns = Math.max(columns, 1)
        const cardHeight = Math.min(
            (regionHeight - (safeRows - 1) * gap) / safeRows,
            (regionWidth - (safeColumns - 1) * gap) / safeColumns / MEMORY_CARD_ASPECT,
            MEMORY_CARD_MAX_HEIGHT_CSS * s.displayScale
        )
        const cardWidth = cardHeight * MEMORY_CARD_ASPECT
        const gridWidth = safeColumns * cardWidth + (safeColumns - 1) * gap
        const gridHeight = safeRows * cardHeight + (safeRows - 1) * gap

        return {
            rows,
            columns,
            cardWidth,
            cardHeight,
            gap,
            left: (s.viewportWidth - gridWidth) / 2,
            top: top + (regionHeight - gridHeight) / 2
        }
    }

    /** Centro de la carta de una rejilla de memoria. */
    static memoryCardCenter(grid: MemoryGrid, row: number, column: number): Point {
        return {
            x: grid.left + column * (grid.cardWidth + grid.gap) + grid.cardWidth / 2,
            y: grid.top + row * (grid.cardHeight + grid.gap) + grid.cardHeight / 2
        }
    }

    /** Posiciones y tamaño de cada opcion: repartidas a lo ancho, sin solaparse entre si. */
    getOptionSlots(count: number): OptionSlot[] {
        if (count <= 0) return []

        const { viewportWidth, hitSize, optionsY } = this.sizes
        const spacing = viewportWidth / (count + 1)
        const size = Math.min(hitSize, spacing * OPTION_FILL_OF_SLOT)

        return Array.from({ length: count }, (_, i) => ({ x: spacing * (i + 1), y: optionsY, size }))
    }
}
