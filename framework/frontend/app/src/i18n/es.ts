export const messages = {
  common: {
    loading: 'Cargando...',
    error: 'Ha ocurrido un error',
    retry: 'Reintentar'
  },
  home: {
    title: 'Inicio',
    loading: 'Cargando...',
    error: 'Ha ocurrido un error al cargar la familia',
    retry: 'Reintentar',
    welcomeFamily: 'Bienvenida familia',
    noFamilyKicker: 'Crea un espacio familiar para comenzar con una experiencia privada y acompañada.',
    familyReadyKicker: 'Elige quién va a jugar o entra al panel del adulto.',
    helpAriaLabel: 'Ayuda',
    settingsAriaLabel: 'Configuración'
  },
  panel: {
    title: 'Panel de Control',
    eyebrow: 'Espacio adulto',
    logout: 'Cerrar sesión',
    logoutAriaLabel: 'Cerrar sesión y volver al inicio',
    nav: {
      managementGroup: 'Gestión',
      experiencesGroup: 'Experiencias',
      settings: 'Configuración',
      settingsAriaLabel: 'Configuración familiar',
      children: 'Niños',
      childrenAriaLabel: 'Gestión de perfiles infantiles',
      agent: 'Agentes',
      agentAriaLabel: 'Configuración del chatbot',
      documentation: 'Documentación',
      documentationAriaLabel: 'Documentación y ayuda',
      familyReading: 'Lectura en familia',
      familyReadingAriaLabel: 'Experiencia de lectura en familia',
      familyRelaxation: 'Relajación en familia',
      familyRelaxationAriaLabel: 'Experiencia de relajación en familia'
    },
    section: {
      settings: {
        title: 'Configuración',
        description: 'Gestiona el idioma, audio, avatar y PIN de la familia.'
      },
      children: {
        title: 'Niños',
        description: 'Gestiona los perfiles de los niños y su acceso.'
      },
      agent: {
        title: 'Agentes',
        description: 'Configura el comportamiento del chatbot familiar.'
      },
      documentation: {
        title: 'Documentación',
        description: 'Consulta la documentación y ayuda disponible.'
      },
      familyReading: {
        title: 'Lectura en familia',
        description: 'Actividades de lectura compartidas.'
      },
      familyRelaxation: {
        title: 'Relajación en familia',
        description: 'Momentos de calma y relajación en familia.'
      }
    },
    placeholder: {
      comingSoon: 'Próximamente'
    }
  },
  docs: {
    title: 'Documentación',
    eyebrow: 'Ayuda y recursos',
    backHome: 'Volver al inicio',
    backPanel: 'Volver al panel',
    backPanelAriaLabel: 'Volver al panel de control',
    sections: {
      gettingStarted: {
        title: 'Primeros pasos',
        description: 'Aprende a crear tu espacio familiar, registrar perfiles de niños y comenzar a usar la aplicación.'
      },
      familyAndProfiles: {
        title: 'Familia y perfiles',
        description: 'Gestiona los perfiles de los miembros de tu familia, sus datos y preferencias.'
      },
      parentControl: {
        title: 'Panel de control parental',
        description: 'Accede a la configuración avanzada, gestiona el acceso de los niños y personaliza la experiencia.'
      },
      familyExperiences: {
        title: 'Experiencias en familia',
        description: 'Descubre las actividades de lectura, relajación y juego disponibles para toda la familia.'
      },
      privacyAndSecurity: {
        title: 'Privacidad y seguridad',
        description: 'Información sobre cómo protegemos tus datos y los de tus hijos.'
      },
      support: {
        title: 'Soporte',
        description: '¿Tienes dudas o problemas? Consulta las preguntas frecuentes o contacta con nuestro equipo.'
      }
    }
  },
  game: {
    title: 'Paseo de aprendizaje',
    mapEyebrow: 'Mundo vivo',
    loaderText: 'Preparando tu aventura...'
  },
  rotation: {
    message: 'Por favor, gira el dispositivo a modo horizontal para continuar'
  },
  modal: {
    close: 'Cerrar',
    pin: {
      title: 'Acceso parental',
      placeholder: 'Ingresa tu PIN de 4 dígitos',
      submit: 'Entrar',
      error401: 'PIN incorrecto',
      errorServer: 'Error del servidor. Intenta más tarde.'
    },
    registerFamily: {
      title: 'Crear espacio familiar',
      step1Label: '¿Cómo se llama tu familia?',
      step2Label: 'Crea un PIN de 4 dígitos',
      namePlaceholder: 'Nombre de la familia',
      nameRequired: 'Ingresa el nombre de tu familia',
      continue: 'Continuar',
      back: 'Volver',
      backAriaLabel: 'Volver al paso anterior',
      digitAria: 'Número {digit}',
      digitEntered: 'ingresado',
      deleteAriaLabel: 'Borrar último dígito',
      clearAriaLabel: 'Borrar todo',
      delete: '⌫',
      clear: 'C',
      keypadAriaLabel: 'Teclado numérico',
      errorBadRequest: 'Datos inválidos. Revisa la información.',
      errorServer: 'Error del servidor. Intenta más tarde.',
      errorConflict: 'Ya existe una familia registrada.',
      confirm: 'Confirmar'
    },
    children: {
      title: '¿Quién va a jugar?',
      addChild: 'Agregar niño',
      empty: 'No hay perfiles aún',
      cardAriaLabel: 'Perfil de {name}'
    },
    addChild: {
      title: 'Nuevo perfil',
      step1Label: '¿Cómo se llama el niño?',
      step2Label: '¿Cuándo es su cumpleaños?',
      step3Label: 'Elige un avatar',
      namePlaceholder: 'Nombre del niño',
      nameRequired: 'Ingresa el nombre del niño',
      nameWhitespace: 'El nombre no puede estar vacío',
      birthdayPlaceholder: 'Fecha de nacimiento',
      birthdayRequired: 'Ingresa la fecha de nacimiento',
      birthdayInvalid: 'Fecha inválida',
      birthdayOutOfRange: 'Esta aplicación es para niños y niñas de 3 a 8 años',
      avatarSectionLabel: 'Selecciona un avatar',
      avatarOptionAria: 'Avatar {name}, {index} de {total}',
      avatarSelectedAria: 'Avatar {name} seleccionado',
      back: 'Volver',
      backAriaLabel: 'Volver al paso anterior',
      continue: 'Continuar',
      confirm: 'Crear perfil',
      submitting: 'Creando perfil...',
      errorBadRequest: 'Datos inválidos. Revisa la información.',
      errorNotFound: 'Familia no encontrada. Intenta de nuevo.',
      errorServer: 'Error del servidor. Intenta más tarde.',
      closeAriaLabel: 'Cerrar'
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
    },
    locales: {
      title: 'Locales de Contenido',
      create: 'Crear Locale',
      edit: 'Editar Locale',
      entityType: 'Tipo de Entidad',
      entityId: 'ID de Entidad',
      localeCode: 'Código de Locale',
      name: 'Nombre',
      description: 'Descripción',
      selectEntityType: 'Seleccione el tipo de entidad',
      selectEntityId: 'Ingrese el ID de la entidad',
      namePlaceholder: 'Ingrese el nombre localizado',
      descriptionPlaceholder: 'Ingrese una descripción (opcional)',
      save: 'Guardar',
      cancel: 'Cancelar',
      noLocales: 'No hay locales registrados',
      errorLoading: 'Error al cargar los locales',
      errorCreate: 'Error al crear el locale',
      errorUpdate: 'Error al actualizar el locale',
      conflictError: 'Ya existe un locale para esta entidad y código',
      i18nNotice: 'Los locales de contenido son datos del catálogo, no traducciones de la interfaz'
    },
    curiosities: {
      title: 'Curiosidades',
      create: 'Crear Curiosidad',
      edit: 'Editar Curiosidad',
      text: 'Texto',
      topic: 'Tema',
      minAge: 'Edad Mínima',
      maxAge: 'Edad Máxima',
      locale: 'Locale',
      tags: 'Etiquetas',
      phoneticHint: 'Sugerencia Fonética',
      status: 'Estado',
      textPlaceholder: 'Ingrese el texto de la curiosidad (máx. 300 caracteres)',
      selectTopic: 'Seleccione un tema (opcional)',
      allTopics: 'Todos los temas',
      tagsPlaceholder: 'etiqueta1, etiqueta2',
      phoneticHintPlaceholder: 'Sugerencia fonética para TTS (opcional)',
      save: 'Guardar',
      cancel: 'Cancelar',
      noCuriosities: 'No hay curiosidades registradas',
      errorLoading: 'Error al cargar las curiosidades',
      errorCreate: 'Error al crear la curiosidad',
      errorUpdate: 'Error al actualizar la curiosidad',
      conflictError: 'Ya existe una curiosidad con ese texto',
      ttsHelper: 'Texto orientado a TTS. Máximo 300 caracteres.',
      charCount: '{count}/300 caracteres'
    },
    avatarEvents: {
      title: 'Eventos de Avatar',
      create: 'Crear Evento',
      edit: 'Editar Evento',
      eventType: 'Tipo de Evento',
      tone: 'Tono',
      locale: 'Locale',
      messageText: 'Texto del Mensaje',
      status: 'Estado',
      selectEventType: 'Seleccione el tipo de evento',
      selectTone: 'Seleccione el tono',
      messageTextPlaceholder: 'Ingrese el texto del mensaje (máx. 300 caracteres)',
      save: 'Guardar',
      cancel: 'Cancelar',
      noEvents: 'No hay eventos de avatar registrados',
      errorLoading: 'Error al cargar los eventos',
      errorCreate: 'Error al crear el evento',
      errorUpdate: 'Error al actualizar el evento',
      conflictError: 'Ya existe un evento con esos parámetros',
      ttsHelper: 'Mensaje fallback optimizado para TTS. Máximo 300 caracteres.',
      charCount: '{count}/300 caracteres'
    },
    localeEntityType: {
      CATEGORY: 'Categoría',
      TOPIC: 'Tema',
      ACTIVITY: 'Actividad',
      DIFFICULTY_LEVEL: 'Nivel de Dificultad',
      ACTIVITY_RESOURCE: 'Recurso de Actividad'
    },
    avatarEventType: {
      ACTIVITY_COMPLETED: 'Actividad Completada',
      ACTIVITY_STARTED: 'Actividad Iniciada',
      ACTIVITY_FAILED: 'Actividad Fallida',
      HELP_REQUESTED: 'Ayuda Solicitada',
      OUT_OF_SCOPE_QUERY: 'Consulta Fuera de Alcance',
      CURIOSITY_REQUESTED: 'Curiosidad Solicitada'
    },
    avatarTone: {
      CALM: 'Calma',
      JOYFUL: 'Alegre',
      ENTHUSIASTIC: 'Entusiasta',
      SERIOUS: 'Serio',
      NEUTRAL: 'Neutral'
    }
  }
}
