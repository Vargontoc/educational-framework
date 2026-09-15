import { createFamilyViaApi, createChildViaApi, uniqueFamilyName } from '../../support/testData'

const PIN = '1234'
const GAME_TIMEOUT = 20000
const VIEWPORT_WIDTH = 1280
const VIEWPORT_HEIGHT = 720
const STIMULUS_ZONE_Y = 0.25
const OPTIONS_ZONE_Y = 0.65
const POSITION_TOLERANCE = 80

function makeGameStartedEvent(category: string) {
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
        targetElementId: 'item_1',
        optionIds: ['item_1', 'item_2', 'item_3'],
        elements: []
      }
    }
  }
}

function makeGameReadyEvent(category: string, elements: any[], targetElementId: string) {
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
        targetElementId,
        optionIds: elements.filter((e: any) => e.id !== targetElementId).map((e: any) => e.id),
        elements
      }
    }
  }
}

const LETTER_ELEMENTS = [
  { id: 'letter_a', code: 'letter_a', displayValue: 'A', resourceRefs: { image: 'letter-a' } },
  { id: 'letter_b', code: 'letter_b', displayValue: 'B', resourceRefs: { image: 'letter-b' } },
  { id: 'letter_c', code: 'letter_c', displayValue: 'C', resourceRefs: { image: 'letter-c' } }
]

const NUMBER_ELEMENTS = [
  { id: 'number_1', code: 'number_1', displayValue: '1', resourceRefs: { image: 'number-1' } },
  { id: 'number_2', code: 'number_2', displayValue: '2', resourceRefs: { image: 'number-2' } },
  { id: 'number_3', code: 'number_3', displayValue: '3', resourceRefs: { image: 'number-3' } }
]

const SHAPE_ELEMENTS = [
  { id: 'shape_circle', code: 'shape_circle', displayValue: 'Circle', resourceRefs: { image: 'shape-circle' } },
  { id: 'shape_square', code: 'shape_square', displayValue: 'Square', resourceRefs: { image: 'shape-square' } },
  { id: 'shape_triangle', code: 'shape_triangle', displayValue: 'Triangle', resourceRefs: { image: 'shape-triangle' } }
]

const COLOR_ELEMENTS = [
  { id: 'color_red', code: 'color_red', displayValue: 'Red', resourceRefs: { image: 'color-red' } },
  { id: 'color_blue', code: 'color_blue', displayValue: 'Blue', resourceRefs: { image: 'color-blue' } },
  { id: 'color_green', code: 'color_green', displayValue: 'Green', resourceRefs: { image: 'color-green' } }
]

const ANIMAL_ELEMENTS = [
  { id: 'animal_cat', code: 'animal_cat', displayValue: 'Cat', resourceRefs: { image: 'animal-cat' } },
  { id: 'animal_dog', code: 'animal_dog', displayValue: 'Dog', resourceRefs: { image: 'animal-dog' } },
  { id: 'animal_bird', code: 'animal_bird', displayValue: 'Bird', resourceRefs: { image: 'animal-bird' } }
]

