<template>
  <div class="max-w-[1100px] mx-auto space-y-5">
    <!-- Header -->
    <div class="flex items-center gap-3">
      <button @click="$router.push('/team')" class="w-8 h-8 rounded-lg bg-white/[0.04] border border-white/[0.06] flex items-center justify-center hover:bg-white/[0.08] transition-colors cursor-pointer">
        <svg v-bind="iconDefaults" class="w-4 h-4 text-trust-300"><polyline points="15 18 9 12 15 6"/></svg>
      </button>
      <div class="flex-1">
        <h2 class="text-[15px] font-semibold text-white font-sans">{{ team.name || '团队详情' }}</h2>
        <p class="text-[11px] text-trust-300 mt-0.5 font-sans">负责人：{{ team.leaderName || '未指定' }}</p>
      </div>
    </div>

    <!-- Stats cards -->
    <div class="grid grid-cols-4 gap-4">
      <div v-for="s in statCards" :key="s.label" class="bg-surface-raised rounded-2xl border border-white/[0.06] p-5">
        <p class="text-[9px] font-semibold text-trust-300 uppercase tracking-[0.1em] mb-2 font-sans">{{ s.label }}</p>
        <p class="text-[22px] font-extrabold text-white font-mono tabular-nums leading-none">{{ s.value }}</p>
        <p class="text-[10px] text-trust-300 mt-1 font-sans">{{ s.sub }}</p>
      </div>
    </div>

    <!-- Progress bar -->
    <div class="bg-surface-raised rounded-2xl border border-white/[0.06] px-6 py-4">
      <div class="flex items-center justify-between mb-2">
        <span class="text-[10px] font-semibold text-trust-300 uppercase tracking-[0.1em] font-sans">季度目标进度</span>
        <span class="text-[16px] font-extrabold text-white font-mono tabular-nums">{{ completionRate }}%</span>
      </div>
      <div class="w-full h-2.5 bg-white/[0.04] rounded-full overflow-hidden">
        <div class="h-full rounded-full bg-gradient-to-r from-brand to-brand-light transition-all duration-700" :style="{ width: Math.min(Number(completionRate), 100) + '%' }" />
      </div>
    </div>

    <!-- Members table -->
    <div class="bg-surface-raised rounded-2xl border border-white/[0.04] overflow-hidden">
      <div class="px-5 py-4 border-b border-white/[0.04] flex items-center justify-between">
        <h3 class="text-[13px] font-semibold text-white font-sans">团队成员 ({{ members.length }})</h3>
        <button v-if="canManage" @click="showAddMember = !showAddMember" class="flex items-center gap-1 px-3 py-1.5 rounded-lg text-[11px] font-medium bg-brand/[0.08] text-brand-light hover:bg-brand/[0.15] transition-colors cursor-pointer font-sans">
          <svg v-bind="iconDefaults" class="w-3.5 h-3.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          添加成员
        </button>
      </div>

      <!-- 添加成员面板 -->
      <div v-if="showAddMember && canManage" class="px-5 py-3 border-b border-white/[0.04] bg-white/[0.02] flex items-center gap-3">
        <select v-model="addUserId" class="flex-1 bg-trust-700 border border-white/[0.06] rounded-lg px-3 py-2 text-[12px] text-gray-300 font-sans focus:outline-none focus:ring-2 focus:ring-brand/30 cursor-pointer">
          <option :value="null" disabled>选择要添加的人员</option>
          <option v-for="u in availableUsers" :key="u.id" :value="u.id">{{ u.name }}（{{ roleLabel[u.role] || u.role }}）</option>
        </select>
        <button @click="doAddMember" class="px-4 py-2 rounded-lg text-[12px] font-semibold bg-brand text-white hover:bg-brand-light cursor-pointer font-sans">添加</button>
        <button @click="showAddMember = false" class="px-3 py-2 rounded-lg text-[12px] text-trust-300 hover:text-white cursor-pointer font-sans">取消</button>
      </div>

      <table class="w-full table-fixed">
        <colgroup>
          <col class="w-[20%]" /><col class="w-[12%]" /><col class="w-[10%]" /><col class="w-[20%]" /><col class="w-[18%]" /><col class="w-[12%]" /><col class="w-[8%]" />
        </colgroup>
        <thead>
          <tr class="border-b border-white/[0.04]">
            <th class="px-4 py-3 text-left text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">姓名</th>
            <th class="px-4 py-3 text-center text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">角色</th>
            <th class="px-4 py-3 text-center text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">职级</th>
            <th class="px-4 py-3 text-right text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">季度目标</th>
            <th class="px-4 py-3 text-left text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">手机号</th>
            <th class="px-4 py-3 text-center text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">状态</th>
            <th v-if="canManage" class="px-4 py-3 text-center text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(m, idx) in members" :key="m.id" :class="['border-b border-white/[0.02] hover:bg-white/[0.04] transition-colors duration-150 group', idx % 2 === 1 ? 'bg-white/[0.015]' : '']">
            <td class="px-4 py-3">
              <div class="flex items-center gap-2.5">
                <div class="w-7 h-7 rounded-lg bg-gradient-to-br from-brand to-brand-light flex items-center justify-center text-white text-[10px] font-semibold font-mono shrink-0">{{ (m.name||'?')[0] }}</div>
                <span class="text-[12px] font-medium text-gray-200 font-sans truncate">{{ m.name }}</span>
              </div>
            </td>
            <td class="px-4 py-3 text-center"><span :class="['inline-flex items-center px-2 py-0.5 rounded-md text-[9px] font-semibold border font-sans', roleBadge[m.role] || roleBadge.sales]">{{ roleLabel[m.role] || m.role }}</span></td>
            <td class="px-4 py-3 text-center"><span class="inline-flex items-center px-2 py-0.5 rounded-md text-[9px] font-bold bg-accent/[0.08] text-accent border border-accent/[0.12] font-mono">{{ m.level || 'K1' }}</span></td>
            <td class="px-4 py-3">
              <div class="flex items-center justify-end gap-2">
                <span v-if="editingTarget !== m.id" class="text-[12px] text-gray-300 font-mono tabular-nums">{{ fmtTarget(m.targetDgmv) }}</span>
                <input v-else v-model.number="editTargetVal" type="number" min="0" step="10000" @keyup.enter="saveTarget(m)" @keyup.escape="editingTarget = null" class="w-24 bg-white/[0.04] border border-brand/[0.3] rounded-lg px-2 py-1 text-[11px] text-white font-mono focus:outline-none focus:ring-2 focus:ring-brand/30" />
                <button v-if="canManage && editingTarget !== m.id" @click.stop="startEditTarget(m)" class="text-[9px] text-brand-light hover:text-white cursor-pointer font-sans opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">设置</button>
                <template v-else-if="canManage && editingTarget === m.id">
                  <button @click.stop="saveTarget(m)" class="text-[9px] text-success-light hover:text-white cursor-pointer font-sans">确定</button>
                  <button @click.stop="editingTarget = null" class="text-[9px] text-trust-300 hover:text-white cursor-pointer font-sans">取消</button>
                </template>
              </div>
            </td>
            <td class="px-4 py-3 text-[12px] text-gray-400 font-mono tabular-nums">{{ m.phone || '—' }}</td>
            <td class="px-4 py-3 text-center">
              <div class="inline-flex items-center gap-1.5">
                <div :class="['w-1.5 h-1.5 rounded-full', m.status === 'active' ? 'bg-success-light' : 'bg-gray-600']" />
                <span class="text-[10px] text-gray-300 font-sans">{{ m.status === 'active' ? '在职' : '离职' }}</span>
              </div>
            </td>
            <td v-if="canManage" class="px-4 py-3 text-center">
              <button @click="doRemoveMember(m)" class="text-[10px] text-danger-light/60 hover:text-danger-light cursor-pointer font-sans transition-colors">移出</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="members.length === 0" class="py-14 text-center text-trust-300 text-[12px] font-sans">暂无成员</div>
    </div>

    <!-- 近期业绩 (分页) -->
    <div class="bg-surface-raised rounded-2xl border border-white/[0.04] overflow-hidden">
      <div class="px-5 py-4 border-b border-white/[0.04] flex items-center justify-between">
        <h3 class="text-[13px] font-semibold text-white font-sans">近期业绩（最近7天）</h3>
        <span class="text-[10px] text-trust-300 font-mono">共 {{ recordTotal }} 条</span>
      </div>
      <table class="w-full table-fixed">
        <colgroup>
          <col class="w-[16%]" /><col class="w-[20%]" /><col class="w-[18%]" /><col class="w-[14%]" /><col class="w-[18%]" /><col class="w-[14%]" />
        </colgroup>
        <thead>
          <tr class="border-b border-white/[0.04]">
            <th class="px-4 py-3 text-left text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">日期</th>
            <th class="px-4 py-3 text-left text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">姓名</th>
            <th class="px-4 py-3 text-right text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">GMV</th>
            <th class="px-4 py-3 text-right text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">退款</th>
            <th class="px-4 py-3 text-right text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">DGMV</th>
            <th class="px-4 py-3 text-center text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(row, idx) in records" :key="`${row.userId}_${row.recordDate}`" :class="['border-b border-white/[0.02] hover:bg-white/[0.04] transition-colors duration-150', idx % 2 === 1 ? 'bg-white/[0.015]' : '']">
            <td class="px-4 py-3 text-[12px] text-gray-300 font-mono tabular-nums whitespace-nowrap">{{ row.recordDate }}</td>
            <td class="px-4 py-3">
              <div class="flex items-center gap-2">
                <div class="w-6 h-6 rounded-lg bg-gradient-to-br from-brand to-brand-light flex items-center justify-center text-white text-[9px] font-semibold font-mono shrink-0">{{ (row.name || '?')[0] }}</div>
                <span class="text-[12px] font-medium text-gray-200 font-sans truncate">{{ row.name }}</span>
              </div>
            </td>
            <td class="px-4 py-3 text-[12px] text-gray-300 font-mono tabular-nums text-right">{{ fmt(row.gmv) }}</td>
            <td class="px-4 py-3 text-[12px] text-danger-light/70 font-mono tabular-nums text-right">-{{ fmt(row.refund) }}</td>
            <td class="px-4 py-3 text-[13px] text-success-light font-mono tabular-nums font-bold text-right">{{ fmt(row.dgmv) }}</td>
            <td class="px-4 py-3 text-center">
              <router-link :to="{ name: 'recordDetail', params: { userId: row.userId }, query: { date: row.recordDate } }" class="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg text-[10px] font-medium text-brand-light hover:text-white hover:bg-brand/[0.12] transition-colors cursor-pointer font-sans">
                <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                详情
              </router-link>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="records.length === 0" class="py-14 text-center text-trust-300 text-[12px] font-sans">暂无业绩记录</div>
      <!-- 分页 -->
      <div v-if="totalPages > 1" class="px-5 py-3 border-t border-white/[0.04] flex items-center justify-between">
        <span class="text-[10px] text-trust-300 font-sans">第 {{ recordPage }} / {{ totalPages }} 页</span>
        <div class="flex items-center gap-2">
          <button @click="goPage(recordPage - 1)" :disabled="recordPage <= 1" :class="['px-3 py-1.5 rounded-lg text-[11px] font-medium font-sans transition-colors cursor-pointer', recordPage <= 1 ? 'text-trust-400/40 cursor-not-allowed' : 'text-gray-300 hover:bg-white/[0.06] hover:text-white']">上一页</button>
          <button @click="goPage(recordPage + 1)" :disabled="recordPage >= totalPages" :class="['px-3 py-1.5 rounded-lg text-[11px] font-medium font-sans transition-colors cursor-pointer', recordPage >= totalPages ? 'text-trust-400/40 cursor-not-allowed' : 'text-gray-300 hover:bg-white/[0.06] hover:text-white']">下一页</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { iconDefaults } from '../components/icons.js'
