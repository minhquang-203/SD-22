<script setup>
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import { Icon } from '@iconify/vue'
import { formatVND } from '@/utils/formatVND'
import { productImageUrl } from '@/utils/productImage'
import { formatOrderDate } from '@/utils/orderStatus'
import {
  hoanTienStatusClass,
  hoanTienStatusLabel,
  maskBankAccount,
  refundMethodLabel,
  traHangStatusClass,
  traHangStatusLabel,
} from '@/utils/returnStatus'

const props = defineProps({
  detail: { type: Object, required: true },
  creatingLabel: { type: Boolean, default: false },
})

const emit = defineEmits(['createReturnLabel'])

const canCreateReturnLabel = computed(() => props.detail?.trangThai === 'DA_DUYET')

const originalOrderLink = computed(() => ({
  path: '/tra-cuu-don',
  query: props.detail?.maHoaDon ? { ma: props.detail.maHoaDon } : {},
}))

const timeline = computed(() => props.detail?.timeline || [])

const proofImages = computed(() => (props.detail?.anhUrls || []).filter(Boolean))

const statusLabel = computed(() =>
  props.detail?.trangThaiLabel || traHangStatusLabel(props.detail?.trangThai),
)

const productSubtotal = computed(() =>
  (props.detail?.chiTiets || []).reduce((sum, line) => sum + Number(line.thanhTien || 0), 0),
)

const tongTien = computed(() => Number(props.detail?.tongTien ?? productSubtotal.value) || 0)

const tienGiamGia = computed(() => Number(props.detail?.tienGiamGia) || 0)

const phiVanChuyen = computed(() => Number(props.detail?.phiVanChuyen) || 0)

const soTienHoan = computed(() => {
  if (props.detail?.soTienHoan != null) return Number(props.detail.soTienHoan)
  return tongTien.value - tienGiamGia.value + phiVanChuyen.value
})
</script>

