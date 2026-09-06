describe('Smoke test', () => {
  it('carga la aplicación y monta Vue en #app', () => {
    cy.visit('/')
    cy.title().should('eq', 'My Friend Nubi')
    cy.get('#app').should('exist')
  })
})
