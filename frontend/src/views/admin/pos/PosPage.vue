<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { NQrCode } from 'naive-ui'
import PosVoucherModal from '@/components/admin/PosVoucherModal.vue'
import {
  getSanPhamBan,
  getPhuongThuc,
  timKhachTheoSdt,
  taoKhachNhanh,
  taoDonTaiQuay,
  buildThanhToanKetHopItem,
  tinhGiaTaiQuay,
  kiemTraThanhToanPos,
  huyThanhToanPos,
  hoanTatThanhToanPos,
  giuDon,
  dsDonCho,
  layDonCho,
  huyDonCho,
} from '@/api/banHangApi'
import { formatCurrency, formatDate } from '@/utils/format'
import { formatDiscountPercent } from '@/utils/formatVND'
import { productImageUrl } from '@/utils/productImage'
import { confirm } from '@/composables/useConfirm'
import { getPhoneValidationError, normalizePhoneDigits } from '@/utils/phone'
import { getLoHangConHangTheoBienThe } from '@/api/loHangApi'
import '@/styles/posAdmin.css'

/**
 * Cấu hình VietQR nhận chuyển khoản tại quầy (1 chỗ sửa).
 * TODO: thay bằng thông tin tài khoản thật của cửa hàng trước khi demo/nộp.
 * - bankBin: mã BIN ngân hàng (VD Vietcombank = 970436)
 * - accountNo: số tài khoản nhận tiền
 * - accountName: tên chủ TK (hiển thị trên ảnh QR)
 */
const VIETQR_CONFIG = {
  bankBin: '970436',
  accountNo: '0123456789',
  accountName: 'SUNOVA STORE',
}

const loading = ref(false)
const paying = ref(false)
const message = ref('')
const messageType = ref('success')

const keyword = ref('')
const searchResults = ref([])
const productsLoaded = ref(false)
const searchInput = ref(null)
/** null = tất cả danh mục */
const categoryFilter = ref(null)

const cart = ref([])
const lotModalOpen = ref(false)
const lotModalLine = ref(null)
const lotOptions = ref([])
const lotLoading = ref(false)
/** draft SL theo id lô khi mở popup chọn lô */
const lotQtyDraft = ref({})

const customerSdt = ref('')
const customerName = ref('')
const selectedCustomer = ref(null)
const showQuickCreate = ref(false)
const quickName = ref('')
const quickSdt = ref('')
const quickEmail = ref('')
const showCreateCustomerModal = ref(false)

const voucherCode = ref('')
const appliedVoucher = ref('')
const voucherDiscount = ref(0)
const voucherLoading = ref(false)
const showVoucherModal = ref(false)

/** Toàn bộ PTTT từ API — cần CHUYEN_KHOAN nội bộ cho thanh toán kết hợp. */
const allPaymentMethods = ref([])
/** Nút POS: chỉ Tiền mặt + VNPay. CK không hiện riêng, dùng trong Kết hợp. */
const paymentMethods = computed(() =>
  allPaymentMethods.value.filter((p) => {
    const ma = String(p.ma || '').toUpperCase()
    return ma === 'TIEN_MAT' || ma === 'VNPAY'
  }),
)
const selectedPaymentId = ref(null)
const cashGiven = ref('')
const transferRef = ref('')
const ghiChu = ref('')

const isSplitMode = ref(false)
const splitCashAmount = ref('')
const splitTransferRef = ref('')
const splitTransferConfirmed = ref(false)
const showVietQrModal = ref(false)

const receipt = ref(null)
const showReceipt = ref(false)

const MAX_HELD_ORDERS = 15

const heldOrders = ref([])
const showHeldDrawer = ref(false)
const activeHeldOrderId = ref(null)
const holding = ref(false)

const showQrModal = ref(false)
const qrPaymentUrl = ref('')
const qrOrderId = ref(null)
const qrOrderCode = ref('')
const qrAmount = ref(0)
const qrTransactionRef = ref('')
const qrStatus = ref('CHO_THANH_TOAN')
const qrPolling = ref(false)
const qrCompleting = ref(false)

let searchTimer = null
let qrPollTimer = null

const heldCount = computed(() =>
  heldOrders.value.filter((o) => o.id !== activeHeldOrderId.value).length,
)

const visibleHeldOrders = computed(() =>
  heldOrders.value.filter((o) => o.id !== activeHeldOrderId.value),
)

const selectedPayment = computed(() =>
  paymentMethods.value.find((p) => p.id === selectedPaymentId.value),
)

const isCash = computed(() => selectedPayment.value?.ma === 'TIEN_MAT')

const isVnpay = computed(() => selectedPayment.value?.ma === 'VNPAY')

const isManualTransfer = computed(() =>
  selectedPayment.value && !isCash.value && !isVnpay.value,
)

const isNonCash = computed(() => isManualTransfer.value)

const paymentByMa = computed(() => {
  const map = {}
  for (const p of allPaymentMethods.value) {
    const ma = String(p.ma || '').toUpperCase()
    if (ma) map[ma] = p
  }
  return map
})

const tongTienHang = computed(() =>
  cart.value.reduce((sum, line) => sum + line.giaBan * line.soLuong, 0),
)

const cartItemCount = computed(() =>
  cart.value.reduce((sum, line) => sum + Number(line.soLuong || 0), 0),
)

const categoryChips = computed(() => {
  const set = new Set()
  for (const p of searchResults.value) {
    if (p.tenDanhMuc) set.add(p.tenDanhMuc)
  }
  return [...set].sort((a, b) => a.localeCompare(b, 'vi'))
})

const filteredProducts = computed(() => {
  if (!categoryFilter.value) return searchResults.value
  return searchResults.value.filter((p) => p.tenDanhMuc === categoryFilter.value)
})

const thanhTien = computed(() => Math.max(0, tongTienHang.value - voucherDiscount.value))

const splitCashNum = computed(() => Number(splitCashAmount.value) || 0)
const splitTransferNum = computed(() => Math.max(0, thanhTien.value - splitCashNum.value))
const splitCashValid = computed(() =>
  splitCashNum.value > 0 && splitCashNum.value < thanhTien.value,
)

const vietQrImageUrl = computed(() => {
  const amount = Math.round(splitTransferNum.value)
  if (amount <= 0) return ''
  const addInfo = encodeURIComponent(`SUNOVA CK ${amount}`)
  const accName = encodeURIComponent(VIETQR_CONFIG.accountName || 'SUNOVA')
  return `https://img.vietqr.io/image/${VIETQR_CONFIG.bankBin}-${VIETQR_CONFIG.accountNo}-compact2.png?amount=${amount}&addInfo=${addInfo}&accountName=${accName}`
})

const tienThua = computed(() => {
  const cash = Number(cashGiven.value) || 0
  return Math.max(0, cash - thanhTien.value)
})

const cashGivenNum = computed(() => Number(cashGiven.value) || 0)

const cashShortage = computed(() => {
  if (!isCash.value || cart.value.length === 0) return 0
  const diff = thanhTien.value - cashGivenNum.value
  return diff > 0 ? diff : 0
})

const canCheckout = computed(() => {
  if (cart.value.length === 0) return false

  if (isSplitMode.value) {
    return splitCashValid.value
      && splitTransferNum.value > 0
      && splitTransferConfirmed.value
  }

  if (!selectedPaymentId.value) return false
  if (isCash.value) {
    const cash = Number(cashGiven.value) || 0
    return cash >= thanhTien.value
  }
  return true
})

const checkoutButtonLabel = computed(() => {
  if (paying.value) return isVnpay.value ? 'Đang tạo mã QR...' : 'Đang tạo...'
  return isVnpay.value ? 'Tạo mã QR thanh toán' : 'Tạo hóa đơn'
})

const qrStatusLabel = computed(() => {
  if (qrStatus.value === 'THANH_CONG') return 'Đã thanh toán'
  if (qrStatus.value === 'THAT_BAI') return 'Thanh toán thất bại'
  return 'Chờ khách quét mã QR'
})

function notify(text, type = 'success') {
  message.value = text
  messageType.value = type
  setTimeout(() => { message.value = '' }, 4000)
}

function formatVariant(item) {
  const parts = []
  if (item.dungTichMl != null) parts.push(`${item.dungTichMl}ml`)
  if (item.tenMauSac) parts.push(item.tenMauSac)
  return parts.join(' / ') || '—'
}

function cartKey(line) {
  if (line.phanBoLos?.length) {
    const sig = [...line.phanBoLos]
      .map((p) => `${p.idLoHang}:${p.soLuong}`)
      .sort()
      .join(',')
    return `${line.idChiTietSanPham}-m-${sig}`
  }
  return `${line.idChiTietSanPham}-${line.idLoHang ?? 'fefo'}`
}

