<script setup lang="ts">
import { ref } from 'vue'
import NubiTextInput from './NubiTextInput.vue'
import NubiIcon from './NubiIcon.vue'

const textValue = ref('')
const emailValue = ref('')
const passwordValue = ref('')
const requiredValue = ref('')
const counterValue = ref('')
</script>

<template>
  <Story title="Base/NubiTextInput">
    <Variant title="Basic">
      <div style="max-width: 400px;">
        <NubiTextInput v-model="textValue" label="Nombre" placeholder="Escribe tu nombre" />
      </div>
    </Variant>
    
    <Variant title="With Icons">
      <div style="max-width: 400px; display: flex; flex-direction: column; gap: 16px;">
        <NubiTextInput label="Email" prefix-icon="mail" placeholder="correo@ejemplo.com" type="email" />
        <NubiTextInput label="Buscar" prefix-icon="search" placeholder="Buscar..." />
        <NubiTextInput label="Contraseña" suffix-icon="eye" placeholder="Tu contraseña" type="password" />
      </div>
    </Variant>
    
    <Variant title="Validation">
      <div style="max-width: 400px; display: flex; flex-direction: column; gap: 16px;">
        <NubiTextInput v-model="requiredValue" label="Campo obligatorio" :required="true" placeholder="Requerido" />
        <NubiTextInput label="Con error" error="Este campo tiene un error" />
        <NubiTextInput label="Con hint" hint="Este es un mensaje de ayuda" />
      </div>
    </Variant>
    
    <Variant title="Character Counter">
      <div style="max-width: 400px;">
        <NubiTextInput v-model="counterValue" label="Descripción" :max-length="100" :show-counter="true" placeholder="Escribe una descripción..." />
      </div>
    </Variant>
    
    <Variant title="States">
      <div style="max-width: 400px; display: flex; flex-direction: column; gap: 16px;">
        <NubiTextInput label="Normal" placeholder="Estado normal" />
        <NubiTextInput label="Disabled" disabled placeholder="Deshabilitado" />
        <NubiTextInput label="Readonly" readonly placeholder="Solo lectura" />
      </div>
    </Variant>
    
    <Variant title="Types">
      <div style="max-width: 400px; display: flex; flex-direction: column; gap: 16px;">
        <NubiTextInput label="Text" type="text" placeholder="Texto" />
        <NubiTextInput label="Email" type="email" placeholder="correo@ejemplo.com" />
        <NubiTextInput label="Password" type="password" placeholder="Contraseña" />
        <NubiTextInput label="Tel" type="tel" placeholder="+34 600 000 000" />
        <NubiTextInput label="URL" type="url" placeholder="https://..." />
      </div>
    </Variant>
  </Story>
</template>

<docs>
## NubiTextInput

Campo de entrada de texto con validación en tiempo real.

### Características
- Label visible siempre
- Placeholder configurable
- Validación en tiempo real (onBlur, onChange)
- Estados error/hint
- Soporte v-model
- Iconos prefix/suffix
- Contador de caracteres
- Accesibilidad WCAG AA

### Uso
```vue
<NubiTextInput v-model="name" label="Nombre" placeholder="Tu nombre" />
<NubiTextInput label="Email" prefix-icon="mail" type="email" />
<NubiTextInput label="Campo" :required="true" :max-length="50" :show-counter="true" />
```
</docs>
