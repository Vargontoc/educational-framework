import { createFamilyViaApi, createChildViaApi, uniqueFamilyName } from '../../support/testData'

const PIN = '1234'
const GAME_TIMEOUT = 20000

const state = (win: Cypress.AUTWindow) => (win as any).__NUBI_GAME_STATE__

function letterElements(letters: string[]) {
  return letters.map(l => ({
    id: `letter_${l}`,
    code: `letter_${l}`,
    displayValue: l.toUpperCase(),
    // LETTER usa `code` como key; este ref inexistente prueba que no se usa
    resourceRefs: { image: `ignored-${l}` }
  }))
}

function numberElements(numbers: number[]) {
  return numbers.map(n => ({
    id: `number_${n}`,
    code: `number_${n}`,
    displayValue: `${n}`,
    resourceRefs: { image: `number_${n}` }
  }))
}

function recognitionState(category: string, elements: any[], roundIndex: number) {
  return {
    recognitionCategory: category,
    roundIndex,
    totalRounds: 6,
    hintActive: false,
    targetElementId: elements[0].id,
    optionIds: elements.map(e => e.id),
    elements
  }
}

function gameReady(category: string, elements: any[]) {
  return {
    event: 'GAME_READY',
    sessionId: 1,
    payload: {
      engine: 'RECOGNITION',
      activityId: 1,
      gameId: 1,
      difficultyLevelId: 1,
      status: 'IN_PROGRESS',
      recognitionState: recognitionState(category, elements, 0)
    }
  }
}

function correctResult(category: string, elements: any[], roundIndex: number) {
  return {
    event: 'GAME_ACTION_RESULT',
    sessionId: 1,
    payload: {
      resultType: 'CORRECT',
      gameCompleted: false,
      difficultyChanged: false,
      newDifficultyLevelId: 1,
      attemptContext: '',
      updatedState: {
        engine: 'RECOGNITION',
        activityId: 1,
        gameId: 1,
        difficultyLevelId: 1,
        status: 'IN_PROGRESS',
        recognitionState: recognitionState(category, elements, roundIndex)
      }
    }
  }
}

function openRecognitionScene(childId: number) {
  cy.selectChildProfile('Nubi')
  cy.visit(`/game/${childId}`)

  cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
    expect(state(win).activeScene).to.eq('world-map')
  })

  cy.window().then((win) => state(win).startRecognitionScene())

  cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
    expect(state(win).activeScene).to.eq('recognition-game')
  })
}

function optionData(win: Cypress.AUTWindow) {
  return state(win).getSceneData().images.filter((i: any) => !i.isStimulus && i.type === 'Image')
}

describe('RecognitionGameScene — carga dinamica y tintado de letras (SPRINT-079)', () => {
  let childId: number

  before(() => {
    createFamilyViaApi({ name: uniqueFamilyName('F7DynLoad'), pin: PIN })
    createChildViaApi({ name: 'Nubi', birthday: '2022-03-15', avatar: 'avatar-01' })
      .then((res) => { childId = res.body.data.id })
  })

  it('positivo: solo se cargan las texturas de la ronda actual (key = code)', () => {
    openRecognitionScene(childId)

    cy.window().then((win) => state(win).injectWsEvent(gameReady('LETTER', letterElements(['a', 'b', 'c']))))

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      expect(state(win).textureExists('letter_a')).to.eq(true)
      expect(state(win).textureExists('letter_b')).to.eq(true)
      expect(state(win).textureExists('letter_c')).to.eq(true)
      expect(state(win).textureExists('letter_z')).to.eq(false)
      expect(optionData(win)).to.have.length(3)
    })
  })

  it('positivo: las letras se tintan con colores distintos y el estimulo no repite ninguno', () => {
    openRecognitionScene(childId)

    cy.window().then((win) => state(win).injectWsEvent(gameReady('LETTER', letterElements(['a', 'b', 'c']))))

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const options = optionData(win)
      expect(options).to.have.length(3)
      const tints = options.map((o: any) => o.tint)
      tints.forEach((t: number | null) => expect(t).to.be.a('number'))
      expect(new Set(tints).size).to.eq(3)

      const stimulus = state(win).getSceneData().images.find((i: any) => i.isStimulus)
      expect(tints).not.to.include(stimulus.tint)
    })
  })

  it('negativo: categorias no-LETTER no aplican tint', () => {
    openRecognitionScene(childId)

    cy.window().then((win) => state(win).injectWsEvent(gameReady('NUMBER', numberElements([1, 2, 3]))))

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const images = state(win).getSceneData().images
      expect(images.length).to.be.greaterThan(0)
      images.forEach((i: any) => expect(i.tint).to.eq(null))
    })
  })

  it('positivo: la siguiente ronda se precarga durante el feedback y se renderiza con sus letras', () => {
    openRecognitionScene(childId)

    cy.window().then((win) => state(win).injectWsEvent(gameReady('LETTER', letterElements(['a', 'b', 'c']))))
    cy.window({ timeout: GAME_TIMEOUT }).should((win) => expect(optionData(win)).to.have.length(3))

    cy.window().then((win) => {
      expect(state(win).textureExists('letter_d')).to.eq(false)
      state(win).injectWsEvent(correctResult('LETTER', letterElements(['d', 'e', 'f']), 1))
    })

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const ids = optionData(win).map((o: any) => o.elementId).sort()
      expect(ids).to.deep.eq(['letter_d', 'letter_e', 'letter_f'])
      expect(state(win).textureExists('letter_d')).to.eq(true)
    })
  })

  it('positivo: la limpieza libera rondas antiguas sin tocar las recientes ni la actual', () => {
    openRecognitionScene(childId)

    const rounds = [['a', 'b', 'c'], ['d', 'e', 'f'], ['g', 'h', 'i'], ['j', 'k', 'l'], ['m', 'n', 'o']]

    cy.window().then((win) => state(win).injectWsEvent(gameReady('LETTER', letterElements(rounds[0]))))
    cy.window({ timeout: GAME_TIMEOUT }).should((win) => expect(optionData(win)).to.have.length(3))

    rounds.slice(1).forEach((letters, idx) => {
      cy.window().then((win) => state(win).injectWsEvent(correctResult('LETTER', letterElements(letters), idx + 1)))
      cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
        const ids = optionData(win).map((o: any) => o.elementId).sort()
        expect(ids).to.deep.eq(letters.map(l => `letter_${l}`))
      })
    })

    cy.window().should((win) => {
      expect(state(win).textureExists('letter_a')).to.eq(false)
      expect(state(win).textureExists('letter_d')).to.eq(false)
      expect(state(win).textureExists('letter_g')).to.eq(true)
      expect(state(win).textureExists('letter_m')).to.eq(true)
      optionData(win).forEach((o: any) => expect(o.tint).to.be.a('number'))
    })
  })

  it('positivo: las texturas y el tint se liberan al salir del minijuego', () => {
    openRecognitionScene(childId)

    cy.window().then((win) => state(win).injectWsEvent(gameReady('LETTER', letterElements(['a', 'b', 'c']))))
    cy.window({ timeout: GAME_TIMEOUT }).should((win) => expect(optionData(win)).to.have.length(3))

    cy.window().then((win) => {
      state(win).tapExitButton()
      state(win).tapExitButton()
    })

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      expect(state(win).activeScene).to.eq('world-map')
      expect(state(win).textureExists('letter_a')).to.eq(false)
    })
  })
})
