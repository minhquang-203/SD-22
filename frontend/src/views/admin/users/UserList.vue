<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatusDot from '@/components/ui/StatusDot.vue'
import {
  getCustomers,
  searchCustomers,
  getCustomerDetail,
  updateCustomerStatus,
} from '@/api/khachHangApi'
import {
  getVoucherById,
  searchVoucher,
  assignVoucherToCustomers,
} from '@/api/voucherApi'
import { formatDate } from '@/utils/format'
import { confirm } from '@/composables/useConfirm'
import { toast } from '@/composables/useToast'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const detailLoading = ref(false)
const assigning = ref(false)
const message = ref('')
const messageType = ref('success')

const keyword = ref('')
const filterDiemTu = ref('')
const filterDiemDen = ref('')
const filterKhachMoi = ref('') // '' | '7' | '30' | '90'

const allCustomers = ref([])
const page = ref(1)
const pageSize = ref(10)

const showDetail = ref(false)
const detail = ref(null)

const selectedVoucherId = ref(route.query.voucherId ? String(route.query.voucherId) : '')
const selectedVoucher = ref(null)
const personalVouchers = ref([])
const selectedIds = ref([])

let searchTimer = null

const filteredCustomers = computed(() => {
  let list = allCustomers.value

  const diemTuRaw = filterDiemTu.value === '' || filterDiemTu.value == null
    ? null
    : Number(filterDiemTu.value)
  const diemDenRaw = filterDiemDen.value === '' || filterDiemDen.value == null
    ? null
    : Number(filterDiemDen.value)
  const diemTu = Number.isFinite(diemTuRaw) ? diemTuRaw : null
  const diemDen = Number.isFinite(diemDenRaw) ? diemDenRaw : null

  if (diemTu != null || diemDen != null) {
    list = list.filter((c) => {
      const diem = Number(c.diemTichLuy) || 0
      if (diemTu != null && diem < diemTu) return false
      if (diemDen != null && diem > diemDen) return false
      return true
    })
  }

  const days = Number(filterKhachMoi.value)
  if (Number.isFinite(days) && days > 0) {
    const from = Date.now() - days * 24 * 60 * 60 * 1000
    list = list.filter((c) => {
      if (!c.ngayTao) return false
      return new Date(c.ngayTao).getTime() >= from
    })
  }

  return list
})

const totalPages = computed(() =>
  Math.max(1, Math.ceil(filteredCustomers.value.length / pageSize.value)),
)

const pagedCustomers = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredCustomers.value.slice(start, start + pageSize.value)
})

const assignableIds = computed(() =>
  filteredCustomers.value
    .filter((c) => c.trangThai !== false && customerMatchesVoucherPoints(c))
    .map((c) => c.id),
)

const voucherHasPointRange = computed(() => {
  const v = selectedVoucher.value
  if (!v) return false
  return v.diemToiThieu != null || v.diemToiDa != null
})

const voucherPointRangeLabel = computed(() => {
  if (!voucherHasPointRange.value) return ''
  const tu = selectedVoucher.value?.diemToiThieu
  const den = selectedVoucher.value?.diemToiDa
  if (tu != null && den != null) return `${tu} – ${den} điểm`
  if (tu != null) return `từ ${tu} điểm`
  return `tối đa ${den} điểm`
})

function customerMatchesVoucherPoints(customer) {
  if (!voucherHasPointRange.value) return true
  const diem = Number(customer?.diemTichLuy) || 0
  const tu = selectedVoucher.value?.diemToiThieu
  const den = selectedVoucher.value?.diemToiDa
  if (tu != null && diem < Number(tu)) return false
  if (den != null && diem > Number(den)) return false
  return true
}

function applyVoucherPointFilters() {
  const v = selectedVoucher.value
  if (!v || (v.diemToiThieu == null && v.diemToiDa == null)) return
  filterDiemTu.value = v.diemToiThieu != null ? String(v.diemToiThieu) : ''
  filterDiemDen.value = v.diemToiDa != null ? String(v.diemToiDa) : ''
}

const selectedAssignableIds = computed(() =>
  selectedIds.value.filter((id) => assignableIds.value.includes(id)),
)

