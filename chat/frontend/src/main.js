import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'

const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

// 监听窗口关闭事件，优雅处理连接断开
window.addEventListener('beforeunload', () => {
  // 关闭所有EventSource连接
  if (window.eventSources) {
    window.eventSources.forEach(es => {
      try {
        es.close()
      } catch (e) {
        // 忽略关闭错误
      }
    })
  }
})

// 监听页面可见性变化，当页面隐藏时减少重连
document.addEventListener('visibilitychange', () => {
  if (document.hidden) {
    // 页面隐藏时，暂停重连尝试
    if (window.pauseReconnect) {
      window.pauseReconnect()
    }
  }
})

app.mount('#app')