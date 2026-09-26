<script setup>
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { guiYeuCauMuaSoLuongLon } from '@/api/yeuCauMuaSoLuongLonApi'
import { fetchKhachToi } from '@/api/khachHangApi'
import { storeInfo } from '@/config/storeInfo'
import { GIOI_HAN_MUA_LE } from '@/constants/cartLimits'
import { useAuth } from '@/composables/useAuth'
import { closeBulkOrderModal, useBulkOrderModal } from '@/composables/useBulkOrderModal'
import { confirm } from '@/composables/useConfirm'
import { getPhoneValidationError, normalizePhoneDigits } from '@/utils/phone'
import { productImageUrl } from '@/utils/productImage'
import { variantLabel } from '@/composables/useCart'

const MIN_QTY = GIOI_HAN_MUA_LE + 1

const { visible, product } = useBulkOrderModal()
const { isLoggedIn, hoTen } = useAuth()

const submitting = ref(false)
const success = ref(false)
const formError = ref('')
const qtyHint = ref('')
const qtyError = ref('')
const shakeField = ref('')
const desiredQty = ref(MIN_QTY)
const qtyDraft = ref(String(MIN_QTY))

const form = reactive({
  tenCongTy: '',
  hoTen: '',
  soDienThoai: '',
  email: '',
  ghiChu: '',
  nhanKhuyenMai: false,
})
const errors = reactive({
  hoTen: '',
  soDienThoai: '',
  email: '',
  tenCongTy: '',
  ghiChu: '',
})
const touched = reactive({
  hoTen: false,
  soDienThoai: false,
  email: false,
  tenCongTy: false,
  ghiChu: false,
})

const modalRef = ref(null)
const qtyInputRef = ref(null)
const tenCongTyRef = ref(null)
const hoTenRef = ref(null)
const sdtRef = ref(null)
const emailRef = ref(null)
const ghiChuRef = ref(null)

let holdTimer = null
let holdInterval = null
let successTimer = null
let initialSnapshot = ''

const bulk = computed(() => storeInfo.bulkOrder || {})

const stock = computed(() => {
  const raw = product.value?.soLuongTon
  if (raw == null || raw === '') return null
  const n = Number(raw)
  return Number.isFinite(n) ? Math.max(0, n) : null
})

const maxQty = computed(() => {
  const ton = getMaxTon()
  return ton != null ? ton : Number.MAX_SAFE_INTEGER
})
const hasStockInfo = computed(() => getMaxTon() != null)

const plusDisabled = computed(
  () =>
    submitting.value ||
    success.value ||
    (hasStockInfo.value && Number(desiredQty.value) >= maxQty.value),
)
const minusDisabled = computed(
  () => submitting.value || success.value || Number(desiredQty.value) <= MIN_QTY,
)

const variantText = computed(() => {
  if (!product.value) return ''
  return variantLabel(product.value)
})

const ghiChuCount = computed(() => form.ghiChu.length)

const maskedPhone = computed(() => {
  const d = form.soDienThoai || ''
  if (d.length < 10) return "số điện thoại bạn đã để lại"
  return `${d.slice(0, 4)} ${d.slice(4, 7)} ${d.slice(7)}`
})

function clampQty(n) {
  const maxTon = getMaxTon()
  let v = Number(n)
  if (!Number.isFinite(v)) v = MIN_QTY
  v = Math.max(MIN_QTY, Math.floor(v))
  if (maxTon != null && maxTon >= MIN_QTY) v = Math.min(v, maxTon)
  return v
}

function syncQtyDisplay(n) {
  const v = Number(n)
  desiredQty.value = Number.isFinite(v) ? v : MIN_QTY
  qtyDraft.value = String(desiredQty.value)
}

function resetForm() {
  const want = Number(product.value?.soLuongMongMuon) || MIN_QTY
  syncQtyDisplay(clampQty(want))
  qtyHint.value = ''
  qtyError.value = ''
  formError.value = ''
  success.value = false
  shakeField.value = ''
  form.tenCongTy = ''
  form.hoTen = ''
  form.soDienThoai = ''
  form.email = ''
  form.ghiChu = ''
  form.nhanKhuyenMai = false
  Object.keys(errors).forEach((k) => { errors[k] = '' })
  Object.keys(touched).forEach((k) => { touched[k] = false })
}

function snapshotForm() {
  return JSON.stringify({
    qty: desiredQty.value,
    tenCongTy: form.tenCongTy,
    hoTen: form.hoTen,
    soDienThoai: form.soDienThoai,
    email: form.email,
    ghiChu: form.ghiChu,
    nhanKhuyenMai: form.nhanKhuyenMai,
  })
}

