# Feat-002 - Home view

## Status

state: proposal
user_history: Pantalla principal de la aplicación
depends_on: FEAT-001-Base-Styles
owned_by: frontend
scope: frontend view + API integration using `docs/contracts/api/openapi.json`. No backend implementation is included in this feature.
test: Validar estados sin familia, con familia, PIN correcto/incorrecto, creación de familia, listado de hijos, creación de hijo, orientación vertical y navegación a juego.

## Description

El objetivo de esta feature es desarrolla e implementar la vista principal de la aplicación.
Esta vista está conformada por:

- El avatar del agente que es la imagen `framework\frontend\app\src\assets\images\avatar-bot.png`
- Superpuesto al avatar hay un boton con el titulo `Bienvenida familia`
  - Al pulsar el botón si no hay una familia registrada en backend abrirá un modal para registrar el nombre de la familia y el PIN configurable.
  - El formulario de registro de familia deberá enviar los campos requeridos por contrato: `name`, `pin`, `ttsEnabled` y `agentEnabled`.
  - `ttsEnabled` y `agentEnabled` deberán tener valores por defecto definidos por producto si no se muestran como controles editables en el modal.
- En la esquina superior derecha hay dos botones:
  - Un icono de interrogación que al pulsar redirigirá a la documentación de la aplicación.
    - El destino de documentación deberá definirse antes de implementar: ruta interna, URL externa configurable o vista estática servida por el frontend.
  - Un icono `Settings` que es el panel de control parental. Este icono aparece si hay una familia registrada. Al pulsar en él aparecerá un modal que solicitará el PIN de la familia para acceder al panel de control parental.
    - El modal de PIN deberá usar `POST /api/v1/auth/login` del contrato API.
    - El token devuelto se guardará únicamente en memoria mediante `useSessionStore`, sin `localStorage`, `sessionStorage` ni persistencia de Pinia.
- Si hay una familia registada, debajo del botón `Bienvenida familia` aparecerá otro botón con el nombre de la familia.
  - Al pulsar en el bottón aparecerá los avatares  de los hijos. Si pulsa en uno de ellos le redirigirá a la vista de juego.
    - Antes de navegar a la vista de juego se deberá abrir sesión infantil usando `POST /api/v1/sessions/children` cuando el contrato y el flujo de autenticación lo permitan.
  - Para una mejor experiencia de usuario en ese modal que aparecen los avatares también tendrá la posibilidad de añadir un nuevo hijo. Al pulsar en añadir hijo aparecerá un modal para registrar el nombre del hijo y su avatar.
    - El formulario de alta de hijo deberá cumplir el contrato `CreateChildProfileRequest`: `name`, `birthday`, `ttsEnabled`, `agentEnabled` y opcionalmente `avatar`.
    - Si producto no quiere mostrar `birthday`, `ttsEnabled` o `agentEnabled` en el modal, deberá definir valores por defecto antes de implementar.

La vista tiene que mantener el estilo del resto de la aplicación, solo se muestra con el dispositivo en horizontal (Landscape) independientemente de si es un móvil o una tablet.

## Acceptance Criteria

- Al cargar la vista se consulta `GET /api/v1/family` para determinar el estado inicial.
- Si no existe familia, se muestra el CTA `Bienvenida familia` y al pulsarlo se abre el modal de registro de familia.
- Si existe familia, se muestra el nombre de la familia y el icono de `Settings`.
- Si el PIN parental es correcto, se permite acceder al panel de control parental usando el token en memoria.
- Si el PIN parental es incorrecto, se muestra un error visible y no se navega al panel parental.
- Al pulsar el nombre de la familia se abre el selector de hijos.
- Al seleccionar un hijo se abre la sesión infantil requerida y se navega a la vista de juego.
- El selector de hijos permite iniciar el alta de un nuevo hijo con todos los campos requeridos por contrato o valores por defecto definidos.
- En orientación vertical se muestra un overlay que solicita rotar el dispositivo; no se debe depender de la Screen Orientation API como única solución.
- Todos los textos visibles se obtienen desde Vue i18n.
- Los botones de icono tienen etiquetas accesibles mediante `aria-label` traducido.
- Los modales gestionan foco inicial, focus trap, cierre con `ESC` y retorno del foco al elemento que los abrió.

## Technical Notes