import { useAuthStore } from '../stores/auth'
import { useConfirm } from '../composables/useConfirm'
import api from '../api'

const route = useRoute()
const teamId = route.params.id
const auth = useAuthStore()
const { confirm } = useConfirm()
const canManage = computed(() => auth.user?.role === 'admin' || (team.value.leaderId === auth.user?.id))

const roleLabel = { admin: '管理员', partner: '合伙人', sales: '运营' }
const roleBadge = {
  admin: 'bg-red-500/[0.08] text-red-400 border-red-500/[0.12]',
  partner: 'bg-brand/[0.08] text-brand border-brand/[0.12]',
  sales: 'bg-white/[0.04] text-gray-400 border-white/[0.04]',
}
const fmt = (n) => `¥${Number(n).toLocaleString()}`
const fmtW = (n) => `${(Number(n) / 10000).toFixed(1)}万`

const team = ref({})
const members = ref([])
const records = ref([])
const recordPage = ref(1)
const recordTotal = ref(0)
const totalPages = computed(() => Math.ceil(recordTotal.value / 10))
const editingTarget = ref(null)
const editTargetVal = ref(0)
const showAddMember = ref(false)
const addUserId = ref(null)
const availableUsers = ref([])

const fmtTarget = (v) => {
  const n = Number(v) || 0
  return n > 0 ? `¥${(n / 10000).toFixed(1)}万` : '未设置'
}

