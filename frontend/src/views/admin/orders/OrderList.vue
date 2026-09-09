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
  <div class="order-list-page">
    <div class="order-list-page__head">
      <PageHeader
        :title="pageTitle"
        :description="pageDescription"
      />
      <div class="order-stats">
        <div class="order-stat">
          <div class="order-stat__n">{{ tabCounts.ALL ?? 0 }}</div>
          <div class="order-stat__l">Tất cả</div>
        </div>
        <div class="order-stat order-stat--warn">
          <div class="order-stat__n">{{ tabCounts.CHO_XAC_NHAN ?? 0 }}</div>
          <div class="order-stat__l">Chờ xác nhận</div>
        </div>
      </div>
    </div>

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
      class="admin-alert px-4 py-3 text-sm"
      :class="messageType === 'error' ? 'admin-alert-error' : 'admin-alert-success'"
    >
      {{ message }}
    </div>

    <div class="soleil-toolbar soleil-toolbar--filter order-filters">
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
      <button type="button" class="soleil-btn-outline order-reload-btn" @click="loadOrders()">
        <Icon icon="icon-park-outline:refresh" />
        Tải lại
      </button>
    </div>

    <div class="soleil-table-card order-table-card">
      <div class="soleil-table-card__head">
        <span class="order-table-title">{{ tableTitle }}</span>
        <span class="order-table-meta">Trang {{ page }} / {{ totalPages }}</span>
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
              <td colspan="8" class="text-center py-10 text-[#5a6a72]">
                Đang tải dữ liệu...
              </td>
            </tr>
            <tr v-else-if="orders.length === 0">
              <td colspan="8" class="text-center py-10 text-[#5a6a72]">
                {{ emptyMessage }}
              </td>
            </tr>
            <template v-else>
              <tr
                v-for="item in orders"
                :key="item.id"
                :class="{ 'order-row--pending': item.trangThai === 'CHO_XAC_NHAN' }"
              >
                <td class="soleil-col-text">
                  <button type="button" class="order-code" @click="openDetail(item)">
                    {{ item.maHoaDon }}
                  </button>
                </td>
                <td class="soleil-col-center">
                  <span
                    class="order-badge"
                    :class="`order-badge--${loaiDonTone(item.loaiDon)}`"
                  >
                    {{ loaiDonLabel(item.loaiDon) }}
                  </span>
                </td>
                <td class="soleil-col-text">
                  <span class="order-customer">{{ customerDisplay(item) }}</span>
                </td>
                <td class="soleil-col-text order-pay">{{ item.tenPhuongThucThanhToan || '—' }}</td>
                <td class="soleil-col-num">
                  <span class="order-money">{{ formatCurrency(item.thanhTien) }}</span>
                </td>
                <td class="soleil-col-center">
                  <span
                    class="order-badge"
                    :class="`order-badge--${statusTone(item.trangThai)}`"
                  >
                    {{ statusLabel(item.trangThai) }}
                  </span>
                </td>
                <td class="soleil-col-text">
                  <span class="order-date">{{ formatDateTime(item.ngayTao) }}</span>
                </td>
                <td class="soleil-col-center">
                  <button
                    type="button"
                    class="order-act-btn"
                    title="Xem chi tiết"
                    @click="openDetail(item)"
                  >
                    <Icon icon="icon-park-outline:eyes" />
                  </button>
                </td>
              </tr>
              <tr
                v-for="n in Math.max(0, 5 - orders.length)"
                :key="`pad-${n}`"
                class="table-pad-row"
                aria-hidden="true"
              >
                <td colspan="8" />
              </tr>
            </template>
          </tbody>
        </table>
      </div>

      <div class="soleil-pagination order-pager">
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
/* Token copy 1:1 từ docs/sunova-hoadon-list-concept.html (bản lúc tạo) */
.order-list-page {
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
  --hd-tab-active: #b8976a;
  --hd-cream-ink: #fffef9;
  --hd-radius: 2px;

  display: flex;
  flex-direction: column;
  gap: 0.85rem;
  color: var(--hd-ink);
}

.order-list-page__head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
}

.order-list-page__head :deep(.soleil-page-header) {
  margin: 0;
  margin-bottom: 0;
  flex: 1;
  min-width: 0;
}

.order-list-page__head :deep(.soleil-page-header__title) {
  font-family: inherit;
  font-size: 1.35rem;
  font-weight: 800;
  font-style: normal;
  letter-spacing: 0.02em;
  color: var(--hd-ink);
}

.order-list-page__head :deep(.soleil-page-header__desc) {
  margin: 0.25rem 0 0;
  font-size: 12.5px;
  font-weight: 500;
  color: var(--hd-muted);
}

