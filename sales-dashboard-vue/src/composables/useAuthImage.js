import { ref, onUnmounted, watch } from 'vue'

const API_BASE = import.meta.env.VITE_API_BASE || ''

/**
 * Load an authenticated image URL (requires JWT token).
 * Returns a reactive blob URL that can be used in <img :src="blobUrl">.
 * Automatically revokes the blob URL on unmount.
 *
 * @param {import('vue').Ref<string> | string} path - server path like "/uploads/avatars/xxx.jpg"
 */
export function useAuthImage(path) {
  const blobUrl = ref('')
  let currentBlob = ''

  function revoke() {
    if (currentBlob) {
      URL.revokeObjectURL(currentBlob)
      currentBlob = ''
    }
  }

  async function load(p) {
    revoke()
    if (!p) { blobUrl.value = ''; return }
    try {
      const token = localStorage.getItem('token')
      const res = await fetch(API_BASE + p, {
        headers: token ? { Authorization: `Bearer ${token}` } : {},
      })
      if (!res.ok) { blobUrl.value = ''; return }
      const blob = await res.blob()
      currentBlob = URL.createObjectURL(blob)
      blobUrl.value = currentBlob
    } catch {
      blobUrl.value = ''
    }
  }

  // Support both ref and plain string
  if (typeof path === 'object' && path !== null && 'value' in path) {
    watch(path, (p) => load(p), { immediate: true })
  } else {
    load(path)
  }

  onUnmounted(revoke)

  return { blobUrl }
}
