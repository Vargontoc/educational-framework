import { createFamilyViaApi, createChildViaApi, uniqueFamilyName } from '../../support/testData'

const PIN = '1234'
const GAME_TIMEOUT = 20000

const state = (win: Cypress.AUTWindow) => (win as any).__NUBI_GAME_STATE__

function numberElements(numbers: number[]) {
  return numbers.map(n => ({
    id: `number_${n}`,
    code: `number_${n}`,
    displayValue: `${n}`,
    // NUMBER usa `code` como key; este ref inexistente prueba que no se usa
    resourceRefs: { image: `ignored-${n}` }
  }))
}

function recognitionState(elements: any[], roundIndex: number) {
  return {
    recognitionCategory: 'NUMBER',
    roundIndex,
    totalRounds: 6,
    hintActive: false,
    targetElementId: elements[0].id,
    optionIds: elements.map(e => e.id),
    elements
  }
}

const gameReady = (elements: any[]) => ({
  event: 'GAME_READY',
  sessionId: 1,
  payload: {
    engine: 'RECOGNITION', activityId: 1, gameId: 1, difficultyLevelId: 1, status: 'IN_PROGRESS',
    recognitionState: recognitionState(elements, 0)
  }
})

const correctResult = (elements: any[], roundIndex: number) => ({
  event: 'GAME_ACTION_RESULT',
  sessionId: 1,
  payload: {
    resultType: 'CORRECT', gameCompleted: false, difficultyChanged: false, newDifficultyLevelId: 1, attemptContext: '',
    updatedState: {
      engine: 'RECOGNITION', activityId: 1, gameId: 1, difficultyLevelId: 1, status: 'IN_PROGRESS',
      recognitionState: recognitionState(elements, roundIndex)
    }
  }
})

function openRecognitionScene(childId: number) {
  cy.selectChildProfile('Nubi')
  cy.visit(`/game/${childId}`)
  cy.window({ timeout: GAME_TIMEOUT }).should((win) => expect(state(win).activeScene).to.eq('world-map'))
  cy.window().then((win) => state(win).startRecognitionScene())
  cy.window({ timeout: GAME_TIMEOUT }).should((win) => expect(state(win).activeScene).to.eq('recognition-game'))
}

const options = (win: Cypress.AUTWindow) =>
  state(win).getSceneData().images.filter((i: any) => !i.isStimulus && i.type === 'Image')

describe('RecognitionGameScene — carga dinamica y tintado de numeros (SPRINT-081)', () => {
  let childId: number

  before(() => {
    createFamilyViaApi({ name: uniqueFamilyName('F7NumLoad'), pin: PIN })
    createChildViaApi({ name: 'Nubi', birthday: '2022-03-15', avatar: 'avatar-01' })
      .then((res) => { childId = res.body.data.id })
  })

  it('positivo: solo se cargan los numeros de la ronda actual (key = code)', () => {
    openRecognitionScene(childId)
    cy.window().then((win) => state(win).injectWsEvent(gameReady(numberElements([1, 2, 3]))))

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      ;[1, 2, 3].forEach(n => expect(state(win).textureExists(`number_${n}`), `number_${n}`).to.eq(true))
      expect(state(win).textureExists('number_9')).to.eq(false)
      expect(options(win)).to.have.length(3)
    })
  })

  it('positivo: los numeros se tintan con colores distintos y el estimulo no repite ninguno', () => {
    openRecognitionScene(childId)
    cy.window().then((win) => state(win).injectWsEvent(gameReady(numberElements([1, 2, 3]))))

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const opts = options(win)
      expect(opts).to.have.length(3)
      const tints = opts.map((o: any) => o.tint)
      tints.forEach((t: number | null) => expect(t).to.be.a('number'))
      expect(new Set(tints).size).to.eq(3)
      const stimulus = state(win).getSceneData().images.find((i: any) => i.isStimulus)
      expect(tints).not.to.include(stimulus.tint)
    })
  })

  it('positivo: la siguiente ronda se precarga durante el feedback y se renderiza', () => {
    openRecognitionScene(childId)
    cy.window().then((win) => state(win).injectWsEvent(gameReady(numberElements([1, 2]))))
    cy.window({ timeout: GAME_TIMEOUT }).should((win) => expect(options(win)).to.have.length(2))

    cy.window().then((win) => {
      expect(state(win).textureExists('number_5')).to.eq(false)
      state(win).injectWsEvent(correctResult(numberElements([5, 6]), 1))
    })

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      expect(options(win).map((o: any) => o.elementId).sort()).to.deep.eq(['number_5', 'number_6'])
      expect(state(win).textureExists('number_5')).to.eq(true)
    })
  })

  it('positivo: la limpieza libera rondas antiguas sin tocar las recientes ni la actual', () => {
    openRecognitionScene(childId)
    const rounds = [[0, 1], [2, 3], [4, 5], [6, 7], [8, 9]]

    cy.window().then((win) => state(win).injectWsEvent(gameReady(numberElements(rounds[0]))))
    cy.window({ timeout: GAME_TIMEOUT }).should((win) => expect(options(win)).to.have.length(2))

    rounds.slice(1).forEach((numbers, idx) => {
      cy.window().then((win) => state(win).injectWsEvent(correctResult(numberElements(numbers), idx + 1)))
      cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
        expect(options(win).map((o: any) => o.elementId).sort()).to.deep.eq(numbers.map(n => `number_${n}`))
      })
    })

    cy.window().should((win) => {
      ;[0, 1, 2, 3].forEach(n => expect(state(win).textureExists(`number_${n}`), `number_${n}`).to.eq(false))
      ;[4, 5, 6, 7, 8, 9].forEach(n => expect(state(win).textureExists(`number_${n}`), `number_${n}`).to.eq(true))
      options(win).forEach((o: any) => expect(o.tint).to.be.a('number'))
    })
  })

  it('positivo: las texturas de numeros se liberan al salir del minijuego', () => {
    openRecognitionScene(childId)
    cy.window().then((win) => state(win).injectWsEvent(gameReady(numberElements([1, 2, 3]))))
    cy.window({ timeout: GAME_TIMEOUT }).should((win) => expect(options(win)).to.have.length(3))

    cy.window().then((win) => {
      state(win).tapExitButton()
      state(win).tapExitButton()
    })

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      expect(state(win).activeScene).to.eq('world-map')
      expect(state(win).textureExists('number_1')).to.eq(false)
    })
  })

  it('negativo: LETTER sigue funcionando igual (key = code y tint)', () => {
    openRecognitionScene(childId)
    const letters = ['a', 'b', 'c'].map(l => ({ id: `letter_${l}`, code: `letter_${l}`, displayValue: l, resourceRefs: {} }))
    cy.window().then((win) => state(win).injectWsEvent({
      ...gameReady(letters),
      payload: { ...gameReady(letters).payload, recognitionState: { ...recognitionState(letters, 0), recognitionCategory: 'LETTER' } }
    }))

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      expect(state(win).textureExists('letter_a')).to.eq(true)
      options(win).forEach((o: any) => expect(o.tint).to.be.a('number'))
    })
  })
})
