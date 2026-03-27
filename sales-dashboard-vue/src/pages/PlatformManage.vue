<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-[15px] font-semibold text-white font-sans">渠道管理</h2>
        <p class="text-[11px] text-trust-300 mt-0.5 font-sans">配置业绩录入可选的渠道平台及账号</p>
      </div>
      <button @click="openCreate" class="flex items-center gap-1.5 px-4 py-2 rounded-xl text-[12px] font-semibold bg-gradient-to-r from-brand to-brand-light text-white hover:opacity-90 transition-opacity cursor-pointer shadow-lg shadow-brand/15 font-sans">
        <svg v-bind="iconDefaults" class="w-3.5 h-3.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        新增渠道
      </button>
    </div>

    <!-- Create / Edit Channel Modal -->
    <div v-if="showModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showModal = false">
      <div class="bg-trust-800 border border-white/[0.08] rounded-2xl p-6 w-[420px] space-y-4">
        <h3 class="text-[14px] font-semibold text-white font-sans">{{ editing ? '编辑渠道' : '新增渠道' }}</h3>
        <div>
          <label class="text-[10px] text-trust-300 font-sans block mb-1">渠道编码</label>
          <input v-model="form.code" placeholder="如 DY、KS" :disabled="editing"
            :class="['w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-4 py-2 text-[12px] text-white placeholder-trust-400 font-mono focus:outline-none focus:ring-2 focus:ring-brand/30', editing ? 'opacity-50' : '']" />
        </div>
        <div>
          <label class="text-[10px] text-trust-300 font-sans block mb-1">渠道名称</label>
          <input v-model="form.label" placeholder="如 抖音、快手"
            class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-4 py-2 text-[12px] text-white placeholder-trust-400 font-sans focus:outline-none focus:ring-2 focus:ring-brand/30" />
        </div>
        <div>
          <label class="text-[10px] text-trust-300 font-sans block mb-1">排序（数字越小越靠前）</label>
          <input v-model.number="form.sort" type="number" placeholder="0"
            class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-4 py-2 text-[12px] text-white placeholder-trust-400 font-mono focus:outline-none focus:ring-2 focus:ring-brand/30" />
        </div>
        <p v-if="formError" class="text-red-400 text-[11px] font-sans">{{ formError }}</p>
        <div class="flex gap-3 justify-end">
          <button @click="showModal = false" class="px-4 py-2 rounded-lg text-[12px] text-trust-300 hover:text-white cursor-pointer font-sans">取消</button>
          <button @click="handleSave" class="px-4 py-2 rounded-lg text-[12px] font-semibold bg-brand text-white hover:bg-brand-light cursor-pointer font-sans">{{ editing ? '保存' : '创建' }}</button>
        </div>
      </div>
    </div>

    <!-- Account Modal -->
    <div v-if="showAccountModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showAccountModal = false">
      <div class="bg-trust-800 border border-white/[0.08] rounded-2xl p-6 w-[420px] space-y-4">
        <h3 class="text-[14px] font-semibold text-white font-sans">{{ editingAccount ? '编辑账号' : '新增账号' }}</h3>
        <div>
          <label class="text-[10px] text-trust-300 font-sans block mb-1">账号名称</label>
          <input v-model="accountForm.accountName" placeholder="如 主账号、子账号A"
            class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-4 py-2 text-[12px] text-white placeholder-trust-400 font-sans focus:outline-none focus:ring-2 focus:ring-brand/30" />
        </div>
        <div>
          <label class="text-[10px] text-trust-300 font-sans block mb-1">排序</label>
          <input v-model.number="accountForm.sort" type="number" placeholder="0"
            class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-4 py-2 text-[12px] text-white placeholder-trust-400 font-mono focus:outline-none focus:ring-2 focus:ring-brand/30" />
        </div>
        <p v-if="accountFormError" class="text-red-400 text-[11px] font-sans">{{ accountFormError }}</p>
        <div class="flex gap-3 justify-end">
          <button @click="showAccountModal = false" class="px-4 py-2 rounded-lg text-[12px] text-trust-300 hover:text-white cursor-pointer font-sans">取消</button>
          <button @click="handleSaveAccount" class="px-4 py-2 rounded-lg text-[12px] font-semibold bg-brand text-white hover:bg-brand-light cursor-pointer font-sans">{{ editingAccount ? '保存' : '创建' }}</button>
        </div>
      </div>
    </div>

    <!-- Shop Modal -->
    <div v-if="showShopModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showShopModal = false">
      <div class="bg-trust-800 border border-white/[0.08] rounded-2xl p-6 w-[420px] space-y-4">
        <h3 class="text-[14px] font-semibold text-white font-sans">{{ editingShop ? '编辑店铺' : '新增店铺' }}</h3>
        <div>
          <label class="text-[10px] text-trust-300 font-sans block mb-1">店铺名称</label>
          <input v-model="shopForm.shopName" placeholder="如 XX旗舰店"
            class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-4 py-2 text-[12px] text-white placeholder-trust-400 font-sans focus:outline-none focus:ring-2 focus:ring-purple-500/30" />
        </div>
        <div>
          <label class="text-[10px] text-trust-300 font-sans block mb-1">排序</label>
          <input v-model.number="shopForm.sort" type="number" placeholder="0"
            class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-4 py-2 text-[12px] text-white placeholder-trust-400 font-mono focus:outline-none focus:ring-2 focus:ring-purple-500/30" />
        </div>
        <p v-if="shopFormError" class="text-red-400 text-[11px] font-sans">{{ shopFormError }}</p>
        <div class="flex gap-3 justify-end">
          <button @click="showShopModal = false" class="px-4 py-2 rounded-lg text-[12px] text-trust-300 hover:text-white cursor-pointer font-sans">取消</button>
          <button @click="handleSaveShop" class="px-4 py-2 rounded-lg text-[12px] font-semibold bg-purple-500 text-white hover:bg-purple-400 cursor-pointer font-sans">{{ editingShop ? '保存' : '创建' }}</button>
        </div>
      </div>
    </div>

    <!-- Platform list -->
    <div v-for="p in platforms" :key="p.id" class="bg-surface-raised rounded-2xl border border-white/[0.04] overflow-hidden">
      <!-- Platform header -->
      <div class="flex items-center justify-between px-5 py-3.5 border-b border-white/[0.04]">
        <div class="flex items-center gap-3">
          <div class="relative group/icon w-8 h-8 rounded-lg bg-white/[0.06] border border-white/[0.06] flex items-center justify-center shrink-0 overflow-hidden cursor-pointer"
               @click="triggerIconUpload(p)">
            <img v-if="p.iconUrl" :src="API_BASE + p.iconUrl" :alt="p.label" class="w-full h-full object-cover" />
            <span v-else class="text-[11px] font-bold text-trust-300 font-mono select-none">{{ (p.label || p.code || '?')[0] }}</span>
            <div class="absolute inset-0 bg-black/60 flex items-center justify-center opacity-0 group-hover/icon:opacity-100 transition-opacity duration-150">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-white"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
            </div>
            <input :ref="el => iconInputRefs[p.id] = el" type="file" accept="image/*" class="hidden" @change="e => uploadIcon(p, e)" />
          </div>
          <div>
            <div class="flex items-center gap-2">
              <span class="inline-flex items-center px-2 py-0.5 rounded-md text-[10px] font-bold bg-brand/[0.08] text-brand-light border border-brand/[0.12] font-mono">{{ p.code }}</span>
              <span class="text-[13px] font-semibold text-white font-sans">{{ p.label }}</span>
              <div class="flex items-center gap-1.5 ml-2">
                <div :class="['w-1.5 h-1.5 rounded-full', p.status === 'active' ? 'bg-success-light' : 'bg-gray-600']" />
                <span class="text-[10px] text-gray-400 font-sans">{{ p.status === 'active' ? '启用' : '停用' }}</span>
              </div>
            </div>
            <p class="text-[10px] text-trust-300 font-sans mt-0.5">{{ (accountsByPlatform[p.code] || []).length }} 个账号 · {{ (shopsByPlatform[p.code] || []).length }} 个店铺</p>
          </div>
        </div>
        <div class="flex items-center gap-3">
          <button @click="openAddAccount(p)" class="flex items-center gap-1 text-[10px] text-brand-light hover:text-white transition-colors cursor-pointer font-sans font-medium">
            <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
            添加账号
          </button>
          <button @click="openAddShop(p)" class="flex items-center gap-1 text-[10px] text-purple-400 hover:text-white transition-colors cursor-pointer font-sans font-medium">
            <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
            添加店铺
          </button>
          <button @click="openEdit(p)" class="text-[10px] text-brand-light hover:text-brand cursor-pointer font-sans font-medium">编辑</button>
          <button v-if="p.status === 'active'" @click="toggleStatus(p, 'inactive')" class="text-[10px] text-accent hover:text-accent/80 cursor-pointer font-sans font-medium">停用</button>
          <button v-else @click="toggleStatus(p, 'active')" class="text-[10px] text-success-light hover:text-success-light/80 cursor-pointer font-sans font-medium">启用</button>
          <button @click="handleDelete(p)" class="text-[10px] text-danger-light hover:text-danger-light/80 cursor-pointer font-sans font-medium">删除</button>
        </div>
      </div>
      <!-- Account list -->
      <div v-if="(accountsByPlatform[p.code] || []).length > 0" class="divide-y divide-white/[0.03]">
        <div v-for="acc in accountsByPlatform[p.code]" :key="acc.id" class="flex items-center justify-between px-5 py-2.5 hover:bg-white/[0.02] transition-colors">
          <div class="flex items-center gap-2.5">
            <span class="w-5 h-5 rounded bg-white/[0.06] flex items-center justify-center text-[9px] font-bold text-trust-300 font-mono">{{ acc.sort }}</span>
            <span class="text-[12px] text-gray-200 font-sans">{{ acc.accountName }}</span>
            <div class="flex items-center gap-1">
              <div :class="['w-1 h-1 rounded-full', acc.status === 'active' ? 'bg-success-light' : 'bg-gray-600']" />
              <span class="text-[9px] text-gray-500 font-sans">{{ acc.status === 'active' ? '启用' : '停用' }}</span>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <button @click="openEditAccount(p, acc)" class="text-[10px] text-brand-light hover:text-brand cursor-pointer font-sans">编辑</button>
            <button v-if="acc.status === 'active'" @click="toggleAccountStatus(acc, 'inactive')" class="text-[10px] text-accent hover:text-accent/80 cursor-pointer font-sans">停用</button>
            <button v-else @click="toggleAccountStatus(acc, 'active')" class="text-[10px] text-success-light hover:text-success-light/80 cursor-pointer font-sans">启用</button>
            <button @click="handleDeleteAccount(acc)" class="text-[10px] text-danger-light hover:text-danger-light/80 cursor-pointer font-sans">删除</button>
          </div>
        </div>
      </div>
      <div v-else class="px-5 py-4 text-center text-[11px] text-trust-400 font-sans">暂无账号，点击"添加账号"创建</div>
      <!-- Shop list -->
      <div v-if="(shopsByPlatform[p.code] || []).length > 0" class="border-t border-white/[0.04]">
        <div class="px-5 py-2 bg-purple-500/[0.03]">
          <span class="text-[9px] font-semibold text-purple-400 uppercase tracking-[0.12em] font-sans">店铺（客服用）</span>
        </div>
        <div class="divide-y divide-white/[0.03]">
          <div v-for="shop in shopsByPlatform[p.code]" :key="shop.id" class="flex items-center justify-between px-5 py-2.5 hover:bg-white/[0.02] transition-colors">
            <div class="flex items-center gap-2.5">
              <span class="w-5 h-5 rounded bg-purple-500/[0.1] flex items-center justify-center text-[9px] font-bold text-purple-400 font-mono">{{ shop.sort }}</span>
              <span class="text-[12px] text-gray-200 font-sans">{{ shop.shopName }}</span>
              <div class="flex items-center gap-1">
                <div :class="['w-1 h-1 rounded-full', shop.status === 'active' ? 'bg-success-light' : 'bg-gray-600']" />
                <span class="text-[9px] text-gray-500 font-sans">{{ shop.status === 'active' ? '启用' : '停用' }}</span>
              </div>
            </div>
            <div class="flex items-center gap-3">
              <button @click="openEditShop(p, shop)" class="text-[10px] text-purple-400 hover:text-purple-300 cursor-pointer font-sans">编辑</button>
              <button v-if="shop.status === 'active'" @click="toggleShopStatus(shop, 'inactive')" class="text-[10px] text-accent hover:text-accent/80 cursor-pointer font-sans">停用</button>
              <button v-else @click="toggleShopStatus(shop, 'active')" class="text-[10px] text-success-light hover:text-success-light/80 cursor-pointer font-sans">启用</button>
              <button @click="handleDeleteShop(shop)" class="text-[10px] text-danger-light hover:text-danger-light/80 cursor-pointer font-sans">删除</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="platforms.length === 0" class="bg-surface-raised rounded-2xl border border-white/[0.04] py-14 text-center text-trust-300 text-[12px] font-sans">暂无渠道数据</div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { iconDefaults } from '../components/icons.js'
