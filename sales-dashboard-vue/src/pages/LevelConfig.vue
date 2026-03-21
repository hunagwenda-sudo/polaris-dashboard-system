<template>
  <div class="max-w-[1100px] mx-auto space-y-4">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-[15px] font-semibold text-white font-sans">职级设定</h2>
        <p class="text-[11px] text-trust-300 mt-0.5 font-sans">自定义职级体系，设置各级晋升所需的季度累计 DGMV 金额</p>
      </div>
      <button @click="addLevel"
        class="flex items-center gap-1.5 px-4 py-2 rounded-xl text-[12px] font-semibold bg-gradient-to-r from-brand to-brand-light text-white hover:opacity-90 transition-opacity cursor-pointer shadow-lg shadow-brand/15 font-sans">
        <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        添加职级
      </button>
    </div>

    <div class="bg-surface-raised rounded-2xl border border-white/[0.04] p-5 space-y-3">
      <div v-for="(item, i) in levels" :key="i" class="flex items-center gap-3 group">
        <div class="w-[90px] shrink-0 text-center">
          <span class="inline-flex items-center px-2 py-1 rounded-lg text-[11px] font-bold font-mono border"
            :class="i === 0 ? 'bg-white/[0.04] text-trust-300 border-white/[0.06]' : 'bg-brand/[0.08] text-brand-light border-brand/[0.12]'">
            K{{ i + 1 }} → K{{ i + 2 }}
          </span>
        </div>
        <div class="flex-1 relative">
          <span class="absolute left-3 top-1/2 -translate-y-1/2 text-[11px] text-trust-300 font-mono">¥</span>
          <input v-model.number="item.amount" type="number" min="0" step="10000"
            class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg pl-7 pr-4 py-2.5 text-[13px] text-white font-mono tabular-nums focus:outline-none focus:ring-2 focus:ring-brand/30" />
        </div>
        <button v-if="levels.length > 1" @click="removeLevel(i)"
          class="opacity-0 group-hover:opacity-100 transition-opacity p-1.5 rounded-lg hover:bg-red-500/[0.1] cursor-pointer">
          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-red-400"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
        </button>
        <div v-else class="w-[30px]" />
      </div>
    </div>

    <div class="flex items-center justify-between">
      <p v-if="msg" :class="['text-[11px] font-sans', msgOk ? 'text-success-light' : 'text-red-400']">{{ msg }}</p>
      <span v-else />
      <button @click="save" :disabled="saving"
        class="px-5 py-2.5 rounded-xl text-[12px] font-semibold bg-gradient-to-r from-brand to-brand-light text-white hover:opacity-90 transition-opacity cursor-pointer shadow-lg shadow-brand/15 font-sans disabled:opacity-50">
        {{ saving ? '保存中...' : '保存设置' }}
      </button>
    </div>

    <!-- Preview -->
    <div class="bg-surface-raised rounded-2xl border border-white/[0.04] p-5">
      <p class="text-[10px] font-semibold text-trust-300 uppercase tracking-[0.12em] mb-3 font-sans">职级体系预览</p>
      <div class="flex items-stretch gap-2 flex-wrap">
        <div v-for="(item, i) in previewLevels" :key="i"
          class="flex-1 min-w-[100px] rounded-xl p-3 border transition-all duration-200"
          :class="levelStyles[i] || levelStyles[levelStyles.length - 1]">
          <div class="flex items-center gap-1.5 mb-1.5">
            <div v-html="levelIcons[i] || levelIcons[levelIcons.length - 1]" />
            <p class="text-[12px] font-bold font-mono">K{{ i + 1 }}</p>
          </div>
          <p class="text-[9px] text-trust-300 font-sans">{{ item }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '../api'

const levels = ref([])
const saving = ref(false)
const msg = ref('')
const msgOk = ref(false)

const levelStyles = [
  // K1 - 青铜：朴素灰
  'bg-white/[0.02] border-white/[0.06]',
  // K2 - 白银：淡蓝
  'bg-slate-400/[0.06] border-slate-400/[0.12]',
  // K3 - 黄金：蓝色
  'bg-blue-500/[0.06] border-blue-500/[0.15]',
  // K4 - 铂金：青色
  'bg-cyan-500/[0.06] border-cyan-500/[0.15]',
  // K5 - 钻石：紫色 + 光晕
  'bg-purple-500/[0.08] border-purple-500/[0.2] shadow-sm shadow-purple-500/10',
  // K6 - 大师：琥珀金 + 光晕
  'bg-amber-500/[0.08] border-amber-500/[0.2] shadow-md shadow-amber-500/15',
  // K7 - 宗师：红金渐变 + 强光
  'bg-gradient-to-br from-amber-500/[0.1] to-red-500/[0.1] border-amber-400/[0.3] shadow-lg shadow-amber-500/20',
  // K8 - 传说：金红 + 光环
  'bg-gradient-to-br from-amber-500/[0.12] to-rose-500/[0.12] border-amber-400/[0.4] shadow-lg shadow-amber-400/25 ring-1 ring-amber-500/20',
  // K9 - 神话：玫红金 + 双光环
  'bg-gradient-to-br from-rose-500/[0.12] to-amber-500/[0.12] border-rose-400/[0.4] shadow-lg shadow-rose-400/25 ring-1 ring-rose-400/20',
  // K10 - 不朽：红紫渐变 + 强光环
  'bg-gradient-to-br from-rose-500/[0.15] to-purple-500/[0.15] border-rose-400/[0.4] shadow-xl shadow-rose-500/25 ring-1 ring-rose-400/25',
  // K11 - 超凡：紫金渐变 + 极光
  'bg-gradient-to-br from-purple-500/[0.15] to-amber-400/[0.15] border-purple-400/[0.4] shadow-xl shadow-purple-500/25 ring-2 ring-purple-400/20',
  // K12 - 至尊：全彩渐变 + 满光环
  'bg-gradient-to-br from-amber-400/[0.15] via-rose-500/[0.15] to-purple-500/[0.15] border-amber-300/[0.5] shadow-xl shadow-amber-400/30 ring-2 ring-amber-300/25',
]

const levelIcons = [
  // K1 青铜 - 种子萌芽
  '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22V8"/><path d="M5 12H2a10 10 0 0 0 20 0h-3"/><path d="M8 8a4 4 0 0 1 8 0"/></svg>',
  // K2 白银 - 盾牌
  '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#a8b4c4" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>',
  // K3 黄金 - 闪电
  '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#60a5fa" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>',
  // K4 铂金 - 钻石
  '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#22d3ee" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 3h12l4 6-10 13L2 9z"/><path d="M2 9h20"/><path d="M10 3l-4 6"/><path d="M14 3l4 6"/></svg>',
  // K5 钻石 - 皇冠
  '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#a78bfa" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 4l3 12h14l3-12-6 7-4-7-4 7-6-7z"/><path d="M5 16h14v3H5z"/></svg>',
  // K6 大师 - 火焰皇冠
  '<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="#fbbf24" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2c1 3 4 5.5 4 8.5a4 4 0 1 1-8 0C8 7.5 11 5 12 2z"/><path d="M2 19h20"/><path d="M5 19l1-3h12l1 3"/></svg>',
  // K7 宗师 - 凤凰之翼
  '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22l-4-8 4-6 4 6-4 8z" fill="#f59e0b" fill-opacity="0.15"/><path d="M4 6c2 2 4 6 8 8"/><path d="M20 6c-2 2-4 6-8 8"/><path d="M2 4c1 3 3 5 6 6"/><path d="M22 4c-1 3-3 5-6 6"/><circle cx="12" cy="6" r="2" fill="#f59e0b" fill-opacity="0.3"/></svg>',
  // K8 传说 - 龙焰王冠
  '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2l2 5h5l-4 3.5 1.5 5L12 13l-4.5 2.5L9 10.5 5 7h5l2-5z" fill="#fbbf24" fill-opacity="0.25" stroke="#fbbf24" stroke-width="1.5"/><path d="M8 17c0-2 1.5-3 4-3s4 1 4 3" stroke="#ef4444" stroke-width="1.5"/><path d="M6 20h12" stroke="#fbbf24" stroke-width="2"/><circle cx="12" cy="8" r="1" fill="#fbbf24"/></svg>',
  // K9 神话 - 三叉戟
  '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#fb7185" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22V6"/><path d="M12 6l-5-4v5l5-1"/><path d="M12 6l5-4v5l-5-1"/><path d="M12 2v4"/><circle cx="12" cy="13" r="1.5" fill="#fb7185" fill-opacity="0.3"/></svg>',
  // K10 不朽 - 永恒之眼
  '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8S1 12 1 12z" stroke="#f43f5e" stroke-width="1.5"/><circle cx="12" cy="12" r="3" stroke="#c084fc" stroke-width="1.5" fill="#c084fc" fill-opacity="0.15"/><circle cx="12" cy="12" r="1" fill="#f43f5e"/></svg>',
  // K11 超凡 - 无限符号
  '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke-linecap="round" stroke-linejoin="round"><path d="M12 12c-2-2.5-4-4-6.5-4a4.5 4.5 0 0 0 0 9c2.5 0 4.5-1.5 6.5-4" stroke="#c084fc" stroke-width="2"/><path d="M12 12c2 2.5 4 4 6.5 4a4.5 4.5 0 0 0 0-9c-2.5 0-4.5 1.5-6.5 4" stroke="#fbbf24" stroke-width="2"/></svg>',
  // K12 至尊 - 日月同辉
  '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke-linecap="round" stroke-linejoin="round"><circle cx="10" cy="12" r="4" stroke="#fbbf24" stroke-width="1.5" fill="#fbbf24" fill-opacity="0.15"/><path d="M10 5v-2M10 21v-2M3 12H1M19 12h-2M4.93 7.05L3.5 5.64M16.07 7.05l1.42-1.41M4.93 16.95l-1.43 1.41M16.07 16.95l1.42 1.41" stroke="#fbbf24" stroke-width="1.2"/><path d="M16 8a5 5 0 0 1 0 8" stroke="#c084fc" stroke-width="1.8" fill="#c084fc" fill-opacity="0.1"/></svg>',
]

const previewLevels = computed(() => {
  const result = []
  for (let i = 0; i <= levels.value.length; i++) {
    const low = i === 0 ? 0 : Number(levels.value[i - 1].amount) || 0
    const high = i < levels.value.length ? Number(levels.value[i].amount) || 0 : null
    result.push(high !== null ? `¥${fmt(low)} ~ ¥${fmt(high)}` : `≥ ¥${fmt(low)}`)
  }
  return result
})

function fmt(n) {
  return n >= 10000 ? (n / 10000) + '万' : n.toLocaleString()
}

function addLevel() {
  const last = levels.value.length > 0 ? Number(levels.value[levels.value.length - 1].amount) || 0 : 0
  levels.value.push({ amount: last + 50000, dictId: null })
}

function removeLevel(i) {
  const item = levels.value[i]
  if (item.dictId) removedIds.push(item.dictId)
  levels.value.splice(i, 1)
}

const removedIds = []

async function fetchThresholds() {
  try {
    const res = await api.get('/dict/level_threshold')
    const list = (res.data || []).sort((a, b) => a.sort - b.sort)
    levels.value = list.map(d => ({ amount: Number(d.label) || 0, dictId: d.id }))
  } catch { /* empty */ }
  if (levels.value.length === 0) {
    levels.value = [{ amount: 50000, dictId: null }]
  }
  removedIds.length = 0
}

async function save() {
  // 校验金额递增
  for (let i = 1; i < levels.value.length; i++) {
    if ((Number(levels.value[i].amount) || 0) <= (Number(levels.value[i - 1].amount) || 0)) {
      msg.value = `K${i + 1}→K${i + 2} 的金额必须大于 K${i}→K${i + 1}`
      msgOk.value = false
      return
    }
  }

  saving.value = true
  msg.value = ''
  try {
    // 删除被移除的
    for (const id of removedIds) {
      await api.delete(`/dict/${id}`)
    }
    removedIds.length = 0

    // 更新或创建
    for (let i = 0; i < levels.value.length; i++) {
      const item = levels.value[i]
      const code = `K${i + 1}_K${i + 2}`
      const label = String(Math.round(Number(item.amount) || 0))
      if (item.dictId) {
        await api.put(`/dict/${item.dictId}`, { code, label, sort: i + 1 })
      } else {
        const res = await api.post('/dict', { type: 'level_threshold', code, label, sort: i + 1 })
        item.dictId = res.data?.id
      }
    }
    msg.value = '保存成功'
    msgOk.value = true
    await fetchThresholds()
  } catch (e) {
    msg.value = e?.message || '保存失败'
    msgOk.value = false
  } finally {
    saving.value = false
    setTimeout(() => { msg.value = '' }, 3000)
  }
}

onMounted(fetchThresholds)
</script>
