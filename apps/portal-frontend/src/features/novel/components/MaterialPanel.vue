<template>
  <div class="material-panel">
    <div class="panel-header">
      <h3>素材管理</h3>
    </div>
    <div class="panel-content">
      <el-space wrap>
        <el-button type="primary" size="small" @click="showImportDialog = true">导入素材</el-button>
        <el-button type="primary" size="small" plain @click="showCrawlDialog = true">网页抓取</el-button>
      </el-space>

      <div class="material-list">
        <div class="list-header">
          <div class="stats">
            <span>已加载文件：{{ materials.totalFiles }}</span>
            <span class="ml16">总分块：{{ materials.totalChunks }}</span>
          </div>
          <el-button size="small" @click="refreshMaterials" :loading="refreshing">刷新</el-button>
        </div>

        <el-table v-if="materials.items && materials.items.length" :data="materials.items" size="small" border style="width: 100%; margin-top: 8px;">
          <el-table-column prop="title" label="标题" min-width="160" />
          <el-table-column prop="chunks" label="分块数" width="90" />
          <el-table-column prop="source" label="来源路径/URL" min-width="320" />
        </el-table>
        <p v-else class="placeholder">尚未加载任何素材</p>
      </div>
    </div>

    <!-- 导入对话框 -->
    <el-dialog v-model="showImportDialog" title="导入素材" width="500px">
      <el-form>
        <el-form-item label="素材路径">
          <el-input v-model="importPath" placeholder="请输入素材文件夹路径" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showImportDialog = false">取消</el-button>
        <el-button type="primary" @click="handleImport">导入</el-button>
      </template>
    </el-dialog
    <!-- 网页抓取对话框 -->
    <el-dialog v-model="showCrawlDialog" title="从网页抓取素材" width="600px">
      <el-form label-width="120px">
        <el-form-item label="入口URL">
          <el-input v-model="crawlForm.url" placeholder="如：https://www.xbookcn.net/某本书的目录页" />
        </el-form-item>
        <el-form-item label="最大页面数">
          <el-input-number v-model="crawlForm.maxPages" :min="1" :max="1000" />
        </el-form-item>
        <el-form-item label="仅同域">
          <el-switch v-model="crawlForm.sameDomainOnly" />
        </el-form-item>
        <el-form-item label="抓取限速(ms)">
          <el-input-number v-model="crawlForm.rateLimitMs" :min="0" :step="100" />
        </el-form-item>
        <el-form-item label="包含正则(可选)">
          <el-input v-model="crawlInclude" placeholder="用逗号分隔，如 .*/\\d+\\.html$,.*/chapter.*" />
        </el-form-item>
        <el-form-item label="正文选择器(可选)">
          <el-input v-model="crawlForm.contentSelector" placeholder="#content,.read-content,#BookText" />
        </el-form-item>
        <el-form-item label="标题选择器(可选)">
          <el-input v-model="crawlForm.titleSelector" placeholder="h1,.title,#bookname" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCrawlDialog = false">取消</el-button>
        <el-button type="primary" :loading="crawling" @click="handleCrawl">开始抓取</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useNovelStore } from '../stores/novel'
import { ElMessage } from 'element-plus'

const novelStore = useNovelStore()
// 通过 computed 映射，确保响应式更新
const materials = computed(() => novelStore.materials)

const showImportDialog = ref(false)
const importPath = ref('')
const refreshing = ref(false)

const showCrawlDialog = ref(false)
const crawling = ref(false)
const crawlForm = ref({
  url: '',
  maxPages: 200,
  sameDomainOnly: true,
  rateLimitMs: 500,
  analyzeStyle: false,
  contentSelector: '',
  titleSelector: ''
})
const crawlInclude = ref('')


const handleImport = async () => {
  if (!importPath.value.trim()) {
    ElMessage.warning('请输入素材路径')
    return
  }

  const result = await novelStore.importMaterials(importPath.value)
  if (result.success) {
    ElMessage.success('素材导入成功')
    showImportDialog.value = false
    importPath.value = ''
  } else {
    ElMessage.error('素材导入失败: ' + result.error)
  }
}

const handleCrawl = async () => {
  if (!crawlForm.value.url.trim()) {
    ElMessage.warning('请输入入口URL')
    return
  }
  crawling.value = true
  try {
    const includePatterns = crawlInclude.value
      ? crawlInclude.value.split(',').map(s => s.trim()).filter(Boolean)
      : []
    const payload = { ...crawlForm.value, includePatterns }
    const res = await novelStore.crawlFromUrl(payload)
    if (res && res.success) {
      ElMessage.success(`抓取完成，页面数：${res.pagesFetched}，分块：${res.totalChunks}`)
      showCrawlDialog.value = false
    } else {
      ElMessage.error('抓取失败：' + (res?.message || res?.error || '未知错误'))
    }
  } catch (e) {
    ElMessage.error('抓取失败：' + (e?.message || '未知错误'))
  } finally {
    crawling.value = false
  }
}

const refreshMaterials = async () => {
  refreshing.value = true
  try { await novelStore.loadMaterials() } catch (e) {} finally { refreshing.value = false }
}

// 组件挂载时确保拉取一次数据（防止父级未调用 initialize 或热更未触发）
onMounted(async () => {
  try { await novelStore.loadMaterials() } catch {}
})
</script>

<style scoped>
.material-panel {
  padding: 16px;
}

.panel-header {
  margin-bottom: 16px;
}

.panel-header h3 {
  margin: 0;
  color: #303133;
  font-size: 16px;
}

.material-list {
  margin-top: 16px;
}

.list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.stats span { color: #606266; font-size: 12px; }
.ml16 { margin-left: 16px; }
.placeholder {
  color: #909399;
  text-align: center;
  margin: 32px 0;
}
.ml8 { margin-left: 8px; }
</style>






