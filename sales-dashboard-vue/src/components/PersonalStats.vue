<template>
  <div class="space-y-4">

    <!-- ═══════════════════════════════════════════════════════
         管理员专属顶部区域：大主卡 + 3 个指标卡
    ════════════════════════════════════════════════════════════ -->
    <template v-if="isAdmin">
      <div class="grid grid-cols-3 gap-4">

        <!-- 主卡：全公司季度进度 (占 2 列) -->
        <div class="col-span-2 bg-surface-raised rounded-2xl border border-brand/[0.12] p-6 relative overflow-hidden">
          <!-- 背景光晕 -->
          <div class="absolute inset-0 bg-gradient-to-br from-brand/[0.07] via-transparent to-transparent pointer-events-none" />
          <div class="absolute -right-16 -top-16 w-64 h-64 rounded-full bg-brand/[0.04] blur-3xl pointer-events-none" />

          <div class="relative z-10">
            <!-- 顶部：标题 + 季度标签 -->
            <div class="flex items-start justify-between mb-5">
              <div>
                <div class="flex items-center gap-2 mb-1">
                  <div class="w-1.5 h-5 rounded-full bg-gradient-to-b from-brand to-brand-light" />
                  <span class="text-[13px] font-semibold text-white font-sans">全公司季度业绩</span>
                </div>
                <p class="text-[12px] text-trust-300 font-sans pl-3.5">
                  2026 · Q{{ currentQuarter }} · 实时数据
                </p>
              </div>
              <div class="flex items-center gap-2">
                <span class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-brand/[0.1] border border-brand/[0.2] text-[11px] font-semibold text-brand-light font-sans">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                  </svg>
                  管理员
                </span>
                <span :class="['inline-flex items-center gap-1 px-2.5 py-1.5 rounded-xl text-[11px] font-semibold font-mono border', rateStatusClass]">
                  {{ rateStatusLabel }}
                </span>
              </div>
            </div>

            <!-- 核心数字区 -->
            <div class="flex items-end gap-6 mb-6">
              <div>
                <p class="text-[11px] text-trust-300 font-sans mb-1">当前累计 DGMV</p>
                <div class="flex items-baseline gap-1.5">
                  <span class="text-[48px] font-extrabold text-white tracking-tight leading-none font-mono tabular-nums">{{ fmtWan(stats.totalDgmv) }}</span>
                  <span class="text-[18px] font-semibold text-trust-300 font-sans mb-1">万</span>
                </div>
              </div>
              <div class="pb-1.5 text-trust-400 font-sans text-[20px]">/</div>
              <div class="pb-1">
                <p class="text-[11px] text-trust-300 font-sans mb-1">季度目标</p>
                <div class="flex items-baseline gap-1">
                  <span class="text-[28px] font-bold text-trust-200 font-mono tabular-nums">{{ fmtWan(stats.targetDgmv) }}</span>
                  <span class="text-[13px] text-trust-300 font-sans">万</span>
                </div>
              </div>
              <div class="ml-auto pb-1 text-right">
                <p class="text-[11px] text-trust-300 font-sans mb-1">完成率</p>
                <div class="flex items-baseline gap-0.5 justify-end">
                  <span :class="['text-[40px] font-extrabold font-mono tabular-nums leading-none', rateColor]">{{ animatedRateDisplay }}</span>
                  <span :class="['text-[16px] font-bold font-mono mb-0.5', rateColor]">%</span>
                </div>
              </div>
            </div>

            <!-- 进度条 -->
            <div class="space-y-2">
              <div class="w-full h-4 bg-white/[0.04] rounded-full overflow-hidden relative border border-white/[0.03]">
                <div class="absolute inset-0 rounded-full" style="box-shadow: inset 0 1px 3px rgba(0,0,0,0.4)" />
                <div class="h-full rounded-full relative overflow-hidden transition-all duration-[2000ms] ease-out"
                     :style="{ width: Math.min(animatedRate, 100) + '%' }">
                  <div class="absolute inset-0 bg-gradient-to-r from-brand via-blue-400 to-brand-light" />
                  <div class="absolute inset-0 bg-gradient-to-r from-transparent via-white/20 to-transparent animate-shimmer" />
                  <div class="absolute inset-x-0 top-0 h-[45%] bg-gradient-to-b from-white/25 to-transparent rounded-full" />
                  <div class="absolute right-0 top-1/2 -translate-y-1/2 w-4 h-4 rounded-full bg-blue-300 shadow-[0_0_12px_rgba(147,197,253,0.8)] animate-pulse-glow" />
                </div>
              </div>
              <div class="flex items-center justify-between">
                <span class="text-[11px] text-trust-300 font-sans">
                  距目标还差
                  <span class="text-accent font-bold font-mono ml-1">¥{{ fmtWan(stats.gap) }} 万</span>
                </span>
                <span class="text-[11px] text-trust-300 font-sans">
                  剩余 <span class="text-white font-semibold font-mono">{{ stats.daysLeft }}</span> 天
                  · 日均需完成 <span class="text-danger-light font-bold font-mono">¥{{ fmtWan(stats.dailyNeeded) }} 万</span>
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧 3 个指标卡 (1 列，纵向排列) -->
        <div class="flex flex-col gap-4">
          <div v-for="c in adminSideCards" :key="c.label"
            class="flex-1 bg-surface-raised rounded-2xl border border-white/[0.06] px-5 py-4 relative overflow-hidden group hover:border-white/[0.1] hover:-translate-y-0.5 transition-all duration-200 cursor-pointer">
            <div class="relative z-10 flex items-center justify-between h-full">
              <div>
                <p class="text-[11px] font-semibold text-trust-300 font-sans mb-2">{{ c.label }}</p>
                <div class="flex items-baseline gap-1">
                  <span :class="['text-[30px] font-extrabold tracking-tight leading-none font-mono tabular-nums', c.valueColor]">{{ c.value }}</span>
                  <span class="text-[13px] font-medium text-trust-300 font-sans">{{ c.unit }}</span>
                </div>
                <p class="text-[11px] text-trust-300 mt-1.5 font-sans">{{ c.sub }}</p>
              </div>
              <div :class="['w-10 h-10 rounded-xl flex items-center justify-center border shrink-0', c.iconBg, c.iconBorder]">
                <svg v-bind="iconDefaults" :class="['w-5 h-5', c.iconColor]" v-html="c.iconPath" />
              </div>
            </div>
            <!-- 右下角 sparkline -->
            <div class="absolute bottom-2 right-2 opacity-30 group-hover:opacity-50 transition-opacity duration-300">
              <svg :width="56" :height="22" :viewBox="'0 0 56 22'">
                <defs>
                  <linearGradient :id="'adm' + c.sColor.replace('#','')" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" :stop-color="c.sColor" stop-opacity="0.25" />
                    <stop offset="100%" :stop-color="c.sColor" stop-opacity="0" />
                  </linearGradient>
                </defs>
                <path :d="sparkAreaSm(c.spark)" :fill="'url(#adm' + c.sColor.replace('#','') + ')'" />
                <path :d="sparkLineSm(c.spark)" fill="none" :stroke="c.sColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
              </svg>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- ═══════════════════════════════════════════════════════
         非管理员：原有 4 卡布局
    ════════════════════════════════════════════════════════════ -->
    <template v-else>
      <div class="grid grid-cols-4 gap-4">
        <div v-for="c in cards" :key="c.label"
          class="bg-surface-raised rounded-2xl border border-white/[0.06] p-5 relative overflow-hidden group hover:border-brand/[0.15] hover:-translate-y-0.5 hover:shadow-lg hover:shadow-brand/[0.06] transition-all duration-200 cursor-pointer">
          <div class="relative z-10">
            <div :class="['w-9 h-9 rounded-xl flex items-center justify-center mb-4 border', c.iconBg, c.iconBorder]">
              <svg v-bind="iconDefaults" :class="['w-4 h-4', c.iconColor]" v-html="c.iconPath" />
            </div>
            <p class="text-[11px] font-semibold text-trust-300 uppercase tracking-[0.08em] mb-2 font-sans">{{ c.label }}</p>
            <div class="flex items-baseline gap-1">
              <span class="text-[30px] font-extrabold text-white tracking-tight leading-none font-mono">{{ c.value }}</span>
              <span class="text-[13px] font-medium text-trust-300 font-sans">{{ c.unit }}</span>
            </div>
            <p class="text-[11px] text-trust-300 mt-1.5 font-sans">{{ c.sub }}</p>
          </div>
          <div class="absolute bottom-3 right-3 opacity-40 group-hover:opacity-70 transition-opacity duration-300">
            <svg :width="68" :height="26" :viewBox="'0 0 68 26'">
              <defs>
                <linearGradient :id="'sp' + c.sColor.replace('#','')" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" :stop-color="c.sColor" stop-opacity="0.2" />
                  <stop offset="100%" :stop-color="c.sColor" stop-opacity="0" />
                </linearGradient>
              </defs>
              <path :d="sparkArea(c.spark)" :fill="'url(#sp' + c.sColor.replace('#','') + ')'" />
              <path :d="sparkLine(c.spark)" fill="none" :stroke="c.sColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" opacity="0.6" />
            </svg>
          </div>
        </div>
      </div>

      <!-- Level Progress -->
      <div v-if="!isPartner" :class="['bg-surface-raised rounded-2xl border px-6 py-5 relative overflow-hidden transition-all duration-500', levelBorder]">
        <div :class="['absolute left-0 top-0 h-full pointer-events-none transition-all duration-700', levelBgGlow]" :style="{ width: animatedPct + '%' }" />
        <div class="relative z-10">
          <div class="flex items-center justify-between mb-1">
            <div class="flex items-center gap-2">
              <span class="text-[13px] font-semibold text-trust-300 uppercase tracking-[0.08em] font-sans">职级晋升进度</span>
              <span :class="['inline-flex items-center px-2 py-0.5 rounded-md text-[13px] font-bold border font-mono', levelBadgeClass]">{{ levelInfo.currentLevel }} → {{ levelInfo.nextLevel }}</span>
              <span v-if="levelInfo.lastLevel && levelInfo.lastLevel !== 'K1'" class="inline-flex items-center px-1.5 py-0.5 rounded-md text-[12px] font-semibold bg-white/[0.04] text-trust-300 border border-white/[0.06] font-sans">上季 {{ levelInfo.lastLevel }}</span>
            </div>
            <div class="flex items-baseline gap-1">
              <span :class="['text-[32px] font-extrabold font-mono tabular-nums leading-none', levelPctColor]">{{ animatedPctDisplay }}</span>
              <span class="text-[14px] font-medium text-trust-300 font-mono">%</span>
            </div>
          </div>
          <div class="relative mt-3 mb-1">
            <div class="flex justify-between text-[12px] font-mono font-bold px-0.5">
              <span :class="levelTextColor">{{ levelInfo.currentLevel }}</span>
              <span class="text-trust-300">{{ levelInfo.nextLevel }}</span>
            </div>
          </div>
          <div class="w-full h-5 bg-white/[0.04] rounded-full overflow-hidden relative border border-white/[0.03]">
            <div class="absolute inset-0 rounded-full" style="box-shadow: inset 0 1px 3px rgba(0,0,0,0.3)" />
            <div class="h-full rounded-full relative overflow-hidden transition-all duration-[2000ms] ease-out" :style="{ width: animatedPct + '%' }">
              <div :class="['absolute inset-0', levelBarGradient]" />
              <div class="absolute inset-0 bg-gradient-to-r from-transparent via-white/25 to-transparent animate-shimmer" />
              <div class="absolute inset-x-0 top-0 h-[40%] bg-gradient-to-b from-white/20 to-transparent rounded-full" />
              <div :class="['absolute right-0 top-1/2 -translate-y-1/2 w-3.5 h-3.5 rounded-full animate-pulse-glow', levelDotClass]" />
            </div>
          </div>
          <div class="flex items-center justify-between mt-4 gap-3">
            <div class="flex items-center gap-4">
              <div class="flex items-center gap-1.5">
                <div :class="['w-2.5 h-2.5 rounded-full animate-pulse', levelDotBg]" />
                <span class="text-[13px] text-trust-300 font-sans">季度累计</span>
                <span :class="['text-[15px] font-bold font-mono', levelAccentColor]">¥{{ fmtWan(levelInfo.totalDgmv) }}万</span>
              </div>
              <div class="flex items-center gap-1.5">
                <div class="w-2.5 h-2.5 rounded-full bg-accent/60" />
                <span class="text-[13px] text-trust-300 font-sans">距{{ levelInfo.nextLevel }}还差</span>
                <span class="text-[15px] font-bold text-accent font-mono">¥{{ fmtWan(levelInfo.gap) }}万</span>
              </div>
            </div>
            <p class="text-[13px] text-trust-300 font-sans">
              实时预估 <span :class="['font-semibold font-mono', levelTextColor]">{{ levelInfo.estimatedLevel }}</span>
            </p>
          </div>
        </div>
      </div>
    </template>

  </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive } from 'vue'
