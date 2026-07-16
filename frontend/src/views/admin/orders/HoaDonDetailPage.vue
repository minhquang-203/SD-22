<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import { getHoaDonDetail, getLichSu, taoVanDonGhn, dongBoGhn, giaLapWebhookGhn, capNhatTrangThai, tuChoiDon } from '@/api/hoaDonApi'
import { confirm } from '@/composables/useConfirm'
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
const ghnMessage = ref('')
const ghnMessageType = ref('success')

const actionLoading = ref(false)
const actionMessage = ref('')
const actionMessageType = ref('success')

const webhookLoading = ref(false)
const webhookMessage = ref('')
const webhookMessageType = ref('success')
const selectedGhnStatus = ref(GHN_STATUS_OPTIONS[0]?.value || '')
const webhookGhiChu = ref('')

const TRANG_THAI_KET_THUC = new Set(['HOAN_THANH', 'TRA_HANG', 'DA_HUY'])

const coTheTaoVanDon = computed(
  () =>
    detail.value &&
    !detail.value.maVanDonGhn &&
    !TRANG_THAI_KET_THUC.has(detail.value.trangThai),
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
  ghnMessage.value = text
  ghnMessageType.value = type
  setTimeout(() => { ghnMessage.value = '' }, 5000)
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

  const ok = await confirm({
    title: 'Xác nhận đơn hàng',
    message: `Xác nhận đơn ${detail.value.maHoaDon}? Hệ thống sẽ thử tạo vận đơn GHN.`,
    confirmText: 'Xác nhận',
  })
  if (!ok) return

  actionLoading.value = true
  try {
    await capNhatTrangThai(orderId.value, {
      trangThai: 'DA_XAC_NHAN',
      ghiChu: 'Admin xác nhận đơn hàng',
    })
    notifyAction('Đã xác nhận đơn hàng', 'success')
    await loadDetail()
    const ghnOk = await tryTaoVanDonSauXacNhan()
    await loadDetail()
    if (!ghnOk && !detail.value?.maVanDonGhn) {
      notifyAction('Đơn đã xác nhận nhưng chưa tạo được vận đơn GHN. Xem thông báo bên phần GHN.', 'error')
    }
  } catch (err) {
    notifyAction(typeof err === 'string' ? err : 'Không xác nhận được đơn hàng', 'error')
  } finally {
    actionLoading.value = false
  }
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
      payload?.thongDiep || 'Không tạo được vận đơn GHN. Vui lòng thử lại bằng nút bên dưới.',
      'error',
    )
    return false
  } catch (err) {
    notifyGhn(
      typeof err === 'string' ? err : 'Không tạo được vận đơn GHN. Vui lòng thử lại bằng nút bên dưới.',
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
          `Đã cập nhật đơn theo webhook GHN "${statusLabelText}"`,
        'success',
      )
    } else {
      notifyWebhook(
        payload?.thongDiep || `Webhook GHN "${statusLabelText}" không đổi trạng thái đơn.`,
        'error',
      )
    }
    await loadDetail()
  } catch (err) {
    notifyWebhook(typeof err === 'string' ? err : 'Không gửi được webhook GHN giả lập', 'error')
  } finally {
    webhookLoading.value = false
  }
}

async function handleTaoVanDon() {
  if (!orderId.value) return
  ghnLoading.value = true
  try {
    const res = await taoVanDonGhn(orderId.value)
    const payload = res.data
    if (payload?.thanhCong) {
      notifyGhn(payload.thongDiep || `Đã tạo vận đơn GHN: ${payload.maVanDon}`, 'success')
      await loadDetail()
    } else {
      notifyGhn(payload?.thongDiep || 'Không tạo được vận đơn GHN', 'error')
    }
  } catch (err) {
    notifyGhn(typeof err === 'string' ? err : 'Không tạo được vận đơn GHN', 'error')
  } finally {
    ghnLoading.value = false
  }
}

