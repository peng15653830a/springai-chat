<template>
  <div class="typewriter-markdown" ref="containerRef">
    <VueMarkdownRender 
      :markdown="displayedContent" 
      :options="markdownOptions"
    />
  </div>
</template>

<script>
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import VueMarkdownRender from 'vue-markdown-render'

export default {
  name: 'TypewriterMarkdown',
  components: {
    VueMarkdownRender
  },
  props: {
    content: {
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
    const displayedContent = ref('')
    const containerRef = ref(null)
    const currentIndex = ref(0)
    const isTyping = ref(false)
    const typewriterTimer = ref(null)
    
    // Markdown渲染配置
    const markdownOptions = {
      html: true,
      linkify: true,
      typographer: true,
      breaks: true,
      highlight: (str, lang) => {
        // 使用highlight.js进行代码高亮
        if (window.hljs && lang && window.hljs.getLanguage(lang)) {
          try {
            return window.hljs.highlight(str, { language: lang }).value
          } catch (err) {
            console.error('代码高亮失败:', err)
          }
        }
        return str
      }
    }
    
    // 清理定时器
    const clearTypewriterTimer = () => {
      if (typewriterTimer.value) {
        clearTimeout(typewriterTimer.value)
        typewriterTimer.value = null
      }
    }
    
    // 打字机效果实现
    const startTypewriter = () => {
      if (!props.enableTypewriter || !props.content) {
        displayedContent.value = props.content
        emit('typing-complete')
        return
      }
      
      isTyping.value = true
      currentIndex.value = 0
      displayedContent.value = ''
      
      const typeNextChar = () => {
        if (currentIndex.value < props.content.length) {
          // 逐字符添加内容
          displayedContent.value = props.content.substring(0, currentIndex.value + 1)
          currentIndex.value++
          
          // 自动滚动到底部
          nextTick(() => {
            scrollToBottom()
          })
          
          typewriterTimer.value = setTimeout(typeNextChar, props.speed)
        } else {
          // 打字完成
          isTyping.value = false
          emit('typing-complete')
        }
      }
      
      typeNextChar()
    }
    
    // 滚动到底部
    const scrollToBottom = () => {
      if (containerRef.value) {
        const messageList = containerRef.value.closest('.message-list')
        if (messageList) {
          messageList.scrollTop = messageList.scrollHeight
        }
      }
    }
    
    // 监听内容变化
    watch(() => props.content, (newContent, oldContent) => {
      clearTypewriterTimer()
      
      if (!oldContent || oldContent.length === 0) {
        // 新消息，启动打字机效果
        startTypewriter()
      } else if (newContent.length > oldContent.length) {
        // 内容追加（流式更新）
        if (props.enableTypewriter) {
          // 如果正在打字，直接更新目标内容，让打字机继续
          // 如果没在打字，重新开始
          if (!isTyping.value) {
            startTypewriter()
          }
        } else {
          displayedContent.value = newContent
        }
      } else {
        // 内容完全替换
        startTypewriter()
      }
    }, { immediate: true })
    
    // 组件销毁时清理
    onBeforeUnmount(() => {
      clearTypewriterTimer()
    })
    
    return {
      displayedContent,
      containerRef,
      markdownOptions,
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
</style>