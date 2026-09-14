<script setup>
import { computed, onMounted, ref } from 'vue'
import { Icon } from '@iconify/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatusDot from '@/components/ui/StatusDot.vue'
import {
  getNhanVienDanhSach,
  createNhanVien,
  updateNhanVien,
  updateNhanVienStatus,
  datLaiMatKhauNhanVien,
} from '@/api/nhanVienApi'
import { ADMIN_ROLES } from '@/constants/adminMenu'
import {
  assignableRoles,
  canManageStaff,
  getRoleLabel,
  STAFF_ACTION_DENIED,
} from '@/utils/adminAuth'
import { useAdminAuth } from '@/composables/useAdminAuth'
import { confirm } from '@/composables/useConfirm'
import { toast } from '@/composables/useToast'

const { vaiTro: currentRole, nhanVienId: currentStaffId } = useAdminAuth()

const loading = ref(false)
const saving = ref(false)
const loadError = ref('')
const staffList = ref([])
const keyword = ref('')
const showForm = ref(false)
const editingId = ref(null)
const showResetPw = ref(false)
const resetTarget = ref(null)
const resetMatKhau = ref('')
const showResetPwVisible = ref(false)
const resetSaving = ref(false)
const generatedTempPw = ref('')

const emptyForm = () => ({
  hoTen: '',
  email: '',
  soDienThoai: '',
  matKhau: '',
  maVaiTro: ADMIN_ROLES.NHAN_VIEN,
  gioiTinh: 'Khac',
  ngayVaoLam: '',
})

const form = ref(emptyForm())

const filteredStaff = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return staffList.value
  return staffList.value.filter((item) => {
    const haystack = [item.hoTen, item.email, item.soDienThoai, item.maNhanVien, item.maVaiTro]
      .filter(Boolean)
      .join(' ')
      .toLowerCase()
    return haystack.includes(kw)
  })
})

const formTitle = computed(() => (editingId.value ? 'Sửa nhân viên' : 'Thêm nhân viên'))

const creatableRoles = computed(() => assignableRoles(currentRole.value))

const canAddStaff = computed(() => creatableRoles.value.length > 0)

function canActOn(item) {
  return canManageStaff(currentRole.value, currentStaffId.value, item)
}

async function loadStaff() {
  loading.value = true
  loadError.value = ''
  try {
    const res = await getNhanVienDanhSach()
    staffList.value = Array.isArray(res.data) ? res.data : []
  } catch (err) {
    loadError.value = String(err)
    staffList.value = []
    toast(loadError.value, 'warn')
  } finally {
    loading.value = false
  }
}

function roleBadgeClass(maVaiTro) {
  if (maVaiTro === ADMIN_ROLES.CHU) return 'staff-role-badge--owner'
  if (maVaiTro === ADMIN_ROLES.QUAN_LY) return 'staff-role-badge--manager'
  return 'staff-role-badge--staff'
}

function openCreate() {
  editingId.value = null
  const roles = creatableRoles.value
  form.value = {
    ...emptyForm(),
    maVaiTro: roles[0] ?? ADMIN_ROLES.NHAN_VIEN,
  }
  showForm.value = true
}

function openEdit(item) {
  if (!canActOn(item)) return
  editingId.value = item.id
  form.value = {
    hoTen: item.hoTen || '',
    email: item.email || '',
    soDienThoai: item.soDienThoai || '',
    matKhau: '',
    maVaiTro: item.maVaiTro || ADMIN_ROLES.NHAN_VIEN,
    gioiTinh: item.gioiTinh || 'Khac',
    ngayVaoLam: item.ngayVaoLam || '',
  }
  showForm.value = true
}

function closeForm() {
  showForm.value = false
  editingId.value = null
  form.value = emptyForm()
}

function openResetPassword(item) {
  if (!canActOn(item)) return
  resetTarget.value = item
  resetMatKhau.value = ''
  generatedTempPw.value = ''
  showResetPwVisible.value = false
  showResetPw.value = true
}

function closeResetPassword() {
  showResetPw.value = false
  resetTarget.value = null
  resetMatKhau.value = ''
  generatedTempPw.value = ''
  showResetPwVisible.value = false
}