function isDirty() {
  if (success.value) return false
  return snapshotForm() !== initialSnapshot
}

async function prefillIfLoggedIn() {
  if (!isLoggedIn.value) return
  form.hoTen = hoTen.value || ''
  try {
    const res = await fetchKhachToi()
    const me = res?.data || {}
    if (me.hoTen) form.hoTen = me.hoTen
    if (me.soDienThoai) form.soDienThoai = normalizePhoneDigits(me.soDienThoai)
    if (me.email) form.email = String(me.email).trim().toLowerCase()
  } catch {
    /* keep auth name */
  }
}

async function focusFirstEmpty() {
  await nextTick()
  const targets = [
    { el: hoTenRef.value, empty: !form.hoTen.trim() },
    { el: sdtRef.value, empty: !form.soDienThoai },
    { el: emailRef.value, empty: !form.email.trim() },
    { el: qtyInputRef.value, empty: false },
    { el: ghiChuRef.value, empty: true },
  ]
  const firstEmpty = targets.find((t) => t.empty && t.el)
  const fallback = isLoggedIn.value
    ? qtyInputRef.value || ghiChuRef.value
    : targets.find((t) => t.el)?.el
  ;(firstEmpty?.el || fallback)?.focus?.()
}

watch(visible, async (v) => {
  clearSuccessTimer()
  stopHold()
  if (!v) return
  if (hasStockInfo.value && stock.value <= GIOI_HAN_MUA_LE) {
    closeBulkOrderModal()
    return
  }
  resetForm()
  await prefillIfLoggedIn()
  initialSnapshot = snapshotForm()
  await focusFirstEmpty()
})

function clearSuccessTimer() {
  if (successTimer) {
    clearTimeout(successTimer)
    successTimer = null
  }
}

async function requestClose() {
  if (submitting.value) return
  if (success.value) {
    clearSuccessTimer()
    closeBulkOrderModal()
    return
  }
  if (isDirty()) {
    const ok = await confirm({
      title: "Bỏ thông tin đã nhập?",
      message: "Bạn có muốn bỏ thông tin đã nhập?",
      confirmText: "Bỏ",
      cancelText: "Tiếp tục",
      danger: true,
    })
    if (!ok) return
  }
  closeBulkOrderModal()
}

function onKeydown(e) {
  if (!visible.value) return
  if (e.key === 'Escape') {
    e.preventDefault()
    void requestClose()
  }
}

onMounted(() => window.addEventListener('keydown', onKeydown))
onUnmounted(() => {
  window.removeEventListener('keydown', onKeydown)
  stopHold()
  clearSuccessTimer()
})

function msgToiThieu() {
  return `Tối thiểu ${MIN_QTY} sản phẩm cho đơn số lượng lớn`
}

function msgToiDa(n) {
  return `Tối đa ${n} sản phẩm theo tồn kho hiện tại`
}

/** Trần tồn kho hiện có; null nếu chưa biết */
function getMaxTon() {
  const n = Number(product.value?.soLuongTon)
  if (!Number.isFinite(n) || n < 0) return null
  return Math.floor(n)
}

/**
 * Điểm vào DUY NHẤT để đổi số lượng — luôn kẹp [16, tồn].
 * @param {number|string} giaTri
 * @param {{ fromTyping?: boolean }} [opts]
 */
function setSoLuong(giaTri, { fromTyping = false } = {}) {
  const maxTon = getMaxTon()
  let n =
    typeof giaTri === 'number'
      ? giaTri
      : parseInt(String(giaTri ?? '').replace(/\D/g, ''), 10)

  qtyHint.value = ''
  qtyError.value = ''

  if (!Number.isFinite(n)) {
    if (fromTyping) {
      desiredQty.value = 0
      qtyDraft.value = ''
      return
    }
    syncQtyDisplay(MIN_QTY)
    qtyError.value = msgToiThieu()
    return
  }

  n = Math.floor(n)

  // Đang gõ: cho phép tạm < 16, vẫn kẹp ngay nếu vượt tồn
  if (fromTyping) {
    if (maxTon != null && maxTon >= MIN_QTY && n > maxTon) {
      syncQtyDisplay(maxTon)
      qtyHint.value = msgToiDa(maxTon)
      return
    }
    if (n < MIN_QTY) {
      desiredQty.value = n
      qtyDraft.value = String(n)
      return
    }
    syncQtyDisplay(n)
    if (maxTon != null && n >= maxTon) qtyHint.value = msgToiDa(maxTon)
    return
  }

  // Nút ± / blur / submit: luôn kẹp cứng
  let hitMin = false
  let hitMax = false
  let clamped = n

  if (clamped < MIN_QTY) {
    clamped = MIN_QTY
    hitMin = true
  }
  if (maxTon != null && clamped > maxTon) {
    clamped = maxTon
    hitMax = true
  }

  syncQtyDisplay(clamped)

  if (hitMax || (maxTon != null && clamped >= maxTon)) {
    qtyHint.value = msgToiDa(maxTon)
  }
  if (hitMin || clamped <= MIN_QTY) {
    qtyError.value = msgToiThieu()
    // Ưu tiên nhắc tối đa nếu vừa chạm trần
    if (hitMax) qtyError.value = ''
  }
}

