<template>
  <NubiInfoModal
    :model-value="modelValue"
    :title="t('modals.parentalAuth.title')"
    :close-on-overlay="false"
    @update:model-value="handleModalUpdate"
    @close="handleCancel"
  >
    <div class="parental-auth-modal__content">
      <p class="parental-auth-modal__description">
        {{ t('modals.parentalAuth.description') }}
      </p>

      <NubiPinInput
        ref="pinInputRef"
        v-model="pin"
        :masked="true"
        :error="errorMessage"
        :disabled="isLocked"
        @complete="handleComplete"
      />

      <div
        v-if="errorMessage"
        class="parental-auth-modal__error"
        role="alert"
        aria-live="assertive"
      >
        {{ errorMessage }}
      </div>

      <div
        v-if="authStore.isInCooldown"
        class="parental-auth-modal__cooldown"
        role="status"
        aria-live="polite"
      >
        {{ t('modals.parentalAuth.cooldown', { seconds: cooldownRemaining }) }}
      </div>

      <div
        v-if="attemptsShown > 0 && !authStore.isInCooldown"
        class="parental-auth-modal__attempts"
        role="status"
        aria-live="polite"
      >
        {{ t('modals.parentalAuth.attemptsRemaining', { count: MAX_ATTEMPTS - attemptsShown }) }}
      </div>
    </div>

    <template #footer>
      <div class="parental-auth-modal__footer">
        <NubiButton variant="secondary" :disabled="verifying" @click="handleCancel">
          {{ t('common.cancel') }}
        </NubiButton>
        <NubiButton
          variant="primary"
          :disabled="pin.length < 4 || isLocked"
          :loading="verifying"
          @click="handleLogin"
        >
          {{ t('modals.parentalAuth.enter') }}
        </NubiButton>
      </div>
    </template>
  </NubiInfoModal>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onBeforeUnmount } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import NubiInfoModal from '../base/NubiInfoModal.vue'
import NubiPinInput from '../base/NubiPinInput.vue'
import NubiButton from '../base/NubiButton.vue'
import { useParentalAuthStore } from '../../stores/parentalAuth'
import { useParentalSession } from '../../composables/useParentalSession'

const MAX_ATTEMPTS = 3

interface Props {
  modelValue: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  close: []
}>()

const { t } = useI18n()
const router = useRouter()
const authStore = useParentalAuthStore()
const { login } = useParentalSession()

const pin = ref('')
const verifying = ref(false)
const errorKey = ref<'invalid-pin' | 'connection' | 'server' | null>(null)
const cooldownRemaining = ref(0)
const pinInputRef = ref<InstanceType<typeof NubiPinInput> | null>(null)

let cooldownTimer: ReturnType<typeof setInterval> | null = null

const attemptsShown = computed(() => authStore.loginAttempts)
const isLocked = computed(() => authStore.isInCooldown || verifying.value)

const errorMessage = computed(() => {
  if (errorKey.value === 'invalid-pin') return t('modals.parentalAuth.errorInvalid')
  if (errorKey.value === 'connection') return t('modals.parentalAuth.errorConnection')
  if (errorKey.value === 'server') return t('modals.parentalAuth.errorServer')
  return ''
})

function resetState() {
  pin.value = ''
  errorKey.value = null
  verifying.value = false
}

function handleModalUpdate(value: boolean) {
  if (!value) {
    resetState()
    emit('close')
  }
  emit('update:modelValue', value)
}

function handleCancel() {
  resetState()
  emit('update:modelValue', false)
  emit('close')
}

function handleComplete() {
  handleLogin()
}

async function handleLogin() {
  if (pin.value.length < 4 || authStore.isInCooldown) return

  verifying.value = true
  errorKey.value = null

  const result = await login(pin.value)

  verifying.value = false

  if (result.success) {
    resetState()
    emit('update:modelValue', false)
    emit('close')
    router.replace({ name: 'PanelCover' })
  } else {
    errorKey.value = result.errorKey || 'server'
    authStore.incrementAttempts()
    pin.value = ''
    pinInputRef.value?.clear()

    if (authStore.isInCooldown) {
      startCooldownDisplay()
    }
  }
}

function startCooldownDisplay() {
  stopCooldownDisplay()
  const update = () => {
    const remaining = Math.max(0, Math.ceil((authStore.cooldownUntil - Date.now()) / 1000))
    cooldownRemaining.value = remaining
    if (remaining <= 0) {
      stopCooldownDisplay()
    }
  }
  update()
  cooldownTimer = setInterval(update, 1000)
}

function stopCooldownDisplay() {
  if (cooldownTimer !== null) {
    clearInterval(cooldownTimer)
    cooldownTimer = null
  }
  cooldownRemaining.value = 0
}

watch(() => authStore.isInCooldown, (inCooldown) => {
  if (!inCooldown) {
    stopCooldownDisplay()
    errorKey.value = null
  }
})

watch(() => authStore.cooldownUntil, (val) => {
  if (val > Date.now()) {
    startCooldownDisplay()
  }
})

watch(() => props.modelValue, async (isOpen) => {
  if (isOpen) {
    resetState()
    await nextTick()
    pinInputRef.value?.focus()
    if (authStore.isInCooldown) {
      startCooldownDisplay()
    }
  } else {
    stopCooldownDisplay()
  }
})

onBeforeUnmount(() => {
  stopCooldownDisplay()
})
</script>

<style scoped>
.parental-auth-modal__content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--nubi-spacing-md);
  padding: var(--nubi-spacing-md) 0;
}

.parental-auth-modal__description {
  font-size: var(--nubi-font-size-base);
  color: var(--nubi-text-secondary);
  text-align: center;
  margin: 0;
  line-height: var(--nubi-line-height-relaxed);
}

.parental-auth-modal__error {
  font-size: var(--nubi-font-size-sm);
  color: var(--nubi-text-error);
  text-align: center;
  padding: var(--nubi-spacing-xs) 0;
}

.parental-auth-modal__cooldown {
  font-size: var(--nubi-font-size-sm);
  color: var(--nubi-text-error);
  text-align: center;
  font-weight: var(--nubi-font-weight-semibold);
  padding: var(--nubi-spacing-xs) 0;
}

.parental-auth-modal__attempts {
  font-size: var(--nubi-font-size-xs);
  color: var(--nubi-text-secondary);
  text-align: center;
}

.parental-auth-modal__footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--nubi-spacing-sm);
}

@media (max-width: 480px) {
  .parental-auth-modal__footer {
    flex-direction: column;
    gap: var(--nubi-spacing-sm);
  }
}
</style>
