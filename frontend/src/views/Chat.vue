<template>
  <div class="chat-container">
    <!-- 侧边栏 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <div class="user-info">
          <el-avatar :size="40" :src="userStore.currentUser?.avatar">
            {{ userStore.currentUser?.nickname?.charAt(0) }}
          </el-avatar>
          <span class="username">{{ userStore.currentUser?.nickname }}</span>
        </div>
        <el-button @click="createNewConversation" type="primary" size="small">
          <el-icon><Plus /></el-icon>
          新对话
        </el-button>
      </div>
      
      <div class="conversation-list">
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
            link
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
            <div class="message-avatar">
              <el-avatar v-if="message.role === 'user'" :size="32">
                {{ userStore.currentUser?.nickname?.charAt(0) }}
              </el-avatar>
              <el-avatar v-else :size="32" class="ai-avatar">AI</el-avatar>
            </div>
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
                  <v-md-preview 
                    :text="message.thinking || ''"
                    class="thinking-body"
                  />
                </div>
              </div>
              
              <div class="message-text">
                <!-- 使用 v-md-preview 组件 -->
                <div v-if="message.role === 'user'" class="message-body">
                  {{ message.content }}
                </div>
                <v-md-preview 
                  v-else
                  :text="message.content || ''"
                  class="message-body markdown-content"
                />
                <div class="message-actions">
                  <el-button
                    link
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
            <div class="message-avatar">
              <el-avatar :size="32" class="ai-avatar">AI</el-avatar>
            </div>
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
          <!-- 搜索设置栏 -->
          <div class="search-settings">
            <div class="search-toggle">
              <el-switch
                v-model="searchEnabled"
                inline-prompt
                active-text="🔍"
                inactive-text="🚫"
                @change="onSearchToggle"
              />
              <span class="search-label">
                {{ searchEnabled ? '联网搜索已开启' : '联网搜索已关闭' }}
              </span>
            </div>
            <div class="search-status" v-if="searchEnabled">
              <el-tag size="small" type="success">
                <el-icon><Connection /></el-icon>
                智能搜索
              </el-tag>
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
    
    <!-- 右侧面板 -->
    <RightPanel 
      ref="rightPanel"
      :searchResults="currentSearchResults"
      :currentMessageId="currentSearchMessageId"
    />
  </div>
</template>

