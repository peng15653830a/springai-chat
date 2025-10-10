<template>
  <div class="novel-container">
    <div class="novel-layout">
      <!-- 左侧面板：素材管理 -->
      <div class="left-panel">
        <MaterialPanel />
      </div>

      <!-- 中间面板：对话区 -->
      <div class="center-panel">
        <ChatPanel />
      </div>

      <!-- 右侧面板：模型参数、RAG引用、MCP工具 -->
      <div class="right-panel">
        <ParameterPanel />
        <RagPanel />
        <McpPanel />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useNovelStore } from '../stores/novel'
import MaterialPanel from '../components/MaterialPanel.vue'
import ChatPanel from '../components/ChatPanel.vue'
import ParameterPanel from '../components/ParameterPanel.vue'
import RagPanel from '../components/RagPanel.vue'
import McpPanel from '../components/McpPanel.vue'

const novelStore = useNovelStore()

onMounted(() => {
  novelStore.initializeNovel()
})
</script>

<style scoped>
.novel-container {
  height: 100vh;
  background: #f5f7fa;
}

.novel-layout {
  display: flex;
  height: 100%;
  gap: 16px;
  padding: 16px;
}

.left-panel {
  flex: 0 0 280px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow-y: auto;
}

.center-panel {
  flex: 1;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

.right-panel {
  flex: 0 0 320px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.right-panel > * {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}
</style>