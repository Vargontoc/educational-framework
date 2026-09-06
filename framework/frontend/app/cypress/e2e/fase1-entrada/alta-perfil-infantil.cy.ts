import { createFamilyViaApi, uniqueFamilyName } from '../../support/testData'

const PIN = '1234'

function typePin(pin: string) {
  pin.split('').forEach((digit, i) => {
    cy.get('.nubi-pin-input__digit:visible').eq(i).focus().type(digit)
  })
}

describe('Alta de perfil infantil (SPRINT-013)', () => {
  beforeEach(() => {
    createFamilyViaApi({ name: uniqueFamilyName('Child'), pin: PIN })
  })

  it('con verificacion parental superada, da de alta un perfil valido', () => {
    cy.visit('/')
    cy.contains('button', /Bienvenida familia/, { timeout: 8000 }).should('be.visible').click({ force: true })
    cy.contains('Registrar niño', { timeout: 8000 }).should('be.visible').click()
    cy.contains('Verificación parental').should('be.visible')

    typePin(PIN)

    cy.get('input[placeholder="Introduce el nombre"]', { timeout: 8000 }).should('be.visible').type('Lucía')
    cy.contains('button', 'Siguiente').click({ force: true })

    cy.get('#child-birthday').type('2022-03-15', { force: true })
    cy.contains('button', 'Crear perfil').click({ force: true })

    cy.contains('Lucía', { timeout: 8000 }).should('be.visible')
  })

  it('PIN incorrecto en verificacion parental bloquea el acceso al formulario de alta', () => {
    cy.visit('/')
    cy.contains('button', /Bienvenida familia/, { timeout: 8000 }).should('be.visible').click({ force: true })
    cy.contains('Registrar niño', { timeout: 8000 }).should('be.visible').click()
    cy.contains('Verificación parental').should('be.visible')

    typePin('0000')

    cy.contains('PIN incorrecto', { timeout: 8000 }).should('be.visible')
    cy.contains('button', 'Crear perfil').should('not.exist')
    cy.contains('Introduce el nombre').should('not.exist')
  })
})
