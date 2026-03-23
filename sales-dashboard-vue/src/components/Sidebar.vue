<template>
  <aside class="w-[232px] h-full bg-trust-800 flex flex-col shrink-0 border-r border-white/[0.06]">
    <!-- Logo -->
    <div class="h-18 flex items-center px-5 gap-3">
      <img src="/logo.png" alt="Logo" class="w-10 h-10 rounded-lg object-contain" />
      <div>
        <span class="text-[13px] font-semibold text-white tracking-tight font-sans block leading-tight">曙光引擎</span>
        <span class="text-[9px] text-trust-300 font-sans tracking-wider">DAWN ENGINE</span>
      </div>
    </div>

    <!-- Nav -->
    <nav class="flex-1 px-3 pt-3 space-y-5 overflow-y-auto">
      <div v-for="group in filteredNavGroups" :key="group.title">
        <p class="text-[10px] font-semibold text-trust-300 uppercase tracking-[0.12em] px-3 mb-1.5 font-sans">{{ group.title }}</p>
        <div class="space-y-0.5">
          <router-link v-for="item in group.items" :key="item.name" :to="item.to"
            custom v-slot="{ isActive, navigate }">
            <button @click="() => { navigate(); emit('navigate') }"
              :class="['w-full text-left px-3 py-2.5 rounded-xl text-[13px] flex items-center gap-2.5 transition-all duration-200 cursor-pointer relative font-sans',
                isActive ? 'bg-brand/[0.12] text-brand-light font-medium' : 'text-trust-300 hover:text-white hover:bg-white/[0.04]']">
              <div v-if="isActive" class="absolute left-0 top-1/2 -translate-y-1/2 w-[3px] h-5 rounded-r-full bg-brand" />
              <component :is="item.icon" :class="['w-[16px] h-[16px]', isActive ? 'text-brand-light' : 'text-trust-300']" />
              {{ item.label }}
            </button>
          </router-link>
        </div>
      </div>
    </nav>

    <!-- Trust badge -->
    <div class="mx-4 mb-3 p-3 rounded-xl bg-brand/[0.06] border border-brand/[0.1]">
      <div class="flex items-center gap-2 mb-1">
        <IconShield class="w-3.5 h-3.5 text-brand-light" />
        <span class="text-[10px] font-semibold text-brand-light font-sans">数据安全保障</span>
      </div>
      <p class="text-[9px] text-trust-300 font-sans leading-relaxed">端到端加密 · SOC2 合规</p>
    </div>

    <!-- User -->
    <div class="px-4 py-3.5 border-t border-white/[0.06]">
      <router-link to="/profile" class="flex items-center gap-2.5 cursor-pointer group">
        <img v-if="avatarBlobUrl" :src="avatarBlobUrl" :alt="auth.user?.name" class="w-8 h-8 rounded-full object-cover" />
        <div v-else class="w-8 h-8 rounded-full bg-brand flex items-center justify-center text-white text-[11px] font-semibold font-mono">{{ avatar }}</div>
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-1.5">
            <p class="text-[12px] font-medium text-white font-sans truncate group-hover:text-brand-light transition-colors">{{ auth.user?.name || '用户' }}</p>
            <LevelBadge :level="auth.user?.estimatedLevel || auth.user?.level" :role="auth.user?.role" />
          </div>
          <p class="text-[10px] text-trust-300 font-sans">{{ roleMap[auth.user?.role] || '销售' }}</p>
        </div>
        <svg v-bind="iconDefaults" class="w-3.5 h-3.5 text-trust-300 opacity-0 group-hover:opacity-100 transition-opacity"><path d="M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z"/><circle cx="12" cy="12" r="3"/></svg>
      </router-link>
    </div>
  </aside>
</template>

<script setup>
import { h, computed } from 'vue'
import { iconDefaults } from './icons.js'
import { useAuthStore } from '../stores/auth'
import { useAuthImage } from '../composables/useAuthImage'
import LevelBadge from './LevelBadge.vue'

const auth = useAuthStore()
const emit = defineEmits(['navigate'])
const roleMap = { admin: '管理员', partner: '合伙人', sales: '运营', service: '客服' }
const avatar = computed(() => (auth.user?.name || '用')[0])

const avatarPath = computed(() => auth.user?.avatar || '')
const { blobUrl: avatarBlobUrl } = useAuthImage(avatarPath)

