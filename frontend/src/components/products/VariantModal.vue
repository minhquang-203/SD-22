<script setup>
import { ref, computed, watch } from 'vue'
import { suggestSku, suggestVariantLabel } from '@/utils/productForm'

const props = defineProps({
  open: { type: Boolean, default: false },
  mode: { type: String, default: 'add' },
  loading: { type: Boolean, default: false },
  productId: { type: Number, default: null },
  maSanPham: { type: String, default: '' },
  tenSanPham: { type: String, default: '' },
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
const skuManual = ref(false)
const lastSuggested = ref('')

function tenMauById(idMauSac) {
  if (idMauSac == null) return ''
  return props.mauSacOptions.find((m) => m.id === idMauSac)?.ten || ''
}

const nhanGoiY = computed(() =>
  suggestVariantLabel(props.tenSanPham, form.value.dungTichMl, tenMauById(form.value.idMauSac)),
)

function applySuggest() {
  if (props.mode !== 'add' || !props.open) return
  if (skuManual.value) return
  const next = suggestSku(props.maSanPham, form.value.dungTichMl, tenMauById(form.value.idMauSac))
  if (!next) return
  form.value.sku = next
  lastSuggested.value = next
}

watch(
  () => [props.open, props.initial, props.mode],
  () => {
    if (!props.open) return
    skuManual.value = false
    lastSuggested.value = ''
    if (props.initial) {
      form.value = {
        sku: props.initial.sku || '',
        idMauSac: props.initial.idMauSac ?? null,
        dungTichMl: props.initial.dungTichMl ?? null,
        giaBan: props.initial.giaBan ?? null,
      }
      // Sửa biến thể cũ: không tự đổi SKU
      skuManual.value = true
    } else {
      form.value = { sku: '', idMauSac: null, dungTichMl: null, giaBan: null }
      applySuggest()
    }
  },
  { immediate: true },
)

watch(
  () => [form.value.dungTichMl, form.value.idMauSac],
  () => applySuggest(),
)

function onSkuInput() {
  const val = String(form.value.sku || '').trim()
  if (!val) {
    skuManual.value = false
    applySuggest()
    return
  }
  if (val !== lastSuggested.value) {
    skuManual.value = true
  }
}

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
        <p class="md:col-span-2 text-sm text-[var(--admin-muted)] m-0 leading-relaxed">
          Biến thể là phiên bản theo dung tích/màu của sản phẩm — VD cùng 1 kem có loại 50ml và 100ml.
          Mã và tên tự tạo, bạn chỉ cần nhập dung tích và giá. Tồn kho quản lý theo lô sau khi tạo.
        </p>
        <div class="md:col-span-2">
          <label class="admin-label">
            SKU *
            <span
              class="sku-help"
              title="Mã định danh biến thể. Hệ thống tự đề xuất theo mã SP + dung tích + màu; bạn có thể sửa tay."
            >?</span>
          </label>
          <input
            v-model="form.sku"
            class="admin-input"
            placeholder="VD: SP006-50ML"
            @input="onSkuInput"
          />
          <p class="sku-hint">
            {{
              mode === 'edit'
                ? 'SKU đã có — giữ nguyên trừ khi bạn đổi tay'
                : 'Mã tự sinh (có thể sửa)'
            }}
          </p>
          <p v-if="nhanGoiY" class="sku-hint sku-hint--label">Nhãn gợi ý: {{ nhanGoiY }}</p>
        </div>
        <div>
          <label class="admin-label">Màu sắc</label>
          <select v-model="form.idMauSac" class="admin-select">
            <option :value="null">Không màu</option>
            <option v-for="m in mauSacOptions" :key="m.id" :value="m.id">{{ m.ten }}</option>
          </select>
        </div>
        <div>
          <label class="admin-label">Dung tích</label>
          <div class="dung-tich-field">
            <input
              v-model.number="form.dungTichMl"
              type="number"
              class="admin-input"
              min="0"
              step="0.1"
              placeholder="VD: 50"
            />
            <span class="dung-tich-unit">ml</span>
          </div>
        </div>
        <div class="md:col-span-2">
          <label class="admin-label">Giá bán *</label>
          <input
            v-model.number="form.giaBan"
            type="number"
            class="admin-input"
            min="0"
            step="1000"
            placeholder="VD: 460000"
          />
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

<style scoped>
.sku-help {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 1rem;
  height: 1rem;
  margin-left: 4px;
  border-radius: 999px;
  font-size: 10px;
  font-weight: 700;
  color: var(--admin-muted);
  border: 1px solid var(--admin-border);
  cursor: help;
  vertical-align: middle;
}
.sku-hint {
  margin: 4px 0 0;
  font-size: 11px;
  color: var(--admin-muted);
  line-height: 1.3;
}
.sku-hint--label {
  color: #8c6b4a;
}
.dung-tich-field {
  display: flex;
  align-items: center;
  gap: 6px;
}
.dung-tich-field .admin-input {
  flex: 1;
  min-width: 0;
}
.dung-tich-unit {
  font-size: 12px;
  color: var(--admin-muted);
  flex-shrink: 0;
}
</style>
