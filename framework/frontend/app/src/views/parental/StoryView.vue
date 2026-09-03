<template>
    <div class="story-container">
        <div class="story-content" @touchstart="onTouchStart" @touchend="onTouchEnd">
            <div class="story-nav-slot story-nav-slot--prev">
                <nubi-icon-button
                    v-if="indexPage > 0"
                    icon="step-back"
                    label="Página anterior"
                    size="lg"
                    @click="stepBack()"
                ></nubi-icon-button>
            </div>

            <div class="story-image-wrapper">
                <transition :name="transitionName">
                    <div class="story-page" :key="indexPage">
                        <img v-if="images[indexPage]" class="story-image" :src="images[indexPage]" :alt="storyEntry?.title" />
                        <div v-if="indexPage === 0 && storyEntry?.title" class="story-title-overlay">
                            <span class="story-title">{{ storyEntry.title }}</span>
                        </div>
                    </div>
                </transition>
            </div>

            <div class="story-nav-slot story-nav-slot--next">
                <nubi-icon-button
                    v-if="indexPage < lastPage"
                    icon="step-forward"
                    label="Página siguiente"
                    size="lg"
                    @click="stepForward()"
                ></nubi-icon-button>
            </div>
        </div>

        <div class="story-footer">
            <nubi-icon-button
                class="story-footer__back"
                icon="arrow-left"
                label="Volver al panel"
                @click="goToPanel()"
            ></nubi-icon-button>

            <template v-if="indexPage === 0">
                <div class="story-footer__spacer"></div>
                <nubi-toggle v-model="withVoice" label="Con voz"></nubi-toggle>
            </template>

            <template v-else>
                <span class="story-footer__text">{{ storyEntry?.pages[indexPage - 1]?.text }}</span>
                <nubi-icon-button
                    class="story-footer__speaker"
                    :icon="withVoice ? 'volume-2' : 'volume-x'"
                    :disabled="!withVoice"
                    label="Reproducir audio"
                    @click="playCurrentAudio()"
                ></nubi-icon-button>
            </template>
        </div>

        <audio ref="audioRef" class="story-audio"></audio>
    </div>

</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router';
import NubiIconButton from '@/components/base/NubiIconButton.vue';
import NubiToggle from '@/components/base/NubiToggle.vue';
import { computed, onMounted, ref } from 'vue';
import { getAudio, getCover, getImage, getStory, Story } from '@/services/storyService';

const route = useRoute()
const router = useRouter()
const storyEntry = ref<Story>()
const withVoice = ref<boolean>(false)
const indexPage = ref<number>(0)
const audioRef = ref<HTMLAudioElement>()

const images = ref<string[]>([])
const audios = ref<string[]>([])

const lastPage = computed(() => images.value.length - 1)

const SWIPE_THRESHOLD = 50
const TRANSITION_MS = 300

const direction = ref<'forward' | 'backward'>('forward')
const isAnimating = ref(false)
const transitionName = computed(() => direction.value === 'forward' ? 'slide-forward' : 'slide-backward')

let touchStartX: number | null = null
let touchStartY: number | null = null

function onTouchStart(e: TouchEvent) {
    touchStartX = e.touches[0].clientX
    touchStartY = e.touches[0].clientY
}

function onTouchEnd(e: TouchEvent) {
    if (touchStartX === null || touchStartY === null || isAnimating.value) return

    const dx = e.changedTouches[0].clientX - touchStartX
    const dy = e.changedTouches[0].clientY - touchStartY
    touchStartX = null
    touchStartY = null

    if (Math.abs(dx) < SWIPE_THRESHOLD || Math.abs(dx) < Math.abs(dy)) return

    if (dx < 0) {
        stepForward()
    } else {
        stepBack()
    }
}

function loadResources(id: string, page: number){

    getImage(id, page).then((blob) => {
        if(blob)
            images.value[page] = URL.createObjectURL(blob)
    })

    getAudio(id, page).then((blob) => {
        if(blob)
            audios.value[page] = URL.createObjectURL(blob)
    })
}

function playCurrentAudio() {
    const src = audios.value[indexPage.value]
    if (!src || !audioRef.value) return
    audioRef.value.src = src
    audioRef.value.play()
}

