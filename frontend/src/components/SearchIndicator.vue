<template>
  <div class="search-indicator" @click="handleClick">
    <div class="search-info">
      <el-icon class="search-icon">
        <Search />
      </el-icon>
      <span class="search-text">已使用联网搜索</span>
      <el-tag size="small" type="success" class="result-count">
        {{ resultCount }}个来源
      </el-tag>
    </div>
    <div class="search-action">
      <el-link type="success" :underline="false" class="view-details">
        查看详情
        <el-icon class="arrow-icon">
          <ArrowRight />
        </el-icon>
      </el-link>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Search, ArrowRight } from '@element-plus/icons-vue'

// Props
const props = defineProps({
  results: {
    type: Array,
    default: () => []
  },
  messageId: {
    type: Number,
    required: true
  }
})

// Emits
const emit = defineEmits(['click'])

// 计算属性
const resultCount = computed(() => {
  return props.results?.length || 0
})

// 方法
const handleClick = () => {
  emit('click', {
    messageId: props.messageId,
    results: props.results
  })
}
</script>

<style scoped>
.search-indicator {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  margin: 8px 0;
  background: linear-gradient(135deg, #f0f9f4 0%, #f6ffed 100%);
  border: 1px solid #b7eb8f;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.search-indicator:hover {
  background: linear-gradient(135deg, #ecf5ff 0%, #f0f9f4 100%);
  border-color: #73d13d;
  box-shadow: 0 2px 4px rgba(82, 196, 26, 0.1);
}

.search-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-icon {
  color: #52c41a;
  font-size: 16px;
}

.search-text {
  color: #52c41a;
  font-size: 14px;
  font-weight: 500;
}

.result-count {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
}

.search-action {
  display: flex;
  align-items: center;
}

.view-details {
  font-size: 13px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
  color: #52c41a !important;
}

.arrow-icon {
  font-size: 12px;
  transition: transform 0.2s ease;
}

.search-indicator:hover .arrow-icon {
  transform: translateX(2px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-indicator {
    padding: 8px 12px;
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }
  
  .search-info {
    gap: 6px;
  }
  
  .search-text {
    font-size: 13px;
  }
  
  .view-details {
    font-size: 12px;
  }
}

/* 深色主题适配 */
@media (prefers-color-scheme: dark) {
  .search-indicator {
    background: linear-gradient(135deg, rgba(82, 196, 26, 0.1) 0%, rgba(82, 196, 26, 0.05) 100%);
    border-color: rgba(82, 196, 26, 0.3);
  }
  
  .search-indicator:hover {
    background: linear-gradient(135deg, rgba(82, 196, 26, 0.15) 0%, rgba(82, 196, 26, 0.08) 100%);
    border-color: rgba(82, 196, 26, 0.5);
    box-shadow: 0 2px 4px rgba(82, 196, 26, 0.2);
  }
}
</style>