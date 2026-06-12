import { ref, readonly } from 'vue'

const visible = ref(false)
const options = ref({
  title: 'Xác nhận',
  message: 'Bạn có chắc muốn tiếp tục?',
  confirmText: 'Xác nhận',
  cancelText: 'Hủy',
  danger: false,
})

let resolver = null

/**
 * Hiện hộp xác nhận Soleil. Trả về true nếu người dùng xác nhận.
 * @param {{ title?: string, message?: string, confirmText?: string, cancelText?: string, danger?: boolean }} opts
 */
export function confirm(opts = {}) {
  return new Promise((resolve) => {
    options.value = {
      title: opts.title ?? 'Xác nhận',
      message: opts.message ?? 'Bạn có chắc muốn tiếp tục?',
      confirmText: opts.confirmText ?? 'Xác nhận',
      cancelText: opts.cancelText ?? 'Hủy',
      danger: opts.danger ?? false,
    }
    visible.value = true
    resolver = resolve
  })
}

export function handleConfirmOk() {
  visible.value = false
  resolver?.(true)
  resolver = null
}

export function handleConfirmCancel() {
  visible.value = false
  resolver?.(false)
  resolver = null
}

export function useConfirmState() {
  return {
    visible: readonly(visible),
    options: readonly(options),
  }
}
