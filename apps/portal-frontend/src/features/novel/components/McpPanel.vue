<template>
  <div class="mcp-panel">
    <div class="panel-header">
      <h3>MCP 工具</h3>
      <el-button size="small" @click="novelStore.loadMcpTools()">刷新</el-button>
    </div>
    <div class="panel-content">
      <div v-if="novelStore.mcpTools.length === 0" class="placeholder">
        暂无可用工具
      </div>

      <div v-for="(tool, index) in novelStore.mcpTools" :key="index" class="tool-item">
        <div class="tool-name">{{ tool.name }}</div>
        <div class="tool-description">{{ tool.description }}</div>
        <el-button size="small" type="primary" @click="openExecuteDialog(tool)">
          执行
        </el-button>
      </div>
    </div>

    <!-- 执行工具对话框 -->
    <el-dialog v-model="showExecuteDialog" :title="`执行工具: ${selectedTool?.name}`" width="600px">
      <div v-if="selectedTool">
        <p>{{ selectedTool.description }}</p>

        <el-form v-if="toolParameters">
          <div v-for="(param, key) in getToolParameters()" :key="key">
            <el-form-item :label="key">
              <el-input
                v-model="toolParameters[key]"
                :placeholder="param.description || `请输入${key}`"
              />
            </el-form-item>
          </div>
        </el-form>

        <div v-if="executeResult" class="execute-result">
          <h4>执行结果:</h4>
          <pre>{{ JSON.stringify(executeResult, null, 2) }}</pre>
        </div>
      </div>

      <template #footer>
        <el-button @click="showExecuteDialog = false">取消</el-button>
        <el-button type="primary" @click="handleExecute" :loading="executing">
          执行
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useNovelStore } from '../stores/novel'
import { ElMessage } from 'element-plus'

const novelStore = useNovelStore()

const showExecuteDialog = ref(false)
const selectedTool = ref(null)
const toolParameters = ref({})
const executeResult = ref(null)
const executing = ref(false)

const openExecuteDialog = (tool) => {
  selectedTool.value = tool
  toolParameters.value = {}
  executeResult.value = null
  showExecuteDialog.value = true
}

const getToolParameters = () => {
  if (!selectedTool.value?.inputSchema?.properties) return {}
  return selectedTool.value.inputSchema.properties
}

const handleExecute = async () => {
  if (!selectedTool.value) return

  executing.value = true
  try {
    const result = await novelStore.executeMcpTool(
      selectedTool.value.name,
      toolParameters.value
    )
    executeResult.value = result

    if (result.success) {
      ElMessage.success('工具执行成功')
    } else {
      ElMessage.error('工具执行失败: ' + result.error)
    }
  } catch (error) {
    ElMessage.error('执行失败: ' + error.message)
  } finally {
    executing.value = false
  }
}
</script>

<style scoped>
.mcp-panel {
  padding: 16px;
  max-height: 300px;
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

.tool-item {
  margin-bottom: 12px;
  padding: 8px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.tool-name {
  font-weight: bold;
  margin-bottom: 4px;
  color: #303133;
  font-size: 13px;
}

.tool-description {
  color: #606266;
  margin-bottom: 8px;
  font-size: 12px;
}

.execute-result {
  margin-top: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.execute-result pre {
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>