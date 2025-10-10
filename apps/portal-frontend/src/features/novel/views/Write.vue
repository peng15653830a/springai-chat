<template>
  <div class="write-container">
    <div class="columns">
      <div class="col left">
        <h3>故事设定</h3>
        <el-form label-width="80px" class="mb12">
          <el-form-item label="标题">
            <el-input v-model="story.title" placeholder="可选" />
          </el-form-item>
          <el-form-item label="背景">
            <el-input v-model="story.background" type="textarea" :rows="4" placeholder="世界观/人物设定" />
          </el-form-item>
          <el-form-item label="风格">
            <el-input v-model="story.style" type="textarea" :rows="2" placeholder="口吻/节奏/限制" />
          </el-form-item>
          <el-button type="primary" size="small" @click="initStory" :loading="loading">初始化</el-button>
          <span v-if="sessionId" class="sid">Session: {{ sessionId }}</span>
        </el-form>

        <h3>段落大纲</h3>
        <el-input v-model="outlineText" type="textarea" :rows="12" placeholder="每行一段：标题|要点|开头句（可省略）" />
        <div class="mt8">
          <el-button type="primary" size="small" @click="submitOutline" :disabled="!sessionId" :loading="loading">提交大纲</el-button>
          <el-button size="small" @click="loadCurrent" :disabled="!sessionId">读取当前段</el-button>
        </div>
      </div>
      <div class="col center">
        <h3>当前段落</h3>
        <div v-if="current">
          <div class="meta">
            <span>#{{ current.index }} {{ current.title || '（无标题）' }}</span>
            <span class="ml16">状态：{{ current.status }}</span>
            <span class="ml16">版本：{{ current.version || 0 }}</span>
          </div>
          <div class="hint">
            <div>要点：{{ current.prompt }}</div>
            <div v-if="current.starter">开头：{{ current.starter }}</div>
          </div>
          <el-card class="mt8" shadow="never">
            <div class="output" v-html="rendered"></div>
          </el-card>
          <div class="mt8">
            <el-button type="primary" size="small" @click="generate" :loading="generating">生成</el-button>
            <el-button size="small" @click="approve" :disabled="!sessionId">通过并下一段</el-button>
          </div>
          <div class="mt8">
            <el-input v-model="feedback" type="textarea" :rows="2" placeholder="不满意？写下调整意见重写" />
            <el-button class="mt4" size="small" @click="revise" :loading="generating">按意见重写</el-button>
          </div>
        </div>
        <div v-else class="placeholder">请先初始化并提交大纲</div>
      </div>
      <div class="col right">
        <h3>操作提示</h3>
        <ul class="tips">
          <li>先填写背景与风格，点击“初始化”。</li>
          <li>在大纲中每行一段，格式“标题|要点|开头句（可省略）”。</li>
          <li>点击“生成”得到草稿，不满意输入意见再“重写”。</li>
          <li>满意后点击“通过并下一段”。</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const story = ref({ title: '', background: '', style: '' })
const outlineText = ref('')
const sessionId = ref(null)
const current = ref(null)
const feedback = ref('')
const output = ref('')
const generating = ref(false)
const loading = ref(false)

const rendered = computed(() => (output.value || current.value?.latestText || '').replace(/\n/g, '<br/>'))

async function initStory() {
  loading.value = true
  try {
    const res = await fetch('/api/novel/story/init', {
      method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(story.value)
    })
    const data = await res.json()
    if (data.success) {
      sessionId.value = data.sessionId
      current.value = null
      output.value = ''
    }
  } finally { loading.value = false }
}

function parseOutline() {
  const raw = outlineText.value || ''
  const lines = raw.split(/\r?\n/)
  const items = []
  for (const rawLine of lines) {
    const line = (rawLine || '').trim()
    if (!line) continue
    const parts = line.split('|')
    const title = (parts[0] || '').trim()
    const prompt = (parts[1] || '').trim()
    const starter = (parts[2] || '').trim()
    items.push({ title, prompt, starter })
  }
  return { items }
}

async function submitOutline() {
  loading.value = true
  try {
    const payload = parseOutline()
    if (!payload.items || payload.items.length === 0) {
      alert('请先在大纲中填写至少一行：标题|要点|开头句（开头句可省略）')
      return
    }
    await fetch(`/api/novel/story/${sessionId.value}/outline`, {
      method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload)
    })
    await loadCurrent()
  } finally { loading.value = false }
}

async function loadCurrent() {
  const res = await fetch(`/api/novel/story/${sessionId.value}/current`)
  const data = await res.json()
  current.value = data
  output.value = data?.latestText || ''
}

async function readSse(stream) {
  const reader = stream.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    const lines = buffer.split('\n')
    buffer = lines.pop() || ''
    for (const line of lines) {
      if (line.startsWith('event:')) {
        // ignore, we only care about data lines; end handled below
        continue
      } else if (line.startsWith('data:')) {
        const dataStr = line.slice(5).trim()
        if (!dataStr) continue
        try {
          const obj = JSON.parse(dataStr)
          if (obj.content && !obj.done) output.value += obj.content
          if (obj.done) return
        } catch {
          // fallback: append raw
          output.value += dataStr
        }
      }
    }
  }
}

async function generate() {
  if (!sessionId.value || !current.value) return
  generating.value = true
  output.value = ''
  try {
    const res = await fetch(`/api/novel/story/${sessionId.value}/generate?segment=${current.value.index}`, {
      method: 'GET', headers: { 'Accept': 'text/event-stream' }
    })
    await readSse(res.body)
  } catch (e) {
    console.error('generate sse failed', e)
  } finally {
    generating.value = false
    await loadCurrent()
  }
}

async function revise() {
  if (!sessionId.value || !current.value) return
  generating.value = true
  output.value = ''
  try {
    const res = await fetch(`/api/novel/story/${sessionId.value}/revise?segment=${current.value.index}`, {
      method: 'POST', headers: { 'Content-Type': 'application/json', 'Accept': 'text/event-stream' }, body: JSON.stringify({ feedback: feedback.value })
    })
    await readSse(res.body)
  } catch (e) {
    console.error('revise sse failed', e)
  } finally {
    generating.value = false
    await loadCurrent()
  }
}

async function approve() {
  if (!sessionId.value || !current.value) return
  await fetch(`/api/novel/story/${sessionId.value}/approve?segment=${current.value.index}`, { method: 'POST' })
  await loadCurrent()
}
</script>

<style scoped>
.write-container { padding: 12px; }
.columns { display: grid; grid-template-columns: 1.2fr 2fr 1fr; gap: 12px; }
.col h3 { margin: 6px 0 8px; }
.mb12 { margin-bottom: 12px; }
.mt8 { margin-top: 8px; }
.mt4 { margin-top: 4px; }
.ml16 { margin-left: 16px; }
.sid { margin-left: 8px; color: #606266; font-size: 12px; }
.meta { color: #606266; font-size: 12px; }
.hint { color: #606266; font-size: 13px; margin-top: 4px; white-space: pre-wrap; }
.output { white-space: pre-wrap; line-height: 1.6; }
.placeholder { color: #909399; text-align: center; margin-top: 40px; }
</style>
