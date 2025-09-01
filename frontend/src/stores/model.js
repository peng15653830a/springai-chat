import { defineStore } from 'pinia'
import { modelApi } from '../api'

export const useModelStore = defineStore('model', {
  state: () => ({
    providers: [],
    models: [],
    userPreferences: [],
    defaultModel: null,
    isLoading: false
  }),
  
  actions: {
    async loadAvailableProviders() {
      this.isLoading = true
      try {
        const response = await modelApi.getAvailableProviders()
        if (response.success) {
          this.providers = response.data || []
          return this.providers
        }
      } catch (error) {
        console.error('加载提供者列表失败:', error)
      } finally {
        this.isLoading = false
      }
    },
    
    async loadProviderModels(providerName) {
      this.isLoading = true
      try {
        const response = await modelApi.getProviderModels(providerName)
        if (response.success) {
          this.models = response.data || []
          return this.models
        }
      } catch (error) {
        console.error('加载模型列表失败:', error)
      } finally {
        this.isLoading = false
      }
    },
    
    async loadAllAvailableModels() {
      this.isLoading = true
      try {
        const response = await modelApi.getAllAvailableModels()
        if (response.success) {
          // 这里返回的是按提供者分组的模型列表
          const providersWithModels = response.data || []
          this.providers = providersWithModels
          return providersWithModels
        }
      } catch (error) {
        console.error('加载所有可用模型失败:', error)
      } finally {
        this.isLoading = false
      }
    },
    
    async loadUserDefaultModel(userId) {
      try {
        const response = await modelApi.getUserDefaultModel(userId)
        if (response.success) {
          this.defaultModel = response.data
          return this.defaultModel
        }
      } catch (error) {
        console.error('加载用户默认模型失败:', error)
      }
    },
    
    async loadUserModelPreferences(userId) {
      try {
        const response = await modelApi.getUserModelPreferences(userId)
        if (response.success) {
          this.userPreferences = response.data || []
          return this.userPreferences
        }
      } catch (error) {
        console.error('加载用户模型偏好失败:', error)
      }
    },
    
    setDefaultModel(model) {
      this.defaultModel = model
    },
    
    setUserPreferences(preferences) {
      this.userPreferences = preferences
    }
  }
})