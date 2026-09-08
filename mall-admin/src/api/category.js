import request from '@/utils/request'
const url = '/category'

export const tree = (params) => request.get(`${url}/tree`, { params })
export const all = () => request.get(`${url}/all`)
export const add = (data) => request.post(url, data)
export const edit = (data) => request.put(url, data)
export const del = (id) => request.delete(`${url}/${id}`)
