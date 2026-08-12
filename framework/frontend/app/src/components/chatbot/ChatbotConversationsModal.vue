<template>
  <NubiInfoModal
    :model-value="modelValue"
    :title="t('views.chatbot.conversations.title')"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div v-if="loading" class="chatbot-conversations__skeleton">
      <NubiSkeleton v-for="n in 4" :key="n" variant="rectangle" height="64px" />
    </div>

    <NubiEmptyState
      v-else-if="conversations.length === 0"
      icon="message-circle"
      :title="t('views.chatbot.conversations.emptyTitle')"
      :description="t('views.chatbot.conversations.emptyDescription')"
    />

    <ul v-else class="chatbot-conversations__list">
      <li
        v-for="conversation in conversations"
        :key="conversation.conversationId"
        class="chatbot-conversations__item"
        :class="{ 'chatbot-conversations__item--current': conversation.conversationId === currentConversationId }"
      >
        <div class="chatbot-conversations__main">
          <button
            v-if="editingId !== conversation.conversationId"
            type="button"
            class="chatbot-conversations__select"
            @click="handleSelect(conversation.conversationId)"
          >
            <span
              v-if="conversation.conversationId === currentConversationId"
              class="chatbot-conversations__current-dot"
              :title="t('views.chatbot.conversations.current')"
            />
            <span class="chatbot-conversations__text">
              <span class="chatbot-conversations__title">{{ displayTitle(conversation) }}</span>
              <span class="chatbot-conversations__meta">{{ formatDate(conversation.lastMessageAt) }}</span>
            </span>
          </button>

          <div v-else class="chatbot-conversations__edit">
            <NubiTextInput
              v-model="editTitleDraft"
              :label="t('views.chatbot.conversations.editTitleLabel')"
              :max-length="40"
              @keydown.enter.prevent="saveTitle(conversation.conversationId)"
              @keydown.escape.prevent="cancelEdit"
            />
          </div>
        </div>

        <div class="chatbot-conversations__actions">
          <template v-if="editingId === conversation.conversationId">
            <NubiIconButton
              icon="check"
              :label="t('views.chatbot.conversations.save')"
              size="sm"
              :disabled="savingTitle"
              @click="saveTitle(conversation.conversationId)"
            />
            <NubiIconButton
              icon="x"
              :label="t('views.chatbot.conversations.cancel')"
              size="sm"
              :disabled="savingTitle"
              @click="cancelEdit"
            />
          </template>
          <template v-else>
            <NubiIconButton
              icon="pencil"
              :label="t('views.chatbot.conversations.edit')"
              size="sm"
              @click="startEdit(conversation)"
            />
            <NubiIconButton
              icon="trash-2"
              :label="t('views.chatbot.conversations.delete')"
              size="sm"
              @click="confirmDeleteId = conversation.conversationId"
            />
          </template>
        </div>
      </li>
    </ul>
  </NubiInfoModal>

  <NubiConfirmModal
    :model-value="confirmDeleteId !== null"
    :title="t('views.chatbot.conversations.deleteConfirmTitle')"
    :message="t('views.chatbot.conversations.deleteConfirmMessage')"
    confirm-variant="destructive"
    icon="trash-2"
    @update:model-value="(value) => { if (!value) confirmDeleteId = null }"
    @confirm="handleDelete"
    @cancel="confirmDeleteId = null"
  />
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  listConversations,
  updateConversationTitle,
  deleteConversationById,
  type ConversationResponse
} from '../../services/chatbotService'
import { useToast } from '../../composables/useToast'
import NubiInfoModal from '../base/NubiInfoModal.vue'
import NubiConfirmModal from '../base/NubiConfirmModal.vue'
import NubiSkeleton from '../base/NubiSkeleton.vue'
import NubiEmptyState from '../base/NubiEmptyState.vue'
import NubiTextInput from '../base/NubiTextInput.vue'
import NubiIconButton from '../base/NubiIconButton.vue'

interface Props {
  modelValue: boolean
  currentConversationId: string | null
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  select: [conversationId: string]
  deleted: [conversationId: string]
}>()

const { t } = useI18n()
const toast = useToast()

const conversations = ref<ConversationResponse[]>([])
const loading = ref(false)
const editingId = ref<string | null>(null)
const editTitleDraft = ref('')
const savingTitle = ref(false)
const confirmDeleteId = ref<string | null>(null)

