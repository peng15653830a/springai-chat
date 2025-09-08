import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    // 配置HMR以减少服务停止后的重连请求
    hmr: {
      // 减少重连尝试的频率
      timeout: 5000,
      // 配置重连选项
      overlay: true,
      // 设置更长的重连间隔
      reconnectFails: 3
    },
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: 'dist'
  }
})