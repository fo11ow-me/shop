import { defineStore } from 'pinia'
import { removeAuth, getAuth, setAuth } from '@/utils/auth'
import { useTagStore } from './tag'
import { useTokenStore } from './token'
import { logout as logoutApi } from '@/api/auth'
import router from '../../router'

export const useUserStore = defineStore('user', {
  state: () => ({
    user: getAuth().user
  }),
  actions: {
    setUser(user) {
      this.user = user
      setAuth({ user: this.user })
    },
    async logout() {
      // 先通知后端将 token 加入黑名单，再清除本地状态（即使 API 失败也继续退出）
      try { await logoutApi() } catch {}
      removeAuth()
      useTokenStore().token = ''
      useTagStore().clearTag()
      router.push({ name: 'login' })
    }
  }
})
