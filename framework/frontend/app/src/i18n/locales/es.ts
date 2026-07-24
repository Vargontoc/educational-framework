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
  views: {
    home: {
      title: 'My Friend Nubi',
      description: 'Bienvenido a My Friend Nubi, tu compañero de aprendizaje'
    },
    panel: {
      title: 'Panel de Control',
      description: 'Panel de control parental'
    },
    game: {
      title: 'Juego',
      description: 'Vista de juego para',
      childId: 'Niño seleccionado: {childId}'
    },
    docs: {
      title: 'Documentación',
      description: 'Documentación y tutoriales de la aplicación'
    },
    notFound: {
      title: 'Página no encontrada',
      description: 'La página que buscas no existe'
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
