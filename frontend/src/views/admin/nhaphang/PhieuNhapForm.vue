<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import {
  createNhaCungCap,
  getNhaCungCapList,
  getPhieuNhapDetail,
  hoanThanhPhieuNhap,
  luuTamPhieuNhap,
  timBienTheNhapHang,
  timSanPhamNhapHang,
  updatePhieuNhap,
} from '@/api/nhapHangApi'
import { toast } from '@/composables/useToast'
import { confirm } from '@/composables/useConfirm'
import { formatApiError } from '@/utils/apiError'
import { productImageUrl } from '@/utils/productImage'
import { PHONE_VN_REGEX, normalizePhoneDigits } from '@/utils/phone'

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const phieuId = ref(null)
const maPhieu = ref('(tự sinh khi lưu)')
const trangThai = ref('PHIEU_TAM')
const readonly = computed(() => trangThai.value !== 'PHIEU_TAM')

const STATUS_LABEL = {
  PHIEU_TAM: 'Phiếu tạm',
  DA_NHAP: 'Đã nhập',
  DA_HUY: 'Đã hủy',
}

function statusTone(st) {
  if (st === 'DA_NHAP') return 'ok'
  if (st === 'DA_HUY') return 'muted'
  return 'draft'
}

const lines = ref([])
const idNhaCungCap = ref(null)
const soHoaDonDauVao = ref('')
const giamGia = ref(0)
const ghiChu = ref('')
const ngayNhap = ref(todayLocal())
const nccOptions = ref([])

/** Lỗi inline theo dòng: { [idChiTiet]: { soLuong?, donGia?, hanSuDung? } } */
const lineErrors = ref({})
const nccFieldError = ref('')
const ngayNhapError = ref('')
const nccFormErrors = ref({ ten: '', soDienThoai: '', email: '' })

const dirtyBaseline = ref('')
const suppressLeaveGuard = ref(false)

const showAddModal = ref(false)
const modalStep = ref(1)
const productQuery = ref('')
const productLoading = ref(false)
const productResults = ref([])
/** @type {import('vue').Ref<Record<number, any>>} */
const selectedProductsMap = ref({})
/** @type {import('vue').Ref<Record<number, { soLuong: number, donGia: number }>>} */
const variantDraft = ref({})
let productSearchTimer = null

/** Gợi ý biến thể phẳng trên ô tìm nhanh (không qua modal). */
const suggestOpen = ref(false)
const suggestLoading = ref(false)
const suggestItems = ref([])
const suggestIndex = ref(-1)
const highlightLineId = ref(null)
let suggestSearchTimer = null
let highlightClearTimer = null
let suggestBlurTimer = null

const showNccModal = ref(false)
const nccForm = ref({ ten: '', soDienThoai: '', email: '', diaChi: '', ghiChu: '' })
const nccSaving = ref(false)

const maxNgayNhap = todayLocal()

const HSD_PRESETS = [
  { key: '6m', label: '+6 tháng', months: 6 },
  { key: '1y', label: '+1 năm', months: 12 },
  { key: '2y', label: '+2 năm', months: 24 },
  { key: '3y', label: '+3 năm', months: 36 },
]

const tongTien = computed(() =>
  lines.value.reduce((sum, row) => sum + Number(row.soLuong || 0) * Number(row.donGia || 0), 0),
)
const canTraNcc = computed(() => Math.max(0, tongTien.value - Number(giamGia.value || 0)))

