<script setup>
import { formatVND } from '@/utils/formatVND'
import { productImageUrl } from '@/utils/productImage'
import { formatOrderDate, orderStatusClass, orderStatusLabel } from '@/utils/orderStatus'

defineProps({
  order: { type: Object, required: true },
})
</script>

<template>
  <article v-if="order" class="sf-order-card">
    <header class="sf-order-card__head">
      <div>
        <p class="sf-order-card__code">{{ order.maHoaDon }}</p>
        <p class="sf-order-card__date">{{ formatOrderDate(order.ngayTao) }}</p>
      </div>
      <span
        class="sf-order-badge"
        :class="orderStatusClass(order.trangThai)"
      >
        {{ order.trangThaiLabel || orderStatusLabel(order.trangThai) }}
      </span>
    </header>

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
          <p class="sf-order-line__qty">{{ line.soLuong }} × {{ formatVND(line.donGia) }}</p>
        </div>
        <strong class="sf-order-line__total">{{ formatVND(line.thanhTien) }}</strong>
      </li>
    </ul>

    <div class="sf-order-card__totals">
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

    <div v-if="order.tenNguoiNhan || order.diaChiGiao" class="sf-order-card__ship">
      <p class="sf-order-card__ship-title">Thông tin nhận hàng</p>
      <p v-if="order.tenNguoiNhan">
        <strong>{{ order.tenNguoiNhan }}</strong>
        <span v-if="order.sdtNguoiNhan"> · {{ order.sdtNguoiNhan }}</span>
      </p>
      <p v-if="order.diaChiGiao">{{ order.diaChiGiao }}</p>
    </div>

    <p
      v-if="order.capNhatGanNhatLuc"
      class="sf-order-card__latest"
    >
      Cập nhật gần nhất:
      {{ order.capNhatGanNhatLabel || order.capNhatGanNhatTrangThai }}
      · {{ formatOrderDate(order.capNhatGanNhatLuc) }}
    </p>
  </article>
</template>
