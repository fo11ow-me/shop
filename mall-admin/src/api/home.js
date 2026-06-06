import request from '@/utils/request'
const url = '/home'

export const queryCount = () => request.get(`${url}/count`)
export const queryTrend = (days) => request.get(`${url}/trend`, { params: { days } })
export const queryCategorySales = () => request.get(`${url}/category-sales`)