async function handleResetPassword() {
  if (!resetTarget.value) return
  if (!resetMatKhau.value || resetMatKhau.value.length < 6) {
    toast('Mật khẩu mới tối thiểu 6 ký tự', 'warn')
    return
  }

  const ok = await confirm({
    title: 'Đặt lại mật khẩu',
    message: `Xác nhận đặt lại mật khẩu cho "${resetTarget.value.hoTen}"?`,
    confirmText: 'Xác nhận',
  })
  if (!ok) return

  resetSaving.value = true
  try {
    const res = await datLaiMatKhauNhanVien(resetTarget.value.id, {
      matKhauMoi: resetMatKhau.value,
      sinhTuDong: false,
    })
    toast(res.data?.message || `Đã đặt lại mật khẩu cho ${resetTarget.value.hoTen}`, 'info')
    closeResetPassword()
  } catch (err) {
    toast(String(err), 'warn')
  } finally {
    resetSaving.value = false
  }
}

async function handleGenerateTempPassword() {
  if (!resetTarget.value) return

  const ok = await confirm({
    title: 'Sinh mật khẩu tạm',
    message: `Hệ thống sẽ tạo mật khẩu ngẫu nhiên cho "${resetTarget.value.hoTen}". Mật khẩu chỉ hiển thị một lần.`,
    confirmText: 'Sinh mật khẩu',
  })
  if (!ok) return

  resetSaving.value = true
  try {
    const res = await datLaiMatKhauNhanVien(resetTarget.value.id, {
      sinhTuDong: true,
    })
    generatedTempPw.value = res.data?.matKhauTam || ''
    resetMatKhau.value = ''
    toast(res.data?.message || `Đã đặt lại mật khẩu cho ${resetTarget.value.hoTen}`, 'info')
  } catch (err) {
    toast(String(err), 'warn')
  } finally {
    resetSaving.value = false
  }
}

async function copyTempPassword() {
  if (!generatedTempPw.value) return
  try {
    await navigator.clipboard.writeText(generatedTempPw.value)
    toast('Đã sao chép mật khẩu tạm', 'info')
  } catch {
    toast('Không sao chép được — hãy chọn và copy thủ công', 'warn')
  }
}

async function handleSubmit() {
  if (!form.value.hoTen.trim() || !form.value.email.trim() || !form.value.soDienThoai.trim()) {
    toast('Vui lòng nhập đầy đủ thông tin bắt buộc', 'warn')
    return
  }
  if (!editingId.value && (!form.value.matKhau || form.value.matKhau.length < 6)) {
    toast('Mật khẩu tối thiểu 6 ký tự', 'warn')
    return
  }

  saving.value = true
  try {
    const payload = {
      hoTen: form.value.hoTen.trim(),
      email: form.value.email.trim(),
      soDienThoai: form.value.soDienThoai.trim(),
      maVaiTro: form.value.maVaiTro,
      gioiTinh: form.value.gioiTinh || 'Khac',
      ngayVaoLam: form.value.ngayVaoLam || null,
    }

    if (editingId.value) {
      await updateNhanVien(editingId.value, payload)
      toast('Đã cập nhật nhân viên', 'info')
    } else {
      await createNhanVien({ ...payload, matKhau: form.value.matKhau })
      toast('Đã thêm nhân viên mới', 'info')
    }

    closeForm()
    await loadStaff()
  } catch (err) {
    toast(String(err), 'warn')
  } finally {
    saving.value = false
  }
}

async function handleToggleStatus(item) {
  if (!canActOn(item)) return
  const isActive = item.trangThai !== false
  const action = isActive ? 'khóa' : 'mở khóa'
  const ok = await confirm({
    title: isActive ? 'Khóa nhân viên' : 'Mở khóa nhân viên',
    message: `Bạn có chắc muốn ${action} nhân viên "${item.hoTen}"?`,
    confirmText: isActive ? 'Khóa' : 'Mở khóa',
    danger: isActive,
  })
  if (!ok) return

  try {
    await updateNhanVienStatus(item.id, !isActive)
    toast(isActive ? 'Đã khóa nhân viên' : 'Đã mở khóa nhân viên', 'info')
    await loadStaff()
  } catch (err) {
    toast(String(err), 'warn')
  }
}

onMounted(() => {
  loadStaff()
})
</script>

