import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 15000,
  withCredentials: true,
  headers: { 'Content-Type': 'application/json;charset=UTF-8' }
})

api.interceptors.response.use(
  (resp) => resp.data,
  (error) => Promise.reject(error)
)

export default api

