import request from '@/utils/request'
const url = '/order'

export const list = (params) => request.get(`${url}/list`, { params })
export const detail = (id) => request.get(`${url}/detail/${id}`)
export const deliver = (id) => request.put(`${url}/deliver/${id}`)
export const cancel = (id) => request.put(`${url}/cancel/${id}`)
export const deleteBatch = (ids) => request.delete(`${url}/batch/${ids}`)
export const batchDeliver = (ids) => request.put(`${url}/batch-deliver/${ids}`)
