<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import { confirm } from '@/composables/useConfirm'
import { useAdminAuth } from '@/composables/useAdminAuth'
import {
  fetchHoanTienList,
  hoanTatHoanTien,
  tuChoiHoanTien,
} from '@/api/hoanTienApi'
import { formatCurrency } from '@/utils/format'
import { useAdminBadges } from '@/composables/useAdminBadges'
import {
  hoanTienStatusLabel,
  hoanTienStatusTone,
  loaiHoanTienLabel,
} from '@/utils/returnStatus'

const router = useRouter()
const { nhanVienId } = useAdminAuth()
const { refreshBadges } = useAdminBadges()

const loading = ref(false)
const actionLoading = ref(null)
const message = ref('')
const messageType = ref('success')
const allItems = ref([])
const keyword = ref('')
const page = ref(1)
const pageSize = ref(12)

const TAB_ALL = 'ALL'
const tabs = [
  { value: TAB_ALL, label: 'Tất cả' },
  { value: 'CHO_XU_LY', label: 'Chờ xử lý' },
  { value: 'DA_HOAN', label: 'Đã hoàn' },
  { value: 'TU_CHOI', label: 'Từ chối' },
]
const currentTab = ref('CHO_XU_LY')
const expandedId = ref(null)

const showCompleteModal = ref(false)
const completeTarget = ref(null)
const completeForm = ref({
  maGiaoDichHoan: '',
  soTien: '',
  ghiChu: '',
})

const showRejectModal = ref(false)
const rejectTarget = ref(null)
const rejectNote = ref('')

function notify(text, type = 'success') {
  message.value = text
  messageType.value = type
  setTimeout(() => { message.value = '' }, 4000)
}

function formatDateTime(value) {
  if (!value) return '—'
  return new Date(value).toLocaleString('vi-VN')
}

function staffPayload(extra = {}) {
  const id = nhanVienId.value
  return id != null ? { idNhanVien: id, ...extra } : { ...extra }
}

function phuongThucLabel(pt) {
  if (pt === 'VNPAY') return 'VNPAY'
  if (pt === 'CHUYEN_KHOAN') return 'Chuyển khoản'
  return pt || '—'
}

const tabCounts = computed(() => {
  const counts = { [TAB_ALL]: allItems.value.length }
  for (const tab of tabs) {
    if (tab.value === TAB_ALL) continue
    counts[tab.value] = allItems.value.filter((i) => i.trangThai === tab.value).length
  }
  return counts
})

const filteredItems = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  return allItems.value.filter((item) => {
    if (currentTab.value !== TAB_ALL && item.trangThai !== currentTab.value) return false
    if (!kw) return true
    const haystack = [
      item.maHoaDon,
      item.maGiaoDichHoan,
      item.soTaiKhoan,
      item.chuTaiKhoan,
      item.tenNganHang,
    ]
      .filter(Boolean)
      .join(' ')
      .toLowerCase()
    return haystack.includes(kw)
  })
})

const totalPages = computed(() =>
  Math.max(1, Math.ceil(filteredItems.value.length / pageSize.value)),
)

const pagedItems = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredItems.value.slice(start, start + pageSize.value)
})

async function loadList() {
  loading.value = true
  try {
    const res = await fetchHoanTienList()
    allItems.value = res.data || []
    page.value = 1
    await refreshBadges()
  } catch (err) {
    notify(String(err), 'error')
  } finally {
    loading.value = false
  }
}

function switchTab(tab) {
  currentTab.value = tab
}

function toggleExpand(id) {
  expandedId.value = expandedId.value === id ? null : id
}

function openOrder(item) {
  if (item?.idHoaDon != null) {
    router.push(`/admin/hoa-don/chi-tiet/${item.idHoaDon}`)
  }
}

function openComplete(item) {
  completeTarget.value = item
  completeForm.value = {
    maGiaoDichHoan: '',
    soTien: item.soTien != null ? String(item.soTien) : '',
    ghiChu: '',
  }
  showCompleteModal.value = true
}

