# FEAT-003 — Selección y alta de perfiles infantiles

## Estado

- **Estado:** aceptada
- **Responsable principal:** frontend
- **Decisión confirmada:** 2026-07-28
- **Historia de usuario:** Como familia con una familia ya registrada, quiero seleccionar de forma sencilla un perfil infantil para iniciar su experiencia y, como adulto, poder registrar un nuevo perfil bajo control parental.
- **Depende de:** FEAT-002 — Pantalla principal y accesos iniciales; PIN familiar vigente.

## 1. Objetivo y valor para la familia

Permitir que la familia identifique y elija con facilidad el perfil infantil que va a usar la experiencia de juego. El alta de un nuevo perfil debe permanecer bajo control parental y pedir únicamente los datos necesarios para configurar ese perfil.

## 2. Hechos, supuestos y decisión confirmada

### Hechos observados

- My Friend Nubi es una aplicación para una única familia, pero puede contener varios perfiles infantiles.
- El PIN familiar protege las acciones parentales.
- El perfil infantil contempla nombre, fecha de nacimiento y avatar.
- El catálogo de producto dispone de seis avatares infantiles predefinidos.

### Decisiones confirmadas

- Con familia registrada, el modal de selección se titula exactamente **«Familia <nombre de familia>»**.
- Los perfiles muestran avatar y nombre debajo, en una cuadrícula adaptable al tamaño del modal.
- Seleccionar un perfil existente continúa hacia su experiencia de juego; dicha experiencia no forma parte de esta funcionalidad.
- La acción **«Registrar niño»**, situada bajo la cuadrícula, requiere una verificación parental correcta mediante PIN antes de iniciar el alta.
- El alta se organiza en dos pasos: primero nombre; después fecha de nacimiento y elección opcional de avatar.
- Nombre y fecha de nacimiento son obligatorios. El avatar es opcional y el primer avatar predefinido está seleccionado inicialmente.
- No se admiten imágenes propias ni existe un límite de perfiles infantiles por familia.
- La edición y eliminación de perfiles permanecen en el panel parental.

### Supuestos explícitos

- El nombre de cada niño es libre; el producto no define límites de longitud ni reglas de contenido adicionales.

## 3. Actores y escenarios de uso

### Niño o adulto que selecciona un perfil

1. Desde la bienvenida de Home, abre el modal de selección de perfiles.
2. Ve el título familiar y los avatares con sus nombres.
3. Pulsa el perfil que desea usar.
4. Continúa a la experiencia de juego de ese perfil.

### Adulto que registra un perfil infantil

1. Desde el mismo modal, elige «Registrar niño».
2. Completa la verificación parental mediante PIN.
3. Introduce el nombre en el primer paso.
4. Introduce la fecha de nacimiento en el segundo paso y conserva el avatar preseleccionado o elige otro del catálogo.
5. Confirma el alta y ve el nuevo perfil en la cuadrícula.

## 4. Requisitos funcionales y no funcionales de producto

1. El modal solo está disponible cuando existe una familia registrada.
2. Debe mostrar el título «Familia <nombre de familia>».
3. Cada perfil infantil visible debe mostrar únicamente avatar y nombre debajo. No debe mostrar fecha de nacimiento, progreso ni información parental.
4. Los perfiles deben organizarse en una cuadrícula que se adapte al espacio disponible en móvil y tableta.
5. Al pulsar un perfil existente se continúa hacia la experiencia de juego correspondiente.
6. Debe existir la acción «Registrar niño» bajo la cuadrícula.
7. No se puede iniciar el alta de un niño sin una verificación parental correcta mediante PIN.
8. El stepper de alta tiene dos pasos: nombre; y fecha de nacimiento con elección opcional de avatar.
9. No puede confirmarse el alta sin nombre y fecha de nacimiento.
10. El primer avatar predefinido se presenta seleccionado inicialmente. El adulto puede elegir cualquiera de los otros avatares predefinidos o conservarlo.
11. No se permiten fotografías, avatares personalizados ni carga de imágenes.
12. Tras confirmar el alta, el nuevo perfil aparece en la cuadrícula con avatar y nombre.
13. No se establece un límite de perfiles infantiles por familia.
14. El modal no debe incluir edición ni eliminación de perfiles.
15. Los controles deben ser táctiles, claros y utilizables en tableta y móvil. La identificación de los perfiles no puede depender exclusivamente de color ni de la lectura del nombre.

