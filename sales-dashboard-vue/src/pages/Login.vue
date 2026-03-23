<template>
  <div class="min-h-screen bg-trust-900 flex items-center justify-center px-4">
    <div class="w-full max-w-sm">
      <!-- Logo -->
      <div class="text-center mb-8">
        <img src="/logo.png" alt="曙光引擎" class="w-16 h-16 mx-auto mb-4 rounded-2xl" />
        <h1 class="text-2xl font-bold text-white">曙光引擎</h1>
        <p class="text-trust-300 mt-1 text-sm tracking-widest">DAWN ENGINE</p>
      </div>

      <!-- Login Card -->
      <div class="bg-trust-800/60 backdrop-blur border border-trust-700/50 rounded-2xl p-6">
        <form @submit.prevent="handleLogin" class="space-y-5">
          <div>
            <label for="username" class="block text-sm text-trust-300 mb-1.5">用户名</label>
            <input
              id="username"
              v-model="username"
              type="text"
              autocomplete="username"
              class="w-full px-4 py-2.5 bg-trust-900/60 border border-trust-600/40 rounded-lg text-white placeholder-trust-500 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500/30 transition-colors"
              placeholder="请输入用户名"
            />
          </div>
          <div>
            <label for="password" class="block text-sm text-trust-300 mb-1.5">密码</label>
            <input
              id="password"
              v-model="password"
              type="password"
              autocomplete="current-password"
              class="w-full px-4 py-2.5 bg-trust-900/60 border border-trust-600/40 rounded-lg text-white placeholder-trust-500 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500/30 transition-colors"
              placeholder="请输入密码"
            />
          </div>
          <p v-if="error" class="text-red-400 text-sm">{{ error }}</p>
          <button
            type="submit"
            :disabled="loading"
            class="w-full py-2.5 bg-gradient-to-r from-blue-600 to-cyan-500 hover:from-blue-500 hover:to-cyan-400 text-white font-medium rounded-lg transition-all cursor-pointer disabled:opacity-50"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </button>
        </form>
        <p class="text-trust-500 text-xs text-center mt-4">首次登录密码为手机号后6位</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function handleLogin() {
  error.value = ''
  loading.value = true
  try {
    await auth.login(username.value, password.value)
    router.push('/')
  } catch (e) {
    error.value = e?.message || '登录失败，请检查用户名和密码'
  } finally {
    loading.value = false
  }
}
</script>
