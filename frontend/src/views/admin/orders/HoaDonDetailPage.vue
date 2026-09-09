<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import GanLoDonHangModal from '@/components/admin/orders/GanLoDonHangModal.vue'
import { getHoaDonDetail, getLichSu, taoVanDonGhn, giaLapWebhookGhn, tuChoiDon } from '@/api/hoaDonApi'
import { confirm, alertDialog } from '@/composables/useConfirm'
import { subscribeAdminOrders } from '@/composables/useRealtime'
import { GHN_STATUS_OPTIONS } from '@/constants/ghnStatuses'
import { formatCurrency } from '@/utils/format'
import { toast } from '@/composables/useToast'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const error = ref('')
const detail = ref(null)
const lichSu = ref([])

const ghnLoading = ref(false)

const actionLoading = ref(false)
const actionMessage = ref('')
const actionMessageType = ref('success')
const showGanLoModal = ref(false)

const webhookLoading = ref(false)
const webhookMessage = ref('')
const webhookMessageType = ref('success')
const selectedGhnStatus = ref(GHN_STATUS_OPTIONS[0]?.value || '')
const webhookGhiChu = ref('')

const TRANG_THAI_KET_THUC = new Set(['HOAN_THANH', 'TRA_HANG', 'DA_HUY'])
const TRANG_THAI_TAO_VAN_DON = new Set(['DA_XAC_NHAN', 'DANG_CHUAN_BI', 'DANG_GIAO'])

const coTheTaoVanDon = computed(
  () =>
    detail.value &&
    !detail.value.maVanDonGhn &&
    TRANG_THAI_TAO_VAN_DON.has(detail.value.trangThai),
)

const coTheXacNhanDon = computed(() => detail.value?.trangThai === 'CHO_XAC_NHAN')

const coTheTuChoiDon = computed(() => detail.value?.trangThai === 'CHO_XAC_NHAN')

const coTheHuyDonDaXacNhan = computed(
  () =>
    detail.value?.loaiDon === 'ONLINE' &&
    (detail.value?.trangThai === 'DA_XAC_NHAN' || detail.value?.trangThai === 'DANG_CHUAN_BI'),
)

const coTheXuLyDon = computed(
  () => coTheXacNhanDon.value || coTheTuChoiDon.value || coTheHuyDonDaXacNhan.value,
)

const coTheGiaLapWebhook = computed(
  () =>
    detail.value &&
    detail.value.maVanDonGhn &&
    !TRANG_THAI_KET_THUC.has(detail.value.trangThai),
)

const canCapNhatWebhook = computed(
  () => Boolean(selectedGhnStatus.value) && coTheGiaLapWebhook.value,
)

function notifyGhn(text, type = 'success') {
  if (type === 'error') {
    void alertDialog({
      title: 'Không tạo được vận đơn GHN',
      message: String(text || 'Không tạo được vận đơn GHN. Vui lòng thử lại.'),
      confirmText: 'Đóng',
    })
    return
  }
  toast(text, 'success')
}

function notifyAction(text, type = 'success') {
  actionMessage.value = text
  actionMessageType.value = type
  setTimeout(() => { actionMessage.value = '' }, 5000)
}

function notifyWebhook(text, type = 'success') {
  webhookMessage.value = text
  webhookMessageType.value = type
  setTimeout(() => { webhookMessage.value = '' }, 5000)
}

function resetWebhookSelection() {
  selectedGhnStatus.value = GHN_STATUS_OPTIONS[0]?.value || ''
  webhookGhiChu.value = ''
}

function ghnStatusLabel(status) {
  return GHN_STATUS_OPTIONS.find((opt) => opt.value === status)?.label || status
}

async function handleXacNhanDon() {
  if (!orderId.value || !coTheXacNhanDon.value || actionLoading.value) return
  showGanLoModal.value = true
}

async function onGanLoSuccess() {
  notifyAction('Đã xác nhận đơn và cập nhật phân bổ lô', 'success')
  await loadDetail()
  await tryTaoVanDonSauXacNhan()
  await loadDetail()
}

function onGanLoError(message) {
  notifyAction(message || 'Không xác nhận / gán lô được', 'error')
}

async function handleTuChoiDon() {
  if (!orderId.value || !coTheTuChoiDon.value || actionLoading.value) return

  const ok = await confirm({
    title: 'Từ chối đơn hàng',
    message: `Từ chối đơn ${detail.value.maHoaDon}? Hàng sẽ được hoàn về kho.`,
    confirmText: 'Từ chối',
    danger: true,
  })
  if (!ok) return

  actionLoading.value = true
  try {
    await tuChoiDon(orderId.value, { ghiChu: 'Admin từ chối đơn hàng' })
    notifyAction('Đã từ chối đơn hàng', 'success')
    await loadDetail()
  } catch (err) {
    notifyAction(typeof err === 'string' ? err : 'Không từ chối được đơn hàng', 'error')
  } finally {
    actionLoading.value = false
  }
}

