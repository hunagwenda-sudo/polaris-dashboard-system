<template>
  <div class="space-y-4">
    <!-- 昨日战报 — 自定义 HTML 条形图 -->
    <div ref="dailyChartRef" :class="[dailyFullscreen
      ? 'bg-surface-raised rounded-2xl border border-white/[0.06] overflow-hidden flex flex-col'
      : 'bg-surface-raised rounded-2xl border border-white/[0.06] overflow-hidden']">
      <div class="px-6 pt-5 pb-3 flex items-center justify-between shrink-0">
        <div>
          <h3 :class="['font-semibold text-white tracking-tight font-sans', dailyFullscreen ? 'text-[16px]' : 'text-[13px]']">昨日战报</h3>
          <p class="text-[10px] text-trust-300 mt-0.5 font-sans">昨日全员 DGMV 总览</p>
        </div>
        <div class="flex items-center gap-3">
          <div class="w-8 h-8 rounded-lg bg-accent/[0.08] border border-accent/[0.12] flex items-center justify-center">
            <svg v-bind="iconDefaults" class="w-4 h-4 text-accent"><path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6"/><path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18"/><path d="M4 22h16"/><path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20 7 22"/><path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20 17 22"/><path d="M18 2H6v7a6 6 0 0 0 12 0V2Z"/></svg>
          </div>
          <button @click="toggleExpand"
            class="w-8 h-8 rounded-lg bg-white/[0.04] border border-white/[0.06] flex items-center justify-center hover:bg-white/[0.08] transition-colors cursor-pointer"
            :title="dailyFullscreen ? '收起' : '展开'">
            <svg v-if="!dailyFullscreen" v-bind="iconDefaults" class="w-4 h-4 text-trust-300"><polyline points="15 3 21 3 21 9"/><polyline points="9 21 3 21 3 15"/><line x1="21" y1="3" x2="14" y2="10"/><line x1="3" y1="21" x2="10" y2="14"/></svg>
            <svg v-else v-bind="iconDefaults" class="w-4 h-4 text-trust-300"><polyline points="4 14 10 14 10 20"/><polyline points="20 10 14 10 14 4"/><line x1="14" y1="10" x2="21" y2="3"/><line x1="3" y1="21" x2="10" y2="14"/></svg>
          </button>
        </div>
      </div>
      <div :class="dailyFullscreen ? 'flex-1 px-4 pb-4 overflow-y-auto' : 'px-4 pb-4 max-h-[360px] overflow-y-auto'">
        <div class="space-y-1.5">
          <div v-for="d in sortedDailyData" :key="d.userId"
            class="flex items-center gap-2 group hover:bg-white/[0.02] rounded-lg px-2 py-1.5 transition-colors">
            <!-- 名字 + 徽章 -->
            <div class="flex items-center gap-1.5 shrink-0" :style="{ width: dailyFullscreen ? '140px' : '120px' }">
              <span :class="['text-gray-200 font-medium font-sans truncate', dailyFullscreen ? 'text-[13px]' : 'text-[11px]']">{{ d.name }}</span>
              <LevelBadge v-if="d.role === 'partner'" role="partner" />
              <LevelBadge v-else :level="d.estimatedLevel || d.level || 'K1'" />
            </div>
            <!-- 条形 -->
            <div class="flex-1 h-5 bg-white/[0.03] rounded-md overflow-hidden relative">
              <div class="h-full rounded-md bg-gradient-to-r from-brand/40 to-brand/90 transition-all duration-700"
                :style="{ width: barPercent(d.dgmv) + '%' }" />
            </div>
            <!-- 金额 -->
            <span :class="['font-mono tabular-nums font-semibold text-trust-300 shrink-0', dailyFullscreen ? 'text-[12px] w-[70px]' : 'text-[10px] w-[60px]']" style="text-align:right">{{ fmt(d.dgmv) }}</span>
          </div>
        </div>
        <div v-if="sortedDailyData.length === 0" class="py-10 text-center text-trust-300 text-[11px] font-sans">暂无数据</div>
      </div>
    </div>

    <!-- Hero Board -->
    <div v-if="!dailyFullscreen" class="bg-surface-raised rounded-2xl border border-white/[0.06] overflow-hidden">
      <div class="px-6 pt-5 pb-3 flex items-center justify-between">
        <div>
          <h3 class="text-[13px] font-semibold text-white tracking-tight font-sans">本周累计英雄榜</h3>
          <p class="text-[10px] text-trust-300 mt-0.5 font-sans">本周 DGMV · 含实时预估职级</p>
        </div>
        <div class="w-8 h-8 rounded-lg bg-danger/[0.08] border border-danger/[0.12] flex items-center justify-center">
          <svg v-bind="iconDefaults" class="w-4 h-4 text-danger-light"><path d="M8.5 14.5A2.5 2.5 0 0 0 11 12c0-1.38-.5-2-1-3-1.072-2.143-.224-4.054 2-6 .5 2.5 2 4.9 4 6.5 2 1.6 3 3.5 3 5.5a7 7 0 1 1-14 0c0-1.153.433-2.294 1-3a2.5 2.5 0 0 0 2.5 2.5z"/></svg>
        </div>
      </div>
      <div class="px-4 pb-4">
        <div v-for="(p, i) in heroBoard" :key="i"
          :class="['flex items-center gap-2.5 px-2 py-3 rounded-lg transition-colors duration-150', i < heroBoard.length - 1 ? 'border-b border-white/[0.04]' : '', 'hover:bg-white/[0.03]']">
          <span v-if="i < 3" :class="['w-5 h-5 rounded-md border flex items-center justify-center text-[10px] font-bold font-mono', rankColors[i]]">{{ i + 1 }}</span>
          <span v-else class="w-5 text-center text-[10px] font-medium text-trust-300 font-mono">{{ i + 1 }}</span>
          <div :class="['w-7 h-7 rounded-lg shrink-0 flex items-center justify-center text-[10px] font-semibold font-mono',
            i < 3 ? avatarColors[i] : 'bg-white/[0.05] text-trust-300']">{{ (p.name || '?')[0] }}</div>
          <span class="text-[12px] font-medium text-trust-300 min-w-[2.5rem] font-sans">{{ p.name }}</span>
          <span class="text-[12px] font-bold font-mono tabular-nums flex-1" :class="p.dgmv === '**' ? 'text-trust-300 blur-[5px] select-none' : 'text-white'">{{ p.dgmv === '**' ? fakeAmount() : fmt(p.dgmv) }}</span>
          <template v-if="p.role === 'partner'">
            <LevelBadge role="partner" />
          </template>
          <template v-else>
            <span class="inline-flex items-center px-1.5 py-0.5 rounded-md text-[9px] font-semibold bg-success/[0.08] text-success-light border border-success/[0.12] font-sans">上季 {{ p.level }}</span>
            <span class="inline-flex items-center px-1.5 py-0.5 rounded-md text-[9px] font-semibold bg-danger/[0.08] text-danger-light border border-danger/[0.12] font-sans">实时预估 {{ p.estimatedLevel }}</span>
          </template>
        </div>
        <div v-if="heroBoard.length === 0" class="py-10 text-center text-trust-300 text-[11px] font-sans">暂无数据</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { iconDefaults } from './icons.js'
