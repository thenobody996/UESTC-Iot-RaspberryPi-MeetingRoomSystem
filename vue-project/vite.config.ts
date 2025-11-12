import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueJsx(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': '/src'
    },
  },
  server: {
    port: 8089, // 将端口改为 8089
    host: '0.0.0.0', // 可选：允许外部访问
    proxy: {
      '/api': {
        target: 'http://localhost:8088',
        changeOrigin: true,
        secure: false,
        configure: (proxy, options) => {
          proxy.on('proxyReq', (proxyReq, req, res) => {
            console.log('🚀 代理请求:', req.method, req.url, '->', proxyReq.protocol, '//', proxyReq.host, proxyReq.path)
          })
          proxy.on('proxyRes', (proxyRes, req, res) => {
            console.log('🚀 代理响应:', proxyRes.statusCode, req.url)
          })
          proxy.on('error', (err, req, res) => {
            console.log('🚀 代理错误:', err.message)
          })
        },
        rewrite: (path) => path.replace(/^\/api/, '/api')
      }
    }

  }
})
