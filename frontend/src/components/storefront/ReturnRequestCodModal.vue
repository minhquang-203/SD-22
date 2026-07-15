<script setup>
import { computed } from 'vue'
import { Icon } from '@iconify/vue'
import { LY_DO_OPTIONS, useReturnRequest } from '@/composables/useReturnRequest'
import '@/styles/returnModal.css'

const props = defineProps({
  visible: { type: Boolean, default: false },
  order: { type: Object, default: null },
})

const emit = defineEmits(['close', 'submitted'])

const orderCode = computed(() => props.order?.maHoaDon || '')

const {
  lyDo,
  moTa,
  tenNganHang,
  soTaiKhoan,
  chuTaiKhoan,
  imagePreviews,
  submitting,
  error,
  fileInputRef,
  MIN_IMAGES,
  MAX_IMAGES,
  handleClose,
  onFileChange,
  removeImage,
  submitReturn,
} = useReturnRequest({
  visible: () => props.visible,
  order: () => props.order,
  emit,
  requireBank: true,
})
</script>

<template>
  <Teleport to="body">
    <div
      v-if="visible"
      class="sf-return-overlay"
      @click.self="handleClose"
    >
      <div
        class="sf-return-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="sf-return-cod-title"
        @click.stop
      >
        <div class="sf-return-modal__header">
          <div class="sf-return-modal__titles">
            <h3 id="sf-return-cod-title">Yêu cầu trả hàng (COD)</h3>
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
          <div class="sf-return-form-group">
            <label>Lý do trả hàng <span class="sf-return-req">*</span></label>
            <select v-model="lyDo" class="sf-return-input">
              <option value="" disabled>Chọn lý do...</option>
              <option v-for="opt in LY_DO_OPTIONS" :key="opt" :value="opt">{{ opt }}</option>
            </select>
          </div>

          <div class="sf-return-form-group">
            <label>Mô tả chi tiết (tùy chọn)</label>
            <textarea
              v-model="moTa"
              class="sf-return-input"
              rows="3"
              placeholder="Mô tả tình trạng sản phẩm hoặc lý do cụ thể..."
            />
          </div>

          <div class="sf-return-form-group">
            <label>Địa chỉ lấy hàng hoàn</label>
            <p class="sf-return-hint">{{ order?.diaChiGiao || 'Sẽ dùng địa chỉ giao hàng của đơn.' }}</p>
          </div>

          <fieldset class="sf-return-bank">
            <legend>Thông tin nhận hoàn tiền <span class="sf-return-req">*</span></legend>
            <p class="sf-return-hint sf-return-hint--mb">
              Đơn COD cần cung cấp số tài khoản ngân hàng để cửa hàng chuyển tiền hoàn.
            </p>
            <div class="sf-return-form-group">
              <label>Tên ngân hàng <span class="sf-return-req">*</span></label>
              <input
                v-model="tenNganHang"
                type="text"
                class="sf-return-input"
                placeholder="VD: Vietcombank, Techcombank..."
              />
            </div>
            <div class="sf-return-form-group">
              <label>Số tài khoản <span class="sf-return-req">*</span></label>
              <input
                v-model="soTaiKhoan"
                type="text"
                class="sf-return-input"
                placeholder="Số tài khoản nhận hoàn tiền"
              />
            </div>
            <div class="sf-return-form-group">
              <label>Chủ tài khoản <span class="sf-return-req">*</span></label>
              <input
                v-model="chuTaiKhoan"
                type="text"
                class="sf-return-input"
                placeholder="Họ tên chủ tài khoản"
              />
            </div>
          </fieldset>

          <div class="sf-return-images">
            <span class="sf-return-images__label">
              Hình ảnh sản phẩm <span class="sf-return-req">*</span>
            </span>
            <p class="sf-return-images__hint">
              Bắt buộc tải tối thiểu {{ MIN_IMAGES }} ảnh (tối đa {{ MAX_IMAGES }}).
              Đã chọn: {{ imagePreviews.length }}/{{ MAX_IMAGES }}
            </p>
            <input
              ref="fileInputRef"
              type="file"
              class="sf-return-images__input"
              accept="image/*"
              multiple
              @change="onFileChange"
            />
            <div v-if="imagePreviews.length" class="sf-return-previews">
              <div
                v-for="(url, index) in imagePreviews"
                :key="`${url}-${index}`"
                class="sf-return-preview"
              >
                <img :src="url" alt="Ảnh trả hàng" />
                <button
                  type="button"
                  class="sf-return-preview__remove"
                  aria-label="Xóa ảnh"
                  @click="removeImage(index)"
                >
                  ×
                </button>
              </div>
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
            :disabled="submitting"
            @click="submitReturn"
          >
            {{ submitting ? 'Đang gửi...' : 'Gửi yêu cầu' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>
