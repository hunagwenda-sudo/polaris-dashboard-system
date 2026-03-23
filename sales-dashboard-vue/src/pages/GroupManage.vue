<template>
  <div class="space-y-5">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-[15px] font-semibold text-white font-sans">小组管理</h2>
        <p class="text-[11px] text-trust-300 mt-0.5 font-sans">{{ subtitle }}</p>
      </div>
      <button v-if="canManage" @click="openCreate" class="flex items-center gap-1.5 px-4 py-2 rounded-xl text-[12px] font-semibold bg-gradient-to-r from-brand to-brand-light text-white hover:opacity-90 transition-opacity cursor-pointer shadow-lg shadow-brand/15 font-sans">
        <svg v-bind="iconDefaults" class="w-3.5 h-3.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        新建小组
      </button>
    </div>

    <!-- Groups grid -->
    <div class="grid grid-cols-3 gap-4">
      <div v-for="g in displayGroups" :key="g.id"
        class="bg-surface-raised rounded-2xl border border-white/[0.04] overflow-hidden transition-all duration-200">
        <div :class="['h-1 bg-gradient-to-r', gradients[g.id % gradients.length]]" />
        <div class="p-5 space-y-3.5">
          <!-- 头部：头像 + 组名 + 组长 -->
          <div class="flex items-center gap-2.5">
            <div :class="['w-9 h-9 rounded-xl bg-gradient-to-br flex items-center justify-center text-white text-[12px] font-bold font-mono shrink-0', gradients[g.id % gradients.length]]">{{ (g.leaderName || '?')[0] }}</div>
            <div class="min-w-0">
              <h3 class="text-[14px] font-bold text-white font-sans leading-tight truncate">{{ g.name }}</h3>
              <p class="text-[10px] text-trust-300 font-sans mt-0.5">组长: {{ g.leaderName || '未指定' }}</p>
            </div>
          </div>

          <!-- 进度条 -->
          <div class="space-y-1.5">
            <div class="flex items-center justify-between">
              <span class="text-[9px] text-trust-300 font-sans uppercase tracking-wider">季度进度</span>
              <span :class="['text-[11px] font-bold font-mono tabular-nums', Number(g.completionRate) >= 100 ? 'text-success-light' : 'text-brand-light']">{{ g.completionRate || 0 }}%</span>
            </div>
            <div class="w-full h-1.5 bg-white/[0.04] rounded-full overflow-hidden">
              <div :class="['h-full rounded-full bg-gradient-to-r transition-all duration-700', gradients[g.id % gradients.length]]" :style="{ width: Math.min(Number(g.completionRate) || 0, 100) + '%' }" />
            </div>
          </div>

          <!-- 数据行 -->
          <div class="grid grid-cols-3 gap-2">
            <div class="bg-white/[0.02] rounded-lg px-3 py-2.5 border border-white/[0.03]">
              <p class="text-[9px] text-trust-300 font-sans tracking-wider">季度累计</p>
              <p class="text-[14px] font-extrabold text-white font-mono tabular-nums mt-0.5">{{ fmtW(g.quarterDgmv) }}</p>
            </div>
            <div class="bg-white/[0.02] rounded-lg px-3 py-2.5 border border-white/[0.03]">
              <p class="text-[9px] text-trust-300 font-sans tracking-wider">季度目标</p>
              <p class="text-[14px] font-extrabold text-gray-400 font-mono tabular-nums mt-0.5">{{ fmtW(g.targetDgmv) }}</p>
            </div>
            <div class="bg-white/[0.02] rounded-lg px-3 py-2.5 border border-white/[0.03]">
              <p class="text-[9px] text-trust-300 font-sans tracking-wider">成员</p>
              <p class="text-[14px] font-extrabold text-white font-mono tabular-nums mt-0.5">{{ g.memberCount || 0 }}<span class="text-[10px] text-trust-300 font-normal ml-0.5">人</span></p>
            </div>
          </div>
        </div>

        <!-- 底部操作栏 -->
        <div class="border-t border-white/[0.04] px-5 py-3 flex items-center gap-2">
          <button @click="openMembers(g)" class="flex items-center gap-1 px-3 py-1.5 rounded-lg text-[11px] font-medium bg-brand/[0.08] text-brand-light hover:bg-brand/[0.15] transition-colors cursor-pointer font-sans">
            <svg v-bind="iconDefaults" class="w-3.5 h-3.5"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
            {{ canManageGroup(g) ? '成员管理' : '查看成员' }}
          </button>
          <button v-if="canManageGroup(g)" @click="openEdit(g)" class="flex items-center gap-1 px-3 py-1.5 rounded-lg text-[11px] font-medium bg-white/[0.04] text-gray-300 hover:bg-white/[0.08] hover:text-white transition-colors cursor-pointer font-sans">
            <svg v-bind="iconDefaults" class="w-3.5 h-3.5"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
            基本信息
          </button>
          <button v-if="canManage" @click="confirmDelete(g)" class="flex items-center gap-1 px-3 py-1.5 rounded-lg text-[11px] font-medium bg-white/[0.04] text-danger-light/70 hover:bg-danger/10 hover:text-danger-light transition-colors cursor-pointer font-sans ml-auto">
            <svg v-bind="iconDefaults" class="w-3.5 h-3.5"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
            删除
          </button>
        </div>
      </div>
    </div>
    <div v-if="displayGroups.length === 0" class="py-14 text-center text-trust-300 text-[12px] font-sans">暂无小组数据</div>

    <!-- Create modal -->
    <div v-if="showCreate" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showCreate = false">
      <div class="bg-trust-800 border border-white/[0.08] rounded-2xl p-6 w-[400px] space-y-4">
        <h3 class="text-[14px] font-semibold text-white font-sans">新建小组</h3>
        <div>
          <label class="text-[10px] text-trust-300 font-sans block mb-1">小组名称</label>
          <input v-model="form.name" placeholder="小组名称" class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-4 py-2 text-[12px] text-white placeholder-trust-400 font-sans focus:outline-none focus:ring-2 focus:ring-brand/30" />
        </div>
        <div>
          <label class="text-[10px] text-trust-300 font-sans block mb-1">组长 <span class="text-danger-light">*</span></label>
          <select v-model="form.leaderId" class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-4 py-2 text-[12px] text-gray-300 font-sans focus:outline-none focus:ring-2 focus:ring-brand/30 cursor-pointer">
            <option :value="null" disabled>请选择组长</option>
            <option v-for="u in createLeaderCandidates" :key="u.id" :value="u.id">{{ u.name }}</option>
          </select>
        </div>
        <p v-if="formError" class="text-red-400 text-[11px]">{{ formError }}</p>
        <div class="flex gap-3 justify-end">
          <button @click="showCreate = false" class="px-4 py-2 rounded-lg text-[12px] text-trust-300 hover:text-white cursor-pointer font-sans">取消</button>
          <button @click="createGroup" class="px-4 py-2 rounded-lg text-[12px] font-semibold bg-brand text-white hover:bg-brand-light cursor-pointer font-sans">创建</button>
        </div>
      </div>
    </div>

    <!-- Edit modal -->
    <div v-if="showEdit" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showEdit = false">
      <div class="bg-trust-800 border border-white/[0.08] rounded-2xl p-6 w-[400px] space-y-4">
        <h3 class="text-[14px] font-semibold text-white font-sans">编辑小组</h3>
        <div>
          <label class="text-[10px] text-trust-300 font-sans block mb-1">小组名称</label>
          <input v-model="editForm.name" class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-4 py-2 text-[12px] text-white placeholder-trust-400 font-sans focus:outline-none focus:ring-2 focus:ring-brand/30" />
        </div>
        <div>
          <label class="text-[10px] text-trust-300 font-sans block mb-1">组长</label>
          <select v-model="editForm.leaderId" class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-4 py-2 text-[12px] text-gray-300 font-sans focus:outline-none focus:ring-2 focus:ring-brand/30 cursor-pointer">
            <option :value="null">暂不指定</option>
            <option v-for="u in editGroupUsers" :key="u.id" :value="u.id">{{ u.name }}</option>
          </select>
        </div>
        <p v-if="editError" class="text-red-400 text-[11px]">{{ editError }}</p>
        <div class="flex gap-3 justify-end">
          <button @click="showEdit = false" class="px-4 py-2 rounded-lg text-[12px] text-trust-300 hover:text-white cursor-pointer font-sans">取消</button>
          <button @click="updateGroup" class="px-4 py-2 rounded-lg text-[12px] font-semibold bg-brand text-white hover:bg-brand-light cursor-pointer font-sans">保存</button>
        </div>
      </div>
    </div>

    <!-- Members modal -->
    <div v-if="showMembers" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showMembers = false">
      <div class="bg-trust-800 border border-white/[0.08] rounded-2xl p-6 w-[480px] space-y-4">
        <h3 class="text-[14px] font-semibold text-white font-sans">{{ currentGroup?.name }} — {{ canManageGroup(currentGroup) ? '成员管理' : '成员列表' }}</h3>
        <div class="space-y-2 max-h-[300px] overflow-y-auto">
          <div v-for="m in displayMembers" :key="m.id" class="flex items-center justify-between bg-white/[0.02] rounded-lg px-3 py-2 border border-white/[0.03]">
            <div class="flex items-center gap-2">
              <div class="w-6 h-6 rounded-md bg-gradient-to-br from-brand to-brand-light flex items-center justify-center text-white text-[9px] font-semibold font-mono">{{ (m.name||'?')[0] }}</div>
              <span class="text-[11px] text-gray-200 font-sans">{{ m.name }}</span>
            </div>
            <button v-if="canManageGroup(currentGroup)" @click="removeMember(m.id)" class="text-[10px] text-danger-light hover:text-danger-light/80 cursor-pointer font-sans">移出</button>
          </div>
          <div v-if="displayMembers.length === 0" class="text-center text-trust-300 text-[11px] py-4 font-sans">暂无成员</div>
        </div>
        <div v-if="canManageGroup(currentGroup)" class="border-t border-white/[0.06] pt-3">
          <label class="text-[10px] text-trust-300 font-sans block mb-1">添加成员（未加入任何小组的人员）</label>
          <div class="flex gap-2">
            <select v-model="addUserId" class="flex-1 bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-2 text-[12px] text-gray-300 font-sans focus:outline-none focus:ring-2 focus:ring-brand/30 cursor-pointer">
              <option :value="null" disabled>选择人员</option>
              <option v-for="u in availableUsers" :key="u.id" :value="u.id">{{ u.name }}</option>
            </select>
            <button @click="addMember" class="px-4 py-2 rounded-lg text-[12px] font-semibold bg-brand text-white hover:bg-brand-light cursor-pointer font-sans">添加</button>
          </div>
        </div>
        <div class="flex justify-end">
          <button @click="showMembers = false" class="px-4 py-2 rounded-lg text-[12px] text-trust-300 hover:text-white cursor-pointer font-sans">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { iconDefaults } from '../components/icons.js'