import { iconDefaults } from './icons.js'
import api from '../api'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const isAdmin = computed(() => authStore.role === 'admin')
const isPartner = computed(() => authStore.role === 'partner')

const stats = reactive({
  targetDgmv: 0, totalDgmv: 0, gap: 0, daysLeft: 0, elapsedDays: 0, quarterTotalDays: 91,
  dailyNeeded: 0, completionRate: 0, scope: 'personal'
})
const levelInfo = reactive({
  lastLevel: 'K1', currentLevel: 'K1', nextLevel: 'K2', totalDgmv: 0, currentThreshold: 0,
  nextThreshold: 50000, gap: 50000, daysLeft: 0, dailyNeeded: 0, completionRate: 0,
  estimatedLevel: 'K1', allLevels: ['K1', 'K2']
})

const targetPct = ref(0)
const animatedPct = ref(0)
const animatedRate = ref(0)
const animatedPctDisplay = computed(() => animatedPct.value.toFixed(1))
const animatedRateDisplay = computed(() => animatedRate.value.toFixed(1))
const fmtWan = (v) => (Number(v) / 10000).toFixed(1)

const currentQuarter = computed(() => Math.ceil((new Date().getMonth() + 1) / 3))

onMounted(async () => {
  try {
    const requests = [api.get('/dashboard/personal')]
    if (!isAdmin.value && !isPartner.value) requests.push(api.get('/dashboard/personal-level'))
    const [statsRes, levelRes] = await Promise.all(requests)
    const s = statsRes.data || {}
    stats.targetDgmv = s.targetDgmv || 0
    stats.totalDgmv = s.totalDgmv || 0
    stats.gap = s.gap || 0
    stats.daysLeft = s.daysLeft || 0
    stats.elapsedDays = s.elapsedDays || 0
    stats.quarterTotalDays = s.quarterTotalDays || 91
    stats.dailyNeeded = s.dailyNeeded || 0
    stats.completionRate = s.completionRate || 0
    stats.scope = s.scope || 'personal'
    if (levelRes) Object.assign(levelInfo, levelRes.data)
    targetPct.value = Number(levelInfo.completionRate) || 0
  } catch { /* use defaults */ }

  // Animate both progress bars
  const duration = 2000
  const start = performance.now()
  const companyTarget = Number(stats.completionRate) || 0
  const tick = (now) => {
    const elapsed = now - start
    const progress = Math.min(elapsed / duration, 1)
    const eased = progress === 1 ? 1 : 1 - Math.pow(2, -10 * progress)
    animatedPct.value = eased * targetPct.value
    animatedRate.value = eased * companyTarget
    if (progress < 1) requestAnimationFrame(tick)
  }
  requestAnimationFrame(tick)
})