function todayLocal() {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function formatYmd(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

/** Cộng tháng giữ ngày trong tháng (clamp cuối tháng nếu cần). */
function addMonthsYmd(ymd, months) {
  const base = ymd || todayLocal()
  const [y, m, d] = base.split('-').map(Number)
  if (!y || !m || !d) return todayLocal()
  const dt = new Date(y, m - 1, d)
  const day = dt.getDate()
  dt.setMonth(dt.getMonth() + months)
  if (dt.getDate() < day) dt.setDate(0)
  return formatYmd(dt)
}

function baseNgayNhap() {
  return ngayNhap.value || todayLocal()
}

function applyHsdPreset(row, preset) {
  if (readonly.value || !row || !preset) return
  const hsd = addMonthsYmd(baseNgayNhap(), preset.months)
  if (hsd <= baseNgayNhap()) {
    toast('Hạn sử dụng phải sau ngày nhập', 'warn')
    return
  }
  row.hanSuDung = hsd
  row.hsdPreset = preset.key
}

function onHsdManualInput(row) {
  if (!row) return
  row.hsdPreset = null
  if (row.hanSuDung && row.hanSuDung <= baseNgayNhap()) {
    toast('Hạn sử dụng phải sau ngày nhập', 'warn')
  } else if (row.hanSuDung && row.hanSuDung < todayLocal()) {
    toast('HSD đang trong quá khứ — kiểm tra lại', 'warn')
  }
}

function reapplyHsdPresets() {
  const base = baseNgayNhap()
  for (const row of lines.value) {
    if (!row.hsdPreset) continue
    const preset = HSD_PRESETS.find((p) => p.key === row.hsdPreset)
    if (!preset) continue
    row.hanSuDung = addMonthsYmd(base, preset.months)
  }
}

function formatMoney(v) {
  return Number(v || 0).toLocaleString('vi-VN') + ' ₫'
}

function lineThanhTien(row) {
  return Number(row.soLuong || 0) * Number(row.donGia || 0)
}

/** Giá nhập > giá bán → bán lỗ (vẫn cho lưu). */
function isLossLine(row) {
  const nhap = Number(row?.donGia || 0)
  const ban = Number(row?.giaBan || 0)
  return ban > 0 && nhap > ban
}

function lineProfitPerUnit(row) {
  return Number(row?.giaBan || 0) - Number(row?.donGia || 0)
}

function lossLineCount() {
  return lines.value.filter((row) => isLossLine(row)).length
}

function buildPayload() {
  return {
    idNhaCungCap: idNhaCungCap.value || null,
    soHoaDonDauVao: soHoaDonDauVao.value || null,
    ngayNhap: ngayNhap.value || null,
    giamGia: Number(giamGia.value || 0),
    ghiChu: ghiChu.value || null,
    chiTiets: lines.value.map((row) => ({
      idChiTietSanPham: row.idChiTietSanPham,
      soLuong: Number(row.soLuong),
      donGia: Number(row.donGia || 0),
      hanSuDung: row.hanSuDung || null,
      soLo: row.soLo || null,
    })),
  }
}

async function loadNcc() {
  const res = await getNhaCungCapList('', true)
  nccOptions.value = (res.data || []).filter((n) => n.trangThai !== false)
}

async function loadDetail(id) {
  loading.value = true
  try {
    const res = await getPhieuNhapDetail(id)
    const p = res.data
    phieuId.value = p.id
    maPhieu.value = p.maPhieuNhap
    trangThai.value = p.trangThai
    idNhaCungCap.value = p.idNhaCungCap
    soHoaDonDauVao.value = p.soHoaDonDauVao || ''
    giamGia.value = Number(p.giamGia || 0)
    ghiChu.value = p.ghiChu || ''
    ngayNhap.value = p.ngayTao ? String(p.ngayTao).slice(0, 10) : todayLocal()
    lines.value = (p.chiTiets || []).map((d) => ({
      idChiTietSanPham: d.idChiTietSanPham,
      sku: d.sku,
      tenSanPham: d.tenSanPham,
      tenMauSac: d.tenMauSac,
      dungTichMl: d.dungTichMl,
      soLuong: d.soLuong,
      donGia: Number(d.donGia || 0),
      giaBan: Number(d.giaBan || 0),
      hanSuDung: d.hanSuDung || '',
      soLo: d.soLo || '',
      hsdPreset: null,
    }))
    markClean()
  } catch (e) {
    toast(formatApiError(e, 'Không tải được phiếu'), 'error')
    router.push('/admin/nhap-hang')
  } finally {
    loading.value = false
  }
}

function formFingerprint() {
  return JSON.stringify({
    idNhaCungCap: idNhaCungCap.value || null,
    soHoaDonDauVao: soHoaDonDauVao.value || '',
    giamGia: Number(giamGia.value || 0),
    ghiChu: ghiChu.value || '',
    ngayNhap: ngayNhap.value || '',
    lines: lines.value.map((r) => ({
      id: r.idChiTietSanPham,
      soLuong: Number(r.soLuong || 0),
      donGia: Number(r.donGia || 0),
      hanSuDung: r.hanSuDung || '',
    })),
  })
}

function markClean() {
  dirtyBaseline.value = formFingerprint()
}

const isDirty = computed(() => {
  if (readonly.value || suppressLeaveGuard.value) return false
  if (!dirtyBaseline.value) return false
  return formFingerprint() !== dirtyBaseline.value
})

function clearLineError(id, field) {
  const cur = lineErrors.value[id]
  if (!cur?.[field]) return
  const next = { ...cur }
  delete next[field]
  const map = { ...lineErrors.value }
  if (Object.keys(next).length) map[id] = next
  else delete map[id]
  lineErrors.value = map
}

function clearFieldErrors() {
  lineErrors.value = {}
  nccFieldError.value = ''
  ngayNhapError.value = ''
}

async function scrollToFirstError() {
  await nextTick()
  const el = document.querySelector('.pn-line__input--error, .pn-control--error')
  el?.scrollIntoView?.({ behavior: 'smooth', block: 'center' })
  el?.focus?.()
}

async function removeLine(index) {
  const row = lines.value[index]
  if (!row) return
  const ok = await confirm({
    title: 'Xóa dòng',
    message: 'Xóa sản phẩm này khỏi phiếu?',
    confirmText: 'Xóa',
    danger: true,
  })
  if (!ok) return
  const id = row.idChiTietSanPham
  lines.value.splice(index, 1)
  if (lineErrors.value[id]) {
    const map = { ...lineErrors.value }
    delete map[id]
    lineErrors.value = map
  }
  toast('Đã xóa dòng khỏi phiếu', 'success')
}

const selectedProductCount = computed(() => Object.keys(selectedProductsMap.value).length)

const selectedProducts = computed(() =>
  Object.values(selectedProductsMap.value).sort((a, b) =>
    String(a.tenSanPham || '').localeCompare(String(b.tenSanPham || ''), 'vi'),
  ),
)

const step2Summary = computed(() => {
  const entries = Object.entries(variantDraft.value)
  let variantCount = 0
  let qtySum = 0
  let amountSum = 0
  for (const [, draft] of entries) {
    const qty = Number(draft.soLuong || 0)
    const price = Number(draft.donGia || 0)
    if (qty <= 0) continue
    variantCount += 1
    qtySum += qty
    amountSum += qty * price
  }
  return { variantCount, qtySum, amountSum }
})

const canGoStep2 = computed(() => selectedProductCount.value > 0)
const canAddToPhieu = computed(
  () => step2Summary.value.variantCount > 0 && step2Summary.value.qtySum > 0,
)

function isProductSelected(id) {
  return Boolean(selectedProductsMap.value[id])
}

function variantLabel(v) {
  const parts = []
  if (v.tenMauSac) parts.push(v.tenMauSac)
  if (v.dungTichMl) parts.push(`${v.dungTichMl}ml`)
  return parts.join(' / ') || 'Mặc định'
}

function isVariantSelected(idChiTietSanPham) {
  return Boolean(variantDraft.value[idChiTietSanPham])
}

function allVariantsSelected(product) {
  const list = product?.bienThes || []
  return list.length > 0 && list.every((v) => isVariantSelected(v.idChiTietSanPham))
}

function normalizeSkuKey(value) {
  return String(value || '').trim().toUpperCase()
}

function buildLineFromVariant(v, qty = 1) {
  return {
    idChiTietSanPham: v.idChiTietSanPham,
    sku: v.sku,
    tenSanPham: v.tenSanPham,
    tenMauSac: v.tenMauSac,
    dungTichMl: v.dungTichMl,
    soLuong: qty,
    donGia: 0,
    giaBan: Number(v.giaBan || 0),
    hanSuDung: '',
    soLo: '',
    hsdPreset: null,
  }
}

function variantSuggestLabel(v) {
  const parts = []
  if (v.tenMauSac) parts.push(v.tenMauSac)
  if (v.dungTichMl != null && v.dungTichMl !== '') parts.push(`${v.dungTichMl}ml`)
  const ton = v.soLuongTon != null ? `tồn ${v.soLuongTon}` : null
  if (ton) parts.push(ton)
  return parts.length ? parts.join(' · ') : '—'
}

async function focusLineQty(idChiTiet) {
  await nextTick()
  const article = document.querySelector(`.pn-line[data-line-id="${idChiTiet}"]`)
  article?.scrollIntoView?.({ behavior: 'smooth', block: 'center' })
  highlightLineId.value = idChiTiet
  clearTimeout(highlightClearTimer)
  highlightClearTimer = setTimeout(() => {
    if (highlightLineId.value === idChiTiet) highlightLineId.value = null
  }, 2200)
  const qtyInput = article?.querySelector('input[data-field="soLuong"]')
  qtyInput?.focus?.()
  qtyInput?.select?.()
}

function addOrFocusVariant(v) {
  if (!v?.idChiTietSanPham || readonly.value) return
  const id = v.idChiTietSanPham
  const existing = lines.value.find((l) => l.idChiTietSanPham === id)
  if (existing) {
    toast(`SKU ${v.sku} đã có trên phiếu`, 'warn')
    closeSuggest()
    void focusLineQty(id)
    return
  }
  lines.value.push(buildLineFromVariant(v, 1))
  toast(`Đã thêm ${v.sku}`, 'success')
  productQuery.value = ''
  closeSuggest()
  void focusLineQty(id)
}

function closeSuggest() {
  suggestOpen.value = false
  suggestIndex.value = -1
  suggestItems.value = []
}

async function searchVariantsSuggest(keyword = productQuery.value) {
  const q = String(keyword || '').trim()
  if (!q) {
    closeSuggest()
    return
  }
  suggestLoading.value = true
  suggestOpen.value = true
  try {
    const res = await timBienTheNhapHang(q, 0, 30)
    suggestItems.value = res.data || []
    suggestOpen.value = suggestItems.value.length > 0
    suggestIndex.value = suggestItems.value.length ? 0 : -1
  } catch (e) {
    toast(formatApiError(e, 'Không tìm được biến thể'), 'error')
    suggestOpen.value = false
  } finally {
    suggestLoading.value = false
  }
}

function scheduleSuggestSearch() {
  if (suggestSearchTimer) clearTimeout(suggestSearchTimer)
  suggestSearchTimer = setTimeout(() => searchVariantsSuggest(productQuery.value), 280)
}

async function onSearchEnter() {
  if (readonly.value) return
  const q = productQuery.value.trim()
  if (!q) {
    await openAddModal()
    return
  }

  if (suggestOpen.value && suggestIndex.value >= 0 && suggestItems.value[suggestIndex.value]) {
    addOrFocusVariant(suggestItems.value[suggestIndex.value])
    return
  }

  suggestLoading.value = true
  try {
    const res = await timBienTheNhapHang(q, 0, 50)
    const list = res.data || []
    const key = normalizeSkuKey(q)
    const exact = list.find((v) => normalizeSkuKey(v.sku) === key)
    if (exact) {
      addOrFocusVariant(exact)
      return
    }
    if (!list.length) {
      toast(`Không tìm thấy SKU «${q}»`, 'warn')
      suggestItems.value = []
      suggestOpen.value = false
      suggestIndex.value = -1
      return
    }
    suggestItems.value = list
    suggestOpen.value = true
    suggestIndex.value = 0
  } catch (e) {
    toast(formatApiError(e, 'Không tìm được sản phẩm'), 'error')
  } finally {
    suggestLoading.value = false
  }
}

function onSearchKeydown(e) {
  if (e.key === 'Escape') {
    if (suggestOpen.value) {
      e.preventDefault()
      suggestOpen.value = false
      suggestIndex.value = -1
    }
    return
  }

  const hasSuggest = suggestOpen.value && suggestItems.value.length > 0

  if (e.key === 'ArrowDown' && hasSuggest) {
    e.preventDefault()
    suggestIndex.value = Math.min(suggestIndex.value + 1, suggestItems.value.length - 1)
    return
  }
  if (e.key === 'ArrowUp' && hasSuggest) {
    e.preventDefault()
    suggestIndex.value = Math.max(suggestIndex.value - 1, 0)
    return
  }
  if (e.key === 'Enter') {
    e.preventDefault()
    void onSearchEnter()
  }
}

function onSearchFocus() {
  if (suggestItems.value.length) suggestOpen.value = true
}

function onSearchBlur() {
  clearTimeout(suggestBlurTimer)
  suggestBlurTimer = setTimeout(() => {
    suggestOpen.value = false
  }, 160)
}

async function searchProducts(keyword = productQuery.value) {
  productLoading.value = true
  try {
    const res = await timSanPhamNhapHang(String(keyword || '').trim(), 0, 40)
    productResults.value = res.data || []
  } catch (e) {
    toast(formatApiError(e, 'Không tìm được sản phẩm'), 'error')
  } finally {
    productLoading.value = false
  }
}

function scheduleProductSearch() {
  if (productSearchTimer) clearTimeout(productSearchTimer)
  productSearchTimer = setTimeout(() => searchProducts(productQuery.value), 350)
}

function resetAddModalState() {
  modalStep.value = 1
  selectedProductsMap.value = {}
  variantDraft.value = {}
  productResults.value = []
}

async function openAddModal() {
  if (readonly.value) return
  resetAddModalState()
  showAddModal.value = true
  await searchProducts(productQuery.value)
}

function closeAddModal() {
  showAddModal.value = false
  resetAddModalState()
}

function toggleProduct(product) {
  const id = product.idSanPham
  if (selectedProductsMap.value[id]) {
    const next = { ...selectedProductsMap.value }
    delete next[id]
    selectedProductsMap.value = next
    const nextDraft = { ...variantDraft.value }
    for (const v of product.bienThes || []) {
      delete nextDraft[v.idChiTietSanPham]
    }
    variantDraft.value = nextDraft
  } else {
    selectedProductsMap.value = { ...selectedProductsMap.value, [id]: product }
  }
}

function selectAllVisibleProducts() {
  const next = { ...selectedProductsMap.value }
  for (const p of productResults.value) {
    next[p.idSanPham] = p
  }
  selectedProductsMap.value = next
}

function goToStep2() {
  if (!canGoStep2.value) return
  // bỏ draft thuộc sản phẩm đã bỏ chọn
  const keepIds = new Set(
    selectedProducts.value.flatMap((p) => (p.bienThes || []).map((v) => v.idChiTietSanPham)),
  )
  const nextDraft = {}
  for (const [id, draft] of Object.entries(variantDraft.value)) {
    if (keepIds.has(Number(id))) nextDraft[id] = draft
  }
  variantDraft.value = nextDraft
  modalStep.value = 2
}

function backToStep1() {
  modalStep.value = 1
}

function setVariantSelected(product, variant, on) {
  const id = variant.idChiTietSanPham
  if (on) {
    if (variantDraft.value[id]) return
    variantDraft.value = {
      ...variantDraft.value,
      [id]: {
        soLuong: 1,
        donGia: 0,
        idChiTietSanPham: id,
        sku: variant.sku,
        tenSanPham: product.tenSanPham,
        tenMauSac: variant.tenMauSac,
        dungTichMl: variant.dungTichMl,
        giaBan: Number(variant.giaBan || 0),
      },
    }
  } else {
    const next = { ...variantDraft.value }
    delete next[id]
    variantDraft.value = next
  }
}

function toggleVariant(product, variant) {
  setVariantSelected(product, variant, !isVariantSelected(variant.idChiTietSanPham))
}

function toggleAllVariants(product, on) {
  for (const v of product.bienThes || []) {
    setVariantSelected(product, v, on)
  }
}

function updateVariantQty(idChiTietSanPham, value) {
  const draft = variantDraft.value[idChiTietSanPham]
  if (!draft) return
  const qty = Math.max(0, Number(value) || 0)
  variantDraft.value = {
    ...variantDraft.value,
    [idChiTietSanPham]: { ...draft, soLuong: qty },
  }
}

function updateVariantPrice(idChiTietSanPham, value) {
  const draft = variantDraft.value[idChiTietSanPham]
  if (!draft) return
  const price = Math.max(0, Number(value) || 0)
  variantDraft.value = {
    ...variantDraft.value,
    [idChiTietSanPham]: { ...draft, donGia: price },
  }
}

function onVariantQtyInput(product, variant, value) {
  const qty = Math.max(0, Number(value) || 0)
  if (qty > 0 && !isVariantSelected(variant.idChiTietSanPham)) {
    setVariantSelected(product, variant, true)
  }
  if (isVariantSelected(variant.idChiTietSanPham)) {
    updateVariantQty(variant.idChiTietSanPham, qty)
  }
}

function addSelectedToPhieu() {
  if (!canAddToPhieu.value) return
  let added = 0
  let skipped = 0
  for (const draft of Object.values(variantDraft.value)) {
    const qty = Number(draft.soLuong || 0)
    if (qty <= 0) continue
    if (lines.value.some((l) => l.idChiTietSanPham === draft.idChiTietSanPham)) {
      skipped += 1
      continue
    }
    lines.value.push({
      idChiTietSanPham: draft.idChiTietSanPham,
      sku: draft.sku,
      tenSanPham: draft.tenSanPham,
      tenMauSac: draft.tenMauSac,
      dungTichMl: draft.dungTichMl,
      soLuong: qty,
      donGia: Number(draft.donGia || 0),
      giaBan: Number(draft.giaBan || 0),
      hanSuDung: '',
      soLo: '',
      hsdPreset: null,
    })
    added += 1
  }
  if (added === 0) {
    toast(skipped ? 'Các SKU đã có trên phiếu' : 'Chọn ít nhất 1 biến thể có số lượng', 'warn')
    return
  }
  if (skipped) toast(`Đã thêm ${added} dòng, bỏ qua ${skipped} SKU trùng`, 'warn')
  else toast(`Đã thêm ${added} dòng vào phiếu`, 'success')
  closeAddModal()
  productQuery.value = ''
}

async function saveNcc() {
  const errs = { ten: '', soDienThoai: '', email: '' }
  if (!nccForm.value.ten?.trim()) {
    errs.ten = 'Nhập tên nhà cung cấp'
  }
  const sdtRaw = (nccForm.value.soDienThoai || '').trim()
  if (sdtRaw) {
    const digits = normalizePhoneDigits(sdtRaw)
    if (!PHONE_VN_REGEX.test(digits)) {
      errs.soDienThoai = 'SĐT phải gồm 10 số, bắt đầu bằng 0 (VN)'
    }
  }
  const emailRaw = (nccForm.value.email || '').trim()
  if (emailRaw && !EMAIL_RE.test(emailRaw)) {
    errs.email = 'Email không đúng định dạng'
  }
  nccFormErrors.value = errs
  if (errs.ten || errs.soDienThoai || errs.email) {
    toast('Kiểm tra lại thông tin nhà cung cấp', 'warn')
    await nextTick()
    document.querySelector('.pn-modal .pn-control--error')?.focus?.()
    return
  }
  nccSaving.value = true
  try {
    const sdtDigits = normalizePhoneDigits(nccForm.value.soDienThoai || '')
    const res = await createNhaCungCap({
      ...nccForm.value,
      ten: nccForm.value.ten.trim(),
      soDienThoai: sdtDigits || null,
      email: emailRaw || null,
    })
    await loadNcc()
    idNhaCungCap.value = res.data.id
    nccFieldError.value = ''
    showNccModal.value = false
    nccForm.value = { ten: '', soDienThoai: '', email: '', diaChi: '', ghiChu: '' }
    nccFormErrors.value = { ten: '', soDienThoai: '', email: '' }
    toast('Đã thêm nhà cung cấp', 'success')
  } catch (e) {
    toast(formatApiError(e, 'Không tạo được NCC'), 'error')
  } finally {
    nccSaving.value = false
  }
}

function validateBeforeSave(requireHsd) {
  clearFieldErrors()
  const errs = {}
  let issueCount = 0

  if (!lines.value.length) {
    toast('Thêm ít nhất 1 dòng hàng', 'warn')
    return false
  }
  if (!ngayNhap.value) {
    ngayNhapError.value = 'Chọn ngày nhập'
    issueCount += 1
  } else if (ngayNhap.value > todayLocal()) {
    ngayNhapError.value = 'Ngày nhập không được lớn hơn ngày hiện tại'
    issueCount += 1
  }

  if (!idNhaCungCap.value) {
    nccFieldError.value = 'Vui lòng chọn nhà cung cấp'
    issueCount += 1
  }

  for (const row of lines.value) {
    const e = {}
    if (!row.soLuong || Number(row.soLuong) <= 0) {
      e.soLuong = 'Số lượng phải > 0'
      issueCount += 1
    }
    if (!row.donGia || Number(row.donGia) <= 0) {
      e.donGia = 'Đơn giá phải > 0'
      issueCount += 1
    }
    if (requireHsd && !row.hanSuDung) {
      e.hanSuDung = 'Cần nhập hạn sử dụng'
      issueCount += 1
    } else if (row.hanSuDung && ngayNhap.value && row.hanSuDung <= ngayNhap.value) {
      e.hanSuDung = 'HSD phải sau ngày nhập'
      issueCount += 1
    }
    if (Object.keys(e).length) errs[row.idChiTietSanPham] = e
  }

  lineErrors.value = errs

  if (issueCount > 0) {
    toast(`Có ${issueCount} lỗi cần sửa trên phiếu`, 'warn')
    scrollToFirstError()
    return false
  }

  const pastHsd = lines.value.find((row) => row.hanSuDung && row.hanSuDung < todayLocal())
  if (pastHsd) {
    toast(`SKU ${pastHsd.sku}: HSD đang trong quá khứ — kiểm tra lại`, 'warn')
  }
  return true
}

async function onLuuTam() {
  if (readonly.value) return
  if (!validateBeforeSave(false)) return
  saving.value = true
  try {
    const payload = buildPayload()
    if (phieuId.value) {
      const res = await updatePhieuNhap(phieuId.value, payload)
      applySaved(res.data)
      markClean()
      toast('Đã cập nhật phiếu tạm', 'success')
    } else {
      const res = await luuTamPhieuNhap(payload)
      applySaved(res.data)
      markClean()
      toast('Đã lưu phiếu tạm', 'success')
      router.replace(`/admin/nhap-hang/${res.data.id}`)
    }
  } catch (e) {
    toast(formatApiError(e, 'Không lưu được phiếu'), 'error')
  } finally {
    saving.value = false
  }
}

async function onHoanThanh() {
  if (readonly.value) return
  if (!validateBeforeSave(true)) return

  const lossCount = lossLineCount()
  if (lossCount > 0) {
    const okLoss = await confirm({
      title: 'Cảnh báo bán lỗ',
      message: `Có ${lossCount} sản phẩm giá nhập cao hơn giá bán — bán ra sẽ lỗ. Vẫn tiếp tục hoàn thành phiếu?`,
      confirmText: 'Vẫn hoàn thành',
    })
    if (!okLoss) return
  }

  const ok = await confirm({
    title: 'Hoàn thành phiếu nhập',
    message: 'Hoàn thành phiếu sẽ sinh lô và cộng tồn. Không hoàn tác được. Tiếp tục?',
    confirmText: 'Hoàn thành',
  })
  if (!ok) return
  saving.value = true
  try {
    const payload = buildPayload()
    let id = phieuId.value
    if (id) {
      await updatePhieuNhap(id, payload)
    } else {
      const res = await luuTamPhieuNhap(payload)
      id = res.data.id
      phieuId.value = id
    }
    await hoanThanhPhieuNhap(id)
    suppressLeaveGuard.value = true
    markClean()
    toast('Đã nhập kho thành công', 'success')
    router.push('/admin/nhap-hang')
  } catch (e) {
    toast(formatApiError(e, 'Không hoàn thành được phiếu'), 'error')
  } finally {
    saving.value = false
  }
}

function applySaved(p) {
  phieuId.value = p.id
  maPhieu.value = p.maPhieuNhap
  trangThai.value = p.trangThai
}

watch(ngayNhap, () => {
  reapplyHsdPresets()
})

watch(productQuery, () => {
  if (showAddModal.value && modalStep.value === 1) {
    scheduleProductSearch()
    return
  }
  if (!showAddModal.value && !readonly.value) {
    scheduleSuggestSearch()
  }
})

onMounted(async () => {
  await loadNcc()
  const id = route.params.id
  if (id && id !== 'tao') {
    await loadDetail(Number(id))
  } else {
    markClean()
  }
  window.addEventListener('beforeunload', onBeforeUnload)
})

onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', onBeforeUnload)
  if (productSearchTimer) clearTimeout(productSearchTimer)
  if (suggestSearchTimer) clearTimeout(suggestSearchTimer)
  clearTimeout(highlightClearTimer)
  clearTimeout(suggestBlurTimer)
})

