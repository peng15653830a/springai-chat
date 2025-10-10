import { defineStore } from "pinia"
import { ref } from "vue"
import axios from "axios"

export const useNovelStore = defineStore('novel', () => {
  // 状态
  const models = ref([])
  const currentModel = ref('')
  const modelParameters = ref({
    temperature: 0.7,
    maxTokens: 2000,
    topP: 1.0
  })

  const chatHistory = ref([])
  const isGenerating = ref(false)
  const currentResponse = ref('')

  const ragResults = ref([])
  const mcpTools = ref([])
  const materials = ref({ success: true, message: 'OK', totalFiles: 0, totalChunks: 0, items: [] })

  // 操作
  const initializeNovel = async () => {
    await loadModels()
    await loadMcpTools()
    await loadMaterials()
  }

  const loadModels = async () => {
    try {
      const response = await axios.get('/api/novel/models')
      models.value = response.data.models || []
      if (models.value.length > 0) {
        currentModel.value = models.value[0].name
      }
    } catch (error) {
      console.error('加载模型列表失败:', error)
    }
  }

  const loadMcpTools = async () => {
    try {
      const response = await axios.get('/api/novel/mcp/tools')
      if (response.data.success) {
        mcpTools.value = response.data.tools || []
      }
    } catch (error) {
      console.error('加载MCP工具失败:', error)
    }
  }

  const loadMaterials = async () => {
    try {
      const response = await axios.get('/api/novel/rag/materials')
      if (response.data && response.data.success) {
        materials.value = response.data
      }
    } catch (error) {
      console.error('加载已导入素材失败:', error)
    }
  }

  const generateText = async (prompt) => {
    if (isGenerating.value) return

    isGenerating.value = true
    currentResponse.value = ''

    // 先添加用户消息到历史
    chatHistory.value.push({
      type: 'user',
      content: prompt,
      timestamp: new Date()
    })

    try {
      const requestData = {
        model: currentModel.value,
        prompt,
        temperature: modelParameters.value.temperature,
        maxTokens: modelParameters.value.maxTokens,
        topP: modelParameters.value.topP
      }

      const response = await fetch('/api/novel/stream', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'text/event-stream'
        },
        body: JSON.stringify(requestData)
      })

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')

        // 保留最后一行可能不完整的数据
        buffer = lines.pop() || ''

        for (const line of lines) {
          if (line.startsWith('event:')) {
            continue
          } else if (line.startsWith('data:')) {
            const dataStr = line.slice(5).trim()
            if (dataStr) {
              try {
                const data = JSON.parse(dataStr)
                if (data.content && !data.done) {
                  currentResponse.value += data.content
                }
              } catch (e) {
                console.error('解析SSE数据失败:', e, 'data:', dataStr)
              }
            }
          }
        }
      }

      // 添加助手回复到聊天历史
      if (currentResponse.value) {
        chatHistory.value.push({
          type: 'assistant',
          content: currentResponse.value,
          timestamp: new Date()
        })
      }

    } catch (error) {
      console.error('生成文本失败:', error)
    } finally {
      currentResponse.value = ''
      isGenerating.value = false
    }
  }

  const searchMaterials = async (query, topK = 5) => {
    try {
      const response = await axios.post('/api/novel/rag/search', {
        query,
        topK
      })

      if (response.data.success) {
        ragResults.value = response.data.results || []
      }
    } catch (error) {
      console.error('搜索素材失败:', error)
    }
  }

  const executeMcpTool = async (toolName, parameters) => {
    try {
      const response = await axios.post('/api/novel/mcp/execute', {
        toolName,
        parameters
      })

      return response.data
    } catch (error) {
      console.error('执行MCP工具失败:', error)
      return { success: false, error: error.message }
    }
  }

  const importMaterials = async (path) => {
    try {
      const response = await axios.post('/api/novel/rag/import', {
        path,
        recursive: true
      })

      const data = response.data
      // 导入成功后刷新已加载素材
      if (data && data.success) {
        try { await loadMaterials() } catch {}
      }
      return data
    } catch (error) {
      console.error('导入素材失败:', error)
      return { success: false, error: error.message }
    }
  }

  // 从网页抓取并导入素材
  const crawlFromUrl = async (payload) => {
    try {
      const body = {
        url: payload.url,
        maxPages: payload.maxPages ?? 200,
        sameDomainOnly: payload.sameDomainOnly ?? true,
        rateLimitMs: payload.rateLimitMs ?? 500,
        analyzeStyle: payload.analyzeStyle ?? false,
        includePatterns: payload.includePatterns && payload.includePatterns.length ? payload.includePatterns : undefined,
        excludePatterns: payload.excludePatterns && payload.excludePatterns.length ? payload.excludePatterns : undefined,
        contentSelector: payload.contentSelector || undefined,
        titleSelector: payload.titleSelector || undefined,
        sitePreset: payload.sitePreset || 'xbookcn'
      }
      const response = await axios.post('/api/novel/rag/crawl', body)
      const data = response.data
      if (data && data.success) {
        try { await loadMaterials() } catch {}
      }
      return data
    } catch (error) {
      console.error('网页抓取失败:', error)
      return { success: false, error: error.message }
    }
  }

  const clearChat = () => {
    chatHistory.value = []
    currentResponse.value = ''
  }

  return {
    // 状态
    models,
    currentModel,
    modelParameters,
    chatHistory,
    isGenerating,
    currentResponse,
    ragResults,
    mcpTools,
    materials,

    // 操作
    initializeNovel,
    loadModels,
    loadMcpTools,
    loadMaterials,
    generateText,
    searchMaterials,
    executeMcpTool,
    importMaterials,
    crawlFromUrl,
    clearChat
  }
})
