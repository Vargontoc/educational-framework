<script setup lang="ts">
import NubiEmptyState from './NubiEmptyState.vue'
</script>

<template>
  <Story title="Base/NubiEmptyState">
    <Variant title="Default">
      <NubiEmptyState 
        title="No hay datos"
        description="No hay elementos para mostrar en este momento"
      />
    </Variant>
    
    <Variant title="With Icon">
      <NubiEmptyState 
        icon="inbox"
        title="Bandeja vacía"
        description="No tienes mensajes nuevos"
      />
    </Variant>
    
    <Variant title="With Action">
      <NubiEmptyState 
        icon="file-plus"
        title="No hay documentos"
        description="Aún no has creado ningún documento"
        action-label="Crear documento"
      />
    </Variant>
    
    <Variant title="Custom Icon Slot">
      <NubiEmptyState 
        title="Búsqueda sin resultados"
        description="No se encontraron resultados para tu búsqueda"
      >
        <template #icon>
          <div style="font-size: 64px;">🔍</div>
        </template>
      </NubiEmptyState>
    </Variant>
    
    <Variant title="Custom Action Slot">
      <NubiEmptyState 
        icon="folder"
        title="Carpeta vacía"
        description="Esta carpeta no contiene archivos"
      >
        <template #action>
          <div style="display: flex; gap: 12px;">
            <NubiButton variant="primary">Subir archivos</NubiButton>
            <NubiButton variant="secondary">Crear carpeta</NubiButton>
          </div>
        </template>
      </NubiEmptyState>
    </Variant>
  </Story>
</template>

<script setup lang="ts">
import NubiButton from './NubiButton.vue'
</script>

<docs>
## NubiEmptyState

Vista para cuando no hay datos que mostrar.

### Características
- Icono opcional (o slot personalizado)
- Título y descripción
- Acción sugerida (botón opcional)
- Totalmente personalizable con slots

### Uso
```vue
<!-- Básico -->
<NubiEmptyState 
  title="No hay datos"
  description="No hay elementos para mostrar"
/>

<!-- Con icono y acción -->
<NubiEmptyState 
  icon="file-plus"
  title="No hay documentos"
  description="Aún no has creado ningún documento"
  action-label="Crear documento"
  @action="createDocument"
/>

<!-- Con slots personalizados -->
<NubiEmptyState title="Carpeta vacía">
  <template #icon>
    <div style="font-size: 64px;">📁</div>
  </template>
  <template #action>
    <NubiButton>Subir archivos</NubiButton>
  </template>
</NubiEmptyState>
```
</docs>
