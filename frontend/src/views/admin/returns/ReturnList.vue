<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import { confirm } from '@/composables/useConfirm'
import { useAdminAuth } from '@/composables/useAdminAuth'
import {
  daNhanHangTraHang,
  duyetTraHang,
  fetchTraHangList,
  tuChoiTraHang,
} from '@/api/traHangApi'
import { useAdminBadges } from '@/composables/useAdminBadges'
import { traHangStatusLabel, traHangStatusTone } from '@/utils/returnStatus'
import { productImageUrl } from '@/utils/productImage'

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
  { value: 'CHO_DUYET', label: 'Chờ duyệt' },
  { value: 'DA_DUYET', label: 'Đã duyệt' },
  { value: 'DANG_HOAN_HANG', label: 'Đang hoàn hàng' },
  { value: 'HOAN_TAT', label: 'Hoàn tất' },
  { value: 'TU_CHOI', label: 'Từ chối' },
]
const currentTab = ref('CHO_DUYET')

const showRejectModal = ref(false)
const rejectTarget = ref(null)
const rejectNote = ref('')
const expandedId = ref(null)
const previewImageUrl = ref('')

function openImagePreview(url) {
  previewImageUrl.value = productImageUrl(url)
}

function closeImagePreview() {
  previewImageUrl.value = ''
}

function onPreviewKeydown(e) {
  if (e.key === 'Escape' && previewImageUrl.value) {
    closeImagePreview()
  }
}

watch(previewImageUrl, (url) => {
  document.body.style.overflow = url ? 'hidden' : ''
})

onUnmounted(() => {
  document.body.style.overflow = ''
  window.removeEventListener('keydown', onPreviewKeydown)
})

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
    const haystack = [item.maHoaDon, item.tenKhachHang, item.lyDo, item.maVanDonTra]
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
    const res = await fetchTraHangList()
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

