import request from '@/utils/request'

export const getHomeData = () => request.get('/product/home')
export const getProductsByCategory = (categoryId, params) => request.get(`/product/category/${categoryId}`, { params })
export const searchProducts = (params) => request.get('/product/search', { params })
export const getProductDetail = (id) => request.get(`/product/detail/${id}`)
export const getCategories = () => request.get('/product/categories')

export const getImageUrl = (key) => `/dev/product/img?key=${key}`