async function handleHuyDonDaXacNhan() {
  if (!orderId.value || !coTheHuyDonDaXacNhan.value || actionLoading.value) return

  const ok = await confirm({
    title: 'Hủy đơn hàng',
    message: `Hủy đơn ${detail.value.maHoaDon}? Hàng sẽ được hoàn về kho.`,
    confirmText: 'Hủy đơn',
    danger: true,
  })
  if (!ok) return

  actionLoading.value = true
  try {
    await tuChoiDon(orderId.value, { ghiChu: 'Admin hủy đơn hàng' })
    notifyAction('Đã hủy đơn hàng', 'success')
    await loadDetail()
  } catch (err) {
    notifyAction(typeof err === 'string' ? err : 'Không hủy được đơn hàng', 'error')
  } finally {
    actionLoading.value = false
  }
}

async function tryTaoVanDonSauXacNhan() {
  if (!coTheTaoVanDon.value) {
    if (detail.value?.maVanDonGhn) {
      notifyGhn(`Đã tạo vận đơn GHN: ${detail.value.maVanDonGhn}`, 'success')
    }
    return false
  }
  ghnLoading.value = true
  try {
    const res = await taoVanDonGhn(orderId.value)
    const payload = res.data
    if (payload?.thanhCong) {
      notifyGhn(payload.thongDiep || `Đã tạo vận đơn GHN: ${payload.maVanDon}`, 'success')
      await loadDetail()
      return true
    }
    notifyGhn(
      payload?.thongDiep || 'Không tạo được vận đơn GHN.',
      'error',
    )
    return false
  } catch (err) {
    notifyGhn(
      typeof err === 'string' ? err : 'Không tạo được vận đơn GHN.',
      'error',
    )
    return false
  } finally {
    ghnLoading.value = false
  }
}

async function handleCapNhatWebhook() {
  if (!orderId.value || !canCapNhatWebhook.value) return

  const status = selectedGhnStatus.value
  const ghiChu = webhookGhiChu.value.trim() || undefined
  const statusLabelText = ghnStatusLabel(status)

  webhookLoading.value = true
  try {
    const res = await giaLapWebhookGhn(orderId.value, { status, ghiChu })
    const payload = res.data
    if (payload?.daCapNhat) {
      notifyWebhook(
        payload.thongDiep ||
          `Đã cập nhật trạng thái đơn thành "${statusLabelText}"`,
        'success',
      )
    } else {
      notifyWebhook(
        payload?.thongDiep || `Trạng thái "${statusLabelText}" không làm thay đổi đơn.`,
        'error',
      )
    }
    await loadDetail()
  } catch (err) {
    notifyWebhook(typeof err === 'string' ? err : 'Không cập nhật được trạng thái đơn', 'error')
  } finally {
    webhookLoading.value = false
  }
}

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
    TRA_HANG: 'Trả hàng',
    DA_HUY: 'Đã hủy',
  }
  return map[trangThai] || trangThai || '—'
}