function closeComplete() {
  showCompleteModal.value = false
  completeTarget.value = null
}

async function confirmComplete() {
  const item = completeTarget.value
  if (!item) return

  const ok = await confirm({
    title: 'Hoàn tất hoàn tiền',
    message: `Xác nhận đã hoàn tiền cho đơn ${item.maHoaDon}?`,
    confirmText: 'Hoàn tất',
  })
  if (!ok) return

  actionLoading.value = item.id
  try {
    const soTienRaw = completeForm.value.soTien.trim()
    const payload = staffPayload({
      maGiaoDichHoan: completeForm.value.maGiaoDichHoan.trim() || null,
      ghiChu: completeForm.value.ghiChu.trim() || null,
      soTien: soTienRaw ? Number(soTienRaw) : null,
    })
    await hoanTatHoanTien(item.id, payload)
    notify(`Đã hoàn tất hoàn tiền đơn ${item.maHoaDon}.`)
    closeComplete()
    await loadList()
  } catch (err) {
    notify(String(err), 'error')
  } finally {
    actionLoading.value = null
  }
}

function openReject(item) {
  rejectTarget.value = item
  rejectNote.value = ''
  showRejectModal.value = true
}

function closeReject() {
  showRejectModal.value = false
  rejectTarget.value = null
}

async function confirmReject() {
  const item = rejectTarget.value
  if (!item) return
  actionLoading.value = item.id
  try {
    await tuChoiHoanTien(item.id, staffPayload({ ghiChu: rejectNote.value.trim() || null }))
    notify(`Đã từ chối hoàn tiền đơn ${item.maHoaDon}.`)
    closeReject()
    await loadList()
  } catch (err) {
    notify(String(err), 'error')
  } finally {
    actionLoading.value = null
  }
}

watch([keyword, currentTab], () => { page.value = 1 })
watch(filteredItems, () => {
  if (page.value > totalPages.value) page.value = totalPages.value
})

onMounted(loadList)
</script>

