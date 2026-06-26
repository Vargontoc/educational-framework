<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useSessionStore } from '@/stores/useSessionStore'
import { useAudioStore } from '@/stores/useAudioStore'
import { useAnimationStore } from '@/stores/useAnimationStore'
import type { GameAvatarEvent, WorldDestinationPayload, WorldStateSyncPayload, WorldActivityStartedPayload, WorldDiscoveryElementPayload } from '@/shared/types/api'

const { t } = useI18n()
const router = useRouter()
const sessionStore = useSessionStore()
const audioStore = useAudioStore()
const animationStore = useAnimationStore()

type GameViewState = 'preparing' | 'world_loading' | 'world_active' | 'error'

const viewState = ref<GameViewState>('preparing')
const worldState = ref<WorldDestinationPayload | null>(null)
const hasPlayedErrorAudio = ref(false)

let ws: WebSocket | null = null
let heartbeatTimer: ReturnType<typeof setInterval> | null = null
let worldHeartbeatTimer: ReturnType<typeof setInterval> | null = null
let worldLoadingTimer: ReturnType<typeof setTimeout> | null = null
let errorNavigationTimer: ReturnType<typeof setTimeout> | null = null

const HEARTBEAT_INTERVAL_MS = 30_000
const WORLD_HEARTBEAT_INTERVAL_MS = 30_000
const WORLD_LOADING_TIMEOUT_MS = 10_000
const MAX_BINARY_WAIT_MS = 3_000

let pendingAudioId: string | null = null
let audioTimeoutTimer: ReturnType<typeof setTimeout> | null = null
let pendingAvatarEventType: 'SESSION_CONNECTED' | 'SESSION_DISCONNECTED' | null = null
const sentProposals = new Set<string>()

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

function startWorldHeartbeat() {
  stopWorldHeartbeat()
  worldHeartbeatTimer = setInterval(() => {
    sendMessage({ type: 'world_heartbeat' })
  }, WORLD_HEARTBEAT_INTERVAL_MS)
}

function stopWorldHeartbeat() {
  if (worldHeartbeatTimer !== null) {
    clearInterval(worldHeartbeatTimer)
    worldHeartbeatTimer = null
  }
}

function startWorldLoadingTimer() {
  stopWorldLoadingTimer()
  worldLoadingTimer = setTimeout(() => {
    enterErrorState()
  }, WORLD_LOADING_TIMEOUT_MS)
}

function stopWorldLoadingTimer() {
  if (worldLoadingTimer !== null) {
    clearTimeout(worldLoadingTimer)
    worldLoadingTimer = null
  }
}

