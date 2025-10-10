<template>
  <div class="chat-panel">
    <div class="chat-header">
      <h3>小说创作对话</h3>
      <el-button size="small" @click="novelStore.clearChat()">清空</el-button>
    </div>

    <div class="chat-content" ref="chatContainer">
      <div v-if="novelStore.chatHistory.length === 0" class="welcome">
        <p>欢迎使用Novel创作助手！</p>
        <p>请在下方输入您的创作想法开始写作。</p>
      </div>

      <div v-for="(message, index) in novelStore.chatHistory" :key="index" class="message-item">
        <div :class="['message', message.type]">
          <div class="message-content">{{ message.content }}</div>
          <div class="message-time">{{ formatTime(message.timestamp) }}</div>
        </div>
      </div>

      <div v-if="novelStore.currentResponse" class="message-item">
        <div class="message assistant generating">
          <div class="message-content">{{ novelStore.currentResponse }}</div>
          <div class="message-time">生成中...</div>
        </div>
      </div>
    </div>

    <div class="chat-input">
      <el-input
        v-model="inputText"
        type="textarea"
        :rows="3"
        placeholder="请输入您的创作提示词..."
        @keydown.ctrl.enter="handleSend"
        :disabled="novelStore.isGenerating"
      />
      <div class="input-actions">
        <el-button
          type="primary"
          @click="handleSend"
          :loading="novelStore.isGenerating"
          :disabled="!inputText.trim()"
        >
          {{ novelStore.isGenerating ? '生成中...' : '发送' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import { useNovelStore } from '../stores/novel'

const novelStore = useNovelStore()

const inputText = ref('')
const chatContainer = ref(null)

const handleSend = async () => {
  if (!inputText.value.trim() || novelStore.isGenerating) return

  const prompt = inputText.value.trim()
  inputText.value = ''

  await novelStore.generateText(prompt)
  await nextTick()
  scrollToBottom()
}

const scrollToBottom = () => {
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight
  }
}

const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleTimeString()
}

// 监听响应更新，自动滚动
watch(() => novelStore.currentResponse, () => {
  nextTick(() => scrollToBottom())
})
</script>

<style scoped>
.chat-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.chat-header h3 {
  margin: 0;
  color: #303133;
  font-size: 16px;
}

.chat-content {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.welcome {
  text-align: center;
  color: #909399;
  margin: 64px 0;
}

.message-item {
  margin-bottom: 16px;
}

.message {
  max-width: 80%;
  padding: 12px;
  border-radius: 8px;
  position: relative;
}

.message.user {
  background: #409eff;
  color: white;
  margin-left: auto;
}

.message.assistant {
  background: #f5f7fa;
  color: #303133;
}

.message.generating {
  border-left: 3px solid #409eff;
}

.message-content {
  white-space: pre-wrap;
  word-break: break-word;
}

.message-time {
  font-size: 12px;
  opacity: 0.7;
  margin-top: 4px;
}

.chat-input {
  padding: 16px;
  border-top: 1px solid #e4e7ed;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}
</style>