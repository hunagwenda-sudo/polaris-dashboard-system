<template>
  <div class="bg-surface-raised rounded-2xl border border-white/[0.06] overflow-hidden">
    <div class="px-6 pt-5 pb-2 flex items-start justify-between">
      <div class="flex items-center gap-3">
        <div class="w-8 h-8 rounded-lg bg-brand/[0.08] border border-brand/[0.1] flex items-center justify-center">
          <svg v-bind="iconDefaults" class="w-4 h-4 text-brand-light"><polyline points="14.5 17.5 3 6 3 3 6 3 17.5 14.5"/><line x1="13" y1="19" x2="19" y2="13"/><line x1="16" y1="16" x2="20" y2="20"/><line x1="19" y1="21" x2="21" y2="19"/><polyline points="14.5 6.5 18 3 21 3 21 6 17.5 9.5"/><line x1="5" y1="14" x2="9" y2="18"/><line x1="7" y1="17" x2="4" y2="20"/><line x1="3" y1="19" x2="5" y2="21"/></svg>
        </div>
        <div>
          <h3 class="text-[13px] font-semibold text-white tracking-tight font-sans">团队荣耀榜</h3>
          <p class="text-[10px] text-trust-300 mt-0.5 font-sans">本季度各团队累计 DGMV 对比</p>
        </div>
      </div>
    </div>

    <!-- Podium -->
    <div class="px-6 pb-5 pt-2">
      <!-- Top 3 podium layout: 2nd | 1st | 3rd -->
      <div v-if="podiumTeams.length > 0" class="flex items-end justify-center gap-3 mb-4" style="min-height: 220px">
        <!-- 2nd place (left) -->
        <div v-if="podiumTeams[1]" class="flex flex-col items-center flex-1 max-w-[160px]">
          <div class="text-[11px] font-bold text-gray-300 font-mono tabular-nums mb-2">{{ fmtA(podiumTeams[1].quarterDgmv) }}</div>
          <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-gray-300 to-gray-400 flex items-center justify-center mb-2 shadow-lg shadow-gray-400/10">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#1E293B" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6"/><path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18"/><path d="M4 22h16"/><path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20 7 22"/><path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20 17 22"/><path d="M18 2H6v7a6 6 0 0 0 12 0V2Z"/></svg>
          </div>
          <div class="w-full rounded-t-xl flex flex-col items-center justify-end bg-gradient-to-t from-gray-500/20 to-gray-400/10 border border-gray-400/15 border-b-0 relative" style="height: 110px">
            <span class="text-[28px] font-extrabold text-gray-400/40 font-mono absolute top-2">2</span>
            <span class="text-[13px] font-bold text-white font-sans mb-3 relative z-10">{{ podiumTeams[1].teamName }}</span>
          </div>
        </div>

        <!-- 1st place (center, tallest) -->
        <div v-if="podiumTeams[0]" class="flex flex-col items-center flex-1 max-w-[180px]">
          <div class="text-[13px] font-extrabold text-amber-400 font-mono tabular-nums mb-2">{{ fmtA(podiumTeams[0].quarterDgmv) }}</div>
          <div class="w-12 h-12 rounded-xl bg-gradient-to-br from-amber-400 to-amber-500 flex items-center justify-center mb-2 shadow-lg shadow-amber-500/20">
            <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#1E293B" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6"/><path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18"/><path d="M4 22h16"/><path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20 7 22"/><path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20 17 22"/><path d="M18 2H6v7a6 6 0 0 0 12 0V2Z"/></svg>
          </div>
          <div class="w-full rounded-t-xl flex flex-col items-center justify-end bg-gradient-to-t from-amber-500/20 to-amber-400/10 border border-amber-400/20 border-b-0 relative" style="height: 150px">
            <span class="text-[32px] font-extrabold text-amber-400/30 font-mono absolute top-2">1</span>
            <span class="text-[14px] font-bold text-white font-sans mb-3 relative z-10">{{ podiumTeams[0].teamName }}</span>
          </div>
        </div>

        <!-- 3rd place (right) -->
        <div v-if="podiumTeams[2]" class="flex flex-col items-center flex-1 max-w-[150px]">
          <div class="text-[11px] font-bold text-amber-700 font-mono tabular-nums mb-2">{{ fmtA(podiumTeams[2].quarterDgmv) }}</div>
          <div class="w-9 h-9 rounded-xl bg-gradient-to-br from-amber-700 to-amber-800 flex items-center justify-center mb-2 shadow-lg shadow-amber-800/10">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#1E293B" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6"/><path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18"/><path d="M4 22h16"/><path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20 7 22"/><path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20 17 22"/><path d="M18 2H6v7a6 6 0 0 0 12 0V2Z"/></svg>
          </div>
          <div class="w-full rounded-t-xl flex flex-col items-center justify-end bg-gradient-to-t from-amber-800/20 to-amber-700/10 border border-amber-700/15 border-b-0 relative" style="height: 80px">
            <span class="text-[24px] font-extrabold text-amber-700/30 font-mono absolute top-1.5">3</span>
            <span class="text-[12px] font-bold text-white font-sans mb-3 relative z-10">{{ podiumTeams[2].teamName }}</span>
          </div>
        </div>
      </div>

      <!-- Remaining teams (4th+) -->
      <div v-if="restTeams.length > 0" class="space-y-2 mt-1">
        <div v-for="(t, i) in restTeams" :key="t.teamId"
          class="flex items-center gap-3 px-3 py-2.5 rounded-xl bg-white/[0.02] border border-white/[0.04] hover:bg-white/[0.04] transition-colors duration-150">
          <span class="w-5 text-center text-[11px] font-bold text-trust-300 font-mono">{{ i + 4 }}</span>
          <span class="text-[12px] font-medium text-gray-300 font-sans flex-1">{{ t.teamName }}</span>
          <span class="text-[12px] font-bold text-gray-400 font-mono tabular-nums">{{ fmtA(t.quarterDgmv) }}</span>
          <div class="w-20 h-1.5 bg-white/[0.04] rounded-full overflow-hidden">
            <div class="h-full rounded-full bg-brand/40" :style="{ width: barWidth(t.quarterDgmv) }" />
          </div>
        </div>
      </div>

      <div v-if="teamData.length === 0" class="py-14 text-center text-trust-300 text-[11px] font-sans">暂无数据</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { iconDefaults } from './icons.js'
import api from '../api'

const teamData = ref([])
const fmtA = (v) => `¥${(Number(v) / 10000).toFixed(1)}万`

// Top 3 for podium (already sorted by backend)
const podiumTeams = computed(() => teamData.value.slice(0, 3))
// 4th and beyond
const restTeams = computed(() => teamData.value.slice(3))

// Bar width relative to max
const barWidth = (val) => {
  const max = teamData.value.length ? Number(teamData.value[0].quarterDgmv) : 1
  return max > 0 ? Math.round((Number(val) / max) * 100) + '%' : '0%'
}

onMounted(async () => {
  try {
    const res = await api.get('/dashboard/team-battle')
    teamData.value = res.data || []
  } catch { /* use empty */ }
})
</script>