function stepBack() {
    if (isAnimating.value || indexPage.value <= 0) return
    isAnimating.value = true
    direction.value = 'backward'
    indexPage.value -= 1
    if(indexPage.value !== 0 && withVoice.value) {
        setTimeout(() => {
            playCurrentAudio()
        }, 500);
    }
    setTimeout(() => { isAnimating.value = false }, TRANSITION_MS)
}

function stepForward() {
    if (isAnimating.value || indexPage.value >= lastPage.value) return
    isAnimating.value = true
    direction.value = 'forward'
    indexPage.value += 1
    if(indexPage.value !== 0 && withVoice.value) {
        setTimeout(() => {
            playCurrentAudio()
        }, 500);
    }
    setTimeout(() => { isAnimating.value = false }, TRANSITION_MS)
}

function goToPanel() {
    router.push({ name: 'PanelLecturaFamiliar' })
}

onMounted(() => {
    const id = route.params['id']
    if(id) {
        getStory(id as string).then((s) => {
            if(s) storyEntry.value = s

            // Página 0 = portada
            getCover(id as string).then((blob) => {
                if(blob) {
                    images.value[0] = URL.createObjectURL(blob)
                }
            })

            if(storyEntry.value) {
                storyEntry.value.pages.forEach((p) => {
                    loadResources(id as string, p.page);
                })
            }
        })
    }
})

</script>

<style lang="css" scoped>
.story-container {
    display: flex;
    flex-direction: column;
    width: 100dvw;
    height: 100dvh;
    overflow: hidden;
    background-color: var(--nubi-bg-surface-primary, #fff);
    box-sizing: border-box;
}

.story-content {
    flex: 1 1 auto;
    min-height: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: var(--nubi-spacing-md);
    padding: var(--nubi-spacing-md);
    overflow: hidden;
    touch-action: pan-y;
}

.story-nav-slot {
    flex: 0 0 auto;
    width: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.story-image-wrapper {
    position: relative;
    flex: 1 1 auto;
    height: 100%;
    min-width: 0;
    overflow: hidden;
}

.story-page {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
}

.slide-forward-enter-active,
.slide-forward-leave-active,
.slide-backward-enter-active,
.slide-backward-leave-active {
    transition: transform var(--nubi-duration-normal, 300ms) var(--nubi-ease-in-out, ease),
                opacity var(--nubi-duration-normal, 300ms) var(--nubi-ease-in-out, ease);
}

.slide-forward-enter-from {
    transform: translateX(60px);
    opacity: 0;
}

.slide-forward-leave-to {
    transform: translateX(-60px);
    opacity: 0;
}

.slide-backward-enter-from {
    transform: translateX(-60px);
    opacity: 0;
}

.slide-backward-leave-to {
    transform: translateX(60px);
    opacity: 0;
}

.story-image {
    max-width: 100%;
    max-height: 100%;
    object-fit: contain;
    border-radius: var(--nubi-radius-lg);
    box-shadow: var(--nubi-shadow-lg);
}

.story-title-overlay {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    justify-content: center;
    padding: var(--nubi-spacing-xl) var(--nubi-spacing-md) var(--nubi-spacing-md);
    background: linear-gradient(to top, rgba(0, 0, 0, 0.7), rgba(0, 0, 0, 0));
    border-radius: 0 0 var(--nubi-radius-lg) var(--nubi-radius-lg);
    pointer-events: none;
}

.story-title {
    color: #fff;
    font-family: var(--nubi-font-family-base);
    font-size: clamp(1.25rem, 4vw, 2rem);
    font-weight: 800;
    text-align: center;
    text-shadow: 0 2px 6px rgba(0, 0, 0, 0.5);
    letter-spacing: 0.02em;
}

.story-footer {
    flex: 0 0 auto;
    display: flex;
    align-items: center;
    gap: var(--nubi-spacing-md);
    padding: var(--nubi-spacing-sm) var(--nubi-spacing-md);
    min-height: 72px;
    box-sizing: border-box;
}

.story-footer__spacer {
    flex: 1 1 auto;
}

.story-footer__text {
    flex: 1 1 auto;
    min-width: 0;
    white-space: normal;
    word-break: break-word;
    color: var(--nubi-text-primary);
    font-size: var(--nubi-font-size-base);
}

.story-footer__back,
.story-footer__speaker {
    flex: 0 0 auto;
}

.story-audio {
    display: none;
}
</style>