// ── Sparkline helpers ──────────────────────────────────────────
function sparkLine(points) {
  const w = 68, h = 26, max = Math.max(...points), min = Math.min(...points), range = max - min || 1
  const coords = points.map((v, i) => `${(i / (points.length - 1)) * w},${h - ((v - min) / range) * (h - 6) - 3}`)
  return `M${coords.join(' L')}`
}
function sparkArea(points) { return `${sparkLine(points)} L68,26 L0,26 Z` }

function sparkLineSm(points) {
  const w = 56, h = 22, max = Math.max(...points), min = Math.min(...points), range = max - min || 1
  const coords = points.map((v, i) => `${(i / (points.length - 1)) * w},${h - ((v - min) / range) * (h - 5) - 2}`)
  return `M${coords.join(' L')}`
}
function sparkAreaSm(points) { return `${sparkLineSm(points)} L56,22 L0,22 Z` }

// ── Admin: completion rate status (time-weighted) ─────────────
// 时间进度 = 已过天数 / 季度总天数 * 100
// delta = 实际完成率 - 时间进度
//   delta >= +5%  → 超前，进度良好
//   delta >= -10% → 基本达标
//   delta <  -10% → 落后预期
const timePct = computed(() => {
  const total = Number(stats.quarterTotalDays) || 91
  const elapsed = Number(stats.elapsedDays) || 0
  return total > 0 ? (elapsed / total) * 100 : 0
})
const rateDelta = computed(() => Number(stats.completionRate) - timePct.value)

