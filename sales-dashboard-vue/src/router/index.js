import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'login', component: () => import('../pages/Login.vue'), meta: { public: true } },
  { path: '/', name: 'dashboard', component: () => import('../pages/Dashboard.vue') },
  { path: '/data-entry', name: 'dataentry', component: () => import('../pages/DataEntry.vue'), meta: { roles: ['partner', 'sales'] } },
  { path: '/service-entry', name: 'serviceentry', component: () => import('../pages/ServiceEntry.vue'), meta: { roles: ['service'] } },
  { path: '/records', name: 'records', component: () => import('../pages/Records.vue'), meta: { roles: ['admin', 'partner', 'sales'] } },
  { path: '/service-records', name: 'serviceRecords', component: () => import('../pages/ServiceRecords.vue'), meta: { roles: ['admin', 'partner', 'service'] } },
  { path: '/records/:userId', name: 'recordDetail', component: () => import('../pages/RecordDetail.vue'), meta: { roles: ['admin', 'partner', 'sales'] } },
  { path: '/service-records/:userId', name: 'serviceRecordDetail', component: () => import('../pages/ServiceRecordDetail.vue'), meta: { roles: ['admin', 'partner', 'service'] } },
  { path: '/team', name: 'team', component: () => import('../pages/TeamManage.vue'), meta: { roles: ['admin', 'partner', 'sales'] } },
  { path: '/team/:id', name: 'teamDetail', component: () => import('../pages/TeamDetail.vue'), meta: { roles: ['admin', 'partner', 'sales'] } },
  { path: '/groups', name: 'groups', component: () => import('../pages/GroupManage.vue'), meta: { roles: ['admin', 'partner', 'sales'] } },
  { path: '/members', name: 'members', component: () => import('../pages/Members.vue'), meta: { roles: ['admin', 'partner'] } },
  { path: '/profile', name: 'profile', component: () => import('../pages/Profile.vue') },
  { path: '/change-password', name: 'changePassword', component: () => import('../pages/ChangePassword.vue') },
  { path: '/platforms', name: 'platforms', component: () => import('../pages/PlatformManage.vue'), meta: { roles: ['admin', 'partner'] } },
  { path: '/backfill', name: 'backfill', component: () => import('../pages/BackfillEntry.vue'), meta: { roles: ['admin', 'partner'] } },
  { path: '/level-config', name: 'levelConfig', component: () => import('../pages/LevelConfig.vue'), meta: { roles: ['admin'] } },
  { path: '/quarterly-report', name: 'quarterlyReport', component: () => import('../pages/QuarterlyReport.vue'), meta: { roles: ['admin'] } },
  { path: '/weekly-archive', name: 'weeklyArchive', component: () => import('../pages/WeeklyArchive.vue'), meta: { roles: ['admin', 'partner', 'sales'] } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (!to.meta.public && !token) return { name: 'login' }
  if (to.name === 'login' && token) return { name: 'dashboard' }

  // Parse user once
  let user = {}
  try { user = JSON.parse(localStorage.getItem('user') || '{}') } catch { /* ignore */ }
  const role = user.role || ''

  // 强制修改密码
  if (user.passwordChanged === false && to.name !== 'changePassword' && to.name !== 'login') {
    return { name: 'changePassword' }
  }

  if (to.meta.roles && !to.meta.roles.includes(role)) {
    return role === 'service' ? { name: 'serviceentry' } : { name: 'dashboard' }
  }
  if (to.name === 'dashboard' && role === 'service') {
    return { name: 'serviceentry' }
  }
})

export default router
