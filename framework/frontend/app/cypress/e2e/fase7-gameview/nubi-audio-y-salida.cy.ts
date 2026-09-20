import { createFamilyViaApi, createChildViaApi, uniqueFamilyName } from '../../support/testData'

const PIN = '1234'
const GAME_TIMEOUT = 20000
const AUDIO_ID = 'round-audio-1'

const ROUND_PROMPT_EVENT = {
  event: 'GAME_AVATAR_EVENT',
  sessionId: 1,
  eventType: 'ROUND_PROMPT',
  audioAvailable: true,
  audioId: AUDIO_ID,
  text: '¿Dónde está la letra A?'
}

const GAME_READY_EVENT = {
  event: 'GAME_READY',
  sessionId: 1,
  payload: {
    engine: 'RECOGNITION',
    activityId: 1,
    gameId: 1,
    difficultyLevelId: 1,
    status: 'IN_PROGRESS',
    recognitionState: {
      recognitionCategory: 'LETTER',
      roundIndex: 0,
      totalRounds: 3,
      hintActive: false,
      targetElementId: 'letter_a',
      optionIds: ['letter_a', 'letter_b'],
      elements: [
        { id: 'letter_a', code: 'letter_a', displayValue: 'A', resourceRefs: { image: 'letter-a' } },
        { id: 'letter_b', code: 'letter_b', displayValue: 'B', resourceRefs: { image: 'letter-b' } }
      ]
    }
  }
}

const state = (win: Cypress.AUTWindow) => (win as any).__NUBI_GAME_STATE__

function openRecognitionScene(childId: number, prefs: { npc: boolean, audio: boolean } = { npc: true, audio: true }) {
  cy.selectChildProfile('Nubi')
  cy.visit(`/game/${childId}`)

  cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
    expect(state(win).activeScene).to.eq('world-map')
  })

  cy.window().then((win) => {
    const s = state(win)
    s.setPreference('npcEnabled', prefs.npc)
    s.setPreference('ttsEnabled', prefs.audio)
    s.setPreference('audioGeneralEnabled', prefs.audio)
    s.spyDynamicAudio()
    s.startRecognitionScene()
  })

  cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
    expect(state(win).activeScene).to.eq('recognition-game')
  })

  cy.window().then((win) => state(win).injectWsEvent(GAME_READY_EVENT))
  cy.wait(500)
}

const played = (win: Cypress.AUTWindow): string[] => (win as any).__NUBI_PLAYED_AUDIO__

describe('RecognitionGameScene — audio de Nubi y boton de salida (SPRINT-078)', () => {
  let childId: number

  before(() => {
    createFamilyViaApi({ name: uniqueFamilyName('F7NubiAudio'), pin: PIN })
    createChildViaApi({ name: 'Nubi', birthday: '2022-03-15', avatar: 'avatar-01' })
      .then((res) => { childId = res.body.data.id })
  })

  it('positivo: ROUND_PROMPT con audio en cache se reproduce automaticamente', () => {
    openRecognitionScene(childId)

    cy.window().then((win) => {
      state(win).seedAudioBuffer(AUDIO_ID)
      state(win).injectWsEvent(ROUND_PROMPT_EVENT)
    })

    cy.window().should((win) => {
      expect(played(win)).to.deep.eq([AUDIO_ID])
    })
  })

  it('positivo: si el frame binario llega despues del evento, se reproduce al recibirlo', () => {
    openRecognitionScene(childId)

    cy.window().then((win) => state(win).injectWsEvent(ROUND_PROMPT_EVENT))
    cy.wait(300)
    cy.window().should((win) => expect(played(win)).to.have.length(0))

    cy.window().then((win) => state(win).seedAudioBuffer(AUDIO_ID))

    cy.window().should((win) => {
      expect(played(win)).to.deep.eq([AUDIO_ID])
    })
  })

  it('positivo: pulsar Nubi repite el audio de la ronda', () => {
    openRecognitionScene(childId)

    cy.window().then((win) => {
      state(win).seedAudioBuffer(AUDIO_ID)
      state(win).injectWsEvent(ROUND_PROMPT_EVENT)
    })
    cy.window().should((win) => expect(played(win)).to.have.length(1))

    cy.window().then((win) => state(win).tapNubi())

    cy.window().should((win) => {
      expect(played(win)).to.deep.eq([AUDIO_ID, AUDIO_ID])
    })
  })

  it('positivo: doble-tap en el boton de salida abandona a WorldMap', () => {
    openRecognitionScene(childId)

    cy.window().then((win) => {
      state(win).tapExitButton()
      state(win).tapExitButton()
    })

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      expect(state(win).activeScene).to.eq('world-map')
    })
  })

  it('negativo: single-tap en Nubi no abandona el minijuego', () => {
    openRecognitionScene(childId)

    cy.window().then((win) => {
      state(win).tapNubi()
      state(win).tapNubi()
    })

    cy.wait(1200)

    cy.window().should((win) => {
      expect(state(win).activeScene).to.eq('recognition-game')
    })
  })

  it('negativo: single-tap en el boton de salida no abandona', () => {
    openRecognitionScene(childId)

    cy.window().then((win) => state(win).tapExitButton())
    cy.wait(1200)

    cy.window().should((win) => {
      expect(state(win).activeScene).to.eq('recognition-game')
    })
  })

  it('negativo: sin audio disponible pulsar Nubi no reproduce nada', () => {
    openRecognitionScene(childId)

    cy.window().then((win) => {
      state(win).injectWsEvent({ ...ROUND_PROMPT_EVENT, audioAvailable: false, audioId: undefined })
      state(win).tapNubi()
    })

    cy.wait(500)

    cy.window().should((win) => {
      expect(played(win)).to.have.length(0)
      expect(state(win).activeScene).to.eq('recognition-game')
    })
  })

  it('negativo: con audio desactivado no se reproduce ni al recibir ni al pulsar Nubi', () => {
    openRecognitionScene(childId, { npc: true, audio: false })

    cy.window().then((win) => {
      state(win).seedAudioBuffer(AUDIO_ID)
      state(win).injectWsEvent(ROUND_PROMPT_EVENT)
      state(win).tapNubi()
    })

    cy.wait(500)

    cy.window().should((win) => {
      expect(played(win)).to.have.length(0)
    })
  })

  it('positivo: la cache de audio se limpia al abandonar el minijuego', () => {
    openRecognitionScene(childId)

    cy.window().then((win) => {
      state(win).seedAudioBuffer(AUDIO_ID)
      state(win).injectWsEvent(ROUND_PROMPT_EVENT)
      expect(state(win).hasAudioBuffer(AUDIO_ID)).to.eq(true)
      state(win).tapExitButton()
      state(win).tapExitButton()
    })

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      expect(state(win).activeScene).to.eq('world-map')
      expect(state(win).hasAudioBuffer(AUDIO_ID)).to.eq(false)
    })
  })
})
