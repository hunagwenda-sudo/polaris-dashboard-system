<template>
  <header class="h-16 bg-trust-900/90 backdrop-blur-xl border-b border-white/[0.06] shrink-0 z-10 relative">
    <div class="flex items-center justify-between px-6 h-full">

      <!-- 左：logo + 标题 -->
      <div class="flex items-center gap-2.5">
        <div class="w-8 h-8 rounded-xl bg-gradient-to-br from-brand to-brand-light flex items-center justify-center shadow-lg shadow-brand/20">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/>
            <polyline points="17 6 23 6 23 12"/>
          </svg>
        </div>
        <div>
          <h1 class="text-[17px] font-bold text-white tracking-tight font-sans leading-none">销售大盘</h1>
          <p class="text-[13px] text-brand-light font-mono font-semibold mt-0.5 leading-none">{{ quarterLabel }}</p>
        </div>
      </div>

      <!-- 右：状态 + 角色 + 用户 + 退出 -->
      <div class="flex items-center gap-3">
        <div class="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-success/[0.08] border border-success/[0.12]">
          <div class="w-1.5 h-1.5 rounded-full bg-success animate-pulse" />
          <span class="text-[12px] font-medium text-success font-sans">已认证</span>
        </div>
        <span class="inline-flex items-center px-3 py-1.5 rounded-xl text-[12px] font-semibold bg-brand/[0.1] text-brand-light border border-brand/[0.15] font-sans">
          {{ roleLabel }}
        </span>
        <div class="w-px h-5 bg-white/[0.08]" />
        <div class="flex items-center gap-2.5">
          <div class="relative">
            <img v-if="avatarBlobUrl" :src="avatarBlobUrl" :alt="auth.user?.name"
              class="w-8 h-8 rounded-xl object-cover ring-2 ring-white/[0.08]" />
            <div v-else
              class="w-8 h-8 rounded-xl bg-gradient-to-br from-brand to-brand-light flex items-center justify-center text-white text-[13px] font-bold font-mono ring-2 ring-white/[0.08]">
              {{ avatar }}
            </div>
            <div class="w-2 h-2 rounded-full bg-success border-2 border-trust-900 absolute -bottom-0.5 -right-0.5" />
          </div>
          <div class="hidden sm:block">
            <div class="flex items-center gap-1.5">
              <p class="text-[13px] font-semibold text-white font-sans leading-none">{{ auth.user?.name || auth.user?.username }}</p>
              <LevelBadge :level="auth.user?.estimatedLevel || auth.user?.level" :role="auth.user?.role" />
            </div>
            <p class="text-[11px] text-trust-400 font-mono mt-0.5 leading-none">@{{ auth.user?.username }}</p>
          </div>
        </div>
        <button @click="handleLogout"
          class="w-8 h-8 rounded-xl flex items-center justify-center text-trust-400 hover:text-white hover:bg-white/[0.06] transition-colors cursor-pointer"
          title="退出登录">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
            <polyline points="16 17 21 12 16 7"/>
            <line x1="21" y1="12" x2="9" y2="12"/>
          </svg>
        </button>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useAuthImage } from '../composables/useAuthImage'
import LevelBadge from './LevelBadge.vue'

const router = useRouter()
const auth = useAuthStore()

const roleMap = { admin: '管理员', partner: '合伙人', sales: '运营', service: '客服' }
const roleLabel = computed(() => roleMap[auth.user?.role] || auth.user?.role || '未知')
const avatar = computed(() => (auth.user?.name || '用')[0])

const avatarPath = computed(() => auth.user?.avatar || '')
const { blobUrl: avatarBlobUrl } = useAuthImage(avatarPath)

const now = new Date()
const q = Math.ceil((now.getMonth() + 1) / 3)
const quarterLabel = `${now.getFullYear()} Q${q}`

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>