const allFilteredSelected = computed(() =>
  assignableIds.value.length > 0
    && assignableIds.value.every((id) => selectedIds.value.includes(id)),
)

function toggleSelect(id) {
  const customer = allCustomers.value.find((c) => c.id === id)
  if (customer && !customerMatchesVoucherPoints(customer)) {
    toast('Khách này không nằm trong khoảng điểm của voucher', 'warn')
    return
  }
  if (selectedIds.value.includes(id)) {
    selectedIds.value = selectedIds.value.filter((x) => x !== id)
  } else {
    selectedIds.value = [...selectedIds.value, id]
  }
}

function toggleSelectAllFiltered() {
  if (allFilteredSelected.value) {
    selectedIds.value = selectedIds.value.filter((id) => !assignableIds.value.includes(id))
  } else {
    const set = new Set([...selectedIds.value, ...assignableIds.value])
    selectedIds.value = [...set]
  }
}

function isSelected(id) {
  return selectedIds.value.includes(id)
}

function notify(text, type = 'success') {
  message.value = text
  messageType.value = type
  setTimeout(() => { message.value = '' }, 3000)
}

async function loadCustomers() {
  loading.value = true
  try {
    const kw = keyword.value.trim()
    const res = kw ? await searchCustomers(kw) : await getCustomers()
    allCustomers.value = res.data || []
    page.value = 1
  } catch (err) {
    notify(String(err), 'error')
  } finally {
    loading.value = false
  }
}

async function loadPersonalVouchers() {
  try {
    const res = await searchVoucher(null, null, null, 1, 100)
    personalVouchers.value = (res.data?.content || []).filter((v) => v.phamVi === 'CA_NHAN')
  } catch {
    personalVouchers.value = []
  }
}

async function loadSelectedVoucher() {
  if (!selectedVoucherId.value) {
    selectedVoucher.value = null
    return
  }
  try {
    const res = await getVoucherById(selectedVoucherId.value)
    selectedVoucher.value = res.data
    if (res.data?.phamVi && res.data.phamVi !== 'CA_NHAN') {
      toast('Chỉ gán được voucher phạm vi Cá nhân', 'warn')
    }
    applyVoucherPointFilters()
  } catch {
    selectedVoucher.value = null
    toast('Không tải được thông tin voucher', 'warn')
  }
}

async function openDetail(customer) {
  detailLoading.value = true
  showDetail.value = true
  detail.value = null
  try {
    const res = await getCustomerDetail(customer.id)
    detail.value = res.data
  } catch (err) {
    showDetail.value = false
    notify(String(err), 'error')
  } finally {
    detailLoading.value = false
  }
}

function closeDetail() {
  showDetail.value = false
  detail.value = null
}

async function handleToggleStatus(customer) {
  const isActive = customer.trangThai !== false
  const action = isActive ? 'khóa' : 'mở khóa'
  const ok = await confirm({
    title: isActive ? 'Khóa khách hàng' : 'Mở khóa khách hàng',
    message: `Bạn có chắc muốn ${action} khách hàng "${customer.hoTen}"?`,
    confirmText: action === 'khóa' ? 'Khóa' : 'Mở khóa',
    danger: isActive,
  })
  if (!ok) return
  try {
    await updateCustomerStatus(customer.id, !isActive)
    notify(isActive ? 'Đã khóa khách hàng' : 'Đã mở khóa khách hàng')
    await loadCustomers()
  } catch (err) {
    notify(String(err), 'error')
  }
}

function formatAddress(dc) {
  const parts = [dc.diaChiChiTiet, dc.phuongXa, dc.tinhThanh].filter(Boolean)
  return parts.join(', ') || '—'
}

function clearAssignContext() {
  selectedVoucherId.value = ''
  selectedVoucher.value = null
  selectedIds.value = []
  router.replace({ name: 'AdminUsers', query: {} })
}

