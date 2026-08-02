<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import { confirm } from '@/composables/useConfirm'
import { useAdminAuth } from '@/composables/useAdminAuth'
import {
  daNhanHangTraHang,
  dongBoVanDonTra,
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
  { value: 'HOAN_TAT', label: 'Hoàn tất' },
  { value: 'TU_CHOI', label: 'Từ chối' },
]
const currentTab = ref('CHO_DUYET')

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

/** Khi đổi SL lỗi → tự trừ SL tốt (tổng luôn = đã bán). */
function onLoiChange(row) {
  const max = Number(row.soLuongDaBan) || 0
  let loi = Math.max(0, Number(row.soLuongLoi) || 0)
  if (loi > max) loi = max
  row.soLuongLoi = loi
  row.soLuongTot = max - loi
}

/** Khi đổi SL tốt → tự trừ SL lỗi. */
function onTotChange(row) {
  const max = Number(row.soLuongDaBan) || 0
  let tot = Math.max(0, Number(row.soLuongTot) || 0)
  if (tot > max) tot = max
  row.soLuongTot = tot
  row.soLuongLoi = max - tot
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
    await loadList()
  } catch (err) {
    notify(String(err), 'error')
  } finally {
    actionLoading.value = null
  }
}

async function handleDongBoGhn(item) {
  actionLoading.value = item.id
  try {
    const res = await dongBoVanDonTra(item.id, staffPayload())
    const updated = res.data
    const ghn = (updated?.ghnTrangThaiTra || '').toLowerCase()
    if (ghn === 'delivered' || ghn === 'returned') {
      notify(
        `Vận đơn hoàn đơn ${item.maHoaDon} đã về cửa hàng — nhấn "Đã nhận hàng" để phân loại lô TỐT/LỖI.`,
      )
    } else {
      notify(
        `Vận đơn hoàn đơn ${item.maHoaDon}: ${updated?.ghnTrangThaiTraLabel || 'chưa có cập nhật mới'}.`,
      )
    }
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
                <td class="soleil-col-text text-sm">
                  <template v-if="item.maVanDonTra">
                    {{ item.maVanDonTra }}
                    <span v-if="item.ghnTrangThaiTraLabel" class="ghn-status">
                      {{ item.ghnTrangThaiTraLabel }}
                    </span>
                  </template>
                  <template v-else>—</template>
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
                      v-if="item.maVanDonTra && item.trangThai === 'DANG_HOAN_HANG'"
                      type="button"
                      class="act-btn act-btn--info"
                      :disabled="actionLoading === item.id"
                      @click="handleDongBoGhn(item)"
                    >
                      Đồng bộ GHN
                    </button>
                    <button
                      v-if="item.trangThai === 'DANG_HOAN_HANG' && item.maVanDonTra"
                      type="button"
                      class="act-btn act-btn--ok"
                      :disabled="actionLoading === item.id"
                      @click="handleDaNhanHang(item)"
                    >
                      Đã nhận hàng
                    </button>
                    <button
                      v-if="item.trangThai === 'DA_NHAN_HANG'"
                      type="button"
                      class="act-btn act-btn--info"
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
                  <td class="font-semibold text-[var(--bronze,#a67c3d)]">{{ row.soLo }}</td>
                  <td class="text-xs">{{ row.sku || '—' }}</td>
                  <td>{{ formatDateShort(row.hanSuDung) }}</td>
                  <td>{{ row.soLuongDaBan }}</td>
                  <td style="width: 96px">
                    <input
                      v-model.number="row.soLuongTot"
                      type="number"
                      min="0"
                      :max="row.soLuongDaBan"
                      class="soleil-toolbar__input"
                      @input="onTotChange(row)"
                    />
                  </td>
                  <td style="width: 96px">
                    <input
                      v-model.number="row.soLuongLoi"
                      type="number"
                      min="0"
                      :max="row.soLuongDaBan"
                      class="soleil-toolbar__input"
                      @input="onLoiChange(row)"
                    />
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <p class="text-xs text-[var(--admin-muted)] mt-3 mb-0">
            Ví dụ: đã bán 3, lỗi 1 → để SL tốt = 2, SL lỗi = 1 → cột SL lỗi của lô +1.
          </p>
        </template>

        <div class="modal-actions">
          <button type="button" class="soleil-btn-outline" @click="closeReceive">Hủy</button>
          <button
            type="button"
            class="act-btn act-btn--ok"
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
.act-btn--info { background: rgba(72, 120, 180, 0.12); color: #3a6ea8; }
.ghn-status {
  display: block;
  font-size: 11px;
  color: rgba(30, 21, 16, 0.5);
}
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
.receive-modal {
  width: min(720px, 96vw);
}
.receive-lot-hint {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 4px;
}
.receive-lot-chip {
  display: inline-flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px 10px;
  padding: 6px 10px;
  border-radius: 8px;
  background: rgba(30, 21, 16, 0.04);
  font-size: 12px;
  color: rgba(30, 21, 16, 0.7);
}
.receive-lot-chip strong {
  color: var(--bronze, #a67c3d);
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
