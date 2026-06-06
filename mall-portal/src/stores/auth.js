import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getAuth, setAuth, removeAuth } from '@/utils/auth'
import { logout as logoutApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(getAuth().token || '')
  const user = ref(getAuth().user || null)

  const isLogin = computed(() => !!token.value)

  function loginSuccess(tokenVal, userVal) {
    token.value = tokenVal
    user.value = userVal
    setAuth({ token: tokenVal, user: userVal })
  }

  async function logout() {
    // 先通知后端将 token 加入黑名单，再清除本地状态（即使 API 失败也继续退出）
    try { await logoutApi() } catch {}
    token.value = ''
    user.value = null
    removeAuth()
  }

  return { token, user, isLogin, loginSuccess, logout }
})