async function handleAssignVoucher() {
  if (!selectedVoucherId.value) {
    toast('Vui lòng chọn voucher cá nhân để gán', 'warn')
    return
  }
  if (selectedVoucher.value?.phamVi && selectedVoucher.value.phamVi !== 'CA_NHAN') {
    toast('Chỉ gán được voucher phạm vi Cá nhân', 'warn')
    return
  }
  const ids = selectedAssignableIds.value
  if (ids.length === 0) {
    toast('Hãy chọn ít nhất 1 khách hàng trong bảng', 'warn')
    return
  }

  const ma = selectedVoucher.value?.ma || selectedVoucherId.value
  const ok = await confirm({
    title: 'Gán voucher',
    message: ids.length === 1
      ? `Gán voucher "${ma}" cho 1 khách hàng đã chọn?`
      : `Gán voucher "${ma}" cho ${ids.length} khách hàng đã chọn?`,
    confirmText: 'Gán voucher',
  })
  if (!ok) return

  assigning.value = true
  try {
    const res = await assignVoucherToCustomers(Number(selectedVoucherId.value), ids)
    const added = res.data?.soKhachGanMoi ?? 0
    toast(`Đã gán cho ${added} khách mới`, 'info')
    notify(`Đã gán voucher "${ma}" cho ${added} khách mới`)
    selectedIds.value = []
  } catch (err) {
    toast(typeof err === 'string' ? err : 'Gán voucher thất bại', 'warn')
  } finally {
    assigning.value = false
  }
}

watch([filterDiemTu, filterDiemDen, filterKhachMoi], () => {
  page.value = 1
  selectedIds.value = []
})

watch(keyword, () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    selectedIds.value = []
    loadCustomers()
  }, 400)
})

watch(filteredCustomers, () => {
  if (page.value > totalPages.value) {
    page.value = totalPages.value
  }
})

watch(selectedVoucherId, async (id) => {
  selectedIds.value = []
  await loadSelectedVoucher()
  const current = route.query.voucherId ? String(route.query.voucherId) : ''
  if ((id || '') === current) return
  router.replace({
    name: 'AdminUsers',
    query: id
      ? { voucherId: String(id), voucherMa: selectedVoucher.value?.ma || '' }
      : {},
  })
})

watch(
  () => route.query.voucherId,
  (id) => {
    const next = id ? String(id) : ''
    if (next !== selectedVoucherId.value) {
      selectedVoucherId.value = next
    }
  },
)

onMounted(async () => {
  await Promise.all([loadCustomers(), loadPersonalVouchers()])
  if (selectedVoucherId.value) await loadSelectedVoucher()
})
</script>

