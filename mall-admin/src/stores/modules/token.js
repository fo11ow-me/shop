import { defineStore } from 'pinia'
import { getAuth, setAuth } from '@/utils/auth'

export const useTokenStore = defineStore('token', {
  state: () => ({
    token: getAuth().token || ''
  }),
  actions: {
    setToken(token) {
      this.token = token
      setAuth({ token })
    }
  }
})
