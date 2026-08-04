export default {
  common: {
    loading: 'Cargando...',
    error: 'Ha ocurrido un error',
    retry: 'Reintentar',
    cancel: 'Cancelar',
    confirm: 'Confirmar',
    save: 'Guardar',
    delete: 'Eliminar',
    edit: 'Editar',
    close: 'Cerrar',
    back: 'Volver',
    next: 'Siguiente',
    search: 'Buscar',
    noResults: 'No se encontraron resultados',
    required: 'Este campo es obligatorio',
    optional: 'Opcional',
    on: 'Activado',
    off: 'Desactivado'
  },
  components: {
    button: {
      primary: 'Botón primario',
      secondary: 'Botón secundario',
      destructive: 'Botón destructivo',
      loading: 'Cargando...'
    },
    emptyState: {
      defaultTitle: 'No hay datos',
      defaultDescription: 'No hay elementos para mostrar en este momento'
    },
    errorState: {
      defaultTitle: 'Error',
      defaultMessage: 'Ha ocurrido un error inesperado',
      defaultRetry: 'Reintentar'
    },
    textInput: {
      placeholder: 'Escribe aquí...',
      required: 'Este campo es obligatorio',
      minLength: 'Mínimo {min} caracteres',
      maxLength: 'Máximo {max} caracteres',
      invalidFormat: 'Formato no válido',
      charactersCount: '{count} / {max} caracteres'
    },
    numberInput: {
      placeholder: '0',
      increment: 'Incrementar',
      decrement: 'Decrementar',
      min: 'El valor mínimo es {min}',
      max: 'El valor máximo es {max}',
      step: 'El valor debe ser múltiplo de {step}'
    },
    pinInput: {
      label: 'PIN de seguridad',
      placeholder: '·',
      complete: 'PIN completado',
      digit: 'Dígito {position} de {total}',
      clear: 'Borrar PIN'
    },
    checkbox: {
      checked: 'Seleccionado',
      unchecked: 'No seleccionado',
      indeterminate: 'Parcialmente seleccionado'
    },
    toggle: {
      label: 'Alternar'
    },
    select: {
      placeholder: 'Selecciona una opción',
      noOptions: 'No hay opciones disponibles',
      selected: 'Seleccionado: {value}'
    },
    radioGroup: {
      selected: 'Seleccionado: {value}'
    },
    sidebar: {
      toggle: 'Alternar menú',
      collapse: 'Colapsar menú',
      expand: 'Expandir menú',
      panel: 'Panel',
      experiences: 'Experiencias',
      settings: 'Configuración',
      children: 'Niños',
      chatbot: 'Chatbot',
      documentation: 'Documentación',
      readingFamily: 'Lectura en familia',
      relaxationFamily: 'Relajación en familia'
    },
    tabs: {
      selected: 'Pestaña seleccionada: {tab}'
    },
    breadcrumb: {
      label: 'Navegación',
      current: 'Página actual'
    },
    backButton: {
      label: 'Volver atrás'
    },
    confirmModal: {
      title: 'Confirmar acción',
      confirm: 'Confirmar',
      cancel: 'Cancelar'
    },
    infoModal: {
      close: 'Cerrar'
    },
    toast: {
      dismiss: 'Cerrar notificación'
    },
    alert: {
      dismiss: 'Cerrar alerta',
      info: 'Información',
      warning: 'Advertencia',
      error: 'Error',
      success: 'Éxito'
    },
    tooltip: {
      label: 'Más información'
    },
    card: {
      actions: 'Acciones'
    },
    avatar: {
      fallback: 'Usuario'
    },
    badge: {
      info: 'Información',
      success: 'Éxito',
      warning: 'Advertencia',
      error: 'Error'
    },
    list: {
      empty: 'No hay elementos'
    },
    grid: {
      empty: 'No hay elementos'
    },
    progressBar: {
      label: 'Progreso: {value}%',
      complete: 'Completado'
    },
    stepper: {
      step: 'Paso {current} de {total}',
      next: 'Siguiente',
      previous: 'Anterior',
      complete: 'Completar'
    },
    counter: {
      label: 'Cantidad: {value}'
    },
    authScreen: {
      title: 'Acceso al Panel',
      subtitle: 'Introduce tu PIN de seguridad',
      error: 'PIN incorrecto. Inténtalo de nuevo.',
      forgot: '¿Olvidaste tu PIN?',
      validating: 'Validando...'
    },
    sessionIndicator: {
      label: 'Sesión activa',
      timeLeft: 'Tiempo restante: {time}',
      warning: 'Tu sesión expirará pronto',
      expired: 'Sesión expirada'
    },
    inactivityOverlay: {
      title: '¿Sigues ahí?',
      message: 'Tu sesión expirará en {time} por inactividad',
      extend: 'Continuar sesión',
      logout: 'Cerrar sesión'
    },
    inactivityDetector: {
      warning: 'Se detectó inactividad'
    }
  },
  sidebar: {
    groups: {
      panel: 'Panel',
      experiences: 'Experiencias'
    },
    sections: {
      configuracion: 'Configuración',
      ninos: 'Niños',
      chatbot: 'Chatbot',
      documentacion: 'Documentación',
      lecturaFamiliar: 'Lectura familiar',
      relajacionFamiliar: 'Relajación familiar'
    },
    logout: 'Salir',
    ariaLabels: {
      openMenu: 'Abrir menú de navegación',
      closeMenu: 'Cerrar menú de navegación',
      mainNavigation: 'Navegación principal del panel parental'
    }
  },
  inactivity: {
    message: 'Tu sesión ha finalizado por inactividad',
    redirecting: 'Redirigiendo en {seconds} segundos...',
    clickToContinue: 'Haz clic para continuar'
  },
  modals: {
    parentalAuth: {
      title: 'Acceso parental',
      description: 'Introduce tu PIN de seguridad para acceder al panel parental',
      enter: 'Entrar',
      errorInvalid: 'PIN incorrecto. Inténtalo de nuevo.',
      errorConnection: 'Sin conexión. Revisa tu red.',
      errorServer: 'Error al iniciar sesión. Inténtalo de nuevo.',
      cooldown: 'Demasiados intentos. Espera {seconds} segundos.',
      attemptsRemaining: 'Intentos restantes: {count}'
    }
  },
  views: {
    home: {
      title: 'My Friend Nubi',
      description: 'Bienvenido a My Friend Nubi, tu compañero de aprendizaje',
      registerFamily: 'Registrar familia',
      welcomeFamily: 'Bienvenida familia {name}',
      documentation: 'Documentación',
      settings: 'Configuración',
      nubiAvatar: 'Nubi, tu compañero de aprendizaje',
      errorLoading: 'No se pudo cargar el estado de la familia. Inténtalo de nuevo.',
      familyRegistration: {
        title: 'Registro de familia',
        stepperLabel: 'Progreso del registro',
        step1Label: 'Nombre',
        step2Label: 'PIN',
        step1Description: 'Elige un nombre para tu familia. Es el nombre que verás en la pantalla de inicio.',
        step2Description: 'Crea un PIN de 4 dígitos para acceder a la configuración. Recuerda guardarlo en un lugar seguro.',
        familyNameLabel: 'Nombre de familia',
        familyNamePlaceholder: 'Ej. Familia García',
        createPinLabel: 'Crea tu PIN',
        confirmPinLabel: 'Confirma tu PIN',
        nameRequired: 'Introduce un nombre para tu familia',
        pinIncomplete: 'El PIN debe tener 4 dígitos',
        pinMismatch: 'Los PIN no coinciden',
        createFamily: 'Crear familia',
        errorValidation: 'Los datos no son válidos. Revisa e inténtalo de nuevo.',
        errorConflict: 'Ya existe una familia registrada.',
        errorServer: 'No se pudo completar el registro. Inténtalo más tarde.',
        errorConnection: 'No hay conexión con el servidor. Revisa tu conexión e inténtalo de nuevo.',
        errorGeneric: 'Ha ocurrido un error inesperado.'
      },
      familyRegistrationTitle: 'Registro de familia',
      familyRegistrationPlaceholder: 'El formulario de registro de familia se implementará en una próxima versión.',
      childSelectionTitle: 'Seleccionar niño',
      childSelectionPlaceholder: 'La selección de niños se implementará en una próxima versión.',
      childSelection: {
        familyTitle: 'Familia {name}',
        familyTitleDefault: 'Seleccionar niño',
        loading: 'Cargando perfiles...',
        errorTitle: 'Error al cargar perfiles',
        noProfiles: 'No hay perfiles registrados',
        registerChild: 'Registrar niño',
        selectProfile: 'Seleccionar perfil de {name}',
        pinVerification: {
          title: 'Verificación parental',
          description: 'Introduce tu PIN de seguridad para continuar',
          verify: 'Verificar',
          verifying: 'Verificando...',
          errorInvalid: 'PIN incorrecto. Inténtalo de nuevo.',
          errorConnection: 'Sin conexión. Revisa tu red.',
          errorServer: 'Error al verificar. Inténtalo de nuevo.'
        },
        registration: {
          title: 'Registrar niño',
          step1Label: 'Nombre',
          step2Label: 'Fecha y avatar',
          nameLabel: 'Nombre del niño',
          namePlaceholder: 'Ej. Laura',
          birthdayLabel: 'Fecha de nacimiento',
          avatarLabel: 'Selecciona un avatar',
          confirm: 'Confirmar alta',
          submitting: 'Guardando...',
          nameRequired: 'Introduce un nombre',
          birthdayRequired: 'Selecciona la fecha de nacimiento',
          birthdayFuture: 'La fecha no puede ser futura',
          errorValidation: 'Los datos no son válidos. Revisa e inténtalo de nuevo.',
          errorConflict: 'Ya existe un perfil con ese nombre.',
          errorServer: 'Error al guardar. Inténtalo de nuevo.',
          errorConnection: 'Sin conexión. Revisa tu red.',
          avatar1: 'Zorro',
          avatar2: 'Gato',
          avatar3: 'Perro',
          avatar4: 'León',
          avatar5: 'Panda',
          avatar6: 'Koala'
        }
      }
    },
    panel: {
      title: 'Panel de Control',
      description: 'Panel de control parental'
    },
    panelCover: {
      title: 'Panel parental',
      description: 'PLACEHOLDER: Descripción breve del panel parental, sujeta a validación de contenido.',
      groups: {
        panel: 'Panel',
        experiences: 'Experiencias'
      },
      sections: {
        settings: 'Configuración',
        children: 'Niños',
        chatbot: 'Chatbot',
        documentation: 'Documentación',
        readingFamily: 'Lectura familiar',
        relaxationFamily: 'Relajación familiar'
      }
    },
    game: {
      title: 'Juego',
      description: 'Vista de juego para',
      childId: 'Niño seleccionado: {childId}'
    },
    docs: {
      title: 'Documentación',
      description: 'Documentación y tutoriales de la aplicación',
      sidebar: {
        label: 'Navegación de documentación',
        sections: {
          quienSoy: 'Quién soy',
          primerosPasos: 'Primeros pasos',
          agentesAi: 'Agentes AI',
          minijuegos: 'Minijuegos',
          contacto: 'Contacto'
        }
      },
      menuToggle: 'Abrir menú de navegación',
      notFound: 'Sección no encontrada',
      backToPanel: 'Volver al panel parental',
      contact: {
        title: 'Contacto',
        typeLabel: 'Tipo de mensaje',
        typeComment: 'Comentario',
        typeSuggest: 'Sugerencia',
        typeError: 'Error',
        textareaLabel: 'Escribe tu comentario, sugerencia o error',
        textareaPlaceholder: 'Tu mensaje...',
        maxLengthError: 'El mensaje es demasiado largo (máximo 2000 caracteres)',
        privacyNotice: 'No incluyas datos de menores, nombres, PIN ni otra información privada.',
        purposeInfo: 'Tu mensaje será recibido por el equipo de My Friend Nubi para atender tu consulta. No se utiliza para publicidad, perfilado ni entrenamiento de IA.',
        adultConfirmation: 'Soy persona adulta responsable y acepto la información sobre el uso de mi mensaje.',
        sendButton: 'Enviar',
        sending: 'Enviando...',
        successMessage: 'Tu mensaje ha sido recibido. Gracias por contactar con nosotros.',
        errorGeneric: 'No se ha podido enviar el mensaje. Inténtalo más tarde.',
        errorValidation: 'El mensaje no es válido. Revisa el contenido e inténtalo de nuevo.',
        errorRateLimit: 'Has realizado demasiados intentos. Inténtalo más tarde.'
      }
    },
    placeholder: {
      title: 'Sección en desarrollo',
      description: 'Esta sección estará disponible en una próxima versión.'
    },
    notFound: {
      title: 'Página no encontrada',
      description: 'La página que buscas no existe'
    },
    configuracion: {
      title: 'Configuración',
      saveButton: 'Guardar cambios',
      saveSuccess: 'Configuración guardada correctamente',
      saveError: 'No se pudo guardar la configuración',
      pinChangedLogout: 'PIN actualizado. Sesión cerrada por seguridad.',
      sections: {
        audioGeneral: {
          title: 'Audio general',
          description: 'Controla todo el sonido de la aplicación.',
          toggleLabel: 'Activar audio general'
        },
        npc: {
          title: 'NPC',
          description: 'Presencia de Nubi durante el juego.',
          toggleLabel: 'Mostrar a Nubi'
        },
        npcVoice: {
          title: 'Voz del NPC',
          description: 'Voz de Nubi durante el juego. Si la apagas, Nubi sigue presente pero en silencio.',
          toggleLabel: 'Activar voz de Nubi'
        },
        narrativeVoice: {
          title: 'Voz narrativa',
          description: 'Voz de la lectura familiar. Independiente del Nubi.',
          toggleLabel: 'Activar voz narrativa'
        },
        pin: {
          title: 'PIN familiar',
          description: 'Cambia el PIN de acceso al panel parental.',
          newPinLabel: 'Nuevo PIN',
          confirmPinLabel: 'Confirmar nuevo PIN',
          mismatchError: 'Los PINs no coinciden. Inténtalo de nuevo.'
        }
      }
    },
    ninos: {
      title: 'Niños',
      registerButton: 'Registrar niño',
      registerPlaceholder: 'Próximamente disponible',
      registerSuccess: '{name} registrado correctamente',
      noProfiles: 'No hay perfiles registrados',
      stepper: {
        step1: {
          title: 'Nombre del niño',
          nameLabel: 'Nombre',
          namePlaceholder: 'Introduce el nombre'
        },
        step2: {
          title: 'Fecha de nacimiento y avatar',
          birthdayLabel: 'Fecha de nacimiento',
          avatarLabel: 'Selecciona un avatar'
        },
        createButton: 'Crear perfil',
        createError: 'No se pudo crear el perfil'
      },
      expelModal: {
        title: 'Expulsar de la sesión',
        message: '¿Terminar la sesión de {name}? El niño volverá a la pantalla de selección.'
      },
      expelSuccess: 'Sesión terminada correctamente',
      expelError: 'No se pudo terminar la sesión',
      blockSuccess: 'Estado de bloqueo actualizado',
      blockError: 'No se pudo actualizar el estado',
      card: {
        expel: 'Expulsar',
        block: 'Bloquear',
        unblock: 'Desbloquear',
        blocked: 'Bloqueado',
        sessionDuration: 'Tiempo de sesión'
      },
      edit: {
        title: 'Editar perfil',
        saveButton: 'Guardar cambios',
        deleteButton: 'Eliminar',
        dashboardButton: 'Dashboard',
        disabledByFamily: 'Deshabilitado a nivel familiar',
        saveSuccess: 'Cambios guardados correctamente',
        saveError: 'No se pudieron guardar los cambios',
        deleteSuccess: 'Perfil eliminado correctamente',
        deleteError: 'No se pudo eliminar el perfil',
        deleteModal: {
          title: 'Eliminar perfil',
          message: '¿Eliminar a {name}? Se eliminarán todos sus datos.'
        },
        sections: {
          basicData: {
            title: 'Datos básicos',
            nameLabel: 'Nombre',
            birthdayLabel: 'Fecha de nacimiento',
            avatarLabel: 'Avatar'
          },
          audio: {
            title: 'Audio del NPC',
            voiceLabel: 'Voz del NPC',
            npcLabel: 'NPC'
          },
          visualAccessibility: {
            title: 'Accesibilidad visual',
            toggleLabel: 'Activar ajuste visual',
            selectorLabel: 'Selecciona un perfil de visualización para comparar',
            warning: 'Esta comparación es orientativa para adaptar algunos minijuegos relacionados con identificar colores. No identifica la visión del niño ni sustituye la orientación de un especialista.',
            cardAriaLabel: 'Perfil de visualización {mode}: {description}'
          }
        }
      },
      dashboard: {
        placeholder: 'Próximamente disponible'
      }
    },
    errors: {
      networkError: 'No se pudo guardar. Revisa tu conexión.',
      validationError: 'Error de validación. Revisa los datos.',
      genericError: 'Ha ocurrido un error. Inténtalo de nuevo.'
    },
    catalog: {
      title: 'Catálogo de Componentes',
      subtitle: 'Sistema de diseño y componentes',
      designTokens: 'Tokens de diseño',
      colors: 'Colores',
      typography: 'Tipografía',
      spacing: 'Espaciado',
      borders: 'Bordes y sombras',
      components: 'Componentes',
      actionComponents: 'Componentes de acción',
      feedbackComponents: 'Componentes de feedback',
      inputComponents: 'Componentes de entrada',
      navigationComponents: 'Componentes de navegación',
      contentComponents: 'Componentes de contenido',
      progressComponents: 'Componentes de progreso',
      sessionComponents: 'Componentes de sesión',
      buttons: 'Botones',
      iconButtons: 'Botones de icono',
      textInputs: 'Entradas de texto',
      numberInputs: 'Entradas numéricas',
      pinInputs: 'Entradas PIN',
      checkboxes: 'Casillas de verificación',
      toggles: 'Interruptores',
      selects: 'Selectores',
      radioGroups: 'Grupos de radio',
      sidebars: 'Barras laterales',
      tabsComponent: 'Pestañas',
      breadcrumbs: 'Migas de pan',
      backButtons: 'Botones atrás',
      confirmModals: 'Modales de confirmación',
      infoModals: 'Modales informativos',
      toasts: 'Notificaciones toast',
      alerts: 'Alertas',
      tooltips: 'Tooltips',
      cards: 'Tarjetas',
      avatars: 'Avatares',
      badges: 'Badges',
      lists: 'Listas',
      grids: 'Cuadrículas',
      progressBars: 'Barras de progreso',
      steppers: 'Steppers',
      counters: 'Contadores',
      authScreens: 'Pantallas de autenticación',
      sessionIndicators: 'Indicadores de sesión',
      inactivityOverlays: 'Overlays de inactividad',
      inputs: 'Entradas',
      modals: 'Modales',
      spinners: 'Spinners',
      skeletons: 'Skeletons',
      emptyStates: 'Estados vacíos',
      errorStates: 'Estados de error',
      theme: 'Tema',
      currentTheme: 'Tema actual',
      lightMode: 'Modo claro',
      darkMode: 'Modo oscuro'
    }
  }
}
