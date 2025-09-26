<template>
  <div class="mcp-page">
    <h2>MCP 工具</h2>
    <div class="ops">
      <el-button @click="checkHealth" :loading="loading.health">健康检查</el-button>
      <el-button @click="listTools" :loading="loading.tools">列出工具</el-button>
    </div>
    <el-card v-if="output" class="result"><pre>{{ output }}</pre></el-card>
    <el-empty v-else description="点击上方按钮调用 MCP 后端" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import mcp from '../api/client'

const output = ref('')
const loading = ref({ health: false, tools: false })

const checkHealth = async () => {
  loading.value.health = true
  output.value = ''
  try {
    const resp = await mcp.get('/api/health')
    output.value = typeof resp === 'string' ? resp : JSON.stringify(resp, null, 2)
  } catch (e) {
    ElMessage.error('调用失败')
  } finally {
    loading.value.health = false
  }
}

const listTools = async () => {
  loading.value.tools = true
  try {
    const resp = await mcp.get('/api/tools')
    output.value = typeof resp === 'string' ? resp : JSON.stringify(resp, null, 2)
  } catch (e) {
    ElMessage.error('调用失败')
  } finally {
    loading.value.tools = false
  }
}
</script>

<style scoped>
.mcp-page { max-width: 980px; margin: 24px auto; padding: 0 16px; }
.ops { display: flex; gap: 8px; margin-bottom: 12px; }
.result pre { white-space: pre-wrap; }
</style>

