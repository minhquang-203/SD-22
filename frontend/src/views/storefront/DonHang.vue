<script setup>
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { Icon } from '@iconify/vue'
import OrderCard from '@/components/storefront/OrderCard.vue'
import ProductReviewModal from '@/components/storefront/ProductReviewModal.vue'
import ReturnRequestModal from '@/components/storefront/ReturnRequestModal.vue'
import { confirm } from '@/composables/useConfirm'
import { getCustomerId } from '@/composables/useAuth'
import { toast } from '@/composables/useToast'
import { fetchChiTietDonCuaToi, fetchDonCuaToi, huyDonCuaToi } from '@/api/donHangApi'
import { taoVanDonTra } from '@/api/traHangApi'
import { formatVND } from '@/utils/formatVND'
import { formatOrderDate, orderStatusClass, orderStatusLabel } from '@/utils/orderStatus'

const loading = ref(true)
const orders = ref([])
const selectedId = ref(null)
const detail = ref(null)
const detailLoading = ref(false)
const error = ref('')
const showReviewModal = ref(false)
const reviewLine = ref(null)
const reviewNotice = ref('')
const cancelLoading = ref(false)
const cancelNotice = ref('')
const cancelError = ref('')
const showReturnModal = ref(false)
const returnOrder = ref(null)
const returnActionLoading = ref(false)
const returnNotice = ref('')

onMounted(loadList)

async function loadList() {
  loading.value = true
  error.value = ''
  try {
    const res = await fetchDonCuaToi()
    orders.value = res.data || []
  } catch {
    error.value = 'Không tải được danh sách đơn hàng.'
  } finally {
    loading.value = false
  }
}

async function openDetail(id) {
  if (selectedId.value === id && detail.value) {
    selectedId.value = null
    detail.value = null
    return
  }
  selectedId.value = id
  detailLoading.value = true
  detail.value = null
  try {
    const res = await fetchChiTietDonCuaToi(id)
    detail.value = res.data
  } catch {
    error.value = 'Không tải được chi tiết đơn hàng.'
    selectedId.value = null
  } finally {
    detailLoading.value = false
  }
}

