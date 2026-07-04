<script setup>
import { computed, ref } from 'vue'
import { formatVND } from '@/utils/formatVND'
import { productImageUrl } from '@/utils/productImage'
import { formatOrderDate, orderStatusClass, orderStatusLabel } from '@/utils/orderStatus'

const props = defineProps({
  order: { type: Object, required: true },
  defaultOpen: { type: Boolean, default: true },
})

const isOpen = ref(props.defaultOpen)

const steps = ['Đặt hàng', 'Xác nhận', 'Vận chuyển', 'Đã nhận']

const timelineSteps = computed(() => {
  const status = props.order?.trangThai
  const map = {
    CHO_XAC_NHAN: ['active', '', '', ''],
    DA_XAC_NHAN: ['done', 'active', '', ''],
    DANG_CHUAN_BI: ['done', 'active', '', ''],
    DANG_GIAO: ['done', 'done', 'active', ''],
    HOAN_THANH: ['done', 'done', 'done', 'done'],
    DA_HUY: ['done', '', '', ''],
  }
  return map[status] || ['active', '', '', '']
})

const isCancelled = computed(() => props.order?.trangThai === 'DA_HUY')

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
            {{ order.trangThaiLabel || orderStatusLabel(order.trangThai) }}
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
</style>
