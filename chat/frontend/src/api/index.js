import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

// 用户API
export const userApi = {
  login: (data) => api.post('/users/login', data),
  getProfile: (userId) => api.get(`/users/profile/${userId}`)
}

// 对话API
export const conversationApi = {
  getList: (userId) => api.get('/conversations', { params: { userId } }),
  create: (data) => api.post('/conversations', data),
  getDetail: (id) => api.get(`/conversations/${id}`),
  delete: (id) => api.delete(`/conversations/${id}`),
  getMessages: (id) => api.get(`/conversations/${id}/messages`)
}

// 聊天API  
export const chatApi = {
  // 这里不需要API方法了，直接使用useEventSource管理SSE连接
}

// 模型API
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