import LevelBadge from './LevelBadge.vue'
import api from '../api'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const role = auth.user?.role || 'sales'

const avatarColors = ['bg-brand text-white', 'bg-brand-dark text-white', 'bg-brand-muted text-brand-light']
const rankColors = ['bg-amber-500/15 border-amber-500/25 text-amber-400', 'bg-gray-400/10 border-gray-400/20 text-gray-300', 'bg-amber-700/10 border-amber-700/20 text-amber-600']
const fmt = (n) => {
  const v = Number(n)
  if (v >= 10000) return `¥${(v / 10000).toFixed(1)}万`
  if (v > 0) return `¥${v.toFixed(0)}`
  return '¥0'
}

const fakeAmount = () => `¥${(Math.random() * 8 + 1).toFixed(1)}万`

const dailyData = ref([])
const heroBoard = ref([])
const dailyFullscreen = ref(false)
const dailyChartRef = ref(null)

function toggleExpand() {
  dailyFullscreen.value = !dailyFullscreen.value
}

defineExpose({ dailyFullscreen })

const dailyTotal = computed(() => dailyData.value.reduce((s, d) => s + Number(d.dgmv || 0), 0))

const sortedDailyData = computed(() => {
  return [...dailyData.value].sort((a, b) => Number(a.userId) - Number(b.userId))
})

// 计算条形百分比
const maxDgmv = computed(() => {
  const vals = sortedDailyData.value.map(d => Number(d.dgmv || 0))
  return Math.max(...vals, 1)
})
function barPercent(dgmv) {
  const v = Number(dgmv || 0)
  return Math.max((v / maxDgmv.value) * 100, 0)
}

onMounted(async () => {
  try {
    const [daily, weekly] = await Promise.all([
      api.get('/dashboard/leaderboard/daily', { params: { role } }),
      api.get('/dashboard/leaderboard/weekly', { params: { role } }),
    ])
    dailyData.value = daily.data || []
    heroBoard.value = weekly.data || []
  } catch { /* use empty */ }
})
</script>
