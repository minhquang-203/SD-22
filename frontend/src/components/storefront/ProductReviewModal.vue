<script setup>
import { ref } from 'vue'
import { Icon } from '@iconify/vue'
import request from '@/api/request'
import { getCustomerId } from '@/composables/useAuth'

const props = defineProps({
  visible: { type: Boolean, default: false },
  line: { type: Object, default: null },
})

const emit = defineEmits(['close', 'submitted'])

const soSao = ref(5)
const noiDung = ref('')
const reviewFile = ref(null)
const submitting = ref(false)
const error = ref('')

function resetForm() {
  soSao.value = 5
  noiDung.value = ''
  reviewFile.value = null
  error.value = ''
}

function handleClose() {
  resetForm()
  emit('close')
}

function handleFileChange(event) {
  reviewFile.value = event.target.files?.[0] || null
}

async function submitReview() {
  const line = props.line
  if (!line?.idSanPham) {
    error.value = 'Không xác định được sản phẩm để đánh giá.'
    return
  }

  const idKhachHang = getCustomerId()
  if (!idKhachHang) {
    error.value = 'Vui lòng đăng nhập để đánh giá.'
    return
  }

  submitting.value = true
  error.value = ''

  try {
    const payload = {
      soSao: soSao.value,
      noiDung: noiDung.value.trim(),
      idSanPham: line.idSanPham,
      idHoaDonChiTiet: line.id,
      idKhachHang,
    }

    if (reviewFile.value) {
      payload.imageBase64 = await new Promise((resolve, reject) => {
        const reader = new FileReader()
        reader.readAsDataURL(reviewFile.value)
        reader.onload = () => resolve(reader.result)
        reader.onerror = (err) => reject(err)
      })
    }

    await request.post('/danh-gia/add', payload)
    emit('submitted', { lineId: line.id })
    handleClose()
  } catch (e) {
    error.value = e.response?.data?.message || e.message || 'Không gửi được đánh giá.'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div v-if="visible" class="sf-modal-overlay" @click.self="handleClose">
    <div class="sf-modal-content">
      <h3>Đánh giá sản phẩm</h3>
      <p v-if="line?.tenSanPham" class="sf-modal-product">{{ line.tenSanPham }}</p>

      <div class="sf-form-group">
        <label>Chất lượng sản phẩm (Sao):</label>
        <div class="sf-stars-input">
          <Icon
            v-for="n in 5"
            :key="n"
            icon="solar:star-bold"
            :class="['sf-star', { active: n <= soSao }]"
            width="24"
            @click="soSao = n"
          />
        </div>
      </div>

      <div class="sf-form-group">
        <label>Nhận xét của bạn:</label>
        <textarea
          v-model="noiDung"
          placeholder="Hãy chia sẻ nhận xét cho sản phẩm này nhé"
          rows="3"
          class="sf-input"
        />
      </div>

      <div class="sf-form-group">
        <label>Ảnh đính kèm (tùy chọn):</label>
        <input type="file" accept="image/*" class="sf-input" @change="handleFileChange" />
      </div>

      <p v-if="error" class="sf-modal-error">{{ error }}</p>

      <div class="sf-modal-actions">
        <button type="button" class="btn-soleil btn-outline" :disabled="submitting" @click="handleClose">
          Trở lại
        </button>
        <button type="button" class="btn-soleil btn-primary" :disabled="submitting" @click="submitReview">
          {{ submitting ? 'Đang gửi...' : 'Hoàn thành' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.sf-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(2px);
}

.sf-modal-content {
  background: white;
  padding: 24px;
  border-radius: 8px;
  width: 400px;
  max-width: 90%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.sf-modal-content h3 {
  margin: 0 0 8px;
  font-size: 18px;
  text-align: center;
}

.sf-modal-product {
  margin: 0 0 20px;
  text-align: center;
  font-size: 14px;
  color: #64748b;
}

.sf-form-group {
  margin-bottom: 16px;
}

.sf-form-group label {
  display: block;
  font-size: 13px;
  color: #555;
  margin-bottom: 8px;
  font-weight: 500;
}

.sf-input {
  width: 100%;
  border: 1px solid #ddd;
  padding: 10px;
  border-radius: 4px;
  font-family: inherit;
  box-sizing: border-box;
}

.sf-stars-input {
  display: flex;
  gap: 8px;
  cursor: pointer;
  justify-content: center;
  margin-bottom: 10px;
}

.sf-star {
  color: #ccc;
  transition: 0.2s;
}

.sf-star.active {
  color: #f59e0b;
}

.sf-modal-error {
  color: #dc2626;
  font-size: 13px;
  margin: 0 0 12px;
}

.sf-modal-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 24px;
  gap: 10px;
}

.btn-soleil {
  flex: 1;
  padding: 10px;
  border-radius: 4px;
  border: none;
  cursor: pointer;
  font-weight: 600;
}

.btn-outline {
  background: white;
  border: 1px solid #ddd;
  color: #555;
}

.btn-primary {
  background: #ee4d2d;
  color: white;
}

.btn-soleil:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
