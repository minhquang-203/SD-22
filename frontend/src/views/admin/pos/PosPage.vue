<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { Icon } from '@iconify/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import {
  getSanPhamBan,
  getPhuongThuc,
  timKhachTheoSdt,
  taoKhachNhanh,
  taoDonTaiQuay,
  giuDon,
  dsDonCho,
  layDonCho,
  huyDonCho,
} from '@/api/banHangApi'
import { formatCurrency, formatMonthYear } from '@/utils/format'
import { confirm } from '@/composables/useConfirm'
import { useAdminAuth } from '@/composables/useAdminAuth'

const { hoTen: currentStaffName } = useAdminAuth()

const loading = ref(false)
const paying = ref(false)
const message = ref('')
const messageType = ref('success')

const keyword = ref('')
const searchResults = ref([])
const productsLoaded = ref(false)
const searchInput = ref(null)

const cart = ref([])

const customerSdt = ref('')
const selectedCustomer = ref(null)
const showQuickCreate = ref(false)
const quickName = ref('')

const voucherCode = ref('')
const appliedVoucher = ref('')

const paymentMethods = ref([])
const selectedPaymentId = ref(null)
const cashGiven = ref('')
const transferRef = ref('')
const ghiChu = ref('')

const receipt = ref(null)
const showReceipt = ref(false)

const heldOrders = ref([])
const showHeldDrawer = ref(false)
const activeHeldOrderId = ref(null)
const holding = ref(false)

let searchTimer = null

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

const isNonCash = computed(() => selectedPayment.value && !isCash.value)

const tongTienHang = computed(() =>
  cart.value.reduce((sum, line) => sum + line.giaBan * line.soLuong, 0),
)

const thanhTien = computed(() => tongTienHang.value)

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
  if (cart.value.length === 0 || !selectedPaymentId.value) return false
  if (isCash.value) {
    const cash = Number(cashGiven.value) || 0
    return cash >= thanhTien.value
  }
  return true
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

function cartKey(id) {
  return id
}

function productInitial(name) {
  const t = (name || '?').trim()
  return t ? t.charAt(0).toUpperCase() : '?'
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

function addToCart(product) {
  if (isOutOfStock(product)) return
  const existing = cart.value.find((l) => l.idChiTietSanPham === product.idChiTietSanPham)
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
      bienThe: formatVariant(product),
      giaBan: Number(product.giaBan),
      soLuongTon: product.soLuongTon,
      soLuong: 1,
    })
  }
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
  line.soLuong = next
}

