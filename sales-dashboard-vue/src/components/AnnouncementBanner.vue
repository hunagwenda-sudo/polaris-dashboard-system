<template>
  <div class="relative rounded-2xl overflow-hidden border border-amber-500/20 group" style="height: 60px;">

    <!-- 背景 -->
    <div class="absolute inset-0 bg-gradient-to-r from-red-900/40 via-amber-900/30 to-red-900/40" />
    <div class="absolute inset-0 bg-[radial-gradient(ellipse_at_30%_50%,rgba(245,158,11,0.15),transparent_70%)]" />

    <!-- 内容层 -->
    <div class="relative h-full px-5 flex items-center gap-4">

      <!-- 图标 -->
      <div class="relative shrink-0">
        <div class="absolute inset-0 rounded-xl bg-amber-500/20 animate-ping-slow" />
        <div class="relative w-8 h-8 rounded-xl bg-gradient-to-br from-amber-500/30 to-red-500/20 border border-amber-500/30 flex items-center justify-center">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" class="text-amber-400 drop-shadow-[0_0_6px_rgba(245,158,11,0.6)]">
            <path d="M8.5 14.5A2.5 2.5 0 0 0 11 12c0-1.38-.5-2-1-3-1.072-2.143-.224-4.054 2-6 .5 2.5 2 4.9 4 6.5 2 1.6 3 3.5 3 5.5a7 7 0 1 1-14 0c0-1.153.433-2.294 1-3a2.5 2.5 0 0 0 2.5 2.5z"/>
          </svg>
        </div>
      </div>

      <!-- 跑马灯文字 -->
      <div class="overflow-hidden flex-1 mask-fade">
        <div ref="scrollEl" class="whitespace-nowrap inline-block">
          <span v-for="(m, i) in doubledMsgs" :key="i" class="text-[13px] font-bold tracking-wide font-sans">
            <svg class="inline-block w-3.5 h-3.5 text-amber-400 drop-shadow-[0_0_8px_rgba(245,158,11,0.4)] mr-1 -mt-0.5" viewBox="0 0 24 24" fill="currentColor"><path d="M13.5 0.67s.74 2.65.74 4.8c0 2.06-1.35 3.73-3.41 3.73-2.07 0-3.63-1.67-3.63-3.73l.03-.36C5.21 7.51 4 10.62 4 14c0 4.42 3.58 8 8 8s8-3.58 8-8C20 8.61 17.41 3.8 13.5.67z"/></svg>
            <span class="text-amber-300/90 mx-1">{{ m.highlight }}</span>
            <span class="text-trust-300">{{ m.text }}</span>
            <span class="inline-block w-24" />
          </span>
        </div>
      </div>

      <!-- TOP 徽章 -->
      <div class="shrink-0 flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-amber-500/10 border border-amber-500/20">
        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-amber-400">
          <path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6"/><path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18"/><path d="M4 22h16"/><path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20 7 22"/><path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20 17 22"/><path d="M18 2H6v7a6 6 0 0 0 12 0V2Z"/>
        </svg>
        <span class="text-[10px] font-bold text-amber-400 font-mono tracking-wider">TOP</span>
      </div>

      <!-- Admin：仅编辑文字按钮 -->
      <template v-if="isAdmin">
        <div class="absolute right-3 top-1/2 -translate-y-1/2 opacity-0 group-hover:opacity-100 transition-opacity duration-200">
          <button @click.stop="openMarqueeEdit"
            class="flex items-center gap-1 px-2.5 py-1 rounded-lg bg-black/50 border border-white/20 text-[10px] text-white/80 hover:text-white hover:bg-black/70 cursor-pointer transition-colors font-sans">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
            编辑文字
          </button>
        </div>
      </template>
    </div>
  </div>

  <!-- 跑马灯编辑弹窗 -->
  <div v-if="showMarqueeEdit" class="fixed inset-0 z-50 flex items-center justify-center bg-black/60" @click.self="showMarqueeEdit = false">
    <div class="bg-surface-raised rounded-2xl border border-white/[0.06] w-[520px] p-6 space-y-4">
      <div class="flex items-center justify-between">
        <div>
          <h3 class="text-[14px] font-semibold text-white font-sans">编辑跑马灯文字</h3>
          <p class="text-[11px] text-trust-300 mt-0.5 font-sans">自定义文案将追加在自动内容之后一起滚动</p>
        </div>
        <button @click="showMarqueeEdit = false" class="text-trust-300 hover:text-white cursor-pointer transition-colors">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
        </button>
      </div>
      <div class="space-y-2">
        <div v-for="(line, idx) in editLines" :key="idx" class="flex items-center gap-2">
          <span class="text-[10px] text-trust-300 font-mono w-4 shrink-0">{{ idx + 1 }}</span>
          <input type="text" v-model="editLines[idx]" placeholder="输入一条滚动消息..."
            class="flex-1 bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-2 text-[12px] text-white placeholder-trust-400 font-sans focus:outline-none focus:ring-2 focus:ring-brand/30 transition-colors" />
          <button v-if="editLines.length > 1" @click="editLines.splice(idx, 1)"
            class="w-7 h-7 flex items-center justify-center rounded-lg hover:bg-danger/[0.1] text-trust-400 hover:text-danger-light transition-colors cursor-pointer shrink-0">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
      </div>
      <button @click="editLines.push('')" class="flex items-center gap-1.5 text-[11px] text-brand-light hover:text-white transition-colors cursor-pointer font-sans">
        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        添加一条
      </button>
      <p v-if="marqueeError" class="text-[11px] text-danger-light font-sans">{{ marqueeError }}</p>
      <div class="flex items-center justify-between pt-1">
        <button v-if="customMarqueeText" @click="clearMarquee" class="text-[11px] text-trust-300 hover:text-danger-light transition-colors cursor-pointer font-sans">
          清除自定义内容
        </button>
        <div v-else />
        <div class="flex items-center gap-3">
          <button @click="showMarqueeEdit = false" class="px-4 py-2 rounded-lg text-[12px] text-trust-300 hover:text-white hover:bg-white/[0.04] transition-colors cursor-pointer font-sans">取消</button>
          <button @click="saveMarquee" :disabled="savingMarquee" class="px-4 py-2 rounded-lg text-[12px] font-medium text-white bg-brand hover:bg-brand/80 transition-colors cursor-pointer font-sans disabled:opacity-50">
            {{ savingMarquee ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useAuthStore } from '../stores/auth'
import api from '../api'

const auth = useAuthStore()
const isAdmin = computed(() => auth.user?.role === 'admin')

const announcement = ref({ bestUser: '', bestDgmv: 0, totalYesterday: 0 })
const customMarqueeText = ref('')

const showMarqueeEdit = ref(false)
const editLines = ref([''])
const savingMarquee = ref(false)
const marqueeError = ref('')

const autoMsgs = computed(() => {
  const a = announcement.value
  const msgs = []

  // 模板1：昨日MVP
  if (a.bestUser) {
    msgs.push({
      highlight: `🔥 势如破竹！恭喜 ${a.bestUser}`,
      text: '斩获昨日MVP，标杆已立，全员对齐 ❗'
    })
  }

  // 模板2：职级晋升达标
  if (a.nearLevelUser && a.nearLevelName) {
    msgs.push({
      highlight: `🚀恭喜 ${a.nearLevelUser}`,
      text: `本季度业绩已达 ${a.nearLevelName} 晋升线🎯继续保持，锁定晋级 ❗`
    })
  }

  // 模板3：本周周榜第一
  if (a.weekTopUser) {
    msgs.push({
      highlight: `🏆 暂列周榜首！ 恭喜 ${a.weekTopUser}`,
      text: '强势领跑本周大盘。守擂者请稳住火力🛡️下周一见真章！'
    })
  }

  // 兜底：没有数据时显示默认
  if (msgs.length === 0) {
    msgs.push({ highlight: '全员', text: '冲冲冲！今日业绩等你来刷新！' })
  }

  return msgs
})

const customMsgs = computed(() => {
  if (!customMarqueeText.value.trim()) return []
  return customMarqueeText.value.split('\n')
    .map(s => s.trim()).filter(Boolean)
    .map(s => ({ highlight: '', text: s }))
})

const msgs = computed(() => [...autoMsgs.value, ...customMsgs.value])
const doubledMsgs = computed(() => [...msgs.value, ...msgs.value])

const scrollEl = ref(null)
let pos = 0, raf = null

function openMarqueeEdit() {
  editLines.value = customMarqueeText.value.trim()
    ? customMarqueeText.value.split('\n').map(s => s.trim()).filter(Boolean)
    : ['']
  marqueeError.value = ''
  showMarqueeEdit.value = true
}

async function saveMarquee() {
  marqueeError.value = ''
  const text = editLines.value.map(s => s.trim()).filter(Boolean).join('\n')
  savingMarquee.value = true
  try {
    await api.put('/banner/marquee', { text })
    customMarqueeText.value = text
    showMarqueeEdit.value = false
  } catch (e) {
    marqueeError.value = e?.message || '保存失败'
  } finally {
    savingMarquee.value = false
  }
}

async function clearMarquee() {
  savingMarquee.value = true
  try {
    await api.put('/banner/marquee', { text: '' })
    customMarqueeText.value = ''
    showMarqueeEdit.value = false
  } catch { /* ignore */ } finally {
    savingMarquee.value = false
  }
}

onMounted(async () => {
  try {
    const [annRes, marqueeRes] = await Promise.all([
      api.get('/dashboard/announcement'),
      api.get('/banner/marquee'),
    ])
    announcement.value = annRes.data
    customMarqueeText.value = marqueeRes?.data?.text || ''
  } catch { /* use defaults */ }

  const el = scrollEl.value
  if (!el) return
  const animate = () => {
    pos -= 0.6
    if (Math.abs(pos) >= el.scrollWidth / 2) pos = 0
    el.style.transform = `translateX(${pos}px)`
    raf = requestAnimationFrame(animate)
  }
  raf = requestAnimationFrame(animate)
})
onUnmounted(() => { if (raf) cancelAnimationFrame(raf) })
</script>
