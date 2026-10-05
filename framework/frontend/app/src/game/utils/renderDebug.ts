/**
 * Diagnostico de renderizado para dispositivos moviles (opt-in, no afecta a un uso normal).
 *
 * Sirve para investigar sprites que salen como un recuadro negro en algunos moviles sin necesidad de
 * depurar por USB. Se activa con parametros en la URL; el valor se guarda en localStorage para que siga
 * activo en la app instalada (donde no hay barra de direcciones):
 *
 *   ?glDebug=1         muestra una capa con datos de la GPU y del estado de las texturas
 *   ?renderer=canvas   fuerza el renderizador Canvas (descarta un fallo de WebGL)
 *   ?maxTextures=4     limita las texturas por lote de WebGL (descarta un limite de unidades de la GPU)
 *   ?glDebug=0         borra la configuracion guardada
 */

const STORAGE_KEY = 'nubi-render-debug'

export interface RenderDebugFlags {
    overlay: boolean
    renderer?: 'canvas' | 'webgl'
    maxTextures?: number
}

function readStored(): RenderDebugFlags | null {
    try {
        const raw = localStorage.getItem(STORAGE_KEY)
        return raw ? (JSON.parse(raw) as RenderDebugFlags) : null
    } catch {
        return null
    }
}

function writeStored(flags: RenderDebugFlags | null): void {
    try {
        if (flags) localStorage.setItem(STORAGE_KEY, JSON.stringify(flags))
        else localStorage.removeItem(STORAGE_KEY)
    } catch {
        // sin almacenamiento disponible: solo vale para esta carga
    }
}

export function readRenderDebugFlags(search: string = window.location.search): RenderDebugFlags {
    const params = new URLSearchParams(search)
    const hasParams = params.has('glDebug') || params.has('renderer') || params.has('maxTextures')

    if (hasParams) {
        if (params.get('glDebug') === '0') {
            writeStored(null)
            return { overlay: false }
        }
        const renderer = params.get('renderer')
        const maxTextures = Number(params.get('maxTextures'))
        const flags: RenderDebugFlags = {
            overlay: params.get('glDebug') === '1',
            renderer: renderer === 'canvas' || renderer === 'webgl' ? renderer : undefined,
            maxTextures: Number.isInteger(maxTextures) && maxTextures > 0 ? maxTextures : undefined
        }
        writeStored(flags)
        return flags
    }

    return readStored() ?? { overlay: false }
}

function glInfo(game: Phaser.Game): string[] {
    const renderer = game.renderer as Phaser.Renderer.WebGL.WebGLRenderer & { maxTextures?: number }
    const gl = (renderer as unknown as { gl?: WebGLRenderingContext }).gl
    if (game.config.renderType === Phaser.CANVAS || !gl) {
        return ['renderer: CANVAS (sin WebGL)']
    }
    const debugInfo = gl.getExtension('WEBGL_debug_renderer_info')
    return [
        'renderer: WEBGL',
        `GPU: ${debugInfo ? gl.getParameter(debugInfo.UNMASKED_RENDERER_WEBGL) : gl.getParameter(gl.RENDERER)}`,
        `MAX_TEXTURE_SIZE: ${gl.getParameter(gl.MAX_TEXTURE_SIZE)}`,
        `MAX_TEXTURE_IMAGE_UNITS: ${gl.getParameter(gl.MAX_TEXTURE_IMAGE_UNITS)}`,
        `maxTextures (Phaser): ${renderer.maxTextures ?? '?'}`,
        `contexto perdido: ${gl.isContextLost()}`
    ]
}

function textureInfo(game: Phaser.Game, key: string): string {
    if (!game.textures.exists(key)) return `${key}: NO CARGADA`
    const texture = game.textures.get(key)
    const source = texture.source[0]
    const frames = texture.getFrameNames(false).length || Object.keys(texture.frames).length - 1
    return `${key}: ${source.width}x${source.height}, ${frames} fotogramas, glTexture=${source.glTexture ? 'si' : 'NO'}`
}

export function installRenderDebugOverlay(game: Phaser.Game): () => void {
    const panel = document.createElement('pre')
    Object.assign(panel.style, {
        position: 'fixed',
        left: '4px',
        top: '4px',
        zIndex: '2147483647',
        margin: '0',
        padding: '6px 8px',
        maxWidth: '96vw',
        font: '11px/1.35 monospace',
        color: '#fff',
        background: 'rgba(0,0,0,0.78)',
        whiteSpace: 'pre-wrap',
        pointerEvents: 'none'
    })
    document.body.appendChild(panel)

    const render = () => {
        const lines = [
            `Phaser ${Phaser.VERSION}  DPR ${window.devicePixelRatio}`,
            `pantalla ${window.innerWidth}x${window.innerHeight}  canvas ${game.canvas.width}x${game.canvas.height}`,
            ...glInfo(game),
            textureInfo(game, 'nubi-greetings'),
            `flags: ${JSON.stringify(readRenderDebugFlags())}`
        ]
        panel.textContent = lines.join('\n')
    }
    render()
    const timer = window.setInterval(render, 1000)

    return () => {
        window.clearInterval(timer)
        panel.remove()
    }
}