function mapCartItems() {
  return cart.value.map((l) => {
    const base = {
      idChiTietSanPham: l.idChiTietSanPham,
      soLuong: l.soLuong,
    }
    if (l.phanBoLos?.length) {
      return {
        ...base,
        phanBoLos: l.phanBoLos.map((p) => ({
          idLoHang: p.idLoHang,
          soLuong: p.soLuong,
        })),
      }
    }
    if (l.idLoHang != null) {
      return { ...base, idLoHang: l.idLoHang }
    }
    return base
  })
}

function isOutOfStock(product) {
  return !product.soLuongTon || product.soLuongTon <= 0
}

function posExpiryBadge(product) {
  if (product.soNgayConLai == null || !product.hanSuDungGanNhat) return null
  if (product.soNgayConLai <= 0) return 'expired'
  if (product.soNgayConLai <= 30) return 'warning'
  return null
}

function isOnSale(product) {
  return Boolean(product.dangGiamGia)
}

function saleLabel(product) {
  return formatDiscountPercent(product.phanTramGiam)
}

function addToCart(product) {
  if (isOutOfStock(product)) return
  const existing = cart.value.find(
    (l) =>
      l.idChiTietSanPham === product.idChiTietSanPham &&
      l.idLoHang == null &&
      !l.phanBoLos?.length,
  )
  if (existing) {
    if (existing.soLuong >= product.soLuongTon) {
      notify(`Đã đạt tồn tối đa cho ${product.sku}`, 'error')
      return
    }
    existing.soLuong += 1
  } else {
    cart.value.push({
      idChiTietSanPham: product.idChiTietSanPham,
      sku: product.sku,
      tenSanPham: product.tenSanPham,
      anhUrl: product.anhUrl,
      bienThe: formatVariant(product),
      giaBan: Number(product.giaBan),
      giaGoc: product.giaGoc != null ? Number(product.giaGoc) : null,
      phanTramGiam: product.phanTramGiam != null ? Number(product.phanTramGiam) : null,
      dangGiamGia: Boolean(product.dangGiamGia),
      soLuongTon: product.soLuongTon,
      soLuong: 1,
      idLoHang: null,
      soLo: null,
      hanSuDungLo: null,
      phanBoLos: null,
      /** HSD lô FEFO gần nhất (từ API bán) — hiện khi chưa chọn lô tay */
      hanSuDungGanNhat: product.hanSuDungGanNhat || null,
      soNgayConLai: product.soNgayConLai != null ? Number(product.soNgayConLai) : null,
    })
  }
  if (appliedVoucher.value) {
    void recalculateVoucher()
  }
}

function clearManualLot(line) {
  if (!line) return
  line.phanBoLos = null
  line.idLoHang = null
  line.soLo = null
  line.hanSuDungLo = null
}

function changeQty(line, delta) {
  const next = line.soLuong + delta
  if (next <= 0) {
    removeLine(line)
    return
  }
  if (next > line.soLuongTon) {
    notify(`Tồn kho còn ${line.soLuongTon}`, 'error')
    return
  }
  const hadManual = Boolean(line.phanBoLos?.length || line.idLoHang != null)
  line.soLuong = next
  if (hadManual) {
    clearManualLot(line)
    notify('Đã đổi số lượng — chọn lại lô hoặc giữ FEFO tự động', 'success')
  }
  if (appliedVoucher.value) {
    void recalculateVoucher()
  }
}

function removeLine(line) {
  cart.value = cart.value.filter((l) => cartKey(l) !== cartKey(line))
  if (appliedVoucher.value) {
    if (cart.value.length === 0) {
      clearVoucher()
    } else {
      void recalculateVoucher()
    }
  }
}

async function openLotPicker(line) {
  lotModalLine.value = line
  lotModalOpen.value = true
  lotOptions.value = []
  lotQtyDraft.value = {}
  lotLoading.value = true
  try {
    const res = await getLoHangConHangTheoBienThe(line.idChiTietSanPham)
    lotOptions.value = res.data || []
    const draft = {}
    lotOptions.value.forEach((l) => {
      draft[l.id] = 0
    })
    if (line.phanBoLos?.length) {
      line.phanBoLos.forEach((p) => {
        if (p.idLoHang != null) draft[p.idLoHang] = Number(p.soLuong) || 0
      })
    } else if (line.idLoHang != null) {
      draft[line.idLoHang] = Number(line.soLuong) || 0
    }
    lotQtyDraft.value = draft
  } catch (err) {
    notify(String(err), 'error')
    lotModalOpen.value = false
  } finally {
    lotLoading.value = false
  }
}

function closeLotPicker() {
  lotModalOpen.value = false
  lotModalLine.value = null
  lotOptions.value = []
  lotQtyDraft.value = {}
}

const lotDraftTotal = computed(() =>
  Object.values(lotQtyDraft.value).reduce((sum, n) => sum + (Number(n) || 0), 0),
)

/** Cho phép xác nhận khi tổng > 0 (số lượng dòng sẽ tự = tổng). */
const lotDraftCanConfirm = computed(() => lotDraftTotal.value > 0)

function setLotDraftQty(lotId, raw) {
  const max = Number(lotOptions.value.find((l) => l.id === lotId)?.soLuongCon) || 0
  let n = Number(raw)
  if (!Number.isFinite(n) || n < 0) n = 0
  if (n > max) n = max
  lotQtyDraft.value = { ...lotQtyDraft.value, [lotId]: Math.floor(n) }
}

async function confirmLotSelection() {
  const line = lotModalLine.value
  if (!line) return

  const selected = lotOptions.value
    .map((lot) => ({
      lot,
      soLuong: Number(lotQtyDraft.value[lot.id]) || 0,
    }))
    .filter((x) => x.soLuong > 0)

  const tong = selected.reduce((s, x) => s + x.soLuong, 0)
  if (tong <= 0) {
    notify('Nhập số lượng lấy từ ít nhất 1 lô.', 'error')
    return
  }
  if (tong > Number(line.soLuongTon)) {
    notify(`Tổng chọn (${tong}) vượt tồn SKU (còn ${line.soLuongTon}).`, 'error')
    return
  }

  for (const { lot, soLuong } of selected) {
    if (soLuong > Number(lot.soLuongCon)) {
      notify(`Lô [${lot.soLo}] không đủ hàng (còn ${lot.soLuongCon}).`, 'error')
      return
    }
  }

  // Cảnh báo nếu bỏ qua lô cận hạn có HSD sớm hơn lô đang lấy
  const skippedNear = lotOptions.value.find((l) => {
    if (!l.sapHetHan) return false
    const take = Number(lotQtyDraft.value[l.id]) || 0
    if (take > 0) return false
    return selected.some(
      (s) =>
        s.lot.hanSuDung &&
        l.hanSuDung &&
        new Date(l.hanSuDung) < new Date(s.lot.hanSuDung),
    )
  })
  if (skippedNear) {
    const ok = await confirm({
      title: 'Bỏ qua lô cận hạn?',
      message: `Bạn đang bỏ qua lô [${skippedNear.soLo}] sắp hết hạn (${formatDate(skippedNear.hanSuDung)}) — vẫn tiếp tục?`,
      confirmText: 'Vẫn tiếp tục',
    })
    if (!ok) return
  }

  // Số lượng dòng = tổng các lô đã chọn
  line.soLuong = tong

  if (selected.length === 1) {
    const { lot } = selected[0]
    line.phanBoLos = null
    line.idLoHang = lot.id
    line.soLo = lot.soLo
    line.hanSuDungLo = lot.hanSuDung
  } else {
    line.idLoHang = null
    line.soLo = null
    line.hanSuDungLo = selected
      .map((s) => s.lot.hanSuDung)
      .filter(Boolean)
      .sort((a, b) => new Date(a) - new Date(b))[0] || null
    line.phanBoLos = selected.map(({ lot, soLuong }) => ({
      idLoHang: lot.id,
      soLo: lot.soLo,
      soLuong,
      hanSuDung: lot.hanSuDung,
    }))
  }

  if (appliedVoucher.value) {
    void recalculateVoucher()
  }
  closeLotPicker()
}

function clearLotSelection() {
  const line = lotModalLine.value
  if (!line) return
  const dup = cart.value.find(
    (l) => l !== line && l.idChiTietSanPham === line.idChiTietSanPham && !l.phanBoLos?.length && l.idLoHang == null,
  )
  if (dup) {
    const total = dup.soLuong + line.soLuong
    if (total > line.soLuongTon) {
      notify(`Gộp dòng vượt tồn SKU (còn ${line.soLuongTon}).`, 'error')
      return
    }
    dup.soLuong = total
    removeLine(line)
  } else {
    clearManualLot(line)
  }
  closeLotPicker()
}

