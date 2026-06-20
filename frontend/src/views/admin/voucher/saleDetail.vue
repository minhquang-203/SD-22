<template>
  <div class="sale-page">
    <div class="page-header">
      <div>
        <button class="btn btn-secondary" @click="goBack">
          <i class="ti ti-arrow-left"></i> Quay lại
        </button>
        <h1 class="page-title">{{ sale?.ten || 'Chi tiết đợt giảm giá' }}</h1>
        <p class="page-sub">
          <span v-if="sale">{{ sale.ma }} · {{ sale.phanTramGiam }}% · {{ sale.timeStatusLabel }}</span>
          <span v-else-if="loading">Đang tải...</span>
        </p>
      </div>
    </div>

    <div v-if="errorMessage" class="table-card" style="padding: 24px; color: var(--coral, #d4624a);">
      {{ errorMessage }}
    </div>

    <template v-else-if="sale">
      <div class="metrics-grid" style="grid-template-columns: repeat(3, 1fr);">
        <div class="metric-card">
          <div class="metric-label">Ngày bắt đầu</div>
          <div class="metric-value" style="font-size: 18px;">{{ formatDate(sale.ngayBatDau) }}</div>
        </div>
        <div class="metric-card">
          <div class="metric-label">Ngày kết thúc</div>
          <div class="metric-value" style="font-size: 18px;">{{ formatDate(sale.ngayKetThuc) }}</div>
        </div>
        <div class="metric-card">
          <div class="metric-label">Phần trăm giảm</div>
          <div class="metric-value" style="font-size: 18px;">{{ sale.phanTramGiam }}%</div>
        </div>
      </div>

      <div class="table-card" style="margin-top: 20px;">
        <div class="page-header" style="padding: 18px 24px; margin: 0; border-bottom: 1px solid var(--sand, #ede5d8);">
          <div>
            <h2 class="page-title" style="font-size: 20px;">Sản phẩm áp dụng</h2>
            <p class="page-sub">{{ products.length }} biến thể</p>
          </div>
        </div>
        <table>
          <thead>
            <tr>
              <th>Sản phẩm</th>
              <th>SKU</th>
              <th>Giá bán</th>
              <th>Giá sau giảm</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loadingProducts">
              <td colspan="4">
                <div class="empty-state"><p>Đang tải sản phẩm...</p></div>
              </td>
            </tr>
            <tr v-else-if="!products.length">
              <td colspan="4">
                <div class="empty-state"><p>Chưa có sản phẩm trong đợt giảm giá này</p></div>
              </td>
            </tr>
            <tr v-for="item in products" :key="item.id">
              <td>{{ item.tenSanPham || '—' }}</td>
              <td>{{ item.sku || '—' }}</td>
              <td>{{ formatCurrency(item.giaBan) }}</td>
              <td>{{ formatCurrency(item.giaSauGiam) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getSaleById, getSaleProducts } from '@/api/saleApi.js'

const route = useRoute()
const router = useRouter()

const sale = ref(null)
const products = ref([])
const loading = ref(false)
const loadingProducts = ref(false)
const errorMessage = ref('')

function goBack() {
  router.push('/admin/sale')
}

function formatDate(value) {
  if (!value) return '—'
  return new Intl.DateTimeFormat('vi-VN').format(new Date(value))
}

function formatCurrency(value) {
  const num = Number(value)
  if (Number.isNaN(num)) return '—'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(num)
}

async function loadDetail() {
  const id = route.params.id
  if (!id) return

  loading.value = true
  errorMessage.value = ''
  try {
    const res = await getSaleById(id)
    sale.value = res.data
  } catch (error) {
    sale.value = null
    errorMessage.value = typeof error === 'string' ? error : error?.message || 'Không tải được đợt giảm giá'
  } finally {
    loading.value = false
  }
}

async function loadProducts() {
  const id = route.params.id
  if (!id) return

  loadingProducts.value = true
  try {
    const res = await getSaleProducts(id)
    products.value = res.data || []
  } catch {
    products.value = []
  } finally {
    loadingProducts.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadDetail(), loadProducts()])
})
</script>

<style scoped>
@import '@/styles/saleCss.css';
</style>
