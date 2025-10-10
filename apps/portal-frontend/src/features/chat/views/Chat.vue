
<template>
  <div class="chat-page">
    <div class="chat-shell" :class="shellClass">
      <aside class="sidebar" :class="{ 'sidebar--collapsed': isLeftCollapsed }">
        <div class="sidebar__top">
          <el-button
            class="sidebar__collapse"
            text
            circle
            :icon="isLeftCollapsed ? Expand : Fold"
            @click="toggleLeft"
          />
          <div v-if="userStore.currentUser" class="sidebar__identity">
            <div class="sidebar__avatar">{{ userInitial }}</div>
            <div class="sidebar__identity-details">
              <span class="sidebar__name">{{ userStore.currentUser.nickname || '未命名用户' }}</span>
              <span v-if="userSecondary" class="sidebar__meta">{{ userSecondary }}</span>
            </div>
          </div>
        </div>

        <el-button
          class="sidebar__new"
          type="primary"
          size="small"
          :loading="isCreatingConversation"
          @click="createConversation"
        >
          <el-icon><Plus /></el-icon>
          <span class="sidebar__new-text">新对话</span>
        </el-button>

        <div v-if="chatStore.conversations?.length" class="sidebar__list">
          <div
            v-for="c in chatStore.conversations"
            :key="c.id"
            :class="['conversation', { 'conversation--active': chatStore.currentConversation?.id === c.id }]"
            :title="c.title?.trim() || ('对话 ' + c.id)"
            @click="selectConversation(c)"
          >
            <div class="conversation__badge">{{ getConversationInitial(c) }}</div>
            <div class="conversation__body">
              <div class="conversation__title">{{ c.title?.trim() || ('对话 ' + c.id) }}</div>
              <div class="conversation__meta">{{ formatConversationMeta(c) }}</div>
            </div>
          </div>
        </div>
        <div v-else class="sidebar__empty">
          <p>暂无历史对话</p>
          <p>点击「新对话」立即开始交流</p>
        </div>
      </aside>

      <section class="main">
        <header class="main__header" :class="{ 'main__header--inactive': !chatStore.currentConversation }">
          <div class="main__title-block">
            <el-button
              class="main__collapse-btn"
              text
              circle
              :icon="isLeftCollapsed ? Expand : Fold"
              @click="toggleLeft"
            />
            <div>
              <h1 class="main__title">{{ currentConversationTitle }}</h1>
              <p class="main__meta">{{ messageCountLabel }}</p>
            </div>
          </div>
          <div class="main__controls">
            <el-select
              v-model="selectedModelKey"
              :placeholder="hasAvailableModel ? '选择模型' : '暂无可用模型'"
              size="small"
              class="main__model-select"
              filterable
              :disabled="!hasAvailableModel"
            >
              <el-option-group
                v-for="group in providerOptions"
                :key="group.key"
                :label="group.label"
                :disabled="group.disabled"
              >
                <el-option
                  v-for="option in group.options"
                  :key="option.key"
                  :label="option.label"
                  :value="option.key"
                  :disabled="option.disabled"
                >
                  <div class="model-option">
                    <div class="model-option__label">
                      {{ option.label }}
                      <span v-if="option.supportsStreaming" class="model-option__tag">流式</span>
                      <span
                        v-if="option.supportsThinking"
                        class="model-option__tag model-option__tag--thinking"
                      >深思</span>
                    </div>
                    <span v-if="option.meta" class="model-option__meta">{{ option.meta }}</span>
                  </div>
                </el-option>
              </el-option-group>
            </el-select>
            <el-tag v-if="chatStore.isLoading" size="small" type="warning">生成中...</el-tag>
            <el-button
              class="main__collapse-btn"
              text
              circle
              :icon="isRightCollapsed ? Expand : Fold"
              @click="toggleRight"
            />
          </div>
        </header>

        <div v-if="!chatStore.currentConversation" class="empty-state">
          <h2>开始新的对话</h2>
          <p>在左侧选择一个已有对话，或点击新对话按钮，即可开启与 AI 的交流。</p>
        </div>

        <div v-else class="messages" ref="messageList">
          <transition-group name="msg-fade" tag="div" class="messages__inner">
            <div
              v-for="m in chatStore.messages"
              :key="m.id"
              :class="['message', `message--${m.role}`]"
            >
              <div class="message__avatar">{{ m.role === 'user' ? userInitial : 'AI' }}</div>
              <div class="message__bubble">
                <div class="message__bubble-header">
                  <span class="message__role">{{ m.role === 'user' ? '你' : '助手' }}</span>
                  <div class="message__actions">
                    <el-tooltip content="复制内容" placement="top">
                      <el-button text circle size="small" :icon="DocumentCopy" @click="copyMessage(m)" />
                    </el-tooltip>
                  </div>
                </div>
                <div class="message__time">{{ formatTime(m.createdAt) }}</div>
                <div class="message__content">
                  <template v-if="m.role === 'user'">
                    {{ m.content }}
                  </template>
                  <template v-else>
                    <MarkdownView :content="String(m.content || '')" />
                  </template>
                </div>
              </div>
            </div>
          </transition-group>
          <div v-if="chatStore.isLoading" class="typing-indicator">
            <span></span><span></span><span></span>
          </div>
        </div>

        <footer class="main__composer">
          <el-input
            class="main__input"
            v-model="input"
            type="textarea"
            :rows="3"
            :disabled="!chatStore.currentConversation"
            :placeholder="chatStore.currentConversation ? '输入你的问题，Enter 发送，Shift+Enter 换行' : '请先在左侧选择或创建一个对话'"
            @keydown.enter="handleEnter"
          />
          <div class="main__toolbar">
            <div class="main__options">
              <el-checkbox v-model="searchEnabled">联网搜索</el-checkbox>
              <el-checkbox v-model="deepThinking">深度思考</el-checkbox>
            </div>
            <div class="main__actions">
              <el-button
                type="primary"
                size="large"
                :disabled="!chatStore.currentConversation || !input.trim() || chatStore.isLoading || !selectedModelKey || !hasAvailableModel"
                :loading="chatStore.isLoading"
                @click="send"
              >发送</el-button>
            </div>
          </div>
        </footer>
      </section>

      <aside class="insight" :class="{ 'insight--collapsed': isRightCollapsed }">
        <div class="insight__header">
          <div class="insight__header-left">
            <el-icon><Search /></el-icon>
            <span class="insight__title">搜索引用</span>
          </div>
          <el-button
            class="insight__collapse"
            text
            circle
            :icon="isRightCollapsed ? Expand : Fold"
            @click="toggleRight"
          />
        </div>
        <div v-if="isRightCollapsed" class="insight__collapsed-label">引用</div>
        <div v-else class="insight__body">
          <div v-if="!searchEnabled" class="insight__empty">
            <p>未开启联网搜索</p>
            <p>勾选左下方「联网搜索」后可查看引文</p>
          </div>
          <div v-else-if="!references.length" class="insight__empty">
            <p>暂无引用数据</p>
            <p>发送问题后将展示命中的参考资料</p>
          </div>
          <div v-else class="insight__list">
            <div
              v-for="(ref, index) in references"
              :key="referenceKey(ref, index)"
              class="insight__item"
              @click="openReference(ref)"
            >
              <div class="insight__item-header">
                <el-icon><Link /></el-icon>
                <div class="insight__item-title">{{ ref.title || ref.name || '未命名引用' }}</div>
              </div>
              <div class="insight__item-meta">{{ formatReferenceSource(ref) }}</div>
              <div v-if="ref.snippet || ref.summary" class="insight__item-snippet">
                {{ ref.snippet || ref.summary }}
              </div>
            </div>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { Fold, Expand, DocumentCopy, Plus, Search, Link } from '@element-plus/icons-vue'