function stopErrorNavigationTimer() {
  if (errorNavigationTimer !== null) {
    clearTimeout(errorNavigationTimer)
    errorNavigationTimer = null
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

  if (viewState.value === 'error') {
    if (!hasPlayedErrorAudio.value && event.audioAvailable) {
      hasPlayedErrorAudio.value = true
      if (event.audioId) {
        pendingAudioId = event.audioId
        audioTimeoutTimer = setTimeout(() => {
          pendingAudioId = null
        }, MAX_BINARY_WAIT_MS)
      }
    }
    return
  }

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
  sendMessage({ type: 'auth', childSessionId: sessionStore.activeChildSession?.id })
}

function onWsMessage(event: MessageEvent) {
  if (typeof event.data === 'string') {
    try {
      const data = JSON.parse(event.data) as { event: string; payload?: unknown }
      switch (data.event) {
        case 'AUTH_ACK':
          viewState.value = 'world_loading'
          animationStore.setWaiting()
          startHeartbeat()
          startWorldLoadingTimer()
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
        case 'WORLD_DESTINATION_READY':
          stopWorldLoadingTimer()
          handleWorldDestinationReady(data.payload as WorldDestinationPayload)
          break
        case 'WORLD_STATE_SYNC':
          stopWorldLoadingTimer()
          handleWorldStateSync(data.payload as WorldStateSyncPayload)
          break
        case 'WORLD_ACTIVITY_STARTED':
          handleWorldActivityStarted(data.payload as WorldActivityStartedPayload)
          break
        case 'GAME_COMPLETED':
        case 'GAME_ABANDONED':
          sentProposals.clear()
          break
        case 'SESSION_EXPIRED':
        case 'SESSION_INVALIDATED':
        case 'CHILD_EXPELLED':
        case 'PARENT_BLOCK':
          enterErrorState()
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

    animationStore.setSpeaking()
    audioStore.playAudio(parsed.mp3Data)
      .then(() => {
        if (viewState.value === 'world_active') {
          animationStore.setIdle()
        } else if (viewState.value === 'error') {
          animationStore.setError()
        } else {
          animationStore.setWaiting()
        }
        doEventGame(capturedType, true)
      })
      .catch(() => {
        if (viewState.value === 'world_active') {
          animationStore.setIdle()
        } else if (viewState.value === 'error') {
          animationStore.setError()
        } else {
          animationStore.setWaiting()
        }
        doEventGame(capturedType, false)
      })
  }
}



function doEventGame(eventType: string | null, _hasAudio: boolean) {
  const capturedEventType = eventType
  pendingAvatarEventType = null

  switch (capturedEventType) {
    case 'SESSION_CONNECTED':
      break
    case 'SESSION_DISCONNECTED':
      revokeSession()
      break
  }
}

function revokeSession() {
  stopErrorNavigationTimer()
  closeWebSocket()
  stopHeartbeat()
  stopWorldHeartbeat()
  stopWorldLoadingTimer()
  animationStore.reset()
  clearChildSessionAndGoHome()
}

function enterErrorState(withFallbackHome = false) {
  worldState.value = null
  stopHeartbeat()
  stopWorldHeartbeat()
  stopWorldLoadingTimer()
  stopErrorNavigationTimer()
  viewState.value = 'error'
  hasPlayedErrorAudio.value = false
  animationStore.setError()
  if (withFallbackHome) {
    errorNavigationTimer = setTimeout(() => {
      clearChildSessionAndGoHome()
    }, 4_000)
  }
}

function isValidDestination(dest: WorldDestinationPayload): boolean {
  return !!(
    dest.destinationId &&
    dest.host?.id !== undefined && dest.host?.code && dest.host?.displayName &&
    dest.narrativeSituation?.id !== undefined && dest.narrativeSituation?.code &&
    dest.biome &&
    Array.isArray(dest.discoveryElements)
  )
}

function handleWorldDestinationReady(payload: WorldDestinationPayload) {
  if (isValidDestination(payload)) {
    worldState.value = payload
    stopHeartbeat()
    startWorldHeartbeat()
    viewState.value = 'world_active'
    hasPlayedErrorAudio.value = false
    animationStore.setIdle()
  } else {
    enterErrorState()
  }
}

function handleWorldStateSync(payload: WorldStateSyncPayload) {
  if (payload.status === 'ACTIVE' && payload.destination && isValidDestination(payload.destination)) {
    worldState.value = payload.destination
    stopHeartbeat()
    startWorldHeartbeat()
    viewState.value = 'world_active'
    hasPlayedErrorAudio.value = false
    animationStore.setIdle()
  } else {
    enterErrorState()
  }
}

function handleWorldActivityStarted(_payload: WorldActivityStartedPayload) {
  worldState.value = null
  stopWorldHeartbeat()
  startHeartbeat()
  viewState.value = 'world_loading'
  animationStore.setWaiting()
  startWorldLoadingTimer()
}

function handleDiscoveryClick(element: WorldDiscoveryElementPayload) {
  if (!element.hasActivity || !element.proposalRuntimeId || !element.discoveryElementId) {
    return
  }

  const proposalKey = `${element.proposalRuntimeId}-${element.discoveryElementId}`
  if (sentProposals.has(proposalKey)) {
    return
  }

  if (!ws || ws.readyState !== WebSocket.OPEN) {
    return
  }

  sentProposals.add(proposalKey)
  sendMessage({
    type: 'world_discovery_interacted',
    proposalRuntimeId: element.proposalRuntimeId,
    discoveryElementId: element.discoveryElementId
  })
}

function getDiscoveryStyle(index: number) {
  const baseLeft = 55 + (index % 4) * 8
  const baseBottom = 20 + (index % 3) * 15
  return {
    left: `${baseLeft}vw`,
    bottom: `${baseBottom}%`
  }
}

function getAssetUrl(assetKey: string): string {
  return `/assets/${assetKey}`
}

function getBiomeClass(biome: string | undefined): string {
  switch (biome) {
    case 'forest': return 'game-view--biome-forest'
    case 'beach': return 'game-view--biome-beach'
    case 'mountain': return 'game-view--biome-mountain'
    default: return 'game-view--biome-meadow'
  }
}

function onWsClose() {
  if (viewState.value !== 'error') {
    enterErrorState(true)
  }
}

function onWsError() {}

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
  viewState.value = 'preparing'
  animationStore.setWaiting()
  initWebSocket()
})

onUnmounted(() => {
  closeWebSocket()
  stopHeartbeat()
  stopWorldHeartbeat()
  stopWorldLoadingTimer()
  stopErrorNavigationTimer()
  animationStore.reset()
})
</script>

<template>
  <main class="game-view" :class="getBiomeClass(worldState?.biome)" aria-label="World map">
    <div v-if="viewState === 'preparing' || viewState === 'world_loading'" class="game-view__loader" aria-live="polite">
      <div class="game-view__loader-avatar" :data-animation-state="animationStore.state">
        <img src="@/assets/animations/base-idle.png" alt="" class="game-view__avatar-img" />
        <div class="game-view__loader-ring" aria-hidden="true"></div>
      </div>
      <p class="game-view__loader-text">{{ t('game.loaderText') }}</p>
    </div>

    <section v-else-if="viewState === 'world_active'" class="game-view__world">
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

      <div class="game-view__host" aria-hidden="true">
        <img :data-animation-state="animationStore.state" v-if="worldState?.host.visualAssetKey" :src="getAssetUrl(worldState.host.visualAssetKey)" :alt="worldState.host.displayName" />
        <img v-else :data-animation-state="animationStore.state" src="@/assets/animations/base-idle.png" alt="" />
      </div>

      <div v-for="(element, index) in worldState?.discoveryElements" :key="element.discoveryElementId"
           class="game-view__discovery"
           :class="{
             'game-view__discovery--has-activity': element.hasActivity,
             'game-view__discovery--with-asset': !!element.visualAssetKey
           }"
           :style="getDiscoveryStyle(index)"
           :aria-label="element.displayName"
           tabindex="0"
           @click="handleDiscoveryClick(element)"
           @keydown.enter="handleDiscoveryClick(element)"
           @keydown.space.prevent="handleDiscoveryClick(element)">
        <img v-if="element.visualAssetKey" 
             :src="getAssetUrl(element.visualAssetKey)" 
             :alt="element.displayName"
             class="game-view__discovery-img" />
      </div>

      <div class="game-view__butterfly game-view__butterfly--one" aria-hidden="true"></div>
      <div class="game-view__butterfly game-view__butterfly--two" aria-hidden="true"></div>
      <div class="game-view__flower game-view__flower--one" aria-hidden="true"></div>
      <div class="game-view__flower game-view__flower--two" aria-hidden="true"></div>
      <div class="game-view__flower game-view__flower--three" aria-hidden="true"></div>
    </section>

    <section v-else class="game-view__error" aria-live="polite">
      <div class="game-view__error-avatar">
        <img :data-animation-state="animationStore.state" src="@/assets/animations/base-idle.png" alt="" />
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
  left: 72vw;
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
  left: 70vw;
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

