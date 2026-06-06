import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUserInfo, updateUserInfo, uploadAvatar } from '@/api'

export const useUserStore = defineStore('user', () => {
  const user = ref(null)
  const loading = ref(false)

  async function fetchUserInfo() {
    loading.value = true
    try {
      const res = await getUserInfo()
      user.value = res.data || null
    } catch { /* ignore */ }
    finally { loading.value = false }
  }

  async function updateInfo(data) {
    await updateUserInfo(data)
    Object.assign(user.value, data)
  }

  async function updateUserAvatar(file) {
    const res = await uploadAvatar(file)
    if (user.value) {
      user.value.avatar = res.data?.avatar || ''
    }
    return res
  }

  function clear() {
    user.value = null
  }

  return { user, loading, fetchUserInfo, updateInfo, updateUserAvatar, clear }
})