<template>
  <article class="sf-return-detail">
    <header class="sf-return-detail__header">
      <div>
        <div class="sf-return-detail__kicker-row">
          <p class="sf-return-detail__kicker">Đơn trả hàng</p>
          <span v-if="detail.maVanDonTra" class="sf-return-detail__return-code">
            {{ detail.maVanDonTra }}
          </span>
        </div>
        <h2 class="sf-return-detail__code">{{ detail.maHoaDon || '—' }}</h2>
        <p v-if="detail.ngayTao" class="sf-return-detail__date">
          Yêu cầu lúc {{ formatOrderDate(detail.ngayTao) }}
        </p>
      </div>
      <div class="sf-return-detail__header-side">
        <span class="sf-order-badge" :class="traHangStatusClass(detail.trangThai)">
          <span class="sf-order-badge__dot"></span>
          {{ statusLabel }}
        </span>
        <RouterLink class="sf-return-detail__origin" :to="originalOrderLink">
          Xem đơn gốc
        </RouterLink>
      </div>
    </header>

    <section v-if="timeline.length" class="sf-return-detail__section">
      <h3 class="sf-detail-section-head">
        <Icon icon="mdi:progress-clock" width="16" />
        Tiến trình trả hàng
      </h3>
      <div class="sf-return-timeline">
        <div
          v-for="step in timeline"
          :key="step.ma"
          class="sf-return-timeline__step"
          :class="step.trangThai"
        >
          <div class="sf-return-timeline__dot">
            {{ step.trangThai === 'done' ? '✓' : step.trangThai === 'active' ? '→' : '' }}
          </div>
          <div class="sf-return-timeline__text">{{ step.label }}</div>
          <div v-if="step.thoiGian" class="sf-return-timeline__time">
            {{ formatOrderDate(step.thoiGian) }}
          </div>
        </div>
      </div>
    </section>

    <section class="sf-return-detail__section">
      <h3 class="sf-detail-section-head">
        <Icon icon="mdi:package-variant-closed-remove" width="16" />
        Sản phẩm hoàn
      </h3>
      <ul class="sf-order-card__lines">
        <li v-for="(line, idx) in detail.chiTiets || []" :key="line.id || idx" class="sf-order-line">
          <img
            :src="productImageUrl(line.anhUrl)"
            :alt="line.tenSanPham"
            class="sf-order-line__img"
          />
          <div class="sf-order-line__body">
            <p class="sf-order-line__name">{{ line.tenSanPham }}</p>
            <p v-if="line.bienThe" class="sf-order-line__variant">{{ line.bienThe }}</p>
            <p class="sf-order-line__qty">x{{ line.soLuong }} · {{ formatVND(line.donGia) }}</p>
          </div>
          <strong class="sf-order-line__total">{{ formatVND(line.thanhTien) }}</strong>
        </li>
      </ul>
      <div class="sf-order-card__totals">
        <div class="sf-order-card__row">
          <span>Tạm tính</span>
          <span>{{ formatVND(tongTien) }}</span>
        </div>
        <div v-if="tienGiamGia > 0" class="sf-order-card__row">
          <span>Giảm giá</span>
          <span>−{{ formatVND(tienGiamGia) }}</span>
        </div>
        <div v-if="phiVanChuyen > 0" class="sf-order-card__row">
          <span>Phí vận chuyển</span>
          <span>{{ formatVND(phiVanChuyen) }}</span>
        </div>
        <div class="sf-order-card__row sf-order-card__row--total">
          <span>Tổng hoàn</span>
          <strong>{{ formatVND(soTienHoan) }}</strong>
        </div>
      </div>
    </section>

    <section class="sf-return-detail__section">
      <h3 class="sf-detail-section-head">
        <Icon icon="mdi:comment-text-outline" width="16" />
        Lý do trả hàng
      </h3>
      <div class="sf-reason-card">
        <p class="sf-reason-card__title">{{ detail.lyDo || '—' }}</p>
        <p v-if="detail.moTa" class="sf-reason-card__desc">{{ detail.moTa }}</p>
      </div>
      <div v-if="proofImages.length" class="sf-return-detail__photos">
        <a
          v-for="(url, idx) in proofImages"
          :key="`${url}-${idx}`"
          :href="productImageUrl(url)"
          target="_blank"
          rel="noopener noreferrer"
        >
          <img :src="productImageUrl(url)" :alt="`Ảnh minh chứng ${idx + 1}`" />
        </a>
      </div>
    </section>

    <section v-if="detail.trangThai === 'TU_CHOI'" class="sf-return-detail__section sf-return-detail__section--alert">
      <h3 class="sf-detail-section-head">
        <Icon icon="mdi:close-circle-outline" width="16" />
        Yêu cầu bị từ chối
      </h3>
      <p>
        {{ detail.ghiChuAdmin || 'Yêu cầu trả hàng đã bị từ chối. Bạn không thể gửi yêu cầu mới cho đơn này.' }}
      </p>
    </section>

    <section class="sf-return-detail__section">
      <h3 class="sf-detail-section-head">
        <Icon icon="mdi:truck-delivery-outline" width="16" />
        Vận chuyển hoàn hàng
      </h3>
      <div class="sf-info-tiles">
        <div class="sf-info-tile">
          <div class="sf-info-tile__icon" aria-hidden="true">
            <Icon icon="mdi:map-marker-outline" width="18" />
          </div>
          <div class="sf-info-tile__body">
            <span class="sf-info-tile__label">Địa chỉ lấy hàng</span>
            <p class="sf-info-tile__value">{{ detail.diaChiTra || detail.diaChiGiao || '—' }}</p>
            <div v-if="detail.pickShiftLabel" class="sf-info-tile__chips">
              <span class="sf-info-chip">
                <Icon icon="mdi:clock-outline" width="14" />
                {{ detail.pickShiftLabel }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="sf-return-detail__section">
      <h3 class="sf-detail-section-head">
        <Icon icon="mdi:cash-refund" width="16" />
        Hoàn tiền
        <span
          v-if="detail.trangThaiHoanTien"
          class="sf-order-badge"
          :class="hoanTienStatusClass(detail.trangThaiHoanTien)"
        >
          <span class="sf-order-badge__dot"></span>
          {{ detail.trangThaiHoanTienLabel || hoanTienStatusLabel(detail.trangThaiHoanTien) }}
        </span>
        <span v-else class="sf-order-badge sf-order-badge--wait">
          <span class="sf-order-badge__dot"></span>
          Chưa phát sinh
        </span>
      </h3>
      <p v-if="!detail.trangThaiHoanTien" class="sf-return-detail__refund-hint">
        Hoàn tiền sau khi cửa hàng nhận và kiểm hàng.
      </p>

      <div
        v-if="detail.phuongThucHoan || detail.phuongThucThanhToan || detail.soTaiKhoan"
        class="sf-info-tiles"
      >
        <div v-if="detail.phuongThucHoan || detail.phuongThucThanhToan" class="sf-info-tile">
          <div class="sf-info-tile__icon sf-info-tile__icon--pay" aria-hidden="true">
            <Icon icon="mdi:wallet-outline" width="18" />
          </div>
          <div class="sf-info-tile__body">
            <span class="sf-info-tile__label">Phương thức</span>
            <p class="sf-info-tile__value">
              {{ refundMethodLabel(detail.phuongThucHoan || detail.phuongThucThanhToan) }}
            </p>
            <div v-if="detail.maGiaoDichHoan || detail.ngayHoan" class="sf-info-tile__chips">
              <span v-if="detail.maGiaoDichHoan" class="sf-code-pill">{{ detail.maGiaoDichHoan }}</span>
              <span v-if="detail.ngayHoan" class="sf-info-chip">
                <Icon icon="mdi:calendar-blank-outline" width="14" />
                {{ formatOrderDate(detail.ngayHoan) }}
              </span>
            </div>
          </div>
        </div>

        <div v-if="detail.soTaiKhoan" class="sf-info-tile">
          <div class="sf-info-tile__icon sf-info-tile__icon--refund" aria-hidden="true">
            <Icon icon="mdi:bank-outline" width="18" />
          </div>
          <div class="sf-info-tile__body">
            <span class="sf-info-tile__label">Tài khoản nhận</span>
            <p class="sf-info-tile__value">{{ detail.chuTaiKhoan || 'Chủ tài khoản' }}</p>
            <div class="sf-info-tile__chips">
              <span v-if="detail.tenNganHang" class="sf-info-chip">{{ detail.tenNganHang }}</span>
              <span class="sf-code-pill">{{ maskBankAccount(detail.soTaiKhoan) }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <div v-if="canCreateReturnLabel" class="sf-return-detail__actions">
      <button
        type="button"
        class="sf-btn-return-order"
        :disabled="creatingLabel"
        @click="emit('createReturnLabel')"
      >
        {{ creatingLabel ? 'Đang tạo vận đơn...' : 'Tạo vận đơn hoàn hàng' }}
      </button>
      <p class="sf-order-card__cancel-hint">
        Yêu cầu đã được duyệt — tạo vận đơn và chọn ca lấy hàng để gửi sản phẩm về cửa hàng.
        Cửa hàng sẽ xử lý hoàn tiền sau khi nhận và kiểm tra hàng.
      </p>
    </div>
  </article>
</template>
