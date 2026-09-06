import { createFamilyViaApi, createChildViaApi, uniqueFamilyName } from '../../support/testData'

const PIN = '1234'
const GAME_TIMEOUT = 20000

describe('BaseStateScene — estado no interactivo (SPRINT-043)', () => {
  let childId: number

  before(() => {
    createFamilyViaApi({ name: uniqueFamilyName('F7Base'), pin: PIN })
    createChildViaApi({ name: 'Mia', birthday: '2022-06-20', avatar: 'avatar-02' })
      .then((res) => { childId = res.body.data.id })
  })

  beforeEach(() => {
    cy.intercept('POST', '**/api/v1/sessions/children').as('openSession')
  })

  it('positivo: BaseStateScene es alcanzada como estado visual de transición no interactivo', () => {
    cy.selectChildProfile('Mia')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('base-state')
    })

    cy.get('canvas').should('exist')
    cy.get('button', { timeout: 2000 }).should('not.exist')
  })

  it('negativo: WorldMapScene y RecognitionGameScene no están registradas en el flujo actual', () => {
    cy.selectChildProfile('Mia')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      const keys: string[] = state.sceneKeys
      expect(keys).to.include('loading')
      expect(keys).to.include('base-state')
      expect(keys).to.include('farewell')
      expect(keys).to.not.include('world-map')
      expect(keys).to.not.include('recognition-game')
    })
  })
})
