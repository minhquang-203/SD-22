<script setup>
import { suggestSku, suggestVariantLabel } from '@/utils/productForm'

const rows = defineModel({ type: Array, required: true })

const props = defineProps({
  mauSacOptions: { type: Array, default: () => [] },
  maSanPham: { type: String, default: '' },
  tenSanPham: { type: String, default: '' },
  /** Lỗi theo index dòng: { [index]: { sku?, dungTichMl?, giaBan? } } */
  errors: { type: Object, default: () => ({}) },
})

const emit = defineEmits(['clear-row-error'])

function rowError(index, key) {
  return props.errors?.[index]?.[key] || ''
}

function clearRowError(index, key) {
  emit('clear-row-error', index, key)
}

function tenMauById(idMauSac) {
  if (idMauSac == null) return ''
  return props.mauSacOptions.find((m) => m.id === idMauSac)?.ten || ''
}

function otherSkus(exceptRow) {
  return (rows.value || [])
    .filter((r) => r !== exceptRow)
    .map((r) => r.sku)
    .filter(Boolean)
}

function variantLabel(row) {
  return suggestVariantLabel(props.tenSanPham, row.dungTichMl, tenMauById(row.idMauSac))
}

/** Chỉ tự sinh cho biến thể MỚI; không đụng SKU cũ; dừng nếu user sửa tay. */
function applySuggest(row) {
  if (row?.id != null) return
  if (row?._skuManual) return
  const next = suggestSku(
    props.maSanPham,
    row.dungTichMl,
    tenMauById(row.idMauSac),
    otherSkus(row),
  )
  if (!next) return
  row.sku = next
  row._lastSuggested = next
}

function onSkuInput(row) {
  const val = String(row.sku || '').trim()
  if (!val) {
    row._skuManual = false
    applySuggest(row)
    return
  }
  if (val !== row._lastSuggested) {
    row._skuManual = true
  }
}

function onVariantFieldChange(row) {
  applySuggest(row)
}

function addRow() {
  const row = {
    sku: '',
    idMauSac: null,
    dungTichMl: null,
    giaBan: null,
    _skuManual: false,
    _lastSuggested: '',
  }
  rows.value.push(row)
  applySuggest(row)
}

function removeRow(index) {
  rows.value.splice(index, 1)
}
</script>

<template>
  <div>
    <div class="flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between mb-3">
      <div class="min-w-0">
        <h4 class="font-semibold text-sm mb-1">Chi tiết sản phẩm / biến thể</h4>
        <p class="text-xs text-[var(--admin-muted)] leading-relaxed m-0">
          Biến thể là phiên bản theo dung tích/màu của sản phẩm — VD cùng 1 kem có loại 50ml và 100ml.
          Mã và tên tự tạo, bạn chỉ cần nhập dung tích và giá.
        </p>
      </div>
      <button type="button" class="admin-btn admin-btn-success shrink-0" @click="addRow">
        + Thêm biến thể
      </button>
    </div>

    <div class="overflow-x-auto border rounded-lg">
      <table class="admin-table">
        <thead>
          <tr>
            <th>
              SKU *
              <span
                class="sku-help"
                title="Mã định danh biến thể. Hệ thống tự đề xuất theo mã SP + dung tích + màu; bạn có thể sửa tay."
              >?</span>
            </th>
            <th>Màu sắc</th>
            <th>Dung tích *</th>
            <th>Giá bán *</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="rows.length === 0">
            <td colspan="5" class="text-center py-6 text-[var(--admin-muted)]">
              Chưa có biến thể — nhấn "Thêm biến thể", rồi nhập dung tích và giá
            </td>
          </tr>
          <tr v-for="(row, index) in rows" :key="row.id ?? `new-${index}`">
            <td class="min-w-[180px]">
              <input
                v-model="row.sku"
                class="admin-input"
                :class="{ 'is-invalid': rowError(index, 'sku') }"
                placeholder="VD: SP006-50ML"
                :title="row.id != null ? 'SKU đã lưu — giữ nguyên trừ khi bạn đổi tay' : ''"
                @input="onSkuInput(row); clearRowError(index, 'sku')"
              />
              <p v-if="rowError(index, 'sku')" class="field-error">{{ rowError(index, 'sku') }}</p>
              <p v-else class="sku-hint">
                {{
                  row.id != null
                    ? 'SKU đã có — không tự đổi khi sửa dung tích/màu'
                    : 'Mã tự sinh (có thể sửa)'
                }}
              </p>
              <p v-if="variantLabel(row)" class="sku-hint sku-hint--label">
                Nhãn gợi ý: {{ variantLabel(row) }}
              </p>
            </td>
            <td>
              <select
                v-model="row.idMauSac"
                class="admin-select"
                @change="onVariantFieldChange(row)"
              >
                <option :value="null">Không màu</option>
                <option v-for="m in mauSacOptions" :key="m.id" :value="m.id">{{ m.ten }}</option>
              </select>
            </td>
            <td>
              <div class="dung-tich-field">
                <input
                  v-model.number="row.dungTichMl"
                  type="number"
                  class="admin-input"
                  :class="{ 'is-invalid': rowError(index, 'dungTichMl') }"
                  min="0"
                  step="0.1"
                  placeholder="VD: 50"
                  @input="onVariantFieldChange(row); clearRowError(index, 'dungTichMl')"
                />
                <span class="dung-tich-unit">ml</span>
              </div>
              <p v-if="rowError(index, 'dungTichMl')" class="field-error">{{ rowError(index, 'dungTichMl') }}</p>
            </td>
            <td>
              <input
                v-model.number="row.giaBan"
                type="number"
                class="admin-input"
                :class="{ 'is-invalid': rowError(index, 'giaBan') }"
                min="0"
                step="1000"
                placeholder="VD: 460000"
                @input="clearRowError(index, 'giaBan')"
              />
              <p v-if="rowError(index, 'giaBan')" class="field-error">{{ rowError(index, 'giaBan') }}</p>
            </td>
            <td>
              <button type="button" class="admin-btn admin-btn-danger !px-2.5" @click="removeRow(index)">
                ✕
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <p class="text-xs text-[var(--admin-muted)] mt-2">
      Tồn kho và HSD quản lý theo lô sau khi tạo sản phẩm (màn biến thể → Lô hàng).
    </p>
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
.field-error {
  margin: 4px 0 0;
  font-size: 11px;
  line-height: 1.35;
  color: #c45c5c;
}
.admin-input.is-invalid,
.admin-select.is-invalid {
  border-color: #c45c5c !important;
}
</style>