import { useConfirm } from '../composables/useConfirm'
import api from '../api'

const API_BASE = import.meta.env.VITE_API_BASE || ''
const { confirm } = useConfirm()

const platforms = ref([])
const allAccounts = ref([])
const allShops = ref([])
const showModal = ref(false)
const editing = ref(false)
const editId = ref(null)
const formError = ref('')
const form = reactive({ code: '', label: '', sort: 0 })

const iconInputRefs = reactive({})

// Account modal
const showAccountModal = ref(false)
const editingAccount = ref(false)
const editAccountId = ref(null)
const currentPlatformCode = ref('')
const accountFormError = ref('')
const accountForm = reactive({ accountName: '', sort: 0 })

// Shop modal
const showShopModal = ref(false)
const editingShop = ref(false)
const editShopId = ref(null)
const shopPlatformCode = ref('')
const shopFormError = ref('')
const shopForm = reactive({ shopName: '', sort: 0 })

const accountsByPlatform = computed(() => {
  const map = {}
  for (const acc of allAccounts.value) {
    if (!map[acc.platformCode]) map[acc.platformCode] = []
    map[acc.platformCode].push(acc)
  }
  return map
})

const shopsByPlatform = computed(() => {
  const map = {}
  for (const s of allShops.value) {
    if (!map[s.platformCode]) map[s.platformCode] = []
    map[s.platformCode].push(s)
  }
  return map
})

