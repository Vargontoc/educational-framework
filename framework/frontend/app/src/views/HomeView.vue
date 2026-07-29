<template>
  <div class="home-view">
    <!-- Fondo infantil (placeholder hasta que producto lo proporcione) -->
    <div class="home-view__background" aria-hidden="true"></div>

    <!-- Cabecera con accesos superiores -->
    <HomeHeader :has-family="hasFamily" @open-parental-auth="showParentalAuthModal = true" />

    <!-- Estado de carga -->
    <div v-if="loading" class="home-view__loading" role="status" :aria-label="t('common.loading')">
      <NubiSpinner size="lg" />
      <p class="home-view__loading-text">{{ t('common.loading') }}</p>
    </div>

    <!-- Estado de error -->
    <div v-else-if="error" class="home-view__error" role="alert">
      <NubiIcon name="alert-circle" :size="48" color="var(--nubi-color-error)" />
      <p class="home-view__error-text">{{ t('views.home.errorLoading') }}</p>
      <NubiButton variant="secondary" @click="fetchFamilyStatus">
        {{ t('common.retry') }}
      </NubiButton>
    </div>

    <!-- Contenido principal: avatar de Nubi con accion superpuesta -->
    <main v-else class="home-view__content">
      <div class="home-view__avatar-container">
        <img
          src="../assets/images/avatar-bot.webp"
          :alt="t('views.home.nubiAvatar')"
          class="home-view__avatar"
        />
        <HomeAction
          :has-family="hasFamily"
          :family-name="truncatedName"
          @activate="handleActionActivate"
        />
      </div>
    </main>

    <!-- Modal de registro familiar -->
    <FamilyRegistrationModal
      v-model="showFamilyRegistrationModal"
      @close="showFamilyRegistrationModal = false"
      @family-created="handleFamilyCreated"
    />

    <!-- Modal de seleccion de ninos -->
    <ChildSelectionModal
      v-model="showChildSelectionModal"
      @close="showChildSelectionModal = false"
    />

    <!-- Modal de autenticacion parental -->
    <ParentalAuthModal
      v-model="showParentalAuthModal"
      @close="showParentalAuthModal = false"
    />
  </div>
</template>

<script setup lang="ts">
/**
 * HomeView - Vista principal de My Friend Nubi
 * 
 * Segun FEAT-002 y SPRINT-008:
 * - Punto de entrada de la aplicacion
 * - Muestra avatar de Nubi centrado con accion principal superpuesta
 * - Logica condicional basada en estado de familia:
 *   - Sin familia: "Registrar familia" → FamilyRegistrationModal
 *   - Con familia: "Bienvenida familia <nombre>" → ChildSelectionModal
 * - Accesos superiores: documentacion (siempre) y configuracion (solo con familia)
 * - Truncamiento de nombre de familia a 50 caracteres
 * - Manejo de estados: loading, error, sin familia, con familia
 * - Responsive en movil y tablet
 * - Accesibilidad tactil 48x48dp minimo
 * - i18n completo
 */

import { ref, onMounted, defineAsyncComponent } from 'vue'
import { useI18n } from 'vue-i18n'
import { useFamilyStatus } from '../composables/useFamilyStatus'
import HomeHeader from '../components/home/HomeHeader.vue'
import HomeAction from '../components/home/HomeAction.vue'
import NubiSpinner from '../components/base/NubiSpinner.vue'
import NubiButton from '../components/base/NubiButton.vue'
import NubiIcon from '../components/base/NubiIcon.vue'

const FamilyRegistrationModal = defineAsyncComponent({
  loader: () => import('../components/home/FamilyRegistrationModal.vue'),
  loadingComponent: NubiSpinner,
  delay: 200
})

const ChildSelectionModal = defineAsyncComponent({
  loader: () => import('../components/home/ChildSelectionModal.vue'),
  loadingComponent: NubiSpinner,
  delay: 200
})

const ParentalAuthModal = defineAsyncComponent({
  loader: () => import('../components/home/ParentalAuthModal.vue'),
  loadingComponent: NubiSpinner,
  delay: 200
})

const { t } = useI18n()
const { loading, error, hasFamily, truncatedName, fetchFamilyStatus } = useFamilyStatus()

// Estado de modales
const showFamilyRegistrationModal = ref(false)
const showChildSelectionModal = ref(false)
const showParentalAuthModal = ref(false)

/**
 * Carga el estado de la familia al montar la vista
 */
onMounted(() => {
  fetchFamilyStatus()
})

/**
 * Maneja la activacion de la accion principal
 * - Sin familia: abre modal de registro familiar
 * - Con familia: abre modal de seleccion de ninos
 */
function handleActionActivate() {
  if (!hasFamily.value) {
    showFamilyRegistrationModal.value = true
  } else {
    showChildSelectionModal.value = true
  }
}

function handleFamilyCreated() {
  fetchFamilyStatus()
}
</script>

<style scoped>
.home-view {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: all 0.3s ease;
}

/* Fondo infantil placeholder */
.home-view__background {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    135deg,
    var(--nubi-color-primary-light) 0%,
    var(--nubi-color-secondary-light) 50%,
    var(--nubi-color-accent-light) 100%
  );
  opacity: 0.3;
  z-index: 0;
}

/* Estado de carga */
.home-view__loading {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--nubi-spacing-md);
  padding: var(--nubi-spacing-2xl);
}

.home-view__loading-text {
  font-size: var(--nubi-font-size-base);
  color: var(--nubi-text-secondary);
  margin: 0;
}

/* Estado de error */
.home-view__error {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--nubi-spacing-md);
  padding: var(--nubi-spacing-2xl);
  text-align: center;
}

.home-view__error-text {
  font-size: var(--nubi-font-size-base);
  color: var(--nubi-text-secondary);
  margin: 0;
  max-width: 300px;
}

/* Contenido principal */
.home-view__content {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
  width: 100%;
  padding: var(--nubi-spacing-3xl) var(--nubi-spacing-md) var(--nubi-spacing-2xl);
  transition: padding 0.3s ease;
}

/* Contenedor del avatar con accion superpuesta */
.home-view__avatar-container {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: gap 0.3s ease, flex-direction 0.3s ease;
}

.home-view__avatar {
  width: 200px;
  height: 200px;
  object-fit: contain;
  border-radius: var(--nubi-radius-full);
  box-shadow: var(--nubi-shadow-xl);
  transition: width 0.3s ease, height 0.3s ease;
}

/* Portrait: reacomodo de elementos */
@media (orientation: portrait) {
  .home-view {
    justify-content: flex-start;
    padding-top: var(--nubi-spacing-3xl);
  }
  
  .home-view__avatar {
    width: 160px;
    height: 160px;
  }
  
  .home-view__avatar-container {
    flex-direction: column;
    gap: var(--nubi-spacing-lg);
    width: 100%;
    align-items: center;
  }
  
  .home-view__content {
    padding-top: var(--nubi-spacing-4xl);
    justify-content: center;
    align-items: center;
  }
}

/* Responsive: movil */
@media (max-width: 640px) {
  .home-view__avatar {
    width: 160px;
    height: 160px;
  }
}

/* Responsive: tablet */
@media (min-width: 641px) and (max-width: 1024px) {
  .home-view__avatar {
    width: 180px;
    height: 180px;
  }
}

/* Responsive: desktop */
@media (min-width: 1025px) {
  .home-view__avatar {
    width: 240px;
    height: 240px;
  }
}
</style>
