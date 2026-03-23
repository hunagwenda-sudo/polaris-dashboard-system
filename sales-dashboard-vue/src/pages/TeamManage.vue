<template>
  <div class="space-y-5">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-[15px] font-semibold text-white font-sans">团队管理</h2>
        <p class="text-[11px] text-trust-300 mt-0.5 font-sans">{{ subtitle }}</p>
      </div>
      <button v-if="isAdmin" @click="showCreate = true" class="flex items-center gap-1.5 px-4 py-2 rounded-xl text-[12px] font-semibold bg-gradient-to-r from-brand to-brand-light text-white hover:opacity-90 transition-opacity cursor-pointer shadow-lg shadow-brand/15 font-sans">
        <svg v-bind="iconDefaults" class="w-3.5 h-3.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        新建团队
      </button>
    </div>

    <!-- Create modal -->
    <div v-if="showCreate" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showCreate = false">
      <div class="bg-trust-800 border border-white/[0.08] rounded-2xl p-6 w-[400px] space-y-4">
        <h3 class="text-[14px] font-semibold text-white font-sans">新建团队</h3>
        <input v-model="newTeam.name" placeholder="团队名称" class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-4 py-2 text-[12px] text-white placeholder-trust-400 font-sans focus:outline-none focus:ring-2 focus:ring-brand/30" />
        <div>
          <label class="text-[10px] text-trust-300 font-sans block mb-1">团队负责人（合伙人，可多选）</label>
          <div class="flex flex-wrap gap-2 p-2 bg-white/[0.03] border border-white/[0.06] rounded-lg min-h-[38px]">
            <label v-for="p in partners" :key="p.id" class="flex items-center gap-1.5 px-2.5 py-1 rounded-lg text-[11px] font-sans cursor-pointer transition-colors"
              :class="newTeam.leaderIds.includes(Number(p.id)) ? 'bg-brand/20 text-brand-light border border-brand/30' : 'bg-white/[0.04] text-trust-300 border border-white/[0.06] hover:bg-white/[0.08]'">
              <input type="checkbox" :value="Number(p.id)" v-model="newTeam.leaderIds" class="hidden" />
              {{ p.name }}
            </label>
            <span v-if="partners.length === 0" class="text-[11px] text-trust-400 font-sans">暂无合伙人</span>
          </div>
        </div>
        <div class="flex gap-3 justify-end">
          <button @click="showCreate = false" class="px-4 py-2 rounded-lg text-[12px] text-trust-300 hover:text-white cursor-pointer font-sans">取消</button>
          <button @click="createTeam" class="px-4 py-2 rounded-lg text-[12px] font-semibold bg-brand text-white hover:bg-brand-light cursor-pointer font-sans">创建</button>
        </div>
      </div>
    </div>

    <!-- Edit modal -->
    <div v-if="showEdit" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showEdit = false">
      <div class="bg-trust-800 border border-white/[0.08] rounded-2xl p-6 w-[400px] space-y-4">
        <h3 class="text-[14px] font-semibold text-white font-sans">编辑团队</h3>
        <div>
          <label class="text-[10px] text-trust-300 font-sans block mb-1">团队名称</label>
          <input v-model="editForm.name" class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-4 py-2 text-[12px] text-white placeholder-trust-400 font-sans focus:outline-none focus:ring-2 focus:ring-brand/30" />
        </div>
        <div>
          <label class="text-[10px] text-trust-300 font-sans block mb-1">团队负责人（合伙人，可多选）</label>
          <template v-if="isAdmin">
            <div class="flex flex-wrap gap-2 p-2 bg-white/[0.03] border border-white/[0.06] rounded-lg min-h-[38px]">
              <label v-for="p in partners" :key="p.id" class="flex items-center gap-1.5 px-2.5 py-1 rounded-lg text-[11px] font-sans cursor-pointer transition-colors"
                :class="editForm.leaderIds.includes(Number(p.id)) ? 'bg-brand/20 text-brand-light border border-brand/30' : 'bg-white/[0.04] text-trust-300 border border-white/[0.06] hover:bg-white/[0.08]'">
                <input type="checkbox" :value="Number(p.id)" v-model="editForm.leaderIds" class="hidden" />
                {{ p.name }}
              </label>
              <span v-if="partners.length === 0" class="text-[11px] text-trust-400 font-sans">暂无合伙人</span>
            </div>
          </template>
          <template v-else>
            <div class="w-full bg-white/[0.02] border border-white/[0.04] rounded-lg px-4 py-2 text-[12px] text-gray-400 font-sans">{{ editLeaderName || '未指定' }}</div>
          </template>
        </div>
        <div class="flex gap-3 justify-end">
          <button @click="showEdit = false" class="px-4 py-2 rounded-lg text-[12px] text-trust-300 hover:text-white cursor-pointer font-sans">取消</button>
          <button @click="updateTeam" class="px-4 py-2 rounded-lg text-[12px] font-semibold bg-brand text-white hover:bg-brand-light cursor-pointer font-sans">保存</button>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-3 gap-4">
      <div v-for="t in displayTeams" :key="t.id"
        class="bg-surface-raised rounded-2xl border border-white/[0.04] overflow-hidden transition-all duration-200">
        <div :class="['h-1 bg-gradient-to-r', gradients[t.id % gradients.length]]" />
        <div class="p-5 space-y-3.5">
          <!-- 头部：头像 + 团队名 + 负责人 -->
          <div class="flex items-center gap-2.5">
            <div :class="['w-9 h-9 rounded-xl bg-gradient-to-br flex items-center justify-center text-white text-[12px] font-bold font-mono shrink-0', gradients[t.id % gradients.length]]">{{ (t.leaderName || '?')[0] }}</div>
            <div class="min-w-0">
              <h3 class="text-[14px] font-bold text-white font-sans leading-tight truncate">{{ t.name }}</h3>
              <p class="text-[10px] text-trust-300 font-sans mt-0.5">负责人: {{ t.leaderName || '未指定' }}</p>
            </div>
          </div>

          <!-- 进度条 -->
          <div class="space-y-1.5">
            <div class="flex items-center justify-between">
              <span class="text-[9px] text-trust-300 font-sans uppercase tracking-wider">季度进度</span>
              <span :class="['text-[11px] font-bold font-mono tabular-nums', Number(t.completionRate) >= 100 ? 'text-success-light' : 'text-brand-light']">{{ t.completionRate || 0 }}%</span>
            </div>
            <div class="w-full h-1.5 bg-white/[0.04] rounded-full overflow-hidden">
              <div :class="['h-full rounded-full bg-gradient-to-r transition-all duration-700', gradients[t.id % gradients.length]]" :style="{ width: Math.min(Number(t.completionRate) || 0, 100) + '%' }" />
            </div>
          </div>

          <!-- 数据行 -->
          <div class="grid grid-cols-3 gap-2">
            <div class="bg-white/[0.02] rounded-lg px-3 py-2.5 border border-white/[0.03]">
              <p class="text-[9px] text-trust-300 font-sans tracking-wider">季度累计</p>
              <p class="text-[14px] font-extrabold text-white font-mono tabular-nums mt-0.5">{{ fmtW(t.quarterDgmv) }}</p>
            </div>
            <div class="bg-white/[0.02] rounded-lg px-3 py-2.5 border border-white/[0.03]">
              <p class="text-[9px] text-trust-300 font-sans tracking-wider">季度目标</p>
              <p class="text-[14px] font-extrabold text-gray-400 font-mono tabular-nums mt-0.5">{{ fmtW(t.targetDgmv) }}</p>
            </div>
            <div class="bg-white/[0.02] rounded-lg px-3 py-2.5 border border-white/[0.03]">
              <p class="text-[9px] text-trust-300 font-sans tracking-wider">成员</p>
              <p class="text-[14px] font-extrabold text-white font-mono tabular-nums mt-0.5">{{ t.memberCount || 0 }}<span class="text-[10px] text-trust-300 font-normal ml-0.5">人</span></p>
            </div>
          </div>
        </div>

        <!-- 底部操作栏 -->
        <div class="border-t border-white/[0.04] px-5 py-3 flex items-center gap-2">
          <button @click="$router.push(`/team/${t.id}`)" class="flex items-center gap-1 px-3 py-1.5 rounded-lg text-[11px] font-medium bg-brand/[0.08] text-brand-light hover:bg-brand/[0.15] transition-colors cursor-pointer font-sans">
            <svg v-bind="iconDefaults" class="w-3.5 h-3.5"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
            查看详情
          </button>
          <button v-if="canManageTeam(t)" @click="openEdit(t)" class="flex items-center gap-1 px-3 py-1.5 rounded-lg text-[11px] font-medium bg-white/[0.04] text-gray-300 hover:bg-white/[0.08] hover:text-white transition-colors cursor-pointer font-sans">
            <svg v-bind="iconDefaults" class="w-3.5 h-3.5"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
            基本信息
          </button>
          <button v-if="isAdmin" @click="confirmDelete(t)" class="flex items-center gap-1 px-3 py-1.5 rounded-lg text-[11px] font-medium bg-white/[0.04] text-danger-light/70 hover:bg-danger/10 hover:text-danger-light transition-colors cursor-pointer font-sans ml-auto">
            <svg v-bind="iconDefaults" class="w-3.5 h-3.5"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
            删除
          </button>
        </div>
      </div>
    </div>
    <div v-if="displayTeams.length === 0" class="py-14 text-center text-trust-300 text-[12px] font-sans">暂无团队数据</div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { iconDefaults } from '../components/icons.js'
