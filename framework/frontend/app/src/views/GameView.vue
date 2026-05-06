<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'

const { t } = useI18n()
const route = useRoute()
const childId = route.params.childId
</script>

<template>
  <div>{{ t('game.title') }} — child: {{ childId }}</div>
</template>
