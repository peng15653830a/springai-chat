import { test, expect } from '@playwright/test'
import fs from 'fs'

test('PG+DeepSeek 实机联调：表格对比题', async ({ page }) => {
  test.setTimeout(180_000)

  await page.goto('/')

  // 登录
  await page.getByPlaceholder('请输入用户名').fill('tester')
  await page.getByPlaceholder('请输入昵称').fill('Tester')
  await page.getByRole('button', { name: '开始聊天' }).click()

  // 确保已进入聊天页
  await expect(page.locator('.chat-content')).toBeVisible({ timeout: 30_000 })

  // 选择 DeepSeek-V3.1 模型
  const selector = page.locator('.model-selector')
  await selector.click()
  await page.getByRole('option', { name: /DeepSeek-V3\.1/i }).click()

  // 保持“联网搜索”开启（默认就是开启，若按钮是success态则不点击）
  // 保险起见，检测它的class，不为el-button--success则点击一次开启
  const searchBtn = page.getByRole('button', { name: /联网搜索/ })
  const hasSuccess = await searchBtn.evaluate(el => el.classList.contains('el-button--success'))
  if (!hasSuccess) await searchBtn.click()

  // 提问：
  const q = '使用表格进行Postman的主要替代品及其优缺点的详细对比'
  await page.locator('textarea').fill(q)
  const ssePromise = page.waitForRequest('**/api/chat/stream/**')
  await page.locator('.send-btn').click()
  await ssePromise

  const assistant = page.locator('.message-item.assistant').last()
  await expect(assistant).toBeVisible({ timeout: 120_000 })

  // 等待表格或标题出现（保持宽松）
  await expect(assistant.locator('table')).toBeVisible({ timeout: 120_000 })

  // 抓取HTML与截图以便复盘
  const html = await assistant.evaluate(node => node.innerHTML)
  fs.mkdirSync('test-results', { recursive: true })
  fs.writeFileSync('test-results/deepseek-pg-assistant.html', html, 'utf-8')
  await assistant.screenshot({ path: 'test-results/deepseek-pg-assistant.png' })
})

