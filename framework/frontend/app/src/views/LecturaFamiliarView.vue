<template>
  <div class="lecture-view">
    <NubiButton style="margin-top: 50%; margin-left: 50%;" >
      Ver Cuentos
    </NubiButton>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import NubiButton from '@/components/base/NubiButton.vue';
const { t } = useI18n()

const breadcrumbItems = computed(() => [
  { label: t('views.panel.title'), to: '/panel' },
  { label: t('views.views.configuracion.title') }
])
</script>

<style lang="css" scoped>
.lecture-view {
  background-image: url("../assets/images/experiencia_familiar.jpg");
  background-size: contain;
  width: 100vh;
  height: 100vw;
}

</style>