import { useConfirm } from '../composables/useConfirm'
import { useAuthStore } from '../stores/auth'
import api from '../api'

const { confirm } = useConfirm()
const auth = useAuthStore()

// admin 有全局管理权限，partner 只能管理自己的小组
const canManage = computed(() => auth.user?.role === 'admin')
const isPartner = computed(() => auth.user?.role === 'partner')
const isSales = computed(() => auth.user?.role === 'sales')

// 是否是某个小组的组长
const isLeaderOfAny = computed(() => groups.value.some(g => g.leaderId === auth.user?.id))

// 判断当前用户能否管理某个具体小组（admin 或该组组长）
function canManageGroup(g) {
  if (!g) return false
  if (canManage.value) return true
  return g.leaderId === auth.user?.id
}

const subtitle = computed(() => {
  if (isSales.value && isLeaderOfAny.value) return '管理所在小组信息和成员'
  if (isSales.value) return '查看所在小组信息和成员'
  if (isPartner.value && isLeaderOfAny.value) return '管理所在小组信息和成员'
  if (isPartner.value) return '查看所在小组信息和成员'
  if (canManage.value) return '管理小组信息、查看小组业绩概览'
  return '查看小组信息和业绩概览'
})

const gradients = ['from-brand to-brand-light', 'from-brand-light to-brand-light', 'from-brand-light to-success-light']
const fmtW = (n) => `¥${(Number(n) / 10000).toFixed(1)}万`

