<template>
  <div class="top-bar">
    <div class="left-section">
      <el-icon class="project-icon"><Notebook /></el-icon>
      <el-input
        v-model="workspaceStore.project.title"
        placeholder="未命名项目"
        class="title-input"
        @change="handleTitleChange"
      />
    </div>

    <div class="center-section">
      <el-select
        v-model="workspaceStore.project.model"
        placeholder="自动使用配置文件中的默认模型"
        class="model-select"
        @change="handleModelChange"
        clearable
        :loading="loadingModels"
      >
        <el-option
          v-for="model in availableModels"
          :key="model.name"
          :label="model.name"
          :value="model.name"
        >
          <div class="model-option">
            <span class="model-name">{{ model.name }}</span>
          </div>
        </el-option>
      </el-select>
    </div>

    <div class="right-section">
      <el-button
        @click="handleSave"
        :loading="saving"
        :icon="DocumentCopy"
      >
        保存
      </el-button>
      <el-button
        @click="handleExport"
        :icon="Download"
      >
        导出
      </el-button>
      <el-button
        circle
        @click="handleSettings"
        :icon="Setting"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Notebook, DocumentCopy, Download, Setting } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useNovelWorkspaceStore } from '../../stores/novelWorkspace'

const workspaceStore = useNovelWorkspaceStore()
const saving = ref(false)
const availableModels = ref([])
const loadingModels = ref(false)

// 从Ollama API获取可用模型列表
const loadAvailableModels = async () => {
  loadingModels.value = true
  try {
    const response = await fetch('/api/novel/models')
    if (response.ok) {
      const data = await response.json()
      availableModels.value = data.models || []
    }
  } catch (error) {
    console.error('Failed to load models:', error)
    ElMessage.warning('无法加载模型列表，请检查Ollama服务')
  } finally {
    loadingModels.value = false
  }
}

// 组件加载时获取模型列表
onMounted(() => {
  loadAvailableModels()
})

const handleTitleChange = () => {
  workspaceStore.saveProject()
}

const handleModelChange = () => {
  if (workspaceStore.project.model) {
    ElMessage.success(`已切换到模型: ${workspaceStore.project.model}`)
  } else {
    ElMessage.info('将使用配置文件中的默认模型')
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    await workspaceStore.saveProject()
    ElMessage.success('项目已保存')
  } catch (error) {
    ElMessage.error('保存失败: ' + error.message)
  } finally {
    saving.value = false
  }
}

const handleExport = async () => {
  try {
    await workspaceStore.exportProject()
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败: ' + error.message)
  }
}

const handleSettings = () => {
  // TODO: 打开设置对话框
  ElMessage.info('设置功能开发中')
}
</script>

<style scoped>
.top-bar {
  height: 56px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: white;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.left-section {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 0 0 auto;
}

.project-icon {
  font-size: 20px;
  color: #409eff;
}

.title-input {
  width: 200px;
}

.title-input :deep(.el-input__wrapper) {
  box-shadow: none;
  border: 1px solid transparent;
  transition: border-color 0.2s;
}

.title-input :deep(.el-input__wrapper:hover) {
  border-color: #c0c4cc;
}

.title-input :deep(.el-input__wrapper.is-focus) {
  border-color: #409eff;
}

.center-section {
  flex: 1;
  display: flex;
  justify-content: center;
}

.model-select {
  width: 280px;
}

.model-option {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.model-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.model-desc {
  font-size: 12px;
  color: #909399;
}

.right-section {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 0 0 auto;
}
</style>
