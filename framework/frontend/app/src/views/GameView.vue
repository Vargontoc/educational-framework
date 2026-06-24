<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useSessionStore } from '@/stores/useSessionStore'
import { useAudioStore } from '@/stores/useAudioStore'
import type { GameAvatarEvent } from '@/shared/types/api'

const { t } = useI18n()
const router = useRouter()
const sessionStore = useSessionStore()
const audioStore = useAudioStore()

const childName = ref<string>('')

type GameViewState = 'preparing' | 'ready' | 'error'

const viewState = ref<GameViewState>('preparing')
const connectionState = ref<'connecting' | 'connected' | 'disconnected'>('disconnected')

let ws: WebSocket | null = null
let heartbeatTimer: ReturnType<typeof setInterval> | null = null
const HEARTBEAT_INTERVAL_MS = 30_000

const MAX_BINARY_WAIT_MS = 3_000  // max wait for binary audio frame after JSON event

let pendingAudioId: string | null = null
let audioTimeoutTimer: ReturnType<typeof setTimeout> | null = null
let pendingAvatarEventType: 'SESSION_CONNECTED' | 'SESSION_DISCONNECTED' | null = null

function getChildProfileId(): number | null {
  return sessionStore.activeChildSession?.childProfileId ?? null
}

function getChildName(): string {
  const profileId = getChildProfileId()
  return profileId !== null ? String(profileId) : ''
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
  console.log("Estado WS " + ws?.readyState)
  if (ws && ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify(payload))
    console.log("Enviado evento")
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

function parseBinaryAudioFrame(buffer: ArrayBuffer): { audioId: string; mp3Data: ArrayBuffer } | null {
  if (buffer.byteLength < 4) return null
  const view = new DataView(buffer)
  const audioIdLength = view.getUint32(0, false)
  if (buffer.byteLength < 4 + audioIdLength) return null
  const audioIdBytes = new Uint8Array(buffer, 4, audioIdLength)
  const audioId = new TextDecoder().decode(audioIdBytes)
  const mp3Data = buffer.slice(4 + audioIdLength)
  return { audioId, mp3Data }
}

function handleGameAvatarEvent(event: GameAvatarEvent) {
  pendingAvatarEventType = event.eventType

  if (!event.audioAvailable) {
    doEventGame(event.eventType, false)
    return
  }

  if (event.audioId) {
    pendingAudioId = event.audioId
    const capturedEventType = event.eventType
    audioTimeoutTimer = setTimeout(() => {
      audioTimeoutTimer = null
      pendingAudioId = null
      doEventGame(capturedEventType, false)
    }, MAX_BINARY_WAIT_MS)
  } else {
    doEventGame(event.eventType, false)
  }
}

function onWsOpen() {
  connectionState.value = 'connected'
  sendMessage({ type: 'auth', childSessionId: sessionStore.activeChildSession?.id })
}

function onWsMessage(event: MessageEvent) {
  if (typeof event.data === 'string') {
    try {
      const data = JSON.parse(event.data) as { event: string; payload?: unknown }
      console.log('WS Message: ' + event)
      switch (data.event) {
        case 'AUTH_ACK':
          startHeartbeat()
          break
        case 'HEARTBEAT_ACK':
          break
        case 'GAME_STATE_UPDATE':
          break
        case 'GAME_AVATAR_EVENT': {
          const avatarEvent = data as unknown as GameAvatarEvent
          handleGameAvatarEvent(avatarEvent)
          
          break
        }
        case 'SESSION_EXPIRED':
        case 'SESSION_INVALIDATED':
        case 'CHILD_EXPELLED':
        case 'PARENT_BLOCK':
          //handleTerminalEvent(data.event)
          break
      }
    } catch {
    }
  } else if (event.data instanceof ArrayBuffer) {
    if (pendingAudioId === null) return
    const parsed = parseBinaryAudioFrame(event.data)
    if (parsed === null) return
    if (parsed.audioId !== pendingAudioId) return

    if (audioTimeoutTimer !== null) {
      clearTimeout(audioTimeoutTimer)
      audioTimeoutTimer = null
    }
    pendingAudioId = null
    const capturedType = pendingAvatarEventType
    pendingAvatarEventType = null

    audioStore.playAudio(parsed.mp3Data)
      .then(() => doEventGame(capturedType, true))
      .catch(() => doEventGame(capturedType, false))
  }
}



function doEventGame(eventType: string | null, _hasAudio: boolean) {
  const capturedEventType = eventType
  pendingAvatarEventType = null

  switch (capturedEventType) {
    case 'SESSION_CONNECTED':
      viewState.value = 'ready'
      break
    case 'SESSION_DISCONNECTED':
      revokeSession()
      break
  }
}

function revokeSession() {
  closeWebSocket()
  stopHeartbeat()
  clearChildSessionAndGoHome()
}

function onWsClose() {
  connectionState.value = 'disconnected'
  stopHeartbeat()
}

function onWsError() {
  connectionState.value = 'disconnected'
}

function initWebSocket() {
  const wsUrl = `${import.meta.env.VITE_WS_BASE_URL}/ws/game`
  ws = new WebSocket(wsUrl)
  ws.binaryType = 'arraybuffer'
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
  <main class="game-view" aria-label="World map">
    <div v-if="viewState === 'preparing'" class="game-view__loader" aria-live="polite">
      <div class="game-view__loader-avatar">
        <img src="@/assets/animations/base-idle.png" alt="" class="game-view__avatar-img" />
        <div class="game-view__loader-ring" aria-hidden="true"></div>
      </div>
      <p class="game-view__loader-text">{{ t('game.loaderText') }}</p>
    </div>

    <section v-else class="game-view__world">
      <div class="game-view__cloud game-view__cloud--one" aria-hidden="true"></div>
      <div class="game-view__cloud game-view__cloud--two" aria-hidden="true"></div>

      <div class="game-view__bg-layer" aria-hidden="true">
        <div class="game-view__hills game-view__hills--far"></div>
        <div class="game-view__hills game-view__hills--mid"></div>
      </div>

      <div class="game-view__mg-layer" aria-hidden="true">
        <div class="game-view__grass-strip"></div>
        <div class="game-view__bush game-view__bush--left"></div>
        <div class="game-view__bush game-view__bush--right"></div>
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
  width: 180vw;
  min-height: 100vh;
  min-height: 100dvh;
  overflow: hidden;
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

.game-view__bg-layer {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.game-view__hills {
  position: absolute;
  bottom: 33%;
  left: -10%;
  width: 120%;
  border-radius: 50% 50% 0 0;
}

.game-view__hills--far {
  height: 18%;
  background: linear-gradient(180deg, #b8d9f0 0%, #c8e6a0 100%);
  opacity: 0.35;
}

.game-view__hills--mid {
  height: 12%;
  background: linear-gradient(180deg, #9fcce8 0%, #b8dcc0 100%);
  opacity: 0.5;
  bottom: 31%;
}

.game-view__mg-layer {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.game-view__grass-strip {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 33%;
  background: linear-gradient(180deg, #b8dcc0 0%, var(--color-grass) 30%);
}

.game-view__bush {
  position: absolute;
  bottom: 26%;
  border-radius: 50%;
  background: radial-gradient(circle at 40% 40%, #8fbc8f, #6b9b6b);
}

.game-view__bush--left {
  left: 12%;
  width: 64px;
  height: 48px;
}

.game-view__bush--right {
  right: 22%;
  width: 52px;
  height: 38px;
  background: radial-gradient(circle at 40% 40%, #9dc89d, #7aaa7a);
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

</style>