<template>
  <div class="space-y-4">
    <div>
      <h2 class="text-[15px] font-semibold text-white font-sans">个人设置</h2>
      <p class="text-[11px] text-trust-300 mt-0.5 font-sans">修改头像、手机号和密码</p>
    </div>

    <div class="grid grid-cols-5 gap-4">

      <!-- 左栏：头像 + 基本信息 (3/5) -->
      <div class="col-span-3 bg-surface-raised rounded-2xl border border-white/[0.06] p-6 space-y-5">

        <!-- 头像 -->
        <div class="flex items-center gap-5">
          <div class="relative group cursor-pointer shrink-0" @click="$refs.fileInput.click()">
            <div v-if="avatarPreview || profile.avatar" class="w-20 h-20 rounded-2xl overflow-hidden border-2 border-brand/20">
              <img :src="avatarPreview || avatarBlobUrl" alt="头像" class="w-full h-full object-cover" />
            </div>
            <div v-else class="w-20 h-20 rounded-2xl bg-gradient-to-br from-brand to-brand-light flex items-center justify-center text-white text-[28px] font-bold font-mono border-2 border-brand/20">
              {{ (profile.name || '?')[0] }}
            </div>
            <div class="absolute inset-0 rounded-2xl bg-black/50 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
              <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/><circle cx="12" cy="13" r="4"/></svg>
            </div>
            <input ref="fileInput" type="file" accept="image/*" class="hidden" @change="handleFileSelect" />
          </div>
          <div>
            <p class="text-[13px] font-semibold text-white font-sans">{{ profile.name || '—' }}</p>
            <p class="text-[11px] text-trust-300 font-mono mt-0.5">@{{ profile.username }}</p>
            <p class="text-[10px] text-trust-400 font-sans mt-1.5">点击头像更换 · JPG / PNG · 最大 2MB</p>
            <p v-if="uploading" class="text-[10px] text-brand-light font-sans mt-1">上传中...</p>
          </div>
        </div>

        <div class="h-px bg-white/[0.04]" />

        <!-- 基本信息 -->
        <div class="space-y-3">
          <p class="text-[10px] font-semibold text-trust-300 uppercase tracking-[0.1em] font-sans">基本信息</p>
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="text-[10px] text-trust-300 font-sans block mb-1">用户名</label>
              <input :value="profile.username" disabled class="w-full bg-white/[0.02] border border-white/[0.04] rounded-lg px-3 py-2 text-[12px] text-gray-500 font-mono opacity-60 cursor-not-allowed" />
            </div>
            <div>
              <label class="text-[10px] text-trust-300 font-sans block mb-1">姓名</label>
              <input :value="profile.name" disabled class="w-full bg-white/[0.02] border border-white/[0.04] rounded-lg px-3 py-2 text-[12px] text-gray-500 font-sans opacity-60 cursor-not-allowed" />
            </div>
          </div>
          <div>
            <label class="text-[10px] text-trust-300 font-sans block mb-1">手机号</label>
            <input v-model="profile.phone" placeholder="13800000000"
              class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-2 text-[12px] text-white placeholder-trust-400 font-mono focus:outline-none focus:ring-2 focus:ring-brand/30" />
          </div>
        </div>

        <p v-if="profileMsg" :class="['text-[11px] font-sans', profileMsgOk ? 'text-success-light' : 'text-red-400']">{{ profileMsg }}</p>

        <button @click="saveProfile" :disabled="saving"
          class="w-full py-2 rounded-xl text-[12px] font-semibold bg-brand text-white hover:bg-brand-light transition-colors cursor-pointer font-sans disabled:opacity-50">
          {{ saving ? '保存中...' : '保存信息' }}
        </button>
      </div>

      <!-- 右栏：修改密码 (2/5) -->
      <div class="col-span-2 bg-surface-raised rounded-2xl border border-white/[0.06] p-6 space-y-4">
        <p class="text-[10px] font-semibold text-trust-300 uppercase tracking-[0.1em] font-sans">修改密码</p>
        <div>
          <label class="text-[10px] text-trust-300 font-sans block mb-1">原密码</label>
          <input v-model="oldPassword" type="password" placeholder="输入原密码"
            class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-2 text-[12px] text-white placeholder-trust-400 font-sans focus:outline-none focus:ring-2 focus:ring-brand/30" />
        </div>
        <div>
          <label class="text-[10px] text-trust-300 font-sans block mb-1">新密码</label>
          <input v-model="newPassword" type="password" placeholder="输入新密码"
            class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-2 text-[12px] text-white placeholder-trust-400 font-sans focus:outline-none focus:ring-2 focus:ring-brand/30" />
        </div>
        <div>
          <label class="text-[10px] text-trust-300 font-sans block mb-1">确认新密码</label>
          <input v-model="confirmPassword" type="password" placeholder="再次输入新密码"
            class="w-full bg-white/[0.03] border border-white/[0.06] rounded-lg px-3 py-2 text-[12px] text-white placeholder-trust-400 font-sans focus:outline-none focus:ring-2 focus:ring-brand/30" />
        </div>

        <p v-if="pwdMsg" :class="['text-[11px] font-sans', pwdMsgOk ? 'text-success-light' : 'text-red-400']">{{ pwdMsg }}</p>

        <button @click="savePassword" :disabled="savingPwd"
          class="w-full py-2 rounded-xl text-[12px] font-semibold bg-white/[0.06] border border-white/[0.08] text-white hover:bg-white/[0.1] transition-colors cursor-pointer font-sans disabled:opacity-50">
          {{ savingPwd ? '修改中...' : '修改密码' }}
        </button>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import api from '../api'
