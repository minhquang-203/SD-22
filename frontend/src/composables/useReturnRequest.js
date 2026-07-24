import { onUnmounted, ref, watch } from 'vue'
import { taoYeuCauTraHang } from '@/api/traHangApi'
import { getCustomerId } from '@/composables/useAuth'
import { toast } from '@/composables/useToast'

export const LY_DO_OPTIONS = [
  'Sản phẩm lỗi / hỏng',
  'Sai sản phẩm / sai biến thể',
  'Không đúng mô tả',
  'Đổi ý / không còn nhu cầu',
  'Khác',
]

const MIN_IMAGES = 2
const MAX_IMAGES = 6

/**
 * Shared state + helpers for return-request modals (COD / wallet).
 * @param {object} options
 * @param {() => boolean} options.visible
 * @param {() => object|null} options.order
 * @param {(event: string, ...args: any[]) => void} options.emit
 * @param {boolean} [options.requireBank=false]
 */
export function useReturnRequest({ visible, order, emit, requireBank = false }) {
  const lyDo = ref('')
  const moTa = ref('')
  const tenNganHang = ref('')
  const soTaiKhoan = ref('')
  const chuTaiKhoan = ref('')
  const imageFiles = ref([])
  const imagePreviews = ref([])
  const submitting = ref(false)
  const error = ref('')
  const fileInputRef = ref(null)

  function revokePreviews() {
    imagePreviews.value.forEach((url) => {
      if (url) URL.revokeObjectURL(url)
    })
    imagePreviews.value = []
  }

  function resetForm() {
    lyDo.value = ''
    moTa.value = ''
    tenNganHang.value = ''
    soTaiKhoan.value = ''
    chuTaiKhoan.value = ''
    imageFiles.value = []
    revokePreviews()
    error.value = ''
    if (fileInputRef.value) {
      fileInputRef.value.value = ''
    }
  }

  function lockBodyScroll(lock) {
    document.body.style.overflow = lock ? 'hidden' : ''
  }

  function onKeydown(e) {
    if (e.key === 'Escape' && visible()) {
      handleClose()
    }
  }

  watch(
    visible,
    (v) => {
      if (v) {
        resetForm()
        lockBodyScroll(true)
        window.addEventListener('keydown', onKeydown)
      } else {
        lockBodyScroll(false)
        window.removeEventListener('keydown', onKeydown)
      }
    },
  )

  onUnmounted(() => {
    lockBodyScroll(false)
    window.removeEventListener('keydown', onKeydown)
    revokePreviews()
  })

  function handleClose() {
    resetForm()
    emit('close')
  }

  function addImages(fileList) {
    const incoming = Array.from(fileList || []).filter((f) => f && f.type.startsWith('image/'))
    if (!incoming.length) return

    const remaining = MAX_IMAGES - imageFiles.value.length
    if (remaining <= 0) {
      error.value = `Chỉ được tải tối đa ${MAX_IMAGES} ảnh.`
      return
    }

    const toAdd = incoming.slice(0, remaining)
    const nextFiles = [...imageFiles.value, ...toAdd]
    const nextPreviews = [
      ...imagePreviews.value,
      ...toAdd.map((f) => URL.createObjectURL(f)),
    ]
    imageFiles.value = nextFiles
    imagePreviews.value = nextPreviews
    error.value = ''
  }

  function onFileChange(event) {
    addImages(event.target.files)
    if (event.target) event.target.value = ''
  }

  function removeImage(index) {
    const url = imagePreviews.value[index]
    if (url) URL.revokeObjectURL(url)
    imageFiles.value = imageFiles.value.filter((_, i) => i !== index)
    imagePreviews.value = imagePreviews.value.filter((_, i) => i !== index)
  }

  function validate() {
    const currentOrder = order()
    if (!currentOrder?.id) {
      return 'Không xác định được đơn hàng.'
    }
    if (!lyDo.value.trim()) {
      return 'Vui lòng chọn hoặc nhập lý do trả hàng.'
    }
    if (imageFiles.value.length < MIN_IMAGES) {
      return `Vui lòng tải lên tối thiểu ${MIN_IMAGES} hình ảnh sản phẩm.`
    }
    if (requireBank) {
      if (!tenNganHang.value.trim()) {
        return 'Vui lòng nhập tên ngân hàng.'
      }
      if (!soTaiKhoan.value.trim()) {
        return 'Vui lòng nhập số tài khoản nhận hoàn tiền.'
      }
      if (!chuTaiKhoan.value.trim()) {
        return 'Vui lòng nhập tên chủ tài khoản.'
      }
    }
    if (!getCustomerId()) {
      return 'Vui lòng đăng nhập để yêu cầu trả hàng.'
    }
    return ''
  }

  async function submitReturn() {
    const validationError = validate()
    if (validationError) {
      error.value = validationError
      return
    }

    const currentOrder = order()
    const idKhachHang = getCustomerId()
    submitting.value = true
    error.value = ''

    try {
      const payload = {
        lyDo: lyDo.value.trim(),
        moTa: moTa.value.trim() || null,
        diaChiTra: currentOrder.diaChiGiao || null,
        tenNganHang: requireBank ? tenNganHang.value.trim() : null,
        soTaiKhoan: requireBank ? soTaiKhoan.value.trim() : null,
        chuTaiKhoan: requireBank ? chuTaiKhoan.value.trim() : null,
      }

      const res = await taoYeuCauTraHang(
        currentOrder.id,
        idKhachHang,
        payload,
        imageFiles.value,
      )
      toast('Đã gửi yêu cầu trả hàng. Vui lòng chờ cửa hàng duyệt.', 'info')
      emit('submitted', res.data)
      handleClose()
    } catch (e) {
      error.value = typeof e === 'string'
        ? e
        : e.response?.data?.message || e.message || 'Không gửi được yêu cầu trả hàng.'
    } finally {
      submitting.value = false
    }
  }

  return {
    lyDo,
    moTa,
    tenNganHang,
    soTaiKhoan,
    chuTaiKhoan,
    imageFiles,
    imagePreviews,
    submitting,
    error,
    fileInputRef,
    MIN_IMAGES,
    MAX_IMAGES,
    handleClose,
    onFileChange,
    removeImage,
    submitReturn,
  }
}
