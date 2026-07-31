# ADR-022 — Gestión parental de perfiles infantiles

## Estado

- **Estado:** aceptada
- **Fecha:** 2026-07-31
- **Supersede:** —

## 1. Contexto y problema

El producto ya permite registrar perfiles infantiles y acceder al panel parental mediante PIN. La familia necesita administrar los perfiles ya creados desde la sección **Niños**: consultar los perfiles, ajustar su configuración individual, terminar una sesión activa, impedir temporalmente el acceso al juego y eliminar un perfil cuando ya no deba conservarse.

También se confirma un ajuste de accesibilidad visual para los minijuegos relacionados con colores. Ese ajuste debe ayudar a la familia a adaptar la experiencia sin representar una prueba, diagnóstico ni recomendación médica.

## 2. Necesidad de la familia y usuarios afectados

- **Adultos autenticados:** necesitan controles claros, deliberados y reversibles para administrar perfiles infantiles y adaptar una experiencia individual.
- **Niños de 3-4 años:** se benefician indirectamente de una experiencia jugable acorde con su edad y de apoyos visuales para los minijuegos de color. No utilizan ni ven esta sección parental.

La necesidad se mantiene dentro del alcance monofamiliar: no habilita comunicación, comparación ni compartición entre perfiles.

## 3. Alternativas de producto consideradas y compromisos

### A. Desactivar el perfil sin borrar sus datos

**Valor:** permite impedir temporalmente el acceso al juego y conservar configuraciones y progreso para un desbloqueo posterior.

**Compromiso:** el adulto debe distinguir con claridad entre bloquear y eliminar.

### B. Eliminar el perfil al impedir el acceso

**Valor:** reduce el número de estados que debe comprender el adulto.

**Inconveniente:** borra información de forma irreversible para una necesidad que puede ser temporal.

### C. Presentar los ajustes de color como diagnóstico o prueba

**Valor aparente:** podría parecer más orientativo para la familia.

**Inconveniente:** es incompatible con el carácter no clínico y no evaluativo de My Friend Nubi y puede inducir interpretaciones sanitarias indebidas.

## 4. Decisión confirmada y justificación

Se confirma la alternativa A para el bloqueo: **Bloquear** cambia únicamente el estado de acceso del perfil al juego; conserva toda su información y progreso para permitir un desbloqueo posterior. Si el niño estuviera jugando, la expulsión derivada de ese bloqueo queda fuera del alcance de esta decisión.

La sección **Niños** del panel parental muestra los perfiles en cuadrícula, incorpora la acción **«Registrar niño»** bajo esa cuadrícula y permite abrir una edición individual desde cada tarjeta. «Registrar niño» reutiliza el mismo stepper de creación confirmado para el acceso inicial. La gestión se reserva al adulto con acceso parental válido. La expulsión solo se ofrece cuando existe una sesión activa y requiere confirmación previa. La eliminación también requiere confirmación y elimina el perfil y toda la información relacionada.

La edición individual incluye ajustes de nombre, fecha de nacimiento, avatar, audio del NPC y su valor porcentual. Los ajustes individuales de audio/NPC se muestran, pero no pueden modificarse cuando los controles globales correspondientes de la familia están deshabilitados; deben comunicar ese motivo de forma explícita.

Se confirma un ajuste visual individual y opcional para los minijuegos de color. Incluye el estado predeterminado sin ajuste y los perfiles: **DEUTERENOPIA**, **DEUTERANOMALY**, **PROTANOPIA**, **PROTANOMALY**, **TRITANOPIA**, **TRITANOMALY**, **ACHROMATOMALY** y **ACHROMATOPSIA**. La familia lo selecciona manualmente. El ajuste se considera activo cuando el perfil posee un valor de configuración visual y permanece inactivo cuando no existe uno. Al desactivarlo y guardar los cambios, se elimina la configuración visual individual del perfil y este vuelve al estado predeterminado sin ajuste. Los ejemplos se limitan a elementos visuales simples, como cuadrados o círculos, y no muestran pruebas, resultados ni lenguaje diagnóstico. Se informa al adulto de que, ante dudas, debe consultar a un especialista.

