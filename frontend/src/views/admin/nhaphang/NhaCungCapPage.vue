<script setup>
import { onMounted, ref } from 'vue'
import {
  createNhaCungCap,
  deleteNhaCungCap,
  getNhaCungCapList,
  updateNhaCungCap,
} from '@/api/nhapHangApi'
import { toast } from '@/composables/useToast'
import { formatApiError } from '@/utils/apiError'

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const keyword = ref('')

const showForm = ref(false)
const editingId = ref(null)
const formMa = ref('(tự sinh)')
const form = ref({
  ten: '',
  soDienThoai: '',
  email: '',
  diaChi: '',
  ghiChu: '',
})

async function load() {
  loading.value = true
  try {
    const res = await getNhaCungCapList(keyword.value.trim(), false)
    rows.value = res.data || []
  } catch (e) {
    toast(formatApiError(e, 'Không tải được nhà cung cấp'), 'error')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  formMa.value = '(tự sinh khi lưu)'
  form.value = { ten: '', soDienThoai: '', email: '', diaChi: '', ghiChu: '' }
  showForm.value = true
}

function openEdit(row) {
  editingId.value = row.id
  formMa.value = row.ma || '—'
  form.value = {
    ten: row.ten || '',
    soDienThoai: row.soDienThoai || '',
    email: row.email || '',
    diaChi: row.diaChi || '',
    ghiChu: row.ghiChu || '',
  }
  showForm.value = true
}

function closeForm() {
  showForm.value = false
}

async function saveForm() {
  if (!form.value.ten?.trim()) {
    toast('Nhập tên nhà cung cấp', 'warn')
    return
  }
  saving.value = true
  try {
    const payload = {
      ten: form.value.ten.trim(),
      soDienThoai: form.value.soDienThoai || null,
      email: form.value.email || null,
      diaChi: form.value.diaChi || null,
      ghiChu: form.value.ghiChu || null,
    }
    if (editingId.value) {
      await updateNhaCungCap(editingId.value, payload)
      toast('Đã cập nhật nhà cung cấp', 'success')
    } else {
      await createNhaCungCap(payload)
      toast('Đã thêm nhà cung cấp', 'success')
    }
    showForm.value = false
    await load()
  } catch (e) {
    toast(formatApiError(e, 'Không lưu được nhà cung cấp'), 'error')
  } finally {
    saving.value = false
  }
}

async function onDelete(row) {
  if (!row.trangThai) return
  if (!confirm(`Ngừng dùng nhà cung cấp ${row.ma} — ${row.ten}?`)) return
  try {
    await deleteNhaCungCap(row.id)
    toast('Đã ngừng dùng nhà cung cấp', 'success')
    await load()
  } catch (e) {
    toast(formatApiError(e, 'Không xóa được nhà cung cấp'), 'error')
  }
}

onMounted(load)
</script>

<template>
  <div class="ncc-page">
    <div class="ncc-page__head">
      <div>
        <h1 class="ncc-page__title">Nhà cung cấp</h1>
        <p class="ncc-page__sub">Quản lý NCC dùng cho phiếu nhập hàng.</p>
      </div>
      <button type="button" class="admin-btn admin-btn-primary" @click="openCreate">
        ＋ Thêm nhà cung cấp
      </button>
    </div>

    <div class="ncc-filters admin-card">
      <label>
        <span>Tìm theo mã / tên</span>
        <div class="ncc-filters__row">
          <input
            v-model="keyword"
            class="admin-input"
            placeholder="VD: NCC0001 hoặc Công ty…"
            @keyup.enter="load"
          />
          <button type="button" class="admin-btn admin-btn-default" @click="load">Tìm</button>
        </div>
      </label>
    </div>

    <div class="admin-card ncc-table-wrap">
      <div v-if="loading" class="ncc-empty">Đang tải…</div>
      <div v-else-if="!rows.length" class="ncc-empty">
        Chưa có nhà cung cấp. Bấm «＋ Thêm nhà cung cấp» để tạo.
      </div>
      <table v-else class="ncc-table">
        <thead>
          <tr>
            <th>Mã</th>
            <th>Tên</th>
            <th>SĐT</th>
            <th>Email</th>
            <th>Địa chỉ</th>
            <th>Trạng thái</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in rows" :key="row.id">
            <td class="ncc-mono">{{ row.ma }}</td>
            <td class="ncc-ten">{{ row.ten }}</td>
            <td>{{ row.soDienThoai || '—' }}</td>
            <td>{{ row.email || '—' }}</td>
            <td class="ncc-addr">{{ row.diaChi || '—' }}</td>
            <td>
              <span
                class="ncc-badge"
                :class="row.trangThai ? 'ncc-badge--ok' : 'ncc-badge--muted'"
              >
                {{ row.trangThai ? 'Đang dùng' : 'Ngừng' }}
              </span>
            </td>
            <td class="ncc-actions">
              <button type="button" class="admin-btn admin-btn-default" @click="openEdit(row)">
                Sửa
              </button>
              <button
                v-if="row.trangThai"
                type="button"
                class="admin-btn admin-btn-danger"
                @click="onDelete(row)"
              >
                Xóa
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showForm" class="ncc-modal" @click.self="closeForm">
      <div class="ncc-modal__panel">
        <div class="ncc-modal__head">
          <h3>{{ editingId ? 'Sửa nhà cung cấp' : 'Thêm nhà cung cấp' }}</h3>
          <button type="button" class="admin-btn admin-btn-default" @click="closeForm">✕</button>
        </div>

        <label class="ncc-field">
          <span>Mã</span>
          <input class="admin-input" :value="formMa" readonly />
        </label>
        <label class="ncc-field">
          <span>Tên *</span>
          <input v-model="form.ten" class="admin-input" placeholder="Tên nhà cung cấp" />
        </label>
        <label class="ncc-field">
          <span>SĐT</span>
          <input v-model="form.soDienThoai" class="admin-input" />
        </label>
        <label class="ncc-field">
          <span>Email</span>
          <input v-model="form.email" class="admin-input" type="email" />
        </label>
        <label class="ncc-field">
          <span>Địa chỉ</span>
          <input v-model="form.diaChi" class="admin-input" />
        </label>
        <label class="ncc-field">
          <span>Ghi chú</span>
          <input v-model="form.ghiChu" class="admin-input" />
        </label>

        <div class="ncc-modal__actions">
          <button type="button" class="admin-btn admin-btn-default" @click="closeForm">Hủy</button>
          <button type="button" class="admin-btn admin-btn-primary" :disabled="saving" @click="saveForm">
            {{ saving ? 'Đang lưu…' : 'Lưu' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ncc-page {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.ncc-page__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
}

.ncc-page__title {
  margin: 0;
  font-size: 1.4rem;
  font-weight: 700;
  color: var(--admin-text, #1a1814);
}

.ncc-page__sub {
  margin: 0.35rem 0 0;
  font-size: 0.875rem;
  color: var(--admin-muted, #8a7b6a);
}

.ncc-filters {
  padding: 1rem 1.15rem;
}

.ncc-filters label {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--admin-muted, #8a7b6a);
}

.ncc-filters__row {
  display: flex;
  gap: 0.5rem;
  max-width: 480px;
}

.ncc-table-wrap {
  padding: 0;
  overflow: hidden;
}

.ncc-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.875rem;
}

.ncc-table th,
.ncc-table td {
  padding: 0.85rem 1rem;
  text-align: left;
  border-bottom: 1px solid var(--admin-border, #e8dcc8);
  vertical-align: middle;
}

.ncc-table th {
  background: rgba(201, 169, 110, 0.12);
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--admin-muted, #8a7b6a);
}

.ncc-mono {
  font-family: ui-monospace, monospace;
  font-weight: 600;
}

.ncc-ten {
  font-weight: 600;
  color: var(--admin-text, #1a1814);
}

.ncc-addr {
  max-width: 220px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ncc-badge {
  display: inline-flex;
  padding: 0.25rem 0.65rem;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 700;
}

.ncc-badge--ok {
  background: rgba(122, 140, 110, 0.25);
  color: #3d5a34;
}

.ncc-badge--muted {
  background: rgba(138, 123, 106, 0.18);
  color: #6a5c4e;
}

.ncc-actions {
  display: flex;
  gap: 0.4rem;
  flex-wrap: wrap;
}

.ncc-empty {
  padding: 2.5rem 1rem;
  text-align: center;
  color: var(--admin-muted, #8a7b6a);
}

.ncc-modal {
  position: fixed;
  inset: 0;
  background: rgba(36, 26, 18, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 80;
  padding: 1rem;
}

.ncc-modal__panel {
  width: min(440px, 100%);
  max-height: 90vh;
  overflow: auto;
  background: var(--admin-card, #fff);
  border-radius: 14px;
  padding: 1.15rem;
  border: 1px solid var(--admin-border, #e8dcc8);
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.ncc-modal__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.ncc-modal__head h3 {
  margin: 0;
  font-size: 1.05rem;
}

.ncc-field {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--admin-muted, #8a7b6a);
}

.ncc-modal__actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 0.25rem;
}
</style>
