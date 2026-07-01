<template>
  <div class="sale-detail-page">

    <!-- BREADCRUMB -->
    <div class="sd-breadcrumb">
      <router-link to="/admin/sale">Đợt giảm giá</router-link>
      <span>/</span>
      <span class="sd-breadcrumb__current">{{ sale?.ten || 'Chi tiết' }}</span>
    </div>

    <div v-if="loading" class="sd-loading">
      <i class="ti ti-loader-2 sd-spinner" style="font-size:1.5rem"></i>
      <p style="margin-top:0.75rem">Đang tải dữ liệu...</p>
    </div>

    <div v-else-if="error" class="sd-error">
      <p>{{ error }}</p>
      <button class="sd-btn-primary" @click="router.push('/admin/sale')">Quay lại danh sách</button>
    </div>

    <div v-else class="sd-container">

      <!-- CAMPAIGN HEADER -->
      <div class="sd-campaign-header">
        <div class="sd-campaign-header__deco-1"></div>
        <div class="sd-campaign-header__deco-2"></div>

        <div class="sd-campaign-header__inner">
          <div class="sd-campaign-header__main">
            <div class="sd-campaign-header__badges">
              <span class="sd-badge" :class="statusBadgeClass">{{ sale.timeStatusLabel }}</span>
              <span class="sd-campaign-code">{{ sale.ma }}</span>
            </div>
            <div class="sd-campaign-title-row">
              <h1 class="sd-campaign-title">{{ sale.ten }}</h1>
              <span class="sd-discount-chip">−{{ sale.phanTramGiam }}%</span>
            </div>
            <p class="sd-campaign-desc">
              {{ formatDate(sale.ngayBatDau) }} — {{ formatDate(sale.ngayKetThuc) }}
              <span class="sd-campaign-desc__dot">·</span>
              {{ products.length }} sản phẩm
            </p>

            <div class="sd-timeline">
              <div class="sd-timeline__labels">
                <span>{{ formatDate(sale.ngayBatDau) }}</span>
                <span>{{ timeRemainingLabel }}</span>
                <span>{{ formatDate(sale.ngayKetThuc) }}</span>
              </div>
              <div class="sd-timeline__track">
                <div class="sd-timeline__fill" :style="{ width: progressPercent + '%' }"></div>
              </div>
              <div class="sd-timeline__footer">
                <span>Bắt đầu</span>
                <span class="highlight">● {{ progressPercent }}% thời gian</span>
                <span>Kết thúc</span>
              </div>
            </div>
          </div>

          <div class="sd-stats">
            <div v-for="stat in campaignStats" :key="stat.label" class="sd-stat-box">
              <div class="sd-stat-box__value" :class="stat.colorClass">{{ stat.value }}</div>
              <div class="sd-stat-box__label">{{ stat.label }}</div>
            </div>
          </div>
        </div>

        <div class="sd-campaign-actions">
          <button class="sd-btn-primary" @click="showModal = true">
            <svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/></svg>
            Thêm sản phẩm
          </button>
          <button class="sd-btn-ghost" @click="router.push('/admin/sale')">
            <svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M10 19l-7-7m0 0l7-7m-7 7h18"/></svg>
            Quay lại
          </button>
        </div>
      </div>

      <div class="sd-layout">

        <!-- SIDEBAR -->
        <aside class="sd-sidebar">

          <div class="sd-panel sd-panel--summary">
            <h3 class="sd-panel__title">Hiệu suất</h3>
            <div class="sd-performance">
              <div v-for="metric in performanceMetrics" :key="metric.label" class="sd-performance__item">
                <div class="sd-performance__head">
                  <span class="sd-performance__label">{{ metric.label }}</span>
                  <span class="sd-performance__value" :class="metric.valueClass">{{ metric.display }}</span>
                </div>
                <div class="sd-performance__track">
                  <div
                    class="sd-performance__fill"
                    :class="metric.barClass"
                    :style="{ width: metric.percent + '%' }"
                  ></div>
                </div>
              </div>
            </div>
            <div class="sd-performance__footer">
              <div class="sd-performance__footer-label">Tổng doanh thu</div>
              <div class="sd-performance__footer-value">{{ formatCurrency(performanceStats.doanhThu) }}</div>
              <div v-if="performanceStats.tangTruong != null" class="sd-performance__growth">
                <svg width="12" height="12" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"/>
                </svg>
                +{{ performanceStats.tangTruong }}% so với đợt trước
              </div>
            </div>
          </div>

          <div class="sd-panel sd-panel--compact">
            <h3 class="sd-panel__title">Lưu ý</h3>
            <ul class="sd-conditions">
              <li>Không dùng chung với mã giảm giá khác</li>
              <li>Giá sau giảm được tính tự động theo đợt</li>
            </ul>
          </div>
        </aside>

        <!-- MAIN -->
        <main class="sd-main">

          <div class="sd-toolbar">
            <div class="sd-toolbar__left">
              <h2 class="sd-toolbar__title">Sản phẩm áp dụng</h2>
              <span class="sd-count-badge">{{ filteredProducts.length }}</span>
            </div>
            <div class="sd-toolbar__right">
              <div class="sd-search">
                <input v-model="searchQuery" type="text" placeholder="Tìm sản phẩm..." />
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
              </div>
              <div class="sd-view-toggle">
                <button :class="{ active: viewMode === 'grid' }" title="Lưới" @click="viewMode = 'grid'">
                  <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zm10 0a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zm10 0a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z"/></svg>
                </button>
                <button :class="{ active: viewMode === 'list' }" title="Danh sách" @click="viewMode = 'list'">
                  <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M4 6h16M4 12h16M4 18h16"/></svg>
                </button>
              </div>
            </div>
          </div>

          <div v-if="loadingProducts" class="sd-empty">Đang tải sản phẩm...</div>

          <div v-else-if="!filteredProducts.length" class="sd-empty">
            <i class="ti ti-package-off"></i>
            <p>Chưa có sản phẩm nào trong đợt giảm giá</p>
            <button class="sd-btn-primary" style="margin:1rem auto 0" @click="showModal = true">Thêm sản phẩm đầu tiên</button>
          </div>

          <!-- GRID VIEW -->
          <div v-else-if="viewMode === 'grid'" class="sd-product-grid">
            <div
              v-for="(product, i) in filteredProducts"
              :key="product.id"
              class="sd-product-card"
              :style="{ animationDelay: (i * 0.05) + 's' }"
            >
              <div class="sd-product-card__image" :class="imageVariantClass(i)">
                <div class="sd-product-card__placeholder">
                  <div class="sd-product-card__avatar">{{ productInitials(product.name) }}</div>
                </div>
                <div class="sd-product-card__overlay">
                  <button class="sd-overlay-btn sd-overlay-btn--danger" @click="removeProduct(product)">Xóa</button>
                </div>
              </div>
              <div class="sd-product-card__body">
                <div class="sd-product-card__sku-line">{{ product.sku }}</div>
                <div class="sd-product-card__name">{{ product.name }}</div>
                <div class="sd-product-card__prices">
                  <span class="sd-product-card__price-new">{{ formatCurrency(product.priceNew) }}</span>
                  <span class="sd-product-card__price-old">{{ formatCurrency(product.priceOld) }}</span>
                  <span class="sd-product-card__price-save">−{{ formatCurrency(product.priceOld - product.priceNew) }}</span>
                </div>
              </div>
            </div>

            <button class="sd-add-card" :style="{ animationDelay: (filteredProducts.length * 0.05) + 's' }" @click="showModal = true">
              <div class="sd-add-card__icon">
                <svg width="20" height="20" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 4v16m8-8H4"/></svg>
              </div>
              <div class="sd-add-card__text">Thêm<br>Sản phẩm</div>
            </button>
          </div>

          <!-- LIST VIEW -->
          <div v-else class="sd-product-grid sd-product-grid--list">
            <div
              v-for="(product, i) in filteredProducts"
              :key="product.id"
              class="sd-product-list-item"
              :style="{ animationDelay: (i * 0.04) + 's' }"
            >
              <div class="sd-product-list-item__inner">
                <div class="sd-product-list-item__thumb">{{ productInitials(product.name) }}</div>
                <div class="sd-product-list-item__info">
                  <div class="sd-product-card__sku-line">{{ product.sku }}</div>
                  <div class="sd-product-card__name">{{ product.name }}</div>
                </div>
                <div class="sd-product-list-item__prices">
                  <div class="sd-product-card__price-new">{{ formatCurrency(product.priceNew) }}</div>
                  <div class="sd-product-card__price-old">{{ formatCurrency(product.priceOld) }}</div>
                </div>
                <div class="sd-product-list-item__save">−{{ formatCurrency(product.priceOld - product.priceNew) }}</div>
                <button class="sd-btn-remove" title="Xóa khỏi đợt" @click="removeProduct(product)">
                  <i class="ti ti-trash"></i>
                </button>
              </div>
            </div>
          </div>

        </main>
      </div>
    </div>

    <!-- MODAL -->
    <Teleport to="body">
      <div v-if="showModal" class="sd-modal-overlay" @click.self="closeModal">
        <div class="sd-modal" role="dialog" aria-modal="true" @click.stop>
          <div class="sd-modal__header">
            <div>
              <div class="sd-modal__eyebrow">Đợt giảm giá — {{ sale?.ten }} · −{{ sale?.phanTramGiam }}%</div>
              <h3 class="sd-modal__title">Thêm sản phẩm</h3>
            </div>
            <button type="button" class="sd-modal__close" aria-label="Đóng" @click="closeModal">
              <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
            </button>
          </div>
          <div class="sd-modal__body">
            <div>
              <label class="sd-form-label">Tìm sản phẩm</label>
              <input v-model="modalSearch" type="text" placeholder="Nhập tên hoặc mã SKU..." class="sd-form-input" />
            </div>
            <div class="sd-product-picker">
              <div class="sd-product-picker__head">
                <span>Sản phẩm chưa áp dụng</span>
                <span>{{ availableProducts.length }} sản phẩm</span>
              </div>
              <div v-if="loadingAvailable" class="sd-product-picker__empty">Đang tìm...</div>
              <div v-else-if="!availableProducts.length" class="sd-product-picker__empty">Không tìm thấy sản phẩm phù hợp</div>
              <div v-else class="sd-product-picker__list">
                <label v-for="ap in availableProducts" :key="ap.idChiTietSanPham" class="sd-product-picker__item">
                  <input type="checkbox" :value="ap.idChiTietSanPham" v-model="selectedIds" />
                  <div class="sd-product-picker__item-info">
                    <div class="sd-product-picker__item-name">{{ ap.tenSanPham }}</div>
                    <div class="sd-product-picker__item-meta">SKU: {{ ap.sku }} · {{ ap.tenMauSac || '—' }}</div>
                  </div>
                  <span class="sd-product-picker__item-price">{{ formatCurrency(ap.giaBan) }}</span>
                </label>
              </div>
            </div>
          </div>
          <div class="sd-modal__footer">
            <span class="sd-modal__footer-note">{{ selectedIds.length }} sản phẩm được chọn</span>
            <div class="sd-modal__footer-actions">
              <button type="button" class="sd-btn-cancel" @click="closeModal">Hủy</button>
              <button type="button" class="sd-btn-submit" :disabled="adding || !selectedIds.length" @click="handleAddProducts">
                {{ adding ? 'Đang thêm...' : 'Thêm vào đợt' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import '@/styles/saleDetailCss.css'
import { getSaleById, getSaleProducts, addSaleProduct, deleteSaleProduct } from '@/api/saleApi'
import { getSanPhamBan } from '@/api/banHangApi'
import { formatCurrency, formatDate } from '@/utils/format'
import { confirm } from '@/composables/useConfirm'
import { toast } from '@/composables/useToast'

const route = useRoute()
const router = useRouter()

const saleId = computed(() => Number(route.params.id))

const loading = ref(true)
const loadingProducts = ref(false)
const loadingAvailable = ref(false)
const adding = ref(false)
const error = ref('')
const sale = ref(null)
const products = ref([])

const searchQuery = ref('')
const viewMode = ref('grid')
const showModal = ref(false)
const modalSearch = ref('')
const availableProducts = ref([])
const selectedIds = ref([])

const statusBadgeClass = computed(() => {
  const map = {
    ACTIVE: 'sd-badge--active',
    UPCOMING: 'sd-badge--upcoming',
    EXPIRED: 'sd-badge--expired',
    INACTIVE: 'sd-badge--inactive',
  }
  return map[sale.value?.timeStatus] || 'sd-badge--expired'
})

const progressPercent = computed(() => {
  if (!sale.value?.ngayBatDau || !sale.value?.ngayKetThuc) return 0
  const start = new Date(sale.value.ngayBatDau).getTime()
  const end = new Date(sale.value.ngayKetThuc).getTime()
  const now = Date.now()
  if (now <= start) return 0
  if (now >= end) return 100
  return Math.round(((now - start) / (end - start)) * 100)
})

const timeRemainingLabel = computed(() => {
  if (!sale.value?.ngayKetThuc) return '—'
  const end = new Date(sale.value.ngayKetThuc)
  const now = new Date()
  if (sale.value.timeStatus === 'EXPIRED') return 'Đã kết thúc'
  if (sale.value.timeStatus === 'UPCOMING') {
    const days = Math.ceil((new Date(sale.value.ngayBatDau) - now) / 86400000)
    return `Còn ${days} ngày đến khi bắt đầu`
  }
  const days = Math.ceil((end - now) / 86400000)
  return days > 0 ? `Còn ${days} ngày` : 'Kết thúc hôm nay'
})

const performanceStats = computed(() => {
  const count = products.value.length
  const mucTieu = Math.max(count * 20, count > 0 ? 100 : 0)
  const daBan = 0
  const luotXem = 0
  const chuyenDoi = 0
  const doanhThu = 0

  return {
    daBan,
    mucTieu,
    luotXem,
    chuyenDoi,
    doanhThu,
    tangTruong: null,
    daBanPercent: mucTieu > 0 ? Math.min(100, Math.round((daBan / mucTieu) * 100)) : 0,
    luotXemPercent: 0,
    chuyenDoiPercent: 0,
  }
})

const performanceMetrics = computed(() => [
  {
    label: 'Đã bán',
    display: `${performanceStats.value.daBan} / ${performanceStats.value.mucTieu}`,
    percent: performanceStats.value.daBanPercent,
    valueClass: '',
    barClass: 'sd-performance__fill--navy',
  },
  {
    label: 'Lượt xem',
    display: performanceStats.value.luotXem.toLocaleString('vi-VN'),
    percent: performanceStats.value.luotXemPercent,
    valueClass: '',
    barClass: 'sd-performance__fill--gold',
  },
  {
    label: 'Chuyển đổi',
    display: `${performanceStats.value.chuyenDoi}%`,
    percent: performanceStats.value.chuyenDoiPercent,
    valueClass: 'sd-performance__value--coral',
    barClass: 'sd-performance__fill--coral',
  },
])

const campaignStats = computed(() => [
  { label: 'Sản phẩm', value: String(products.value.length), colorClass: 'sd-stat-box__value--gold' },
  { label: 'Giảm tối đa', value: `${sale.value?.phanTramGiam ?? 0}%`, colorClass: 'sd-stat-box__value--light' },
  { label: 'Doanh thu', value: formatShortCurrency(performanceStats.value.doanhThu), colorClass: 'sd-stat-box__value--coral' },
])

const filteredProducts = computed(() => {
  const q = searchQuery.value.trim().toLowerCase()
  if (!q) return products.value
  return products.value.filter((p) =>
    p.name.toLowerCase().includes(q) || p.sku.toLowerCase().includes(q),
  )
})

function imageVariantClass(index) {
  const variants = ['', 'sd-product-card__image--alt1', 'sd-product-card__image--alt2', 'sd-product-card__image--alt3']
  return variants[index % variants.length]
}

function productInitials(name) {
  if (!name) return '?'
  const words = name.trim().split(/\s+/).filter(Boolean)
  if (words.length === 1) return words[0].slice(0, 2).toUpperCase()
  return (words[0][0] + words[words.length - 1][0]).toUpperCase()
}

function formatShortCurrency(value) {
  const num = Number(value || 0)
  if (num >= 1_000_000) return `₫${(num / 1_000_000).toFixed(1)}M`
  if (num >= 1_000) return `₫${Math.round(num / 1_000)}K`
  return formatCurrency(num)
}

function mapProduct(item) {
  const priceOld = Number(item.giaBan || 0)
  const priceNew = Number(item.giaSauGiam || 0)

  return {
    id: item.id,
    idChiTietSanPham: item.idChiTietSanPham,
    sku: item.sku || '—',
    name: item.tenSanPham || 'Không rõ tên',
    priceOld,
    priceNew,
  }
}

function normalizeError(err) {
  return typeof err === 'string' ? err : err?.message
}

async function loadSale() {
  loading.value = true
  error.value = ''
  try {
    const res = await getSaleById(saleId.value)
    sale.value = res.data
  } catch (err) {
    error.value = normalizeError(err) || 'Không tải được thông tin đợt giảm giá'
  } finally {
    loading.value = false
  }
}

async function loadProducts() {
  loadingProducts.value = true
  try {
    const res = await getSaleProducts(saleId.value)
    products.value = (res.data || []).map(mapProduct)
  } catch (err) {
    toast(normalizeError(err) || 'Không tải được danh sách sản phẩm', 'warn')
  } finally {
    loadingProducts.value = false
  }
}

async function loadAvailableProducts() {
  loadingAvailable.value = true
  try {
    const res = await getSanPhamBan(modalSearch.value || '', 0)
    const existingIds = new Set(products.value.map((p) => p.idChiTietSanPham))
    availableProducts.value = (res.data || []).filter(
      (p) => !existingIds.has(p.idChiTietSanPham),
    )
  } catch (err) {
    availableProducts.value = []
    toast(normalizeError(err) || 'Không tìm được sản phẩm', 'warn')
  } finally {
    loadingAvailable.value = false
  }
}

async function handleAddProducts() {
  if (!selectedIds.value.length) return
  adding.value = true
  try {
    for (const idChiTietSanPham of selectedIds.value) {
      await addSaleProduct(saleId.value, { idChiTietSanPham })
    }
    toast(`Đã thêm ${selectedIds.value.length} sản phẩm`, 'info')
    closeModal()
    await loadProducts()
  } catch (err) {
    toast(normalizeError(err) || 'Thêm sản phẩm thất bại', 'warn')
  } finally {
    adding.value = false
  }
}

async function removeProduct(product) {
  const ok = await confirm({
    title: 'Xóa sản phẩm',
    message: `Xóa "${product.name}" khỏi đợt giảm giá?`,
    confirmText: 'Xóa',
    danger: true,
  })
  if (!ok) return
  try {
    await deleteSaleProduct(saleId.value, product.id)
    toast('Đã xóa sản phẩm khỏi đợt', 'info')
    await loadProducts()
  } catch (err) {
    toast(normalizeError(err) || 'Xóa thất bại', 'warn')
  }
}

function closeModal() {
  showModal.value = false
  selectedIds.value = []
  modalSearch.value = ''
}

function onModalKeydown(event) {
  if (event.key === 'Escape' && showModal.value) {
    closeModal()
  }
}

let modalSearchTimer
watch(modalSearch, () => {
  if (!showModal.value) return
  clearTimeout(modalSearchTimer)
  modalSearchTimer = setTimeout(loadAvailableProducts, 300)
})

watch(showModal, (open) => {
  if (open) {
    selectedIds.value = []
    document.body.style.overflow = 'hidden'
    document.addEventListener('keydown', onModalKeydown)
    loadAvailableProducts()
  } else {
    document.body.style.overflow = ''
    document.removeEventListener('keydown', onModalKeydown)
  }
})

onUnmounted(() => {
  if (modalSearchTimer) clearTimeout(modalSearchTimer)
  document.body.style.overflow = ''
  document.removeEventListener('keydown', onModalKeydown)
})

onMounted(async () => {
  if (!saleId.value || Number.isNaN(saleId.value)) {
    error.value = 'ID đợt giảm giá không hợp lệ'
    loading.value = false
    return
  }
  await loadSale()
  if (!error.value) {
    await loadProducts()
  }
})
</script>

<style>
@import url('https://cdn.jsdelivr.net/npm/@tabler/icons-webfont@2.47.0/tabler-icons.min.css');
</style>
