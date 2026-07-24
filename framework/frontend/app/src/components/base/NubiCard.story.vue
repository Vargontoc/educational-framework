<script setup lang="ts">
import NubiCard from './NubiCard.vue'
import NubiButton from './NubiButton.vue'
</script>

<template>
  <Story title="Base/NubiCard">
    <Variant title="Basic">
      <div style="max-width: 400px;">
        <NubiCard title="Título de la tarjeta" description="Descripción de la tarjeta con información relevante." />
      </div>
    </Variant>
    
    <Variant title="With Image">
      <div style="max-width: 400px;">
        <NubiCard 
          image="https://picsum.photos/400/225" 
          image-alt="Imagen de ejemplo"
          title="Título con imagen" 
          description="Esta tarjeta tiene una imagen en la parte superior."
        />
      </div>
    </Variant>
    
    <Variant title="Clickable">
      <div style="max-width: 400px;">
        <NubiCard 
          title="Tarjeta clickeable" 
          description="Esta tarjeta es clickeable y tiene efecto hover."
          :clickable="true"
        />
      </div>
    </Variant>
    
    <Variant title="With Actions">
      <div style="max-width: 400px;">
        <NubiCard title="Tarjeta con acciones" description="Esta tarjeta tiene botones de acción en el footer.">
          <template #actions>
            <NubiButton variant="secondary" size="sm">Cancelar</NubiButton>
            <NubiButton variant="primary" size="sm">Aceptar</NubiButton>
          </template>
        </NubiCard>
      </div>
    </Variant>
    
    <Variant title="Custom Slots">
      <div style="max-width: 400px;">
        <NubiCard>
          <template #header>
            <div style="display: flex; align-items: center; gap: 8px;">
              <NubiBadge variant="success">Nuevo</NubiBadge>
              <h3 style="margin: 0; font-size: 18px;">Header personalizado</h3>
            </div>
          </template>
          <template #default>
            <p>Contenido personalizado usando el slot default.</p>
          </template>
          <template #footer>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span style="font-size: 14px; color: var(--nubi-text-secondary);">Footer personalizado</span>
              <NubiButton variant="primary" size="sm">Ver más</NubiButton>
            </div>
          </template>
        </NubiCard>
      </div>
    </Variant>
  </Story>
</template>

<docs>
## NubiCard

Contenedor de contenido con estructura flexible.

### Características
- Imagen opcional en la parte superior
- Título y descripción
- Slots para header, body, footer y actions
- Variante clickable con hover effect

### Uso
```vue
<NubiCard title="Título" description="Descripción" />

<NubiCard image="/path/to/image" title="Con imagen">
  <template #actions>
    <NubiButton>Aceptar</NubiButton>
  </template>
</NubiCard>
```
</docs>
