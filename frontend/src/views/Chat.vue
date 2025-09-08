<template>
  <div class="chat-container">
    <!-- 侧边栏 -->
    <div class="sidebar" :class="{ collapsed: leftSidebarCollapsed }">
      <div class="sidebar-header">
        <div v-if="!leftSidebarCollapsed" class="user-info">
          <el-avatar :size="40" :src="userStore.currentUser?.avatar">
            {{ userStore.currentUser?.nickname?.charAt(0) }}
          </el-avatar>
          <span class="username">{{ userStore.currentUser?.nickname }}</span>
        </div>
        <div class="sidebar-controls">
          <el-button v-if="!leftSidebarCollapsed" @click="createNewConversation" type="primary" size="small">
            <el-icon><Plus /></el-icon>
            新对话
          </el-button>
        </div>
      </div>
      
      <div v-if="!leftSidebarCollapsed" class="conversation-list">
        <div
          v-for="conversation in chatStore.conversations"
          :key="conversation.id"
          :class="['conversation-item', { active: chatStore.currentConversation?.id === conversation.id }]"
          @click="selectConversation(conversation)"
        >
          <div class="conversation-title">{{ conversation.title }}</div>
          <div class="conversation-time">{{ formatTime(conversation.updatedAt) }}</div>
          <el-button
            @click.stop="deleteConversation(conversation.id)"
            type="danger"
            size="small"
            :link="true"
            class="delete-btn"
          >
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 聊天区域 -->
    <div class="chat-area">
      <div v-if="!chatStore.currentConversation" class="welcome">
        <h2>欢迎使用AI智能聊天</h2>
        <p>选择一个对话或创建新对话开始聊天</p>
      </div>
      
      <div v-else class="chat-content">
        <!-- 消息列表 -->
        <div ref="messageList" class="message-list">
          <div
            v-for="message in processedMessages"
            :key="message.id"
            :class="['message-item', message.role]"
          >
            <div class="message-content">
              <!-- 搜索指示器（仅AI消息且有搜索结果时显示） -->
              <SearchIndicator 
                v-if="message.searchResults && message.role === 'assistant'" 
                :results="parseSearchResults(message.searchResults)"
                :messageId="message.id"
                @click="handleSearchIndicatorClick"
              />
              
              <!-- 推理过程 (仅AI消息且有推理内容时显示) -->
              <div v-if="message.thinking && message.role === 'assistant'" class="thinking-section">
                <div 
                  class="thinking-header" 
                  @click="toggleThinking(message.id)"
                  :class="{ expanded: expandedThinking.has(message.id) }"
                >
                  <el-icon class="thinking-icon">
                    <Operation />
                  </el-icon>
                  <span class="thinking-label">推理过程</span>
                  <el-icon class="expand-icon">
                    <ArrowRight v-if="!expandedThinking.has(message.id)" />
                    <ArrowDown v-else />
                  </el-icon>
                </div>
                <div 
                  v-show="expandedThinking.has(message.id)" 
                  class="thinking-content"
                >
                  <VueMarkdownRender 
                    :source="String(message.thinking || '')"
                    :options="markdownOptions"
                    class="thinking-body"
                  />
                </div>
              </div>
              
              <div class="message-text">
                <!-- 使用 v-md-preview 组件 -->
                <div v-if="message.role === 'user'" class="message-body">
                  {{ message.content }}
                </div>
                <VueMarkdownRender 
                  v-else
                  :source="String(message.content || '')"
                  :options="markdownOptions"
                  class="message-body markdown-content"
                />
                <div class="message-actions">
                  <el-button
                    :link="true"
                    size="small"
                    @click="copyMessage(message.content)"
                    class="copy-btn"
                    title="复制"
                  >
                    <el-icon><CopyDocument /></el-icon>
                  </el-button>
                </div>
              </div>
              <div class="message-time">{{ formatTime(message.createdAt) }}</div>
            </div>
          </div>
          
          <!-- 加载中 -->
          <div v-if="chatStore.isLoading" class="message-item assistant">
            <div class="message-content">
              <div class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 输入区域 -->
        <div class="input-area">
          <!-- 功能按钮栏 - 借鉴腾讯元宝设计 -->
          <div class="function-toolbar">
            <div class="toolbar-left">
              <!-- 模型选择下拉框 -->
              <el-select
                v-model="selectedModel"
                placeholder="选择模型"
                size="small"
                class="model-selector"
                @change="onModelChange"
              >
                <el-option
                  v-for="model in availableModels"
                  :key="model.name"
                  :label="model.displayName"
                  :value="model.name"
                  :disabled="!model.available"
                >
                  <span style="float: left">{{ model.displayName }}</span>
                  <span style="float: right; color: #8492a6; font-size: 13px" v-if="!model.available">不可用</span>
                </el-option>
              </el-select>
              
              <el-button
                :type="deepThinking ? 'primary' : ''"
                :plain="!deepThinking"
                size="small"
                @click="toggleDeepThinking"
                class="function-btn"
              >
                <el-icon><Operation /></el-icon>
                深度思考
                <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
              </el-button>
              
              <el-button
                :type="searchEnabled ? 'success' : ''"
                :plain="!searchEnabled"
                size="small"
                @click="toggleSearch"
                class="function-btn"
              >
                <el-icon><Connection /></el-icon>
                联网搜索
                <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
              </el-button>
            </div>
          </div>
          
          <div class="input-container">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="2"
              :placeholder="searchEnabled ? '输入问题，支持联网搜索...' : '输入你的问题...'"
              @keydown.enter.prevent="handleSendMessage"
              :disabled="chatStore.isLoading"
            />
            <div class="send-area">
              <el-button
                @click="handleSendMessage"
                type="primary"
                :disabled="!inputMessage.trim() || chatStore.isLoading"
                class="send-btn"
              >
                <el-icon><Promotion /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 左侧悬浮收缩按钮 -->
    <div class="floating-left-toggle" @click="toggleLeftSidebar">
      <el-icon>
        <ArrowLeft v-if="!leftSidebarCollapsed" />
        <ArrowRight v-else />
      </el-icon>
    </div>
    
    <!-- 右侧悬浮搜索按钮 -->
    <div class="floating-right-toggle" @click="toggleRightSidebar">
      <el-icon>
        <Search />
      </el-icon>
    </div>
    
    <!-- 右侧面板 -->
    <RightPanel 
      ref="rightPanel"
      :searchResults="currentSearchResults"
      :currentMessageId="currentSearchMessageId"
      :collapsed="rightSidebarCollapsed"
      @toggle="toggleRightSidebar"
    />
  </div>