.order-stats {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.order-stat {
  min-width: 88px;
  padding: 0.45rem 0.7rem;
  background: var(--hd-surface);
  border: 1px solid var(--hd-line);
  border-left: 3px solid var(--hd-accent);
}

.order-stat--warn {
  border-left-color: var(--hd-warn);
}

.order-stat__n {
  font-family: ui-monospace, "Cascadia Mono", "Segoe UI Mono", monospace;
  font-size: 1.15rem;
  font-weight: 700;
  line-height: 1;
  color: var(--hd-ink);
}

.order-stat__l {
  margin-top: 0.2rem;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--hd-muted);
}

.order-tabs {
  display: flex;
  width: fit-content;
  border: 1px solid var(--hd-line-strong);
  background: var(--hd-surface);
}

.order-tab-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  min-height: 36px;
  padding: 0 0.95rem;
  border: none;
  border-right: 1px solid var(--hd-line-strong);
  border-radius: 0;
  background: transparent;
  color: var(--hd-muted);
  font-size: 13px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: background-color 0.15s, color 0.15s;
}

.order-tab-btn:last-child {
  border-right: 0;
}

.order-tab-btn:hover:not(.active) {
  color: var(--hd-ink);
  background: #f5f8fa;
}

.order-tab-btn.active {
  background: var(--hd-tab-active);
  color: var(--hd-cream-ink);
  font-weight: 800;
  box-shadow: none;
}

.order-tab-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 1.35rem;
  height: 1.25rem;
  padding: 0 0.35rem;
  border-radius: var(--hd-radius);
  background: rgba(15, 26, 28, 0.08);
  font-family: ui-monospace, "Cascadia Mono", "Segoe UI Mono", monospace;
  font-size: 11px;
  font-weight: 700;
}

.order-tab-btn.active .order-tab-count {
  background: rgba(255, 254, 249, 0.22);
  color: var(--hd-cream-ink);
}

.order-filters {
  border-radius: var(--hd-radius) !important;
  background: var(--hd-surface) !important;
  border-color: var(--hd-line) !important;
  padding: 0.85rem 1rem;
}

.order-filters :deep(.soleil-toolbar__label) {
  font-size: 10.5px;
  font-weight: 800;
  letter-spacing: 0.08em;
  color: var(--hd-ink);
}

.order-filters :deep(.soleil-toolbar__input),
.order-filters :deep(.soleil-toolbar__select) {
  min-height: 36px;
  border-radius: var(--hd-radius) !important;
  border-color: var(--hd-line-strong) !important;
  background: #fff !important;
  color: var(--hd-ink);
  font-weight: 600;
}

.order-filters :deep(.soleil-toolbar__input:focus),
.order-filters :deep(.soleil-toolbar__select:focus) {
  outline: 2px solid rgba(11, 110, 117, 0.25);
  border-color: var(--hd-accent) !important;
}

.order-filters :deep(.soleil-toolbar__search-icon) {
  color: var(--hd-muted);
}

.order-reload-btn {
  align-self: flex-end;
  border-radius: var(--hd-radius) !important;
  font-weight: 700 !important;
  background: var(--hd-surface) !important;
  border-color: var(--hd-line-strong) !important;
  color: var(--hd-ink) !important;
}

.order-reload-btn:hover {
  border-color: var(--hd-ink) !important;
  color: var(--hd-ink) !important;
  background: var(--hd-surface) !important;
}

.order-table-card {
  border: 1.5px solid var(--hd-ink) !important;
  border-radius: var(--hd-radius) !important;
  border-color: var(--hd-ink) !important;
  background: var(--hd-surface) !important;
  overflow: hidden;
}

.order-table-card {
  --tbl-row-h: 52px;
}

.order-table-card :deep(.overflow-x-auto) {
  min-height: calc(var(--tbl-row-h) * 5 + 40px);
  max-height: calc(var(--tbl-row-h) * 12 + 40px);
  overflow: auto;
}

.order-table-card :deep(table.soleil-table--orders) {
  width: 100%;
  min-width: 880px;
}

.order-table-card :deep(tr.table-pad-row td) {
  height: var(--tbl-row-h);
  padding: 0 !important;
  border-bottom: 1px solid var(--hd-line);
  vertical-align: middle;
  pointer-events: none;
}

.order-table-card :deep(tr.table-pad-row:hover) {
  background: transparent !important;
}

.order-table-card :deep(.soleil-table-card__head) {
  background: #f7fafb;
  border-bottom-color: var(--hd-line);
  padding: 0.7rem 1rem;
}

.order-table-title {
  margin: 0;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--hd-ink);
}

.order-table-meta {
  margin-left: auto;
  font-size: 12px;
  color: var(--hd-muted);
  font-variant-numeric: tabular-nums;
}

