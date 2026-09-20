export type DeviceType = 'mobile' | 'tablet' | 'desktop'

export interface DeviceProfile {
    type: DeviceType
    /** Tamaño táctil mínimo en píxeles CSS para este tipo de dispositivo. */
    minHitCss: number
}

export const DEVICE_PROFILE_REGISTRY_KEY = 'deviceProfile'

const MOBILE_MAX_SHORT_SIDE = 600
const TABLET_MAX_SHORT_SIDE = 1100
const ABSOLUTE_MIN_TOUCH_CSS = 44

const PROFILES: Record<DeviceType, DeviceProfile> = {
    mobile: { type: 'mobile', minHitCss: 48 },
    tablet: { type: 'tablet', minHitCss: 56 },
    desktop: { type: 'desktop', minHitCss: ABSOLUTE_MIN_TOUCH_CSS }
}

export interface DeviceSignals {
    /** Lado corto de la pantalla en píxeles CSS. */
    shortSide: number
    /** El puntero principal es táctil (`pointer: coarse`). */
    coarsePointer: boolean
}

export function profileFor(signals: DeviceSignals): DeviceProfile {
    if (!signals.coarsePointer) return PROFILES.desktop
    if (signals.shortSide < MOBILE_MAX_SHORT_SIDE) return PROFILES.mobile
    if (signals.shortSide <= TABLET_MAX_SHORT_SIDE) return PROFILES.tablet
    return PROFILES.desktop
}

export function detectDeviceProfile(win: Window = window): DeviceProfile {
    return profileFor({
        shortSide: Math.min(win.screen.width, win.screen.height),
        coarsePointer: win.matchMedia('(pointer: coarse)').matches
    })
}
