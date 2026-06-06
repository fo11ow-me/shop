import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getAuth, removeAuth } from '@/utils/auth'

const request = axios.create({
  baseURL: '/dev',
  timeout: 10000
})

function trimValues(obj) {
  if (typeof obj === 'string') return obj.trim()
  if (Array.isArray(obj)) return obj.map(trimValues)
  if (obj !== null && typeof obj === 'object' && !(obj instanceof FormData)) {
    for (const key in obj) { obj[key] = trimValues(obj[key]) }
  }
  return obj
}

request.interceptors.request.use(config => {
  if (config.data) config.data = trimValues(config.data)
  if (config.params) config.params = trimValues(config.params)
  const token = getAuth().token
  if (token) config.headers.Authorization = 'Bearer ' + token
  return config
})

request.interceptors.response.use(
  res => {
    if (res.config.responseType === 'blob') return res
    if (res.data.code !== 200) {
      if (res.data.code === 1200) {
        removeAuth()
        window.location.href = '/#/login'
      }
      ElMessage.error(res.data.message || '请求失败')
      return Promise.reject(res.data)
    }
    return res.data
  },
  err => {
    if (err.response && err.response.status === 401) {
      removeAuth()
      window.location.href = '/#/login'
      return Promise.reject(err)
    }
    ElMessage.error('网络错误')
    return Promise.reject(err)
  }
)

export default request
