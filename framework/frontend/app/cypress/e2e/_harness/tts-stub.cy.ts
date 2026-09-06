import { createFamilyViaApi, createChildViaApi } from '../../support/testData'

describe('Harness: stub de audio/TTS', () => {
  before(() => {
    createFamilyViaApi({ name: 'TTS E2E', pin: '1234' })
    createChildViaApi({ name: 'Leo', birthday: '2022-03-15', avatar: 'avatar-01' })
  })

  it('patrón cy.intercept para stubear rutas TTS sin reproducir audio', () => {
    cy.intercept('POST', '**/tts/**', {
      statusCode: 200,
      body: { audioContent: '' }
    }).as('ttsPostRequest')

    cy.intercept('GET', '**/tts/**', {
      statusCode: 200,
      body: { audioContent: '' }
    }).as('ttsGetRequest')

    cy.loginAsParent()
    cy.visit('/panel')
    cy.url().should('include', '/panel')
  })

  it('el hook __NUBI_GAME_STATE__ refleja ttsEnabled como boolean', () => {
    cy.intercept('POST', '/api/v1/auth/login').as('login')
    cy.intercept('GET', '/api/v1/family/children').as('children')
    cy.intercept('POST', '/api/v1/sessions/children').as('openSession')
    cy.intercept('POST', '**/tts/**', { statusCode: 200, body: {} }).as('ttsStub')
    cy.intercept('GET', '**/tts/**', { statusCode: 200, body: {} }).as('ttsGetStub')

    cy.selectChildProfile('Leo')
    cy.visit('/game/1')
    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state).to.exist
      expect(state.ttsEnabled).to.be.a('boolean')
    })
  })
})
