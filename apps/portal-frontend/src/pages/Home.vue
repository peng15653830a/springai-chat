<template>
  <div class="home">
    <h2>选择功能</h2>
    <div class="grid">
      <div class="card" @click="go('/chat')">
        <h3>聊天助手</h3>
        <p>与 AI 实时对话，支持搜索与工具调用</p>
      </div>
      <div class="card" @click="go('/mcp')">
        <h3>MCP 工具</h3>
        <p>示例工具调用与自动化集成</p>
      </div>
      <div class="card" @click="go('/novel')">
        <h3>Novel 创作</h3>
        <p>专业长文本创作工作台</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
const go = (path) => router.push(path)
</script>

<style scoped>
.home { max-width: 980px; margin: 24px auto; padding: 0 16px; }
.grid { display: grid; grid-template-columns: repeat(auto-fit,minmax(240px,1fr)); gap: 14px; }
.card { background: #fff; border-radius: 12px; padding: 18px; box-shadow: 0 6px 18px rgba(0,0,0,.06); cursor: pointer; }
.card:hover { box-shadow: 0 8px 22px rgba(0,0,0,.08); }
h2 { color: #333; margin-bottom: 16px; }
h3 { margin: 0 0 6px; }
p { margin: 0; color: #666; font-size: 13px; }
</style>

