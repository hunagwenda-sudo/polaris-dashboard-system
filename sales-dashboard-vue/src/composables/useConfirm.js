import { ref } from 'vue'

const visible = ref(false)
const title = ref('')
const message = ref('')
const confirmText = ref('确认')
const confirmType = ref('danger')
let resolvePromise = null

export function useConfirm() {
  function confirm(msg, opts = {}) {
    title.value = opts.title || '确认操作'
    message.value = msg
    confirmText.value = opts.confirmText || '确认'
    confirmType.value = opts.type || 'danger'
    visible.value = true
    return new Promise((resolve) => {
      resolvePromise = resolve
    })
  }

  function handleConfirm() {
    visible.value = false
    resolvePromise?.(true)
  }

  function handleCancel() {
    visible.value = false
    resolvePromise?.(false)
  }

  return { visible, title, message, confirmText, confirmType, confirm, handleConfirm, handleCancel }
}
