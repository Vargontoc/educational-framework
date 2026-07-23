<script setup lang="ts">
import NubiButton from './NubiButton.vue'
</script>

<template>
  <Story title="Base/NubiButton">
    <Variant title="Primary">
      <NubiButton variant="primary">
        Botón Primario
      </NubiButton>
    </Variant>
    
    <Variant title="Secondary">
      <NubiButton variant="secondary">
        Botón Secundario
      </NubiButton>
    </Variant>
    
    <Variant title="Destructive">
      <NubiButton variant="destructive">
        Eliminar
      </NubiButton>
    </Variant>
    
    <Variant title="Sizes">
      <div style="display: flex; gap: 16px; align-items: center;">
        <NubiButton size="sm">Pequeño</NubiButton>
        <NubiButton size="md">Mediano</NubiButton>
        <NubiButton size="lg">Grande</NubiButton>
      </div>
    </Variant>
    
    <Variant title="With Icon">
      <NubiButton variant="primary">
        <template #icon>
          <NubiIcon name="plus" :size="20" />
        </template>
        Agregar
      </NubiButton>
    </Variant>
    
    <Variant title="Loading">
      <NubiButton variant="primary" :loading="true">
        Guardando...
      </NubiButton>
    </Variant>
    
    <Variant title="Disabled">
      <NubiButton variant="primary" :disabled="true">
        Deshabilitado
      </NubiButton>
    </Variant>
    
    <Variant title="All States">
      <div style="display: flex; flex-direction: column; gap: 24px;">
        <div>
          <h4>Primary</h4>
          <div style="display: flex; gap: 16px;">
            <NubiButton variant="primary">Normal</NubiButton>
            <NubiButton variant="primary" :loading="true">Loading</NubiButton>
            <NubiButton variant="primary" :disabled="true">Disabled</NubiButton>
          </div>
        </div>
        
        <div>
          <h4>Secondary</h4>
          <div style="display: flex; gap: 16px;">
            <NubiButton variant="secondary">Normal</NubiButton>
            <NubiButton variant="secondary" :loading="true">Loading</NubiButton>
            <NubiButton variant="secondary" :disabled="true">Disabled</NubiButton>
          </div>
        </div>
        
        <div>
          <h4>Destructive</h4>
          <div style="display: flex; gap: 16px;">
            <NubiButton variant="destructive">Normal</NubiButton>
            <NubiButton variant="destructive" :loading="true">Loading</NubiButton>
            <NubiButton variant="destructive" :disabled="true">Disabled</NubiButton>
          </div>
        </div>
      </div>
    </Variant>
  </Story>
</template>

<docs>
## NubiButton

Componente de botón principal con 3 variantes: primary, secondary y destructive.

### Características
- Objetivo táctil mínimo 48x48dp (accesibilidad)
- 4 estados: normal, hover, pressed, disabled
- Soporte para iconos y loading state
- Internacionalización completa
- Accesibilidad WCAG AA

### Variantes
- **primary**: Color destacado, acción principal
- **secondary**: Menos prominente, acciones secundarias
- **destructive**: Color rojo suave, acciones irreversibles

### Uso
```vue
<NubiButton variant="primary" @click="handleClick">
  Guardar
</NubiButton>

<NubiButton variant="secondary">
  Cancelar
</NubiButton>

<NubiButton variant="destructive">
  Eliminar
</NubiButton>
```
</docs>
