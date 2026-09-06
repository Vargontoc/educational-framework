import { createFamilyViaApi, createChildViaApi, uniqueFamilyName } from '../../support/testData'

const PIN = '1234'
const STREAM_TIMEOUT = 30000
const LOAD_TIMEOUT = 20000

function dispatchStompEvent(event: object) {
  cy.window().then((win) => {
    const pinia = (win as any).__nubi_pinia
    const wsStore = pinia._s.get('ws')
    wsStore.handleChatbotEvent({ body: JSON.stringify(event) })
  })
}

function setupChatbotIntercepts(children: object[] = []) {
  cy.intercept('GET', '**/api/v1/agents/conversations*', {
    success: true, message: null, errors: [], data: []
  })
  cy.intercept('GET', '**/api/v1/agents/conversations/commands*', {
    success: true, message: null, errors: [], data: []
  })
  cy.intercept('GET', '**/api/v1/family/children*', {
    success: true, message: null, errors: [], data: children
  })
}

function forceConnected() {
  cy.window().then((win) => {
    const pinia = (win as any).__nubi_pinia
    const wsStore = pinia._s.get('ws')
    wsStore.parentChannelStatus = 'connected'
  })
}

describe('Interacción de chat: envío, streaming y atajo @ (SPRINT-036)', () => {
  beforeEach(() => {
    cy.viewport(1280, 800)
  })

  it('positivo: enviar un mensaje muestra streaming y el mensaje final en el historial', () => {
    createFamilyViaApi({ name: uniqueFamilyName('ChatPos'), pin: PIN })
    createChildViaApi({ name: 'Leo' })
    cy.loginAsParent(PIN)

    setupChatbotIntercepts([{ id: 1, name: 'Leo', birthday: '2022-03-15', avatar: 'avatar-01' }])
    cy.visit('/panel/chatbot')
    cy.get('.chatbot-view', { timeout: LOAD_TIMEOUT }).should('exist')
    forceConnected()

    cy.get('.nubi-textarea__input').should('be.visible').type('Hola Nubi')
    cy.get('.chatbot-view__input-row').find('button').click()

    cy.get('.chatbot-view__message--user', { timeout: 5000 }).should('contain.text', 'Hola Nubi')

    const convId = 'e2e-conv-' + Date.now()
    dispatchStompEvent({ event: 'TOKEN', conversationId: convId, content: 'Hola ' })
    dispatchStompEvent({ event: 'TOKEN', conversationId: convId, content: 'amigo' })

    cy.get('.chatbot-view__message--live', { timeout: 5000 }).should('be.visible')
    cy.get('.chatbot-view__message-content--live').should('contain.text', 'Hola')

    dispatchStompEvent({ event: 'COMPLETE', conversationId: convId, content: 'Hola amigo' })

    cy.get('.chatbot-view__message--assistant', { timeout: STREAM_TIMEOUT })
      .last()
      .should('contain.text', 'Hola amigo')

    cy.get('.chatbot-view__message--live').should('not.exist')
  })

  it('positivo: el atajo @ sugiere perfiles infantiles y permite mencionar', () => {
    createFamilyViaApi({ name: uniqueFamilyName('ChatMen'), pin: PIN })
    createChildViaApi({ name: 'Marta' })
    createChildViaApi({ name: 'Miguel' })
    cy.loginAsParent(PIN)

    setupChatbotIntercepts([
      { id: 1, name: 'Marta', birthday: '2022-03-15', avatar: 'avatar-01' },
      { id: 2, name: 'Miguel', birthday: '2021-07-20', avatar: 'avatar-02' }
    ])
    cy.visit('/panel/chatbot')
    cy.get('.chatbot-view', { timeout: LOAD_TIMEOUT }).should('exist')
    forceConnected()

    cy.get('.nubi-textarea__input').should('be.visible').type('Pregunta para @M')

    cy.get('.chatbot-view__mentions', { timeout: 5000 }).should('be.visible')
    cy.get('.chatbot-view__mention').should('have.length.gte', 1)
    cy.get('.chatbot-view__mention').first().should('contain.text', 'M')

    cy.get('.chatbot-view__mention').first().click()

    cy.get('.nubi-textarea__input').should('have.value', 'Pregunta para @Marta ')
    cy.get('.chatbot-view__mentions').should('not.exist')
  })

  it('negativo: un mensaje que excede maxLength (4000) no se puede enviar', () => {
    createFamilyViaApi({ name: uniqueFamilyName('ChatMax'), pin: PIN })
    cy.loginAsParent(PIN)

    setupChatbotIntercepts()
    cy.visit('/panel/chatbot')
    cy.get('.chatbot-view', { timeout: LOAD_TIMEOUT }).should('exist')
    forceConnected()

    const longText = 'a'.repeat(4000)
    cy.get('.nubi-textarea__input').should('be.visible').type(longText, { delay: 0 })

    cy.get('.nubi-textarea__input').should('have.value.length', 4000)

    cy.get('.nubi-textarea__input').type('b')
    cy.get('.nubi-textarea__input').should('have.value.length', 4000)

    cy.get('.nubi-textarea__counter').should('contain.text', '4000 / 4000')
  })

  it('negativo: un ERROR de streaming desbloquea el chat y permite reintentar', () => {
    createFamilyViaApi({ name: uniqueFamilyName('ChatErr'), pin: PIN })
    cy.loginAsParent(PIN)

    setupChatbotIntercepts()
    cy.visit('/panel/chatbot')
    cy.get('.chatbot-view', { timeout: LOAD_TIMEOUT }).should('exist')
    forceConnected()

    cy.get('.nubi-textarea__input').should('be.visible').type('Test error')
    cy.get('.chatbot-view__input-row').find('button').click()

    cy.get('.chatbot-view__message--user', { timeout: 5000 }).should('contain.text', 'Test error')

    const convId = 'e2e-conv-err-' + Date.now()
    dispatchStompEvent({ event: 'TOKEN', conversationId: convId, content: 'Parcial...' })
    cy.get('.chatbot-view__message--live', { timeout: 5000 }).should('be.visible')

    dispatchStompEvent({ event: 'ERROR', conversationId: convId, content: 'Error del modelo' })

    cy.get('.nubi-alert', { timeout: 5000 }).should('be.visible')
    cy.get('.nubi-alert').should('contain.text', 'Error del modelo')

    cy.get('.nubi-textarea__input').should('not.be.disabled')
    cy.get('.chatbot-view__message--live').should('not.exist')

    cy.get('.nubi-textarea__input').clear().type('Reintento')
    cy.get('.chatbot-view__input-row').find('button').should('not.be.disabled')
  })
})
