import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import UnoCSS from 'unocss/vite'

export default defineConfig({
  base: '/admin/',
  plugins: [vue(), UnoCSS()],
  server: {
    host: 'localhost',
    port: 3016,
    proxy: {
      '/dev': {
        target: 'http://localhost:8800',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/dev/, '/admin')
      },
      '/api': {
        target: 'http://localhost:8800',
        changeOrigin: true
      }
    }
  },
  resolve: {
    alias: {
      '@': '/src'
    }
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          'element-plus': ['element-plus'],
          'echarts': ['echarts'],
        }
      }
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        api: 'modern-compiler'
      }
    }
  }
})
