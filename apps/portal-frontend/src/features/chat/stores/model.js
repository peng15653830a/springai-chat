// copied from chat/frontend/src/stores/model.js (simplified)
import { defineStore } from 'pinia'

export const useModelStore = defineStore('model', {
  state: () => ({
    availableProviders: [],
    providerModels: {},
    userDefaultModel: null
  }),
  actions: {
    setAvailableProviders(list) { this.availableProviders = list || [] },
    setProviderModels(provider, models) { this.providerModels[provider] = models || [] },
    setUserDefaultModel(m) { this.userDefaultModel = m || null }
  }
})