function lineLotLabel(line) {
  if (line.phanBoLos?.length) {
    return `Chọn lô thủ công · ${line.soLuong} sp (${line.phanBoLos.length} lô)`
  }
  if (line.soLo) return `Lô: ${line.soLo} · ${line.soLuong} sp`
  return 'Tự động (FEFO) · Chọn lô'
}

function lineLotHint(line) {
  if (!line.phanBoLos?.length) return null
  return line.phanBoLos.map((p) => `${p.soLo || '#' + p.idLoHang}×${p.soLuong}`).join(', ')
}

async function loadProducts() {
  loading.value = true
  try {
    const res = await getSanPhamBan(keyword.value.trim(), 0)
    searchResults.value = res.data || []
    productsLoaded.value = true
  } catch (err) {
    notify(String(err), 'error')
  } finally {
    loading.value = false
  }
}

function expiryTone(line) {
  const hsd = lineDisplayHsd(line)
  if (!hsd) return null
  const days =
    (line.phanBoLos?.length || line.idLoHang != null) && line.hanSuDungLo
      ? Math.ceil((new Date(line.hanSuDungLo) - new Date()) / (1000 * 60 * 60 * 24))
      : line.soNgayConLai
  if (days == null || Number.isNaN(days)) return null
  if (days <= 0) return 'expired'
  if (days < 30) return 'warning'
  return null
}

function lineDisplayHsd(line) {
  if (line.hanSuDungLo) return line.hanSuDungLo
  return line.hanSuDungGanNhat || null
}

function onSearchEnter() {
  if (filteredProducts.value.length > 0) {
    const first = filteredProducts.value.find((p) => p.soLuongTon > 0) || filteredProducts.value[0]
    if (first.soLuongTon > 0) addToCart(first)
  }
}

async function loadMeta() {
  try {
    const ptRes = await getPhuongThuc()
    allPaymentMethods.value = ptRes.data || []
    const cash = paymentByMa.value.TIEN_MAT
    if (cash) selectedPaymentId.value = cash.id
  } catch (err) {
    notify(String(err), 'error')
  }
}

async function findCustomer() {
  const sdt = normalizePhoneDigits(customerSdt.value)
  customerSdt.value = sdt
  const phoneErr = getPhoneValidationError(sdt)
  if (phoneErr) {
    notify(phoneErr, 'error')
    return
  }
  showQuickCreate.value = false
  try {
    const res = await timKhachTheoSdt(sdt)
    selectedCustomer.value = res.data
    customerName.value = res.data.hoTen || ''
    notify(`Đã tìm thấy: ${res.data.hoTen}`)
  } catch {
    selectedCustomer.value = null
    showQuickCreate.value = true
    quickSdt.value = sdt
    quickName.value = customerName.value
    notify('Không tìm thấy khách — có thể tạo nhanh', 'error')
  }
}

function validateGuestFields({ requireBoth = false } = {}) {
  const name = customerName.value.trim()
  const sdtRaw = customerSdt.value.trim()
  const sdt = normalizePhoneDigits(sdtRaw)
  if (sdtRaw) customerSdt.value = sdt

  if (requireBoth || name || sdt) {
    if (!name || name.length < 2) {
      return 'Nhập tên khách (ít nhất 2 ký tự) để gắn vào hóa đơn chờ'
    }
    if (/^\d+$/.test(name)) {
      return 'Tên khách không được chỉ gồm số'
    }
    const phoneErr = getPhoneValidationError(sdt)
    if (phoneErr) return phoneErr
  }
  return ''
}

async function createQuickCustomer() {
  const name = (quickName.value || customerName.value).trim()
  const sdt = normalizePhoneDigits(quickSdt.value || customerSdt.value)
  quickSdt.value = sdt
  if (!name || name.length < 2) {
    notify('Vui lòng nhập tên khách (ít nhất 2 ký tự)', 'error')
    return
  }
  if (/^\d+$/.test(name)) {
    notify('Tên khách không được chỉ gồm số', 'error')
    return
  }
  const phoneErr = getPhoneValidationError(sdt)
  if (phoneErr) {
    notify(phoneErr, 'error')
    return
  }
  try {
    const payload = {
      hoTen: name,
      soDienThoai: sdt,
    }
    if (quickEmail.value.trim()) {
      const email = quickEmail.value.trim()
      if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        notify('Email không hợp lệ', 'error')
        return
      }
      payload.email = email
    }
    const res = await taoKhachNhanh(payload)
    selectedCustomer.value = res.data
    customerSdt.value = res.data.soDienThoai || sdt
    customerName.value = res.data.hoTen || name
    showQuickCreate.value = false
    showCreateCustomerModal.value = false
    quickName.value = ''
    quickSdt.value = ''
    quickEmail.value = ''
    notify(`Đã tạo khách: ${res.data.hoTen}`)
  } catch (err) {
    notify(String(err), 'error')
  }
}

function openCreateCustomerModal() {
  quickSdt.value = customerSdt.value.trim()
  quickName.value = customerName.value.trim()
  quickEmail.value = ''
  showCreateCustomerModal.value = true
}

function clearCustomer() {
  selectedCustomer.value = null
  customerSdt.value = ''
  customerName.value = ''
  quickName.value = ''
  quickSdt.value = ''
  quickEmail.value = ''
  showQuickCreate.value = false
  showCreateCustomerModal.value = false
}

function fillExactCash() {
  cashGiven.value = String(thanhTien.value)
}

function addDenomination(amount) {
  cashGiven.value = String(cashGivenNum.value + amount)
}

function clearCash() {
  cashGiven.value = ''
}

function resetSplitFields() {
  splitCashAmount.value = ''
  splitTransferRef.value = ''
  splitTransferConfirmed.value = false
  showVietQrModal.value = false
}

function toggleSplitMode() {
  isSplitMode.value = !isSplitMode.value
  resetSplitFields()
  cashGiven.value = ''
  transferRef.value = ''
  const cash = paymentByMa.value.TIEN_MAT
  if (cash) selectedPaymentId.value = cash.id
}

function payMethodLabel(pt) {
  if (!pt) return ''
  if (pt.ma === 'TIEN_MAT') return 'Tiền mặt'
  if (pt.ma === 'VNPAY') return 'VNPay'
  if (pt.ma === 'CHUYEN_KHOAN' || /chuyển|ck/i.test(pt.ten || '')) return 'CK'
  return pt.ten
}

function selectPayMethod(pt) {
  if (isSplitMode.value) {
    isSplitMode.value = false
    resetSplitFields()
  }
  selectedPaymentId.value = pt.id
}

function selectSplitPay() {
  if (!isSplitMode.value) toggleSplitMode()
}

function openVietQrModal() {
  if (splitTransferNum.value <= 0) {
    notify('Nhập tiền mặt nhỏ hơn thành tiền để còn phần chuyển khoản', 'error')
    return
  }
  showVietQrModal.value = true
}

async function confirmVietQrReceived() {
  splitTransferConfirmed.value = true
  showVietQrModal.value = false
  if (!canCheckout.value) {
    notify('Đã ghi nhận chuyển khoản. Nhập tiền mặt (nhỏ hơn tổng) rồi bấm Tạo hóa đơn.')
    return
  }
  await checkout({ skipConfirm: true })
}

function cancelVietQrModal() {
  showVietQrModal.value = false
}

watch(splitCashAmount, () => {
  splitTransferConfirmed.value = false
})

watch(thanhTien, () => {
  if (isSplitMode.value) splitTransferConfirmed.value = false
})

watch(
  () => selectedCustomer.value?.id ?? null,
  () => {
    if (appliedVoucher.value) void recalculateVoucher()
  },
)

function clearVoucher() {
  voucherCode.value = ''
  appliedVoucher.value = ''
  voucherDiscount.value = 0
}

async function recalculateVoucher() {
  if (!appliedVoucher.value || cart.value.length === 0) {
    voucherDiscount.value = 0
    return
  }
  voucherLoading.value = true
  try {
    const res = await tinhGiaTaiQuay({
      items: mapCartItems(),
      maPhieuGiamGia: appliedVoucher.value,
      idKhachHang: selectedCustomer.value?.id ?? null,
    })
    voucherDiscount.value = Number(res.data?.tienGiamGia) || 0
  } catch (err) {
    clearVoucher()
    notify(String(err), 'error')
  } finally {
    voucherLoading.value = false
  }
}

function openVoucherModal() {
  if (cart.value.length === 0) {
    notify('Thêm sản phẩm trước khi chọn mã giảm giá', 'error')
    return
  }
  showVoucherModal.value = true
}

