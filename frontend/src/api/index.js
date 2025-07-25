import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
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
  create: (userId, data) => api.post('/conversations', data, { params: { userId } }),
  getDetail: (id) => api.get(`/conversations/${id}`),
  delete: (id) => api.delete(`/conversations/${id}`),
  getMessages: (id) => api.get(`/conversations/${id}/messages`)
}

// 聊天API
export const chatApi = {
  sendMessage: (conversationId, data) => api.post(`/chat/conversations/${conversationId}/messages`, data),
  connectSSE: (conversationId) => {
    return new EventSource(`/api/chat/stream/${conversationId}`)
  }
}

export default api