<template>
  <div class="space-y-6 order-list-page">
    <PageHeader
      title="Hoàn tiền"
      :description="`SUNOVA — ${filteredItems.length} bản ghi hoàn tiền`"
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
            placeholder="Mã đơn, mã GD, số tài khoản..."
          />
        </div>
      </div>
      <button type="button" class="soleil-btn-outline" style="align-self: flex-end" @click="loadList">
        <Icon icon="icon-park-outline:refresh" />
        Tải lại
      </button>
    </div>

    <div class="soleil-table-card">
      <div class="soleil-table-card__head">
        <span class="soleil-label" style="margin: 0">Danh sách hoàn tiền</span>
        <span class="text-xs text-[rgba(30,21,16,0.45)]">Trang {{ page }} / {{ totalPages }}</span>
      </div>

      <div class="overflow-x-auto">
        <table class="soleil-table admin-table--soleil soleil-table--orders">
          <thead>
            <tr>
              <th class="soleil-col-text">Mã HĐ</th>
              <th class="soleil-col-center">Loại</th>
              <th class="soleil-col-num">Số tiền</th>
              <th class="soleil-col-center">Phương thức</th>
              <th class="soleil-col-center">Trạng thái</th>
              <th class="soleil-col-text">Ngày tạo</th>
              <th class="soleil-col-center">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="7" class="text-center py-10 text-[var(--admin-muted)]">Đang tải dữ liệu...</td>
            </tr>
            <tr v-else-if="pagedItems.length === 0">
              <td colspan="7" class="text-center py-10 text-[var(--admin-muted)]">
                Không có bản ghi hoàn tiền phù hợp
              </td>
            </tr>
            <template v-for="item in pagedItems" :key="item.id">
              <tr>
                <td class="soleil-col-text">
                  <button type="button" class="soleil-sp-code link-btn" @click="openOrder(item)">
                    {{ item.maHoaDon }}
                  </button>
                </td>
                <td class="soleil-col-center text-sm">
                  {{ item.loaiLabel || loaiHoanTienLabel(item.loai) }}
                </td>
                <td class="soleil-col-num font-medium">{{ formatCurrency(item.soTien) }}</td>
                <td class="soleil-col-center text-sm">{{ phuongThucLabel(item.phuongThuc) }}</td>
                <td class="soleil-col-center">
                  <span
                    class="order-badge"
                    :class="`order-badge--${hoanTienStatusTone(item.trangThai)}`"
                  >
                    {{ item.trangThaiLabel || hoanTienStatusLabel(item.trangThai) }}
                  </span>
                </td>
                <td class="soleil-col-text text-sm text-[var(--admin-muted)]">
                  {{ formatDateTime(item.ngayTao) }}
                </td>
                <td class="soleil-col-center">
                  <div class="action-row">
                    <button
                      type="button"
                      class="soleil-act-btn-round"
                      title="Chi tiết"
                      @click="toggleExpand(item.id)"
                    >
                      <Icon :icon="expandedId === item.id ? 'icon-park-outline:up' : 'icon-park-outline:down'" />
                    </button>
                    <template v-if="item.trangThai === 'CHO_XU_LY'">
                      <button
                        type="button"
                        class="act-btn act-btn--ok"
                        :disabled="actionLoading === item.id"
                        @click="openComplete(item)"
                      >
                        Hoàn tất
                      </button>
                      <button
                        type="button"
                        class="act-btn act-btn--danger"
                        :disabled="actionLoading === item.id"
                        @click="openReject(item)"
                      >
                        Từ chối
                      </button>
                    </template>
                  </div>
                </td>
              </tr>
              <tr v-if="expandedId === item.id" class="detail-row">
                <td colspan="7">
                  <div class="detail-grid">
                    <div><strong>Mã GD hoàn:</strong> {{ item.maGiaoDichHoan || '—' }}</div>
                    <div><strong>Ngân hàng:</strong> {{ item.tenNganHang || '—' }}</div>
                    <div><strong>STK:</strong> {{ item.soTaiKhoan || '—' }}</div>
                    <div><strong>Chủ TK:</strong> {{ item.chuTaiKhoan || '—' }}</div>
                    <div><strong>Nhân viên:</strong> {{ item.tenNhanVien || '—' }}</div>
                    <div><strong>Ngày hoàn:</strong> {{ formatDateTime(item.ngayHoan) }}</div>
                    <div><strong>Ghi chú:</strong> {{ item.ghiChu || '—' }}</div>
                    <div><strong>ID yêu cầu trả:</strong> {{ item.idYeuCauTraHang ?? '—' }}</div>
                  </div>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>

      <div class="soleil-pagination">
        <span class="soleil-pagination__info">
          Hiển thị {{ pagedItems.length }} / {{ filteredItems.length }} bản ghi
        </span>
        <div class="soleil-pagination__btns">
          <button type="button" class="soleil-page-btn" :disabled="page <= 1" @click="page--">Trước</button>
          <button type="button" class="soleil-page-btn" :disabled="page >= totalPages" @click="page++">Sau</button>
        </div>
      </div>
    </div>

    <div v-if="showCompleteModal" class="modal-overlay" @click.self="closeComplete">
      <div class="modal-card">
        <h3>Hoàn tất hoàn tiền</h3>
        <p class="modal-sub">Đơn {{ completeTarget?.maHoaDon }} · {{ phuongThucLabel(completeTarget?.phuongThuc) }}</p>

        <label v-if="completeTarget?.phuongThuc !== 'VNPAY'" class="soleil-toolbar__label">Mã giao dịch hoàn</label>
        <input
          v-if="completeTarget?.phuongThuc !== 'VNPAY'"
          v-model="completeForm.maGiaoDichHoan"
          class="soleil-toolbar__input modal-input"
          type="text"
          placeholder="Mã chuyển khoản"
        />
        <p v-else class="modal-sub" style="margin-top: 0">
          VNPAY sẽ gọi API hoàn tiền tự động và lưu mã giao dịch refund.
        </p>
        <label class="soleil-toolbar__label">Số tiền hoàn</label>
        <input
          v-model="completeForm.soTien"
          class="soleil-toolbar__input modal-input"
          type="number"
          min="0"
          step="1000"
        />

        <label class="soleil-toolbar__label">Ghi chú</label>
        <textarea
          v-model="completeForm.ghiChu"
          class="soleil-toolbar__input modal-textarea"
          rows="2"
          placeholder="Ghi chú (tùy chọn)..."
        />

        <div class="modal-actions">
          <button type="button" class="soleil-btn-outline" @click="closeComplete">Hủy</button>
          <button
            type="button"
            class="act-btn act-btn--ok"
            :disabled="actionLoading === completeTarget?.id"
            @click="confirmComplete"
          >
            Xác nhận hoàn tất
          </button>
        </div>
      </div>
    </div>

    <div v-if="showRejectModal" class="modal-overlay" @click.self="closeReject">
      <div class="modal-card">
        <h3>Từ chối hoàn tiền</h3>
        <p class="modal-sub">Đơn {{ rejectTarget?.maHoaDon }}</p>
        <label class="soleil-toolbar__label">Lý do từ chối</label>
        <textarea
          v-model="rejectNote"
          class="soleil-toolbar__input modal-textarea"
          rows="3"
          placeholder="Nhập lý do từ chối (tùy chọn)..."
        />
        <div class="modal-actions">
          <button type="button" class="soleil-btn-outline" @click="closeReject">Hủy</button>
          <button
            type="button"
            class="act-btn act-btn--danger"
            :disabled="actionLoading === rejectTarget?.id"
            @click="confirmReject"
          >
            Xác nhận từ chối
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.order-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 2px;
  width: fit-content;
  max-width: 100%;
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
  white-space: nowrap;
}
.order-badge--success { background: rgba(72, 140, 82, 0.12); color: #3d7a4a; }
.order-badge--info { background: rgba(72, 120, 180, 0.12); color: #3a6ea8; }
.order-badge--danger { background: rgba(180, 72, 72, 0.12); color: #a83a3a; }
.order-badge--warning { background: rgba(196, 149, 84, 0.18); color: #8a6428; }
.order-badge--neutral { background: rgba(30, 21, 16, 0.06); color: rgba(30, 21, 16, 0.55); }
.action-row {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 6px;
  justify-content: center;
  align-items: center;
}
.act-btn {
  padding: 4px 10px;
  border-radius: 999px;
  border: none;
  font-size: 12px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
}
.act-btn:disabled { opacity: 0.55; cursor: not-allowed; }
.act-btn--ok { background: rgba(72, 140, 82, 0.14); color: #3d7a4a; }
.act-btn--danger { background: rgba(180, 72, 72, 0.12); color: #a83a3a; }
.link-btn {
  background: none;
  border: none;
  cursor: pointer;
  font: inherit;
  color: inherit;
  padding: 0;
}
.link-btn:hover { color: var(--bronze, #a67c3d); text-decoration: underline; }
.detail-row td {
  background: rgba(196, 149, 84, 0.04);
  padding: 12px 16px !important;
}
.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 8px 16px;
  font-size: 13px;
  color: rgba(30, 21, 16, 0.8);
}
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 1100;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}
.modal-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  width: 440px;
  max-width: 100%;
  box-shadow: 0 18px 50px rgba(15, 23, 42, 0.25);
}
.modal-card h3 { margin: 0 0 4px; font-size: 16px; }
.modal-sub { margin: 0 0 14px; font-size: 13px; color: #64748b; }
.modal-input { width: 100%; margin: 6px 0 12px; }
.modal-textarea { width: 100%; min-height: 72px; resize: vertical; margin: 6px 0 0; }
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}
</style>
