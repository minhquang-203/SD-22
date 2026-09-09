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
  fetchLoHangTraHang,
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
  { value: 'DA_NHAN_HANG', label: 'Đã nhận hàng' },
  { value: 'TU_CHOI', label: 'Từ chối' },
]
const currentTab = ref('CHO_DUYET')

const FLOW_STEPS = [
  { value: 'CHO_DUYET', title: 'Chờ duyệt', desc: 'Admin duyệt / từ chối' },
  { value: 'DA_DUYET', title: 'Đã duyệt', desc: 'Khách tạo vận đơn GHN' },
  { value: 'DANG_HOAN_HANG', title: 'Đang hoàn hàng', desc: 'Theo dõi mã vận đơn' },
  { value: 'DA_NHAN_HANG', title: 'Đã nhận hàng', desc: 'Phân lô tốt / lỗi' },
  { value: 'HOAN_TAT', title: 'Hoàn tất', desc: 'Sang trang hoàn tiền' },
]

const showRejectModal = ref(false)
const rejectTarget = ref(null)
const rejectNote = ref('')
const expandedId = ref(null)
const previewImageUrl = ref('')

const showReceiveModal = ref(false)
const receiveTarget = ref(null)
const receiveLots = ref([])
const receiveRows = ref([])
const receiveLoading = ref(false)

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
  if (!tab || tab.value === TAB_ALL) return 'Danh sách yêu cầu trả hàng'
  return `Yêu cầu ${tab.label.toLowerCase()}`
})

