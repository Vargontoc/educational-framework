<template>
  <article class="contact-view">
    <h1 class="contact-view__title">{{ t('views.docs.contact.title') }}</h1>

    <NubiAlert
      type="warning"
      :message="t('views.docs.contact.privacyNotice')"
      :dismissible="false"
    />

    <div class="contact-view__form">
      <NubiSelect
        v-model="messageType"
        :label="t('views.docs.contact.typeLabel')"
        :options="typeOptions"
      />

      <NubiTextarea
        v-model="message"
        :label="t('views.docs.contact.textareaLabel')"
        :placeholder="t('views.docs.contact.textareaPlaceholder')"
        :max-length="2000"
        :error="messageError || undefined"
      />

      <p class="contact-view__purpose">
        {{ t('views.docs.contact.purposeInfo') }}
      </p>

      <NubiCheckbox
        v-model="isAdultConfirmed"
        :label="t('views.docs.contact.adultConfirmation')"
      />

      <NubiButton
        :disabled="!isValid || isSubmitting"
        :loading="isSubmitting"
        @click="handleSubmit"
      >
        {{ isSubmitting ? t('views.docs.contact.sending') : t('views.docs.contact.sendButton') }}
      </NubiButton>

      <NubiAlert
        v-if="submitSuccess"
        type="success"
        :message="t('views.docs.contact.successMessage')"
        :dismissible="true"
        @dismiss="submitSuccess = false"
      />

      <NubiAlert
        v-if="submitError"
        type="error"
        :message="submitError"
        :dismissible="true"
        @dismiss="submitError = null"
      />
    </div>
  </article>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import NubiAlert from '../../components/base/NubiAlert.vue'
import NubiSelect from '../../components/base/NubiSelect.vue'
import NubiTextarea from '../../components/base/NubiTextarea.vue'
import NubiCheckbox from '../../components/base/NubiCheckbox.vue'
import NubiButton from '../../components/base/NubiButton.vue'
import { useContactForm } from '../../composables/useContactForm'
import type { ContactMessageType } from '../../services/contactService'

const { t } = useI18n()

const {
  messageType,
  message,
  isAdultConfirmed,
  isSubmitting,
  submitError,
  submitSuccess,
  isValid,
  messageError,
  submit
} = useContactForm()

const typeOptions: { value: ContactMessageType; label: string }[] = [
  { value: 'COMMENT', label: 'Comentario' },
  { value: 'SUGGEST', label: 'Sugerencia' },
  { value: 'ERROR', label: 'Error' }
]

function handleSubmit() {
  submit()
}
</script>

<style scoped>
.contact-view {
  padding: var(--nubi-spacing-lg) var(--nubi-spacing-xl);
  max-width: 720px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-lg);
}

.contact-view__title {
  font-size: var(--nubi-font-size-2xl);
  font-weight: var(--nubi-font-weight-bold);
  color: var(--nubi-text-primary);
  margin: 0;
  line-height: var(--nubi-line-height-tight);
}

.contact-view__form {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-lg);
}

.contact-view__purpose {
  font-size: var(--nubi-font-size-sm);
  color: var(--nubi-text-secondary);
  line-height: var(--nubi-line-height-normal);
  margin: 0;
  padding: var(--nubi-spacing-md);
  background-color: var(--nubi-bg-surface-secondary);
  border-radius: var(--nubi-radius-md);
}

@media (max-width: 1023px) {
  .contact-view {
    padding: var(--nubi-spacing-md);
  }
}
</style>
