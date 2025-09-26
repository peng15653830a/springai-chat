<template>
  <div class="search-indicator" @click="$emit('click')">🔍 搜索引用 ({{ (results||[]).length }})</div>
</template>

<script setup>
defineProps({ results: { type: Array, default: () => [] }, messageId: { type: [String, Number], default: null } })
</script>

<style scoped>
.search-indicator { font-size: 12px; color: #409eff; cursor: pointer; margin-bottom: 8px; }
</style>