import { useUserStore } from '../../chat/stores/user'
import { useChatStore } from '../../chat/stores/chat'
import { conversationApi, modelApi } from '../../chat/api'
import MarkdownView from '../../chat/components/MarkdownView.vue'

const userStore = useUserStore()
const chatStore = useChatStore()

const input = ref('')
const searchEnabled = ref(true)
const deepThinking = ref(false)
const messageList = ref(null)
const isCreatingConversation = ref(false)
const isLeftCollapsed = ref(false)
const isRightCollapsed = ref(false)

const shellClass = computed(() => ({
  'chat-shell--left-collapsed': isLeftCollapsed.value,
  'chat-shell--right-collapsed': isRightCollapsed.value
}))

const userInitial = computed(() => {
  const name = userStore.currentUser?.nickname || userStore.currentUser?.username || userStore.currentUser?.email || '你'
  return name?.trim()?.charAt(0)?.toUpperCase() || '你'
})

const userSecondary = computed(() => userStore.currentUser?.username || userStore.currentUser?.email || '')

const currentConversationTitle = computed(() => chatStore.currentConversation?.title?.trim() || '未命名对话')

const messageCountLabel = computed(() => {
  const count = chatStore.messages?.length || 0
  return count ? `${count} 条消息` : '暂无消息'
})

