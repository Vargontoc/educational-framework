const DEFAULT_PIN = '1234'

function persistAuth(token: string, sessionId: number, familyId: number) {
  const sessionData = JSON.stringify({
    familyId: String(familyId),
    selectedChildId: null,
    isAuthenticated: true
  })
  window.sessionStorage.setItem('nubi-session', sessionData)
  window.sessionStorage.setItem('nubi-parental-token', token)
  window.sessionStorage.setItem('nubi-parental-sessionId', String(sessionId))
  window.sessionStorage.setItem('nubi-parental-familyId', String(familyId))
  window.localStorage.setItem('nubi-e2e-auth', JSON.stringify({
    token, sessionId, familyId, sessionData
  }))
}

function restoreAuthFromSnapshot() {
  const raw = window.localStorage.getItem('nubi-e2e-auth')
  if (!raw) return
  const auth = JSON.parse(raw)
  window.sessionStorage.setItem('nubi-session', auth.sessionData)
  window.sessionStorage.setItem('nubi-parental-token', auth.token)
  window.sessionStorage.setItem('nubi-parental-sessionId', String(auth.sessionId))
  window.sessionStorage.setItem('nubi-parental-familyId', String(auth.familyId))
}

Cypress.Commands.add('loginAsParent', (pin: string = DEFAULT_PIN) => {
  cy.session(
    `parent-${pin}`,
    () => {
      cy.request({
        method: 'POST',
        url: '/api/v1/auth/login',
        body: { pin }
      }).then((response) => {
        expect(response.status).to.be.oneOf([200, 201])
        expect(response.body.success).to.eq(true)
        const { token, sessionId, familyId } = response.body.data
        persistAuth(token, sessionId, familyId)
      })
    },
    {
      validate() {
        restoreAuthFromSnapshot()
        expect(window.sessionStorage.getItem('nubi-parental-token')).to.not.be.null
      }
    }
  )
})

Cypress.Commands.add('selectChildProfile', (name: string) => {
  cy.session(
    `child-${name}`,
    () => {
      cy.request({
        method: 'POST',
        url: '/api/v1/auth/login',
        body: { pin: DEFAULT_PIN }
      }).then((loginResp) => {
        expect(loginResp.body.success).to.eq(true)
        const { token, sessionId, familyId } = loginResp.body.data
        persistAuth(token, sessionId, familyId)
        cy.request({
          method: 'GET',
          url: '/api/v1/family/children',
          headers: { Authorization: `Bearer ${token}` }
        }).then((childrenResp) => {
          expect(childrenResp.body.success).to.eq(true)
          const child = childrenResp.body.data.find(
            (c: { name: string; id: number }) => c.name === name
          )
          expect(child, `child profile "${name}" not found`).to.not.be.undefined
          cy.request({
            method: 'POST',
            url: '/api/v1/sessions/children',
            headers: { Authorization: `Bearer ${token}` },
            body: { childProfileId: child.id, heartbeatIntervalSeconds: 60 }
          }).then((sessionResp) => {
            expect(sessionResp.body.success).to.eq(true)
            const familyIdStr = String(familyId)
            const sessionData = JSON.stringify({
              familyId: familyIdStr,
              selectedChildId: String(child.id),
              isAuthenticated: true
            })
            window.sessionStorage.setItem('nubi-session', sessionData)
            const authRaw = window.localStorage.getItem('nubi-e2e-auth')
            if (authRaw) {
              const auth = JSON.parse(authRaw)
              auth.sessionData = sessionData
              window.localStorage.setItem('nubi-e2e-auth', JSON.stringify(auth))
            }
          })
        })
      })
    },
    {
      validate() {
        restoreAuthFromSnapshot()
        const session = JSON.parse(window.sessionStorage.getItem('nubi-session') || '{}')
        expect(session.selectedChildId).to.not.be.null
      }
    }
  )
})
