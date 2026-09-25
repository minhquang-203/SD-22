<script setup>
import { computed } from 'vue'
import { NQrCode } from 'naive-ui'
import logoMark from '@/assets/logo/sunova_mark.png'
import { storeInfo, buildLookupUrl } from '@/config/storeInfo'
import {
  formatReceiptMoney,
  formatReceiptDateTime,
  formatReceiptDate,
} from '@/utils/invoiceReceipt'

const props = defineProps({
  /** Đã normalize hoặc raw (component tự xử lý field phổ biến) */
  invoice: { type: Object, required: true },
  /** true khi render trong iframe in (ẩn khung màn hình) */
  compact: { type: Boolean, default: false },
})

const inv = computed(() => props.invoice || {})

const customerLabel = computed(() => {
  const name = inv.value.tenKhachHang || 'Khách lẻ'
  const phone = inv.value.soDienThoaiKhachHang
  if (phone) return `${name} · ${phone}`
  return name
})

const hasDiscount = computed(() => Number(inv.value.tienGiamGia || 0) > 0)

const discountLabel = computed(() => {
  if (inv.value.maPhieuGiamGia) return `Giảm giá (${inv.value.maPhieuGiamGia})`
  return 'Giảm giá'
})

const payments = computed(() => inv.value.danhSachThanhToan || [])

const cashGiven = computed(() => {
  if (inv.value.soTienKhachDua != null) return inv.value.soTienKhachDua
  const cashLine = payments.value.find(
    (p) =>
      p.soTienKhachDua != null &&
      (/tiền mặt|tien mat|cash/i.test(p.tenPhuongThucThanhToan || '') ||
        /TIEN_MAT|CASH/i.test(p.maPhuongThucThanhToan || '')),
  )
  return cashLine?.soTienKhachDua ?? null
})

const changeAmount = computed(() => {
  if (inv.value.tienThua != null) return inv.value.tienThua
  const cashLine = payments.value.find((p) => p.tienThua != null)
  return cashLine?.tienThua ?? null
})

const showPoints = computed(
  () =>
    inv.value.idKhachHang != null &&
    inv.value.diemCong != null &&
    Number(inv.value.diemCong) > 0,
)

const qrValue = computed(() => buildLookupUrl(inv.value.maHoaDon))

function lotLine(item) {
  const parts = []
  if (item.sku) parts.push(item.sku)
  if (item.bienThe) parts.push(item.bienThe)
  const lots = item.loHangs || []
  if (lots.length) {
    const lotText = lots
      .map((lo) => {
        const bits = []
        if (lo.soLo) bits.push(`Lô ${lo.soLo}`)
        if (lo.hanSuDung) bits.push(`HSD ${formatReceiptDate(lo.hanSuDung)}`)
        return bits.join(' – ')
      })
      .filter(Boolean)
      .join('; ')
    if (lotText) parts.push(lotText)
  }
  return parts.join(' · ')
}

function paymentLabel(p) {
  return p.tenPhuongThucThanhToan || p.maPhuongThucThanhToan || 'Thanh toán'
}
</script>

