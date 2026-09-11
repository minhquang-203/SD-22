<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import {
  getNhaCungCapList,
  getPhieuNhapList,
  huyPhieuNhap,
} from '@/api/nhapHangApi'
import { toast } from '@/composables/useToast'
import { confirm } from '@/composables/useConfirm'
import { formatApiError } from '@/utils/apiError'

const router = useRouter()
const loading = ref(false)
const rows = ref([])
const nccOptions = ref([])

const filters = ref({
  trangThai: '',
  idNcc: '',
  from: '',
  to: '',
})

const STATUS_LABEL = {
  PHIEU_TAM: 'Phiếu tạm',
  DA_NHAP: 'Đã nhập',
  DA_HUY: 'Đã hủy',
}

function statusTone(st) {
  if (st === 'DA_NHAP') return 'ok'
  if (st === 'DA_HUY') return 'muted'
  return 'draft'
}

function formatMoney(v) {
  const n = Number(v || 0)
  return n.toLocaleString('vi-VN') + ' ₫'
}

function formatDate(v) {
  if (!v) return '—'
  const d = new Date(v)
  if (Number.isNaN(d.getTime())) return String(v).slice(0, 10)
  return d.toLocaleDateString('vi-VN')
}

async function loadNcc() {
  try {
    const res = await getNhaCungCapList()
    nccOptions.value = res.data || []
  } catch {
    nccOptions.value = []
  }
}

