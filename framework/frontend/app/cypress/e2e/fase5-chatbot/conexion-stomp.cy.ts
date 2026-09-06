import { createFamilyViaApi, uniqueFamilyName } from '../../support/testData'

const PIN = '1234'
const STOMP_TIMEOUT = 15000
const CHATBOT_LOAD_TIMEOUT = 20000

function assertParentChannelStatus(expected: string, timeout = STOMP_TIMEOUT) {
  cy.window().should((win) => {
    const pinia = (win as any).__nubi_pinia
    const wsStore = pinia?._s?.get('ws')
    const actualStatus = wsStore ? wsStore.parentChannelStatus : 'disconnected'
    expect(actualStatus).to.eq(expected)
  }, { timeout })
}

describe('Conexión STOMP del panel parental (SPRINT-034)', () => {
  before(() => {
    createFamilyViaApi({ name: uniqueFamilyName('STOMP'), pin: PIN })
  })

  beforeEach(() => {
    cy.viewport(1280, 800)
    cy.loginAsParent(PIN)
  })

  it('positivo: al entrar en /panel se establece la conexión STOMP', () => {
    cy.visit('/panel')
    cy.get('.parent-panel-layout', { timeout: 10000 }).should('exist')
    assertParentChannelStatus('connected')
  })

  it('positivo: navegar entre secciones del panel no reconecta (la conexión persiste)', () => {
    cy.visit('/panel')
    cy.get('.parent-panel-layout', { timeout: 10000 }).should('exist')
    assertParentChannelStatus('connected')

    cy.get('.parent-sidebar__link').contains('Configuración').click()
    cy.url().should('include', '/panel/configuracion')
    cy.get('.parent-panel-layout', { timeout: 8000 }).should('exist')
    assertParentChannelStatus('connected', 3000)

    cy.get('.parent-sidebar__link').contains('Chatbot').click()
    cy.url().should('include', '/panel/chatbot')
    cy.get('.chatbot-view', { timeout: CHATBOT_LOAD_TIMEOUT }).should('exist')
    assertParentChannelStatus('connected', 3000)
  })

  it('negativo: al salir del árbol /panel la conexión STOMP se cierra', () => {
    cy.visit('/panel')
    cy.get('.parent-panel-layout', { timeout: 10000 }).should('exist')
    assertParentChannelStatus('connected')

    cy.get('.parent-sidebar__link').contains('Documentación').click()
    cy.get('.documentation-layout', { timeout: 8000 }).should('exist')
    assertParentChannelStatus('disconnected')
  })
})
