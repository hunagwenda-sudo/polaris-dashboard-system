<template>
  <div class="space-y-4">
    <!-- Header -->
    <div class="flex items-center gap-3">
      <button @click="$router.back()" class="flex items-center justify-center w-8 h-8 rounded-xl bg-white/[0.04] border border-white/[0.06] hover:bg-white/[0.08] transition-colors cursor-pointer">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-trust-300"><polyline points="15 18 9 12 15 6"/></svg>
      </button>
      <div>
        <h2 class="text-[15px] font-semibold text-white font-sans">{{ userName }} · 业绩详情</h2>
        <p class="text-[11px] text-trust-300 mt-0.5 font-sans">{{ date }}</p>
      </div>
    </div>

    <!-- Summary cards -->
    <div class="grid grid-cols-3 gap-3">
      <div class="bg-surface-raised rounded-2xl border border-white/[0.04] p-4">
        <p class="text-[10px] text-trust-300 font-sans mb-1">总 GMV</p>
        <p class="text-[20px] font-extrabold text-white font-mono tabular-nums">{{ fmt(totalGmv) }}</p>
      </div>
      <div class="bg-surface-raised rounded-2xl border border-white/[0.04] p-4">
        <p class="text-[10px] text-trust-300 font-sans mb-1">总退款</p>
        <p class="text-[20px] font-extrabold text-danger-light/70 font-mono tabular-nums">-{{ fmt(totalRefund) }}</p>
      </div>
      <div class="bg-surface-raised rounded-2xl border border-white/[0.04] p-4">
        <p class="text-[10px] text-trust-300 font-sans mb-1">总 DGMV</p>
        <p class="text-[20px] font-extrabold text-success-light font-mono tabular-nums">{{ fmt(totalDgmv) }}</p>
      </div>
    </div>

    <!-- Detail table -->
    <div class="bg-surface-raised rounded-2xl border border-white/[0.04] overflow-hidden">
      <table class="w-full table-fixed">
        <colgroup>
          <col :class="canEdit ? 'w-[20%]' : 'w-[22%]'" />
          <col :class="canEdit ? 'w-[20%]' : 'w-[22%]'" />
          <col :class="canEdit ? 'w-[18%]' : 'w-[20%]'" />
          <col :class="canEdit ? 'w-[16%]' : 'w-[18%]'" />
          <col :class="canEdit ? 'w-[16%]' : 'w-[18%]'" />
          <col v-if="canEdit" class="w-[10%]" />
        </colgroup>
        <thead>
          <tr class="border-b border-white/[0.04]">
            <th class="px-4 py-3 text-left text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">渠道</th>
            <th class="px-4 py-3 text-left text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">账号备注</th>
            <th class="px-4 py-3 text-right text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">GMV</th>
            <th class="px-4 py-3 text-right text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">退款</th>
            <th class="px-4 py-3 text-right text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">DGMV</th>
            <th v-if="canEdit" class="px-4 py-3 text-center text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in detailRecords" :key="r.id" class="border-b border-white/[0.02] hover:bg-white/[0.03] transition-colors duration-150">
            <td class="px-4 py-3">
              <span class="inline-flex items-center px-2 py-0.5 rounded-md text-[10px] font-semibold bg-brand/[0.06] text-brand-light border border-brand/[0.1] font-sans">{{ platformMap[r.platform] || r.platform }}</span>
            </td>
            <td class="px-4 py-3 text-[11px] text-trust-300 font-sans">{{ r.accountNote || '—' }}</td>
            <td class="px-4 py-3 text-[12px] text-gray-300 font-mono tabular-nums text-right">{{ fmt(r.gmv) }}</td>
            <td class="px-4 py-3 text-[12px] text-danger-light/70 font-mono tabular-nums text-right">-{{ fmt(r.refund) }}</td>
            <td class="px-4 py-3 text-[13px] text-success-light font-mono tabular-nums font-bold text-right">{{ fmt(r.dgmv) }}</td>
            <td v-if="canEdit" class="px-4 py-3 text-center">
              <button @click="openEdit(r)" class="inline-flex items-center justify-center w-6 h-6 rounded-lg hover:bg-brand/[0.1] text-trust-300 hover:text-brand-light transition-colors cursor-pointer">
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
          <h3 class="text-[14px] font-semibold text-white font-sans">修改业绩记录</h3>
          <button @click="editTarget = null" class="text-trust-300 hover:text-white cursor-pointer transition-colors">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
        <div class="flex items-center gap-2">
          <span class="inline-flex items-center px-2 py-0.5 rounded-md text-[10px] font-semibold bg-brand/[0.06] text-brand-light border border-brand/[0.1] font-sans">{{ platformMap[editTarget.platform] || editTarget.platform }}</span>
          <span class="text-[11px] text-trust-300 font-sans">{{ editTarget.accountNote || '默认账号' }}</span>
        </div>
        <div class="space-y-3">
          <div>
            <label class="block text-[10px] font-semibold text-trust-300 uppercase tracking-[0.1em] mb-1.5 font-sans">账号备注</label>
            <input type="text" v-model="editForm.accountNote" placeholder="可留空"
              class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-2 text-[12px] text-white placeholder-trust-400 font-sans focus:outline-none focus:ring-2 focus:ring-brand/30 transition-colors" />
          </div>
          <div>
            <label class="block text-[10px] font-semibold text-trust-300 uppercase tracking-[0.1em] mb-1.5 font-sans">GMV</label>
            <MoneyInput v-model="editForm.gmv"
              class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-2 text-[12px] text-white font-mono focus:outline-none focus:ring-2 focus:ring-brand/30 transition-colors tabular-nums" />
          </div>
          <div>
            <label class="block text-[10px] font-semibold text-trust-300 uppercase tracking-[0.1em] mb-1.5 font-sans">退款</label>
            <MoneyInput v-model="editForm.refund"
              class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-2 text-[12px] text-white font-mono focus:outline-none focus:ring-2 focus:ring-danger/30 transition-colors tabular-nums" />
          </div>
          <div class="bg-white/[0.02] rounded-lg px-4 py-2.5 flex items-center justify-between border border-white/[0.04]">
            <span class="text-[10px] text-trust-300 font-sans">DGMV 预览</span>
            <span class="text-[14px] font-bold text-success-light font-mono tabular-nums">
              ¥{{ ((parseFloat(editForm.gmv) || 0) - (parseFloat(editForm.refund) || 0)).toLocaleString() }}
            </span>
          </div>
        </div>
        <p v-if="editError" class="text-[11px] text-danger-light font-sans">{{ editError }}</p>
        <div class="flex items-center justify-end gap-3 pt-1">
          <button @click="editTarget = null" class="px-4 py-2 rounded-lg text-[12px] text-trust-300 hover:text-white hover:bg-white/[0.04] transition-colors cursor-pointer font-sans">取消</button>
          <button @click="saveEdit" :disabled="saving" class="px-4 py-2 rounded-lg text-[12px] font-medium text-white bg-brand hover:bg-brand/80 transition-colors cursor-pointer font-sans disabled:opacity-50">
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
import { useAuthStore } from '../stores/auth'
import MoneyInput from '../components/MoneyInput.vue'
import api from '../api'

