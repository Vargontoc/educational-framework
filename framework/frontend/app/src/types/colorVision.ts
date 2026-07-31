export enum ColorVisionMode {
  NONE = 'NONE',
  PROTANOPIA = 'PROTANOPIA',
  PROTANOMALY = 'PROTANOMALY',
  DEUTERANOPIA = 'DEUTERANOPIA',
  DEUTERANOMALY = 'DEUTERANOMALY',
  TRITANOPIA = 'TRITANOPIA',
  TRITANOMALY = 'TRITANOMALY',
  ACHROMATOMALY = 'ACHROMATOMALY',
  ACHROMATOPSIA = 'ACHROMATOPSIA'
}

export const COLOR_VISION_LABELS: Record<ColorVisionMode, string> = {
  [ColorVisionMode.NONE]: 'Sin ajuste',
  [ColorVisionMode.PROTANOPIA]: 'Protanopia',
  [ColorVisionMode.PROTANOMALY]: 'Protanomalía',
  [ColorVisionMode.DEUTERANOPIA]: 'Deuteranopia',
  [ColorVisionMode.DEUTERANOMALY]: 'Deuteranomalía',
  [ColorVisionMode.TRITANOPIA]: 'Tritanopia',
  [ColorVisionMode.TRITANOMALY]: 'Tritanomalía',
  [ColorVisionMode.ACHROMATOMALY]: 'Acromatomalía',
  [ColorVisionMode.ACHROMATOPSIA]: 'Acromatopsia'
}
