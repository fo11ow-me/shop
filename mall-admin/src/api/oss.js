import request from '@/utils/request'

export const upload = (file, dir = 'product') => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('dir', dir)
  return request.post('/oss/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