const references = computed(() => (Array.isArray(chatStore.references) ? chatStore.references : []))

const providerOptions = computed(() => {
  const providers = Array.isArray(chatStore.availableModels) ? chatStore.availableModels : []
  return providers.map((provider, index) => {
    const providerValue =
      provider?.name ||
      provider?.displayName ||
      (provider?.id != null ? String(provider.id) : `provider_${index}`)
    const providerLabel = provider?.displayName || provider?.name || `提供者 ${index + 1}`
    const models = Array.isArray(provider?.models) ? provider.models : []
    const options = models.map((model, modelIndex) => {
      const modelValue =
        model?.name ||
        (model?.displayName
          ? `${model.displayName}`
          : model?.id != null
          ? String(model.id)
          : `model_${index}_${modelIndex}`)
      const key = JSON.stringify({ provider: providerValue, model: modelValue })
      const label = model?.displayName || model?.name || `模型 ${modelIndex + 1}`
      const metaParts = []
      if (model?.name && model?.displayName && model.displayName !== model.name) metaParts.push(model.name)
      if (model?.maxTokens) metaParts.push(`上限 ${model.maxTokens}`)
      return {
        key,
        label,
        providerValue,
        providerLabel,
        modelValue,
        disabled: model?.available === false,
        supportsStreaming: model?.supportsStreaming !== false,
        supportsThinking: model?.supportsThinking === true,
        meta: metaParts.join(' · ')
      }
    })
    const hasEnabledOption = options.some((option) => !option.disabled)
    return {
      key: providerValue || `provider_${index}`,
      label: providerLabel,
      providerValue,
      disabled: !hasEnabledOption,
      options
    }
  })
})

const hasAvailableModel = computed(() =>
  providerOptions.value.some((group) => group.options.some((option) => !option.disabled))
)

const selectedModelKey = computed({
  get: () => {
    if (chatStore.selectedProvider && chatStore.selectedModel) {
      return JSON.stringify({ provider: chatStore.selectedProvider, model: chatStore.selectedModel })
    }
    return ''
  },
  set: (value) => {
    if (!value) {
      chatStore.setSelectedProvider('')
      chatStore.setSelectedModel('')
      return
    }
    try {
      const parsed = JSON.parse(value)
      chatStore.setSelectedProvider(parsed?.provider || '')
      chatStore.setSelectedModel(parsed?.model || '')
    } catch (err) {
      console.warn(err)
      chatStore.setSelectedProvider('')
      chatStore.setSelectedModel('')
    }
  }
})

const ensureModelSelection = () => {
  const groups = providerOptions.value
  if (!groups.length || !hasAvailableModel.value) {
    if (chatStore.selectedProvider || chatStore.selectedModel) {
      chatStore.setSelectedProvider('')
      chatStore.setSelectedModel('')
    }
    return
  }
  const matched = groups.some((group) =>
    group.options.some(
      (option) =>
        !option.disabled &&
        option.providerValue === chatStore.selectedProvider &&
        option.modelValue === chatStore.selectedModel
    )
  )
  if (matched) return
  for (const group of groups) {
    if (group.disabled) continue
    for (const option of group.options) {
      if (!option.disabled) {
        chatStore.setSelectedProvider(option.providerValue)
        chatStore.setSelectedModel(option.modelValue)
        return
      }
    }
  }
  chatStore.setSelectedProvider('')
  chatStore.setSelectedModel('')
}

watch(providerOptions, () => {
  ensureModelSelection()
})

