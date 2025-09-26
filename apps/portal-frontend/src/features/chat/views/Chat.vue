<template>
  <div class="chat-page">
    <div class="sidebar">
      <div class="user" v-if="userStore.currentUser">
        <div class="avatar">{{ userStore.currentUser.nickname?.charAt(0) }}</div>
        <div class="name">{{ userStore.currentUser.nickname }}</div>
      </div>
      <div class="actions">
        <el-button type="primary" size="small" @click="createConversation">新对话</el-button>
      </div>
      <div class="list">
        <div
          v-for="c in chatStore.conversations"
          :key="c.id"
          :class="['item', {active: chatStore.currentConversation?.id === c.id}]"
          @click="selectConversation(c)"
        >{{ c.title || ('对话 ' + c.id) }}</div>
      </div>
    </div>

    <div class="main">
      <div v-if="!chatStore.currentConversation" class="welcome">
        请选择左侧对话或新建对话
      </div>

      <div v-else class="content">
        <div class="messages" ref="messageList">
          <div v-for="m in chatStore.messages" :key="m.id" :class="['msg', m.role]">
            <div class="time">{{ formatTime(m.createdAt) }}</div>
            <div class="body">
              <div v-if="m.role==='user'">{{ m.content }}</div>
              <MarkdownView v-else :content="String(m.content||'')" />
            </div>
          </div>
        </div>
        <div class="input">
          <el-input v-model="input" type="textarea" :rows="2" placeholder="输入你的问题..." @keydown.enter.prevent="send" />
          <div class="ops">
            <el-checkbox v-model="searchEnabled">联网搜索</el-checkbox>
            <el-checkbox v-model="deepThinking">深度思考</el-checkbox>
            <el-button type="primary" :disabled="!input.trim() || chatStore.isLoading" @click="send">发送</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../chat/stores/user'
import { useChatStore } from '../../chat/stores/chat'
import { conversationApi } from '../../chat/api'
import MarkdownView from '../../chat/components/MarkdownView.vue'

const userStore = useUserStore()
const chatStore = useChatStore()
const input = ref('')
const searchEnabled = ref(true)
const deepThinking = ref(false)
const messageList = ref()

const formatTime = (t) => {
  const d = t ? new Date(t) : new Date()
  return d.toLocaleString()
}

const scrollToBottom = async () => {
  await nextTick()
  try { messageList.value.scrollTop = messageList.value.scrollHeight } catch {}
}

const loadConversations = async () => {
  try {
    const resp = await conversationApi.getList(userStore.currentUser.id)
    if (resp?.success) chatStore.setConversations(resp.data)
  } catch (e) { console.warn(e) }
}

const selectConversation = async (c) => {
  chatStore.setCurrentConversation(c)
  const resp = await conversationApi.getMessages(c.id)
  chatStore.setMessages(resp?.data || [])
  await scrollToBottom()
}

const createConversation = async () => {
  try {
    const resp = await conversationApi.create({ userId: userStore.currentUser.id, title: '新对话' })
    if (resp?.success) {
      await loadConversations()
      const created = (chatStore.conversations || [])[0] || resp.data
      await selectConversation(created)
    }
  } catch (e) { ElMessage.error('新建对话失败') }
}

let es = null
const closeSSE = () => { try { es && es.close() } catch {} es = null }

const send = async () => {
  if (!chatStore.currentConversation?.id || !input.value.trim()) return
  const content = input.value
  input.value = ''

  // 保存用户消息（本地立即展示）
  chatStore.addMessage({ id: Date.now(), role: 'user', content, createdAt: new Date() })
  // 预置一条 assistant 草稿
  const draftId = Date.now() + 1
  chatStore.addMessage({ id: draftId, role: 'assistant', content: '', createdAt: new Date() })
  chatStore.setLoading(true)

  // 建立 SSE
  closeSSE()
  const params = new URLSearchParams({ message: content, searchEnabled: String(searchEnabled.value), deepThinking: String(deepThinking.value) })
  es = new EventSource(`/api/chat/stream/${chatStore.currentConversation.id}?${params.toString()}`)

  es.addEventListener('start', (e) => { /* 可选处理 */ })
  es.addEventListener('chunk', (e) => {
    try {
      const data = JSON.parse(e.data)
      const chunk = (typeof data === 'string') ? data : (data?.data || data?.content || '')
      const last = chatStore.messages[chatStore.messages.length - 1]
      if (last && last.role === 'assistant') { last.content = (last.content || '') + chunk; chatStore.messages = [...chatStore.messages] }
      scrollToBottom()
    } catch {}
  })
  es.addEventListener('end', (e) => {
    chatStore.setLoading(false)
    closeSSE()
  })
  es.addEventListener('error', () => {
    chatStore.setLoading(false)
    closeSSE()
    ElMessage.error('SSE 连接出错')
  })
}

onMounted(async () => {
  userStore.loadUserFromStorage()
  if (!userStore.isLoggedIn) return
  await loadConversations()
})

</script>

<style scoped>
.chat-page { display: flex; height: 100vh; }
.sidebar { width: 260px; background: #fff; border-right: 1px solid #eee; padding: 12px; box-sizing: border-box; }
.main { flex: 1; display: flex; flex-direction: column; }
.user { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; }
.avatar { width: 36px; height: 36px; border-radius: 50%; background: #409eff; color: #fff; display:flex; align-items:center; justify-content:center; }
.list { overflow: auto; max-height: calc(100vh - 160px); }
.item { padding: 8px; border-radius: 6px; cursor: pointer; }
.item.active { background: #f0f7ff; }
.content { display: flex; flex-direction: column; height: 100%; }
.messages { flex: 1; overflow: auto; padding: 12px; }
.msg { margin-bottom: 8px; }
.msg .time { color: #888; font-size: 12px; }
.msg.user .body { background: #e8f3ff; padding: 8px 10px; border-radius: 8px; display: inline-block; }
.msg.assistant .body { background: #fff; padding: 8px 10px; border-radius: 8px; display: inline-block; box-shadow: 0 2px 8px rgba(0,0,0,.04); }
.input { border-top: 1px solid #eee; padding: 10px; background: #fff; }
.ops { display:flex; justify-content: space-between; align-items: center; margin-top: 6px; }
</style>
