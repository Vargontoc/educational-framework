<template>
  <div class="chatbot-view">
    <NubiBreadcrumb class="chatbot-view__breadcrumb" :items="breadcrumbItems" />

    <h1 class="chatbot-view__title">{{ t('views.chatbot.title') }}</h1>

    <div v-if="loading" class="chatbot-view__skeleton">
      <NubiSkeleton v-for="n in 3" :key="n" variant="rectangle" height="72px" />
    </div>

    <template v-else>
      <div
        class="chatbot-view__box"
        role="log"
        aria-live="polite"
        :aria-label="t('views.chatbot.messagesLabel')"
      >
        <NubiEmptyState
          v-if="wsStore.chatbotMessages.length === 0 && !isWaitingForChatbot"
          icon="message-circle"
          :title="t('views.chatbot.emptyTitle')"
          :description="t('views.chatbot.emptyDescription')"
        />

        <div v-else class="chatbot-view__messages">
          <div
            v-for="(message, index) in wsStore.chatbotMessages"
            :key="index"
            class="chatbot-view__message"
            :class="`chatbot-view__message--${message.role.toLowerCase()}`"
          >
            <span class="chatbot-view__message-role">
              {{ message.role === 'USER' ? t('views.chatbot.roleUser') : t('views.chatbot.roleAssistant') }}
            </span>

            <div v-if="message.toolCalls?.length" class="chatbot-view__tool-calls">
              <NubiBadge
                v-for="(toolCall, tcIndex) in message.toolCalls"
                :key="tcIndex"
                :variant="toolCallVariant(toolCall)"
                :icon="toolCallIcon(toolCall)"
                :label="toolCallLabel(toolCall)"
              />
            </div>

            <p class="chatbot-view__message-content">{{ message.content }}</p>
          </div>

          <div
            v-if="isWaitingForChatbot"
            class="chatbot-view__message chatbot-view__message--assistant chatbot-view__message--live"
          >
            <span class="chatbot-view__message-role">{{ t('views.chatbot.roleAssistant') }}</span>

            <div v-if="wsStore.chatbotStreamToolCalls.length > 0" class="chatbot-view__tool-calls">
              <NubiBadge
                v-for="(toolCall, tcIndex) in wsStore.chatbotStreamToolCalls"
                :key="tcIndex"
                :variant="toolCallVariant(toolCall)"
                :icon="toolCallIcon(toolCall)"
                :label="toolCallLabel(toolCall)"
              />
            </div>

            <p class="chatbot-view__message-content chatbot-view__message-content--live">
              {{ wsStore.chatbotStreamBuffer || t('views.chatbot.streaming') }}
            </p>
          </div>
        </div>
      </div>

      <NubiAlert
        v-if="wsStore.chatbotError"
        type="error"
        :message="wsStore.chatbotError"
        @dismiss="wsStore.chatbotError = null"
      />

      <div class="chatbot-view__composer">
        <ul
          v-if="activeSuggestions.length > 0"
          class="chatbot-view__mentions"
          role="listbox"
          :aria-label="isCommandMode ? t('views.chatbot.commandSuggestionsLabel') : t('views.chatbot.mentionSuggestionsLabel')"
        >
          <li
            v-for="(suggestion, sIndex) in activeSuggestions"
            :key="suggestion"
            role="option"
            :aria-selected="sIndex === highlightedIndex"
            class="chatbot-view__mention"
            :class="{ 'chatbot-view__mention--highlighted': sIndex === highlightedIndex }"
            @mousedown.prevent="selectSuggestion(suggestion)"
          >
            {{ suggestion }}
          </li>
        </ul>

        <div class="chatbot-view__input-row">
          <NubiTextarea
            :model-value="draft"
            :label="t('views.chatbot.inputLabel')"
            :placeholder="t('views.chatbot.inputPlaceholder')"
            :max-length="4000"
            :disabled="isWaitingForChatbot"
            :rows="2"
            @input="handleComposerInput"
            @keydown="handleComposerKeydown"
          >
            <template #suffix>
              <NubiIconButton
                icon="send-horizontal"
                :label="t('views.chatbot.send')"
                size="sm"
                :disabled="sendDisabled"
                @click="handleSend"
              />
            </template>
          </NubiTextarea>
        </div>
      </div>
    </template>

    <ChatbotConversationsModal
      v-model="showConversationsModal"
      :current-conversation-id="wsStore.chatbotConversationId"
      @select="handleSelectConversation"
      @deleted="handleConversationDeleted"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useWSStore } from '../stores/ws'
