import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    currentUser: null,
    isLoggedIn: false
  }),
  
  actions: {
    setUser(user) {
      this.currentUser = user
      this.isLoggedIn = true
      localStorage.setItem('user', JSON.stringify(user))
    },
    
    logout() {
      this.currentUser = null
      this.isLoggedIn = false
      localStorage.removeItem('user')
    },
    
    loadUserFromStorage() {
      const user = localStorage.getItem('user')
      if (user) {
        this.currentUser = JSON.parse(user)
        this.isLoggedIn = true
      }
    }
  }
})