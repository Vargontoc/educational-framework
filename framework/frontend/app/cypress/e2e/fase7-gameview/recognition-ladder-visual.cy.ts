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

function getOptionImages(sceneData: any) {
  return sceneData.images.filter((img: any) => img.elementId && img.elementId !== 'letter_a' || (img.elementId && !sceneData.images.find((s: any) => s.elementId === img.elementId && s !== img)))
    .filter((img: any) => img.elementId)
}

function getOptionImagesByElementIds(sceneData: any, elementIds: string[]) {
  return sceneData.images.filter((img: any) => elementIds.includes(img.elementId))
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

  it('positivo: touchEnableDelayMs = 0 habilita el toque inmediatamente con alpha 1.0', () => {
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
      const data = state.getSceneData()
      expect(data).to.not.be.null
      expect(data.touchEnableTimerActive).to.be.false
      const optionImgs = data.images.filter((img: any) =>
        ['letter_a', 'letter_b', 'letter_c'].includes(img.elementId) && !data.images.find((s: any) => s.elementId === img.elementId && img !== s)
      )
      const optionAlphas = data.images
        .filter((img: any) => ['letter_b', 'letter_c'].includes(img.elementId))
      optionAlphas.forEach((img: any) => {
        expect(img.alpha).to.be.closeTo(1.0, 0.05)
      })
    })
  })

  it('positivo: touchEnableDelayMs > 0 bloquea el toque con alpha 0.5 y timer activo', () => {
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

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      const data = state.getSceneData()
      expect(data).to.not.be.null
      expect(data.touchEnableTimerActive).to.be.true
      const optionImgs = data.images.filter((img: any) =>
        ['letter_b', 'letter_c'].includes(img.elementId)
      )
      optionImgs.forEach((img: any) => {
        expect(img.alpha).to.be.closeTo(0.5, 0.05)
      })
    })

    cy.wait(600)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      const data = state.getSceneData()
      expect(data).to.not.be.null
      expect(data.touchEnableTimerActive).to.be.false
      const optionImgs = data.images.filter((img: any) =>
        ['letter_b', 'letter_c'].includes(img.elementId)
      )
      optionImgs.forEach((img: any) => {
        expect(img.alpha).to.be.closeTo(1.0, 0.05)
      })
    })
  })

  it('positivo: guideChromEnabled = true ya no dibuja el halo sobre las opciones (el tablero lo sustituye)', () => {
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
      const data = state.getSceneData()
      expect(data).to.not.be.null
      expect(data.guideChromGraphicsExists).to.be.false
      expect(data.images.filter((img: any) => !img.isStimulus)).to.have.length.greaterThan(0)
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
      const data = state.getSceneData()
      expect(data).to.not.be.null
      expect(data.guideChromGraphicsExists).to.be.false
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
      const data = state.getSceneData()
      expect(data).to.not.be.null
      expect(data.nonChromaticKeyRequired).to.be.true
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

    cy.wait(200)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      const data = state.getSceneData()
      expect(data).to.not.be.null
      expect(data.touchEnableTimerActive).to.be.true
      const optionImgs = data.images.filter((img: any) =>
        ['letter_b', 'letter_c'].includes(img.elementId)
      )
      optionImgs.forEach((img: any) => {
        expect(img.alpha).to.be.closeTo(0.5, 0.05)
      })
    })

    cy.wait(800)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      const data = state.getSceneData()
      expect(data).to.not.be.null
      expect(data.touchEnableTimerActive).to.be.false
    })
  })

  it('positivo: guideChromEnabled = true no dibuja halo en ninguna ronda', () => {
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

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      const data = state.getSceneData()
      expect(data.guideChromGraphicsExists).to.be.false
    })

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(makeActionResult('CORRECT', false, 1, 5, { guideChromEnabled: true }))
    })

    cy.wait(800)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      const data = state.getSceneData()
      expect(data).to.not.be.null
      expect(data.guideChromGraphicsExists).to.be.false
    })
  })

  it('accesibilidad: prefers-reduced-motion: reduce hace transicion instantanea', () => {
    cy.selectChildProfile('Nubi')
    cy.visit(`/game/${childId}`, {
      onBeforeLoad(win) {
        Object.defineProperty(win, 'matchMedia', {
          value: (query: string) => ({
            matches: query === '(prefers-reduced-motion: reduce)',
            media: query,
            onchange: null,
            addListener: () => {},
            removeListener: () => {},
            addEventListener: () => {},
            removeEventListener: () => {},
            dispatchEvent: () => false
          }),
          writable: true
        })
      }
    })

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(makeGameStartedEvent())
      state.injectWsEvent(makeGameReadyEvent({
        touchEnableDelayMs: 500,
        guideChromEnabled: true
      }))
    })

    cy.wait(200)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      const data = state.getSceneData()
      expect(data).to.not.be.null
      expect(data.touchEnableTimerActive).to.be.true
      expect(data.guideChromGraphicsExists).to.be.false
      const optionImgs = data.images.filter((img: any) =>
        ['letter_b', 'letter_c'].includes(img.elementId)
      )
      optionImgs.forEach((img: any) => {
        expect(img.alpha).to.be.closeTo(0.5, 0.05)
      })
    })

    cy.wait(600)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      const data = state.getSceneData()
      expect(data).to.not.be.null
      expect(data.touchEnableTimerActive).to.be.false
      expect(data.guideChromGraphicsExists).to.be.true
      const optionImgs = data.images.filter((img: any) =>
        ['letter_b', 'letter_c'].includes(img.elementId)
      )
      optionImgs.forEach((img: any) => {
        expect(img.alpha).to.be.closeTo(1.0, 0.05)
      })
    })
  })
})