function startEditTarget(m) {
  editingTarget.value = m.id
  editTargetVal.value = Number(m.targetDgmv) || 0
}

async function saveTarget(m) {
  try {
    await api.put(`/users/${m.id}`, { targetDgmv: editTargetVal.value })
    m.targetDgmv = editTargetVal.value
    editingTarget.value = null
  } catch { /* ignore */ }
}

const completionRate = computed(() => {
  const target = Number(team.value.targetDgmv) || 0
  const current = Number(team.value.quarterDgmv) || 0
  return target > 0 ? ((current / target) * 100).toFixed(1) : '0.0'
})

const statCards = computed(() => {
  const t = team.value
  const target = Number(t.targetDgmv) || 0
  const current = Number(t.quarterDgmv) || 0
  const gap = Math.max(target - current, 0)
  return [
    { label: '季度目标', value: `¥${fmtW(target)}`, sub: `Q${Math.ceil((new Date().getMonth()+1)/3)} 季度` },
    { label: '季度累计', value: `¥${fmtW(current)}`, sub: `已完成 ${completionRate.value}%` },
    { label: '目标差额', value: `¥${fmtW(gap)}`, sub: '距离目标还差' },
    { label: '团队人数', value: `${members.value.length}`, sub: `${t.leaderName || '—'} 带队` },
  ]
})

