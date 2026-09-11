<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import AccountSidebar from '@/components/storefront/AccountSidebar.vue'
import OrderCard from '@/components/storefront/OrderCard.vue'
import ProductReviewModal from '@/components/storefront/ProductReviewModal.vue'
import ReturnRequestCodModal from '@/components/storefront/ReturnRequestCodModal.vue'
import ReturnRequestWalletModal from '@/components/storefront/ReturnRequestWalletModal.vue'
import { confirm } from '@/composables/useConfirm'
import { toast } from '@/composables/useToast'
import { subscribeCustomerOrders } from '@/composables/useRealtime'
import { fetchChiTietDonCuaToi, fetchDonCuaToi, huyDonCuaToi } from '@/api/donHangApi'

const route = useRoute()
const router = useRouter()
const search = ref(typeof route.query.ma === 'string' ? route.query.ma : '')
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
const showReturnModal = ref(false)
const returnOrder = ref(null)
const returnNotice = ref('')

let unsubscribeRealtime = null

const filters = [
  { value: 'all', label: 'Tất cả' },
  { value: 'shipping', label: 'Đang giao' },
  { value: 'processing', label: 'Đang xử lý' },
  { value: 'delivered', label: 'Đã giao' },
  { value: 'cancelled', label: 'Đã hủy' },
  { value: 'returned', label: 'Trả hàng' },
]

onMounted(() => {
  loadOrders()
  unsubscribeRealtime = subscribeCustomerOrders(async (event) => {
    if (!event?.idHoaDon) return
    await applyRealtimeOrder(event)
  })
})

onUnmounted(() => {
  unsubscribeRealtime?.()
  unsubscribeRealtime = null
})

watch(
  () => route.query.ma,
  (ma) => {
    if (typeof ma === 'string') search.value = ma
  },
)

const filteredOrders = computed(() => {
  const q = search.value.trim().toLowerCase()
  return orders.value.filter((order) => {
    const matchFilter = currentFilter.value === 'all' || statusGroup(order) === currentFilter.value
    const matchSearch = !q
      || String(order.maHoaDon || '').toLowerCase().includes(q)
      || (order.chiTiets || []).some((line) => String(line.tenSanPham || '').toLowerCase().includes(q))
    return matchFilter && matchSearch
  })
})

/** Xóa mã đơn dính từ "Xem đơn gốc" (?ma=) để bộ lọc hoạt động lại. */
function clearStickyMaQuery() {
  if (typeof route.query.ma !== 'string') return
  search.value = ''
  router.replace({ path: route.path, query: {} })
}

function applyFilter(filterValue) {
  currentFilter.value = filterValue
  // Chỉ lọc client-side — không reload API (trước đây "Tất cả" gọi lại loadOrders → 1+N request + GHN).
  if (filterValue === 'all') {
    search.value = ''
    if (route.query.ma != null) {
      router.replace({ path: route.path, query: {} })
    }
    return
  }
  clearStickyMaQuery()
}

async function loadOrders() {
  loading.value = true
  error.value = ''
  try {
    const res = await fetchDonCuaToi()
    // Dùng summary cho danh sách; chi tiết (+ GHN) chỉ tải khi mở thẻ.
    orders.value = (res.data || []).map((summary) => ({
      ...summary,
      chiTiets: summary.chiTiets || [],
    }))
  } catch {
    error.value = 'Không tải được danh sách đơn hàng.'
  } finally {
    loading.value = false
  }
}

const detailLoadingIds = ref(new Set())

async function ensureOrderDetail(order) {
  if (!order?.id || order.__detailLoaded) return
  if (detailLoadingIds.value.has(order.id)) return

  const next = new Set(detailLoadingIds.value)
  next.add(order.id)
  detailLoadingIds.value = next
  try {
    const res = await fetchChiTietDonCuaToi(order.id)
    const detail = res.data
    if (!detail) return
    const idx = orders.value.findIndex((item) => item.id === order.id)
    if (idx < 0) return
    const summary = orders.value[idx]
    orders.value[idx] = {
      ...summary,
      ...detail,
      maVanDon: detail.maVanDon || summary.maVanDon || '',
      donViVanChuyen: detail.donViVanChuyen || summary.donViVanChuyen || '',
      __detailLoaded: true,
    }
  } catch {
    // Giữ summary; body sẽ hiện thiếu chi tiết nhẹ.
  } finally {
    const done = new Set(detailLoadingIds.value)
    done.delete(order.id)
    detailLoadingIds.value = done
  }
}

async function applyRealtimeOrder(event) {
  const orderId = Number(event.idHoaDon)
  const idx = orders.value.findIndex((item) => Number(item.id) === orderId)

  try {
    const res = await fetchChiTietDonCuaToi(orderId)
    if (res.data) {
      if (idx >= 0) {
        orders.value[idx] = { ...res.data, __detailLoaded: true }
      } else if (event.type === 'ORDER_CREATED') {
        orders.value = [{ ...res.data, __detailLoaded: true }, ...orders.value]
      }
    } else if (idx >= 0 && event.trangThai) {
      orders.value[idx] = {
        ...orders.value[idx],
        trangThai: event.trangThai,
        trangThaiLabel: event.trangThaiLabel || orders.value[idx].trangThaiLabel,
      }
    }
  } catch {
    if (idx >= 0 && event.trangThai) {
      orders.value[idx] = {
        ...orders.value[idx],
        trangThai: event.trangThai,
        trangThaiLabel: event.trangThaiLabel || orders.value[idx].trangThaiLabel,
      }
    }
  }

  if (event.type === 'ORDER_STATUS_CHANGED') {
    toast(
      event.message || `Đơn ${event.maHoaDon || ''} đã cập nhật: ${event.trangThaiLabel || event.trangThai || ''}`,
      event.trangThai === 'DA_HUY' ? 'warning' : 'info',
    )
  } else if (event.type === 'ORDER_CREATED') {
    toast(event.message || `Đơn mới: ${event.maHoaDon || ''}`, 'info')
  }
}