<template>
  <div class="users-page">
    <div class="users-page__head">
      <PageHeader
        title="Quản lý khách hàng"
        :description="`SUNOVA — ${filteredCustomers.length} khách hàng`"
      />
    </div>

    <div
      v-if="message"
      class="admin-alert px-4 py-3 text-sm"
      :class="messageType === 'error' ? 'admin-alert-error' : 'admin-alert-success'"
    >
      {{ message }}
    </div>

    <div v-if="selectedVoucherId" class="users-assign-card">
      <div class="users-assign-card__body">
        <div class="min-w-0">
          <div class="users-assign-card__label">Đang gán voucher</div>
          <div class="users-assign-card__title">
            <span class="users-mono">{{ selectedVoucher?.ma || route.query.voucherMa || selectedVoucherId }}</span>
            <span v-if="selectedVoucher?.ten" class="users-assign-card__sub">
              — {{ selectedVoucher.ten }}
            </span>
          </div>
          <div class="users-assign-card__meta">
            Đã chọn {{ selectedAssignableIds.length }} / {{ assignableIds.length }} khách hàng khả dụng
            <template v-if="voucherHasPointRange">
              · Auto theo điểm {{ voucherPointRangeLabel }} — vẫn có thể tick thủ công nếu điểm khớp
            </template>
            <template v-else>
              · Chưa cấu hình điểm — gán thủ công
            </template>
          </div>
        </div>
        <div class="users-assign-card__actions">
          <button
            type="button"
            class="soleil-btn-outline"
            :disabled="assigning"
            @click="clearAssignContext"
          >
            Hủy
          </button>
          <button
            type="button"
            class="soleil-btn-primary"
            :disabled="assigning || selectedAssignableIds.length === 0"
            @click="handleAssignVoucher"
          >
            {{ assigning ? 'Đang gán…' : `Gán voucher (${selectedAssignableIds.length})` }}
          </button>
        </div>
      </div>
    </div>

    <div class="soleil-toolbar soleil-toolbar--filter users-toolbar">
      <div class="soleil-toolbar__field soleil-toolbar__field--wide users-field-search">
        <label class="soleil-toolbar__label">Tìm kiếm</label>
        <div class="soleil-toolbar__search">
          <Icon icon="icon-park-outline:search" class="soleil-toolbar__search-icon" />
          <input
            v-model="keyword"
            class="soleil-toolbar__input"
            type="text"
            placeholder="Họ tên, email, số điện thoại…"
          />
        </div>
      </div>

      <div class="soleil-toolbar__field users-field-compact users-field-compact--sm">
        <label class="soleil-toolbar__label">Điểm từ</label>
        <input
          v-model="filterDiemTu"
          class="soleil-toolbar__input users-input-plain"
          type="number"
          min="0"
          step="1"
          placeholder="0"
        />
      </div>

      <div class="soleil-toolbar__field users-field-compact users-field-compact--sm">
        <label class="soleil-toolbar__label">Đến</label>
        <input
          v-model="filterDiemDen"
          class="soleil-toolbar__input users-input-plain"
          type="number"
          min="0"
          step="1"
          placeholder="Max"
        />
      </div>

      <div class="soleil-toolbar__field users-field-compact users-field-compact--md">
        <label class="soleil-toolbar__label">Khách mới</label>
        <select v-model="filterKhachMoi" class="soleil-toolbar__input users-input-plain">
          <option value="">Tất cả</option>
          <option value="7">7 ngày</option>
          <option value="30">30 ngày</option>
          <option value="90">90 ngày</option>
        </select>
      </div>

      <div class="soleil-toolbar__field users-field-compact users-field-compact--lg">
        <label class="soleil-toolbar__label">Voucher</label>
        <select v-model="selectedVoucherId" class="soleil-toolbar__input users-input-plain">
          <option value="">Chọn gán</option>
          <option
            v-for="v in personalVouchers"
            :key="v.id"
            :value="String(v.id)"
          >
            {{ v.ma }} — {{ v.ten || 'Không tên' }}
          </option>
        </select>
      </div>

      <button type="button" class="soleil-btn-outline users-reload" @click="loadCustomers">
        <Icon icon="icon-park-outline:refresh" width="15" />
        Tải lại
      </button>
    </div>

    <div class="soleil-table-card">
      <div class="soleil-table-card__head">
        <span class="users-table-title">Danh sách khách hàng</span>
        <div class="users-table-head-meta">
          <button
            v-if="selectedVoucherId"
            type="button"
            class="soleil-btn-outline users-select-all"
            :disabled="assignableIds.length === 0"
            @click="toggleSelectAllFiltered"
          >
            {{ allFilteredSelected ? 'Bỏ chọn tất cả' : 'Chọn tất cả' }}
          </button>
          <span v-if="selectedVoucherId" class="users-avail-pill">
            Khách hàng khả dụng ({{ assignableIds.length }})
          </span>
          <span class="users-table-meta">Trang {{ page }} / {{ totalPages }}</span>
        </div>
      </div>

      <div class="overflow-x-auto">
        <table class="soleil-table admin-table--soleil soleil-table--customers">
          <thead>
            <tr>
              <th v-if="selectedVoucherId" class="soleil-col-center users-check-col">
                <input
                  type="checkbox"
                  class="users-checkbox"
                  :checked="allFilteredSelected"
                  :disabled="assignableIds.length === 0"
                  @change="toggleSelectAllFiltered"
                />
              </th>
              <th class="soleil-col-num">STT</th>
              <th class="soleil-col-text">Mã KH</th>
              <th class="soleil-col-text">Họ tên</th>
              <th class="soleil-col-text">Email</th>
              <th class="soleil-col-text">SĐT</th>
              <th class="soleil-col-text">Điểm tích lũy</th>
              <th class="soleil-col-center">Ngày tạo</th>
              <th class="soleil-col-center">Trạng thái</th>
              <th class="soleil-col-center">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td :colspan="selectedVoucherId ? 10 : 9" class="users-empty-cell">
                Đang tải dữ liệu…
              </td>
            </tr>
            <tr v-else-if="pagedCustomers.length === 0">
              <td :colspan="selectedVoucherId ? 10 : 9" class="users-empty-cell">
                Không có khách hàng phù hợp
              </td>
            </tr>
            <tr v-for="(item, index) in pagedCustomers" :key="item.id">
              <td v-if="selectedVoucherId" class="soleil-col-center">
                <input
                  v-if="item.trangThai !== false && customerMatchesVoucherPoints(item)"
                  type="checkbox"
                  class="users-checkbox"
                  :checked="isSelected(item.id)"
                  @change="toggleSelect(item.id)"
                />
                <span
                  v-else-if="item.trangThai !== false && voucherHasPointRange"
                  class="users-points-miss"
                  title="Điểm không khớp khoảng voucher"
                >—</span>
              </td>
              <td class="soleil-col-num users-stt">
                {{ (page - 1) * pageSize + index + 1 }}
              </td>
              <td class="soleil-col-text">
                <span class="users-mono">{{ item.maKhachHang }}</span>
              </td>
              <td class="soleil-col-text">
                <span class="users-name">{{ item.hoTen }}</span>
              </td>
              <td class="soleil-col-text">{{ item.email || '—' }}</td>
              <td class="soleil-col-text">{{ item.soDienThoai || '—' }}</td>
              <td class="soleil-col-text">
                <span class="users-points">{{ item.diemTichLuy ?? 0 }} điểm</span>
              </td>
              <td class="soleil-col-center users-date">
                {{ item.ngayTao ? formatDate(item.ngayTao) : '—' }}
              </td>
              <td class="soleil-col-center">
                <button
                  type="button"
                  class="soleil-status-toggle"
                  :title="item.trangThai !== false ? 'Nhấn để khóa' : 'Nhấn để mở khóa'"
                  @click="handleToggleStatus(item)"
                >
                  <StatusDot
                    :status="item.trangThai !== false ? 'active' : 'expired'"
                    :label="item.trangThai !== false ? 'Hoạt động' : 'Đã khóa'"
                  />
                </button>
              </td>
              <td class="soleil-col-center">
                <button
                  type="button"
                  class="soleil-act-btn"
                  title="Xem chi tiết"
                  @click="openDetail(item)"
                >
                  <Icon icon="icon-park-outline:eyes" width="16" />
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="soleil-pagination">
        <span class="soleil-pagination__info">
          Hiển thị {{ pagedCustomers.length }} / {{ filteredCustomers.length }} khách hàng
        </span>
        <div class="soleil-pagination__btns">
          <button
            type="button"
            class="soleil-page-btn"
            :disabled="page <= 1"
            @click="page--"
          >
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

    <div v-if="showDetail" class="users-modal" @click.self="closeDetail">
      <div class="users-modal__panel">
        <div class="users-modal__head">
          <div>
            <h3>Chi tiết khách hàng</h3>
            <p v-if="detail">{{ detail.maKhachHang }} · {{ detail.hoTen }}</p>
            <p v-else>Đang tải thông tin…</p>
          </div>
          <button type="button" class="soleil-btn-outline users-icon-btn" aria-label="Đóng" @click="closeDetail">
            <Icon icon="icon-park-outline:close" width="15" />
          </button>
        </div>

        <div class="users-modal__body">
          <div v-if="detailLoading" class="users-empty-cell">Đang tải…</div>
          <template v-else-if="detail">
            <div class="users-detail-grid">
              <div class="users-detail-item">
                <span class="users-detail-label">Mã khách hàng</span>
                <span class="users-mono">{{ detail.maKhachHang }}</span>
              </div>
              <div class="users-detail-item">
                <span class="users-detail-label">Họ tên</span>
                <span class="users-name">{{ detail.hoTen }}</span>
              </div>
              <div class="users-detail-item">
                <span class="users-detail-label">Email</span>
                <span>{{ detail.email || '—' }}</span>
              </div>
              <div class="users-detail-item">
                <span class="users-detail-label">Số điện thoại</span>
                <span>{{ detail.soDienThoai || '—' }}</span>
              </div>
              <div class="users-detail-item">
                <span class="users-detail-label">Loại da</span>
                <span>{{ detail.tenLoaiDa || '—' }}</span>
              </div>
              <div class="users-detail-item">
                <span class="users-detail-label">Điểm tích lũy</span>
                <span>{{ detail.diemTichLuy ?? 0 }}</span>
              </div>
              <div class="users-detail-item">
                <span class="users-detail-label">Ngày tạo</span>
                <span>{{ formatDate(detail.ngayTao) }}</span>
              </div>
              <div class="users-detail-item">
                <span class="users-detail-label">Trạng thái</span>
                <StatusDot
                  :status="detail.trangThai !== false ? 'active' : 'expired'"
                  :label="detail.trangThai !== false ? 'Hoạt động' : 'Đã khóa'"
                />
              </div>
            </div>

            <div class="users-address-block">
              <h4>Địa chỉ giao hàng</h4>
              <div v-if="!detail.diaChis?.length" class="users-muted">Chưa có địa chỉ</div>
              <div v-else class="users-address-list">
                <div
                  v-for="(dc, idx) in detail.diaChis"
                  :key="idx"
                  class="users-address-card"
                >
                  <div class="users-address-card__top">
                    <span class="users-name">{{ dc.hoTenNguoiNhan }}</span>
                    <span v-if="dc.macDinh" class="users-points">Mặc định</span>
                  </div>
                  <div class="users-muted">{{ dc.soDienThoai }}</div>
                  <div>{{ formatAddress(dc) }}</div>
                </div>
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.users-page {
  --users-ink: #1a120c;
  --users-muted: #5c4f42;
  --users-line: #c9b8a4;
  --users-line-strong: #a89278;
  --users-mist: #f3ebe1;
  --users-bronze: #8f7349;

  display: flex;
  flex-direction: column;
  gap: 0.85rem;
  color: var(--users-ink);
}

