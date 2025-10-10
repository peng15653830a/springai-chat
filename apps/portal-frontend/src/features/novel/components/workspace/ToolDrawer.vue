<template>
  <div class="tool-drawer" :class="{ 'is-open': isOpen }">
    <div class="drawer-handle" @click="toggleDrawer">
      <div class="handle-content">
        <el-icon><Tools /></el-icon>
        <span>MCP 工具</span>
        <el-badge :value="activeToolCount" :hidden="activeToolCount === 0" />
      </div>
      <el-icon class="toggle-icon" :class="{ rotated: isOpen }">
        <ArrowUp />
      </el-icon>
    </div>

    <transition name="slide-up">
      <div v-show="isOpen" class="drawer-content">
        <el-tabs v-model="activeToolTab" class="tool-tabs">
          <!-- Bash 工具 -->
          <el-tab-pane name="bash">
            <template #label>
              <span class="tab-label">
                <span>$</span>
                Bash
              </span>
            </template>

            <div class="tool-panel">
              <div class="command-input-area">
                <el-input
                  v-model="bashCommand"
                  placeholder="输入命令... (Enter执行)"
                  @keyup.enter="executeBash"
                  class="command-input"
                >
                  <template #prepend>
                    <span class="prompt-symbol">$</span>
                  </template>
                  <template #append>
                    <el-button
                      @click="executeBash"
                      :loading="bashExecuting"
                      :icon="Position"
                    >
                      执行
                    </el-button>
                  </template>
                </el-input>
              </div>

              <div class="output-area" ref="bashOutputRef">
                <div v-if="bashHistory.length === 0" class="empty-hint">
                  等待命令输入...
                </div>
                <div
                  v-for="(item, index) in bashHistory"
                  :key="index"
                  class="output-item"
                >
                  <div class="command-line">
                    <span class="prompt">$</span>
                    <span class="command">{{ item.command }}</span>
                  </div>
                  <pre class="output-text">{{ item.output }}</pre>
                </div>
              </div>

              <div class="tool-actions">
                <el-button
                  text
                  @click="clearBashHistory"
                  :icon="Delete"
                >
                  清空历史
                </el-button>
              </div>
            </div>
          </el-tab-pane>

          <!-- 文件系统 -->
          <el-tab-pane name="filesystem">
            <template #label>
              <span class="tab-label">
                <el-icon><FolderOpened /></el-icon>
                文件
              </span>
            </template>

            <div class="tool-panel">
              <div class="filesystem-header">
                <el-input
                  v-model="currentPath"
                  placeholder="文件路径"
                  readonly
                  class="path-input"
                >
                  <template #prepend>
                    <el-icon><Folder /></el-icon>
                  </template>
                </el-input>
                <el-button @click="refreshFileTree" :icon="Refresh">
                  刷新
                </el-button>
              </div>

              <div class="file-tree-area">
                <el-tree
                  :data="fileTree"
                  :props="fileTreeProps"
                  @node-click="handleFileNodeClick"
                  default-expand-all
                >
                  <template #default="{ node, data }">
                    <span class="file-node">
                      <el-icon>
                        <component :is="data.isDirectory ? Folder : Document" />
                      </el-icon>
                      <span>{{ node.label }}</span>
                    </span>
                  </template>
                </el-tree>
              </div>
            </div>
          </el-tab-pane>

          <!-- 浏览器 -->
          <el-tab-pane name="browser">
            <template #label>
              <span class="tab-label">
                <el-icon><Monitor /></el-icon>
                浏览器
              </span>
            </template>

            <div class="tool-panel">
              <div class="browser-controls">
                <el-input
                  v-model="browserUrl"
                  placeholder="输入URL..."
                  @keyup.enter="navigateTo"
                  class="url-input"
                >
                  <template #prepend>
                    <el-icon><Link /></el-icon>
                  </template>
                  <template #append>
                    <el-button
                      @click="navigateTo"
                      :loading="browserLoading"
                      :icon="Position"
                    >
                      访问
                    </el-button>
                  </template>
                </el-input>
              </div>

              <div class="browser-status">
                <span v-if="browserCurrentUrl" class="status-text">
                  当前: {{ browserCurrentUrl }}
                </span>
                <span v-else class="status-text empty">
                  未访问任何页面
                </span>
              </div>

              <div class="browser-actions">
                <el-button @click="browserBack" :icon="Back">
                  后退
                </el-button>
                <el-button @click="browserForward" :icon="Right">
                  前进
                </el-button>
                <el-button @click="browserRefresh" :icon="Refresh">
                  刷新
                </el-button>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import {
  Tools,
  ArrowUp,
  Position,
  Delete,
  FolderOpened,
  Folder,
  Document,
  Refresh,
  Monitor,
  Link,
  Back,
  Right
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const isOpen = ref(false)
const activeToolTab = ref('bash')

// Bash 相关
const bashCommand = ref('')
const bashExecuting = ref(false)
const bashHistory = ref([])
const bashOutputRef = ref(null)

// 文件系统相关
const currentPath = ref('~/')
const fileTree = ref([
  {
    label: 'materials',
    isDirectory: true,
    children: [
      { label: 'sample1.txt', isDirectory: false },
      { label: 'sample2.md', isDirectory: false }
    ]
  },
  {
    label: 'exports',
    isDirectory: true,
    children: []
  }
])
const fileTreeProps = {
  children: 'children',
  label: 'label'
}

// 浏览器相关
const browserUrl = ref('')
const browserCurrentUrl = ref('')
const browserLoading = ref(false)

const activeToolCount = computed(() => {
  let count = 0
  if (bashHistory.value.length > 0) count++
  if (browserCurrentUrl.value) count++
  return count
})

const toggleDrawer = () => {
  isOpen.value = !isOpen.value
}

const executeBash = async () => {
  if (!bashCommand.value.trim()) return

  bashExecuting.value = true
  const cmd = bashCommand.value

  try {
    // TODO: 调用MCP Bash工具
    // 模拟执行
    await new Promise(resolve => setTimeout(resolve, 500))
    bashHistory.value.push({
      command: cmd,
      output: `模拟输出: ${cmd} 执行成功`
    })

    bashCommand.value = ''
    scrollToBottom()
  } catch (error) {
    ElMessage.error('命令执行失败: ' + error.message)
  } finally {
    bashExecuting.value = false
  }
}

const clearBashHistory = () => {
  bashHistory.value = []
  ElMessage.success('历史已清空')
}

const scrollToBottom = () => {
  nextTick(() => {
    if (bashOutputRef.value) {
      bashOutputRef.value.scrollTop = bashOutputRef.value.scrollHeight
    }
  })
}

const refreshFileTree = () => {
  // TODO: 刷新文件树
  ElMessage.success('已刷新文件树')
}

const handleFileNodeClick = (data) => {
  if (!data.isDirectory) {
    ElMessage.info(`选择文件: ${data.label}`)
    // TODO: 读取文件内容
  }
}

const navigateTo = async () => {
  if (!browserUrl.value.trim()) return

  browserLoading.value = true
  try {
    // TODO: 调用MCP浏览器工具
    await new Promise(resolve => setTimeout(resolve, 800))
    browserCurrentUrl.value = browserUrl.value
    ElMessage.success('页面加载完成')
  } catch (error) {
    ElMessage.error('访问失败: ' + error.message)
  } finally {
    browserLoading.value = false
  }
}

const browserBack = () => {
  ElMessage.info('后退功能开发中')
}

const browserForward = () => {
  ElMessage.info('前进功能开发中')
}

const browserRefresh = () => {
  if (browserCurrentUrl.value) {
    navigateTo()
  }
}
</script>

<style scoped>
.tool-drawer {
  border-top: 1px solid #e4e7ed;
  background: white;
  transition: all 0.3s ease;
}

.tool-drawer.is-open {
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.08);
}

