<script setup>
import { computed, ref, watch } from 'vue'
import { confirm } from '@/composables/useConfirm'
import { formatDate } from '@/utils/format'

const props = defineProps({
  open: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  mode: { type: String, default: 'add' }, // 'add' | 'edit'
  variant: { type: Object, default: null },
  tenSanPham: { type: String, default: '' },
  initial: { type: Object, default: null },
})

const emit = defineEmits(['close', 'submit'])

const form = ref({
  soLo: '',
  ngayNhap: new Date().toISOString().slice(0, 10),
  hanSuDung: '',
  soLuongNhap: null,
  ghiChu: '',
})

const quantityLocked = computed(() => {
  if (props.mode !== 'edit' || !props.initial) return false
  return Number(props.initial.soLuongCon) < Number(props.initial.soLuongNhap)
})

const title = computed(() => (props.mode === 'edit' ? 'Sửa lô hàng' : 'Nhập lô hàng'))
const submitLabel = computed(() => {
  if (props.loading) return 'Đang lưu...'
  return props.mode === 'edit' ? 'Cập nhật' : 'Nhập lô'
})

watch(
  () => [props.open, props.mode, props.initial],
  ([isOpen]) => {
    if (!isOpen) return
    if (props.mode === 'edit' && props.initial) {
      form.value = {
        soLo: props.initial.soLo || '',
        ngayNhap: props.initial.ngayNhap ? String(props.initial.ngayNhap).slice(0, 10) : '',
        hanSuDung: props.initial.hanSuDung ? String(props.initial.hanSuDung).slice(0, 10) : '',
        soLuongNhap: props.initial.soLuongNhap ?? null,
        ghiChu: props.initial.ghiChu || '',
      }
      return
    }
    form.value = {
      soLo: '',
      ngayNhap: new Date().toISOString().slice(0, 10),
      hanSuDung: '',
      soLuongNhap: null,
      ghiChu: '',
    }
  },
)

function validateClient() {
  if (!form.value.soLo?.trim()) {
    alert('Vui lòng nhập số lô')
    return false
  }
  if (!form.value.ngayNhap) {
    alert('Vui lòng chọn ngày nhập')
    return false
  }
  const qty = Number(form.value.soLuongNhap)
  if (!Number.isInteger(qty) || qty <= 0) {
    alert('Số lượng nhập phải là số nguyên lớn hơn 0')
    return false
  }
  if (form.value.hanSuDung && form.value.hanSuDung <= form.value.ngayNhap) {
    alert('Hạn sử dụng phải sau ngày nhập')
    return false
  }
  return true
}

async function submit() {
  if (!validateClient()) return

  const payload = {
    idChiTietSanPham: props.variant?.id,
    soLo: form.value.soLo.trim(),
    ngayNhap: form.value.ngayNhap,
    hanSuDung: form.value.hanSuDung || null,
    soLuongNhap: Number(form.value.soLuongNhap),
    ghiChu: form.value.ghiChu?.trim() || null,
  }

  if (props.mode === 'add') {
    const lines = [
      `${props.tenSanPham || 'Sản phẩm'} — SKU ${props.variant?.sku || '—'}`,
      `Số lô: ${payload.soLo}`,
      `Ngày nhập: ${formatDate(payload.ngayNhap)}`,
      `Hạn sử dụng: ${payload.hanSuDung ? formatDate(payload.hanSuDung) : 'chưa nhập'}`,
      `Số lượng: ${payload.soLuongNhap}`,
    ]
    if (payload.soLuongNhap > 10000) {
      lines.push('⚠ Số lượng lớn bất thường (> 10.000). Hãy kiểm tra lại trước khi xác nhận.')
    }
    const ok = await confirm({
      title: 'Xác nhận nhập lô',
      confirmText: 'Xác nhận nhập',
      cancelText: 'Kiểm tra lại',
      message: lines.join('\n'),
    })
    if (!ok) return
  }

  emit('submit', payload)
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
          <input
            v-model.number="form.soLuongNhap"
            type="number"
            min="1"
            step="1"
            class="admin-input"
            :disabled="quantityLocked"
          />
          <p v-if="quantityLocked" class="mt-1 text-xs text-amber-700">
            Lô đã bán, không sửa được số lượng
          </p>
        </div>
        <div class="md:col-span-2">
          <label class="admin-label">Ghi chú</label>
          <input
            v-model="form.ghiChu"
            class="admin-input"
            placeholder="Tùy chọn"
            :disabled="quantityLocked"
          />
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
