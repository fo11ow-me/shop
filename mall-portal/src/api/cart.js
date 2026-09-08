import request from '@/utils/request'

export const getCartList = () => request.get('/cart/list')
export const addToCart = (data) => request.post('/cart/add', data)
export const updateCartAmount = (data) => request.put('/cart/update', data)
export const deleteCartItem = (id) => request.delete(`/cart/delete/${id}`)
export const batchDeleteCart = (ids) => request.delete('/cart/batchDelete', { data: { ids } })
