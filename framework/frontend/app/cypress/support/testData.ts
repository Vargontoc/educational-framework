export interface TestFamily {
  name: string
  pin: string
}

export interface TestChild {
  name: string
  birthday: string
  avatar: string
}

export const DEFAULT_FAMILY: TestFamily = {
  name: 'Familia E2E',
  pin: '1234'
}

export const DEFAULT_CHILD: TestChild = {
  name: 'Leo',
  birthday: '2022-03-15',
  avatar: 'avatar-01'
}

export function uniqueFamilyName(base: string = 'Familia'): string {
  return `${base}-${Date.now()}`
}

export function createFamilyViaApi(family?: Partial<TestFamily>) {
  const payload = {
    name: family?.name ?? uniqueFamilyName(),
    pin: family?.pin ?? DEFAULT_FAMILY.pin,
    ttsEnabled: true,
    agentEnabled: true
  }
  return cy.request({
    method: 'POST',
    url: '/api/v1/family',
    body: payload,
    failOnStatusCode: false
  }).then((response) => {
    expect(response.status).to.be.oneOf([200, 201, 409])
    return response
  })
}

export function createChildViaApi(child?: Partial<TestChild>) {
  const payload = {
    name: child?.name ?? DEFAULT_CHILD.name,
    birthday: child?.birthday ?? DEFAULT_CHILD.birthday,
    avatar: child?.avatar ?? DEFAULT_CHILD.avatar,
    npcVoiceEnabled: true,
    npcEnabled: true,
    npcVoiceVolume: 100,
    colorVisionMode: 'NONE'
  }
  return cy.request({
    method: 'POST',
    url: '/api/v1/family/children',
    body: payload
  })
}
