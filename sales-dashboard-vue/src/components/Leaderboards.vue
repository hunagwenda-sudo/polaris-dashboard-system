<template>
  <div class="space-y-4">
    <!-- 昨日战报 — bar chart -->
    <div class="bg-surface-raised rounded-2xl border border-white/[0.06] overflow-hidden">
      <div class="px-6 pt-5 pb-1 flex items-center justify-between">
        <div>
          <h3 class="text-[13px] font-semibold text-white tracking-tight font-sans">昨日战报</h3>
          <p class="text-[10px] text-trust-300 mt-0.5 font-sans">昨日全员 DGMV 总览</p>
        </div>
        <div class="flex items-center gap-3">
          <span class="text-[10px] text-trust-300 font-sans">合计 <span class="text-white font-bold font-mono">{{ fmt(dailyTotal) }}</span></span>
          <div class="w-8 h-8 rounded-lg bg-accent/[0.08] border border-accent/[0.12] flex items-center justify-center">
            <svg v-bind="iconDefaults" class="w-4 h-4 text-accent"><path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6"/><path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18"/><path d="M4 22h16"/><path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20 7 22"/><path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20 17 22"/><path d="M18 2H6v7a6 6 0 0 0 12 0V2Z"/></svg>
          </div>
        </div>
      </div>
      <div class="px-3 pb-4 max-h-[360px] overflow-y-auto">
        <v-chart :option="dragonChartOption" autoresize :style="{ height: chartHeight }" />
      </div>
    </div>

    <!-- Hero Board -->
    <div class="bg-surface-raised rounded-2xl border border-white/[0.06] overflow-hidden">
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
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { iconDefaults } from './icons.js'
import LevelBadge from './LevelBadge.vue'
import api from '../api'
import { useAuthStore } from '../stores/auth'

use([BarChart, GridComponent, TooltipComponent, CanvasRenderer])

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

// 生成假金额用于模糊显示，防止 DevTools 泄露真实数据
const fakeAmount = () => `¥${(Math.random() * 8 + 1).toFixed(1)}万`

const dailyData = ref([])
const heroBoard = ref([])

const dailyTotal = computed(() => dailyData.value.reduce((s, d) => s + Number(d.dgmv || 0), 0))

// 按 id 排序
const sortedDailyData = computed(() => {
  return [...dailyData.value].sort((a, b) => Number(a.userId) - Number(b.userId))
})

// 动态高度：每人 32px，最小 200px
const chartHeight = computed(() => {
  const count = sortedDailyData.value.length
  return Math.max(200, count * 32) + 'px'
})

const dragonChartOption = computed(() => {
  // ECharts Y 轴从下到上，所以 reverse 让第一名在最上面
  const sorted = [...sortedDailyData.value].reverse()
  const names = sorted.map(d => d.name)
  const values = sorted.map(d => Number(d.dgmv || 0))
  const count = sorted.length
  const barWidth = count <= 8 ? 18 : count <= 14 ? 14 : 11
  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow', shadowStyle: { color: 'rgba(59,130,246,0.06)' } },
      backgroundColor: 'rgba(15,23,42,0.95)',
      borderColor: 'rgba(59,130,246,0.15)',
      borderWidth: 1,
      padding: [10, 14],
      textStyle: { color: '#E2E8F0', fontSize: 12, fontFamily: 'Inter' },
      formatter: (params) => {
        const d = params[0]
        const v = d.value || 0
        const display = v >= 10000 ? `¥${(v / 10000).toFixed(1)}万` : `¥${v.toFixed(0)}`
        return `<span style="color:#94A3B8">${d.name}</span><br/><span style="font-family:JetBrains Mono;font-size:14px;font-weight:700;color:#3B82F6">${display}</span>`
      },
    },
    grid: { left: 70, right: 60, top: 12, bottom: 28 },
    xAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.04)', type: 'dashed' } },
      axisLabel: {
        color: '#64748B',
        fontSize: 10,
        fontFamily: 'JetBrains Mono',
        formatter: (v) => v === 0 ? '0' : v >= 10000 ? `${(v / 10000).toFixed(0)}万` : `¥${v}`,
      },
    },
    yAxis: {
      type: 'category',
      data: names,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: {
        color: '#CBD5E1',
        fontSize: 11,
        fontFamily: 'Inter',
        fontWeight: 500,
      },
    },
    series: [{
      type: 'bar',
      barWidth,
      itemStyle: {
        borderRadius: [0, 6, 6, 0],
        color: {
          type: 'linear', x: 0, y: 0, x2: 1, y2: 0,
          colorStops: [
            { offset: 0, color: 'rgba(59,130,246,0.35)' },
            { offset: 1, color: 'rgba(59,130,246,0.9)' },
          ],
        },
      },
      emphasis: {
        itemStyle: {
          color: {
            type: 'linear', x: 0, y: 0, x2: 1, y2: 0,
            colorStops: [
              { offset: 0, color: 'rgba(59,130,246,0.5)' },
              { offset: 1, color: 'rgba(96,165,250,1)' },
            ],
          },
          shadowColor: 'rgba(59,130,246,0.25)',
          shadowBlur: 12,
        },
      },
      label: {
        show: true,
        position: 'right',
        formatter: (p) => {
          const v = p.value
          if (v >= 10000) return `¥${(v / 10000).toFixed(1)}万`
          if (v > 0) return `¥${v}`
          return '¥0'
        },
        color: '#94A3B8',
        fontSize: 10,
        fontWeight: 600,
        fontFamily: 'JetBrains Mono',
      },
      data: values,
      animationDuration: 1200,
      animationEasing: 'cubicOut',
    }],
  }
})

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