async function fetchTeam() {
  try {
    const res = await api.get('/teams')
    const all = res.data || []
    team.value = all.find(t => String(t.id) === String(teamId)) || {}
  } catch { /* empty */ }
}

async function fetchMembers() {
  try {
    const res = await api.get(`/teams/${teamId}/members`)
    members.value = res.data || []
  } catch { /* empty */ }
}

async function fetchAvailableUsers() {
  try {
    const res = await api.get(`/teams/${teamId}/available-members`)
    availableUsers.value = res.data || []
  } catch { availableUsers.value = [] }
}

async function doAddMember() {
  if (!addUserId.value) return
  try {
    await api.post(`/teams/${teamId}/members`, { userId: addUserId.value })
    addUserId.value = null
    await fetchMembers()
    await fetchAvailableUsers()
  } catch { /* ignore */ }
}

async function doRemoveMember(m) {
  const ok = await confirm(`确定将「${m.name}」移出团队吗？`, { title: '移出成员' })
  if (!ok) return
  try {
    await api.delete(`/teams/${teamId}/members/${m.id}`)
    await fetchMembers()
    await fetchAvailableUsers()
  } catch { /* ignore */ }
}

async function fetchRecords(page = 1) {
  try {
    const res = await api.get(`/teams/${teamId}/records`, { params: { page, size: 10 } })
    const data = res.data || {}
    records.value = data.records || []
    recordTotal.value = data.total || 0
    recordPage.value = page
  } catch { /* empty */ }
}

function goPage(p) {
  if (p < 1 || p > totalPages.value) return
  fetchRecords(p)
}

onMounted(async () => {
  await fetchTeam()
  await fetchMembers()
  await fetchRecords()
  if (canManage.value) fetchAvailableUsers()
})
</script>
