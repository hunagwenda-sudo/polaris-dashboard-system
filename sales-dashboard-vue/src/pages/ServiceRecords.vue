<template>
  <div class="max-w-[1100px] mx-auto space-y-4">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-[15px] font-semibold text-white font-sans">客服业绩查看</h2>
        <p class="text-[11px] text-trust-300 mt-0.5 font-sans">{{ isService ? '查看个人每日填报的接待量、回复率、好评率' : '查看客服人员每日填报的接待量、回复率、好评率' }}</p>
      </div>
      <button v-if="isAdmin" @click="openWebhookModal" class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-white/[0.04] border border-white/[0.06] text-[11px] text-trust-300 hover:text-white hover:bg-white/[0.08] transition-colors cursor-pointer font-sans">
        <svg v-bind="iconDefaults" class="w-3.5 h-3.5"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
        飞书提醒
        <span v-if="webhookConfigured" class="w-1.5 h-1.5 rounded-full bg-success-light"></span>
      </button>
    </div>

    <!-- 筛选栏 -->
    <div class="bg-surface-raised rounded-2xl border border-white/[0.04] px-5 py-3.5 flex items-center gap-4">
      <svg v-bind="iconDefaults" class="w-4 h-4 text-trust-300 shrink-0"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
      <template v-if="canViewAll">
        <label for="sk" class="sr-only">搜索姓名</label>
        <input id="sk" type="text" v-model="keyword" @input="debounceSearch" placeholder="搜索姓名..." class="bg-transparent text-[12px] text-gray-300 placeholder-gray-500 focus:outline-none font-sans w-28" />
        <div class="w-px h-5 bg-white/[0.06]" />
      </template>
      <label for="ss" class="sr-only">开始日期</label>
      <input id="ss" type="date" v-model="startDate" @change="fetchRecords" class="bg-white/[0.04] border border-white/[0.06] rounded-lg px-3 py-1.5 text-[11px] text-gray-300 focus:outline-none focus:ring-2 focus:ring-brand/30 cursor-pointer font-mono [color-scheme:dark]" />
      <label for="se" class="sr-only">结束日期</label>
      <input id="se" type="date" v-model="endDate" @change="fetchRecords" class="bg-white/[0.04] border border-white/[0.06] rounded-lg px-3 py-1.5 text-[11px] text-gray-300 focus:outline-none focus:ring-2 focus:ring-brand/30 cursor-pointer font-mono [color-scheme:dark]" />
      <button v-if="startDate || endDate || keyword" @click="clearFilters" class="text-[10px] text-brand hover:text-brand/80 cursor-pointer font-sans font-medium">清除</button>
      <span class="text-[10px] text-trust-300 ml-auto font-mono">{{ allGroupedRows.length }} 条</span>
    </div>

    <!-- 未填报人员 -->
    <div v-if="canViewAll && unfilledUsers.length > 0" class="bg-surface-raised rounded-2xl border border-amber-500/[0.15] overflow-hidden">
      <button @click="showUnfilled = !showUnfilled" class="w-full px-5 py-3 flex items-center justify-between cursor-pointer hover:bg-white/[0.02] transition-colors">
        <div class="flex items-center gap-2.5">
          <svg v-bind="iconDefaults" class="w-4 h-4 text-amber-400"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
          <span class="text-[12px] font-semibold text-amber-400 font-sans">{{ yesterday }} 未填报</span>
          <span class="inline-flex items-center px-1.5 py-0.5 rounded-md text-[10px] font-bold bg-amber-500/[0.1] text-amber-400 border border-amber-500/[0.15] font-mono">{{ unfilledUsers.length }} 人</span>
        </div>
        <svg v-bind="iconDefaults" :class="['w-4 h-4 text-trust-300 transition-transform duration-200', showUnfilled ? 'rotate-180' : '']"><polyline points="6 9 12 15 18 9"/></svg>
      </button>
      <div v-if="showUnfilled" class="px-5 pb-4 pt-1">
        <div class="flex flex-wrap gap-2">
          <span v-for="u in unfilledUsers" :key="u.id" class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-white/[0.03] border border-white/[0.06]">
            <div class="w-4 h-4 rounded bg-amber-500/[0.15] flex items-center justify-center text-amber-400 text-[8px] font-semibold font-mono">{{ (u.name||'?')[0] }}</div>
            <span class="text-[11px] text-gray-300 font-sans">{{ u.name }}</span>
          </span>
        </div>
      </div>
    </div>

    <!-- 表格 -->
    <div class="bg-surface-raised rounded-2xl border border-white/[0.04] overflow-hidden">
      <table class="w-full table-fixed">
        <colgroup>
          <col class="w-[14%]" /><!-- 日期 -->
          <col class="w-[18%]" /><!-- 姓名 -->
          <col class="w-[16%]" /><!-- 接待量 -->
          <col class="w-[18%]" /><!-- 3分钟回复率 -->
          <col class="w-[18%]" /><!-- 好评率 -->
          <col class="w-[16%]" /><!-- 操作 -->
        </colgroup>
        <thead>
          <tr class="border-b border-white/[0.04]">
            <th v-for="col in columns" :key="col.key" :class="thClass(col)" @click="col.sortable && toggleSort(col.key)">
              <span class="inline-flex items-center gap-1">{{ col.label }}
                <svg v-if="col.sortable" width="10" height="10" viewBox="0 0 10 10" :class="['transition-transform', sortField === col.key ? 'text-brand-light' : 'text-trust-300/40']">
                  <path v-if="sortField !== col.key || sortOrder === 'desc'" d="M5 7L1 3h8z" fill="currentColor"/>
                  <path v-else d="M5 3l4 4H1z" fill="currentColor"/>
                </svg>
              </span>
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in pagedRows" :key="r.key" class="border-b border-white/[0.02] hover:bg-white/[0.03] transition-colors duration-150">
            <td class="px-3 py-2.5 text-[11px] text-gray-300 font-mono tabular-nums whitespace-nowrap">{{ r.date }}</td>
            <td class="px-3 py-2.5">
              <div class="flex items-center gap-2">
                <div class="w-5 h-5 rounded-md bg-gradient-to-br from-purple-500 to-purple-400 flex items-center justify-center text-white text-[8px] font-semibold font-mono shrink-0">{{ (r.name || '?')[0] }}</div>
                <span class="text-[11px] font-medium text-gray-300 font-sans truncate">{{ r.name }}</span>
              </div>
            </td>
            <td class="px-3 py-2.5 text-[12px] text-white font-mono tabular-nums text-right font-medium">{{ r.receptionCount }}</td>
            <td class="px-3 py-2.5 text-[12px] font-mono tabular-nums text-right" :class="rateColor(r.replyRate)">{{ fmtRate(r.replyRate) }}</td>
            <td class="px-3 py-2.5 text-[12px] font-mono tabular-nums text-right" :class="rateColor(r.praiseRate)">{{ fmtRate(r.praiseRate) }}</td>
            <td class="px-3 py-2.5 text-center">
              <router-link :to="{ name: 'serviceRecordDetail', params: { userId: r.userId }, query: { date: r.date } }" class="inline-flex items-center gap-1 px-2 py-1 rounded-lg text-[10px] font-medium text-brand-light hover:text-white hover:bg-brand/[0.1] transition-colors cursor-pointer font-sans">
                <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                详情
              </router-link>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="pagedRows.length === 0" class="py-14 text-center text-trust-300 text-[12px] font-sans">暂无匹配记录</div>
      <div v-if="totalPages > 1" class="flex items-center justify-center gap-2 py-3 border-t border-white/[0.04]">
        <button @click="prevPage" :disabled="page <= 1" class="px-3 py-1 rounded-lg text-[11px] text-trust-300 hover:bg-white/[0.04] disabled:opacity-30 cursor-pointer font-sans">上一页</button>
        <span class="text-[11px] text-trust-300 font-mono">{{ page }} / {{ totalPages }}</span>
        <button @click="nextPage" :disabled="page >= totalPages" class="px-3 py-1 rounded-lg text-[11px] text-trust-300 hover:bg-white/[0.04] disabled:opacity-30 cursor-pointer font-sans">下一页</button>
      </div>
    </div>

    <!-- Webhook 配置弹窗 -->
    <div v-if="showWebhookModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/60" @click.self="showWebhookModal = false">
      <div class="bg-surface-raised rounded-2xl border border-white/[0.06] w-[480px] p-6 space-y-4">
        <div class="flex items-center justify-between">
          <h3 class="text-[14px] font-semibold text-white font-sans">飞书群提醒配置</h3>
          <button @click="showWebhookModal = false" class="text-trust-300 hover:text-white cursor-pointer">
            <svg v-bind="iconDefaults" class="w-4 h-4"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
        <p class="text-[11px] text-trust-300 font-sans leading-relaxed">配置飞书机器人 Webhook 地址后，系统将在每天上午 9:00 和 10:00 自动推送前一天未填报客服人员提醒到飞书群。支持添加多个地址，同时推送到多个群。</p>
        <div class="space-y-2">
          <label class="block text-[11px] text-trust-300 font-sans">Webhook 地址</label>
          <div v-for="(_, i) in webhookUrls" :key="i" class="flex items-center gap-2">
            <input type="text" v-model="webhookUrls[i]" placeholder="https://open.feishu.cn/open-apis/bot/v2/hook/..."
              class="flex-1 bg-white/[0.04] border border-white/[0.06] rounded-lg px-3 py-2 text-[12px] text-gray-300 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-brand/30 font-mono" />
            <button @click="webhookUrls.splice(i, 1)" class="w-8 h-8 rounded-lg bg-white/[0.04] border border-white/[0.06] flex items-center justify-center text-trust-300 hover:text-danger-light hover:bg-danger/[0.08] transition-colors cursor-pointer shrink-0"
              :class="{ 'opacity-30 pointer-events-none': webhookUrls.length <= 1 }">
              <svg v-bind="iconDefaults" class="w-3.5 h-3.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
          </div>
          <button @click="webhookUrls.push('')" class="inline-flex items-center gap-1 text-[11px] text-brand-light hover:text-white transition-colors cursor-pointer font-sans">
            <svg v-bind="iconDefaults" class="w-3.5 h-3.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
            添加地址
          </button>
        </div>
        <div class="flex items-center justify-end gap-3 pt-2">
          <button @click="showWebhookModal = false" class="px-4 py-2 rounded-lg text-[12px] text-trust-300 hover:text-white hover:bg-white/[0.04] transition-colors cursor-pointer font-sans">取消</button>
          <button @click="saveWebhook" :disabled="savingWebhook" class="px-4 py-2 rounded-lg text-[12px] font-medium text-white bg-brand hover:bg-brand/80 transition-colors cursor-pointer font-sans disabled:opacity-50">
            {{ savingWebhook ? '保存中...' : '保存' }}
          </button>
        </div>
        <p v-if="webhookMsg" :class="['text-[11px] font-sans', webhookMsgOk ? 'text-success-light' : 'text-danger-light']">{{ webhookMsg }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { iconDefaults } from '../components/icons.js'
import { useAuthStore } from '../stores/auth'
import api from '../api'

const auth = useAuthStore()
const isAdmin = computed(() => auth.user?.role === 'admin')
const isService = computed(() => auth.user?.role === 'service')
const canViewAll = computed(() => ['admin', 'partner'].includes(auth.user?.role))

const PAGE_SIZE = 15
const today = new Date().toLocaleDateString('sv-SE', { timeZone: 'Asia/Shanghai' })
const sevenDaysAgo = (() => {
  const d = new Date(new Date().toLocaleString('en-US', { timeZone: 'Asia/Shanghai' }))
  d.setDate(d.getDate() - 6)
  return d.toLocaleDateString('sv-SE')
})()
const records = ref([])
const userMap = ref({})
const platformMap = ref({})
const page = ref(1)
const startDate = ref(sevenDaysAgo)
const endDate = ref(today)
const keyword = ref('')
const sortField = ref('')
const sortOrder = ref('')
const unfilledUsers = ref([])
const showUnfilled = ref(true)

// 昨日日期
const yesterday = (() => {
  const d = new Date(new Date().toLocaleString('en-US', { timeZone: 'Asia/Shanghai' }))
  d.setDate(d.getDate() - 1)
  return d.toLocaleDateString('sv-SE')
})()
let debounceTimer = null

const columns = [
  { key: 'date', label: '日期', sortable: true, align: 'left' },
  { key: 'name', label: '姓名', sortable: false, align: 'left' },
  { key: 'receptionCount', label: '接待量', sortable: true, align: 'right' },
  { key: 'replyRate', label: '3分钟回复率', sortable: true, align: 'right' },
  { key: 'praiseRate', label: '好评率', sortable: true, align: 'right' },
  { key: 'action', label: '操作', sortable: false, align: 'center' },
]

const allGroupedRows = computed(() => {
  const map = new Map()
  for (const r of records.value) {
    const key = `${r.userId}_${r.recordDate}`
    if (!map.has(key)) {
      map.set(key, { key, date: r.recordDate, userId: r.userId, name: userMap.value[r.userId] || auth.user?.name || String(r.userId), receptionCount: 0, replyRateSum: 0, praiseRateSum: 0, count: 0 })
    }
    const g = map.get(key)
    g.receptionCount += Number(r.receptionCount) || 0
    if (r.replyRate != null) { g.replyRateSum += Number(r.replyRate); g.count++ }
    if (r.praiseRate != null) g.praiseRateSum += Number(r.praiseRate)
  }
  let rows = [...map.values()].map(g => ({
    ...g,
    replyRate: g.count > 0 ? g.replyRateSum / g.count : null,
    praiseRate: g.count > 0 ? g.praiseRateSum / g.count : null,
  }))
  if (sortField.value) {
    const asc = sortOrder.value === 'asc'
    rows.sort((a, b) => {
      const va = a[sortField.value] ?? (sortField.value === 'date' ? a.date : 0)
      const vb = b[sortField.value] ?? (sortField.value === 'date' ? b.date : 0)
      if (va < vb) return asc ? -1 : 1
      if (va > vb) return asc ? 1 : -1
      return 0
    })
  }
  return rows
})

const totalPages = computed(() => Math.max(1, Math.ceil(allGroupedRows.value.length / PAGE_SIZE)))

const pagedRows = computed(() => {
  const start = (page.value - 1) * PAGE_SIZE
  return allGroupedRows.value.slice(start, start + PAGE_SIZE)
})

function thClass(col) {
  return ['px-3 py-3 text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans whitespace-nowrap',
    col.align === 'right' ? 'text-right' : col.align === 'center' ? 'text-center' : 'text-left',
    col.sortable ? 'cursor-pointer hover:text-white select-none transition-colors' : ''].join(' ')
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

function toggleSort(field) {
  if (sortField.value === field) sortOrder.value = sortOrder.value === 'desc' ? 'asc' : 'desc'
  else { sortField.value = field; sortOrder.value = 'desc' }
  page.value = 1
}

function debounceSearch() { clearTimeout(debounceTimer); debounceTimer = setTimeout(() => { page.value = 1; fetchRecords() }, 300) }
function clearFilters() { startDate.value = ''; endDate.value = ''; keyword.value = ''; page.value = 1; fetchRecords() }
function prevPage() { if (page.value > 1) page.value-- }
function nextPage() { if (page.value < totalPages.value) page.value++ }

async function fetchRecords() {
  try {
    const params = { page: 1, size: 1000 }
    if (startDate.value) params.startDate = startDate.value
    if (endDate.value) params.endDate = endDate.value
    if (keyword.value) params.keyword = keyword.value
    const res = await api.get('/service-records', { params })
    records.value = res.data?.records || []
    page.value = 1
  } catch { /* empty */ }
}

async function fetchUnfilled() {
  try {
    const res = await api.get('/service-records/unfilled', { params: { date: yesterday } })
    unfilledUsers.value = res.data || []
  } catch { unfilledUsers.value = [] }
}

async function fetchUsers() {
  try {
    const res = await api.get('/users', { params: { size: 999 } })
    const map = {}
    ;(res.data?.records || []).forEach(u => { map[u.id] = u.name })
    userMap.value = map
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

// Webhook 配置
const showWebhookModal = ref(false)
const webhookUrls = ref([''])
const webhookConfigured = ref(false)
const savingWebhook = ref(false)
const webhookMsg = ref('')
const webhookMsgOk = ref(false)

async function openWebhookModal() {
  showWebhookModal.value = true
  webhookMsg.value = ''
  try {
    const res = await api.get('/webhook/service_record_unfilled')
    const urls = res.data?.urls || []
    webhookUrls.value = urls.length > 0 ? [...urls] : ['']
  } catch { webhookUrls.value = [''] }
}

async function saveWebhook() {
  savingWebhook.value = true
  webhookMsg.value = ''
  try {
    const cleaned = webhookUrls.value.filter(u => u && u.trim())
    await api.put('/webhook/service_record_unfilled', { urls: cleaned })
    webhookConfigured.value = cleaned.length > 0
    webhookMsg.value = '保存成功'
    webhookMsgOk.value = true
    setTimeout(() => { showWebhookModal.value = false }, 800)
  } catch { webhookMsg.value = '保存失败'; webhookMsgOk.value = false }
  savingWebhook.value = false
}

async function fetchWebhookStatus() {
  if (!isAdmin.value) return
  try {
    const res = await api.get('/webhook/service_record_unfilled')
    const urls = res.data?.urls || []
    webhookConfigured.value = urls.some(u => u && u.trim())
  } catch { /* ignore */ }
}

onMounted(() => {
  fetchPlatforms(); fetchRecords()
  if (canViewAll.value) { fetchUsers(); fetchUnfilled(); fetchWebhookStatus() }
})
</script>
