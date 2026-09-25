<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import SearchableSelect from '@/components/ui/SearchableSelect.vue'
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
const hasLoadedOnce = ref(false)

const filters = ref({
  q: '',
  trangThai: '',
  idNcc: null,
  from: '',
  to: '',
})

const STATUS_LABEL = {
  PHIEU_TAM: 'Phiếu tạm',
  DA_NHAP: 'Đã nhập',
  DA_HUY: 'Đã hủy',
}

const nccSelectOptions = computed(() =>
  nccOptions.value.map((n) => ({
    value: n.id,
    label: `${n.ma || ''} — ${n.ten || ''}`.replace(/^ — /, ''),
  })),
)

const hasActiveFilters = computed(() =>
  Boolean(
    filters.value.q?.trim()
      || filters.value.trangThai
      || filters.value.idNcc
      || filters.value.from
      || filters.value.to,
  ),
)

let filterDebounceTimer = null
let loadSeq = 0
let lastRequestKey = ''

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

function buildParams() {
  const params = {}
  const q = filters.value.q?.trim()
  if (q) params.q = q
  if (filters.value.trangThai) params.trangThai = filters.value.trangThai
  if (filters.value.idNcc) params.idNcc = Number(filters.value.idNcc)
  if (filters.value.from) params.from = filters.value.from
  if (filters.value.to) params.to = filters.value.to
  return params
}

function paramsKey(params) {
  return JSON.stringify(params)
}

async function loadNcc() {
  try {
    const res = await getNhaCungCapList('', true)
    nccOptions.value = (res.data || []).filter((n) => n.trangThai !== false)
  } catch {
    nccOptions.value = []
  }
}

/**
 * @param {{ force?: boolean }} opts force=true: bấm Lọc — hủy debounce, gọi lại dù key trùng
 */
async function load(opts = {}) {
  const force = Boolean(opts.force)
  const params = buildParams()
  const key = paramsKey(params)
  if (!force && key === lastRequestKey && hasLoadedOnce.value) return

  if (filterDebounceTimer) {
    clearTimeout(filterDebounceTimer)
    filterDebounceTimer = null
  }

  const seq = ++loadSeq
  lastRequestKey = key
  loading.value = true
  try {
    const res = await getPhieuNhapList(params)
    if (seq !== loadSeq) return
    rows.value = res.data || []
    hasLoadedOnce.value = true
  } catch (e) {
    if (seq !== loadSeq) return
    toast(formatApiError(e, 'Không tải được phiếu nhập'), 'error')
  } finally {
    if (seq === loadSeq) loading.value = false
  }
}

function scheduleLoad(delayMs = 0) {
  if (filterDebounceTimer) clearTimeout(filterDebounceTimer)
  if (delayMs <= 0) {
    void load()
    return
  }
  filterDebounceTimer = setTimeout(() => {
    filterDebounceTimer = null
    void load()
  }, delayMs)
}

function applyFiltersNow() {
  void load({ force: true })
}

function resetFilters() {
  filters.value = { q: '', trangThai: '', idNcc: null, from: '', to: '' }
  lastRequestKey = ''
  void load({ force: true })
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
    lastRequestKey = ''
    await load({ force: true })
  } catch (e) {
    toast(formatApiError(e, 'Không hủy được phiếu'), 'error')
  }
}

watch(
  () => [filters.value.trangThai, filters.value.idNcc, filters.value.from, filters.value.to],
  () => scheduleLoad(0),
)

watch(
  () => filters.value.q,
  () => scheduleLoad(400),
)

onMounted(async () => {
  await loadNcc()
  await load({ force: true })
})

onBeforeUnmount(() => {
  if (filterDebounceTimer) clearTimeout(filterDebounceTimer)
  loadSeq += 1
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
      <div class="soleil-toolbar__field soleil-toolbar__field--wide">
        <label class="soleil-toolbar__label">Tìm kiếm</label>
        <div class="soleil-toolbar__search">
          <Icon icon="icon-park-outline:search" class="soleil-toolbar__search-icon" />
          <input
            v-model="filters.q"
            class="soleil-toolbar__input"
            type="search"
            placeholder="Mã phiếu hoặc số HĐ…"
            @keydown.enter.prevent="applyFiltersNow"
          />
        </div>
      </div>
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
        <SearchableSelect
          v-model="filters.idNcc"
          class="pn-ncc-select"
          :options="nccSelectOptions"
          placeholder="Tất cả NCC"
        />
      </div>
      <div class="soleil-toolbar__field">
        <label class="soleil-toolbar__label">Từ ngày</label>
        <input v-model="filters.from" type="date" class="soleil-toolbar__input pn-date" />
      </div>
      <div class="soleil-toolbar__field">
        <label class="soleil-toolbar__label">Đến ngày</label>
        <input v-model="filters.to" type="date" class="soleil-toolbar__input pn-date" />
      </div>
      <button type="button" class="soleil-btn-outline" @click="applyFiltersNow">
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
                <template v-if="hasActiveFilters">
                  Không có phiếu nhập phù hợp.
                </template>
                <template v-else>
                  Chưa có phiếu nhập. Bấm «Nhập hàng» để tạo phiếu đầu tiên.
                </template>
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
  --pn-mist: #f3ebe1;

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
  flex-wrap: wrap;
  align-items: flex-end;
}

.pn-ncc-select {
  width: 100%;
  min-width: 0;
}

.pn-date {
  min-width: 9.5rem;
}

.pn-table-card {
  border-color: var(--pn-line-strong);
}

.pn-table-title {
  font-size: 14px;
  font-weight: 800;
}

.pn-table-meta {
  margin-left: auto;
  font-size: 12px;
  font-weight: 600;
  color: var(--pn-muted);
}

.pn-empty-cell {
  text-align: center;
  padding: 2.5rem 1rem !important;
  color: var(--pn-muted);
}

.pn-code {
  border: 0;
  background: none;
  padding: 0;
  font-family: ui-monospace, 'Cascadia Mono', monospace;
  font-weight: 800;
  color: #0f4c52;
  cursor: pointer;
}

.pn-code:hover {
  text-decoration: underline;
}

.pn-date-text,
.pn-ncc {
  font-weight: 600;
}

.pn-money {
  font-weight: 800;
  font-variant-numeric: tabular-nums;
}

.pn-badge {
  display: inline-flex;
  align-items: center;
  padding: 0.2rem 0.55rem;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 800;
}

.pn-badge--ok {
  color: var(--pn-ok);
  background: var(--pn-ok-bg);
}

.pn-badge--draft {
  color: var(--pn-draft);
  background: var(--pn-draft-bg);
}

.pn-badge--muted {
  color: var(--pn-cancel);
  background: var(--pn-cancel-bg);
}

.pn-actions {
  justify-content: center;
}

.soleil-act-btn--danger {
  color: #991b1b;
}

.soleil-act-btn--danger:hover {
  background: #fdecec;
  border-color: #f5c2c2;
}

.pn-page :deep(table.admin-table--soleil thead th) {
  background: #8f7349 !important;
  color: #fffef9 !important;
}
</style>
