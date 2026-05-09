<script setup lang="ts">
import { onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useFamilyStore } from '@/stores/useFamilyStore'
import FamilyRegistrationModal from '@/components/home/FamilyRegistrationModal.vue'
import PinModal from '@/components/home/PinModal.vue'
import ChildSelectorModal from '@/components/home/ChildSelectorModal.vue'
import AddChildModal from '@/components/home/AddChildModal.vue'

const { t } = useI18n()
const router = useRouter()
const familyStore = useFamilyStore()

onMounted(() => {
  familyStore.fetchFamily()
})

function openChildSelector() {
  familyStore.fetchChildren()
  familyStore.setActiveModal('childSelector')
}

function openPin() {
  familyStore.setActiveModal('pin')
}

function onAuthenticated() {
  router.push('/panel')
}

function handleRetry() {
  familyStore.fetchFamily()
}
</script>

<template>
  <div class="home">
    <!-- Loading state -->
    <div v-if="familyStore.viewState === 'loading'" class="home__center">
      <div class="spinner" aria-hidden="true"></div>
      <p>{{ t('home.loading') }}</p>
    </div>

    <!-- Error state -->
    <div v-else-if="familyStore.viewState === 'error'" class="home__center">
      <p class="home__error">{{ t('home.error') }}</p>
      <button class="home__retry-btn" @click="handleRetry">{{ t('home.retry') }}</button>
    </div>

    <!-- No family state -->
    <div v-else-if="familyStore.viewState === 'noFamily'" class="home__center">
      <div class="home__avatar-container">
        <img src="@/assets/images/avatar-bot.png" alt="" class="home__avatar" />
        <button class="home__cta" @click="familyStore.setActiveModal('familyRegistration')">
          {{ t('home.welcomeFamily') }}
        </button>
      </div>
    </div>

    <!-- Family ready state -->
    <div v-else-if="familyStore.viewState === 'familyReady'" class="home__center">
      <button
        class="home__help-icon"
        :aria-label="t('home.helpAriaLabel')"
        @click.prevent
      >
        ?
      </button>
      <button
        class="home__settings-icon"
        :aria-label="t('home.settingsAriaLabel')"
        @click="openPin"
      >
        &#9881;
      </button>
      <div class="home__avatar-container">
        <img src="@/assets/images/avatar-bot.png" alt="" class="home__avatar" />
        <button class="home__family-btn" @click="openChildSelector">
          {{ familyStore.family?.name }}
        </button>
      </div>
    </div>

    <!-- Modals -->
    <FamilyRegistrationModal />
    <PinModal @authenticated="onAuthenticated" />
    <ChildSelectorModal />
    <AddChildModal />
  </div>
</template>

<style scoped>
.home {
  position: relative;
  min-height: 100dvh;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: auto;
}

.home__center {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-md);
  text-align: center;
  position: relative;
}

.home__avatar-container {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.home__avatar {
  width: 200px;
  height: 200px;
  object-fit: contain;
}

.home__cta {
  position: absolute;
  bottom: var(--space-md);
  min-width: 180px;
  min-height: var(--touch-target-min);
  padding: var(--space-sm) var(--space-md);
  border: none;
  border-radius: var(--radius-md);
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
  font-size: var(--font-size-md);
  font-family: var(--font-family-base);
  font-weight: 700;
  cursor: pointer;
  transition: background-color var(--transition-base);
  box-shadow: 0 4px 12px rgba(124, 58, 237, 0.4);
}

.home__cta:hover {
  background-color: var(--color-primary-dark);
}

.home__family-btn {
  position: absolute;
  bottom: var(--space-md);
  min-width: 180px;
  min-height: var(--touch-target-min);
  padding: var(--space-sm) var(--space-md);
  border: none;
  border-radius: var(--radius-md);
  background-color: var(--color-secondary);
  color: var(--color-text-on-secondary);
  font-size: var(--font-size-md);
  font-family: var(--font-family-base);
  font-weight: 700;
  cursor: pointer;
  transition: background-color var(--transition-base);
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.4);
}

.home__family-btn:hover {
  background-color: var(--color-secondary-dark);
}

.home__settings-icon {
  position: fixed;
  top: var(--space-md);
  right: var(--space-md);
  width: var(--touch-target-min);
  height: var(--touch-target-min);
  border: none;
  border-radius: 50%;
  background-color: rgba(0, 0, 0, 0.05);
  font-size: var(--font-size-lg);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color var(--transition-base);
}

.home__settings-icon:hover {
  background-color: rgba(0, 0, 0, 0.1);
}

.home__help-icon {
  position: fixed;
  top: var(--space-md);
  right: calc(var(--space-md) + var(--touch-target-min) + var(--space-sm));
  width: var(--touch-target-min);
  height: var(--touch-target-min);
  border: none;
  border-radius: 50%;
  background-color: rgba(0, 0, 0, 0.05);
  font-size: var(--font-size-md);
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color var(--transition-base);
}

.home__help-icon:hover {
  background-color: rgba(0, 0, 0, 0.1);
}

.home__error {
  color: #ef4444;
  font-size: var(--font-size-md);
}

.home__retry-btn {
  min-height: var(--touch-target-min);
  padding: var(--space-sm) var(--space-md);
  border: 2px solid var(--color-primary);
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--color-primary);
  font-size: var(--font-size-md);
  font-family: var(--font-family-base);
  font-weight: 700;
  cursor: pointer;
  transition: background-color var(--transition-base), color var(--transition-base);
}

.home__retry-btn:hover {
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid var(--color-neutral);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
