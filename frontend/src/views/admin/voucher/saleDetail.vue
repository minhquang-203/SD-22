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
        <div class="sd-campaign-header__top">
          <div class="sd-campaign-header__meta">
            <span class="sd-badge" :class="statusBadgeClass">{{ sale.timeStatusLabel }}</span>
            <span class="sd-campaign-code">{{ sale.ma }}</span>
          </div>
          <div class="sd-campaign-actions">
            <button class="sd-btn-ghost" @click="router.push('/admin/sale')">
              <svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M10 19l-7-7m0 0l7-7m-7 7h18"/></svg>
              Quay lại
            </button>
            <button class="sd-btn-primary" @click="showModal = true">
              <svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/></svg>
              Thêm sản phẩm
            </button>
          </div>
        </div>

        <div class="sd-campaign-title-row">
          <h1 class="sd-campaign-title">{{ sale.ten }}</h1>
          <span class="sd-discount-chip">−{{ sale.phanTramGiam }}%</span>
        </div>

        <div class="sd-campaign-facts">
          <span class="sd-fact">
            <svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M8 7V3m8 4V3M3 11h18M5 5h14a2 2 0 012 2v12a2 2 0 01-2 2H5a2 2 0 01-2-2V7a2 2 0 012-2z"/></svg>
            {{ formatDate(sale.ngayBatDau) }} — {{ formatDate(sale.ngayKetThuc) }}
          </span>
          <span class="sd-fact">
            <svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/></svg>
            {{ products.length }} sản phẩm áp dụng
          </span>
          <span class="sd-fact sd-fact--accent">{{ timeRemainingLabel }}</span>
        </div>

        <div class="sd-timeline">
          <div class="sd-timeline__track">
            <div class="sd-timeline__fill" :style="{ width: progressPercent + '%' }"></div>
          </div>
          <div class="sd-timeline__footer">
            <span>{{ formatDate(sale.ngayBatDau) }}</span>
            <span class="highlight">{{ progressPercent }}% thời gian</span>
            <span>{{ formatDate(sale.ngayKetThuc) }}</span>
          </div>
        </div>
      </div>

      <div class="sd-layout">

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
              <div class="sd-product-card__image">
                <img :src="productImageUrl(product.anhUrl)" :alt="product.name" loading="lazy" />
                <div class="sd-product-card__overlay">
                  <button class="sd-overlay-btn sd-overlay-btn--danger" @click="removeProduct(product)">
                    <i class="ti ti-trash"></i> Xóa
                  </button>
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
                <div class="sd-product-list-item__thumb">
                  <img :src="productImageUrl(product.anhUrl)" :alt="product.name" loading="lazy" />
                </div>
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
                <label
                  v-for="ap in availableProducts"
                  :key="ap.idChiTietSanPham"
                  class="sd-product-picker__item"
                  :class="{ 'is-selected': selectedIds.includes(ap.idChiTietSanPham) }"
                >
                  <input type="checkbox" :value="ap.idChiTietSanPham" v-model="selectedIds" />
                  <div class="sd-product-picker__item-thumb">
                    <img :src="productImageUrl(ap.anhUrl)" :alt="ap.tenSanPham" loading="lazy" />
                  </div>
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
import { productImageUrl } from '@/utils/productImage'
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

const filteredProducts = computed(() => {
  const q = searchQuery.value.trim().toLowerCase()
  if (!q) return products.value
  return products.value.filter((p) =>
    p.name.toLowerCase().includes(q) || p.sku.toLowerCase().includes(q),
  )
})

function mapProduct(item) {
  const priceOld = Number(item.giaBan || 0)
  const priceNew = Number(item.giaSauGiam || 0)

  return {
    id: item.id,
    idChiTietSanPham: item.idChiTietSanPham,
    sku: item.sku || '—',
    name: item.tenSanPham || 'Không rõ tên',
    anhUrl: item.anhUrl || '',
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
