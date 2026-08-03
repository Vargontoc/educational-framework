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

export const COLOR_VISION_DESCRIPTIONS: Record<ColorVisionMode, string> = {
  [ColorVisionMode.NONE]: 'Sin ajuste de visualización.',
  [ColorVisionMode.PROTANOPIA]: 'Algunos rojos y verdes pueden confundirse.',
  [ColorVisionMode.PROTANOMALY]: 'Algunos rojos y verdes pueden parecerse.',
  [ColorVisionMode.DEUTERANOPIA]: 'Rojo y verde pueden verse muy parecidos.',
  [ColorVisionMode.DEUTERANOMALY]: 'Rojo y verde pueden costar más de distinguir.',
  [ColorVisionMode.TRITANOPIA]: 'Azul y amarillo pueden verse parecidos.',
  [ColorVisionMode.TRITANOMALY]: 'Azul y amarillo pueden costar más de distinguir.',
  [ColorVisionMode.ACHROMATOMALY]: 'Los colores pueden verse menos intensos o apagados.',
  [ColorVisionMode.ACHROMATOPSIA]: 'Los colores pueden verse en tonos grises.'
}
