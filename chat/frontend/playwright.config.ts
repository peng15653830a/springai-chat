import { defineConfig, devices } from '@playwright/test'

const DIRNAME = new URL('.', import.meta.url).pathname
const BACKEND_DIR = new URL('../backend/', import.meta.url).pathname

export default defineConfig({
  testDir: './tests',
  timeout: 30_000,
  retries: process.env.CI ? 2 : 0,
  reporter: 'list',
  use: {
    baseURL: 'http://localhost:3000',
    trace: 'on-first-retry'
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] }
    }
  ],
  webServer: [
    // 启动后端（H2内存库，避免外部依赖）
    {
      command: 'bash -lc "EMBEDDED_DB=true mvn -q -Dpmd.skip=true -Dspotless.skip=true -DskipTests spring-boot:run"',
      cwd: BACKEND_DIR,
      port: 8080,
      reuseExistingServer: !process.env.CI,
      stdout: 'pipe',
      stderr: 'pipe'
    },
    // 启动前端
    {
      command: 'npm run dev',
      cwd: DIRNAME,
      port: 3000,
      reuseExistingServer: !process.env.CI,
      stdout: 'pipe',
      stderr: 'pipe'
    }
  ]
})