const scrollToBottom = async () => {
  await nextTick()
  try {
    if (messageList.value) {
      messageList.value.scrollTop = messageList.value.scrollHeight
    }
  } catch (err) {
    console.warn(err)
  }
}

watch(
  () => chatStore.messages.length,
  () => { scrollToBottom() }
)

watch(searchEnabled, (enabled) => {
  if (!enabled) {
    chatStore.setReferences([])
  }
})

const syncReferencesFromMessages = (messages) => {
  const reversed = [...(messages || [])].reverse()
  const matched = reversed.find((msg) => msg?.role === 'assistant' && Array.isArray(msg?.references) && msg.references.length)
  if (matched) {
    chatStore.setReferences(matched.references)
  } else if (!chatStore.isLoading) {
    chatStore.setReferences([])
  }
}

let es = null
const closeSSE = () => {
  try { es && es.close() } catch (err) { console.warn(err) }
  es = null
}

const loadConversations = async () => {
  if (!userStore.currentUser?.id) return
  try {
    const resp = await conversationApi.getList(userStore.currentUser.id)
    if (resp?.success) {
      chatStore.setConversations(resp.data)
    }
  } catch (err) {
    console.warn(err)
  }
}

const applyMessagesForConversation = async (conversationId) => {
  try {
    const resp = await conversationApi.getMessages(conversationId)
    const messages = resp?.data || []
    chatStore.setMessages(messages)
    syncReferencesFromMessages(messages)
  } catch (err) {
    ElMessage.error('加载对话失败，请稍后重试')
    chatStore.setMessages([])
    chatStore.setReferences([])
  }
  await scrollToBottom()
}

const selectConversation = async (conversation) => {
  if (!conversation?.id) return
  closeSSE()
  chatStore.setCurrentConversation(conversation)
  await applyMessagesForConversation(conversation.id)
}

const getConversationInitial = (conversation) => {
  const text = conversation?.title?.trim() || String(conversation?.id || '')
  return text ? text.charAt(0).toUpperCase() : '#'
}

const createConversation = async () => {
  if (!userStore.currentUser?.id || isCreatingConversation.value) return
  isCreatingConversation.value = true
  try {
    const resp = await conversationApi.create({ userId: userStore.currentUser.id, title: '新对话' })
    if (resp?.success) {
      await loadConversations()
      const created = (chatStore.conversations || [])[0] || resp.data
      await selectConversation(created)
    }
  } catch (err) {
    ElMessage.error('新建对话失败，请稍后再试')
  } finally {
    isCreatingConversation.value = false
  }
}

const handleEnter = (event) => {
  if (event.shiftKey) return
  event.preventDefault()
  send()
}

const extractMessageText = (message) => {
  if (!message) return ''
  const content = message.content
  if (typeof content === 'string') return content
  if (Array.isArray(content)) {
    return content
      .filter((item) => item !== undefined && item !== null)
      .map((item) => String(item))
      .join('\n')
  }
  if (content && typeof content === 'object') return JSON.stringify(content, null, 2)
  return String(content ?? '')
}

const copyMessage = async (message) => {
  const text = extractMessageText(message)
  if (!text.trim()) {
    ElMessage.warning('暂无可复制内容')
    return
  }
  try {
    if (navigator?.clipboard?.writeText) {
      await navigator.clipboard.writeText(text)
    } else {
      const textarea = document.createElement('textarea')
      textarea.value = text
      textarea.style.position = 'fixed'
      textarea.style.opacity = '0'
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
    }
    ElMessage.success('已复制')
  } catch (err) {
    console.warn(err)
    ElMessage.error('复制失败，请手动选择文本')
  }
}

const formatTime = (value) => {
  if (!value) return new Date().toLocaleString()
  try {
    return new Date(value).toLocaleString()
  } catch (err) {
    return value
  }
}

const formatConversationMeta = (conversation) => {
  const ts = conversation?.updatedAt || conversation?.createdAt
  if (!ts) return '刚刚创建'
  try {
    const date = new Date(ts)
    return `${date.toLocaleDateString()} ${date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`
  } catch (err) {
    return '时间未知'
  }
}

const referenceKey = (ref, index) => ref?.id || ref?.url || ref?.link || index

