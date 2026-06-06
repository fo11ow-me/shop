import { defineStore } from 'pinia'
import { getEnums } from '@/api/enum'

export const useEnumStore = defineStore('enum', {
  state: () => ({
    enums: null,
    loaded: false
  }),
  getters: {
    getOptions: (state) => (key) => state.enums?.[key] || [],
    getLabel: (state) => (key, code) => {
      const item = state.enums?.[key]?.find(e => e.code === code)
      return item?.message || '-'
    },
    getMap: (state) => (key) => {
      const map = {}
      state.enums?.[key]?.forEach(e => { map[e.code] = e.message })
      return map
    }
  },
  actions: {
    async fetchEnums() {
      if (this.loaded) return
      const res = await getEnums()
      this.enums = res.data || {}
      this.loaded = true
    }
  }
})
