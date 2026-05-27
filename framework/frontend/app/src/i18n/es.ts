export const messages = {
  common: {
    loading: 'Cargando...',
    error: 'Ha ocurrido un error'
  },
  home: {
    title: 'Inicio',
    loading: 'Cargando...',
    error: 'Ha ocurrido un error al cargar la familia',
    retry: 'Reintentar',
    welcomeFamily: 'Bienvenida familia',
    helpAriaLabel: 'Ayuda',
    settingsAriaLabel: 'Configuración'
  },
  panel: {
    title: 'Panel de Control'
  },
  game: {
    title: 'Juego'
  },
  rotation: {
    message: 'Por favor, gira el dispositivo a modo horizontal para continuar'
  },
  modal: {
    close: 'Cerrar',
    pin: {
      title: 'Acceso parental',
      placeholder: 'Ingresa tu PIN',
      submit: 'Entrar',
      error401: 'PIN incorrecto'
    },
    registerFamily: {
      title: 'Registrar familia',
      namePlaceholder: 'Nombre de la familia',
      pinPlaceholder: 'Crea un PIN de acceso',
      submit: 'Crear familia'
    },
    children: {
      title: '¿Quién eres?',
      addChild: 'Agregar niño',
      empty: 'No hay perfiles aún'
    },
    addChild: {
      title: 'Agregar perfil de niño',
      namePlaceholder: 'Nombre del niño',
      birthdayPlaceholder: 'Fecha de nacimiento',
      avatarPlaceholder: 'URL del avatar (opcional)',
      submit: 'Crear perfil'
    }
  },
  devContent: {
    title: 'Gestor de Contenido',
    nav: {
      categories: 'Categorías',
      topics: 'Temas',
      activities: 'Actividades',
      difficultyLevels: 'Niveles de Dificultad',
      resources: 'Recursos',
      locales: 'Locales',
      curiosities: 'Curiosidades',
      avatarEvents: 'Eventos de Avatar'
    },
    empty: {
      categories: 'No hay categorías registradas',
      topics: 'No hay temas registrados',
      activities: 'No hay actividades registradas',
      difficultyLevels: 'No hay niveles de dificultad registrados',
      resources: 'No hay recursos registrados',
      locales: 'No hay locales registrados',
      curiosities: 'No hay curiosidades registradas',
      avatarEvents: 'No hay eventos de avatar registrados'
    },
    loading: 'Cargando sección...',
    error: 'Error al cargar la sección',
    retry: 'Reintentar',
    categories: {
      title: 'Categorías',
      create: 'Crear Categoría',
      edit: 'Editar Categoría',
      name: 'Nombre',
      description: 'Descripción',
      status: 'Estado',
      displayOrder: 'Orden',
      iconUrl: 'URL del Icono',
      namePlaceholder: 'Ingrese el nombre de la categoría',
      descriptionPlaceholder: 'Ingrese una descripción (opcional)',
      iconUrlPlaceholder: 'URL del icono (opcional)',
      save: 'Guardar',
      cancel: 'Cancelar',
      noCategories: 'No hay categorías registradas',
      errorLoading: 'Error al cargar las categorías',
      errorCreate: 'Error al crear la categoría',
      errorUpdate: 'Error al actualizar la categoría',
      conflictError: 'Ya existe una categoría con ese nombre'
    },
    topics: {
      title: 'Temas',
      create: 'Crear Tema',
      edit: 'Editar Tema',
      name: 'Nombre',
      description: 'Descripción',
      category: 'Categoría',
      status: 'Estado',
      minAge: 'Edad Mínima',
      maxAge: 'Edad Máxima',
      compatibleVariants: 'Variantes Compatibles',
      namePlaceholder: 'Ingrese el nombre del tema',
      descriptionPlaceholder: 'Ingrese una descripción (opcional)',
      selectCategory: 'Seleccione una categoría',
      allCategories: 'Todas las categorías',
      save: 'Guardar',
      cancel: 'Cancelar',
      noTopics: 'No hay temas registrados',
      errorLoading: 'Error al cargar los temas',
      errorCreate: 'Error al crear el tema',
      errorUpdate: 'Error al actualizar el tema',
      categoryNotFound: 'La categoría seleccionada no existe',
      conflictError: 'Ya existe un tema con ese nombre en la categoría'
    },
    status: {
      ACTIVE: 'Activo',
      INACTIVE: 'Inactivo',
      DRAFT: 'Borrador'
    },
    form: {
      required: 'Este campo es obligatorio',
      save: 'Guardar',
      cancel: 'Cancelar',
      create: 'Crear',
      edit: 'Editar'
    },
    errors: {
      badRequest: 'Solicitud incorrecta',
      notFound: 'Recurso no encontrado',
      conflict: 'Conflicto: el recurso ya existe',
      serverError: 'Error del servidor',
      invalidJson: 'JSON inválido'
    },
    activities: {
      title: 'Actividades',
      create: 'Crear Actividad',
      edit: 'Editar Actividad',
      name: 'Nombre',
      description: 'Descripción',
      gameEngineType: 'Tipo de Motor',
      status: 'Estado',
      minAge: 'Edad Mínima',
      maxAge: 'Edad Máxima',
      topics: 'Temas',
      namePlaceholder: 'Ingrese el nombre de la actividad',
      descriptionPlaceholder: 'Ingrese una descripción (opcional)',
      gameEngineTypePlaceholder: 'Tipo de motor de juego (opcional)',
      selectTopics: 'Seleccione los temas',
      allTopics: 'Todos los temas',
      save: 'Guardar',
      cancel: 'Cancelar',
      noActivities: 'No hay actividades registradas',
      errorLoading: 'Error al cargar las actividades',
      errorCreate: 'Error al crear la actividad',
      errorUpdate: 'Error al actualizar la actividad',
      conflictError: 'Ya existe una actividad con ese nombre'
    },
    difficultyLevels: {
      title: 'Niveles de Dificultad',
      create: 'Crear Nivel',
      edit: 'Editar Nivel',
      activity: 'Actividad',
      difficultyCode: 'Código de Dificultad',
      engineParams: 'Parámetros del Motor',
      adaptiveThresholdConfig: 'Configuración de Umbral Adaptativo',
      selectActivity: 'Seleccione una actividad primero',
      selectDifficulty: 'Seleccione el nivel de dificultad',
      engineParamsPlaceholder: 'Ingrese los parámetros del motor (JSON)',
      adaptiveThresholdPlaceholder: 'Ingrese la configuración de umbral (JSON)',
      save: 'Guardar',
      cancel: 'Cancelar',
      noLevels: 'No hay niveles de dificultad registrados',
      errorLoading: 'Error al cargar los niveles de dificultad',
      errorCreate: 'Error al crear el nivel de dificultad',
      errorUpdate: 'Error al actualizar el nivel de dificultad',
      invalidJson: 'El JSON ingresado no es válido'
    },
    resources: {
      title: 'Recursos de Actividad',
      create: 'Crear Recurso',
      edit: 'Editar Recurso',
      activity: 'Actividad',
      topic: 'Tema',
      resourceType: 'Tipo de Recurso',
      path: 'Ruta',
      metadata: 'Metadatos',
      selectActivity: 'Seleccione una actividad primero',
      selectResourceType: 'Seleccione el tipo de recurso',
      selectTopic: 'Seleccione un tema (opcional)',
      pathPlaceholder: 'Ingrese la ruta del recurso',
      metadataPlaceholder: 'Ingrese los metadatos (JSON)',
      save: 'Guardar',
      cancel: 'Cancelar',
      noResources: 'No hay recursos registrados',
      errorLoading: 'Error al cargar los recursos',
      errorCreate: 'Error al crear el recurso',
      errorUpdate: 'Error al actualizar el recurso',
      invalidJson: 'El JSON ingresado no es válido',
      v1Notice: 'V1 gestiona referencias de recursos, no archivos físicos'
    },
    difficultyCode: {
      EASY: 'Fácil',
      MEDIUM: 'Medio',
      HARD: 'Difícil'
    },
    resourceType: {
      IMAGE: 'Imagen',
      AUDIO: 'Audio',
      VIDEO: 'Video'
    }
  }
}