import { useAuthStore } from '../stores/auth'
import { useAuthImage } from '../composables/useAuthImage'

const auth = useAuthStore()
const profile = reactive({ username: '', name: '', phone: '', avatar: '' })
const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const saving = ref(false)
const savingPwd = ref(false)
const uploading = ref(false)
const profileMsg = ref('')
const profileMsgOk = ref(false)
const pwdMsg = ref('')
const pwdMsgOk = ref(false)
const fileInput = ref(null)
const avatarPreview = ref('')

const avatarPath = computed(() => avatarPreview.value ? '' : profile.avatar)
const { blobUrl: avatarBlobUrl } = useAuthImage(avatarPath)

onMounted(async () => {
  try {
    const res = await api.get('/auth/me')
    const u = res.data
    profile.username = u.username
    profile.name = u.name
    profile.phone = u.phone || ''
    profile.avatar = u.avatar || ''
  } catch { /* empty */ }
})

async function handleFileSelect(e) {
  const file = e.target.files?.[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = (ev) => { avatarPreview.value = ev.target.result }
  reader.readAsDataURL(file)
  uploading.value = true
  profileMsg.value = ''
  try {
    const form = new FormData()
    form.append('file', file)
    const res = await api.post('/auth/avatar', form)
    profile.avatar = res.data.url
    auth.setUser({ ...auth.user, avatar: res.data.url })
    profileMsg.value = '头像已更新'
    profileMsgOk.value = true
  } catch (err) {
    profileMsg.value = err?.message || '头像上传失败'
    profileMsgOk.value = false
    avatarPreview.value = ''
  } finally {
    uploading.value = false
    if (fileInput.value) fileInput.value.value = ''
  }
}

async function saveProfile() {
  profileMsg.value = ''
  saving.value = true
  try {
    const res = await api.put('/auth/profile', { phone: profile.phone })
    profileMsg.value = '保存成功'
    profileMsgOk.value = true
    if (res.data) auth.setUser({ ...auth.user, phone: res.data.phone })
  } catch (e) {
    profileMsg.value = e?.message || '保存失败'
    profileMsgOk.value = false
  } finally {
    saving.value = false
  }
}

async function savePassword() {
  pwdMsg.value = ''
  if (!oldPassword.value) { pwdMsg.value = '请输入原密码'; pwdMsgOk.value = false; return }
  if (!newPassword.value) { pwdMsg.value = '请输入新密码'; pwdMsgOk.value = false; return }
  if (newPassword.value !== confirmPassword.value) { pwdMsg.value = '两次密码不一致'; pwdMsgOk.value = false; return }
  savingPwd.value = true
  try {
    await api.put('/auth/profile', { oldPassword: oldPassword.value, newPassword: newPassword.value })
    pwdMsg.value = '密码修改成功'
    pwdMsgOk.value = true
    auth.setUser({ ...auth.user, passwordChanged: true })
    oldPassword.value = ''
    newPassword.value = ''
    confirmPassword.value = ''
  } catch (e) {
    pwdMsg.value = e?.message || '修改失败'
    pwdMsgOk.value = false
  } finally {
    savingPwd.value = false
  }
}
</script>
