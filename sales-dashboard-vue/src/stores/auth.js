import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const role = computed(() => user.value?.role || '')

  async function login(username, password) {
    const res = await api.post('/auth/login', { username, password })
    token.value = res.data.token
    user.value = {
      id: res.data.userId,
      username: res.data.username,
      name: res.data.name,
      role: res.data.role,
      level: res.data.level,
      teamId: res.data.teamId,
      groupId: res.data.groupId,
      avatar: res.data.avatar,
      passwordChanged: res.data.passwordChanged,
    }
    localStorage.setItem('token', token.value)
    localStorage.setItem('user', JSON.stringify(user.value))
    // 拉取实时职级
    fetchEstimatedLevel()
    return user.value
  }

  async function fetchMe() {
    const res = await api.get('/auth/me')
    user.value = res.data
    localStorage.setItem('user', JSON.stringify(user.value))
    // 拉取实时职级
    fetchEstimatedLevel()
  }

  async function fetchEstimatedLevel() {
    try {
      const res = await api.get('/dashboard/personal-level')
      if (res.data?.currentLevel && user.value) {
        user.value = { ...user.value, estimatedLevel: res.data.currentLevel }
        localStorage.setItem('user', JSON.stringify(user.value))
      }
    } catch { /* ignore for non-sales roles */ }
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  function setUser(u) {
    user.value = u
    localStorage.setItem('user', JSON.stringify(u))
  }

  return { token, user, isLoggedIn, role, login, fetchMe, fetchEstimatedLevel, logout, setUser }
})
