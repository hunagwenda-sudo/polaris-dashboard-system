<template>
  <div class="space-y-4">
    <div class="flex items-center gap-3">
      <button @click="$router.back()" class="flex items-center justify-center w-8 h-8 rounded-xl bg-white/[0.04] border border-white/[0.06] hover:bg-white/[0.08] transition-colors cursor-pointer">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-trust-300"><polyline points="15 18 9 12 15 6"/></svg>
      </button>
      <div>
        <h2 class="text-[15px] font-semibold text-white font-sans">{{ userName }} · 客服业绩详情</h2>
        <p class="text-[11px] text-trust-300 mt-0.5 font-sans">{{ date }}</p>
      </div>
    </div>

    <!-- 汇总卡片 -->
    <div class="grid grid-cols-3 gap-3">
      <div class="bg-surface-raised rounded-2xl border border-white/[0.04] p-4">
        <p class="text-[10px] text-trust-300 font-sans mb-1">总接待量</p>
        <p class="text-[20px] font-extrabold text-white font-mono tabular-nums">{{ totalReception }}</p>
      </div>
      <div class="bg-surface-raised rounded-2xl border border-white/[0.04] p-4">
        <p class="text-[10px] text-trust-300 font-sans mb-1">平均回复率</p>
        <p class="text-[20px] font-extrabold font-mono tabular-nums" :class="rateColor(avgReplyRate)">{{ fmtRate(avgReplyRate) }}</p>
      </div>
      <div class="bg-surface-raised rounded-2xl border border-white/[0.04] p-4">
        <p class="text-[10px] text-trust-300 font-sans mb-1">平均好评率</p>
        <p class="text-[20px] font-extrabold font-mono tabular-nums" :class="rateColor(avgPraiseRate)">{{ fmtRate(avgPraiseRate) }}</p>
      </div>
    </div>

    <!-- 明细表格 -->
    <div class="bg-surface-raised rounded-2xl border border-white/[0.04] overflow-hidden">
      <table class="w-full table-fixed">
        <colgroup>
          <col class="w-[28%]" />
          <col class="w-[16%]" />
          <col class="w-[18%]" />
          <col class="w-[19%]" />
          <col class="w-[19%]" />
        </colgroup>
        <thead>
          <tr class="border-b border-white/[0.04]">
            <th class="px-4 py-3 text-left text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">渠道</th>
            <th class="px-4 py-3 text-left text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">班次</th>
            <th class="px-4 py-3 text-right text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">接待量</th>
            <th class="px-4 py-3 text-right text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">3分钟回复率</th>
            <th class="px-4 py-3 text-right text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">好评率</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in detailRecords" :key="r.id" class="border-b border-white/[0.02] hover:bg-white/[0.03] transition-colors duration-150">
            <td class="px-4 py-3">
              <span class="inline-flex items-center px-2 py-0.5 rounded-md text-[10px] font-semibold bg-purple-500/[0.06] text-purple-300 border border-purple-500/[0.1] font-sans">{{ platformMap[r.platform] || r.platform }}</span>
            </td>
            <td class="px-4 py-3">
              <span :class="['inline-flex items-center px-1.5 py-0.5 rounded-md text-[9px] font-semibold border font-sans', r.shift === 'morning' ? 'bg-amber-500/[0.08] text-amber-400 border-amber-500/[0.12]' : 'bg-indigo-500/[0.08] text-indigo-400 border-indigo-500/[0.12]']">{{ r.shift === 'morning' ? '早班' : '晚班' }}</span>
            </td>
            <td class="px-4 py-3 text-[12px] text-white font-mono tabular-nums text-right font-medium">{{ r.receptionCount }}</td>
            <td class="px-4 py-3 text-[12px] font-mono tabular-nums text-right" :class="rateColor(r.replyRate)">{{ fmtRate(r.replyRate) }}</td>
            <td class="px-4 py-3 text-[12px] font-mono tabular-nums text-right" :class="rateColor(r.praiseRate)">{{ fmtRate(r.praiseRate) }}</td>
          </tr>
        </tbody>
      </table>
      <div v-if="detailRecords.length === 0 && !loading" class="py-14 text-center text-trust-300 text-[12px] font-sans">暂无记录</div>
      <div v-if="loading" class="py-14 text-center text-trust-300 text-[12px] font-sans">加载中...</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../api'

const route = useRoute()
const userId = computed(() => route.params.userId)
const date = computed(() => route.query.date || '')

const detailRecords = ref([])
const userName = ref('')
const platformMap = ref({})
const loading = ref(true)

function fmtRate(v) {
  if (v == null) return '—'
  return Number(v).toFixed(1) + '%'
}
function rateColor(v) {
  if (v == null) return 'text-gray-500'
  const n = Number(v)
  if (n >= 95) return 'text-success-light'
  if (n >= 80) return 'text-brand-light'
  return 'text-danger-light'
}

const totalReception = computed(() => detailRecords.value.reduce((s, r) => s + (Number(r.receptionCount) || 0), 0))
const avgReplyRate = computed(() => {
  const valid = detailRecords.value.filter(r => r.replyRate != null)
  if (!valid.length) return null
  return valid.reduce((s, r) => s + Number(r.replyRate), 0) / valid.length
})
const avgPraiseRate = computed(() => {
  const valid = detailRecords.value.filter(r => r.praiseRate != null)
  if (!valid.length) return null
  return valid.reduce((s, r) => s + Number(r.praiseRate), 0) / valid.length
})

async function fetchDetail() {
  loading.value = true
  try {
    const params = { startDate: date.value, endDate: date.value, size: 100 }
    // 客服只能看自己的，admin/partner 用 /service-records 带 userId 过滤
    const res = await api.get('/service-records', { params })
    const all = res.data?.records || []
    detailRecords.value = all.filter(r => String(r.userId) === String(userId.value))
  } catch { /* ignore */ }
  loading.value = false
}

async function fetchUserName() {
  try {
    const res = await api.get('/users', { params: { size: 999 } })
    const users = res.data?.records || []
    const u = users.find(u => String(u.id) === String(userId.value))
    if (u) userName.value = u.name
  } catch { /* ignore */ }
}

async function fetchPlatforms() {
  try {
    const res = await api.get('/dict/platform')
    const map = {}
    ;(res.data || []).forEach(p => { map[p.code] = p.label })
    platformMap.value = map
  } catch { /* ignore */ }
}

onMounted(() => { fetchUserName(); fetchPlatforms(); fetchDetail() })
</script>
