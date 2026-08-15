<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import { formatVND } from '@/utils/formatVND'
import { productImageUrl } from '@/utils/productImage'
import { formatOrderDate, orderStatusLabel, orderStatusClass, coTheHuyDon } from '@/utils/orderStatus'
import { traHangStatusLabel, traHangStatusClass } from '@/utils/returnStatus'

const props = defineProps({
  order: { type: Object, required: true },
  defaultOpen: { type: Boolean, default: false },
  cancelLoading: { type: Boolean, default: false },
  returnActionLoading: { type: Boolean, default: false },
})

const emit = defineEmits(['review', 'cancelOrder', 'requestReturn'])

const router = useRouter()
const hasReturnRequest = computed(() => Boolean(props.order?.idYeuCauTraHang))
const isOpen = ref(Boolean(props.defaultOpen) && !hasReturnRequest.value)

const steps = ['Đặt hàng', 'Chờ Xác nhận', 'Đã xác nhận', 'Vận chuyển', 'Đã nhận']

const timelineSteps = computed(() => {
  const status = props.order?.trangThai
  const map = {
    CHO_XAC_NHAN: ['done', 'active', '', '', ''],
    DA_XAC_NHAN: ['done', 'done', 'active', '', ''],
    DANG_CHUAN_BI: ['done', 'done', 'done', 'active', ''],
    DANG_GIAO: ['done', 'done', 'done', 'active', ''],
    HOAN_THANH: ['done', 'done', 'done', 'done', 'done'],
    TRA_HANG: ['done', 'done', 'done', '', ''],
    DA_HUY: ['done', '', '', '', ''],
  }
  return map[status] || ['done', 'active', '', '', '']
})

const isCancelled = computed(() => props.order?.trangThai === 'DA_HUY')

const isReturned = computed(() => props.order?.trangThai === 'TRA_HANG')

const canCancel = computed(() => coTheHuyDon(props.order?.trangThai))

const canRequestReturn = computed(() => props.order?.coTheYeuCauTraHang === true)

const headerStatus = computed(() => {
  if (props.order?.trangThaiTraHang) {
    return props.order.trangThaiTraHangLabel || traHangStatusLabel(props.order.trangThaiTraHang)
  }
  return orderStatusLabel(props.order?.trangThai)
})

const headerStatusClass = computed(() => {
  if (props.order?.trangThaiTraHang) {
    return traHangStatusClass(props.order.trangThaiTraHang)
  }
  return orderStatusClass(props.order?.trangThai)
})

const paymentLabel = computed(() => {
  const ma = String(props.order?.maPhuongThucThanhToan || '').toUpperCase()
  const map = {
    COD: 'Thanh toán khi nhận hàng',
    VNPAY: 'VNPAY',
    CHUYEN_KHOAN: 'Chuyển khoản',
  }
  return map[ma] || ''
})

const productPreview = computed(() => {
  const order = props.order || {}
  const lines = order.chiTiets || []
  const first = lines[0]
  if (first) {
    return {
      anhUrl: first.anhUrl,
      tenSanPham: first.tenSanPham || 'Đơn hàng SUNOVA',
      soLuong: first.soLuong,
      more: lines.length > 1 ? `+${lines.length - 1} sản phẩm` : '',
    }
  }
  const soDong = Number(order.soDongHang || 0)
  return {
    anhUrl: order.anhUrl,
    tenSanPham: order.tenSanPham || 'Đơn hàng SUNOVA',
    soLuong: order.soLuong,
    more: soDong > 1 ? `+${soDong - 1} sản phẩm` : '',
  }
})

const shippingInfo = computed(() => {
  const order = props.order || {}
  let statusLabel = order.ghnTrangThaiLabel
  if (order.trangThai === 'HOAN_THANH' || order.trangThai === 'TRA_HANG') {
    statusLabel = 'Đã giao'
  }
  const showEta = Boolean(
    order.ghnHenGiao && order.trangThai !== 'HOAN_THANH' && order.trangThai !== 'TRA_HANG',
  )
  return {
    carrier: order.donViVanChuyen || (order.maVanDon ? 'Giao hàng nhanh' : ''),
    trackingCode: order.maVanDon || '',
    statusLabel: statusLabel || '',
    eta: showEta ? formatOrderDate(order.ghnHenGiao) : '',
  }
})

function onHeadClick() {
  if (hasReturnRequest.value && props.order?.idYeuCauTraHang) {
    router.push(`/tra-cuu-don/tra-hang/${props.order.idYeuCauTraHang}`)
    return
  }
  isOpen.value = !isOpen.value
}

function canReview(line) {
  const delivered = props.order?.trangThai === 'HOAN_THANH' || props.order?.trangThai === 'GIAO_THANH_CONG'
  return delivered && !line?.daDanhGia && line?.idSanPham
}
</script>