function triggerIconUpload(p) { iconInputRefs[p.id]?.click() }

async function uploadIcon(p, e) {
  const file = e.target.files?.[0]
  if (!file) return
  try {
    const fd = new FormData()
    fd.append('file', file)
    const res = await api.post(`/dict/${p.id}/icon`, fd)
    p.iconUrl = res.data?.iconUrl
  } catch (err) {
    await confirm(err?.message || '上传失败', { title: '上传失败', confirmText: '知道了', type: 'danger' })
  }
  e.target.value = ''
}

async function fetchPlatforms() {
  try {
    const res = await api.get('/dict/platform', { params: { all: true } })
    platforms.value = res.data || []
  } catch { /* empty */ }
}

async function fetchAccounts() {
  try {
    const res = await api.get('/platform-accounts')
    allAccounts.value = res.data || []
  } catch { /* empty */ }
}

async function fetchShops() {
  try {
    const res = await api.get('/platform-shops')
    allShops.value = res.data || []
  } catch { /* empty */ }
}

function openCreate() {
  editing.value = false; editId.value = null
  form.code = ''; form.label = ''; form.sort = (platforms.value.length + 1)
  formError.value = ''; showModal.value = true
}

function openEdit(p) {
  editing.value = true; editId.value = p.id
  form.code = p.code; form.label = p.label; form.sort = p.sort
  formError.value = ''; showModal.value = true
}

