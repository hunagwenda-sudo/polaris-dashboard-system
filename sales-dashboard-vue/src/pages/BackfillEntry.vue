<template>
  <div class="max-w-[780px] mx-auto">
    <div class="bg-surface-raised rounded-2xl border border-white/[0.06] overflow-hidden">
      <div class="px-4 sm:px-7 pt-5 sm:pt-6 pb-4">
        <h3 class="text-[14px] font-semibold text-white tracking-tight font-sans">业绩补录</h3>
        <p class="text-[11px] text-trust-300 mt-1 font-sans">为指定员工补录某天的业绩数据，选择员工后自动加载其分配的渠道账号。</p>
      </div>
      <div class="px-4 sm:px-7 pb-5 sm:pb-7 space-y-5">
        <!-- 选择员工 -->
        <div>
          <label class="block text-[10px] font-semibold text-trust-300 uppercase tracking-[0.1em] mb-2 font-sans">选择员工</label>
          <select v-model="selectedUserId" @change="onUserChange"
            class="w-full bg-white/[0.03] border border-white/[0.06] rounded-xl px-4 py-2.5 text-[13px] text-white font-sans focus:outline-none focus:ring-2 focus:ring-brand/30 focus:border-brand/30 transition-colors [color-scheme:dark] cursor-pointer">
            <option value="" disabled class="bg-trust-800 text-trust-300">请选择员工</option>
            <option v-for="u in users" :key="u.id" :value="u.id" class="bg-trust-800">{{ u.name }}（{{ u.username }}）</option>
          </select>
        </div>

        <!-- 日期 -->
        <div>
          <label class="block text-[10px] font-semibold text-trust-300 uppercase tracking-[0.1em] mb-2 font-sans">日期</label>
          <input type="date" v-model="date" :max="yesterday" @change="checkSubmitted"
            class="w-full bg-white/[0.03] border border-white/[0.06] rounded-xl px-4 py-2.5 text-[13px] text-white font-mono focus:outline-none focus:ring-2 focus:ring-brand/30 focus:border-brand/30 transition-colors [color-scheme:dark]" />
        </div>

        <!-- 已提交提示 -->
        <div v-if="alreadySubmitted" class="flex items-center gap-3 bg-accent/[0.06] border border-accent/[0.12] rounded-xl px-5 py-4">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-accent shrink-0"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          <div>
            <p class="text-[12px] font-semibold text-accent font-sans">该员工 {{ date }} 已有录入记录</p>
            <p class="text-[11px] text-trust-300 font-sans mt-0.5">继续提交将新增记录，不会覆盖已有数据</p>
          </div>
        </div>

        <!-- 未分配提示 -->
        <div v-if="selectedUserId && loaded && channels.length === 0" class="flex items-center gap-3 bg-accent/[0.06] border border-accent/[0.12] rounded-xl px-5 py-4">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-accent shrink-0"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          <div>
            <p class="text-[12px] font-semibold text-accent font-sans">该员工暂无分配的渠道账号</p>
            <p class="text-[11px] text-trust-300 font-sans mt-0.5">请先在人员管理中为其分配渠道</p>
          </div>
        </div>

        <template v-if="channels.length > 0">
          <!-- 渠道列表 -->
          <div class="space-y-4">
            <div v-for="ch in channels" :key="ch.code" class="rounded-xl border border-white/[0.06] overflow-hidden">
              <div class="flex items-center justify-between px-4 py-2.5 bg-white/[0.03] border-b border-white/[0.04]">
                <div class="flex items-center gap-2">
                  <span class="w-6 h-6 rounded-md bg-brand/[0.08] border border-brand/[0.1] flex items-center justify-center text-[9px] font-bold text-brand-light font-mono">{{ ch.code }}</span>
                  <span class="text-[12px] font-semibold text-white font-sans">{{ ch.label }}</span>
                </div>
                <span class="text-[11px] text-trust-300 font-mono tabular-nums">
                  DGMV <span class="text-success-light font-semibold">¥{{ channelDGMV(ch) }}</span>
                </span>
              </div>
              <div class="divide-y divide-white/[0.03]">
                <div v-for="row in ch.rows" :key="row.accountId" class="px-4 py-2.5">
                  <div class="flex items-center gap-2 mb-2 sm:mb-0 sm:hidden">
                    <span class="text-[11px] text-gray-300 font-sans truncate">{{ row.accountName }}</span>
                  </div>
                  <div class="grid grid-cols-2 sm:grid-cols-[1fr_1fr_1fr] gap-2 items-center">
                    <div class="hidden sm:flex items-center gap-2">
                      <span class="text-[11px] text-gray-300 font-sans truncate">{{ row.accountName }}</span>
                    </div>
                    <input type="number" placeholder="GMV" v-model="row.gmv"
                      class="bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-2 text-[12px] text-white placeholder-trust-400 font-mono focus:outline-none focus:ring-2 focus:ring-brand/30 transition-colors tabular-nums" />
                    <input type="number" placeholder="退款" v-model="row.refund"
                      class="bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-2 text-[12px] text-white placeholder-trust-400 font-mono focus:outline-none focus:ring-2 focus:ring-danger/30 transition-colors tabular-nums" />
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 合计 -->
          <div class="bg-white/[0.02] rounded-xl px-5 py-3.5 flex items-center justify-between border border-white/[0.04]">
            <span class="text-[10px] font-semibold text-trust-300 uppercase tracking-[0.1em] font-sans">当日 DGMV 合计</span>
            <span class="text-[20px] font-extrabold text-white font-mono tabular-nums">¥{{ totalDGMV }}</span>
          </div>

          <p v-if="error" class="text-red-400 text-[11px] font-sans">{{ error }}</p>
          <button @click="handleSubmit" :disabled="submitting"
            :class="['w-full py-2.5 rounded-xl text-[13px] font-semibold transition-all duration-200 cursor-pointer font-sans',
              submitted ? 'bg-success text-white scale-[0.98]' : 'bg-brand text-white hover:bg-brand-light active:scale-[0.98] shadow-lg shadow-brand/20 disabled:opacity-50']">
            {{ submitting ? '提交中...' : submitted ? '补录成功' : '提交补录' }}
          </button>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import api from '../api'