const rateColor = computed(() => {
  if (rateDelta.value >= 5) return 'text-success-light'
  if (rateDelta.value >= -10) return 'text-accent'
  return 'text-danger-light'
})
const rateStatusClass = computed(() => {
  if (rateDelta.value >= 5) return 'bg-success/[0.1] text-success-light border-success/[0.2]'
  if (rateDelta.value >= -10) return 'bg-accent/[0.1] text-accent border-accent/[0.2]'
  return 'bg-danger/[0.1] text-danger-light border-danger/[0.2]'
})
const rateStatusLabel = computed(() => {
  if (rateDelta.value >= 5) return '进度超前'
  if (rateDelta.value >= -10) return '基本达标'
  return '落后预期'
})

// ── Admin side cards ───────────────────────────────────────────
const adminSideCards = computed(() => {
  const g = Number(stats.gap) || 0
  const d = Number(stats.dailyNeeded) || 0
  const days = Number(stats.daysLeft) || 0
  return [
    {
      label: '目标差额',
      value: fmtWan(g > 0 ? g : 0), unit: '万',
      sub: '距离季度目标还差',
      valueColor: 'text-accent',
      spark: [180, 165, 140, 120, 105, 90, g / 1000], sColor: '#F59E0B',
      iconPath: '<path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/>',
      iconBg: 'bg-accent/[0.1]', iconBorder: 'border-accent/[0.15]', iconColor: 'text-accent',
    },
    {
      label: '日均需完成',
      value: fmtWan(d), unit: '万/天',
      sub: `按剩余 ${days} 天均摊`,
      valueColor: 'text-danger-light',
      spark: [1.2, 1.3, 1.1, 1.4, 1.5, 1.3, d / 10000], sColor: '#DC2626',
      iconPath: '<circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>',
      iconBg: 'bg-danger/[0.1]', iconBorder: 'border-danger/[0.15]', iconColor: 'text-danger-light',
    },
    {
      label: '季度剩余天数',
      value: String(days), unit: '天',
      sub: `Q${currentQuarter.value} 截止倒计时`,
      valueColor: days <= 14 ? 'text-danger-light' : days <= 30 ? 'text-accent' : 'text-trust-100',
      spark: [90, 80, 70, 60, 50, 40, days], sColor: '#6366F1',
      iconPath: '<rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>',
      iconBg: 'bg-indigo-500/[0.1]', iconBorder: 'border-indigo-500/[0.15]', iconColor: 'text-indigo-400',
    },
  ]
})