.order-table-card :deep(table.admin-table--soleil thead th),
.order-table-card :deep(thead th) {
  background: #b8976a !important;
  color: #fffef9 !important;
  font-size: 11px !important;
  font-weight: 700 !important;
  letter-spacing: 0.08em !important;
  padding: 0.6rem 0.75rem !important;
  border-bottom: none !important;
}

.order-table-card :deep(thead th:first-child),
.order-table-card :deep(tbody td:first-child) {
  padding-left: 0.85rem !important;
}

.order-table-card :deep(tbody td) {
  padding: 0.72rem 0.75rem !important;
  border-bottom: 1px solid var(--hd-line);
  vertical-align: middle;
  color: var(--hd-ink);
}

.order-table-card :deep(tbody tr) {
  background: #fff;
  border-bottom: none;
}

.order-table-card :deep(tbody tr:hover) {
  background: #f3f8f8 !important;
}

.order-table-card :deep(tbody tr.order-row--pending) {
  box-shadow: inset 3px 0 0 var(--hd-warn);
}

.order-code {
  font-family: ui-monospace, "Cascadia Mono", "Segoe UI Mono", monospace;
  font-size: 12.5px;
  font-weight: 700;
  color: var(--hd-accent-deep);
  letter-spacing: 0.01em;
  background: none;
  border: 0;
  padding: 0;
  cursor: pointer;
}

.order-code:hover {
  text-decoration: underline;
  text-underline-offset: 2px;
}

.order-customer {
  font-weight: 600;
  font-size: 13px;
  color: var(--hd-ink);
}

.order-pay {
  font-size: 12.5px;
  font-weight: 500;
  color: var(--hd-ink);
}

.order-money {
  font-family: ui-monospace, "Cascadia Mono", "Segoe UI Mono", monospace;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  font-size: 12.5px;
  color: var(--hd-ink);
}

.order-date {
  font-variant-numeric: tabular-nums;
  color: var(--hd-muted);
  font-size: 12.5px;
  white-space: nowrap;
}

.order-badge {
  display: inline-flex;
  align-items: center;
  min-height: 22px;
  padding: 0 0.45rem;
  border-radius: var(--hd-radius);
  border: 1px solid currentColor;
  font-size: 10.5px;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  white-space: nowrap;
}

/* tag--pos */
.order-badge--gold {
  color: #7c4a12;
  background: #fff4e5;
  border-color: #d4a574;
}

/* tag--online */
.order-badge--teal {
  color: var(--hd-accent-deep);
  background: var(--hd-accent-soft);
}

.order-badge--success {
  color: var(--hd-ok);
  background: var(--hd-ok-bg);
}

.order-badge--info {
  color: var(--hd-info);
  background: var(--hd-info-bg);
}

.order-badge--danger {
  color: var(--hd-danger);
  background: var(--hd-danger-bg);
}

.order-badge--warning {
  color: var(--hd-warn);
  background: var(--hd-warn-bg);
}

.order-badge--neutral {
  color: #4b5563;
  background: #f1f3f5;
  border-color: #c5ccd3;
}

.order-act-btn {
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--hd-line-strong);
  border-radius: var(--hd-radius);
  background: #fff;
  color: var(--hd-ink);
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s, color 0.15s;
}

.order-act-btn:hover {
  border-color: var(--hd-accent);
  color: var(--hd-accent-deep);
  background: var(--hd-accent-soft);
}

.order-pager {
  background: #f7fafb !important;
  border-top: 1px solid var(--hd-line);
}

.order-pager :deep(.soleil-pagination__info) {
  font-size: 12.5px;
  color: var(--hd-muted);
}

.order-pager :deep(.soleil-page-btn) {
  min-width: 32px;
  height: 32px;
  padding: 0 0.4rem;
  border: 1px solid var(--hd-line-strong) !important;
  background: #fff !important;
  border-radius: var(--hd-radius) !important;
  font-size: 12.5px;
  font-weight: 700;
  color: var(--hd-ink) !important;
}

.order-pager :deep(.soleil-page-btn:hover:not(:disabled):not(.soleil-page-btn--active)) {
  border-color: #0b6e75 !important;
  color: #06484e !important;
  background: #fff !important;
}

.order-pager :deep(.soleil-page-btn--active),
.order-pager :deep(.soleil-page-btn.soleil-page-btn--active) {
  background: #06484e !important;
  border-color: #06484e !important;
  color: #fff !important;
}

.order-pager :deep(.soleil-page-btn:disabled) {
  opacity: 0.4;
  cursor: not-allowed;
}

.soleil-page-ellipsis {
  min-width: 24px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--hd-muted);
  font-size: 13px;
  user-select: none;
}
</style>
