<template>
  <!-- Admin 专属徽章 -->
  <span v-if="role === 'admin'" class="inline-flex items-center gap-0.5 font-mono font-bold leading-none bg-gradient-to-r from-red-500/20 to-amber-500/20 text-amber-300 border border-amber-400/30 shadow-lg shadow-amber-500/20 ring-1 ring-amber-400/15" :class="sizeClass">
    <svg width="10" height="10" viewBox="0 0 24 24" fill="currentColor" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
      <path d="M12 2L9 9H2l6 4.5L5.5 21 12 16.5 18.5 21 16 13.5 22 9h-7L12 2z"/>
    </svg>
    ADMIN
  </span>
  <!-- 合伙人专属徽章 -->
  <span v-else-if="role === 'partner'" class="inline-flex items-center gap-0.5 font-mono font-bold leading-none bg-gradient-to-r from-indigo-500/20 to-cyan-500/20 text-cyan-300 border border-cyan-400/30 shadow-lg shadow-cyan-500/15 ring-1 ring-cyan-400/15" :class="sizeClass">
    <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
      <path d="M16.5 9.4l-9-5.19M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/>
    </svg>
    合伙人
  </span>
  <!-- 普通职级徽章 -->
  <span v-else-if="level" :class="badgeClass" class="inline-flex items-center gap-0.5 font-mono font-bold leading-none">
    <span v-html="icon" />
    {{ level }}
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  level: { type: String, default: '' },
  role: { type: String, default: '' },
  size: { type: String, default: 'sm' }
})

const num = computed(() => {
  const m = props.level?.match(/\d+/)
  return m ? parseInt(m[0]) : 1
})

const sizeClass = computed(() => {
  if (props.size === 'lg') return 'text-[13px] px-2.5 py-1 rounded-lg'
  if (props.size === 'md') return 'text-[11px] px-2 py-1 rounded-lg'
  return 'text-[10px] px-1.5 py-0.5 rounded-md'
})

const styles = [
  '',
  'bg-trust-600/30 text-trust-300 border border-trust-500/20',
  'bg-slate-400/20 text-slate-300 border border-slate-400/20',
  'bg-blue-500/15 text-blue-400 border border-blue-500/25',
  'bg-cyan-500/15 text-cyan-400 border border-cyan-500/25',
  'bg-purple-500/15 text-purple-400 border border-purple-500/25 shadow-sm shadow-purple-500/10',
  'bg-amber-500/15 text-amber-400 border border-amber-500/30 shadow-md shadow-amber-500/15',
  'bg-gradient-to-r from-amber-500/20 to-red-500/20 text-amber-300 border border-amber-400/30 shadow-lg shadow-amber-500/20',
  'bg-gradient-to-r from-amber-500/20 to-rose-500/20 text-amber-300 border border-amber-400/40 shadow-lg shadow-amber-400/25 ring-1 ring-amber-500/20',
  'bg-gradient-to-r from-rose-500/20 to-amber-500/20 text-rose-300 border border-rose-400/40 shadow-lg shadow-rose-400/25 ring-1 ring-rose-400/20',
  'bg-gradient-to-r from-rose-500/20 to-purple-500/20 text-rose-300 border border-rose-400/40 shadow-xl shadow-rose-500/25 ring-1 ring-rose-400/25',
  'bg-gradient-to-r from-purple-500/20 to-amber-400/20 text-purple-300 border border-purple-400/40 shadow-xl shadow-purple-500/25 ring-2 ring-purple-400/20',
  'bg-gradient-to-r from-amber-400/20 via-rose-500/20 to-purple-500/20 text-amber-200 border border-amber-300/50 shadow-xl shadow-amber-400/30 ring-2 ring-amber-300/25',
]

const icons = {
  1: '<svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22V8"/><path d="M5 12H2a10 10 0 0 0 20 0h-3"/><path d="M8 8a4 4 0 0 1 8 0"/></svg>',
  2: '<svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>',
  3: '<svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>',
  4: '<svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M6 3h12l4 6-10 13L2 9z"/><path d="M2 9h20"/></svg>',
  5: '<svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M2 4l3 12h14l3-12-6 7-4-7-4 7-6-7z"/><path d="M5 16h14v3H5z"/></svg>',
  6: '<svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2c1 3 4 5.5 4 8.5a4 4 0 1 1-8 0C8 7.5 11 5 12 2z"/><path d="M5 19l1-3h12l1 3"/></svg>',
  7: '<svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 6c2 2 4 6 8 8"/><path d="M20 6c-2 2-4 6-8 8"/><path d="M2 4c1 3 3 5 6 6"/><path d="M22 4c-1 3-3 5-6 6"/><circle cx="12" cy="6" r="2"/></svg>',
  8: '<svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2l2 5h5l-4 3.5 1.5 5L12 13l-4.5 2.5L9 10.5 5 7h5l2-5z"/><path d="M6 20h12"/></svg>',
  9: '<svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22V6"/><path d="M12 6l-5-4v5l5-1"/><path d="M12 6l5-4v5l-5-1"/><path d="M12 2v4"/></svg>',
  10: '<svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8S1 12 1 12z"/><circle cx="12" cy="12" r="3"/></svg>',
  11: '<svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 12c-2-2.5-4-4-6.5-4a4.5 4.5 0 0 0 0 9c2.5 0 4.5-1.5 6.5-4"/><path d="M12 12c2 2.5 4 4 6.5 4a4.5 4.5 0 0 0 0-9c-2.5 0-4.5 1.5-6.5 4"/></svg>',
  12: '<svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="10" cy="12" r="4"/><path d="M10 5v-2M10 21v-2M3 12H1M19 12h-2"/><path d="M16 8a5 5 0 0 1 0 8"/></svg>',
}

const tierStyle = computed(() => {
  const n = Math.min(num.value, styles.length - 1)
  return styles[n] || styles[styles.length - 1]
})
const icon = computed(() => icons[num.value] || icons[Math.min(num.value, 12)])
const badgeClass = computed(() => `${sizeClass.value} ${tierStyle.value}`)
</script>
