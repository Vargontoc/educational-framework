import { createFamilyViaApi, createChildViaApi, uniqueFamilyName } from '../../support/testData'

describe('Harness: estrategia de datos de prueba', () => {
  const familyName = uniqueFamilyName('Datos')

  before(() => {
    createFamilyViaApi({ name: familyName, pin: '1234' })
  })

  it('crea un perfil infantil vía API y verifica que aparece en el listado', () => {
    cy.loginAsParent()
    createChildViaApi({ name: 'Ana', birthday: '2022-06-20', avatar: 'avatar-02' }).then(
      (response) => {
        expect(response.status).to.be.oneOf([200, 201])
        expect(response.body.success).to.eq(true)
        expect(response.body.data.name).to.eq('Ana')
      }
    )
    cy.request('/api/v1/family/children').then((response) => {
      expect(response.body.success).to.eq(true)
      const names = response.body.data.map((c: { name: string }) => c.name)
      expect(names).to.include('Ana')
    })
  })

  it('cada spec es independiente: crea sus propios datos', () => {
    cy.loginAsParent()
    createChildViaApi({ name: 'Dani', birthday: '2021-11-10', avatar: 'avatar-03' }).then(
      (response) => {
        expect(response.body.success).to.eq(true)
        expect(response.body.data.name).to.eq('Dani')
      }
    )
  })
})