- Se utilizará [Vue Router](https://router.vuejs.org/) para la navegación entre vistas.
- Se utilizará [Vue i18n](https://vue-i18n.js.org/) para la internacionalización.
- Se utilizará [Pinia](https://pinia.vuejs.org/) para el manejo del estado (corregido según el stack definido en el framework frontend).
- Todas las llamadas API deberán usar el cliente compartido Axios definido para frontend.
- Las formas de los datos deberán derivarse de `docs/contracts/api/openapi.json`; no se deben inventar modelos locales ni usar mocks para estos flujos.
- El estado de la vista deberá contemplar explícitamente: `loading`, `noFamily`, `familyReady` y `error`.
- La feature no debe implementarse dentro de un sprint que prohíba modificar `HomeView` o añadir llamadas Axios.

## Análisis: Mejoras, Riesgos y Mitigaciones

### Capa Frontend

- **Riesgo:** El scope original indicaba `frontend-only`, pero la vista necesita consultar familia, crear familia, validar PIN, listar/crear hijos y abrir sesión infantil.
  - **Mitigación:** Tratar FEAT-002 como integración frontend con contrato API, o dividirla en una primera fase de UI shell y una segunda fase de integración.
- **Riesgo:** Inconsistencia de herramientas de estado. En las notas originales se mencionaba *Vuex*, pero el stack estándar del proyecto (`framework/frontend/agent.md`) define el uso de **Pinia**.
  - **Mitigación:** Corregido en las notas técnicas de esta feature para utilizar *Pinia*.
- **Riesgo:** Dependencia de estados de backend (familia, hijos, validación de PIN) para probar los diferentes flujos de la UI.
  - **Mitigación:** **NO utilizar mocks**. Se debe usar el contrato definido en `docs/contracts/api/openapi.json` para las llamadas y las interfaces. Si durante el desarrollo se detecta que un endpoint necesario no existe en el contrato, se deberá avisar inmediatamente al equipo de backend y bloquear esa parte del desarrollo hasta que esté disponible.
- **Riesgo:** Formularios incompletos respecto al contrato API. La creación de familia requiere `ttsEnabled` y `agentEnabled`; la creación de hijo requiere `birthday`, `ttsEnabled` y `agentEnabled` además de los campos visibles descritos.
  - **Mitigación:** Producto debe decidir si estos campos se muestran o si se envían con valores por defecto documentados antes de implementar.
- **Riesgo:** Estado inicial ambiguo de la pantalla mientras se consulta si existe familia.
  - **Mitigación:** Definir estados explícitos de UI: `loading`, `noFamily`, `familyReady` y `error`.
- **Riesgo:** Bloqueo de la orientación en "Landscape". La *Screen Orientation API* no es 100% fiable en todos los navegadores móviles (como Safari en iOS).
  - **Mitigación:** Implementar una directiva o componente *Overlay* mediante CSS (`@media (orientation: portrait)`) que cubra la pantalla y pida al usuario "Por favor, rota tu dispositivo" cuando esté en vertical.
- **Mejora:** Internacionalización (i18n). Hay textos literales ("Bienvenida familia", "Settings") mencionados en la descripción.
  - **Mitigación:** Extraer todos los textos literales a archivos de idioma (ej. `es.json`, `en.json`) desde el primer momento, usando las claves de traducción de Vue i18n.
- **Riesgo:** Implementar la feature durante un sprint que explícitamente excluye cambios en `HomeView` o llamadas Axios.
  - **Mitigación:** Abrir FEAT-002 como sprint posterior o actualizar formalmente el sprint frontend antes de iniciar implementación.

### Capa Backend

- **Riesgo:** Ausencia de endpoints en el contrato `openapi.json` necesarios para los modales de familia, hijos o la validación del PIN.
  - **Mitigación:** Validar los contratos en `docs/contracts/api/openapi.json` de forma rigurosa. Si el contrato no cubre un caso de uso requerido por el frontend, reportarlo inmediatamente a backend en lugar de inventar estructuras de datos o usar datos mockeados en el frontend.
- **Riesgo:** La validación de PIN para acceder al panel parental puede confundirse con una validación local.
  - **Mitigación:** Usar `POST /api/v1/auth/login` como único mecanismo de validación de PIN y apertura de sesión parental.
- **Riesgo:** La navegación al juego puede omitirse sin crear sesión infantil.
  - **Mitigación:** Confirmar que la selección de un hijo llama a `POST /api/v1/sessions/children` antes de navegar a la vista de juego.
- **Riesgo:** Inconsistencia de seguridad entre endpoints de familia/hijos y endpoints de sesión infantil.
  - **Mitigación:** Revisar con backend si los endpoints de familia e hijos son públicos por diseño inicial o si deben protegerse tras autenticación parental.

### Capa Seguridad / Sesión

- **Riesgo:** Persistir el token parental en almacenamiento local por comodidad.
  - **Mitigación:** Mantener el token únicamente en memoria, dentro de `useSessionStore`, según la estrategia de autenticación frontend.
- **Riesgo:** Al refrescar la página se pierde el token y el usuario deberá introducir el PIN de nuevo.
  - **Mitigación:** Documentar este comportamiento como esperado y reflejarlo en pruebas de aceptación.

### Capa UI / UX

- **Riesgo:** Accesibilidad y manejo de Modales. Múltiples flujos abren modales (Registro de familia, Settings, Selección/Añadir hijo).
  - **Mitigación:** Asegurar que los modales tengan un correcto manejo del foco (Focus Trap), cierren al pulsar fuera o usar la tecla `ESC`, y cuenten con atributos ARIA adecuados para lectores de pantalla.
- **Riesgo:** Demasiados modales encadenados pueden dejar estados residuales o confundir al usuario.
  - **Mitigación:** Definir que solo puede haber un modal activo a la vez y resetear el estado local al cerrar cada flujo.
- **Riesgo:** Los iconos de interrogación y `Settings` pueden no ser accesibles si solo muestran un icono visual.
  - **Mitigación:** Añadir `aria-label` traducido y estados focus visibles para todos los botones de icono.
- **Riesgo:** El destino de documentación no está definido.
  - **Mitigación:** Producto debe definir la ruta o URL final antes de implementar el botón de interrogación.
- **Mejora:** Retroalimentación visual. Al introducir un PIN incorrecto o faltar validaciones en el registro del hijo/familia, el usuario necesita saber qué ocurrió.
  - **Mitigación:** Incluir en el diseño de los modales el manejo de estados de error en los formularios (mensajes en rojo, inputs inválidos).

### Capa Testing

- **Riesgo:** La prueba original es demasiado genérica y no cubre los flujos críticos.
  - **Mitigación:** Cubrir como mínimo los escenarios definidos en Acceptance Criteria: sin familia, con familia, PIN correcto, PIN incorrecto, creación de familia, listado de hijos, creación de hijo, orientación vertical y navegación a juego.