function onBeforeUnload(e) {
  if (!isDirty.value) return
  e.preventDefault()
  e.returnValue = ''
}

onBeforeRouteLeave(async () => {
  if (suppressLeaveGuard.value || !isDirty.value) return true
  const ok = await confirm({
    title: 'Rời trang',
    message: 'Phiếu chưa lưu, rời trang sẽ mất dữ liệu?',
    confirmText: 'Rời trang',
    danger: true,
  })
  return ok
})
</script>

<template>
  <div class="pn-form">
    <div class="pn-form__head">
      <div class="pn-form__title-block">
        <button type="button" class="pn-back" @click="router.push('/admin/nhap-hang')">
          <Icon icon="icon-park-outline:left" width="16" />
          Danh sách phiếu nhập
        </button>
        <div class="pn-form__title-row">
          <h1 class="pn-form__title">
            {{ phieuId ? maPhieu : 'Tạo phiếu nhập' }}
          </h1>
          <span
            v-if="phieuId"
            class="pn-badge"
            :class="`pn-badge--${statusTone(trangThai)}`"
          >
            {{ STATUS_LABEL[trangThai] || trangThai }}
          </span>
        </div>
        <p class="pn-form__hint">
          <template v-if="readonly">Phiếu đã khóa — chỉ xem, không chỉnh sửa.</template>
          <template v-else>Thêm hàng bên trái, điền thông tin NCC bên phải, rồi lưu tạm hoặc hoàn thành.</template>
        </p>
      </div>
    </div>

    <div v-if="loading" class="pn-loading">Đang tải phiếu…</div>

    <div v-else class="pn-form__grid">
      <!-- LEFT: lines -->
      <section class="pn-panel pn-left">
        <div class="pn-panel__head">
          <div>
            <h2 class="pn-panel__title">Dòng hàng</h2>
            <p class="pn-panel__sub">{{ lines.length }} sản phẩm trên phiếu</p>
          </div>
          <button
            type="button"
            class="soleil-btn-primary"
            :disabled="readonly"
            @click="openAddModal"
          >
            <Icon icon="icon-park-outline:plus" width="15" />
            Thêm hàng
          </button>
        </div>

        <div v-if="!readonly" class="pn-search-row">
          <div class="pn-search">
            <Icon icon="icon-park-outline:search" class="pn-search__icon" />
            <input
              v-model="productQuery"
              class="pn-search__input"
              placeholder="SKU hoặc tên SP — Enter thêm thẳng biến thể…"
              autocomplete="off"
              @keydown="onSearchKeydown"
              @focus="onSearchFocus"
              @blur="onSearchBlur"
            />
            <div
              v-if="suggestOpen && (suggestLoading || suggestItems.length)"
              class="pn-suggest"
              role="listbox"
            >
              <div v-if="suggestLoading" class="pn-suggest__status">Đang tìm biến thể…</div>
              <button
                v-for="(v, i) in suggestItems"
                :key="v.idChiTietSanPham"
                type="button"
                class="pn-suggest__item"
                :class="{ 'is-active': i === suggestIndex }"
                role="option"
                :aria-selected="i === suggestIndex"
                @mousedown.prevent="addOrFocusVariant(v)"
              >
                <span class="pn-suggest__sku">{{ v.sku }}</span>
                <span class="pn-suggest__name">{{ v.tenSanPham }}</span>
                <span class="pn-suggest__meta">{{ variantSuggestLabel(v) }}</span>
              </button>
            </div>
          </div>
          <button type="button" class="soleil-btn-outline" title="Chọn nhiều SP / biến thể" @click="openAddModal">
            Tìm
          </button>
        </div>

        <div v-if="!lines.length" class="pn-empty-lines">
          <Icon icon="icon-park-outline:inbox" width="28" class="pn-empty-lines__icon" />
          <p>Chưa có dòng hàng</p>
          <span>Gõ SKU + Enter, hoặc bấm «Tìm» để chọn nhiều biến thể.</span>
        </div>

        <div v-else class="pn-lines">
          <article
            v-for="(row, idx) in lines"
            :key="row.idChiTietSanPham"
            class="pn-line"
            :class="{ 'pn-line--flash': highlightLineId === row.idChiTietSanPham }"
            :data-line-id="row.idChiTietSanPham"
          >
            <div class="pn-line__index">{{ idx + 1 }}</div>

            <div class="pn-line__product">
              <div class="pn-line__sku">{{ row.sku }}</div>
              <div class="pn-line__name">{{ row.tenSanPham }}</div>
              <div class="pn-line__meta">
                <span v-if="row.tenMauSac">{{ row.tenMauSac }}</span>
                <span v-if="row.dungTichMl">{{ row.dungTichMl }}ml</span>
                <span class="pn-line__ref">Giá bán {{ formatMoney(row.giaBan) }}</span>
              </div>
            </div>

            <div class="pn-line__fields">
              <label class="pn-line__field">
                <span>Số lượng</span>
                <input
                  v-model.number="row.soLuong"
                  type="number"
                  min="1"
                  class="pn-line__input"
                  data-field="soLuong"
                  :class="{ 'pn-line__input--error': lineErrors[row.idChiTietSanPham]?.soLuong }"
                  :disabled="readonly"
                  @input="clearLineError(row.idChiTietSanPham, 'soLuong')"
                />
                <em
                  v-if="lineErrors[row.idChiTietSanPham]?.soLuong"
                  class="pn-line__field-error"
                >{{ lineErrors[row.idChiTietSanPham].soLuong }}</em>
              </label>
              <label class="pn-line__field">
                <span>Đơn giá nhập</span>
                <input
                  v-model.number="row.donGia"
                  type="number"
                  min="0"
                  class="pn-line__input"
                  :class="{
                    'pn-line__input--loss': isLossLine(row),
                    'pn-line__input--error': lineErrors[row.idChiTietSanPham]?.donGia,
                  }"
                  :disabled="readonly"
                  placeholder="0"
                  @input="clearLineError(row.idChiTietSanPham, 'donGia')"
                />
                <em
                  v-if="lineErrors[row.idChiTietSanPham]?.donGia"
                  class="pn-line__field-error"
                >{{ lineErrors[row.idChiTietSanPham].donGia }}</em>
                <p v-else-if="isLossLine(row)" class="pn-line__loss-warn">
                  ⚠ Giá nhập cao hơn giá bán ({{ formatMoney(row.giaBan) }}) — bán ra sẽ lỗ
                </p>
                <p
                  v-if="Number(row.donGia) > 0 && Number(row.giaBan) > 0"
                  class="pn-line__profit"
                  :class="{ 'pn-line__profit--loss': isLossLine(row) }"
                >
                  {{ isLossLine(row) ? 'Lỗ/sp' : 'Lợi nhuận/sp' }}:
                  {{ formatMoney(Math.abs(lineProfitPerUnit(row))) }}
                  <template v-if="isLossLine(row)"> (âm)</template>
                </p>
              </label>
              <label class="pn-line__field pn-line__field--hsd">
                <span>Hạn sử dụng</span>
                <input
                  v-model="row.hanSuDung"
                  type="date"
                  class="pn-line__input"
                  :class="{ 'pn-line__input--error': lineErrors[row.idChiTietSanPham]?.hanSuDung }"
                  :min="ngayNhap || undefined"
                  :disabled="readonly"
                  @change="onHsdManualInput(row); clearLineError(row.idChiTietSanPham, 'hanSuDung')"
                />
                <em
                  v-if="lineErrors[row.idChiTietSanPham]?.hanSuDung"
                  class="pn-line__field-error"
                >{{ lineErrors[row.idChiTietSanPham].hanSuDung }}</em>
                <div v-if="!readonly" class="pn-hsd-presets" role="group" aria-label="Gợi ý hạn sử dụng">
                  <button
                    v-for="preset in HSD_PRESETS"
                    :key="preset.key"
                    type="button"
                    class="pn-hsd-preset"
                    :class="{ 'is-on': row.hsdPreset === preset.key }"
                    @click="applyHsdPreset(row, preset); clearLineError(row.idChiTietSanPham, 'hanSuDung')"
                  >
                    {{ preset.label }}
                  </button>
                </div>
              </label>
              <div class="pn-line__field pn-line__field--total">
                <span>Thành tiền</span>
                <strong>{{ formatMoney(lineThanhTien(row)) }}</strong>
              </div>
            </div>

            <button
              v-if="!readonly"
              type="button"
              class="pn-line__remove"
              title="Xóa dòng"
              @click="removeLine(idx)"
            >
              <Icon icon="icon-park-outline:delete" width="16" />
            </button>
          </article>
        </div>
      </section>

      <!-- RIGHT: meta -->
      <aside class="pn-panel pn-right">
        <div class="pn-panel__head pn-panel__head--compact">
          <h2 class="pn-panel__title">Thông tin phiếu</h2>
        </div>

        <label class="pn-field">
          <span>Nhà cung cấp *</span>
          <div class="pn-ncc-row">
            <select
              v-model="idNhaCungCap"
              class="pn-control"
              :class="{ 'pn-control--error': nccFieldError }"
              :disabled="readonly"
              @change="nccFieldError = ''"
            >
              <option :value="null">— Chọn NCC —</option>
              <option v-for="n in nccOptions" :key="n.id" :value="n.id">
                {{ n.ma }} — {{ n.ten }}
              </option>
            </select>
            <button
              type="button"
              class="soleil-btn-outline pn-icon-btn"
              :disabled="readonly"
              title="Thêm NCC"
              @click="showNccModal = true"
            >
              <Icon icon="icon-park-outline:plus" width="15" />
            </button>
          </div>
          <em v-if="nccFieldError" class="pn-field-error">{{ nccFieldError }}</em>
        </label>

        <div class="pn-field-grid">
          <label class="pn-field">
            <span>Mã phiếu</span>
            <input class="pn-control" :value="maPhieu" readonly />
          </label>
          <label class="pn-field">
            <span>Ngày nhập</span>
            <input
              v-model="ngayNhap"
              type="date"
              class="pn-control"
              :class="{ 'pn-control--error': ngayNhapError }"
              :max="maxNgayNhap"
              :disabled="readonly"
              @change="ngayNhapError = ''"
            />
            <em v-if="ngayNhapError" class="pn-field-error">{{ ngayNhapError }}</em>
          </label>
        </div>

        <label class="pn-field">
          <span>Số HĐ đầu vào</span>
          <input
            v-model="soHoaDonDauVao"
            class="pn-control"
            :disabled="readonly"
            placeholder="Tuỳ chọn"
          />
        </label>

        <div class="pn-totals">
          <div class="pn-totals__row">
            <span>Tổng tiền hàng</span>
            <strong>{{ formatMoney(tongTien) }}</strong>
          </div>
          <label class="pn-field pn-field--inline">
            <span>Giảm giá</span>
            <input
              v-model.number="giamGia"
              type="number"
              min="0"
              class="pn-control"
              :disabled="readonly"
            />
          </label>
          <div class="pn-totals__row pn-totals__row--emph">
            <span>Cần trả NCC</span>
            <strong>{{ formatMoney(canTraNcc) }}</strong>
          </div>
        </div>

        <label class="pn-field">
          <span>Ghi chú</span>
          <textarea
            v-model="ghiChu"
            class="pn-control pn-control--area"
            rows="3"
            :disabled="readonly"
            placeholder="Ghi chú nội bộ…"
          />
        </label>

        <div v-if="!readonly" class="pn-right__actions">
          <button
            type="button"
            class="soleil-btn-outline"
            :disabled="saving"
            @click="onLuuTam"
          >
            <Icon icon="icon-park-outline:save-one" width="15" />
            Lưu tạm
          </button>
          <button
            type="button"
            class="soleil-btn-primary"
            :disabled="saving"
            @click="onHoanThanh"
          >
            <Icon icon="icon-park-outline:check-one" width="15" />
            Hoàn thành nhập kho
          </button>
        </div>
      </aside>
    </div>

    <!-- Thêm hàng: modal 2 bước -->
    <div v-if="showAddModal" class="pn-modal" @click.self="closeAddModal">
      <div class="pn-add-modal">
        <!-- Bước 1: chọn sản phẩm -->
        <template v-if="modalStep === 1">
          <div class="pn-add-modal__head">
            <div class="pn-add-modal__title-block">
              <span class="pn-step-tag">Bước 1/2</span>
              <h3>Chọn sản phẩm</h3>
              <p>Tìm và chọn một hoặc nhiều sản phẩm muốn nhập trong lô này</p>
            </div>
            <button type="button" class="pn-add-modal__icon-btn" @click="closeAddModal">
              <Icon icon="icon-park-outline:close" width="16" />
            </button>
          </div>

          <div class="pn-add-modal__search">
            <div class="pn-search">
              <Icon icon="icon-park-outline:search" class="pn-search__icon" />
              <input
                v-model="productQuery"
                class="pn-search__input"
                placeholder="Tìm theo tên sản phẩm hoặc mã SKU…"
                autofocus
              />
            </div>
            <div class="pn-add-modal__search-meta">
              <span>{{ productResults.length }} sản phẩm</span>
              <button
                type="button"
                class="pn-link-btn"
                :disabled="!productResults.length"
                @click="selectAllVisibleProducts"
              >
                Chọn tất cả kết quả
              </button>
            </div>
          </div>

          <div class="pn-add-modal__body">
            <p v-if="productLoading" class="pn-modal__empty">Đang tìm…</p>
            <p v-else-if="!productResults.length" class="pn-modal__empty">Không tìm thấy sản phẩm.</p>
            <button
              v-for="p in productResults"
              :key="p.idSanPham"
              type="button"
              class="pn-prod-row"
              :class="{ 'pn-prod-row--checked': isProductSelected(p.idSanPham) }"
              @click="toggleProduct(p)"
            >
              <input
                type="checkbox"
                :checked="isProductSelected(p.idSanPham)"
                tabindex="-1"
                @click.stop
                @change="toggleProduct(p)"
              />
              <div class="pn-prod-row__thumb">
                <img :src="productImageUrl(p.anhUrl)" :alt="p.tenSanPham" loading="lazy" />
              </div>
              <div class="pn-prod-row__info">
                <div class="pn-prod-row__name">{{ p.tenSanPham }}</div>
                <div class="pn-prod-row__sub">
                  {{ p.soBienThe || (p.bienThes || []).length }} biến thể
                  <template v-if="p.maSanPham"> · {{ p.maSanPham }}</template>
                </div>
              </div>
            </button>
          </div>

          <div class="pn-add-modal__foot">
            <div class="pn-add-modal__summary">
              Đã chọn <b>{{ selectedProductCount }}</b> sản phẩm
            </div>
            <div class="pn-add-modal__actions">
              <button type="button" class="soleil-btn-outline" @click="closeAddModal">Hủy</button>
              <button
                type="button"
                class="soleil-btn-primary"
                :disabled="!canGoStep2"
                @click="goToStep2"
              >
                Tiếp tục
              </button>
            </div>
          </div>
        </template>

        <!-- Bước 2: chọn biến thể + SL + giá nhập -->
        <template v-else>
          <div class="pn-add-modal__head">
            <button type="button" class="pn-add-modal__icon-btn" @click="backToStep1">
              <Icon icon="icon-park-outline:left" width="16" />
            </button>
            <div class="pn-add-modal__title-block">
              <span class="pn-step-tag">Bước 2/2</span>
              <h3>Nhập số lượng biến thể</h3>
              <p>Chọn biến thể và nhập số lượng, giá nhập cho từng sản phẩm đã chọn</p>
            </div>
            <button type="button" class="pn-add-modal__icon-btn" @click="closeAddModal">
              <Icon icon="icon-park-outline:close" width="16" />
            </button>
          </div>

          <div class="pn-add-modal__body pn-add-modal__body--qty">
            <div
              v-for="p in selectedProducts"
              :key="p.idSanPham"
              class="pn-qty-group"
            >
              <div class="pn-qty-group__head">
                <div class="pn-qty-group__thumb">
                  <img :src="productImageUrl(p.anhUrl)" :alt="p.tenSanPham" loading="lazy" />
                </div>
                <div class="pn-qty-group__name">{{ p.tenSanPham }}</div>
                <div v-if="p.maSanPham" class="pn-qty-group__code">{{ p.maSanPham }}</div>
              </div>
              <table class="pn-qty-table">
                <thead>
                  <tr>
                    <th class="pn-qty-table__check">
                      <input
                        type="checkbox"
                        :checked="allVariantsSelected(p)"
                        @change="toggleAllVariants(p, $event.target.checked)"
                      />
                    </th>
                    <th>Biến thể</th>
                    <th class="num">Tồn kho</th>
                    <th class="num pn-qty-table__price">Giá nhập</th>
                    <th class="num pn-qty-table__qty">Số lượng</th>
                    <th class="num pn-qty-table__amount">Thành tiền</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="v in p.bienThes || []"
                    :key="v.idChiTietSanPham"
                    :class="{ 'is-selected': isVariantSelected(v.idChiTietSanPham) }"
                  >
                    <td class="pn-qty-table__check">
                      <input
                        type="checkbox"
                        :checked="isVariantSelected(v.idChiTietSanPham)"
                        @change="toggleVariant(p, v)"
                      />
                    </td>
                    <td>
                      <div class="pn-variant-cell">
                        <div class="pn-variant-cell__thumb">
                          <img :src="productImageUrl(p.anhUrl)" :alt="variantLabel(v)" loading="lazy" />
                        </div>
                        <div>
                          <div class="pn-variant-cell__name">{{ variantLabel(v) }}</div>
                          <div class="pn-variant-cell__sku">{{ v.sku }}</div>
                        </div>
                      </div>
                    </td>
                    <td
                      class="num"
                      :class="{ 'stock-low': (v.soLuongTon ?? 0) <= 2 }"
                    >
                      {{ v.soLuongTon ?? 0 }}
                    </td>
                    <td class="num pn-qty-table__price">
                      <input
                        type="number"
                        min="0"
                        step="1000"
                        class="pn-qty-input"
                        :disabled="!isVariantSelected(v.idChiTietSanPham)"
                        :value="variantDraft[v.idChiTietSanPham]?.donGia ?? 0"
                        @input="updateVariantPrice(v.idChiTietSanPham, $event.target.value)"
                      />
                    </td>
                    <td class="num pn-qty-table__qty">
                      <input
                        type="number"
                        min="0"
                        step="1"
                        class="pn-qty-input"
                        placeholder="0"
                        :disabled="!isVariantSelected(v.idChiTietSanPham)"
                        :value="variantDraft[v.idChiTietSanPham]?.soLuong || ''"
                        @input="onVariantQtyInput(p, v, $event.target.value)"
                      />
                    </td>
                    <td class="num pn-qty-table__amount">
                      {{
                        formatMoney(
                          Number(variantDraft[v.idChiTietSanPham]?.soLuong || 0) *
                            Number(variantDraft[v.idChiTietSanPham]?.donGia || 0),
                        )
                      }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div class="pn-add-modal__foot">
            <div class="pn-add-modal__summary pn-add-modal__summary--multi">
              <span>Đã chọn: <b>{{ step2Summary.variantCount }}</b> biến thể</span>
              <span>Tổng SL: <b>{{ step2Summary.qtySum }}</b></span>
              <span>Tạm tính: <b>{{ formatMoney(step2Summary.amountSum) }}</b></span>
            </div>
            <div class="pn-add-modal__actions">
              <button type="button" class="soleil-btn-outline" @click="closeAddModal">Hủy</button>
              <button
                type="button"
                class="soleil-btn-primary"
                :disabled="!canAddToPhieu"
                @click="addSelectedToPhieu"
              >
                Thêm vào phiếu nhập
              </button>
            </div>
          </div>
        </template>
      </div>
    </div>

    <!-- NCC modal -->
    <div v-if="showNccModal" class="pn-modal" @click.self="showNccModal = false">
      <div class="pn-modal__panel pn-modal__panel--sm">
        <div class="pn-modal__head">
          <div>
            <h3>Thêm nhà cung cấp</h3>
            <p>Tạo nhanh NCC để gắn vào phiếu</p>
          </div>
          <button type="button" class="soleil-btn-outline pn-icon-btn" @click="showNccModal = false">
            <Icon icon="icon-park-outline:close" width="15" />
          </button>
        </div>
        <label class="pn-field">
          <span>Tên *</span>
          <input
            v-model="nccForm.ten"
            class="pn-control"
            :class="{ 'pn-control--error': nccFormErrors.ten }"
            @input="nccFormErrors.ten = ''"
          />
          <em v-if="nccFormErrors.ten" class="pn-field-error">{{ nccFormErrors.ten }}</em>
        </label>
        <label class="pn-field">
          <span>SĐT</span>
          <input
            v-model="nccForm.soDienThoai"
            class="pn-control"
            :class="{ 'pn-control--error': nccFormErrors.soDienThoai }"
            placeholder="0xxxxxxxxx"
            @input="nccFormErrors.soDienThoai = ''"
          />
          <em v-if="nccFormErrors.soDienThoai" class="pn-field-error">{{ nccFormErrors.soDienThoai }}</em>
        </label>
        <label class="pn-field">
          <span>Email</span>
          <input
            v-model="nccForm.email"
            class="pn-control"
            :class="{ 'pn-control--error': nccFormErrors.email }"
            @input="nccFormErrors.email = ''"
          />
          <em v-if="nccFormErrors.email" class="pn-field-error">{{ nccFormErrors.email }}</em>
        </label>
        <label class="pn-field">
          <span>Địa chỉ</span>
          <input v-model="nccForm.diaChi" class="pn-control" />
        </label>
        <label class="pn-field">
          <span>Ghi chú</span>
          <input v-model="nccForm.ghiChu" class="pn-control" />
        </label>
        <button
          type="button"
          class="soleil-btn-primary"
          :disabled="nccSaving"
          @click="saveNcc"
        >
          Lưu NCC
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.pn-form {
  --pn-ink: #1a120c;
  --pn-muted: #5c4f42;
  --pn-line: #c9b8a4;
  --pn-line-strong: #a89278;
  --pn-surface: #ffffff;
  --pn-mist: #f3ebe1;
  --pn-accent: #0f4c52;
  --pn-ok: #14532d;
  --pn-ok-bg: #dcfce7;
  --pn-draft: #9a3412;
  --pn-draft-bg: #ffedd5;
  --pn-cancel: #3f3f46;
  --pn-cancel-bg: #e4e4e7;

  display: flex;
  flex-direction: column;
  gap: 1rem;
  color: var(--pn-ink);
}

.pn-back {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  border: none;
  background: none;
  color: var(--pn-ink);
  cursor: pointer;
  padding: 0;
  margin-bottom: 0.5rem;
  font-size: 0.8125rem;
  font-weight: 600;
}

.pn-back:hover {
  color: var(--pn-accent);
}

.pn-form__title-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.pn-form__title {
  margin: 0;
  font-size: 1.4rem;
  font-weight: 800;
  letter-spacing: 0.01em;
  color: var(--pn-ink);
}

.pn-form__hint {
  margin: 0.4rem 0 0;
  color: var(--pn-muted);
  font-size: 0.875rem;
  line-height: 1.45;
}

.pn-badge {
  display: inline-flex;
  align-items: center;
  padding: 0.3rem 0.7rem;
  border-radius: 3px;
  border: 1px solid transparent;
  font-size: 11.5px;
  font-weight: 800;
  letter-spacing: 0.02em;
}

.pn-badge--draft {
  background: var(--pn-draft-bg);
  border-color: #fdba74;
  color: var(--pn-draft);
}

.pn-badge--ok {
  background: var(--pn-ok-bg);
  border-color: #86efac;
  color: var(--pn-ok);
}

.pn-badge--muted {
  background: var(--pn-cancel-bg);
  border-color: #a1a1aa;
  color: var(--pn-cancel);
}

.pn-loading {
  padding: 3rem 1rem;
  text-align: center;
  color: var(--pn-muted);
  background: var(--pn-surface);
  border: 1px solid var(--pn-line);
  border-radius: 12px;
}

.pn-form__grid {
  display: grid;
  grid-template-columns: minmax(0, 1.75fr) minmax(300px, 0.85fr);
  gap: 1rem;
  align-items: start;
}

.pn-panel {
  background: var(--pn-surface);
  border: 1px solid var(--pn-line-strong);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 2px rgba(26, 18, 12, 0.05);
}

.pn-panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  flex-wrap: wrap;
  padding: 1rem 1.15rem;
  border-bottom: 1px solid var(--pn-line);
  background: #efe4d4;
}

