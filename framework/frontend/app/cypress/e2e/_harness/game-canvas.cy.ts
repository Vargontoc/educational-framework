import { createFamilyViaApi, createChildViaApi } from '../../support/testData'

describe('Harness: inspección Phaser/GameView', () => {
  before(() => {
    createFamilyViaApi({ name: 'GameCanvas E2E', pin: '1234' })
    createChildViaApi({ name: 'Leo', birthday: '2022-03-15', avatar: 'avatar-01' })
  })

  beforeEach(() => {
    cy.intercept('POST', '/api/v1/auth/login').as('login')
    cy.intercept('GET', '/api/v1/family/children').as('children')
    cy.intercept('POST', '/api/v1/sessions/children').as('openSession')
  })

  it('expone el hook __NUBI_GAME_STATE__ en GameView', () => {
    cy.selectChildProfile('Leo')
    cy.visit('/game/1')
    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state).to.exist
      expect(state).to.have.property('childId')
      expect(state).to.have.property('npcEnabled')
      expect(state).to.have.property('ttsEnabled')
      expect(state).to.have.property('activeScene')
    })
  })

  it('el hook refleja los valores registrados en Phaser', () => {
    cy.selectChildProfile('Leo')
    cy.visit('/game/1')
    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state).to.exist
      expect(state.npcEnabled).to.be.a('boolean')
      expect(state.ttsEnabled).to.be.a('boolean')
    })
  })
})
