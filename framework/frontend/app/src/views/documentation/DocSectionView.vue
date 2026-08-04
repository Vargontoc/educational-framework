<template>
  <article class="doc-section-view">
    <div v-if="sectionContent" class="doc-section-view__content" v-html="renderedContent" />
    <div v-else class="doc-section-view__not-found">
      <p>{{ t('views.docs.notFound') }}</p>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { parseMarkdown } from '../../utils/parseMarkdown'

const route = useRoute()
const { t } = useI18n()

const modules = import.meta.glob('../../content/docs/*.md', { query: '?raw', import: 'default', eager: true }) as Record<string, string>

const sectionContent = computed(() => {
  const section = route.params.section
  if (!section || typeof section !== 'string') {
    return null
  }
  const modulePath = `../../content/docs/${section}.md`
  return modules[modulePath] || null
})

const renderedContent = computed(() => {
  if (!sectionContent.value) return ''
  return parseMarkdown(sectionContent.value)
})
</script>

<style scoped>
.doc-section-view {
  padding: var(--nubi-spacing-lg) var(--nubi-spacing-xl);
  max-width: 720px;
  margin: 0 auto;
}

.doc-section-view__content :deep(h1) {
  font-size: var(--nubi-font-size-2xl);
  font-weight: var(--nubi-font-weight-bold);
  color: var(--nubi-text-primary);
  margin-bottom: var(--nubi-spacing-lg);
  line-height: var(--nubi-line-height-tight);
}

.doc-section-view__content :deep(h2) {
  font-size: var(--nubi-font-size-xl);
  font-weight: var(--nubi-font-weight-semibold);
  color: var(--nubi-text-primary);
  margin-top: var(--nubi-spacing-xl);
  margin-bottom: var(--nubi-spacing-md);
}

.doc-section-view__content :deep(h3) {
  font-size: var(--nubi-font-size-lg);
  font-weight: var(--nubi-font-weight-semibold);
  color: var(--nubi-text-primary);
  margin-top: var(--nubi-spacing-lg);
  margin-bottom: var(--nubi-spacing-sm);
}

.doc-section-view__content :deep(p) {
  font-size: var(--nubi-font-size-base);
  line-height: var(--nubi-line-height-normal);
  color: var(--nubi-text-secondary);
  margin-bottom: var(--nubi-spacing-md);
}

.doc-section-view__content :deep(ul),
.doc-section-view__content :deep(ol) {
  padding-left: var(--nubi-spacing-lg);
  margin-bottom: var(--nubi-spacing-md);
  color: var(--nubi-text-secondary);
}

.doc-section-view__content :deep(li) {
  margin-bottom: var(--nubi-spacing-xs);
  line-height: var(--nubi-line-height-normal);
}

.doc-section-view__content :deep(a) {
  color: var(--nubi-color-primary);
  text-decoration: underline;
}

.doc-section-view__content :deep(a:hover) {
  color: var(--nubi-color-primary-dark);
}

.doc-section-view__not-found {
  padding: var(--nubi-spacing-xl);
  text-align: center;
  color: var(--nubi-text-tertiary);
  font-size: var(--nubi-font-size-lg);
}

@media (max-width: 1023px) {
  .doc-section-view {
    padding: var(--nubi-spacing-md);
  }
}
</style>
