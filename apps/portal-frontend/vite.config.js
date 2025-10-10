import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5174,
    proxy: {
      // 具体路径优先匹配
      // Novel API - 端口8083
      '/api/novel': {
        target: 'http://localhost:8083',
        changeOrigin: true
      },
      // MCP Server API - 端口8082
      '/api/mcp-server': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/mcp-server/, '/api')
      },
      // MCP Client API - 端口8081
      '/api/mcp-client': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/mcp-client/, '/api')
      },
      // Chat API - 端口8080 (默认，最后匹配)
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