// ── Non-admin: 4 cards ─────────────────────────────────────────
const scopeLabel = computed(() => {
  const s = stats.scope
  if (s === 'company') return '全公司'
  if (s === 'team') return '我的团队'
  return '我的'
})

const cards = computed(() => {
  const t = Number(stats.targetDgmv) || 0
  const c = Number(stats.totalDgmv) || 0
  const g = Number(stats.gap) || 0
  const d = Number(stats.dailyNeeded) || 0
  const rate = Number(stats.completionRate) || 0
  const sl = scopeLabel.value
  return [
    {
      label: '本季目标 DGMV', value: fmtWan(t), unit: '万',
      sub: `${sl} · Q${currentQuarter.value} 季度目标`,
      spark: [40, 42, 45, 43, 48, 50, 52], sColor: '#2563EB',
      iconPath: '<circle cx="12" cy="12" r="10"/><circle cx="12" cy="12" r="6"/><circle cx="12" cy="12" r="2"/>',
      iconBg: 'bg-brand/[0.1]', iconBorder: 'border-brand/[0.15]', iconColor: 'text-brand-light',
    },
    {
      label: '当前累计 DGMV', value: fmtWan(c), unit: '万',
      sub: `已完成 ${rate}%`,
      spark: [20, 35, 52, 68, 80, 95, c / 1000], sColor: '#059669',
      iconPath: '<polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/><polyline points="17 6 23 6 23 12"/>',
      iconBg: 'bg-success/[0.1]', iconBorder: 'border-success/[0.15]', iconColor: 'text-success-light',
    },
    {
      label: '目标差额', value: fmtWan(g > 0 ? g : 0), unit: '万',
      sub: '距离目标还差',
      spark: [180, 165, 140, 120, 105, 90, g / 1000], sColor: '#F59E0B',
      iconPath: '<path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/>',
      iconBg: 'bg-accent/[0.1]', iconBorder: 'border-accent/[0.15]', iconColor: 'text-accent',
    },
    {
      label: '日均需完成', value: fmtWan(d), unit: '万',
      sub: `剩余 ${stats.daysLeft} 天`,
      spark: [1.2, 1.3, 1.1, 1.4, 1.5, 1.3, d / 10000], sColor: '#DC2626',
      iconPath: '<circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>',
      iconBg: 'bg-danger/[0.1]', iconBorder: 'border-danger/[0.15]', iconColor: 'text-danger-light',
    },
  ]
})

