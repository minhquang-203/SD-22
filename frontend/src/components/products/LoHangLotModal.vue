<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  open: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  variant: { type: Object, default: null },
})

const emit = defineEmits(['close', 'submit'])

const form = ref({
  soLo: '',
  ngayNhap: new Date().toISOString().slice(0, 10),
  hanSuDung: '',
  soLuongNhap: null,
  ghiChu: '',
})

watch(
  () => props.open,
  (isOpen) => {
    if (!isOpen) return
    form.value = {
      soLo: '',
      ngayNhap: new Date().toISOString().slice(0, 10),
      hanSuDung: '',
      soLuongNhap: null,
      ghiChu: '',
    }
  },
)

function submit() {
  if (!form.value.soLo?.trim()) return alert('Vui lòng nhập số lô')
  if (!form.value.ngayNhap) return alert('Vui lòng chọn ngày nhập')
  if (!form.value.soLuongNhap || Number(form.value.soLuongNhap) <= 0) {
    return alert('Số lượng nhập phải lớn hơn 0')
  }
  emit('submit', {
    idChiTietSanPham: props.variant?.id,
    soLo: form.value.soLo.trim(),
    ngayNhap: form.value.ngayNhap,
    hanSuDung: form.value.hanSuDung || null,
    soLuongNhap: Number(form.value.soLuongNhap),
    ghiChu: form.value.ghiChu?.trim() || null,
  })
}
</script>

<template>
  <div v-if="open" class="modal-overlay" @click.self="emit('close')">
    <div class="modal-panel" style="max-width: 520px">
      <div class="px-5 py-4 border-b flex justify-between" style="border-color: var(--admin-border)">
        <div>
          <h2 class="text-lg font-semibold">Nhập lô hàng</h2>
          <p v-if="variant" class="text-sm text-[var(--admin-muted)]">{{ variant.sku }}</p>
        </div>
        <button type="button" class="admin-btn admin-btn-default !px-2" @click="emit('close')">✕</button>
      </div>
      <div class="p-5 grid grid-cols-1 md:grid-cols-2 gap-4">
        <div class="md:col-span-2">
          <label class="admin-label">Số lô *</label>
          <input v-model="form.soLo" class="admin-input" placeholder="Số lô nhà sản xuất" />
        </div>
        <div>
          <label class="admin-label">Ngày nhập *</label>
          <input v-model="form.ngayNhap" type="date" class="admin-input" />
        </div>
        <div>
          <label class="admin-label">Hạn sử dụng</label>
          <input v-model="form.hanSuDung" type="date" class="admin-input" />
        </div>
        <div class="md:col-span-2">
          <label class="admin-label">Số lượng nhập *</label>
          <input v-model.number="form.soLuongNhap" type="number" min="1" class="admin-input" />
        </div>
        <div class="md:col-span-2">
          <label class="admin-label">Ghi chú</label>
          <input v-model="form.ghiChu" class="admin-input" placeholder="Tùy chọn" />
        </div>
      </div>
      <div class="px-5 py-4 border-t flex justify-end gap-3" style="border-color: var(--admin-border)">
        <button type="button" class="admin-btn admin-btn-default" @click="emit('close')">Hủy</button>
        <button type="button" class="admin-btn admin-btn-primary" :disabled="loading" @click="submit">
          {{ loading ? 'Đang lưu...' : 'Nhập lô' }}
        </button>
      </div>
    </div>
  </div>
</template>
