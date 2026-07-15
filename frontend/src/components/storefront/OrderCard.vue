<script setup>
import { computed, ref } from 'vue'
import { formatVND } from '@/utils/formatVND'
import { productImageUrl } from '@/utils/productImage'
import { formatOrderDate, orderStatusClass, orderStatusLabel, coTheHuyDon } from '@/utils/orderStatus'
import {
  hoanTienStatusClass,
  hoanTienStatusLabel,
  traHangStatusClass,
  traHangStatusLabel,
} from '@/utils/returnStatus'

const props = defineProps({
  order: { type: Object, required: true },
  defaultOpen: { type: Boolean, default: true },
  cancelLoading: { type: Boolean, default: false },
  returnActionLoading: { type: Boolean, default: false },
})

const emit = defineEmits(['review', 'cancelOrder', 'requestReturn', 'createReturnLabel'])

const isOpen = ref(props.defaultOpen)

const steps = ['Đặt hàng', 'Chờ Xác nhận','Đã xác nhận', 'Vận chuyển', 'Đã nhận']

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

const canCancel = computed(() => coTheHuyDon(props.order?.trangThai))

const canRequestReturn = computed(() => props.order?.coTheYeuCauTraHang === true)

const canCreateReturnLabel = computed(() => props.order?.trangThaiTraHang === 'DA_DUYET')

const hasReturnInfo = computed(() => Boolean(props.order?.trangThaiTraHang || props.order?.trangThaiHoanTien))

const productPreview = computed(() => {
  const lines = props.order?.chiTiets || []
  const first = lines[0]?.tenSanPham || 'Đơn hàng SUNOVA'
  return {
    first,
    more: lines.length > 1 ? `+${lines.length - 1} sản phẩm` : '',
  }
})

const shippingText = computed(() => {
  const order = props.order || {}
  if (order.maVanDon) {
    const carrier = order.donViVanChuyen || 'Giao hàng nhanh'
    const status = order.ghnTrangThaiLabel ? ` · ${order.ghnTrangThaiLabel}` : ''
    return `${carrier} · Mã vận đơn: ${order.maVanDon}${status}`
  }
  if (order.phiVanChuyen > 0) {
    return `Phí vận chuyển ${formatVND(order.phiVanChuyen)}`
  }
  return 'Chưa có thông tin vận chuyển'
})

function toggleOpen() {
  isOpen.value = !isOpen.value
}

function canReview(line) {
  const delivered = props.order?.trangThai === 'HOAN_THANH' || props.order?.trangThai === 'GIAO_THANH_CONG'
  return delivered && !line?.daDanhGia && line?.idSanPham
}
</script>