function bumpQty(delta) {
  if (submitting.value || success.value) return
  const base = Number(desiredQty.value)
  const start = Number.isFinite(base) ? base : MIN_QTY
  setSoLuong(start + Number(delta || 0))
}

function startHold(delta, e) {
  e?.preventDefault?.()
  if (submitting.value || success.value) return
  // Luôn gọi setSoLuong (kể cả khi đã ở biên) để hiện dòng nhắc
  bumpQty(delta)
  stopHold()
  holdTimer = setTimeout(() => {
    holdInterval = setInterval(() => {
      const cur = Number(desiredQty.value)
      const maxTon = getMaxTon()
      if (delta < 0 && cur <= MIN_QTY) {
        setSoLuong(MIN_QTY - 1)
        stopHold()
        return
      }
      if (delta > 0 && maxTon != null && cur >= maxTon) {
        setSoLuong(maxTon + 1)
        stopHold()
        return
      }
      bumpQty(delta)
    }, 70)
  }, 380)
}

function stopHold() {
  if (holdTimer) clearTimeout(holdTimer)
  if (holdInterval) clearInterval(holdInterval)
  holdTimer = null
  holdInterval = null
}

function onQtyInput(e) {
  const raw = String(e.target.value || '').replace(/\D/g, '')
  if (raw === '') {
    setSoLuong(NaN, { fromTyping: true })
    return
  }
  setSoLuong(parseInt(raw, 10), { fromTyping: true })
}

function onQtyBlur() {
  const n = parseInt(qtyDraft.value, 10)
  setSoLuong(Number.isFinite(n) ? n : NaN)
}

function onQtyKeydown(e) {
  const allow = ['Backspace', 'Delete', 'Tab', 'ArrowLeft', 'ArrowRight', 'Home', 'End', 'Enter']
  if (allow.includes(e.key)) return
  if (e.ctrlKey || e.metaKey) return
  if (!/^\d$/.test(e.key)) e.preventDefault()
}

function onQtyPaste(e) {
  e.preventDefault()
  const text = (e.clipboardData || window.clipboardData)?.getData('text') || ''
  const digits = text.replace(/\D/g, '')
  if (!digits) {
    setSoLuong(NaN, { fromTyping: true })
    return
  }
  setSoLuong(parseInt(digits, 10), { fromTyping: true })
}

function normalizeHoTen(value) {
  return String(value || '').replace(/\s+/g, ' ').trim()
}

function validateHoTenValue(value) {
  const t = normalizeHoTen(value)
  if (!t) return "Vui lòng nhập họ tên"
  if (t.length < 2 || t.length > 60) return "Họ tên từ 2–60 ký tự"
  if (!/\p{L}/u.test(t)) return "Họ tên không được toàn số hoặc ký tự đặc biệt"
  return ''
}

function validateEmailValue(value) {
  const email = String(value || '').trim().toLowerCase()
  if (!email) return "Vui lòng nhập email"
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) return "Email không hợp lệ"
  return ''
}

function validateField(field) {
  if (field === 'hoTen') {
    form.hoTen = normalizeHoTen(form.hoTen)
    errors.hoTen = validateHoTenValue(form.hoTen)
  }
  if (field === 'soDienThoai') {
    form.soDienThoai = normalizePhoneDigits(form.soDienThoai)
    errors.soDienThoai = getPhoneValidationError(form.soDienThoai) || ''
  }
  if (field === 'email') {
    form.email = String(form.email || '').trim().toLowerCase()
    errors.email = validateEmailValue(form.email)
  }
  if (field === 'tenCongTy') {
    form.tenCongTy = String(form.tenCongTy || '').trim()
    errors.tenCongTy = form.tenCongTy.length > 100 ? "Tên công ty tối đa 100 ký tự" : ''
  }
  if (field === 'ghiChu') {
    errors.ghiChu = form.ghiChu.length > 500 ? "Ghi chú tối đa 500 ký tự" : ''
  }
}

