import { defineStore } from 'pinia'
import { ElMessage } from 'element-plus'

export const useNovelWorkspaceStore = defineStore('novelWorkspace', {
  state: () => ({
    // 项目基础信息
    project: {
      id: null,
      title: '未命名项目',
      model: null, // 使用null让后端从application.yml读取默认配置
      settings: {
        background: '',
        characters: '',
        style: ''
      },
      outline: [], // 段落数组
      createdAt: null,
      updatedAt: null
    },

    // 当前选中的段落索引
    currentSegmentIndex: null,

    // 当前编辑的设定ID (background/characters/style)
    editingSettings: null,

    // 段落详情映射 (index -> segment)
    segments: new Map(),

    // RAG素材
    materials: [],

    // 模型参数
    params: {
      temperature: 0.7,
      maxTokens: 2048,
      topP: 0.9
    },

    // 生成状态
    generating: false,

    // 已引用的素材ID列表
    referencedMaterialIds: []
  }),

  getters: {
    // 当前选中的段落
    currentSegment: (state) => {
      if (state.currentSegmentIndex === null) return null
      return state.segments.get(state.currentSegmentIndex) ||
        state.project.outline.find(s => s.index === state.currentSegmentIndex)
    },

    // 已定稿的段落数量
    approvedCount: (state) => {
      return state.project.outline.filter(s => s.status === 'approved').length
    },

    // 总段落数
    totalSegments: (state) => {
      return state.project.outline.length
    },

    // 项目进度百分比
    progressPercentage: (state) => {
      if (state.project.outline.length === 0) return 0
      const approved = state.project.outline.filter(s => s.status === 'approved').length
      return Math.round((approved / state.project.outline.length) * 100)
    }
  },

  actions: {
    /**
     * 初始化工作区
     */
    initialize() {
      console.log('Novel workspace initialized')
      this.loadFromLocalStorage()
    },

    /**
     * 初始化项目
     */
    async initProject(config) {
      try {
        const response = await fetch('/api/novel/story/init', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            title: config.title || this.project.title,
            background: config.background || '',
            style: config.style || ''
          })
        })

        if (!response.ok) {
          throw new Error('初始化失败')
        }

        const data = await response.json()

        if (data.success) {
          this.project.id = data.sessionId
          this.project.title = config.title || this.project.title
          this.project.settings.background = config.background || ''
          this.project.settings.style = config.style || ''
          this.project.createdAt = new Date().toISOString()
          this.saveToLocalStorage()
          return true
        }

        throw new Error(data.message || '初始化失败')
      } catch (error) {
        console.error('Init project error:', error)
        throw error
      }
    },

    /**
     * 添加段落
     */
    async addSegment(segmentData) {
      const newIndex = this.project.outline.length + 1
      const segment = {
        index: newIndex,
        title: segmentData.title,
        prompt: segmentData.prompt,
        starter: segmentData.starter || '',
        status: 'draft',
        version: 0,
        latestText: null,
        createdAt: new Date().toISOString()
      }

      this.project.outline.push(segment)
      this.segments.set(newIndex, segment)

      // 提交到后端
      if (this.project.id) {
        await this.submitOutline()
      }

      this.saveToLocalStorage()
      return segment
    },

    /**
     * 提交大纲到后端
     */
    async submitOutline() {
      if (!this.project.id) {
        throw new Error('项目未初始化')
      }

      const items = this.project.outline.map(seg => ({
        title: seg.title,
        prompt: seg.prompt,
        starter: seg.starter
      }))

      const response = await fetch(`/api/novel/story/${this.project.id}/outline`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ items })
      })

      if (!response.ok) {
        throw new Error('提交大纲失败')
      }

      return true
    },

    /**
     * 选择段落
     */
    selectSegment(index) {
      this.currentSegmentIndex = index
      this.editingSettings = null  // 切换到段落时清除设定编辑
      this.loadSegmentDetails(index)
    },

    /**
     * 编辑设定
     */
    editSettings(settingId) {
      this.editingSettings = settingId
      this.currentSegmentIndex = null  // 切换到设定时清除段落选择
    },

    /**
     * 更新设定
     */
    updateSettings(newSettings) {
      this.project.settings = { ...this.project.settings, ...newSettings }
      this.saveToLocalStorage()
    },

    /**
     * 加载段落详情
     */
    async loadSegmentDetails(index) {
      if (!this.project.id) return

      try {
        const response = await fetch(`/api/novel/story/${this.project.id}/current`)
        if (!response.ok) return

        const data = await response.json()
        if (data && data.index === index) {
          this.segments.set(index, data)
          // 同步到outline
          const outlineIndex = this.project.outline.findIndex(s => s.index === index)
          if (outlineIndex !== -1) {
            this.project.outline[outlineIndex] = { ...this.project.outline[outlineIndex], ...data }
          }
        }
      } catch (error) {
        console.error('Load segment details error:', error)
      }
    },

    /**
     * 生成段落内容
     */
    async generateSegment(index, onChunk) {
      if (!this.project.id) {
        throw new Error('项目未初始化')
      }

      this.generating = true

      try {
        const response = await fetch(`/api/novel/story/${this.project.id}/generate?segment=${index}`, {
          method: 'GET',
          headers: { 'Accept': 'text/event-stream' }
        })

        if (!response.ok) {
          throw new Error('生成失败')
        }

        await this.readSSE(response.body, onChunk)

        // 生成完成后重新加载段落
        await this.loadSegmentDetails(index)
      } finally {
        this.generating = false
      }
    },

    /**
     * 重写段落
     */
    async reviseSegment(index, feedback, onChunk) {
      if (!this.project.id) {
        throw new Error('项目未初始化')
      }

      this.generating = true

      try {
        const response = await fetch(`/api/novel/story/${this.project.id}/revise?segment=${index}`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Accept': 'text/event-stream'
          },
          body: JSON.stringify({ feedback })
        })

        if (!response.ok) {
          throw new Error('重写失败')
        }

        await this.readSSE(response.body, onChunk)

        // 重写完成后重新加载段落
        await this.loadSegmentDetails(index)
      } finally {
        this.generating = false
      }
    },

    /**
     * 通过段落并切换到下一段
     */
    async approveSegment(index) {
      if (!this.project.id) {
        throw new Error('项目未初始化')
      }

      const response = await fetch(`/api/novel/story/${this.project.id}/approve?segment=${index}`, {
        method: 'POST'
      })

      if (!response.ok) {
        throw new Error('通过失败')
      }

      // 更新状态
      const segment = this.segments.get(index)
      if (segment) {
        segment.status = 'approved'
      }

      const outlineIndex = this.project.outline.findIndex(s => s.index === index)
      if (outlineIndex !== -1) {
        this.project.outline[outlineIndex].status = 'approved'
      }

      // 切换到下一段
      const nextIndex = index + 1
      if (nextIndex <= this.project.outline.length) {
        this.selectSegment(nextIndex)
      }

      this.saveToLocalStorage()
    },

    /**
     * 重新排序段落
     */
    reorderSegments(newOrder) {
      this.project.outline = newOrder
      this.saveToLocalStorage()
    },

    /**
     * 搜索素材
     */
    async searchMaterials(query) {
      try {
        const response = await fetch('/api/novel/rag/search', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            query,
            topK: 5,
            minSimilarity: 0.2
          })
        })

        if (!response.ok) {
          throw new Error('搜索失败')
        }

        const data = await response.json()
        if (data.success && data.results) {
          this.materials = data.results.map(r => ({
            id: r.id || `material-${Date.now()}`,
            title: r.title || '无标题',
            excerpt: r.excerpt || r.content || '',
            similarity: r.similarity || 0,
            createdAt: r.createdAt || new Date().toISOString()
          }))
        }
      } catch (error) {
        console.error('Search materials error:', error)
        throw error
      }
    },

    /**
     * 清空素材
     */
    clearMaterials() {
      this.materials = []
    },

    /**
     * 添加素材引用
     */
    addMaterialReference(materialId) {
      if (!this.referencedMaterialIds.includes(materialId)) {
        this.referencedMaterialIds.push(materialId)
      }
    },

    /**
     * 更新参数
     */
    updateParams(newParams) {
      this.params = { ...this.params, ...newParams }
      this.saveToLocalStorage()
    },

    /**
     * 重置参数
     */
    resetParams() {
      this.params = {
        temperature: 0.7,
        maxTokens: 2048,
        topP: 0.9
      }
      this.saveToLocalStorage()
    },

    /**
     * 保存项目
     */
    async saveProject() {
      this.project.updatedAt = new Date().toISOString()
      this.saveToLocalStorage()
      return true
    },

    /**
     * 导出项目
     */
    async exportProject() {
      const exportData = {
        ...this.project,
        segments: Array.from(this.segments.values()),
        params: this.params,
        exportedAt: new Date().toISOString()
      }

      const blob = new Blob([JSON.stringify(exportData, null, 2)], {
        type: 'application/json'
      })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `${this.project.title || '未命名项目'}_${Date.now()}.json`
      a.click()
      URL.revokeObjectURL(url)
    },


    /**
     * 读取SSE流
     */
    async readSSE(stream, onChunk) {
      const reader = stream.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const line of lines) {
          if (line.startsWith('data:')) {
            const dataStr = line.slice(5).trim()
            if (!dataStr) continue

            try {
              const obj = JSON.parse(dataStr)
              if (obj.content && !obj.done && onChunk) {
                onChunk(obj.content)
              }
              if (obj.done) return
            } catch {
              // Fallback: append raw data
              if (onChunk) onChunk(dataStr)
            }
          }
        }
      }
    },

    /**
     * 保存到本地存储
     */
    saveToLocalStorage() {
      try {
        const state = {
          project: this.project,
          currentSegmentIndex: this.currentSegmentIndex,
          segments: Array.from(this.segments.entries()),
          params: this.params
        }
        localStorage.setItem('novel-workspace', JSON.stringify(state))
      } catch (error) {
        console.error('Save to localStorage error:', error)
      }
    },

    /**
     * 从本地存储加载
     */
    loadFromLocalStorage() {
      try {
        const saved = localStorage.getItem('novel-workspace')
        if (saved) {
          const state = JSON.parse(saved)
          this.project = state.project || this.project
          this.currentSegmentIndex = state.currentSegmentIndex
          this.segments = new Map(state.segments || [])
          this.params = state.params || this.params
        }
      } catch (error) {
        console.error('Load from localStorage error:', error)
      }
    }
  }
})
