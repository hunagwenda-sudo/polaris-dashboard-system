<template>
  <div class="space-y-5 max-w-[1280px] mx-auto">

    <!-- 横幅图片区域 -->
    <div class="relative group rounded-2xl overflow-hidden h-[166px] bg-white/[0.03] border border-white/[0.06]">
      <img v-if="bannerBlobUrl" :src="bannerBlobUrl" alt="banner"
        class="w-full h-full object-cover" />
      <div v-else class="w-full h-full flex items-center justify-center gap-2 text-trust-400">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/>
          <polyline points="21 15 16 10 5 21"/>
        </svg>
        <span class="text-[13px] font-sans">暂无横幅图片</span>
      </div>

      <!-- Admin hover 操作 -->
      <template v-if="isAdmin">
        <div class="absolute inset-0 bg-black/0 group-hover:bg-black/40 transition-colors duration-200 flex items-center justify-center gap-3 opacity-0 group-hover:opacity-100">
          <label class="flex items-center gap-1.5 px-3 py-2 rounded-lg bg-black/60 border border-white/20 text-[12px] text-white hover:bg-black/80 cursor-pointer transition-colors font-sans">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/>
            </svg>
            {{ uploading ? '上传中...' : '上传图片' }}
            <input type="file" accept="image/*" class="hidden" @change="uploadBanner" :disabled="uploading" />
          </label>
          <button v-if="bannerBlobUrl" @click="clearBanner"
            class="flex items-center gap-1.5 px-3 py-2 rounded-lg bg-black/60 border border-white/20 text-[12px] text-red-400 hover:bg-black/80 cursor-pointer transition-colors font-sans">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4h6v2"/>
            </svg>
            移除
          </button>
        </div>
      </template>
    </div>

    <AnnouncementBanner />
    <PersonalStats />
    <ChannelBreakdown v-if="auth.user?.role === 'admin'" />
    <Leaderboards />
    <TeamBattle v-if="auth.user?.role === 'admin'" />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useAuthImage } from '../composables/useAuthImage'
import api from '../api'
import AnnouncementBanner from '../components/AnnouncementBanner.vue'
import PersonalStats from '../components/PersonalStats.vue'
import ChannelBreakdown from '../components/ChannelBreakdown.vue'
import Leaderboards from '../components/Leaderboards.vue'
import TeamBattle from '../components/TeamBattle.vue'

const auth = useAuthStore()
const isAdmin = computed(() => auth.user?.role === 'admin')

const bannerPath = ref('')
const uploading = ref(false)
const { blobUrl: bannerBlobUrl } = useAuthImage(bannerPath)

api.get('/banner').then(res => {
  if (res?.data?.imageUrl) bannerPath.value = res.data.imageUrl
}).catch(() => {})

async function uploadBanner(e) {
  const file = e.target.files?.[0]
  if (!file) return
  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', file)
    const res = await api.post('/banner/image', fd)
    if (res?.data?.imageUrl) bannerPath.value = res.data.imageUrl
  } catch (err) {
    console.error('上传失败', err)
  } finally {
    uploading.value = false
    e.target.value = ''
  }
}

async function clearBanner() {
  try {
    await api.delete('/banner/image')
    bannerPath.value = ''
  } catch { /* ignore */ }
}
</script>
