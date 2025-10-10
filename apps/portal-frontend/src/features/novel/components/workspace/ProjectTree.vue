<template>
  <div class="project-tree">
    <div class="tree-header">
      <h3>项目结构</h3>
      <el-button
        text
        :icon="Plus"
        @click="handleAddSegment"
      />
    </div>

    <el-tree
      ref="treeRef"
      :data="treeData"
      :props="treeProps"
      node-key="id"
      default-expand-all
      draggable
      :allow-drop="allowDrop"
      :allow-drag="allowDrag"
      @node-click="handleNodeClick"
      @node-drop="handleNodeDrop"
    >
      <template #default="{ node, data }">
        <span class="tree-node">
          <el-icon class="node-icon" :class="getIconClass(data)">
            <component :is="getIcon(data.type)" />
          </el-icon>
          <span class="node-label">{{ data.label }}</span>
          <div class="node-badges">
            <el-tag
              v-if="data.status === 'approved'"
              size="small"
              type="success"
              effect="plain"
            >
              ✓
            </el-tag>
            <el-tag
              v-else-if="data.status === 'draft'"
              size="small"
              type="info"
              effect="plain"
            >
              草稿
            </el-tag>
            <el-tag
              v-else-if="data.status === 'needs_revision'"
              size="small"
              type="warning"
              effect="plain"
            >
              待修订
            </el-tag>
            <span v-if="data.version" class="version-badge">
              v{{ data.version }}
            </span>
          </div>
        </span>
      </template>
    </el-tree>

    <!-- 添加段落对话框 -->
    <el-dialog
      v-model="addSegmentDialogVisible"
      title="添加段落"
      width="500px"
    >
      <el-form :model="newSegment" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="newSegment.title" placeholder="段落标题" />
        </el-form-item>
        <el-form-item label="要点">
          <el-input
            v-model="newSegment.prompt"
            type="textarea"
            :rows="3"
            placeholder="本段落需要描写的要点"
          />
        </el-form-item>
        <el-form-item label="开头句">
          <el-input
            v-model="newSegment.starter"
            placeholder="可选：段落开头语句"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addSegmentDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAddSegment">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import {
  Setting,
  Document,
  Folder,
  Download,
  Plus,
  Edit
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useNovelWorkspaceStore } from '../../stores/novelWorkspace'

const workspaceStore = useNovelWorkspaceStore()
const treeRef = ref(null)
const addSegmentDialogVisible = ref(false)
const newSegment = ref({
  title: '',
  prompt: '',
  starter: ''
})

const treeProps = {
  children: 'children',
  label: 'label'
}

const treeData = computed(() => {
  const outline = workspaceStore.project.outline || []
  return [
    {
      id: 'settings',
      label: '故事设定',
      type: 'settings',
      children: [
        { id: 'background', label: '世界观背景', type: 'doc' },
        { id: 'characters', label: '角色设定', type: 'doc' },
        { id: 'style', label: '写作风格', type: 'doc' }
      ]
    },
    {
      id: 'outline',
      label: '章节大纲',
      type: 'outline',
      children: outline.map(seg => ({
        id: `segment-${seg.index}`,
        label: seg.title || `段落 ${seg.index}`,
        type: 'segment',
        status: seg.status,
        version: seg.version,
        segmentData: seg
      }))
    },
    {
      id: 'drafts',
      label: '草稿箱',
      type: 'folder',
      children: []
    },
    {
      id: 'exports',
      label: '导出历史',
      type: 'folder',
      children: []
    }
  ]
})

const getIcon = (type) => {
  const iconMap = {
    settings: Setting,
    outline: Document,
    folder: Folder,
    segment: Edit,
    doc: Document
  }
  return iconMap[type] || Document
}

const getIconClass = (data) => {
  const classMap = {
    settings: 'icon-settings',
    outline: 'icon-outline',
    folder: 'icon-folder',
    segment: 'icon-segment',
    doc: 'icon-doc'
  }
  return classMap[data.type] || ''
}

const allowDrop = (draggingNode, dropNode, type) => {
  // 只允许段落在大纲内重排序
  if (draggingNode.data.type !== 'segment') return false
  if (dropNode.data.id !== 'outline' && dropNode.data.type !== 'segment') return false
  return type !== 'inner'
}

const allowDrag = (draggingNode) => {
  // 只允许拖拽段落
  return draggingNode.data.type === 'segment'
}

const handleNodeClick = (data) => {
  if (data.type === 'segment' && data.segmentData) {
    workspaceStore.selectSegment(data.segmentData.index)
  } else if (data.type === 'doc') {
    // 打开设定编辑
    workspaceStore.editSettings(data.id)
  }
}

const handleNodeDrop = (draggingNode, dropNode, dropType) => {
  // 更新段落顺序
  const outlineNode = treeData.value.find(n => n.id === 'outline')
  if (outlineNode && outlineNode.children) {
    const newOrder = outlineNode.children.map((child, index) => ({
      ...child.segmentData,
      index: index + 1
    }))
    workspaceStore.reorderSegments(newOrder)
    ElMessage.success('段落顺序已更新')
  }
}

const handleAddSegment = () => {
  addSegmentDialogVisible.value = true
  newSegment.value = {
    title: '',
    prompt: '',
    starter: ''
  }
}

const confirmAddSegment = async () => {
  if (!newSegment.value.title || !newSegment.value.prompt) {
    ElMessage.warning('请填写标题和要点')
    return
  }

  try {
    await workspaceStore.addSegment(newSegment.value)
    addSegmentDialogVisible.value = false
    ElMessage.success('段落已添加')
  } catch (error) {
    ElMessage.error('添加失败: ' + error.message)
  }
}
</script>

<style scoped>
.project-tree {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.tree-header {
  padding: 12px 16px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tree-header h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.el-tree {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
  background: transparent;
}

.tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  padding-right: 8px;
}

.node-icon {
  font-size: 16px;
  flex-shrink: 0;
}

.icon-settings {
  color: #909399;
}

.icon-outline {
  color: #409eff;
}

.icon-folder {
  color: #e6a23c;
}

.icon-segment {
  color: #67c23a;
}

.icon-doc {
  color: #909399;
}

.node-label {
  flex: 1;
  font-size: 13px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.node-badges {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.version-badge {
  font-size: 11px;
  color: #909399;
  padding: 0 4px;
}

/* Tree样式优化 */
:deep(.el-tree-node__content) {
  height: 32px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

:deep(.el-tree-node__content:hover) {
  background-color: #f5f7fa;
}

:deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: #ecf5ff;
  color: #409eff;
}

:deep(.el-tree-node.is-drop-inner) {
  background-color: #ecf5ff;
}
</style>