<template>
  <article v-if="order" class="sf-order-card" :class="{ open: isOpen }">
    <button type="button" class="sf-order-card__head" @click="toggleOpen">
      <div class="sf-order-card__head-left">
        <div class="sf-order-card__meta">
          <span class="sf-order-card__code">{{ order.maHoaDon }}</span>
          <span
            class="sf-order-badge"
            :class="orderStatusClass(order.trangThai)"
          >
            <span class="sf-order-badge__dot"></span>
            {{ orderStatusLabel(order.trangThai) }}
          </span>
        </div>
        <div class="sf-order-card__preview">
          {{ productPreview.first }}
          <span v-if="productPreview.more">{{ productPreview.more }}</span>
        </div>
      </div>

      <div class="sf-order-card__head-right">
        <strong class="sf-order-card__amount">{{ formatVND(order.thanhTien) }}</strong>
        <span class="sf-order-card__date">{{ formatOrderDate(order.ngayTao) }}</span>
        <span class="sf-order-card__chevron">▾</span>
      </div>
    </button>

    <div class="sf-order-card__body">
      <div v-if="!isCancelled" class="sf-order-timeline">
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

      <div class="sf-order-detail-grid">
        <div class="sf-order-detail-block">
          <label>Địa chỉ nhận hàng</label>
          <p>
            <strong v-if="order.tenNguoiNhan">{{ order.tenNguoiNhan }}</strong>
            <span v-if="order.tenNguoiNhan && order.sdtNguoiNhan"> · </span>
            <span v-if="order.sdtNguoiNhan">{{ order.sdtNguoiNhan }}</span>
            <br v-if="order.tenNguoiNhan || order.sdtNguoiNhan" />
            {{ order.diaChiGiao || 'Chưa có địa chỉ giao hàng' }}
          </p>
        </div>
        <div class="sf-order-detail-block">
          <label>Vận chuyển</label>
          <p>{{ shippingText }}</p>
          <p v-if="order.ghnHenGiao" class="sf-order-detail-block__muted">
            Dự kiến giao: {{ formatOrderDate(order.ghnHenGiao) }}
          </p>
        </div>
      </div>

      <div class="sf-order-products-label">Sản phẩm</div>
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

      <div v-if="hasReturnInfo" class="sf-order-return-info">
        <div v-if="order.trangThaiTraHang" class="sf-order-return-row">
          <span class="sf-order-return-label">Trả hàng</span>
          <span
            class="sf-order-badge"
            :class="traHangStatusClass(order.trangThaiTraHang)"
          >
            <span class="sf-order-badge__dot"></span>
            {{ order.trangThaiTraHangLabel || traHangStatusLabel(order.trangThaiTraHang) }}
          </span>
        </div>
        <p v-if="order.maVanDonTra" class="sf-order-return-tracking">
          Mã vận đơn hoàn: <strong>{{ order.maVanDonTra }}</strong>
        </p>
        <div v-if="order.trangThaiHoanTien" class="sf-order-return-row">
          <span class="sf-order-return-label">Hoàn tiền</span>
          <span
            class="sf-order-badge"
            :class="hoanTienStatusClass(order.trangThaiHoanTien)"
          >
            <span class="sf-order-badge__dot"></span>
            {{ order.trangThaiHoanTienLabel || hoanTienStatusLabel(order.trangThaiHoanTien) }}
          </span>
        </div>
        <p v-if="order.maGiaoDichHoan" class="sf-order-return-tracking">
          Mã GD hoàn: <strong>{{ order.maGiaoDichHoan }}</strong>
        </p>
      </div>

      <div v-if="canCancel || canRequestReturn || canCreateReturnLabel" class="sf-order-card__actions">
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
        <button
          v-if="canCreateReturnLabel"
          type="button"
          class="sf-btn-return-order"
          :disabled="returnActionLoading"
          @click.stop="emit('createReturnLabel', order)"
        >
          {{ returnActionLoading ? 'Đang tạo vận đơn...' : 'Tạo vận đơn hoàn hàng' }}
        </button>
        <p v-if="canCancel" class="sf-order-card__cancel-hint">
          Có thể hủy khi đơn chưa chuyển sang trạng thái đang giao.
        </p>
        <p v-if="canRequestReturn" class="sf-order-card__cancel-hint">
          Đơn đã giao thành công — bạn có thể gửi yêu cầu trả hàng.
        </p>
        <p v-if="canCreateReturnLabel" class="sf-order-card__cancel-hint">
          Yêu cầu đã được duyệt — tạo vận đơn để gửi hàng về cửa hàng.
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

.sf-order-return-info {
  margin-top: 16px;
  padding: 12px 14px;
  border-radius: 10px;
  background: rgba(196, 149, 84, 0.08);
  border: 1px solid rgba(196, 149, 84, 0.2);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.sf-order-return-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.sf-order-return-label {
  font-size: 12px;
  font-weight: 600;
  color: rgba(30, 21, 16, 0.65);
  min-width: 72px;
}

.sf-order-return-tracking {
  margin: 0;
  font-size: 13px;
  color: rgba(30, 21, 16, 0.75);
}
</style>
