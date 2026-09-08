import { defineStore } from 'pinia'

export const useMenuStore = defineStore('menu', {
  state: () => ({
    isCollapsed: false
  }),
  actions: {
    collapseMenu() {
      this.isCollapsed = !this.isCollapsed
    }
  }
})
