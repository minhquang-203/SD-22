<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import { getHoaDonAdminCounts, searchHoaDon } from '@/api/hoaDonApi'
import { useAdminBadges } from '@/composables/useAdminBadges'
import { subscribeAdminOrders } from '@/composables/useRealtime'
import { formatCurrency } from '@/utils/format'
import { orderStatusLabel } from '@/utils/orderStatus'

const router = useRouter()
const { refreshBadges } = useAdminBadges()
const loading = ref(false)
const message = ref('')
const messageType = ref('success')

let unsubscribeOrders = null
let searchTimer = null

const orders = ref([])
const keyword = ref('')
const filterLoaiDon = ref('')
const filterTrangThai = ref('')
const dateFrom = ref('')
const dateTo = ref('')

const page = ref(1)
const pageSize = ref(12)
const totalElements = ref(0)
const totalPages = ref(1)
const tabCounts = ref({ ALL: 0, CHO_XAC_NHAN: 0 })

const TAB_ALL = 'ALL'
const TAB_PENDING = 'CHO_XAC_NHAN'

const tabs = [
  { value: TAB_ALL, label: 'Tất cả' },
  { value: TAB_PENDING, label: 'Chờ xác nhận' },
]

const currentTab = ref(TAB_PENDING)

const LOAI_OPTIONS = [
  { value: '', label: 'Tất cả loại' },
  { value: 'TAI_QUAY', label: 'Tại quầy' },
  { value: 'ONLINE', label: 'Online' },
]

const TRANG_THAI_OPTIONS = [
  { value: '', label: 'Tất cả trạng thái' },
  { value: 'CHO_XAC_NHAN', label: 'Chờ xác nhận' },
  { value: 'DA_XAC_NHAN', label: 'Đã xác nhận' },
  { value: 'DANG_CHUAN_BI', label: 'Đang chuẩn bị' },
  { value: 'DANG_GIAO', label: 'Đang giao' },
  { value: 'HOAN_THANH', label: 'Hoàn thành' },
  { value: 'TRA_HANG', label: 'Trả hàng' },
  { value: 'DA_HUY', label: 'Đã hủy' },
  { value: 'CHO', label: 'Chờ tại quầy' },
]

function notify(text, type = 'success') {
  message.value = text
  messageType.value = type
  setTimeout(() => { message.value = '' }, 4000)
}

function statusLabel(trangThai) {
  if (trangThai === 'CHO') return 'Chờ tại quầy'
  if (trangThai === 'HOAN_THANH') return 'Hoàn thành'
  return orderStatusLabel(trangThai)
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

function resolveTrangThaiFilter() {
  if (currentTab.value === TAB_PENDING) return 'CHO_XAC_NHAN'
  return filterTrangThai.value || undefined
}

const pageTitle = computed(() =>
  currentTab.value === TAB_PENDING ? 'Đơn hàng chờ xác nhận' : 'Hóa đơn',
)

const listLabel = computed(() =>
  currentTab.value === TAB_PENDING ? 'đơn chờ xác nhận' : 'hóa đơn',
)

const pageDescription = computed(() =>
  `SUNOVA — ${totalElements.value} ${listLabel.value}`,
)

const tableTitle = computed(() =>
  currentTab.value === TAB_PENDING ? 'Đơn chờ xác nhận' : 'Danh sách hóa đơn',
)

const emptyMessage = computed(() =>
  currentTab.value === TAB_PENDING
    ? 'Không có đơn hàng chờ xác nhận'
    : 'Không có hóa đơn phù hợp',
)

/** Dãy trang: 1 … 4 5 6 … 20 */
const pageItems = computed(() => {
  const total = totalPages.value
  const current = page.value
  if (total <= 7) {
    return Array.from({ length: total }, (_, i) => i + 1)
  }

  const set = new Set([1, total, current, current - 1, current + 1, current - 2, current + 2])
  const nums = [...set].filter((n) => n >= 1 && n <= total).sort((a, b) => a - b)

  const items = []
  for (let i = 0; i < nums.length; i++) {
    if (i > 0 && nums[i] - nums[i - 1] > 1) {
      items.push('…')
    }
    items.push(nums[i])
  }
  return items
})

async function loadTabCounts() {
  try {
    const res = await getHoaDonAdminCounts()
    tabCounts.value = {
      ALL: Number(res.data?.all) || 0,
      CHO_XAC_NHAN: Number(res.data?.choXacNhan) || 0,
    }
  } catch {
    // im lặng — không chặn list
  }
}

async function loadOrders({ silent = false, resetPage = false } = {}) {
  if (resetPage) page.value = 1
  if (!silent) loading.value = true
  try {
    const res = await searchHoaDon({
      keyword: keyword.value.trim() || undefined,
      loaiDon: filterLoaiDon.value || undefined,
      trangThai: resolveTrangThaiFilter(),
      from: dateFrom.value || undefined,
      to: dateTo.value || undefined,
      page: page.value,
      size: pageSize.value,
    })
    const data = res.data || {}
    orders.value = data.content || []
    totalElements.value = Number(data.totalElements) || 0
    totalPages.value = Math.max(1, Number(data.totalPages) || 1)
    if (page.value > totalPages.value) {
      page.value = totalPages.value
    }
    await Promise.all([loadTabCounts(), refreshBadges()])
  } catch (err) {
    if (!silent) notify(String(err), 'error')
  } finally {
    if (!silent) loading.value = false
  }
}

function onOrderRealtime() {
  loadOrders({ silent: true })
}

function onOrderRealtimeWindow() {
  loadOrders({ silent: true })
}

function openDetail(order) {
  router.push(`/admin/hoa-don/chi-tiet/${order.id}`)
}

function customerDisplay(row) {
  return row.tenKhachHang || 'Khách lẻ'
}

function switchTab(tab) {
  currentTab.value = tab
  if (tab === TAB_PENDING) {
    filterTrangThai.value = ''
  }
}

function changePage(next) {
  if (next < 1 || next > totalPages.value || next === page.value) return
  page.value = next
  loadOrders()
}

watch([keyword, filterLoaiDon, filterTrangThai, dateFrom, dateTo, currentTab], () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    loadOrders({ resetPage: true })
  }, 300)
})

