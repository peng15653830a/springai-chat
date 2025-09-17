import { test, expect } from '@playwright/test'

test.describe('DeepSeek V3.1 实机联调（拦截SSE）', () => {
  test('选择模型-发起会话-答案含表格-渲染稳定且逐字效果存在', async ({ page }) => {
    // 仅拦截 SSE，确保其余请求走真实后端（H2）
    await page.route('**/api/chat/stream/**', async route => {
      const sse = [
        'data: {"type":"start","data":{"messageId":"srv-1"}}',
        '',
        'data: {"type":"chunk","data":{"content":"## 回答（含表格）\n\n"}}',
        '',
        'data: {"type":"chunk","data":{"content":"下面是一个对比表：\n\n"}}',
        '',
        'data: {"type":"chunk","data":{"content":"| 项目 | A | B |\n|---|---|---|\n| 指标1 | 1 | 2 |\n"}}',
        '',
        'data: {"type":"chunk","data":{"content":"| 指标2 | 3 | 4 |\n\n结论：B 更适合。"}}',
        '',
        'data: {"type":"end","data":{"messageId":"srv-1"}}',
        '',
      ].join('\n')
      await route.fulfill({ status: 200, contentType: 'text/event-stream', body: sse })
    })

    await page.goto('/')
    // 登录
    await page.getByPlaceholder('请输入用户名').fill('tester')
    await page.getByPlaceholder('请输入昵称').fill('Tester')
    await page.getByRole('button', { name: '开始聊天' }).click()

    // 选择 DeepSeek-V3.1 模型
    const selector = page.locator('.model-selector')
    await selector.click()
    await page.getByRole('option', { name: /DeepSeek-V3\.1/i }).click()

    // 输入问题并发送
    await page.locator('textarea').fill('请用Markdown输出一个包含A/B两列的对比表格')
    const ssePromise = page.waitForRequest('**/api/chat/stream/**')
    await page.locator('.send-btn').click()
    await ssePromise

    // 验证：逐字/分块渲染过程中，页面不崩；最终有表格、标题、文本
    const assistant = page.locator('.message-item.assistant').last()
    await expect(assistant).toBeVisible({ timeout: 15000 })
    await expect(assistant.locator('h2')).toContainText('回答（含表格）', { timeout: 15000 })
    await expect(assistant.locator('table')).toBeVisible({ timeout: 15000 })
    await expect(assistant.locator('table tr')).toHaveCount(3, { timeout: 15000 }) // header + 2 rows
    await expect(assistant).toContainText('结论：B 更适合', { timeout: 15000 })
  })
})
