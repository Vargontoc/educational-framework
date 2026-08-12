import { ref, computed, type Ref } from 'vue'

export type ComposerTriggerChar = '@' | '/'

interface TriggerMatch {
  trigger: ComposerTriggerChar
  query: string
}

const TRIGGER_PATTERN = /([@/])(\w*)$/

function stripSlash(trigger: string): string {
  return trigger.startsWith('/') ? trigger.slice(1) : trigger
}

/**
 * Detecta el disparador activo (`@` o `/`) en la posición del cursor.
 *
 * El carácter disparador solo se reconoce si es el primero de su token: si el
 * carácter inmediatamente anterior es el mismo disparador (p. ej. "@@" o "//"),
 * se considera texto normal y no se activa ninguna sugerencia.
 */
function detectTrigger(text: string): TriggerMatch | null {
  const match = TRIGGER_PATTERN.exec(text)
  if (!match) return null

  const [, trigger, query] = match
  const precedingChar = text[match.index - 1]
  if (precedingChar === trigger) {
    return null
  }

  return { trigger: trigger as ComposerTriggerChar, query }
}

/**
 * Detección de atajos en el composer del chatbot:
 * - `@` para mencionar perfiles infantiles
 * - `/` para comandos
 */
export function useChatbotComposer(profileNames: Ref<string[]>, commandNames: Ref<string[]>) {
  const draft = ref('')
  const activeTrigger = ref<TriggerMatch | null>(null)

  function onInput(value: string, cursorPos: number): void {
    draft.value = value
    activeTrigger.value = detectTrigger(value.slice(0, cursorPos))
  }

  const mentionQuery = computed(() =>
    activeTrigger.value?.trigger === '@' ? activeTrigger.value.query : null
  )

  const commandQuery = computed(() =>
    activeTrigger.value?.trigger === '/' ? activeTrigger.value.query : null
  )

  const mentionSuggestions = computed(() =>
    mentionQuery.value === null
      ? []
      : profileNames.value.filter(n => n.toLowerCase().startsWith(mentionQuery.value!.toLowerCase()))
  )

  const commandSuggestions = computed(() =>
    commandQuery.value === null
      ? []
      : commandNames.value.filter(c =>
          stripSlash(c).toLowerCase().startsWith(commandQuery.value!.toLowerCase())
        )
  )

  function applyMention(name: string): void {
    draft.value = draft.value.replace(/@(\w*)$/, `@${name} `)
    activeTrigger.value = null
  }

  function applyCommand(trigger: string): void {
    draft.value = draft.value.replace(/\/(\w*)$/, `/${stripSlash(trigger)} `)
    activeTrigger.value = null
  }

  function clear(): void {
    draft.value = ''
    activeTrigger.value = null
  }

  return {
    draft,
    mentionSuggestions,
    commandSuggestions,
    onInput,
    applyMention,
    applyCommand,
    clear
  }
}
