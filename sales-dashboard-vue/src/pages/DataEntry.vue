<template>
  <div class="max-w-[780px] mx-auto">
    <div class="bg-surface-raised rounded-2xl border border-white/[0.06] overflow-hidden">
      <div class="px-4 sm:px-7 pt-5 sm:pt-6 pb-4">
        <h3 class="text-[14px] font-semibold text-white tracking-tight font-sans">昨日业绩录入</h3>
        <p class="text-[11px] text-trust-300 mt-1 font-sans">按分配的渠道账号录入数据，填写对应日期的 GMV 和退款金额。每日填写前一日业绩，跨季度时不影响每季度业绩计算。</p>
      </div>
      <div class="px-4 sm:px-7 pb-5 sm:pb-7 space-y-5">
        <!-- 日期 -->
        <div>
          <label for="entry-date" class="block text-[10px] font-semibold text-trust-300 uppercase tracking-[0.1em] mb-2 font-sans">日期</label>
          <input id="entry-date" type="date" v-model="date" :max="yesterday" @change="checkSubmitted"
            class="w-full bg-white/[0.03] border border-white/[0.06] rounded-xl px-4 py-2.5 text-[13px] text-white font-mono focus:outline-none focus:ring-2 focus:ring-brand/30 focus:border-brand/30 transition-colors [color-scheme:dark]" />
        </div>

        <!-- 已提交提示 -->
        <div v-if="alreadySubmitted" class="flex items-center gap-3 bg-success/[0.06] border border-success/[0.12] rounded-xl px-5 py-4">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-success shrink-0"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
          <div>
            <p class="text-[12px] font-semibold text-success font-sans">{{ date }} 已完成录入</p>
            <p class="text-[11px] text-trust-300 font-sans mt-0.5">如需修改请联系管理员</p>
          </div>
        </div>

        <!-- 未分配提示 -->
        <div v-else-if="loaded && channels.length === 0" class="flex items-center gap-3 bg-accent/[0.06] border border-accent/[0.12] rounded-xl px-5 py-4">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-accent shrink-0"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          <div>
            <p class="text-[12px] font-semibold text-accent font-sans">暂无分配的渠道账号</p>
            <p class="text-[11px] text-trust-300 font-sans mt-0.5">请联系管理员分配渠道和账号</p>
          </div>
        </div>

        <template v-else>
          <!-- 渠道列表 -->
          <div class="space-y-4">
            <div v-for="ch in channels" :key="ch.code" class="rounded-xl border border-white/[0.06] overflow-hidden">
              <!-- 渠道标题行 -->
              <div class="flex items-center justify-between px-4 py-2.5 bg-white/[0.03] border-b border-white/[0.04]">
                <div class="flex items-center gap-2">
                  <span class="w-6 h-6 rounded-md bg-brand/[0.08] border border-brand/[0.1] flex items-center justify-center text-[9px] font-bold text-brand-light font-mono">{{ ch.code }}</span>
                  <span class="text-[12px] font-semibold text-white font-sans">{{ ch.label }}</span>
                </div>
                <span class="text-[11px] text-trust-300 font-mono tabular-nums">
                  DGMV <span class="text-success-light font-semibold">¥{{ channelDGMV(ch) }}</span>
                </span>
              </div>
              <!-- 账号行（固定，不可添加/删除） -->
              <div class="divide-y divide-white/[0.03]">
                <div v-for="row in ch.rows" :key="row.accountId" class="px-4 py-2.5">
                  <div class="flex items-center gap-2 mb-2 sm:mb-0 sm:hidden">
                    <span class="text-[11px] text-gray-300 font-sans truncate">{{ row.accountName }}</span>
                  </div>
                  <div class="grid grid-cols-2 sm:grid-cols-[1fr_1fr_1fr] gap-2 items-center">
                    <div class="hidden sm:flex items-center gap-2">
                      <span class="text-[11px] text-gray-300 font-sans truncate">{{ row.accountName }}</span>
                    </div>
                    <MoneyInput placeholder="GMV" v-model="row.gmv"
                      class="bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-2 text-[12px] text-white placeholder-trust-400 font-mono focus:outline-none focus:ring-2 focus:ring-brand/30 transition-colors tabular-nums" />
                    <MoneyInput placeholder="退款" v-model="row.refund"
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
          <button @click="handleSubmit"
            :disabled="submitting"
            :class="['w-full py-2.5 rounded-xl text-[13px] font-semibold transition-all duration-200 cursor-pointer font-sans',
              submitted ? 'bg-success text-white scale-[0.98]' : 'bg-brand text-white hover:bg-brand-light active:scale-[0.98] shadow-lg shadow-brand/20 disabled:opacity-50']">
            {{ submitting ? '提交中...' : submitted ? '提交成功' : '提交今日业绩' }}
          </button>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import MoneyInput from '../components/MoneyInput.vue'