## 5. Impacto

### Experiencia infantil

- El niño no accede a los controles ni a datos del panel parental.
- El bloqueo impide entrar al juego, sin convertirlo en castigo, comparación ni mensaje sobre la capacidad del niño.
- El ajuste visual busca hacer más comprensibles los minijuegos de color, sin depender de una interpretación clínica.
- La fecha de nacimiento se usa únicamente para adecuar la experiencia jugable a la edad.

### Experiencia parental

- La cuadrícula y la navegación jerárquica permiten localizar y gestionar un perfil de forma predecible.
- El adulto dispone de un acceso directo para registrar un nuevo perfil sin tener que salir del área parental, manteniendo el mismo proceso de alta ya conocido.
- Expulsar, bloquear y eliminar tienen propósitos distintos y las acciones irreversibles o que afectan a una sesión requieren confirmación.
- El dashboard individual se reconoce como destino independiente y actualmente puede mostrarse como placeholder.

### Accesibilidad

- El ajuste de colores se explica en lenguaje no médico, con ejemplos simples y aviso de consulta a especialista.
- Los estados y controles parentales deben ser comprensibles sin depender exclusivamente del color y utilizables en móvil y tableta.
- Los minijuegos de color requieren apoyos visuales adicionales al color; el perfil visual no es la única vía de accesibilidad.

### Seguridad infantil y privacidad

- Nombre y fecha de nacimiento siguen siendo datos personales del menor y se mantienen bajo control parental.
- El bloqueo conserva los datos para el desbloqueo; la eliminación confirmada elimina el perfil y toda la información asociada.
- No se incluyen publicidad, perfilado, reutilización ni compartición de datos infantiles.
- El ajuste visual no debe registrar, comunicar ni inferir una condición médica.

## 6. Límites, exclusiones y preguntas abiertas para los responsables técnicos

### Límites y exclusiones

- La entrega del evento al niño cuando sea expulsado como consecuencia de un bloqueo durante el juego.
- Un dashboard funcional, métricas de progreso, evaluaciones o clasificaciones: el destino Dashboard se limita por ahora a un placeholder.
- Diagnóstico, cribado, recomendación clínica o recogida de información médica o visual del niño.
- Fotografías, avatares personalizados, carga de imágenes y cambios en el catálogo de avatares predefinidos.
- Diseño técnico de sesiones, estados, almacenamiento, navegación, controles o eliminación de información.

### Preguntas abiertas para los responsables técnicos

- **Frontend y contenido:** validar que los textos, los ejemplos de color y el aviso a especialista se comprendan como orientación no médica y que los controles sean accesibles en móvil y tableta.
- **Backend, datos y seguridad/privacidad:** validar que solo la familia autorizada pueda consultar o modificar un perfil, que la eliminación alcance toda la información asociada y que la fecha de nacimiento solo se destine a la adecuación jugable acordada.
- **Backend y frontend:** validar la disponibilidad real de la sesión activa para condicionar la visibilidad de «Expulsar», y el efecto observable de los estados bloqueado y desbloqueado sin alterar la decisión de producto.
- **Datos, frontend y contenido:** validar que el ajuste visual se trate exclusivamente como una preferencia de juego, sin inferencias ni terminología diagnóstica fuera de los nombres confirmados de perfiles.
- El «tiempo de sesión» de cada tarjeta representa exclusivamente la duración transcurrida de la sesión actual mientras el niño está jugando; no es progreso, rendimiento ni una medida de capacidad.

## Referencias

- README.md
- ADR-020 — Estructura adaptable del panel parental.
- ADR-021 — Configuración global de audio, NPC y PIN.
- FEAT-003 — Selección y alta de perfiles infantiles.
- FEAT-005 — Configuración global de audio, NPC y PIN.
