import { createFamilyViaApi, createChildViaApi, uniqueFamilyName } from '../../support/testData'

const PIN = '1234'
const GAME_TIMEOUT = 20000

const COLOR_ELEMENTS = [
  { id: 'color_red', code: 'color_red', displayValue: 'Red', resourceRefs: { image: 'color-red' } },
  { id: 'color_blue', code: 'color_blue', displayValue: 'Blue', resourceRefs: { image: 'color-blue' } },
  { id: 'color_green', code: 'color_green', displayValue: 'Green', resourceRefs: { image: 'color-green' } }
]

const LETTER_ELEMENTS = [
  { id: 'letter_a', code: 'letter_a', displayValue: 'A', resourceRefs: { image: 'letter-a' } },
  { id: 'letter_b', code: 'letter_b', displayValue: 'B', resourceRefs: { image: 'letter-b' } },
  { id: 'letter_c', code: 'letter_c', displayValue: 'C', resourceRefs: { image: 'letter-c' } }
]

function makeGameStartedEvent(category: string, nonChromaticKeyRequired: boolean) {
  return {
    event: 'GAME_STARTED',
    sessionId: 1,
    payload: {
      engine: 'RECOGNITION',
      activityId: 1,
      gameId: 1,
      difficultyLevelId: 1,
      status: 'IN_PROGRESS',
      recognitionState: {
        recognitionCategory: category,
        roundIndex: 0,
        totalRounds: 3,
        hintActive: false,
        guideChromEnabled: false,
        nonChromaticKeyRequired,
        targetElementId: category === 'COLOR' ? 'color_red' : 'letter_a',
        optionIds: [],
        elements: []
      }
    }
  }
}

function makeGameReadyEvent(category: string, elements: any[], targetElementId: string, nonChromaticKeyRequired: boolean) {
  return {
    event: 'GAME_READY',
    sessionId: 1,
    payload: {
      engine: 'RECOGNITION',
      activityId: 1,
      gameId: 1,
      difficultyLevelId: 1,
      status: 'IN_PROGRESS',
      recognitionState: {
        recognitionCategory: category,
        roundIndex: 0,
        totalRounds: 3,
        hintActive: false,
        guideChromEnabled: false,
        nonChromaticKeyRequired,
        touchEnableDelayMs: 0,
        targetElementId,
        optionIds: elements.filter((e: any) => e.id !== targetElementId).map((e: any) => e.id),
        elements
      }
    }
  }
}

function makeActionResult(
  resultType: string,
  gameCompleted: boolean,
  roundIndex: number,
  totalRounds: number,
  category: string,
  elements: any[],
  nonChromaticKeyRequired: boolean
) {
  return {
    event: 'GAME_ACTION_RESULT',
    sessionId: 1,
    payload: {
      resultType,
      gameCompleted,
      difficultyChanged: false,
      newDifficultyLevelId: 1,
      attemptContext: '',
      updatedState: {
        engine: 'RECOGNITION',
        activityId: 1,
        gameId: 1,
        difficultyLevelId: 1,
        status: gameCompleted ? 'COMPLETED' : 'IN_PROGRESS',
        recognitionState: {
          recognitionCategory: category,
          roundIndex,
          totalRounds,
          hintActive: false,
          guideChromEnabled: false,
          nonChromaticKeyRequired,
          touchEnableDelayMs: 0,
          targetElementId: roundIndex < totalRounds ? elements[0].id : '',
          optionIds: roundIndex < totalRounds ? elements.filter((e: any) => e.id !== elements[0].id).map((e: any) => e.id) : [],
          elements: roundIndex < totalRounds ? elements : []
        }
      }
    }
  }
}

describe('RecognitionGameScene — color accessibility patterns (SPRINT-075)', () => {
  let childId: number

  before(() => {
    createFamilyViaApi({ name: uniqueFamilyName('F7Color'), pin: PIN })
    createChildViaApi({ name: 'Nubi', birthday: '2022-03-15', avatar: 'avatar-01' })
      .then((res) => { childId = res.body.data.id })
  })

  beforeEach(() => {
    cy.intercept('POST', '**/api/v1/sessions/children').as('openSession')
  })

  it('con nonChromaticKeyRequired=true y categoria COLOR, se muestran patrones', () => {
    cy.selectChildProfile('Nubi')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(makeGameStartedEvent('COLOR', true))
      state.injectWsEvent(makeGameReadyEvent('COLOR', COLOR_ELEMENTS, 'color_red', true))
    })

    cy.wait(1000)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      const data = state.getSceneData()
      expect(data).to.not.be.null
      expect(data.nonChromaticKeyRequired).to.be.true
      expect(data.recognitionCategory).to.eq('COLOR')
      expect(data.nonChromaticPatternGraphicsExists).to.be.true
    })
  })

  it('con nonChromaticKeyRequired=false, no se muestran patrones', () => {
    cy.selectChildProfile('Nubi')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(makeGameStartedEvent('COLOR', false))
      state.injectWsEvent(makeGameReadyEvent('COLOR', COLOR_ELEMENTS, 'color_red', false))
    })

    cy.wait(1000)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      const data = state.getSceneData()
      expect(data).to.not.be.null
      expect(data.nonChromaticKeyRequired).to.be.false
      expect(data.nonChromaticPatternGraphicsExists).to.be.false
    })
  })

  it('con categoria no-COLOR, no se muestran patrones aunque nonChromaticKeyRequired=true', () => {
    cy.selectChildProfile('Nubi')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(makeGameStartedEvent('LETTER', true))
      state.injectWsEvent(makeGameReadyEvent('LETTER', LETTER_ELEMENTS, 'letter_a', true))
    })

    cy.wait(1000)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      const data = state.getSceneData()
      expect(data).to.not.be.null
      expect(data.nonChromaticKeyRequired).to.be.true
      expect(data.recognitionCategory).to.eq('LETTER')
      expect(data.nonChromaticPatternGraphicsExists).to.be.false
    })
  })

  it('los patrones no bloquean el toque (las opciones siguen siendo tactiles)', () => {
    cy.selectChildProfile('Nubi')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(makeGameStartedEvent('COLOR', true))
      state.injectWsEvent(makeGameReadyEvent('COLOR', COLOR_ELEMENTS, 'color_red', true))
    })

    cy.wait(1000)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      const data = state.getSceneData()
      expect(data).to.not.be.null
      expect(data.nonChromaticPatternGraphicsExists).to.be.true

      const optionImgs = data.images.filter((img: any) => img.elementId !== 'color_red')
      expect(optionImgs.length).to.be.greaterThan(0)
      optionImgs.forEach((img: any) => {
        expect(img.inputEnabled).to.be.true
      })
    })
  })

  it('los patrones se destruyen al cambiar de ronda', () => {
    cy.selectChildProfile('Nubi')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(makeGameStartedEvent('COLOR', true))
      state.injectWsEvent(makeGameReadyEvent('COLOR', COLOR_ELEMENTS, 'color_red', true))
    })

    cy.wait(1000)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      const data = state.getSceneData()
      expect(data).to.not.be.null
      expect(data.nonChromaticPatternGraphicsExists).to.be.true
    })

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(makeActionResult('CORRECT', false, 1, 3, 'COLOR', COLOR_ELEMENTS, true))
    })

    cy.wait(1000)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      const data = state.getSceneData()
      expect(data).to.not.be.null
      expect(data.nonChromaticPatternGraphicsExists).to.be.true
    })
  })
})
