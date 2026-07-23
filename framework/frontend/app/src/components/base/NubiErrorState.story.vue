<script setup lang="ts">
import NubiErrorState from './NubiErrorState.vue'
</script>

<template>
  <Story title="Base/NubiErrorState">
    <Variant title="Default">
      <NubiErrorState />
    </Variant>
    
    <Variant title="Custom Message">
      <NubiErrorState 
        title="Error de conexión"
        message="No se pudo conectar con el servidor. Por favor, verifica tu conexión a internet."
      />
    </Variant>
    
    <Variant title="With Details">
      <NubiErrorState 
        title="Error al cargar datos"
        message="Ha ocurrido un error al cargar la información"
        details="Error 500: Internal Server Error at /api/v1/users"
      />
    </Variant>
    
    <Variant title="Without Retry">
      <NubiErrorState 
        title="Acceso denegado"
        message="No tienes permisos para acceder a este recurso"
        :show-retry="false"
      />
    </Variant>
    
    <Variant title="Custom Retry Label">
      <NubiErrorState 
        title="Tiempo de espera agotado"
        message="La operación tardó demasiado en completarse"
        retry-label="Intentar de nuevo"
      />
    </Variant>
  </Story>
</template>

<docs>
## NubiErrorState

Vista para mostrar errores con opción de reintentar.

### Características
- Icono de error
- Título y mensaje claros
- Detalles técnicos opcionales
- Botón de reintentar
- Accesibilidad completa

### Uso
```vue
<!-- Básico -->
<NubiErrorState />

<!-- Con mensaje personalizado -->
<NubiErrorState 
  title="Error de conexión"
  message="No se pudo conectar con el servidor"
  @retry="handleRetry"
/>

<!-- Con detalles técnicos -->
<NubiErrorState 
  title="Error al cargar"
  message="Ha ocurrido un error"
  details="Error 500: Internal Server Error"
/>

<!-- Sin botón de reintentar -->
<NubiErrorState 
  title="Acceso denegado"
  message="No tienes permisos"
  :show-retry="false"
/>
```
</docs>
