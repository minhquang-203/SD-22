<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import { getAllHoaDon } from '@/api/hoaDonApi'
import { useAdminBadges } from '@/composables/useAdminBadges'
import { formatCurrency } from '@/utils/format'
import { orderStatusLabel } from '@/utils/orderStatus'

const router = useRouter()
const { refreshBadges } = useAdminBadges()
const loading = ref(false)
const message = ref('')
const messageType = ref('success')

const allOrders = ref([])
const keyword = ref('')
const filterLoaiDon = ref('')
const filterTrangThai = ref('')
const dateFrom = ref('')
const dateTo = ref('')

const page = ref(1)
const pageSize = ref(12)

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

function matchesDate(ngayTao) {
  if (!ngayTao) return true
  const d = new Date(ngayTao)
  if (dateFrom.value) {
    const from = new Date(dateFrom.value)
    from.setHours(0, 0, 0, 0)
    if (d < from) return false
  }
  if (dateTo.value) {
    const to = new Date(dateTo.value)
    to.setHours(23, 59, 59, 999)
    if (d > to) return false
  }
  return true
}

function matchesTab(trangThai) {
  if (currentTab.value === TAB_PENDING) {
    return trangThai === 'CHO_XAC_NHAN'
  }
  return true
}

function matchesTrangThai(trangThai) {
  if (currentTab.value === TAB_PENDING) return true
  if (!filterTrangThai.value) return true
  return trangThai === filterTrangThai.value
}

const tabCounts = computed(() => ({
  [TAB_ALL]: allOrders.value.length,
  [TAB_PENDING]: allOrders.value.filter((o) => o.trangThai === 'CHO_XAC_NHAN').length,
}))

const pageTitle = computed(() =>
  currentTab.value === TAB_PENDING ? 'Đơn hàng chờ xác nhận' : 'Hóa đơn',
)

const listLabel = computed(() =>
  currentTab.value === TAB_PENDING ? 'đơn chờ xác nhận' : 'hóa đơn',
)

const pageDescription = computed(() =>
  `SUNOVA — ${filteredOrders.value.length} ${listLabel.value}`,
)

const tableTitle = computed(() =>
  currentTab.value === TAB_PENDING ? 'Đơn chờ xác nhận' : 'Danh sách hóa đơn',
)

const emptyMessage = computed(() =>
  currentTab.value === TAB_PENDING
    ? 'Không có đơn hàng chờ xác nhận'
    : 'Không có hóa đơn phù hợp',
)

const filteredOrders = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  return allOrders.value.filter((o) => {
    if (!matchesTab(o.trangThai)) return false
    if (filterLoaiDon.value && o.loaiDon !== filterLoaiDon.value) return false
    if (!matchesTrangThai(o.trangThai)) return false
    if (!matchesDate(o.ngayTao)) return false
    if (!kw) return true
    const haystack = [
      o.maHoaDon,
      o.tenKhachHang,
      o.tenNhanVien,
      o.tenPhuongThucThanhToan,
    ]
      .filter(Boolean)
      .join(' ')
      .toLowerCase()
    return haystack.includes(kw)
  })
})

const totalPages = computed(() =>
  Math.max(1, Math.ceil(filteredOrders.value.length / pageSize.value)),
)

const pagedOrders = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredOrders.value.slice(start, start + pageSize.value)
})

async function loadOrders() {
  loading.value = true
  try {
    const res = await getAllHoaDon()
    allOrders.value = res.data || []
    page.value = 1
    await refreshBadges()
  } catch (err) {
    notify(String(err), 'error')
  } finally {
    loading.value = false
  }
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

watch([keyword, filterLoaiDon, filterTrangThai, dateFrom, dateTo, currentTab], () => {
  page.value = 1
})

watch(filteredOrders, () => {
  if (page.value > totalPages.value) {
    page.value = totalPages.value
  }
})

onMounted(() => loadOrders())
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
      <button type="button" class="soleil-btn-outline" style="align-self: flex-end" @click="loadOrders">
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
            <tr v-else-if="pagedOrders.length === 0">
              <td colspan="8" class="text-center py-10 text-[var(--admin-muted)]">
                {{ emptyMessage }}
              </td>
            </tr>
            <tr v-for="item in pagedOrders" :key="item.id">
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
          Hiển thị {{ pagedOrders.length }} / {{ filteredOrders.length }} {{ listLabel }}
        </span>
        <div class="soleil-pagination__btns">
          <button type="button" class="soleil-page-btn" :disabled="page <= 1" @click="page--">
            Trước
          </button>
          <button
            type="button"
            class="soleil-page-btn"
            :disabled="page >= totalPages"
            @click="page++"
          >
            Sau
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
</style>
