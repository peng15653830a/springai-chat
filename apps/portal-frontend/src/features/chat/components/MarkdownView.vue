<template>
  <div class="markdown" v-html="content"></div>
</template>

<script>
export default {
  name: 'MarkdownView',
  props: { content: { type: String, default: '' }, enableTypewriter: { type: Boolean, default: false }, anchorPrefix: { type: String, default: '' } }
}
</script>

<style scoped>
.markdown { white-space: pre-wrap; }
</style>