const yesterday = new Date(Date.now() - 86400000).toLocaleDateString('sv-SE', { timeZone: 'Asia/Shanghai' })
const date = ref(yesterday)
const users = ref([])
const selectedUserId = ref('')
const channels = reactive([])
const loaded = ref(false)
const alreadySubmitted = ref(false)
const submitted = ref(false)
const submitting = ref(false)
const error = ref('')
const platformMap = ref({})

function channelDGMV(ch) {
  const v = ch.rows.reduce((s, r) => s + (parseFloat(r.gmv) || 0) - (parseFloat(r.refund) || 0), 0)
  return v.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const totalDGMV = computed(() => {
  const v = channels.reduce((s, ch) =>
    s + ch.rows.reduce((rs, r) => rs + (parseFloat(r.gmv) || 0) - (parseFloat(r.refund) || 0), 0), 0)
  return v.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
})

onMounted(async () => {
  const [userRes, platformRes] = await Promise.all([
    api.get('/users', { params: { page: 1, size: 500 } }),
    api.get('/dict/platform')
  ])
  const allUsers = userRes.data?.records || userRes.data || []
  users.value = allUsers.filter(u => u.status === 'active' && u.role !== 'admin')
  const pList = platformRes.data || []
  pList.forEach(p => { platformMap.value[p.code] = p.label })
})

async function onUserChange() {
  channels.splice(0)
  loaded.value = false
  alreadySubmitted.value = false
  if (!selectedUserId.value) return
  try {
    const res = await api.get(`/records/user-platforms/${selectedUserId.value}`)
    const assignments = res.data || []
    const grouped = {}
    for (const a of assignments) {
      if (!grouped[a.platformCode]) {
        grouped[a.platformCode] = {
          code: a.platformCode,
          label: platformMap.value[a.platformCode] || a.platformCode,
          rows: []
        }
      }
      grouped[a.platformCode].rows.push({ accountId: a.accountId, accountName: a.accountName, gmv: '', refund: '' })
    }
    const sorted = Object.values(grouped)
    channels.push(...sorted)
  } catch { /* ignore */ }
  loaded.value = true
  checkSubmitted()
}

async function checkSubmitted() {
  if (!selectedUserId.value || !date.value) { alreadySubmitted.value = false; return }
  try {
    // 用管理员身份查该用户是否已提交（复用 records 列表接口）
    const res = await api.get('/records', { params: { userId: selectedUserId.value, startDate: date.value, endDate: date.value, page: 1, size: 1 } })
    const records = res.data?.records || []
    alreadySubmitted.value = records.length > 0
  } catch { alreadySubmitted.value = false }
}

async function handleSubmit() {
  error.value = ''
  if (!selectedUserId.value) { error.value = '请选择员工'; return }
  if (!date.value) { error.value = '请选择日期'; return }
  const items = []
  for (const ch of channels) {
    for (const row of ch.rows) {
      if (parseFloat(row.gmv) > 0 || parseFloat(row.refund) > 0) {
        items.push({ platform: ch.code, accountId: row.accountId, accountNote: row.accountName, gmv: parseFloat(row.gmv) || 0, refund: parseFloat(row.refund) || 0 })
      }
    }
  }
  if (items.length === 0) { error.value = '请至少填写一个渠道的数据'; return }
  submitting.value = true
  try {
    await api.post('/records/backfill', { userId: selectedUserId.value, recordDate: date.value, items })
    submitted.value = true
    alreadySubmitted.value = true
    setTimeout(() => { submitted.value = false }, 2000)
    channels.forEach(ch => { ch.rows.forEach(r => { r.gmv = ''; r.refund = '' }) })
  } catch (e) {
    error.value = e?.message || '提交失败'
  } finally {
    submitting.value = false
  }
}
</script>
