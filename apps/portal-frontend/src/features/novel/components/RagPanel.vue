<template>
  <div class="rag-panel">
    <div class="panel-header">
      <h3>RAG 引用</h3>
      <el-button size="small" @click="showSearchDialog = true">搜索</el-button>
    </div>
    <div class="panel-content">
      <div v-if="novelStore.ragResults.length === 0" class="placeholder">
        暂无引用内容
      </div>

      <div v-for="(result, index) in novelStore.ragResults" :key="index" class="result-item">
        <div class="result-title">{{ result.title }}</div>
        <div class="result-excerpt">{{ result.excerpt }}</div>
        <div class="result-meta">
          <span class="similarity">相似度: {{ (result.similarity * 100).toFixed(1) }}%</span>
          <el-button size="small" type="text" @click="copyContent(result.content)">复制</el-button>
        </div>
      </div>
    </div>

    <!-- 搜索对话框 -->
    <el-dialog v-model="showSearchDialog" title="搜索素材" width="500px">
      <el-form>
        <el-form-item label="搜索关键词">
          <el-input v-model="searchQuery" placeholder="请输入搜索关键词" />
        </el-form-item>
        <el-form-item label="返回数量">
          <el-input-number v-model="searchTopK" :min="1" :max="20" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSearchDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useNovelStore } from '../stores/novel'
import { ElMessage } from 'element-plus'

const novelStore = useNovelStore()

const showSearchDialog = ref(false)
const searchQuery = ref('')
const searchTopK = ref(5)

const handleSearch = async () => {
  if (!searchQuery.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  await novelStore.searchMaterials(searchQuery.value, searchTopK.value)
  showSearchDialog.value = false
  searchQuery.value = ''
}

const copyContent = async (content) => {
  try {
    await navigator.clipboard.writeText(content)
    ElMessage.success('内容已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}
</script>

<style scoped>
.rag-panel {
  padding: 16px;
  max-height: 400px;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.panel-header h3 {
  margin: 0;
  color: #303133;
  font-size: 14px;
}

.panel-content {
  flex: 1;
  overflow-y: auto;
}

.placeholder {
  color: #909399;
  text-align: center;
  margin: 32px 0;
  font-size: 13px;
}

.result-item {
  margin-bottom: 12px;
  padding: 8px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  font-size: 12px;
}

.result-title {
  font-weight: bold;
  margin-bottom: 4px;
  color: #303133;
}

.result-excerpt {
  color: #606266;
  margin-bottom: 4px;
  line-height: 1.4;
}

.result-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 11px;
}

.similarity {
  color: #909399;
}
</style>