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
    .filter((c) => c.trangThai !== false)
    .map((c) => c.id),
)

const selectedAssignableIds = computed(() =>
  selectedIds.value.filter((id) => assignableIds.value.includes(id)),
)

const allFilteredSelected = computed(() =>
  assignableIds.value.length > 0
    && assignableIds.value.every((id) => selectedIds.value.includes(id)),
)

function toggleSelect(id) {
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
  <div class="space-y-6">
    <PageHeader
      title="Quản lý khách hàng"
      :description="`SUNOVA — ${filteredCustomers.length} khách hàng`"
    />

    <div
      v-if="message"
      class="admin-alert rounded-lg px-4 py-3 text-sm"
      :class="messageType === 'error' ? 'admin-alert-error' : 'admin-alert-success'"
    >
      {{ message }}
    </div>

    <!-- Gán voucher context -->
    <div v-if="selectedVoucherId" class="soleil-table-card" style="padding: 14px 18px">
      <div class="flex flex-wrap items-center justify-between gap-3">
        <div class="min-w-0">
          <div class="text-xs uppercase tracking-wide text-[rgba(30,21,16,0.45)] mb-1">
            Đang gán voucher
          </div>
          <div class="font-medium text-[var(--ink)]">
            <span class="font-mono">{{ selectedVoucher?.ma || route.query.voucherMa || selectedVoucherId }}</span>
            <span v-if="selectedVoucher?.ten" class="text-[rgba(30,21,16,0.55)]">
              — {{ selectedVoucher.ten }}
            </span>
          </div>
          <div class="text-xs text-[rgba(30,21,16,0.45)] mt-1">
            Đã chọn {{ selectedAssignableIds.length }} / {{ assignableIds.length }} khách hàng khả dụng
          </div>
        </div>
        <div class="flex items-center gap-2">
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
            class="bg-black text-[#c8a97e] px-4 py-2 rounded-lg text-sm disabled:opacity-50"
            :disabled="assigning || selectedAssignableIds.length === 0"
            @click="handleAssignVoucher"
          >
            {{ assigning ? 'Đang gán...' : `Gán voucher (${selectedAssignableIds.length})` }}
          </button>
        </div>
      </div>
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
            placeholder="Họ tên, email, số điện thoại..."
          />
        </div>
      </div>

      <div class="soleil-toolbar__field">
        <label class="soleil-toolbar__label">Điểm từ</label>
        <input
          v-model="filterDiemTu"
          class="soleil-toolbar__input"
          type="number"
          min="0"
          step="1"
          placeholder="0"
        />
      </div>

      <div class="soleil-toolbar__field">
        <label class="soleil-toolbar__label">Đến</label>
        <input
          v-model="filterDiemDen"
          class="soleil-toolbar__input"
          type="number"
          min="0"
          step="1"
          placeholder="Không giới hạn"
        />
      </div>

      <div class="soleil-toolbar__field">
        <label class="soleil-toolbar__label">Khách mới</label>
        <select v-model="filterKhachMoi" class="soleil-toolbar__input">
          <option value="">Tất cả</option>
          <option value="7">7 ngày gần đây</option>
          <option value="30">30 ngày gần đây</option>
          <option value="90">90 ngày gần đây</option>
        </select>
      </div>

      <div class="soleil-toolbar__field">
        <label class="soleil-toolbar__label">Voucher cá nhân</label>
        <select v-model="selectedVoucherId" class="soleil-toolbar__input">
          <option value="">— Chọn để gán —</option>
          <option
            v-for="v in personalVouchers"
            :key="v.id"
            :value="String(v.id)"
          >
            {{ v.ma }} — {{ v.ten || 'Không tên' }}
          </option>
        </select>
      </div>

      <button type="button" class="soleil-btn-outline" style="align-self: flex-end" @click="loadCustomers">
        <Icon icon="icon-park-outline:refresh" />
        Tải lại
      </button>
    </div>

    <div class="soleil-table-card">
      <div class="soleil-table-card__head">
        <span class="soleil-label" style="margin: 0">Danh sách khách hàng</span>
        <div class="flex items-center gap-3">
          <button
            v-if="selectedVoucherId"
            type="button"
            class="soleil-btn-outline text-xs"
            :disabled="assignableIds.length === 0"
            @click="toggleSelectAllFiltered"
          >
            {{ allFilteredSelected ? 'Bỏ chọn tất cả' : 'Chọn tất cả' }}
          </button>
          <span
            v-if="selectedVoucherId"
            class="inline-flex items-center border border-[#1e1510] bg-[#1e1510] px-3 py-2 text-[13px] font-semibold text-[#c8a97e]"
            style="border-radius: 4px;"
          >
            Khách hàng khả dụng ({{ assignableIds.length }})
          </span>
          <span class="text-xs text-[rgba(30,21,16,0.45)]">Trang {{ page }} / {{ totalPages }}</span>
        </div>
      </div>

      <div class="overflow-x-auto">
        <table class="soleil-table admin-table--soleil soleil-table--customers">
          <thead>
            <tr>
              <th v-if="selectedVoucherId" class="soleil-col-center" style="width: 40px">
                <input
                  type="checkbox"
                  style="accent-color: #c9a96e"
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
              <th class="soleil-col-center">Điểm tích lũy</th>
              <th class="soleil-col-center">Ngày tạo</th>
              <th class="soleil-col-center">Trạng thái</th>
              <th class="soleil-col-center">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td :colspan="selectedVoucherId ? 10 : 9" class="text-center py-10 text-[var(--admin-muted)]">
                Đang tải dữ liệu...
              </td>
            </tr>
            <tr v-else-if="pagedCustomers.length === 0">
              <td :colspan="selectedVoucherId ? 10 : 9" class="text-center py-10 text-[var(--admin-muted)]">
                Không có khách hàng phù hợp
              </td>
            </tr>
            <tr v-for="(item, index) in pagedCustomers" :key="item.id">
              <td v-if="selectedVoucherId" class="soleil-col-center">
                <input
                  v-if="item.trangThai !== false"
                  type="checkbox"
                  style="accent-color: #c9a96e"
                  :checked="isSelected(item.id)"
                  @change="toggleSelect(item.id)"
                />
              </td>
              <td class="soleil-col-num text-[rgba(30,21,16,0.45)]">
                {{ (page - 1) * pageSize + index + 1 }}
              </td>
              <td class="soleil-col-text">
                <span class="soleil-sp-code">{{ item.maKhachHang }}</span>
              </td>
              <td class="soleil-col-text font-medium text-[var(--ink)]">{{ item.hoTen }}</td>
              <td class="soleil-col-text text-sm">{{ item.email || '—' }}</td>
              <td class="soleil-col-text text-sm">{{ item.soDienThoai || '—' }}</td>
              <td class="soleil-col-center">
                <span class="soleil-pill--form text-xs">{{ item.diemTichLuy ?? 0 }} điểm</span>
              </td>
              <td class="soleil-col-center text-sm text-[rgba(30,21,16,0.55)]">
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

    <div v-if="showDetail" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div class="absolute inset-0 bg-black/45" @click="closeDetail" />
      <div class="relative w-full max-w-2xl max-h-[90vh] overflow-hidden admin-card flex flex-col">
        <div class="px-5 py-4 border-b flex items-center justify-between" style="border-color: var(--admin-border)">
          <h2 class="text-lg font-semibold">Chi tiết khách hàng</h2>
          <button type="button" class="admin-btn admin-btn-default !px-2.5" @click="closeDetail">✕</button>
        </div>
        <div class="px-5 py-4 overflow-y-auto flex-1">
          <div v-if="detailLoading" class="text-center py-10 text-[var(--admin-muted)]">
            Đang tải...
          </div>
          <template v-else-if="detail">
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 text-sm">
              <div>
                <div class="text-xs text-[var(--admin-muted)] mb-1">Mã khách hàng</div>
                <div class="font-medium">{{ detail.maKhachHang }}</div>
              </div>
              <div>
                <div class="text-xs text-[var(--admin-muted)] mb-1">Họ tên</div>
                <div class="font-medium">{{ detail.hoTen }}</div>
              </div>
              <div>
                <div class="text-xs text-[var(--admin-muted)] mb-1">Email</div>
                <div>{{ detail.email || '—' }}</div>
              </div>
              <div>
                <div class="text-xs text-[var(--admin-muted)] mb-1">Số điện thoại</div>
                <div>{{ detail.soDienThoai || '—' }}</div>
              </div>
              <div>
                <div class="text-xs text-[var(--admin-muted)] mb-1">Loại da</div>
                <div>{{ detail.tenLoaiDa || '—' }}</div>
              </div>
              <div>
                <div class="text-xs text-[var(--admin-muted)] mb-1">Điểm tích lũy</div>
                <div>{{ detail.diemTichLuy ?? 0 }}</div>
              </div>
              <div>
                <div class="text-xs text-[var(--admin-muted)] mb-1">Ngày tạo</div>
                <div>{{ formatDate(detail.ngayTao) }}</div>
              </div>
              <div>
                <div class="text-xs text-[var(--admin-muted)] mb-1">Trạng thái</div>
                <StatusDot
                  :status="detail.trangThai !== false ? 'active' : 'expired'"
                  :label="detail.trangThai !== false ? 'Hoạt động' : 'Đã khóa'"
                />
              </div>
            </div>

            <div class="mt-6">
              <h3 class="text-sm font-semibold mb-3">Địa chỉ giao hàng</h3>
              <div v-if="!detail.diaChis?.length" class="text-sm text-[var(--admin-muted)]">
                Chưa có địa chỉ
              </div>
              <div v-else class="space-y-3">
                <div
                  v-for="(dc, idx) in detail.diaChis"
                  :key="idx"
                  class="rounded-lg border p-3 text-sm"
                  style="border-color: var(--admin-border)"
                >
                  <div class="flex items-center gap-2 mb-1">
                    <span class="font-medium">{{ dc.hoTenNguoiNhan }}</span>
                    <span v-if="dc.macDinh" class="soleil-pill--form text-xs">Mặc định</span>
                  </div>
                  <div class="text-[var(--admin-muted)]">{{ dc.soDienThoai }}</div>
                  <div class="mt-1">{{ formatAddress(dc) }}</div>
                </div>
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>