<template>
  <article v-if="order" class="sf-order-card" :class="{ open: isOpen && !hasReturnRequest }">
    <button type="button" class="sf-order-card__head" @click="onHeadClick">
      <div class="sf-order-card__head-main">
        <div v-if="order.maHoaDon || order.maVanDonTra" class="sf-order-card__codes">
          <p v-if="order.maHoaDon" class="sf-order-card__code">{{ order.maHoaDon }}</p>
          <span v-if="order.maVanDonTra" class="sf-order-card__return-code">
            {{ order.maVanDonTra }}
          </span>
        </div>
        <div class="sf-order-card__head-row">
          <div class="sf-order-card__head-left">
            <img
              :src="productImageUrl(productPreview.anhUrl)"
              :alt="productPreview.tenSanPham"
              class="sf-order-card__thumb"
            />
            <div class="sf-order-card__preview-wrap">
              <div class="sf-order-card__preview">
                {{ productPreview.tenSanPham }}
              </div>
              <div class="sf-order-card__preview-meta">
                <span v-if="productPreview.soLuong">x{{ productPreview.soLuong }}</span>
                <span v-if="productPreview.more">{{ productPreview.more }}</span>
              </div>
            </div>
          </div>

          <div class="sf-order-card__head-right">
            <div class="sf-order-card__head-summary">
              <strong class="sf-order-card__amount">{{ formatVND(order.thanhTien) }}</strong>
              <span class="sf-order-badge" :class="headerStatusClass">
                <span class="sf-order-badge__dot"></span>
                {{ headerStatus }}
              </span>
            </div>
            <span class="sf-order-card__chevron">{{ hasReturnRequest ? '›' : '▾' }}</span>
          </div>
        </div>
      </div>
    </button>

    <div v-if="!hasReturnRequest" class="sf-order-card__body">
      <div v-if="order.ngayTao || paymentLabel" class="sf-detail-meta">
        <span v-if="order.ngayTao" class="sf-info-chip">
          <Icon icon="mdi:calendar-blank-outline" width="14" />
          {{ formatOrderDate(order.ngayTao) }}
        </span>
        <span v-if="paymentLabel" class="sf-info-chip">
          <Icon icon="mdi:wallet-outline" width="14" />
          {{ paymentLabel }}
        </span>
      </div>

      <div v-if="!isCancelled && !isReturned" class="sf-order-timeline">
        <div class="sf-order-timeline__row">
          <div
            v-for="(step, idx) in steps"
            :key="step"
            class="sf-order-timeline__step"
            :class="timelineSteps[idx]"
          >
            <div class="sf-order-timeline__dot">
              {{ timelineSteps[idx] === 'done' ? '✓' : timelineSteps[idx] === 'active' ? '→' : '' }}
            </div>
            <div class="sf-order-timeline__text">{{ step }}</div>
          </div>
        </div>
      </div>

      <div class="sf-info-tiles">
        <div class="sf-info-tile">
          <div class="sf-info-tile__icon" aria-hidden="true">
            <Icon icon="mdi:map-marker-outline" width="18" />
          </div>
          <div class="sf-info-tile__body">
            <span class="sf-info-tile__label">Người nhận</span>
            <p class="sf-info-tile__value">{{ order.tenNguoiNhan || '—' }}</p>
            <div v-if="order.sdtNguoiNhan" class="sf-info-tile__chips">
              <span class="sf-info-chip">
                <Icon icon="mdi:phone-outline" width="14" />
                {{ order.sdtNguoiNhan }}
              </span>
            </div>
            <p class="sf-info-tile__sub">{{ order.diaChiGiao || 'Chưa có địa chỉ giao hàng' }}</p>
          </div>
        </div>

        <div class="sf-info-tile">
          <div class="sf-info-tile__icon sf-info-tile__icon--ship" aria-hidden="true">
            <Icon icon="mdi:truck-delivery-outline" width="18" />
          </div>
          <div class="sf-info-tile__body">
            <span class="sf-info-tile__label">Vận chuyển</span>
            <p class="sf-info-tile__value">
              {{ shippingInfo.carrier || 'Chưa có thông tin vận chuyển' }}
            </p>
            <div v-if="shippingInfo.trackingCode || shippingInfo.statusLabel" class="sf-info-tile__chips">
              <span v-if="shippingInfo.trackingCode" class="sf-code-pill">
                {{ shippingInfo.trackingCode }}
              </span>
              <span v-if="shippingInfo.statusLabel" class="sf-info-chip sf-info-chip--status">
                {{ shippingInfo.statusLabel }}
              </span>
            </div>
            <p v-if="shippingInfo.eta" class="sf-info-tile__sub sf-info-tile__sub--accent">
              Dự kiến giao {{ shippingInfo.eta }}
            </p>
          </div>
        </div>
      </div>

      <div class="sf-detail-section-head">
        <Icon icon="mdi:package-variant-closed" width="16" />
        Sản phẩm
      </div>
      <ul class="sf-order-card__lines">
        <li v-for="(line, idx) in order.chiTiets || []" :key="idx" class="sf-order-line">
          <img
            :src="productImageUrl(line.anhUrl)"
            :alt="line.tenSanPham"
            class="sf-order-line__img"
          />
          <div class="sf-order-line__body">
            <p class="sf-order-line__name">{{ line.tenSanPham }}</p>
            <p v-if="line.bienThe" class="sf-order-line__variant">{{ line.bienThe }}</p>
            <p class="sf-order-line__qty">x{{ line.soLuong }} · {{ formatVND(line.donGia) }}</p>

            <button
              v-if="canReview(line)"
              type="button"
              class="sf-btn-review"
              @click.stop="$emit('review', line)"
            >
              Đánh giá
            </button>
            <span v-else-if="line.daDanhGia && line.trangThaiDanhGia === 'CHO_DUYET'" class="sf-review-status sf-review-status--pending">
              Đang chờ duyệt
            </span>
            <span v-else-if="line.daDanhGia" class="sf-review-status sf-review-status--done">
              Đã đánh giá
            </span>
          </div>
          <strong class="sf-order-line__total">{{ formatVND(line.thanhTien) }}</strong>
        </li>
      </ul>

      <div class="sf-order-card__totals">
        <div class="sf-order-card__row">
          <span>Tạm tính</span>
          <span>{{ formatVND(order.tongTien) }}</span>
        </div>
        <div v-if="order.tienGiamGia > 0" class="sf-order-card__row">
          <span>Giảm giá</span>
          <span>−{{ formatVND(order.tienGiamGia) }}</span>
        </div>
        <div v-if="order.phiVanChuyen > 0" class="sf-order-card__row">
          <span>Phí vận chuyển</span>
          <span>{{ formatVND(order.phiVanChuyen) }}</span>
        </div>
        <div class="sf-order-card__row sf-order-card__row--total">
          <span>Tổng thanh toán</span>
          <strong>{{ formatVND(order.thanhTien) }}</strong>
        </div>
      </div>

      <p
        v-if="order.capNhatGanNhatLuc"
        class="sf-order-card__latest"
      >
        Cập nhật gần nhất:
        {{ order.capNhatGanNhatLabel || order.capNhatGanNhatTrangThai }}
        · {{ formatOrderDate(order.capNhatGanNhatLuc) }}
      </p>

      <div v-if="canCancel || canRequestReturn" class="sf-order-card__actions">
        <button
          v-if="canCancel"
          type="button"
          class="sf-btn-cancel-order"
          :disabled="cancelLoading"
          @click.stop="emit('cancelOrder', order)"
        >
          {{ cancelLoading ? 'Đang hủy...' : 'Hủy đơn hàng' }}
        </button>
        <button
          v-if="canRequestReturn"
          type="button"
          class="sf-btn-return-order"
          :disabled="returnActionLoading"
          @click.stop="emit('requestReturn', order)"
        >
          Yêu cầu trả hàng
        </button>
        <p v-if="canCancel" class="sf-order-card__cancel-hint">
          Có thể hủy khi đơn chưa chuyển sang trạng thái đang giao.
        </p>
        <p v-if="canRequestReturn" class="sf-order-card__cancel-hint">
          Đơn đã giao thành công — bạn có thể gửi yêu cầu trả hàng.
        </p>
      </div>
    </div>
  </article>
