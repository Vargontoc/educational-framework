export type ErrorSeverity = 'RECOVERABLE' | 'CRITICAL'

export interface ClassifiedError {
  severity: ErrorSeverity
  message: string
  originalEvent?: unknown
}

export class ErrorClassifier {
  static classifyFromBackendEvent(event: { event: string; payload?: { errorCode?: string } }): ClassifiedError {
    switch (event.event) {
      case 'GAME_ERROR':
        if (event.payload?.errorCode) {
          return this.classifyByErrorCode(event.payload.errorCode)
        }
        return {
          severity: 'RECOVERABLE',
          message: 'Error de comunicación temporal',
          originalEvent: event
        }

      case 'SESSION_EXPIRED':
      case 'SESSION_INVALIDATED':
      case 'CHILD_EXPELLED':
        return {
          severity: 'CRITICAL',
          message: 'Sesión terminada',
          originalEvent: event
        }

      default:
        return {
          severity: 'RECOVERABLE',
          message: 'Error desconocido',
          originalEvent: event
        }
    }
  }

  static classifyFromNetworkError(error: unknown): ClassifiedError {
    if (error instanceof WebSocket) {
      return {
        severity: 'RECOVERABLE',
        message: 'Conexión interrumpida',
        originalEvent: error
      }
    }

    if ((error as { type?: string })?.type === 'timeout') {
      return {
        severity: 'RECOVERABLE',
        message: 'Timeout de conexión',
        originalEvent: error
      }
    }

    return {
      severity: 'RECOVERABLE',
      message: 'Error de red',
      originalEvent: error
    }
  }

  private static classifyByErrorCode(errorCode: string): ClassifiedError {
    switch (errorCode) {
      case 'TEMPORARY_FAILURE':
      case 'RATE_LIMIT':
      case 'TIMEOUT':
        return {
          severity: 'RECOVERABLE',
          message: 'Error temporal'
        }

      case 'SESSION_NOT_FOUND':
      case 'INVALID_STATE':
      case 'PERMISSION_DENIED':
        return {
          severity: 'CRITICAL',
          message: 'Error crítico de sesión'
        }

      default:
        return {
          severity: 'RECOVERABLE',
          message: 'Error desconocido'
        }
    }
  }
}