watch(() => props.modelValue, async (open) => {
  if (open) {
    loading.value = true
    conversations.value = await listConversations(20)
    loading.value = false
  } else {
    editingId.value = null
    confirmDeleteId.value = null
  }
})

function displayTitle(conversation: ConversationResponse): string {
  if (conversation.title) return conversation.title

  const firstUserMessage = conversation.message.find(m => m.role === 'USER')
  if (firstUserMessage) {
    return firstUserMessage.content.length > 40
      ? `${firstUserMessage.content.slice(0, 40)}…`
      : firstUserMessage.content
  }

  return formatDate(conversation.startedAt)
}

function formatDate(iso: string): string {
  return new Intl.DateTimeFormat('es', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(iso))
}

function handleSelect(conversationId: string): void {
  emit('select', conversationId)
  emit('update:modelValue', false)
}

function startEdit(conversation: ConversationResponse): void {
  editingId.value = conversation.conversationId
  editTitleDraft.value = conversation.title ?? ''
}

function cancelEdit(): void {
  editingId.value = null
  editTitleDraft.value = ''
}

async function saveTitle(conversationId: string): Promise<void> {
  const title = editTitleDraft.value.trim()
  if (!title) {
    cancelEdit()
    return
  }

  savingTitle.value = true
  const updated = await updateConversationTitle(conversationId, title)
  savingTitle.value = false

  if (updated) {
    const index = conversations.value.findIndex(c => c.conversationId === conversationId)
    if (index !== -1) {
      conversations.value[index] = updated
    }
    toast.success(t('views.chatbot.conversations.saveSuccess'))
  } else {
    toast.error(t('views.chatbot.conversations.saveError'))
  }

  cancelEdit()
}

async function handleDelete(): Promise<void> {
  const conversationId = confirmDeleteId.value
  if (!conversationId) return

  const ok = await deleteConversationById(conversationId)
  if (ok) {
    conversations.value = conversations.value.filter(c => c.conversationId !== conversationId)
    emit('deleted', conversationId)
    toast.success(t('views.chatbot.conversations.deleteSuccess'))
  } else {
    toast.error(t('views.chatbot.conversations.deleteError'))
  }

  confirmDeleteId.value = null
}
</script>

<style scoped>
.chatbot-conversations__skeleton {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-sm);
}

.chatbot-conversations__list {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-sm);
  list-style: none;
  margin: 0;
  padding: 0;
}

.chatbot-conversations__item {
  display: flex;
  align-items: center;
  gap: var(--nubi-spacing-sm);
  border: var(--nubi-border-width) solid var(--nubi-border-default);
  border-left: 4px solid transparent;
  border-radius: var(--nubi-radius-md);
  padding: var(--nubi-spacing-xs);
  transition: border-color var(--nubi-duration-fast) var(--nubi-ease-in-out),
              background-color var(--nubi-duration-fast) var(--nubi-ease-in-out);
}

.chatbot-conversations__item--current {
  border-left-color: var(--nubi-color-primary);
  background-color: var(--nubi-color-primary-light);
}

.chatbot-conversations__main {
  flex: 1;
  min-width: 0;
}

.chatbot-conversations__select {
  display: flex;
  align-items: center;
  gap: var(--nubi-spacing-sm);
  width: 100%;
  min-height: 48px;
  padding: var(--nubi-spacing-xs) var(--nubi-spacing-sm);
  border: none;
  background: none;
  border-radius: var(--nubi-radius-sm);
  cursor: pointer;
  text-align: left;
}

.chatbot-conversations__select:hover {
  background-color: var(--nubi-bg-surface-tertiary);
}

.chatbot-conversations__current-dot {
  flex-shrink: 0;
  width: 10px;
  height: 10px;
  border-radius: var(--nubi-radius-full);
  background-color: var(--nubi-color-primary);
}

.chatbot-conversations__text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.chatbot-conversations__title {
  font-size: var(--nubi-font-size-base);
  font-weight: var(--nubi-font-weight-medium);
  color: var(--nubi-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chatbot-conversations__meta {
  font-size: var(--nubi-font-size-xs);
  color: var(--nubi-text-tertiary);
}

.chatbot-conversations__edit {
  padding: 0 var(--nubi-spacing-sm);
}

.chatbot-conversations__actions {
  display: flex;
  gap: var(--nubi-spacing-xs);
  flex-shrink: 0;
}
</style>
