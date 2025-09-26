import axios from 'axios'

const mcp = axios.create({
  baseURL: '/mcp-api',
  timeout: 15000,
  withCredentials: true,
  headers: { 'Content-Type': 'application/json;charset=UTF-8' }
})

mcp.interceptors.response.use(
  (resp) => resp.data,
  (error) => Promise.reject(error)
)

export default mcp

