<template>
  <div class="markdown-renderer" v-html="renderedHtml"></div>
</template>

<script>
import { ref, watch } from 'vue'
import MarkdownIt from 'markdown-it'
import lazyHeaders from 'markdown-it-lazy-headers'
import multimdTable from 'markdown-it-multimd-table'
import taskLists from 'markdown-it-task-lists'
import deflist from 'markdown-it-deflist'
import anchor from 'markdown-it-anchor'
import toc from 'markdown-it-toc-done-right'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

export default {
  name: 'MarkdownRenderer',
  props: {
    content: {
      type: String,
      default: ''
    }
  },
  setup(props) {
    const renderedHtml = ref('')

    const md = new MarkdownIt({
      html: true,
      linkify: true,
      typographer: true,
      breaks: true,
      highlight: function (str, lang) {
        if (lang && hljs.getLanguage(lang)) {
          try {
            return hljs.highlight(str, { language: lang }).value
          } catch (_) {}
        }
        return ''
      }
    })
      .use(lazyHeaders)
      .use(multimdTable, { multiline: true, rowspan: true, headerless: true })
      .use(taskLists, { label: true, labelAfter: true })
      .use(deflist)

    try {
      const slugify = (s) => String(s || '')
        .trim().toLowerCase()
        .replace(/[\s\u3000]+/g, '-')
        .replace(/[^a-z0-9_\-\u4e00-\u9fa5]/g, '')

      let anchorOptions = { level: [1,2,3,4,5,6], slugify }
      const permalinkFactory = anchor?.permalink?.linkInsideHeader || anchor?.permalink?.headerLink
      if (permalinkFactory) {
        anchorOptions.permalink = permalinkFactory({ symbol: '🔗', placement: 'after' })
      } else {
        anchorOptions.permalink = true
        anchorOptions.permalinkSymbol = '🔗'
        anchorOptions.permalinkBefore = false
      }
      md.use(anchor, anchorOptions)
    } catch (_) {}

    try {
      md.use(toc, { containerClass: 'md-toc', listType: 'ul', level: [1,2,3,4,5,6] })
    } catch (_) {}

    const normalize = (raw) => String(raw || '').replace(/\r\n?/g, '\n')

    const render = (raw) => {
      const text = normalize(raw)
      renderedHtml.value = text ? md.render(text) : ''
    }

    watch(() => props.content, (val) => render(val), { immediate: true })

    return { renderedHtml }
  }
}
</script>

<style scoped>
.markdown-renderer {
  width: 100%;
  line-height: 1.6;
}
</style>

