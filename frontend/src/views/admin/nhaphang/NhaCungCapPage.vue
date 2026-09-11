<script setup>
import { onMounted, ref } from 'vue'
import { Icon } from '@iconify/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import {
  createNhaCungCap,
  deleteNhaCungCap,
  getNhaCungCapList,
  updateNhaCungCap,
} from '@/api/nhapHangApi'
import { toast } from '@/composables/useToast'
import { confirm } from '@/composables/useConfirm'
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
  const ok = await confirm({
    title: 'Ngừng dùng nhà cung cấp',
    message: `Ngừng dùng nhà cung cấp ${row.ma} — ${row.ten}?`,
    confirmText: 'Ngừng dùng',
    danger: true,
  })
  if (!ok) return
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
      <PageHeader
        title="Nhà cung cấp"
        description="Quản lý NCC dùng cho phiếu nhập hàng"
      >
        <template #actions>
          <button type="button" class="soleil-btn-primary" @click="openCreate">
            <Icon icon="icon-park-outline:plus" width="16" />
            Thêm nhà cung cấp
          </button>
        </template>
      </PageHeader>
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
            placeholder="Mã hoặc tên nhà cung cấp…"
            @keyup.enter="load"
          />
        </div>
      </div>
      <button type="button" class="soleil-btn-outline" @click="load">
        <Icon icon="icon-park-outline:search" width="15" />
        Tìm
      </button>
    </div>

    <div class="soleil-table-card">
      <div class="soleil-table-card__head">
        <span class="ncc-table-title">Danh sách nhà cung cấp</span>
        <span class="ncc-table-meta">{{ rows.length }} NCC</span>
      </div>

      <div class="overflow-x-auto">
        <table class="soleil-table admin-table--soleil">
          <thead>
            <tr>
              <th class="soleil-col-text">Mã</th>
              <th class="soleil-col-text">Tên</th>
              <th class="soleil-col-text">SĐT</th>
              <th class="soleil-col-text">Email</th>
              <th class="soleil-col-text">Địa chỉ</th>
              <th class="soleil-col-center">Trạng thái</th>
              <th class="soleil-col-center">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="7" class="ncc-empty-cell">Đang tải…</td>
            </tr>
            <tr v-else-if="!rows.length">
              <td colspan="7" class="ncc-empty-cell">
                Chưa có nhà cung cấp. Bấm «Thêm nhà cung cấp» để tạo.
              </td>
            </tr>
            <template v-else>
              <tr v-for="row in rows" :key="row.id">
                <td class="soleil-col-text">
                  <span class="ncc-mono">{{ row.ma }}</span>
                </td>
                <td class="soleil-col-text">
                  <span class="ncc-ten">{{ row.ten }}</span>
                </td>
                <td class="soleil-col-text">{{ row.soDienThoai || '—' }}</td>
                <td class="soleil-col-text">{{ row.email || '—' }}</td>
                <td class="soleil-col-text">
                  <span class="ncc-addr" :title="row.diaChi || ''">{{ row.diaChi || '—' }}</span>
                </td>
                <td class="soleil-col-center">
                  <span
                    class="ncc-badge"
                    :class="row.trangThai ? 'ncc-badge--ok' : 'ncc-badge--muted'"
                  >
                    {{ row.trangThai ? 'Đang dùng' : 'Ngừng' }}
                  </span>
                </td>
                <td class="soleil-col-center">
                  <div class="soleil-actions-cell ncc-actions">
                    <button
                      type="button"
                      class="soleil-act-btn"
                      title="Sửa"
                      @click="openEdit(row)"
                    >
                      <Icon icon="icon-park-outline:edit" width="16" />
                    </button>
                    <button
                      v-if="row.trangThai"
                      type="button"
                      class="soleil-act-btn soleil-act-btn--danger"
                      title="Ngừng dùng"
                      @click="onDelete(row)"
                    >
                      <Icon icon="icon-park-outline:delete" width="16" />
                    </button>
                  </div>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="showForm" class="ncc-modal" @click.self="closeForm">
      <div class="ncc-modal__panel">
        <div class="ncc-modal__head">
          <div>
            <h3>{{ editingId ? 'Sửa nhà cung cấp' : 'Thêm nhà cung cấp' }}</h3>
            <p>{{ editingId ? 'Cập nhật thông tin NCC' : 'Tạo mới NCC cho phiếu nhập' }}</p>
          </div>
          <button type="button" class="soleil-btn-outline ncc-icon-btn" @click="closeForm">
            <Icon icon="icon-park-outline:close" width="15" />
          </button>
        </div>

        <label class="ncc-field">
          <span>Mã</span>
          <input class="ncc-control" :value="formMa" readonly />
        </label>
        <label class="ncc-field">
          <span>Tên *</span>
          <input v-model="form.ten" class="ncc-control" placeholder="Tên nhà cung cấp" />
        </label>
        <label class="ncc-field">
          <span>SĐT</span>
          <input v-model="form.soDienThoai" class="ncc-control" />
        </label>
        <label class="ncc-field">
          <span>Email</span>
          <input v-model="form.email" class="ncc-control" type="email" />
        </label>
        <label class="ncc-field">
          <span>Địa chỉ</span>
          <input v-model="form.diaChi" class="ncc-control" />
        </label>
        <label class="ncc-field">
          <span>Ghi chú</span>
          <input v-model="form.ghiChu" class="ncc-control" />
        </label>

        <div class="ncc-modal__actions">
          <button type="button" class="soleil-btn-outline" @click="closeForm">Hủy</button>
          <button
            type="button"
            class="soleil-btn-primary"
            :disabled="saving"
            @click="saveForm"
          >
            {{ saving ? 'Đang lưu…' : 'Lưu' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ncc-page {
  --ncc-ink: #1a120c;
  --ncc-muted: #5c4f42;
  --ncc-line: #c9b8a4;
  --ncc-line-strong: #a89278;
  --ncc-mist: #f3ebe1;
  --ncc-ok: #14532d;
  --ncc-ok-bg: #dcfce7;
  --ncc-cancel: #3f3f46;
  --ncc-cancel-bg: #e4e4e7;

  display: flex;
  flex-direction: column;
  gap: 0.85rem;
  color: var(--ncc-ink);
}

.ncc-page__head :deep(.soleil-page-header) {
  margin: 0;
}

.ncc-page__head :deep(.soleil-page-header__title) {
  font-family: inherit;
  font-size: 1.35rem;
  font-weight: 800;
  font-style: normal;
  letter-spacing: 0.02em;
  color: var(--ncc-ink);
}

.ncc-page :deep(.soleil-page-header__desc) {
  margin: 0.25rem 0 0;
  font-size: 13px;
  font-weight: 500;
  color: var(--ncc-muted);
}

.ncc-page :deep(.soleil-toolbar) {
  border-color: var(--ncc-line);
}

.ncc-page :deep(.soleil-toolbar__label) {
  color: var(--ncc-ink);
  font-weight: 700;
}

.ncc-page :deep(.soleil-toolbar__input) {
  border-color: var(--ncc-line-strong);
  background: #fff;
  color: var(--ncc-ink);
  font-weight: 500;
}

.ncc-page :deep(.soleil-table-card) {
  border-color: var(--ncc-line-strong);
}

.ncc-page :deep(table.admin-table--soleil thead th) {
  background: #8f7349 !important;
  color: #fffef9 !important;
  font-size: 11px !important;
  font-weight: 700 !important;
  letter-spacing: 0.08em !important;
  border-bottom: none !important;
}

.ncc-page :deep(table.admin-table--soleil tbody td) {
  color: var(--ncc-ink);
  border-bottom: 1px solid var(--ncc-line);
  font-size: 13.5px;
}

.ncc-table-title {
  font-size: 14px;
  font-weight: 800;
}

.ncc-table-meta {
  margin-left: auto;
  font-size: 12px;
  font-weight: 600;
  color: var(--ncc-muted);
}

.ncc-empty-cell {
  text-align: center;
  padding: 2.5rem 1rem !important;
  color: var(--ncc-muted);
}

.ncc-mono {
  font-family: ui-monospace, 'Cascadia Mono', monospace;
  font-weight: 800;
  color: #0f4c52;
}

.ncc-ten {
  font-weight: 700;
}

.ncc-addr {
  display: inline-block;
  max-width: 220px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ncc-badge {
  display: inline-flex;
  padding: 0.3rem 0.7rem;
  border-radius: 3px;
  border: 1px solid transparent;
  font-size: 11.5px;
  font-weight: 800;
}

.ncc-badge--ok {
  background: var(--ncc-ok-bg);
  border-color: #86efac;
  color: var(--ncc-ok);
}

.ncc-badge--muted {
  background: var(--ncc-cancel-bg);
  border-color: #a1a1aa;
  color: var(--ncc-cancel);
}

.ncc-actions {
  justify-content: center;
}

:deep(.soleil-col-center) {
  text-align: center;
}

.soleil-act-btn--danger {
  color: #991b1b;
}

.soleil-act-btn--danger:hover {
  background: #fdecec;
  border-color: #f5c2c2;
}

.ncc-modal {
  position: fixed;
  inset: 0;
  background: rgba(15, 26, 28, 0.45);
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
  background: #fff;
  border-radius: 14px;
  padding: 1.15rem;
  border: 1px solid var(--ncc-line);
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  box-shadow: 0 16px 40px rgba(15, 26, 28, 0.18);
}

.ncc-modal__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 0.75rem;
}

.ncc-modal__head h3 {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 700;
}

.ncc-modal__head p {
  margin: 0.25rem 0 0;
  font-size: 0.8125rem;
  color: var(--ncc-muted);
}

.ncc-icon-btn {
  padding: 0.55rem 0.7rem !important;
}

.ncc-control {
  width: 100%;
  border: 1px solid var(--ncc-line-strong);
  border-radius: 8px;
  padding: 0.6rem 0.75rem;
  font-size: 0.875rem;
  font-weight: 600;
  text-transform: none;
  letter-spacing: 0;
  color: var(--ncc-ink);
  background: #fff;
  outline: none;
}

.ncc-control:focus {
  border-color: #8f7349;
  background: #fff;
  box-shadow: 0 0 0 2px rgba(143, 115, 73, 0.18);
}

.ncc-field {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: #4a3f34;
}

.ncc-modal__actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 0.35rem;
}
</style>
