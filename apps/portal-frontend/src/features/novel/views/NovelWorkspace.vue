<template>
  <div class="novel-workspace">
    <TopBar />
    <div class="main-layout">
      <ProjectTree class="left-panel" />
      <EditorArea class="center-panel" />
      <ResourcePanel class="right-panel" />
    </div>
    <ToolDrawer />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useNovelWorkspaceStore } from '../stores/novelWorkspace'
import TopBar from '../components/workspace/TopBar.vue'
import ProjectTree from '../components/workspace/ProjectTree.vue'
import EditorArea from '../components/workspace/EditorArea.vue'
import ResourcePanel from '../components/workspace/ResourcePanel.vue'
import ToolDrawer from '../components/workspace/ToolDrawer.vue'

const workspaceStore = useNovelWorkspaceStore()

onMounted(() => {
  workspaceStore.initialize()
})
</script>

<style scoped>
.novel-workspace {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
  overflow: hidden;
}

.main-layout {
  flex: 1;
  display: flex;
  gap: 0;
  overflow: hidden;
}

.left-panel {
  width: 240px;
  flex-shrink: 0;
  border-right: 1px solid #e4e7ed;
  background: #fafafa;
}

.center-panel {
  flex: 1;
  background: white;
  overflow: hidden;
}

.right-panel {
  width: 340px;
  flex-shrink: 0;
  border-left: 1px solid #e4e7ed;
  background: white;
}
</style>
