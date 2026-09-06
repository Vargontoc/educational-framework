import { createFamilyViaApi, createChildViaApi, uniqueFamilyName } from '../../support/testData'

const PIN = '1234'
const LOAD_TIMEOUT = 20000

describe('Estado de conversación del chatbot y carga inicial (SPRINT-035)', () => {
  beforeEach(() => {
    cy.viewport(1280, 800)
  })

  it('positivo: con conversación previa, entrar al chatbot carga y muestra su último estado', () => {
    createFamilyViaApi({ name: uniqueFamilyName('ConvPos'), pin: PIN })
    createChildViaApi({ name: 'Ana' })
    cy.loginAsParent(PIN)

    const mockConversation = {
      conversationId: 'test-conv-123',
      title: 'Conversación de prueba',
      startedAt: '2026-01-01T00:00:00Z',
      lastMessageAt: '2026-01-01T00:01:00Z',
      message: [
        { role: 'USER', content: 'Hola Nubi', createdAt: '2026-01-01T00:00:00Z' },
        { role: 'ASSISTANT', content: '¡Hola! Soy Nubi, tu amigo virtual.', createdAt: '2026-01-01T00:00:30Z' }
      ]
    }

    cy.intercept('GET', '**/api/v1/agents/conversations*', {
      success: true, message: null, errors: [], data: [mockConversation]
    }).as('getConversation')

    cy.visit('/panel/chatbot')
    cy.get('.chatbot-view', { timeout: LOAD_TIMEOUT }).should('exist')
    cy.wait('@getConversation', { timeout: 10000 })

    cy.get('.chatbot-view__messages', { timeout: LOAD_TIMEOUT }).should('exist')
    cy.get('.chatbot-view__message').should('have.length', 2)
    cy.get('.chatbot-view__message').first().should('contain.text', 'Hola Nubi')
    cy.get('.chatbot-view__message').last().should('contain.text', '¡Hola! Soy Nubi')
    cy.get('.chatbot-view__message-role').first().should('contain.text', 'Tú')
    cy.get('.chatbot-view__message-role').last().should('contain.text', 'Nubi')
  })

  it('negativo: sin conversación previa, el chatbot muestra estado vacío sin error', () => {
    createFamilyViaApi({ name: uniqueFamilyName('ConvNeg'), pin: PIN })
    cy.loginAsParent(PIN)

    cy.intercept('GET', '**/api/v1/agents/conversations*', {
      success: true, message: null, errors: [], data: []
    }).as('getEmptyConversation')

    cy.visit('/panel/chatbot')
    cy.get('.chatbot-view', { timeout: LOAD_TIMEOUT }).should('exist')
    cy.wait('@getEmptyConversation', { timeout: 10000 })

    cy.get('.nubi-empty-state', { timeout: LOAD_TIMEOUT }).should('be.visible')
    cy.get('.nubi-empty-state').should('contain.text', 'Aún no hay conversaciones')
    cy.get('.nubi-alert--error').should('not.exist')
  })

  it('negativo: si la API de conversaciones falla, el chatbot muestra estado vacío sin crash', () => {
    createFamilyViaApi({ name: uniqueFamilyName('ConvErr'), pin: PIN })
    cy.loginAsParent(PIN)

    cy.intercept('GET', '**/api/v1/agents/conversations*', {
      statusCode: 500,
      body: { success: false, message: 'Internal error', errors: ['Server error'], data: [] }
    }).as('getConversationError')

    cy.visit('/panel/chatbot')
    cy.get('.chatbot-view', { timeout: LOAD_TIMEOUT }).should('exist')
    cy.wait('@getConversationError', { timeout: 10000 })

    cy.get('.nubi-empty-state', { timeout: LOAD_TIMEOUT }).should('be.visible')
    cy.get('.nubi-alert--error').should('not.exist')
  })
})
