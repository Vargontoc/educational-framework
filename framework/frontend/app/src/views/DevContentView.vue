<script setup lang="ts">
import { ref } from 'vue'
import DevSidebar from '@/components/dev-content/DevSidebar.vue'
import DevSection from '@/components/dev-content/DevSection.vue'

const activeSection = ref('categories')

function handleNavigate(section: string) {
  activeSection.value = section
}
</script>

<template>
  <div class="dev-content">
    <DevSidebar
      :active-section="activeSection"
      @navigate="handleNavigate"
    />
    <main class="dev-content__main">
      <DevSection :section-id="activeSection" />
    </main>
  </div>
</template>

<style scoped>
.dev-content {
  display: flex;
  min-height: 100vh;
  min-height: 100dvh;
  background-color: #ffffff;
}

.dev-content__main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: auto;
}

@media (max-width: 768px) {
  .dev-content {
    flex-direction: column;
  }
}
</style>
