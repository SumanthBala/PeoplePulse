import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  timeout: 30000,
  use: {
    baseURL: 'http://localhost:8080',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure'
  },
  webServer: {
    command: 'cd ../backend && mvn spring-boot:run',
    url: 'http://localhost:8080',
    reuseExistingServer: true,
    timeout: 120000
  }
});
