<script setup>
import { suggestSku } from '@/utils/productForm'

const rows = defineModel({ type: Array, required: true })

const props = defineProps({
  mauSacOptions: { type: Array, default: () => [] },
  maSanPham: { type: String, default: '' },
})

function onDungTichChange(row) {
  if (!row.sku?.trim()) {
    row.sku = suggestSku(props.maSanPham, row.dungTichMl)
  }
}

function addRow() {
  rows.value.push({
    sku: '',
    idMauSac: null,
    dungTichMl: null,
    giaBan: null,
    soLuongTon: 0,
    hanSuDung: '',
  })
}

function removeRow(index) {
  rows.value.splice(index, 1)
}
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-3">
      <h4 class="font-semibold text-sm">Chi tiết sản phẩm / SKU</h4>
      <button type="button" class="admin-btn admin-btn-success" @click="addRow">
        + Thêm biến thể
      </button>
    </div>

    <div class="overflow-x-auto border rounded-lg">
      <table class="admin-table">
        <thead>
          <tr>
            <th>SKU *</th>
            <th>Màu sắc</th>
            <th>Dung tích (ml)</th>
            <th>Giá bán *</th>
            <th>Tồn kho</th>
            <th>Hạn dùng</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="rows.length === 0">
            <td colspan="7" class="text-center py-6 text-[var(--admin-muted)]">
              Chưa có biến thể — nhấn "Thêm biến thể"
            </td>
          </tr>
          <tr v-for="(row, index) in rows" :key="index">
            <td>
              <input v-model="row.sku" class="admin-input" placeholder="VD: ANESSA-60" />
            </td>
            <td>
              <select v-model="row.idMauSac" class="admin-select">
                <option :value="null">Không màu</option>
                <option v-for="m in mauSacOptions" :key="m.id" :value="m.id">{{ m.ten }}</option>
              </select>
            </td>
            <td>
              <input
                v-model.number="row.dungTichMl"
                type="number"
                class="admin-input"
                min="0"
                step="0.1"
                @input="onDungTichChange(row)"
              />
            </td>
            <td>
              <input v-model.number="row.giaBan" type="number" class="admin-input" min="0" step="1000" />
            </td>
            <td>
              <input v-model.number="row.soLuongTon" type="number" class="admin-input" min="0" />
            </td>
            <td>
              <input v-model="row.hanSuDung" type="date" class="admin-input" />
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
      Mỗi dòng là một biến thể (vd: 60ml, 90ml). SKU gợi ý theo mã sản phẩm + dung tích khi ô SKU đang trống.
    </p>
  </div>
</template>
