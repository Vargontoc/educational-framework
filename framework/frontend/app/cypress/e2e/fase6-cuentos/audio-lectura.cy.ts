import { createFamilyViaApi, uniqueFamilyName } from '../../support/testData'

const PIN = '1234'
const LOAD_TIMEOUT = 15000
const TRANSITION_WAIT = 400
const AUTOPLAY_WAIT = 800

function loginViaUi() {
  cy.visit('/')
  cy.get('.home-view').should('exist')
  cy.get('[aria-label="Configuración"]').should('be.visible').click()
  cy.contains('Acceso parental').should('be.visible')
  cy.get('.nubi-pin-input__digit').first().type(PIN)
  cy.url().should('include', '/panel')
}

function navigateToReader() {
  loginViaUi()
  cy.get('.parent-sidebar', { timeout: LOAD_TIMEOUT }).should('be.visible')
  cy.get('.parent-sidebar__link').contains('Lectura').click()
  cy.get('.grid-catalog .story-cover', { timeout: LOAD_TIMEOUT }).first().click()
  cy.url().should('include', '/story/')
  cy.get('.story-container', { timeout: LOAD_TIMEOUT }).should('exist')
}

describe('Audio de lectura: altavoz, repetición y autoplay (SPRINT-040)', () => {
  before(() => {
    createFamilyViaApi({ name: uniqueFamilyName('CatAudio'), pin: PIN })
  })

  beforeEach(() => {
    cy.viewport(1280, 800)
    cy.on('uncaught:exception', () => false)
  })

  it('positivo: activar voz y pulsar altavoz dispara la petición de audio de la página', () => {
    cy.intercept({
      method: 'GET',
      url: '**/api/v1/stories/*/pages/*/audio**',
    }, {
      statusCode: 200,
      body: '',
      headers: { 'content-type': 'audio/mpeg' }
    }).as('getAudio')

    navigateToReader()

    cy.get('.nubi-toggle').should('exist')
    cy.get('.nubi-toggle__input').should('not.be.checked')
    cy.get('.nubi-toggle').click()
    cy.get('.nubi-toggle__input').should('be.checked')

    cy.get('.story-nav-slot--next .nubi-icon-button').click()
    cy.wait(TRANSITION_WAIT)
    cy.get('.story-footer__speaker').should('be.visible').and('not.be.disabled')
    cy.get('.story-footer__speaker').click()
    cy.wait('@getAudio', { timeout: 5000 })
  })

  it('positivo: con autoplay, cambiar de página dispara el audio automáticamente', () => {
    cy.intercept({
      method: 'GET',
      url: '**/api/v1/stories/*/pages/*/audio**',
    }, {
      statusCode: 200,
      body: '',
      headers: { 'content-type': 'audio/mpeg' }
    }).as('getAudioAuto')

    navigateToReader()

    cy.get('.nubi-toggle').click()
    cy.get('.nubi-toggle__input').should('be.checked')
    cy.get('.story-nav-slot--next .nubi-icon-button').click()
    cy.wait('@getAudioAuto', { timeout: AUTOPLAY_WAIT + 5000 })
  })

  it('positivo: repetir audio (replay) vuelve a solicitar el recurso', () => {
    cy.intercept({
      method: 'GET',
      url: '**/api/v1/stories/*/pages/*/audio**',
    }, {
      statusCode: 200,
      body: '',
      headers: { 'content-type': 'audio/mpeg' }
    }).as('getAudioReplay')

    navigateToReader()

    cy.get('.nubi-toggle').click()
    cy.get('.nubi-toggle__input').should('be.checked')
    cy.get('.story-nav-slot--next .nubi-icon-button').click()
    cy.wait(TRANSITION_WAIT)
    cy.wait('@getAudioReplay', { timeout: AUTOPLAY_WAIT + 5000 })
    cy.get('.story-footer__speaker').click()
    cy.wait('@getAudioReplay', { timeout: 5000 })
  })

  it('negativo: sin activar voz, el botón de altavoz está deshabilitado', () => {
    navigateToReader()

    cy.get('.nubi-toggle__input').should('not.be.checked')
    cy.get('.story-nav-slot--next .nubi-icon-button').click()
    cy.wait(TRANSITION_WAIT)
    cy.get('.story-footer__speaker').should('be.visible').and('be.disabled')
  })

  it('negativo: desactivar voz después de activarla deshabilita el altavoz', () => {
    navigateToReader()

    cy.get('.nubi-toggle').click()
    cy.get('.nubi-toggle__input').should('be.checked')

    cy.get('body').then(($body) => {
      const hasNext = $body.find('.story-nav-slot--next .nubi-icon-button').length > 0
      if (hasNext) {
        cy.get('.story-nav-slot--next .nubi-icon-button').click()
        cy.wait(TRANSITION_WAIT)
        cy.get('.story-footer__speaker').should('not.be.disabled')

        cy.get('.story-nav-slot--prev .nubi-icon-button').click()
        cy.wait(TRANSITION_WAIT)
        cy.get('.nubi-toggle').click()
        cy.get('.nubi-toggle__input').should('not.be.checked')

        cy.get('.story-nav-slot--next .nubi-icon-button').click()
        cy.wait(TRANSITION_WAIT)
        cy.get('.story-footer__speaker').should('be.disabled')
      } else {
        cy.get('.nubi-toggle').click()
        cy.get('.nubi-toggle__input').should('not.be.checked')
      }
    })
  })
})