const openReference = (ref) => {
  const url = ref?.url || ref?.link
  if (!url) return
  try {
    if (typeof window !== 'undefined') {
      window.open(url, '_blank', 'noopener')
    }
  } catch (err) {
    console.warn(err)
  }
}

const formatReferenceSource = (ref) => {
  if (ref?.source) return ref.source
  if (ref?.domain) return ref.domain
  if (ref?.provider) return ref.provider
  const url = ref?.url || ref?.link
  if (!url) return '未知来源'
  try {
    return new URL(url).host
  } catch (err) {
    return url
  }
}

const toggleLeft = () => { isLeftCollapsed.value = !isLeftCollapsed.value }
const toggleRight = () => { isRightCollapsed.value = !isRightCollapsed.value }

const loadModels = async () => {
  try {
    const resp = await modelApi.getAllAvailableModels()
    if (resp?.success && Array.isArray(resp.data)) {
      chatStore.setAvailableModels(resp.data)
    } else {
      chatStore.setAvailableModels([])
      chatStore.setSelectedProvider('')
      chatStore.setSelectedModel('')
    }
  } catch (err) {
    console.warn(err)
    chatStore.setAvailableModels([])
    chatStore.setSelectedProvider('')
    chatStore.setSelectedModel('')
  }
  await nextTick()
  ensureModelSelection()
}


const send = async () => {
  if (chatStore.isLoading) {
    ElMessage.warning('请等待当前回复完成')
    return
  }
  if (!chatStore.currentConversation?.id || !input.value.trim()) return
  if (!chatStore.selectedProvider || !chatStore.selectedModel) {
    ElMessage.warning('请先选择模型')
    return
  }
  const content = input.value
  input.value = ''

  chatStore.addMessage({ id: Date.now(), role: 'user', content, createdAt: new Date() })
  chatStore.addMessage({ id: Date.now() + 1, role: 'assistant', content: '', createdAt: new Date(), references: [] })
  await scrollToBottom()

  chatStore.setLoading(true)
  chatStore.setReferences([])
  closeSSE()

  const params = new URLSearchParams({
    message: content,
    searchEnabled: String(searchEnabled.value),
    deepThinking: String(deepThinking.value)
  })
  if (userStore.currentUser?.id) {
    params.set('userId', String(userStore.currentUser.id))
  }
  params.set('provider', chatStore.selectedProvider)
  params.set('model', chatStore.selectedModel)

  es = new EventSource(`/api/chat/stream/${chatStore.currentConversation.id}?${params.toString()}`)

  es.addEventListener('chunk', (event) => {
    try {
      const data = JSON.parse(event.data)
      const chunk = typeof data === 'string' ? data : data?.data ?? data?.content ?? ''
      const last = chatStore.messages[chatStore.messages.length - 1]
      if (last && last.role === 'assistant') {
        last.content = (last.content || '') + chunk
        if (Array.isArray(data?.references)) {
          last.references = data.references
          chatStore.setReferences(data.references)
        } else if (Array.isArray(data?.citations)) {
          last.references = data.citations
          chatStore.setReferences(data.citations)
        }
        chatStore.messages = [...chatStore.messages]
      }
      scrollToBottom()
    } catch (err) {
      console.warn(err)
    }
  })

  es.addEventListener('search_results', (event) => {
    try {
      const data = JSON.parse(event.data)
      if (Array.isArray(data)) {
        chatStore.setReferences(data)
        const last = chatStore.messages[chatStore.messages.length - 1]
        if (last && last.role === 'assistant') {
          last.references = data
          chatStore.messages = [...chatStore.messages]
        }
      }
    } catch (err) {
      console.warn(err)
    }
  })

  es.addEventListener('end', () => {
    chatStore.setLoading(false)
    closeSSE()
    syncReferencesFromMessages(chatStore.messages)
  })

  es.addEventListener('error', () => {
    chatStore.setLoading(false)
    closeSSE()
    ElMessage.error('SSE 连接出错，请重试')
  })
}

