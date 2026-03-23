<template>
  <input
    ref="inputRef"
    type="text"
    inputmode="decimal"
    :value="display"
    :placeholder="placeholder"
    @input="onInput"
    @blur="onBlur"
  />
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  modelValue: { type: [Number, String], default: '' },
  placeholder: { type: String, default: '' },
})
const emit = defineEmits(['update:modelValue'])

const inputRef = ref(null)

// 千分位格式化
function formatNumber(val) {
  if (val === '' || val === null || val === undefined) return ''
  const str = String(val).replace(/,/g, '')
  if (str === '' || str === '-') return str
  const parts = str.split('.')
  parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',')
  return parts.join('.')
}

const display = computed(() => formatNumber(props.modelValue))

function onInput(e) {
  const el = e.target
  const cursorPos = el.selectionStart
  const oldVal = el.value
  const oldCommasBefore = (oldVal.substring(0, cursorPos).match(/,/g) || []).length

  // 去逗号，只保留数字和小数点
  let raw = oldVal.replace(/,/g, '').replace(/[^\d.]/g, '')
  // 防止多个小数点
  const dotIdx = raw.indexOf('.')
  if (dotIdx !== -1) {
    raw = raw.substring(0, dotIdx + 1) + raw.substring(dotIdx + 1).replace(/\./g, '')
  }

  emit('update:modelValue', raw)

  // 格式化并恢复光标
  const formatted = formatNumber(raw)
  el.value = formatted
  const digitPos = cursorPos - oldCommasBefore
  // 找到 formatted 中第 digitPos 个数字/点的位置
  let count = 0
  let newPos = 0
  for (let i = 0; i < formatted.length; i++) {
    if (formatted[i] !== ',') count++
    if (count >= digitPos) { newPos = i + 1; break }
  }
  if (digitPos <= 0) newPos = 0
  el.setSelectionRange(newPos, newPos)
}

function onBlur() {
  const raw = String(props.modelValue || '').replace(/,/g, '')
  if (raw && !isNaN(Number(raw))) {
    emit('update:modelValue', raw)
  }
}

defineExpose({ inputRef })
</script>
