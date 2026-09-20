import type { Scene } from "phaser"

/** El item ocupa este porcentaje del splash (sprint: 50-60 %) para que el splash siga visible alrededor. */
export const ITEM_SIZE_RATIO = 0.55

export const SPLASH_CHILD = 'splash'
export const ITEM_CHILD = 'item'

/**
 * Opcion/estimulo de COLOR: el splash (fondo de color) con el item superpuesto y centrado.
 * Es un Container para que el toque, las animaciones y el reposicionado traten a ambos como uno.
 */
export function createSplashCompound(scene: Scene, splashKey: string, itemKey?: string | null): Phaser.GameObjects.Container {
    const container = scene.add.container(0, 0)

    const splash = scene.add.image(0, 0, splashKey)
    splash.setName(SPLASH_CHILD)
    container.add(splash)

    if (itemKey && scene.textures.exists(itemKey)) {
        const item = scene.add.image(0, 0, itemKey)
        item.setName(ITEM_CHILD)
        container.add(item)
    }

    return container
}

/** Ajusta el splash a `size` y el item al `ITEM_SIZE_RATIO` del splash, ambos centrados. */
export function layoutSplashCompound(container: Phaser.GameObjects.Container, size: number): void {
    container.setSize(size, size)
    container.input?.hitArea?.setSize(size, size)

    const splash = container.getByName(SPLASH_CHILD) as Phaser.GameObjects.Image | null
    if (splash) {
        splash.setPosition(0, 0)
        splash.setScale(size / Math.max(splash.width, splash.height))
    }

    const item = container.getByName(ITEM_CHILD) as Phaser.GameObjects.Image | null
    if (item) {
        item.setPosition(0, 0)
        item.setScale((size * ITEM_SIZE_RATIO) / Math.max(item.width, item.height))
    }
}