onMounted(async () => {
  if (typeof window !== 'undefined') {
    if (window.innerWidth < 1180) {
      isRightCollapsed.value = true
    }
    if (window.innerWidth < 900) {
      isLeftCollapsed.value = true
    }
  }
  userStore.loadUserFromStorage()
  await loadModels()
  if (!userStore.isLoggedIn) return
  await loadConversations()
})

onBeforeUnmount(() => {
  closeSSE()
})
</script>

<style scoped>
.chat-page {
  min-height: 100vh;
  padding: 32px 24px;
  box-sizing: border-box;
  display: flex;
  justify-content: center;
  background: radial-gradient(circle at 15% 20%, #f3f8ff 0%, #eef2ff 40%, #ffffff 100%);
}

.chat-shell {
  width: min(1440px, 100%);
  min-height: calc(100vh - 64px);
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr) 320px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 26px;
  overflow: hidden;
  border: 1px solid rgba(148, 163, 184, 0.18);
  box-shadow: 0 28px 65px rgba(15, 23, 42, 0.14);
  backdrop-filter: blur(24px);
  transition: grid-template-columns 0.25s ease;
}

.chat-shell--left-collapsed {
  grid-template-columns: 82px minmax(0, 1fr) 320px;
}

.chat-shell--right-collapsed {
  grid-template-columns: 320px minmax(0, 1fr) 76px;
}

.chat-shell--left-collapsed.chat-shell--right-collapsed {
  grid-template-columns: 82px minmax(0, 1fr) 76px;
}

.sidebar {
  padding: 28px 22px;
  display: flex;
  flex-direction: column;
  gap: 18px;
  background: linear-gradient(180deg, rgba(244, 250, 255, 0.95), rgba(240, 243, 255, 0.9));
  border-right: 1px solid rgba(148, 163, 184, 0.25);
  transition: padding 0.2s ease;
}

.sidebar--collapsed {
  padding: 28px 14px;
  align-items: center;
}

.sidebar__top {
  display: flex;
  align-items: center;
  gap: 12px;
  justify-content: space-between;
}

.sidebar__collapse {
  color: #64748b;
}

.sidebar__identity {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.sidebar__avatar {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background: linear-gradient(135deg, #409eff, #5a77ff);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  letter-spacing: 0.08em;
}

.sidebar__identity-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
  line-height: 1.2;
}

.sidebar__name {
  font-weight: 600;
  color: #1f2937;
}

.sidebar__meta {
  font-size: 12px;
  color: #6b7280;
}

.sidebar__new {
  width: 100%;
  height: 40px;
  border-radius: 12px;
  font-weight: 600;
  background: linear-gradient(135deg, #3b82f6, #6366f1);
  border: none;
  gap: 6px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.sidebar__new-text {
  color: #fff;
}

.sidebar__list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-right: -6px;
  padding-right: 6px;
}

.sidebar__empty {
  margin-top: 32px;
  padding: 24px;
  border-radius: 16px;
  background: rgba(226, 232, 240, 0.45);
  color: #475569;
  text-align: center;
  font-size: 13px;
  line-height: 1.6;
}

.conversation {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  padding: 14px 16px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.2s ease;
}

.conversation:hover {
  border-color: rgba(99, 102, 241, 0.45);
  box-shadow: 0 10px 24px rgba(59, 130, 246, 0.15);
}

.conversation--active {
  border-color: rgba(99, 102, 241, 0.6);
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1), rgba(56, 189, 248, 0.08));
  box-shadow: 0 12px 26px rgba(99, 102, 241, 0.18);
}

.conversation__badge {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(99, 102, 241, 0.12);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: #4c1d95;
  flex-shrink: 0;
}

.conversation__body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.conversation__title {
  font-weight: 600;
  color: #1e293b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation__meta {
  font-size: 12px;
  color: #64748b;
}

.main {
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 252, 0.9));
}

.main__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28px 32px 18px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
}

.main__header--inactive {
  opacity: 0.6;
}

.main__title-block {
  display: flex;
  align-items: center;
  gap: 16px;
}

.main__collapse-btn {
  color: #64748b;
}

.main__title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.main__meta {
  margin: 6px 0 0;
  font-size: 13px;
  color: #64748b;
}

.main__controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.main__model-select {
  min-width: 200px;
}

.model-option {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.model-option__label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: #1f2937;
}

