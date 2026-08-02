<template>
  <div class="banner-manager">
    <div class="page-header">
      <div>
        <h1 class="page-title">Banner trang chủ</h1>
        <p class="page-subtitle">Quản lý các banner CTA trên trang chủ (nội dung, ảnh, link khi bấm).</p>
      </div>
      <button class="btn-primary" type="button" @click="openAddModal">+ Thêm banner</button>
    </div>

    <div class="card">
      <div v-if="loading" class="empty-state">Đang tải...</div>
      <div v-else-if="banners.length === 0" class="empty-state">
        <h3>Chưa có banner nào</h3>
        <p>Thêm banner để hiện trên trang chủ (ví dụ CTA làm Quiz).</p>
        <button class="btn-outline" type="button" @click="openAddModal">Thêm banner đầu tiên</button>
      </div>
      <div v-else class="table-responsive">
        <table class="admin-table">
          <thead>
            <tr>
              <th>Thứ tự</th>
              <th>Nội dung</th>
              <th>Link</th>
              <th>Trạng thái</th>
              <th class="actions-col">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="b in banners" :key="b.id">
              <td>{{ b.thuTu }}</td>
              <td>
                <div class="banner-info">
                  <span class="banner-title">{{ b.tieuDeChinh }}</span>
                  <span v-if="b.tieuDe" class="banner-meta">{{ b.tieuDe }}</span>
                  <span v-if="b.nutText" class="banner-meta">Nút: {{ b.nutText }}</span>
                </div>
              </td>
              <td><code class="link-code">{{ b.linkUrl }}</code></td>
              <td>
                <span class="status-badge" :class="b.trangThai ? 'active' : 'inactive'">
                  {{ b.trangThai ? 'Đang hiện' : 'Đã ẩn' }}
                </span>
              </td>
              <td class="actions-col">
                <button class="btn-icon" type="button" title="Sửa" @click="editBanner(b)">✏️</button>
                <button class="btn-icon" type="button" title="Xóa" @click="confirmDelete(b.id)">🗑️</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="isModalOpen" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>{{ isEditing ? 'Cập nhật banner' : 'Thêm banner mới' }}</h2>
          <button class="btn-close" type="button" @click="closeModal">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>Tiêu đề nhỏ (eyebrow)</label>
            <input v-model="form.tieuDe" type="text" placeholder="VD: Trắc nghiệm da" />
          </div>
          <div class="form-group">
            <label>Tiêu đề chính <span class="required">*</span></label>
            <input v-model="form.tieuDeChinh" type="text" placeholder="VD: Tìm sản phẩm chống nắng phù hợp với bạn" />
          </div>
          <div class="form-group">
            <label>Mô tả</label>
            <textarea v-model="form.moTa" rows="3" placeholder="Nội dung mô tả ngắn..." />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>Chữ trên nút</label>
              <input v-model="form.nutText" type="text" placeholder="VD: Làm Quiz Ngay" />
            </div>
            <div class="form-group">
              <label>Thứ tự</label>
              <input v-model.number="form.thuTu" type="number" min="0" />
            </div>
          </div>
          <div class="form-group">
            <label>Link khi bấm <span class="required">*</span></label>
            <input v-model="form.linkUrl" type="text" placeholder="/quiz hoặc https://..." />
            <p class="hint">Đường dẫn nội bộ (bắt đầu bằng /) hoặc URL ngoài (https://...).</p>
          </div>
          <div class="form-group">
            <label>Ảnh nền (tuỳ chọn)</label>
            <div class="upload-row">
              <input type="file" accept="image/*" :disabled="uploading" @change="onFileChange" />
              <span v-if="uploading" class="upload-status">Đang tải ảnh lên...</span>
            </div>
            <p class="hint">Chọn ảnh là hệ thống tự tải lên. Hoặc dán sẵn đường dẫn ảnh bên dưới.</p>
            <input v-model="form.anhUrl" type="text" class="mt-8" placeholder="Hoặc dán đường dẫn ảnh /uploads/..." />
            <img v-if="form.anhUrl" :src="previewUrl(form.anhUrl)" alt="Preview" class="preview-img" />
          </div>
          <div class="form-group">
            <label class="checkbox-label">
              <input v-model="form.trangThai" type="checkbox" />
              Hiện trên trang chủ
            </label>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-outline" type="button" @click="closeModal">Hủy</button>
          <button class="btn-primary" type="button" :disabled="saving" @click="saveBanner">
            {{ saving ? 'Đang lưu...' : 'Lưu banner' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { confirm } from '@/composables/useConfirm'
import {
  createBannerAdmin,
  deleteBannerAdmin,
  getAllBannersAdmin,
  updateBannerAdmin,
  uploadBannerImageAdmin,
} from '@/api/bannerApi'
import { productImageUrl } from '@/utils/productImage'

const banners = ref([])
const loading = ref(true)
const isModalOpen = ref(false)
const isEditing = ref(false)
const saving = ref(false)
const uploading = ref(false)
const pendingFile = ref(null)
const editingId = ref(null)

const emptyForm = () => ({
  tieuDe: '',
  tieuDeChinh: '',
  moTa: '',
  nutText: 'Xem ngay',
  linkUrl: '/quiz',
  anhUrl: '',
  thuTu: 1,
  trangThai: true,
})

const form = ref(emptyForm())

function previewUrl(url) {
  return productImageUrl(url)
}

async function loadData() {
  loading.value = true
  try {
    const res = await getAllBannersAdmin()
    banners.value = res.data || []
  } catch (e) {
    console.error(e)
    alert('Không tải được danh sách banner')
  } finally {
    loading.value = false
  }
}

onMounted(loadData)

function openAddModal() {
  isEditing.value = false
  editingId.value = null
  form.value = emptyForm()
  form.value.thuTu = (banners.value.length || 0) + 1
  pendingFile.value = null
  isModalOpen.value = true
}

function editBanner(b) {
  isEditing.value = true
  editingId.value = b.id
  form.value = {
    tieuDe: b.tieuDe || '',
    tieuDeChinh: b.tieuDeChinh || '',
    moTa: b.moTa || '',
    nutText: b.nutText || '',
    linkUrl: b.linkUrl || '',
    anhUrl: b.anhUrl || '',
    thuTu: b.thuTu ?? 0,
    trangThai: b.trangThai !== false,
  }
  pendingFile.value = null
  isModalOpen.value = true
}

function closeModal() {
  isModalOpen.value = false
}

function onFileChange(e) {
  pendingFile.value = e.target.files?.[0] || null
  // Tự động tải ảnh lên ngay khi chọn để tránh trường hợp quên bấm "Tải ảnh lên"
  if (pendingFile.value) {
    uploadImage()
  }
}

async function uploadImage() {
  if (!pendingFile.value) return
  uploading.value = true
  try {
    const res = await uploadBannerImageAdmin(pendingFile.value)
    form.value.anhUrl = res.data?.url || ''
    pendingFile.value = null
  } catch (e) {
    alert(typeof e === 'string' ? e : 'Tải ảnh thất bại')
    console.error(e)
  } finally {
    uploading.value = false
  }
}

async function saveBanner() {
  if (!form.value.tieuDeChinh?.trim()) {
    alert('Vui lòng nhập tiêu đề chính')
    return
  }
  if (!form.value.linkUrl?.trim()) {
    alert('Vui lòng nhập link')
    return
  }
  saving.value = true
  try {
    // Nếu còn ảnh vừa chọn nhưng chưa tải lên, tải trước khi lưu
    if (pendingFile.value) {
      await uploadImage()
      if (pendingFile.value) {
        // Upload thất bại (pendingFile chưa được xoá) → dừng lưu
        return
      }
    }
    const payload = {
      tieuDe: form.value.tieuDe,
      tieuDeChinh: form.value.tieuDeChinh,
      moTa: form.value.moTa,
      nutText: form.value.nutText,
      linkUrl: form.value.linkUrl,
      anhUrl: form.value.anhUrl || null,
      thuTu: Number(form.value.thuTu) || 0,
      trangThai: !!form.value.trangThai,
    }
    if (isEditing.value) {
      await updateBannerAdmin(editingId.value, payload)
    } else {
      await createBannerAdmin(payload)
    }
    closeModal()
    await loadData()
  } catch (e) {
    alert(typeof e === 'string' ? e : 'Lưu banner thất bại')
    console.error(e)
  } finally {
    saving.value = false
  }
}

async function confirmDelete(id) {
  const ok = await confirm({
    title: 'Xóa banner',
    message: 'Xóa banner này khỏi trang chủ?',
    confirmText: 'Xóa',
    danger: true,
  })
  if (!ok) return
  try {
    await deleteBannerAdmin(id)
    await loadData()
  } catch (e) {
    alert('Xóa thất bại')
    console.error(e)
  }
}
</script>

<style scoped>
.banner-manager { padding: 24px; background-color: #f8f9fa; min-height: 100vh; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; gap: 16px; }
.page-title { font-size: 24px; font-weight: 600; color: #1a1412; margin: 0; }
.page-subtitle { color: #6b7280; font-size: 14px; margin-top: 4px; }
.card { background: white; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); padding: 20px; }
.empty-state { text-align: center; padding: 40px 16px; color: #6b7280; }
.admin-table { width: 100%; border-collapse: collapse; }
.admin-table th { text-align: left; padding: 12px; background: #f9fafb; color: #4b5563; font-weight: 600; border-bottom: 1px solid #e5e7eb; }
.admin-table td { padding: 16px 12px; border-bottom: 1px solid #f3f4f6; vertical-align: top; }
.banner-info { display: flex; flex-direction: column; gap: 2px; }
.banner-title { font-weight: 600; color: #111827; }
.banner-meta { font-size: 12px; color: #9ca3af; }
.link-code { font-size: 12px; background: #f3f4f6; padding: 2px 6px; border-radius: 4px; }
.status-badge { padding: 4px 8px; border-radius: 12px; font-size: 12px; font-weight: 500; }
.status-badge.active { background: #dcfce7; color: #15803d; }
.status-badge.inactive { background: #f3f4f6; color: #6b7280; }
.btn-primary { background: #4f46e5; color: white; border: none; padding: 10px 18px; border-radius: 6px; cursor: pointer; font-weight: 500; }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-outline { background: white; border: 1px solid #d1d5db; padding: 8px 14px; border-radius: 6px; cursor: pointer; }
.btn-outline:disabled { opacity: 0.5; cursor: not-allowed; }
.actions-col { text-align: right; white-space: nowrap; }
.btn-icon { background: none; border: none; cursor: pointer; font-size: 16px; padding: 5px; opacity: 0.6; }
.btn-icon:hover { opacity: 1; background: #f3f4f6; border-radius: 4px; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: flex-start; justify-content: center; padding-top: 50px; z-index: var(--admin-z-modal, 5000); }
.modal-content { background: white; border-radius: 8px; width: 560px; max-width: calc(100vw - 32px); max-height: 90vh; overflow-y: auto; box-shadow: 0 20px 25px -5px rgba(0,0,0,0.1); }
.modal-header { padding: 16px 20px; border-bottom: 1px solid #e5e7eb; display: flex; justify-content: space-between; align-items: center; }
.modal-header h2 { margin: 0; font-size: 18px; }
.btn-close { border: none; background: none; font-size: 24px; cursor: pointer; line-height: 1; }
.modal-body { padding: 20px; }
.modal-footer { padding: 16px 20px; border-top: 1px solid #e5e7eb; display: flex; justify-content: flex-end; gap: 10px; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 14px; font-weight: 500; color: #374151; margin-bottom: 5px; }
.form-group input, .form-group textarea { width: 100%; padding: 8px 12px; border: 1px solid #d1d5db; border-radius: 6px; box-sizing: border-box; }
.form-row { display: grid; grid-template-columns: 1fr 120px; gap: 12px; }
.hint { margin: 6px 0 0; font-size: 12px; color: #9ca3af; }
.required { color: #ef4444; }
.upload-row { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
.upload-status { font-size: 13px; color: #4f46e5; font-weight: 500; }
.mt-8 { margin-top: 8px; }
.preview-img { margin-top: 10px; max-width: 100%; max-height: 140px; object-fit: cover; border-radius: 6px; border: 1px solid #e5e7eb; }
.checkbox-label { display: flex !important; align-items: center; gap: 8px; font-weight: 500; }
.checkbox-label input { width: auto; }
</style>
