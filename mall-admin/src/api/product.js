import request from '@/utils/request'
const url = '/product'

export const list = (params) => request.get(`${url}/list`, { params })
export const add = (data) => request.post(url, data)
export const edit = (data) => request.put(url, data)
export const del = (id) => request.delete(`${url}/${id}`)
export const toggleStatus = (id) => request.put(`${url}/status/${id}`)

export const getProductImage = (key) => request.get(`/product/img?key=${key}`, { responseType: 'blob' })
