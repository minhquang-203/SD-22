<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
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

function statusClass(st) {
  if (st === 'DA_NHAP') return 'pn-badge--ok'
  if (st === 'DA_HUY') return 'pn-badge--muted'
  return 'pn-badge--draft'
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
    <div class="pn-page__head">
      <div>
        <h1 class="pn-page__title">Nhập hàng</h1>
        <p class="pn-page__sub">Phiếu nhập kho từ nhà cung cấp — mỗi dòng sinh một lô khi hoàn thành.</p>
      </div>
      <button type="button" class="admin-btn admin-btn-primary" @click="openCreate">
        ＋ Nhập hàng
      </button>
    </div>

    <div class="pn-filters admin-card">
      <div class="pn-filters__grid">
        <label>
          <span>Trạng thái</span>
          <select v-model="filters.trangThai" class="admin-select">
            <option value="">Tất cả</option>
            <option value="PHIEU_TAM">Phiếu tạm</option>
            <option value="DA_NHAP">Đã nhập</option>
            <option value="DA_HUY">Đã hủy</option>
          </select>
        </label>
        <label>
          <span>Nhà cung cấp</span>
          <select v-model="filters.idNcc" class="admin-select">
            <option value="">Tất cả</option>
            <option v-for="n in nccOptions" :key="n.id" :value="n.id">{{ n.ten }}</option>
          </select>
        </label>
        <label>
          <span>Từ ngày</span>
          <input v-model="filters.from" type="date" class="admin-input" />
        </label>
        <label>
          <span>Đến ngày</span>
          <input v-model="filters.to" type="date" class="admin-input" />
        </label>
        <div class="pn-filters__actions">
          <button type="button" class="admin-btn admin-btn-default" @click="load">Lọc</button>
        </div>
      </div>
    </div>

    <div class="admin-card pn-table-wrap">
      <div v-if="loading" class="pn-empty">Đang tải…</div>
      <div v-else-if="!rows.length" class="pn-empty">
        Chưa có phiếu nhập. Bấm «＋ Nhập hàng» để tạo phiếu đầu tiên.
      </div>
      <table v-else class="pn-table">
        <thead>
          <tr>
            <th>Mã phiếu</th>
            <th>Ngày nhập</th>
            <th>Nhà cung cấp</th>
            <th>Tổng tiền</th>
            <th>Trạng thái</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in rows" :key="row.id">
            <td class="pn-mono">{{ row.maPhieuNhap }}</td>
            <td>{{ formatDate(row.ngayTao) }}</td>
            <td>{{ row.tenNhaCungCap || '—' }}</td>
            <td class="pn-money">{{ formatMoney(row.tongTien) }}</td>
            <td>
              <span class="pn-badge" :class="statusClass(row.trangThai)">
                {{ STATUS_LABEL[row.trangThai] || row.trangThai }}
              </span>
            </td>
            <td class="pn-actions">
              <button type="button" class="admin-btn admin-btn-default" @click="openDetail(row)">
                {{ row.trangThai === 'PHIEU_TAM' ? 'Sửa' : 'Xem' }}
              </button>
              <button
                v-if="row.trangThai === 'PHIEU_TAM'"
                type="button"
                class="admin-btn admin-btn-danger"
                @click="onHuy(row)"
              >
                Hủy
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.pn-page {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.pn-page__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
}

.pn-page__title {
  margin: 0;
  font-size: 1.4rem;
  font-weight: 700;
  color: var(--admin-text, #1a1814);
}

.pn-page__sub {
  margin: 0.35rem 0 0;
  font-size: 0.875rem;
  color: var(--admin-muted, #8a7b6a);
}

.pn-filters {
  padding: 1rem 1.15rem;
}

.pn-filters__grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 0.75rem;
  align-items: end;
}

.pn-filters label {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--admin-muted, #8a7b6a);
}

.pn-filters__actions {
  display: flex;
  align-items: flex-end;
}

.pn-table-wrap {
  padding: 0;
  overflow: hidden;
}

.pn-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.875rem;
}

.pn-table th,
.pn-table td {
  padding: 0.85rem 1rem;
  text-align: left;
  border-bottom: 1px solid var(--admin-border, #e8dcc8);
}

.pn-table th {
  background: rgba(201, 169, 110, 0.12);
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--admin-muted, #8a7b6a);
}

.pn-mono {
  font-family: ui-monospace, monospace;
  font-weight: 600;
}

.pn-money {
  font-weight: 600;
  color: var(--admin-text, #1a1814);
}

.pn-badge {
  display: inline-flex;
  padding: 0.25rem 0.65rem;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 700;
}

.pn-badge--draft {
  background: rgba(201, 169, 110, 0.25);
  color: #7a5a28;
}

.pn-badge--ok {
  background: rgba(122, 140, 110, 0.25);
  color: #3d5a34;
}

.pn-badge--muted {
  background: rgba(138, 123, 106, 0.18);
  color: #6a5c4e;
}

.pn-actions {
  display: flex;
  gap: 0.4rem;
  flex-wrap: wrap;
}

.pn-empty {
  padding: 2.5rem 1rem;
  text-align: center;
  color: var(--admin-muted, #8a7b6a);
}

@media (max-width: 960px) {
  .pn-filters__grid {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