## 5. Criterios de aceptación verificables

1. Con familia registrada, el modal muestra «Familia <nombre de familia>».
2. Cada perfil existente muestra avatar y nombre debajo, sin fecha de nacimiento ni progreso.
3. Los perfiles se presentan en cuadrícula adaptable en móvil y tableta.
4. Al seleccionar un perfil existente, la aplicación continúa hacia su experiencia de juego.
5. «Registrar niño» aparece bajo la cuadrícula y no inicia la recogida de datos infantiles sin verificación parental correcta mediante PIN.
6. Tras la verificación, el alta muestra primero el nombre y después la fecha de nacimiento junto con la selección opcional de avatar.
7. No se puede confirmar el alta si falta nombre o fecha de nacimiento.
8. El primer avatar está seleccionado inicialmente y solo se pueden elegir avatares del catálogo predefinido.
9. No se ofrece ninguna opción para subir imágenes propias.
10. Tras confirmar el alta, el nuevo perfil aparece con avatar y nombre.
11. La familia puede registrar perfiles adicionales sin que el producto imponga un límite.
12. No existen acciones de editar o eliminar perfiles en este modal.

## 6. Ámbitos que deben validar los responsables y dependencias conocidas

### Frontend

- Presentación accesible de la cuadrícula y del stepper en móvil y tableta.
- Claridad de la verificación parental previa al alta, de los estados de error y de la cancelación.
- Identificación visual de avatares sin depender exclusivamente de color.

### Backend y datos

- Disponibilidad exclusiva de los perfiles de la familia registrada.
- Alta de perfiles sin límite funcional y uso de nombre, fecha de nacimiento y avatar conforme a esta especificación.

### Seguridad y privacidad

- Verificación parental antes de iniciar el alta.
- Aislamiento de los datos de cada niño dentro de la familia y tratamiento proporcionado de nombre y fecha de nacimiento.

### Dependencias de producto conocidas

- FEAT-002 — Pantalla principal y accesos iniciales.
- PIN familiar y acceso parental vigente.
- Catálogo de seis avatares infantiles predefinidos proporcionado por el producto.

## 7. Privacidad, seguridad infantil, accesibilidad y límites de IA aplicables

### Privacidad y seguridad infantil

- El alta recoge exclusivamente nombre, fecha de nacimiento y avatar predefinido para configurar el perfil infantil.
- No se solicitan fotografías, datos de contacto, texto libre adicional ni información de progreso.
- La fecha de nacimiento no se muestra en la selección de perfiles.
- La acción de alta está protegida por PIN parental. No hay publicidad, perfilado, compartición ni reutilización de datos infantiles.

### Accesibilidad y experiencia infantil

- Avatar y nombre apoyan la identificación de cada perfil; el avatar evita que la selección dependa solo de la lectura.
- La cuadrícula y los controles deben tener objetivos táctiles amplios y ser comprensibles en pantallas pequeñas.
- No se incluyen temporizadores, presión, competición, comparativas ni mensajes que valoren la capacidad del niño.

### Límites de IA

- Esta funcionalidad no activa IA ni recoge contenido generado.

## 8. Exclusiones, riesgos y decisiones pendientes

### Exclusiones

- La experiencia de juego posterior a seleccionar un perfil.
- Edición y eliminación de perfiles, que corresponden al panel parental.
- Avatares personalizados, fotografías y carga de imágenes propias.
- Progreso, comparativas, evaluación infantil o configuraciones del juego.

### Riesgos

- Nombre y fecha de nacimiento son datos personales de un menor. Mostrar solo avatar y nombre en la selección y exigir PIN para el alta reducen la exposición; privacidad y seguridad deben validar el riesgo residual.
- Un catálogo de avatares poco distinguible podría dificultar la selección para niños pequeños. Frontend y contenido deben validarlo sin depender solo del color.

### Decisiones pendientes

- El producto no añade validaciones al nombre infantil más allá de solicitarlo. Los responsables deben señalar si existe una limitación necesaria que afecte a la experiencia o a la protección de datos antes de aplicarla.
- Los responsables técnicos deben definir el tratamiento seguro del PIN, la disponibilidad de perfiles y los estados de error, sin alterar los comportamientos acordados.
