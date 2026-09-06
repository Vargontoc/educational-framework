import { defineConfig } from 'cypress'

export default defineConfig({
  e2e: {
    baseUrl: process.env.CYPRESS_BASE_URL || 'http://localhost:80',
    specPattern: 'cypress/e2e/**/*.cy.ts',
    supportFile: 'cypress/support/e2e.ts',
    video: true,
    videosFolder: 'cypress/results/videos',
    screenshotsFolder: 'cypress/results/screenshots',
    downloadsFolder: 'cypress/results/downloads'
  },
  reporter: 'junit',
  reporterOptions: {
    mochaFile: 'cypress/results/junit/results-[hash].xml',
    toConsole: true
  }
})
