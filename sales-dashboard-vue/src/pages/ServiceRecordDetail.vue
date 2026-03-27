<template>
  <div class="space-y-4">
    <!-- Header -->
    <div class="flex items-center gap-3">
      <button @click="$router.back()" class="flex items-center justify-center w-8 h-8 rounded-xl bg-white/[0.04] border border-white/[0.06] hover:bg-white/[0.08] transition-colors cursor-pointer">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-trust-300"><polyline points="15 18 9 12 15 6"/></svg>
      </button>
      <div>
        <h2 class="text-[15px] font-semibold text-white font-sans">{{ userName }} · 客服业绩详情</h2>
        <p class="text-[11px] text-trust-300 mt-0.5 font-sans">{{ date }}</p>
      </div>
    </div>

    <!-- Summary cards -->
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

    <!-- Detail table -->
    <div class="bg-surface-raised rounded-2xl border border-white/[0.04] overflow-hidden">
      <table class="w-full table-fixed">
        <colgroup>
          <col class="w-[16%]" />
          <col class="w-[16%]" />
          <col class="w-[10%]" />
          <col class="w-[14%]" />
          <col class="w-[16%]" />
          <col class="w-[16%]" />
          <col class="w-[12%]" />
        </colgroup>
        <thead>
          <tr class="border-b border-white/[0.04]">
            <th class="px-4 py-3 text-left text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">渠道</th>
            <th class="px-4 py-3 text-left text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">店铺</th>
            <th class="px-4 py-3 text-left text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">班次</th>
            <th class="px-4 py-3 text-right text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">接待量</th>
            <th class="px-4 py-3 text-right text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">3分钟回复率</th>
            <th class="px-4 py-3 text-right text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">好评率</th>
            <th class="px-4 py-3 text-center text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in detailRecords" :key="r.id" class="border-b border-white/[0.02] hover:bg-white/[0.03] transition-colors duration-150">
            <td class="px-4 py-3">
              <span class="inline-flex items-center px-2 py-0.5 rounded-md text-[10px] font-semibold bg-purple-500/[0.06] text-purple-300 border border-purple-500/[0.1] font-sans">{{ platformMap[r.platform] || r.platform }}</span>
            </td>
            <td class="px-4 py-3 text-[11px] text-trust-300 font-sans">{{ r.shopNote || '—' }}</td>
            <td class="px-4 py-3">
              <span :class="['inline-flex items-center px-1.5 py-0.5 rounded-md text-[9px] font-semibold border font-sans', r.shift === 'morning' ? 'bg-amber-500/[0.08] text-amber-400 border-amber-500/[0.12]' : 'bg-indigo-500/[0.08] text-indigo-400 border-indigo-500/[0.12]']">{{ r.shift === 'morning' ? '早班' : '晚班' }}</span>
            </td>
            <td class="px-4 py-3 text-[12px] text-white font-mono tabular-nums text-right font-medium">{{ r.receptionCount }}</td>
            <td class="px-4 py-3 text-[12px] font-mono tabular-nums text-right" :class="rateColor(r.replyRate)">{{ fmtRate(r.replyRate) }}</td>
            <td class="px-4 py-3 text-[12px] font-mono tabular-nums text-right" :class="rateColor(r.praiseRate)">{{ fmtRate(r.praiseRate) }}</td>
            <td class="px-4 py-3 text-center">
              <button @click="openEdit(r)" class="inline-flex items-center justify-center w-6 h-6 rounded-lg hover:bg-purple-500/[0.1] text-trust-300 hover:text-purple-300 transition-colors cursor-pointer">
                <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="detailRecords.length === 0 && !loading" class="py-14 text-center text-trust-300 text-[12px] font-sans">暂无记录</div>
      <div v-if="loading" class="py-14 text-center text-trust-300 text-[12px] font-sans">加载中...</div>
    </div>

    <!-- 编辑弹窗 -->
    <div v-if="editTarget" class="fixed inset-0 z-50 flex items-center justify-center bg-black/60" @click.self="editTarget = null">
      <div class="bg-surface-raised rounded-2xl border border-white/[0.06] w-[380px] p-6 space-y-4">
        <div class="flex items-center justify-between">
          <h3 class="text-[14px] font-semibold text-white font-sans">修改客服记录</h3>
          <button @click="editTarget = null" class="text-trust-300 hover:text-white cursor-pointer transition-colors">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
        <div class="flex items-center gap-2">
          <span class="inline-flex items-center px-2 py-0.5 rounded-md text-[10px] font-semibold bg-purple-500/[0.06] text-purple-300 border border-purple-500/[0.1] font-sans">{{ platformMap[editTarget.platform] || editTarget.platform }}</span>
          <span class="text-[11px] text-trust-300 font-sans">{{ editTarget.shopNote || '—' }}</span>
          <span :class="['inline-flex items-center px-1.5 py-0.5 rounded-md text-[9px] font-semibold border font-sans', editTarget.shift === 'morning' ? 'bg-amber-500/[0.08] text-amber-400 border-amber-500/[0.12]' : 'bg-indigo-500/[0.08] text-indigo-400 border-indigo-500/[0.12]']">{{ editTarget.shift === 'morning' ? '早班' : '晚班' }}</span>
        </div>
        <div class="space-y-3">
          <div>
            <label class="block text-[10px] font-semibold text-trust-300 uppercase tracking-[0.1em] mb-1.5 font-sans">班次</label>
            <select v-model="editForm.shift" class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-2 text-[12px] text-gray-300 font-sans focus:outline-none focus:ring-2 focus:ring-purple-500/30 cursor-pointer">
              <option value="morning">早班</option>
              <option value="evening">晚班</option>
            </select>
          </div>
          <div>
            <label class="block text-[10px] font-semibold text-trust-300 uppercase tracking-[0.1em] mb-1.5 font-sans">接待量</label>
            <input type="number" v-model="editForm.receptionCount" min="0"
              class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-2 text-[12px] text-white font-mono focus:outline-none focus:ring-2 focus:ring-purple-500/30 transition-colors tabular-nums" />
          </div>
          <div>
            <label class="block text-[10px] font-semibold text-trust-300 uppercase tracking-[0.1em] mb-1.5 font-sans">3分钟回复率 (%)</label>
            <input type="number" v-model="editForm.replyRate" min="0" max="100" step="0.1"
              class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-2 text-[12px] text-white font-mono focus:outline-none focus:ring-2 focus:ring-purple-500/30 transition-colors tabular-nums" />
          </div>
          <div>
            <label class="block text-[10px] font-semibold text-trust-300 uppercase tracking-[0.1em] mb-1.5 font-sans">好评率 (%)</label>
            <input type="number" v-model="editForm.praiseRate" min="0" max="100" step="0.1"
              class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-2 text-[12px] text-white font-mono focus:outline-none focus:ring-2 focus:ring-purple-500/30 transition-colors tabular-nums" />
          </div>
        </div>
        <p v-if="editError" class="text-[11px] text-danger-light font-sans">{{ editError }}</p>
        <div class="flex items-center justify-end gap-3 pt-1">
          <button @click="editTarget = null" class="px-4 py-2 rounded-lg text-[12px] text-trust-300 hover:text-white hover:bg-white/[0.04] transition-colors cursor-pointer font-sans">取消</button>
          <button @click="saveEdit" :disabled="saving" class="px-4 py-2 rounded-lg text-[12px] font-medium text-white bg-purple-500 hover:bg-purple-400 transition-colors cursor-pointer font-sans disabled:opacity-50">
            {{ saving ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
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

// 编辑状态
const editTarget = ref(null)
const editForm = ref({ shift: 'morning', receptionCount: 0, replyRate: 0, praiseRate: 0 })
const editError = ref('')
const saving = ref(false)

function openEdit(r) {
  editTarget.value = r
  editForm.value = {
    shift: r.shift,
    receptionCount: r.receptionCount,
    replyRate: Number(r.replyRate),
    praiseRate: Number(r.praiseRate),
  }
  editError.value = ''
}

async function saveEdit() {
  editError.value = ''
  saving.value = true
  try {
    await api.put(`/service-records/${editTarget.value.id}`, {
      shift: editForm.value.shift,
      receptionCount: parseInt(editForm.value.receptionCount) || 0,
      replyRate: parseFloat(editForm.value.replyRate) || 0,
      praiseRate: parseFloat(editForm.value.praiseRate) || 0,
    })
    editTarget.value = null
    await fetchDetail()
  } catch (e) {
    editError.value = e?.message || '保存失败'
  } finally {
    saving.value = false
  }
}

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