async function reloadDetail(id) {
  if (!id) return
  try {
    const res = await fetchChiTietDonCuaToi(id)
    detail.value = res.data
    const idx = orders.value.findIndex((item) => item.id === id)
    if (idx >= 0 && res.data) {
      orders.value[idx] = {
        ...orders.value[idx],
        trangThai: res.data.trangThai,
        trangThaiLabel: res.data.trangThaiLabel,
      }
    }
  } catch {
    // giữ nguyên detail cũ
  }
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
  const line = (detail.value?.chiTiets || []).find((item) => item.id === lineId)
  if (line) {
    line.daDanhGia = true
    line.trangThaiDanhGia = 'CHO_DUYET'
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

async function onReturnSubmitted() {
  returnNotice.value = 'Đã gửi yêu cầu trả hàng. Cửa hàng sẽ sớm phản hồi.'
  await reloadDetail(selectedId.value || returnOrder.value?.id)
}

async function handleCreateReturnLabel(order) {
  if (!order?.idYeuCauTraHang || returnActionLoading.value) return

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

  returnActionLoading.value = true
  try {
    const res = await taoVanDonTra(order.idYeuCauTraHang, idKhachHang)
    const updated = res.data
    if (detail.value && updated) {
      detail.value = {
        ...detail.value,
        trangThaiTraHang: updated.trangThai,
        trangThaiTraHangLabel: updated.trangThaiLabel,
        maVanDonTra: updated.maVanDonTra,
        idYeuCauTraHang: updated.id,
      }
    }
    returnNotice.value = updated?.maVanDonTra
      ? `Đã tạo vận đơn hoàn: ${updated.maVanDonTra}`
      : 'Đã tạo vận đơn hoàn hàng.'
    toast(returnNotice.value, 'info')
  } catch (err) {
    toast(typeof err === 'string' ? err : 'Không tạo được vận đơn hoàn hàng.', 'warn')
  } finally {
    returnActionLoading.value = false
  }
}

async function handleCancelOrder(order) {
  if (!order?.id || cancelLoading.value) return

  const ok = await confirm({
    title: 'Hủy đơn hàng',
    message: `Bạn có chắc muốn hủy đơn ${order.maHoaDon}? Hành động này không thể hoàn tác.`,
    confirmText: 'Hủy đơn',
    danger: true,
  })
  if (!ok) return

  cancelLoading.value = true
  cancelError.value = ''
  cancelNotice.value = ''
  try {
    const res = await huyDonCuaToi(order.id)
    detail.value = res.data || order
    const idx = orders.value.findIndex((item) => item.id === order.id)
    if (idx >= 0) {
      orders.value[idx] = {
        ...orders.value[idx],
        trangThai: detail.value.trangThai,
        trangThaiLabel: detail.value.trangThaiLabel,
      }
    }
    cancelNotice.value = `Đã hủy đơn ${order.maHoaDon}.`
  } catch (err) {
    cancelError.value = typeof err === 'string' ? err : 'Không hủy được đơn hàng. Vui lòng thử lại.'
  } finally {
    cancelLoading.value = false
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

      <h1 class="sf-order-page__title">Đơn hàng của tôi</h1>
      <p class="sf-order-page__desc">Lịch sử mua hàng khi bạn đăng nhập tài khoản SUNOVA.</p>

      <p v-if="error" class="sf-order-msg sf-order-msg--err">{{ error }}</p>
      <p v-if="cancelError" class="sf-order-msg sf-order-msg--err">{{ cancelError }}</p>
      <p v-if="reviewNotice" class="sf-order-msg sf-order-msg--ok">{{ reviewNotice }}</p>
      <p v-if="cancelNotice" class="sf-order-msg sf-order-msg--ok">{{ cancelNotice }}</p>
      <p v-if="returnNotice" class="sf-order-msg sf-order-msg--ok">{{ returnNotice }}</p>

      <div v-if="loading" class="sf-order-skeleton" />

      <div v-else-if="!orders.length" class="sf-order-empty">
        <Icon icon="solar:box-linear" width="56" class="sf-order-empty__icon" />
        <p>Bạn chưa có đơn hàng nào.</p>
        <RouterLink to="/san-pham" class="btn-soleil btn-soleil--espresso">
          <span>Tiếp tục mua sắm</span>
        </RouterLink>
      </div>

      <div v-else class="sf-order-history">
        <div class="sf-order-list">
          <button
            v-for="item in orders"
            :key="item.id"
            type="button"
            class="sf-order-list__item"
            :class="{ active: selectedId === item.id }"
            @click="openDetail(item.id)"
          >
            <div class="sf-order-list__main">
              <strong>{{ item.maHoaDon }}</strong>
              <span>{{ formatOrderDate(item.ngayTao) }}</span>
            </div>
            <span
              class="sf-order-badge"
              :class="orderStatusClass(item.trangThai)"
            >
              {{ orderStatusLabel(item.trangThai) }}
            </span>
            <strong class="sf-order-list__price">{{ formatVND(item.thanhTien) }}</strong>
          </button>
        </div>

        <div v-if="selectedId" class="sf-order-detail-panel">
          <div v-if="detailLoading" class="sf-order-skeleton sf-order-skeleton--short" />
          <OrderCard
            v-else-if="detail"
            :order="detail"
            :cancel-loading="cancelLoading"
            :return-action-loading="returnActionLoading"
            @review="openReview"
            @cancel-order="handleCancelOrder"
            @request-return="openReturn"
            @create-return-label="handleCreateReturnLabel"
          />
        </div>
      </div>
    </div>

    <ProductReviewModal
      :visible="showReviewModal"
      :line="reviewLine"
      @close="closeReview"
      @submitted="onReviewSubmitted"
    />

    <ReturnRequestModal
      :visible="showReturnModal"
      :order="returnOrder"
      @close="closeReturn"
      @submitted="onReturnSubmitted"
    />
  </div>
</template>
