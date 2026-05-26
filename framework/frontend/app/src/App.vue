<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import RotationOverlay from '@/components/RotationOverlay.vue'

const route = useRoute()
const showRotationOverlay = computed(() => route.name !== 'dev-content')
</script>

<template>
  <router-view />
  <RotationOverlay v-if="showRotationOverlay" />
</template>