async function load() {
  loading.value = true
  try {
    const params = {}
    if (filters.value.trangThai) params.trangThai = filters.value.trangThai
    if (filters.value.idNcc) params.idNcc = Number(filters.value.idNcc)
    if (filters.value.from) params.from = filters.value.from
    if (filters.value.to) params.to = filters.value.to
    const res = await getPhieuNhapList(params)
    rows.value = res.data || []
  } catch (e) {
    toast(formatApiError(e, 'Không tải được phiếu nhập'), 'error')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = { trangThai: '', idNcc: '', from: '', to: '' }
  load()
}

function openCreate() {
  router.push('/admin/nhap-hang/tao')
}

function openDetail(row) {
  router.push(`/admin/nhap-hang/${row.id}`)
}

async function onHuy(row) {
  if (row.trangThai !== 'PHIEU_TAM') return
  const ok = await confirm({
    title: 'Hủy phiếu nhập',
    message: `Hủy phiếu ${row.maPhieuNhap}?`,
    confirmText: 'Hủy phiếu',
    danger: true,
  })
  if (!ok) return
  try {
    await huyPhieuNhap(row.id)
    toast('Đã hủy phiếu', 'success')
    await load()
  } catch (e) {
    toast(formatApiError(e, 'Không hủy được phiếu'), 'error')
  }
}

onMounted(async () => {
  await loadNcc()
  await load()
})
</script>

<template>
  <div class="pn-page">
    <PageHeader
      title="Nhập hàng"
      description="Phiếu nhập kho từ nhà cung cấp"
    >
      <template #actions>
        <button type="button" class="soleil-btn-primary" @click="openCreate">
          <Icon icon="icon-park-outline:plus" width="16" />
          Nhập hàng
        </button>
      </template>
    </PageHeader>

    <div class="soleil-toolbar soleil-toolbar--filter pn-filters">
      <div class="soleil-toolbar__field">
        <label class="soleil-toolbar__label">Trạng thái</label>
        <select v-model="filters.trangThai" class="soleil-toolbar__select">
          <option value="">Tất cả</option>
          <option value="PHIEU_TAM">Phiếu tạm</option>
          <option value="DA_NHAP">Đã nhập</option>
          <option value="DA_HUY">Đã hủy</option>
        </select>
      </div>
      <div class="soleil-toolbar__field soleil-toolbar__field--wide">
        <label class="soleil-toolbar__label">Nhà cung cấp</label>
        <select v-model="filters.idNcc" class="soleil-toolbar__select">
          <option value="">Tất cả</option>
          <option v-for="n in nccOptions" :key="n.id" :value="n.id">{{ n.ten }}</option>
        </select>
      </div>
      <div class="soleil-toolbar__field">
        <label class="soleil-toolbar__label">Từ ngày</label>
        <input v-model="filters.from" type="date" class="soleil-toolbar__input pn-date" />
      </div>
      <div class="soleil-toolbar__field">
        <label class="soleil-toolbar__label">Đến ngày</label>
        <input v-model="filters.to" type="date" class="soleil-toolbar__input pn-date" />
      </div>
      <button type="button" class="soleil-btn-outline" @click="load">
        <Icon icon="icon-park-outline:filter" width="15" />
        Lọc
      </button>
      <button type="button" class="soleil-btn-outline" title="Xóa bộ lọc" @click="resetFilters">
        <Icon icon="icon-park-outline:refresh" width="15" />
      </button>
    </div>

    <div class="soleil-table-card pn-table-card">
      <div class="soleil-table-card__head">
        <span class="pn-table-title">Danh sách phiếu nhập</span>
        <span class="pn-table-meta">{{ rows.length }} phiếu</span>
      </div>

      <div class="overflow-x-auto">
        <table class="soleil-table admin-table--soleil pn-table">
          <thead>
            <tr>
              <th class="soleil-col-text">Mã phiếu</th>
              <th class="soleil-col-text">Ngày nhập</th>
              <th class="soleil-col-text">Nhà cung cấp</th>
              <th class="soleil-col-num">Tổng tiền</th>
              <th class="soleil-col-center">Trạng thái</th>
              <th class="soleil-col-center">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="6" class="pn-empty-cell">Đang tải…</td>
            </tr>
            <tr v-else-if="!rows.length">
              <td colspan="6" class="pn-empty-cell">
                Chưa có phiếu nhập. Bấm «Nhập hàng» để tạo phiếu đầu tiên.
              </td>
            </tr>
            <template v-else>
              <tr
                v-for="row in rows"
                :key="row.id"
                class="pn-row"
                :class="{ 'pn-row--draft': row.trangThai === 'PHIEU_TAM' }"
              >
                <td class="soleil-col-text">
                  <button type="button" class="pn-code" @click="openDetail(row)">
                    {{ row.maPhieuNhap }}
                  </button>
                </td>
                <td class="soleil-col-text">
                  <span class="pn-date-text">{{ formatDate(row.ngayTao) }}</span>
                </td>
                <td class="soleil-col-text">
                  <span class="pn-ncc">{{ row.tenNhaCungCap || '—' }}</span>
                </td>
                <td class="soleil-col-num">
                  <span class="pn-money">{{ formatMoney(row.tongTien) }}</span>
                </td>
                <td class="soleil-col-center">
                  <span class="pn-badge" :class="`pn-badge--${statusTone(row.trangThai)}`">
                    {{ STATUS_LABEL[row.trangThai] || row.trangThai }}
                  </span>
                </td>
                <td class="soleil-col-center">
                  <div class="soleil-actions-cell pn-actions">
                    <button
                      type="button"
                      class="soleil-act-btn"
                      :title="row.trangThai === 'PHIEU_TAM' ? 'Sửa phiếu' : 'Xem phiếu'"
                      @click="openDetail(row)"
                    >
                      <Icon
                        :icon="row.trangThai === 'PHIEU_TAM' ? 'icon-park-outline:edit' : 'icon-park-outline:eyes'"
                        width="16"
                      />
                    </button>
                    <button
                      v-if="row.trangThai === 'PHIEU_TAM'"
                      type="button"
                      class="soleil-act-btn soleil-act-btn--danger"
                      title="Hủy phiếu"
                      @click="onHuy(row)"
                    >
                      <Icon icon="icon-park-outline:close-one" width="16" />
                    </button>
                  </div>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.pn-page {
  --pn-ink: #1a120c;
  --pn-muted: #5c4f42;
  --pn-line: #c9b8a4;
  --pn-line-strong: #a89278;
  --pn-surface: #ffffff;
  --pn-ok: #14532d;
  --pn-ok-bg: #dcfce7;
  --pn-draft: #9a3412;
  --pn-draft-bg: #ffedd5;
  --pn-cancel: #3f3f46;
  --pn-cancel-bg: #e4e4e7;

  display: flex;
  flex-direction: column;
  gap: 0.85rem;
  color: var(--pn-ink);
}

.pn-page :deep(.soleil-page-header) {
  margin: 0;
}

.pn-page :deep(.soleil-page-header__title) {
  font-family: inherit;
  font-size: 1.35rem;
  font-weight: 800;
  font-style: normal;
  letter-spacing: 0.02em;
  color: var(--pn-ink);
}

.pn-page :deep(.soleil-page-header__desc) {
  margin: 0.25rem 0 0;
  font-size: 13px;
  font-weight: 500;
  color: var(--pn-muted);
}

.pn-filters {
  border-color: var(--pn-line) !important;
  box-shadow: 0 1px 0 rgba(26, 18, 12, 0.04);
}

.pn-filters :deep(.soleil-toolbar__label) {
  color: var(--pn-ink);
  font-weight: 700;
}

.pn-filters :deep(.soleil-toolbar__input),
.pn-filters :deep(.soleil-toolbar__select) {
  border-color: var(--pn-line-strong);
  background: #fff;
  color: var(--pn-ink);
  font-weight: 500;
}

.pn-filters :deep(.soleil-toolbar__input:focus),
.pn-filters :deep(.soleil-toolbar__select:focus) {
  border-color: #8b6914;
  background: #fff;
}

.pn-date {
  padding-left: 14px !important;
}

.pn-table-card {
  border-color: var(--pn-line-strong) !important;
}

.pn-table-card :deep(.soleil-table-card__head) {
  border-bottom-color: var(--pn-line);
  background: #fff;
}

.pn-table-title {
  font-size: 14px;
  font-weight: 800;
  color: var(--pn-ink);
}

.pn-table-meta {
  margin-left: auto;
  font-size: 12px;
  font-weight: 600;
  color: var(--pn-muted);
}

.pn-table-card :deep(table.pn-table thead th) {
  background: #8f7349 !important;
  color: #fffef9 !important;
  font-size: 11px !important;
  font-weight: 700 !important;
  letter-spacing: 0.08em !important;
  border-bottom: none !important;
}

.pn-table-card :deep(table.pn-table tbody td) {
  color: var(--pn-ink);
  border-bottom: 1px solid var(--pn-line);
  font-size: 13.5px;
}

.pn-table-card :deep(table.pn-table tbody tr:hover) {
  background: #f7f1e8 !important;
}

.pn-empty-cell {
  text-align: center;
  padding: 2.5rem 1rem !important;
  color: var(--pn-muted);
  font-weight: 500;
}

.pn-row--draft {
  background: #fff7ed;
}

.pn-code {
  border: none;
  background: none;
  padding: 0;
  font-family: ui-monospace, 'Cascadia Mono', monospace;
  font-size: 13.5px;
  font-weight: 800;
  color: #0f4c52;
  cursor: pointer;
  text-decoration: underline;
  text-underline-offset: 2px;
}

.pn-code:hover {
  color: #062f33;
}

.pn-date-text,
.pn-ncc {
  color: var(--pn-ink);
}

.pn-ncc {
  font-weight: 600;
}

.pn-money {
  font-family: ui-monospace, 'Cascadia Mono', monospace;
  font-weight: 800;
  color: var(--pn-ink);
  white-space: nowrap;
}

.pn-badge {
  display: inline-flex;
  align-items: center;
  padding: 0.3rem 0.7rem;
  border-radius: 3px;
  border: 1px solid transparent;
  font-size: 11.5px;
  font-weight: 800;
  letter-spacing: 0.02em;
  white-space: nowrap;
}

.pn-badge--draft {
  background: var(--pn-draft-bg);
  border-color: #fdba74;
  color: var(--pn-draft);
}

.pn-badge--ok {
  background: var(--pn-ok-bg);
  border-color: #86efac;
  color: var(--pn-ok);
}

.pn-badge--muted {
  background: var(--pn-cancel-bg);
  border-color: #a1a1aa;
  color: var(--pn-cancel);
}

.pn-actions {
  justify-content: center;
}

.pn-actions :deep(.soleil-act-btn) {
  border-color: var(--pn-line-strong);
  color: var(--pn-ink);
  background: #fff;
}

.pn-actions :deep(.soleil-act-btn:hover) {
  border-color: #8f7349;
  color: #6b542f;
  background: #f7f1e8;
}

:deep(.soleil-col-center) {
  text-align: center;
}

:deep(.soleil-col-num) {
  text-align: right;
}

.soleil-act-btn--danger {
  color: #991b1b !important;
}

.soleil-act-btn--danger:hover {
  background: #fee2e2 !important;
  border-color: #f87171 !important;
  color: #7f1d1d !important;
}
</style>
