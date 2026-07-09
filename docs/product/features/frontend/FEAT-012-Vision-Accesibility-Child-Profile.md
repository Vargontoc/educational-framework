# FEAT-012 - Frontend: Child profile: Visual accessibility

## Status

state: proposal
user_history: Padre puede configurar accesibilidad visual del nino en caso de daltonismo
depends_on: FEAT-005-Parent-Control, FEAT-008-Child-Section
owned_by: frontend
scope: Implementacion de la configuracion de accesibilidad visual del nino en el perfil, usando el enum backend `colorVisionMode` y renderizando sus opciones en UI con lenguaje descriptivo.
test: Comprobar que se lee, muestra, actualiza y persiste `colorVisionMode` en el perfil del nino.

## Description

El scope de esta feature es implementar la opcion para que el padre configure la accesibilidad visual del nino en caso de que intuya que tenga daltonismo. NO es una aplicacion medica ni valorativa. Dentro del modal de modificacion de los valores del nino habra una nueva opcion toggle llamada accesibilidad visual.

- Si esta off o el padre pone off, se envia `colorVisionMode = NONE`.
- Al activar el toggle, mostrar un pequeno texto debajo: "Este ajuste no sustituye una valoracion oftalmologica".
- Al activar el toggle, renderizar las opciones soportadas por backend usando etiquetas descriptivas y el termino tecnico como apoyo secundario, no como etiqueta principal.
- "No estoy seguro" no es un valor backend. Debe ser una accion auxiliar que selecciona `DEUTERANOMALY`, por ser el ajuste inicial mas comun y reversible.

## Backend Contract

El backend ya expone el campo `colorVisionMode` en el perfil del nino.

- `CreateChildProfileRequest.colorVisionMode`: opcional; si no se envia, backend aplica `NONE`.
- `UpdateChildProfileRequest.colorVisionMode`: nullable; si se envia `null`, backend conserva el valor existente.
- `ChildProfileResponse.colorVisionMode`: requerido; frontend debe usarlo para renderizar el estado actual.

Valores soportados por backend:

- `NONE`
- `PROTANOPIA`
- `DEUTERANOMALY`
- `DEUTERANOPIA`
- `TRITANOPIA`
- `ACHROMATOPSIA`

## UI Options

El frontend debe renderizar los enumerados existentes en backend. Las etiquetas son de producto; el valor enviado al backend debe ser exactamente el enum.

| Backend value | Etiqueta principal | Subtitulo |
| --- | --- | --- |
| `NONE` | Sin ajuste visual | Valor por defecto |
| `PROTANOPIA` | Dificultad con rojo, tipo protan | Protanopia |
| `DEUTERANOMALY` | Dificultad con rojo y verde, intensidad habitual | Deuteranomalia |
| `DEUTERANOPIA` | Dificultad con rojo y verde, tipo deutan | Deuteranopia |
| `TRITANOPIA` | Dificultad con azul y amarillo | Tritanopia |
| `ACHROMATOPSIA` | No distingue colores | Acromatopsia |

Reglas de comportamiento:

- Toggle off: enviar `colorVisionMode = NONE`.
- Toggle on: mostrar y permitir seleccionar uno de los modos backend distintos de `NONE`.
- Al leer el perfil: renderizar el toggle y la seleccion desde `ChildProfileResponse.colorVisionMode`.
- Al actualizar el perfil: enviar el valor exacto de `colorVisionMode` seleccionado.
- Accion "No estoy seguro": seleccionar `DEUTERANOMALY` y mostrar un texto de ayuda indicando que es un ajuste comun, reversible y no diagnostico.

## Design Principles

- Al pulsar en una de las opciones mostrar una vista simple con un set de muestras de color que cambie en tiempo real segun la opcion seleccionada para que el padre observe el efecto del ajuste sin necesidad de entender la terminologia.

- No usar unicamente color para diferenciar las propias opciones del selector, acompanar cada opcion con un icono o patron distinto.

- Cuando se renderice contenido de categoria COLOR, usar la metadata accesible del catalogo backend: paleta por `colorVisionMode` y diferenciador no cromatico (`shapeIcon`, `labelKey`, `symbol` o equivalente disponible).

- NO incluir ningun tipo de test clinico ni referencias de diagnostico. No es una aplicacion de diagnostico.

## Backend Seed Reference Values

Estos valores reflejan el seed backend actual y sirven como referencia funcional. Frontend no debe hardcodearlos si puede obtenerlos del catalogo backend.

`Accessible color palette`

| conceptualIdentity | colorVisionMode | accessibleColorValue | accessibleLabelKey |
| --- | --- | --- | --- |
| `RED` | `NONE` | `#FF0000` | `color.red` |
| `RED` | `PROTANOPIA` | `#808000` | `color.red.protan` |
| `RED` | `DEUTERANOMALY` | `#FF8000` | `color.red.deutan` |
| `RED` | `DEUTERANOPIA` | `#808000` | `color.red.deutan` |
| `RED` | `TRITANOPIA` | `#FF0080` | `color.red.tritan` |
| `RED` | `ACHROMATOPSIA` | `GRAY` | `color.gray` |
| `BLUE` | `NONE` | `#0000FF` | `color.blue` |
| `BLUE` | `PROTANOPIA` | `#008080` | `color.blue.protan` |
| `BLUE` | `DEUTERANOMALY` | `#0080FF` | `color.blue.deutan` |
| `BLUE` | `DEUTERANOPIA` | `#008080` | `color.blue.deutan` |
| `BLUE` | `TRITANOPIA` | `#FF8000` | `color.blue.tritan` |
| `BLUE` | `ACHROMATOPSIA` | `GRAY` | `color.gray` |
| `GREEN` | `NONE` | `#00FF00` | `color.green` |
| `GREEN` | `PROTANOPIA` | `#00FF80` | `color.green.protan` |
| `GREEN` | `DEUTERANOMALY` | `#80FF00` | `color.green.deutan` |
| `GREEN` | `DEUTERANOPIA` | `#808000` | `color.green.deutan` |
| `GREEN` | `TRITANOPIA` | `#00FF80` | `color.green.tritan` |
| `GREEN` | `ACHROMATOPSIA` | `GRAY` | `color.gray` |
| `YELLOW` | `NONE` | `#FFFF00` | `color.yellow` |
| `YELLOW` | `PROTANOPIA` | `#FFFF00` | `color.yellow.protan` |
| `YELLOW` | `DEUTERANOMALY` | `#FFFF80` | `color.yellow.deutan` |
| `YELLOW` | `DEUTERANOPIA` | `#FFFF00` | `color.yellow.deutan` |
| `YELLOW` | `TRITANOPIA` | `#00FFFF` | `color.yellow.tritan` |
| `YELLOW` | `ACHROMATOPSIA` | `GRAY` | `color.gray` |

`Accessible color`

| conceptualIdentity | labelKey | shapeIcon |
| --- | --- | --- |
| `RED` | `color.red` | `circle` |
| `BLUE` | `color.blue` | `square` |
| `GREEN` | `color.green` | `triangle` |
| `YELLOW` | `color.yellow` | `star` |
