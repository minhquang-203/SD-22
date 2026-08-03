<script setup>
import { formatDate } from '@/utils/format'

defineProps({
  open: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  variant: { type: Object, default: null },
  lots: { type: Array, default: () => [] },
  canWrite: { type: Boolean, default: true },
})

const emit = defineEmits(['close', 'edit'])
</script>

<template>
  <div v-if="open" class="modal-overlay" @click.self="emit('close')">
    <div class="modal-panel" style="max-width: 820px">
      <div class="px-5 py-4 border-b flex justify-between items-center" style="border-color: var(--admin-border)">
        <div>
          <h2 class="text-lg font-semibold">Lô hàng — {{ variant?.sku }}</h2>
          <p class="text-sm text-[var(--admin-muted)]">
            <template v-if="canWrite">
              Lô chỉ tạo qua phiếu nhập. Tại đây xem tồn và sửa nhẹ HSD / ghi chú.
            </template>
            <template v-else>
              Lô chỉ tạo qua phiếu nhập. Tại đây xem tồn kho theo lô.
            </template>
          </p>
        </div>
        <button type="button" class="admin-btn admin-btn-default !px-2" @click="emit('close')">✕</button>
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
              <th>SL lỗi</th>
              <th v-if="canWrite" class="text-right">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td :colspan="canWrite ? 7 : 6" class="text-center py-8 text-[var(--admin-muted)]">Đang tải...</td>
            </tr>
            <tr v-else-if="lots.length === 0">
              <td :colspan="canWrite ? 7 : 6" class="text-center py-8 text-[var(--admin-muted)]">
                Chưa có lô. Vào menu <strong>Nhập hàng</strong> để lập phiếu và nhập kho.
              </td>
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
              <td>
                <span
                  v-if="Number(lot.soLuongLoi) > 0"
                  class="font-semibold text-[#a83a3a]"
                  title="Hàng lỗi/hỏng đã ghi nhận — không bán lại"
                >
                  {{ lot.soLuongLoi }}
                  <span class="ml-1 inline-block text-[10px] px-2 py-0.5 rounded-full bg-red-100 text-red-800">
                    Có lỗi
                  </span>
                </span>
                <span v-else class="text-[var(--admin-muted)]">0</span>
              </td>
              <td v-if="canWrite" class="text-right whitespace-nowrap">
                <button
                  type="button"
                  class="admin-btn admin-btn-default !px-2 !py-1 text-xs"
                  @click="emit('edit', lot)"
                >
                  Sửa
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
