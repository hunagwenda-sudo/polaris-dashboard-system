<template>
  <div class="min-h-screen flex items-center justify-center bg-trust-900 px-4">
    <div class="w-full max-w-sm bg-surface-raised rounded-2xl border border-white/[0.06] p-8">
      <div class="text-center mb-6">
        <div class="w-12 h-12 rounded-xl bg-brand/[0.1] border border-brand/[0.15] flex items-center justify-center mx-auto mb-3">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="w-5 h-5 text-brand-light"><rect width="18" height="11" x="3" y="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
        </div>
        <h2 class="text-lg font-semibold text-white font-sans">修改密码</h2>
        <p class="text-[11px] text-trust-300 mt-1 font-sans">首次登录请修改默认密码</p>
      </div>

      <div v-if="error" class="mb-4 px-3 py-2 rounded-lg bg-danger/[0.08] border border-danger/[0.12] text-[11px] text-danger-light font-sans">{{ error }}</div>

      <div class="space-y-4">
        <div>
          <label class="text-[10px] text-trust-300 font-sans block mb-1">新密码</label>
          <input v-model="newPwd" type="password" placeholder="请输入新密码" class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-4 py-2.5 text-[13px] text-white placeholder-trust-400 font-sans focus:outline-none focus:ring-2 focus:ring-brand/30" />
        </div>
        <div>
          <label class="text-[10px] text-trust-300 font-sans block mb-1">确认新密码</label>
          <input v-model="confirmPwd" type="password" placeholder="再次输入新密码" @keyup.enter="submit" class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-4 py-2.5 text-[13px] text-white placeholder-trust-400 font-sans focus:outline-none focus:ring-2 focus:ring-brand/30" />
        </div>
        <button @click="submit" :disabled="loading" class="w-full py-2.5 rounded-lg bg-brand hover:bg-brand-light text-white text-[13px] font-semibold font-sans transition-colors cursor-pointer disabled:opacity-50">
          {{ loading ? '提交中...' : '确认修改' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import api from '../api'

const router = useRouter()
const auth = useAuthStore()
const newPwd = ref('')
const confirmPwd = ref('')
const error = ref('')
const loading = ref(false)

async function submit() {
  error.value = ''
  if (!newPwd.value || newPwd.value.length < 6) { error.value = '密码至少6位'; return }
  if (newPwd.value !== confirmPwd.value) { error.value = '两次密码不一致'; return }
  loading.value = true
  try {
    await api.put('/auth/profile', { oldPassword: null, newPassword: newPwd.value, forceChange: true })
    auth.setUser({ ...auth.user, passwordChanged: true })
    router.replace({ name: 'dashboard' })
  } catch (e) {
    error.value = e.response?.data?.message || '修改失败'
  } finally {
    loading.value = false
  }
}
</script>