function onVoucherModalSelect(code) {
  const next = String(code || '').trim()
  if (!next) {
    clearVoucher()
    notify('Đã bỏ mã giảm giá')
    return
  }
  voucherCode.value = next
  void applyVoucher(next, { skipConfirm: true })
}

async function applyVoucher(codeOverride, { skipConfirm = false } = {}) {
  const code = String(codeOverride ?? voucherCode.value).trim()
  if (!code) {
    clearVoucher()
    return
  }
  if (cart.value.length === 0) {
    notify('Thêm sản phẩm trước khi áp mã giảm giá', 'error')
    return
  }
  if (!skipConfirm) {
    const ok = await confirm({
      title: 'Áp mã giảm giá',
      message: `Áp dụng mã "${code}" cho đơn này?`,
      confirmText: 'Áp mã',
    })
    if (!ok) return
  }
  voucherLoading.value = true
  try {
    const res = await tinhGiaTaiQuay({
      items: mapCartItems(),
      maPhieuGiamGia: code,
      idKhachHang: selectedCustomer.value?.id ?? null,
    })
    voucherCode.value = code
    appliedVoucher.value = code
    voucherDiscount.value = Number(res.data?.tienGiamGia) || 0
    notify(`Đã áp mã "${code}" — giảm ${formatCurrency(voucherDiscount.value)}`)
  } catch (err) {
    clearVoucher()
    notify(String(err), 'error')
  } finally {
    voucherLoading.value = false
  }
}

async function loadHeldOrders() {
  try {
    const res = await dsDonCho()
    const rows = Array.isArray(res.data) ? res.data : []
    // Phòng thủ: bỏ bản ghi thiếu id / dữ liệu hỏng, soLuong/soMatHang null → 0
    heldOrders.value = rows
      .filter((o) => o && o.id != null)
      .map((o) => ({
        ...o,
        soMatHang: Number(o.soMatHang) || 0,
        thanhTien: Number(o.thanhTien) || 0,
        tenKhachHang: o.tenKhachHang || 'Khách lẻ',
        soDienThoai: o.soDienThoai || '',
      }))
  } catch (err) {
    heldOrders.value = []
    notify(String(err), 'error')
  }
}

async function openHeldDrawer() {
  showHeldDrawer.value = true
  await loadHeldOrders()
}

function closeHeldDrawer() {
  showHeldDrawer.value = false
}

function formatHeldAgo(dateStr) {
  if (!dateStr) return ''
  const diff = Date.now() - new Date(dateStr).getTime()
  const mins = Math.floor(diff / 60000)
  if (mins < 1) return 'vừa xong'
  if (mins < 60) return `${mins} phút trước`
  const hours = Math.floor(mins / 60)
  return `${hours} giờ trước`
}

function clearCartOnly() {
  cart.value = []
  clearVoucher()
  cashGiven.value = ''
  transferRef.value = ''
}

async function holdCurrentOrder() {
  if (cart.value.length === 0) return

  if (!selectedCustomer.value) {
    const guestErr = validateGuestFields({ requireBoth: true })
    if (guestErr) {
      notify(guestErr, 'error')
      return
    }
  }

  // Làm mới danh sách trước khi chặn — tránh lệch số khi nhiều máy/quầy
  await loadHeldOrders()
  const replacingId = activeHeldOrderId.value
  // Khi đang tiếp tục 1 đơn chờ rồi giữ lại: xóa cũ trước rồi tạo mới → không tăng tổng
  if (!replacingId && heldOrders.value.length >= MAX_HELD_ORDERS) {
    notify('Đã đạt tối đa 15 hóa đơn chờ', 'error')
    return
  }

  const ok = await confirm({
    title: 'Giữ đơn',
    message: 'Lưu giỏ hàng hiện tại thành đơn chờ?',
    confirmText: 'Giữ đơn',
  })
  if (!ok) return
  holding.value = true
  try {
    if (replacingId) {
      try {
        await huyDonCho(replacingId)
      } catch {
        /* đơn cũ có thể đã xử lý */
      }
      activeHeldOrderId.value = null
    }
    await giuDon({
      items: mapCartItems(),
      idKhachHang: selectedCustomer.value?.id ?? null,
      tenKhachHang: selectedCustomer.value?.hoTen || customerName.value.trim() || null,
      soDienThoai: selectedCustomer.value?.soDienThoai
        || normalizePhoneDigits(customerSdt.value)
        || null,
    })
    clearCartOnly()
    clearCustomer()
    await loadHeldOrders()
    notify('Đã giữ đơn')
    nextTick(() => searchInput.value?.focus())
  } catch (err) {
    notify(String(err), 'error')
  } finally {
    holding.value = false
  }
}

function loadCartFromDetail(detail) {
  const items = Array.isArray(detail?.items) ? detail.items : []
  cart.value = items
    .filter((item) => item && item.idChiTietSanPham != null)
    .map((item) => ({
      idChiTietSanPham: item.idChiTietSanPham,
      sku: item.sku || '—',
      tenSanPham: item.tenSanPham || 'Sản phẩm',
      bienThe: formatVariant(item),
      giaBan: Number(item.donGia) || 0,
      soLuongTon: Number(item.soLuongTon) || 0,
      soLuong: Math.max(0, Number(item.soLuong) || 0),
      idLoHang: item.idLoHang ?? null,
      soLo: item.soLo || null,
      hanSuDungLo: item.phanBoLos?.length
        ? [...item.phanBoLos]
            .map((p) => p.hanSuDung)
            .filter(Boolean)
            .sort((a, b) => new Date(a) - new Date(b))[0] || null
        : null,
      phanBoLos: item.phanBoLos?.length
        ? item.phanBoLos.map((p) => ({
            idLoHang: p.idLoHang,
            soLo: p.soLo,
            soLuong: Number(p.soLuong) || 0,
            hanSuDung: p.hanSuDung || null,
          }))
        : null,
    }))
    .filter((line) => line.soLuong > 0)
}

async function resumeHeldOrder(order) {
  if (cart.value.length > 0) {
    const ok = await confirm({
      title: 'Tiếp tục đơn chờ',
      message: 'Giỏ đang có hàng. Thay thế bằng đơn chờ này?',
      confirmText: 'Thay thế',
    })
    if (!ok) return
  }
  try {
    const res = await layDonCho(order?.id)
    const detail = res?.data
    if (!detail) {
      notify('Không tải được đơn chờ', 'error')
      return
    }
    loadCartFromDetail(detail)
    if (detail.idKhachHang) {
      selectedCustomer.value = {
        id: detail.idKhachHang,
        hoTen: detail.hoTenKhachHang,
        soDienThoai: detail.soDienThoai,
      }
      customerSdt.value = detail.soDienThoai || ''
      customerName.value = detail.hoTenKhachHang || ''
      showQuickCreate.value = false
    } else {
      selectedCustomer.value = null
      customerSdt.value = detail.soDienThoai || ''
      customerName.value = detail.hoTenKhachHang || ''
      showQuickCreate.value = false
    }
    activeHeldOrderId.value = order.id
    closeHeldDrawer()
    notify(`Đã nạp đơn ${order.maHoaDon || ''}`)
    nextTick(() => searchInput.value?.focus())
  } catch (err) {
    notify(String(err), 'error')
  }
}

async function cancelHeldOrder(order) {
  const ok = await confirm({
    title: 'Hủy đơn chờ',
    message: `Hủy đơn chờ của ${order.tenKhachHang || 'khách'}?`,
    confirmText: 'Hủy đơn',
    danger: true,
  })
  if (!ok) return
  try {
    await huyDonCho(order.id)
    if (activeHeldOrderId.value === order.id) {
      activeHeldOrderId.value = null
    }
    await loadHeldOrders()
    notify('Đã hủy đơn chờ')
  } catch (err) {
    notify(String(err), 'error')
  }
}

