<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import OrderCard from '@/components/storefront/OrderCard.vue'
import ProductReviewModal from '@/components/storefront/ProductReviewModal.vue'
import { confirm } from '@/composables/useConfirm'
import { fetchChiTietDonCuaToi, fetchDonCuaToi, huyDonCuaToi } from '@/api/donHangApi'

const search = ref('')
const currentFilter = ref('all')
const loading = ref(true)
const orders = ref([])
const error = ref('')
const showReviewModal = ref(false)
const reviewLine = ref(null)
const reviewNotice = ref('')
const cancelLoadingId = ref(null)
const cancelNotice = ref('')
const cancelError = ref('')

const filters = [
  { value: 'all', label: 'Tất cả' },
  { value: 'shipping', label: 'Đang giao' },
  { value: 'processing', label: 'Đang xử lý' },
  { value: 'delivered', label: 'Đã giao' },
  { value: 'cancelled', label: 'Đã hủy / Trả hàng' },
]

onMounted(loadOrders)

const filteredOrders = computed(() => {
  const q = search.value.trim().toLowerCase()
  return orders.value.filter((order) => {
    const matchFilter = currentFilter.value === 'all' || statusGroup(order.trangThai) === currentFilter.value
    const matchSearch = !q ||
      String(order.maHoaDon || '').toLowerCase().includes(q) ||
      (order.chiTiets || []).some((line) => String(line.tenSanPham || '').toLowerCase().includes(q))
    return matchFilter && matchSearch
  })
})

async function loadOrders() {
  loading.value = true
  error.value = ''
  try {
    const res = await fetchDonCuaToi()
    const summaries = res.data || []
    orders.value = await Promise.all(summaries.map(loadDetailOrSummary))
  } catch {
    error.value = 'Không tải được danh sách đơn hàng.'
  } finally {
    loading.value = false
  }
}

async function loadDetailOrSummary(summary) {
  try {
    const res = await fetchChiTietDonCuaToi(summary.id)
    return res.data || summary
  } catch {
    return { ...summary, chiTiets: [] }
  }
}

function statusGroup(status) {
  const map = {
    CHO_XAC_NHAN: 'processing',
    DA_XAC_NHAN: 'processing',
    DANG_CHUAN_BI: 'processing',
    DANG_GIAO: 'shipping',
    HOAN_THANH: 'delivered',
    TRA_HANG: 'cancelled',
    DA_HUY: 'cancelled',
  }
  return map[status] || 'processing'
}

function openReview(line) {
  reviewLine.value = line
  showReviewModal.value = true
}

function closeReview() {
  showReviewModal.value = false
  reviewLine.value = null
}

function onReviewSubmitted({ lineId }) {
  reviewNotice.value = 'Cảm ơn bạn đã đánh giá! Vui lòng chờ admin duyệt.'
  for (const order of orders.value) {
    const line = (order.chiTiets || []).find((item) => item.id === lineId)
    if (line) {
      line.daDanhGia = true
      line.trangThaiDanhGia = 'CHO_DUYET'
      break
    }
  }
}

async function handleCancelOrder(order) {
  if (!order?.id || cancelLoadingId.value) return

  const ok = await confirm({
    title: 'Hủy đơn hàng',
    message: `Bạn có chắc muốn hủy đơn ${order.maHoaDon}? Hành động này không thể hoàn tác.`,
    confirmText: 'Hủy đơn',
    danger: true,
  })
  if (!ok) return

  cancelLoadingId.value = order.id
  cancelError.value = ''
  cancelNotice.value = ''
  try {
    const res = await huyDonCuaToi(order.id)
    const updated = res.data || order
    const idx = orders.value.findIndex((item) => item.id === order.id)
    if (idx >= 0) {
      orders.value[idx] = updated
    }
    cancelNotice.value = `Đã hủy đơn ${order.maHoaDon}.`
  } catch (err) {
    cancelError.value = typeof err === 'string' ? err : 'Không hủy được đơn hàng. Vui lòng thử lại.'
  } finally {
    cancelLoadingId.value = null
  }
}
</script>

<template>
  <div class="sf-order-page">
    <div class="sf-container">
      <nav class="sf-breadcrumb">
        <RouterLink to="/">Trang chủ</RouterLink>
        <span>/</span>
        <span>Đơn hàng của tôi</span>
      </nav>

      <section class="sf-order-content">
      
        <div class="sf-order-filter-bar">
          <label class="sf-order-filter-search">
            <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
              <circle cx="11" cy="11" r="8" />
              <path d="m21 21-4.35-4.35" />
            </svg>
            <input v-model="search" type="search" placeholder="Tìm theo tên sản phẩm, mã đơn..." autocomplete="off" />
          </label>

          <div class="sf-order-filter-chips">
            <button
              v-for="filter in filters"
              :key="filter.value"
              type="button"
              class="sf-order-chip"
              :class="{ 'sf-order-chip--active': currentFilter === filter.value }"
              @click="currentFilter = filter.value"
            >
              {{ filter.label }}
            </button>
          </div>
        </div>

        <p v-if="error" class="sf-order-msg sf-order-msg--err">{{ error }}</p>
        <p v-if="cancelError" class="sf-order-msg sf-order-msg--err">{{ cancelError }}</p>
        <p v-if="reviewNotice" class="sf-order-msg sf-order-msg--ok">{{ reviewNotice }}</p>
        <p v-if="cancelNotice" class="sf-order-msg sf-order-msg--ok">{{ cancelNotice }}</p>

        <div v-if="loading" class="sf-order-skeleton" />

        <div v-else-if="filteredOrders.length" class="sf-order-list">
          <OrderCard
            v-for="(order, idx) in filteredOrders"
            :key="order.id"
            :order="order"
            :default-open="idx === 0"
            :cancel-loading="cancelLoadingId === order.id"
            @review="openReview"
            @cancel-order="handleCancelOrder"
          />
        </div>

        <div v-else class="sf-order-empty">
          <div class="sf-order-empty__emoji">📦</div>
          <h3>Không có đơn hàng nào</h3>
          <p>Chưa có đơn hàng phù hợp với bộ lọc bạn chọn.</p>
        </div>
      </section>
    </div>

    <ProductReviewModal
      :visible="showReviewModal"
      :line="reviewLine"
      @close="closeReview"
      @submitted="onReviewSubmitted"
    />
  </div>
</template>