function statusTone(trangThai) {
  if (trangThai === 'HOAN_THANH') return 'success'
  if (trangThai === 'TRA_HANG') return 'warning'
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

function formatDate(value) {
  if (!value) return '—'
  return new Date(value).toLocaleDateString('vi-VN')
}

function timelineToneClass(ma) {
  if (ma === 'HOAN_THANH' || ma === 'DA_XAC_NHAN' || ma === 'THANH_TOAN') return 'is-done'
  if (ma === 'CHO_XAC_NHAN' || ma === 'CHO' || ma === 'TRA_HANG' || ma === 'DANG_GIAO') return 'is-warn'
  if (ma === 'DA_HUY') return 'is-danger'
  return ''
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
    resetWebhookSelection()
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

let unsubscribeOrders = null

function onOrderRealtime(event) {
  const currentId = Number(route.params.id)
  const eventId = Number(event?.idHoaDon)
  if (!eventId || eventId !== currentId) return
  loadDetail()
  // Tránh toast trùng khi chính tab này vừa bấm webhook / đổi trạng thái
  if (
    event.type === 'ORDER_STATUS_CHANGED' &&
    !webhookLoading.value &&
    !actionLoading.value &&
    !ghnLoading.value
  ) {
    toast(event.message || 'Trạng thái đơn đã cập nhật', 'info')
  }
}

function onOrderRealtimeWindow(e) {
  onOrderRealtime(e?.detail)
}

onMounted(() => {
  loadDetail()
  unsubscribeOrders = subscribeAdminOrders(onOrderRealtime)
  window.addEventListener('sunova-admin-order-realtime', onOrderRealtimeWindow)
})

onUnmounted(() => {
  unsubscribeOrders?.()
  unsubscribeOrders = null
  window.removeEventListener('sunova-admin-order-realtime', onOrderRealtimeWindow)
})
</script>

<template>
  <div class="hoa-don-detail-page">
    <PageHeader
      class="no-print"
      title="Chi tiết hóa đơn"
      description="Xử lý đơn · phân bổ lô · vận đơn GHN · nhật ký trạng thái"
    >
      <template #actions>
        <button type="button" class="soleil-btn-outline hd-btn" @click="goBack">
          <Icon icon="icon-park-outline:back" />
          Danh sách
        </button>
      </template>
    </PageHeader>

    <div v-if="loading" class="soleil-card hd-panel p-12 text-center text-[#5a6a72]">
      Đang tải hóa đơn...
    </div>

    <div v-else-if="error" class="admin-alert admin-alert-error px-4 py-3">
      {{ error }}
    </div>

    <template v-else-if="detail">
      <div id="hoa-don-print-area" class="hoa-don-print-area">
        <section class="hoa-don-summary soleil-card hd-panel">
          <div class="hoa-don-print-brand">SUNOVA</div>
          <div class="hoa-don-summary__main">
            <div>
              <p class="hoa-don-summary__kicker">Hóa đơn bán hàng</p>
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
              <span class="hoa-don-summary__value">{{ detail.tenKhachHang || 'Khách lẻ' }}</span>
              <span v-if="detail.soDienThoaiKhachHang" class="hoa-don-summary__sub">
                {{ detail.soDienThoaiKhachHang }}
              </span>
            </div>
            <div v-if="detail.tenNhanVien" class="hoa-don-summary__meta-item">
              <span class="hoa-don-summary__label">Nhân viên</span>
              <span class="hoa-don-summary__value">{{ detail.tenNhanVien }}</span>
            </div>
            <div v-if="detail.tenPhuongThucThanhToan" class="hoa-don-summary__meta-item">
              <span class="hoa-don-summary__label">Thanh toán</span>
              <span class="hoa-don-summary__value">{{ detail.tenPhuongThucThanhToan }}</span>
            </div>
            <div v-if="detail.diaChiGiao" class="hoa-don-summary__meta-item">
              <span class="hoa-don-summary__label">Địa chỉ nhận hàng</span>
              <span class="hoa-don-summary__value">{{ detail.diaChiGiao }}</span>
            </div>
            <div v-if="detail.maVanDonGhn" class="hoa-don-summary__meta-item">
              <span class="hoa-don-summary__label">Mã vận đơn</span>
              <span class="hoa-don-summary__value">
                <span class="hoa-don-mono">{{ detail.maVanDonGhn }}</span>
              </span>
            </div>
          </div>

          <div class="hoa-don-summary__footer">
            <div class="hoa-don-summary__total">
              <span class="hoa-don-summary__total-label">Thành tiền</span>
              <span class="hoa-don-summary__total-value">{{ formatCurrency(detail.thanhTien) }}</span>
            </div>
            <button type="button" class="soleil-btn-outline hd-btn no-print" @click="printInvoice">
              <Icon icon="icon-park-outline:printer" />
              In hóa đơn
            </button>
          </div>
        </section>

        <section v-if="coTheXuLyDon" class="hoa-don-strip no-print">
          <div>
            <h2 class="hoa-don-strip__title">Cần xử lý</h2>
            <p class="hoa-don-strip__hint">
              <template v-if="coTheXacNhanDon">
                Đơn chờ xác nhận. Xác nhận sẽ mở phân bổ lô (FEFO / chỉnh tay), chuyển sang
                <strong>Đã xác nhận</strong>, rồi tự thử tạo vận đơn GHN.
              </template>
              <template v-else-if="coTheHuyDonDaXacNhan">
                Đơn online chưa chuyển sang đang giao — có thể hủy và hoàn hàng về kho.
              </template>
            </p>
          </div>
          <div class="hoa-don-strip__btns">
            <button
              v-if="coTheXacNhanDon"
              type="button"
              class="soleil-btn-primary hd-btn hoa-don-strip__primary"
              :disabled="actionLoading || ghnLoading"
              @click="handleXacNhanDon"
            >
              <Icon icon="mdi:check-circle-outline" />
              {{ actionLoading ? 'Đang xử lý...' : 'Xác nhận đơn' }}
            </button>

            <button
              v-if="coTheTuChoiDon"
              type="button"
              class="soleil-btn-outline hd-btn hoa-don-strip__danger"
              :disabled="actionLoading"
              @click="handleTuChoiDon"
            >
              <Icon icon="mdi:close-circle-outline" />
              {{ actionLoading ? 'Đang xử lý...' : 'Từ chối' }}
            </button>

            <button
              v-if="coTheHuyDonDaXacNhan"
              type="button"
              class="soleil-btn-outline hd-btn hoa-don-strip__danger"
              :disabled="actionLoading"
              @click="handleHuyDonDaXacNhan"
            >
              <Icon icon="mdi:cancel" />
              {{ actionLoading ? 'Đang xử lý...' : 'Hủy đơn' }}
            </button>
          </div>
          <div
            v-if="actionMessage"
            class="admin-alert px-4 py-2 text-sm hoa-don-strip__msg"
            :class="actionMessageType === 'error' ? 'admin-alert-error' : 'admin-alert-success'"
          >
            {{ actionMessage }}
          </div>
        </section>

        <div class="hoa-don-modules">
        <div v-if="coTheGiaLapWebhook" class="hoa-don-ops no-print">
          <section class="soleil-card hd-panel hd-module hoa-don-webhook">
            <div class="hoa-don-ops__head hd-module__head">
              <h2 class="hoa-don-ops__title">
                <span class="hd-module__idx" aria-hidden="true" />
                Cập nhật trạng thái
              </h2>
            </div>

            <div class="hd-module__body">
            <div class="hoa-don-webhook__form">
              <div class="hoa-don-webhook__field">
                <span class="hoa-don-webhook__label">Trạng thái hiện tại</span>
                <div class="hoa-don-webhook__select hoa-don-webhook__current" aria-readonly="true">
                  {{ statusLabel(detail.trangThai) }}
                </div>
              </div>

              <div class="hoa-don-webhook__field">
                <label class="hoa-don-webhook__label" for="webhook-ghn-status">Trạng thái GHN</label>
                <select
                  id="webhook-ghn-status"
                  v-model="selectedGhnStatus"
                  class="hoa-don-webhook__select"
                >
                  <option
                    v-for="opt in GHN_STATUS_OPTIONS"
                    :key="opt.value"
                    :value="opt.value"
                  >
                    {{ opt.label }} ({{ opt.value }})
                  </option>
                </select>
              </div>

              <div class="hoa-don-webhook__field hoa-don-webhook__field--wide">
                <label class="hoa-don-webhook__label" for="webhook-ghi-chu">Ghi chú</label>
                <input
                  id="webhook-ghi-chu"
                  v-model="webhookGhiChu"
                  type="text"
                  class="hoa-don-webhook__input"
                  placeholder="Ví dụ: Khách hẹn nhận chiều"
                />
              </div>

              <button
                type="button"
                class="soleil-btn-primary hd-btn hoa-don-webhook__submit"
                :disabled="!canCapNhatWebhook || webhookLoading"
                @click="handleCapNhatWebhook"
              >
                <Icon icon="icon-park-outline:refresh" />
                {{ webhookLoading ? 'Đang cập nhật...' : 'Cập nhật trạng thái' }}
              </button>
            </div>

            <div
              v-if="webhookMessage"
              class="admin-alert px-4 py-2 mt-3 text-sm"
              :class="webhookMessageType === 'error' ? 'admin-alert-error' : 'admin-alert-success'"
            >
              {{ webhookMessage }}
            </div>
            </div>
          </section>
        </div>

        <div class="hoa-don-detail-grid">
          <section class="soleil-card hd-panel hd-module hoa-don-lines">
            <h2 class="hoa-don-section-title hd-module__head">
              <span class="hd-module__label">
                <span class="hd-module__idx" aria-hidden="true" />
                Dòng hàng
              </span>
              <span v-if="detail.chiTiets?.length">{{ detail.chiTiets.length }} dòng</span>
            </h2>

            <div class="hd-module__body">
            <div class="overflow-x-auto">
              <table class="soleil-table admin-table--soleil hoa-don-lines-table w-full">
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
                    <td colspan="4" class="text-center py-8 text-[#5a6a72]">Không có dòng hàng</td>
                  </tr>
                  <template v-for="line in detail.chiTiets || []" :key="line.id">
                    <tr>
                      <td>
                        <div class="hoa-don-prod-name">{{ line.tenSanPham }}</div>
                        <div v-if="line.bienThe || line.sku" class="hoa-don-prod-sku">
                          {{ line.bienThe || line.sku }}
                        </div>
                        <div v-if="line.loHangs?.length" class="hoa-don-lots">
                          <strong>Lô:</strong>
                          <span
                            v-for="(lo, idx) in line.loHangs"
                            :key="lo.idLoHang"
                            class="hoa-don-lots__item"
                          >
                            <template v-if="idx > 0">; </template>
                            {{ lo.soLo || `#${lo.idLoHang}` }}
                            · SL {{ lo.soLuongDaBan }}
                            <template v-if="lo.hanSuDung"> · HSD {{ formatDate(lo.hanSuDung) }}</template>
                          </span>
                        </div>
                      </td>
                      <td class="text-center"><strong>{{ line.soLuong }}</strong></td>
                      <td class="text-right hoa-don-money">{{ formatCurrency(line.donGia) }}</td>
                      <td class="text-right hoa-don-money hoa-don-money--strong">{{ formatCurrency(line.thanhTien) }}</td>
                    </tr>
                  </template>
                </tbody>
              </table>
            </div>

            <div class="hoa-don-totals">
              <div class="hoa-don-totals__row">
                <span>Tổng tiền hàng</span>
                <span>{{ formatCurrency(detail.tongTien) }}</span>
              </div>
              <div v-if="detail.tienGiamGia > 0" class="hoa-don-totals__row hoa-don-totals__row--disc">
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
                  · Tiền thối: <strong>{{ formatCurrency(detail.tienThua) }}</strong>
                </span>
              </p>
              <p v-if="detail.maGiaoDich" class="text-sm mt-1">
                Mã giao dịch: <span class="hoa-don-mono">{{ detail.maGiaoDich }}</span>
              </p>
            </div>

            <div v-if="detail.trangThaiHoanTien" class="hoa-don-pay-block">
              <p class="text-sm font-medium mb-1">Hoàn tiền</p>
              <p class="text-sm">
                Trạng thái:
                <strong>{{ detail.trangThaiHoanTienLabel || detail.trangThaiHoanTien }}</strong>
                <template v-if="detail.soTienHoan != null">
                  · Số tiền: <strong>{{ formatCurrency(detail.soTienHoan) }}</strong>
                </template>
              </p>
              <p v-if="detail.maGiaoDichHoan" class="text-sm mt-1">
                Mã GD hoàn: <span class="hoa-don-mono">{{ detail.maGiaoDichHoan }}</span>
              </p>
            </div>
            </div>
          </section>

          <aside class="soleil-card hd-panel hd-module hoa-don-timeline no-print">
            <h2 class="hoa-don-section-title hd-module__head">
              <span class="hd-module__label">
                <span class="hd-module__idx" aria-hidden="true" />
                Nhật ký
              </span>
              <span v-if="detail.maVanDonGhn" class="hoa-don-mono">{{ detail.maVanDonGhn }}</span>
            </h2>

            <div class="hd-module__body">
            <div v-if="lichSu.length === 0" class="text-sm text-[#5a6a72] py-8 text-center">
              Chưa có nhật ký
            </div>

            <ul v-else class="hoa-don-timeline__list">
              <li
                v-for="entry in lichSu"
                :key="entry.id"
                class="hoa-don-timeline__item"
                :class="timelineToneClass(entry.trangThai)"
              >
                <div class="hoa-don-timeline__rail" aria-hidden="true">
                  <span class="hoa-don-timeline__mark" />
                </div>
                <div class="hoa-don-timeline__body">
                  <p class="hoa-don-timeline__text">{{ timelineText(entry) }}</p>
                  <time class="hoa-don-timeline__time">{{ formatDateTime(entry.thoiGian) }}</time>
                </div>
              </li>
            </ul>
            </div>
          </aside>
        </div>
        </div>
      </div>
    </template>

    <GanLoDonHangModal
      v-model:visible="showGanLoModal"
      :order-id="orderId"
      :ma-hoa-don="detail?.maHoaDon || ''"
      :ten-khach-hang="detail?.tenKhachHang || ''"
      :ngay-tao="detail?.ngayTao || null"
      @success="onGanLoSuccess"
      @error="onGanLoError"
    />
  </div>
</template>

<style scoped>
/* Token copy 1:1 từ docs/sunova-hoadon-admin-concept.html (bản lúc tạo) */
.hoa-don-detail-page {
  --hd-bg: #eef2f5;
  --hd-surface: #ffffff;
  --hd-ink: #0f1a1c;
  --hd-muted: #5a6a72;
  --hd-line: #d5dde6;
  --hd-line-strong: #b8c4cc;
  --hd-accent: #0b6e75;
  --hd-accent-deep: #06484e;
  --hd-accent-soft: #e0eff0;
  --hd-ok: #166534;
  --hd-ok-bg: #e8f5ec;
  --hd-warn: #9a3412;
  --hd-warn-bg: #fff1e8;
  --hd-danger: #991b1b;
  --hd-danger-bg: #fdecec;
  --hd-info: #1e4d7b;
  --hd-info-bg: #e8f0f8;
  --hd-radius: 2px;

  display: flex;
  flex-direction: column;
  gap: 0.85rem;
  color: var(--hd-ink);
}

.hd-panel {
  background: var(--hd-surface) !important;
  border: 1px solid var(--hd-line) !important;
  border-radius: var(--hd-radius) !important;
  padding: 1rem 1.15rem !important;
}

.hoa-don-modules {
  counter-reset: hd-mod;
  display: flex;
  flex-direction: column;
  gap: 1.15rem;
}

.hd-module {
  counter-increment: hd-mod;
  padding: 0 !important;
  overflow: hidden;
  border-left: 4px solid var(--hd-accent) !important;
}

.hd-module__idx {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 1.55rem;
  font-family: ui-monospace, "Cascadia Mono", "Segoe UI Mono", monospace;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.04em;
  color: var(--hd-accent-deep);
}

.hd-module__idx::before {
  content: counter(hd-mod, decimal-leading-zero);
}

.hd-module__label {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.hd-module__head,
.hd-module .hoa-don-ops__head,
.hd-module .hoa-don-section-title {
  margin: 0;
  padding: 0.55rem 1rem;
  background: #f3f6f8;
  border-bottom: 1px solid var(--hd-line);
}

.hd-module__body {
  padding: 0.9rem 1.15rem 1.05rem;
}

.hd-module__body > :first-child {
  margin-top: 0;
}

.hoa-don-detail-page :deep(.soleil-page-header) {
  margin-bottom: 0;
}

.hoa-don-detail-page :deep(.soleil-page-header__title) {
  font-family: inherit;
  font-size: 1.35rem;
  font-weight: 800;
  font-style: normal;
  letter-spacing: 0.02em;
  color: var(--hd-ink);
}

.hoa-don-detail-page :deep(.soleil-page-header__desc) {
  margin: 0.25rem 0 0;
  font-size: 12.5px;
  font-weight: 500;
  color: var(--hd-muted);
}

.hd-btn {
  border-radius: var(--hd-radius) !important;
  font-weight: 700 !important;
  min-height: 36px;
}

.hd-btn--sm {
  min-height: 32px;
  font-size: 12px !important;
  padding: 0 0.75rem !important;
}

/* btn-ghost / btn-primary từ concept — đè soleil-btn ink + vàng kim */
.hoa-don-detail-page :deep(.soleil-btn-outline) {
  background: var(--hd-surface) !important;
  border-color: var(--hd-line-strong) !important;
  color: var(--hd-ink) !important;
}

.hoa-don-detail-page :deep(.soleil-btn-outline:hover) {
  border-color: var(--hd-ink) !important;
  color: var(--hd-ink) !important;
  background: var(--hd-surface) !important;
}

.hoa-don-detail-page :deep(.soleil-btn-primary) {
  background: #0b6e75 !important;
  color: #fff !important;
  border: none !important;
}

.hoa-don-detail-page :deep(.soleil-btn-primary:hover) {
  background: #06484e !important;
  color: #fff !important;
}

.hoa-don-print-brand {
  display: none;
  font-family: ui-monospace, "Cascadia Mono", monospace;
  font-size: 1.1rem;
  font-weight: 700;
  letter-spacing: 0.28em;
  text-align: center;
  margin-bottom: 0.75rem;
}

@media print {
  .hoa-don-print-brand {
    display: block !important;
  }
}

.hoa-don-summary {
  border-left: 4px solid var(--hd-accent) !important;
}

.hoa-don-summary__main {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  flex-wrap: wrap;
}

.hoa-don-summary__kicker {
  margin: 0;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--hd-accent);
}

.hoa-don-summary__code {
  margin: 0.2rem 0 0;
  font-family: ui-monospace, "Cascadia Mono", "Segoe UI Mono", monospace;
  font-size: 1.55rem;
  font-weight: 700;
  letter-spacing: 0.02em;
  line-height: 1.15;
  color: var(--hd-ink);
}

.hoa-don-summary__datetime {
  margin: 0.35rem 0 0;
  font-size: 12.5px;
  color: var(--hd-muted);
  font-variant-numeric: tabular-nums;
}

.hoa-don-summary__badges {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.4rem;
}

.hd-badge {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 0.55rem;
  border-radius: var(--hd-radius);
  border: 1px solid currentColor;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  white-space: nowrap;
}

/* tag--pos */
.hd-badge--gold {
  color: #7c4a12;
  background: #fff4e5;
  border-color: #d4a574;
}
/* tag--online */
.hd-badge--teal {
  color: var(--hd-accent-deep);
  background: var(--hd-accent-soft);
}
.hd-badge--success {
  color: var(--hd-ok);
  background: var(--hd-ok-bg);
}
.hd-badge--info {
  color: var(--hd-info);
  background: var(--hd-info-bg);
}
.hd-badge--danger {
  color: var(--hd-danger);
  background: var(--hd-danger-bg);
}
.hd-badge--warning {
  color: var(--hd-warn);
  background: var(--hd-warn-bg);
}
.hd-badge--neutral {
  color: #4b5563;
  background: #f1f3f5;
  border-color: #c5ccd3;
}

.hoa-don-summary__meta {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0;
  border-top: 1px solid var(--hd-line);
  margin-top: 1rem;
}

.hoa-don-summary__meta-item {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
  padding: 0.85rem 1rem 0.15rem 0;
  border-right: 1px solid var(--hd-line);
  min-width: 0;
}

.hoa-don-summary__meta-item:last-child {
  border-right: 0;
  padding-right: 0;
}

.hoa-don-summary__label {
  font-size: 10.5px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--hd-muted);
}

.hoa-don-summary__value {
  font-size: 13.5px;
  font-weight: 600;
  word-break: break-word;
  color: var(--hd-ink);
}

.hoa-don-summary__sub {
  font-size: 12px;
  font-weight: 500;
  color: var(--hd-muted);
}

.hoa-don-summary__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
  margin-top: 1rem;
  padding-top: 0.9rem;
  border-top: 2px solid var(--hd-ink);
}