const route = useRoute()
const auth = useAuthStore()
const isAdmin = computed(() => auth.user?.role === 'admin')
const canEdit = computed(() => isAdmin.value || String(auth.user?.id) === String(userId.value))

const userId = computed(() => route.params.userId)
const date = computed(() => route.query.date || '')

const fmt = (n) => `¥${Number(n).toLocaleString()}`
const detailRecords = ref([])
const userName = ref('')
const platformMap = ref({})
const loading = ref(true)

const totalGmv = computed(() => detailRecords.value.reduce((s, r) => s + (Number(r.gmv) || 0), 0))
const totalRefund = computed(() => detailRecords.value.reduce((s, r) => s + (Number(r.refund) || 0), 0))
const totalDgmv = computed(() => detailRecords.value.reduce((s, r) => s + (Number(r.dgmv) || 0), 0))

// 编辑状态
const editTarget = ref(null)
const editForm = ref({ gmv: '', refund: '', accountNote: '' })
const editError = ref('')
const saving = ref(false)

function openEdit(r) {
  editTarget.value = r
  editForm.value = { gmv: r.gmv, refund: r.refund, accountNote: r.accountNote || '' }
  editError.value = ''
}

async function saveEdit() {
  editError.value = ''
  const gmv = parseFloat(editForm.value.gmv)
  const refund = parseFloat(editForm.value.refund)
  if (isNaN(gmv) || gmv < 0) { editError.value = 'GMV 不能为负'; return }
  if (isNaN(refund) || refund < 0) { editError.value = '退款不能为负'; return }
  saving.value = true
  try {
    await api.put(`/records/${editTarget.value.id}`, {
      gmv,
      refund,
      accountNote: editForm.value.accountNote
    })
    editTarget.value = null
    await fetchDetail()
  } catch (e) {
    editError.value = e?.message || '保存失败'
  } finally {
    saving.value = false
  }
}

async function fetchDetail() {
  loading.value = true
  try {
    const params = { userId: userId.value, startDate: date.value, endDate: date.value, size: 100 }
    const res = await api.get('/records', { params })
    detailRecords.value = res.data?.records || []
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