function removeLine(line) {
  cart.value = cart.value.filter((l) => l.idChiTietSanPham !== line.idChiTietSanPham)
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

function onSearchEnter() {
  if (searchResults.value.length > 0) {
    const first = searchResults.value.find((p) => p.soLuongTon > 0) || searchResults.value[0]
    if (first.soLuongTon > 0) addToCart(first)
  }
}

async function loadMeta() {
  try {
    const ptRes = await getPhuongThuc()
    paymentMethods.value = ptRes.data || []
    const cash = paymentMethods.value.find((p) => p.ma === 'TIEN_MAT')
    if (cash) selectedPaymentId.value = cash.id
  } catch (err) {
    notify(String(err), 'error')
  }
}

async function findCustomer() {
  const sdt = customerSdt.value.trim()
  if (!sdt) return
  showQuickCreate.value = false
  try {
    const res = await timKhachTheoSdt(sdt)
    selectedCustomer.value = res.data
    notify(`Đã tìm thấy: ${res.data.hoTen}`)
  } catch {
    selectedCustomer.value = null
    showQuickCreate.value = true
    notify('Không tìm thấy khách — có thể tạo nhanh', 'error')
  }
}

async function createQuickCustomer() {
  const sdt = customerSdt.value.trim()
  if (!sdt) {
    notify('Vui lòng nhập SĐT', 'error')
    return
  }
  try {
    const res = await taoKhachNhanh({ hoTen: quickName.value.trim() || undefined, soDienThoai: sdt })
    selectedCustomer.value = res.data
    showQuickCreate.value = false
    notify(`Đã tạo khách: ${res.data.hoTen}`)
  } catch (err) {
    notify(String(err), 'error')
  }
}

function clearCustomer() {
  selectedCustomer.value = null
  customerSdt.value = ''
  quickName.value = ''
  showQuickCreate.value = false
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

function applyVoucher() {
  const code = voucherCode.value.trim()
  if (!code) {
    appliedVoucher.value = ''
    return
  }
  confirm({
    title: 'Áp mã giảm giá',
    message: `Áp dụng mã "${code}" cho đơn này?`,
    confirmText: 'Áp mã',
  }).then((ok) => {
    if (!ok) return
    appliedVoucher.value = code
    notify(`Mã "${code}" sẽ được áp dụng khi thanh toán`)
  })
}

async function loadHeldOrders() {
  try {
    const res = await dsDonCho()
    heldOrders.value = res.data || []
  } catch (err) {
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
  voucherCode.value = ''
  appliedVoucher.value = ''
  cashGiven.value = ''
  transferRef.value = ''
}

async function holdCurrentOrder() {
  if (cart.value.length === 0) return
  const ok = await confirm({
    title: 'Giữ đơn',
    message: 'Lưu giỏ hàng hiện tại thành đơn chờ?',
    confirmText: 'Giữ đơn',
  })
  if (!ok) return
  holding.value = true
  try {
    await giuDon({
      items: cart.value.map((l) => ({
        idChiTietSanPham: l.idChiTietSanPham,
        soLuong: l.soLuong,
      })),
      idKhachHang: selectedCustomer.value?.id ?? null,
    })
    if (activeHeldOrderId.value) {
      try {
        await huyDonCho(activeHeldOrderId.value)
      } catch {
        /* đơn cũ có thể đã xử lý */
      }
      activeHeldOrderId.value = null
    }
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
  cart.value = (detail.items || []).map((item) => ({
    idChiTietSanPham: item.idChiTietSanPham,
    sku: item.sku,
    tenSanPham: item.tenSanPham,
    bienThe: formatVariant(item),
    giaBan: Number(item.donGia),
    soLuongTon: item.soLuongTon ?? 0,
    soLuong: item.soLuong,
  }))
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
    const res = await layDonCho(order.id)
    const detail = res.data
    loadCartFromDetail(detail)
    if (detail.idKhachHang) {
      selectedCustomer.value = {
        id: detail.idKhachHang,
        hoTen: detail.hoTenKhachHang,
        soDienThoai: detail.soDienThoai,
      }
      customerSdt.value = detail.soDienThoai || ''
      showQuickCreate.value = false
    } else {
      clearCustomer()
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

async function checkout() {
  if (!canCheckout.value) return
  const ok = await confirm({
    title: 'Tạo hóa đơn',
    message: 'Bạn có chắc muốn tạo hóa đơn này?',
    confirmText: 'Tạo hóa đơn',
  })
  if (!ok) return
  paying.value = true
  try {
    const payload = {
      items: cart.value.map((l) => ({
        idChiTietSanPham: l.idChiTietSanPham,
        soLuong: l.soLuong,
      })),
      idKhachHang: selectedCustomer.value?.id ?? null,
      maPhieuGiamGia: appliedVoucher.value || null,
      idPhuongThucThanhToan: selectedPaymentId.value,
      soTienKhachDua: isCash.value ? Number(cashGiven.value) : null,
      maGiaoDich: isNonCash.value && transferRef.value.trim() ? transferRef.value.trim() : null,
      ghiChu: ghiChu.value.trim() || null,
      idHoaDonCho: activeHeldOrderId.value ?? null,
    }
    const res = await taoDonTaiQuay(payload)
    receipt.value = res.data
    showReceipt.value = true
    activeHeldOrderId.value = null
    clearCustomer()
    cart.value = []
    void loadProducts()
    await loadHeldOrders()
    notify('Thanh toán thành công!')
  } catch (err) {
    notify(String(err), 'error')
  } finally {
    paying.value = false
  }
}

function resetSale() {
  clearTimeout(searchTimer)
  cart.value = []
  keyword.value = ''
  clearCustomer()
  voucherCode.value = ''
  appliedVoucher.value = ''
  cashGiven.value = ''
  transferRef.value = ''
  ghiChu.value = ''
  receipt.value = null
  showReceipt.value = false
  activeHeldOrderId.value = null
  const cash = paymentMethods.value.find((p) => p.ma === 'TIEN_MAT')
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
</script>

<template>
  <div class="admin-page">
    <PageHeader title="Bán hàng tại quầy" description="Point of sale — chọn sản phẩm, thu tiền, in biên lai">
      <template #actions>
        <button type="button" class="soleil-btn-outline mr-3" @click="openHeldDrawer">
          Đơn chờ
          <span v-if="heldCount > 0" class="pos-held-badge">{{ heldCount }}</span>
        </button>
        <div class="pos-current-staff" title="Nhân viên bán hàng (tài khoản đang đăng nhập)">
          <Icon icon="icon-park-outline:user" class="text-base opacity-70" />
          <span>{{ currentStaffName || '—' }}</span>
        </div>
      </template>
    </PageHeader>

    <div
      v-if="message"
      class="admin-alert mb-4"
      :class="messageType === 'error' ? 'admin-alert-error' : 'admin-alert-success'"
    >
      {{ message }}
    </div>

    <div class="pos-layout">
      <!-- Cột trái: chọn sản phẩm -->
      <div class="pos-panel">
        <p class="soleil-eyebrow mb-3">Chọn sản phẩm</p>
        <div class="pos-search-wrap">
          <input
            ref="searchInput"
            v-model="keyword"
            type="text"
            class="pos-search-input"
            placeholder="Tìm theo SKU hoặc tên sản phẩm..."
            @keydown.enter.prevent="onSearchEnter"
          />
        </div>

        <div v-if="loading && !productsLoaded" class="text-sm text-[var(--admin-muted)] py-8 text-center">
          Đang tải sản phẩm...
        </div>
        <div v-else-if="productsLoaded && searchResults.length === 0" class="pos-cart-empty">
          {{ keyword.trim() ? 'Không tìm thấy sản phẩm phù hợp' : 'Không có sản phẩm đang bán' }}
        </div>
        <div v-else class="pos-product-grid">
          <button
            v-for="item in searchResults"
            :key="item.idChiTietSanPham"
            type="button"
            class="pos-product-card"
            :class="{ 'pos-product-card--out': isOutOfStock(item) }"
            :disabled="isOutOfStock(item)"
            @click="addToCart(item)"
          >
            <span v-if="!isOutOfStock(item)" class="pos-product-card__add" aria-hidden="true">
              <Icon icon="mdi:plus" width="18" />
            </span>
            <span v-if="isOutOfStock(item)" class="pos-product-card__badge-out">Hết hàng</span>
            <div class="pos-product-card__thumb">
              {{ productInitial(item.tenSanPham) }}
            </div>
            <p class="pos-product-card__name">{{ item.tenSanPham }}</p>
            <p class="pos-product-card__variant">{{ formatVariant(item) }}</p>
            <p class="pos-product-card__price">{{ formatCurrency(item.giaBan) }}</p>
            <p class="pos-product-card__stock">
              {{ isOutOfStock(item) ? 'Hết hàng' : `Còn ${item.soLuongTon}` }}
            </p>
            <p
              v-if="item.hanSuDungGanNhat && !isOutOfStock(item)"
              class="pos-product-card__hsd"
            >
              HSD: {{ formatMonthYear(item.hanSuDungGanNhat) }}
            </p>
            <span
              v-if="posExpiryBadge(item) === 'expired' && !isOutOfStock(item)"
              class="pos-product-card__badge-expiry pos-product-card__badge-expiry--danger"
            >
              Hết hạn
            </span>
            <span
              v-else-if="posExpiryBadge(item) === 'warning' && !isOutOfStock(item)"
              class="pos-product-card__badge-expiry pos-product-card__badge-expiry--warn"
            >
              Sắp hết hạn
            </span>
          </button>
        </div>
      </div>

      <!-- Cột phải: hóa đơn -->
      <div class="pos-panel pos-panel--sticky">
        <p class="soleil-eyebrow mb-3">Hóa đơn</p>

        <!-- Khách hàng -->
        <div class="pos-customer-bar">
          <template v-if="selectedCustomer">
            <div class="pos-customer-info">
              <strong>{{ selectedCustomer.hoTen }}</strong>
              <span class="text-[var(--admin-muted)]"> · {{ selectedCustomer.soDienThoai }}</span>
              <span v-if="selectedCustomer.diemTichLuy != null" class="block text-sm mt-1">
                Điểm tích lũy: <span class="text-[var(--warm-tan)]">{{ selectedCustomer.diemTichLuy }}</span>
              </span>
            </div>
            <button type="button" class="admin-btn admin-btn-default text-sm" @click="clearCustomer">
              Bỏ chọn
            </button>
          </template>
          <template v-else>
            <span class="text-sm text-[var(--admin-muted)]">Khách lẻ</span>
            <input
              v-model="customerSdt"
              type="text"
              class="admin-input flex-1 min-w-[140px]"
              placeholder="Số điện thoại"
            />
            <button type="button" class="admin-btn admin-btn-default" @click="findCustomer">
              Tìm
            </button>
          </template>
        </div>

        <div v-if="showQuickCreate && !selectedCustomer" class="mb-4 p-3 rounded-lg bg-[var(--cream)] border border-[var(--admin-border)]">
          <p class="text-sm mb-2 text-[var(--admin-muted)]">Chưa có khách với SĐT này</p>
          <input
            v-model="quickName"
            type="text"
            class="admin-input w-full mb-2"
            placeholder="Họ tên (tùy chọn)"
          />
          <button type="button" class="admin-btn admin-btn-primary w-full" @click="createQuickCustomer">
            Tạo nhanh
          </button>
        </div>

        <!-- Giỏ hàng -->
        <div v-if="cart.length === 0" class="pos-cart-empty">
          Chưa có sản phẩm trong đơn
        </div>
        <div v-else class="pos-cart-lines">
          <div v-for="line in cart" :key="cartKey(line.idChiTietSanPham)" class="pos-cart-line">
            <div class="pos-cart-line__info">
              <div class="pos-cart-line__name">{{ line.tenSanPham }}</div>
              <div class="pos-cart-line__variant">{{ line.bienThe }}</div>
              <div class="pos-cart-line__price">{{ formatCurrency(line.giaBan) }} / sp</div>
            </div>
            <button
              type="button"
              class="soleil-act-btn-round self-start"
              title="Xóa dòng"
              @click="removeLine(line)"
            >
              <Icon icon="mdi:close" width="16" />
            </button>
            <div class="pos-qty-control">
              <button
                type="button"
                class="pos-qty-btn"
                :disabled="line.soLuong <= 1"
                @click="changeQty(line, -1)"
              >−</button>
              <span class="pos-qty-value">{{ line.soLuong }}</span>
              <button
                type="button"
                class="pos-qty-btn"
                :disabled="line.soLuong >= line.soLuongTon"
                @click="changeQty(line, 1)"
              >+</button>
            </div>
            <div class="pos-cart-line__total">
              {{ formatCurrency(line.giaBan * line.soLuong) }}
            </div>
          </div>
        </div>

        <!-- Voucher -->
        <div class="mb-4">
          <p class="pos-section-title">Mã giảm giá</p>
          <div class="flex gap-2">
            <input
              v-model="voucherCode"
              type="text"
              class="admin-input flex-1"
              placeholder="Nhập mã (tùy chọn)"
            />
            <button type="button" class="admin-btn admin-btn-default" @click="applyVoucher">
              Áp dụng
            </button>
          </div>
          <p v-if="appliedVoucher" class="text-xs text-[var(--admin-muted)] mt-1">
            Mã đã nhập: <strong>{{ appliedVoucher }}</strong> — giảm tính khi thanh toán
          </p>
        </div>

        <!-- Tổng tiền -->
        <div class="pos-totals">
          <div class="pos-totals__row">
            <span>Tổng tiền hàng</span>
            <span>{{ formatCurrency(tongTienHang) }}</span>
          </div>
          <div v-if="appliedVoucher" class="pos-totals__row">
            <span>Giảm giá</span>
            <span class="text-[var(--sage)]">Theo mã {{ appliedVoucher }}</span>
          </div>
          <div class="pos-totals__grand">
            <span class="pos-totals__grand-label">Thành tiền</span>
            <span class="pos-totals__grand-value">{{ formatCurrency(thanhTien) }}</span>
          </div>
        </div>

        <!-- Thanh toán -->
        <p class="pos-section-title">Phương thức thanh toán</p>
        <div class="pos-pay-methods">
          <button
            v-for="pt in paymentMethods"
            :key="pt.id"
            type="button"
            class="pos-pay-btn"
            :class="{ 'pos-pay-btn--active': selectedPaymentId === pt.id }"
            @click="selectedPaymentId = pt.id"
          >
            {{ pt.ten }}
          </button>
        </div>

        <div v-if="isCash" class="mb-4">
          <label class="soleil-label block mb-2">Tiền khách đưa</label>
          <input
            v-model="cashGiven"
            type="number"
            min="0"
            class="admin-input w-full text-lg"
            placeholder="0"
          />
          <div class="flex flex-wrap gap-2 mt-2">
            <button type="button" class="pos-denom-btn" @click="addDenomination(50000)">+50k</button>
            <button type="button" class="pos-denom-btn" @click="addDenomination(100000)">+100k</button>
            <button type="button" class="pos-denom-btn" @click="addDenomination(200000)">+200k</button>
            <button type="button" class="pos-denom-btn" @click="addDenomination(500000)">+500k</button>
            <button type="button" class="pos-denom-btn pos-denom-btn--accent" @click="fillExactCash">
              Đủ tiền
            </button>
            <button type="button" class="pos-denom-btn" @click="clearCash">Xóa</button>
          </div>
          <p v-if="cashShortage > 0" class="text-sm mt-2 text-[var(--coral)] font-medium">
            Khách chưa đưa đủ tiền (thiếu {{ formatCurrency(cashShortage) }})
          </p>
          <p v-else-if="cashGiven" class="text-sm mt-2 text-[var(--admin-muted)]">
            Tiền thối:
            <strong class="text-[var(--warm-tan)]">{{ formatCurrency(tienThua) }}</strong>
          </p>
        </div>

        <div v-else-if="isNonCash" class="mb-4">
          <label class="soleil-label block mb-2">Mã giao dịch (tùy chọn)</label>
          <input v-model="transferRef" type="text" class="admin-input w-full" placeholder="Mã GD / tham chiếu..." />
        </div>

        <div class="pos-checkout-row">
          <button
            type="button"
            class="soleil-btn-outline pos-hold-btn"
            :disabled="cart.length === 0 || holding"
            @click="holdCurrentOrder"
          >
            {{ holding ? 'Đang giữ...' : 'Giữ đơn' }}
          </button>
          <button
            type="button"
            class="pos-create-invoice-btn pos-checkout-btn"
            :disabled="!canCheckout || paying"
            @click="checkout"
          >
            {{ paying ? 'Đang tạo...' : 'Tạo hóa đơn' }}
          </button>
        </div>
        <p v-if="activeHeldOrderId" class="text-xs text-[var(--admin-muted)] mt-2 text-center">
          Đang tiếp tục đơn chờ — thanh toán sẽ hoàn tất đơn này
        </p>
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
            <div class="mt-1">PTTT: {{ receipt.tenPhuongThucThanhToan }}</div>
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
  </div>
</template>
