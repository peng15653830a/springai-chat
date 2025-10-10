<template>
  <div class="resource-panel">
    <el-tabs v-model="activeTab" stretch class="resource-tabs">
      <!-- 素材库 -->
      <el-tab-pane name="materials">
        <template #label>
          <span class="tab-label">
            <el-icon><Files /></el-icon>
            素材库
          </span>
        </template>

        <div class="tab-content">
          <el-input
            v-model="ragQuery"
            placeholder="搜索相关素材..."
            :prefix-icon="Search"
            @input="handleSearchMaterials"
            clearable
          />

          <div class="material-list">
            <div v-if="searching" class="loading-state">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>搜索中...</span>
            </div>

            <div v-else-if="materials.length === 0" class="empty-state">
              <el-empty description="暂无相关素材" :image-size="80" />
            </div>

            <div
              v-for="m in materials"
              :key="m.id"
              class="material-item"
              @click="handleInsertMaterial(m)"
            >
              <div class="material-header">
                <span class="material-title">{{ m.title }}</span>
                <el-tag size="small" type="info">
                  {{ (m.similarity * 100).toFixed(0) }}%
                </el-tag>
              </div>
              <div class="material-excerpt">{{ m.excerpt }}</div>
              <div class="material-meta">
                <el-icon><Clock /></el-icon>
                <span>{{ formatDate(m.createdAt) }}</span>
              </div>
            </div>
          </div>

          <el-button
            class="upload-btn"
            @click="handleUploadMaterial"
            :icon="Upload"
            type="primary"
            plain
          >
            上传素材
          </el-button>
        </div>
      </el-tab-pane>

      <!-- 参数调优 -->
      <el-tab-pane name="parameters">
        <template #label>
          <span class="tab-label">
            <el-icon><Setting /></el-icon>
            参数
          </span>
        </template>

        <div class="tab-content">
          <div class="param-item">
            <div class="param-label">
              <label>Temperature</label>
              <span class="param-desc">创造性 vs 稳定性</span>
            </div>
            <el-slider
              :model-value="params.temperature"
              @update:model-value="(val) => handleParamChange('temperature', val)"
              :min="0"
              :max="1"
              :step="0.1"
              :marks="{ 0: '保守', 0.5: '适中', 1: '创意' }"
              show-input
              :input-size="'small'"
            />
          </div>

          <el-divider />

          <div class="param-item">
            <div class="param-label">
              <label>Max Tokens</label>
              <span class="param-desc">最大生成长度</span>
            </div>
            <el-slider
              :model-value="params.maxTokens"
              @update:model-value="(val) => handleParamChange('maxTokens', val)"
              :min="512"
              :max="4096"
              :step="128"
              :marks="{ 512: '512', 2048: '2048', 4096: '4096' }"
              show-input
              :input-size="'small'"
            />
          </div>

          <el-divider />

          <div class="param-item">
            <div class="param-label">
              <label>Top P</label>
              <span class="param-desc">采样多样性</span>
            </div>
            <el-slider
              :model-value="params.topP"
              @update:model-value="(val) => handleParamChange('topP', val)"
              :min="0"
              :max="1"
              :step="0.1"
              :marks="{ 0: '0', 0.5: '0.5', 1: '1' }"
              show-input
              :input-size="'small'"
            />
          </div>

          <el-divider />

          <el-button @click="handleResetParams" plain>
            <el-icon><RefreshLeft /></el-icon>
            恢复默认值
          </el-button>
        </div>
      </el-tab-pane>

      <!-- 提示词模板 -->
      <el-tab-pane name="prompts">
        <template #label>
          <span class="tab-label">
            <el-icon><EditPen /></el-icon>
            提示词
          </span>
        </template>

        <div class="tab-content">
          <el-select
            v-model="selectedTemplate"
            placeholder="选择模板"
            @change="handleTemplateChange"
            class="template-select"
          >
            <el-option
              v-for="t in templates"
              :key="t.id"
              :label="t.name"
              :value="t.id"
            >
              <div class="template-option">
                <span>{{ t.name }}</span>
                <el-tag v-if="t.category" size="small" type="info">
                  {{ t.category }}
                </el-tag>
              </div>
            </el-option>
          </el-select>

          <div v-if="selectedTemplateData" class="template-preview">
            <div class="template-header">
              <span class="template-name">{{ selectedTemplateData.name }}</span>
              <el-button
                text
                :icon="DocumentCopy"
                @click="handleCopyTemplate"
              />
            </div>
            <div class="template-content">
              {{ selectedTemplateData.content }}
            </div>
          </div>

          <el-input
            v-model="customPrompt"
            type="textarea"
            :rows="8"
            placeholder="自定义提示词... (可从模板复制后修改)"
            class="custom-prompt"
          />

          <el-button
            @click="handleSaveTemplate"
            :icon="Plus"
            plain
          >
            保存为新模板
          </el-button>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import {
  Files,
  Search,
  Loading,
  Clock,
  Upload,
  Setting,
  RefreshLeft,
  EditPen,
  DocumentCopy,
  Plus
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useNovelWorkspaceStore } from '../../stores/novelWorkspace'