function onBlurField(field) {
  touched[field] = true
  validateField(field)
}

function onInputField(field) {
  if (!touched[field]) return
  validateField(field)
}

function validateQtyForSubmit() {
  const raw = parseInt(String(qtyDraft.value).replace(/\D/g, ''), 10)
  const maxTon = getMaxTon()
  const wasOver = Number.isFinite(raw) && maxTon != null && raw > maxTon
  const wasUnder = !Number.isFinite(raw) || raw < MIN_QTY

  setSoLuong(Number.isFinite(raw) ? raw : NaN)

  // Đã kẹp + báo — không gửi lần bấm này nếu vừa vượt/thiếu
  if (wasOver || wasUnder) return false
  if (desiredQty.value < MIN_QTY) return false
  if (maxTon != null && desiredQty.value > maxTon) return false
  return true
}

function validateAll() {
  ;['hoTen', 'soDienThoai', 'email', 'tenCongTy', 'ghiChu'].forEach((f) => {
    touched[f] = true
    validateField(f)
  })
  return validateQtyForSubmit() && !errors.hoTen && !errors.soDienThoai && !errors.email && !errors.tenCongTy && !errors.ghiChu
}

const fieldOrder = ['hoTen', 'soDienThoai', 'email', 'tenCongTy', 'ghiChu', 'qty']

function fieldEl(name) {
  if (name === 'hoTen') return hoTenRef.value
  if (name === 'soDienThoai') return sdtRef.value
  if (name === 'email') return emailRef.value
  if (name === 'tenCongTy') return tenCongTyRef.value
  if (name === 'ghiChu') return ghiChuRef.value
  if (name === 'qty') return qtyInputRef.value
  return null
}

function fieldHasError(name) {
  if (name === 'qty') return !!qtyError.value
  return !!errors[name]
}

async function focusFirstError() {
  const name = fieldOrder.find((f) => fieldHasError(f))
  if (!name) return
  shakeField.value = name
  const el = fieldEl(name)
  el?.scrollIntoView?.({ behavior: 'smooth', block: 'center' })
  el?.focus?.()
  setTimeout(() => { if (shakeField.value === name) shakeField.value = '' }, 450)
}

function fieldClass(name) {
  const err = fieldHasError(name) && (touched[name] || name === 'qty')
  return { 'bulk-field--error': !!err, 'bulk-field--shake': shakeField.value === name }
}

const canSubmit = computed(
  () =>
    !submitting.value &&
    !success.value &&
    !!product.value?.idChiTietSanPham &&
    (!hasStockInfo.value || maxQty.value >= MIN_QTY),
)

async function onSubmit() {
  if (submitting.value || success.value) return
  formError.value = ''
  if (!validateAll()) { await focusFirstError(); return }
  if (!canSubmit.value) return
  submitting.value = true
  try {
    await guiYeuCauMuaSoLuongLon({
      idChiTietSanPham: product.value.idChiTietSanPham,
      soLuong: desiredQty.value,
      tenCongTy: form.tenCongTy.trim() || null,
      hoTen: normalizeHoTen(form.hoTen),
      soDienThoai: form.soDienThoai,
      email: form.email.trim().toLowerCase(),
      ghiChu: form.ghiChu.trim() || null,
      nhanKhuyenMai: !!form.nhanKhuyenMai,
    })
    success.value = true
    initialSnapshot = snapshotForm()
    successTimer = setTimeout(() => closeBulkOrderModal(), 4000)
  } catch (err) {
    const msg = typeof err === 'string' ? err : ''
    const lower = msg.toLowerCase()
    const isNetwork =
      !msg ||
      lower.includes('status code') ||
      lower.includes('network') ||
      lower.includes('timeout') ||
      lower.includes('không kết nối') ||
      lower.includes('khong ket noi')
    formError.value = isNetwork ? 'Chưa gửi được, vui lòng thử lại' : msg
  } finally {
    submitting.value = false
  }
}

function continueShopping() {
  clearSuccessTimer()
  closeBulkOrderModal()
}
</script>