.game-view__host {
  position: absolute;
  left: 18%;
  bottom: 22%;
  width: 140px;
  height: 140px;
}

.game-view__host img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.game-view__discovery {
  position: absolute;
  min-width: 80px;
  min-height: 80px;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  box-shadow: 0 0 0 4px rgba(255, 255, 255, 0.1), 0 0 16px 4px rgba(255, 255, 255, 0.1);
  animation: discovery-pulse 3s ease-in-out infinite;
}

.game-view__discovery--has-activity {
  cursor: pointer;
}

.game-view__discovery:focus-visible {
  outline: 4px solid var(--color-primary);
  outline-offset: 4px;
}

.game-view__discovery--has-activity:focus-visible {
  outline: 4px solid var(--color-celebration, #F5A623);
  outline-offset: 4px;
}

@keyframes discovery-pulse {
  0%, 100% {
    opacity: 0.7;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.05);
  }
}

.game-view__error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  min-height: 100dvh;
}

.game-view__error-avatar {
  width: 160px;
  height: 160px;
}

.game-view__error-avatar img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 50%;
}

.game-view--biome-meadow {
  background: linear-gradient(180deg, var(--color-sky) 0 66%, var(--color-grass) 67% 100%);
}

.game-view--biome-forest {
  background: linear-gradient(180deg, #8cb369 0 60%, #4a7c3f 61% 100%);
}

.game-view--biome-beach {
  background: linear-gradient(180deg, #87ceeb 0 55%, #f4d03f 56% 70%, #c2b280 71% 100%);
}

.game-view--biome-mountain {
  background: linear-gradient(180deg, #b8c4d0 0 50%, #8b9a8b 51% 75%, #6b7b6b 76% 100%);
}

.game-view__discovery--with-asset {
  background: transparent;
  box-shadow: none;
  animation: none;
}

.game-view__discovery-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 50%;
  animation: discovery-pulse 3s ease-in-out infinite;
}

.game-view__discovery--with-asset .game-view__discovery-img {
  box-shadow: 0 0 0 4px rgba(255, 255, 255, 0.1), 0 0 16px 4px rgba(255, 255, 255, 0.1);
}

.game-view__butterfly {
  position: absolute;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  pointer-events: none;
}

.game-view__butterfly--one {
  top: 15%;
  left: 30%;
  background: radial-gradient(circle, #ffb6c1 30%, transparent 70%);
  animation: butterfly-float 8s ease-in-out infinite;
}

.game-view__butterfly--two {
  top: 25%;
  left: 65vw;
  background: radial-gradient(circle, #add8e6 30%, transparent 70%);
  animation: butterfly-float 10s ease-in-out infinite reverse;
}

@keyframes butterfly-float {
  0%, 100% {
    transform: translate(0, 0) rotate(0deg);
  }
  25% {
    transform: translate(15px, -20px) rotate(10deg);
  }
  50% {
    transform: translate(30px, 10px) rotate(-5deg);
  }
  75% {
    transform: translate(10px, 15px) rotate(5deg);
  }
}

.game-view__flower {
  position: absolute;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  pointer-events: none;
  background: radial-gradient(circle, #ff69b4 40%, #228b22 40% 100%);
}

.game-view__flower--one {
  bottom: 8%;
  left: 8%;
}

.game-view__flower--two {
  bottom: 12%;
  left: 72vw;
  transform: scale(0.8);
}

.game-view__flower--three {
  bottom: 5%;
  left: 45%;
  transform: scale(0.6);
}

</style>