import { useConfirm } from '../composables/useConfirm'
import { useAuthStore } from '../stores/auth'
import api from '../api'

const { confirm } = useConfirm()

const auth = useAuthStore()
const isAdmin = computed(() => auth.user?.role === 'admin')
const allTeams = ref([])
const displayTeams = computed(() => {
  if (isAdmin.value) return allTeams.value
  const uid = auth.user?.id
  const tid = auth.user?.teamId
  // 合伙人：能看到自己作为负责人的团队，或自己所在的团队
  return allTeams.value.filter(t => {
    const ids = t.leaderIds || []
    return ids.includes(uid) || t.leaderId === uid || t.id === tid
  })
})

// 是否是某个团队的负责人
function canManageTeam(t) {
  if (!t) return false
  if (isAdmin.value) return true
  const uid = auth.user?.id
  const ids = t.leaderIds || []
  return ids.includes(uid) || t.leaderId === uid
}

const subtitle = computed(() => {
  if (isAdmin.value) return '管理团队信息、查看团队业绩概览'
  const myTeam = displayTeams.value[0]
  if (myTeam && canManageTeam(myTeam)) return '管理所在团队信息和业绩概览'
  return '查看所在团队信息和业绩概览'
})
const showCreate = ref(false)
const newTeam = reactive({ name: '', leaderIds: [] })
const showEdit = ref(false)
const editTeamId = ref(null)
const editForm = ref({ name: '', leaderIds: [] })
const editLeaderName = ref('')
const partners = ref([])