function statusGroup(order) {
  if (order?.idYeuCauTraHang || order?.trangThaiTraHang || order?.trangThai === 'TRA_HANG') {
    return 'returned'
  }
  const map = {
    CHO_XAC_NHAN: 'processing',
    DA_XAC_NHAN: 'processing',
    DANG_CHUAN_BI: 'processing',
    DANG_GIAO: 'shipping',
    HOAN_THANH: 'delivered',
    DA_HUY: 'cancelled',
  }
  return map[order?.trangThai] || 'processing'
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
  reviewNotice.value = 'Cảm ơn bạn đã đánh giá! Đánh giá của bạn đã được hiển thị.'
  for (const order of orders.value) {
    const line = (order.chiTiets || []).find((item) => item.id === lineId)
    if (line) {
      line.daDanhGia = true
      line.trangThaiDanhGia = 'DA_DUYET'
      break
    }
  }
}

function openReturn(order) {
  returnOrder.value = order
  showReturnModal.value = true
}

function closeReturn() {
  showReturnModal.value = false
  returnOrder.value = null
}

function onReturnSubmitted(result) {
  closeReturn()
  const returnId = result?.id
  if (returnId) {
    router.push(`/tra-cuu-don/tra-hang/${returnId}`)
    return
  }
  returnNotice.value = 'Đã gửi yêu cầu trả hàng. Cửa hàng sẽ sớm phản hồi.'
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
      orders.value[idx] = { ...updated, __detailLoaded: true }
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
  <div class="sf-account-page">
    <div class="sf-container">
      <nav class="sf-breadcrumb">
        <RouterLink to="/">Trang chủ</RouterLink>
        <span>/</span>
        <RouterLink to="/tai-khoan">Tài khoản</RouterLink>
        <span>/</span>
        <span>Tra cứu đơn</span>
      </nav>

      <h1 class="sf-account-page__title">Trung tâm tài khoản</h1>

      <div class="sf-account-layout">
        <AccountSidebar />

        <div class="sf-account-main sf-account-main--orders">
          <h2 class="sf-account-main__heading">Tra cứu đơn</h2>
          <p class="sf-account-main__sub">Tìm và theo dõi đơn hàng của bạn theo mã hóa đơn hoặc tên sản phẩm.</p>

          <div class="sf-order-filter-bar">
            <label class="sf-order-filter-search">
              <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <circle cx="11" cy="11" r="8" />
                <path d="m21 21-4.35-4.35" />
              </svg>
              <input v-model="search" type="search" placeholder="Tìm theo tên sản phẩm, mã hóa đơn..." autocomplete="off" />
            </label>

            <div class="sf-order-filter-chips">
              <button
                v-for="filter in filters"
                :key="filter.value"
                type="button"
                class="sf-order-chip"
                :class="{ 'sf-order-chip--active': currentFilter === filter.value }"
                @click="applyFilter(filter.value)"
              >
                {{ filter.label }}
              </button>
            </div>
          </div>

          <p v-if="error" class="sf-order-msg sf-order-msg--err">{{ error }}</p>
          <p v-if="cancelError" class="sf-order-msg sf-order-msg--err">{{ cancelError }}</p>
          <p v-if="reviewNotice" class="sf-order-msg sf-order-msg--ok">{{ reviewNotice }}</p>
          <p v-if="cancelNotice" class="sf-order-msg sf-order-msg--ok">{{ cancelNotice }}</p>
          <p v-if="returnNotice" class="sf-order-msg sf-order-msg--ok">{{ returnNotice }}</p>

          <div v-if="loading" class="sf-order-skeleton" />

          <div v-else-if="filteredOrders.length" class="sf-order-list">
            <OrderCard
              v-for="order in filteredOrders"
              :key="order.id"
              :order="order"
              :default-open="false"
              :cancel-loading="cancelLoadingId === order.id"
              :detail-loading="detailLoadingIds.has(order.id)"
              @expand="ensureOrderDetail"
              @review="openReview"
              @cancel-order="handleCancelOrder"
              @request-return="openReturn"
            />
          </div>

          <div v-else class="sf-order-empty">
            <div class="sf-order-empty__emoji">📦</div>
            <h3>Không có đơn hàng nào</h3>
            <p>Chưa có đơn hàng phù hợp với bộ lọc bạn chọn.</p>
          </div>
        </div>
      </div>
    </div>

    <ProductReviewModal
      :visible="showReviewModal"
      :line="reviewLine"
      @close="closeReview"
      @submitted="onReviewSubmitted"
    />

    <ReturnRequestCodModal
      v-if="returnOrder && String(returnOrder.maPhuongThucThanhToan || '').toUpperCase() === 'COD'"
      :visible="showReturnModal"
      :order="returnOrder"
      @close="closeReturn"
      @submitted="onReturnSubmitted"
    />
    <ReturnRequestWalletModal
      v-else-if="returnOrder"
      :visible="showReturnModal"
      :order="returnOrder"
      @close="closeReturn"
      @submitted="onReturnSubmitted"
    />
  </div>
</template>
