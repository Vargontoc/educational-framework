import { createFamilyViaApi, createChildViaApi, uniqueFamilyName } from '../../support/testData'

const PIN = '1234'
const GAME_TIMEOUT = 20000

const state = (win: Cypress.AUTWindow) => (win as any).__NUBI_GAME_STATE__

function animalElements(codes: string[]) {
  return codes.map(code => ({
    id: code,
    code,
    displayValue: code,
    // ANIMAL usa `code` como key (bloque recognition-animals); backend no envia `image`.
    // biome/group son metadatos del backend y el cliente no los usa.
    resourceRefs: { 'nubi-audio': `Donde esta ${code}`, biome: '["FARM"]', group: '[]' }
  }))
}

function recognitionState(elements: any[], roundIndex: number) {
  return {
    recognitionCategory: 'ANIMAL',
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

describe('RecognitionGameScene — animales: carga dinamica (bloque recognition-animals)', () => {
  let childId: number

  before(() => {
    createFamilyViaApi({ name: uniqueFamilyName('F7Animals'), pin: PIN })
    createChildViaApi({ name: 'Nubi', birthday: '2022-03-15', avatar: 'avatar-01' })
      .then((res) => { childId = res.body.data.id })
  })

  it('solo se cargan los animales de la ronda actual (key = code)', () => {
    openRecognitionScene(childId)
    cy.window().then((win) => state(win).injectWsEvent(gameReady(animalElements(['cow', 'pig', 'chicken']))))

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      ;['cow', 'pig', 'chicken'].forEach(a => expect(state(win).textureExists(a), a).to.eq(true))
      expect(state(win).textureExists('owl')).to.eq(false)
      expect(options(win)).to.have.length(3)
    })
  })

  it('las opciones usan la textura del animal, sin tint y sin depender de resourceRefs.image', () => {
    openRecognitionScene(childId)
    cy.window().then((win) => state(win).injectWsEvent(gameReady(animalElements(['cow', 'pig']))))

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const opts = options(win)
      expect(opts.map((o: any) => o.textureKey).sort()).to.deep.eq(['cow', 'pig'])
      opts.forEach((o: any) => expect(o.tint).to.eq(null))
      const stimulus = state(win).getSceneData().images.find((i: any) => i.isStimulus)
      expect(stimulus.textureKey).to.eq('cow')
    })
  })

  it('la siguiente ronda se precarga durante el feedback y se renderiza', () => {
    openRecognitionScene(childId)
    cy.window().then((win) => state(win).injectWsEvent(gameReady(animalElements(['cow', 'pig']))))
    cy.window({ timeout: GAME_TIMEOUT }).should((win) => expect(options(win)).to.have.length(2))

    cy.window().then((win) => {
      expect(state(win).textureExists('sheep')).to.eq(false)
      state(win).injectWsEvent(correctResult(animalElements(['sheep', 'goat']), 1))
    })

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      expect(options(win).map((o: any) => o.elementId).sort()).to.deep.eq(['goat', 'sheep'])
      expect(state(win).textureExists('sheep')).to.eq(true)
    })
  })

  it('las texturas de animales se liberan al salir del minijuego', () => {
    openRecognitionScene(childId)
    cy.window().then((win) => state(win).injectWsEvent(gameReady(animalElements(['cow', 'pig', 'cat']))))
    cy.window({ timeout: GAME_TIMEOUT }).should((win) => expect(options(win)).to.have.length(3))

    cy.window().then((win) => {
      state(win).tapExitButton()
      state(win).tapExitButton()
    })

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      expect(state(win).activeScene).to.eq('world-map')
      expect(state(win).textureExists('cow')).to.eq(false)
    })
  })
})