const IconGrid = (_, { attrs }) => h('svg', { ...iconDefaults, ...attrs, innerHTML: '<rect x="3" y="3" width="7" height="7" rx="1.5"/><rect x="14" y="3" width="7" height="7" rx="1.5"/><rect x="3" y="14" width="7" height="7" rx="1.5"/><rect x="14" y="14" width="7" height="7" rx="1.5"/>' })
const IconEdit = (_, { attrs }) => h('svg', { ...iconDefaults, ...attrs, innerHTML: '<path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/>' })
const IconFile = (_, { attrs }) => h('svg', { ...iconDefaults, ...attrs, innerHTML: '<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/>' })
const IconUsers = (_, { attrs }) => h('svg', { ...iconDefaults, ...attrs, innerHTML: '<path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>' })
const IconUser = (_, { attrs }) => h('svg', { ...iconDefaults, ...attrs, innerHTML: '<path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>' })
const IconShield = (_, { attrs }) => h('svg', { ...iconDefaults, ...attrs, innerHTML: '<path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>' })
const IconLayers = (_, { attrs }) => h('svg', { ...iconDefaults, ...attrs, innerHTML: '<polygon points="12 2 2 7 12 12 22 7 12 2"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/>' })
const IconFolder = (_, { attrs }) => h('svg', { ...iconDefaults, ...attrs, innerHTML: '<path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>' })
const IconTrending = (_, { attrs }) => h('svg', { ...iconDefaults, ...attrs, innerHTML: '<polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/><polyline points="17 6 23 6 23 12"/>' })
const IconCalendar = (_, { attrs }) => h('svg', { ...iconDefaults, ...attrs, innerHTML: '<rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>' })
const IconHeadphones = (_, { attrs }) => h('svg', { ...iconDefaults, ...attrs, innerHTML: '<path d="M3 18v-6a9 9 0 0 1 18 0v6"/><path d="M21 19a2 2 0 0 1-2 2h-1a2 2 0 0 1-2-2v-3a2 2 0 0 1 2-2h3zM3 19a2 2 0 0 0 2 2h1a2 2 0 0 0 2-2v-3a2 2 0 0 0-2-2H3z"/>' })
const IconClipboard = (_, { attrs }) => h('svg', { ...iconDefaults, ...attrs, innerHTML: '<path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><rect x="8" y="2" width="8" height="4" rx="1" ry="1"/><line x1="12" y1="11" x2="12" y2="17"/><line x1="9" y1="14" x2="15" y2="14"/>' })

const navGroups = [
  {
    title: '工作台',
    items: [
      { name: 'dashboard', to: '/', label: '销售大盘看板', icon: IconGrid, roles: ['admin', 'partner', 'sales'] },
      { name: 'dataentry', to: '/data-entry', label: '昨日业绩录入', icon: IconEdit, roles: ['partner', 'sales'] },
      { name: 'serviceentry', to: '/service-entry', label: '客服日报录入', icon: IconHeadphones, roles: ['service'] },
      { name: 'backfill', to: '/backfill', label: '业绩补录', icon: IconClipboard, roles: ['admin', 'partner'] },
      { name: 'records', to: '/records', label: '业绩查看', icon: IconFile, roles: ['admin', 'partner', 'sales'] },
      { name: 'serviceRecords', to: '/service-records', label: '客服业绩查看', icon: IconHeadphones, roles: ['admin', 'partner', 'service'] },
    ],
  },
  {
    title: '管理',
    items: [
      { name: 'team', to: '/team', label: '团队管理', icon: IconUsers, roles: ['admin', 'partner'] },
      { name: 'groups', to: '/groups', label: '小组管理', icon: IconFolder, roles: ['admin', 'partner'] },
      { name: 'members', to: '/members', label: '人员管理', icon: IconUser, roles: ['admin', 'partner'] },
      { name: 'platforms', to: '/platforms', label: '渠道管理', icon: IconLayers, roles: ['admin', 'partner'] },
      { name: 'levelConfig', to: '/level-config', label: '职级设定', icon: IconTrending, roles: ['admin'] },
      { name: 'quarterlyReport', to: '/quarterly-report', label: '季度报表', icon: IconCalendar, roles: ['admin'] },
    ],
  },
]

const filteredNavGroups = computed(() =>
  navGroups.map(g => ({
    ...g,
    items: g.items.filter(item => !item.roles || item.roles.includes(auth.user?.role)),
  })).filter(g => g.items.length > 0)
)
</script>
