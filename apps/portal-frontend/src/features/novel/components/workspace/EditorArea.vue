<template>
  <div class="editor-area">
    <!-- 故事设定编辑 -->
    <div v-if="editingSettings" class="settings-editor">
      <div class="settings-header">
        <h2 class="settings-title">
          <el-icon><Setting /></el-icon>
          {{ getSettingsTitle(editingSettings) }}
        </h2>
      </div>

      <div class="settings-content">
        <el-input
          v-model="settingsContent[editingSettings]"
          type="textarea"
          :rows="20"
          :placeholder="getSettingsPlaceholder(editingSettings)"
          @change="handleSettingsChange"
        />
      </div>

      <div class="settings-actions">
        <el-button type="primary" @click="handleSaveSettings">
          <el-icon><DocumentChecked /></el-icon>
          保存设定
        </el-button>
      </div>
    </div>

    <!-- 段落信息头 -->
    <div v-else-if="currentSegment" class="segment-header">
      <div class="title-row">
        <h2 class="segment-title">
          #{{ currentSegment.index }} {{ currentSegment.title || '无标题' }}
        </h2>
        <el-tag :type="getStatusType(currentSegment.status)" effect="plain">
          {{ getStatusText(currentSegment.status) }}
        </el-tag>
      </div>

      <div class="meta-row">
        <span class="meta-item">
          <el-icon><Document /></el-icon>
          版本 v{{ currentSegment.version || 0 }}
        </span>
        <span class="divider">|</span>
        <span class="meta-item">
          <el-icon><Reading /></el-icon>
          字数: {{ wordCount }}
        </span>
        <span class="divider">|</span>
        <span
          class="meta-item"
          :class="{ 'text-warning': wordCount < 800, 'text-success': wordCount >= 800 && wordCount <= 1200 }"
        >
          <el-icon><Warning /></el-icon>
          目标: 800-1200字
        </span>
      </div>

      <div v-if="currentSegment.prompt" class="prompt-row">
        <el-icon><Guide /></el-icon>
        <span>{{ currentSegment.prompt }}</span>
      </div>

      <div v-if="currentSegment.starter" class="starter-row">
        <el-icon><EditPen /></el-icon>
        <span>开头: {{ currentSegment.starter }}</span>
      </div>
    </div>

    <!-- 编辑区域 (仅在段落模式下显示) -->
    <div v-if="currentSegment" class="editor-content" ref="editorRef">
      <div v-if="generating" class="generating-indicator">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>正在生成中... {{ wordCount }}字</span>
        <el-progress
          :percentage="progressPercentage"
          :show-text="false"
          :stroke-width="3"
        />
      </div>

      <div class="markdown-preview" v-html="renderedContent"></div>
    </div>

    <!-- 空状态 (既不是段落也不是设定时) -->
    <div v-else-if="!editingSettings" class="editor-content">
      <div class="empty-state">
        <el-empty description="请从左侧选择段落开始创作">
          <el-button type="primary" @click="handleInitialize">
            初始化项目
          </el-button>
        </el-empty>
      </div>
    </div>

    <!-- 操作按钮区 -->
    <div v-if="currentSegment" class="action-bar">
      <div class="primary-actions">
        <el-button
          type="primary"
          @click="handleGenerate"
          :loading="generating"
          :icon="MagicStick"
        >
          {{ currentSegment.latestText ? '重新生成' : '生成' }}
        </el-button>
        <el-button
          @click="handleApprove"
          :disabled="generating || !currentSegment.latestText"
          :icon="Check"
        >
          通过并下一段
        </el-button>
        <el-button
          @click="handleCopy"
          :disabled="!currentSegment.latestText"
          :icon="DocumentCopy"
        >
          复制
        </el-button>
      </div>

      <el-divider />

      <div class="revision-section">
        <el-input
          v-model="feedback"
          type="textarea"
          :rows="2"
          placeholder="不满意当前内容？输入修改意见重新生成..."
          :disabled="generating"
          class="feedback-input"
        />
        <el-button
          @click="handleRevise"
          :loading="generating"
          :disabled="!feedback.trim()"
          :icon="RefreshRight"
        >
          按意见重写
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import {
  Document,
  Reading,
  Warning,
  Guide,
  EditPen,
  Loading,
  MagicStick,
  Check,
  DocumentCopy,
  RefreshRight,
  Setting,
  DocumentChecked
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'
import { useNovelWorkspaceStore } from '../../stores/novelWorkspace'

const workspaceStore = useNovelWorkspaceStore()
const editorRef = ref(null)
const feedback = ref('')
const streamedContent = ref('')

const editingSettings = computed(() => workspaceStore.editingSettings)
const settingsContent = computed({
  get: () => workspaceStore.project.settings,
  set: (val) => workspaceStore.updateSettings(val)
})

const currentSegment = computed(() => workspaceStore.currentSegment)
const generating = computed(() => workspaceStore.generating)

const wordCount = computed(() => {
  const text = streamedContent.value || currentSegment.value?.latestText || ''
  return text.replace(/\s/g, '').length
})

const progressPercentage = computed(() => {
  if (!generating.value) return 0
  const target = 1000
  return Math.min((wordCount.value / target) * 100, 100)
})

const renderedContent = computed(() => {
  const text = streamedContent.value || currentSegment.value?.latestText || ''
  if (!text) return '<p class="placeholder-text">等待生成内容...</p>'

  try {
    return marked.parse(text, {
      breaks: true,
      gfm: true
    })
  } catch (error) {
    console.error('Markdown parsing error:', error)
    return `<pre>${text}</pre>`
  }
})

const getStatusType = (status) => {
  const map = {
    approved: 'success',
    draft: 'info',
    needs_revision: 'warning'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    approved: '已定稿',
    draft: '草稿',
    needs_revision: '待修订'
  }
  return map[status] || '未知'
}

const getSettingsTitle = (settingId) => {
  const map = {
    background: '世界观背景',
    characters: '角色设定',
    style: '写作风格'
  }
  return map[settingId] || '故事设定'
}

const getSettingsPlaceholder = (settingId) => {
  const map = {
    background: '描述故事发生的时代、地点、社会背景...\n例如：\n- 时代：2077年，AI普及的近未来\n- 地点：新东京超级都市\n- 社会背景：人类与AI共存的世界',
    characters: '描述主要角色的性格、背景、关系...\n例如：\n- 主角：零号AI，第一个拥有自我意识的AI\n- 配角：创造者李博士，AI伦理研究员',
    style: '定义写作风格、语气、叙事视角...\n例如：\n- 第一人称视角\n- 赛博朋克风格\n- 节奏紧凑，注重心理描写'
  }
  return map[settingId] || '请输入设定内容...'
}

const handleSettingsChange = () => {
  workspaceStore.saveProject()
}

const handleSaveSettings = () => {
  workspaceStore.saveProject()
  ElMessage.success('设定已保存')
}

const handleInitialize = async () => {
  try {
    await workspaceStore.initProject({
      title: workspaceStore.project.title || '新项目',
      background: '',
      style: ''
    })
    ElMessage.success('项目已初始化')
  } catch (error) {
    ElMessage.error('初始化失败: ' + error.message)
  }
}

const handleGenerate = async () => {
  if (!currentSegment.value) return

  streamedContent.value = ''
  try {
    await workspaceStore.generateSegment(
      currentSegment.value.index,
      (chunk) => {
        streamedContent.value += chunk
        scrollToBottom()
      }
    )
    ElMessage.success('生成完成')
  } catch (error) {
    ElMessage.error('生成失败: ' + error.message)
  } finally {
    streamedContent.value = ''
  }
}

const handleRevise = async () => {
  if (!currentSegment.value || !feedback.value.trim()) return

  streamedContent.value = ''
  try {
    await workspaceStore.reviseSegment(
      currentSegment.value.index,
      feedback.value,
      (chunk) => {
        streamedContent.value += chunk
        scrollToBottom()
      }
    )
    feedback.value = ''
    ElMessage.success('重写完成')
  } catch (error) {
    ElMessage.error('重写失败: ' + error.message)
  } finally {
    streamedContent.value = ''
  }
}

const handleApprove = async () => {
  if (!currentSegment.value) return

  try {
    await workspaceStore.approveSegment(currentSegment.value.index)
    ElMessage.success('已通过，自动切换到下一段')
  } catch (error) {
    ElMessage.error('操作失败: ' + error.message)
  }
}

const handleCopy = async () => {
  const text = currentSegment.value?.latestText || ''
  if (!text) return

  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (editorRef.value) {
      editorRef.value.scrollTop = editorRef.value.scrollHeight
    }
  })
}

