<template>
  <div v-show="!collapsed" class="right-panel">
    <h4>搜索结果</h4>
    <ul>
      <li v-for="(r,i) in (searchResults||[])" :key="i">
        <a :href="r.url" target="_blank">{{ r.title || r.url }}</a>
      </li>
    </ul>
  </div>
</template>

<script setup>
defineProps({ searchResults: { type: Array, default: () => [] }, currentMessageId: [String, Number], collapsed: Boolean })
defineEmits(['toggle'])
</script>

<style scoped>
.right-panel { position: fixed; right: 16px; top: 80px; width: 280px; background: #fff; border-radius: 8px; padding: 12px; box-shadow: 0 6px 18px rgba(0,0,0,.06); }
</style>

