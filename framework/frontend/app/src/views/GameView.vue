<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { useSessionStore } from '@/stores/useSessionStore'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const sessionStore = useSessionStore()

const childId = computed(() => String(route.params.childId))
const childName = ref<string>('')

type GameViewState = 'preparing' | 'ready' | 'error'

const viewState = ref<GameViewState>('preparing')
const connectionState = ref<'connecting' | 'connected' | 'disconnected'>('disconnected')

let ws: WebSocket | null = null
let heartbeatTimer: ReturnType<typeof setInterval> | null = null
const HEARTBEAT_INTERVAL_MS = 30_000

function getChildName(): string {
  const child = sessionStore.activeChildSession
  if (child) {
    return String(child.childProfileId)
  }
  return childId.value
}

function startHeartbeat() {
  stopHeartbeat()
  heartbeatTimer = setInterval(() => {
    sendMessage({ type: 'heartbeat' })
  }, HEARTBEAT_INTERVAL_MS)
}

function stopHeartbeat() {
  if (heartbeatTimer !== null) {
    clearInterval(heartbeatTimer)
    heartbeatTimer = null
  }
}

function sendMessage(payload: object) {
  if (ws && ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify(payload))
  }
}

function closeWebSocket() {
  if (ws) {
    ws.close()
    ws = null
  }
}

function clearChildSessionAndGoHome() {
  sessionStore.clearActiveChildSession()
  router.replace('/')
}

function handleTerminalEvent(_event: string) {
  closeWebSocket()
  stopHeartbeat()
  clearChildSessionAndGoHome()
}

function onWsOpen() {
  connectionState.value = 'connected'
  sendMessage({ type: 'auth', childSessionId: sessionStore.activeChildSession?.id })
}

function onWsMessage(event: MessageEvent) {
  try {
    const data = JSON.parse(event.data) as { event: string; payload?: unknown }
    switch (data.event) {
      case 'AUTH_ACK':
        startHeartbeat()
        viewState.value = 'ready'
        break
      case 'HEARTBEAT_ACK':
        break
      case 'GAME_STATE_UPDATE':
        break
      case 'SESSION_EXPIRED':
      case 'SESSION_INVALIDATED':
      case 'CHILD_EXPELLED':
      case 'PARENT_BLOCK':
        handleTerminalEvent(data.event)
        break
    }
  } catch {
  }
}

function onWsClose() {
  connectionState.value = 'disconnected'
  stopHeartbeat()
}

function onWsError() {
  connectionState.value = 'disconnected'
}

function initWebSocket() {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const wsUrl = `${protocol}//${window.location.host}/ws/game`
  ws = new WebSocket(wsUrl)
  ws.onopen = onWsOpen
  ws.onmessage = onWsMessage
  ws.onclose = onWsClose
  ws.onerror = onWsError
}

onMounted(() => {
  if (!sessionStore.activeChildSession) {
    router.replace('/')
    return
  }
  childName.value = getChildName()
  viewState.value = 'preparing'
  initWebSocket()
})

onUnmounted(() => {
  closeWebSocket()
  stopHeartbeat()
})
</script>

<template>
  <main class="game-view" aria-labelledby="game-title">
    <div v-if="viewState === 'preparing'" class="game-view__loader" aria-live="polite">
      <div class="game-view__loader-avatar">
        <img src="@/assets/animations/base-idle.png" alt="" class="game-view__avatar-img" />
        <div class="game-view__loader-ring" aria-hidden="true"></div>
      </div>
      <p class="game-view__loader-text">{{ t('game.loaderText') }}</p>
    </div>

    <section v-else class="game-view__world" aria-label="World map">
      <div class="game-view__cloud game-view__cloud--one" aria-hidden="true"></div>
      <div class="game-view__cloud game-view__cloud--two" aria-hidden="true"></div>
      <div class="game-view__path" aria-hidden="true">
        <span class="game-view__node game-view__node--done"></span>
        <span class="game-view__node game-view__node--available"></span>
        <span class="game-view__node game-view__node--locked"></span>
      </div>
      <div class="game-view__card">
        <p class="game-view__eyebrow">{{ t('game.mapEyebrow') }}</p>
        <h1 id="game-title">{{ t('game.title') }}</h1>
      </div>
    </section>
  </main>
