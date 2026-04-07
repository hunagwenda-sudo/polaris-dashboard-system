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
    // 403 且没有 token 说明未登录，也跳登录页
    if (status === 403 && !localStorage.getItem('token')) {
      router.push({ name: 'login' })
    }
    const data = err.response?.data || {}
    const fallback = {
      400: '请求参数有误',
      401: '登录已过期，请重新登录',
      403: '无权限执行此操作',
      404: '请求的资源不存在',
      413: '上传内容过大',
      500: '服务器开小差了，请稍后重试',
    }
    const message = data.message || fallback[status] || '网络异常，请检查网络连接'
    return Promise.reject({ code: data.code || status, message })
  }
)

export default api