async function loadList({ silent = false } = {}) {
  if (!silent) loading.value = true
  try {
    const res = await fetchTraHangList()
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

async function handleDuyet(item) {
  const ok = await confirm({
    title: 'Duyệt yêu cầu trả hàng',
    message: `Duyệt đơn ${item.maHoaDon}? Khách sẽ được thông báo để tạo vận đơn hoàn hàng.`
      + ' Hoàn tiền chỉ được xét sau khi cửa hàng nhận lại hàng.',
    confirmText: 'Duyệt',
  })
  if (!ok) return

  actionLoading.value = item.id
  try {
    await duyetTraHang(item.id, staffPayload())
    notify(`Đã duyệt yêu cầu trả hàng đơn ${item.maHoaDon}. Chờ khách tạo vận đơn hoàn hàng.`)
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
    await loadList({ silent: true })
  } catch (err) {
    notify(String(err), 'error')
  } finally {
    actionLoading.value = null
  }
}

async function handleDaNhanHang(item) {
  if (!item?.maVanDonTra) {
    notify('Khách chưa tạo vận đơn hoàn hàng. Không thể xác nhận đã nhận hàng.', 'error')
    return
  }
  receiveTarget.value = item
  receiveRows.value = []
  receiveLots.value = []
  showReceiveModal.value = true
  receiveLoading.value = true
  try {
    const res = await fetchLoHangTraHang(item.id)
    const lots = res.data || []
    receiveLots.value = lots
    // Mỗi lô: nhập SL tốt + SL lỗi (tổng = đã bán). Lỗi → cột SL lỗi của lô tăng.
    receiveRows.value = lots.map((lot) => ({
      idLoHang: lot.idLoHang,
      soLo: lot.soLo,
      hanSuDung: lot.hanSuDung,
      sku: lot.sku,
      tenSanPham: lot.tenSanPham,
      soLuongDaBan: Number(lot.soLuongDaBan) || 0,
      soLuongTot: Number(lot.soLuongDaBan) || 0,
      soLuongLoi: 0,
    }))
  } catch (err) {
    notify(String(err), 'error')
    closeReceive()
  } finally {
    receiveLoading.value = false
  }
}

function closeReceive() {
  showReceiveModal.value = false
  receiveTarget.value = null
  receiveLots.value = []
  receiveRows.value = []
}

function isEmptyInput(value) {
  return value === '' || value === null || value === undefined
}

/** Khi đang gõ SL lỗi → chỉ tính lại SL tốt khi đã có số hợp lệ (cho phép để trống lúc gõ dở). */
function onLoiChange(row) {
  const max = Number(row.soLuongDaBan) || 0
  if (isEmptyInput(row.soLuongLoi)) return
  let loi = Math.max(0, Number(row.soLuongLoi) || 0)
  if (loi > max) {
    loi = max
    row.soLuongLoi = loi
  }
  row.soLuongTot = max - loi
}

/** Rời ô SL lỗi → chuẩn hóa: trống = 0, kẹp trong [0, đã bán], SL tốt bù phần còn lại. */
function onLoiBlur(row) {
  const max = Number(row.soLuongDaBan) || 0
  let loi = Math.max(0, Number(row.soLuongLoi) || 0)
  if (loi > max) loi = max
  row.soLuongLoi = loi
  row.soLuongTot = max - loi
}

/** Đặt SL lỗi (kẹp 0..đã bán), SL tốt tự bù — dùng cho nút +/-. */
function setLoi(row, value) {
  const max = Number(row.soLuongDaBan) || 0
  let loi = Math.floor(Number(value) || 0)
  if (loi < 0) loi = 0
  if (loi > max) loi = max
  row.soLuongLoi = loi
  row.soLuongTot = max - loi
}

function incLoi(row) {
  setLoi(row, (Number(row.soLuongLoi) || 0) + 1)
}

function decLoi(row) {
  setLoi(row, (Number(row.soLuongLoi) || 0) - 1)
}

function formatDateShort(value) {
  if (!value) return '—'
  return new Date(value).toLocaleDateString('vi-VN')
}

function buildChiTietLoPayload() {
  const chiTietLo = []
  for (const row of receiveRows.value) {
    const tot = Number(row.soLuongTot) || 0
    const loi = Number(row.soLuongLoi) || 0
    if (tot > 0) {
      chiTietLo.push({ idLoHang: row.idLoHang, soLuong: tot, loaiHang: 'TOT' })
    }
    if (loi > 0) {
      chiTietLo.push({ idLoHang: row.idLoHang, soLuong: loi, loaiHang: 'LOI' })
    }
  }
  return chiTietLo
}

async function confirmReceive() {
  const item = receiveTarget.value
  if (!item) return

  const hasLots = receiveLots.value.length > 0
  let chiTietLo = []
  if (hasLots) {
    for (const row of receiveRows.value) {
      const max = Number(row.soLuongDaBan) || 0
      const tot = Number(row.soLuongTot) || 0
      const loi = Number(row.soLuongLoi) || 0
      if (tot < 0 || loi < 0 || tot + loi !== max) {
        notify(
          `Lô ${row.soLo}: SL tốt (${tot}) + SL lỗi (${loi}) phải = ${max}.`,
          'error',
        )
        return
      }
    }
    chiTietLo = buildChiTietLoPayload()
    if (!chiTietLo.length) {
      notify('Vui lòng phân bổ số lượng trả về từng lô.', 'error')
      return
    }
  }

  const loiCount = chiTietLo
    .filter((r) => r.loaiHang === 'LOI')
    .reduce((s, r) => s + r.soLuong, 0)
  const totCount = chiTietLo
    .filter((r) => r.loaiHang === 'TOT')
    .reduce((s, r) => s + r.soLuong, 0)

  const ok = await confirm({
    title: 'Xác nhận đã nhận hàng',
    message: hasLots
      ? `Đơn ${item.maHoaDon}: ${totCount} tốt (hoàn tồn bán) + ${loiCount} lỗi (cột SL lỗi lô tăng). Tiếp tục?`
      : `Xác nhận đã nhận hàng hoàn của đơn ${item.maHoaDon}?`,
    confirmText: 'Đã nhận hàng',
  })
  if (!ok) return

  actionLoading.value = item.id
  try {
    await daNhanHangTraHang(item.id, staffPayload({ chiTietLo }))
    notify(
      hasLots
        ? `Đã nhận hàng đơn ${item.maHoaDon}: ${totCount} tốt → hoàn lô, ${loiCount} lỗi → SL lỗi +${loiCount}.`
        : `Đã xác nhận nhận hàng đơn ${item.maHoaDon}. Vào trang Hoàn tiền để quyết định hoàn tiền.`,
    )
    closeReceive()
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

onMounted(() => {
  window.addEventListener('keydown', onPreviewKeydown)
  loadList()
})
</script>

<template>
  <div class="order-list-page">
    <PageHeader
      title="Yêu cầu trả hàng"
      description="Duyệt hoàn hàng trước. Hoàn tiền chỉ xét sau khi cửa hàng nhận lại hàng."
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
            placeholder="Mã đơn, tên khách, lý do..."
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
              <th class="soleil-col-text">Khách</th>
              <th class="soleil-col-text">Lý do</th>
              <th class="soleil-col-center">Trạng thái</th>
              <th class="soleil-col-text">Mã vận đơn hoàn</th>
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
                Không có yêu cầu trả hàng phù hợp
              </td>
            </tr>
            <template v-for="item in pagedItems" :key="item.id">
              <tr
                :class="{
                  'order-row--pending': item.trangThai === 'CHO_DUYET',
                  'order-row--info': item.trangThai === 'DANG_HOAN_HANG',
                }"
              >
                <td class="soleil-col-text">
                  <button type="button" class="order-code" @click="openOrder(item)">
                    {{ item.maHoaDon }}
                  </button>
                </td>
                <td class="soleil-col-text">
                  <span class="order-customer">{{ item.tenKhachHang || '—' }}</span>
                  <span v-if="item.phuongThucThanhToan" class="order-customer-sub">
                    {{ item.phuongThucThanhToan }}
                  </span>
                </td>
                <td class="soleil-col-text text-sm">{{ item.lyDo || '—' }}</td>
                <td class="soleil-col-center">
                  <span
                    class="order-badge"
                    :class="`order-badge--${traHangStatusTone(item.trangThai)}`"
                  >
                    {{ item.trangThaiLabel || traHangStatusLabel(item.trangThai) }}
                  </span>
                </td>
                <td class="soleil-col-text text-sm">
                  <template v-if="item.maVanDonTra">
                    <span class="order-code order-code--static">{{ item.maVanDonTra }}</span>
                    <span v-if="item.ghnTrangThaiTraLabel" class="ghn-status">
                      {{ item.ghnTrangThaiTraLabel }}
                    </span>
                  </template>
                  <template v-else>—</template>
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
                    <template v-if="item.trangThai === 'CHO_DUYET'">
                      <button
                        type="button"
                        class="hd-btn hd-btn--primary"
                        :disabled="actionLoading === item.id"
                        @click="handleDuyet(item)"
                      >
                        Duyệt
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
                    <button
                      v-if="item.trangThai === 'DANG_HOAN_HANG' && item.maVanDonTra"
                      type="button"
                      class="hd-btn hd-btn--primary"
                      :disabled="actionLoading === item.id"
                      @click="handleDaNhanHang(item)"
                    >
                      Đã nhận hàng
                    </button>
                    <button
                      v-if="item.trangThai === 'DA_NHAN_HANG'"
                      type="button"
                      class="hd-btn hd-btn--primary"
                      title="Quyết định hoàn tiền hay từ chối"
                      @click="router.push('/admin/hoan-tien')"
                    >
                      Xử lý hoàn tiền
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
                    <div><strong>Ca lấy hàng:</strong> {{ item.pickShiftLabel || '—' }}</div>
                    <div><strong>Trạng thái vận đơn hoàn:</strong> {{ item.ghnTrangThaiTraLabel || '—' }}</div>
                    <div><strong>Nhận lại hàng lúc:</strong> {{ formatDateTime(item.ngayNhanHang) }}</div>
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

      <div class="soleil-pagination order-pager">
        <span class="soleil-pagination__info">
          Hiển thị {{ pagedItems.length }} / {{ filteredItems.length }} yêu cầu
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

    <div v-if="showReceiveModal" class="modal-overlay" @click.self="closeReceive">
      <div class="modal-card receive-modal">
        <h3>Nhận hàng trả — phân loại lô</h3>
        <p class="modal-sub">
          Đơn {{ receiveTarget?.maHoaDon }} — với mỗi lô: nhập bao nhiêu còn tốt / bao nhiêu lỗi.
          Lỗi sẽ cộng vào cột <strong>SL lỗi</strong> của lô (không bán lại).
        </p>

        <div v-if="receiveLoading" class="text-sm text-[var(--admin-muted)] py-6 text-center">
          Đang tải danh sách lô...
        </div>
        <template v-else-if="receiveLots.length === 0">
          <p class="text-sm text-[var(--admin-muted)] mb-4">
            Đơn cũ chưa ghi nhận phân bổ lô — hệ thống sẽ hoàn tồn mặc định (toàn bộ hàng tốt).
          </p>
        </template>
        <template v-else>
          <div class="overflow-x-auto mt-1">
            <table class="soleil-table admin-table--soleil w-full text-sm">
              <thead>
                <tr>
                  <th>Số lô</th>
                  <th>SKU</th>
                  <th>HSD</th>
                  <th>Đã bán</th>
                  <th>SL tốt</th>
                  <th>SL lỗi</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in receiveRows" :key="row.idLoHang">
                  <td class="lot-code">{{ row.soLo }}</td>
                  <td class="text-xs">{{ row.sku || '—' }}</td>
                  <td>{{ formatDateShort(row.hanSuDung) }}</td>
                  <td>{{ row.soLuongDaBan }}</td>
                  <td class="font-semibold text-[#3d7a4a]" style="width: 80px">
                    {{ row.soLuongTot }}
                  </td>
                  <td style="width: 150px">
                    <div class="qty-stepper">
                      <button
                        type="button"
                        class="qty-stepper__btn"
                        :disabled="(Number(row.soLuongLoi) || 0) <= 0"
                        @click="decLoi(row)"
                      >
                        −
                      </button>
                      <input
                        v-model.number="row.soLuongLoi"
                        type="number"
                        min="0"
                        :max="row.soLuongDaBan"
                        class="qty-stepper__input"
                        @input="onLoiChange(row)"
                        @blur="onLoiBlur(row)"
                      />
                      <button
                        type="button"
                        class="qty-stepper__btn"
                        :disabled="(Number(row.soLuongLoi) || 0) >= (Number(row.soLuongDaBan) || 0)"
                        @click="incLoi(row)"
                      >
                        +
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <p class="text-xs text-[var(--admin-muted)] mt-3 mb-0">
            Nhập <strong>SL lỗi</strong> (bấm +/− hoặc gõ số) — <strong>SL tốt</strong> tự tính phần còn lại.
            VD: đã bán 3, lỗi 1 → SL tốt = 2 → cột SL lỗi của lô +1.
          </p>
        </template>

        <div class="modal-actions">
          <button type="button" class="hd-btn hd-btn--ghost" @click="closeReceive">Hủy</button>
          <button
            type="button"
            class="hd-btn hd-btn--primary"
            :disabled="actionLoading === receiveTarget?.id || receiveLoading"
            @click="confirmReceive"
          >
            Xác nhận đã nhận hàng
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

.ops-flow {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
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
.ops-flow__step.is-now { background: var(--hd-accent-soft); }
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
  padding: 0 0.85rem;
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
.order-tab-btn.active {
  background: var(--hd-accent-deep);
  color: #fff;
  font-weight: 800;
}
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
.order-table-card :deep(tbody tr.order-row--info) { box-shadow: inset 3px 0 0 var(--hd-info); }

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
.order-code--static { cursor: default; }
.order-code:hover { text-decoration: underline; text-underline-offset: 2px; }
.order-customer { display: block; font-weight: 600; font-size: 13px; }
.order-customer-sub { display: block; margin-top: 0.1rem; font-size: 11.5px; color: var(--hd-muted); }
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
.hd-btn--primary { background: var(--hd-accent); color: #fff; }
.hd-btn--primary:hover:not(:disabled) { background: var(--hd-accent-deep); }
.hd-btn--danger { background: var(--hd-danger); color: #fff; }
.hd-btn--ghost { background: #fff; border-color: var(--hd-line-strong); color: var(--hd-ink); }

.ghn-status { display: block; font-size: 11px; color: var(--hd-muted); }
.detail-row td { background: #f7fafb; padding: 12px 16px !important; }
.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 8px 16px;
  font-size: 13px;
  color: var(--hd-ink);
}
.detail-grid strong { color: var(--hd-muted); font-weight: 600; }
.return-images { margin-top: 12px; }
.return-images--empty { font-size: 13px; color: var(--hd-muted); }
.return-images__label { display: block; margin-bottom: 8px; font-size: 13px; }
.return-images__grid { display: flex; flex-wrap: wrap; gap: 8px; }
.return-images__item {
  display: block;
  width: 72px;
  height: 72px;
  padding: 0;
  overflow: hidden;
  border: 1px solid var(--hd-line);
  background: #fff;
  cursor: pointer;
}
.return-images__item:hover { border-color: var(--hd-accent); }
.return-images__item img { width: 100%; height: 100%; object-fit: cover; display: block; }
.return-lightbox {
  position: fixed;
  inset: 0;
  z-index: 6000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 24px 24px;
  background: rgba(15, 26, 28, 0.72);
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
  border: 1px solid var(--hd-line-strong);
  background: #fff;
  color: var(--hd-ink);
  cursor: pointer;
}
.return-lightbox__img {
  max-width: min(960px, 100%);
  max-height: calc(100vh - 80px);
  object-fit: contain;
  background: #fff;
}
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
  width: 440px;
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
.modal-card .modal-textarea,
.modal-card .overflow-x-auto,
.modal-card > p,
.modal-card > .text-sm {
  margin-left: 1.1rem;
  margin-right: 1.1rem;
}
.modal-card .soleil-toolbar__label {
  display: block;
  margin-top: 1rem;
  font-size: 13.5px;
  font-weight: 700;
  letter-spacing: 0.01em;
  text-transform: none;
  color: var(--hd-ink);
}
.receive-modal { width: min(760px, 96vw); }
.receive-modal > *:not(h3):not(.modal-sub):not(.modal-actions) {
  margin-left: 1.1rem;
  margin-right: 1.1rem;
}
.lot-code { color: var(--hd-accent-deep); font-weight: 800; }
.qty-stepper { display: inline-flex; border: 1px solid var(--hd-line-strong); }
.qty-stepper__btn {
  width: 28px;
  height: 28px;
  flex: 0 0 28px;
  border: 0;
  background: #fff;
  color: var(--hd-ink);
  font-size: 16px;
  font-weight: 800;
  cursor: pointer;
}
.qty-stepper__btn:disabled { opacity: 0.4; cursor: not-allowed; }
.qty-stepper__input {
  width: 44px;
  height: 28px;
  box-sizing: border-box;
  margin: 0;
  padding: 0;
  border: 0;
  border-left: 1px solid var(--hd-line-strong);
  border-right: 1px solid var(--hd-line-strong);
  background: #fff;
  font-family: ui-monospace, "Cascadia Mono", monospace;
  font-size: 13px;
  font-weight: 700;
  text-align: center;
  outline: none;
  -moz-appearance: textfield;
}
.qty-stepper__input::-webkit-outer-spin-button,
.qty-stepper__input::-webkit-inner-spin-button { -webkit-appearance: none; margin: 0; }
.modal-textarea {
  width: calc(100% - 2.2rem);
  min-height: 96px;
  resize: vertical;
  margin-top: 8px;
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
.modal-textarea::placeholder {
  color: #5a6a72;
  font-weight: 500;
  opacity: 1;
}
.modal-textarea:focus {
  outline: 2px solid rgba(11, 110, 117, 0.22);
  border-color: var(--hd-accent);
  background: #fff;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin: 0;
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
  .ops-flow { grid-template-columns: 1fr 1fr; }
  .ops-flow__step { border-bottom: 1px solid var(--hd-line); }
}
</style>