onMounted(() => {
  loadOrders()
  unsubscribeOrders = subscribeAdminOrders(onOrderRealtime)
  window.addEventListener('sunova-admin-order-realtime', onOrderRealtimeWindow)
})

onUnmounted(() => {
  clearTimeout(searchTimer)
  unsubscribeOrders?.()
  unsubscribeOrders = null
  window.removeEventListener('sunova-admin-order-realtime', onOrderRealtimeWindow)
})
</script>

<template>
  <div class="space-y-6 order-list-page">
    <PageHeader
      :title="pageTitle"
      :description="pageDescription"
    />

    <div class="order-tabs">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        type="button"
        class="order-tab-btn"
        :class="{ active: currentTab === tab.value }"
        @click="switchTab(tab.value)"
      >
        {{ tab.label }}
        <span class="order-tab-count">{{ tabCounts[tab.value] ?? 0 }}</span>
      </button>
    </div>

    <div
      v-if="message"
      class="admin-alert rounded-lg px-4 py-3 text-sm"
      :class="messageType === 'error' ? 'admin-alert-error' : 'admin-alert-success'"
    >
      {{ message }}
    </div>

    <div class="soleil-toolbar soleil-toolbar--filter">
      <div class="soleil-toolbar__field soleil-toolbar__field--wide">
        <label class="soleil-toolbar__label">Tìm kiếm</label>
        <div class="soleil-toolbar__search">
          <Icon icon="icon-park-outline:search" class="soleil-toolbar__search-icon" />
          <input
            v-model="keyword"
            class="soleil-toolbar__input"
            type="text"
            placeholder="Mã HĐ, tên khách, nhân viên..."
          />
        </div>
      </div>
      <div class="soleil-toolbar__field">
        <label class="soleil-toolbar__label">Loại đơn</label>
        <select v-model="filterLoaiDon" class="soleil-toolbar__select">
          <option v-for="opt in LOAI_OPTIONS" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </option>
        </select>
      </div>
      <div v-if="currentTab === TAB_ALL" class="soleil-toolbar__field">
        <label class="soleil-toolbar__label">Trạng thái</label>
        <select v-model="filterTrangThai" class="soleil-toolbar__select">
          <option v-for="opt in TRANG_THAI_OPTIONS" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </option>
        </select>
      </div>
      <div class="soleil-toolbar__field">
        <label class="soleil-toolbar__label">Từ ngày</label>
        <input v-model="dateFrom" type="date" class="soleil-toolbar__input" />
      </div>
      <div class="soleil-toolbar__field">
        <label class="soleil-toolbar__label">Đến ngày</label>
        <input v-model="dateTo" type="date" class="soleil-toolbar__input" />
      </div>
      <button type="button" class="soleil-btn-outline" style="align-self: flex-end" @click="loadOrders()">
        <Icon icon="icon-park-outline:refresh" />
        Tải lại
      </button>
    </div>

    <div class="soleil-table-card">
      <div class="soleil-table-card__head">
        <span class="soleil-label" style="margin: 0">{{ tableTitle }}</span>
        <span class="text-xs text-[rgba(30,21,16,0.45)]">Trang {{ page }} / {{ totalPages }}</span>
      </div>

      <div class="overflow-x-auto">
        <table class="soleil-table admin-table--soleil soleil-table--orders">
          <thead>
            <tr>
              <th class="soleil-col-text">Mã HĐ</th>
              <th class="soleil-col-center">Loại</th>
              <th class="soleil-col-text">Khách</th>
              <th class="soleil-col-text">Phương thức</th>
              <th class="soleil-col-num">Thành tiền</th>
              <th class="soleil-col-center">Trạng thái</th>
              <th class="soleil-col-text">Ngày</th>
              <th class="soleil-col-center">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="8" class="text-center py-10 text-[var(--admin-muted)]">
                Đang tải dữ liệu...
              </td>
            </tr>
            <tr v-else-if="orders.length === 0">
              <td colspan="8" class="text-center py-10 text-[var(--admin-muted)]">
                {{ emptyMessage }}
              </td>
            </tr>
            <tr v-for="item in orders" :key="item.id">
              <td class="soleil-col-text">
                <span class="soleil-sp-code">{{ item.maHoaDon }}</span>
              </td>
              <td class="soleil-col-center">
                <span
                  class="order-badge"
                  :class="`order-badge--${loaiDonTone(item.loaiDon)}`"
                >
                  {{ loaiDonLabel(item.loaiDon) }}
                </span>
              </td>
              <td class="soleil-col-text text-sm">{{ customerDisplay(item) }}</td>
              <td class="soleil-col-text text-sm">{{ item.tenPhuongThucThanhToan || '—' }}</td>
              <td class="soleil-col-num font-medium">{{ formatCurrency(item.thanhTien) }}</td>
              <td class="soleil-col-center">
                <span
                  class="order-badge"
                  :class="`order-badge--${statusTone(item.trangThai)}`"
                >
                  {{ statusLabel(item.trangThai) }}
                </span>
              </td>
              <td class="soleil-col-text text-sm text-[var(--admin-muted)]">
                {{ formatDateTime(item.ngayTao) }}
              </td>
              <td class="soleil-col-center">
                <button
                  type="button"
                  class="soleil-act-btn-round"
                  title="Xem chi tiết"
                  @click="openDetail(item)"
                >
                  <Icon icon="icon-park-outline:eyes" />
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="soleil-pagination">
        <span class="soleil-pagination__info">
          Hiển thị {{ orders.length }} / {{ totalElements }} {{ listLabel }}
        </span>
        <div class="soleil-pagination__btns">
          <button
            type="button"
            class="soleil-page-btn"
            title="Trang trước"
            :disabled="page <= 1 || loading"
            @click="changePage(page - 1)"
          >
            <Icon icon="icon-park-outline:left" width="14" />
          </button>
          <template v-for="(item, idx) in pageItems" :key="`${item}-${idx}`">
            <span v-if="item === '…'" class="soleil-page-ellipsis">…</span>
            <button
              v-else
              type="button"
              class="soleil-page-btn"
              :class="{ 'soleil-page-btn--active': item === page }"
              :disabled="loading"
              @click="changePage(item)"
            >
              {{ item }}
            </button>
          </template>
          <button
            type="button"
            class="soleil-page-btn"
            title="Trang sau"
            :disabled="page >= totalPages || loading"
            @click="changePage(page + 1)"
          >
            <Icon icon="icon-park-outline:right" width="14" />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.order-tabs {
  display: flex;
  gap: 2px;
  width: fit-content;
  padding: 3px;
  border-radius: 10px;
  background: rgba(30, 21, 16, 0.05);
}