.users-page__head :deep(.soleil-page-header) {
  margin: 0;
}

.users-page__head :deep(.soleil-page-header__title) {
  font-family: inherit;
  font-size: 1.35rem;
  font-weight: 800;
  font-style: normal;
  letter-spacing: 0.02em;
  color: var(--users-ink);
}

.users-page :deep(.soleil-page-header__desc) {
  margin: 0.25rem 0 0;
  font-size: 13px;
  font-weight: 500;
  color: var(--users-muted);
}

.users-page :deep(.soleil-toolbar) {
  border-color: var(--users-line);
  background: #fff;
}

.users-toolbar {
  flex-wrap: nowrap;
  align-items: flex-end;
  gap: 8px;
  padding: 12px 14px;
}

.users-field-search {
  flex: 1 1 auto;
  min-width: 180px;
}

.users-field-compact {
  flex: 0 0 auto;
  min-width: 0;
}

.users-field-compact--sm {
  width: 88px;
}

.users-field-compact--md {
  width: 108px;
}

.users-field-compact--lg {
  width: 132px;
}

.users-page :deep(.soleil-toolbar__label) {
  color: var(--users-ink);
  font-weight: 700;
  white-space: nowrap;
}

.users-page :deep(.soleil-toolbar__input),
.users-page :deep(.soleil-toolbar__select) {
  border-color: var(--users-line-strong);
  background: #fff;
  color: var(--users-ink);
  font-weight: 500;
}