async function checkout(options = {}) {
  if (!canCheckout.value) return
  if (!options.skipConfirm && !isVnpay.value) {
    const ok = await confirm({
      title: 'Tạo hóa đơn',
      message: 'Bạn có chắc muốn tạo hóa đơn này?',
      confirmText: 'Tạo hóa đơn',
    })
    if (!ok) return
  }
  paying.value = true
  try {
    const cashPt = paymentByMa.value.TIEN_MAT
    const transferPt = paymentByMa.value.CHUYEN_KHOAN

    const payload = {
      items: mapCartItems(),
      idKhachHang: selectedCustomer.value?.id ?? null,
      maPhieuGiamGia: appliedVoucher.value || null,
      idPhuongThucThanhToan: isSplitMode.value
        ? (splitCashNum.value >= splitTransferNum.value ? cashPt?.id : transferPt?.id) ?? selectedPaymentId.value
        : selectedPaymentId.value,
      soTienKhachDua: !isSplitMode.value && isCash.value ? Number(cashGiven.value) : null,
      maGiaoDich: !isSplitMode.value && isManualTransfer.value && transferRef.value.trim()
        ? transferRef.value.trim()
        : null,
      ghiChu: ghiChu.value.trim() || null,
      idHoaDonCho: activeHeldOrderId.value ?? null,
    }

    if (isSplitMode.value) {
      if (!cashPt?.id || !transferPt?.id) {
        notify('Thiếu phương thức Tiền mặt hoặc Chuyển khoản', 'error')
        return
      }
      payload.danhSachThanhToan = [
        buildThanhToanKetHopItem({
          idPhuongThucThanhToan: cashPt.id,
          soTien: splitCashNum.value,
        }),
        buildThanhToanKetHopItem({
          idPhuongThucThanhToan: transferPt.id,
          soTien: splitTransferNum.value,
          maGiaoDich: splitTransferRef.value.trim() || null,
        }),
      ]
    }

    const res = await taoDonTaiQuay(payload)
    if (res.data?.paymentUrl) {
      // Giữ giỏ / khách — chỉ xóa khi thanh toán xong (modal hoặc IPN)
      openQrPayment(res.data)
      notify('Đã tạo mã QR — chờ khách thanh toán')
      return
    }
    receipt.value = res.data
    showReceipt.value = true
    clearSaleAfterPaid()
    notify('Thanh toán thành công!')
  } catch (err) {
    notify(String(err), 'error')
  } finally {
    paying.value = false
  }
}

/** Xóa giỏ / khách sau khi đơn đã thanh toán xong (tiền mặt hoặc QR). */
function clearSaleAfterPaid() {
  activeHeldOrderId.value = null
  clearCustomer()
  cart.value = []
  clearVoucher()
  cashGiven.value = ''
  transferRef.value = ''
  isSplitMode.value = false
  resetSplitFields()
  void loadProducts()
  void loadHeldOrders()
}

function openQrPayment(data) {
  qrOrderId.value = data.id
  qrOrderCode.value = data.maHoaDon
  qrAmount.value = Number(data.thanhTien) || 0
  qrPaymentUrl.value = data.paymentUrl
  qrTransactionRef.value = data.transactionRef || ''
  qrStatus.value = data.trangThaiThanhToan || 'CHO_THANH_TOAN'
  showQrModal.value = true
  startQrPolling()
}

function stopQrPolling() {
  qrPolling.value = false
  if (qrPollTimer) {
    clearInterval(qrPollTimer)
    qrPollTimer = null
  }
}

async function pollQrPaymentStatus() {
  if (!qrOrderId.value || qrPolling.value) return
  qrPolling.value = true
  try {
    const res = await kiemTraThanhToanPos(qrOrderId.value)
    const status = res.data?.trangThaiThanhToan
    if (status) qrStatus.value = status
    if (status === 'THANH_CONG' && res.data?.hoaDon) {
      stopQrPolling()
      showQrModal.value = false
      receipt.value = res.data.hoaDon
      showReceipt.value = true
      clearSaleAfterPaid()
      notify('Thanh toán QR thành công!')
    } else if (status === 'THAT_BAI') {
      stopQrPolling()
      notify('Thanh toán thất bại hoặc đã hủy', 'error')
    }
  } catch (err) {
    notify(String(err), 'error')
  } finally {
    qrPolling.value = false
  }
}

function startQrPolling() {
  stopQrPolling()
  void pollQrPaymentStatus()
  qrPollTimer = setInterval(() => {
    void pollQrPaymentStatus()
  }, 3000)
}

async function cancelQrPayment() {
  const ok = await confirm({
    title: 'Hủy thanh toán QR',
    message: 'Hủy giao dịch và hoàn tồn kho cho đơn này?',
    confirmText: 'Hủy thanh toán',
    danger: true,
  })
  if (!ok) return
  try {
    if (qrOrderId.value) {
      await huyThanhToanPos(qrOrderId.value)
    }
    stopQrPolling()
    showQrModal.value = false
    qrPaymentUrl.value = ''
    qrOrderId.value = null
    void loadProducts()
    notify('Đã hủy thanh toán QR')
  } catch (err) {
    notify(String(err), 'error')
  }
}

/** Hoàn tất thủ công (chưa có IPN VNPAY) — nhân viên xác nhận khách đã thanh toán xong. */
async function completeQrPayment() {
  if (!qrOrderId.value || qrCompleting.value) return
  const ok = await confirm({
    title: 'Hoàn tất thanh toán',
    message: 'Xác nhận khách đã thanh toán thành công trên app VNPay / ngân hàng?',
    confirmText: 'Thanh toán',
  })
  if (!ok) return
  qrCompleting.value = true
  try {
    const res = await hoanTatThanhToanPos(qrOrderId.value)
    stopQrPolling()
    showQrModal.value = false
    qrPaymentUrl.value = ''
    qrOrderId.value = null
    if (res.data?.hoaDon) {
      receipt.value = res.data.hoaDon
      showReceipt.value = true
    }
    clearSaleAfterPaid()
    notify('Thanh toán QR thành công!')
  } catch (err) {
    notify(String(err), 'error')
  } finally {
    qrCompleting.value = false
  }
}

function closeQrModal() {
  if (qrStatus.value === 'CHO_THANH_TOAN') {
    void cancelQrPayment()
    return
  }
  stopQrPolling()
  showQrModal.value = false
}

function resetSale() {
  clearTimeout(searchTimer)
  stopQrPolling()
  showQrModal.value = false
  qrPaymentUrl.value = ''
  qrOrderId.value = null
  cart.value = []
  keyword.value = ''
  clearCustomer()
  voucherCode.value = ''
  appliedVoucher.value = ''
  voucherDiscount.value = 0
  cashGiven.value = ''
  transferRef.value = ''
  isSplitMode.value = false
  resetSplitFields()
  ghiChu.value = ''
  receipt.value = null
  showReceipt.value = false
  activeHeldOrderId.value = null
  const cash = paymentByMa.value.TIEN_MAT
  if (cash) selectedPaymentId.value = cash.id
  void loadProducts()
  void loadHeldOrders()
  nextTick(() => searchInput.value?.focus())
}

function printReceipt() {
  document.body.classList.add('printing-receipt')
  nextTick(() => {
    window.print()
    window.addEventListener(
      'afterprint',
      () => document.body.classList.remove('printing-receipt'),
      { once: true },
    )
  })
}

function formatDateTime(value) {
  if (!value) return '—'
  return new Date(value).toLocaleString('vi-VN')
}

watch(selectedPaymentId, () => {
  cashGiven.value = ''
  transferRef.value = ''
})

watch(keyword, () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(loadProducts, 300)
})

onMounted(async () => {
  await Promise.all([loadMeta(), loadHeldOrders(), loadProducts()])
  await nextTick()
  searchInput.value?.focus()
})

onBeforeUnmount(() => {
  stopQrPolling()
})
</script>