// ── Level tier styling ─────────────────────────────────────────
const levelTier = computed(() => {
  const lv = (levelInfo.currentLevel || 'K1').toUpperCase()
  if (lv === 'K1') return 1
  if (lv === 'K2') return 2
  if (lv === 'K3') return 3
  if (lv === 'K4') return 4
  return 5
})
const levelBorder = computed(() => {
  const t = levelTier.value
  if (t <= 1) return 'border-brand/[0.12]'
  if (t === 2) return 'border-emerald-500/[0.15]'
  if (t === 3) return 'border-purple-500/[0.18]'
  if (t === 4) return 'border-amber-500/[0.22]'
  return 'border-red-500/[0.25]'
})
const levelBgGlow = computed(() => {
  const t = levelTier.value
  if (t <= 1) return 'bg-gradient-to-r from-brand/[0.06] to-transparent'
  if (t === 2) return 'bg-gradient-to-r from-emerald-500/[0.08] to-transparent'
  if (t === 3) return 'bg-gradient-to-r from-purple-500/[0.10] via-purple-500/[0.04] to-transparent'
  if (t === 4) return 'bg-gradient-to-r from-amber-500/[0.12] via-amber-500/[0.05] to-transparent'
  return 'bg-gradient-to-r from-red-500/[0.14] via-orange-500/[0.06] to-transparent'
})
const levelBadgeClass = computed(() => {
  const t = levelTier.value
  if (t <= 1) return 'bg-brand/[0.1] text-blue-400 border-brand/[0.2]'
  if (t === 2) return 'bg-emerald-500/[0.1] text-emerald-400 border-emerald-500/[0.2]'
  if (t === 3) return 'bg-purple-500/[0.1] text-purple-400 border-purple-500/[0.2]'
  if (t === 4) return 'bg-amber-500/[0.12] text-amber-400 border-amber-500/[0.25]'
  return 'bg-red-500/[0.12] text-red-400 border-red-500/[0.25]'
})
const levelPctColor = computed(() => {
  const t = levelTier.value
  if (t <= 1) return 'text-blue-400'
  if (t === 2) return 'text-emerald-400'
  if (t === 3) return 'text-purple-400'
  if (t === 4) return 'text-amber-400'
  return 'text-red-400'
})
const levelTextColor = computed(() => levelPctColor.value)
const levelBarGradient = computed(() => {
  const t = levelTier.value
  if (t <= 1) return 'bg-gradient-to-r from-blue-600 to-blue-400'
  if (t === 2) return 'bg-gradient-to-r from-emerald-600 to-emerald-400'
  if (t === 3) return 'bg-gradient-to-r from-purple-600 via-purple-400 to-fuchsia-400'
  if (t === 4) return 'bg-gradient-to-r from-amber-600 via-yellow-400 to-amber-300'
  return 'bg-gradient-to-r from-red-600 via-orange-500 to-yellow-400'
})
const levelDotClass = computed(() => {
  const t = levelTier.value
  if (t <= 1) return 'bg-blue-400 shadow-[0_0_10px_rgba(59,130,246,0.6)]'
  if (t === 2) return 'bg-emerald-400 shadow-[0_0_10px_rgba(16,185,129,0.6)]'
  if (t === 3) return 'bg-purple-400 shadow-[0_0_12px_rgba(168,85,247,0.7)]'
  if (t === 4) return 'bg-amber-400 shadow-[0_0_14px_rgba(245,158,11,0.7)]'
  return 'bg-red-400 shadow-[0_0_16px_rgba(239,68,68,0.8)]'
})
const levelDotBg = computed(() => {
  const t = levelTier.value
  if (t <= 1) return 'bg-blue-400'
  if (t === 2) return 'bg-emerald-400'
  if (t === 3) return 'bg-purple-400'
  if (t === 4) return 'bg-amber-400'
  return 'bg-red-400'
})
const levelAccentColor = computed(() => {
  const t = levelTier.value
  if (t <= 1) return 'text-blue-400'
  if (t === 2) return 'text-emerald-400'
  if (t === 3) return 'text-purple-400'
  if (t === 4) return 'text-amber-400'
  return 'text-red-400'
})
</script>