async function handleDongBoGhn() {
  if (!orderId.value) return
  ghnLoading.value = true
  try {
    const res = await dongBoGhn(orderId.value)
    notifyGhn(res.data?.thongDiep || 'Đã đồng bộ trạng thái GHN', 'success')
    await loadDetail()
  } catch (err) {
    notifyGhn(typeof err === 'string' ? err : 'Không đồng bộ được trạng thái GHN', 'error')
  } finally {
    ghnLoading.value = false
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
    TRA_HANG: 'mdi:package-variant-closed-remove',
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

        <section v-if="coTheXuLyDon" class="soleil-card hoa-don-actions no-print">
          <div class="hoa-don-actions__head">
            <h2 class="hoa-don-section-title" style="margin: 0">
              <Icon icon="mdi:clipboard-check-outline" width="18" class="hoa-don-actions__title-icon" />
              Xử lý đơn hàng
            </h2>
          </div>

          <p class="hoa-don-actions__hint">
            <template v-if="coTheXacNhanDon">
              Đơn đang chờ xác nhận. Xác nhận để chuyển sang <strong>Đã xác nhận</strong> và tự thử tạo vận đơn GHN.
            </template>
            <template v-else-if="coTheHuyDonDaXacNhan">
              Đơn online chưa chuyển sang đang giao — có thể hủy và hoàn hàng về kho.
            </template>
          </p>

          <div class="hoa-don-actions__buttons">
            <button
              v-if="coTheXacNhanDon"
              type="button"
              class="soleil-btn-primary"
              :disabled="actionLoading || ghnLoading"
              @click="handleXacNhanDon"
            >
              <Icon icon="mdi:check-circle-outline" />
              {{ actionLoading ? 'Đang xử lý...' : 'Xác nhận đơn hàng' }}
            </button>

            <button
              v-if="coTheTuChoiDon"
              type="button"
              class="soleil-btn-outline hoa-don-actions__danger"
              :disabled="actionLoading"
              @click="handleTuChoiDon"
            >
              <Icon icon="mdi:close-circle-outline" />
              {{ actionLoading ? 'Đang xử lý...' : 'Từ chối đơn' }}
            </button>

            <button
              v-if="coTheHuyDonDaXacNhan"
              type="button"
              class="soleil-btn-outline hoa-don-actions__danger"
              :disabled="actionLoading"
              @click="handleHuyDonDaXacNhan"
            >
              <Icon icon="mdi:cancel" />
              {{ actionLoading ? 'Đang xử lý...' : 'Hủy đơn' }}
            </button>
          </div>

          <div
            v-if="actionMessage"
            class="admin-alert rounded-lg px-4 py-2 mt-3 text-sm"
            :class="actionMessageType === 'error' ? 'admin-alert-error' : 'admin-alert-success'"
          >
            {{ actionMessage }}
          </div>
        </section>

        <section v-if="coTheGiaLapWebhook" class="soleil-card hoa-don-webhook no-print">
          <div class="hoa-don-webhook__head">
            <h2 class="hoa-don-section-title" style="margin: 0">
              <Icon icon="mdi:webhook" width="18" class="hoa-don-webhook__title-icon" />
              Giả lập webhook cập nhật trạng thái
            </h2>
          </div>

          <p class="hoa-don-webhook__hint">
            Mô phỏng webhook GHN gửi về hệ thống: chọn <strong>trạng thái vận đơn GHN</strong>,
            hệ thống sẽ ánh xạ sang trạng thái đơn nội bộ (giống webhook thật).
            Chỉ dùng khi đơn đã có mã vận đơn GHN.
          </p>

          <div class="hoa-don-webhook__form">
            <div class="hoa-don-webhook__field">
              <span class="hoa-don-webhook__label">Trạng thái đơn hiện tại</span>
              <div class="hoa-don-webhook__select hoa-don-webhook__current" aria-readonly="true">
                {{ statusLabel(detail.trangThai) }}
              </div>
            </div>

            <div class="hoa-don-webhook__field">
              <label class="hoa-don-webhook__label" for="webhook-ghn-status">Trạng thái GHN (webhook)</label>
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
              <label class="hoa-don-webhook__label" for="webhook-ghi-chu">Ghi chú (tuỳ chọn)</label>
              <input
                id="webhook-ghi-chu"
                v-model="webhookGhiChu"
                type="text"
                class="hoa-don-webhook__input"
                placeholder="Ví dụ: Webhook test từ GHN"
              />
            </div>

            <button
              type="button"
              class="soleil-btn-primary hoa-don-webhook__submit"
              :disabled="!canCapNhatWebhook || webhookLoading"
              @click="handleCapNhatWebhook"
            >
              <Icon icon="icon-park-outline:refresh" />
              {{ webhookLoading ? 'Đang gửi webhook...' : 'Gửi webhook GHN giả lập' }}
            </button>
          </div>

          <div
            v-if="webhookMessage"
            class="admin-alert rounded-lg px-4 py-2 mt-3 text-sm"
            :class="webhookMessageType === 'error' ? 'admin-alert-error' : 'admin-alert-success'"
          >
            {{ webhookMessage }}
          </div>
        </section>

        <section class="soleil-card hoa-don-ghn no-print">
          <div class="hoa-don-ghn__head">
            <h2 class="hoa-don-section-title" style="margin: 0">
              <Icon icon="mdi:truck-delivery-outline" width="18" class="hoa-don-ghn__title-icon" />
              Vận chuyển (Giao Hàng Nhanh)
            </h2>
            <div class="hoa-don-ghn__actions">
              <button
                v-if="coTheTaoVanDon"
                type="button"
                class="soleil-btn-outline"
                :disabled="ghnLoading"
                @click="handleTaoVanDon"
              >
                <Icon icon="mdi:package-variant-closed-plus" />
                {{ ghnLoading ? 'Đang xử lý...' : 'Tạo vận đơn GHN' }}
              </button>
              <button
                v-if="detail.maVanDonGhn"
                type="button"
                class="soleil-btn-outline"
                :disabled="ghnLoading"
                @click="handleDongBoGhn"
              >
                <Icon icon="icon-park-outline:refresh" />
                {{ ghnLoading ? 'Đang xử lý...' : 'Đồng bộ trạng thái' }}
              </button>
            </div>
          </div>

          <div
            v-if="ghnMessage"
            class="admin-alert rounded-lg px-4 py-2 mt-3 text-sm"
            :class="ghnMessageType === 'error' ? 'admin-alert-error' : 'admin-alert-success'"
          >
            {{ ghnMessage }}
          </div>

          <p v-if="detail.maVanDonGhn" class="hoa-don-ghn__code">
            Mã vận đơn: <span class="soleil-sp-code">{{ detail.maVanDonGhn }}</span>
          </p>
          <p v-else class="hoa-don-ghn__hint">
            <template v-if="coTheTaoVanDon">
              Đơn chưa có vận đơn GHN. Bấm "Tạo vận đơn GHN" để thử lại (ví dụ khi tạo tự động lúc xác nhận thất bại).
            </template>
            <template v-else>
              Đơn ở trạng thái này không thể tạo vận đơn GHN.
            </template>
          </p>
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
              Mã GD hoàn: <span class="soleil-sp-code">{{ detail.maGiaoDichHoan }}</span>
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

.hoa-don-actions {
  margin-top: 1.5rem;
}
.hoa-don-actions__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
}
.hoa-don-actions__title-icon {
  vertical-align: -3px;
  margin-right: 0.35rem;
}
.hoa-don-actions__hint {
  margin-top: 0.85rem;
  font-size: 0.85rem;
  color: var(--admin-muted);
}
.hoa-don-actions__buttons {
  margin-top: 1rem;
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}
.hoa-don-actions__danger {
  border-color: rgba(180, 72, 72, 0.35);
  color: #a83a3a;
}
.hoa-don-actions__danger:hover {
  background: rgba(180, 72, 72, 0.06);
}

.hoa-don-webhook {
  margin-top: 1.5rem;
}
.hoa-don-webhook__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
}
.hoa-don-webhook__title-icon {
  vertical-align: -3px;
  margin-right: 0.35rem;
}
.hoa-don-webhook__hint {
  margin-top: 0.85rem;
  font-size: 0.85rem;
  color: var(--admin-muted);
}
.hoa-don-webhook__form {
  margin-top: 1rem;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(12rem, 1fr));
  gap: 1rem;
  align-items: end;
}
.hoa-don-webhook__field {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}
.hoa-don-webhook__field--wide {
  grid-column: 1 / -1;
}
.hoa-don-webhook__label {
  font-size: 0.72rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--admin-muted);
}
.hoa-don-webhook__select,
.hoa-don-webhook__input {
  width: 100%;
  padding: 0.55rem 0.75rem;
  border: 1px solid var(--admin-border, rgba(30, 21, 16, 0.12));
  border-radius: 0.5rem;
  background: #fff;
  font-size: 0.9rem;
  color: var(--ink);
}
.hoa-don-webhook__current {
  background: var(--cream, #faf6f0);
  cursor: default;
  user-select: none;
}
.hoa-don-webhook__submit {
  justify-self: start;
}

.hoa-don-ghn {
  margin-top: 1.5rem;
}
.hoa-don-ghn__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
}
.hoa-don-ghn__title-icon {
  vertical-align: -3px;
  margin-right: 0.35rem;
}
.hoa-don-ghn__actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}
.hoa-don-ghn__code {
  margin-top: 0.85rem;
  font-size: 0.9rem;
}
.hoa-don-ghn__hint {
  margin-top: 0.85rem;
  font-size: 0.85rem;
  color: var(--admin-muted);
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
