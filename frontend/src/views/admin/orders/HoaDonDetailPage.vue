<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import { getHoaDonDetail, getLichSu } from '@/api/hoaDonApi'
import { formatCurrency } from '@/utils/format'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const error = ref('')
const detail = ref(null)
const lichSu = ref([])

const orderId = computed(() => {
  const id = route.params.id
  return id ? Number(id) : null
})

function statusLabel(trangThai) {
  const map = {
    CHO: 'Chờ tại quầy',
    CHO_XAC_NHAN: 'Chờ xác nhận',
    DA_XAC_NHAN: 'Đã xác nhận',
    DANG_CHUAN_BI: 'Đang chuẩn bị',
    DANG_GIAO: 'Đang giao',
    HOAN_THANH: 'Hoàn thành',
    DA_HUY: 'Đã hủy',
  }
  return map[trangThai] || trangThai || '—'
}

function statusTone(trangThai) {
  if (trangThai === 'HOAN_THANH') return 'success'
  if (trangThai === 'DANG_GIAO') return 'info'
  if (trangThai === 'DA_HUY') return 'danger'
  if (trangThai === 'CHO' || trangThai === 'CHO_XAC_NHAN') return 'warning'
  return 'neutral'
}

function loaiDonLabel(loai) {
  if (loai === 'TAI_QUAY') return 'Tại quầy'
  if (loai === 'ONLINE') return 'Online'
  return loai || '—'
}

function loaiDonTone(loai) {
  return loai === 'TAI_QUAY' ? 'gold' : 'teal'
}

function formatDateTime(value) {
  if (!value) return '—'
  return new Date(value).toLocaleString('vi-VN')
}

function actionIcon(ma) {
  const map = {
    TAO_DON: 'icon-park-outline:bill',
    THEM_HANG: 'mdi:plus-circle-outline',
    AP_MA: 'mdi:ticket-percent-outline',
    THANH_TOAN: 'icon-park-outline:pay-code-two',
    HOAN_THANH: 'mdi:check-circle-outline',
    CHO_XAC_NHAN: 'mdi:clock-outline',
    DA_XAC_NHAN: 'mdi:check',
    DANG_CHUAN_BI: 'mdi:package-variant',
    DANG_GIAO: 'mdi:truck-delivery-outline',
    DA_HUY: 'mdi:close-circle-outline',
    CHO: 'mdi:pause-circle-outline',
  }
  return map[ma] || 'mdi:history'
}

function timelineText(entry) {
  const who = entry.tenNhanVien || 'Hệ thống'
  const desc = entry.ghiChu || entry.trangThaiLabel || entry.trangThai
  return `${who} — ${desc}`
}

async function loadDetail() {
  if (!orderId.value || Number.isNaN(orderId.value)) {
    error.value = 'Mã hóa đơn không hợp lệ'
    return
  }
  loading.value = true
  error.value = ''
  detail.value = null
  lichSu.value = []
  try {
    const [detailRes, lsRes] = await Promise.all([
      getHoaDonDetail(orderId.value),
      getLichSu(orderId.value),
    ])
    detail.value = detailRes.data
    lichSu.value = lsRes.data || []
  } catch (err) {
    error.value = String(err)
  } finally {
    loading.value = false
  }
}

function printInvoice() {
  window.print()
}

function goBack() {
  router.push('/admin/hoa-don')
}

watch(() => route.params.id, () => loadDetail())

onMounted(() => loadDetail())
</script>

