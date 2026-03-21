import axios from 'axios'
import router from '../router'

const api = axios.create({
  baseURL: `${import.meta.env.VITE_API_BASE}/api`,
  timeout: 10000,
})

// 请求拦截器：注入 token
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：统一错误处理
api.interceptors.response.use(
  res => res.data,
  err => {
    const status = err.response?.status
    if (status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push({ name: 'login' })
    }
    const data = err.response?.data || {}
    return Promise.reject({ code: data.code || status, message: data.message || '请求失败' })
  }
)

export default api
