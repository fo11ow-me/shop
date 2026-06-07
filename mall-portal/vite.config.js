import fs from 'fs'
import path from 'path'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

const logDir = path.resolve('../log')
if (!fs.existsSync(logDir)) {
  fs.mkdirSync(logDir, { recursive: true })
}
const logFile = path.join(logDir, 'mall-portal.log')
const stripAnsi = (str) => str.replace(/\x1b\[[0-9;]*m/g, '')
const writeLog = (level, msg) => {
  const timestamp = new Date().toISOString().replace('T', ' ').substring(0, 19)
  const text = stripAnsi(typeof msg === 'string' ? msg : msg?.message || JSON.stringify(msg))
  fs.appendFileSync(logFile, `${timestamp} ${level} ${text}\n`)
}

function logPlugin() {
  return {
    name: 'log-plugin',
    configureServer(server) {
      const logger = server.config.logger
      const { info: _info, warn: _warn, error: _error } = logger
      logger.info = (msg, opts) => { writeLog('INFO', msg); return _info.call(logger, msg, opts) }
      logger.warn = (msg, opts) => { writeLog('WARN', msg); return _warn.call(logger, msg, opts) }
      logger.error = (msg, opts) => { writeLog('ERROR', msg); return _error.call(logger, msg, opts) }
    }
  }
}

export default defineConfig({
  plugins: [vue(), logPlugin()],
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
        }
      }
    }
  },
  server: {
    port: 3001,
    proxy: {
      '/dev': {
        target: 'http://localhost:8800',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/dev/, '/portal')
      },
      '/api': {
        target: 'http://localhost:8800',
        changeOrigin: true
      }
    }
  }
})