async function handleSave() {
  formError.value = ''
  if (!form.code || !form.label) { formError.value = '编码和名称不能为空'; return }
  try {
    if (editing.value) {
      await api.put(`/dict/${editId.value}`, { type: 'platform', code: form.code, label: form.label, sort: form.sort })
    } else {
      await api.post('/dict', { type: 'platform', code: form.code, label: form.label, sort: form.sort })
    }
    showModal.value = false; fetchPlatforms()
  } catch (e) { formError.value = e?.message || '操作失败' }
}

async function toggleStatus(p, status) {
  try { await api.put(`/dict/${p.id}`, { status }); fetchPlatforms() } catch { /* ignore */ }
}

async function handleDelete(p) {
  const ok = await confirm(`确定删除渠道「${p.label}」？`, { title: '删除渠道' })
  if (!ok) return
  try { await api.delete(`/dict/${p.id}`); fetchPlatforms() } catch { /* ignore */ }
}

// Account CRUD
function openAddAccount(p) {
  editingAccount.value = false; editAccountId.value = null
  currentPlatformCode.value = p.code
  accountForm.accountName = ''; accountForm.sort = (accountsByPlatform.value[p.code] || []).length + 1
  accountFormError.value = ''; showAccountModal.value = true
}

function openEditAccount(p, acc) {
  editingAccount.value = true; editAccountId.value = acc.id
  currentPlatformCode.value = p.code
  accountForm.accountName = acc.accountName; accountForm.sort = acc.sort
  accountFormError.value = ''; showAccountModal.value = true
}