// 监听当前段落变化，清空流式内容
watch(currentSegment, () => {
  streamedContent.value = ''
  feedback.value = ''
})
</script>

<style scoped>
.editor-area {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.segment-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e4e7ed;
  background: #fafafa;
  flex-shrink: 0;
}

.title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.segment-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #606266;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.divider {
  color: #dcdfe6;
}

.text-warning {
  color: #e6a23c;
  font-weight: 500;
}

.text-success {
  color: #67c23a;
  font-weight: 500;
}

.prompt-row,
.starter-row {
  margin-top: 8px;
  padding: 10px 12px;
  background: white;
  border-radius: 4px;
  font-size: 13px;
  display: flex;
  align-items: flex-start;
  gap: 8px;
  border-left: 3px solid #409eff;
}

.starter-row {
  border-left-color: #67c23a;
}

.editor-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: white;
}

.empty-state {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.generating-indicator {
  position: sticky;
  top: 0;
  background: linear-gradient(135deg, #ecf5ff 0%, #e6f7ff 100%);
  padding: 12px 16px;
  border-radius: 6px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
  z-index: 10;
}

.generating-indicator .el-progress {
  flex: 1;
}

.markdown-preview {
  line-height: 1.8;
  font-size: 15px;
  color: #303133;
}

.markdown-preview :deep(p) {
  margin: 1em 0;
}

.markdown-preview :deep(h1),
.markdown-preview :deep(h2),
.markdown-preview :deep(h3) {
  margin-top: 1.5em;
  margin-bottom: 0.8em;
  font-weight: 600;
}

.markdown-preview :deep(code) {
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Consolas', 'Monaco', monospace;
}

.markdown-preview :deep(pre) {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  overflow-x: auto;
}

.placeholder-text {
  color: #909399;
  text-align: center;
  padding: 40px 0;
}

.action-bar {
  border-top: 1px solid #e4e7ed;
  padding: 16px 20px;
  background: #fafafa;
  flex-shrink: 0;
}

.primary-actions {
  display: flex;
  gap: 12px;
}

.el-divider {
  margin: 12px 0;
}

.revision-section {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.feedback-input {
  flex: 1;
}

/* 故事设定编辑器样式 */
.settings-editor {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.settings-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e4e7ed;
  background: #fafafa;
  flex-shrink: 0;
}

.settings-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.settings-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: white;
}

.settings-content .el-textarea {
  height: 100%;
}

.settings-content :deep(.el-textarea__inner) {
  height: 100%;
  font-size: 14px;
  line-height: 1.8;
  resize: none;
}

.settings-actions {
  border-top: 1px solid #e4e7ed;
  padding: 16px 20px;
  background: #fafafa;
  flex-shrink: 0;
}
</style>