<template>
  <div class="hoa-don-detail-page space-y-6">
    <PageHeader
      class="no-print"
      title="Chi tiết hóa đơn"
      description="Xem thông tin đơn, dòng hàng và nhật ký hành động"
    >
      <template #actions>
        <button type="button" class="soleil-btn-outline" @click="goBack">
          <Icon icon="icon-park-outline:back" />
          Danh sách
        </button>
      </template>
    </PageHeader>

    <div v-if="loading" class="soleil-card p-12 text-center text-[var(--admin-muted)]">
      Đang tải hóa đơn...
    </div>

    <div v-else-if="error" class="admin-alert admin-alert-error rounded-lg px-4 py-3">
      {{ error }}
    </div>

    <template v-else-if="detail">
      <div id="hoa-don-print-area" class="hoa-don-print-area">
        <section class="hoa-don-summary soleil-card">
          <div class="hoa-don-print-brand">SUNOVA</div>
          <div class="hoa-don-summary__main">
          <div>
            <p class="soleil-eyebrow mb-1">Hóa đơn</p>
            <h1 class="hoa-don-summary__code">{{ detail.maHoaDon }}</h1>
            <p class="hoa-don-summary__datetime">{{ formatDateTime(detail.ngayTao) }}</p>
          </div>
          <div class="hoa-don-summary__badges">
            <span class="hd-badge" :class="`hd-badge--${loaiDonTone(detail.loaiDon)}`">
              {{ loaiDonLabel(detail.loaiDon) }}
            </span>
            <span class="hd-badge" :class="`hd-badge--${statusTone(detail.trangThai)}`">
              {{ statusLabel(detail.trangThai) }}
            </span>
          </div>
        </div>

        <div class="hoa-don-summary__meta">
          <div class="hoa-don-summary__meta-item">
            <span class="hoa-don-summary__label">Khách hàng</span>
            <span>{{ detail.tenKhachHang || 'Khách lẻ' }}</span>
            <span v-if="detail.soDienThoaiKhachHang" class="text-sm text-[var(--admin-muted)]">
              {{ detail.soDienThoaiKhachHang }}
            </span>
          </div>
          <div v-if="detail.tenNhanVien" class="hoa-don-summary__meta-item">
            <span class="hoa-don-summary__label">Nhân viên</span>
            <span>{{ detail.tenNhanVien }}</span>
          </div>
          <div v-if="detail.tenPhuongThucThanhToan" class="hoa-don-summary__meta-item">
            <span class="hoa-don-summary__label">Thanh toán</span>
            <span>{{ detail.tenPhuongThucThanhToan }}</span>
          </div>
        </div>

        <div class="hoa-don-summary__footer">
          <div class="hoa-don-summary__total">
            <span class="hoa-don-summary__total-label">Thành tiền</span>
            <span class="hoa-don-summary__total-value">{{ formatCurrency(detail.thanhTien) }}</span>
          </div>
          <button type="button" class="soleil-btn-outline no-print" @click="printInvoice">
            <Icon icon="icon-park-outline:printer" />
            In hóa đơn
          </button>
        </div>
        </section>

        <div class="hoa-don-detail-grid">
          <section class="soleil-card hoa-don-lines">
          <h2 class="hoa-don-section-title">Hóa đơn chi tiết</h2>

          <div class="overflow-x-auto">
            <table class="soleil-table admin-table--soleil w-full">
              <thead>
                <tr>
                  <th>Sản phẩm</th>
                  <th class="text-center">SL</th>
                  <th class="text-right">Đơn giá</th>
                  <th class="text-right">Thành tiền</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="!detail.chiTiets?.length">
                  <td colspan="4" class="text-center py-8 text-[var(--admin-muted)]">Không có dòng hàng</td>
                </tr>
                <tr v-for="line in detail.chiTiets || []" :key="line.id">
                  <td>
                    <div class="font-medium">{{ line.tenSanPham }}</div>
                    <div v-if="line.bienThe || line.sku" class="text-xs text-[var(--admin-muted)]">
                      {{ line.bienThe || line.sku }}
                    </div>
                  </td>
                  <td class="text-center">{{ line.soLuong }}</td>
                  <td class="text-right">{{ formatCurrency(line.donGia) }}</td>
                  <td class="text-right font-medium">{{ formatCurrency(line.thanhTien) }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="hoa-don-totals">
            <div class="hoa-don-totals__row">
              <span>Tổng tiền hàng</span>
              <span>{{ formatCurrency(detail.tongTien) }}</span>
            </div>
            <div v-if="detail.tienGiamGia > 0" class="hoa-don-totals__row text-[var(--sage)]">
              <span>
                Giảm giá
                <template v-if="detail.maPhieuGiamGia"> ({{ detail.maPhieuGiamGia }})</template>
              </span>
              <span>−{{ formatCurrency(detail.tienGiamGia) }}</span>
            </div>
            <div v-if="detail.phiVanChuyen > 0" class="hoa-don-totals__row">
              <span>Phí vận chuyển</span>
              <span>{{ formatCurrency(detail.phiVanChuyen) }}</span>
            </div>
            <div class="hoa-don-totals__grand">
              <span>Thành tiền</span>
              <span>{{ formatCurrency(detail.thanhTien) }}</span>
            </div>
          </div>

          <div v-if="detail.soTienKhachDua != null || detail.maGiaoDich" class="hoa-don-pay-block">
            <p v-if="detail.soTienKhachDua != null" class="text-sm">
              Tiền khách đưa: <strong>{{ formatCurrency(detail.soTienKhachDua) }}</strong>
              <span v-if="detail.tienThua != null">
                · Tiền thối: <strong class="text-[var(--warm-tan)]">{{ formatCurrency(detail.tienThua) }}</strong>
              </span>
            </p>
            <p v-if="detail.maGiaoDich" class="text-sm mt-1">
              Mã giao dịch: <span class="soleil-sp-code">{{ detail.maGiaoDich }}</span>
            </p>
          </div>
        </section>

        <aside class="soleil-card hoa-don-timeline no-print">
          <h2 class="hoa-don-section-title">Nhật ký hành động</h2>

          <div v-if="lichSu.length === 0" class="text-sm text-[var(--admin-muted)] py-8 text-center">
            Chưa có nhật ký
          </div>

          <ul v-else class="hoa-don-timeline__list">
            <li v-for="entry in lichSu" :key="entry.id" class="hoa-don-timeline__item">
              <span class="hoa-don-timeline__icon" aria-hidden="true">
                <Icon :icon="actionIcon(entry.trangThai)" width="18" />
              </span>
              <div class="hoa-don-timeline__body">
                <p class="hoa-don-timeline__text">{{ timelineText(entry) }}</p>
                <time class="hoa-don-timeline__time">{{ formatDateTime(entry.thoiGian) }}</time>
              </div>
            </li>
          </ul>
        </aside>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.soleil-card {
  background: #fff;
  border: 1px solid var(--admin-border, rgba(30, 21, 16, 0.1));
  border-radius: 1rem;
  padding: 1.5rem;
}

.hoa-don-print-brand {
  display: none;
  font-family: var(--font-serif, Georgia, serif);
  font-size: 1.25rem;
  letter-spacing: 0.3em;
  text-align: center;
  margin-bottom: 0.75rem;
}

@media print {
  .hoa-don-print-brand {
    display: block !important;
  }
}

.hoa-don-summary__main {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  flex-wrap: wrap;
  margin-bottom: 1.25rem;
  padding-bottom: 1.25rem;
  border-bottom: 1px dashed var(--admin-border);
}
.hoa-don-summary__code {
  font-family: var(--font-serif, Georgia, serif);
  font-size: 1.75rem;
  font-weight: 600;
  color: var(--ink);
}
.hoa-don-summary__datetime {
  font-size: 0.85rem;
  color: var(--admin-muted);
  margin-top: 0.25rem;
}
.hoa-don-summary__badges {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}
.hd-badge {
  display: inline-block;
  padding: 0.25rem 0.65rem;
  border-radius: 999px;
  font-size: 0.72rem;
  font-weight: 600;
}
.hd-badge--gold { background: rgba(196, 149, 84, 0.15); color: var(--bronze, #a67c3d); }
.hd-badge--teal { background: rgba(72, 140, 130, 0.12); color: var(--sage, #488c82); }
.hd-badge--success { background: rgba(72, 140, 82, 0.12); color: #3d7a4a; }
.hd-badge--info { background: rgba(72, 120, 180, 0.12); color: #3a6ea8; }
.hd-badge--danger { background: rgba(180, 72, 72, 0.12); color: #a83a3a; }
.hd-badge--warning { background: rgba(196, 149, 84, 0.18); color: #8a6428; }
.hd-badge--neutral { background: rgba(30, 21, 16, 0.06); color: rgba(30, 21, 16, 0.55); }

.hoa-don-summary__meta {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(10rem, 1fr));
  gap: 1rem;
  margin-bottom: 1.25rem;
}
.hoa-don-summary__meta-item {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
  font-size: 0.9rem;
}
.hoa-don-summary__label {
  font-size: 0.7rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--admin-muted);
}
.hoa-don-summary__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
  padding-top: 1rem;
  border-top: 1px solid var(--admin-border);
}
.hoa-don-summary__total-label {
  display: block;
  font-size: 0.7rem;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: var(--admin-muted);
}
.hoa-don-summary__total-value {
  font-family: var(--font-serif, Georgia, serif);
  font-size: 1.75rem;
  font-weight: 600;
  color: var(--bronze, #a67c3d);
}

.hoa-don-detail-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.5rem;
}
@media (min-width: 1024px) {
  .hoa-don-detail-grid {
    grid-template-columns: 3fr 2fr;
  }
}

.hoa-don-section-title {
  font-family: var(--font-serif, Georgia, serif);
  font-size: 1.05rem;
  font-weight: 600;
  margin-bottom: 1rem;
  color: var(--ink);
}

.hoa-don-totals {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px dashed var(--admin-border);
}
.hoa-don-totals__row {
  display: flex;
  justify-content: space-between;
  font-size: 0.875rem;
  color: var(--admin-muted);
  padding: 0.2rem 0;
}
.hoa-don-totals__grand {
  display: flex;
  justify-content: space-between;
  margin-top: 0.5rem;
  padding-top: 0.5rem;
  border-top: 1px solid var(--admin-border);
  font-weight: 600;
  font-size: 1rem;
  color: var(--ink);
}
.hoa-don-pay-block {
  margin-top: 1rem;
  padding: 0.75rem 1rem;
  background: var(--cream, #faf6f0);
  border-radius: 0.5rem;
}

.hoa-don-timeline__list {
  list-style: none;
  margin: 0;
  padding: 0;
}
.hoa-don-timeline__item {
  display: flex;
  gap: 0.75rem;
  padding: 0.85rem 0;
  border-bottom: 1px solid rgba(30, 21, 16, 0.06);
}
.hoa-don-timeline__item:last-child {
  border-bottom: none;
}
.hoa-don-timeline__icon {
  flex-shrink: 0;
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(201, 169, 110, 0.15);
  color: var(--bronze, #a67c3d);
}
.hoa-don-timeline__text {
  font-size: 0.85rem;
  line-height: 1.45;
  color: var(--ink);
}
.hoa-don-timeline__time {
  font-size: 0.75rem;
  color: var(--admin-muted);
  margin-top: 0.2rem;
  display: block;
}
</style>
