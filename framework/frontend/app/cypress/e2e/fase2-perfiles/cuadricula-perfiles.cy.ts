import { createFamilyViaApi, createChildViaApi, uniqueFamilyName } from '../../support/testData'

const PIN = '1234'
const baseUrl = Cypress.config('baseUrl') || 'http://localhost:80'
let apiUrl: string
if (baseUrl.includes('app:')) {
  apiUrl = baseUrl.replace(/app(:\d+)?/, 'api:8080')
} else if (baseUrl.includes('8880')) {
  apiUrl = 'http://localhost:18080'
} else {
  apiUrl = 'http://localhost:8080'
}

function loginAndGoToPanel() {
  cy.visit('/')
  cy.get('.home-view').should('exist')
  cy.get('[aria-label="Configuración"]').should('be.visible').click()
  cy.contains('Acceso parental').should('be.visible')
  cy.get('.nubi-pin-input__digit').first().type(PIN)
  cy.url().should('include', '/panel')
  cy.contains('Panel parental').should('be.visible')
}

function goToNinos() {
  cy.get('.panel-cover-view__card').contains('Niños').click({ force: true })
  cy.url().should('include', '/panel/ninos')
}

describe('Cuadrícula de perfiles — sesiones, expulsar, bloquear (SPRINT-027)', () => {
  it('muestra perfiles con sesion activa y su duracion; expulsar cambia el estado visible', () => {
    const familyName = uniqueFamilyName('Grid')
    createFamilyViaApi({ name: familyName, pin: PIN })
    createChildViaApi({ name: 'Marco' })

    let realChildId = 0
    cy.request({
      method: 'POST', url: `${apiUrl}/api/v1/auth/login`, body: { pin: PIN }
    }).then((resp) => {
      const { token } = resp.body.data
      return cy.request({
        method: 'GET', url: `${apiUrl}/api/v1/family/children`,
        headers: { Authorization: `Bearer ${token}` }
      }).then((childResp) => {
        realChildId = childResp.body.data[0].id
      })
    }).then(() => {
      cy.intercept('GET', '**/api/v1/sessions/children*', {
        statusCode: 200,
        body: {
          success: true, message: null, errors: [],
          data: [{
            id: 999, childProfileId: realChildId, familyId: 1, status: 'ACTIVE',
            startedAt: new Date(Date.now() - 120000).toISOString(),
            endedAt: null, durationSeconds: 120,
            lastActivityAt: new Date().toISOString()
          }]
        }
      }).as('sessionsPoll')

      loginAndGoToPanel()
      goToNinos()

      cy.contains('.parental-child-card__name', 'Marco').should('be.visible')
      cy.contains('.parental-child-card__duration-value', /\d+:\d+/).should('be.visible')
      cy.contains('button', 'Expulsar').should('be.visible')

      cy.intercept('DELETE', '**/api/v1/sessions/children/*/expel', {
        statusCode: 200, body: { success: true, data: null }
      }).as('expelSession')

      cy.intercept('GET', '**/api/v1/sessions/children*', {
        statusCode: 200, body: { success: true, message: null, errors: [], data: [] }
      }).as('sessionsEmpty')

      cy.contains('button', 'Expulsar').click({ force: true })
      cy.contains('button', 'Confirmar').click({ force: true })
      cy.wait('@expelSession')

      cy.contains('.parental-child-card', 'Marco').within(() => {
        cy.contains('button', 'Expulsar').should('not.exist')
      })
    })
  })

  it('bloquear un perfil muestra el badge de bloqueado', () => {
    const familyName = uniqueFamilyName('GridBlock')
    createFamilyViaApi({ name: familyName, pin: PIN })
    createChildViaApi({ name: 'Sofía' })

    cy.intercept('GET', '**/api/v1/sessions/children*', {
      statusCode: 200, body: { success: true, message: null, errors: [], data: [] }
    }).as('sessionsPoll')

    loginAndGoToPanel()
    goToNinos()

    cy.contains('.parental-child-card__name', 'Sofía').should('be.visible')

    cy.intercept('PUT', '**/api/v1/family/children/activation/*', {
      statusCode: 200, body: { success: true, data: null }
    }).as('toggleActivation')

    cy.intercept('GET', '**/api/v1/family/children*', (req) => {
      req.continue((res) => {
        if (res.body.data && res.body.data.length > 0) {
          res.body.data[0].active = false
        }
      })
    }).as('childrenBlocked')

    cy.contains('button', 'Bloquear').click({ force: true })
    cy.wait('@toggleActivation')

    cy.contains('.parental-child-card__blocked-badge', 'Bloqueado').should('be.visible')
    cy.contains('button', 'Desbloquear').should('be.visible')
  })

  it('sin confirmar la expulsion, el perfil mantiene su sesion activa', () => {
    const familyName = uniqueFamilyName('GridCancel')
    createFamilyViaApi({ name: familyName, pin: PIN })
    createChildViaApi({ name: 'Diego' })

    let realChildId = 0
    cy.request({
      method: 'POST', url: `${apiUrl}/api/v1/auth/login`, body: { pin: PIN }
    }).then((resp) => {
      const { token } = resp.body.data
      return cy.request({
        method: 'GET', url: `${apiUrl}/api/v1/family/children`,
        headers: { Authorization: `Bearer ${token}` }
      }).then((childResp) => {
        realChildId = childResp.body.data[0].id
      })
    }).then(() => {
      cy.intercept('GET', '**/api/v1/sessions/children*', {
        statusCode: 200,
        body: {
          success: true, message: null, errors: [],
          data: [{
            id: 888, childProfileId: realChildId, familyId: 1, status: 'ACTIVE',
            startedAt: new Date(Date.now() - 60000).toISOString(),
            endedAt: null, durationSeconds: 60,
            lastActivityAt: new Date().toISOString()
          }]
        }
      }).as('sessionsPoll')

      loginAndGoToPanel()
      goToNinos()

      cy.contains('button', 'Expulsar').should('be.visible')
      cy.contains('button', 'Expulsar').click({ force: true })
      cy.contains('button', 'Cancelar').click({ force: true })
      cy.contains('button', 'Expulsar').should('be.visible')
    })
  })
})