const allUsers = ref([])
const groups = ref([])
const displayGroups = computed(() => {
  const uid = auth.user?.id
  const gid = auth.user?.groupId
  if (canManage.value) return groups.value
  // partner 和 sales：只看自己作为组长的小组，或自己所在的小组
  return groups.value.filter(g => g.leaderId === uid || g.id === gid)
})

// Create
const showCreate = ref(false)
const formError = ref('')
const form = reactive({ name: '', leaderId: null })

// Edit
const showEdit = ref(false)
const editError = ref('')
const editId = ref(null)
const editForm = reactive({ name: '', leaderId: null })

// 编辑时可选的组长：该小组的成员（admin/partner 从 allUsers，组长从 fetchedMembers）
const editGroupUsers = computed(() => {
  if (canManage.value) {
    return editId.value ? allUsers.value.filter(u => u.groupId === editId.value) : []
  }
  // 组长编辑时，用 fetchedMembers（从 API 获取的成员列表）
  return editMembersList.value
})

// Members
const showMembers = ref(false)
const currentGroup = ref(null)
const addUserId = ref(null)

const groupMembers = computed(() =>
  currentGroup.value ? allUsers.value.filter(u => u.groupId === currentGroup.value.id) : []
)
const fetchedMembers = ref([])
const editMembersList = ref([])
const displayMembers = computed(() => {
  if (canManage.value) return groupMembers.value
  return fetchedMembers.value
})

