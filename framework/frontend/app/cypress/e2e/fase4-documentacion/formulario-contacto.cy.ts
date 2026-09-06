describe('Formulario de contacto público (SPRINT-033)', () => {
  beforeEach(() => {
    cy.viewport(1280, 800)
    cy.visit('/docs/contacto')
    cy.get('.contact-view', { timeout: 8000 }).should('exist')
  })

  it('positivo: completar formulario con datos válidos y enviar confirma el envío', () => {
    cy.get('.nubi-alert--warning').should('be.visible')
      .and('contain.text', 'No incluyas datos de menores')

    cy.get('.nubi-textarea__input').should('be.visible')

    cy.get('.nubi-checkbox__label')
      .contains('persona adulta responsable')
      .should('be.visible')

    cy.get('.nubi-button').should('be.visible').and('be.disabled')

    cy.get('.nubi-textarea__input')
      .clear()
      .type('Este es un mensaje de prueba desde el test E2E de contacto.')

    cy.get('.nubi-checkbox__label')
      .contains('persona adulta responsable')
      .click()

    cy.get('.nubi-button').should('not.be.disabled')

    cy.intercept('POST', '**/api/v1/contact').as('contactRequest')

    cy.get('.nubi-button').click()

    cy.wait('@contactRequest').then((interception) => {
      expect(interception.request.body).to.have.property('message')
      expect(interception.request.body).to.have.property('type')
      expect(interception.request.body.message).to.include('mensaje de prueba')
    })

    cy.get('@contactRequest').its('response.statusCode').should('be.oneOf', [200, 201, 202])

    cy.get('.nubi-alert--success', { timeout: 10000 })
      .should('be.visible')
      .and('contain.text', 'Tu mensaje ha sido recibido')
  })

  it('negativo: textarea vacío bloquea el envío sin llamar a la API', () => {
    cy.get('.nubi-textarea__input').should('have.value', '')

    cy.get('.nubi-checkbox__label')
      .contains('persona adulta responsable')
      .click()

    cy.get('.nubi-button').should('be.disabled')

    let apiCalled = false
    cy.intercept('POST', '**/api/v1/contact', () => {
      apiCalled = true
    }).as('contactAttempt')

    cy.get('.nubi-button').click({ force: true })

    cy.wait(500).then(() => {
      expect(apiCalled).to.be.false
    })
  })

  it('negativo: checkbox de confirmación adulta sin marcar bloquea el envío sin llamar a la API', () => {
    cy.get('.nubi-textarea__input')
      .clear()
      .type('Mensaje de prueba sin confirmar adultez.')

    cy.get('.nubi-button').should('be.disabled')

    let apiCalled = false
    cy.intercept('POST', '**/api/v1/contact', () => {
      apiCalled = true
    }).as('contactAttempt')

    cy.get('.nubi-button').click({ force: true })

    cy.wait(500).then(() => {
      expect(apiCalled).to.be.false
    })
  })
})
