# Educational Framework App

## Description

Aplicación web educativa en esta versión (v. 1.0) para niños de 3 a 4 años. Tiene las siguientes características principales:

- Mono familiar
- Dificultad adaptativa
- Dashboard con la información del prograso del niño
- Minijuegos adaptados a la edad del niño
- NO es una aplicación profesional. El juego es un acompañamiento para el refuerzo nunca es evaluativo.
- Seguridad mínima, el panel parental es mediante un código PIN configurable por la familia.
- Chatbot para los padres donde el agente hará puede darles un resumen del niño o responder de forma genérica a preguntas de los padres. NUNCA actua como un profesional, siempre derivará a uno.
- Sección de lectura en familia donde tendra un catalogo cuentos generados, NUNCA con refranes o finales con valores morales para no contradecir los que tenga la familia. Son cuentos absepticos.
- Sección de relajación en familia donde se describen pautas para el niño que puedan hacer en familia.

### TTS Service

Capa independiente donde habita una pequeña API con funcionalidad minima para generar audio, esta capa se conecta a un contenedor que contiene Chatterbox, este contenedor contendra dos voces (npc-voice y narrative-voice). Se llamará al mismo endpoint con los parametros específicos para cada voz y distintas tonalidades.

- npc-voice: es la voz que sonará en las animaciones del npc del juego del niño
- narrative-voice: es la voz que se oira en la sección de lectura en familia.

### Agents

Capa independiente donde habita los agentes de dominio específicos del aplicativo. 

- npc-agent: Agente que tiene la personalidad del personaje de la aplicacion (Nubi, se prevee posibilñidad de adaptar el nombre a que sea configurable). Este npc no es un chatbot sino que va por eventos y hara respuestas cortas de refuerzo para el niño durante el juego.

- chatbot-agent (nombre provisional): agente específico para los padres, si actua como un chatbot dando información del nió que solicite el padre o respondiendo dudas pero de forma genérica NUNCA actuando como un profesional y siempre respondiendo con calma sin importar el tipo de pregunta.

### API

Capa backend donde habita la logica de negocio y el core del juego. Tiene distintas secciones / módulos:

- Family & Session: donde se configura la familia, se registra los hijos, se controla las sesiones Websocket.
- Agents: módulo no REST donde habita la lógica de comunicación con los agentes descritos anteriormente como con el TTS Service. La ausencia de conexion con estos elementos no rompe la aplicación pues el niño puede interactuar con el juego sin audio ni npc. 
- Tracking: módulo donde reside la lógica de la interacción del niño con el juego controlando los aciertos, fallos, tiempos sin conexión durante el minijuego, etc. Y aqui se implementa la lógica para crear los distintos dashboards para mostrar la información al padre.
- Game: Es el núcleo principal de la aplicación donde reside la lógica de los distintos minijuegos.
- Content: habita el contenido de la aplicación como actividades, temas, cuentos, animaciones, etc.

** Hay un procedimiento para los contenedores Docker y agilizar y es que cuando en desarrollo se genera nuevo contenido se actualiza el contenedor SIN borrar la información que tuviera.

### App

Capa frontend donde habita la lógica de la aplicación visual. Se plantea una aplicación web para tablets y moviles. Y reqiere que esté levantado la API, sino la app no se levanta.
Tiene distintas vistas:

- Home: Vista principal donde entrará el usuario y que puede registrar el nombre de la familia y los hijos. También desde esta vista accede al panel parental (PIN) como a la vista Game seleccionando al niño
- Parent Panel: vista donde el padre/madre podrá aplicar distintas configuraciones en ambito global como ambito individual del niño. Se accede mediante PIN. Esta vista tendra siguientes secciones principales
    - Panel: aqui varias subsecciones
        - Configuración: Configura el audio o npc a nivel global. Y también cambio de PIN entre otras opciones
        - Niños: Donde puede registrar o editar información del niño. En la edición del niño tambien tiene opciones de accesibilidad como el daltonismo para los minijuegos que aplique (ej. reconocer colores). Y tambien redirige al dashboard
        - Chatbot: Es donde habita el chat, en caso de que este activo el agente en el contendor (no es configurable). Sera un chat y tambien tendra comandos especificos para mejorar la experiencia.
        - Documentación: Donde reside la documentacion y pequeños tutoriales de la aplicación, como el envio de sugerencias o bugs. Aunque reside en el panel parental, se puede acceder sin seguridad.
    - Experiencias:
        - Lectura en familia: catalogo de cuentos ligeros auto narrativos (se puede deshabilitar)
        - Relajacion en familia: distintos ejercicos que puedan hacer en familia para relajarse con el niño
- Game: Vista principal de la aplicación donde el niño interactuará. El juego tiene que funcionar igualmente sin audio ni npc activo. Tiene dos "capas".
    - World map: Donde el npc caminara por un paisaje (sistema de biomas) en el que aparecerá distintos elementos interactivos para el niño, estos elementos pueden emitir un sonido o hacer una animación.
    - Minigames: Algunos de los elementos interactivos mostrarán un minijuego al niño donde debera interactual, el npc animará o dará pistas. NO hay pensalizaciones visuales ni auditivas, estas "penalizaciones" se registraran en el tracking para que lo vea el padre/madre



## Requisitos del sistema

- Instalado Docker
- Contendor Chatterbox levantado en el Docker
- Contenedor Ollama levantado en el Docker
- Gráfica NVIDIA RTX GeForce 4070 SUPER o superior

## Installation

### TTS Service

### Agents

### API

### App

## License

[MIT](https://choosealicense.com/licenses/mit/)