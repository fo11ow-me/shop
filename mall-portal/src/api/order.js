import request from '@/utils/request'

export const createOrderFromCart = (data) => request.post('/order/create', data)
export const buyNow = (data) => request.post('/order/buyNow', data)
export const getOrderList = (params) => request.get('/order/list', { params })
export const getOrderDetail = (id) => request.get(`/order/detail/${id}`)
export const payOrder = (id, payMethod) => request.put(`/order/pay/${id}`, { payMethod })
export const cancelOrder = (id) => request.put(`/order/cancel/${id}`)
export const receiptOrder = (id) => request.put(`/order/receipt/${id}`)
export const updateRecipient = (id, data) => request.put(`/order/recipient/${id}`, data)
export const deleteOrder = (id) => request.delete(`/order/delete/${id}`)
