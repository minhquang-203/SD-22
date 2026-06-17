<script setup>
import { NPopover } from 'naive-ui'
import { Icon } from '@iconify/vue'
import { formatDate } from '@/utils/format'
import { resolveProductImageUrl } from '@/utils/productForm'
import StatusDot from '@/components/ui/StatusDot.vue'

defineProps({
  products: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  page: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 },
})

const emit = defineEmits(['edit', 'toggle-status', 'toggle-noi-bat', 'manage'])

function formatLoaiChongNang(value) {
  const map = {
    VAT_LY: 'Vật lý',
    HOA_HOC: 'Hóa học',
    LAI: 'Vật lý và Hóa học',
  }
  return map[value] || value
}
</script>

<template>
  <div class="overflow-x-auto">
    <table class="soleil-table soleil-table--product admin-table--soleil">
      <thead>
        <tr>
          <th>STT</th>
          <th>Mã SP</th>
          <th>Ảnh</th>
          <th>Tên sản phẩm</th>
          <th>Thương hiệu</th>
          <th>Danh mục</th>
          <th>Dạng SP</th>
          <th>SPF / PA</th>
          <th>Ngày tạo</th>
          <th>Trạng thái</th>
          <th>Nổi bật</th>
          <th />
        </tr>
      </thead>
      <tbody>
        <tr v-if="loading">
          <td colspan="12" class="text-center py-10 text-[var(--admin-muted)]">
            Đang tải dữ liệu...
          </td>
        </tr>
        <tr v-else-if="products.length === 0">
          <td colspan="12" class="text-center py-10 text-[var(--admin-muted)]">
            Không có sản phẩm phù hợp
          </td>
        </tr>
        <tr
          v-for="(item, index) in products"
          :key="item.id"
          class="soleil-table-row--clickable cursor-pointer"
          @click="emit('manage', item)"
        >
          <td class="text-[rgba(30,21,16,0.45)]">{{ (page - 1) * pageSize + index + 1 }}</td>
          <td>
            <span class="soleil-sp-code">{{ item.maSanPham }}</span>
          </td>
          <td>
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
                  class="product-thumb--table cursor-zoom-in"
                />
              </template>
              <img
                :src="resolveProductImageUrl(item.anhChinhUrl)"
                :alt="item.ten"
                class="product-thumb-popover"
              />
            </NPopover>
            <div v-else class="product-thumb--table-empty">Chưa có ảnh</div>
          </td>
          <td class="max-w-[260px]">
            <div class="font-medium text-[var(--ink)]">{{ item.ten }}</div>
            <div v-if="item.loaiChongNang" class="text-xs text-[rgba(30,21,16,0.45)] mt-0.5">
              {{ formatLoaiChongNang(item.loaiChongNang) }}
            </div>
          </td>
          <td>
            <span v-if="item.tenThuongHieu" class="soleil-pill--brand">{{ item.tenThuongHieu }}</span>
            <span v-else class="text-[rgba(30,21,16,0.35)]">—</span>
          </td>
          <td>
            <span v-if="item.tenDanhMuc" class="soleil-pill--category">{{ item.tenDanhMuc }}</span>
            <span v-else class="text-[rgba(30,21,16,0.35)]">—</span>
          </td>
          <td>
            <span v-if="item.tenDangSanPham" class="soleil-pill--form">{{ item.tenDangSanPham }}</span>
            <span v-else class="text-[rgba(30,21,16,0.35)]">—</span>
          </td>
          <td>
            <div class="flex flex-wrap gap-1">
              <span v-if="item.chiSoSpf" class="soleil-pill--spf">{{ item.chiSoSpf }}</span>
              <span v-if="item.chiSoPa" class="soleil-pill--spf">{{ item.chiSoPa }}</span>
              <span v-if="!item.chiSoSpf && !item.chiSoPa" class="text-[rgba(30,21,16,0.35)]">—</span>
            </div>
          </td>
          <td class="text-xs text-[rgba(30,21,16,0.55)]">{{ formatDate(item.ngayTao) }}</td>
          <td @click.stop>
            <button
              type="button"
              class="soleil-status-toggle"
              :title="item.trangThai !== false ? 'Nhấn để ngưng hoạt động' : 'Nhấn để kích hoạt'"
              @click="emit('toggle-status', item)"
            >
              <StatusDot
                :status="item.trangThai !== false ? 'active' : 'expired'"
                :label="item.trangThai !== false ? 'Đang hoạt động' : 'Ngưng hoạt động'"
              />
            </button>
          </td>
          <td @click.stop>
            <button
              type="button"
              class="soleil-status-toggle"
              :title="item.noiBat ? 'Nhấn để bỏ nổi bật' : 'Nhấn để đánh dấu nổi bật'"
              @click="emit('toggle-noi-bat', item)"
            >
              <StatusDot
                :status="item.noiBat ? 'upcoming' : 'paused'"
                :label="item.noiBat ? 'Nổi bật' : 'Thường'"
              />
            </button>
          </td>
          <td @click.stop>
            <div class="soleil-actions-cell">
              <button
                type="button"
                class="soleil-act-btn-round"
                title="Quản lý biến thể"
                @click="emit('manage', item)"
              >
                <Icon icon="icon-park-outline:box" />
              </button>
              <button
                type="button"
                class="soleil-act-btn-round"
                title="Sửa"
                @click="emit('edit', item)"
              >
                <Icon icon="icon-park-outline:edit" />
              </button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
