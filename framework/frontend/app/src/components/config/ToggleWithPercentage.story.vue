<template>
  <Story title="Config/ToggleWithPercentage">
    <Variant title="Activo al 100%">
      <ToggleWithPercentage
        v-model:enabled="enabled1"
        v-model:percentage="percentage1"
        label="Volumen general"
      />
    </Variant>
    <Variant title="Activo al 50%">
      <ToggleWithPercentage
        v-model:enabled="enabled2"
        v-model:percentage="percentage2"
        label="Volumen general"
      />
    </Variant>
    <Variant title="Desactivado">
      <ToggleWithPercentage
        v-model:enabled="enabled3"
        v-model:percentage="percentage3"
        label="Volumen general"
      />
    </Variant>
    <Variant title="Disabled">
      <ToggleWithPercentage
        v-model:enabled="enabled4"
        v-model:percentage="percentage4"
        label="Volumen general"
        disabled
      />
    </Variant>
    <Variant title="Rango personalizado (0-50)">
      <ToggleWithPercentage
        v-model:enabled="enabled5"
        v-model:percentage="percentage5"
        label="Volumen personalizado"
        :min="0"
        :max="50"
        :step="5"
      />
    </Variant>
    <Variant title="Acción rápida - Probar poner a 0">
      <div class="space-y-4">
        <ToggleWithPercentage
          v-model:enabled="enabled6"
          v-model:percentage="percentage6"
          label="Mueve el slider a 0 para ver la acción rápida"
        />
        <div class="text-sm text-gray-600">
          Estado: {{ enabled6 ? 'Activado' : 'Desactivado' }} - {{ percentage6 }}%
        </div>
      </div>
    </Variant>
  </Story>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import ToggleWithPercentage from './ToggleWithPercentage.vue'

const enabled1 = ref(true)
const percentage1 = ref(100)

const enabled2 = ref(true)
const percentage2 = ref(50)

const enabled3 = ref(false)
const percentage3 = ref(75)

const enabled4 = ref(true)
const percentage4 = ref(100)

const enabled5 = ref(true)
const percentage5 = ref(25)

const enabled6 = ref(true)
const percentage6 = ref(100)
</script>
