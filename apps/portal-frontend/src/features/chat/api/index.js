import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json;charset=UTF-8' }
})

api.interceptors.response.use(
  (response) => response.data,
  (error) => Promise.reject(error)
)

export const userApi = {
  login: (data) => api.post('/users/login', data),
  getProfile: (userId) => api.get(`/users/profile/${userId}`)
}

export const conversationApi = {
  getList: (userId) => api.get('/conversations', { params: { userId } }),
  create: (data) => api.post('/conversations', data),
  getDetail: (id) => api.get(`/conversations/${id}`),
  delete: (id) => api.delete(`/conversations/${id}`),
  getMessages: (id) => api.get(`/conversations/${id}/messages`)
}

export const chatApi = {}

export const modelApi = {
  getAvailableProviders: () => api.get('/models/providers'),
  getProviderModels: (providerName) => api.get(`/models/providers/${providerName}/models`),
  getAllAvailableModels: () => api.get('/models/available'),
  getModelInfo: (providerName, modelName) => api.get(`/models/providers/${providerName}/models/${modelName}`),
  checkModelAvailability: (providerName, modelName) => api.get(`/models/providers/${providerName}/models/${modelName}/available`),
  getUserDefaultModel: (userId) => api.get(`/models/users/${userId}/default`),
  getUserModelPreferences: (userId) => api.get(`/models/users/${userId}/preferences`)
}

export default api

