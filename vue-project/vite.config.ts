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
      // 1) API calls keep the /api prefix so backend controllers mapped under /api/** work as-is
      '/api': {
        target: 'http://localhost:8088',
        changeOrigin: true,
        secure: false,
        configure: (proxy) => {
          proxy.on('proxyReq', (proxyReq, req) => {
            console.log('🚀 代理请求(api):', req.method, req.url, '->', proxyReq.protocol, '//', proxyReq.host, proxyReq.path)
          })
          proxy.on('proxyRes', (proxyRes, req) => {
            console.log('🚀 代理响应(api):', proxyRes.statusCode, req.url)
          })
          proxy.on('error', (err) => {
            console.log('🚀 代理错误(api):', err.message)
          })
        }
      },

      // 2) Static profile images are served by backend at /profile/** (note: no /api prefix)
      //    Proxy '/profile' so client requests to /profile/<file> are forwarded to backend /profile/<file>
      '/profile': {
        target: 'http://localhost:8088',
        changeOrigin: true,
        secure: false,
        configure: (proxy) => {
          proxy.on('proxyReq', (proxyReq, req) => {
            console.log('🚀 代理请求(profile):', req.method, req.url, '->', proxyReq.protocol, '//', proxyReq.host, proxyReq.path)
          })
          proxy.on('proxyRes', (proxyRes, req) => {
            console.log('🚀 代理响应(profile):', proxyRes.statusCode, req.url)
          })
          proxy.on('error', (err) => {
            console.log('🚀 代理错误(profile):', err.message)
          })
        }
      }
    }

  }
})
