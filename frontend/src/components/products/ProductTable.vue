<script setup>
import { NPopover } from 'naive-ui'
import { formatDate } from '@/utils/format'
import { resolveProductImageUrl } from '@/utils/productForm'
import AdminSwitch from '@/components/admin/AdminSwitch.vue'

defineProps({
  products: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  page: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 },
})

const emit = defineEmits(['edit', 'toggle-status', 'manage'])
</script>

<template>
  <div class="overflow-x-auto">
    <table class="admin-table admin-table--striped admin-table--products">
      <thead>
        <tr>
          <th>STT</th>
          <th>Mã SP</th>
          <th class="product-thumb-cell">Ảnh</th>
          <th>Tên sản phẩm</th>
          <th>Thương hiệu</th>
          <th>Danh mục</th>
          <th>Dạng SP</th>
          <th>SPF / PA</th>
          <th>Ngày tạo</th>
          <th class="text-center">Trạng thái</th>
          <th class="text-center">Thao tác</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="loading">
          <td colspan="11" class="text-center py-10 text-[var(--admin-muted)]">
            Đang tải dữ liệu...
          </td>
        </tr>
        <tr v-else-if="products.length === 0">
          <td colspan="11" class="text-center py-10 text-[var(--admin-muted)]">
            Không có sản phẩm phù hợp
          </td>
        </tr>
        <tr v-for="(item, index) in products" :key="item.id">
          <td>{{ (page - 1) * pageSize + index + 1 }}</td>
          <td>
            <span class="font-semibold text-[var(--admin-primary)]">{{ item.maSanPham }}</span>
          </td>
          <td class="product-thumb-cell">
            <NPopover
              v-if="item.anhChinhUrl"
              trigger="hover"
              placement="right"
              :show-arrow="false"
              :delay="120"
              :duration="100"
            >
              <template #trigger>
                <img
                  :src="resolveProductImageUrl(item.anhChinhUrl)"
                  :alt="item.ten"
                  class="product-thumb cursor-zoom-in"
                />
              </template>
              <img
                :src="resolveProductImageUrl(item.anhChinhUrl)"
                :alt="item.ten"
                class="product-thumb-popover"
              />
            </NPopover>
            <div v-else class="product-thumb--empty">
              Chưa có ảnh
            </div>
          </td>
          <td class="max-w-[240px]">
            <div class="font-medium">{{ item.ten }}</div>
            <div v-if="item.loaiChongNang" class="text-xs text-[var(--admin-muted)]">
              {{ item.loaiChongNang }}
            </div>
          </td>
          <td>{{ item.tenThuongHieu || '—' }}</td>
          <td>{{ item.tenDanhMuc || '—' }}</td>
          <td>{{ item.tenDangSanPham || '—' }}</td>
          <td>
            <div>{{ item.chiSoSpf || '—' }}</div>
            <div class="text-xs text-[var(--admin-muted)]">{{ item.chiSoPa || '—' }}</div>
          </td>
          <td>{{ formatDate(item.ngayTao) }}</td>
          <td class="text-center">
            <div class="flex flex-col items-center gap-1">
              <AdminSwitch
                :model-value="item.trangThai !== false"
                @update:model-value="emit('toggle-status', item)"
              />
              <span
                class="text-xs"
                :class="item.trangThai !== false ? 'text-[var(--admin-primary)]' : 'text-[var(--admin-muted)]'"
              >
                {{ item.trangThai !== false ? 'Hoạt động' : 'Ngưng' }}
              </span>
            </div>
          </td>
          <td>
            <div class="flex items-center justify-center gap-2">
              <button
                type="button"
                class="admin-btn admin-btn-default !px-2.5 !py-2"
                title="Quản lý biến thể (SKU, giá, tồn)"
                @click="emit('manage', item)"
              >
                📦
              </button>
              <button
                type="button"
                class="admin-btn admin-btn-default !px-2.5 !py-2"
                title="Sửa"
                @click="emit('edit', item)"
              >
                ✏️
              </button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