<template>
  <div class="staff-page">
    <div class="staff-page__head">
      <PageHeader
        title="Quản lý nhân viên"
        :description="`SUNOVA — ${filteredStaff.length} nhân viên`"
      >
        <template #actions>
          <button
            v-if="canAddStaff"
            type="button"
            class="soleil-btn-primary"
            @click="openCreate"
          >
            <Icon icon="icon-park-outline:plus" width="16" />
            Thêm nhân viên
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
            type="search"
            class="soleil-toolbar__input"
            placeholder="Tên, email, SĐT, mã NV…"
          />
        </div>
      </div>
      <button type="button" class="soleil-btn-outline staff-reload" @click="loadStaff">
        <Icon icon="icon-park-outline:refresh" width="15" />
        Tải lại
      </button>
    </div>

    <div class="soleil-table-card">
      <div class="soleil-table-card__head">
        <span class="staff-table-title">Danh sách nhân viên</span>
        <span class="staff-table-meta">{{ filteredStaff.length }} nhân viên</span>
      </div>

      <div class="overflow-x-auto">
        <table class="soleil-table admin-table--soleil">
          <thead>
            <tr>
              <th class="soleil-col-text">Mã NV</th>
              <th class="soleil-col-text">Họ tên</th>
              <th class="soleil-col-text">Email</th>
              <th class="soleil-col-text">SĐT</th>
              <th class="soleil-col-text">Vai trò</th>
              <th class="soleil-col-center">Trạng thái</th>
              <th class="soleil-col-center">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="7" class="staff-empty-cell">Đang tải…</td>
            </tr>
            <tr v-else-if="loadError">
              <td colspan="7" class="staff-empty-cell">
                <p class="staff-empty-msg">{{ loadError }}</p>
                <button type="button" class="soleil-btn-outline" @click="loadStaff">Thử lại</button>
              </td>
            </tr>
            <tr v-else-if="!filteredStaff.length">
              <td colspan="7" class="staff-empty-cell">
                Chưa có nhân viên nào trong hệ thống
              </td>
            </tr>
            <template v-else>
              <tr v-for="item in filteredStaff" :key="item.id">
                <td class="soleil-col-text">
                  <span class="staff-mono">{{ item.maNhanVien || '—' }}</span>
                </td>
                <td class="soleil-col-text">
                  <span class="staff-name">{{ item.hoTen }}</span>
                </td>
                <td class="soleil-col-text">{{ item.email || '—' }}</td>
                <td class="soleil-col-text">{{ item.soDienThoai || '—' }}</td>
                <td class="soleil-col-text">
                  <span class="staff-role-badge" :class="roleBadgeClass(item.maVaiTro)">
                    {{ getRoleLabel(item.maVaiTro) }}
                  </span>
                </td>
                <td class="soleil-col-center">
                  <StatusDot
                    :status="item.trangThai !== false ? 'active' : 'paused'"
                    :label="item.trangThai !== false ? 'Hoạt động' : 'Đã khóa'"
                  />
                </td>
                <td class="soleil-col-center">
                  <div class="soleil-actions-cell staff-actions">
                    <button
                      type="button"
                      class="soleil-act-btn"
                      :disabled="!canActOn(item)"
                      :title="canActOn(item) ? 'Sửa nhân viên' : STAFF_ACTION_DENIED"
                      @click="openEdit(item)"
                    >
                      <Icon icon="icon-park-outline:edit" width="16" />
                    </button>
                    <button
                      type="button"
                      class="soleil-act-btn"
                      :disabled="!canActOn(item)"
                      :title="canActOn(item) ? 'Đặt lại mật khẩu' : STAFF_ACTION_DENIED"
                      @click="openResetPassword(item)"
                    >
                      <Icon icon="icon-park-outline:lock" width="16" />
                    </button>
                    <button
                      type="button"
                      class="soleil-act-btn"
                      :class="{ 'soleil-act-btn--danger': item.trangThai !== false }"
                      :disabled="!canActOn(item)"
                      :title="canActOn(item) ? (item.trangThai !== false ? 'Khóa nhân viên' : 'Mở khóa nhân viên') : STAFF_ACTION_DENIED"
                      @click="handleToggleStatus(item)"
                    >
                      <Icon
                        :icon="item.trangThai !== false ? 'icon-park-outline:forbid' : 'icon-park-outline:unlock'"
                        width="16"
                      />
                    </button>
                  </div>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="showForm" class="staff-modal" @click.self="closeForm">
      <div class="staff-modal__panel">
        <div class="staff-modal__head">
          <div>
            <h3>{{ formTitle }}</h3>
            <p>{{ editingId ? 'Cập nhật thông tin nhân viên' : 'Tạo tài khoản nhân viên mới' }}</p>
          </div>
          <button type="button" class="soleil-btn-outline staff-icon-btn" aria-label="Đóng" @click="closeForm">
            <Icon icon="icon-park-outline:close" width="15" />
          </button>
        </div>

        <form class="staff-modal__form" @submit.prevent="handleSubmit">
          <label class="staff-field">
            <span>Họ tên *</span>
            <input v-model="form.hoTen" type="text" class="staff-control" />
          </label>

          <div class="staff-field-grid">
            <label class="staff-field">
              <span>Email *</span>
              <input v-model="form.email" type="email" class="staff-control" />
            </label>
            <label class="staff-field">
              <span>Số điện thoại *</span>
              <input v-model="form.soDienThoai" type="text" class="staff-control" />
            </label>
          </div>

          <label v-if="!editingId" class="staff-field">
            <span>Mật khẩu *</span>
            <input
              v-model="form.matKhau"
              type="password"
              class="staff-control"
              placeholder="Tối thiểu 6 ký tự"
            />
          </label>

          <div class="staff-field-grid">
            <label class="staff-field">
              <span>Vai trò *</span>
              <select v-model="form.maVaiTro" class="staff-control">
                <option
                  v-for="role in creatableRoles"
                  :key="role"
                  :value="role"
                >
                  {{ getRoleLabel(role) }}
                </option>
              </select>
            </label>
            <label class="staff-field">
              <span>Ngày vào làm</span>
              <input v-model="form.ngayVaoLam" type="date" class="staff-control" />
            </label>
          </div>

          <div class="staff-modal__actions">
            <button type="button" class="soleil-btn-outline" @click="closeForm">Hủy</button>
            <button type="submit" class="soleil-btn-primary" :disabled="saving">
              {{ saving ? 'Đang lưu…' : 'Lưu' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <div v-if="showResetPw && resetTarget" class="staff-modal" @click.self="closeResetPassword">
      <div class="staff-modal__panel">
        <div class="staff-modal__head">
          <div>
            <h3>Đặt lại mật khẩu</h3>
            <p>{{ resetTarget.hoTen }} · {{ resetTarget.email }}</p>
          </div>
          <button type="button" class="soleil-btn-outline staff-icon-btn" aria-label="Đóng" @click="closeResetPassword">
            <Icon icon="icon-park-outline:close" width="15" />
          </button>
        </div>

        <div class="staff-modal__form">
          <div v-if="generatedTempPw" class="staff-temp-pw-box">
            <p class="staff-temp-pw-box__label">Mật khẩu tạm (chỉ hiển thị một lần)</p>
            <div class="staff-temp-pw-box__row">
              <code class="staff-temp-pw-box__code">{{ generatedTempPw }}</code>
              <button type="button" class="soleil-btn-outline" @click="copyTempPassword">
                Sao chép
              </button>
            </div>
            <p class="staff-temp-pw-box__hint">
              Hãy đưa mật khẩu này cho nhân viên và yêu cầu đổi sau khi đăng nhập.
            </p>
            <button type="button" class="soleil-btn-primary staff-temp-close" @click="closeResetPassword">
              Đóng
            </button>
          </div>

          <template v-else>
            <label class="staff-field">
              <span>Mật khẩu mới</span>
              <div class="staff-password">
                <input
                  v-model="resetMatKhau"
                  :type="showResetPwVisible ? 'text' : 'password'"
                  class="staff-control"
                  placeholder="Tối thiểu 6 ký tự"
                  autocomplete="new-password"
                />
                <button
                  type="button"
                  class="staff-password__toggle"
                  :aria-label="showResetPwVisible ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
                  @click="showResetPwVisible = !showResetPwVisible"
                >
                  <Icon :icon="showResetPwVisible ? 'icon-park-outline:preview-close-one' : 'icon-park-outline:preview-open'" />
                </button>
              </div>
            </label>

            <div class="staff-modal__actions staff-modal__actions--wrap">
              <button
                type="button"
                class="soleil-btn-outline"
                :disabled="resetSaving"
                @click="handleGenerateTempPassword"
              >
                Sinh mật khẩu tạm
              </button>
              <button type="button" class="soleil-btn-outline" @click="closeResetPassword">
                Hủy
              </button>
              <button
                type="button"
                class="soleil-btn-primary"
                :disabled="resetSaving"
                @click="handleResetPassword"
              >
                {{ resetSaving ? 'Đang lưu…' : 'Xác nhận' }}
              </button>
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.staff-page {
  --staff-ink: #1a120c;
  --staff-muted: #5c4f42;
  --staff-line: #c9b8a4;
  --staff-line-strong: #a89278;
  --staff-mist: #f3ebe1;
  --staff-bronze: #8f7349;

  display: flex;
  flex-direction: column;
  gap: 0.85rem;
  color: var(--staff-ink);
}

.staff-page__head :deep(.soleil-page-header) {
  margin: 0;
}

.staff-page__head :deep(.soleil-page-header__title) {
  font-family: inherit;
  font-size: 1.35rem;
  font-weight: 800;
  font-style: normal;
  letter-spacing: 0.02em;
  color: var(--staff-ink);
}

.staff-page :deep(.soleil-page-header__desc) {
  margin: 0.25rem 0 0;
  font-size: 13px;
  font-weight: 500;
  color: var(--staff-muted);
}

.staff-page :deep(.soleil-toolbar) {
  border-color: var(--staff-line);
  background: #fff;
}

.staff-page :deep(.soleil-toolbar__label) {
  color: var(--staff-ink);
  font-weight: 700;
}

.staff-page :deep(.soleil-toolbar__input) {
  border-color: var(--staff-line-strong);
  background: #fff;
  color: var(--staff-ink);
  font-weight: 500;
}

.staff-reload {
  align-self: flex-end;
}

.staff-page :deep(.soleil-table-card) {
  border-color: var(--staff-line-strong);
  background: #fff;
}

.staff-page :deep(.soleil-table-card__head) {
  background: #fff;
  border-bottom-color: var(--staff-line);
}

.staff-page :deep(table.admin-table--soleil thead th) {
  background: var(--staff-bronze) !important;
  color: #fffef9 !important;
  font-size: 11px !important;
  font-weight: 700 !important;
  letter-spacing: 0.08em !important;
  border-bottom: none !important;
}

.staff-page :deep(table.admin-table--soleil tbody td) {
  color: var(--staff-ink);
  border-bottom: 1px solid var(--staff-line);
  font-size: 13.5px;
  background: #fff;
}

.staff-page :deep(table.admin-table--soleil tbody tr:hover td) {
  background: var(--staff-mist);
}

.staff-table-title {
  font-size: 14px;
  font-weight: 800;
}

.staff-table-meta {
  margin-left: auto;
  font-size: 12px;
  font-weight: 600;
  color: var(--staff-muted);
}

.staff-empty-cell {
  text-align: center;
  padding: 2.5rem 1rem !important;
  color: var(--staff-muted);
}

.staff-empty-msg {
  margin: 0 0 0.75rem;
}

.staff-mono {
  font-family: ui-monospace, 'Cascadia Mono', monospace;
  font-weight: 800;
  color: #0f4c52;
}

.staff-name {
  font-weight: 700;
}

.staff-actions {
  justify-content: center;
}

.soleil-act-btn--danger {
  color: #991b1b;
}

.soleil-act-btn--danger:hover {
  background: #fdecec;
  border-color: #f5c2c2;
}

:deep(.soleil-col-center) {
  text-align: center;
}

.staff-page :deep(.staff-role-badge) {
  border-radius: 3px;
  font-weight: 800;
  letter-spacing: 0.04em;
}

.staff-modal {
  position: fixed;
  inset: 0;
  background: rgba(26, 18, 12, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: var(--admin-z-modal);
  padding: 1rem;
}

.staff-modal__panel {
  width: min(520px, 100%);
  max-height: 90vh;
  overflow: auto;
  background: #fff;
  border-radius: 14px;
  padding: 1.15rem;
  border: 1px solid var(--staff-line);
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  box-shadow: 0 16px 40px rgba(26, 18, 12, 0.18);
}

.staff-modal__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 0.75rem;
}

.staff-modal__head h3 {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--staff-ink);
}

.staff-modal__head p {
  margin: 0.25rem 0 0;
  font-size: 0.8125rem;
  color: var(--staff-muted);
}

.staff-icon-btn {
  padding: 0.55rem 0.7rem !important;
}

.staff-modal__form {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.staff-field-grid {
  display: grid;
  gap: 0.75rem;
  grid-template-columns: 1fr;
}

@media (min-width: 640px) {
  .staff-field-grid {
    grid-template-columns: 1fr 1fr;
  }
}

.staff-field {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: #4a3f34;
}

.staff-control {
  width: 100%;
  border: 1px solid var(--staff-line-strong);
  border-radius: 8px;
  padding: 0.6rem 0.75rem;
  font-size: 0.875rem;
  font-weight: 600;
  text-transform: none;
  letter-spacing: 0;
  color: var(--staff-ink);
  background: #fff;
  outline: none;
}

.staff-control:focus {
  border-color: var(--staff-bronze);
  box-shadow: 0 0 0 2px rgba(143, 115, 73, 0.18);
}

.staff-password {
  position: relative;
}

.staff-password .staff-control {
  padding-right: 2.75rem;
}

.staff-password__toggle {
  position: absolute;
  right: 0.55rem;
  top: 50%;
  transform: translateY(-50%);
  border: none;
  background: transparent;
  color: var(--staff-muted);
  cursor: pointer;
  display: inline-flex;
  padding: 0.25rem;
}

.staff-modal__actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 0.35rem;
}

.staff-modal__actions--wrap {
  flex-wrap: wrap;
}

.staff-temp-close {
  width: 100%;
  justify-content: center;
  margin-top: 0.5rem;
}
</style>
