import { createFamilyViaApi, createChildViaApi, uniqueFamilyName } from '../../support/testData'

const PIN = '1234'
const GAME_TIMEOUT = 20000

const GAME_STARTED_EVENT = {
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
      totalRounds: 3,
      hintActive: false,
      targetElementId: 'letter_a',
      optionIds: ['letter_a', 'letter_b', 'letter_c'],
      elements: []
    }
  }
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
      optionIds: ['letter_a', 'letter_b', 'letter_c'],
      elements: [
        { id: 'letter_a', code: 'letter_a', displayValue: 'A', resourceRefs: { image: 'letter-a' } },
        { id: 'letter_b', code: 'letter_b', displayValue: 'B', resourceRefs: { image: 'letter-b' } },
        { id: 'letter_c', code: 'letter_c', displayValue: 'C', resourceRefs: { image: 'letter-c' } }
      ]
    }
  }
}

function makeActionResult(resultType: string, gameCompleted: boolean, roundIndex: number, totalRounds: number, hintActive: boolean = false) {
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
          hintActive,
          targetElementId: roundIndex < totalRounds ? 'letter_a' : '',
          optionIds: roundIndex < totalRounds ? ['letter_a', 'letter_b', 'letter_c'] : [],
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

describe('RecognitionGameScene — pista visual y celebración (SPRINT-072)', () => {
  let childId: number

  before(() => {
    createFamilyViaApi({ name: uniqueFamilyName('F7Hint'), pin: PIN })
    createChildViaApi({ name: 'Nubi', birthday: '2022-03-15', avatar: 'avatar-01' })
      .then((res) => { childId = res.body.data.id })
  })

  beforeEach(() => {
    cy.intercept('POST', '**/api/v1/sessions/children').as('openSession')
  })

  it('positivo: pista visual aparece cuando hintActive cambia de false a true', () => {
    cy.selectChildProfile('Nubi')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(GAME_STARTED_EVENT)
      state.injectWsEvent(GAME_READY_EVENT)
    })

    cy.wait(800)

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(makeActionResult('INCORRECT', false, 0, 3, false))
    })

    cy.wait(800)

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(makeActionResult('INCORRECT', false, 0, 3, true))
    })

    cy.wait(800)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })
  })

  it('positivo: pista visual no aparece cuando hintActive permanece false', () => {
    cy.selectChildProfile('Nubi')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(GAME_STARTED_EVENT)
      state.injectWsEvent(GAME_READY_EVENT)
    })

    cy.wait(800)

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(makeActionResult('INCORRECT', false, 0, 3, false))
    })

    cy.wait(800)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })
  })

  it('positivo: celebración aparece al completar todas las rondas', () => {
    cy.selectChildProfile('Nubi')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(GAME_STARTED_EVENT)
      state.injectWsEvent(GAME_READY_EVENT)
    })

    cy.wait(800)

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(makeActionResult('CORRECT', true, 3, 3))
    })

    cy.wait(2500)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })
  })

  it('positivo: celebración no muestra puntuaciones ni premios', () => {
    cy.selectChildProfile('Nubi')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(GAME_STARTED_EVENT)
      state.injectWsEvent(GAME_READY_EVENT)
    })

    cy.wait(800)

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(makeActionResult('CORRECT', true, 3, 3))
    })

    cy.wait(1000)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })
  })

  it('positivo: minijuego se puede repetir libremente tras completar', () => {
    cy.selectChildProfile('Nubi')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(GAME_STARTED_EVENT)
      state.injectWsEvent(GAME_READY_EVENT)
    })

    cy.wait(800)

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(makeActionResult('CORRECT', true, 3, 3))
    })

    cy.wait(2500)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(GAME_STARTED_EVENT)
      state.injectWsEvent(GAME_READY_EVENT)
    })

    cy.wait(800)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })
  })

  it('positivo: GAME_READY con hintActive=true muestra pista visual', () => {
    const readyWithHint = {
      ...GAME_READY_EVENT,
      payload: {
        ...GAME_READY_EVENT.payload,
        recognitionState: {
          ...GAME_READY_EVENT.payload.recognitionState,
          hintActive: true
        }
      }
    }

    cy.selectChildProfile('Nubi')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(GAME_STARTED_EVENT)
      state.injectWsEvent(readyWithHint)
    })

    cy.wait(1000)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })
  })
})
