<script setup>
import { computed, ref, watch } from 'vue'
import { formatDate } from '@/utils/format'

const props = defineProps({
  open: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  variant: { type: Object, default: null },
  tenSanPham: { type: String, default: '' },
  initial: { type: Object, default: null },
})

const emit = defineEmits(['close', 'submit'])

const form = ref({
  soLo: '',
  ngayNhap: '',
  hanSuDung: '',
  soLuongNhap: null,
  ghiChu: '',
})

const title = computed(() => 'Sửa thông tin lô')
const submitLabel = computed(() => (props.loading ? 'Đang lưu...' : 'Cập nhật'))

watch(
  () => [props.open, props.initial],
  ([isOpen]) => {
    if (!isOpen || !props.initial) return
    form.value = {
      soLo: props.initial.soLo || '',
      ngayNhap: props.initial.ngayNhap ? String(props.initial.ngayNhap).slice(0, 10) : '',
      hanSuDung: props.initial.hanSuDung ? String(props.initial.hanSuDung).slice(0, 10) : '',
      soLuongNhap: props.initial.soLuongNhap ?? null,
      ghiChu: props.initial.ghiChu || '',
    }
  },
)

function validateClient() {
  if (form.value.hanSuDung && form.value.ngayNhap && form.value.hanSuDung <= form.value.ngayNhap) {
    alert('Hạn sử dụng phải sau ngày nhập')
    return false
  }
  return true
}

function submit() {
  if (!validateClient()) return
  emit('submit', {
    idChiTietSanPham: props.variant?.id,
    soLo: form.value.soLo,
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
          <h2 class="text-lg font-semibold">{{ title }}</h2>
          <p v-if="variant || tenSanPham" class="text-sm text-[var(--admin-muted)]">
            <span v-if="tenSanPham">{{ tenSanPham }}</span>
            <span v-if="tenSanPham && variant?.sku"> — </span>
            <span v-if="variant?.sku">{{ variant.sku }}</span>
          </p>
          <p class="text-xs text-[var(--admin-muted)] mt-1">
            Chỉ sửa HSD và ghi chú. Số lô / ngày nhập / SL do phiếu nhập quyết định.
          </p>
        </div>
        <button type="button" class="admin-btn admin-btn-default !px-2" @click="emit('close')">✕</button>
      </div>
      <div class="p-5 grid grid-cols-1 md:grid-cols-2 gap-4">
        <div class="md:col-span-2">
          <label class="admin-label">Số lô</label>
          <input :value="form.soLo" class="admin-input" disabled />
        </div>
        <div>
          <label class="admin-label">Ngày nhập</label>
          <input :value="form.ngayNhap" type="date" class="admin-input" disabled />
          <p v-if="form.ngayNhap" class="mt-1 text-xs text-[var(--admin-muted)]">
            {{ formatDate(form.ngayNhap) }}
          </p>
        </div>
        <div>
          <label class="admin-label">Hạn sử dụng</label>
          <input v-model="form.hanSuDung" type="date" class="admin-input" />
        </div>
        <div class="md:col-span-2">
          <label class="admin-label">Số lượng nhập</label>
          <input :value="form.soLuongNhap" type="number" class="admin-input" disabled />
        </div>
        <div class="md:col-span-2">
          <label class="admin-label">Ghi chú</label>
          <input v-model="form.ghiChu" class="admin-input" placeholder="Tùy chọn" />
        </div>
      </div>
      <div class="px-5 py-4 border-t flex justify-end gap-3" style="border-color: var(--admin-border)">
        <button type="button" class="admin-btn admin-btn-default" @click="emit('close')">Hủy</button>
        <button type="button" class="admin-btn admin-btn-primary" :disabled="loading" @click="submit">
          {{ submitLabel }}
        </button>
      </div>
    </div>
  </div>
</template>