.drawer-handle {
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  cursor: pointer;
  background: #fafafa;
  transition: background-color 0.2s;
}

.drawer-handle:hover {
  background: #f0f2f5;
}

.handle-content {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
}

.toggle-icon {
  transition: transform 0.3s ease;
}

.toggle-icon.rotated {
  transform: rotate(180deg);
}

.drawer-content {
  height: 240px;
  overflow: hidden;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  height: 0;
  opacity: 0;
}

.tool-tabs {
  height: 100%;
}

.tool-tabs :deep(.el-tabs__header) {
  margin: 0;
  padding: 0 16px;
  border-bottom: 1px solid #e4e7ed;
}

.tool-tabs :deep(.el-tabs__content) {
  height: calc(100% - 40px);
  overflow: hidden;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
}

.tool-panel {
  height: 100%;
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* Bash 工具样式 */
.command-input-area {
  flex-shrink: 0;
}

.prompt-symbol {
  font-family: 'Consolas', 'Monaco', monospace;
  color: #67c23a;
  font-weight: bold;
}

.output-area {
  flex: 1;
  overflow-y: auto;
  background: #1e1e1e;
  border-radius: 4px;
  padding: 12px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
}

.empty-hint {
  color: #666;
  text-align: center;
  padding: 20px;
}

.output-item {
  margin-bottom: 12px;
}

.command-line {
  color: #67c23a;
  margin-bottom: 4px;
}

.prompt {
  margin-right: 8px;
}

.output-text {
  color: #d4d4d4;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
}

.tool-actions {
  flex-shrink: 0;
  display: flex;
  justify-content: flex-end;
}

/* 文件系统样式 */
.filesystem-header {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.path-input {
  flex: 1;
}

.file-tree-area {
  flex: 1;
  overflow-y: auto;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 8px;
}

.file-node {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 浏览器样式 */
.browser-controls {
  flex-shrink: 0;
}

.url-input {
  width: 100%;
}

.browser-status {
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  flex-shrink: 0;
}

.status-text {
  font-size: 13px;
  color: #606266;
}

.status-text.empty {
  color: #909399;
}

.browser-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}
</style>
