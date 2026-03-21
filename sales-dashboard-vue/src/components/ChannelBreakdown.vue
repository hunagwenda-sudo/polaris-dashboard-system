<template>
  <div class="bg-surface-raised rounded-2xl border border-white/[0.06] overflow-hidden">
    <div class="px-6 pt-5 pb-2">
      <h3 class="text-[13px] font-semibold text-white tracking-tight font-sans">销售渠道分布</h3>
      <p class="text-[10px] text-trust-300 mt-0.5 font-sans">本季度各渠道 DGMV 占比</p>
    </div>
    <div class="flex items-center px-6 pb-5 gap-6">
      <div class="w-[180px] h-[180px] shrink-0">
        <v-chart :option="chartOption" autoresize class="w-full h-full" />
      </div>
      <div class="flex-1 space-y-2.5">
        <div v-for="c in channels" :key="c.name" class="flex items-center gap-3 group cursor-pointer">
          <div class="w-2.5 h-2.5 rounded-full shrink-0" :style="{ backgroundColor: c.color }" />
          <span class="text-[12px] font-medium text-trust-300 font-sans w-16">{{ c.name }}</span>
          <div class="flex-1 h-2.5 bg-white/[0.04] rounded-full overflow-hidden">
            <div class="h-full rounded-full transition-all duration-500 group-hover:opacity-100 opacity-80 group-hover:shadow-[0_0_8px_rgba(255,255,255,0.1)]"
              :style="{ width: c.pct + '%', backgroundColor: c.color }" />
          </div>
          <span class="text-[12px] font-bold text-white font-mono tabular-nums w-16 text-right">¥{{ (c.value / 10000).toFixed(1) }}万</span>
        </div>
        <div v-if="channels.length === 0" class="py-6 text-center text-trust-300 text-[11px] font-sans">暂无数据</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { PieChart } from 'echarts/charts'
import { TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import api from '../api'

use([PieChart, TooltipComponent, CanvasRenderer])

const defaultColors = ['#2563EB', '#059669', '#F59E0B', '#8B5CF6', '#06B6D4', '#EC4899', '#F97316', '#14B8A6', '#6366F1', '#EF4444']
const platformList = ref([])
const rawBreakdown = ref({})

const colorMap = computed(() => {
  const map = {}
  platformList.value.forEach((p, i) => { map[p.code] = defaultColors[i % defaultColors.length] })
  return map
})

const labelMap = computed(() => {
  const map = {}
  platformList.value.forEach(p => { map[p.code] = p.label })
  return map
})

const channels = computed(() => {
  const bd = rawBreakdown.value
  const cm = colorMap.value
  const lm = labelMap.value
  const entries = Object.entries(bd).map(([code, value]) => ({ name: lm[code] || code, value: Number(value), color: cm[code] || '#475569' }))
  const total = entries.reduce((s, e) => s + e.value, 0) || 1
  return entries.map(e => ({ ...e, pct: ((e.value / total) * 100).toFixed(1) })).sort((a, b) => b.value - a.value)
})

const total = computed(() => channels.value.reduce((s, c) => s + c.value, 0))

const chartOption = computed(() => ({
  tooltip: {
    trigger: 'item', backgroundColor: '#1E293B', borderColor: 'rgba(255,255,255,0.08)',
    textStyle: { color: '#E2E8F0', fontSize: 11, fontFamily: 'Inter' },
    formatter: (p) => `${p.name}<br/><b style="font-family:JetBrains Mono">¥${(p.value / 10000).toFixed(1)}万</b> (${p.percent}%)`,
  },
  series: [{
    type: 'pie', radius: ['60%', '88%'], center: ['50%', '50%'], padAngle: 3,
    itemStyle: { borderRadius: 4 }, label: { show: false },
    data: channels.value.map(c => ({ name: c.name, value: c.value, itemStyle: { color: c.color } })),
  }],
  graphic: [{
    type: 'group', left: 'center', top: 'center',
    children: [
      { type: 'text', style: { text: '总计', fill: '#94A3B8', fontSize: 9, fontFamily: 'Inter', textAlign: 'center' }, top: -14 },
      { type: 'text', style: { text: `¥${(total.value / 10000).toFixed(0)}万`, fill: '#fff', fontSize: 18, fontWeight: 800, fontFamily: 'JetBrains Mono', textAlign: 'center' }, top: 2 },
    ],
  }],
}))

onMounted(async () => {
  try {
    const [channelRes, dictRes] = await Promise.all([
      api.get('/dashboard/channel'),
      api.get('/dict/platform'),
    ])
    rawBreakdown.value = channelRes.data?.breakdown || {}
    platformList.value = dictRes.data || []
  } catch { /* use empty */ }
})
</script>
