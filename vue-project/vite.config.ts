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
        target: 'http://47.109.101.70:8088/api',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }

  }
})
