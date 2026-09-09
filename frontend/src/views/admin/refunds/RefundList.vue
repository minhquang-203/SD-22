<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
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
import { productImageUrl } from '@/utils/productImage'
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

const FLOW_STEPS = [
  { value: 'CHO_XU_LY', title: 'Chờ xử lý', desc: 'Hoàn tất hoặc từ chối' },
  { value: 'DA_HOAN', title: 'Đã hoàn tiền', desc: 'CK: mã GD + ảnh. VNPAY: gọi API' },
  { value: 'TU_CHOI', title: 'Từ chối', desc: 'Ghi lý do, không hoàn' },
]
const expandedId = ref(null)

const showCompleteModal = ref(false)
const completeTarget = ref(null)
const completeForm = ref({
  maGiaoDichHoan: '',
  ghiChu: '',
})
const proofFiles = ref([])
const proofPreviews = ref([])
const fileInputRef = ref(null)
const MAX_PROOF_IMAGES = 6

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
    if (i > 0 && nums[i] - nums[i - 1] > 1) items.push('…')
    items.push(nums[i])
  }
  return items
})

function changePage(next) {
  if (next < 1 || next > totalPages.value || next === page.value) return
  page.value = next
}

const tableTitle = computed(() => {
  const tab = tabs.find((t) => t.value === currentTab.value)
  if (!tab || tab.value === TAB_ALL) return 'Danh sách hoàn tiền'
  return `Bản ghi ${tab.label.toLowerCase()}`
})

async function loadList({ silent = false } = {}) {
  if (!silent) loading.value = true
  try {
    const res = await fetchHoanTienList()
    allItems.value = res.data || []
    await refreshBadges()
  } catch (err) {
    notify(String(err), 'error')
  } finally {
    if (!silent) loading.value = false
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
    ghiChu: '',
  }
  clearProofImages()
  showCompleteModal.value = true
}

function clearProofImages() {
  proofPreviews.value.forEach((url) => {
    if (url) URL.revokeObjectURL(url)
  })
  proofFiles.value = []
  proofPreviews.value = []
  if (fileInputRef.value) fileInputRef.value.value = ''
}

function closeComplete() {
  showCompleteModal.value = false
  completeTarget.value = null
  clearProofImages()
}

function onProofFileChange(event) {
  const incoming = Array.from(event.target.files || []).filter((f) => f?.type?.startsWith('image/'))
  if (event.target) event.target.value = ''
  if (!incoming.length) return
  const remaining = MAX_PROOF_IMAGES - proofFiles.value.length
  if (remaining <= 0) {
    notify(`Chỉ được tải tối đa ${MAX_PROOF_IMAGES} ảnh.`, 'error')
    return
  }
  const toAdd = incoming.slice(0, remaining)
  proofFiles.value = [...proofFiles.value, ...toAdd]
  proofPreviews.value = [
    ...proofPreviews.value,
    ...toAdd.map((f) => URL.createObjectURL(f)),
  ]
}

function removeProofImage(index) {
  const url = proofPreviews.value[index]
  if (url) URL.revokeObjectURL(url)
  proofFiles.value = proofFiles.value.filter((_, i) => i !== index)
  proofPreviews.value = proofPreviews.value.filter((_, i) => i !== index)
}