<template>
  <div class="inv-receipt" :class="{ 'inv-receipt--compact': compact }">
    <header class="inv-receipt__head">
      <img :src="logoMark" alt="SUNOVA" class="inv-receipt__logo" />
      <div class="inv-receipt__brand">{{ storeInfo.tagline }}</div>
      <div v-if="storeInfo.address" class="inv-receipt__store">{{ storeInfo.address }}</div>
      <div v-if="storeInfo.hotline" class="inv-receipt__store">Hotline: {{ storeInfo.hotline }}</div>
      <div v-if="storeInfo.taxCode" class="inv-receipt__store">MST: {{ storeInfo.taxCode }}</div>
    </header>

    <div class="inv-receipt__rule" />

    <section class="inv-receipt__meta">
      <div class="inv-receipt__title">HÓA ĐƠN BÁN LẺ</div>
      <div v-if="inv.maHoaDon" class="inv-receipt__row">
        <span>Số HĐ</span>
        <span class="inv-receipt__strong">{{ inv.maHoaDon }}</span>
      </div>
      <div v-if="inv.ngayTao" class="inv-receipt__row">
        <span>Ngày giờ</span>
        <span>{{ formatReceiptDateTime(inv.ngayTao) }}</span>
      </div>
      <div v-if="inv.tenNhanVien" class="inv-receipt__row">
        <span>Thu ngân</span>
        <span>{{ inv.tenNhanVien }}</span>
      </div>
      <div class="inv-receipt__row inv-receipt__row--wrap">
        <span>Khách</span>
        <span>{{ customerLabel }}</span>
      </div>
    </section>

    <div class="inv-receipt__rule" />

    <section class="inv-receipt__items">
      <div v-for="(item, idx) in inv.items || []" :key="item.id || idx" class="inv-receipt__item">
        <div class="inv-receipt__item-name">{{ item.tenSanPham }}</div>
        <div v-if="lotLine(item)" class="inv-receipt__item-sub">{{ lotLine(item) }}</div>
        <div class="inv-receipt__item-amt">
          <span>{{ item.soLuong }} x {{ formatReceiptMoney(item.donGia) }}</span>
          <span class="inv-receipt__dots" aria-hidden="true" />
          <span class="inv-receipt__money">{{ formatReceiptMoney(item.thanhTien) }}</span>
        </div>
      </div>
    </section>

    <div class="inv-receipt__rule" />

    <section class="inv-receipt__totals">
      <div class="inv-receipt__row">
        <span>Tạm tính</span>
        <span class="inv-receipt__money">{{ formatReceiptMoney(inv.tongTien) }}</span>
      </div>
      <div v-if="hasDiscount" class="inv-receipt__row">
        <span>{{ discountLabel }}</span>
        <span class="inv-receipt__money">-{{ formatReceiptMoney(inv.tienGiamGia) }}</span>
      </div>
      <div class="inv-receipt__row inv-receipt__grand">
        <span>TỔNG THANH TOÁN</span>
        <span class="inv-receipt__money">{{ formatReceiptMoney(inv.thanhTien) }}</span>
      </div>
    </section>

    <div class="inv-receipt__rule" />

    <section class="inv-receipt__pay">
      <div class="inv-receipt__pay-title">Thanh toán</div>
      <template v-if="payments.length">
        <div v-for="(p, i) in payments" :key="i" class="inv-receipt__pay-block">
          <div class="inv-receipt__row">
            <span>{{ paymentLabel(p) }}</span>
            <span class="inv-receipt__money">{{ formatReceiptMoney(p.soTien) }}</span>
          </div>
          <div v-if="p.maGiaoDich" class="inv-receipt__row inv-receipt__row--sub">
            <span>Mã GD</span>
            <span>{{ p.maGiaoDich }}</span>
          </div>
        </div>
      </template>
      <div
        v-else-if="inv.tenPhuongThucThanhToan"
        class="inv-receipt__row"
      >
        <span>{{ inv.tenPhuongThucThanhToan }}</span>
        <span class="inv-receipt__money">{{ formatReceiptMoney(inv.thanhTien) }}</span>
      </div>
      <div v-if="inv.maGiaoDich && !payments.some((p) => p.maGiaoDich)" class="inv-receipt__row inv-receipt__row--sub">
        <span>Mã GD</span>
        <span>{{ inv.maGiaoDich }}</span>
      </div>
      <div v-if="cashGiven != null" class="inv-receipt__row">
        <span>Tiền khách đưa</span>
        <span class="inv-receipt__money">{{ formatReceiptMoney(cashGiven) }}</span>
      </div>
      <div v-if="changeAmount != null" class="inv-receipt__row">
        <span>Tiền thừa trả khách</span>
        <span class="inv-receipt__money">{{ formatReceiptMoney(changeAmount) }}</span>
      </div>
    </section>

    <template v-if="showPoints">
      <div class="inv-receipt__rule" />
      <section class="inv-receipt__points">
        +{{ inv.diemCong }} điểm
        <template v-if="inv.diemTichLuySau != null">
          · Tổng điểm: {{ inv.diemTichLuySau }}
        </template>
      </section>
    </template>

    <div class="inv-receipt__rule" />

    <section class="inv-receipt__qr">
      <NQrCode :value="qrValue" :size="96" :padding="4" error-correction-level="M" />
      <div class="inv-receipt__qr-cap">Tra cứu đơn tại {{ storeInfo.website }}</div>
    </section>

    <div class="inv-receipt__rule" />

    <p class="inv-receipt__policy">{{ storeInfo.returnPolicy }}</p>
    <p class="inv-receipt__thanks">{{ storeInfo.thankYou }}</p>
    <p class="inv-receipt__legal">{{ storeInfo.legalNote }}</p>
  </div>
</template>

<style src="./invoiceReceipt.css"></style>
