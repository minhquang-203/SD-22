<script setup>
import { ref, watch } from 'vue'
import { suggestSku } from '@/utils/productForm'

const props = defineProps({
  open: { type: Boolean, default: false },
  mode: { type: String, default: 'add' },
  loading: { type: Boolean, default: false },
  productId: { type: Number, default: null },
  maSanPham: { type: String, default: '' },
  initial: { type: Object, default: null },
  mauSacOptions: { type: Array, default: () => [] },
})

const emit = defineEmits(['close', 'submit'])

const form = ref({
  sku: '',
  idMauSac: null,
  dungTichMl: null,
  giaBan: null,
})

watch(
  () => [props.open, props.initial],
  () => {
    if (!props.open) return
    if (props.initial) {
      form.value = {
        sku: props.initial.sku || '',
        idMauSac: props.initial.idMauSac ?? null,
        dungTichMl: props.initial.dungTichMl ?? null,
        giaBan: props.initial.giaBan ?? null,
      }
    } else {
      form.value = { sku: '', idMauSac: null, dungTichMl: null, giaBan: null }
    }
  },
  { immediate: true },
)

watch(
  () => form.value.dungTichMl,
  (vol) => {
    if (props.mode !== 'add' || !props.open) return
    if (!form.value.sku?.trim()) {
      form.value.sku = suggestSku(props.maSanPham, vol)
    }
  },
)

function submit() {
  if (!form.value.sku?.trim()) return alert('SKU không được để trống')
  if (!form.value.giaBan || Number(form.value.giaBan) <= 0) return alert('Giá bán phải lớn hơn 0')
  emit('submit', {
    ...form.value,
    idSanPham: props.productId,
    sku: form.value.sku.trim(),
  })
}
</script>

<template>
  <div v-if="open" class="modal-overlay" @click.self="emit('close')">
    <div class="modal-panel" style="max-width: 560px">
      <div class="px-5 py-4 border-b flex justify-between" style="border-color: var(--admin-border)">
        <h2 class="text-lg font-semibold">
          {{ mode === 'add' ? 'Thêm biến thể' : 'Cập nhật biến thể' }}
        </h2>
        <button type="button" class="admin-btn admin-btn-default !px-2" @click="emit('close')">✕</button>
      </div>
      <div class="p-5 grid grid-cols-1 md:grid-cols-2 gap-4">
        <p class="md:col-span-2 text-sm text-[var(--admin-muted)]">
          Tồn kho được quản lý theo lô — sau khi tạo biến thể, dùng nút <strong>Lô hàng</strong> để nhập tồn.
        </p>
        <div class="md:col-span-2">
          <label class="admin-label">SKU *</label>
          <input v-model="form.sku" class="admin-input" placeholder="VD: ANESSA-60" />
        </div>
        <div>
          <label class="admin-label">Màu sắc</label>
          <select v-model="form.idMauSac" class="admin-select">
            <option :value="null">Không màu</option>
            <option v-for="m in mauSacOptions" :key="m.id" :value="m.id">{{ m.ten }}</option>
          </select>
        </div>
        <div>
          <label class="admin-label">Dung tích (ml)</label>
          <input v-model.number="form.dungTichMl" type="number" class="admin-input" min="0" step="0.1" />
        </div>
        <div class="md:col-span-2">
          <label class="admin-label">Giá bán *</label>
          <input v-model.number="form.giaBan" type="number" class="admin-input" min="0" step="1000" />
        </div>
      </div>
      <div class="px-5 py-4 border-t flex justify-end gap-3" style="border-color: var(--admin-border)">
        <button type="button" class="admin-btn admin-btn-default" @click="emit('close')">Hủy</button>
        <button type="button" class="admin-btn admin-btn-primary" :disabled="loading" @click="submit">
          {{ loading ? 'Đang lưu...' : 'Lưu thay đổi' }}
        </button>
      </div>
    </div>
  </div>
</template>
