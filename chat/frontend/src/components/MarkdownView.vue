<template>
  <div class="typewriter-markdown" ref="containerRef" v-html="renderedHtml"></div>
</template>

<script>
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import MarkdownIt from 'markdown-it'
import lazyHeaders from 'markdown-it-lazy-headers'
import multimdTable from 'markdown-it-multimd-table'
import taskLists from 'markdown-it-task-lists'
import deflist from 'markdown-it-deflist'
import anchor from 'markdown-it-anchor'
import toc from 'markdown-it-toc-done-right'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'
import DOMPurify from 'dompurify'

export default {
  name: 'MarkdownView',
  props: {
    content: {
      type: String,
      default: ''
    },
    anchorPrefix: {
      type: String,
      default: ''
    },
    enableTypewriter: {
      type: Boolean,
      default: true
    },
    speed: {
      type: Number,
      default: 30 // 打字速度（毫秒）
    }
  },
  emits: ['typing-complete'],
  setup(props, { emit }) {
    const renderedHtml = ref('')
    const containerRef = ref(null)
    const isTyping = ref(false)
    const typewriterTimer = ref(null)
    const fullText = ref('')
    const typedIndex = ref(0)

    // 初始化markdown-it实例
    const md = new MarkdownIt({
      html: true,
      linkify: true,
      typographer: true,
      breaks: true,
      highlight: function (str, lang) {
        if (lang && hljs.getLanguage(lang)) {
          try {
            return hljs.highlight(str, { language: lang }).value
          } catch (__) {}
        }
        return '' // 使用外部默认转义
      }
    })
    // 启用社区插件：懒标题、增强表格、任务清单、定义列表
    md.use(lazyHeaders)
    md.use(multimdTable, { multiline: true, rowspan: true, headerless: true })
    md.use(taskLists, { label: true, labelAfter: true })
    md.use(deflist)

    // 启用 Anchor（标题锚点）和 TOC（[[toc]] 目录）
    try {
      const safeSlugify = (s) => {
        const base = String(s || '')
          .trim()
          .toLowerCase()
          .replace(/[\s\u3000]+/g, '-')
          .replace(/[^a-z0-9_\-\u4e00-\u9fa5]/g, '')
        const prefix = (props.anchorPrefix || '').replace(/[^a-zA-Z0-9_\-]/g, '')
        return (prefix ? prefix + '-' : '') + base
      }

      // 兼容不同版本的 permalink API
      let anchorOptions = {
        level: [1,2,3,4,5,6],
        slugify: safeSlugify,
      }
      try {
        const permalinkFactory = anchor?.permalink?.linkInsideHeader || anchor?.permalink?.headerLink
        if (permalinkFactory) {
          anchorOptions.permalink = permalinkFactory({
            symbol: '🔗',
            placement: 'after'
          })
        } else {
          anchorOptions.permalink = true
          anchorOptions.permalinkSymbol = '🔗'
          anchorOptions.permalinkBefore = false
        }
      } catch (_) {
        anchorOptions.permalink = true
        anchorOptions.permalinkSymbol = '🔗'
        anchorOptions.permalinkBefore = false
      }
      md.use(anchor, anchorOptions)
    } catch (e) {
      console.warn('[Markdown] anchor配置失败，已跳过', e)
    }

    try {
      md.use(toc, {
        containerClass: 'md-toc',
        listType: 'ul',
        level: [1,2,3,4,5,6]
      })
    } catch (e) {
      console.warn('[Markdown] toc配置失败，已跳过', e)
    }

    // 仅规范换行，不注入字符
    const normalize = (raw) => String(raw || '').replace(/\r\n?/g, '\n')

    // 渲染阶段的 GFM 友好规范化（不修改源，而是在渲染前修补可读性）
    const normalizeGfmForRender = (raw) => {
      let text = normalize(raw)
      // 非行首出现的标题标记前插入空行，避免与上一段黏连
      text = text.replace(/([^\n])(?=(#{1,6}\s))/g, '$1\n\n')
      // 标题 # 后缺空格则补空格
      text = text.replace(/(^|\n)(#{1,6})([^\s#])/g, '$1$2 $3')
      // 列表标记不在行首，前插入换行
      text = text.replace(/([^\n])(?=(-\s|\d+\.\s))/g, '$1\n')
      // 行首无序列表补空格
      text = text.replace(/(^|\n)([-*+])([^\s\-\*\+])/g, '$1$2 $3')
      // 行首有序列表补空格
      text = text.replace(/(^|\n)(\d+\.)([^\s])/g, '$1$2 $3')
      return text
    }

    // 若当前片段出现不成对的围栏，临时闭合以避免渲染错乱
    const balanceFences = (text) => {
      const fenceCount = (text.match(/```/g) || []).length
      if (fenceCount % 2 === 1) {
        return text.replace(/\s*$/, '') + '\n```\n'
      }
      return text
    }
    
    // 清理定时器
    const clearTypewriterTimer = () => {
      if (typewriterTimer.value) {
        clearTimeout(typewriterTimer.value)
        typewriterTimer.value = null
      }
    }
    
    // 渲染markdown内容
    const renderMarkdown = (content) => {
      const safe = normalizeGfmForRender(content || '')
      const fixed = balanceFences(safe)
      const html = fixed ? md.render(fixed) : ''
      renderedHtml.value = DOMPurify.sanitize(html, { ADD_ATTR: ['target', 'rel'] })
      nextTick(() => postProcessDom())
    }

    // 渲染后对DOM进行一次纠偏：
    // 1) 移除空标题（只剩锚点符号的）
    // 2) 表格列宽对齐：
    //    - 若表头首列以#开头（如"# XXX"），提取为caption并移除该列
    //    - 对齐每行单元格数量，不足补空单元格
    const postProcessDom = () => {
      const root = containerRef.value
      if (!root) return
      try {
        // 1) 移除空标题
        const headings = root.querySelectorAll('h1, h2, h3, h4, h5, h6')
        headings.forEach(h => {
          const text = (h.textContent || '').replace('🔗', '').trim()
          if (!text) {
            h.remove()
          }
        })

        // 2) 表格修正
        const tables = root.querySelectorAll('table')
        tables.forEach(table => {
          const thead = table.querySelector('thead')
          const headerRow = thead ? thead.querySelector('tr') : null
          let headerCount = headerRow ? headerRow.cells.length : 0

          // 2.1 若表头首单元为"# XXX"，转为caption
          if (headerRow && headerRow.cells.length > 0) {
            const first = headerRow.cells[0]
            const firstText = (first.textContent || '').trim()
            if (/^#+\s*/.test(firstText)) {
              const captionText = firstText.replace(/^#+\s*/, '').trim()
              if (captionText) {
                const cap = document.createElement('caption')
                cap.textContent = captionText
                table.insertBefore(cap, table.firstChild)
              }
              headerRow.deleteCell(0)
              headerCount = headerRow.cells.length
            }
          }

          // 2.2 若没有thead或headerCount为0，则以第一行作为基准列数
          if (!thead || headerCount === 0) {
            const firstBodyRow = table.querySelector('tbody tr') || table.querySelector('tr')
            if (firstBodyRow) headerCount = firstBodyRow.cells.length
          }

          // 2.3 对齐每一行单元格数至headerCount
          const allRows = table.querySelectorAll('tr')
          allRows.forEach(row => {
            while (headerCount > 0 && row.cells.length < headerCount) {
              const td = row.insertCell(-1)
              td.textContent = ''
            }
          })
        })
      } catch (e) {
        // ignore post-fix errors
      }
    }

    // 不再逐字符渲染，改为按段落渲染（稳定块）
    
    // 滚动到底部
    const scrollToBottom = () => {
      if (containerRef.value) {
        const messageList = containerRef.value.closest('.message-list')
        if (messageList) {
          messageList.scrollTop = messageList.scrollHeight
        }
      }
    }
    
    // 打字机：逐字符展示，同时在渲染前做最小纠偏与围栏闭合
    const tick = () => {
      if (!props.enableTypewriter) return
      if (typedIndex.value >= fullText.value.length) {
        clearTypewriterTimer()
        isTyping.value = false
        emit('typing-complete')
        return
      }
      isTyping.value = true
      typedIndex.value = Math.min(typedIndex.value + Math.max(1, Math.floor(props.speed / 10)), fullText.value.length)
      const view = fullText.value.slice(0, typedIndex.value)
      renderMarkdown(view)
      typewriterTimer.value = setTimeout(tick, props.speed)
    }

    // 监听内容变化：恢复打字机效果
    watch(() => props.content, (newContent) => {
      const incoming = String(newContent || '')
      // 累积目标文本（SSE增量到达时继续往后打字）
      if (incoming.length > fullText.value.length) {
        fullText.value = incoming
      } else {
        fullText.value = incoming
        typedIndex.value = Math.min(typedIndex.value, fullText.value.length)
      }
      if (props.enableTypewriter) {
        if (!typewriterTimer.value) tick()
      } else {
        // 非打字机模式立即渲染完整内容
        typedIndex.value = fullText.value.length
        renderMarkdown(fullText.value)
        isTyping.value = false
        emit('typing-complete')
      }
      nextTick(() => scrollToBottom())
    }, { immediate: true })
    
    // 组件销毁时清理
    onBeforeUnmount(() => {
      clearTypewriterTimer()
    })
    
    return {
      renderedHtml,
      containerRef,
      isTyping
    }
  }
}
</script>

<style scoped>
.typewriter-markdown {
  width: 100%;
  line-height: 1.6;
}

/* 代码块样式 */
.typewriter-markdown :deep(pre) {
  background: #f6f8fa;
  border-radius: 8px;
  padding: 16px;
  overflow-x: auto;
  border: 1px solid #e1e4e8;
  margin: 16px 0;
}

.typewriter-markdown :deep(code) {
  font-family: 'SF Mono', Monaco, 'Cascadia Code', 'Roboto Mono', Consolas, 'Courier New', monospace;
  font-size: 0.9em;
}

.typewriter-markdown :deep(p code) {
  background: #f3f4f6;
  padding: 2px 4px;
  border-radius: 3px;
  color: #e53e3e;
}

/* 表格样式 */
.typewriter-markdown :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 16px 0;
  border: 1px solid #e1e4e8;
}

.typewriter-markdown :deep(th),
.typewriter-markdown :deep(td) {
  border: 1px solid #e1e4e8;
  padding: 8px 12px;
  text-align: left;
}

.typewriter-markdown :deep(th) {
  background: #f6f8fa;
  font-weight: 600;
}

/* 列表样式 */
.typewriter-markdown :deep(ul),
.typewriter-markdown :deep(ol) {
  margin: 16px 0;
  padding-left: 24px;
}

.typewriter-markdown :deep(li) {
  margin: 4px 0;
}

/* 引用样式 */
.typewriter-markdown :deep(blockquote) {
  border-left: 4px solid #e1e4e8;
  padding: 0 16px;
  margin: 16px 0;
  color: #6a737d;
}

/* 链接样式 */
.typewriter-markdown :deep(a) {
  color: #0366d6;
  text-decoration: none;
}

.typewriter-markdown :deep(a:hover) {
  text-decoration: underline;
}

/* 标题样式 */
.typewriter-markdown :deep(h1),
.typewriter-markdown :deep(h2),
.typewriter-markdown :deep(h3),
.typewriter-markdown :deep(h4),
.typewriter-markdown :deep(h5),
.typewriter-markdown :deep(h6) {
  margin: 24px 0 16px 0;
  font-weight: 600;
  line-height: 1.25;
}

.typewriter-markdown :deep(h1) { font-size: 1.75em; }
.typewriter-markdown :deep(h2) { font-size: 1.5em; }
.typewriter-markdown :deep(h3) { font-size: 1.25em; }
.typewriter-markdown :deep(h4) { font-size: 1.1em; }

/* 分隔线样式 */
.typewriter-markdown :deep(hr) {
  border: none;
  height: 1px;
  background: #e1e4e8;
  margin: 24px 0;
}

/* 目录（TOC）样式 */
.typewriter-markdown :deep(.md-toc) {
  border: 1px solid #e5e7eb;
  background: #fafafa;
  border-radius: 8px;
  padding: 12px 16px;
  margin: 12px 0 16px 0;
}

.typewriter-markdown :deep(.md-toc ul) {
  list-style: none;
  padding-left: 0;
  margin: 0;
}

.typewriter-markdown :deep(.md-toc li) {
  margin: 4px 0;
}

.typewriter-markdown :deep(.md-toc ul ul) {
  padding-left: 16px;
  margin-top: 4px;
  border-left: 2px solid #eee;
}

.typewriter-markdown :deep(.md-toc a) {
  color: #409eff;
  text-decoration: none;
}

.typewriter-markdown :deep(.md-toc a:hover) {
  text-decoration: underline;
}

/* 标题锚点（Anchor）样式 */
.typewriter-markdown :deep(h1 a.header-anchor,
                           h2 a.header-anchor,
                           h3 a.header-anchor,
                           h4 a.header-anchor,
                           h5 a.header-anchor,
                           h6 a.header-anchor,
                           h1 a.anchor,
                           h2 a.anchor,
                           h3 a.anchor,
                           h4 a.anchor,
                           h5 a.anchor,
                           h6 a.anchor) {
  margin-left: 8px;
  opacity: 0;
  transition: opacity 0.2s ease, color 0.2s ease;
  color: #9aa4af;
  text-decoration: none;
  font-size: 0.9em;
}

.typewriter-markdown :deep(h1:hover a.header-anchor,
                           h2:hover a.header-anchor,
                           h3:hover a.header-anchor,
                           h4:hover a.header-anchor,
                           h5:hover a.header-anchor,
                           h6:hover a.header-anchor,
                           h1:hover a.anchor,
                           h2:hover a.anchor,
                           h3:hover a.anchor,
                           h4:hover a.anchor,
                           h5:hover a.anchor,
                           h6:hover a.anchor) {
  opacity: 1;
  color: #409eff;
}

/* 深色主题适配 */
@media (prefers-color-scheme: dark) {
  .typewriter-markdown :deep(.md-toc) {
    background: #1f1f1f;
    border-color: #333;
  }
  .typewriter-markdown :deep(.md-toc ul ul) {
    border-left-color: #444;
  }
  .typewriter-markdown :deep(.md-toc a) {
    color: #6aa9ff;
  }
  .typewriter-markdown :deep(h1 a.header-anchor,
                             h2 a.header-anchor,
                             h3 a.header-anchor,
                             h4 a.header-anchor,
                             h5 a.header-anchor,
                             h6 a.header-anchor,
                             h1 a.anchor,
                             h2 a.anchor,
                             h3 a.anchor,
                             h4 a.anchor,
                             h5 a.anchor,
                             h6 a.anchor) {
    color: #8a94a1;
  }
  .typewriter-markdown :deep(h1:hover a.header-anchor,
                             h2:hover a.header-anchor,
                             h3:hover a.header-anchor,
                             h4:hover a.header-anchor,
                             h5:hover a.header-anchor,
                             h6:hover a.header-anchor,
                             h1:hover a.anchor,
                             h2:hover a.anchor,
                             h3:hover a.anchor,
                             h4:hover a.anchor,
                             h5:hover a.anchor,
                             h6:hover a.anchor) {
    color: #6aa9ff;
  }
}
</style>