import api from '../api'

const today = new Date().toLocaleDateString('sv-SE', { timeZone: 'Asia/Shanghai' })
const yesterday = new Date(Date.now() - 86400000).toLocaleDateString('sv-SE', { timeZone: 'Asia/Shanghai' })
const date = ref(yesterday)
const channels = reactive([])
const submitted = ref(false)
const submitting = ref(false)
const error = ref('')
const alreadySubmitted = ref(false)
const loaded = ref(false)

function channelDGMV(ch) {
  const v = ch.rows.reduce((s, r) => s + (parseFloat(r.gmv) || 0) - (parseFloat(r.refund) || 0), 0)
  return v.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const totalDGMV = computed(() => {
  const v = channels.reduce((s, ch) =>
    s + ch.rows.reduce((rs, r) => rs + (parseFloat(r.gmv) || 0) - (parseFloat(r.refund) || 0), 0), 0)
  return v.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
})

async function checkSubmitted() {
  if (!date.value) return
  try {
    const res = await api.get('/records/check', { params: { date: date.value } })
    alreadySubmitted.value = res.data === true
  } catch { alreadySubmitted.value = false }
}

onMounted(async () => {
  // 获取用户分配的渠道+账号
  try {
    const [assignRes, platformRes] = await Promise.all([
      api.get('/records/my-platforms'),
      api.get('/dict/platform')
    ])
    const assignments = assignRes.data || []
    const platformList = platformRes.data || []
    const platformMap = {}
    platformList.forEach(p => { platformMap[p.code] = p.label })

    // 按 platformCode 分组
    const grouped = {}
    for (const a of assignments) {
      if (!grouped[a.platformCode]) {
        grouped[a.platformCode] = {
          code: a.platformCode,
          label: platformMap[a.platformCode] || a.platformCode,
          rows: []
        }
      }
      grouped[a.platformCode].rows.push({
        accountId: a.accountId,
        accountName: a.accountName,
        gmv: '',
        refund: ''
      })
    }
    // 按平台排序
    const sortedCodes = platformList.map(p => p.code)
    const sorted = sortedCodes.filter(c => grouped[c]).map(c => grouped[c])
    channels.push(...sorted)
  } catch { /* ignore */ }
  loaded.value = true
  checkSubmitted()
})

async function handleSubmit() {
  error.value = ''
  const items = []
  for (const ch of channels) {
    for (const row of ch.rows) {
      if (parseFloat(row.gmv) > 0 || parseFloat(row.refund) > 0) {
        items.push({
          platform: ch.code,
          accountId: row.accountId,
          accountNote: row.accountName,
          gmv: parseFloat(row.gmv) || 0,
          refund: parseFloat(row.refund) || 0
        })
      }
    }
  }
  if (items.length === 0) { error.value = '请至少填写一个渠道的数据'; return }
  submitting.value = true
  try {
    await api.post('/records', { recordDate: date.value, items })
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
