<template>
  <div class="max-w-[1100px] mx-auto space-y-4">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-[15px] font-semibold text-white font-sans">渠道管理</h2>
        <p class="text-[11px] text-trust-300 mt-0.5 font-sans">配置业绩录入可选的渠道平台</p>
      </div>
      <button @click="openCreate" class="flex items-center gap-1.5 px-4 py-2 rounded-xl text-[12px] font-semibold bg-gradient-to-r from-brand to-brand-light text-white hover:opacity-90 transition-opacity cursor-pointer shadow-lg shadow-brand/15 font-sans">
        <svg v-bind="iconDefaults" class="w-3.5 h-3.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        新增渠道
      </button>
    </div>

    <!-- Create / Edit Modal -->
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

    <!-- Table -->
    <div class="bg-surface-raised rounded-2xl border border-white/[0.04] overflow-hidden">
      <table class="w-full">
        <thead>
          <tr class="border-b border-white/[0.04]">
            <th class="px-5 py-3.5 text-left text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">排序</th>
            <th class="px-5 py-3.5 text-left text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">编码</th>
            <th class="px-5 py-3.5 text-left text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">渠道</th>
            <th class="px-5 py-3.5 text-left text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">状态</th>
            <th class="px-5 py-3.5 text-left text-[9px] font-semibold text-trust-300 uppercase tracking-[0.12em] font-sans">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in platforms" :key="p.id" class="border-b border-white/[0.02] hover:bg-white/[0.03] transition-colors duration-150">
            <td class="px-5 py-3 text-[11px] text-gray-400 font-mono tabular-nums">{{ p.sort }}</td>
            <td class="px-5 py-3">
              <span class="inline-flex items-center px-2 py-0.5 rounded-md text-[10px] font-bold bg-brand/[0.08] text-brand-light border border-brand/[0.12] font-mono">{{ p.code }}</span>
            </td>
            <td class="px-5 py-3">
              <div class="flex items-center gap-2.5">
                <!-- icon 区域：有上传图片则显示图片，否则显示首字母占位 -->
                <div class="relative group/icon w-8 h-8 rounded-lg bg-white/[0.06] border border-white/[0.06] flex items-center justify-center shrink-0 overflow-hidden cursor-pointer"
                     @click="triggerIconUpload(p)">
                  <img v-if="p.iconUrl" :src="API_BASE + p.iconUrl" :alt="p.label" class="w-full h-full object-cover" />
                  <span v-else class="text-[11px] font-bold text-trust-300 font-mono select-none">{{ (p.label || p.code || '?')[0] }}</span>
                  <!-- hover 遮罩 -->
                  <div class="absolute inset-0 bg-black/60 flex items-center justify-center opacity-0 group-hover/icon:opacity-100 transition-opacity duration-150">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-white">
                      <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/>
                    </svg>
                  </div>
                  <input :ref="el => iconInputRefs[p.id] = el" type="file" accept="image/*" class="hidden"
                         @change="e => uploadIcon(p, e)" />
                </div>
                <div>
                  <p class="text-[12px] font-medium text-gray-200 font-sans">{{ p.label }}</p>
                  <p v-if="p.iconUrl" class="text-[9px] text-trust-300 font-sans mt-0.5 flex items-center gap-1">
                    <span>已上传图标</span>
                    <button @click.stop="removeIcon(p)" class="text-danger-light hover:text-danger-light/70 cursor-pointer transition-colors">移除</button>
                  </p>
                  <p v-else class="text-[9px] text-trust-400 font-sans mt-0.5">点击图标上传</p>
                </div>
              </div>
            </td>
            <td class="px-5 py-3">
              <div class="flex items-center gap-1.5">
                <div :class="['w-1.5 h-1.5 rounded-full', p.status === 'active' ? 'bg-success-light' : 'bg-gray-600']" />
                <span class="text-[10px] text-gray-300 font-sans">{{ p.status === 'active' ? '启用' : '停用' }}</span>
              </div>
            </td>
            <td class="px-5 py-3">
              <div class="flex items-center gap-3">
                <button @click="openEdit(p)" class="text-[10px] text-brand-light hover:text-brand cursor-pointer font-sans font-medium">编辑</button>
                <button v-if="p.status === 'active'" @click="toggleStatus(p, 'inactive')" class="text-[10px] text-accent hover:text-accent/80 cursor-pointer font-sans font-medium">停用</button>
                <button v-else @click="toggleStatus(p, 'active')" class="text-[10px] text-success-light hover:text-success-light/80 cursor-pointer font-sans font-medium">启用</button>
                <button @click="handleDelete(p)" class="text-[10px] text-danger-light hover:text-danger-light/80 cursor-pointer font-sans font-medium">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="platforms.length === 0" class="py-14 text-center text-trust-300 text-[12px] font-sans">暂无渠道数据</div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { iconDefaults } from '../components/icons.js'
import { useConfirm } from '../composables/useConfirm'
import api from '../api'

const API_BASE = import.meta.env.VITE_API_BASE || ''
const { confirm } = useConfirm()

const platforms = ref([])
const showModal = ref(false)
const editing = ref(false)
const editId = ref(null)
const formError = ref('')
const form = reactive({ code: '', label: '', sort: 0 })

const iconInputRefs = reactive({})

function triggerIconUpload(p) {
  iconInputRefs[p.id]?.click()
}

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

async function removeIcon(p) {
  const ok = await confirm(`移除「${p.label}」的图标？`, { title: '移除图标' })
  if (!ok) return
  try {
    await api.delete(`/dict/${p.id}/icon`)
    p.iconUrl = null
  } catch { /* ignore */ }
}

async function fetchPlatforms() {
  try {
    const res = await api.get('/dict/platform', { params: { all: true } })
    platforms.value = res.data || []
  } catch { /* empty */ }
}

function openCreate() {
  editing.value = false
  editId.value = null
  form.code = ''
  form.label = ''
  form.sort = (platforms.value.length + 1) * 1
  formError.value = ''
  showModal.value = true
}

function openEdit(p) {
  editing.value = true
  editId.value = p.id
  form.code = p.code
  form.label = p.label
  form.sort = p.sort
  formError.value = ''
  showModal.value = true
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
    showModal.value = false
    fetchPlatforms()
  } catch (e) {
    formError.value = e?.message || '操作失败'
  }
}

async function toggleStatus(p, status) {
  try {
    await api.put(`/dict/${p.id}`, { status })
    fetchPlatforms()
  } catch { /* ignore */ }
}

async function handleDelete(p) {
  const ok = await confirm(`确定删除渠道「${p.label}」？`, { title: '删除渠道' })
  if (!ok) return
  try {
    await api.delete(`/dict/${p.id}`)
    fetchPlatforms()
  } catch { /* ignore */ }
}

onMounted(fetchPlatforms)
</script>