.users-page :deep(.users-input-plain) {
  padding: 8px 10px;
  padding-left: 10px;
  min-height: 36px;
  font-size: 12.5px;
}

.users-reload {
  flex: 0 0 auto;
  align-self: flex-end;
  white-space: nowrap;
  padding: 0.5rem 0.75rem !important;
}

@media (max-width: 1100px) {
  .users-toolbar {
    flex-wrap: wrap;
  }

  .users-field-compact--sm,
  .users-field-compact--md,
  .users-field-compact--lg {
    width: auto;
    min-width: 100px;
    flex: 1 1 100px;
  }
}

.users-assign-card {
  border: 1px solid var(--users-line-strong);
  border-radius: var(--radius-lg);
  background: #fff;
  padding: 14px 18px;
}

.users-assign-card__body {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
}

.users-assign-card__label {
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--users-muted);
  margin-bottom: 0.25rem;
}

.users-assign-card__title {
  font-weight: 700;
  color: var(--users-ink);
}

.users-assign-card__sub {
  font-weight: 500;
  color: var(--users-muted);
}

.users-assign-card__meta {
  margin-top: 0.25rem;
  font-size: 12px;
  color: var(--users-muted);
}

.users-assign-card__actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.users-page :deep(.soleil-table-card) {
  border-color: var(--users-line-strong);
  background: #fff;
}

