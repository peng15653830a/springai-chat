import { test, expect } from '@playwright/test'

// 拦截后端接口，返回可控数据，便于聚焦Markdown渲染
async function setupApiInterceptions(page) {
  // 登录
  await page.route('**/api/users/login', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ success: true, data: { id: 'u1', username: 'tester', nickname: 'Tester' } })
    })
  })

  // 模型相关（最小化返回）
  await page.route('**/api/models/**', async route => {
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ success: true, data: [] }) })
  })

  // 对话列表为空 -> 触发自动创建
  await page.route('**/api/conversations?**', async route => {
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ success: true, data: [] }) })
  })

  // 创建对话
  await page.route('**/api/conversations', async route => {
    if (route.request().method() === 'POST') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ success: true, data: { id: 'c1', title: 'Test Conversation', updatedAt: Date.now() } })
      })
      return
    }
    await route.continue()
  })

  // 对话消息：返回包含“糟糕格式”的Markdown，考察前端纠偏与稳定渲染
  await page.route('**/api/conversations/c1/messages', async route => {
    const badMarkdown = [
      '这是首段文字',
      '##没有空格的标题',
      '紧跟一段说明文字',
      '-列表项A',
      '1.数字列表1',
      '',
      '```js',
      "console.log('x')",
      '```',
      '',
      '|列A|列B|',
      '|---|---|',
      '|1|2|'
    ].join('\n')

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        success: true,
        data: [
          { id: 'm0', role: 'user', content: '演示一下Markdown渲染', createdAt: Date.now() - 1000 },
          { id: 'm1', role: 'assistant', content: badMarkdown, createdAt: Date.now() }
        ]
      })
    })
  })
}

test('静态Markdown渲染应结构稳定且语义正确', async ({ page }) => {
  await setupApiInterceptions(page)

  // 打开应用并完成登录
  await page.goto('/')
  await page.getByPlaceholder('请输入用户名').fill('tester')
  await page.getByPlaceholder('请输入昵称').fill('Tester')
  await page.getByRole('button', { name: '开始聊天' }).click()

  // 进入聊天页后，应渲染一条 assistant 消息
  const assistant = page.locator('.message-item.assistant').first()
  await expect(assistant).toBeVisible()

  // h2 标题应被规范化（允许尾部有锚点符号）
  await expect(assistant.locator('h2')).toContainText('没有空格的标题')

  // 列表应被正确渲染
  await expect(assistant.locator('ul li')).toHaveCount(1)
  await expect(assistant.locator('ol li')).toHaveCount(1)

  // 代码块与表格应存在
  await expect(assistant.locator('pre code')).toBeVisible()
  await expect(assistant.locator('table')).toBeVisible()
})

test('模拟流式追加时渲染应保持稳定，最终结构正确', async ({ page }) => {
  await setupApiInterceptions(page)

  // 拦截SSE，分块下发半截与完整代码围栏
  await page.route('**/api/chat/stream/**', async route => {
    const sse = [
      'data: {"type":"start","data":{}}',
      '',
      'data: {"type":"chunk","data":{"content":"段落A\\n```js\\nconsole.log(1)"}}',
      '',
      'data: {"type":"chunk","data":{"content":"\\n```\\n##标题B"}}',
      '',
      'data: {"type":"end","data":{}}',
      '',
    ].join('\n')
    await route.fulfill({ status: 200, contentType: 'text/event-stream', body: sse })
  })

  await page.goto('/')
  await page.getByPlaceholder('请输入用户名').fill('tester')
  await page.getByPlaceholder('请输入昵称').fill('Tester')
  await page.getByRole('button', { name: '开始聊天' }).click()

  // 触发一次发送（对话已创建，仅用于触发SSE连接）
  await page.locator('textarea').fill('test streaming')
  const sseReq = page.waitForRequest('**/api/chat/stream/**')
  await page.locator('.send-btn').click()
  await sseReq

  const assistant = page.locator('.message-item.assistant').last()
  await expect(assistant).toBeVisible()
  await expect(assistant.locator('pre code')).toBeVisible()
  await expect(assistant.locator('h2')).toContainText('标题B')
})