</template>

<script>
import { ref, onMounted, onBeforeUnmount, nextTick, watch, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'
import { useChatStore } from '../stores/chat'
import { useModelStore } from '../stores/model'
import { conversationApi, chatApi, modelApi } from '../api'
import { useEventSource } from '@vueuse/core'
import hljs from 'highlight.js'
import { debounce } from 'lodash-es'
import SearchIndicator from '../components/SearchIndicator.vue'
import RightPanel from '../components/RightPanel.vue'
import VueMarkdownRender from 'vue-markdown-render'

export default {
  name: 'Chat',
  components: {
    VueMarkdownRender,
    SearchIndicator,
    RightPanel
  },
  setup(props, { emit }) {
    const userStore = useUserStore()
    const chatStore = useChatStore()
    const modelStore = useModelStore()
    const inputMessage = ref('')
    const messageList = ref()
    const rightPanel = ref() // 右侧面板引用
    const searchEnabled = ref(true) // 默认开启搜索
    const deepThinking = ref(false) // 默认关闭深度思考
    const expandedThinking = ref(new Set()) // 展开的推理过程ID集合
    
    // 模型选择相关状态
    const selectedModel = ref('') // 当前选择的模型
    const availableModels = ref([]) // 可用模型列表
    const selectedProvider = ref('') // 当前选择的提供者
    
    // 待发送的消息（用于触发SSE连接）
    const pendingMessage = ref('')
    const pendingSearchEnabled = ref(false)
    const pendingDeepThinking = ref(false)
    
    // 动态SSE URL - 只在有待发送消息时才建立连接
    const sseUrl = computed(() => {
      if (!chatStore.currentConversation?.id || !pendingMessage.value) {
        return undefined // 无消息时不建立连接
      }
      
      const params = new URLSearchParams({
        message: pendingMessage.value,
        searchEnabled: pendingSearchEnabled.value.toString(),
        deepThinking: pendingDeepThinking.value.toString()
      })
      
      // 添加模型信息到参数中
      if (selectedModel.value) {
        params.append('model', selectedModel.value)
      }
      if (selectedProvider.value) {
        params.append('provider', selectedProvider.value)
      }
      
      return `/api/chat/stream/${chatStore.currentConversation.id}?${params}`
    })
    
    // 使用useEventSource - 配置为按需连接，减少服务停止后的重连
    const { data: sseData, status: sseStatus, error: sseError, close: closeSSE } = useEventSource(
      sseUrl,
      [],
      {
        immediate: false, // 不立即连接
        autoReconnect: {
          retries: 2, // 最多重试2次
          delay: 3000, // 3秒重试间隔
          onFailed() {
            console.log('🔌 SSE连接最终失败，停止重试')
            chatStore.setLoading(false)
            chatStore.setConnected(false)
            // 清理待发送消息，停止进一步重连
            pendingMessage.value = ''
            pendingSearchEnabled.value = false
            pendingDeepThinking.value = false
            // 显示用户友好的错误信息
            ElMessage.warning('服务连接中断，请检查服务是否正在运行')
          }
        }
      }
    )
    
    // 监听SSE数据
    watch(sseData, (newData) => {
      if (newData) {
        try {
          const sseEvent = JSON.parse(newData)
          handleSSEEvent(sseEvent)
        } catch (error) {
          console.error('❌ 解析SSE事件失败:', error, newData)
        }
      }
    })
    
    // 监听SSE状态
    watch(sseStatus, (status) => {
      console.log('📡 SSE状态变化:', status)
      chatStore.setConnected(status === 'OPEN')
      
      if (status === 'CLOSED') {
        // 连接关闭后清理待发送消息
        pendingMessage.value = ''
        pendingSearchEnabled.value = false
        pendingDeepThinking.value = false
        chatStore.setLoading(false)
      }
    })
    
    // 监听SSE错误
    watch(sseError, (error) => {
      if (error) {
        console.error('❌ SSE连接错误:', error)
        chatStore.setLoading(false)
        chatStore.setConnected(false)
        // 清理待发送消息
        pendingMessage.value = ''
        pendingSearchEnabled.value = false
        pendingDeepThinking.value = false
      }
    })
    
    // 统一SSE事件处理器
    const handleSSEEvent = (sseEvent) => {
      const { type, data } = sseEvent
      console.log('📨 收到SSE事件:', type, data)
      
      switch (type) {
        case 'start':
          handleStartEvent(data)
          break
        case 'chunk':
          handleChunkEvent(data)
          break
        case 'thinking':
          handleThinkingEvent(data)
          break
        case 'search':
          handleSearchEvent(data)
          break
        case 'search_results':
          handleSearchResultsEvent(data)
          break
        case 'end':
          handleEndEvent(data)
          break
        case 'error':
          handleErrorEvent(data)
          break
        default:
          console.warn('未知SSE事件类型:', type)
      }
    }
    
    // 侧边栏收缩状态
    const leftSidebarCollapsed = ref(false)
    // 右侧搜索面板默认收起
    const rightSidebarCollapsed = ref(true)
    
    // 侧边栏收缩功能
    const toggleLeftSidebar = () => {
      leftSidebarCollapsed.value = !leftSidebarCollapsed.value
    }
    
    const toggleRightSidebar = () => {
      rightSidebarCollapsed.value = !rightSidebarCollapsed.value
    }
    
    // 右侧面板状态管理
    const currentSearchResults = ref([])
    const currentSearchMessageId = ref(null)
    
    // Markdown 渲染配置 - 确保支持表格
    const markdownOptions = {
      breaks: true,      // 启用换行
      typographer: true, // 启用排版优化
      html: false,       // 禁用HTML标签（安全考虑）
      linkify: true,     // 自动识别链接
      // 确保表格解析功能开启
      tables: true,
      // markdown-it 插件配置
      plugins: []
    }
    
    // 加载对话列表
    const loadConversations = async () => {
      try {
        const response = await conversationApi.getList(userStore.currentUser.id)
        if (response.success) {
          chatStore.setConversations(response.data)
          
          // 自动选择最新的对话，或者创建新对话
          if (response.data && response.data.length > 0) {
            // 如果有对话，选择最新的一个
            const latestConversation = response.data[0] // 假设已按时间排序
            selectConversation(latestConversation)
          } else {
            // 如果没有对话，自动创建一个新对话
            await createNewConversation()
          }
        }
      } catch (error) {
        console.error('Load conversations error:', error)
      }
    }
    
    // 创建新对话
    const createNewConversation = async () => {
      try {
        const response = await conversationApi.create({
          userId: userStore.currentUser.id,
          title: null // 不传递硬编码标题，让后端自动生成
        })
        if (response.success) {
          chatStore.addConversation(response.data)
          selectConversation(response.data)
        }
      } catch (error) {
        ElMessage.error('创建对话失败')
      }
    }
    
    // 选择对话 - 使用useEventSource自动管理连接
    const selectConversation = async (conversation) => {
      // 防止重复点击同一对话
      if (chatStore.currentConversation?.id === conversation.id) {
        console.log('⚠️ 已经是当前对话，跳过切换')
        return
      }
      
      console.log('🔄 切换到对话:', conversation.id)
      chatStore.setCurrentConversation(conversation)
      
      // useEventSource会自动管理连接，无需手动断开
      
      // 加载消息历史
      try {
        const response = await conversationApi.getMessages(conversation.id)
        if (response.success) {
          chatStore.setMessages(response.data)
          
          // 自动展开所有包含thinking的消息
          response.data.forEach(msg => {
            if (msg.role === 'assistant' && msg.thinking && msg.thinking.trim()) {
              expandedThinking.value.add(msg.id)
              console.log('🔍 自动展开thinking消息:', msg.id, msg.thinking.substring(0, 50))
            }
          })
          
          // 自动显示最新的搜索结果
          const latestMessageWithSearch = response.data
            .filter(msg => msg.role === 'assistant' && msg.searchResults)
            .pop() // 获取最新的一条
          
          if (latestMessageWithSearch) {
            const searchResults = parseSearchResults(latestMessageWithSearch.searchResults)
            currentSearchResults.value = searchResults
            currentSearchMessageId.value = latestMessageWithSearch.id
          } else {
            // 清空右侧面板内容，但保持用户设置的展开/收起状态
            currentSearchResults.value = []
            currentSearchMessageId.value = null
          }
          
          scrollToBottom()
        }
      } catch (error) {
        console.error('Load messages error:', error)
      }
      
      // useEventSource会根据sseUrl的变化自动建立新连接
    }
    
    // 删除对话
    const deleteConversation = async (conversationId) => {
      try {
        await ElMessageBox.confirm('确定要删除这个对话吗？', '确认删除', {
          type: 'warning'
        })
        
        const response = await conversationApi.delete(conversationId)
        if (response.success) {
          chatStore.removeConversation(conversationId)
          ElMessage.success('对话已删除')
        }
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除失败')
        }
      }
    }
    
    // 发送消息
    const handleSendMessage = () => {
      if (!inputMessage.value.trim() || !chatStore.currentConversation) return
      
      const message = inputMessage.value.trim()
      inputMessage.value = ''
      
      // 添加用户消息到界面
      chatStore.addMessage({
        id: Date.now(),
        role: 'user',
        content: message,
        createdAt: new Date()
      })
      
      scrollToBottom()
      chatStore.setLoading(true)
      
      // 设置待发送消息，触发useEventSource建立SSE连接
      pendingMessage.value = message
      pendingSearchEnabled.value = searchEnabled.value
      pendingDeepThinking.value = deepThinking.value
      
      // useEventSource会自动检测到sseUrl变化并建立连接
      console.log('🚀 触发SSE连接发送消息:', message)
    }
    
    // 从本地存储加载设置
    const loadSettings = () => {
      const savedSearch = localStorage.getItem('searchEnabled')
      if (savedSearch !== null) {
        searchEnabled.value = savedSearch === 'true'
      }
      
      const savedDeepThinking = localStorage.getItem('deepThinking')
      if (savedDeepThinking !== null) {
        deepThinking.value = savedDeepThinking === 'true'
      }
    }
    
    // SSE事件处理函数
    const handleStartEvent = (data) => {
      console.log('🎯 SSE start event received:', data)
      // start事件只是通知开始，实际消息在chunk中创建
    }
    
    const handleChunkEvent = (data) => {
      console.log('🔥 SSE chunk event received:', data)
      
      try {
        // 从标准SSE事件数据中获取内容
        const chunkContent = data?.content || ''
        console.log('📦 Chunk content:', chunkContent.substring(0, 100))
        
        if (!chunkContent) return
        
        // 获取最后一条消息
        let lastMessage = chatStore.messages[chatStore.messages.length - 1]
        
        // 如果不是assistant消息，创建新的
        if (!lastMessage || lastMessage.role !== 'assistant') {
          const newMessage = {
            id: 'temp-' + Date.now(),
            role: 'assistant',
            content: '',
            thinking: '',  // 确保有thinking字段
            createdAt: new Date()
          }
          chatStore.addMessage(newMessage)
          lastMessage = newMessage
          console.log('📝 Chunk事件创建新消息:', lastMessage.id)
        }
        
        // 更新内容 - v-md-preview会自动处理渲染
        if (lastMessage && lastMessage.role === 'assistant') {
          lastMessage.content = (lastMessage.content || '') + chunkContent
          // 触发响应式更新
          chatStore.messages = [...chatStore.messages]
          scrollToBottom()
        }
      } catch (error) {
        console.error('❌ Error processing chunk:', error)
      }
    }
    
    const handleEndEvent = (data) => {
      console.log('🏁 SSE end event received:', data)
      try {
        // 更新消息ID（如果提供）
        if (chatStore.messages.length > 0 && data?.messageId) {
          const lastMessage = chatStore.messages[chatStore.messages.length - 1]
          if (lastMessage.role === 'assistant') {
            const oldId = lastMessage.id
            const newId = data.messageId
            
            // 更新消息ID
            lastMessage.id = newId
            
            // 如果旧ID在expandedThinking中，需要更新为新ID
            if (expandedThinking.value.has(oldId)) {
              expandedThinking.value.delete(oldId)
              expandedThinking.value.add(newId)
              console.log('✅ 更新推理过程展开状态:', oldId, '->', newId)
            }
          }
        }
        
        chatStore.setLoading(false)
        scrollToBottom()
      } catch (error) {
        console.error('❌ Error parsing end event:', error, data)
        chatStore.setLoading(false)
      }
    }
    
    const handleSearchEvent = (data) => {
      console.log('🔍 SSE search event:', data)
      try {
        // 处理搜索状态事件
        if (data?.type === 'start') {
          ElMessage.info('正在搜索相关信息...')
        } else if (data?.type === 'complete') {
          ElMessage.success('搜索完成')
        }
      } catch (error) {
        console.error('❌ Error parsing search event:', error, data)
      }
    }
    
    const handleSearchResultsEvent = (data) => {
      console.log('📋 SSE search_results event:', data)
      try {
        // 处理搜索结果数据 - 更新当前正在构建的assistant消息
        if (data && data.results) {
          const lastMessage = chatStore.messages[chatStore.messages.length - 1]
          if (lastMessage && lastMessage.role === 'assistant') {
            // 将搜索结果数据存储到消息中
            lastMessage.searchResults = JSON.stringify(data.results)
            // 触发响应式更新
            chatStore.messages = [...chatStore.messages]
            console.log('✅ 搜索结果已添加到消息:', data.results.length, '条结果')
          } else {
            // 如果没有assistant消息，创建一个临时消息来存储搜索结果
            const newMessage = {
              id: 'temp-search-' + Date.now(),
              role: 'assistant',
              content: '',
              searchResults: JSON.stringify(data.results),
              createdAt: new Date()
            }
            chatStore.addMessage(newMessage)
            console.log('✅ 创建新消息存储搜索结果:', data.results.length, '条结果')
          }
        }
      } catch (error) {
        console.error('❌ Error processing search_results event:', error, data)
      }
    }
    
    const handleThinkingEvent = (data) => {
      console.log('🧠 SSE thinking event received:', data)
      try {
        // 从标准SSE事件数据中获取thinking内容
        const thinkingContent = data?.content || ''
        
        if (thinkingContent) {
          // 获取最后一条消息
          let lastMessage = chatStore.messages[chatStore.messages.length - 1]
          
          // 如果不是assistant消息，创建新的
          if (!lastMessage || lastMessage.role !== 'assistant') {
            const newMessage = {
              id: 'temp-' + Date.now(),
              role: 'assistant',
              content: '',
              thinking: '',
              createdAt: new Date()
            }
            chatStore.addMessage(newMessage)
            lastMessage = newMessage
            console.log('🧠 Thinking事件创建新消息:', lastMessage.id)
          }
          
          // 如果这个消息有thinking内容，自动展开推理过程
          if (lastMessage && lastMessage.role === 'assistant' && !expandedThinking.value.has(lastMessage.id)) {
            expandedThinking.value.add(lastMessage.id)
            console.log('🧠 自动展开推理过程:', lastMessage.id, '当前展开列表:', Array.from(expandedThinking.value))
          }
          
          // 累加thinking内容 - 与chunk处理完全一致
          if (lastMessage && lastMessage.role === 'assistant') {
            lastMessage.thinking = (lastMessage.thinking || '') + thinkingContent
            // 触发响应式更新
            chatStore.messages = [...chatStore.messages]
            scrollToBottom()
            console.log('✅ Thinking内容已累加，当前长度:', lastMessage.thinking.length)
          }
        }
      } catch (error) {
        console.error('❌ Error processing thinking event:', error, data)
      }
    }
    
    const handleErrorEvent = (data) => {
      console.error('❌ SSE error event received:', data)
      
      // 显示错误信息
      if (typeof data === 'string' && data.trim()) {
        ElMessage.error(data)
      } else {
        ElMessage.error('发生未知错误')
      }
      
      // 停止加载状态
      chatStore.setLoading(false)
    }
    
    // 滚动到底部
    const scrollToBottom = () => {
      nextTick(() => {
        if (messageList.value) {
          messageList.value.scrollTop = messageList.value.scrollHeight
        }
      })
    }
    
    // 格式化时间
    const formatTime = (time) => {
      if (!time) return ''
      const date = new Date(time)
      return date.toLocaleString('zh-CN', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
    
    // 搜索开关处理
    const onSearchToggle = (value) => {
      if (value) {
        ElMessage.success('联网搜索已开启，AI将能够搜索最新信息')
      } else {
        ElMessage.info('联网搜索已关闭，AI将基于已有知识回答')
      }
      // 保存设置到本地存储
      localStorage.setItem('searchEnabled', value.toString())
    }
    
    // 深度思考开关处理
    const onDeepThinkingToggle = (value) => {
      if (value) {
        ElMessage.success('深度思考已开启，AI将显示详细推理过程（响应可能较慢）')
      } else {
        ElMessage.info('深度思考已关闭，AI将直接给出答案')
      }
      // 保存设置到本地存储
      localStorage.setItem('deepThinking', value.toString())
    }
    
    // 新的按钮切换方法 - 模仿腾讯元宝交互
    const toggleDeepThinking = () => {
      deepThinking.value = !deepThinking.value
      onDeepThinkingToggle(deepThinking.value)
    }
    
    const toggleSearch = () => {
      searchEnabled.value = !searchEnabled.value
      onSearchToggle(searchEnabled.value)
    }
    
    // 复制消息内容
    const copyMessage = async (content) => {
      try {
        await navigator.clipboard.writeText(content)
        ElMessage.success('已复制到剪贴板')
      } catch (error) {
        // 降级处理：使用传统方法复制
        const textArea = document.createElement('textarea')
        textArea.value = content
        document.body.appendChild(textArea)
        textArea.select()
        try {
          document.execCommand('copy')
          ElMessage.success('已复制到剪贴板')
        } catch (err) {
          ElMessage.error('复制失败')
        }
        document.body.removeChild(textArea)
      }
    }
    
    // 加载可用模型
    const loadAvailableModels = async () => {
      try {
        const response = await modelApi.getAllAvailableModels()
        if (response.success) {
          // 展平所有提供者的模型列表
          const allModels = []
          response.data.forEach(provider => {
            if (provider.models && provider.models.length > 0) {
              provider.models.forEach(model => {
                // 添加提供者前缀到模型名称
                model.fullModelId = `${provider.id}-${model.name}`
                model.providerName = provider.name
                model.providerDisplayName = provider.displayName
                allModels.push(model)
              })
            }
          })
          availableModels.value = allModels
          
          // 如果有模型，选择第一个可用的作为默认模型
          if (allModels.length > 0) {
            const firstAvailable = allModels.find(model => model.available)
            if (firstAvailable) {
              selectedModel.value = firstAvailable.name
              selectedProvider.value = firstAvailable.providerName
              console.log('🎯 设置默认模型:', firstAvailable.displayName)
            }
          }
        }
      } catch (error) {
        console.error('加载模型列表失败:', error)
        ElMessage.error('加载模型列表失败')
      }
    }
    
    // 模型选择变更处理
    const onModelChange = (modelName) => {
      const selected = availableModels.value.find(model => model.name === modelName)
      if (selected) {
        selectedProvider.value = selected.providerName
        ElMessage.success(`已选择模型: ${selected.displayName}`)
        // 保存到本地存储
        localStorage.setItem('selectedModel', modelName)
        localStorage.setItem('selectedProvider', selected.providerName)
      }
    }
    
    // 从本地存储加载模型选择
    const loadModelSelection = () => {
      const savedModel = localStorage.getItem('selectedModel')
      const savedProvider = localStorage.getItem('selectedProvider')
      
      if (savedModel && savedProvider) {
        // 验证模型是否仍然可用
        const model = availableModels.value.find(m => m.name === savedModel && m.providerName === savedProvider)
        if (model && model.available) {
          selectedModel.value = savedModel
          selectedProvider.value = savedProvider
          console.log('📥 从本地存储加载模型选择:', model.displayName)
        }
      }
    }
    
    // 检测并提取推理过程
    const extractThinking = (content) => {
      if (!content || typeof content !== 'string') {
        return { thinking: null, content: content || '' }
      }
      
      // 匹配 <think>...</think> 或 <thinking>...</thinking> 标签
      const thinkRegex = /<think(?:ing)?>[\s\S]*?<\/think(?:ing)?>/gi
      const matches = content.match(thinkRegex)
      
      if (matches && matches.length > 0) {
        // 提取推理内容（去掉标签）
        const thinking = matches.map(match => 
          match.replace(/<\/?think(?:ing)?>/gi, '').trim()
        ).join('\n\n')
        
        // 移除原内容中的推理标签，但保持原有的换行和格式
        let cleanContent = content.replace(thinkRegex, '').trim()
        
        return { thinking, content: cleanContent }
      }
      
      return { thinking: null, content: content }
    }
    
    // 直接使用chatStore.messages
    const processedMessages = computed(() => {
      return chatStore.messages
    })
    
    // 切换推理过程展开状态
    const toggleThinking = (messageId) => {
      if (expandedThinking.value.has(messageId)) {
        expandedThinking.value.delete(messageId)
      } else {
        expandedThinking.value.add(messageId)
      }
    }
    
    // 处理搜索指示器点击
    const handleSearchIndicatorClick = ({ messageId, results }) => {
      currentSearchResults.value = results
      currentSearchMessageId.value = messageId
      // 展开右侧面板
      if (rightPanel.value) {
        rightPanel.value.expand()
      }
    }
    
    
    // 监听消息变化，自动滚动
    watch(() => chatStore.messages.length, () => {
      scrollToBottom()
    })
    
    onMounted(() => {
      userStore.loadUserFromStorage()
      if (!userStore.isLoggedIn) {
        this.$router.push('/login')
        return
      }
      loadConversations()
      loadSettings()
      loadAvailableModels().then(() => {
        loadModelSelection()
      })
      
      // EventSource无需全局配置
    })
    
    // 组件销毁时手动清理连接，防止重连
    onBeforeUnmount(() => {
      console.log('🗑️ 组件销毁，清理连接和状态')
      
      // 手动关闭SSE连接
      try {
        closeSSE()
        console.log('✅ SSE连接已手动关闭')
      } catch (e) {
        console.log('⚠️ 关闭SSE连接时出错:', e.message)
      }
      
      // 清理待发送消息状态，停止重连
      pendingMessage.value = ''
      pendingSearchEnabled.value = false
      pendingDeepThinking.value = false
      chatStore.setLoading(false)
      chatStore.setConnected(false)
    })
    
    // 解析搜索结果JSON数据
    const parseSearchResults = (searchResultsData) => {
      if (!searchResultsData) return []
      
      try {
        // 如果已经是对象数组，直接返回
        if (Array.isArray(searchResultsData)) {
          return searchResultsData
        }
        
        // 如果是字符串，尝试解析JSON
        if (typeof searchResultsData === 'string') {
          return JSON.parse(searchResultsData)
        }
        
        return []
      } catch (error) {
        console.error('解析搜索结果失败:', error)
        return []
      }
    }

    return {
      userStore,
      chatStore,
      inputMessage,
      messageList,
      rightPanel,
      searchEnabled,
      deepThinking,
      expandedThinking,
      processedMessages,
      parseSearchResults,
      markdownOptions,
      createNewConversation,
      selectConversation,
      deleteConversation,
      handleSendMessage,
      onSearchToggle,
      onDeepThinkingToggle,
      toggleDeepThinking,
      toggleSearch,
      copyMessage,
      formatTime,
      toggleThinking,
      currentSearchResults,
      currentSearchMessageId,
      handleSearchIndicatorClick,
      leftSidebarCollapsed,
      rightSidebarCollapsed,
      toggleLeftSidebar,
      toggleRightSidebar,
      // 模型相关
      selectedModel,
      availableModels,
      selectedProvider,
      loadAvailableModels,
      onModelChange
    }
  }
}
</script>

<style scoped>
.chat-container {
  height: 100vh;
  display: flex;
}

.sidebar {
  width: 280px;
  background: #f5f5f5;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
}

.sidebar.collapsed {
  width: 60px;
  min-width: 60px;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.sidebar.collapsed .sidebar-header {
  padding: 10px;
}

.sidebar-controls {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.sidebar.collapsed .sidebar-controls {
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.username {
  margin-left: 10px;
  font-weight: 500;
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
}

.conversation-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  position: relative;
  transition: all 0.2s ease;
  border-radius: 8px;
  margin: 4px 8px;
  border-bottom: none;
}

.conversation-item:hover {
  background: #f8f9fa;
  transform: translateX(2px);
}

.conversation-item.active {
  background: linear-gradient(135deg, #e3f2fd 0%, #f0f8ff 100%);
  border-left: 3px solid #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
}

.conversation-title {
  font-weight: 500;
  margin-bottom: 5px;
}

.conversation-time {
  font-size: 11px;
  color: #999;
  opacity: 0.8;
}

.delete-btn {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  opacity: 0;
  transition: opacity 0.2s;
}

.conversation-item:hover .delete-btn {
  opacity: 1;
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow-x: hidden;
}

.welcome {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #666;
}

.chat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px 3% 20px 3%;
  width: 100%;
  box-sizing: border-box;
}

.message-item {
  display: flex;
  flex-direction: column;
  margin-bottom: 12px;
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
  width: 100%;
}

.message-item.user {
  align-items: flex-end;
}

.message-item.assistant {
  align-items: flex-start;
}

.message-content {
  width: auto;
}

.message-item.user .message-content {
  max-width: 70%;
  margin-left: auto;
}

.message-item.assistant .message-content {
  max-width: 100%;
  width: 100%;
}

.message-text {
  background: #f8f9fa;
  padding: 12px 16px;
  border-radius: 16px;
  word-wrap: break-word;
  word-break: break-word;
  overflow-wrap: break-word;
  position: relative;
  display: inline-block;
  width: fit-content;
  min-width: 0;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.message-item.assistant .message-text {
  width: 100%;
}

.message-actions {
  position: absolute;
  top: 8px;
  right: 8px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.message-text:hover .message-actions {
  opacity: 1;
}

.copy-btn {
  padding: 4px 6px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.9);
  color: #666;
  backdrop-filter: blur(4px);
}

.copy-btn:hover {
  background: rgba(255, 255, 255, 1);
  color: #409eff;
  transform: scale(1.05);
}

.message-item.user .message-text {
  background: linear-gradient(135deg, #409eff 0%, #5dade2 100%);
  color: white;
  border-radius: 16px 16px 4px 16px;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.message-item.assistant .message-text {
  border-radius: 16px 16px 16px 4px;
}

.message-item.user .copy-btn {
  background: rgba(255, 255, 255, 0.3);
  color: white;
}

.message-item.user .copy-btn:hover {
  background: rgba(255, 255, 255, 0.5);
  color: white;
}

/* 响应式工具栏设计 */
@media (max-width: 768px) {
  .message-toolbar {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .toolbar-actions {
    align-self: flex-end;
  }
  
  .message-toolbar {
    opacity: 1; /* 移动端始终显示 */
  }
}

/* 工具栏扩展性设计 */
.toolbar-actions .toolbar-btn + .toolbar-btn {
  margin-left: 2px;
}

.toolbar-actions .el-divider {
  height: 16px;
  margin: 0 4px;
}

.message-time {
  font-size: 11px;
  color: #999;
  margin-top: 4px;
}

.typing-indicator {
  display: inline-flex;
  padding: 12px 16px;
  background: #f0f0f0;
  border-radius: 12px 12px 12px 4px;
  width: 100%;
  box-sizing: border-box;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #666;
  margin-right: 4px;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
  margin-right: 0;
}

@keyframes typing {
  0%, 60%, 100% {
    opacity: 0.3;
  }
  30% {
    opacity: 1;
  }
}

.input-area {
  padding: 20px;
  border-top: 1px solid #e0e0e0;
}

/* 功能工具栏 - 腾讯元宝风格 */
.function-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.model-selector {
  width: 180px;
}

.function-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s ease;
  border: 1px solid #e4e4e7;
  background: #ffffff;
  color: #71717a;
}

.function-btn:hover {
  border-color: #d4d4d8;
  background: #f8f9fa;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.function-btn.el-button--primary {
  background: linear-gradient(135deg, #409eff 0%, #36a3f7 100%);
  border-color: #409eff;
  color: white;
}

.function-btn.el-button--success {
  background: linear-gradient(135deg, #67c23a 0%, #5cb85c 100%);
  border-color: #67c23a;
  color: white;
}

.function-btn .dropdown-icon {
  font-size: 12px;
  transition: transform 0.2s ease;
  opacity: 0.7;
}

.function-btn:hover .dropdown-icon {
  opacity: 1;
}

.input-container {
  display: flex;
  gap: 10px;
  align-items: flex-end;
  max-width: 800px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

.send-area {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

.send-btn {
  height: 40px;
  padding: 0 16px;
}

/* 用户消息格式化样式 - 只用于用户消息的纯文本显示 */
.message-body {
  line-height: 1.6;
  word-wrap: break-word;
  white-space: pre-line !important; /* 保持换行，但合并空格 */
  overflow-wrap: break-word; /* 长单词换行 */
}

/* 推理过程样式 - 按照业界最佳实践 */
.thinking-section {
  margin-bottom: 12px;
  border: 1px solid #e8ecf0;
  border-radius: 12px;
  background: linear-gradient(135deg, #f8fafe 0%, #f0f7ff 100%);
  overflow: hidden;
  width: 100%;
  box-sizing: border-box;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.thinking-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f1f3f4;
  border-bottom: 1px solid #e1e4e8;
  cursor: pointer;
  user-select: none;
  transition: background-color 0.2s;
}

.thinking-header:hover {
  background: #e8eaed;
}

.thinking-header.expanded {
  background: #e8f0fe;
  border-bottom-color: #d2e3fc;
}

.thinking-icon {
  color: #1a73e8;
  font-size: 14px;
}

.thinking-label {
  flex: 1;
  font-size: 13px;
  font-weight: 500;
  color: #5f6368;
}

.expand-icon {
  color: #5f6368;
  font-size: 12px;
  transition: transform 0.2s;
}

.thinking-content {
  padding: 16px 24px;
  border-top: 1px solid #e8eaed;
  background: #fafbfc;
}

.thinking-body {
  font-size: 14px;
  line-height: 1.8;
  color: #2c3e50;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

.thinking-body p {
  margin: 0 0 14px 0;
}

.thinking-body p:last-child {
  margin-bottom: 0;
}

/* 优化推理过程显示 - 增加层次感和可读性 */
.thinking-body h1,
.thinking-body h2,
.thinking-body h3,
.thinking-body h4,
.thinking-body h5,
.thinking-body h6 {
  color: #1976d2;
  margin: 20px 0 12px 0;
  font-weight: 600;
}

.thinking-body ul {
  margin: 12px 0;
  padding-left: 24px;
}

.thinking-body ol {
  margin: 12px 0;
  padding-left: 48px;
  list-style-type: decimal !important;
  list-style-position: outside !important;
}

.thinking-body li {
  margin: 8px 0;
  line-height: 1.7;
  padding-left: 4px;
  display: list-item !important;
}

.thinking-body ol li {
  list-style-type: decimal !important;
}

.thinking-body ul li {
  list-style-type: disc !important;
}

.thinking-body blockquote {
  background: #f8f9fa;
  border-left: 4px solid #1976d2;
  margin: 16px 0;
  padding: 12px 16px;
  border-radius: 0 6px 6px 0;
}

.thinking-body pre {
  background: #f8f9fa;
  border: 1px solid #e8eaed;
  border-radius: 6px;
  padding: 12px;
  margin: 12px 0;
  overflow-x: auto;
  font-size: 13px;
}

.thinking-body code {
  background: #f1f3f4;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 13px;
  color: #d73a49;
}

/* v-md-preview 组件样式调整 */
.markdown-content {
  background: transparent !important;
  padding: 0 !important;
}

.markdown-content :deep(.v-md-preview) {
  background: transparent;
  padding: 0;
}

.markdown-content :deep(.vuepress-markdown-body) {
  background: transparent;
  padding: 0;
  color: inherit;
  font-size: 14px;
  line-height: 1.6;
}

/* 表格样式修复 - 确保边框显示 */
.markdown-content :deep(table),
.markdown-content :deep(.github-markdown-body table),
.markdown-content :deep(.v-md-table),
.markdown-content table {
  display: table !important;
  table-layout: auto !important;
  width: 100% !important;
  border-collapse: collapse !important;
  border-spacing: 0 !important;
  margin: 16px 0 !important;
  border: 1px solid #d0d7de !important;
  overflow: visible !important;
}

.markdown-content :deep(thead),
.markdown-content :deep(.github-markdown-body thead),
.markdown-content thead {
  display: table-header-group !important;
}

.markdown-content :deep(tbody),
.markdown-content :deep(.github-markdown-body tbody),
.markdown-content tbody {
  display: table-row-group !important;
}

.markdown-content :deep(tr),
.markdown-content :deep(.github-markdown-body tr),
.markdown-content tr {
  display: table-row !important;
  border-bottom: 1px solid #d0d7de !important;
}

.markdown-content :deep(th),
.markdown-content :deep(td),
.markdown-content :deep(.github-markdown-body th),
.markdown-content :deep(.github-markdown-body td),
.markdown-content th,
.markdown-content td {
  display: table-cell !important;
  box-sizing: border-box !important;
  padding: 8px 12px !important;
  text-align: left !important;
  vertical-align: top !important;
  border: 1px solid #d0d7de !important;
  border-right: 1px solid #d0d7de !important;
  border-bottom: 1px solid #d0d7de !important;
  background-color: #ffffff !important;
}

.markdown-content :deep(th),
.markdown-content :deep(.github-markdown-body th),
.markdown-content th {
  background-color: #f6f8fa !important;
  font-weight: 600 !important;
  border-bottom: 2px solid #d0d7de !important;
}

/* 确保表格第一行和最后一行边框正确 */
.markdown-content :deep(tr:first-child th),
.markdown-content :deep(tr:first-child td),
.markdown-content tr:first-child th,
.markdown-content tr:first-child td {
  border-top: 1px solid #d0d7de !important;
}

.markdown-content :deep(tr:last-child th),
.markdown-content :deep(tr:last-child td),
.markdown-content tr:last-child th,
.markdown-content tr:last-child td {
  border-bottom: 1px solid #d0d7de !important;
}

/* 确保表格第一列和最后一列边框正确 */
.markdown-content :deep(th:first-child),
.markdown-content :deep(td:first-child),
.markdown-content th:first-child,
.markdown-content td:first-child {
  border-left: 1px solid #d0d7de !important;
}

.markdown-content :deep(th:last-child),
.markdown-content :deep(td:last-child),
.markdown-content th:last-child,
.markdown-content td:last-child {
  border-right: 1px solid #d0d7de !important;
}

/* 表格样式完善 - 移除调试样式，使用正常样式 */

/* 悬浮按钮样式 - VS Code风格 */
.floating-left-toggle,
.floating-right-toggle {
  position: fixed;
  top: 50%;
  transform: translateY(-50%);
  width: 24px;
  height: 48px;
  background: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 0 6px 6px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 1000;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.floating-left-toggle {
  left: 0;
  border-left: none;
  border-radius: 0 6px 6px 0;
}

.floating-right-toggle {
  right: 0;
  border-right: none;
  border-radius: 6px 0 0 6px;
}

.floating-left-toggle:hover,
.floating-right-toggle:hover {
  background: #f8f9fa;
  transform: translateY(-50%) scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.floating-left-toggle .el-icon,
.floating-right-toggle .el-icon {
  font-size: 14px;
  color: #666;
  transition: color 0.2s ease;
}

.floating-left-toggle:hover .el-icon,
.floating-right-toggle:hover .el-icon {
  color: #409eff;
}

/* 当侧边栏展开时，调整悬浮按钮位置 */
.sidebar:not(.collapsed) + .chat-area ~ .floating-left-toggle {
  left: 280px;
  transition: left 0.3s ease, transform 0.2s ease;
}

/* 右侧面板展开时，调整右侧按钮位置 */
.floating-right-toggle {
  right: 0;
  transition: right 0.3s ease, transform 0.2s ease;
}

</style>