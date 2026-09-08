import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getAuth, setAuth, removeAuth } from '@/utils/auth'
import { logout as logoutApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(getAuth().user || null)

  const isLogin = computed(() => !!user.value)

  function loginSuccess(userVal) {
    user.value = userVal
    setAuth({ user: userVal })
  }

  async function logout() {
    try { await logoutApi() } catch {}
    user.value = null
    removeAuth()
  }

  return { user, isLogin, loginSuccess, logout }
})