async function handleSaveAccount() {
  accountFormError.value = ''
  if (!accountForm.accountName) { accountFormError.value = '账号名称不能为空'; return }
  try {
    if (editingAccount.value) {
      await api.put(`/platform-accounts/${editAccountId.value}`, { accountName: accountForm.accountName, sort: accountForm.sort })
    } else {
      await api.post('/platform-accounts', { platformCode: currentPlatformCode.value, accountName: accountForm.accountName, sort: accountForm.sort })
    }
    showAccountModal.value = false; fetchAccounts()
  } catch (e) { accountFormError.value = e?.message || '操作失败' }
}

async function toggleAccountStatus(acc, status) {
  try { await api.put(`/platform-accounts/${acc.id}`, { status }); fetchAccounts() } catch { /* ignore */ }
}

async function handleDeleteAccount(acc) {
  const ok = await confirm(`确定删除账号「${acc.accountName}」？`, { title: '删除账号' })
  if (!ok) return
  try { await api.delete(`/platform-accounts/${acc.id}`); fetchAccounts() } catch { /* ignore */ }
}

// Shop CRUD
function openAddShop(p) {
  editingShop.value = false; editShopId.value = null
  shopPlatformCode.value = p.code
  shopForm.shopName = ''; shopForm.sort = (shopsByPlatform.value[p.code] || []).length + 1
  shopFormError.value = ''; showShopModal.value = true
}

function openEditShop(p, shop) {
  editingShop.value = true; editShopId.value = shop.id
  shopPlatformCode.value = p.code
  shopForm.shopName = shop.shopName; shopForm.sort = shop.sort
  shopFormError.value = ''; showShopModal.value = true
}

async function handleSaveShop() {
  shopFormError.value = ''
  if (!shopForm.shopName) { shopFormError.value = '店铺名称不能为空'; return }
  try {
    if (editingShop.value) {
      await api.put(`/platform-shops/${editShopId.value}`, { shopName: shopForm.shopName, sort: shopForm.sort })
    } else {
      await api.post('/platform-shops', { platformCode: shopPlatformCode.value, shopName: shopForm.shopName, sort: shopForm.sort })
    }
    showShopModal.value = false; fetchShops()
  } catch (e) { shopFormError.value = e?.message || '操作失败' }
}

async function toggleShopStatus(shop, status) {
  try { await api.put(`/platform-shops/${shop.id}`, { status }); fetchShops() } catch { /* ignore */ }
}

async function handleDeleteShop(shop) {
  const ok = await confirm(`确定删除店铺「${shop.shopName}」？`, { title: '删除店铺' })
  if (!ok) return
  try { await api.delete(`/platform-shops/${shop.id}`); fetchShops() } catch { /* ignore */ }
}

onMounted(() => { fetchPlatforms(); fetchAccounts(); fetchShops() })
</script>