<template>
  <div class="pos-page">
  <div class="pos-counter">
    <header class="pos-top">
      <div class="pos-search pos-search--top">
        <Icon icon="icon-park-outline:search" class="pos-search__icon" />
        <input
          ref="searchInput"
          v-model="keyword"
          type="search"
          placeholder="Tìm tên, mã SP, quét barcode…"
          @keydown.enter.prevent="onSearchEnter"
        />
        <span class="pos-search__hint">Enter</span>
      </div>
      <div class="pos-top__right">
        <button type="button" class="pos-top-btn pos-top-btn--held" @click="openHeldDrawer">
          Đơn chờ
          <span v-if="heldCount > 0" class="pos-badge">{{ heldCount }}</span>
        </button>
        <button type="button" class="pos-top-btn" @click="loadProducts">Làm mới</button>
      </div>
    </header>

    <div
      v-if="message"
      class="admin-alert pos-alert"
      :class="messageType === 'error' ? 'admin-alert-error' : 'admin-alert-success'"
    >
      {{ message }}
    </div>

    <div class="pos-main">
      <section class="pos-catalog" aria-label="Chọn sản phẩm">
        <div class="pos-chips">
          <button
            type="button"
            class="pos-chip"
            :class="{ 'is-on': categoryFilter == null }"
            @click="categoryFilter = null"
          >
            Tất cả
          </button>
          <button
            v-for="cat in categoryChips"
            :key="cat"
            type="button"
            class="pos-chip"
            :class="{ 'is-on': categoryFilter === cat }"
            @click="categoryFilter = cat"
          >
            {{ cat }}
          </button>
        </div>

        <div class="pos-grid">
          <div v-if="loading && !productsLoaded" class="pos-grid-loading">Đang tải sản phẩm...</div>
          <div v-else-if="productsLoaded && filteredProducts.length === 0" class="pos-grid-empty">
            {{ keyword.trim() ? 'Không tìm thấy sản phẩm phù hợp' : 'Không có sản phẩm đang bán' }}
          </div>
          <button
            v-for="item in filteredProducts"
            :key="item.idChiTietSanPham"
            type="button"
            class="pos-card"
            :class="{ 'pos-card--out': isOutOfStock(item) }"
            :disabled="isOutOfStock(item)"
            @click="addToCart(item)"
          >
            <span v-if="isOnSale(item) && saleLabel(item) && !isOutOfStock(item)" class="pos-card__tag">
              {{ saleLabel(item) }}
            </span>
            <span v-else-if="isOutOfStock(item)" class="pos-card__tag pos-card__tag--out">Hết</span>
            <span
              v-else-if="posExpiryBadge(item) === 'expired'"
              class="pos-card__tag pos-card__tag--warn"
            >Hết hạn</span>
            <span
              v-else-if="posExpiryBadge(item) === 'warning'"
              class="pos-card__tag pos-card__tag--warn"
            >Sắp HSD</span>
            <div class="pos-card__img">
              <img :src="productImageUrl(item.anhUrl)" :alt="item.tenSanPham" loading="lazy" />
            </div>
            <p class="pos-card__name">{{ item.tenSanPham }}</p>
            <p v-if="formatVariant(item)" class="pos-card__variant">{{ formatVariant(item) }}</p>
            <div class="pos-card__meta">
              <div v-if="isOnSale(item)" class="pos-card__prices">
                <p class="pos-card__price pos-card__price--original">{{ formatCurrency(item.giaGoc) }}</p>
                <p class="pos-card__price pos-card__price--sale">{{ formatCurrency(item.giaBan) }}</p>
              </div>
              <p v-else class="pos-card__price">{{ formatCurrency(item.giaBan) }}</p>
              <span
                class="pos-card__stock"
                :class="{ 'is-low': !isOutOfStock(item) && item.soLuongTon <= 10 }"
              >
                {{ isOutOfStock(item) ? '0' : item.soLuongTon }}
              </span>
            </div>
          </button>
        </div>
      </section>

      <aside class="pos-checkout" aria-label="Thanh toán">
        <div class="pos-customer">
          <template v-if="selectedCustomer">
            <div class="pos-customer__who">
              <div>
                <strong>{{ selectedCustomer.hoTen }}</strong>
                <span class="muted"> · {{ selectedCustomer.soDienThoai }}</span>
              </div>
              <button type="button" class="pos-pill" @click="clearCustomer">Bỏ chọn</button>
            </div>
            <p v-if="selectedCustomer.diemTichLuy != null" class="pos-customer__hint">
              Điểm tích lũy: {{ selectedCustomer.diemTichLuy }}
            </p>
          </template>
          <template v-else>
            <div class="pos-customer__row">
              <input
                v-model="customerSdt"
                type="text"
                placeholder="SĐT khách hàng"
                inputmode="numeric"
                maxlength="10"
                @input="customerSdt = normalizePhoneDigits(customerSdt)"
                @keyup.enter="findCustomer"
              />
              <button type="button" class="pos-icon-btn" title="Tìm" @click="findCustomer">⌕</button>
              <button type="button" class="pos-icon-btn pos-icon-btn--cream" title="Thêm khách" @click="openCreateCustomerModal">＋</button>
            </div>
            <div class="pos-customer__row">
              <input
                v-model="customerName"
                type="text"
                placeholder="Tên khách * (bắt buộc khi giữ đơn)"
                maxlength="100"
              />
            </div>
          </template>

          <div v-if="showQuickCreate && !selectedCustomer" class="pos-quick-inline">
            <p>Chưa có khách với SĐT này — tạo nhanh?</p>
            <input v-model="quickName" type="text" class="pos-input" placeholder="Họ tên *" />
            <button type="button" class="pos-btn-pay" style="height:36px;font-size:13px" @click="createQuickCustomer">
              Tạo nhanh
            </button>
          </div>
        </div>

        <div class="pos-cart">
          <div v-if="cart.length === 0" class="pos-cart__empty">Chưa có sản phẩm trong đơn</div>
          <div v-for="line in cart" :key="cartKey(line)" class="pos-line">
            <div class="pos-line__thumb">
              <img :src="productImageUrl(line.anhUrl)" :alt="line.tenSanPham" loading="lazy" />
            </div>
            <div>
              <div class="pos-line__name">{{ line.tenSanPham }}</div>
              <div class="pos-line__sub">
                {{ formatCurrency(line.giaBan) }}
                <template v-if="line.bienThe"> · {{ line.bienThe }}</template>
              </div>
              <div
                v-if="lineDisplayHsd(line)"
                class="pos-line__sub"
                :class="{
                  'pos-line__sub--warn': expiryTone(line) === 'warning',
                  'pos-line__sub--expired': expiryTone(line) === 'expired',
                }"
              >
                HSD: {{ formatDate(lineDisplayHsd(line)) }}
                <template v-if="line.phanBoLos?.length"> · thủ công</template>
                <template v-else-if="line.idLoHang && line.soLo"> · lô {{ line.soLo }}</template>
                <template v-else> · FEFO</template>
              </div>
              <p v-if="lineLotHint(line)" class="pos-line__sub">{{ lineLotHint(line) }}</p>
              <button type="button" class="pos-line__lot" @click="openLotPicker(line)">
                {{ lineLotLabel(line) }}
              </button>
            </div>
            <div class="pos-line__right">
              <div class="pos-line__sum">{{ formatCurrency(line.giaBan * line.soLuong) }}</div>
              <div class="pos-qty">
                <button type="button" :disabled="line.soLuong <= 1" @click="changeQty(line, -1)">−</button>
                <span>{{ line.soLuong }}</span>
                <button type="button" :disabled="line.soLuong >= line.soLuongTon" @click="changeQty(line, 1)">＋</button>
              </div>
              <button type="button" class="pos-line__rm" @click="removeLine(line)">Xóa</button>
            </div>
          </div>
        </div>

        <div class="pos-pay">
          <div class="pos-voucher">
            <input
              v-model="voucherCode"
              type="text"
              placeholder="Mã giảm giá"
              :disabled="voucherLoading || cart.length === 0"
              @keydown.enter.prevent="applyVoucher()"
            />
            <button
              type="button"
              class="pos-btn-ghost"
              :disabled="voucherLoading || cart.length === 0"
              @click="applyVoucher()"
            >
              Áp mã
            </button>
            <button
              type="button"
              class="pos-btn-ghost"
              :disabled="voucherLoading || cart.length === 0"
              @click="openVoucherModal"
            >
              Chọn
            </button>
          </div>
          <p v-if="appliedVoucher" class="pos-voucher-applied">
            Đã áp <strong>{{ appliedVoucher }}</strong>
            — giảm <strong>{{ formatCurrency(voucherDiscount) }}</strong>
            <button type="button" class="pos-voucher-clear" :disabled="voucherLoading" @click="clearVoucher">
              Bỏ mã
            </button>
          </p>

          <div class="pos-totals">
            <div class="pos-totals__row">
              <span>Tạm tính ({{ cartItemCount }} SP)</span>
              <strong>{{ formatCurrency(tongTienHang) }}</strong>
            </div>
            <div v-if="voucherDiscount > 0" class="pos-totals__row pos-totals__row--disc">
              <span>Giảm giá</span>
              <strong>−{{ formatCurrency(voucherDiscount) }}</strong>
            </div>
            <div class="pos-totals__due">
              <span>Tổng tiền</span>
              <strong>{{ formatCurrency(thanhTien) }}</strong>
            </div>
          </div>

          <div class="pos-methods">
            <button
              v-for="pt in paymentMethods"
              :key="pt.id"
              type="button"
              class="pos-method"
              :class="{ 'is-on': !isSplitMode && selectedPaymentId === pt.id }"
              @click="selectPayMethod(pt)"
            >
              {{ payMethodLabel(pt) }}
            </button>
            <button
              type="button"
              class="pos-method"
              :class="{ 'is-on': isSplitMode }"
              @click="selectSplitPay"
            >
              Kết hợp
            </button>
          </div>

          <div v-if="isSplitMode" class="pos-pay-extra">
            <div>
              <label>Tiền mặt</label>
              <input v-model="splitCashAmount" type="number" min="0" placeholder="0" />
              <p v-if="splitCashNum > 0 && splitCashNum >= thanhTien" class="pos-pay-msg pos-pay-msg--warn">
                Tiền mặt đã đủ — dùng Tiền mặt thường, hoặc nhập ít hơn để kết hợp.
              </p>
            </div>
            <div>
              <label>Chuyển khoản (tự tính)</label>
              <div class="pos-split-transfer-row">
                <input type="text" :value="formatCurrency(splitTransferNum)" readonly tabindex="-1" />
                <button
                  type="button"
                  class="pos-btn-ghost"
                  :disabled="splitTransferNum <= 0"
                  @click="openVietQrModal"
                >
                  Tạo QR
                </button>
              </div>
              <p v-if="splitTransferConfirmed" class="pos-pay-msg pos-pay-msg--ok">
                Đã xác nhận nhận chuyển khoản
              </p>
              <p v-else-if="splitTransferNum > 0" class="pos-pay-msg">
                Tạo QR → kiểm tra app ngân hàng → bấm “Đã nhận chuyển khoản”
              </p>
            </div>
            <div>
              <label>Mã giao dịch CK (tùy chọn)</label>
              <input v-model="splitTransferRef" type="text" placeholder="Mã GD / tham chiếu..." />
            </div>
            <div class="pos-split-summary">
              <div><span>Tiền mặt</span><strong>{{ formatCurrency(splitCashNum) }}</strong></div>
              <div><span>Chuyển khoản</span><strong>{{ formatCurrency(splitTransferNum) }}</strong></div>
              <div><span>Thành tiền</span><strong>{{ formatCurrency(thanhTien) }}</strong></div>
            </div>
          </div>

          <div v-else-if="isCash" class="pos-pay-extra">
            <div>
              <label>Tiền khách đưa</label>
              <input v-model="cashGiven" type="number" min="0" placeholder="0" />
            </div>
            <div class="pos-denoms">
              <button type="button" class="pos-denom" @click="addDenomination(50000)">+50k</button>
              <button type="button" class="pos-denom" @click="addDenomination(100000)">+100k</button>
              <button type="button" class="pos-denom" @click="addDenomination(200000)">+200k</button>
              <button type="button" class="pos-denom" @click="addDenomination(500000)">+500k</button>
              <button type="button" class="pos-denom pos-denom--accent" @click="fillExactCash">Đủ tiền</button>
              <button type="button" class="pos-denom" @click="clearCash">Xóa</button>
            </div>
            <p v-if="cashShortage > 0" class="pos-pay-msg pos-pay-msg--warn">
              Khách chưa đưa đủ (thiếu {{ formatCurrency(cashShortage) }})
            </p>
            <p v-else-if="cashGiven" class="pos-pay-msg">
              Tiền thối: <strong>{{ formatCurrency(tienThua) }}</strong>
            </p>
          </div>

          <div v-else-if="isManualTransfer" class="pos-pay-extra">
            <div>
              <label>Mã giao dịch (tùy chọn)</label>
              <input v-model="transferRef" type="text" placeholder="Mã GD / tham chiếu..." />
            </div>
          </div>

          <div class="pos-actions">
            <button
              type="button"
              class="pos-btn-hold"
              :disabled="cart.length === 0 || holding"
              @click="holdCurrentOrder"
            >
              {{ holding ? 'Đang giữ...' : 'Giữ đơn' }}
            </button>
            <button
              type="button"
              class="pos-btn-pay"
              :disabled="!canCheckout || paying"
              @click="checkout"
            >
              {{ checkoutButtonLabel }}
            </button>
          </div>
          <p v-if="activeHeldOrderId" class="pos-held-note">
            Đang tiếp tục đơn chờ — thanh toán sẽ hoàn tất đơn này
          </p>
        </div>
      </aside>
    </div>
  </div>

    <!-- Khay đơn chờ -->
    <div v-if="showHeldDrawer" class="pos-drawer-overlay" @click="closeHeldDrawer" />
    <aside v-if="showHeldDrawer" class="pos-drawer">
      <div class="pos-drawer__head">
        <h2 class="pos-drawer__title">Đơn chờ</h2>
        <button type="button" class="admin-icon-btn" title="Đóng" @click="closeHeldDrawer">
          <Icon icon="mdi:close" width="20" />
        </button>
      </div>
      <div class="pos-drawer__body">
        <div v-if="visibleHeldOrders.length === 0" class="pos-cart-empty">
          Chưa có đơn chờ
        </div>
        <div
          v-for="(order, idx) in visibleHeldOrders"
          :key="order.id"
          class="pos-held-card"
        >
          <div class="pos-held-card__top">
            <span class="pos-held-card__customer">
              #{{ idx + 1 }} · {{ order.tenKhachHang || 'Khách lẻ' }}
            </span>
          </div>
          <p class="pos-held-card__meta">
            <span v-if="order.soDienThoai">{{ order.soDienThoai }} · </span>
            {{ order.soMatHang }} món · giữ {{ formatHeldAgo(order.ngayTao) }}
          </p>
          <p class="pos-held-card__amount">{{ formatCurrency(order.thanhTien) }}</p>
          <div class="pos-held-card__actions">
            <button type="button" class="soleil-btn-primary" @click="resumeHeldOrder(order)">
              Tiếp tục
            </button>
            <button type="button" class="pos-held-cancel-btn" @click="cancelHeldOrder(order)">
              Hủy
            </button>
          </div>
        </div>
      </div>
    </aside>

    <!-- Modal tạo nhanh khách (Teleport + z-index cao hơn overlay) -->
    <Teleport to="body">
      <div
        v-if="showCreateCustomerModal"
        class="pos-quick-customer-overlay"
        @click.self="showCreateCustomerModal = false"
      >
        <div class="pos-quick-customer-modal" role="dialog" aria-modal="true" @click.stop>
          <div class="pos-quick-customer-modal__head">
            <h3>Thêm nhanh khách hàng</h3>
            <button type="button" class="admin-icon-btn" @click="showCreateCustomerModal = false">
              <Icon icon="mdi:close" width="20" />
            </button>
          </div>
          <label class="pos-field">
            <span>Tên *</span>
            <input
              v-model="quickName"
              class="admin-input"
              placeholder="Họ tên khách"
              autofocus
            />
          </label>
          <label class="pos-field">
            <span>SĐT *</span>
            <input
              v-model="quickSdt"
              class="admin-input"
              placeholder="VD: 0912345678"
              inputmode="numeric"
              maxlength="10"
              @input="quickSdt = normalizePhoneDigits(quickSdt)"
            />
          </label>
          <label class="pos-field">
            <span>Email (tuỳ chọn)</span>
            <input v-model="quickEmail" class="admin-input" type="email" placeholder="email@..." />
          </label>
          <button type="button" class="admin-btn admin-btn-primary w-full" @click="createQuickCustomer">
            Lưu &amp; gán vào hóa đơn
          </button>
        </div>
      </div>
    </Teleport>

    <!-- Modal biên lai (Teleport ra body để in đúng) -->
    <Teleport to="body">
      <div v-if="showReceipt && receipt" class="pos-receipt-overlay" @click.self="showReceipt = false">
        <div class="pos-receipt-modal">
          <div id="pos-receipt-print" class="pos-receipt-print">
          <div class="pos-receipt-print__brand">SUNOVA</div>
          <div class="pos-receipt-print__meta">
            {{ receipt.maHoaDon }}<br />
            {{ formatDateTime(receipt.ngayTao) }}
          </div>
          <p v-if="receipt.tenNhanVien" class="text-xs mb-1">NV: {{ receipt.tenNhanVien }}</p>
          <p class="text-xs mb-2">KH: {{ receipt.tenKhachHang }}</p>
          <table class="pos-receipt-print__table">
            <thead>
              <tr>
                <th>Hàng</th>
                <th>SL</th>
                <th>ĐG</th>
                <th>TT</th>
              </tr>
            </thead>
            <tbody>
              <template v-for="(item, idx) in receipt.items" :key="idx">
                <tr class="pos-receipt-print__item-name">
                  <td colspan="4">
                    {{ item.tenSanPham }}
                    <span v-if="item.bienThe" class="pos-receipt-print__variant">{{ item.bienThe }}</span>
                  </td>
                </tr>
                <tr class="pos-receipt-print__item-meta">
                  <td></td>
                  <td>{{ item.soLuong }}</td>
                  <td>{{ formatCurrency(item.donGia) }}</td>
                  <td>{{ formatCurrency(item.thanhTien) }}</td>
                </tr>
              </template>
            </tbody>
          </table>
          <div class="pos-receipt-print__summary">
            <div class="flex justify-between"><span>Tổng</span><span>{{ formatCurrency(receipt.tongTien) }}</span></div>
            <div v-if="receipt.tienGiamGia > 0" class="flex justify-between">
              <span>Giảm</span><span>-{{ formatCurrency(receipt.tienGiamGia) }}</span>
            </div>
            <div class="flex justify-between font-bold mt-1">
              <span>Thành tiền</span><span>{{ formatCurrency(receipt.thanhTien) }}</span>
            </div>
            <div v-if="receipt.soTienKhachDua != null" class="flex justify-between mt-1">
              <span>Khách đưa</span><span>{{ formatCurrency(receipt.soTienKhachDua) }}</span>
            </div>
            <div v-if="receipt.tienThua != null" class="flex justify-between">
              <span>Tiền thối</span><span>{{ formatCurrency(receipt.tienThua) }}</span>
            </div>
            <template v-if="receipt.danhSachThanhToan && receipt.danhSachThanhToan.length > 1">
              <div class="mt-1">Thanh toán:</div>
              <div
                v-for="(tt, ttIdx) in receipt.danhSachThanhToan"
                :key="ttIdx"
                class="flex justify-between"
              >
                <span>{{ tt.tenPhuongThucThanhToan }}</span>
                <span>{{ formatCurrency(tt.soTien) }}</span>
              </div>
            </template>
            <div v-else class="mt-1">PTTT: {{ receipt.tenPhuongThucThanhToan }}</div>
          </div>
          <p class="pos-receipt-print__thanks">Cảm ơn quý khách!</p>
        </div>
        <div class="pos-receipt-actions">
          <button type="button" class="soleil-btn-outline flex-1" @click="printReceipt">
            In hóa đơn
          </button>
          <button type="button" class="soleil-btn-primary flex-1" @click="resetSale">
            Bán đơn mới
          </button>
        </div>
      </div>
    </div>
    </Teleport>

    <!-- Modal QR thanh toán VNPay -->
    <Teleport to="body">
      <div v-if="showQrModal" class="pos-qr-overlay" @click.self="closeQrModal">
        <div class="pos-qr-modal">
          <div class="pos-qr-modal__head">
            <h2 class="pos-qr-modal__title">Thanh toán QR VNPay</h2>
            <button type="button" class="admin-icon-btn" title="Đóng" @click="closeQrModal">
              <Icon icon="mdi:close" width="20" />
            </button>
          </div>

          <div class="pos-qr-modal__body">
            <p class="pos-qr-modal__order">{{ qrOrderCode }}</p>
            <p class="pos-qr-modal__amount">{{ formatCurrency(qrAmount) }}</p>
            <p class="pos-qr-modal__status" :class="{ 'pos-qr-modal__status--ok': qrStatus === 'THANH_CONG' }">
              {{ qrStatusLabel }}
            </p>

            <div v-if="qrPaymentUrl && qrStatus === 'CHO_THANH_TOAN'" class="pos-qr-modal__code">
              <NQrCode :value="qrPaymentUrl" :size="240" padding="12" />
            </div>

            <p v-if="qrTransactionRef" class="pos-qr-modal__ref">
              Mã GD: <strong>{{ qrTransactionRef }}</strong>
            </p>
            <p class="pos-qr-modal__hint">
              Khách mở app ngân hàng / VNPay và quét mã. Sau khi khách thanh toán xong, bấm
              <strong>Thanh toán</strong> để hoàn tất giao dịch.
            </p>
          </div>

          <div class="pos-qr-modal__actions">
            <button
              v-if="qrStatus === 'CHO_THANH_TOAN'"
              type="button"
              class="soleil-btn-outline flex-1"
              :disabled="qrCompleting"
              @click="cancelQrPayment"
            >
              Hủy thanh toán
            </button>
            <button
              v-if="qrStatus === 'CHO_THANH_TOAN'"
              type="button"
              class="soleil-btn-primary flex-1"
              :disabled="qrCompleting"
              @click="completeQrPayment"
            >
              {{ qrCompleting ? 'Đang hoàn tất...' : 'Thanh toán' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <PosVoucherModal
      v-model:visible="showVoucherModal"
      :selected-code="appliedVoucher"
      :subtotal="tongTienHang"
      :customer-id="selectedCustomer?.id ?? null"
      @select="onVoucherModalSelect"
    />

    <Teleport to="body">
      <div v-if="lotModalOpen" class="modal-overlay" @click.self="closeLotPicker">
        <div class="modal-panel" style="max-width: 640px">
          <div class="px-5 py-4 border-b flex justify-between items-center" style="border-color: var(--admin-border)">
            <div>
              <h2 class="text-lg font-semibold m-0">Chọn lô</h2>
              <p class="text-sm text-[var(--admin-muted)] m-0 mt-1">
                {{ lotModalLine?.sku }} — gõ số lượng lấy từ từng lô; số lượng dòng sẽ tự cập nhật theo tổng
              </p>
            </div>
            <button type="button" class="admin-btn admin-btn-default !px-2" @click="closeLotPicker">✕</button>
          </div>
          <div class="p-5">
            <button
              type="button"
              class="admin-btn admin-btn-default w-full mb-3"
              @click="clearLotSelection"
            >
              Dùng FEFO (tự động)
            </button>
            <div
              class="mb-3 rounded-lg px-3 py-2 text-sm"
              :style="{
                background: lotDraftCanConfirm ? 'rgba(122, 140, 110, 0.12)' : 'rgba(201, 169, 110, 0.12)',
                color: lotDraftCanConfirm ? '#5a6b52' : '#8c6b4a',
              }"
            >
              Tổng: <strong>{{ lotDraftTotal }}</strong> sản phẩm
              <span class="opacity-80"> — sẽ gán vào số lượng dòng khi xác nhận</span>
            </div>
            <div v-if="lotLoading" class="text-center py-8 text-[var(--admin-muted)]">Đang tải lô...</div>
            <div v-else-if="lotOptions.length === 0" class="text-center py-8 text-[var(--admin-muted)]">
              Không có lô còn hàng
            </div>
            <div v-else class="overflow-x-auto">
              <table class="admin-table admin-table--striped w-full">
                <thead>
                  <tr>
                    <th>Số lô</th>
                    <th>Ngày nhập</th>
                    <th>HSD</th>
                    <th>Còn</th>
                    <th>Lấy</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="lot in lotOptions" :key="lot.id">
                    <td class="font-medium">
                      {{ lot.soLo }}
                      <span
                        v-if="lot.sapHetHan"
                        class="ml-1 inline-block text-[10px] px-2 py-0.5 rounded-full bg-amber-100 text-amber-800"
                      >Cận hạn</span>
                    </td>
                    <td>{{ formatDate(lot.ngayNhap) }}</td>
                    <td>{{ lot.hanSuDung ? formatDate(lot.hanSuDung) : '—' }}</td>
                    <td>{{ lot.soLuongCon }}</td>
                    <td style="min-width: 96px">
                      <input
                        type="number"
                        class="admin-input !py-1"
                        min="0"
                        :max="lot.soLuongCon"
                        :value="lotQtyDraft[lot.id] ?? 0"
                        @input="setLotDraftQty(lot.id, $event.target.value)"
                      />
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div class="flex justify-end gap-2 mt-4">
              <button type="button" class="admin-btn admin-btn-default" @click="closeLotPicker">
                Hủy
              </button>
              <button
                type="button"
                class="admin-btn admin-btn-primary"
                :disabled="!lotDraftCanConfirm || lotLoading"
                @click="confirmLotSelection"
              >
                Xác nhận
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Modal VietQR chuyển khoản (thanh toán kết hợp) -->
    <Teleport to="body">
      <div v-if="showVietQrModal" class="pos-qr-overlay" @click.self="cancelVietQrModal">
        <div class="pos-qr-modal">
          <div class="pos-qr-modal__head">
            <h2 class="pos-qr-modal__title">QR chuyển khoản (VietQR)</h2>
            <button type="button" class="admin-icon-btn" title="Đóng" @click="cancelVietQrModal">
              <Icon icon="mdi:close" width="20" />
            </button>
          </div>

          <div class="pos-qr-modal__body">
            <p class="pos-qr-modal__amount">{{ formatCurrency(splitTransferNum) }}</p>
            <p class="pos-qr-modal__status">Quét QR — kiểm tra tiền về trên app ngân hàng</p>

            <div v-if="vietQrImageUrl" class="pos-qr-modal__code">
              <img
                :src="vietQrImageUrl"
                alt="VietQR chuyển khoản"
                class="pos-vietqr-img"
                width="240"
                height="240"
              />
            </div>

            <p class="pos-qr-modal__hint">
              Số tiền QR = phần chuyển khoản còn thiếu.
              Bấm <strong>Đã nhận chuyển khoản</strong> sẽ tạo hóa đơn (tiền mặt + CK).
            </p>
          </div>

          <div class="pos-qr-modal__actions">
            <button
              type="button"
              class="soleil-btn-outline flex-1"
              @click="cancelVietQrModal"
            >
              Hủy
            </button>
            <button
              type="button"
              class="soleil-btn-primary flex-1"
              @click="confirmVietQrReceived"
            >
              Đã nhận chuyển khoản
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>
