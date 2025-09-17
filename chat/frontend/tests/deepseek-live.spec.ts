import { test, expect } from '@playwright/test'

test('DeepSeek V3.1 真实联调：选择模型→发送表格问题→观察渲染', async ({ page }) => {
  test.setTimeout(120_000)

  await page.goto('/')

  // 登录
  await page.getByPlaceholder('请输入用户名').fill('tester')
  await page.getByPlaceholder('请输入昵称').fill('Tester')
  await page.getByRole('button', { name: '开始聊天' }).click()

  // 选择 DeepSeek-V3.1
  const selector = page.locator('.model-selector')
  await selector.click()
  await page.getByRole('option', { name: /DeepSeek-V3\.1/i }).click()

  // 发送一个明确要求表格的提示
  const prompt = [
    '请用 Markdown 输出一个包含两列 A 与 B 的对比表格，',
    '至少包含2行数据；并在表格上方给出二级标题：回答（含表格）。',
  ].join('')
  await page.locator('textarea').fill(prompt)

  const ssePromise = page.waitForRequest('**/api/chat/stream/**')
  await page.locator('.send-btn').click()
  await ssePromise

  const assistant = page.locator('.message-item.assistant').last()
  await expect(assistant).toBeVisible({ timeout: 60_000 })

  // 等待流式完成的核心结构：标题 + 表格（尽量宽松，以适配模型差异）
  await expect(assistant.locator('h2')).toBeVisible({ timeout: 60_000 })
  await expect(assistant.locator('table')).toBeVisible({ timeout: 60_000 })

  // 截图记录实际渲染
  await assistant.screenshot({ path: 'test-results/deepseek-live-assistant.png' })
})

