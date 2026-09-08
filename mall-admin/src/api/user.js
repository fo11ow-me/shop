import request from '@/utils/request'

export const add = (data) => request.post('/user', data)
export const del = (id) => request.delete(`/user/${id}`)
export const deleteBatch = (ids) => request.delete(`/user/batch/${ids}`)
export const edit = (data) => request.put('/user', data)
export const list = (params) => request.get('/user', { params })
export const queryByCode = (code) => request.get(`/user/code/${code}`)
export const reset = (data) => request.put('/user/reset', data)
export const exp = (filename) => request.get(`/user/export/${filename}`, { responseType: 'blob' })

export const getUserAvatar = (id) => request.get(`/user/avatar/${id}`, { responseType: 'blob' })

export const uploadAvatar = (file) => {
  const fd = new FormData()
  fd.append('file', file)
  return request.post('/user/avatar', fd)
}
