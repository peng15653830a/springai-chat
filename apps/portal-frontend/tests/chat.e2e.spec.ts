import { test, expect } from '@playwright/test'

const portal = 'http://localhost:5174'

test.describe('Portal Chat E2E (DeepSeek, SSE)', () => {
  test('login -> home -> chat -> new conversation -> send message -> receive SSE', async ({ page }) => {
    // 登录页
    await page.goto(`${portal}/login`)
    await page.getByPlaceholder('用户名').fill('e2e_user')
    await page.getByPlaceholder('昵称').fill('E2E')
    await page.getByRole('button', { name: '登录' }).click()

    // 首页
    await page.waitForURL('**/home')
    await expect(page.getByText('选择功能')).toBeVisible()

    // 进入聊天
    await page.getByText('聊天助手').click()
    await page.waitForURL('**/chat')

    // 新建对话
    await page.getByRole('button', { name: '新对话' }).click()
    // 对话列表出现或消息区域可见
    await expect(page.locator('.list .item').first()).toBeVisible({ timeout: 30000 })

    // 发送一条消息
    const textarea = page.locator('textarea')
    await textarea.fill('你好，介绍一下你自己。')
    await page.getByRole('button', { name: '发送' }).click()

    // 期望出现 assistant 消息（SSE chunk/end 之后）
    // 用较大的超时以容忍模型响应
    await expect(page.locator('.msg.assistant .body').last()).toBeVisible({ timeout: 60000 })
    const text = await page.locator('.msg.assistant .body').last().innerText()
    expect(text.length).toBeGreaterThan(5)
  })
})

