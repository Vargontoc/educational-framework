<script setup lang="ts">
import { onMounted, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useFamilyStore } from '@/stores/useFamilyStore'
import { useSessionStore } from '@/stores/useSessionStore'
import * as childSessionService from '@/services/childSessionService'
import FamilyRegistrationModal from '@/components/home/FamilyRegistrationModal.vue'
import PinModal from '@/components/home/PinModal.vue'
import ChildSelectorModal from '@/components/home/ChildSelectorModal.vue'
import AddChildModal from '@/components/home/AddChildModal.vue'
import type { ChildProfileResponse } from '@/shared/types/api'

const { t } = useI18n()
const router = useRouter()
const familyStore = useFamilyStore()
const sessionStore = useSessionStore()

onMounted(async () => {
  await nextTick()
  familyStore.fetchFamily()
})

function openChildSelector() {
  familyStore.fetchChildren()
  familyStore.setActiveModal('childSelector')
}

function openPin() {
  familyStore.setActiveModal('pin')
}

function handleRetry() {
  familyStore.fetchFamily()
}

async function handleChildSelect(child: ChildProfileResponse) {
  try {
    const session = await childSessionService.openChildSession({ childProfileId: child.id })
    sessionStore.setActiveChildSession(session)
    familyStore.setActiveModal(null)
    router.replace(`/game/${child.id}`)
  } catch {
  }
}
</script>

<template>
  <div class="home">
    <!-- Loading state -->
    <div v-if="familyStore.viewState === 'loading'" class="home__center">
      <img src="@/assets/images/avatar-bot.png" alt="" class="home__avatar home__avatar--loading" />
      <p class="home__lead">{{ t('home.loading') }}</p>
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
        <p class="home__kicker">{{ t('home.noFamilyKicker') }}</p>
        <button class="home__cta" @click="familyStore.setActiveModal('familyRegistration')">
          {{ t('home.welcomeFamily') }}
        </button>
      </div>
    </div>

    <!-- Family ready state -->
    <div v-else-if="familyStore.viewState === 'familyReady'" class="home__center">
      <button
        class="home__settings-icon"
        :aria-label="t('home.settingsAriaLabel')"
        @click="openPin"
      >
        &#9881;
      </button>
      <div class="home__avatar-container">
        <img src="@/assets/images/avatar-bot.png" alt="" class="home__avatar" />
        <p class="home__kicker">{{ t('home.familyReadyKicker') }}</p>
        <button class="home__family-btn" @click="openChildSelector">
          {{ familyStore.family?.name }}
        </button>
      </div>
    </div>

    <!-- Modals -->
    <FamilyRegistrationModal />
    <PinModal />
    <ChildSelectorModal @select="handleChildSelect" />
    <AddChildModal />
  </div>
</template>

<style scoped>
.home {
  position: relative;
  min-height: 100vh;
  min-height: 100dvh;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: auto;
  background:
    radial-gradient(circle at 18% 20%, rgba(255, 255, 255, 0.86) 0 9%, transparent 10%),
    radial-gradient(circle at 75% 24%, rgba(255, 255, 255, 0.72) 0 8%, transparent 9%),
    linear-gradient(180deg, var(--color-sky) 0 67%, var(--color-grass) 68% 100%);
  color: var(--color-text-primary);
}

.home::after {
  content: '';
  position: fixed;
  left: -4vw;
  right: -4vw;
  bottom: 0;
  height: 22vh;
  pointer-events: none;
  background:
    radial-gradient(ellipse at 18% 100%, rgba(92, 145, 70, 0.16) 0 18%, transparent 19%),
    radial-gradient(ellipse at 63% 100%, rgba(92, 145, 70, 0.14) 0 22%, transparent 23%);
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
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-sm);
  padding: var(--space-lg);
  border-radius: 48px;
  background: rgba(255, 255, 255, 0.36);
  box-shadow: 0 24px 80px rgba(26, 35, 64, 0.08);
  backdrop-filter: blur(8px);
}

.home__avatar {
  width: 200px;
  height: 200px;
  object-fit: contain;
  filter: drop-shadow(0 18px 22px rgba(26, 35, 64, 0.16));
}

.home__avatar--loading {
  animation: soft-bounce 1.7s ease-in-out infinite;
}

.home__kicker,
.home__lead {
  max-width: 340px;
  color: var(--color-text-secondary);
  font-size: var(--font-size-body);
  font-weight: 600;
  line-height: 1.4;
}

.home__cta {
  position: relative;
  min-width: 180px;
  min-height: var(--touch-target-child-primary);
  padding: var(--space-md) var(--space-lg);
  border: none;
  border-radius: var(--radius-pill);
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
  font-size: var(--font-size-body);
  font-family: var(--font-family-base);
  font-weight: 700;
  cursor: pointer;
  transition: background-color var(--transition-base);
  box-shadow: 0 4px 0 var(--color-primary-dark), 0 18px 28px rgba(43, 91, 224, 0.22);
}

.home__cta:hover {
  background-color: var(--color-primary-dark);
}

.home__family-btn {
  position: relative;
  min-width: 180px;
  min-height: var(--touch-target-child-primary);
  padding: var(--space-md) var(--space-lg);
  border: none;
  border-radius: var(--radius-pill);
  background-color: var(--color-celebration);
  color: var(--color-text-on-secondary);
  font-size: var(--font-size-body);
  font-family: var(--font-family-base);
  font-weight: 700;
  cursor: pointer;
  transition: background-color var(--transition-base);
  box-shadow: 0 4px 0 var(--color-celebration-dark), 0 18px 28px rgba(245, 166, 35, 0.24);
}

.home__family-btn:hover {
  background-color: var(--color-celebration-dark);
}

.home__settings-icon {
  position: fixed;
  top: var(--space-md);
  left: var(--space-md);
  width: var(--touch-target-min);
  height: var(--touch-target-min);
  border: none;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.78);
  color: var(--color-text-primary);
  font-size: var(--font-size-lg);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color var(--transition-base);
}

.home__settings-icon:hover {
  background-color: var(--color-surface);
}

.home__help-icon {
  position: fixed;
  top: var(--space-md);
  right: var(--space-md);
  width: var(--touch-target-min);
  height: var(--touch-target-min);
  border: none;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.78);
  color: var(--color-primary);
  font-size: var(--font-size-md);
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color var(--transition-base);
}

.home__help-icon:hover {
  background-color: var(--color-surface);
}

.home__error {
  color: var(--color-error-adult);
  font-size: var(--font-size-md);
}

.home__retry-btn {
  min-height: var(--touch-target-min);
  padding: var(--space-sm) var(--space-md);
  border: 2px solid var(--color-primary);
  border-radius: var(--radius-pill);
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

@keyframes soft-bounce {
  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-8px);
  }
}

@media (max-width: 720px) {
  .home__avatar-container {
    padding: var(--space-md);
  }

  .home__avatar {
    width: 150px;
    height: 150px;
  }
}
</style>
