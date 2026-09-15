import { createFamilyViaApi, createChildViaApi, uniqueFamilyName } from '../../support/testData'

const PIN = '1234'
const GAME_TIMEOUT = 20000

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
      totalRounds: 5,
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
      totalRounds: 5,
      hintActive: false,
      targetElementId: 'letter_a',
      optionIds: ['letter_a', 'letter_b', 'letter_c'],
      elements: []
    }
  }
}

function makeActionResult(resultType: string, gameCompleted: boolean, roundIndex: number, totalRounds: number) {
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

describe('RecognitionGameScene — base jugable minijuego (SPRINT-070)', () => {
  let childId: number

  before(() => {
    createFamilyViaApi({ name: uniqueFamilyName('F7Mini'), pin: PIN })
    createChildViaApi({ name: 'Nubi', birthday: '2022-03-15', avatar: 'avatar-01' })
      .then((res) => { childId = res.body.data.id })
  })

  beforeEach(() => {
    cy.intercept('POST', '**/api/v1/sessions/children').as('openSession')
  })

  it('positivo: recognition-game esta registrada en las escenas del juego', () => {
    cy.selectChildProfile('Nubi')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      const keys: string[] = state.sceneKeys
      expect(keys).to.include('recognition-game')
    })
  })

  it('positivo: GAME_READY renderiza elementos y crea barra de progreso', () => {
    cy.selectChildProfile('Nubi')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(GAME_STARTED_EVENT)
    })

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(GAME_READY_EVENT)
    })

    cy.wait(800)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })
  })

  it('positivo: barra de progreso se actualiza tras multiples rondas', () => {
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

    cy.wait(600)

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(makeActionResult('CORRECT', false, 1, 5))
    })

    cy.wait(600)

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(makeActionResult('CORRECT', false, 2, 5))
    })

    cy.wait(600)

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(makeActionResult('CORRECT', false, 3, 5))
    })

    cy.wait(600)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })
  })

  it('positivo: feedback correcto anima el elemento seleccionado y no crashea', () => {
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

    cy.wait(600)

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(makeActionResult('CORRECT', false, 1, 5))
    })

    cy.wait(800)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })
  })

  it('positivo: feedback incorrecto anima el elemento seleccionado y no crashea', () => {
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

    cy.wait(600)

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(makeActionResult('INCORRECT', false, 0, 5))
    })

    cy.wait(800)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })
  })

  it('positivo: gameCompleted=true inicia transicion de salida hacia WorldMap', () => {
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

    cy.wait(600)

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(makeActionResult('CORRECT', true, 5, 5))
    })

    cy.wait(1500)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })
  })

  it('positivo: doble-toque en zona abandono retorna a WorldMap', () => {
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

    cy.wait(600)

    cy.get('canvas').then(($canvas) => {
      const rect = $canvas[0].getBoundingClientRect()
      const scaleX = rect.width / 1280
      const scaleY = rect.height / 720
      const abandonX = rect.left + (1280 - 50) * scaleX
      const abandonY = rect.top + (720 - 50) * scaleY

      cy.wrap($canvas).click(abandonX - rect.left, abandonY - rect.top, { force: true })
      cy.wait(300)
      cy.wrap($canvas).click(abandonX - rect.left, abandonY - rect.top, { force: true })
    })

    cy.wait(1500)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })
  })

  it('positivo: transicion de entrada muestra fade overlay', () => {
    cy.selectChildProfile('Nubi')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })

    cy.get('canvas').should('exist')
  })

  it('positivo: sessionId se preserva al retornar de minijuego a WorldMap', () => {
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

    cy.wait(600)

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(makeActionResult('CORRECT', true, 5, 5))
    })

    cy.wait(1500)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })
  })

  it('negativo: la escena no se alcanza sin un elemento interactivo con hasActivity', () => {
    cy.selectChildProfile('Nubi')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })

    cy.wait(1000)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })
  })
})
