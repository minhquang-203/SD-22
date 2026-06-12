<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { Icon } from '@iconify/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatusDot from '@/components/ui/StatusDot.vue'
import {
  getCustomers,
  searchCustomers,
  getCustomerDetail,
  updateCustomerStatus,
} from '@/api/khachHangApi'
import { formatDate } from '@/utils/format'
import { confirm } from '@/composables/useConfirm'

const loading = ref(false)
const detailLoading = ref(false)
const message = ref('')
const messageType = ref('success')

const keyword = ref('')
const allCustomers = ref([])
const page = ref(1)
const pageSize = ref(10)

const showDetail = ref(false)
const detail = ref(null)

let searchTimer = null

const filteredCustomers = computed(() => allCustomers.value)

const totalPages = computed(() =>
  Math.max(1, Math.ceil(filteredCustomers.value.length / pageSize.value)),
)

const pagedCustomers = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredCustomers.value.slice(start, start + pageSize.value)
})

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
  const parts = [dc.diaChiChiTiet, dc.phuongXa, dc.quanHuyen, dc.tinhThanh].filter(Boolean)
  return parts.join(', ') || '—'
}

watch(keyword, () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => loadCustomers(), 400)
})

watch(filteredCustomers, () => {
  if (page.value > totalPages.value) {
    page.value = totalPages.value
  }
})

onMounted(() => loadCustomers())
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

    <div class="soleil-toolbar soleil-toolbar--filter">
      <div class="soleil-toolbar__field soleil-toolbar__field--wide">
        <label class="soleil-toolbar__label">Tìm kiếm</label>
        <div class="soleil-toolbar__search">
          <Icon icon="icon-park-outline:search" class="soleil-toolbar__search-icon" />
          <input
            v-model="keyword"
            class="soleil-toolbar__input"
            type="text"
            placeholder="Tìm theo họ tên, email, số điện thoại..."
          />
        </div>
      </div>
      <button type="button" class="soleil-btn-outline" style="align-self: flex-end" @click="loadCustomers">
        <Icon icon="icon-park-outline:refresh" />
        Tải lại
      </button>
    </div>

    <div class="soleil-table-card">
      <div class="soleil-table-card__head">
        <span class="soleil-label" style="margin: 0">Danh sách khách hàng</span>
        <span class="text-xs text-[rgba(30,21,16,0.45)]">Trang {{ page }} / {{ totalPages }}</span>
      </div>

      <div class="overflow-x-auto">
        <table class="soleil-table admin-table--soleil soleil-table--customers">
          <thead>
            <tr>
              <th class="soleil-col-num">STT</th>
              <th class="soleil-col-text">Mã KH</th>
              <th class="soleil-col-text">Họ tên</th>
              <th class="soleil-col-text">Email</th>
              <th class="soleil-col-text">SĐT</th>
              <th class="soleil-col-center">Điểm tích lũy</th>
              <th class="soleil-col-center">Trạng thái</th>
              <th class="soleil-col-center">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="8" class="text-center py-10 text-[var(--admin-muted)]">
                Đang tải dữ liệu...
              </td>
            </tr>
            <tr v-else-if="pagedCustomers.length === 0">
              <td colspan="8" class="text-center py-10 text-[var(--admin-muted)]">
                Không có khách hàng phù hợp
              </td>
            </tr>
            <tr v-for="(item, index) in pagedCustomers" :key="item.id">
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
                <div class="text-xs text-[var(--admin-muted)] mb-1">Giới tính</div>
                <div>{{ detail.gioiTinh || '—' }}</div>
              </div>
              <div>
                <div class="text-xs text-[var(--admin-muted)] mb-1">Ngày sinh</div>
                <div>{{ detail.ngaySinh ? formatDate(detail.ngaySinh) : '—' }}</div>
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
