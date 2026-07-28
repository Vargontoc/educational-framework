<template>
  <NubiInfoModal
    :model-value="modelValue"
    :title="modalTitle"
    @update:model-value="$emit('update:modelValue', $event)"
    @close="$emit('close')"
  >
    <div class="child-selection-modal__content">
      <Transition name="nubi-fade" mode="out-in">
        <div v-if="familyLoading || profilesLoading" key="loading" class="child-selection-modal__state">
          <NubiSpinner size="lg" :label="t('views.home.childSelection.loading')" show-label />
        </div>

        <div v-else-if="familyError || profilesError" key="error" class="child-selection-modal__state">
          <NubiErrorState
            :title="t('views.home.childSelection.errorTitle')"
            :message="familyError || profilesErrorMessage"
            :show-retry="true"
            :retry-label="t('common.retry')"
            @retry="loadData"
          />
        </div>

        <div v-else-if="profiles.length === 0" key="empty" class="child-selection-modal__state">
          <p class="child-selection-modal__empty-text">
            {{ t('views.home.childSelection.noProfiles') }}
          </p>
        </div>

        <div v-else key="content" class="child-selection-modal__grid-wrapper">
          <NubiGrid :items="profiles" :cols="3" :empty-text="t('views.home.childSelection.noProfiles')">
            <template #item="{ item }">
              <ChildProfileCard
                :profile="item"
                :selected="selectedProfileId === item.id"
                @select="handleSelectProfile(item)"
              />
            </template>
          </NubiGrid>
        </div>
      </Transition>
    </div>

    <template #footer>
      <NubiButton variant="secondary" @click="handleRegisterChild">
        {{ t('views.home.childSelection.registerChild') }}
      </NubiButton>
    </template>
  </NubiInfoModal>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import NubiInfoModal from '../base/NubiInfoModal.vue'
import NubiGrid from '../base/NubiGrid.vue'
import NubiButton from '../base/NubiButton.vue'
import NubiSpinner from '../base/NubiSpinner.vue'
import NubiErrorState from '../base/NubiErrorState.vue'
import ChildProfileCard from './ChildProfileCard.vue'
import { getFamily, type ChildProfile } from '../../services/familyService'
import { useChildProfiles } from '../../composables/useChildProfiles'
import { useSessionStore } from '../../stores/session'
import type { ApiError } from '../../services/api'

interface Props {
  modelValue: boolean
}

defineProps<Props>()

defineEmits<{
  'update:modelValue': [value: boolean]
  close: []
}>()

const { t } = useI18n()
const router = useRouter()
const sessionStore = useSessionStore()
const { profiles, loading: profilesLoading, error: profilesError, errorMessage: profilesErrorMessage, fetchProfiles } = useChildProfiles()

const familyName = ref('')
const familyLoading = ref(false)
const familyError = ref('')
const selectedProfileId = ref<number | null>(null)

const modalTitle = computed(() => {
  if (familyName.value) {
    return t('views.home.childSelection.familyTitle', { name: familyName.value })
  }
  return t('views.home.childSelection.familyTitleDefault')
})

async function loadData(): Promise<void> {
  familyLoading.value = true
  familyError.value = ''

  try {
    const family = await getFamily()
    if (family) {
      familyName.value = family.name
    }
  } catch (err) {
    const apiError = err as ApiError
    familyError.value = apiError.message || 'Error al obtener la familia'
  } finally {
    familyLoading.value = false
  }

  await fetchProfiles()
}

function handleSelectProfile(profile: ChildProfile): void {
  selectedProfileId.value = profile.id
  sessionStore.selectChild(String(profile.id))
  router.replace({ name: 'GameView', params: { childId: String(profile.id) } })
}

function handleRegisterChild(): void {
  // SPRINT-013
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.child-selection-modal__content {
  min-height: 200px;
}

.child-selection-modal__state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--nubi-spacing-xl) 0;
}

.child-selection-modal__empty-text {
  font-size: var(--nubi-font-size-base);
  color: var(--nubi-text-secondary);
  text-align: center;
  margin: 0;
  line-height: var(--nubi-line-height-relaxed);
}

.child-selection-modal__grid-wrapper {
  padding: var(--nubi-spacing-sm) 0;
}

.nubi-fade-enter-active {
  transition: opacity var(--nubi-duration-normal) var(--nubi-ease-out);
}

.nubi-fade-leave-active {
  transition: opacity var(--nubi-duration-fast) var(--nubi-ease-in);
}

.nubi-fade-enter-from,
.nubi-fade-leave-to {
  opacity: 0;
}
</style>
