import request from '@/utils/request'

export const getUserInfo = () => request.get('/user/info')
export const updateUserInfo = (data) => request.put('/user/update', data)
export const getAvatar = () => request.get('/user/avatar', { responseType: 'blob' })

export const uploadAvatar = (file) => {
  const fd = new FormData()
  fd.append('file', file)
  return request.post('/user/avatar', fd)
}
