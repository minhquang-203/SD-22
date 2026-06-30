<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import OrderCard from '@/components/storefront/OrderCard.vue'
import { fetchChiTietDonCuaToi, fetchDonCuaToi } from '@/api/donHangApi'

const search = ref('')
const currentFilter = ref('all')
const loading = ref(true)
const orders = ref([])
const error = ref('')

const filters = [
  { value: 'all', label: 'Tất cả' },
  { value: 'shipping', label: 'Đang giao' },
  { value: 'processing', label: 'Đang xử lý' },
  { value: 'delivered', label: 'Đã giao' },
  { value: 'cancelled', label: 'Đã hủy' },
]

onMounted(loadOrders)

const filteredOrders = computed(() => {
  const q = search.value.trim().toLowerCase()
  return orders.value.filter((order) => {
    const matchFilter = currentFilter.value === 'all' || statusGroup(order.trangThai) === currentFilter.value
    const matchSearch = !q ||
      String(order.maHoaDon || '').toLowerCase().includes(q) ||
      (order.chiTiets || []).some((line) => String(line.tenSanPham || '').toLowerCase().includes(q))
    return matchFilter && matchSearch
  })
})

async function loadOrders() {
  loading.value = true
  error.value = ''
  try {
    const res = await fetchDonCuaToi()
    const summaries = res.data || []
    orders.value = await Promise.all(summaries.map(loadDetailOrSummary))
  } catch {
    error.value = 'Không tải được danh sách đơn hàng.'
  } finally {
    loading.value = false
  }
}

async function loadDetailOrSummary(summary) {
  try {
    const res = await fetchChiTietDonCuaToi(summary.id)
    return res.data || summary
  } catch {
    return { ...summary, chiTiets: [] }
  }
}

function statusGroup(status) {
  const map = {
    CHO_XAC_NHAN: 'processing',
    DA_XAC_NHAN: 'processing',
    DANG_CHUAN_BI: 'processing',
    DANG_GIAO: 'shipping',
    HOAN_THANH: 'delivered',
    DA_HUY: 'cancelled',
  }
  return map[status] || 'processing'
}
</script>

<template>
  <div class="sf-order-page">
    <div class="sf-container">
      <nav class="sf-breadcrumb">
        <RouterLink to="/">Trang chủ</RouterLink>
        <span>/</span>
        <span>Đơn hàng của tôi</span>
      </nav>

      <section class="sf-order-content">
      
        <div class="sf-order-filter-bar">
          <label class="sf-order-filter-search">
            <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
              <circle cx="11" cy="11" r="8" />
              <path d="m21 21-4.35-4.35" />
            </svg>
            <input v-model="search" type="search" placeholder="Tìm theo tên sản phẩm, mã đơn..." autocomplete="off" />
          </label>

          <div class="sf-order-filter-chips">
            <button
              v-for="filter in filters"
              :key="filter.value"
              type="button"
              class="sf-order-chip"
              :class="{ 'sf-order-chip--active': currentFilter === filter.value }"
              @click="currentFilter = filter.value"
            >
              {{ filter.label }}
            </button>
          </div>
        </div>

        <p v-if="error" class="sf-order-msg sf-order-msg--err">{{ error }}</p>

        <div v-if="loading" class="sf-order-skeleton" />

        <div v-else-if="filteredOrders.length" class="sf-order-list">
          <OrderCard
            v-for="(order, idx) in filteredOrders"
            :key="order.id"
            :order="order"
            :default-open="idx === 0"
          />
        </div>

        <div v-else class="sf-order-empty">
          <div class="sf-order-empty__emoji">📦</div>
          <h3>Không có đơn hàng nào</h3>
          <p>Chưa có đơn hàng phù hợp với bộ lọc bạn chọn.</p>
        </div>
      </section>
    </div>
  </div>
</template>