.pn-panel__head--compact {
  padding: 0.9rem 1.15rem;
}

.pn-panel__title {
  margin: 0;
  font-size: 0.95rem;
  font-weight: 800;
  color: var(--pn-ink);
}

.pn-panel__sub {
  margin: 0.2rem 0 0;
  font-size: 12px;
  font-weight: 600;
  color: var(--pn-muted);
}

.pn-left {
  padding-bottom: 1rem;
}

.pn-search-row {
  display: flex;
  gap: 0.5rem;
  padding: 1rem 1.15rem 0;
}

.pn-search {
  position: relative;
  flex: 1;
  min-width: 0;
}

.pn-search__icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--pn-muted);
  font-size: 16px;
  pointer-events: none;
}

.pn-search__input {
  width: 100%;
  border: 1px solid var(--pn-line-strong);
  border-radius: 8px;
  padding: 0.65rem 0.85rem 0.65rem 2.35rem;
  font-size: 0.875rem;
  font-weight: 500;
  background: #fff;
  color: var(--pn-ink);
  outline: none;
}

.pn-search__input:focus {
  border-color: #8f7349;
  background: #fff;
  box-shadow: 0 0 0 2px rgba(143, 115, 73, 0.18);
}

.pn-suggest {
  position: absolute;
  left: 0;
  right: 0;
  top: calc(100% + 4px);
  z-index: 40;
  max-height: 280px;
  overflow-y: auto;
  background: #fff;
  border: 1px solid var(--pn-line-strong, #a89278);
  border-radius: 10px;
  box-shadow: 0 12px 28px rgba(15, 26, 28, 0.14);
  padding: 0.35rem;
  scrollbar-width: thin;
  scrollbar-color: #e0d3be transparent;
}

.pn-suggest__status {
  padding: 0.65rem 0.75rem;
  font-size: 0.8125rem;
  color: var(--pn-muted, #5c4f42);
}

.pn-suggest__item {
  display: grid;
  grid-template-columns: minmax(7rem, auto) 1fr;
  grid-template-rows: auto auto;
  column-gap: 0.65rem;
  row-gap: 0.1rem;
  width: 100%;
  text-align: left;
  border: 0;
  background: transparent;
  border-radius: 8px;
  padding: 0.55rem 0.65rem;
  cursor: pointer;
  font-family: inherit;
}

.pn-suggest__item:hover,
.pn-suggest__item.is-active {
  background: #f5efe6;
}

.pn-suggest__sku {
  grid-row: 1 / 3;
  align-self: center;
  font-size: 0.75rem;
  font-weight: 800;
  color: #6b4f2a;
  font-family: ui-monospace, 'Cascadia Mono', monospace;
  word-break: break-all;
}

.pn-suggest__name {
  font-size: 0.8125rem;
  font-weight: 700;
  color: var(--pn-ink, #1a120c);
}

.pn-suggest__meta {
  font-size: 0.75rem;
  font-weight: 500;
  color: var(--pn-muted, #5c4f42);
}

.pn-line--flash {
  outline: 2px solid #c4a574;
  outline-offset: 1px;
  background: #fffaf3;
  transition: background 0.35s ease, outline-color 0.35s ease;
}

.pn-empty-lines {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.35rem;
  margin: 1.25rem 1.15rem;
  padding: 2.25rem 1rem;
  border: 1px dashed var(--pn-line-strong);
  border-radius: 10px;
  text-align: center;
  color: var(--pn-muted);
  background: #faf6f0;
}

.pn-empty-lines__icon {
  opacity: 0.45;
  margin-bottom: 0.25rem;
}

.pn-empty-lines p {
  margin: 0;
  font-weight: 700;
  color: var(--pn-ink);
}

.pn-empty-lines span {
  font-size: 0.8125rem;
}

.pn-lines {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding: 1rem 1.15rem 0;
}

.pn-line {
  display: grid;
  grid-template-columns: 36px minmax(0, 1.2fr) minmax(0, 1.6fr) auto;
  gap: 0.85rem;
  align-items: start;
  padding: 1rem;
  border: 1px solid var(--pn-line-strong);
  border-radius: 10px;
  background: #fff;
}

.pn-line:hover {
  border-color: #8f7349;
  background: #fffdf9;
}

.pn-line__index {
  width: 28px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: #efe4d4;
  border: 1px solid var(--pn-line-strong);
  font-size: 12px;
  font-weight: 800;
  color: var(--pn-ink);
  margin-top: 0.15rem;
}

.pn-line__sku {
  font-family: ui-monospace, 'Cascadia Mono', monospace;
  font-size: 12px;
  font-weight: 700;
  color: var(--pn-accent);
}

.pn-line__name {
  margin-top: 0.2rem;
  font-size: 0.9375rem;
  font-weight: 700;
  line-height: 1.35;
  color: var(--pn-ink);
}

.pn-line__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem 0.65rem;
  margin-top: 0.35rem;
  font-size: 12px;
  font-weight: 600;
  color: var(--pn-muted);
}

.pn-line__ref {
  color: #6b542f;
}

.pn-line__fields {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.65rem 0.75rem;
}

.pn-line__field {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: #4a3f34;
}

.pn-line__field--total strong {
  font-family: ui-monospace, 'Cascadia Mono', monospace;
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--pn-ink);
  text-transform: none;
  letter-spacing: 0;
  padding-top: 0.45rem;
}

.pn-line__input {
  width: 100%;
  border: 1px solid var(--pn-line-strong);
  border-radius: 8px;
  padding: 0.55rem 0.7rem;
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--pn-ink);
  background: #fff;
  outline: none;
}

.pn-line__input:focus {
  border-color: #8f7349;
  background: #fff;
  box-shadow: 0 0 0 2px rgba(143, 115, 73, 0.18);
}

.pn-line__input--loss {
  border-color: #c45c3e;
  background: #fff8f5;
}

.pn-line__input--loss:focus {
  border-color: #a33b1c;
  box-shadow: 0 0 0 2px rgba(163, 59, 28, 0.16);
}

.pn-line__input--error {
  border-color: #c45c3e;
  background: #fff8f5;
}

.pn-line__input--error:focus {
  border-color: #a33b1c;
  box-shadow: 0 0 0 2px rgba(163, 59, 28, 0.16);
}

.pn-line__field-error {
  margin: 0.15rem 0 0;
  font-style: normal;
  font-size: 11.5px;
  font-weight: 600;
  letter-spacing: 0;
  text-transform: none;
  line-height: 1.35;
  color: #a33b1c;
}

.pn-line__loss-warn {
  margin: 0.15rem 0 0;
  font-size: 11.5px;
  font-weight: 600;
  letter-spacing: 0;
  text-transform: none;
  line-height: 1.35;
  color: #a33b1c;
}

.pn-line__profit {
  margin: 0.1rem 0 0;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0;
  text-transform: none;
  color: #5c4f42;
}

.pn-line__profit--loss {
  color: #a33b1c;
}

.pn-line__field--hsd {
  grid-column: 1 / -1;
}

.pn-hsd-presets {
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem;
  margin-top: 0.15rem;
}

.pn-hsd-preset {
  border: 1px solid #e0d3be;
  border-radius: 999px;
  background: #fffdfa;
  color: #4a3f34;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.01em;
  text-transform: none;
  padding: 0.28rem 0.65rem;
  font-family: inherit;
  cursor: pointer;
  transition:
    background 0.15s ease,
    border-color 0.15s ease,
    color 0.15s ease;
}

.pn-hsd-preset:hover {
  background: #f5efe6;
  border-color: #c9a96e;
}

.pn-hsd-preset.is-on {
  background: #241a12;
  border-color: #241a12;
  color: #f9f5f0;
}

.pn-line__input:disabled {
  opacity: 0.75;
  cursor: not-allowed;
}

.pn-line__remove {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: 1px solid #f87171;
  border-radius: 8px;
  background: #fff;
  color: #991b1b;
  cursor: pointer;
  margin-top: 0.1rem;
}

.pn-line__remove:hover {
  background: #fee2e2;
  border-color: #ef4444;
}

.pn-right {
  padding: 0 1.15rem 1.15rem;
  position: sticky;
  top: 0.75rem;
}

.pn-field {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  margin-top: 0.95rem;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: #4a3f34;
}

.pn-field-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
}