.order-tab-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  padding: 0.45rem 0.9rem;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: rgba(30, 21, 16, 0.55);
  font-size: 0.8125rem;
  font-family: inherit;
  cursor: pointer;
  transition: background-color 0.15s, color 0.15s, box-shadow 0.15s;
}

.order-tab-btn:hover:not(.active) {
  color: rgba(30, 21, 16, 0.75);
}

.order-tab-btn.active {
  background: #fff;
  color: var(--bronze, #a67c3d);
  font-weight: 600;
  box-shadow: 0 1px 3px rgba(30, 21, 16, 0.08);
}

.order-tab-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 1.25rem;
  height: 1.25rem;
  padding: 0 0.35rem;
  border-radius: 999px;
  background: rgba(30, 21, 16, 0.08);
  font-size: 0.6875rem;
  font-weight: 600;
}

.order-tab-btn.active .order-tab-count {
  background: rgba(196, 149, 84, 0.18);
  color: var(--bronze, #a67c3d);
}

.order-badge {
  display: inline-block;
  padding: 0.2rem 0.55rem;
  border-radius: 999px;
  font-size: 0.7rem;
  font-weight: 600;
  letter-spacing: 0.02em;
  white-space: nowrap;
  transition: background-color 0.2s, color 0.2s;
}
.order-badge--gold {
  background: rgba(196, 149, 84, 0.15);
  color: var(--bronze, #a67c3d);
}
.order-badge--teal {
  background: rgba(72, 140, 130, 0.12);
  color: var(--sage, #488c82);
}
.order-badge--success {
  background: rgba(72, 140, 82, 0.12);
  color: #3d7a4a;
}
.order-badge--info {
  background: rgba(72, 120, 180, 0.12);
  color: #3a6ea8;
}
.order-badge--danger {
  background: rgba(180, 72, 72, 0.12);
  color: #a83a3a;
}
.order-badge--warning {
  background: rgba(196, 149, 84, 0.18);
  color: #8a6428;
}
.order-badge--neutral {
  background: rgba(30, 21, 16, 0.06);
  color: rgba(30, 21, 16, 0.55);
}

.soleil-page-ellipsis {
  min-width: 24px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: rgba(30, 21, 16, 0.4);
  font-size: 13px;
  user-select: none;
}
</style>
