<template>
  <div class="max-w-[800px] mx-auto space-y-4">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-[15px] font-semibold text-white font-sans">周榜存档</h2>
        <p class="text-[11px] text-trust-300 mt-0.5 font-sans">查看历史每周英雄榜排名</p>
      </div>
      <button v-if="isAdmin" @click="showGenerate = true" class="flex items-center gap-1.5 px-4 py-2 rounded-xl text-[12px] font-semibold bg-gradient-to-r from-brand to-brand-light text-white hover:opacity-90 transition-opacity cursor-pointer shadow-lg shadow-brand/15 font-sans">
        <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        补录存档
      </button>
    </div>

    <!-- 周选择器 -->
    <div class="bg-surface-raised rounded-2xl border border-white/[0.04] px-5 py-3.5 flex items-center gap-4">
      <span class="text-[10px] font-semibold text-trust-300 uppercase tracking-[0.1em] font-sans">选择周</span>
      <div class="flex flex-wrap gap-2">
        <button v-for="w in weeks" :key="w.weekLabel" @click="selectWeek(w)"
          :class="['px-3 py-1.5 rounded-lg text-[11px] font-sans transition-colors cursor-pointer border',
            selectedWeek === w.weekLabel ? 'bg-brand/[0.15] text-brand-light border-brand/[0.3] font-semibold' : 'bg-white/[0.03] text-gray-400 border-white/[0.06] hover:border-white/[0.12]']">
          {{ w.weekLabel }}
          <span class="text-[9px] text-trust-400 ml-1">{{ w.weekStart.slice(5) }}~{{ w.weekEnd.slice(5) }}</span>
        </button>
      </div>
      <span v-if="weeks.length === 0" class="text-[11px] text-trust-400 font-sans">暂无存档</span>
    </div>

    <!-- 榜单 -->
    <div v-if="rows.length > 0" class="bg-surface-raised rounded-2xl border border-white/[0.04] overflow-hidden">
      <table class="w-full table-fixed">
        <colgroup>
          <col class="w-[10%]" />
          <col class="w-[22%]" />
          <col class="w-[14%]" />
          <col class="w-[14%]" />
          <col class="w-[14%]" />
          <col class="w-[26%]" />
        </colgroup>
        <thead>
          <tr class="border-b border-white/[0.04]">
            <th class="px-4 py-3 text-center text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">排名</th>
            <th class="px-4 py-3 text-left text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">姓名</th>
            <th class="px-4 py-3 text-center text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">角色</th>
            <th class="px-4 py-3 text-center text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">职级</th>
            <th class="px-4 py-3 text-center text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">预估职级</th>
            <th class="px-4 py-3 text-right text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">周 DGMV</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in rows" :key="r.id" class="border-b border-white/[0.02] hover:bg-white/[0.03] transition-colors duration-150">
            <td class="px-4 py-3 text-center">
              <span v-if="r.rankNum <= 3" :class="['inline-flex items-center justify-center w-6 h-6 rounded-md border text-[10px] font-bold font-mono', rankColors[r.rankNum - 1]]">{{ r.rankNum }}</span>
              <span v-else class="text-[11px] text-trust-300 font-mono">{{ r.rankNum }}</span>
            </td>
            <td class="px-4 py-3">
              <div class="flex items-center gap-2">
                <div class="w-6 h-6 rounded-md bg-gradient-to-br from-brand to-brand-light flex items-center justify-center text-white text-[9px] font-semibold font-mono shrink-0">{{ (r.userName||'?')[0] }}</div>
                <span class="text-[12px] font-medium text-gray-200 font-sans truncate">{{ r.userName }}</span>
              </div>
            </td>
            <td class="px-4 py-3 text-center"><span :class="['inline-flex items-center px-1.5 py-0.5 rounded-md text-[9px] font-semibold border font-sans', roleBadge[r.userRole] || roleBadge.sales]">{{ roleLabel[r.userRole] || r.userRole }}</span></td>
            <td class="px-4 py-3 text-center"><span class="inline-flex items-center px-1.5 py-0.5 rounded-md text-[9px] font-bold bg-accent/[0.08] text-accent border border-accent/[0.12] font-mono">{{ r.userLevel || 'K1' }}</span></td>
            <td class="px-4 py-3 text-center"><span class="inline-flex items-center px-1.5 py-0.5 rounded-md text-[9px] font-bold bg-success/[0.08] text-success-light border border-success/[0.12] font-mono">{{ r.estimatedLevel || 'K1' }}</span></td>
            <td class="px-4 py-3 text-right">
              <span v-if="r.rankNum <= 5" class="text-[13px] font-bold text-success-light font-mono tabular-nums">¥{{ Number(r.dgmv).toLocaleString() }}</span>
              <span v-else class="text-[13px] font-bold text-trust-300 font-mono">**</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <div v-else-if="selectedWeek" class="bg-surface-raised rounded-2xl border border-white/[0.04] py-14 text-center text-trust-300 text-[12px] font-sans">该周暂无数据</div>

    <!-- 补录弹窗 -->
    <div v-if="showGenerate" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showGenerate = false">
      <div class="bg-trust-800 border border-white/[0.08] rounded-2xl p-6 w-[380px] space-y-4">
        <h3 class="text-[14px] font-semibold text-white font-sans">补录周榜存档</h3>
        <p class="text-[11px] text-trust-300 font-sans">选择某周的周一日期，系统会根据该周业绩数据生成存档</p>
        <div>
          <label class="text-[10px] text-trust-300 font-sans block mb-1">周一日期</label>
          <input type="date" v-model="generateDate" class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-4 py-2 text-[12px] text-white font-mono focus:outline-none focus:ring-2 focus:ring-brand/30 [color-scheme:dark]" />
        </div>
        <p v-if="generateError" class="text-red-400 text-[11px] font-sans">{{ generateError }}</p>
        <p v-if="generateSuccess" class="text-success-light text-[11px] font-sans">{{ generateSuccess }}</p>
        <div class="flex gap-3 justify-end">
          <button @click="showGenerate = false" class="px-4 py-2 rounded-lg text-[12px] text-trust-300 hover:text-white cursor-pointer font-sans">关闭</button>
          <button @click="doGenerate" :disabled="generating" class="px-4 py-2 rounded-lg text-[12px] font-semibold bg-brand text-white hover:bg-brand-light cursor-pointer font-sans disabled:opacity-50">
            {{ generating ? '生成中...' : '生成存档' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import api from '../api'

const auth = useAuthStore()
const isAdmin = computed(() => auth.user?.role === 'admin')

const roleLabel = { admin: '管理员', partner: '合伙人', sales: '运营' }
const roleBadge = {
  admin: 'bg-red-500/[0.08] text-red-400 border-red-500/[0.12]',
  partner: 'bg-brand/[0.08] text-brand border-brand/[0.12]',
  sales: 'bg-white/[0.04] text-gray-400 border-white/[0.04]',
}
const rankColors = [
  'bg-amber-500/[0.15] text-amber-400 border-amber-500/[0.25]',
  'bg-gray-400/[0.1] text-gray-300 border-gray-400/[0.2]',
  'bg-amber-700/[0.12] text-amber-600 border-amber-700/[0.2]',
]

const weeks = ref([])
const selectedWeek = ref('')
const rows = ref([])

const showGenerate = ref(false)
const generateDate = ref('')
const generateError = ref('')
const generateSuccess = ref('')
const generating = ref(false)

async function fetchWeeks() {
  try {
    const res = await api.get('/dashboard/weekly-archive/weeks')
    weeks.value = res.data || []
    if (weeks.value.length > 0) selectWeek(weeks.value[0])
  } catch { /* ignore */ }
}

async function selectWeek(w) {
  selectedWeek.value = w.weekLabel
  try {
    const res = await api.get('/dashboard/weekly-archive/detail', { params: { weekLabel: w.weekLabel } })
    rows.value = res.data || []
  } catch { rows.value = [] }
}

async function doGenerate() {
  generateError.value = ''
  generateSuccess.value = ''
  if (!generateDate.value) { generateError.value = '请选择日期'; return }
  generating.value = true
  try {
    const res = await api.post('/dashboard/weekly-archive/generate', { weekStart: generateDate.value })
    generateSuccess.value = `存档成功，共 ${res.data?.count || 0} 条记录`
    fetchWeeks()
  } catch (e) {
    generateError.value = e?.message || '生成失败'
  } finally {
    generating.value = false
  }
}

onMounted(fetchWeeks)
</script>
