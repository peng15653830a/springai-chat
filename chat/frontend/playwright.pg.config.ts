import { defineConfig, devices } from '@playwright/test'

const DIRNAME = new URL('.', import.meta.url).pathname
const BACKEND_DIR = new URL('../backend/', import.meta.url).pathname

export default defineConfig({
  testDir: './tests',
  timeout: 60_000,
  reporter: 'list',
  use: {
    baseURL: 'http://localhost:3000',
    trace: 'retain-on-failure'
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } }
  ],
  webServer: [
    {
      // 启动后端：使用真实PostgreSQL（读取application.yml配置），并传递密钥到环境变量
      command: 'mvn -q -Dpmd.skip=true -Dspotless.skip=true -DskipTests spring-boot:run',
      cwd: BACKEND_DIR,
      port: 8080,
      reuseExistingServer: !process.env.CI,
      stdout: 'pipe',
      stderr: 'pipe',
      env: {
        DB_USERNAME: process.env.DB_USERNAME || 'root',
        DB_PASSWORD: process.env.DB_PASSWORD || 'xupeng2016',
        TAVILY_API_KEY: process.env.TAVILY_API_KEY || '',
        DEEPSEEK_API_KEY: process.env.DEEPSEEK_API_KEY || '',
      },
    },
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

