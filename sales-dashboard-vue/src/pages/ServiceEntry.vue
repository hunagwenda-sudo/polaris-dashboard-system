<template>
  <div class="max-w-[780px] mx-auto">
    <div class="bg-surface-raised rounded-2xl border border-white/[0.06] overflow-hidden">
      <div class="px-4 sm:px-7 pt-5 sm:pt-6 pb-4">
        <h3 class="text-[14px] font-semibold text-white tracking-tight font-sans">客服日报录入</h3>
        <p class="text-[11px] text-trust-300 mt-1 font-sans">填写各渠道班次的接待量、回复率、好评率</p>
      </div>
      <div class="px-4 sm:px-7 pb-5 sm:pb-7 space-y-5">
        <!-- 日期 -->
        <div>
          <label for="svc-date" class="block text-[10px] font-semibold text-trust-300 uppercase tracking-[0.1em] mb-2 font-sans">日期</label>
          <input id="svc-date" type="date" v-model="date" :max="today"
            class="w-full bg-white/[0.03] border border-white/[0.06] rounded-xl px-4 py-2.5 text-[13px] text-white font-mono focus:outline-none focus:ring-2 focus:ring-brand/30 focus:border-brand/30 transition-colors [color-scheme:dark]" />
        </div>

        <!-- 渠道 × 班次 表格 -->
        <div class="space-y-1">
          <!-- Desktop header -->
          <div class="hidden sm:grid grid-cols-[1fr_0.7fr_0.8fr_0.8fr_0.8fr] gap-2 text-[9px] font-semibold text-trust-300 tracking-[0.12em] uppercase px-1 mb-2 font-sans">
            <span>渠道</span><span>班次</span><span>接待量</span><span>3分钟回复率</span><span>好评率</span>
          </div>
          <div v-for="(row, i) in rows" :key="i" class="rounded-xl sm:rounded-none border border-white/[0.04] sm:border-0 p-3 sm:p-0 space-y-2 sm:space-y-0">
            <!-- Mobile: stacked layout -->
            <div class="sm:hidden space-y-2.5">
              <div class="flex items-center gap-2">
                <span class="w-7 h-7 rounded-lg bg-brand/[0.08] border border-brand/[0.1] flex items-center justify-center text-[9px] font-bold text-brand-light font-mono">{{ row.abbr }}</span>
                <span class="text-[12px] font-medium text-trust-300 font-sans">{{ row.name }}</span>
              </div>
              <select v-model="row.shift" class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-2 text-[11px] text-gray-300 font-sans focus:outline-none focus:ring-2 focus:ring-brand/30 cursor-pointer">
                <option value="morning">早班</option>
                <option value="evening">晚班</option>
              </select>
              <div class="grid grid-cols-3 gap-2">
                <div>
                  <label class="block text-[9px] text-trust-400 mb-1 font-sans">接待量</label>
                  <input type="number" placeholder="0" v-model="row.receptionCount" min="0"
                    class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-2 text-[12px] text-white placeholder-trust-400 font-mono focus:outline-none focus:ring-2 focus:ring-brand/30 transition-colors tabular-nums" />
                </div>
                <div>
                  <label class="block text-[9px] text-trust-400 mb-1 font-sans">回复率</label>
                  <div class="relative">
                    <input type="number" placeholder="0" v-model="row.replyRate" min="0" max="100" step="0.1"
                      class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-2 pr-7 text-[12px] text-white placeholder-trust-400 font-mono focus:outline-none focus:ring-2 focus:ring-brand/30 transition-colors tabular-nums" />
                    <span class="absolute right-2.5 top-1/2 -translate-y-1/2 text-[10px] text-trust-400 font-mono">%</span>
                  </div>
                </div>
                <div>
                  <label class="block text-[9px] text-trust-400 mb-1 font-sans">好评率</label>
                  <div class="relative">
                    <input type="number" placeholder="0" v-model="row.praiseRate" min="0" max="100" step="0.1"
                      class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-2 pr-7 text-[12px] text-white placeholder-trust-400 font-mono focus:outline-none focus:ring-2 focus:ring-brand/30 transition-colors tabular-nums" />
                    <span class="absolute right-2.5 top-1/2 -translate-y-1/2 text-[10px] text-trust-400 font-mono">%</span>
                  </div>
                </div>
              </div>
            </div>
            <!-- Desktop: row layout -->
            <div class="hidden sm:grid grid-cols-[1fr_0.7fr_0.8fr_0.8fr_0.8fr] gap-2 items-center py-1">
              <div class="flex items-center gap-2">
                <span class="w-7 h-7 rounded-lg bg-brand/[0.08] border border-brand/[0.1] flex items-center justify-center text-[9px] font-bold text-brand-light font-mono">{{ row.abbr }}</span>
                <span class="text-[12px] font-medium text-trust-300 font-sans">{{ row.name }}</span>
              </div>
              <select v-model="row.shift" class="bg-white/[0.03] border border-white/[0.06] rounded-lg px-2 py-2 text-[11px] text-gray-300 font-sans focus:outline-none focus:ring-2 focus:ring-brand/30 cursor-pointer">
                <option value="morning">早班</option>
                <option value="evening">晚班</option>
              </select>
              <input type="number" placeholder="0" v-model="row.receptionCount" min="0"
                class="bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-2 text-[12px] text-white placeholder-trust-400 font-mono focus:outline-none focus:ring-2 focus:ring-brand/30 transition-colors tabular-nums" />
              <div class="relative">
                <input type="number" placeholder="0" v-model="row.replyRate" min="0" max="100" step="0.1"
                  class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-2 pr-7 text-[12px] text-white placeholder-trust-400 font-mono focus:outline-none focus:ring-2 focus:ring-brand/30 transition-colors tabular-nums" />
                <span class="absolute right-2.5 top-1/2 -translate-y-1/2 text-[10px] text-trust-400 font-mono">%</span>
              </div>
              <div class="relative">
                <input type="number" placeholder="0" v-model="row.praiseRate" min="0" max="100" step="0.1"
                  class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-2 pr-7 text-[12px] text-white placeholder-trust-400 font-mono focus:outline-none focus:ring-2 focus:ring-brand/30 transition-colors tabular-nums" />
                <span class="absolute right-2.5 top-1/2 -translate-y-1/2 text-[10px] text-trust-400 font-mono">%</span>
              </div>
            </div>
          </div>
        </div>

        <p v-if="error" class="text-red-400 text-[11px] font-sans">{{ error }}</p>
        <button @click="handleSubmit" :disabled="submitting"
          :class="['w-full py-2.5 rounded-xl text-[13px] font-semibold transition-all duration-200 cursor-pointer font-sans',
            submitted ? 'bg-success text-white scale-[0.98]' : 'bg-brand text-white hover:bg-brand-light active:scale-[0.98] shadow-lg shadow-brand/20 disabled:opacity-50']">
          {{ submitting ? '提交中...' : submitted ? '提交成功' : '提交日报' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '../api'

const today = new Date().toLocaleDateString('sv-SE', { timeZone: 'Asia/Shanghai' })
const date = ref(today)
const rows = reactive([])
const submitted = ref(false)
const submitting = ref(false)
const error = ref('')

onMounted(async () => {
  try {
    const res = await api.get('/dict/platform')
    const platforms = res.data || []
    rows.push(...platforms.map(p => ({
      name: p.label, abbr: p.code, shift: 'morning',
      receptionCount: '', replyRate: '', praiseRate: '',
    })))
  } catch { /* empty */ }
})

async function handleSubmit() {
  error.value = ''
  const items = rows
    .filter(r => parseInt(r.receptionCount) > 0 || parseFloat(r.replyRate) > 0 || parseFloat(r.praiseRate) > 0)
    .map(r => ({
      platform: r.abbr,
      shift: r.shift,
      receptionCount: parseInt(r.receptionCount) || 0,
      replyRate: parseFloat(r.replyRate) || 0,
      praiseRate: parseFloat(r.praiseRate) || 0,
    }))
  if (items.length === 0) { error.value = '请至少填写一个渠道的数据'; return }
  submitting.value = true
  try {
    await api.post('/service-records', { recordDate: date.value, items })
    submitted.value = true
    setTimeout(() => { submitted.value = false }, 2000)
    rows.forEach(r => { r.receptionCount = ''; r.replyRate = ''; r.praiseRate = ''; r.shift = 'morning' })
  } catch (e) {
    error.value = e?.message || '提交失败'
  } finally {
    submitting.value = false
  }
}
</script>
