<template>
  <div class="max-w-[680px] mx-auto">
    <div class="bg-surface-raised rounded-2xl border border-white/[0.06] overflow-hidden">
      <div class="px-7 pt-6 pb-4">
        <h3 class="text-[14px] font-semibold text-white tracking-tight font-sans">每日业绩录入</h3>
        <p class="text-[11px] text-trust-300 mt-1 font-sans">每个渠道可添加多个账号分别录入</p>
      </div>
      <div class="px-7 pb-7 space-y-5">
        <!-- 日期 -->
        <div>
          <label for="entry-date" class="block text-[10px] font-semibold text-trust-300 uppercase tracking-[0.1em] mb-2 font-sans">日期</label>
          <input id="entry-date" type="date" v-model="date" :max="today" @change="checkSubmitted"
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
                <div class="flex items-center gap-4">
                  <span class="text-[11px] text-trust-300 font-mono tabular-nums">
                    DGMV <span class="text-success-light font-semibold">¥{{ channelDGMV(ch) }}</span>
                  </span>
                  <button @click="addRow(ch)" class="flex items-center gap-1 text-[10px] text-brand-light hover:text-white transition-colors cursor-pointer font-sans">
                    <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                    添加账号
                  </button>
                </div>
              </div>
              <!-- 账号行 -->
              <div class="divide-y divide-white/[0.03]">
                <div v-for="(row, idx) in ch.rows" :key="idx" class="grid grid-cols-[1fr_1fr_1fr_auto] gap-2 items-center px-4 py-2.5">
                  <input type="text" :placeholder="ch.rows.length > 1 ? '账号备注' : '账号备注（可选）'" v-model="row.accountNote"
                    class="bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-2 text-[12px] text-white placeholder-trust-400 font-sans focus:outline-none focus:ring-2 focus:ring-brand/30 transition-colors" />
                  <input type="number" placeholder="GMV" v-model="row.gmv"
                    class="bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-2 text-[12px] text-white placeholder-trust-400 font-mono focus:outline-none focus:ring-2 focus:ring-brand/30 transition-colors tabular-nums" />
                  <input type="number" placeholder="退款" v-model="row.refund"
                    class="bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-2 text-[12px] text-white placeholder-trust-400 font-mono focus:outline-none focus:ring-2 focus:ring-danger/30 transition-colors tabular-nums" />
                  <button v-if="ch.rows.length > 1" @click="removeRow(ch, idx)"
                    class="w-7 h-7 flex items-center justify-center rounded-lg hover:bg-danger/[0.1] text-trust-400 hover:text-danger-light transition-colors cursor-pointer">
                    <svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                  </button>
                  <div v-else class="w-7" />
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
import api from '../api'

const today = new Date().toLocaleDateString('sv-SE', { timeZone: 'Asia/Shanghai' })
const date = ref(today)
const channels = reactive([])
const submitted = ref(false)
const submitting = ref(false)
const error = ref('')
const alreadySubmitted = ref(false)

function addRow(ch) {
  ch.rows.push({ accountNote: '', gmv: '', refund: '' })
}

function removeRow(ch, idx) {
  ch.rows.splice(idx, 1)
}

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
  try {
    const res = await api.get('/dict/platform')
    const platforms = res.data || []
    channels.push(...platforms.map(p => ({
      code: p.code,
      label: p.label,
      rows: [{ accountNote: '', gmv: '', refund: '' }]
    })))
  } catch { /* ignore */ }
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
          accountNote: row.accountNote?.trim() || '',
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
    channels.forEach(ch => { ch.rows = [{ accountNote: '', gmv: '', refund: '' }] })
  } catch (e) {
    error.value = e?.message || '提交失败'
  } finally {
    submitting.value = false
  }
}
</script>
