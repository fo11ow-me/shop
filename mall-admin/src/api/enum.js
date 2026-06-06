import request from '@/utils/request'

export const getEnums = () => request.get('/enum/enums')