.hoa-don-summary__total-label {
  display: block;
  font-size: 10.5px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--hd-muted);
}

.hoa-don-summary__total-value {
  font-family: ui-monospace, "Cascadia Mono", "Segoe UI Mono", monospace;
  font-size: 1.65rem;
  font-weight: 700;
  color: var(--hd-accent-deep);
  letter-spacing: -0.01em;
  font-variant-numeric: tabular-nums;
}

.hoa-don-strip {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 1rem;
  align-items: center;
  padding: 0.85rem 1.05rem;
  background: var(--hd-accent-deep);
  color: #fff;
  border-radius: var(--hd-radius);
  margin-bottom: 1.15rem;
}

.hoa-don-strip__title {
  margin: 0;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: #fff;
}

.hoa-don-strip__hint {
  margin: 0.25rem 0 0;
  font-size: 12.5px;
  color: rgba(255, 255, 255, 0.72);
  max-width: 52rem;
}

.hoa-don-strip__hint strong {
  color: #fff;
}

.hoa-don-strip__btns {
  display: flex;
  gap: 0.45rem;
  flex-wrap: wrap;
}

.hoa-don-detail-page :deep(.hoa-don-strip .soleil-btn-primary.hoa-don-strip__primary),
.hoa-don-strip__primary {
  background: #fff !important;
  color: #06484e !important;
}

