import { ref, onUnmounted } from 'vue'

export function useImageUrl() {
  const url = ref('')
  const loading = ref(false)

  async function load(apiFn, ...args) {
    loading.value = true
    try {
      const res = await apiFn(...args)
      const old = url.value
      url.value = URL.createObjectURL(res.data)
      if (old) URL.revokeObjectURL(old)
    } catch { url.value = '' }
    finally { loading.value = false }
  }

  onUnmounted(() => {
    if (url.value) URL.revokeObjectURL(url.value)
  })

  return { url, loading, load }
}
