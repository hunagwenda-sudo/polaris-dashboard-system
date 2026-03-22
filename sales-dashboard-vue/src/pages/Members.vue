<template>
  <div class="max-w-[1100px] mx-auto space-y-4">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-[15px] font-semibold text-white font-sans">人员管理</h2>
        <p class="text-[11px] text-trust-300 mt-0.5 font-sans">管理系统中的所有人员信息</p>
      </div>
      <button @click="openCreate" class="flex items-center gap-1.5 px-4 py-2 rounded-xl text-[12px] font-semibold bg-gradient-to-r from-brand to-brand-light text-white hover:opacity-90 transition-opacity cursor-pointer shadow-lg shadow-brand/15 font-sans">
        <svg v-bind="iconDefaults" class="w-3.5 h-3.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        添加人员
      </button>
    </div>

    <!-- Create modal -->
    <div v-if="showCreate" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showCreate = false">
      <div class="bg-trust-800 border border-white/[0.08] rounded-2xl p-6 w-[420px] space-y-4">
        <h3 class="text-[14px] font-semibold text-white font-sans">添加人员</h3>
        <div>
          <label class="text-[10px] text-trust-300 font-sans block mb-1">用户名（登录用）</label>
          <input v-model="form.username" placeholder="username" class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-4 py-2 text-[12px] text-white placeholder-trust-400 font-sans focus:outline-none focus:ring-2 focus:ring-brand/30" />
        </div>
        <div>
          <label class="text-[10px] text-trust-300 font-sans block mb-1">姓名</label>
          <input v-model="form.name" placeholder="真实姓名" class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-4 py-2 text-[12px] text-white placeholder-trust-400 font-sans focus:outline-none focus:ring-2 focus:ring-brand/30" />
        </div>
        <div>
          <label class="text-[10px] text-trust-300 font-sans block mb-1">手机号</label>
          <input v-model="form.phone" placeholder="13800000000" class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-4 py-2 text-[12px] text-white placeholder-trust-400 font-mono focus:outline-none focus:ring-2 focus:ring-brand/30" />
        </div>
        <div>
          <label class="text-[10px] text-trust-300 font-sans block mb-1">角色</label>
          <select v-model="form.role" class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-4 py-2 text-[12px] text-gray-300 font-sans focus:outline-none focus:ring-2 focus:ring-brand/30 cursor-pointer">
            <option value="sales">运营</option><option value="partner">合伙人</option><option value="admin">管理员</option><option value="service">客服</option>
          </select>
        </div>
        <div>
          <label class="text-[10px] text-trust-300 font-sans block mb-1">所属团队</label>
          <select v-model="form.teamId" class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-4 py-2 text-[12px] text-gray-300 font-sans focus:outline-none focus:ring-2 focus:ring-brand/30 cursor-pointer">
            <option :value="null">无团队</option>
            <option v-for="t in teams" :key="t.id" :value="t.id">{{ t.name }}</option>
          </select>
        </div>
        <div class="grid grid-cols-2 gap-3">
          <div>
            <label class="text-[10px] text-trust-300 font-sans block mb-1">生日</label>
            <input v-model="form.birthday" type="date" :max="today" class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-4 py-2 text-[12px] text-gray-300 font-mono focus:outline-none focus:ring-2 focus:ring-brand/30" />
          </div>
          <div>
            <label class="text-[10px] text-trust-300 font-sans block mb-1">入职日期</label>
            <input v-model="form.hireDate" type="date" :max="today" class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-4 py-2 text-[12px] text-gray-300 font-mono focus:outline-none focus:ring-2 focus:ring-brand/30" />
          </div>
        </div>
        <div>
          <label class="text-[10px] text-trust-300 font-sans block mb-1.5">渠道账号分配</label>
          <div class="space-y-2 max-h-[200px] overflow-y-auto">
            <div v-for="p in platforms" :key="p.code">
              <p class="text-[10px] font-semibold text-trust-300 font-sans mb-1">{{ p.label }}</p>
              <div class="flex flex-wrap gap-1.5 ml-2">
                <label v-for="acc in (accountsByPlatform[p.code] || [])" :key="acc.id"
                  class="flex items-center gap-1 px-2.5 py-1 rounded-md text-[10px] font-sans cursor-pointer transition-colors duration-150"
                  :class="form.assignedAccountIds.includes(acc.id) ? 'bg-brand/[0.15] text-brand-light border border-brand/[0.3]' : 'bg-white/[0.03] text-gray-400 border border-white/[0.06] hover:border-white/[0.12]'">
                  <input type="checkbox" :value="acc.id" v-model="form.assignedAccountIds" class="hidden" />
                  {{ acc.accountName }}
                </label>
                <span v-if="!(accountsByPlatform[p.code] || []).length" class="text-[9px] text-trust-400 font-sans">暂无账号</span>
              </div>
            </div>
          </div>
        </div>
        <p v-if="formError" class="text-red-400 text-[11px]">{{ formError }}</p>
        <div class="flex gap-3 justify-end">
          <button @click="showCreate = false" class="px-4 py-2 rounded-lg text-[12px] text-trust-300 hover:text-white cursor-pointer font-sans">取消</button>
          <button @click="createUser" class="px-4 py-2 rounded-lg text-[12px] font-semibold bg-brand text-white hover:bg-brand-light cursor-pointer font-sans">创建</button>
        </div>
      </div>
    </div>

    <!-- Edit modal -->
    <div v-if="showEdit" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showEdit = false">
      <div class="bg-trust-800 border border-white/[0.08] rounded-2xl p-6 w-[560px] max-h-[90vh] overflow-y-auto space-y-4">
        <h3 class="text-[14px] font-semibold text-white font-sans">编辑人员</h3>
        <!-- 基本信息 2列 -->
        <div class="grid grid-cols-2 gap-3">
          <div>
            <label class="text-[10px] text-trust-300 font-sans block mb-1">姓名</label>
            <input v-model="editForm.name" class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-1.5 text-[12px] text-white font-sans focus:outline-none focus:ring-2 focus:ring-brand/30" />
          </div>
          <div>
            <label class="text-[10px] text-trust-300 font-sans block mb-1">手机号</label>
            <input v-model="editForm.phone" class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-1.5 text-[12px] text-white font-mono focus:outline-none focus:ring-2 focus:ring-brand/30" />
          </div>
          <div>
            <label class="text-[10px] text-trust-300 font-sans block mb-1">角色</label>
            <select v-model="editForm.role" class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-1.5 text-[12px] text-gray-300 font-sans focus:outline-none focus:ring-2 focus:ring-brand/30 cursor-pointer">
              <option value="sales">运营</option><option value="partner">合伙人</option><option value="admin">管理员</option><option value="service">客服</option>
            </select>
          </div>
          <div>
            <label class="text-[10px] text-trust-300 font-sans block mb-1">所属团队</label>
            <select v-model="editForm.teamId" class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-1.5 text-[12px] text-gray-300 font-sans focus:outline-none focus:ring-2 focus:ring-brand/30 cursor-pointer">
              <option :value="null">无团队</option>
              <option v-for="t in teams" :key="t.id" :value="t.id">{{ t.name }}</option>
            </select>
          </div>
          <div>
            <label class="text-[10px] text-trust-300 font-sans block mb-1">职级</label>
            <select v-model="editForm.level" class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-1.5 text-[12px] text-gray-300 font-mono focus:outline-none focus:ring-2 focus:ring-brand/30 cursor-pointer">
              <option v-for="lv in levelOptions" :key="lv" :value="lv">{{ lv }}</option>
            </select>
          </div>
          <div class="flex items-end pb-0.5">
            <div class="flex items-center justify-between w-full px-3 py-1.5 rounded-lg bg-white/[0.03] border border-white/[0.06]">
              <span class="text-[11px] text-white font-sans">填报提醒</span>
              <button type="button" @click="editForm.remindEnabled = editForm.remindEnabled ? 0 : 1"
                :class="['relative inline-flex items-center w-9 h-5 rounded-full transition-colors duration-200 cursor-pointer shrink-0 overflow-hidden',
                  editForm.remindEnabled ? 'bg-brand' : 'bg-white/[0.15]']">
                <span :class="['absolute top-0.5 w-4 h-4 rounded-full bg-white shadow transition-transform duration-200',
                  editForm.remindEnabled ? 'translate-x-4' : 'translate-x-0.5']" />
              </button>
            </div>
          </div>
          <div>
            <label class="text-[10px] text-trust-300 font-sans block mb-1">生日</label>
            <input v-model="editForm.birthday" type="date" :max="today" class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-1.5 text-[12px] text-gray-300 font-mono focus:outline-none focus:ring-2 focus:ring-brand/30" />
          </div>
          <div>
            <label class="text-[10px] text-trust-300 font-sans block mb-1">入职日期</label>
            <input v-model="editForm.hireDate" type="date" :max="today" class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-1.5 text-[12px] text-gray-300 font-mono focus:outline-none focus:ring-2 focus:ring-brand/30" />
          </div>
        </div>
        <!-- 渠道账号分配 -->
        <div>
          <label class="text-[10px] text-trust-300 font-sans block mb-1.5">渠道账号分配</label>
          <div class="rounded-lg border border-white/[0.06] bg-white/[0.02] p-3 space-y-2 max-h-[180px] overflow-y-auto">
            <div v-for="p in platforms" :key="p.code" class="flex items-start gap-2">
              <span class="text-[10px] font-semibold text-trust-300 font-sans w-14 shrink-0 pt-0.5">{{ p.label }}</span>
              <div class="flex flex-wrap gap-1.5">
                <label v-for="acc in (accountsByPlatform[p.code] || [])" :key="acc.id"
                  class="flex items-center gap-1 px-2 py-0.5 rounded text-[10px] font-sans cursor-pointer transition-colors duration-150"
                  :class="editForm.assignedAccountIds.includes(acc.id) ? 'bg-brand/[0.15] text-brand-light border border-brand/[0.3]' : 'bg-white/[0.03] text-gray-400 border border-white/[0.06] hover:border-white/[0.12]'">
                  <input type="checkbox" :value="acc.id" v-model="editForm.assignedAccountIds" class="hidden" />
                  {{ acc.accountName }}
                </label>
                <span v-if="!(accountsByPlatform[p.code] || []).length" class="text-[9px] text-trust-400 font-sans">暂无账号</span>
              </div>
            </div>
          </div>
        </div>
        <!-- 操作区 -->
        <div class="flex items-center gap-2 pt-1 border-t border-white/[0.06]">
          <button @click="resetPasswordInEdit" class="flex items-center gap-1 px-3 py-1.5 rounded-lg text-[11px] font-medium bg-white/[0.04] text-accent-light hover:bg-accent/[0.1] hover:text-accent transition-colors cursor-pointer font-sans">
            <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
            重置密码
          </button>
          <button v-if="editForm.status === 'active'" @click="toggleStatusInEdit('inactive')" class="flex items-center gap-1 px-3 py-1.5 rounded-lg text-[11px] font-medium bg-white/[0.04] text-danger-light/70 hover:bg-danger/[0.1] hover:text-danger-light transition-colors cursor-pointer font-sans">
            <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/></svg>
            禁用账号
          </button>
          <button v-else @click="toggleStatusInEdit('active')" class="flex items-center gap-1 px-3 py-1.5 rounded-lg text-[11px] font-medium bg-white/[0.04] text-success-light/70 hover:bg-success/[0.1] hover:text-success-light transition-colors cursor-pointer font-sans">
            <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
            启用账号
          </button>
        </div>

        <p v-if="editError" class="text-red-400 text-[11px]">{{ editError }}</p>
        <div class="flex gap-3 justify-end">
          <button @click="showEdit = false" class="px-4 py-2 rounded-lg text-[12px] text-trust-300 hover:text-white cursor-pointer font-sans">取消</button>
          <button @click="updateUser" class="px-4 py-2 rounded-lg text-[12px] font-semibold bg-brand text-white hover:bg-brand-light cursor-pointer font-sans">保存</button>
        </div>
      </div>
    </div>

    <div class="bg-surface-raised rounded-2xl border border-white/[0.04] px-5 py-3 flex items-center gap-3">
      <svg v-bind="iconDefaults" class="w-4 h-4 text-trust-300 shrink-0"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
      <label for="member-search" class="sr-only">搜索人员</label>
      <input id="member-search" type="text" placeholder="搜索姓名..." v-model="search" @input="debounceFetch"
        class="flex-1 bg-transparent text-[12px] text-gray-300 placeholder-gray-500 focus:outline-none font-sans" />
      <span class="text-[10px] text-trust-300 font-mono">{{ total }} 人</span>
    </div>
    <div class="bg-surface-raised rounded-2xl border border-white/[0.04] overflow-hidden">
      <table class="w-full table-fixed">
        <colgroup>
          <col class="w-[13%]" /><!-- 姓名 -->
          <col class="w-[7%]" /><!-- 角色 -->
          <col class="w-[9%]" /><!-- 团队 -->
          <col class="w-[6%]" /><!-- 职级 -->
          <col class="w-[11%]" /><!-- 手机号 -->
          <col class="w-[9%]" /><!-- 生日 -->
          <col class="w-[9%]" /><!-- 入职日期 -->
          <col class="w-[7%]" /><!-- 入职天数 -->
          <col class="w-[6%]" /><!-- 提醒 -->
          <col class="w-[7%]" /><!-- 状态 -->
          <col class="w-[6%]" /><!-- 操作 -->
        </colgroup>
        <thead>
          <tr class="border-b border-white/[0.04]">
            <th v-for="h in memberHeaders" :key="h.label"
              :class="['px-3 py-3 text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans whitespace-nowrap', h.align === 'right' ? 'text-right' : 'text-left']">{{ h.label }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="m in members" :key="m.id" class="border-b border-white/[0.02] hover:bg-white/[0.03] transition-colors duration-150">
            <td class="px-3 py-2.5">
              <div class="flex items-center gap-2">
                <img v-if="m.avatar" :src="memberAvatarUrl(m.id, m.avatar)" :alt="m.name" class="w-6 h-6 rounded-md object-cover shrink-0" />
                <div v-else class="w-6 h-6 rounded-md bg-gradient-to-br from-brand to-brand-light flex items-center justify-center text-white text-[9px] font-semibold font-mono shrink-0">{{ (m.name||'?')[0] }}</div>
                <span class="text-[11px] font-medium text-gray-200 font-sans truncate">{{ m.name }}</span>
              </div>
            </td>
            <td class="px-3 py-2.5"><span :class="['inline-flex items-center px-1.5 py-0.5 rounded-md text-[9px] font-semibold border font-sans whitespace-nowrap', roleBadge[m.role] || roleBadge.sales]">{{ roleLabel[m.role] || m.role }}</span></td>
            <td class="px-3 py-2.5 text-[11px] text-gray-400 font-sans truncate">{{ teamMap[m.teamId] || '—' }}</td>
            <td class="px-3 py-2.5">
              <span v-if="m.role === 'admin' || m.role === 'partner'" class="text-[10px] text-trust-300 font-sans">—</span>
              <span v-else class="inline-flex items-center px-1.5 py-0.5 rounded-md text-[9px] font-bold bg-accent/[0.08] text-accent border border-accent/[0.12] font-mono">{{ m.level || 'K1' }}</span>
            </td>
            <td class="px-3 py-2.5 text-[11px] text-gray-300 font-mono tabular-nums whitespace-nowrap">{{ m.phone || '—' }}</td>
            <td class="px-3 py-2.5 text-[11px] text-gray-300 font-mono tabular-nums whitespace-nowrap">{{ m.birthday || '—' }}</td>
            <td class="px-3 py-2.5 text-[11px] text-gray-300 font-mono tabular-nums whitespace-nowrap">{{ m.hireDate || '—' }}</td>
            <td class="px-3 py-2.5 text-[11px] font-mono tabular-nums whitespace-nowrap text-right">
              <span v-if="m.hireDate" class="text-brand-light">{{ hireDays(m.hireDate) }}</span>
              <span v-else class="text-gray-500">—</span>
            </td>
            <td class="px-3 py-2.5">
              <span v-if="m.remindEnabled !== 0"
                class="inline-flex items-center gap-1 px-1.5 py-0.5 rounded-md text-[9px] font-semibold bg-success/[0.08] text-success border border-success/[0.12] font-sans whitespace-nowrap">
                <svg width="9" height="9" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
                提醒
              </span>
              <span v-else
                class="inline-flex items-center gap-1 px-1.5 py-0.5 rounded-md text-[9px] font-semibold bg-white/[0.03] text-gray-500 border border-white/[0.04] font-sans whitespace-nowrap">
                <svg width="9" height="9" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="1" y1="1" x2="23" y2="23"/><path d="M17 17H3s3-2 3-9a6 6 0 0 1 .34-2"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/><path d="M21 21c0-7-3-9-3-9"/></svg>
                静默
              </span>
            </td>
            <td class="px-3 py-2.5">
              <div class="flex items-center gap-1.5">
                <div :class="['w-1.5 h-1.5 rounded-full shrink-0', m.status === 'active' ? 'bg-success-light' : 'bg-gray-600']" />
                <span class="text-[10px] text-gray-300 font-sans whitespace-nowrap">{{ m.status === 'active' ? '在职' : '离职' }}</span>
              </div>
            </td>
            <td class="px-3 py-2.5">
              <button @click="openEdit(m)" class="text-[10px] text-brand-light hover:text-brand cursor-pointer font-sans font-medium">编辑</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="loading" class="py-14 text-center text-trust-300 text-[12px] font-sans">加载中...</div>
      <div v-else-if="members.length === 0" class="py-14 text-center text-trust-300 text-[12px] font-sans">暂无人员数据</div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { iconDefaults } from '../components/icons.js'
import { useConfirm } from '../composables/useConfirm'
import api from '../api'

const { confirm } = useConfirm()
const API_BASE = import.meta.env.VITE_API_BASE || ''
const today = new Date().toLocaleDateString('sv-SE', { timeZone: 'Asia/Shanghai' })

// 带认证的头像 blob URL 缓存
const avatarCache = reactive({})
async function loadAvatar(id, path) {
  if (!path || avatarCache[id]) return
  try {
    const token = localStorage.getItem('token')
    const res = await fetch(API_BASE + path, { headers: { Authorization: `Bearer ${token}` } })
    if (res.ok) avatarCache[id] = URL.createObjectURL(await res.blob())
  } catch { /* ignore */ }
}
function memberAvatarUrl(id, path) {
  if (!avatarCache[id] && path) loadAvatar(id, path)
  return avatarCache[id] || ''
}

const memberHeaders = [
  { label: '姓名', align: 'left' },
  { label: '角色', align: 'left' },
  { label: '团队', align: 'left' },
  { label: '确定职级', align: 'left' },
  { label: '手机号', align: 'left' },
  { label: '生日', align: 'left' },
  { label: '入职日期', align: 'left' },
  { label: '入职天数', align: 'right' },
  { label: '提醒', align: 'left' },
  { label: '状态', align: 'left' },
  { label: '操作', align: 'left' },
]
const roleLabel = { admin: '管理员', partner: '合伙人', sales: '运营', service: '客服' }
const roleBadge = {
  admin: 'bg-red-500/[0.08] text-red-400 border-red-500/[0.12]',
  partner: 'bg-brand/[0.08] text-brand border-brand/[0.12]',
  sales: 'bg-white/[0.04] text-gray-400 border-white/[0.04]',
  service: 'bg-purple-500/[0.08] text-purple-400 border-purple-500/[0.12]',
}
const members = ref([])
const teams = ref([])
const platforms = ref([])
const allAccounts = ref([])
const total = ref(0)
const search = ref('')
const levelOptions = ref(['K1'])
const loading = ref(false)

// team id → name map
const teamMap = computed(() => {
  const map = {}
  teams.value.forEach(t => { map[t.id] = t.name })
  return map
})

const accountsByPlatform = computed(() => {
  const map = {}
  for (const acc of allAccounts.value) {
    if (!map[acc.platformCode]) map[acc.platformCode] = []
    map[acc.platformCode].push(acc)
  }
  return map
})

// Create
const showCreate = ref(false)
const formError = ref('')
const form = reactive({ username: '', name: '', phone: '', role: 'sales', teamId: null, birthday: '', hireDate: '', assignedAccountIds: [] })

// Edit
const showEdit = ref(false)
const editError = ref('')
const editId = ref(null)
const editForm = reactive({ name: '', phone: '', role: 'sales', teamId: null, level: 'K1', birthday: '', hireDate: '', remindEnabled: 1, status: 'active', assignedAccountIds: [] })

let debounceTimer = null

function debounceFetch() {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(fetchMembers, 300)
}

async function fetchTeams() {
  try {
    const res = await api.get('/teams')
    teams.value = (res.data || []).map(t => ({ id: t.id || t.teamId, name: t.name || t.teamName }))
  } catch { /* empty */ }
}

async function fetchPlatforms() {
  try {
    const res = await api.get('/dict/platform')
    platforms.value = (res.data || []).sort((a, b) => a.sort - b.sort)
  } catch { /* empty */ }
}

async function fetchAccounts() {
  try {
    const res = await api.get('/platform-accounts')
    allAccounts.value = res.data || []
  } catch { /* empty */ }
}

async function fetchLevels() {
  try {
    const res = await api.get('/dict/level_threshold')
    const list = (res.data || []).sort((a, b) => a.sort - b.sort)
    if (list.length > 0) {
      const opts = new Set()
      list.forEach(d => {
        const parts = d.code.split('_')
        if (parts.length === 2) { opts.add(parts[0]); opts.add(parts[1]) }
      })
      levelOptions.value = [...opts]
    }
  } catch { /* empty */ }
}

async function fetchMembers() {
  loading.value = true
  try {
    const params = { page: 1, size: 50 }
    if (search.value) params.keyword = search.value
    const res = await api.get('/users', { params })
    members.value = res.data?.records || []
    total.value = res.data?.total || members.value.length
  } catch { /* empty */ }
  loading.value = false
}

function hireDays(hireDate) {
  if (!hireDate) return '—'
  const days = Math.floor((Date.now() - new Date(hireDate).getTime()) / 86400000)
  return days >= 0 ? days + '天' : '—'
}

function openCreate() {
  Object.assign(form, { username: '', name: '', phone: '', role: 'sales', teamId: null, birthday: '', hireDate: '', assignedAccountIds: [] })
  formError.value = ''
  showCreate.value = true
}

async function createUser() {
  formError.value = ''
  try {
    const { assignedAccountIds, ...rest } = form
    const payload = { ...rest, birthday: form.birthday || null, hireDate: form.hireDate || null }
    const res = await api.post('/users', payload)
    // 保存渠道分配
    if (assignedAccountIds.length > 0 && res.data?.id) {
      await api.put(`/users/${res.data.id}/platforms`, { accountIds: assignedAccountIds })
    }
    showCreate.value = false
    fetchMembers()
  } catch (e) {
    formError.value = e?.message || '创建失败'
  }
}

async function openEdit(m) {
  editId.value = m.id
  editForm.name = m.name
  editForm.phone = m.phone || ''
  editForm.role = m.role
  editForm.teamId = m.teamId || null
  editForm.level = m.level || 'K1'
  editForm.birthday = m.birthday || ''
  editForm.hireDate = m.hireDate || ''
  editForm.remindEnabled = m.remindEnabled !== 0 ? 1 : 0
  editForm.status = m.status || 'active'
  // 加载用户渠道分配
  editForm.assignedAccountIds = []
  try {
    const res = await api.get(`/users/${m.id}/platforms`)
    editForm.assignedAccountIds = (res.data || []).map(a => a.accountId)
  } catch { /* ignore */ }
  editError.value = ''
  showEdit.value = true
}

async function updateUser() {
  editError.value = ''
  try {
    const { assignedAccountIds, ...rest } = editForm
    const payload = { ...rest, birthday: editForm.birthday || null, hireDate: editForm.hireDate || null }
    await api.put(`/users/${editId.value}`, payload)
    // 保存渠道分配
    await api.put(`/users/${editId.value}/platforms`, { accountIds: assignedAccountIds })
    showEdit.value = false
    fetchMembers()
  } catch (e) {
    editError.value = e?.message || '保存失败'
  }
}

async function toggleStatusInEdit(status) {
  const label = status === 'active' ? '启用' : '禁用'
  const ok = await confirm(`确定${label}该用户？`, { title: `${label}用户`, confirmText: `确认${label}`, type: status === 'active' ? 'brand' : 'danger' })
  if (!ok) return
  try {
    await api.put(`/users/${editId.value}/status`, { status })
    editForm.status = status
    fetchMembers()
  } catch { /* ignore */ }
}

async function resetPasswordInEdit() {
  const ok = await confirm(`确定重置密码为手机号后6位？`, { title: '重置密码', confirmText: '确认重置', type: 'danger' })
  if (!ok) return
  try {
    await api.put(`/users/${editId.value}/reset-password`)
    await confirm('密码已重置为手机号后6位', { title: '重置成功', confirmText: '知道了', type: 'brand' })
  } catch (e) {
    await confirm(e?.message || '重置失败', { title: '操作失败', confirmText: '知道了', type: 'danger' })
  }
}

onMounted(() => { fetchTeams(); fetchLevels(); fetchPlatforms(); fetchAccounts(); fetchMembers() })
</script>
