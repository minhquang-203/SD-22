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
  <div class="space-y-6">
    <PageHeader
      title="Quản lý nhân viên"
      description="Thêm, sửa và khóa tài khoản nhân viên / quản lý"
    >
      <template #actions>
        <button
          v-if="canAddStaff"
          type="button"
          class="soleil-btn-primary"
          @click="openCreate"
        >
          <Icon icon="icon-park-outline:plus" class="text-lg" />
          Thêm nhân viên
        </button>
      </template>
    </PageHeader>

    <div class="admin-card p-4">
      <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <input
          v-model="keyword"
          type="search"
          class="admin-input max-w-md"
          placeholder="Tìm theo tên, email, SĐT, mã NV..."
        />
        <p class="text-sm text-[var(--admin-muted)]">
          {{ filteredStaff.length }} nhân viên
        </p>
      </div>
    </div>

    <div class="admin-card overflow-hidden">
      <div v-if="loading" class="p-8 text-center text-[var(--admin-muted)]">Đang tải...</div>
      <div v-else-if="loadError" class="p-8 text-center">
        <p class="text-[var(--admin-muted)] mb-3">{{ loadError }}</p>
        <button type="button" class="soleil-btn-outline" @click="loadStaff">Thử lại</button>
      </div>
      <div v-else class="overflow-x-auto">
        <table class="admin-table w-full">
          <thead>
            <tr>
              <th>Mã NV</th>
              <th>Họ tên</th>
              <th>Email</th>
              <th>SĐT</th>
              <th>Vai trò</th>
              <th>Trạng thái</th>
              <th class="text-right">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!filteredStaff.length">
              <td colspan="7" class="text-center text-[var(--admin-muted)] py-8">
                Chưa có nhân viên nào trong hệ thống
              </td>
            </tr>
            <tr v-for="item in filteredStaff" :key="item.id">
              <td>{{ item.maNhanVien || '—' }}</td>
              <td class="font-medium">{{ item.hoTen }}</td>
              <td>{{ item.email || '—' }}</td>
              <td>{{ item.soDienThoai || '—' }}</td>
              <td>
                <span class="staff-role-badge" :class="roleBadgeClass(item.maVaiTro)">
                  {{ getRoleLabel(item.maVaiTro) }}
                </span>
              </td>
              <td>
                <StatusDot
                  :status="item.trangThai !== false ? 'active' : 'paused'"
                  :label="item.trangThai !== false ? 'Hoạt động' : 'Đã khóa'"
                />
              </td>
              <td>
                <div class="flex flex-wrap justify-end gap-2">
                  <button
                    type="button"
                    class="soleil-btn-outline !px-3 !py-1.5 text-xs"
                    :disabled="!canActOn(item)"
                    :title="canActOn(item) ? 'Sửa nhân viên' : STAFF_ACTION_DENIED"
                    @click="openEdit(item)"
                  >
                    Sửa
                  </button>
                  <button
                    type="button"
                    class="soleil-btn-outline !px-3 !py-1.5 text-xs !text-[var(--bronze)] !border-[rgba(201,169,110,0.45)]"
                    :disabled="!canActOn(item)"
                    :title="canActOn(item) ? 'Đặt lại mật khẩu' : STAFF_ACTION_DENIED"
                    @click="openResetPassword(item)"
                  >
                    Đặt lại MK
                  </button>
                  <button
                    type="button"
                    class="soleil-btn-outline !px-3 !py-1.5 text-xs"
                    :class="item.trangThai !== false ? '!text-red-700 !border-red-200' : '!text-emerald-800 !border-emerald-200'"
                    :disabled="!canActOn(item)"
                    :title="canActOn(item) ? (item.trangThai !== false ? 'Khóa nhân viên' : 'Mở khóa nhân viên') : STAFF_ACTION_DENIED"
                    @click="handleToggleStatus(item)"
                  >
                    {{ item.trangThai !== false ? 'Khóa' : 'Mở khóa' }}
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="showForm" class="admin-modal-backdrop" @click.self="closeForm">
      <div class="admin-modal admin-modal--md">
        <div class="admin-modal__header">
          <h2 class="admin-modal__title">{{ formTitle }}</h2>
          <button type="button" class="admin-topbar__btn-icon" aria-label="Đóng" @click="closeForm">
            <Icon icon="icon-park-outline:close" />
          </button>
        </div>

        <form class="admin-modal__body space-y-4" @submit.prevent="handleSubmit">
          <div class="admin-login-field">
            <label>Họ tên *</label>
            <input v-model="form.hoTen" type="text" class="admin-input" />
          </div>

          <div class="grid gap-4 sm:grid-cols-2">
            <div class="admin-login-field">
              <label>Email *</label>
              <input v-model="form.email" type="email" class="admin-input" />
            </div>
            <div class="admin-login-field">
              <label>Số điện thoại *</label>
              <input v-model="form.soDienThoai" type="text" class="admin-input" />
            </div>
          </div>

          <div v-if="!editingId" class="admin-login-field">
            <label>Mật khẩu *</label>
            <input v-model="form.matKhau" type="password" class="admin-input" placeholder="Tối thiểu 6 ký tự" />
          </div>

          <div class="grid gap-4 sm:grid-cols-2">
            <div class="admin-login-field">
              <label>Vai trò *</label>
              <select v-model="form.maVaiTro" class="admin-input">
                <option
                  v-for="role in creatableRoles"
                  :key="role"
                  :value="role"
                >
                  {{ getRoleLabel(role) }}
                </option>
              </select>
            </div>
            <div class="admin-login-field">
              <label>Ngày vào làm</label>
              <input v-model="form.ngayVaoLam" type="date" class="admin-input" />
            </div>
          </div>

          <div class="admin-modal__footer">
            <button type="button" class="soleil-btn-outline" @click="closeForm">
              Hủy
            </button>
            <button type="submit" class="soleil-btn-primary" :disabled="saving">
              {{ saving ? 'Đang lưu...' : 'Lưu' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <div v-if="showResetPw && resetTarget" class="admin-modal-backdrop" @click.self="closeResetPassword">
      <div class="admin-modal admin-modal--md">
        <div class="admin-modal__header">
          <h2 class="admin-modal__title">Đặt lại mật khẩu</h2>
          <button type="button" class="admin-topbar__btn-icon" aria-label="Đóng" @click="closeResetPassword">
            <Icon icon="icon-park-outline:close" />
          </button>
        </div>

        <div class="admin-modal__body space-y-4">
          <p class="text-sm text-[var(--admin-muted)]">
            Nhân viên: <strong>{{ resetTarget.hoTen }}</strong> ({{ resetTarget.email }})
          </p>

          <div v-if="generatedTempPw" class="staff-temp-pw-box">
            <p class="staff-temp-pw-box__label">Mật khẩu tạm (chỉ hiển thị một lần)</p>
            <div class="staff-temp-pw-box__row">
              <code class="staff-temp-pw-box__code">{{ generatedTempPw }}</code>
              <button type="button" class="soleil-btn-outline !px-3 !py-1.5 text-xs" @click="copyTempPassword">
                Sao chép
              </button>
            </div>
            <p class="staff-temp-pw-box__hint">Hãy đưa mật khẩu này cho nhân viên và yêu cầu đổi sau khi đăng nhập.</p>
            <button type="button" class="soleil-btn-primary w-full justify-center mt-2" @click="closeResetPassword">
              Đóng
            </button>
          </div>

          <template v-else>
            <div class="admin-login-field">
              <label>Mật khẩu mới</label>
              <div class="admin-login-password">
                <input
                  v-model="resetMatKhau"
                  :type="showResetPwVisible ? 'text' : 'password'"
                  class="admin-input"
                  placeholder="Tối thiểu 6 ký tự"
                  autocomplete="new-password"
                />
                <button
                  type="button"
                  class="admin-login-password__toggle"
                  :aria-label="showResetPwVisible ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
                  @click="showResetPwVisible = !showResetPwVisible"
                >
                  <Icon :icon="showResetPwVisible ? 'icon-park-outline:preview-close-one' : 'icon-park-outline:preview-open'" />
                </button>
              </div>
            </div>

            <div class="admin-modal__footer !pt-0 flex-wrap">
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
                {{ resetSaving ? 'Đang lưu...' : 'Xác nhận' }}
              </button>
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>