<script>
import { ref, onMounted, onBeforeUnmount, nextTick, watch, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'
import { useChatStore } from '../stores/chat'
import { conversationApi, chatApi } from '../api'
import VMdPreview from '@kangc/v-md-editor/lib/preview'
import '@kangc/v-md-editor/lib/style/preview.css'
import githubTheme from '@kangc/v-md-editor/lib/theme/github.js'
import '@kangc/v-md-editor/lib/theme/style/github.css'
import hljs from 'highlight.js'
import { debounce } from 'lodash-es'
import SearchIndicator from '../components/SearchIndicator.vue'
import RightPanel from '../components/RightPanel.vue'

// 使用 GitHub 主题，配置代码高亮
VMdPreview.use(githubTheme, {
  Hljs: hljs,
})

export default {
  name: 'Chat',
  components: {
    VMdPreview,
    SearchIndicator,
    RightPanel
  },
  setup(props, { emit }) {
    const userStore = useUserStore()
    const chatStore = useChatStore()
    const inputMessage = ref('')
    const messageList = ref()
    const rightPanel = ref() // 右侧面板引用
    const searchEnabled = ref(true) // 默认开启搜索
    const expandedThinking = ref(new Set()) // 展开的推理过程ID集合
    const eventSource = ref(null) // 存储EventSource实例
    
    // 右侧面板状态管理
    const currentSearchResults = ref([])
    const currentSearchMessageId = ref(null)
    
    // 加载对话列表
    const loadConversations = async () => {
      try {
        const response = await conversationApi.getList(userStore.currentUser.id)
        if (response.success) {
          chatStore.setConversations(response.data)
        }
      } catch (error) {
        console.error('Load conversations error:', error)
      }
    }
    
    // 创建新对话
    const createNewConversation = async () => {
      try {
        const response = await conversationApi.create(userStore.currentUser.id, {
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
    
    // 选择对话 - 使用标准EventSource
    const selectConversation = async (conversation) => {
      // 防止重复点击同一对话
      if (chatStore.currentConversation?.id === conversation.id) {
        console.log('⚠️ 已经是当前对话，跳过切换')
        return
      }
      
      console.log('🔄 切换到对话:', conversation.id)
      chatStore.setCurrentConversation(conversation)
      
      // 断开之前的SSE连接
      disconnectSSE()
      
      // 加载消息历史
      try {
        const response = await conversationApi.getMessages(conversation.id)
        if (response.success) {
          chatStore.setMessages(response.data)
          
          // 自动展开所有包含thinking的消息
          response.data.forEach(msg => {
            if (msg.role === 'assistant' && msg.thinking) {
              expandedThinking.value.add(msg.id)
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
            // 清空右侧面板并确保收起状态
            currentSearchResults.value = []
            currentSearchMessageId.value = null
          }
          
          scrollToBottom()
        }
      } catch (error) {
        console.error('Load messages error:', error)
      }
      
      // 建立标准SSE连接
      setupEventSource(conversation.id)
    }
    
    // 断开SSE连接
    const disconnectSSE = () => {
      if (eventSource.value) {
        console.log('🔌 断开SSE连接')
        eventSource.value.close()
        eventSource.value = null
      }
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
    const handleSendMessage = async () => {
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
      
      try {
        // 发送消息到后端，包含搜索开关状态
        await chatApi.sendMessage(chatStore.currentConversation.id, {
          content: message,
          searchEnabled: searchEnabled.value
        })
        
        // 重新加载对话列表以获取更新的标题
        loadConversations()
      } catch (error) {
        ElMessage.error('发送消息失败')
        chatStore.setLoading(false)
      }
    }
    
    // 建立标准EventSource连接
    const setupEventSource = (conversationId) => {
      // 先断开现有连接
      disconnectSSE()
      
      console.log('🔗 建立SSE连接到对话:', conversationId)
      
      // 创建标准EventSource
      const source = new EventSource(`/api/chat/stream/${conversationId}`)
      eventSource.value = source
      
      // 统一SSE事件分发器
      source.onmessage = (event) => {
        try {
          // 解析标准SSE事件数据
          const sseEvent = JSON.parse(event.data)
          const { type, data } = sseEvent
          
          console.log('📨 收到SSE事件:', type, data)
          
          // 根据事件类型分发处理
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
        } catch (error) {
          console.error('❌ 解析SSE事件失败:', error, event.data)
        }
      }
      
      source.onerror = (error) => {
        console.error('❌ SSE连接错误:', error)
        chatStore.setLoading(false)
        chatStore.setConnected(false)
        
        // 只在真正的连接错误时显示提示
        if (source.readyState === EventSource.CLOSED) {
          console.debug('🔌 SSE连接正常关闭')
        } else {
          ElMessage.error('连接异常，请刷新页面重试')
        }
      }
      
      source.onopen = () => {
        console.log('✅ SSE连接已建立')
        chatStore.setConnected(true)
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
            createdAt: new Date()
          }
          chatStore.addMessage(newMessage)
          lastMessage = newMessage
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
            lastMessage.id = data.messageId
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
              id: 'temp-thinking-' + Date.now(),
              role: 'assistant',
              content: '',
              thinking: '',
              createdAt: new Date()
            }
            chatStore.addMessage(newMessage)
            lastMessage = newMessage
            
            // thinking开始时自动展开推理过程
            expandedThinking.value.add(lastMessage.id)
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
    
    // 从本地存储加载搜索设置
    const loadSearchSettings = () => {
      const saved = localStorage.getItem('searchEnabled')
      if (saved !== null) {
        searchEnabled.value = saved === 'true'
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
      loadSearchSettings()
      
      // EventSource无需全局配置
    })
    
    // 组件销毁前确保断开SSE连接
    onBeforeUnmount(() => {
      console.log('🗑️ 组件销毁，断开SSE连接')
      disconnectSSE()
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
      expandedThinking,
      processedMessages,
      parseSearchResults,
      createNewConversation,
      selectConversation,
      deleteConversation,
      handleSendMessage,
      onSearchToggle,
      copyMessage,
      formatTime,
      toggleThinking,
      currentSearchResults,
      currentSearchMessageId,
      handleSearchIndicatorClick
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
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
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
  padding: 15px 20px;
  border-bottom: 1px solid #e0e0e0;
  cursor: pointer;
  position: relative;
  transition: background-color 0.2s;
}

.conversation-item:hover {
  background: #f0f0f0;
}

.conversation-item.active {
  background: #e3f2fd;
}

.conversation-title {
  font-weight: 500;
  margin-bottom: 5px;
}

.conversation-time {
  font-size: 12px;
  color: #666;
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
  padding: 20px;
}

.message-item {
  display: flex;
  margin-bottom: 20px;
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-avatar {
  margin: 0 10px;
}

.ai-avatar {
  background: #409eff;
}

.message-content {
  max-width: 70%;
}

.message-item.user .message-content {
  text-align: right;
}

.message-text {
  background: #f0f0f0;
  padding: 10px 15px;
  border-radius: 10px;
  word-wrap: break-word;
  position: relative;
}

.message-actions {
  position: absolute;
  top: 5px;
  right: 5px;
  opacity: 0;
  transition: opacity 0.2s;
}

.message-text:hover .message-actions {
  opacity: 1;
}

.copy-btn {
  padding: 4px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.8);
  color: #666;
}

.copy-btn:hover {
  background: rgba(255, 255, 255, 1);
  color: #409eff;
}

.message-item.user .message-text {
  background: #409eff;
  color: white;
}

.message-item.user .copy-btn {
  background: rgba(255, 255, 255, 0.3);
  color: white;
}

.message-item.user .copy-btn:hover {
  background: rgba(255, 255, 255, 0.5);
  color: white;
}

.message-time {
  font-size: 12px;
  color: #666;
  margin-top: 5px;
}

.typing-indicator {
  display: flex;
  padding: 10px 15px;
  background: #f0f0f0;
  border-radius: 10px;
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

.search-settings {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 15px;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.search-toggle {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-label {
  font-size: 14px;
  color: #495057;
  font-weight: 500;
}

.search-status {
  display: flex;
  align-items: center;
  gap: 8px;
}

.input-container {
  display: flex;
  gap: 10px;
  align-items: flex-end;
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
  border: 1px solid #e1e4e8;
  border-radius: 8px;
  background: #f6f8fa;
  overflow: hidden;
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
  padding: 12px;
  border-top: 1px solid #e8eaed;
  background: #fafbfc;
}

.thinking-body {
  font-size: 13px;
  line-height: 1.5;
  color: #3c4043;
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
}

.thinking-body p {
  margin: 0 0 8px 0;
}

.thinking-body p:last-child {
  margin-bottom: 0;
}

.thinking-body pre {
  background: #f8f9fa;
  border: 1px solid #e8eaed;
  border-radius: 4px;
  padding: 8px;
  margin: 8px 0;
  overflow-x: auto;
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

/* 修复表格对齐问题 - 覆盖github-markdown-body的display: block */
.markdown-content :deep(.github-markdown-body table) {
  display: table !important;
  table-layout: fixed !important;
  width: 100% !important;
  border-collapse: collapse !important;
  overflow: visible !important;
}

.markdown-content :deep(.github-markdown-body thead) {
  display: table-header-group !important;
}

.markdown-content :deep(.github-markdown-body tbody) {
  display: table-row-group !important;
}

.markdown-content :deep(.github-markdown-body tr) {
  display: table-row !important;
}

.markdown-content :deep(.github-markdown-body th),
.markdown-content :deep(.github-markdown-body td) {
  display: table-cell !important;
  box-sizing: border-box !important;
  padding: 8px 12px !important;
  text-align: left !important;
  vertical-align: top !important;
  border: 1px solid #d0d7de !important;
}



</style>