// 创建时可选的组长：非管理员且不在任何小组的人
const createLeaderCandidates = computed(() =>
  allUsers.value.filter(u => u.role !== 'admin' && !u.groupId)
)

// 可添加的人员
const availableUsers = ref([])

async function fetchUsers() {
  try {
    const res = await api.get('/users', { params: { size: 999 } })
    allUsers.value = res.data?.records || []
  } catch { /* empty */ }
}

async function fetchGroups() {
  try {
    const res = await api.get('/groups')
    groups.value = res.data || []
  } catch { /* empty */ }
}

async function fetchAvailableMembers(groupId) {
  try {
    const res = await api.get(`/groups/${groupId}/available-members`)
    availableUsers.value = res.data || []
  } catch { availableUsers.value = [] }
}

function openCreate() {
  form.name = ''; form.leaderId = null
  formError.value = ''
  showCreate.value = true
}

async function createGroup() {
  formError.value = ''
  if (!form.name) { formError.value = '请填写小组名称'; return }
  if (!form.leaderId) { formError.value = '请选择组长'; return }
  try {
    await api.post('/groups', { name: form.name, leaderId: form.leaderId })
    showCreate.value = false
    fetchGroups(); fetchUsers()
  } catch (e) { formError.value = e?.message || '创建失败' }
}

async function openEdit(g) {
  editId.value = g.id
  editForm.name = g.name
  editForm.leaderId = g.leaderId || null
  editError.value = ''
  // 组长需要从 API 获取成员列表
  if (!canManage.value) {
    try {
      const res = await api.get(`/groups/${g.id}/members`)
      editMembersList.value = res.data || []
    } catch { editMembersList.value = [] }
  }
  showEdit.value = true
}

async function updateGroup() {
  editError.value = ''
  try {
    await api.put(`/groups/${editId.value}`, { ...editForm })
    showEdit.value = false
    fetchGroups()
  } catch (e) { editError.value = e?.message || '保存失败' }
}

async function openMembers(g) {
  currentGroup.value = g
  addUserId.value = null
  fetchedMembers.value = []
  availableUsers.value = []
  showMembers.value = true
  if (!canManage.value) {
    try {
      const res = await api.get(`/groups/${g.id}/members`)
      fetchedMembers.value = res.data || []
    } catch { /* empty */ }
  }
  // 组长或 admin/partner 打开成员管理时，加载可添加人员
  if (canManageGroup(g)) {
    fetchAvailableMembers(g.id)
  }
}

async function addMember() {
  if (!addUserId.value || !currentGroup.value) return
  try {
    await api.post(`/groups/${currentGroup.value.id}/members`, { userId: addUserId.value })
    addUserId.value = null
    if (canManage.value) { fetchUsers(); fetchGroups() }
    else {
      // 组长：刷新成员列表和可添加人员
      const res = await api.get(`/groups/${currentGroup.value.id}/members`)
      fetchedMembers.value = res.data || []
      fetchAvailableMembers(currentGroup.value.id)
      fetchGroups()
    }
  } catch { /* empty */ }
}

async function removeMember(userId) {
  if (!currentGroup.value) return
  try {
    await api.delete(`/groups/${currentGroup.value.id}/members/${userId}`)
    if (canManage.value) { fetchUsers(); fetchGroups() }
    else {
      const res = await api.get(`/groups/${currentGroup.value.id}/members`)
      fetchedMembers.value = res.data || []
      fetchAvailableMembers(currentGroup.value.id)
      fetchGroups()
    }
  } catch { /* empty */ }
}

async function confirmDelete(g) {
  const ok = await confirm(`确定删除小组「${g.name}」吗？成员将自动移出。`, { title: '删除小组' })
  if (!ok) return
  try {
    await api.delete(`/groups/${g.id}`)
    fetchGroups(); fetchUsers()
  } catch { /* empty */ }
}

onMounted(() => {
  if (canManage.value) fetchUsers()
  fetchGroups()
})
</script>
