<template>
  <div class="search-results" v-if="results && results.length > 0">
    <div class="search-results-header" @click="toggleExpanded">
      <el-icon class="search-icon">
        <Search />
      </el-icon>
      <span class="search-label">搜索来源 ({{ results.length }})</span>
      <el-icon class="expand-icon" :class="{ expanded: isExpanded }">
        <ArrowDown v-if="isExpanded" />
        <ArrowRight v-else />
      </el-icon>
    </div>
    
    <div v-show="isExpanded" class="search-results-content">
      <div 
        v-for="(result, index) in results" 
        :key="index"
        class="search-result-item"
      >
        <div class="result-header">
          <span class="result-index">{{ index + 1 }}.</span>
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
          <span class="result-url">{{ formatUrl(result.url) }}</span>
        </div>
        
        <div v-if="result.score" class="result-score">
          <el-tag size="small" type="info">
            相关度: {{ Math.round(result.score * 100) }}%
          </el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Search, ArrowDown, ArrowRight, Link } from '@element-plus/icons-vue'

// Props
const props = defineProps({
  results: {
    type: Array,
    default: () => []
  },
  defaultExpanded: {
    type: Boolean,
    default: false
  }
})

// 响应式数据
const isExpanded = ref(props.defaultExpanded)

// 方法
const toggleExpanded = () => {
  isExpanded.value = !isExpanded.value
}

const formatUrl = (url) => {
  if (!url) return ''
  
  try {
    const urlObj = new URL(url)
    return urlObj.hostname
  } catch (e) {
    return url.length > 30 ? url.substring(0, 30) + '...' : url
  }
}

// 计算属性
const hasResults = computed(() => {
  return props.results && props.results.length > 0
})
</script>

<style scoped>
.search-results {
  margin: 12px 0;
  background: #f8faff;
  border: 1px solid #e1e8ff;
  border-radius: 8px;
  font-size: 14px;
}

.search-results-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid #e1e8ff;
  transition: background-color 0.2s;
}

.search-results-header:hover {
  background-color: #f0f5ff;
}

.search-icon {
  color: #1890ff;
  margin-right: 8px;
  font-size: 16px;
}

.search-label {
  flex: 1;
  color: #1890ff;
  font-weight: 500;
}

.expand-icon {
  color: #1890ff;
  transition: transform 0.2s;
  font-size: 14px;
}

.expand-icon.expanded {
  transform: rotate(0deg);
}

.search-results-content {
  padding: 0;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    max-height: 0;
  }
  to {
    opacity: 1;
    max-height: 1000px;
  }
}

.search-result-item {
  padding: 16px;
  border-bottom: 1px solid #e8e8e8;
  background: white;
}

.search-result-item:last-child {
  border-bottom: none;
  border-radius: 0 0 7px 7px;
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
}

.result-title {
  margin: 0;
  color: #2c3e50;
  font-size: 15px;
  font-weight: 600;
  line-height: 1.4;
  word-break: break-word;
}

.result-content {
  margin: 8px 0 12px 20px;
}

.result-summary {
  margin: 0;
  color: #666;
  line-height: 1.6;
  word-break: break-word;
}

.result-footer {
  display: flex;
  align-items: center;
  margin-left: 20px;
  gap: 12px;
}

.result-link {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  text-decoration: none;
}

.result-url {
  color: #999;
  font-size: 12px;
  flex: 1;
}

.result-score {
  margin: 8px 0 0 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-results-header {
    padding: 10px 12px;
  }
  
  .search-result-item {
    padding: 12px;
  }
  
  .result-content,
  .result-footer,
  .result-score {
    margin-left: 16px;
  }
  
  .result-title {
    font-size: 14px;
  }
  
  .result-summary {
    font-size: 13px;
  }
}

/* 深色主题适配 */
@media (prefers-color-scheme: dark) {
  .search-results {
    background: #1a1a1a;
    border-color: #333;
  }
  
  .search-results-header {
    border-bottom-color: #333;
  }
  
  .search-results-header:hover {
    background-color: #2a2a2a;
  }
  
  .search-result-item {
    background: #262626;
    border-bottom-color: #333;
  }
  
  .result-title {
    color: #e8e8e8;
  }
  
  .result-summary {
    color: #b3b3b3;
  }
  
  .result-url {
    color: #888;
  }
}
</style>