.pn-ncc-row {
  display: flex;
  gap: 0.4rem;
}

.pn-control {
  width: 100%;
  border: 1px solid var(--pn-line-strong);
  border-radius: 8px;
  padding: 0.6rem 0.75rem;
  font-size: 0.875rem;
  font-weight: 600;
  text-transform: none;
  letter-spacing: 0;
  color: var(--pn-ink);
  background: #fff;
  outline: none;
}

.pn-control:focus {
  border-color: #8f7349;
  background: #fff;
  box-shadow: 0 0 0 2px rgba(143, 115, 73, 0.18);
}

.pn-control--error {
  border-color: #c45c3e;
  background: #fff8f5;
}

.pn-control--error:focus {
  border-color: #a33b1c;
  box-shadow: 0 0 0 2px rgba(163, 59, 28, 0.16);
}

.pn-field-error {
  font-style: normal;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0;
  text-transform: none;
  color: #a33b1c;
}

.pn-control:disabled,
.pn-control[readonly] {
  opacity: 0.85;
}

.pn-control--area {
  resize: vertical;
  min-height: 84px;
  font-family: inherit;
}

.pn-icon-btn {
  padding: 0.55rem 0.7rem !important;
  flex-shrink: 0;
}

.pn-totals {
  margin-top: 1rem;
  padding: 0.95rem;
  border: 1px solid var(--pn-line-strong);
  border-radius: 10px;
  background: #efe4d4;
}

