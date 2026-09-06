describe('App Shell (SPRINT-001, SPRINT-002)', () => {
  it('carga la app, renderiza el shell y muestra la portada neutral', () => {
    cy.visit('/')
    cy.title().should('eq', 'My Friend Nubi')
    cy.get('#app').should('exist')
    cy.get('.home-view').should('exist')
    cy.get('.home-view__avatar').should('be.visible')
  })

  it('navega a /docs sin error (ruta publica)', () => {
    cy.visit('/docs')
    cy.url().should('include', '/docs')
    cy.get('#app').should('exist')
  })

  it('redirige /panel a Home sin sesion parental activa', () => {
    cy.clearAllSessionStorage()
    cy.visit('/panel')
    cy.url().should('not.include', '/panel')
    cy.get('#app').should('exist')
  })

  it('redirige /game/:childId a Home sin sesion infantil activa', () => {
    cy.clearAllSessionStorage()
    cy.visit('/game/1')
    cy.url().should('not.include', '/game')
    cy.get('#app').should('exist')
  })
})
