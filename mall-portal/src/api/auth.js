import request from '@/utils/request'

export const login = (data) => request.post('/auth/login', data)
export const register = (data) => request.post('/auth/register', data)
export const getVerificationCode = async () => {
  const res = await request.get('/auth/verificationCode', { responseType: 'blob' })
  return { blob: res.data, uuid: res.headers['x-verification-uuid'] || res.headers['X-Verification-Uuid'] || '' }
}
export const logout = () => request.post('/auth/logout')
