<template>
  <div class="right-panel" :class="{ collapsed: props.collapsed }">
    <div class="panel-header">
      <h3 class="panel-title">搜索详情</h3>
      <el-button
        @click="togglePanel"
        :icon="props.collapsed ? ArrowLeft : ArrowRight"
        size="small"
        text
        class="collapse-btn"
      />
    </div>
    
    <div v-show="!props.collapsed" class="panel-content">
      <!-- 搜索结果详情 -->
      <div v-if="internalResults && internalResults.length > 0" class="search-detail-section">
        <div class="search-info">
          <el-icon class="search-icon"><Search /></el-icon>
          <span class="search-count">找到 {{ internalResults.length }} 个相关来源</span>
        </div>
        
        <div class="search-results-list" :key="renderKey">
          <div 
            v-for="(result, index) in internalResults" 
            :key="computeKey(result, index)"
            class="search-result-card"
          >
            <div class="result-header">
              <span class="result-index">{{ index + 1 }}</span>
              <h4 class="result-title">{{ result.title || '无标题' }}</h4>
            </div>
            
            <div class="result-content" v-if="result.content">
              <p class="result-summary">{{ result.content }}</p>
            </div>
            
            <div class="result-footer" v-if="result.url">
              <el-link 
                :href="result.url" 
                target="_blank" 
                type="primary" 
                class="result-link"
              >
                <el-icon><Link /></el-icon>
                查看原文
              </el-link>
              <span class="result-domain">{{ formatDomain(result.url) }}</span>
            </div>
            
            <div v-if="result.score" class="result-score">
              <el-tag size="small" :type="getScoreType(result.score)">
                相关度: {{ Math.round(result.score * 100) }}%
              </el-tag>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 无搜索结果时的占位内容 -->
      <div v-else class="no-search-placeholder">
        <el-empty 
          :image-size="120" 
          description="当前对话暂无搜索结果"
        >
          <template #description>
            <p class="empty-description">
              AI回复包含联网搜索时，<br>
              搜索来源将在此处显示
            </p>
          </template>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ArrowLeft, ArrowRight, Search, Link } from '@element-plus/icons-vue'

// Props
const props = defineProps({
  searchResults: {
    type: Array,
    default: () => []
  },
  currentMessageId: {
    type: Number,
    default: null
  },
  collapsed: {
    type: Boolean,
    default: true
  }
})

// Emits
const emit = defineEmits(['toggle'])

// 内部结果列表，避免直接依赖外部引用，确保变更触发渲染
const internalResults = ref([])
const renderKey = ref(0)

// 方法
const togglePanel = () => {
  emit('toggle')
}

const formatDomain = (url) => {
  if (!url) return ''
  
  try {
    const urlObj = new URL(url)
    return urlObj.hostname
  } catch (e) {
    return url.length > 30 ? url.substring(0, 30) + '...' : url
  }
}

const getScoreType = (score) => {
  if (score >= 0.8) return 'success'
  if (score >= 0.6) return 'warning'
  return 'info'
}

// 监听搜索结果变化，仅在从无到有时自动展开面板
watch(() => props.searchResults, (newResults, oldResults) => {
  // 只有当之前没有结果，现在有结果时才自动展开（表示新的搜索）
  // 避免在切换历史对话时自动展开
  if (newResults && newResults.length > 0 && 
      (!oldResults || oldResults.length === 0)) {
    emit('toggle') // 通知父组件展开
  }
  // 同步内部列表，使用拷贝确保渲染更新
  if (Array.isArray(newResults)) {
    internalResults.value = newResults.map(r => ({ ...r }))
  } else {
    internalResults.value = []
  }
  // bump 渲染key，强制列表重建，避免索引key导致的复用问题
  renderKey.value += 1
}, { deep: true, immediate: true })

// 暴露展开方法供父组件调用
const expand = () => {
  emit('toggle') // 通知父组件展开
}

// 定义暴露的方法
defineExpose({
  expand
})

// 工具：为结果项生成稳定且随变更更新的key
const computeKey = (result, index) => {
  const u = result?.url || ''
  const t = result?.title || ''
  const c = (result?.content || '').slice(0, 20)
  return `${renderKey.value}-${u}-${t}-${index}-${c.length}`
}
</script>

<style scoped>
.right-panel {
  width: 300px;
  background: #ffffff;
  border-left: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
  height: 100vh;
  transition: width 0.3s ease;
}

.right-panel.collapsed {
  width: 60px;
}

.right-panel.collapsed .panel-title {
  display: none;
}

.right-panel.collapsed .panel-header {
  justify-content: center;
  padding: 16px 8px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #e0e0e0;
  background: white;
}

.panel-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.collapse-btn {
  padding: 4px;
  color: #666;
}

.panel-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #ffffff;
}

.search-detail-section {
  height: 100%;
}

.search-info {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #e8f4fd;
  border-radius: 8px;
  border-left: 4px solid #1890ff;
}

.search-icon {
  color: #1890ff;
  margin-right: 8px;
  font-size: 16px;
}

.search-count {
  color: #1890ff;
  font-weight: 500;
  font-size: 14px;
}

.search-results-list {
  space-y: 12px;
}

.search-result-card {
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  transition: all 0.2s ease;
}

.search-result-card:hover {
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.1);
}

.result-header {
  display: flex;
  align-items: flex-start;
  margin-bottom: 8px;
}

.result-index {
  color: #1890ff;
  font-weight: bold;
  margin-right: 8px;
  margin-top: 2px;
  flex-shrink: 0;
  width: 20px;
}

.result-title {
  margin: 0;
  color: #2c3e50;
  font-size: 14px;
  font-weight: 600;
  line-height: 1.4;
  word-break: break-word;
  flex: 1;
}

.result-content {
  margin: 8px 0 12px 28px;
}

.result-summary {
  margin: 0;
  color: #666;
  line-height: 1.5;
  word-break: break-word;
  font-size: 13px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.result-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 8px 0 0 28px;
  gap: 8px;
}

.result-link {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  text-decoration: none;
}

.result-domain {
  color: #999;
  font-size: 11px;
  flex: 1;
  text-align: right;
}

.result-score {
  margin: 8px 0 0 28px;
}

.no-search-placeholder {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-description {
  color: #999;
  font-size: 14px;
  line-height: 1.6;
  text-align: center;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .right-panel {
    width: 280px;
  }
}

/* 深色主题适配 */
@media (prefers-color-scheme: dark) {
  .right-panel {
    background: #1f1f1f;
    border-left-color: #333;
  }
  
  .panel-content {
    background: #1f1f1f;
  }
  
  .panel-header {
    background: #2a2a2a;
    border-bottom-color: #333;
  }
  
  .panel-title {
    color: #e8e8e8;
  }
  
  .search-info {
    background: rgba(24, 144, 255, 0.1);
    border-left-color: #1890ff;
  }
  
  .search-result-card {
    background: #262626;
    border-color: #333;
  }
  
  .search-result-card:hover {
    border-color: #1890ff;
    box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
  }
  
  .result-title {
    color: #e8e8e8;
  }
  
  .result-summary {
    color: #b3b3b3;
  }
  
  .result-domain {
    color: #888;
  }
  
  .empty-description {
    color: #888;
  }
}
</style>
