import { createFamilyViaApi, createChildViaApi, uniqueFamilyName } from '../../support/testData'

const PIN = '1234'

function loginAndOpenDiary(childName: string) {
  cy.visit('/')
  cy.get('.home-view').should('exist')
  cy.get('[aria-label="Configuración"]').should('be.visible').click()
  cy.contains('Acceso parental').should('be.visible')
  cy.get('.nubi-pin-input__digit').first().type(PIN)
  cy.url().should('include', '/panel')
  cy.get('.panel-cover-view__card').contains('Niños').click({ force: true })
  cy.url().should('include', '/panel/ninos')

  cy.intercept('GET', '**/api/v1/family/children/*').as('getChild')
  cy.contains('.parental-child-card', childName).click({ force: true })
  cy.wait('@getChild')
  cy.get('.child-profile-edit-view').should('be.visible')

  cy.contains('button', 'Diario').click({ force: true })
  cy.url().should('include', '/diary')
}

function summaryBody(playedTimeMinutes: number, uniqueActivitiesCompleted: number) {
  return { success: true, message: null, errors: null, data: { playedTimeMinutes, uniqueActivitiesCompleted } }
}

function activitiesBody(activities: Array<{
  activityId: number
  name: string
  category: string
  subcategory: string | null
  engine: string
  currentDifficulty: string
}>) {
  return { success: true, message: null, errors: null, data: activities }
}

function abandonmentBody(data: { activityId: number; abandonmentCount: number } | null) {
  return { success: true, message: null, errors: null, data }
}

describe('Diario parental - vista y filtros (SPRINT-085)', () => {
  it('periodo por defecto es Semana y muestra resumen + lista ordenada por categoria', () => {
    const familyName = uniqueFamilyName('Diary')
    const childName = `Nubi-${Date.now()}`
    createFamilyViaApi({ name: familyName, pin: PIN })
    createChildViaApi({ name: childName, birthday: '2022-03-15', avatar: 'avatar-1' })

    cy.intercept('GET', '**/api/v1/diary/children/*/summary*', (req) => {
      expect(req.url).to.include('period=WEEK')
      req.reply(summaryBody(45, 2))
    }).as('getSummary')

    cy.intercept('GET', '**/api/v1/diary/children/*/activities*', (req) => {
      expect(req.url).to.include('period=WEEK')
      req.reply(activitiesBody([
        { activityId: 3, name: 'Memoria de animales', category: 'MEMORY', subcategory: null, engine: 'MEMORY', currentDifficulty: 'HARD' },
        { activityId: 1, name: 'Letras del bosque', category: 'RECOGNITION', subcategory: 'LETTER', engine: 'RECOGNITION', currentDifficulty: 'MEDIUM' },
        { activityId: 2, name: 'Grande y pequeño', category: 'COMPARISON', subcategory: null, engine: 'COMPARISON', currentDifficulty: 'EASY' }
      ]))
    }).as('getActivities')

    cy.intercept('GET', '**/api/v1/diary/children/*/abandonment-signal*', (req) => {
      req.reply(abandonmentBody(null))
    }).as('getAbandonment')

    loginAndOpenDiary(childName)
    cy.wait(['@getSummary', '@getActivities'])

    cy.get('.diary-period-filter button[aria-pressed="true"]').should('contain.text', 'Semana')

    cy.contains('.diary-summary__stat-value', '45').should('be.visible')
    cy.contains('.diary-summary__stat-value', '2').should('be.visible')

    cy.get('.diary-activity-list__group-title').then(($titles) => {
      const titles = [...$titles].map((el) => el.textContent?.trim())
      expect(titles).to.deep.equal(['Reconocimiento', 'Comparación', 'Memoria'])
    })

    cy.contains('.diary-activity-card', 'Letras del bosque').within(() => {
      cy.contains('Reconocimiento · Letras').should('be.visible')
      cy.contains('Nivel actual de esta actividad: Normal.').should('be.visible')
    })
  })

  it('cambiar de periodo recarga resumen y actividades', () => {
    const familyName = uniqueFamilyName('DiaryPeriod')
    const childName = `Nubi-${Date.now()}`
    createFamilyViaApi({ name: familyName, pin: PIN })
    createChildViaApi({ name: childName, birthday: '2022-03-15', avatar: 'avatar-1' })

    cy.intercept('GET', '**/api/v1/diary/children/*/summary*', (req) => {
      const isMonth = req.url.includes('period=MONTH')
      req.reply(summaryBody(isMonth ? 120 : 0, isMonth ? 4 : 0))
    }).as('getSummary')
    cy.intercept('GET', '**/api/v1/diary/children/*/activities*', (req) => {
      const isMonth = req.url.includes('period=MONTH')
      req.reply(activitiesBody(isMonth
        ? [{ activityId: 1, name: 'Letras del bosque', category: 'RECOGNITION', subcategory: 'LETTER', engine: 'RECOGNITION', currentDifficulty: 'EASY' }]
        : []))
    }).as('getActivities')
    cy.intercept('GET', '**/api/v1/diary/children/*/abandonment-signal*', (req) => {
      req.reply(abandonmentBody(null))
    })

    loginAndOpenDiary(childName)
    cy.wait(['@getSummary', '@getActivities'])
    cy.contains(/^Hoy todavía no ha jugado$/).should('be.visible')

    cy.intercept('GET', '**/api/v1/diary/children/*/summary*period=MONTH*').as('getSummaryMonth')
    cy.intercept('GET', '**/api/v1/diary/children/*/activities*period=MONTH*').as('getActivitiesMonth')
    cy.contains('.diary-period-filter button', 'Mes').click({ force: true })
    cy.wait(['@getSummaryMonth', '@getActivitiesMonth'])

    cy.contains('.diary-summary__stat-value', '120').should('be.visible')
    cy.contains('.diary-activity-card', 'Letras del bosque').should('be.visible')
  })

  it('senal de abandono solo se muestra cuando el backend la confirma', () => {
    const familyName = uniqueFamilyName('DiaryAbandon')
    const childName = `Nubi-${Date.now()}`
    createFamilyViaApi({ name: familyName, pin: PIN })
    createChildViaApi({ name: childName, birthday: '2022-03-15', avatar: 'avatar-1' })

    cy.intercept('GET', '**/api/v1/diary/children/*/summary*', { body: summaryBody(30, 2) })
    cy.intercept('GET', '**/api/v1/diary/children/*/activities*', {
      body: activitiesBody([
        { activityId: 1, name: 'Letras del bosque', category: 'RECOGNITION', subcategory: 'LETTER', engine: 'RECOGNITION', currentDifficulty: 'EASY' },
        { activityId: 2, name: 'Grande y pequeño', category: 'COMPARISON', subcategory: null, engine: 'COMPARISON', currentDifficulty: 'EASY' }
      ])
    }).as('getActivities')
    cy.intercept('GET', '**/api/v1/diary/children/*/abandonment-signal*activityId=1*', {
      body: abandonmentBody({ activityId: 1, abandonmentCount: 4 })
    })
    cy.intercept('GET', '**/api/v1/diary/children/*/abandonment-signal*activityId=2*', {
      body: abandonmentBody(null)
    })

    loginAndOpenDiary(childName)
    cy.wait('@getActivities')

    cy.contains('.diary-activity-card', 'Letras del bosque')
      .contains('Esta actividad ha sido abandonada varias veces recientemente')
      .should('be.visible')

    cy.contains('.diary-activity-card', 'Grande y pequeño')
      .contains('Esta actividad ha sido abandonada varias veces recientemente')
      .should('not.exist')
  })
})