</template>

<style scoped>
.sf-btn-review {
  margin-top: 8px;
  padding: 6px 12px;
  background-color: #f59e0b;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}
.sf-btn-review:hover {
  background-color: #d97706;
}

.sf-review-status {
  display: inline-block;
  margin-top: 8px;
  font-size: 12px;
  font-weight: 600;
}

.sf-review-status--pending {
  color: #d97706;
}

.sf-review-status--done {
  color: #16a34a;
}

.sf-order-card__actions {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed rgba(30, 21, 16, 0.12);
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.sf-btn-cancel-order {
  padding: 8px 16px;
  border: 1px solid rgba(180, 72, 72, 0.35);
  background: rgba(180, 72, 72, 0.08);
  color: #a83a3a;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s ease, border-color 0.15s ease;
}

.sf-btn-cancel-order:hover:not(:disabled) {
  background: rgba(180, 72, 72, 0.14);
  border-color: rgba(180, 72, 72, 0.5);
}

.sf-btn-cancel-order:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.sf-btn-return-order {
  padding: 8px 16px;
  border: 1px solid rgba(166, 124, 61, 0.45);
  background: rgba(196, 149, 84, 0.12);
  color: #8a6428;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s ease, border-color 0.15s ease;
}

.sf-btn-return-order:hover:not(:disabled) {
  background: rgba(196, 149, 84, 0.2);
  border-color: rgba(166, 124, 61, 0.65);
}

.sf-btn-return-order:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.sf-order-card__cancel-hint {
  margin: 0;
  flex-basis: 100%;
  font-size: 12px;
  color: rgba(30, 21, 16, 0.55);
}
</style>
