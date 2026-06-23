import { ref } from 'vue'

const toasts = ref([])
let idSeq = 0
let lastKey = ''
let lastAt = 0

export function useToastState() {
  return { toasts }
}

/**
 * Toast góc phải — chặn bắn trùng cùng nội dung trong ~2s.
 * @param {'info'|'warn'} type
 */
export function toast(message, type = 'warn') {
  const text = String(message || '').trim()
  if (!text) return

  const now = Date.now()
  const key = `${type}:${text}`
  if (key === lastKey && now - lastAt < 2000) return

  lastKey = key
  lastAt = now

  const id = ++idSeq
  toasts.value = [...toasts.value, { id, message: text, type }]
  setTimeout(() => dismissToast(id), 2200)
}

export function dismissToast(id) {
  toasts.value = toasts.value.filter((t) => t.id !== id)
}
