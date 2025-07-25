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
            v-for="message in chatStore.messages"
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
              <div class="message-text">{{ message.content }}</div>
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
import { ref, onMounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'
import { useChatStore } from '../stores/chat'
import { conversationApi, chatApi } from '../api'

export default {
  name: 'Chat',
  setup() {
    const userStore = useUserStore()
    const chatStore = useChatStore()
    const inputMessage = ref('')
    const messageList = ref()
    const searchEnabled = ref(true) // 默认开启搜索
    
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
    
    // 从本地存储加载搜索设置
    const loadSearchSettings = () => {
      const saved = localStorage.getItem('searchEnabled')
      if (saved !== null) {
        searchEnabled.value = saved === 'true'
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
      createNewConversation,
      selectConversation,
      deleteConversation,
      handleSendMessage,
      onSearchToggle,
      formatTime
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
  white-space: pre-wrap;
}

.message-item.user .message-text {
  background: #409eff;
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
</style>