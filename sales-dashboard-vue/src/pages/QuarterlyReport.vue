<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-[15px] font-semibold text-white font-sans">季度报表</h2>
        <p class="text-[11px] text-trust-300 mt-0.5 font-sans">查看历史季度每位运营的职级与总业绩</p>
      </div>
      <div class="flex items-center gap-3">
        <!-- 生成快照（仅管理员） -->
        <button v-if="auth.user?.role === 'admin'" @click="showGenerate = true"
          class="flex items-center gap-1.5 px-3 py-2 rounded-xl text-[11px] font-semibold bg-white/[0.04] text-trust-300 hover:text-white hover:bg-white/[0.08] transition-colors cursor-pointer font-sans border border-white/[0.06]">
          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 5v14"/><path d="M5 12h14"/></svg>
          补录季度快照
        </button>
        <!-- 季度选择 -->
        <select v-model="selectedQuarter" @change="fetchSnapshots"
          class="bg-white/[0.03] border border-white/[0.06] rounded-xl px-4 py-2 text-[12px] text-gray-300 font-mono focus:outline-none focus:ring-2 focus:ring-brand/30 cursor-pointer">
          <option value="" disabled>选择季度</option>
          <option v-for="q in quarters" :key="q" :value="q">{{ q }}</option>
        </select>
      </div>
    </div>

    <!-- 生成快照弹窗 -->
    <div v-if="showGenerate" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showGenerate = false">
      <div class="bg-trust-800 border border-white/[0.08] rounded-2xl p-6 w-[380px] space-y-4">
        <h3 class="text-[14px] font-semibold text-white font-sans">补录季度快照</h3>
        <p class="text-[11px] text-trust-300 font-sans">为历史季度生成快照数据（根据业绩记录计算）</p>
        <div>
          <label class="text-[10px] text-trust-300 font-sans block mb-1">季度</label>
          <select v-model="generateQuarter"
            class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-4 py-2 text-[12px] text-gray-300 font-mono focus:outline-none focus:ring-2 focus:ring-brand/30 cursor-pointer">
            <option v-for="q in pastQuarters" :key="q" :value="q">{{ q }}</option>
          </select>
        </div>
        <p v-if="generateError" class="text-red-400 text-[11px] font-sans">{{ generateError }}</p>
        <div class="flex gap-3 justify-end">
          <button @click="showGenerate = false" class="px-4 py-2 rounded-lg text-[12px] text-trust-300 hover:text-white cursor-pointer font-sans">取消</button>
          <button @click="handleGenerate" :disabled="generating"
            class="px-4 py-2 rounded-lg text-[12px] font-semibold bg-brand text-white hover:bg-brand-light cursor-pointer font-sans disabled:opacity-50">
            {{ generating ? '生成中...' : '生成' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 汇总卡片 -->
    <div v-if="snapshots.length > 0" class="grid grid-cols-3 gap-3">
      <div class="bg-surface-raised rounded-xl border border-white/[0.04] px-5 py-4">
        <p class="text-[10px] text-trust-300 font-sans uppercase tracking-[0.1em]">运营人数</p>
        <p class="text-[22px] font-extrabold text-white font-mono tabular-nums mt-1">{{ snapshots.length }}</p>
      </div>
      <div class="bg-surface-raised rounded-xl border border-white/[0.04] px-5 py-4">
        <p class="text-[10px] text-trust-300 font-sans uppercase tracking-[0.1em]">总 DGMV</p>
        <p class="text-[22px] font-extrabold text-success-light font-mono tabular-nums mt-1">¥{{ formatMoney(totalDgmv) }}</p>
      </div>
      <div class="bg-surface-raised rounded-xl border border-white/[0.04] px-5 py-4">
        <p class="text-[10px] text-trust-300 font-sans uppercase tracking-[0.1em]">人均 DGMV</p>
        <p class="text-[22px] font-extrabold text-brand-light font-mono tabular-nums mt-1">¥{{ formatMoney(avgDgmv) }}</p>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="bg-surface-raised rounded-2xl border border-white/[0.04] overflow-hidden">
      <table v-if="snapshots.length > 0" class="w-full table-fixed">
        <colgroup>
          <col class="w-[5%]" />
          <col class="w-[14%]" />
          <col class="w-[14%]" />
          <col class="w-[10%]" />
          <col class="w-[12%]" />
          <col class="w-[20%]" />
        </colgroup>
        <thead>
          <tr class="border-b border-white/[0.04]">
            <th class="px-3 py-3 text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans text-center">#</th>
            <th class="px-3 py-3 text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans text-left">姓名</th>
            <th class="px-3 py-3 text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans text-left">团队</th>
            <th class="px-3 py-3 text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans text-left">确定职级</th>
            <th class="px-3 py-3 text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans text-left">估算职级</th>
            <th class="px-3 py-3 text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans text-right">季度 DGMV</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(s, idx) in snapshots" :key="s.id" class="border-b border-white/[0.02] hover:bg-white/[0.03] transition-colors duration-150">
            <td class="px-3 py-2.5 text-center">
              <span v-if="idx < 3" :class="['inline-flex items-center justify-center w-5 h-5 rounded-full text-[9px] font-bold font-mono',
                idx === 0 ? 'bg-yellow-500/[0.15] text-yellow-400 border border-yellow-500/[0.2]' :
                idx === 1 ? 'bg-gray-400/[0.12] text-gray-300 border border-gray-400/[0.15]' :
                'bg-amber-700/[0.12] text-amber-500 border border-amber-700/[0.15]']">{{ idx + 1 }}</span>
              <span v-else class="text-[10px] text-trust-300 font-mono">{{ idx + 1 }}</span>
            </td>
            <td class="px-3 py-2.5 text-[12px] font-medium text-gray-200 font-sans">{{ s.userName || '—' }}</td>
            <td class="px-3 py-2.5 text-[11px] text-gray-400 font-sans">{{ s.teamName || '—' }}</td>
            <td class="px-3 py-2.5">
              <span v-if="s.level" class="inline-flex items-center px-1.5 py-0.5 rounded-md text-[9px] font-bold bg-accent/[0.08] text-accent border border-accent/[0.12] font-mono">{{ s.level }}</span>
              <span v-else class="text-[10px] text-trust-400 font-sans">未设定</span>
            </td>
            <td class="px-3 py-2.5">
              <span class="inline-flex items-center px-1.5 py-0.5 rounded-md text-[9px] font-bold bg-brand/[0.08] text-brand-light border border-brand/[0.12] font-mono">{{ s.estimatedLevel || 'K1' }}</span>
            </td>
            <td class="px-3 py-2.5 text-right">
              <span class="text-[13px] font-semibold text-white font-mono tabular-nums">¥{{ formatMoney(s.totalDgmv) }}</span>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else-if="loading" class="py-14 text-center text-trust-300 text-[12px] font-sans">加载中...</div>
      <div v-else-if="selectedQuarter" class="py-14 text-center text-trust-300 text-[12px] font-sans">该季度暂无快照数据</div>
      <div v-else class="py-14 text-center text-trust-300 text-[12px] font-sans">请选择一个季度查看报表</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useConfirm } from '../composables/useConfirm'
