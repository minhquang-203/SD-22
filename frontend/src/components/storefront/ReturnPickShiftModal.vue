<script setup>
import { computed, ref, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { fetchCaLayHang } from '@/api/traHangApi'
import '@/styles/returnModal.css'

const props = defineProps({
  visible: { type: Boolean, default: false },
  order: { type: Object, default: null },
  submitting: { type: Boolean, default: false },
})

const emit = defineEmits(['close', 'confirm'])

const shifts = ref([])
const selectedShiftId = ref(null)
const loading = ref(false)
const error = ref('')

const orderCode = computed(() => props.order?.maHoaDon || '')
const pickupAddress = computed(() => props.order?.diaChiGiao || '')

function formatShiftTime(shift) {
  const range = [shift.fromTime, shift.toTime]
    .filter((t) => Number.isFinite(t))
    .map((t) => {
      const date = new Date(t * 1000)
      return date.toLocaleString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
      })
    })
  return range.length === 2 ? `${range[0]} — ${range[1]}` : ''
}

async function loadShifts() {
  loading.value = true
  error.value = ''
  try {
    const res = await fetchCaLayHang()
    shifts.value = res.data || []
    selectedShiftId.value = shifts.value[0]?.id ?? null
    if (!shifts.value.length) {
      error.value = 'Giao Hàng Nhanh hiện không có ca lấy hàng nào. Bạn vẫn có thể tạo vận đơn, '
        + 'shipper sẽ liên hệ để hẹn lấy hàng.'
    }
  } catch (e) {
    shifts.value = []
    error.value = typeof e === 'string'
      ? e
      : 'Không tải được danh sách ca lấy hàng. Bạn vẫn có thể tạo vận đơn không chọn ca.'
  } finally {
    loading.value = false
  }
}

watch(
  () => props.visible,
  (visible) => {
    document.body.style.overflow = visible ? 'hidden' : ''
    if (visible) {
      loadShifts()
    } else {
      shifts.value = []
      selectedShiftId.value = null
      error.value = ''
    }
  },
)

function handleClose() {
  if (props.submitting) return
  emit('close')
}

function handleConfirm() {
  emit('confirm', selectedShiftId.value)
}
</script>

<template>
  <Teleport to="body">
    <div v-if="visible" class="sf-return-overlay" @click.self="handleClose">
      <div
        class="sf-return-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="sf-pick-shift-title"
        @click.stop
      >
        <div class="sf-return-modal__header">
          <div class="sf-return-modal__titles">
            <h3 id="sf-pick-shift-title">Tạo vận đơn hoàn hàng</h3>
            <p v-if="orderCode" class="sf-return-modal__product">Đơn {{ orderCode }}</p>
          </div>
          <button
            type="button"
            class="sf-return-modal__close"
            aria-label="Đóng"
            :disabled="submitting"
            @click="handleClose"
          >
            <Icon icon="mdi:close" width="20" />
          </button>
        </div>

        <div class="sf-return-modal__body">
          <p class="sf-return-info">
            Chọn ca lấy hàng để Giao Hàng Nhanh đến lấy sản phẩm tại địa chỉ của bạn.
            Cửa hàng chịu phí vận chuyển hoàn.
          </p>

          <div v-if="pickupAddress" class="sf-return-form-group">
            <label>Địa chỉ lấy hàng</label>
            <p class="sf-return-hint">{{ pickupAddress }}</p>
          </div>

          <div class="sf-return-form-group">
            <label>Ca lấy hàng</label>
            <p v-if="loading" class="sf-return-hint">Đang tải ca lấy hàng...</p>
            <div v-else-if="shifts.length" class="sf-shift-list">
              <label
                v-for="shift in shifts"
                :key="shift.id"
                class="sf-shift-option"
                :class="{ active: selectedShiftId === shift.id }"
              >
                <input
                  v-model="selectedShiftId"
                  type="radio"
                  name="sf-pick-shift"
                  :value="shift.id"
                  :disabled="submitting"
                />
                <span class="sf-shift-option__body">
                  <span class="sf-shift-option__title">{{ shift.title || `Ca ${shift.id}` }}</span>
                  <span v-if="formatShiftTime(shift)" class="sf-shift-option__time">
                    {{ formatShiftTime(shift) }}
                  </span>
                </span>
              </label>
            </div>
          </div>

          <p v-if="error" class="sf-return-error">{{ error }}</p>
        </div>

        <div class="sf-return-modal__footer">
          <button
            type="button"
            class="sf-return-btn sf-return-btn--outline"
            :disabled="submitting"
            @click="handleClose"
          >
            Hủy
          </button>
          <button
            type="button"
            class="sf-return-btn sf-return-btn--primary"
            :disabled="submitting || loading"
            @click="handleConfirm"
          >
            {{ submitting ? 'Đang tạo vận đơn...' : 'Tạo vận đơn' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.sf-shift-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.sf-shift-option {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 400;
  margin: 0;
  transition: border-color 0.15s ease, background 0.15s ease;
}

.sf-shift-option:hover {
  border-color: #c49554;
}

.sf-shift-option.active {
  border-color: #c49554;
  background: rgba(196, 149, 84, 0.08);
}

.sf-shift-option input {
  accent-color: #c49554;
  margin: 0;
}

.sf-shift-option__body {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.sf-shift-option__title {
  font-size: 14px;
  color: #1e1510;
}

.sf-shift-option__time {
  font-size: 12px;
  color: #64748b;
}
</style>