describe('RecognitionGameScene — layout, interactivity, categories, hit size (SPRINT-073)', () => {
  let childId: number

  before(() => {
    createFamilyViaApi({ name: uniqueFamilyName('F7Layout'), pin: PIN })
    createChildViaApi({ name: 'Nubi', birthday: '2022-03-15', avatar: 'avatar-01' })
      .then((res) => { childId = res.body.data.id })
  })

  beforeEach(() => {
    cy.intercept('POST', '**/api/v1/sessions/children').as('openSession')
  })

  it('positivo: stimulus renders at y:25% (upper zone), options at y:65% (lower zone)', () => {
    cy.selectChildProfile('Nubi')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(makeGameStartedEvent('LETTER'))
      state.injectWsEvent(makeGameReadyEvent('LETTER', LETTER_ELEMENTS, 'letter_a'))
    })

    cy.wait(1000)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      const data = state.getSceneData()
      expect(data).to.not.be.null
      expect(data.images.length).to.be.greaterThan(0)

      const expectedStimulusY = VIEWPORT_HEIGHT * STIMULUS_ZONE_Y
      const expectedOptionsY = VIEWPORT_HEIGHT * OPTIONS_ZONE_Y

      const stimulusImg = data.images.find((img: any) => img.elementId === 'letter_a')
      expect(stimulusImg).to.exist
      expect(stimulusImg.y).to.be.closeTo(expectedStimulusY, POSITION_TOLERANCE)

      const optionImgs = data.images.filter((img: any) => img.elementId !== 'letter_a')
      expect(optionImgs.length).to.be.greaterThan(0)
      optionImgs.forEach((img: any) => {
        expect(img.y).to.be.closeTo(expectedOptionsY, POSITION_TOLERANCE)
      })
    })
  })

  it('positivo: stimulus does NOT respond to pointerdown (no setInteractive)', () => {
    cy.selectChildProfile('Nubi')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(makeGameStartedEvent('LETTER'))
      state.injectWsEvent(makeGameReadyEvent('LETTER', LETTER_ELEMENTS, 'letter_a'))
    })

    cy.wait(1000)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      const data = state.getSceneData()
      expect(data).to.not.be.null

      const stimulusImg = data.images.find((img: any) => img.elementId === 'letter_a')
      expect(stimulusImg).to.exist
      expect(stimulusImg.inputEnabled).to.be.false

      const optionImgs = data.images.filter((img: any) => img.elementId !== 'letter_a')
      optionImgs.forEach((img: any) => {
        expect(img.inputEnabled).to.be.true
      })
    })
  })

  it('positivo: all 5 categories load without error (assets or placeholders)', () => {
    const categories = [
      { name: 'LETTER', elements: LETTER_ELEMENTS, target: 'letter_a' },
      { name: 'NUMBER', elements: NUMBER_ELEMENTS, target: 'number_1' },
      { name: 'SHAPE', elements: SHAPE_ELEMENTS, target: 'shape_circle' },
      { name: 'COLOR', elements: COLOR_ELEMENTS, target: 'color_red' },
      { name: 'ANIMAL', elements: ANIMAL_ELEMENTS, target: 'animal_cat' }
    ]

    cy.selectChildProfile('Nubi')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })

    categories.forEach((cat) => {
      cy.window().then((win) => {
        const state = (win as any).__NUBI_GAME_STATE__
        state.injectWsEvent(makeGameStartedEvent(cat.name))
        state.injectWsEvent(makeGameReadyEvent(cat.name, cat.elements, cat.target))
      })

      cy.wait(1000)

      cy.window().should((win) => {
        const state = (win as any).__NUBI_GAME_STATE__
        const data = state.getSceneData()
        expect(data).to.not.be.null
        expect(data.images.length).to.be.greaterThan(0)

        const targetImg = data.images.find((img: any) => img.elementId === cat.target)
        expect(targetImg).to.exist
      })
    })
  })

  it('positivo: minElementHitSize field is used in scale calculations', () => {
    cy.selectChildProfile('Nubi')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })

    cy.window().then((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      state.injectWsEvent(makeGameStartedEvent('LETTER'))
      state.injectWsEvent(makeGameReadyEvent('LETTER', LETTER_ELEMENTS, 'letter_a'))
    })

    cy.wait(1000)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      const data = state.getSceneData()
      expect(data).to.not.be.null
      expect(data.minElementHitSize).to.be.a('number')
      expect(data.minElementHitSize).to.be.greaterThan(0)

      const optionImgs = data.images.filter((img: any) => img.elementId !== 'letter_a')
      optionImgs.forEach((img: any) => {
        const effectiveWidth = img.displayWidth || img.width || 0
        const effectiveHeight = img.displayHeight || img.height || 0
        const maxDim = Math.max(effectiveWidth, effectiveHeight)
        expect(maxDim).to.be.greaterThanOrEqual(data.minElementHitSize * 0.9)
      })
    })
  })
})
