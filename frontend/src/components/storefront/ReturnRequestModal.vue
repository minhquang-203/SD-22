<script setup>
import { computed, ref, watch } from 'vue'
import { taoYeuCauTraHang } from '@/api/traHangApi'
import { getCustomerId } from '@/composables/useAuth'
import { toast } from '@/composables/useToast'

const props = defineProps({
  visible: { type: Boolean, default: false },
  order: { type: Object, default: null },
})

const emit = defineEmits(['close', 'submitted'])

const lyDo = ref('')
const moTa = ref('')
const tenNganHang = ref('')
const soTaiKhoan = ref('')
const chuTaiKhoan = ref('')
const submitting = ref(false)
const error = ref('')

const LY_DO_OPTIONS = [
  'Sản phẩm lỗi / hỏng',
  'Sai sản phẩm / sai biến thể',
  'Không đúng mô tả',
  'Đổi ý / không còn nhu cầu',
  'Khác',
]

const orderCode = computed(() => props.order?.maHoaDon || '')

function resetForm() {
  lyDo.value = ''
  moTa.value = ''
  tenNganHang.value = ''
  soTaiKhoan.value = ''
  chuTaiKhoan.value = ''
  error.value = ''
}

watch(
  () => props.visible,
  (v) => {
    if (v) resetForm()
  },
)

function handleClose() {
  resetForm()
  emit('close')
}

async function submitReturn() {
  const order = props.order
  if (!order?.id) {
    error.value = 'Không xác định được đơn hàng.'
    return
  }
  if (!lyDo.value.trim()) {
    error.value = 'Vui lòng chọn hoặc nhập lý do trả hàng.'
    return
  }

  const idKhachHang = getCustomerId()
  if (!idKhachHang) {
    error.value = 'Vui lòng đăng nhập để yêu cầu trả hàng.'
    return
  }

  submitting.value = true
  error.value = ''

  try {
    const payload = {
      lyDo: lyDo.value.trim(),
      moTa: moTa.value.trim() || null,
      diaChiTra: order.diaChiGiao || null,
      tenNganHang: tenNganHang.value.trim() || null,
      soTaiKhoan: soTaiKhoan.value.trim() || null,
      chuTaiKhoan: chuTaiKhoan.value.trim() || null,
    }

    const res = await taoYeuCauTraHang(order.id, idKhachHang, payload)
    toast('Đã gửi yêu cầu trả hàng. Vui lòng chờ cửa hàng duyệt.', 'info')
    emit('submitted', res.data)
    handleClose()
  } catch (e) {
    error.value = typeof e === 'string'
      ? e
      : e.response?.data?.message || e.message || 'Không gửi được yêu cầu trả hàng.'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div v-if="visible" class="sf-modal-overlay" @click.self="handleClose">
    <div class="sf-modal-content sf-modal-content--wide">
      <h3>Yêu cầu trả hàng</h3>
      <p v-if="orderCode" class="sf-modal-product">Đơn {{ orderCode }}</p>

      <div class="sf-form-group">
        <label>Lý do trả hàng <span class="sf-req">*</span></label>
        <select v-model="lyDo" class="sf-input">
          <option value="" disabled>Chọn lý do...</option>
          <option v-for="opt in LY_DO_OPTIONS" :key="opt" :value="opt">{{ opt }}</option>
        </select>
      </div>

      <div class="sf-form-group">
        <label>Mô tả chi tiết (tùy chọn)</label>
        <textarea
          v-model="moTa"
          class="sf-input"
          rows="3"
          placeholder="Mô tả tình trạng sản phẩm hoặc lý do cụ thể..."
        />
      </div>

      <div class="sf-form-group">
        <label>Địa chỉ lấy hàng hoàn</label>
        <p class="sf-hint">{{ order?.diaChiGiao || 'Sẽ dùng địa chỉ giao hàng của đơn.' }}</p>
      </div>

      <fieldset class="sf-bank-block">
        <legend>Thông tin hoàn tiền (bắt buộc với đơn COD)</legend>
        <p class="sf-hint sf-hint--mb">
          Đơn thanh toán VNPAY sẽ được hoàn về tài khoản đã thanh toán. Đơn COD cần cung cấp số tài khoản nhận tiền.
        </p>
        <div class="sf-form-group">
          <label>Tên ngân hàng</label>
          <input v-model="tenNganHang" type="text" class="sf-input" placeholder="VD: Vietcombank, Techcombank..." />
        </div>
        <div class="sf-form-group">
          <label>Số tài khoản</label>
          <input v-model="soTaiKhoan" type="text" class="sf-input" placeholder="Số tài khoản nhận hoàn tiền" />
        </div>
        <div class="sf-form-group">
          <label>Chủ tài khoản</label>
          <input v-model="chuTaiKhoan" type="text" class="sf-input" placeholder="Họ tên chủ tài khoản" />
        </div>
      </fieldset>

      <p v-if="error" class="sf-modal-error">{{ error }}</p>

      <div class="sf-modal-actions">
        <button type="button" class="btn-soleil btn-outline" :disabled="submitting" @click="handleClose">
          Hủy
        </button>
        <button type="button" class="btn-soleil btn-primary" :disabled="submitting" @click="submitReturn">
          {{ submitting ? 'Đang gửi...' : 'Gửi yêu cầu' }}
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
  padding: 16px;
}

.sf-modal-content {
  background: white;
  padding: 24px;
  border-radius: 8px;
  width: 400px;
  max-width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.sf-modal-content--wide {
  width: 480px;
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
  margin-bottom: 14px;
}

.sf-form-group label {
  display: block;
  font-size: 13px;
  color: #555;
  margin-bottom: 6px;
  font-weight: 500;
}

.sf-req {
  color: #dc2626;
}

.sf-input {
  width: 100%;
  border: 1px solid #ddd;
  padding: 10px;
  border-radius: 4px;
  font-family: inherit;
  box-sizing: border-box;
  font-size: 14px;
}

.sf-hint {
  margin: 0;
  font-size: 13px;
  color: #64748b;
  line-height: 1.4;
}

.sf-hint--mb {
  margin-bottom: 12px;
}

.sf-bank-block {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 12px 14px 4px;
  margin: 0 0 16px;
}

.sf-bank-block legend {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  padding: 0 4px;
}

.sf-modal-error {
  color: #dc2626;
  font-size: 13px;
  margin: 0 0 12px;
}

.sf-modal-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
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