import api from '../api'

const auth = useAuthStore()
const { confirm } = useConfirm()

const quarters = ref([])
const selectedQuarter = ref('')
const snapshots = ref([])
const loading = ref(false)

// 生成快照
const showGenerate = ref(false)
const generateQuarter = ref('')
const generateError = ref('')
const generating = ref(false)

const totalDgmv = computed(() => snapshots.value.reduce((s, r) => s + (parseFloat(r.totalDgmv) || 0), 0))
const avgDgmv = computed(() => snapshots.value.length > 0 ? totalDgmv.value / snapshots.value.length : 0)

// 生成过去的季度列表（最近8个季度）
const pastQuarters = computed(() => {
  const result = []
  const now = new Date()
  let year = now.getFullYear()
  let q = Math.ceil((now.getMonth() + 1) / 3)
  for (let i = 0; i < 8; i++) {
    q--
    if (q < 1) { q = 4; year-- }
    result.push(`${year}-Q${q}`)
  }
  return result
})

function formatMoney(v) {
  return (parseFloat(v) || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

async function fetchQuarters() {
  try {
    const res = await api.get('/dashboard/quarterly/quarters')
    quarters.value = res.data || []
    if (quarters.value.length > 0) {
      selectedQuarter.value = quarters.value[0]
      fetchSnapshots()
    }
  } catch { /* ignore */ }
}

async function fetchSnapshots() {
  if (!selectedQuarter.value) return
  loading.value = true
  try {
    const res = await api.get('/dashboard/quarterly/snapshots', { params: { quarter: selectedQuarter.value } })
    snapshots.value = res.data || []
  } catch { snapshots.value = [] }
  loading.value = false
}

async function handleGenerate() {
  generateError.value = ''
  if (!generateQuarter.value) { generateError.value = '请选择季度'; return }
  generating.value = true
  try {
    const res = await api.post('/dashboard/quarterly/generate', { quarter: generateQuarter.value })
    showGenerate.value = false
    await confirm(`已生成 ${res.data?.count || 0} 条快照`, { title: '生成成功', confirmText: '知道了', type: 'brand' })
    // 刷新
    await fetchQuarters()
  } catch (e) {
    generateError.value = e?.message || '生成失败'
  } finally {
    generating.value = false
  }
}

onMounted(() => {
  fetchQuarters()
  if (pastQuarters.value.length > 0) generateQuarter.value = pastQuarters.value[0]
})
</script>