.model-option__tag {
  padding: 0 6px;
  border-radius: 999px;
  background: rgba(79, 70, 229, 0.12);
  color: #4f46e5;
  font-size: 11px;
  font-weight: 600;
}

.model-option__tag--thinking {
  background: rgba(16, 185, 129, 0.12);
  color: #0f766e;
}

.model-option__meta {
  font-size: 12px;
  color: #64748b;
}

:deep(.main__model-select .el-select__wrapper) {
  border-radius: 12px;
  background: rgba(248, 250, 252, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.25);
}

.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 48px 24px;
  gap: 12px;
  color: #475569;
}

.empty-state h2 {
  margin: 0;
  font-size: 22px;
  color: #1f2937;
}

.messages {
  position: relative;
  flex: 1;
  overflow-y: auto;
  padding: 24px 32px 32px;
  scroll-behavior: smooth;
}

.messages__inner {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.message {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.message--user {
  flex-direction: row-reverse;
}

.message__avatar {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  background: rgba(99, 102, 241, 0.12);
  color: #4c1d95;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  flex-shrink: 0;
}

.message--user .message__avatar {
  background: rgba(59, 130, 246, 0.15);
  color: #1d4ed8;
}

.message__bubble {
  max-width: min(68ch, 100%);
  padding: 18px 20px;
  border-radius: 20px;
  background: #ffffff;
  box-shadow: 0 18px 35px rgba(15, 23, 42, 0.08);
  line-height: 1.65;
  color: #1e293b;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.message--assistant .message__bubble {
  border-bottom-left-radius: 6px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(239, 246, 255, 0.9));
}

.message--user .message__bubble {
  border-bottom-right-radius: 6px;
  background: linear-gradient(135deg, #2563eb, #6366f1);
  color: #f8fafc;
  box-shadow: 0 18px 40px rgba(37, 99, 235, 0.32);
}

.message__bubble-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.message__role {
  font-size: 13px;
  font-weight: 600;
  color: inherit;
}

.message__actions {
  display: flex;
  align-items: center;
}

.message__time {
  font-size: 12px;
  color: #94a3b8;
}

.message--user .message__time {
  color: rgba(248, 250, 252, 0.75);
}

.message__content {
  word-break: break-word;
}

:deep(.message--user .markdown) {
  color: inherit;
}

.typing-indicator {
  position: sticky;
  bottom: 8px;
  display: flex;
  gap: 6px;
  margin-top: 14px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #6366f1;
  opacity: 0.55;
  animation: typing 1.2s ease-in-out infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.12s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.24s;
}

@keyframes typing {
  0%, 80%, 100% {
    transform: translateY(0);
    opacity: 0.35;
  }
  40% {
    transform: translateY(-4px);
    opacity: 1;
  }
}

.main__composer {
  padding: 20px 24px 24px;
  border-top: 1px solid rgba(148, 163, 184, 0.2);
  background: rgba(255, 255, 255, 0.95);
  display: flex;
  flex-direction: column;
  gap: 14px;
}

:deep(.main__input .el-textarea__inner) {
  border-radius: 18px;
  border: 1px solid rgba(148, 163, 184, 0.25);
  background: rgba(248, 250, 252, 0.9);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.6);
  padding: 16px 18px;
  line-height: 1.7;
  font-size: 15px;
  resize: none;
  transition: all 0.2s ease;
}

:deep(.main__input .el-textarea__inner:focus) {
  border-color: rgba(99, 102, 241, 0.65);
  background: #ffffff;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.15);
}

:deep(.main__input .el-textarea__inner:disabled) {
  background: rgba(226, 232, 240, 0.6);
  color: #94a3b8;
  cursor: not-allowed;
}

.main__toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.main__options {
  display: flex;
  gap: 16px;
  align-items: center;
  color: #475569;
  font-size: 14px;
}

:deep(.main__options .el-checkbox__label) {
  color: #475569;
  font-weight: 500;
}

.main__actions {
  display: flex;
  gap: 12px;
}

:deep(.main__actions .el-button) {
  padding: 0 28px;
  height: 42px;
  border-radius: 20px;
  font-weight: 600;
  background: linear-gradient(135deg, #2563eb, #6366f1);
  border: none;
  box-shadow: 0 12px 24px rgba(37, 99, 235, 0.35);
}

.insight {
  padding: 28px 24px;
  display: flex;
  flex-direction: column;
  gap: 18px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(248, 250, 252, 0.92));
  border-left: 1px solid rgba(148, 163, 184, 0.22);
  transition: width 0.2s ease, padding 0.2s ease;
}

.insight--collapsed {
  padding: 28px 10px;
  align-items: center;
}

.insight__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.insight__header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #475569;
  font-weight: 600;
}