<template>
  <Teleport to="body">
    <Transition name="bulk-fade">
      <div v-if="visible && product" class="bulk-modal-overlay" @click.self="requestClose">
        <div
          ref="modalRef"
          class="bulk-modal"
          :class="{ 'bulk-modal--success': success }"
          role="dialog"
          aria-modal="true"
          aria-labelledby="bulk-modal-title"
        >
          <button type="button" class="bulk-modal__close" aria-label="Đóng" :disabled="submitting" @click="requestClose">
            <Icon icon="mdi:close" width="20" />
          </button>

          <div v-if="success" class="bulk-success">
            <div class="bulk-success__tick" aria-hidden="true">
              <svg viewBox="0 0 52 52">
                <circle class="bulk-success__circle" cx="26" cy="26" r="24" fill="none" />
                <path class="bulk-success__check" fill="none" d="M14 27 l8 8 16-16" />
              </svg>
            </div>
            <h2 id="bulk-modal-title" class="bulk-modal__title bulk-modal__title--center">SUNOVA đã nhận yêu cầu</h2>
            <p class="bulk-success__msg">
              Chúng tôi sẽ liên hệ qua số <strong>{{ maskedPhone }}</strong> trong vòng 24 giờ.
            </p>
            <button type="button" class="bulk-btn bulk-btn--solid" @click="continueShopping">Tiếp tục mua sắm</button>
          </div>

          <div v-else class="bulk-modal__grid">
            <aside class="bulk-modal__left">
              <h2 id="bulk-modal-title" class="bulk-modal__title">Đăng ký nhận tư vấn giá tốt nhất</h2>
              <p class="bulk-modal__sub">Bạn đã chọn tối đa {{ GIOI_HAN_MUA_LE }} sản phẩm theo hình thức mua lẻ.</p>

              <div class="bulk-product-card">
                <p class="bulk-modal__label">Sản phẩm bạn quan tâm</p>
                <div class="bulk-product-card__row">
                  <img :src="productImageUrl(product.anhUrl)" :alt="product.tenSanPham" class="bulk-product-card__thumb" />
                  <div class="bulk-product-card__meta">
                    <div class="bulk-product-card__name">{{ product.tenSanPham }}</div>
                    <div v-if="variantText" class="bulk-product-card__var">{{ variantText }}</div>
                  </div>
                </div>
                <div class="bulk-modal__qty" :class="{ 'bulk-field--shake': shakeField === 'qty' }">
                  <button
                    type="button"
                    :disabled="minusDisabled"
                    aria-label="Giảm"
                    @mousedown="startHold(-1, $event)"
                    @mouseup="stopHold"
                    @mouseleave="stopHold"
                    @touchstart.prevent="startHold(-1, $event)"
                    @touchend="stopHold"
                    @touchcancel="stopHold"
                  >
                    −
                  </button>
                  <input
                    ref="qtyInputRef"
                    type="text"
                    inputmode="numeric"
                    pattern="[0-9]*"
                    autocomplete="off"
                    :value="qtyDraft"
                    :disabled="submitting"
                    aria-label="Số lượng mong muốn"
                    @input="onQtyInput"
                    @blur="onQtyBlur"
                    @keydown="onQtyKeydown"
                    @paste="onQtyPaste"
                  />
                  <button
                    type="button"
                    :disabled="plusDisabled"
                    aria-label="Tăng"
                    @mousedown="startHold(1, $event)"
                    @mouseup="stopHold"
                    @mouseleave="stopHold"
                    @touchstart.prevent="startHold(1, $event)"
                    @touchend="stopHold"
                    @touchcancel="stopHold"
                  >
                    +
                  </button>
                </div>
                <p v-if="hasStockInfo" class="bulk-qty-stock">Hiện còn {{ stock }} sản phẩm trong kho</p>
                <p v-if="qtyHint" class="bulk-qty-hint">{{ qtyHint }}</p>
                <p v-if="qtyError" class="bulk-field__err">{{ qtyError }}</p>
              </div>

              <div class="bulk-contact">
                <p class="bulk-modal__label">Liên hệ nhanh</p>
                <div class="bulk-contact__row">
                  <Icon icon="mdi:phone-outline" width="18" />
                  <span>Hotline <strong>{{ bulk.hotline }}</strong><template v-if="bulk.hotlineBranch"> ({{ bulk.hotlineBranch }})</template></span>
                </div>
                <div class="bulk-contact__row">
                  <Icon icon="mdi:email-outline" width="18" />
                  <a :href="`mailto:${bulk.email}`">{{ bulk.email }}</a>
                </div>
                <div class="bulk-contact__row">
                  <Icon icon="mdi:clock-outline" width="18" />
                  <span>Giờ làm việc {{ bulk.hours }}</span>
                </div>
              </div>
            </aside>

            <form class="bulk-modal__right" @submit.prevent="onSubmit">
              <label class="bulk-field" :class="fieldClass('tenCongTy')">
                <span>Tên công ty/tổ chức</span>
                <input ref="tenCongTyRef" v-model="form.tenCongTy" type="text" maxlength="100" :disabled="submitting"
                  placeholder="Không bắt buộc" @blur="onBlurField('tenCongTy')" @input="onInputField('tenCongTy')" />
                <small v-if="touched.tenCongTy && errors.tenCongTy" class="bulk-field__err">{{ errors.tenCongTy }}</small>
              </label>

              <div class="bulk-field-row">
                <label class="bulk-field" :class="fieldClass('hoTen')">
                  <span>Họ tên <em>*</em></span>
                  <input ref="hoTenRef" v-model="form.hoTen" type="text" maxlength="60" :disabled="submitting"
                    autocomplete="name" @blur="onBlurField('hoTen')" @input="onInputField('hoTen')" />
                  <small v-if="touched.hoTen && errors.hoTen" class="bulk-field__err">{{ errors.hoTen }}</small>
                </label>
                <label class="bulk-field" :class="fieldClass('soDienThoai')">
                  <span>Số điện thoại <em>*</em></span>
                  <input ref="sdtRef" v-model="form.soDienThoai" type="tel" inputmode="numeric" maxlength="10"
                    :disabled="submitting" autocomplete="tel"
                    @input="form.soDienThoai = normalizePhoneDigits(form.soDienThoai); onInputField('soDienThoai')"
                    @blur="onBlurField('soDienThoai')" />
                  <small v-if="touched.soDienThoai && errors.soDienThoai" class="bulk-field__err">{{ errors.soDienThoai }}</small>
                </label>
              </div>

              <label class="bulk-field" :class="fieldClass('email')">
                <span>Email <em>*</em></span>
                <input ref="emailRef" v-model="form.email" type="email" maxlength="100" :disabled="submitting"
                  autocomplete="email" @blur="onBlurField('email')" @input="onInputField('email')" />
                <small v-if="touched.email && errors.email" class="bulk-field__err">{{ errors.email }}</small>
              </label>

              <label class="bulk-field" :class="fieldClass('ghiChu')">
                <span>Ghi chú</span>
                <textarea ref="ghiChuRef" v-model="form.ghiChu" rows="3" maxlength="500" :disabled="submitting"
                  placeholder="Nhu cầu, thời gian liên hệ…" @blur="onBlurField('ghiChu')" @input="onInputField('ghiChu')" />
                <div class="bulk-field__meta">
                  <small v-if="touched.ghiChu && errors.ghiChu" class="bulk-field__err">{{ errors.ghiChu }}</small>
                  <span class="bulk-field__count">{{ ghiChuCount }}/500</span>
                </div>
              </label>

              <label class="bulk-check">
                <input v-model="form.nhanKhuyenMai" type="checkbox" :disabled="submitting" />
                <span>Nhận tin khuyến mãi từ SUNOVA</span>
              </label>

              <p v-if="formError" class="bulk-form-error" role="alert">{{ formError }}</p>

              <div class="bulk-modal__actions">
                <button type="button" class="bulk-btn bulk-btn--ghost" :disabled="submitting" @click="requestClose">Hủy</button>
                <button type="submit" class="bulk-btn bulk-btn--solid" :disabled="!canSubmit">
                  <span v-if="submitting" class="bulk-btn__spinner" aria-hidden="true" />
                  {{ submitting ? "Đang gửi…" : "Gửi yêu cầu" }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.bulk-modal-overlay{position:fixed;inset:0;z-index:5000;display:flex;align-items:center;justify-content:center;padding:1.25rem;background:rgba(36,26,18,.52)}
.bulk-modal{position:relative;width:min(920px,92vw);max-height:min(92vh,820px);overflow:hidden;display:flex;flex-direction:column;background:var(--sf-warm-white,#fffaf4);border:1px solid rgba(158,115,64,.2);border-radius:16px;box-shadow:0 24px 64px rgba(36,26,18,.28);color:var(--sf-espresso,#3e2c1c);font-family:var(--sf-font-body,'Be Vietnam Pro',system-ui,sans-serif)}
.bulk-modal--success{max-width:480px;width:min(480px,92vw)}
.bulk-modal__close{position:absolute;top:.85rem;right:.85rem;border:none;background:#fff;width:36px;height:36px;border-radius:50%;display:grid;place-items:center;cursor:pointer;color:var(--sf-espresso,#3e2c1c);z-index:3;box-shadow:0 2px 8px rgba(36,26,18,.08)}
.bulk-modal__close:disabled{opacity:.5;cursor:not-allowed}
.bulk-modal__grid{display:grid;grid-template-columns:.4fr .6fr;min-height:0;flex:1;overflow:hidden}
.bulk-modal__left{padding:1.5rem 1.35rem 1.35rem;background:#f3ebe0;overflow-y:auto;border-right:1px solid rgba(158,115,64,.14)}
.bulk-modal__right{display:flex;flex-direction:column;gap:.85rem;padding:1.5rem 1.5rem 1.25rem;overflow-y:auto;min-height:0}
.bulk-modal__title{margin:0 2rem .45rem 0;font-family:var(--sf-font-display,'Playfair Display',Georgia,serif);font-size:1.55rem;font-weight:600;line-height:1.25}
.bulk-modal__title--center{margin:0 0 .5rem;text-align:center}
.bulk-modal__sub{margin:0 0 1rem;font-size:.95rem;line-height:1.5;color:var(--sf-mid,#5a5248)}
.bulk-modal__label{margin:0 0 .5rem;font-size:.72rem;font-weight:700;letter-spacing:.07em;text-transform:uppercase;color:var(--sf-gold-dark,#9e7340)}
.bulk-product-card{margin-bottom:1rem;padding:.9rem;border-radius:12px;background:rgba(255,250,244,.72);border:1px solid rgba(158,115,64,.14)}
.bulk-product-card__row{display:flex;gap:.85rem;align-items:flex-start}
.bulk-product-card__thumb{width:88px;height:88px;object-fit:cover;border-radius:12px;background:#fff;flex-shrink:0}
.bulk-product-card__name{font-weight:600;font-size:1rem;line-height:1.35}
.bulk-product-card__var{margin-top:4px;font-size:.88rem;color:var(--sf-mid,#5a5248)}
.bulk-modal__qty{display:inline-flex;align-items:center;margin-top:.75rem;border:1px solid rgba(158,115,64,.3);border-radius:999px;overflow:hidden;background:#fff}
.bulk-modal__qty button,.bulk-modal__qty input{border:none;background:transparent;font:inherit;color:inherit}
.bulk-modal__qty button{width:40px;height:40px;cursor:pointer;user-select:none;touch-action:none;font-size:1.1rem}
.bulk-modal__qty button:disabled{opacity:.35;cursor:not-allowed;pointer-events:none}
.bulk-modal__qty input{width:64px;text-align:center;outline:none;font-size:1rem;font-weight:600}
.bulk-qty-stock{margin:.4rem 0 0;font-size:.84rem;color:var(--sf-mid,#5a5248)}
.bulk-qty-hint{margin:.3rem 0 0;font-size:.8rem;color:#c2782c}
.bulk-contact{padding:.85rem .9rem;border-radius:12px;background:rgba(255,250,244,.55);border:1px solid rgba(158,115,64,.12)}
.bulk-contact__row{display:flex;align-items:flex-start;gap:.55rem;font-size:.9rem;line-height:1.45;color:var(--sf-mid,#5a5248);margin-top:.45rem}
.bulk-contact__row:first-of-type{margin-top:.15rem}
.bulk-contact__row a{color:var(--sf-gold-dark,#9e7340)}
.bulk-field-row{display:grid;grid-template-columns:1fr 1fr;gap:.75rem}
.bulk-field{display:flex;flex-direction:column;gap:.35rem;font-size:13px}
.bulk-field>span{font-weight:600;color:var(--sf-espresso,#3e2c1c)}
.bulk-field em{color:var(--sf-accent,#a33b1c);font-style:normal}
.bulk-field input,.bulk-field textarea{border:1px solid rgba(158,115,64,.28);border-radius:10px;padding:0 .85rem;min-height:44px;font:inherit;font-size:15px;background:#fff;color:inherit;transition:border-color .15s ease,box-shadow .15s ease}
.bulk-field textarea{padding-top:.7rem;padding-bottom:.7rem;min-height:96px;resize:vertical}
.bulk-field input:focus,.bulk-field textarea:focus{outline:none;border-color:var(--sf-gold-dark,#9e7340);box-shadow:0 0 0 2px rgba(158,115,64,.22)}
.bulk-field--error input,.bulk-field--error textarea{border-color:var(--sf-accent,#a33b1c)}
.bulk-field__err{display:block;color:var(--sf-accent,#a33b1c);font-size:.78rem;line-height:1.35;animation:bulk-err-in .18s ease}
.bulk-field__meta{display:flex;justify-content:space-between;align-items:flex-start;gap:.5rem;min-height:1.1rem}
.bulk-field__count{margin-left:auto;font-size:.75rem;color:var(--sf-mid,#5a5248);white-space:nowrap}
.bulk-check{display:flex;align-items:flex-start;gap:.55rem;font-size:.92rem;cursor:pointer}
.bulk-check input{margin-top:.2rem;accent-color:var(--sf-gold-dark,#9e7340)}
.bulk-form-error{margin:0;padding:.55rem .75rem;border-radius:10px;background:rgba(163,59,28,.08);color:var(--sf-accent,#a33b1c);font-size:.88rem;line-height:1.4}
.bulk-modal__actions{display:flex;gap:.65rem;justify-content:flex-end;margin-top:auto;padding-top:.35rem}
.bulk-btn{border-radius:999px;padding:.65rem 1.25rem;font:inherit;font-weight:600;cursor:pointer;display:inline-flex;align-items:center;justify-content:center;gap:.45rem;min-height:44px}
.bulk-btn:disabled{opacity:.55;cursor:not-allowed}
.bulk-btn--ghost{border:1px solid rgba(158,115,64,.35);background:transparent;color:var(--sf-espresso,#3e2c1c)}
.bulk-btn--solid{border:1px solid var(--sf-espresso,#3e2c1c);background:var(--sf-espresso,#3e2c1c);color:#fffaf4}
.bulk-btn__spinner{width:14px;height:14px;border:2px solid rgba(255,250,244,.35);border-top-color:#fffaf4;border-radius:50%;animation:bulk-spin .7s linear infinite}
.bulk-success{text-align:center;padding:2.5rem 1.5rem 1.75rem}
.bulk-success__tick{width:64px;height:64px;margin:0 auto 1rem}
.bulk-success__tick svg{width:64px;height:64px}
.bulk-success__circle{stroke:var(--sf-gold-dark,#9e7340);stroke-width:2;stroke-dasharray:160;stroke-dashoffset:160;animation:bulk-circle .45s ease forwards}
.bulk-success__check{stroke:var(--sf-espresso,#3e2c1c);stroke-width:2.5;stroke-linecap:round;stroke-linejoin:round;stroke-dasharray:48;stroke-dashoffset:48;animation:bulk-check .35s .28s ease forwards}
.bulk-success__msg{margin:0 0 1.25rem;font-size:.98rem;line-height:1.55;color:var(--sf-mid,#5a5248)}
.bulk-field--shake{animation:bulk-shake .4s ease}
.bulk-fade-enter-active,.bulk-fade-leave-active{transition:opacity .2s ease}
.bulk-fade-enter-active .bulk-modal,.bulk-fade-leave-active .bulk-modal{transition:transform .2s ease}
.bulk-fade-enter-from,.bulk-fade-leave-to{opacity:0}
.bulk-fade-enter-from .bulk-modal,.bulk-fade-leave-to .bulk-modal{transform:scale(.96)}
@keyframes bulk-err-in{from{opacity:0;transform:translateY(-3px)}to{opacity:1;transform:translateY(0)}}
@keyframes bulk-shake{0%,100%{transform:translateX(0)}20%{transform:translateX(-5px)}40%{transform:translateX(5px)}60%{transform:translateX(-3px)}80%{transform:translateX(3px)}}
@keyframes bulk-spin{to{transform:rotate(360deg)}}
@keyframes bulk-circle{to{stroke-dashoffset:0}}
@keyframes bulk-check{to{stroke-dashoffset:0}}
@media (max-width:768px){
.bulk-modal-overlay{padding:0;align-items:flex-end}
.bulk-modal{width:100%;max-width:none;max-height:96vh;border-radius:16px 16px 0 0}
.bulk-modal__grid{grid-template-columns:1fr;overflow-y:auto}
.bulk-modal__left{border-right:none;border-bottom:1px solid rgba(158,115,64,.14);padding:1.2rem 1rem 1rem}
.bulk-modal__right{padding:1rem 1rem calc(1rem + env(safe-area-inset-bottom,0))}
.bulk-field-row{grid-template-columns:1fr}
.bulk-modal__actions{position:sticky;bottom:0;flex-direction:column-reverse;background:linear-gradient(transparent,var(--sf-warm-white,#fffaf4) 28%);padding-top:.75rem}
.bulk-modal__actions .bulk-btn{width:100%}
.bulk-product-card__thumb{width:72px;height:72px}
}
@media (prefers-reduced-motion:reduce){
.bulk-fade-enter-active,.bulk-fade-leave-active,.bulk-fade-enter-active .bulk-modal,.bulk-fade-leave-active .bulk-modal,.bulk-field__err,.bulk-field--shake,.bulk-btn__spinner,.bulk-success__circle,.bulk-success__check{animation:none!important;transition:none!important}
.bulk-success__circle,.bulk-success__check{stroke-dashoffset:0}
}
</style>