import { useParentalAuthStore } from '../stores/parentalAuth'
import { useChildProfiles } from '../composables/useChildProfiles'
import { useChatbotComposer } from '../composables/useChatbotComposer'
import { useChatbotStream } from '../composables/useChatbotStream'
import {
  getLastConversation,
  getCommands,
  getConversationById,
  type ConversationMessageResponse
} from '../services/chatbotService'
import type { ChatMessage, ToolCallInfo } from '../types/chatbot'
import NubiBreadcrumb from '../components/base/NubiBreadcrumb.vue'
import NubiSkeleton from '../components/base/NubiSkeleton.vue'
import NubiEmptyState from '../components/base/NubiEmptyState.vue'
import NubiBadge from '../components/base/NubiBadge.vue'
import NubiAlert from '../components/base/NubiAlert.vue'
import NubiTextarea from '../components/base/NubiTextarea.vue'
import NubiIconButton from '../components/base/NubiIconButton.vue'
import ChatbotConversationsModal from '../components/chatbot/ChatbotConversationsModal.vue'

const NEW_CONVERSATION_COMMAND = '/new'
const CONVERSATIONS_COMMAND = '/conversations'

const { t, te } = useI18n()
const wsStore = useWSStore()
const authStore = useParentalAuthStore()
const { profiles, fetchProfiles } = useChildProfiles()

const loading = ref(false)

const breadcrumbItems = computed(() => [
  { label: t('views.panel.title'), to: '/panel' },
  { label: t('views.chatbot.title') }
])

const profileNames = computed(() => profiles.value.map(p => p.name))
const commandTriggers = ref<string[]>([])
const {
  draft,
  mentionSuggestions,
  commandSuggestions,
  onInput,
  applyMention,
  applyCommand,
  clear: clearComposer
} = useChatbotComposer(profileNames, commandTriggers)
const { send, isWaitingForChatbot } = useChatbotStream()

const highlightedIndex = ref(0)
const showConversationsModal = ref(false)

// Sugerencias activas: solo una de las dos listas puede estar poblada a la vez
// (el composer solo reconoce un disparador '@'/'/' a la vez, ver useChatbotComposer)
const activeSuggestions = computed(() =>
  mentionSuggestions.value.length > 0 ? mentionSuggestions.value : commandSuggestions.value
)
const isCommandMode = computed(() => commandSuggestions.value.length > 0)

const sendDisabled = computed(() =>
  !draft.value.trim() || isWaitingForChatbot.value || wsStore.parentChannelStatus !== 'connected'
)

function toolCallVariant(toolCall: ToolCallInfo): 'info' | 'success' | 'error' {
  if (toolCall.status === 'SUCCESS') return 'success'
  if (toolCall.status === 'ERROR') return 'error'
  return 'info'
}

function toolCallIcon(toolCall: ToolCallInfo): string {
  if (toolCall.status === 'SUCCESS') return 'check-circle'
  if (toolCall.status === 'ERROR') return 'alert-circle'
  return 'loader'
}

function toolCallLabel(toolCall: ToolCallInfo): string {
  if (toolCall.status === 'SUCCESS' && toolCall.summary) {
    return toolCall.summary
  }
  const key = `views.chatbot.toolCall.${toolCall.toolName}`
  return te(key) ? t(key) : t('views.chatbot.toolCall.fallback')
}

function handleComposerInput(event: Event): void {
  const target = event.target as HTMLTextAreaElement
  onInput(target.value, target.selectionStart ?? target.value.length)
  highlightedIndex.value = 0
}

function selectSuggestion(value: string): void {
  if (isCommandMode.value) {
    applyCommand(value)
  } else {
    applyMention(value)
  }
  highlightedIndex.value = 0
}

function handleComposerKeydown(event: KeyboardEvent): void {
  if (activeSuggestions.value.length > 0) {
    if (event.key === 'ArrowDown') {
      event.preventDefault()
      highlightedIndex.value = (highlightedIndex.value + 1) % activeSuggestions.value.length
      return
    }
    if (event.key === 'ArrowUp') {
      event.preventDefault()
      highlightedIndex.value =
        (highlightedIndex.value - 1 + activeSuggestions.value.length) % activeSuggestions.value.length
      return
    }
    if (event.key === 'Enter') {
      event.preventDefault()
      selectSuggestion(activeSuggestions.value[highlightedIndex.value])
      return
    }
  }

  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    handleSend()
  }
}

function extractCommandTrigger(text: string): string | null {
  const trimmed = text.trim()
  if (!trimmed.startsWith('/')) return null
  return trimmed.split(/\s+/)[0].toLowerCase()
}

function toChatMessages(messages: ConversationMessageResponse[]): ChatMessage[] {
  return messages.map(m => ({
    role: m.role as 'USER' | 'ASSISTANT',
    content: m.content,
    createdAt: m.createdAt
  }))
}

