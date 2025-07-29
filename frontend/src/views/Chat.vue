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
            text
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
                  <div class="thinking-body" v-html="formatMessage(message.thinking)"></div>
                </div>
              </div>
              
              <div class="message-text">
                <div class="message-body" v-html="formatMessage(message.processedContent)"></div>
                <div class="message-actions">
                  <el-button
                    type="text"
                    size="small"
                    @click="copyMessage(message.processedContent)"
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
  </div>
</template>

<script>
import { ref, onMounted, nextTick, watch, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'
import { useChatStore } from '../stores/chat'
import { conversationApi, chatApi } from '../api'
import MarkdownIt from 'markdown-it'

export default {
  name: 'Chat',
  setup() {
    const userStore = useUserStore()
    const chatStore = useChatStore()
    const inputMessage = ref('')
    const messageList = ref()
    const searchEnabled = ref(true) // 默认开启搜索
    const expandedThinking = ref(new Set()) // 展开的推理过程ID集合
    
    // 初始化Markdown渲染器
    const md = new MarkdownIt({
      html: true,          // 启用HTML标签
      breaks: true,        // 将换行符转换为<br>
      linkify: true,       // 自动识别链接
      typographer: true    // 启用智能引号等排版特性
    })
    
    // 配置markdown-it以更好地处理中文内容
    md.configure('commonmark')
    
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
          title: '新对话'
        })
        if (response.success) {
          chatStore.addConversation(response.data)
          selectConversation(response.data)
        }
      } catch (error) {
        ElMessage.error('创建对话失败')
      }
    }
    
    // 选择对话
    const selectConversation = async (conversation) => {
      chatStore.setCurrentConversation(conversation)
      
      // 断开之前的SSE连接
      chatStore.disconnectSSE()
      
      // 加载消息历史
      try {
        const response = await conversationApi.getMessages(conversation.id)
        if (response.success) {
          chatStore.setMessages(response.data)
          scrollToBottom()
        }
      } catch (error) {
        console.error('Load messages error:', error)
      }
      
      // 建立SSE连接
      setupSSE(conversation.id)
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
      } catch (error) {
        ElMessage.error('发送消息失败')
        chatStore.setLoading(false)
      }
    }
    
    // 设置SSE连接
    const setupSSE = (conversationId) => {
      const eventSource = chatStore.connectSSE(conversationId)
      
      eventSource.addEventListener('message', (event) => {
        try {
          const data = JSON.parse(event.data)
          handleSSEMessage(data)
        } catch (error) {
          console.error('Parse SSE message error:', error)
        }
      })
      
      eventSource.addEventListener('search', (event) => {
        try {
          const data = JSON.parse(event.data)
          handleSearchEvent(data)
        } catch (error) {
          console.error('Parse search event error:', error)
        }
      })
      
      eventSource.addEventListener('error', (event) => {
        console.error('SSE error:', event)
        chatStore.setLoading(false)
      })
      
      eventSource.onerror = () => {
        chatStore.setLoading(false)
      }
    }
    
    // 处理SSE消息
    const handleSSEMessage = (data) => {
      switch (data.type) {
        case 'start':
          // 开始接收AI回复，添加空消息
          chatStore.addMessage({
            id: 'temp-' + Date.now(),
            role: 'assistant',
            content: '',
            createdAt: new Date()
          })
          break
          
        case 'chunk':
          // 追加消息内容
          chatStore.updateLastMessage(data.content)
          scrollToBottom()
          break
          
        case 'end':
          // 消息结束，更新消息ID
          if (chatStore.messages.length > 0) {
            const lastMessage = chatStore.messages[chatStore.messages.length - 1]
            if (data.messageId) {
              lastMessage.id = data.messageId
            }
          }
          chatStore.setLoading(false)
          break
      }
    }
    
    // 处理搜索事件
    const handleSearchEvent = (data) => {
      if (data.type === 'start') {
        ElMessage.info('正在搜索相关信息...')
      } else if (data.type === 'complete') {
        ElMessage.success('搜索完成')
      }
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
    
    // 处理消息，提取推理过程
    const processedMessages = computed(() => {
      return chatStore.messages.map(message => {
        if (message.role === 'assistant' && message.content) {
          const { thinking, content } = extractThinking(message.content)
          return {
            ...message,
            processedContent: content,
            thinking: thinking
          }
        }
        return {
          ...message,
          processedContent: message.content,
          thinking: null
        }
      })
    })
    
    // 切换推理过程展开状态
    const toggleThinking = (messageId) => {
      if (expandedThinking.value.has(messageId)) {
        expandedThinking.value.delete(messageId)
      } else {
        expandedThinking.value.add(messageId)
      }
    }
    
    // 使用Markdown渲染器格式化消息内容
    const formatMessage = (content) => {
      if (!content) return ''
      
      try {
        // 清理内容，确保换行符正确
        const cleanedContent = content.trim()
        
        // 使用markdown-it渲染内容
        const rendered = md.render(cleanedContent)
        
        // 调试输出
        console.log('Original content:', cleanedContent)
        console.log('Rendered HTML:', rendered)
        
        return rendered
      } catch (error) {
        console.error('Markdown render error:', error)
        // 降级处理：返回带换行的HTML
        const div = document.createElement('div')
        div.textContent = content
        return div.innerHTML.replace(/\n/g, '<br>')
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
    })
    
    return {
      userStore,
      chatStore,
      inputMessage,
      messageList,
      searchEnabled,
      expandedThinking,
      processedMessages,
      createNewConversation,
      selectConversation,
      deleteConversation,
      handleSendMessage,
      onSearchToggle,
      copyMessage,
      formatTime,
      formatMessage,
      toggleThinking
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

/* 消息格式化样式 */
.message-body {
  line-height: 1.6;
  word-wrap: break-word;
  white-space: normal;
}

.message-body p {
  margin: 0 0 12px 0;
  display: block;
}

.message-body p:last-child {
  margin-bottom: 0;
}

/* 确保标题样式正确 */
.message-body h1, .message-body h2, .message-body h3, .message-body h4, .message-body h5, .message-body h6 {
  margin: 16px 0 8px 0;
  font-weight: bold;
  line-height: 1.4;
}

.message-body h1 { font-size: 1.6em; }
.message-body h2 { font-size: 1.4em; }
.message-body h3 { font-size: 1.2em; }
.message-body h4 { font-size: 1.1em; }
.message-body h5 { font-size: 1.05em; }
.message-body h6 { font-size: 1em; }

.message-body h1:first-child, .message-body h2:first-child, .message-body h3:first-child,
.message-body h4:first-child, .message-body h5:first-child, .message-body h6:first-child {
  margin-top: 0;
}

/* 分隔线样式 */
.message-body hr {
  border: none;
  border-top: 1px solid rgba(0, 0, 0, 0.15);
  margin: 16px 0;
}

/* 列表样式 */
.message-body ul, .message-body ol {
  margin: 8px 0;
  padding-left: 20px;
}

.message-body ol {
  list-style-type: decimal;
}

.message-body ul {
  list-style-type: disc;
}

.message-body li {
  margin: 4px 0;
  line-height: 1.5;
}

/* 表格样式 */
.message-body table {
  border-collapse: collapse;
  width: 100%;
  margin: 12px 0;
  font-size: 14px;
}

.message-body th, .message-body td {
  border: 1px solid rgba(0, 0, 0, 0.15);
  padding: 8px 12px;
  text-align: left;
}

.message-body th {
  background: rgba(0, 0, 0, 0.05);
  font-weight: 600;
}

.message-body tr:nth-child(even) {
  background: rgba(0, 0, 0, 0.02);
}

/* 代码样式 */
.message-body code {
  background: rgba(0, 0, 0, 0.1);
  padding: 2px 4px;
  border-radius: 3px;
  font-family: 'Monaco', 'Consolas', monospace;
  font-size: 0.9em;
}

.message-body strong {
  font-weight: 600;
}

.message-body em {
  font-style: italic;
}

/* 用户消息中的样式调整 */
.message-item.user .message-body code {
  background: rgba(255, 255, 255, 0.3);
}

.message-item.user .message-body hr {
  border-top-color: rgba(255, 255, 255, 0.3);
}

.message-item.user .message-body th, .message-item.user .message-body td {
  border-color: rgba(255, 255, 255, 0.3);
}

.message-item.user .message-body th {
  background: rgba(255, 255, 255, 0.15);
}

.message-item.user .message-body tr:nth-child(even) {
  background: rgba(255, 255, 255, 0.08);
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
</style>