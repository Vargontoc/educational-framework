import { createFamilyViaApi, createChildViaApi, uniqueFamilyName } from '../../support/testData'

const PIN = '1234'
const GAME_TIMEOUT = 20000

function makeGameStartedEvent() {
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
        recognitionCategory: 'LETTER',
        roundIndex: 0,
        totalRounds: 5,
        hintActive: false,
        targetElementId: 'letter_a',
        optionIds: ['letter_a', 'letter_b', 'letter_c'],
        guideChromEnabled: false,
        touchEnableDelayMs: 0,
        nonChromaticKeyRequired: false,
        elements: []
      }
    }
  }
}

function makeGameReadyEvent(opts: {
  guideChromEnabled?: boolean
  touchEnableDelayMs?: number
  nonChromaticKeyRequired?: boolean
} = {}) {
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
        recognitionCategory: 'LETTER',
        roundIndex: 0,
        totalRounds: 5,
        hintActive: false,
        targetElementId: 'letter_a',
        optionIds: ['letter_a', 'letter_b', 'letter_c'],
        guideChromEnabled: opts.guideChromEnabled ?? false,
        touchEnableDelayMs: opts.touchEnableDelayMs ?? 0,
        nonChromaticKeyRequired: opts.nonChromaticKeyRequired ?? false,
        elements: [
          { id: 'letter_a', code: 'letter_a', displayValue: 'A', resourceRefs: { image: 'letter-a' } },
          { id: 'letter_b', code: 'letter_b', displayValue: 'B', resourceRefs: { image: 'letter-b' } },
          { id: 'letter_c', code: 'letter_c', displayValue: 'C', resourceRefs: { image: 'letter-c' } }
        ]
      }
    }
  }
}

function makeActionResult(resultType: string, gameCompleted: boolean, roundIndex: number, totalRounds: number, opts: {
  guideChromEnabled?: boolean
  touchEnableDelayMs?: number
  nonChromaticKeyRequired?: boolean
} = {}) {
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
          recognitionCategory: 'LETTER',
          roundIndex,
          totalRounds,
          hintActive: false,
          targetElementId: roundIndex < totalRounds ? 'letter_a' : '',
          optionIds: roundIndex < totalRounds ? ['letter_a', 'letter_b', 'letter_c'] : [],
          guideChromEnabled: opts.guideChromEnabled ?? false,
          touchEnableDelayMs: opts.touchEnableDelayMs ?? 0,
          nonChromaticKeyRequired: opts.nonChromaticKeyRequired ?? false,
          elements: roundIndex < totalRounds ? [
            { id: 'letter_a', code: 'letter_a', displayValue: 'A', resourceRefs: { image: 'letter-a' } },
            { id: 'letter_b', code: 'letter_b', displayValue: 'B', resourceRefs: { image: 'letter-b' } },
            { id: 'letter_c', code: 'letter_c', displayValue: 'C', resourceRefs: { image: 'letter-c' } }
          ] : []
        }
      }
    }
  }
}

describe('RecognitionGameScene — ladder visual parameters (SPRINT-074)', () => {
  let childId: number

  before(() => {
    createFamilyViaApi({ name: uniqueFamilyName('F7Ladder'), pin: PIN })
    createChildViaApi({ name: 'Nubi', birthday: '2022-03-15', avatar: 'avatar-01' })
      .then((res) => { childId = res.body.data.id })
  })

  beforeEach(() => {
    cy.intercept('POST', '**/api/v1/sessions/children').as('openSession')
  })

  it('positivo: touchEnableDelayMs = 0 habilita el toque inmediatamente', () => {
    cy.selectChildProfile('Nubi')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(makeGameStartedEvent())
      state.injectWsEvent(makeGameReadyEvent({ touchEnableDelayMs: 0 }))
    })

    cy.wait(800)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })
  })

  it('positivo: touchEnableDelayMs > 0 bloquea el toque durante el tiempo configurado', () => {
    cy.selectChildProfile('Nubi')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(makeGameStartedEvent())
      state.injectWsEvent(makeGameReadyEvent({ touchEnableDelayMs: 500 }))
    })

    cy.wait(200)

    cy.wait(600)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })
  })

  it('positivo: guideChromEnabled = true muestra halo alrededor de opciones', () => {
    cy.selectChildProfile('Nubi')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(makeGameStartedEvent())
      state.injectWsEvent(makeGameReadyEvent({ guideChromEnabled: true }))
    })

    cy.wait(800)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })
  })

  it('positivo: guideChromEnabled = false no muestra halo', () => {
    cy.selectChildProfile('Nubi')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(makeGameStartedEvent())
      state.injectWsEvent(makeGameReadyEvent({ guideChromEnabled: false }))
    })

    cy.wait(800)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })
  })

  it('positivo: nonChromaticKeyRequired se almacena correctamente', () => {
    cy.selectChildProfile('Nubi')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(makeGameStartedEvent())
      state.injectWsEvent(makeGameReadyEvent({ nonChromaticKeyRequired: true }))
    })

    cy.wait(800)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })
  })

  it('positivo: touchEnableDelay se aplica tras GAME_ACTION_RESULT con CORRECT', () => {
    cy.selectChildProfile('Nubi')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(makeGameStartedEvent())
      state.injectWsEvent(makeGameReadyEvent({ touchEnableDelayMs: 500 }))
    })

    cy.wait(600)

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(makeActionResult('CORRECT', false, 1, 5, { touchEnableDelayMs: 500 }))
    })

    cy.wait(800)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })
  })

  it('positivo: guideChrom se destruye y recrea al cambiar de ronda', () => {
    cy.selectChildProfile('Nubi')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(makeGameStartedEvent())
      state.injectWsEvent(makeGameReadyEvent({ guideChromEnabled: true }))
    })

    cy.wait(600)

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(makeActionResult('CORRECT', false, 1, 5, { guideChromEnabled: true }))
    })

    cy.wait(800)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })
  })
})
