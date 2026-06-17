<script setup>
import { ref, watch } from 'vue'
import { formatDate } from '@/utils/format'

const props = defineProps({
  open: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  variant: { type: Object, default: null },
  lots: { type: Array, default: () => [] },
})

const emit = defineEmits(['close', 'open-nhap'])
</script>

<template>
  <div v-if="open" class="modal-overlay" @click.self="emit('close')">
    <div class="modal-panel" style="max-width: 720px">
      <div class="px-5 py-4 border-b flex justify-between items-center" style="border-color: var(--admin-border)">
        <div>
          <h2 class="text-lg font-semibold">Lô hàng — {{ variant?.sku }}</h2>
          <p class="text-sm text-[var(--admin-muted)]">Tồn kho = tổng số lượng còn của các lô</p>
        </div>
        <button type="button" class="admin-btn admin-btn-default !px-2" @click="emit('close')">✕</button>
      </div>

      <div class="px-5 py-3 border-b flex justify-end" style="border-color: var(--admin-border)">
        <button type="button" class="admin-btn admin-btn-primary" @click="emit('open-nhap')">+ Nhập lô</button>
      </div>

      <div class="p-5 overflow-x-auto">
        <table class="admin-table admin-table--striped w-full">
          <thead>
            <tr>
              <th>Số lô</th>
              <th>Ngày nhập</th>
              <th>HSD</th>
              <th>SL nhập</th>
              <th>Còn lại</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="5" class="text-center py-8 text-[var(--admin-muted)]">Đang tải...</td>
            </tr>
            <tr v-else-if="lots.length === 0">
              <td colspan="5" class="text-center py-8 text-[var(--admin-muted)]">Chưa có lô hàng. Nhấn "Nhập lô" để thêm.</td>
            </tr>
            <tr v-for="lot in lots" :key="lot.id">
              <td class="font-medium">{{ lot.soLo }}</td>
              <td>{{ formatDate(lot.ngayNhap) }}</td>
              <td>
                <span>{{ lot.hanSuDung ? formatDate(lot.hanSuDung) : '—' }}</span>
                <span
                  v-if="lot.sapHetHan"
                  class="ml-2 inline-block text-[10px] px-2 py-0.5 rounded-full bg-amber-100 text-amber-800"
                >
                  Sắp hết hạn
                </span>
              </td>
              <td>{{ lot.soLuongNhap }}</td>
              <td class="font-semibold text-[var(--admin-primary)]">{{ lot.soLuongCon }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