.pn-totals__row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.75rem;
  font-size: 0.875rem;
  font-weight: 500;
  text-transform: none;
  letter-spacing: 0;
  color: var(--pn-ink);
}

.pn-totals__row strong {
  font-family: ui-monospace, 'Cascadia Mono', monospace;
}

.pn-field--inline {
  margin-top: 0.75rem;
  margin-bottom: 0.75rem;
}

.pn-totals__row--emph {
  padding-top: 0.75rem;
  border-top: 1px solid var(--pn-line);
}

.pn-totals__row--emph strong {
  color: #6b4520;
  font-size: 1.15rem;
  font-weight: 800;
}

.pn-right__actions {
  display: flex;
  flex-direction: column;
  gap: 0.55rem;
  margin-top: 1.1rem;
}

.pn-right__actions .soleil-btn-primary,
.pn-right__actions .soleil-btn-outline {
  width: 100%;
  justify-content: center;
}

.pn-modal {
  position: fixed;
  inset: 0;
  background: rgba(15, 26, 28, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: var(--admin-z-modal, 5000);
  padding: 1.25rem;
}

.pn-modal__panel {
  width: min(560px, 100%);
  max-height: 85vh;
  overflow: auto;
  background: var(--pn-surface);
  border-radius: 14px;
  padding: 1.15rem;
  border: 1px solid var(--pn-line);
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
  box-shadow: 0 16px 40px rgba(15, 26, 28, 0.18);
}

.pn-modal__panel--sm {
  width: min(420px, 100%);
}

.pn-modal__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 0.75rem;
}

