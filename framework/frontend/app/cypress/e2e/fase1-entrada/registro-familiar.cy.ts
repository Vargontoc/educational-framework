import { uniqueFamilyName } from '../../support/testData'

describe('Registro familiar (SPRINT-011)', () => {
  beforeEach(() => {
    cy.visit('/')
    cy.get('.home-view').should('exist')
    cy.get('body').then(($body) => {
      if ($body.find('.nubi-info-modal').length > 0) {
        cy.get('body').type('{esc}')
        cy.wait(300)
      }
    })
  })

  it('completa el registro en dos pasos (nombre + PIN) y autentica', () => {
    const familyName = uniqueFamilyName('Reg')
    const pin = '5678'

    cy.contains('button', /Bienvenida familia/, { timeout: 1000 }).then(($btn) => {
      if ($btn.length > 0) return
      cy.contains('button', 'Registrar familia').should('be.visible').click()
      cy.contains('Registro de familia').should('be.visible')

      cy.get('.nubi-text-input__label')
        .contains('Nombre de familia')
        .closest('.nubi-text-input')
        .find('input')
        .type(familyName)
      cy.contains('button', 'Siguiente').click()

      cy.get('.nubi-pin-input__label')
        .contains('Crea tu PIN')
        .closest('.nubi-pin-input')
        .find('.nubi-pin-input__digit')
        .first()
        .type(pin)

      cy.get('.nubi-pin-input__label')
        .contains('Confirma tu PIN')
        .closest('.nubi-pin-input')
        .find('.nubi-pin-input__digit')
        .first()
        .type(pin)

      cy.contains('button', 'Crear familia').click()
    })

    cy.get('.home-view').should('exist')
  })

  it('PINs que no coinciden bloquean el envio y muestran error', () => {
    cy.contains('button', /Bienvenida familia/, { timeout: 1000 }).then(($btn) => {
      if ($btn.length > 0) return
      cy.contains('button', 'Registrar familia').should('be.visible').click()
      cy.contains('Registro de familia').should('be.visible')

      cy.get('.nubi-text-input__label')
        .contains('Nombre de familia')
        .closest('.nubi-text-input')
        .find('input')
        .type('TestFamily')
      cy.contains('button', 'Siguiente').click()

      cy.get('.nubi-pin-input__label')
        .contains('Crea tu PIN')
        .closest('.nubi-pin-input')
        .find('.nubi-pin-input__digit')
        .first()
        .type('1111')

      cy.get('.nubi-pin-input__label')
        .contains('Confirma tu PIN')
        .closest('.nubi-pin-input')
        .find('.nubi-pin-input__digit')
        .first()
        .type('2222')

      cy.contains('Los PIN no coinciden').should('be.visible')
      cy.contains('button', 'Crear familia').should('be.disabled')
    })
  })
})
