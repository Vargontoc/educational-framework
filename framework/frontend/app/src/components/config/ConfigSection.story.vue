<template>
  <Story title="Config/ConfigSection">
    <Variant title="Default">
      <ConfigSection title="Audio general" description="Controla todo el sonido de la aplicación.">
        <div class="p-4 bg-gray-100 rounded">Contenido de ejemplo</div>
      </ConfigSection>
    </Variant>
    <Variant title="Disabled">
      <ConfigSection title="Audio general" description="Controla todo el sonido de la aplicación." disabled>
        <div class="p-4 bg-gray-100 rounded">Contenido de ejemplo (deshabilitado)</div>
      </ConfigSection>
    </Variant>
    <Variant title="Sin descripción">
      <ConfigSection title="Audio general">
        <div class="p-4 bg-gray-100 rounded">Contenido de ejemplo sin descripción</div>
      </ConfigSection>
    </Variant>
    <Variant title="Con controles">
      <ConfigSection title="Voz del NPC" description="Configura la voz del personaje.">
        <div class="space-y-4">
          <div class="flex items-center justify-between">
            <span>Volumen</span>
            <span class="font-semibold">75%</span>
          </div>
        </div>
      </ConfigSection>
    </Variant>
  </Story>
</template>

<script setup lang="ts">
import ConfigSection from './ConfigSection.vue'
</script>