.pn-modal__head h3 {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 700;
}

.pn-modal__head p {
  margin: 0.25rem 0 0;
  font-size: 0.8125rem;
  color: var(--pn-muted);
}

.pn-modal__empty {
  text-align: center;
  color: var(--pn-muted);
  padding: 1.5rem 0.5rem;
  margin: 0;
}

/* ---- Modal thêm hàng 2 bước (rộng) ---- */
.pn-add-modal {
  width: min(920px, 100%);
  height: min(720px, 100%);
  background: var(--pn-surface);
  border-radius: 14px;
  border: 1px solid var(--pn-line);
  box-shadow: 0 24px 60px -20px rgba(15, 30, 51, 0.35), 0 4px 12px rgba(15, 30, 51, 0.08);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.pn-add-modal__head {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 1.1rem 1.35rem;
  border-bottom: 1px solid var(--pn-line);
  flex-shrink: 0;
}

.pn-add-modal__title-block {
  flex: 1;
  min-width: 0;
}

.pn-step-tag {
  display: inline-block;
  font-size: 11.5px;
  font-weight: 800;
  color: #9a3412;
  background: #ffedd5;
  padding: 2px 8px;
  border-radius: 5px;
  margin-bottom: 0.4rem;
}

.pn-add-modal__title-block h3 {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 800;
  letter-spacing: -0.01em;
  color: var(--pn-ink);
}

.pn-add-modal__title-block p {
  margin: 0.2rem 0 0;
  font-size: 0.8125rem;
  color: var(--pn-muted);
}

.pn-add-modal__icon-btn {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: 1px solid var(--pn-line-strong);
  background: transparent;
  color: var(--pn-muted);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
}

.pn-add-modal__icon-btn:hover {
  background: var(--pn-mist);
  color: var(--pn-ink);
}

.pn-add-modal__search {
  padding: 0.85rem 1.25rem;
  border-bottom: 1px solid var(--pn-line);
  flex-shrink: 0;
}

.pn-add-modal__search-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 0.5rem;
  font-size: 12px;
  color: var(--pn-muted);
  font-weight: 600;
}

