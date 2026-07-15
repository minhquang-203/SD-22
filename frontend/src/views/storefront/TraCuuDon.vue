<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import OrderCard from '@/components/storefront/OrderCard.vue'
import ProductReviewModal from '@/components/storefront/ProductReviewModal.vue'
import ReturnRequestCodModal from '@/components/storefront/ReturnRequestCodModal.vue'
import ReturnRequestWalletModal from '@/components/storefront/ReturnRequestWalletModal.vue'
import { confirm } from '@/composables/useConfirm'
import { getCustomerId } from '@/composables/useAuth'
import { toast } from '@/composables/useToast'
import { fetchChiTietDonCuaToi, fetchDonCuaToi, huyDonCuaToi } from '@/api/donHangApi'
import { taoVanDonTra } from '@/api/traHangApi'

const route = useRoute()
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
const returnActionLoadingId = ref(null)
const returnNotice = ref('')

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

function openReturn(order) {
  returnOrder.value = order
  showReturnModal.value = true
}

function closeReturn() {
  showReturnModal.value = false
  returnOrder.value = null
}

async function onReturnSubmitted(result) {
  returnNotice.value = 'Đã gửi yêu cầu trả hàng. Cửa hàng sẽ sớm phản hồi.'
  const orderId = returnOrder.value?.id || result?.idHoaDon
  if (!orderId) return
  try {
    const res = await fetchChiTietDonCuaToi(orderId)
    const idx = orders.value.findIndex((item) => item.id === orderId)
    if (idx >= 0 && res.data) {
      orders.value[idx] = res.data
    }
  } catch {
    // giữ nguyên
  }
}

async function handleCreateReturnLabel(order) {
  if (!order?.idYeuCauTraHang || returnActionLoadingId.value) return

  const idKhachHang = getCustomerId()
  if (!idKhachHang) {
    toast('Vui lòng đăng nhập để tạo vận đơn hoàn hàng.', 'warn')
    return
  }

  const ok = await confirm({
    title: 'Tạo vận đơn hoàn hàng',
    message: `Tạo vận đơn GHN để gửi hàng hoàn cho đơn ${order.maHoaDon}?`,
    confirmText: 'Tạo vận đơn',
  })
  if (!ok) return

  returnActionLoadingId.value = order.id
  try {
    const res = await taoVanDonTra(order.idYeuCauTraHang, idKhachHang)
    const updated = res.data
    const idx = orders.value.findIndex((item) => item.id === order.id)
    if (idx >= 0 && updated) {
      orders.value[idx] = {
        ...orders.value[idx],
        trangThaiTraHang: updated.trangThai,
        trangThaiTraHangLabel: updated.trangThaiLabel,
        maVanDonTra: updated.maVanDonTra,
        idYeuCauTraHang: updated.id,
        coTheYeuCauTraHang: false,
      }
    }
    returnNotice.value = updated?.maVanDonTra
      ? `Đã tạo vận đơn hoàn: ${updated.maVanDonTra}`
      : 'Đã tạo vận đơn hoàn hàng.'
    toast(returnNotice.value, 'info')
  } catch (err) {
    toast(typeof err === 'string' ? err : 'Không tạo được vận đơn hoàn hàng.', 'warn')
  } finally {
    returnActionLoadingId.value = null
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
        <p v-if="returnNotice" class="sf-order-msg sf-order-msg--ok">{{ returnNotice }}</p>

        <div v-if="loading" class="sf-order-skeleton" />

        <div v-else-if="filteredOrders.length" class="sf-order-list">
          <OrderCard
            v-for="(order, idx) in filteredOrders"
            :key="order.id"
            :order="order"
            :default-open="idx === 0"
            :cancel-loading="cancelLoadingId === order.id"
            :return-action-loading="returnActionLoadingId === order.id"
            @review="openReview"
            @cancel-order="handleCancelOrder"
            @request-return="openReturn"
            @create-return-label="handleCreateReturnLabel"
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