async function handleDuyet(item) {
  const isVnpay = String(item.phuongThucThanhToan || '').toUpperCase() === 'VNPAY'
  const ok = await confirm({
    title: 'Duyệt yêu cầu trả hàng',
    message: isVnpay
      ? `Duyệt đơn ${item.maHoaDon}? Hệ thống sẽ hoàn tiền VNPAY tự động. Tồn kho được hoàn sau khi xác nhận đã nhận hàng trả.`
      : `Duyệt yêu cầu trả hàng cho đơn ${item.maHoaDon}?`,
    confirmText: 'Duyệt',
  })
  if (!ok) return

  actionLoading.value = item.id
  try {
    await duyetTraHang(item.id, staffPayload())
    notify(
      isVnpay
        ? `Đã duyệt và hoàn tiền VNPAY cho đơn ${item.maHoaDon}.`
        : `Đã duyệt yêu cầu trả hàng đơn ${item.maHoaDon}.`,
    )
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
  rejectNote.value = ''
}

async function confirmReject() {
  const item = rejectTarget.value
  if (!item) return
  actionLoading.value = item.id
  try {
    await tuChoiTraHang(item.id, staffPayload({ ghiChu: rejectNote.value.trim() || null }))
    notify(`Đã từ chối yêu cầu trả hàng đơn ${item.maHoaDon}.`)
    closeReject()
    await loadList()
  } catch (err) {
    notify(String(err), 'error')
  } finally {
    actionLoading.value = null
  }
}

async function handleDaNhanHang(item) {
  const ok = await confirm({
    title: 'Xác nhận đã nhận hàng',
    message: `Xác nhận đã nhận hàng hoàn của đơn ${item.maHoaDon}? Hệ thống sẽ hoàn kho và tạo yêu cầu hoàn tiền.`,
    confirmText: 'Đã nhận hàng',
  })
  if (!ok) return

  actionLoading.value = item.id
  try {
    await daNhanHangTraHang(item.id, staffPayload())
    notify(`Đã xác nhận nhận hàng đơn ${item.maHoaDon}.`)
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

onMounted(() => {
  window.addEventListener('keydown', onPreviewKeydown)
  loadList()
})
</script>

<template>
  <div class="space-y-6 order-list-page">
    <PageHeader
      title="Yêu cầu trả hàng"
      :description="`SUNOVA — ${filteredItems.length} yêu cầu`"
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
            placeholder="Mã đơn, tên khách, lý do..."
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
        <span class="soleil-label" style="margin: 0">Danh sách yêu cầu trả hàng</span>
        <span class="text-xs text-[rgba(30,21,16,0.45)]">Trang {{ page }} / {{ totalPages }}</span>
      </div>

      <div class="overflow-x-auto">
        <table class="soleil-table admin-table--soleil soleil-table--orders">
          <thead>
            <tr>
              <th class="soleil-col-text">Mã HĐ</th>
              <th class="soleil-col-text">Khách</th>
              <th class="soleil-col-text">Lý do</th>
              <th class="soleil-col-center">Trạng thái</th>
              <th class="soleil-col-text">Mã vận đơn hoàn</th>
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
                Không có yêu cầu trả hàng phù hợp
              </td>
            </tr>
            <template v-for="item in pagedItems" :key="item.id">
              <tr>
                <td class="soleil-col-text">
                  <button type="button" class="soleil-sp-code link-btn" @click="openOrder(item)">
                    {{ item.maHoaDon }}
                  </button>
                </td>
                <td class="soleil-col-text text-sm">{{ item.tenKhachHang || '—' }}</td>
                <td class="soleil-col-text text-sm">{{ item.lyDo || '—' }}</td>
                <td class="soleil-col-center">
                  <span
                    class="order-badge"
                    :class="`order-badge--${traHangStatusTone(item.trangThai)}`"
                  >
                    {{ item.trangThaiLabel || traHangStatusLabel(item.trangThai) }}
                  </span>
                </td>
                <td class="soleil-col-text text-sm">{{ item.maVanDonTra || '—' }}</td>
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
                    <template v-if="item.trangThai === 'CHO_DUYET'">
                      <button
                        type="button"
                        class="act-btn act-btn--ok"
                        :disabled="actionLoading === item.id"
                        @click="handleDuyet(item)"
                      >
                        Duyệt
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
                    <button
                      v-if="item.trangThai === 'DANG_HOAN_HANG' || item.trangThai === 'DA_DUYET'"
                      type="button"
                      class="act-btn act-btn--ok"
                      :disabled="actionLoading === item.id"
                      @click="handleDaNhanHang(item)"
                    >
                      Đã nhận hàng
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="expandedId === item.id" class="detail-row">
                <td colspan="7">
                  <div class="detail-grid">
                    <div><strong>Mô tả:</strong> {{ item.moTa || '—' }}</div>
                    <div><strong>Địa chỉ trả:</strong> {{ item.diaChiTra || '—' }}</div>
                    <div><strong>Phương thức TT:</strong> {{ item.phuongThucThanhToan || '—' }}</div>
                    <div><strong>Ngân hàng:</strong> {{ item.tenNganHang || '—' }}</div>
                    <div><strong>STK:</strong> {{ item.soTaiKhoan || '—' }}</div>
                    <div><strong>Chủ TK:</strong> {{ item.chuTaiKhoan || '—' }}</div>
                    <div><strong>Ghi chú admin:</strong> {{ item.ghiChuAdmin || '—' }}</div>
                    <div><strong>Cập nhật:</strong> {{ formatDateTime(item.ngayCapNhat) }}</div>
                  </div>
                  <div v-if="item.anhUrls?.length" class="return-images">
                    <strong class="return-images__label">Ảnh đính kèm:</strong>
                    <div class="return-images__grid">
                      <button
                        v-for="(url, idx) in item.anhUrls"
                        :key="`${item.id}-${idx}`"
                        type="button"
                        class="return-images__item"
                        title="Xem ảnh"
                        @click="openImagePreview(url)"
                      >
                        <img :src="productImageUrl(url)" :alt="`Ảnh trả hàng ${idx + 1}`" />
                      </button>
                    </div>
                  </div>
                  <div v-else class="return-images return-images--empty">
                    <strong>Ảnh đính kèm:</strong> —
                  </div>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>

      <div class="soleil-pagination">
        <span class="soleil-pagination__info">
          Hiển thị {{ pagedItems.length }} / {{ filteredItems.length }} yêu cầu
        </span>
        <div class="soleil-pagination__btns">
          <button type="button" class="soleil-page-btn" :disabled="page <= 1" @click="page--">Trước</button>
          <button type="button" class="soleil-page-btn" :disabled="page >= totalPages" @click="page++">Sau</button>
        </div>
      </div>
    </div>

    <div v-if="showRejectModal" class="modal-overlay" @click.self="closeReject">
      <div class="modal-card">
        <h3>Từ chối yêu cầu trả hàng</h3>
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

    <Teleport to="body">
      <div
        v-if="previewImageUrl"
        class="return-lightbox"
        role="dialog"
        aria-modal="true"
        aria-label="Xem ảnh trả hàng"
        @click.self="closeImagePreview"
      >
        <button
          type="button"
          class="return-lightbox__close"
          aria-label="Đóng"
          @click="closeImagePreview"
        >
          <Icon icon="mdi:close" width="24" />
        </button>
        <img
          :src="previewImageUrl"
          alt="Ảnh trả hàng"
          class="return-lightbox__img"
          @click.stop
        />
      </div>
    </Teleport>
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
.return-images {
  margin-top: 12px;
}
.return-images--empty {
  font-size: 13px;
  color: rgba(30, 21, 16, 0.8);
}
.return-images__label {
  display: block;
  margin-bottom: 8px;
  font-size: 13px;
}
.return-images__grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.return-images__item {
  display: block;
  width: 72px;
  height: 72px;
  padding: 0;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid rgba(30, 21, 16, 0.12);
  background: #fff;
  cursor: pointer;
}
.return-images__item:hover {
  border-color: var(--bronze, #a67c3d);
  box-shadow: 0 0 0 2px rgba(166, 124, 61, 0.2);
}
.return-images__item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.return-lightbox {
  position: fixed;
  inset: 0;
  z-index: 6000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 24px 24px;
  background: rgba(15, 23, 42, 0.82);
}
.return-lightbox__close {
  position: fixed;
  top: 16px;
  right: 16px;
  z-index: 6001;
  width: 44px;
  height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 999px;
  background: #fff;
  color: #0f172a;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.25);
}
.return-lightbox__close:hover {
  background: #f1f5f9;
}
.return-lightbox__img {
  max-width: min(960px, 100%);
  max-height: calc(100vh - 80px);
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.35);
  background: #fff;
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
  width: 420px;
  max-width: 100%;
  box-shadow: 0 18px 50px rgba(15, 23, 42, 0.25);
}
.modal-card h3 { margin: 0 0 4px; font-size: 16px; }
.modal-sub { margin: 0 0 14px; font-size: 13px; color: #64748b; }
.modal-textarea { width: 100%; min-height: 80px; resize: vertical; margin-top: 6px; }
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}
</style>
