import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { getAuth, removeAuth } from '@/utils/auth'

const request = axios.create({
  baseURL: '/admin-api',
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
  config.headers['Content-Type'] = 'application/json;charset=utf-8'
  if (config.data) config.data = trimValues(config.data)
  if (config.params) config.params = trimValues(config.params)
  const token = getAuth().token
  if (token) config.headers.Authorization = 'Bearer ' + token
  return config
}, error => {
  return Promise.reject(error)
})

request.interceptors.response.use(response => {
  const res = response.data
  if (response.config.responseType === 'blob' || response.config.responseType === 'arraybuffer') {
    return response
  }
  if (res.code !== 200) {
    ElMessage({ message: res.message || '请求失败', type: 'error', duration: 5000 })
    if (res.code === 400 || res.code === 500 || res.code === 1200) {
      removeAuth()
      router.push('/login')
    }
    return Promise.reject(res.message)
  }
  return res
}, error => {
  if (error.response && error.response.status === 401) {
    removeAuth()
    router.push('/login')
    return Promise.reject(error)
  }
  ElMessage({ message: error.message || '网络错误', type: 'error', duration: 5000 })
  return Promise.reject(error)
})

export default request