.hoa-don-detail-page :deep(.hoa-don-strip .soleil-btn-primary.hoa-don-strip__primary:hover),
.hoa-don-strip__primary:hover {
  background: #e0eff0 !important;
  color: #06484e !important;
}

.hoa-don-detail-page :deep(.hoa-don-strip .soleil-btn-outline.hoa-don-strip__danger),
.hoa-don-strip__danger {
  background: transparent !important;
  border-color: rgba(255, 255, 255, 0.35) !important;
  color: #fff !important;
}

.hoa-don-detail-page :deep(.hoa-don-strip .soleil-btn-outline.hoa-don-strip__danger:hover),
.hoa-don-strip__danger:hover {
  background: rgba(255, 255, 255, 0.08) !important;
  border-color: #fff !important;
  color: #fff !important;
}

.hoa-don-strip__msg {
  grid-column: 1 / -1;
}

.hoa-don-ops {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 0.85rem;
  margin-bottom: 0;
}

.hoa-don-ops:has(> :only-child) {
  grid-template-columns: 1fr;
}

.hoa-don-ops__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
}

.hoa-don-ops__title {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  margin: 0;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--hd-ink);
}

.hoa-don-mono {
  font-family: ui-monospace, "Cascadia Mono", "Segoe UI Mono", monospace;
  font-weight: 600;
  font-size: 12.5px;
  background: var(--hd-accent-soft);
  color: var(--hd-accent-deep);
  padding: 0.15rem 0.4rem;
  border-radius: var(--hd-radius);
}

