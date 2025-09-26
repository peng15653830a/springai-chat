import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    currentUser: null,
    isLoggedIn: false
  }),
  actions: {
    setUser(user) {
      this.currentUser = user
      this.isLoggedIn = true
      localStorage.setItem('portal_user', JSON.stringify(user))
      // 兼容旧 Chat 存储键，便于迁移阶段直接复用
      localStorage.setItem('user', JSON.stringify(user))
    },
    logout() {
      this.currentUser = null
      this.isLoggedIn = false
      localStorage.removeItem('portal_user')
      localStorage.removeItem('user')
    },
    loadFromStorage() {
      const raw = localStorage.getItem('portal_user')
      if (raw) {
        try {
          const user = JSON.parse(raw)
          this.currentUser = user
          this.isLoggedIn = true
        } catch {}
      }
    }
  }
})
