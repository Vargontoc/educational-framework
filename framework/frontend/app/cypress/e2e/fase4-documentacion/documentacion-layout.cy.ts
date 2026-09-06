describe('Layout de documentación y navegación lateral (SPRINT-031)', () => {
  beforeEach(() => {
    cy.viewport(1280, 800)
  })

  it('positivo: navegar entre secciones desde el sidebar carga el contenido sin recargar la SPA', () => {
    cy.visit('/docs')
    cy.url().should('include', '/docs/quien-soy')

    cy.get('.documentation-sidebar', { timeout: 8000 }).should('exist')
    cy.get('.documentation-sidebar__link').should('have.length', 5)

    cy.get('.documentation-sidebar__link--active')
      .should('exist')
      .and('contain.text', 'Quién soy')

    cy.get('.doc-section-view__content', { timeout: 6000 }).should('exist')

    const contentSelector = '.doc-section-view__content'

    cy.get(contentSelector).then(($el) => {
      const initialHtml = $el.html()

      cy.get('.documentation-sidebar__link')
        .contains('Primeros pasos')
        .click()

      cy.url().should('include', '/docs/primeros-pasos')
      cy.get('.documentation-sidebar__link--active')
        .should('contain.text', 'Primeros pasos')
      cy.get('.documentation-sidebar__link--active')
        .should('have.attr', 'aria-current', 'page')

      cy.get(contentSelector).should('exist')
      cy.get(contentSelector).then(($el2) => {
        expect($el2.html()).to.not.equal(initialHtml)
      })

      cy.get('.documentation-sidebar__link')
        .contains('Agentes AI')
        .click()

      cy.url().should('include', '/docs/agentes-ai')
      cy.get('.documentation-sidebar__link--active')
        .should('contain.text', 'Agentes AI')

      cy.get('.documentation-sidebar__link')
        .contains('Minijuegos')
        .click()

      cy.url().should('include', '/docs/minijuegos')
      cy.get('.documentation-sidebar__link--active')
        .should('contain.text', 'Minijuegos')
    })

    cy.window().then((win) => {
      expect(win.performance.getEntriesByType('navigation').length).to.be.greaterThan(0)
    })
  })

  it('positivo: la seccion Contacto se muestra dentro del layout de documentacion', () => {
    cy.visit('/docs')

    cy.get('.documentation-sidebar__link')
      .contains('Contacto')
      .click()

    cy.url().should('include', '/docs/contacto')
    cy.get('.documentation-sidebar__link--active')
      .should('contain.text', 'Contacto')
    cy.get('.contact-view', { timeout: 6000 }).should('exist')
    cy.get('.contact-view__title').should('contain.text', 'Contacto')
  })

  it('negativo: acceder a una seccion inexistente muestra estado no encontrado dentro del layout', () => {
    cy.visit('/docs/seccion-inexistente')

    cy.get('.documentation-layout', { timeout: 8000 }).should('exist')
    cy.get('.documentation-sidebar').should('exist')

    cy.get('.doc-section-view__not-found', { timeout: 6000 })
      .should('be.visible')
      .and('contain.text', 'Sección no encontrada')

    cy.get('.documentation-sidebar__link').should('have.length', 5)
  })
})