.hoa-don-webhook__form {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.65rem;
  align-items: end;
}

.hoa-don-webhook__field {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.hoa-don-webhook__field--wide {
  grid-column: 1 / -1;
}

.hoa-don-webhook__label {
  font-size: 10.5px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--hd-muted);
}

.hoa-don-webhook__select,
.hoa-don-webhook__input {
  width: 100%;
  min-height: 36px;
  padding: 0 0.65rem;
  border: 1px solid var(--hd-line-strong);
  border-radius: var(--hd-radius);
  background: #fff;
  font-size: 13px;
  color: var(--hd-ink);
}

.hoa-don-webhook__select:focus,
.hoa-don-webhook__input:focus {
  outline: 2px solid rgba(11, 110, 117, 0.25);
  border-color: #0b6e75;
}

.hoa-don-webhook__current {
  display: flex;
  align-items: center;
  background: #f3f6f8;
  font-weight: 600;
  cursor: default;
  user-select: none;
}

.hoa-don-webhook__submit {
  justify-self: start;
}

.hoa-don-detail-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 0.85rem;
}

@media (min-width: 1024px) {
  .hoa-don-detail-grid {
    grid-template-columns: minmax(0, 1.7fr) minmax(260px, 1fr);
  }
}

.hoa-don-section-title {
  margin: 0;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  color: var(--hd-ink);
}