.users-page :deep(.soleil-table-card__head) {
  background: #fff;
  border-bottom-color: var(--users-line);
}

.users-page :deep(table.admin-table--soleil thead th) {
  background: var(--users-bronze) !important;
  color: #fffef9 !important;
  font-size: 11px !important;
  font-weight: 700 !important;
  letter-spacing: 0.08em !important;
  border-bottom: none !important;
}

.users-page :deep(table.admin-table--soleil tbody td) {
  color: var(--users-ink);
  border-bottom: 1px solid var(--users-line);
  font-size: 13.5px;
  background: #fff;
}

.users-page :deep(table.admin-table--soleil tbody tr:hover td) {
  background: var(--users-mist);
}

.users-table-title {
  font-size: 14px;
  font-weight: 800;
}

.users-table-head-meta {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.users-table-meta {
  font-size: 12px;
  font-weight: 600;
  color: var(--users-muted);
}

.users-select-all {
  font-size: 12px !important;
  padding: 0.4rem 0.7rem !important;
}

.users-avail-pill {
  display: inline-flex;
  align-items: center;
  border: 1px solid var(--users-bronze);
  background: var(--users-bronze);
  color: #fffef9;
  padding: 0.4rem 0.75rem;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 700;
}

.users-empty-cell {
  text-align: center;
  padding: 2.5rem 1rem !important;
  color: var(--users-muted);
}

.users-mono {
  font-family: ui-monospace, 'Cascadia Mono', monospace;
  font-weight: 800;
  color: #0f4c52;
}

.users-name {
  font-weight: 700;
}

.users-stt,
.users-date {
  color: var(--users-muted);
  font-size: 13px;
}

.users-points {
  display: inline-flex;
  padding: 0.25rem 0.55rem;
  border-radius: 3px;
  border: 1px solid var(--users-line);
  background: #fff;
  font-size: 11.5px;
  font-weight: 700;
  color: var(--users-ink);
}

.users-points-miss {
  color: var(--users-muted);
  font-size: 0.8125rem;
  font-weight: 600;
}

.users-checkbox {
  accent-color: var(--users-bronze);
}

.users-check-col {
  width: 40px;
}

.users-page :deep(.soleil-pagination) {
  background: #fff;
  border-top: 1px solid var(--users-line);
}

.users-modal {
  position: fixed;
  inset: 0;
  z-index: var(--admin-z-modal);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  background: rgba(26, 18, 12, 0.45);
}

.users-modal__panel {
  width: min(640px, 100%);
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 14px;
  border: 1px solid var(--users-line);
  box-shadow: 0 16px 40px rgba(26, 18, 12, 0.18);
}

.users-modal__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 0.75rem;
  padding: 1.15rem 1.15rem 0.85rem;
  border-bottom: 1px solid var(--users-line);
  background: #fff;
}

.users-modal__head h3 {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--users-ink);
}

.users-modal__head p {
  margin: 0.25rem 0 0;
  font-size: 0.8125rem;
  color: var(--users-muted);
}

.users-icon-btn {
  padding: 0.55rem 0.7rem !important;
}

.users-modal__body {
  padding: 1.15rem;
  overflow-y: auto;
  flex: 1;
}

.users-detail-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 0.9rem;
}

@media (min-width: 640px) {
  .users-detail-grid {
    grid-template-columns: 1fr 1fr;
  }
}

.users-detail-item {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
  font-size: 0.875rem;
}

.users-detail-label {
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: #4a3f34;
}

.users-address-block {
  margin-top: 1.35rem;
}

.users-address-block h4 {
  margin: 0 0 0.75rem;
  font-size: 0.875rem;
  font-weight: 800;
  color: var(--users-ink);
}

.users-address-list {
  display: flex;
  flex-direction: column;
  gap: 0.65rem;
}

.users-address-card {
  border: 1px solid var(--users-line);
  border-radius: 8px;
  padding: 0.75rem 0.85rem;
  background: #fff;
  font-size: 0.875rem;
}

.users-address-card__top {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.25rem;
}

.users-muted {
  color: var(--users-muted);
  font-size: 0.875rem;
}
</style>
