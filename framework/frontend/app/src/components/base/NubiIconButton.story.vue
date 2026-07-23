<script setup lang="ts">
import NubiIconButton from './NubiIconButton.vue'
</script>

<template>
  <Story title="Base/NubiIconButton">
    <Variant title="Default">
      <NubiIconButton icon="settings" label="Configuración" />
    </Variant>
    
    <Variant title="Sizes">
      <div style="display: flex; gap: 16px; align-items: center;">
        <NubiIconButton icon="settings" label="Configuración" size="sm" />
        <NubiIconButton icon="settings" label="Configuración" size="md" />
        <NubiIconButton icon="settings" label="Configuración" size="lg" />
      </div>
    </Variant>
    
    <Variant title="With Tooltip">
      <NubiIconButton 
        icon="settings" 
        label="Configuración" 
        tooltip="Abrir configuración"
      />
    </Variant>
    
    <Variant title="Disabled">
      <NubiIconButton 
        icon="settings" 
        label="Configuración" 
        :disabled="true"
      />
    </Variant>
    
    <Variant title="Different Icons">
      <div style="display: flex; gap: 16px;">
        <NubiIconButton icon="home" label="Inicio" />
        <NubiIconButton icon="user" label="Perfil" />
        <NubiIconButton icon="settings" label="Configuración" />
        <NubiIconButton icon="bell" label="Notificaciones" />
        <NubiIconButton icon="search" label="Buscar" />
      </div>
    </Variant>
  </Story>
</template>

<docs>
## NubiIconButton

Botón de acción rápida con icono y tooltip.

### Características
- Objetivo táctil mínimo 48x48dp (accesibilidad)
- Forma circular
- Tooltip al hover
- 3 tamaños: sm, md, lg
- Accesibilidad WCAG AA

### Uso
```vue
<NubiIconButton 
  icon="settings" 
  label="Configuración"
  @click="openSettings"
/>
```
</docs>