.hoa-don-section-title span {
  font-weight: 600;
  letter-spacing: 0;
  text-transform: none;
  color: var(--hd-muted);
  font-size: 12px;
}

.hoa-don-section-title .hoa-don-mono {
  color: var(--hd-accent-deep);
}

.hoa-don-lines-table.admin-table--soleil thead th,
.hoa-don-lines-table thead th,
.hoa-don-lines-table :deep(table.admin-table--soleil thead th),
.hoa-don-lines-table :deep(thead th) {
  background: #b8976a !important;
  color: #fffef9 !important;
  font-size: 11px !important;
  font-weight: 700 !important;
  letter-spacing: 0.08em !important;
  padding: 0.55rem 0.7rem !important;
  border-bottom: none !important;
}

.hoa-don-lines-table :deep(tbody td) {
  padding: 0.7rem !important;
  border-bottom: 1px solid var(--hd-line);
  vertical-align: top;
  color: var(--hd-ink);
}

.hoa-don-lines-table :deep(tbody tr:hover) {
  background: #f3f8f8 !important;
}

.hoa-don-prod-name {
  font-weight: 700;
  font-size: 13.5px;
}

.hoa-don-prod-sku {
  margin-top: 0.15rem;
  font-family: ui-monospace, "Cascadia Mono", monospace;
  font-size: 11.5px;
  color: var(--hd-muted);
}

