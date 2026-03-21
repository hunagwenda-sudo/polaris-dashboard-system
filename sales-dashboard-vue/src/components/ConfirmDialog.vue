<template>
  <Teleport to="body">
    <Transition name="confirm-fade">
      <div v-if="visible" class="fixed inset-0 z-[100] flex items-center justify-center" @click.self="handleCancel">
        <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" />
        <div class="relative bg-trust-800 border border-white/[0.08] rounded-2xl p-6 w-[360px] shadow-2xl shadow-black/40">
          <div class="flex items-center gap-3 mb-3">
            <div :class="['w-9 h-9 rounded-xl border flex items-center justify-center shrink-0',
              confirmType === 'danger' ? 'bg-danger/[0.1] border-danger/[0.15]' : 'bg-brand/[0.1] border-brand/[0.15]']">
              <svg v-if="confirmType === 'danger'" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="w-4 h-4 text-danger-light">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>
              </svg>
              <svg v-else xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="w-4 h-4 text-brand-light">
                <circle cx="12" cy="12" r="10"/><path d="m9 12 2 2 4-4"/>
              </svg>
            </div>
            <h3 class="text-[14px] font-semibold text-white font-sans">{{ title }}</h3>
          </div>
          <p class="text-[12px] text-trust-300 leading-relaxed mb-5 pl-12 font-sans">{{ message }}</p>
          <div class="flex gap-3 justify-end">
            <button @click="handleCancel" class="px-4 py-2 rounded-xl text-[12px] font-medium text-trust-300 hover:text-white hover:bg-white/[0.05] transition-colors cursor-pointer font-sans">取消</button>
            <button @click="handleConfirm" :class="['px-4 py-2 rounded-xl text-[12px] font-semibold transition-colors cursor-pointer shadow-lg font-sans',
              confirmType === 'danger' ? 'bg-danger text-white hover:bg-danger-light shadow-danger/20' : 'bg-brand text-white hover:bg-brand-light shadow-brand/20']">{{ confirmText }}</button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { useConfirm } from '../composables/useConfirm'
const { visible, title, message, confirmText, confirmType, handleConfirm, handleCancel } = useConfirm()
</script>

<style scoped>
.confirm-fade-enter-active, .confirm-fade-leave-active { transition: opacity 0.15s ease; }
.confirm-fade-enter-from, .confirm-fade-leave-to { opacity: 0; }
</style>
