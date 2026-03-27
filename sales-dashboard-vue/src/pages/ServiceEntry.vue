<template>
  <div class="max-w-[780px] mx-auto">
    <div class="bg-surface-raised rounded-2xl border border-white/[0.06] overflow-hidden">
      <div class="px-4 sm:px-7 pt-5 sm:pt-6 pb-4">
        <h3 class="text-[14px] font-semibold text-white tracking-tight font-sans">客服日报录入</h3>
        <p class="text-[11px] text-trust-300 mt-1 font-sans">按分配的渠道店铺录入数据，填写班次、接待量、回复率、好评率</p>
      </div>
      <div class="px-4 sm:px-7 pb-5 sm:pb-7 space-y-5">
        <!-- 日期 -->
        <div>
          <label for="svc-date" class="block text-[10px] font-semibold text-trust-300 uppercase tracking-[0.1em] mb-2 font-sans">日期</label>
          <input id="svc-date" type="date" v-model="date" :max="today"
            class="w-full bg-white/[0.03] border border-white/[0.06] rounded-xl px-4 py-2.5 text-[13px] text-white font-mono focus:outline-none focus:ring-2 focus:ring-brand/30 focus:border-brand/30 transition-colors [color-scheme:dark]" />
        </div>

        <!-- 未分配提示 -->
        <div v-if="loaded && channels.length === 0" class="flex items-center gap-3 bg-accent/[0.06] border border-accent/[0.12] rounded-xl px-5 py-4">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-accent shrink-0"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          <div>
            <p class="text-[12px] font-semibold text-accent font-sans">暂无分配的渠道店铺</p>
            <p class="text-[11px] text-trust-300 font-sans mt-0.5">请联系管理员分配渠道和店铺</p>
          </div>
        </div>

        <template v-else-if="loaded">
          <!-- 渠道列表（和运营一样的层级结构） -->
          <div class="space-y-4">
            <div v-for="ch in channels" :key="ch.code" class="rounded-xl border border-white/[0.06] overflow-hidden">
              <!-- 渠道标题行 -->
              <div class="flex items-center justify-between px-4 py-2.5 bg-white/[0.03] border-b border-white/[0.04]">
                <div class="flex items-center gap-2">
                  <span class="w-6 h-6 rounded-md bg-brand/[0.08] border border-brand/[0.1] flex items-center justify-center text-[9px] font-bold text-brand-light font-mono">{{ ch.code }}</span>
                  <span class="text-[12px] font-semibold text-white font-sans">{{ ch.label }}</span>
                </div>
                <span class="text-[10px] text-trust-300 font-sans">{{ ch.rows.length }} 个店铺</span>
              </div>
              <!-- 店铺行 -->
              <div class="divide-y divide-white/[0.03]">
                <div v-for="row in ch.rows" :key="row.shopId" class="px-4 py-2.5">
                  <!-- 店铺名 -->
                  <div class="flex items-center gap-2 mb-2">
                    <span class="text-[11px] text-gray-300 font-sans">{{ row.shopName }}</span>
                  </div>
                  <!-- 输入行 -->
                  <div class="grid grid-cols-2 sm:grid-cols-4 gap-2">
                    <select v-model="row.shift"
                      class="bg-white/[0.03] border border-white/[0.06] rounded-lg px-2 py-2 text-[11px] text-gray-300 font-sans focus:outline-none focus:ring-2 focus:ring-brand/30 cursor-pointer">
                      <option value="morning">早班</option>
                      <option value="evening">晚班</option>
                    </select>
                    <input type="number" placeholder="接待量" v-model="row.receptionCount" min="0"
                      class="bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-2 text-[12px] text-white placeholder-trust-400 font-mono focus:outline-none focus:ring-2 focus:ring-brand/30 transition-colors tabular-nums" />
                    <div class="relative">
                      <input type="number" placeholder="回复率" v-model="row.replyRate" min="0" max="100" step="0.1"
                        class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-2 pr-7 text-[12px] text-white placeholder-trust-400 font-mono focus:outline-none focus:ring-2 focus:ring-brand/30 transition-colors tabular-nums" />
                      <span class="absolute right-2.5 top-1/2 -translate-y-1/2 text-[10px] text-trust-400 font-mono">%</span>
                    </div>
                    <div class="relative">
                      <input type="number" placeholder="好评率" v-model="row.praiseRate" min="0" max="100" step="0.1"
                        class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-2 pr-7 text-[12px] text-white placeholder-trust-400 font-mono focus:outline-none focus:ring-2 focus:ring-brand/30 transition-colors tabular-nums" />
                      <span class="absolute right-2.5 top-1/2 -translate-y-1/2 text-[10px] text-trust-400 font-mono">%</span>
                    </div>
                  </div>
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
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '../api'

const today = new Date().toLocaleDateString('sv-SE', { timeZone: 'Asia/Shanghai' })
const date = ref(today)
const channels = reactive([])
const submitted = ref(false)
const submitting = ref(false)
const error = ref('')
const loaded = ref(false)

onMounted(async () => {
  try {
    const [assignRes, platformRes] = await Promise.all([
      api.get('/service-records/my-shops'),
      api.get('/dict/platform')
    ])
    const assignments = assignRes.data || []
    const platformList = platformRes.data || []
    const platformMap = {}
    platformList.forEach(p => { platformMap[p.code] = p.label })

    // 按 platformCode 分组（和运营 DataEntry 一样的层级）
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
        shopId: a.shopId,
        shopName: a.shopName,
        shift: 'morning',
        receptionCount: '', replyRate: '', praiseRate: '',
      })
    }
    const sortedCodes = platformList.map(p => p.code)
    const sorted = sortedCodes.filter(c => grouped[c]).map(c => grouped[c])
    channels.push(...sorted)
  } catch { /* empty */ }
  loaded.value = true
})

async function handleSubmit() {
  error.value = ''
  const items = []
  for (const ch of channels) {
    for (const row of ch.rows) {
      if (parseInt(row.receptionCount) > 0 || parseFloat(row.replyRate) > 0 || parseFloat(row.praiseRate) > 0) {
        items.push({
          platform: ch.code,
          shopId: row.shopId,
          shopNote: row.shopName,
          shift: row.shift,
          receptionCount: parseInt(row.receptionCount) || 0,
          replyRate: parseFloat(row.replyRate) || 0,
          praiseRate: parseFloat(row.praiseRate) || 0,
        })
      }
    }
  }
  if (items.length === 0) { error.value = '请至少填写一个店铺的数据'; return }
  submitting.value = true
  try {
    await api.post('/service-records', { recordDate: date.value, items })
    submitted.value = true
    setTimeout(() => { submitted.value = false }, 2000)
    channels.forEach(ch => { ch.rows.forEach(r => { r.receptionCount = ''; r.replyRate = ''; r.praiseRate = ''; r.shift = 'morning' }) })
  } catch (e) {
    error.value = e?.message || '提交失败'
  } finally {
    submitting.value = false
  }
}
</script>
