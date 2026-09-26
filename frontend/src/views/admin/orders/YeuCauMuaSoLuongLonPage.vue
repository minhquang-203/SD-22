<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { Icon } from '@iconify/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import {
  demYeuCauMuaSoLuongLonMoi,
  fetchYeuCauMuaSoLuongLon,
  updateYeuCauMuaSoLuongLon,
} from '@/api/yeuCauMuaSoLuongLonApi'
import { confirm } from '@/composables/useConfirm'
import { toast } from '@/composables/useToast'

const STATUS_OPTIONS = [
  { value: '', label: 'Tất cả' },
  { value: 'MOI', label: 'Mới' },
  { value: 'DA_LIEN_HE', label: 'Đã liên hệ' },
  { value: 'HOAN_TAT', label: 'Hoàn tất' },
  { value: 'HUY', label: 'Hủy' },
]

const STATUS_FLOW = {
  MOI: ['DA_LIEN_HE', 'HUY'],
  DA_LIEN_HE: ['HOAN_TAT', 'HUY'],
  HOAN_TAT: [],
  HUY: [],
}

const loading = ref(false)
const rows = ref([])
const filterStatus = ref('')
const page = ref(0)
const size = ref(10)
const totalPages = ref(0)
const totalElements = ref(0)
const moiCount = ref(0)

const detail = ref(null)
const detailOpen = ref(false)
const saving = ref(false)
const editStatus = ref('MOI')
const editNote = ref('')

function statusLabel(tt) {
  return STATUS_OPTIONS.find((o) => o.value === tt)?.label || tt || '—'
}

function statusTone(tt) {
  if (tt === 'MOI') return 'new'
  if (tt === 'DA_LIEN_HE') return 'progress'
  if (tt === 'HOAN_TAT') return 'done'
  if (tt === 'HUY') return 'cancel'
  return ''
}

function formatDateTime(v) {
  if (!v) return '—'
  return new Date(v).toLocaleString('vi-VN')
}

async function loadList() {
  loading.value = true
  try {
    const res = await fetchYeuCauMuaSoLuongLon({
      trangThai: filterStatus.value || undefined,
      page: page.value,
      size: size.value,
    })
    const data = res.data || {}
    rows.value = data.content || []
    totalPages.value = data.totalPages || 0
    totalElements.value = data.totalElements || 0
  } catch (err) {
    toast(typeof err === 'string' ? err : 'Không tải được danh sách')
  } finally {
    loading.value = false
  }
}

async function loadMoiCount() {
  try {
    const res = await demYeuCauMuaSoLuongLonMoi()
    moiCount.value = Number(res.data?.count) || 0
  } catch {
    moiCount.value = 0
  }
}

function openDetail(row) {
  detail.value = row
  editStatus.value = row.trangThai || 'MOI'
  editNote.value = row.ghiChuNoiBo || ''
  detailOpen.value = true
}

function closeDetail() {
  detailOpen.value = false
  detail.value = null
}

const nextStatuses = computed(() => {
  const current = detail.value?.trangThai || 'MOI'
  const allowed = STATUS_FLOW[current] || []
  return STATUS_OPTIONS.filter((o) => o.value && (o.value === current || allowed.includes(o.value)))
})

async function saveDetail() {
  if (!detail.value) return
  const ok = await confirm({
    title: 'Cập nhật yêu cầu',
    message: `Đổi trạng thái sang «${statusLabel(editStatus.value)}»?`,
  })
  if (!ok) return
  saving.value = true
  try {
    const res = await updateYeuCauMuaSoLuongLon(detail.value.id, {
      trangThai: editStatus.value,
      ghiChuNoiBo: editNote.value || null,
    })
    detail.value = res.data
    toast('Đã cập nhật yêu cầu', 'info')
    await loadList()
    await loadMoiCount()
    closeDetail()
  } catch (err) {
    toast(typeof err === 'string' ? err : 'Không cập nhật được')
  } finally {
    saving.value = false
  }
}