.insight__title {
  font-size: 15px;
  color: #1f2937;
}

.insight__collapse {
  color: #64748b;
}

.insight__collapsed-label {
  writing-mode: vertical-rl;
  transform: rotate(180deg);
  letter-spacing: 4px;
  color: #6366f1;
  font-weight: 600;
  margin-top: 32px;
}

.insight__body {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.insight__empty {
  margin-top: 24px;
  padding: 20px;
  border-radius: 16px;
  background: rgba(226, 232, 240, 0.4);
  text-align: center;
  color: #475569;
  font-size: 13px;
  line-height: 1.6;
}

.insight__list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.insight__item {
  padding: 16px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(148, 163, 184, 0.24);
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.08);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.insight__item:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 28px rgba(99, 102, 241, 0.16);
  border-color: rgba(99, 102, 241, 0.35);
}

.insight__item-header {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #4338ca;
  font-weight: 600;
}

.insight__item-title {
  color: #1f2937;
}

.insight__item-meta {
  margin-top: 6px;
  font-size: 12px;
  color: #64748b;
}

.insight__item-snippet {
  margin-top: 10px;
  font-size: 13px;
  color: #475569;
  line-height: 1.6;
}

.msg-fade-enter-active,
.msg-fade-leave-active {
  transition: all 0.35s cubic-bezier(0.16, 1, 0.3, 1);
}

.msg-fade-enter-from,
.msg-fade-leave-to {
  opacity: 0;
  transform: translateY(12px);
}

.sidebar--collapsed .sidebar__identity-details,
.sidebar--collapsed .sidebar__meta,
.sidebar--collapsed .conversation__body,
.sidebar--collapsed .sidebar__new-text {
  display: none;
}

.sidebar--collapsed .sidebar__identity {
  justify-content: center;
}

.sidebar--collapsed .sidebar__avatar {
  margin: 0;
}

.sidebar--collapsed .sidebar__new {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  padding: 0;
}

.sidebar--collapsed .conversation {
  justify-content: center;
  padding: 14px;
}

.insight--collapsed .insight__body,
.insight--collapsed .insight__title,
.insight--collapsed .insight__header-left {
  display: none;
}

@media (max-width: 1360px) {
  .chat-shell {
    width: 100%;
  }

  .main__model-select {
    min-width: 160px;
  }
}

@media (max-width: 1100px) {
  .chat-shell {
    grid-template-columns: 280px minmax(0, 1fr) 280px;
  }

  .chat-shell--left-collapsed {
    grid-template-columns: 76px minmax(0, 1fr) 280px;
  }

  .chat-shell--right-collapsed {
    grid-template-columns: 280px minmax(0, 1fr) 72px;
  }
}

@media (max-width: 920px) {
  .chat-page {
    padding: 16px;
  }

  .chat-shell {
    grid-template-columns: 1fr;
  }

  .chat-shell--left-collapsed,
  .chat-shell--right-collapsed,
  .chat-shell--left-collapsed.chat-shell--right-collapsed {
    grid-template-columns: 1fr;
  }

  .sidebar,
  .insight {
    border: none;
    border-bottom: 1px solid rgba(148, 163, 184, 0.18);
  }

  .insight {
    border-top: 1px solid rgba(148, 163, 184, 0.18);
  }
}

@media (max-width: 640px) {
  .main__header {
    padding: 20px 20px 14px;
    flex-wrap: wrap;
    gap: 12px;
  }

  .main__controls {
    width: 100%;
    justify-content: space-between;
  }

  .messages {
    padding: 20px 18px 24px;
  }

  :deep(.main__actions .el-button) {
    width: 100%;
  }

  .main__toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .main__options {
    justify-content: space-between;
  }
}
</style>