const workspaceStore = useNovelWorkspaceStore()

const activeTab = ref('materials')
const ragQuery = ref('')
const searching = ref(false)
const selectedTemplate = ref('')
const customPrompt = ref('')

const materials = computed(() => workspaceStore.materials || [])
const params = computed(() => workspaceStore.params)

const templates = ref([
  {
    id: 'scene-description',
    name: '场景描写',
    category: '描写',
    content: '详细描写以下场景的视觉、听觉、嗅觉等感官细节，营造氛围...'
  },
  {
    id: 'character-dialogue',
    name: '人物对话',
    category: '对话',
    content: '展现人物性格和矛盾冲突的对话，包含语气、动作、心理...'
  },
  {
    id: 'plot-twist',
    name: '剧情转折',
    category: '情节',
    content: '设计意外但合理的剧情转折点，前有铺垫，后有呼应...'
  },
  {
    id: 'action-scene',
    name: '动作场景',
    category: '动作',
    content: '紧张刺激的动作场景，注重节奏和视觉冲击力...'
  }
])

const selectedTemplateData = computed(() => {
  return templates.value.find(t => t.id === selectedTemplate.value)
})

const handleSearchMaterials = async () => {
  if (!ragQuery.value.trim()) {
    workspaceStore.clearMaterials()
    return
  }

  searching.value = true
  try {
    await workspaceStore.searchMaterials(ragQuery.value)
  } catch (error) {
    ElMessage.error('搜索失败: ' + error.message)
  } finally {
    searching.value = false
  }
}

const handleInsertMaterial = (material) => {
  // TODO: 将素材插入到编辑器
  ElMessage.success(`已引用素材: ${material.title}`)
  workspaceStore.addMaterialReference(material.id)
}

const handleUploadMaterial = () => {
  // TODO: 打开文件上传对话框
  ElMessage.info('上传功能开发中')
}

const handleResetParams = () => {
  workspaceStore.resetParams()
  ElMessage.success('已恢复默认参数')
}

const handleTemplateChange = () => {
  if (selectedTemplateData.value) {
    customPrompt.value = selectedTemplateData.value.content
  }
}

const handleCopyTemplate = async () => {
  if (!selectedTemplateData.value) return

  try {
    await navigator.clipboard.writeText(selectedTemplateData.value.content)
    ElMessage.success('已复制模板')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

const handleSaveTemplate = () => {
  if (!customPrompt.value.trim()) {
    ElMessage.warning('请输入提示词内容')
    return
  }
  // TODO: 保存自定义模板
  ElMessage.info('保存功能开发中')
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

// 参数变化时自动保存（通过v-model直接修改）
const handleParamChange = (key, value) => {
  workspaceStore.updateParams({ [key]: value })
}
</script>

<style scoped>
.resource-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.resource-tabs {
  height: 100%;
}

.resource-tabs :deep(.el-tabs__header) {
  margin: 0;
  border-bottom: 1px solid #e4e7ed;
}

.resource-tabs :deep(.el-tabs__content) {
  height: calc(100% - 40px);
  overflow: hidden;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
}

.tab-content {
  padding: 16px;
  height: 100%;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 素材库样式 */
.material-list {
  flex: 1;
  overflow-y: auto;
  margin: 12px 0;
}

.loading-state,
.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #909399;
  gap: 8px;
}

.material-item {
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.material-item:hover {
  border-color: #409eff;
  background: #ecf5ff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
}

.material-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.material-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.material-excerpt {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.material-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.upload-btn {
  width: 100%;
}

/* 参数调优样式 */
.param-item {
  margin-bottom: 24px;
}

.param-label {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 12px;
}

.param-label label {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.param-desc {
  font-size: 12px;
  color: #909399;
}

.el-slider :deep(.el-slider__marks-text) {
  font-size: 11px;
  color: #909399;
}

/* 提示词模板样式 */
.template-select {
  width: 100%;
}

.template-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.template-preview {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
}

.template-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.template-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.template-content {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
}

.custom-prompt {
  flex: 1;
}
</style>