async function confirmComplete() {
  const item = completeTarget.value
  if (!item) return

  const isVnpay = item.phuongThuc === 'VNPAY'
  if (!isVnpay && !completeForm.value.maGiaoDichHoan.trim()) {
    notify('Vui lòng nhập mã giao dịch hoàn tiền.', 'error')
    return
  }

  const ok = await confirm({
    title: 'Hoàn tất hoàn tiền',
    message: isVnpay
      ? `Hoàn tiền VNPAY cho đơn ${item.maHoaDon}? Hệ thống sẽ gọi API hoàn tiền ngay bây giờ.`
      : `Xác nhận đã hoàn tiền cho đơn ${item.maHoaDon}?`,
    confirmText: 'Hoàn tất',
  })
  if (!ok) return

  actionLoading.value = item.id
  try {
    const payload = staffPayload({
      phuongThuc: item.phuongThuc,
      maGiaoDichHoan: completeForm.value.maGiaoDichHoan.trim() || null,
      ghiChu: completeForm.value.ghiChu.trim() || null,
    })
    await hoanTatHoanTien(item.id, payload, isVnpay ? [] : proofFiles.value)
    notify(`Đã hoàn tất hoàn tiền đơn ${item.maHoaDon}.`)
    closeComplete()
    await loadList({ silent: true })
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
    await loadList({ silent: true })
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
onUnmounted(clearProofImages)
</script>

<template>
  <div class="order-list-page">
    <PageHeader
      title="Hoàn tiền"
      description="Quyết định hoàn / từ chối sau khi nhận hàng, hoặc khi hủy đơn đã thanh toán."
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
      class="ops-toast"
      :class="messageType === 'error' ? 'ops-toast--error' : 'ops-toast--ok'"
    >
      {{ message }}
    </div>

    <div class="soleil-table-card order-table-card">
      <div class="soleil-table-card__head">
        <span class="order-table-title">{{ tableTitle }}</span>
        <div class="order-table-search">
          <Icon icon="icon-park-outline:search" class="order-table-search__icon" />
          <input
            v-model="keyword"
            class="order-table-search__input"
            type="text"
            placeholder="Mã đơn, mã GD, số tài khoản..."
          />
        </div>
        <button
          type="button"
          class="soleil-btn-outline order-reload-btn"
          :class="{ 'is-busy': loading }"
          :disabled="loading"
          @click="loadList()"
        >
          <Icon icon="icon-park-outline:refresh" />
          Tải lại
        </button>
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
          <tbody :class="{ 'is-refreshing': loading && pagedItems.length }">
            <tr v-if="!pagedItems.length && loading">
              <td colspan="7" class="text-center py-10 text-[var(--admin-muted)]">Đang tải dữ liệu...</td>
            </tr>
            <tr v-else-if="!pagedItems.length">
              <td colspan="7" class="text-center py-10 text-[var(--admin-muted)]">
                Không có bản ghi hoàn tiền phù hợp
              </td>
            </tr>
            <template v-for="item in pagedItems" :key="item.id">
              <tr :class="{ 'order-row--pending': item.trangThai === 'CHO_XU_LY' }">
                <td class="soleil-col-text">
                  <button type="button" class="order-code" @click="openOrder(item)">
                    {{ item.maHoaDon }}
                  </button>
                </td>
                <td class="soleil-col-center">
                  <span
                    class="order-badge"
                    :class="item.loai === 'HUY_DON' ? 'order-badge--neutral' : 'order-badge--info'"
                  >
                    {{ item.loaiLabel || loaiHoanTienLabel(item.loai) }}
                  </span>
                </td>
                <td class="soleil-col-num"><span class="order-money">{{ formatCurrency(item.soTien) }}</span></td>
                <td class="soleil-col-center text-sm">{{ phuongThucLabel(item.phuongThuc) }}</td>
                <td class="soleil-col-center">
                  <span
                    class="order-badge"
                    :class="`order-badge--${hoanTienStatusTone(item.trangThai)}`"
                  >
                    {{ item.trangThaiLabel || hoanTienStatusLabel(item.trangThai) }}
                  </span>
                </td>
                <td class="soleil-col-text">
                  <span class="order-date">{{ formatDateTime(item.ngayTao) }}</span>
                </td>
                <td class="soleil-col-center">
                  <div class="action-row">
                    <button
                      type="button"
                      class="order-act-btn"
                      title="Chi tiết"
                      @click="toggleExpand(item.id)"
                    >
                      <Icon :icon="expandedId === item.id ? 'icon-park-outline:up' : 'icon-park-outline:down'" />
                    </button>
                    <template v-if="item.trangThai === 'CHO_XU_LY'">
                      <button
                        type="button"
                        class="hd-btn hd-btn--ok"
                        :disabled="actionLoading === item.id"
                        @click="openComplete(item)"
                      >
                        Hoàn tất
                      </button>
                      <button
                        type="button"
                        class="hd-btn hd-btn--danger"
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
                    <div><strong>Số tiền:</strong> {{ formatCurrency(item.soTien) }}</div>
                    <div><strong>Mã GD hoàn:</strong> {{ item.maGiaoDichHoan || '—' }}</div>
                    <div><strong>Ngân hàng:</strong> {{ item.tenNganHang || '—' }}</div>
                    <div><strong>STK:</strong> {{ item.soTaiKhoan || '—' }}</div>
                    <div><strong>Chủ TK:</strong> {{ item.chuTaiKhoan || '—' }}</div>
                    <div><strong>Nhân viên:</strong> {{ item.tenNhanVien || '—' }}</div>
                    <div><strong>Ngày hoàn:</strong> {{ formatDateTime(item.ngayHoan) }}</div>
                    <div><strong>Ghi chú:</strong> {{ item.ghiChu || '—' }}</div>
                    <div><strong>ID yêu cầu trả:</strong> {{ item.idYeuCauTraHang ?? '—' }}</div>
                  </div>
                  <div v-if="item.anhUrls?.length" class="proof-images">
                    <strong class="proof-images__label">Ảnh chứng từ:</strong>
                    <div class="proof-images__grid">
                      <a
                        v-for="(url, idx) in item.anhUrls"
                        :key="`${item.id}-anh-${idx}`"
                        :href="productImageUrl(url)"
                        target="_blank"
                        rel="noopener"
                        class="proof-images__item"
                      >
                        <img :src="productImageUrl(url)" :alt="`Chứng từ ${idx + 1}`" />
                      </a>
                    </div>
                  </div>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>

      <div class="soleil-pagination order-pager">
        <span class="soleil-pagination__info">
          Hiển thị {{ pagedItems.length }} / {{ filteredItems.length }} bản ghi
        </span>
        <div class="soleil-pagination__btns">
          <button
            type="button"
            class="soleil-page-btn"
            title="Trang trước"
            :disabled="page <= 1"
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
              @click="changePage(item)"
            >
              {{ item }}
            </button>
          </template>
          <button
            type="button"
            class="soleil-page-btn"
            title="Trang sau"
            :disabled="page >= totalPages"
            @click="changePage(page + 1)"
          >
            <Icon icon="icon-park-outline:right" width="14" />
          </button>
        </div>
      </div>
    </div>

    <div v-if="showCompleteModal" class="modal-overlay" @click.self="closeComplete">
      <div class="modal-card">
        <h3>Hoàn tất hoàn tiền</h3>
        <p class="modal-sub">
          Đơn {{ completeTarget?.maHoaDon }} · {{ phuongThucLabel(completeTarget?.phuongThuc) }}
          <template v-if="completeTarget?.soTien != null">
            · {{ formatCurrency(completeTarget.soTien) }}
          </template>
        </p>

        <template v-if="completeTarget?.phuongThuc !== 'VNPAY'">
          <label class="soleil-toolbar__label">Số tiền cần chuyển</label>
          <div class="refund-amount-box" aria-live="polite">
            <span class="refund-amount-box__value">
              {{ formatCurrency(completeTarget?.soTien) }}
            </span>
            <span class="refund-amount-box__hint">
              Nhân viên chuyển đúng số tiền này về tài khoản khách
            </span>
            <div
              v-if="completeTarget?.soTaiKhoan || completeTarget?.tenNganHang || completeTarget?.chuTaiKhoan"
              class="refund-amount-box__bank"
            >
              <span v-if="completeTarget?.tenNganHang">{{ completeTarget.tenNganHang }}</span>
              <span v-if="completeTarget?.soTaiKhoan">STK {{ completeTarget.soTaiKhoan }}</span>
              <span v-if="completeTarget?.chuTaiKhoan">{{ completeTarget.chuTaiKhoan }}</span>
            </div>
          </div>

          <label class="soleil-toolbar__label">
            Mã giao dịch hoàn <span class="req-mark">*</span>
          </label>
          <input
            v-model="completeForm.maGiaoDichHoan"
            class="soleil-toolbar__input modal-input"
            type="text"
            required
            placeholder="Bắt buộc — mã chuyển khoản"
          />

          <label class="soleil-toolbar__label">Ghi chú & ảnh chứng từ</label>
          <div class="proof-box">
            <textarea
              v-model="completeForm.ghiChu"
              class="proof-box__textarea"
              rows="3"
              placeholder="Ghi chú (tùy chọn)..."
            />
            <div class="proof-box__footer">
              <input
                ref="fileInputRef"
                type="file"
                class="proof-box__file"
                accept="image/*"
                multiple
                @change="onProofFileChange"
              />
              <button
                type="button"
                class="proof-box__upload"
                :disabled="proofPreviews.length >= MAX_PROOF_IMAGES"
                @click="fileInputRef?.click()"
              >
                <Icon icon="icon-park-outline:upload-picture" width="16" />
                Thêm ảnh
              </button>
              <span class="proof-box__count">
                {{ proofPreviews.length }}/{{ MAX_PROOF_IMAGES }} ảnh
              </span>
            </div>
            <div v-if="proofPreviews.length" class="proof-previews">
              <div
                v-for="(url, index) in proofPreviews"
                :key="`${url}-${index}`"
                class="proof-preview"
              >
                <img :src="url" alt="Chứng từ" />
                <button type="button" class="proof-preview__remove" @click="removeProofImage(index)">×</button>
              </div>
            </div>
          </div>
        </template>
        <template v-else>
          <p class="vnpay-note">
            Khi xác nhận, hệ thống mới gọi API hoàn tiền VNPAY và lưu mã giao dịch refund.
          </p>
          <label class="soleil-toolbar__label">Ghi chú</label>
          <textarea
            v-model="completeForm.ghiChu"
            class="soleil-toolbar__input modal-textarea"
            rows="2"
            placeholder="Ghi chú (tùy chọn)..."
          />
        </template>

        <div class="modal-actions">
          <button type="button" class="hd-btn hd-btn--ghost" @click="closeComplete">Hủy</button>
          <button
            type="button"
            class="hd-btn hd-btn--ok"
            :disabled="
              actionLoading === completeTarget?.id
              || (completeTarget?.phuongThuc !== 'VNPAY' && !completeForm.maGiaoDichHoan.trim())
            "
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
          <button type="button" class="hd-btn hd-btn--ghost" @click="closeReject">Hủy</button>
          <button
            type="button"
            class="hd-btn hd-btn--danger"
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
.order-list-page {
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
.order-list-page :deep(.soleil-page-header) { margin: 0; }
.order-list-page :deep(.soleil-page-header__title) {
  font-size: 1.35rem;
  font-weight: 800;
  letter-spacing: 0.02em;
  color: var(--hd-ink);
}
.order-list-page :deep(.soleil-page-header__desc) {
  margin: 0.25rem 0 0;
  font-size: 12.5px;
  color: var(--hd-muted);
}

.ops-types {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.55rem;
}
.ops-type {
  background: var(--hd-surface);
  border: 1px solid var(--hd-line);
  padding: 0.75rem 0.9rem;
}
.ops-type h3 {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  font-weight: 800;
}
.ops-type p { margin: 0.3rem 0 0; font-size: 12.5px; color: var(--hd-muted); }

.ops-flow {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  background: var(--hd-surface);
  border: 1px solid var(--hd-line);
}
.ops-flow__step {
  display: block;
  text-align: left;
  padding: 0.7rem 0.85rem;
  border: 0;
  border-right: 1px solid var(--hd-line);
  background: transparent;
  color: inherit;
  font: inherit;
  cursor: pointer;
}
.ops-flow__step:last-child { border-right: 0; }
.ops-flow__step:hover { background: #f5f8fa; }
.ops-flow__step.is-now { background: var(--hd-warn-bg); }
.ops-flow__n {
  display: block;
  font-family: ui-monospace, "Cascadia Mono", monospace;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.08em;
  color: var(--hd-accent);
}
.ops-flow__step strong { display: block; margin-top: 0.15rem; font-size: 12.5px; }
.ops-flow__step span:last-child { display: block; margin-top: 0.15rem; font-size: 11px; color: var(--hd-muted); }

.order-tabs {
  display: flex;
  flex-wrap: wrap;
  width: fit-content;
  max-width: 100%;
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
}
.order-tab-btn:last-child { border-right: 0; }
.order-tab-btn:hover:not(.active) { color: var(--hd-ink); background: #f5f8fa; }
.order-tab-btn.active { background: var(--hd-accent-deep); color: #fff; font-weight: 800; }
.order-tab-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 1.35rem;
  height: 1.25rem;
  padding: 0 0.35rem;
  background: rgba(15, 26, 28, 0.08);
  font-family: ui-monospace, "Cascadia Mono", monospace;
  font-size: 11px;
  font-weight: 700;
}
.order-tab-btn.active .order-tab-count { background: rgba(255, 255, 255, 0.18); color: #fff; }

.order-reload-btn {
  height: 36px;
  flex: 0 0 auto;
  border-radius: var(--hd-radius) !important;
  font-weight: 700 !important;
}
.order-reload-btn.is-busy :deep(svg) {
  animation: ops-spin 0.7s linear infinite;
}
@keyframes ops-spin {
  to { transform: rotate(360deg); }
}
.ops-toast {
  position: fixed;
  top: 16px;
  right: 16px;
  z-index: 80;
  max-width: min(420px, calc(100vw - 32px));
  padding: 10px 14px;
  font-size: 13px;
  font-weight: 600;
  border: 1px solid var(--hd-line);
  background: #fff;
}
.ops-toast--ok { border-left: 3px solid var(--hd-ok); }
.ops-toast--error { border-left: 3px solid var(--hd-danger); }

.order-table-card {
  border: 1px solid var(--hd-ink) !important;
  border-radius: var(--hd-radius) !important;
  background: var(--hd-surface) !important;
  overflow: hidden;
}
.order-table-card :deep(.soleil-table-card__head) {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 10px;
  flex-wrap: nowrap;
  background: #f7fafb;
  border-bottom-color: var(--hd-line);
  padding: 10px 14px;
}
.order-table-title {
  margin: 0;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--hd-ink);
  white-space: nowrap;
}
.order-table-search {
  position: relative;
  min-width: 0;
  width: 100%;
  max-width: 420px;
  justify-self: end;
}
.order-table-search__icon {
  position: absolute;
  left: 0.65rem;
  top: 50%;
  transform: translateY(-50%);
  width: 15px;
  height: 15px;
  color: var(--hd-muted);
  pointer-events: none;
}
.order-table-search__input {
  width: 100%;
  height: 36px;
  padding: 0 0.65rem 0 2rem;
  border: 1px solid var(--hd-line-strong);
  border-radius: var(--hd-radius);
  background: #fff;
  color: var(--hd-ink);
  font: inherit;
  font-size: 13px;
}
.order-table-search__input:focus {
  outline: 2px solid rgba(184, 151, 106, 0.28);
  border-color: #b8976a;
}
.order-table-card :deep(.overflow-x-auto) {
  overflow: auto;
}
.order-table-card :deep(table.admin-table--soleil thead th),
.order-table-card :deep(thead th) {
  background: #b8976a !important;
  color: #fffef9 !important;
  font-size: 11px !important;
  font-weight: 700 !important;
  letter-spacing: 0.08em !important;
}
.order-table-card :deep(tbody.is-refreshing) {
  opacity: 0.55;
  transition: opacity 0.15s;
  pointer-events: none;
}
.order-table-card :deep(tbody td) {
  height: 52px;
  vertical-align: middle;
}
.order-table-card :deep(tbody tr.detail-row td) {
  height: auto;
}
.order-table-card :deep(tbody tr:hover) { background: #f3f8f8 !important; }
.order-table-card :deep(tbody tr.order-row--pending) { box-shadow: inset 3px 0 0 var(--hd-warn); }

.order-code {
  font-family: ui-monospace, "Cascadia Mono", monospace;
  font-size: 12.5px;
  font-weight: 700;
  color: var(--hd-accent-deep);
  background: none;
  border: 0;
  padding: 0;
  cursor: pointer;
}
.order-code:hover { text-decoration: underline; text-underline-offset: 2px; }
.order-money {
  font-family: ui-monospace, "Cascadia Mono", monospace;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  font-size: 12.5px;
}
.order-date { font-variant-numeric: tabular-nums; color: var(--hd-muted); font-size: 12.5px; white-space: nowrap; }

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
.order-badge--success { color: var(--hd-ok); background: var(--hd-ok-bg); }
.order-badge--info { color: var(--hd-info); background: var(--hd-info-bg); }
.order-badge--danger { color: var(--hd-danger); background: var(--hd-danger-bg); }
.order-badge--warning { color: var(--hd-warn); background: var(--hd-warn-bg); }
.order-badge--neutral { color: #4b5563; background: #f1f3f5; border-color: #c5ccd3; }

.action-row {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 6px;
  justify-content: center;
  align-items: center;
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
}
.order-act-btn:hover { border-color: var(--hd-accent); background: var(--hd-accent-soft); color: var(--hd-accent-deep); }
.hd-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 28px;
  padding: 0 0.65rem;
  border: 1px solid transparent;
  border-radius: var(--hd-radius);
  font-size: 12px;
  font-weight: 700;
  font-family: inherit;
  cursor: pointer;
}
.hd-btn:disabled { opacity: 0.55; cursor: not-allowed; }
.hd-btn--ok { background: var(--hd-ok); color: #fff; }
.hd-btn--danger { background: var(--hd-danger); color: #fff; }
.hd-btn--ghost { background: #fff; border-color: var(--hd-line-strong); color: var(--hd-ink); }

.detail-row td { background: #f7fafb; padding: 12px 16px !important; }
.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 8px 16px;
  font-size: 13px;
}
.detail-grid strong { color: var(--hd-muted); font-weight: 600; }

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 26, 28, 0.42);
  z-index: 1100;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 5vh 16px 16px;
}
.modal-card {
  background: #fff;
  border: 1px solid var(--hd-line);
  padding: 0;
  width: 520px;
  max-width: 100%;
}
.modal-card h3 {
  margin: 0;
  padding: 1rem 1.1rem 0.25rem;
  font-size: 1.05rem;
  font-weight: 800;
}
.modal-sub {
  margin: 0;
  padding: 0 1.1rem 0.85rem;
  font-size: 12.5px;
  color: var(--hd-muted);
  border-bottom: 1px solid var(--hd-line);
}
.modal-card .soleil-toolbar__label,
.modal-card .modal-input,
.modal-card .modal-textarea,
.modal-card .refund-amount-box,
.modal-card .proof-box,
.modal-card .vnpay-note {
  margin-left: 1.1rem;
  margin-right: 1.1rem;
}
.modal-card .soleil-toolbar__label {
  display: block;
  margin-top: 0.85rem;
  font-size: 13.5px;
  font-weight: 700;
  letter-spacing: 0.01em;
  text-transform: none;
  color: var(--hd-ink);
}
.req-mark { color: var(--hd-danger); }
.refund-amount-box {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-top: 6px;
  margin-bottom: 4px;
  padding: 12px;
  background: var(--hd-accent-soft);
  border-left: 3px solid var(--hd-accent);
}
.refund-amount-box__value {
  font-family: ui-monospace, "Cascadia Mono", monospace;
  font-size: 1.35rem;
  font-weight: 700;
  color: var(--hd-accent-deep);
  line-height: 1.2;
}
.refund-amount-box__hint { font-size: 12px; color: var(--hd-muted); }
.refund-amount-box__bank {
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem;
  margin-top: 0.45rem;
}
.refund-amount-box__bank span {
  min-height: 22px;
  padding: 0 0.45rem;
  background: #fff;
  border: 1px solid var(--hd-line-strong);
  font-size: 11.5px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
}
.modal-input,
.modal-textarea {
  width: calc(100% - 2.2rem);
  margin: 8px 0 4px;
  padding: 10px 12px;
  border: 1px solid var(--hd-line-strong);
  border-radius: var(--hd-radius);
  background: #fff;
  color: var(--hd-ink);
  font-family: inherit;
  font-size: 14.5px;
  font-weight: 500;
  line-height: 1.55;
  box-sizing: border-box;
}
.modal-input { height: 42px; }
.modal-textarea { min-height: 96px; resize: vertical; }
.modal-input::placeholder,
.modal-textarea::placeholder {
  color: #5a6a72;
  font-weight: 500;
  opacity: 1;
}
.modal-input:focus,
.modal-textarea:focus {
  outline: 2px solid rgba(11, 110, 117, 0.22);
  border-color: var(--hd-accent);
  background: #fff;
}
.vnpay-note {
  margin-top: 1rem;
  padding: 10px 12px;
  background: var(--hd-info-bg);
  border-left: 3px solid var(--hd-info);
  font-size: 12.5px;
}
.proof-box {
  margin-top: 6px;
  border: 1px solid var(--hd-line-strong);
  background: #fff;
  overflow: hidden;
}
.proof-box:focus-within { border-color: var(--hd-accent); }
.proof-box__textarea {
  display: block;
  width: 100%;
  min-height: 88px;
  border: none;
  resize: vertical;
  padding: 12px 14px;
  font-family: inherit;
  font-size: 14.5px;
  font-weight: 500;
  line-height: 1.55;
  color: var(--hd-ink);
  background: transparent;
  box-sizing: border-box;
}
.proof-box__textarea::placeholder {
  color: #5a6a72;
  font-weight: 500;
  opacity: 1;
}
.proof-box__textarea:focus { outline: none; }
.proof-box__footer {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-top: 1px solid var(--hd-line);
  background: #f7fafb;
}
.proof-box__file { display: none; }
.proof-box__upload {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border: 1px solid var(--hd-line-strong);
  background: #fff;
  color: var(--hd-ink);
  font: inherit;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}
.proof-box__upload:disabled { opacity: 0.55; cursor: not-allowed; }
.proof-box__count { margin-left: auto; font-size: 12px; color: var(--hd-muted); }
.proof-previews,
.proof-images__grid { display: flex; flex-wrap: wrap; gap: 8px; }
.proof-box .proof-previews { padding: 0 10px 10px; }
.proof-images { margin-top: 12px; }
.proof-images__label { display: block; margin-bottom: 8px; font-size: 13px; }
.proof-preview,
.proof-images__item {
  position: relative;
  width: 72px;
  height: 72px;
  overflow: hidden;
  border: 1px solid var(--hd-line);
  background: #fff;
  display: block;
  padding: 0;
}
.proof-preview img,
.proof-images__item img { width: 100%; height: 100%; object-fit: cover; display: block; }
.proof-preview__remove {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 22px;
  height: 22px;
  border: none;
  background: rgba(15, 26, 28, 0.72);
  color: #fff;
  cursor: pointer;
  line-height: 1;
  padding: 0;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin: 0.85rem 0 0;
  padding: 0.85rem 1.1rem;
  border-top: 1px solid var(--hd-line);
  background: #f7fafb;
}
.order-pager { background: #f7fafb !important; border-top: 1px solid var(--hd-line); }
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
@media (max-width: 900px) {
  .ops-types,
  .ops-flow { grid-template-columns: 1fr; }
}
</style>