.hoa-don-lots {
  margin-top: 0.35rem;
  padding: 0.35rem 0.5rem;
  background: #f3f6f8;
  border-left: 2px solid var(--hd-line-strong);
  font-size: 11.5px;
  color: var(--hd-muted);
  line-height: 1.4;
}

.hoa-don-lots strong {
  color: var(--hd-ink);
  font-weight: 700;
  margin-right: 0.25rem;
}

.hoa-don-lots__item {
  color: var(--hd-ink);
}

.hoa-don-money {
  font-variant-numeric: tabular-nums;
  font-size: 13px;
}

.hoa-don-money--strong {
  font-weight: 700;
  font-family: ui-monospace, "Cascadia Mono", monospace;
  font-size: 12.5px;
}

.hoa-don-totals {
  margin-top: 0.85rem;
  padding-top: 0.75rem;
  border-top: 1px solid var(--hd-line);
  max-width: 280px;
  margin-left: auto;
}

.hoa-don-totals__row {
  display: flex;
  justify-content: space-between;
  gap: 1.5rem;
  padding: 0.2rem 0;
  font-size: 13px;
  color: var(--hd-muted);
}

.hoa-don-totals__row span:last-child {
  color: var(--hd-ink);
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

.hoa-don-totals__row--disc span:last-child {
  color: var(--hd-ok);
}

.hoa-don-totals__grand {
  display: flex;
  justify-content: space-between;
  gap: 1.5rem;
  margin-top: 0.45rem;
  padding-top: 0.55rem;
  border-top: 2px solid var(--hd-ink);
  font-size: 14px;
  font-weight: 800;
  color: var(--hd-ink);
}

.hoa-don-totals__grand span:last-child {
  font-family: ui-monospace, "Cascadia Mono", monospace;
  font-size: 1.05rem;
  color: var(--hd-accent-deep);
}

.hoa-don-pay-block {
  margin-top: 0.85rem;
  padding: 0.65rem 0.75rem;
  border: 1px dashed var(--hd-line-strong);
  border-radius: var(--hd-radius);
  background: transparent;
  font-size: 12.5px;
}

.hoa-don-timeline__list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.hoa-don-timeline__item {
  display: grid;
  grid-template-columns: 14px 1fr;
  gap: 0.7rem;
  padding-bottom: 1rem;
}

.hoa-don-timeline__item:last-child {
  padding-bottom: 0;
}

.hoa-don-timeline__rail {
  position: relative;
  display: flex;
  justify-content: center;
}

.hoa-don-timeline__rail::before {
  content: "";
  position: absolute;
  top: 14px;
  bottom: -4px;
  width: 2px;
  background: var(--hd-line-strong);
}

.hoa-don-timeline__item:last-child .hoa-don-timeline__rail::before {
  display: none;
}

.hoa-don-timeline__mark {
  width: 10px;
  height: 10px;
  margin-top: 4px;
  background: var(--hd-accent);
  border: 2px solid var(--hd-surface);
  box-shadow: 0 0 0 1px var(--hd-accent);
  position: relative;
  z-index: 1;
}

.hoa-don-timeline__item.is-done .hoa-don-timeline__mark {
  background: var(--hd-ok);
  box-shadow: 0 0 0 1px var(--hd-ok);
}

.hoa-don-timeline__item.is-warn .hoa-don-timeline__mark {
  background: var(--hd-warn);
  box-shadow: 0 0 0 1px var(--hd-warn);
}

.hoa-don-timeline__item.is-danger .hoa-don-timeline__mark {
  background: var(--hd-danger);
  box-shadow: 0 0 0 1px var(--hd-danger);
}

.hoa-don-timeline__text {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  line-height: 1.4;
  color: var(--hd-ink);
}

.hoa-don-timeline__time {
  display: block;
  margin-top: 0.2rem;
  font-size: 11.5px;
  color: var(--hd-muted);
  font-variant-numeric: tabular-nums;
  font-family: ui-monospace, "Cascadia Mono", monospace;
}

@media (max-width: 1100px) {
  .hoa-don-ops {
    grid-template-columns: 1fr;
  }
  .hoa-don-summary__meta {
    grid-template-columns: 1fr 1fr;
  }
  .hoa-don-summary__meta-item {
    border-right: 0;
    border-bottom: 1px solid var(--hd-line);
    padding: 0.75rem 0;
  }
}

@media (max-width: 640px) {
  .hoa-don-summary__meta {
    grid-template-columns: 1fr;
  }
  .hoa-don-summary__badges {
    align-items: flex-start;
    flex-direction: row;
  }
  .hoa-don-strip {
    grid-template-columns: 1fr;
  }
  .hoa-don-webhook__form {
    grid-template-columns: 1fr;
  }
}
</style>