.pn-link-btn {
  background: none;
  border: none;
  color: var(--pn-accent);
  font-size: 12.5px;
  font-weight: 700;
  cursor: pointer;
  padding: 0;
}

.pn-link-btn:hover:not(:disabled) {
  text-decoration: underline;
}

.pn-link-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.pn-add-modal__body {
  flex: 1;
  overflow-y: auto;
  padding: 0.5rem 0.85rem;
  min-height: 0;
}

.pn-add-modal__body--qty {
  padding: 0.85rem 1rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.pn-prod-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  width: 100%;
  padding: 0.7rem 0.65rem;
  border-radius: 9px;
  border: 1px solid transparent;
  background: transparent;
  cursor: pointer;
  text-align: left;
  color: inherit;
}

.pn-prod-row:hover {
  background: var(--pn-mist);
}

.pn-prod-row--checked {
  background: #ffedd5;
  border-color: #fdba74;
}

.pn-prod-row input[type='checkbox'] {
  width: 17px;
  height: 17px;
  accent-color: #c2660c;
  cursor: pointer;
  flex-shrink: 0;
}

.pn-prod-row__thumb {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  overflow: hidden;
  background: #efe4d4;
  border: 1px solid var(--pn-line);
  flex-shrink: 0;
}

.pn-prod-row__thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.pn-prod-row__info {
  flex: 1;
  min-width: 0;
}

.pn-prod-row__name {
  font-size: 0.9rem;
  font-weight: 700;
  color: var(--pn-ink);
}

.pn-prod-row__sub {
  margin-top: 0.1rem;
  font-size: 12.5px;
  font-weight: 600;
  color: var(--pn-muted);
}

.pn-add-modal__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
  padding: 0.9rem 1.35rem;
  border-top: 1px solid var(--pn-line);
  background: var(--pn-mist);
  flex-shrink: 0;
}

.pn-add-modal__summary {
  font-size: 0.85rem;
  color: var(--pn-muted);
  font-weight: 600;
}

.pn-add-modal__summary b {
  color: var(--pn-ink);
}

.pn-add-modal__summary--multi {
  display: flex;
  gap: 1.1rem;
  flex-wrap: wrap;
}

.pn-add-modal__actions {
  display: flex;
  gap: 0.6rem;
}

.pn-qty-group {
  flex-shrink: 0;
}

.pn-qty-group__head {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  padding: 0.75rem 1.25rem;
  background: var(--pn-mist);
  position: sticky;
  top: 0;
  z-index: 2;
}

.pn-qty-group__thumb {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  overflow: hidden;
  background: #efe4d4;
  flex-shrink: 0;
}

.pn-qty-group__thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.pn-qty-group__name {
  font-size: 0.85rem;
  font-weight: 800;
  flex: 1;
  min-width: 0;
  color: var(--pn-ink);
}

.pn-qty-group__code {
  font-size: 12px;
  font-weight: 600;
  color: var(--pn-muted);
  font-family: ui-monospace, 'Cascadia Mono', monospace;
}

.pn-qty-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.85rem;
}

.pn-qty-table thead th {
  text-align: left;
  font-size: 12px;
  color: var(--pn-muted);
  font-weight: 700;
  padding: 0.6rem 0.85rem;
  background: #faf6f0;
  border-bottom: 1px solid var(--pn-line);
  position: sticky;
  top: 0;
  z-index: 1;
}

.pn-qty-table thead th.num,
.pn-qty-table td.num {
  text-align: right;
  font-variant-numeric: tabular-nums;
}

.pn-qty-table tbody td {
  padding: 0.65rem 0.85rem;
  border-bottom: 1px solid var(--pn-line);
  vertical-align: middle;
}

.pn-qty-table tbody tr.is-selected {
  background: #fff7ed;
}

.pn-qty-table__check {
  width: 40px;
  text-align: center !important;
}

.pn-qty-table__check input {
  width: 17px;
  height: 17px;
  accent-color: #c2660c;
  cursor: pointer;
}

.pn-qty-table__price {
  width: 130px;
}

.pn-qty-table__qty {
  width: 100px;
}

.pn-qty-table__amount {
  width: 130px;
  font-weight: 700;
}

.pn-variant-cell {
  display: flex;
  align-items: center;
  gap: 0.65rem;
}

.pn-variant-cell__thumb {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  overflow: hidden;
  background: #efe4d4;
  flex-shrink: 0;
}

.pn-variant-cell__thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.pn-variant-cell__name {
  font-weight: 700;
  font-size: 0.85rem;
  color: var(--pn-ink);
}

.pn-variant-cell__sku {
  font-size: 11.5px;
  font-weight: 600;
  color: var(--pn-muted);
  font-family: ui-monospace, 'Cascadia Mono', monospace;
}

.stock-low {
  color: #c23a3a;
  font-weight: 700;
}

.pn-qty-input {
  width: 100%;
  border: 1px solid var(--pn-line-strong);
  border-radius: 7px;
  padding: 0.4rem 0.5rem;
  font-family: inherit;
  font-size: 0.85rem;
  font-weight: 600;
  text-align: right;
  outline: none;
  color: var(--pn-ink);
  background: #fff;
}

.pn-qty-input:focus {
  border-color: #8f7349;
  box-shadow: 0 0 0 2px rgba(143, 115, 73, 0.18);
}

.pn-qty-input:disabled {
  background: #faf6f0;
  color: var(--pn-muted);
  cursor: not-allowed;
}

@media (max-width: 1100px) {
  .pn-line {
    grid-template-columns: 36px minmax(0, 1fr) auto;
  }

  .pn-line__fields {
    grid-column: 1 / -1;
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }

  .pn-line__remove {
    grid-column: 3;
    grid-row: 1;
  }
}

@media (max-width: 960px) {
  .pn-form__grid {
    grid-template-columns: 1fr;
  }

  .pn-right {
    position: static;
  }
}

@media (max-width: 640px) {
  .pn-add-modal {
    width: 100%;
    height: 100%;
    border-radius: 0;
  }

  .pn-line {
    grid-template-columns: 28px minmax(0, 1fr) auto;
  }

  .pn-line__fields {
    grid-template-columns: 1fr 1fr;
  }

  .pn-field-grid {
    grid-template-columns: 1fr;
  }

  .pn-qty-table__price,
  .pn-qty-table__qty,
  .pn-qty-table__amount {
    width: auto;
  }
}
</style>