</template>

<style scoped>
.game-view {
  position: fixed;
  inset: 0;
  min-height: 100vh;
  min-height: 100dvh;
  background: linear-gradient(180deg, var(--color-sky) 0 66%, var(--color-grass) 67% 100%);
  overflow: hidden;
}

.game-view__loader {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  min-height: 100dvh;
  gap: var(--space-lg);
}

.game-view__loader-avatar {
  position: relative;
  width: 160px;
  height: 160px;
}

.game-view__avatar-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 50%;
}

.game-view__loader-ring {
  position: absolute;
  inset: -12px;
  border-radius: 50%;
  border: 6px solid rgba(255, 255, 255, 0.6);
  border-top-color: var(--color-primary);
  animation: spin 1.2s linear infinite;
}

.game-view__loader-text {
  color: var(--color-text-secondary);
  font-size: var(--font-size-md);
  font-weight: 600;
}

.game-view__world {
  position: relative;
  display: grid;
  min-height: 100vh;
  min-height: 100dvh;
  place-items: center;
  padding: var(--space-lg);
}

.game-view__cloud {
  position: absolute;
  width: 150px;
  height: 54px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.86);
  animation: drift 28s ease-in-out infinite alternate;
}

.game-view__cloud::before,
.game-view__cloud::after {
  content: '';
  position: absolute;
  bottom: 18px;
  border-radius: 50%;
  background: inherit;
}

.game-view__cloud::before {
  left: 22px;
  width: 62px;
  height: 62px;
}

.game-view__cloud::after {
  right: 28px;
  width: 78px;
  height: 78px;
}

.game-view__cloud--one {
  top: 12%;
  left: 8%;
}

.game-view__cloud--two {
  top: 20%;
  right: 10%;
  transform: scale(0.74);
  animation-duration: 34s;
}

.game-view__path {
  position: absolute;
  right: 10vw;
  bottom: 16vh;
  display: flex;
  align-items: center;
  gap: var(--space-lg);
}

.game-view__node {
  width: 82px;
  height: 82px;
  border-radius: 50%;
  border: 8px solid rgba(255, 255, 255, 0.72);
  box-shadow: 0 14px 30px rgba(26, 35, 64, 0.12);
}

.game-view__node--done {
  background: var(--color-success-child);
}

.game-view__node--available {
  min-width: var(--touch-target-child-primary);
  min-height: var(--touch-target-child-primary);
  background: var(--color-celebration);
  animation: call-attention 2s ease-in-out infinite;
}

.game-view__node--locked {
  background: var(--color-disabled);
  opacity: 0.5;
}

.game-view__card {
  position: relative;
  z-index: 1;
  max-width: 560px;
  padding: var(--space-lg);
  border-radius: 36px;
  background: rgba(255, 255, 255, 0.74);
  box-shadow: 0 22px 70px rgba(26, 35, 64, 0.12);
  text-align: center;
}

.game-view__card h1 {
  margin-bottom: var(--space-sm);
  font-size: var(--font-size-game-instruction);
  font-weight: 800;
  line-height: 1.5;
}

.game-view__eyebrow {
  color: var(--color-primary) !important;
  font-size: var(--font-size-button) !important;
  font-weight: 800;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@keyframes drift {
  to {
    transform: translateX(42px);
  }
}

@keyframes call-attention {
  0%,
  100% {
    transform: scale(1);
    box-shadow: 0 14px 30px rgba(26, 35, 64, 0.12), 0 0 0 0 rgba(245, 166, 35, 0.34);
  }

  50% {
    transform: scale(1.03);
    box-shadow: 0 14px 30px rgba(26, 35, 64, 0.12), 0 0 0 16px rgba(245, 166, 35, 0);
  }
}
</style>