function handleSend(): void {
  const trigger = extractCommandTrigger(draft.value)

  if (trigger === NEW_CONVERSATION_COMMAND) {
    if (isWaitingForChatbot.value) return
    wsStore.resetChatbotConversation()
    clearComposer()
    highlightedIndex.value = 0
    return
  }

  if (trigger === CONVERSATIONS_COMMAND) {
    showConversationsModal.value = true
    clearComposer()
    highlightedIndex.value = 0
    return
  }

  if (sendDisabled.value) return
  send(draft.value.trim())
  clearComposer()
  highlightedIndex.value = 0
}

async function handleSelectConversation(conversationId: string): Promise<void> {
  const conversation = await getConversationById(conversationId)
  if (!conversation) return
  wsStore.loadChatbotConversation(conversation.conversationId, toChatMessages(conversation.message))
}

function handleConversationDeleted(conversationId: string): void {
  if (wsStore.chatbotConversationId === conversationId) {
    wsStore.resetChatbotConversation()
  }
}

onMounted(async () => {
  if (authStore.familyId !== null) {
    wsStore.subscribeChatbotTopic(authStore.familyId)
  }

  loading.value = true
  const [conversation, commands] = await Promise.all([
    getLastConversation(),
    getCommands(),
    fetchProfiles()
  ])
  commandTriggers.value = commands?.map(c => c.trigger) ?? []
  if (conversation) {
    wsStore.loadChatbotConversation(conversation.conversationId, toChatMessages(conversation.message))
  }
  loading.value = false
})
</script>

<style scoped>
.chatbot-view {
  padding: var(--nubi-spacing-lg);
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-lg);
  height: 100%;
  min-height: 0;
}

.chatbot-view__title {
  font-size: var(--nubi-font-size-xl);
  font-weight: var(--nubi-font-weight-bold);
  color: var(--nubi-text-primary);
  margin: 0;
}

.chatbot-view__skeleton {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-md);
}

.chatbot-view__box {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  border: var(--nubi-border-width) solid var(--nubi-border-default);
  border-radius: var(--nubi-radius-md);
  padding: var(--nubi-spacing-md);
}

.chatbot-view__messages {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-md);
}

.chatbot-view__message {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-xs);
  padding: var(--nubi-spacing-md);
  border-radius: var(--nubi-radius-md);
  background-color: var(--nubi-bg-surface);
  border: var(--nubi-border-width) solid var(--nubi-border-default);
  max-width: 70%;
}

.chatbot-view__message--user {
  align-self: flex-end;
  background-color: var(--nubi-bg-surface-tertiary);
}

.chatbot-view__message--assistant {
  align-self: flex-start;
}

.chatbot-view__message--live {
  opacity: 0.85;
}

.chatbot-view__message-role {
  font-size: var(--nubi-font-size-sm);
  font-weight: var(--nubi-font-weight-semibold);
  color: var(--nubi-text-secondary);
}

.chatbot-view__message-content {
  margin: 0;
  color: var(--nubi-text-primary);
  white-space: pre-wrap;
}

.chatbot-view__message-content--live {
  color: var(--nubi-text-secondary);
  font-style: italic;
}

.chatbot-view__tool-calls {
  display: flex;
  flex-wrap: wrap;
  gap: var(--nubi-spacing-xs);
}

.chatbot-view__composer {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-xs);
}

.chatbot-view__mentions {
  position: absolute;
  bottom: 100%;
  left: 0;
  right: 0;
  margin: 0 0 var(--nubi-spacing-xs) 0;
  padding: var(--nubi-spacing-xs);
  list-style: none;
  background-color: var(--nubi-bg-surface);
  border: var(--nubi-border-width) solid var(--nubi-border-default);
  border-radius: var(--nubi-radius-md);
  box-shadow: var(--nubi-shadow-md);
  max-height: 240px;
  overflow-y: auto;
}

.chatbot-view__mention {
  display: flex;
  align-items: center;
  min-height: 48px;
  padding: 0 var(--nubi-spacing-md);
  border-radius: var(--nubi-radius-sm);
  color: var(--nubi-text-primary);
  cursor: pointer;
}

.chatbot-view__mention--highlighted {
  background-color: var(--nubi-bg-surface-tertiary);
}

@media (max-width: 480px) {
  .chatbot-view__message {
    max-width: 90%;
  }
}

/* Viewports muy bajos (móvil en horizontal): la caja de mensajes es lo
   importante, el resto del cromado debe ceder espacio en vez de empujarla
   a min-height:0 hasta hacerla invisible. */
@media (max-height: 500px) {
  .chatbot-view {
    padding: var(--nubi-spacing-sm) var(--nubi-spacing-md);
    gap: var(--nubi-spacing-xs);
  }

  .chatbot-view__breadcrumb {
    display: none;
  }

  .chatbot-view__title {
    font-size: var(--nubi-font-size-base);
  }

  .chatbot-view__box {
    min-height: 96px;
  }

  .chatbot-view__composer :deep(.nubi-textarea__input) {
    min-height: 36px;
  }
}
</style>