watch(filterStatus, () => {
  page.value = 0
  loadList()
})

onMounted(() => {
  loadList()
  loadMoiCount()
})
</script>

<template>
  <div class="bulk-admin">
    <PageHeader
      title="Yêu cầu mua số lượng lớn"
      description="Tiếp nhận và xử lý yêu cầu tư vấn mua sỉ từ cửa hàng"
    >
      <template #actions>
        <span v-if="moiCount" class="bulk-admin__pill">{{ moiCount }} mới</span>
      </template>
    </PageHeader>

    <div class="bulk-admin__toolbar soleil-card">
      <label>
        Trạng thái
        <select v-model="filterStatus" class="admin-input">
          <option v-for="o in STATUS_OPTIONS" :key="o.value || 'all'" :value="o.value">
            {{ o.label }}
          </option>
        </select>
      </label>
      <span class="bulk-admin__total">{{ totalElements }} yêu cầu</span>
    </div>

    <div class="soleil-card bulk-admin__table-wrap">
      <div v-if="loading" class="p-8 text-center text-[#5a6a72]">Đang tải…</div>
      <table v-else class="bulk-admin__table">
        <thead>
          <tr>
            <th>Ngày</th>
            <th>Khách / Công ty</th>
            <th>SĐT</th>
            <th>Email</th>
            <th>Sản phẩm</th>
            <th>SL</th>
            <th>Trạng thái</th>
            <th>Người xử lý</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in rows" :key="row.id">
            <td>{{ formatDateTime(row.ngayTao) }}</td>
            <td>
              <div class="font-medium">{{ row.hoTen }}</div>
              <div v-if="row.tenCongTy" class="text-xs text-[#6b7280]">{{ row.tenCongTy }}</div>
            </td>
            <td>{{ row.soDienThoai }}</td>
            <td>{{ row.email }}</td>
            <td>
              <div>{{ row.tenSanPham || '—' }}</div>
              <div v-if="row.bienThe" class="text-xs text-[#6b7280]">{{ row.bienThe }}</div>
            </td>
            <td class="text-right font-semibold">{{ row.soLuong }}</td>
            <td>
              <span class="bulk-badge" :class="`bulk-badge--${statusTone(row.trangThai)}`">
                {{ statusLabel(row.trangThai) }}
              </span>
            </td>
            <td>{{ row.tenNhanVienXuLy || '—' }}</td>
            <td>
              <button type="button" class="soleil-btn-outline bulk-admin__btn" @click="openDetail(row)">
                Xem
              </button>
            </td>
          </tr>
          <tr v-if="!rows.length">
            <td colspan="9" class="text-center py-10 text-[#6b7280]">Chưa có yêu cầu nào.</td>
          </tr>
        </tbody>
      </table>

      <div v-if="totalPages > 1" class="bulk-admin__pager">
        <button type="button" class="soleil-btn-outline" :disabled="page <= 0" @click="page--; loadList()">
          Trước
        </button>
        <span>Trang {{ page + 1 }} / {{ totalPages }}</span>
        <button
          type="button"
          class="soleil-btn-outline"
          :disabled="page >= totalPages - 1"
          @click="page++; loadList()"
        >
          Sau
        </button>
      </div>
    </div>

    <Teleport to="body">
      <div v-if="detailOpen && detail" class="bulk-admin-modal" @click.self="closeDetail">
        <div class="bulk-admin-modal__panel">
          <div class="bulk-admin-modal__head">
            <h2>Chi tiết yêu cầu #{{ detail.id }}</h2>
            <button type="button" class="admin-icon-btn" @click="closeDetail">
              <Icon icon="mdi:close" width="20" />
            </button>
          </div>
          <div class="bulk-admin-modal__body">
            <p><strong>Ngày:</strong> {{ formatDateTime(detail.ngayTao) }}</p>
            <p><strong>Họ tên:</strong> {{ detail.hoTen }}</p>
            <p v-if="detail.tenCongTy"><strong>Công ty:</strong> {{ detail.tenCongTy }}</p>
            <p><strong>SĐT:</strong> {{ detail.soDienThoai }}</p>
            <p><strong>Email:</strong> {{ detail.email }}</p>
            <p>
              <strong>Sản phẩm:</strong> {{ detail.tenSanPham }}
              <span v-if="detail.bienThe"> · {{ detail.bienThe }}</span>
              · SL {{ detail.soLuong }}
            </p>
            <p v-if="detail.ghiChu"><strong>Ghi chú khách:</strong> {{ detail.ghiChu }}</p>
            <p>
              <strong>Nhận KM:</strong>
              {{ detail.nhanKhuyenMai ? 'Có' : 'Không' }}
            </p>

            <label class="bulk-admin-field">
              Trạng thái
              <select v-model="editStatus" class="admin-input">
                <option v-for="o in nextStatuses" :key="o.value" :value="o.value">{{ o.label }}</option>
              </select>
            </label>
            <label class="bulk-admin-field">
              Ghi chú nội bộ
              <textarea v-model="editNote" class="admin-input" rows="3" />
            </label>
          </div>
          <div class="bulk-admin-modal__foot">
            <button type="button" class="soleil-btn-outline" :disabled="saving" @click="closeDetail">Đóng</button>
            <button type="button" class="soleil-btn-primary" :disabled="saving" @click="saveDetail">
              {{ saving ? 'Đang lưu…' : 'Lưu' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.bulk-admin__pill {
  display: inline-flex;
  align-items: center;
  padding: 0.35rem 0.75rem;
  border-radius: 999px;
  background: rgba(158, 115, 64, 0.15);
  color: #7a5528;
  font-weight: 700;
  font-size: 0.85rem;
}

.bulk-admin__toolbar {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 1rem;
  padding: 1rem 1.1rem;
  margin-bottom: 1rem;
}

.bulk-admin__toolbar label {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  font-size: 0.85rem;
}

.bulk-admin__total {
  color: #6b7280;
  font-size: 0.9rem;
}

.bulk-admin__table-wrap {
  padding: 0;
  overflow: auto;
}

.bulk-admin__table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
}

.bulk-admin__table th,
.bulk-admin__table td {
  padding: 0.75rem 0.85rem;
  border-bottom: 1px solid #eee4d6;
  text-align: left;
  vertical-align: top;
}

.bulk-admin__table th {
  background: #faf6ef;
  font-size: 0.78rem;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: #6b5630;
}

.bulk-admin__btn {
  padding: 0.35rem 0.7rem !important;
  font-size: 0.82rem !important;
}

.bulk-badge {
  display: inline-flex;
  padding: 0.2rem 0.55rem;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 700;
}

.bulk-badge--new {
  background: #fff3d9;
  color: #8a5a12;
}
.bulk-badge--progress {
  background: #e8eef8;
  color: #2f4a7a;
}
.bulk-badge--done {
  background: #e5f3ea;
  color: #1f6b3a;
}
.bulk-badge--cancel {
  background: #f3e8e4;
  color: #8a3b2a;
}

.bulk-admin__pager {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  padding: 0.9rem;
}

.bulk-admin-modal {
  position: fixed;
  inset: 0;
  z-index: 5000;
  background: rgba(30, 21, 16, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
}

.bulk-admin-modal__panel {
  width: 100%;
  max-width: 520px;
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
}

.bulk-admin-modal__head,
.bulk-admin-modal__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  padding: 0.9rem 1rem;
  border-bottom: 1px solid #eee4d6;
}

.bulk-admin-modal__foot {
  border-bottom: none;
  border-top: 1px solid #eee4d6;
  justify-content: flex-end;
}

.bulk-admin-modal__head h2 {
  margin: 0;
  font-size: 1.05rem;
}

.bulk-admin-modal__body {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.55rem;
  font-size: 0.92rem;
}

.bulk-admin-field {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  margin-top: 0.35rem;
}
</style>