async function fetchPartners() {
  if (!isAdmin.value) return  // 只有 admin 才能拉合伙人列表
  try {
    const res = await api.get('/teams/partners')
    partners.value = res.data || []
  } catch { /* ignore */ }
}
const gradients = ['from-brand to-brand-light', 'from-brand-light to-brand-light', 'from-brand-light to-success-light']
const fmtW = (n) => `¥${(Number(n) / 10000).toFixed(1)}万`

async function fetchTeams() {
  try {
    const res = await api.get('/teams')
    allTeams.value = res.data || []
  } catch { /* empty */ }
}

async function createTeam() {
  try {
    await api.post('/teams', { name: newTeam.name, leaderIds: newTeam.leaderIds.length > 0 ? newTeam.leaderIds : null })
    showCreate.value = false
    newTeam.name = ''; newTeam.leaderIds = []
    fetchTeams()
  } catch { /* ignore */ }
}

async function openEdit(team) {
  showEdit.value = false
  editTeamId.value = team.id
  editForm.value = {
    name: team.name,
    leaderIds: (team.leaderIds || []).map(Number)
  }
  editLeaderName.value = team.leaderName || ''
  await nextTick()
  showEdit.value = true
}

async function updateTeam() {
  try {
    await api.put(`/teams/${editTeamId.value}`, { name: editForm.value.name, leaderIds: editForm.value.leaderIds.length > 0 ? editForm.value.leaderIds : null })
    showEdit.value = false
    fetchTeams()
  } catch { /* ignore */ }
}

async function confirmDelete(t) {
  const ok = await confirm(`确定删除团队「${t.name}」吗？`, { title: '删除团队' })
  if (!ok) return
  try {
    await api.delete(`/teams/${t.id}`)
    fetchTeams()
  } catch { /* ignore */ }
}

onMounted(() => { fetchTeams(); fetchPartners() })
</script>
