<template>
  <NubiEmptyState class="diary-empty-state" :title="message" icon="smile" />
</template>

<script setup lang="ts">
/**
 * DiaryEmptyState - Estado vacío del diario, con mensaje neutro (no evaluativo).
 */

import NubiEmptyState from '../base/NubiEmptyState.vue'

interface Props {
  message: string
}

defineProps<Props>()
</script>

<style scoped>
.diary-empty-state {
  min-height: 160px;
  padding: var(--nubi-spacing-lg);
}
</style>
