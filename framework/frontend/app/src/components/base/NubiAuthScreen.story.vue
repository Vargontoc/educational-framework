<script setup lang="ts">
import NubiAuthScreen from './NubiAuthScreen.vue'
</script>

<template>
  <Story title="Base/NubiAuthScreen">
    <Variant title="Basic">
      <NubiAuthScreen expected-pin="1234" @success="(pin) => alert(`PIN correcto: ${pin}`)" @error="(pin) => console.log(`PIN incorrecto: ${pin}`)" />
    </Variant>
    
    <Variant title="Custom Validation">
      <NubiAuthScreen 
        :validate-pin="async (pin) => pin === '5678'"
        @success="(pin) => alert(`PIN correcto: ${pin}`)"
      />
    </Variant>
  </Story>
</template>

<docs>
## NubiAuthScreen

Pantalla de autenticación con PIN.

### Características
- Integración con NubiPinInput
- Validación de PIN (mock o real)
- Mensaje de error tras fallo
- Estado de validación con spinner
- Botón "¿Olvidaste tu PIN?"

### Uso
```vue
<NubiAuthScreen 
  expected-pin="1234"
  @success="(pin) => handleSuccess(pin)"
  @error="(pin) => handleError(pin)"
/>
```
</docs>
