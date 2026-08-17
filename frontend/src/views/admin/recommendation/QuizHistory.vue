<template>
  <div class="insight-page">
    <!-- Header -->
    <div class="insight-header">
      <h1 class="insight-title">Insight khách hàng</h1>
      <p class="insight-subtitle">Kết quả phân tích loại da và sản phẩm gợi ý cho từng khách hàng.</p>
    </div>

    <div class="insight-layout">
      <!-- LEFT PANEL: Customer list -->
      <aside class="insight-sidebar">
        <div class="sidebar-head">
          <div class="search-box">
            <Icon icon="icon-park-outline:search" class="search-icon" />
            <input
              v-model="searchQuery"
              type="text"
              placeholder="Tìm tên, SĐT, email..."
              class="search-input"
            />
          </div>
          <div class="sidebar-count">{{ filteredResults.length }} kết quả</div>
        </div>

        <div class="sidebar-list">
          <div v-if="loading" class="sidebar-empty">Đang tải...</div>
          <div v-else-if="filteredResults.length === 0" class="sidebar-empty">
            Không tìm thấy kết quả nào.
          </div>
          <button
            v-for="item in filteredResults"
            :key="item.id"
            class="customer-card"
            :class="{ 'customer-card--active': selectedId === item.id }"
            @click="selectCustomer(item)"
          >
            <div class="customer-avatar">
              {{ getInitial(item.tenKhachHang) }}
            </div>
            <div class="customer-info">
              <div class="customer-name">{{ item.tenKhachHang || 'Khách vãng lai' }}</div>
              <div class="customer-meta">
                <span class="skin-badge" :class="'skin-badge--' + item.maLoaiDa">
                  {{ item.tenLoaiDa }}
                </span>
                <span class="customer-date">{{ formatDate(item.thoiGian) }}</span>
              </div>
            </div>
          </button>
        </div>
      </aside>

      <!-- RIGHT PANEL: Detail -->
      <section class="insight-detail">
        <div v-if="!selected" class="detail-empty">
          <Icon icon="icon-park-outline:user-positioning" class="empty-icon" />
          <p>Chọn một khách hàng bên trái để xem chi tiết kết quả Quiz.</p>
        </div>

        <template v-else>
          <!-- Customer Info -->
          <div class="detail-section">
            <h3 class="detail-section-title">Thông tin khách hàng</h3>
            <div class="info-grid">
              <div class="info-item">
                <span class="info-label">Họ tên</span>
                <span class="info-value">{{ selected.tenKhachHang || 'Khách vãng lai' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">Số điện thoại</span>
                <span class="info-value">{{ selected.sdtKhachHang || '—' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">Email</span>
                <span class="info-value">{{ selected.emailKhachHang || '—' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">Thời gian làm Quiz</span>
                <span class="info-value">{{ formatDateTime(selected.thoiGian) }}</span>
              </div>
            </div>
          </div>

          <!-- Quiz Result -->
          <div class="detail-section">
            <h3 class="detail-section-title">Kết quả phân tích</h3>
            <div class="result-card">
              <div class="result-badge" :class="'skin-badge--' + selected.maLoaiDa">
                {{ selected.tenLoaiDa }}
              </div>
              <p class="result-desc">{{ selected.moTaLoaiDa || 'Chưa có mô tả cho loại da này.' }}</p>
            </div>
          </div>

          <!-- Recommended Products -->
          <div class="detail-section">
            <h3 class="detail-section-title">
              Sản phẩm gợi ý
              <span class="product-count" v-if="sanPhamGoiY.length">({{ sanPhamGoiY.length }})</span>
            </h3>
            <div v-if="loadingProducts" class="products-loading">Đang tải sản phẩm...</div>
            <div v-else-if="sanPhamGoiY.length === 0" class="products-empty">
              Chưa có sản phẩm gợi ý cho loại da này.
            </div>
            <div v-else class="products-grid">
              <div v-for="sp in sanPhamGoiY" :key="sp.id" class="product-card">
                <div class="product-img-wrap">
                  <img
                    :src="resolveImg(sp.anhChinhUrl)"
                    :alt="sp.ten"
                    class="product-img"
                    @error="onImgError"
                  />
                </div>
                <div class="product-name">{{ sp.ten }}</div>
                <div class="product-price" v-if="sp.giaBan">{{ formatPrice(sp.giaBan) }}</div>
              </div>
            </div>
          </div>
        </template>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import { getQuizHistory, getSanPhamGoiY } from '@/api/quizHistoryApi'

const results = ref([])
const loading = ref(true)
const searchQuery = ref('')
const selectedId = ref(null)
const selected = ref(null)
const sanPhamGoiY = ref([])
const loadingProducts = ref(false)

const filteredResults = computed(() => {
  if (!searchQuery.value.trim()) return results.value
  const q = searchQuery.value.toLowerCase().trim()
  return results.value.filter(r => {
    const name = (r.tenKhachHang || 'Khách vãng lai').toLowerCase()
    const phone = (r.sdtKhachHang || '').toLowerCase()
    const email = (r.emailKhachHang || '').toLowerCase()
    const skin = (r.tenLoaiDa || '').toLowerCase()
    return name.includes(q) || phone.includes(q) || email.includes(q) || skin.includes(q)
  })
})

onMounted(async () => {
  try {
    const { data } = await getQuizHistory()
    results.value = data
  } catch (e) {
    console.error('Lỗi tải lịch sử Quiz:', e)
  } finally {
    loading.value = false
  }
})

async function selectCustomer(item) {
  selectedId.value = item.id
  selected.value = item
  loadingProducts.value = true
  sanPhamGoiY.value = []
  try {
    const { data } = await getSanPhamGoiY(item.idLoaiDa)
    sanPhamGoiY.value = data
  } catch (e) {
    console.error('Lỗi tải sản phẩm gợi ý:', e)
  } finally {
    loadingProducts.value = false
  }
}

function getInitial(name) {
  if (!name) return '?'
  return name.charAt(0).toUpperCase()
}

function formatDate(dt) {
  if (!dt) return ''
  const d = new Date(dt)
  return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' })
}

function formatDateTime(dt) {
  if (!dt) return ''
  const d = new Date(dt)
  return d.toLocaleString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}

function formatPrice(val) {
  if (!val) return ''
  return Number(val).toLocaleString('vi-VN') + 'đ'
}

import { productImageUrl, PRODUCT_IMAGE_PLACEHOLDER } from '@/utils/productImage'

function resolveImg(url) {
  return productImageUrl(url)
}

function onImgError(e) {
  e.target.onerror = null // Rất quan trọng: Ngăn chặn vòng lặp vô hạn
  e.target.src = PRODUCT_IMAGE_PLACEHOLDER
}
</script>

<style scoped>
.insight-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: calc(100vh - 140px);
}

.insight-header {
  padding: 0 4px;
}

.insight-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--admin-text, #3a2a1a);
  margin: 0 0 4px;
}

.insight-subtitle {
  font-size: 14px;
  color: var(--admin-muted, #8c7a66);
  margin: 0;
}

/* ===== Master-Detail Layout ===== */
.insight-layout {
  display: grid;
  grid-template-columns: 340px 1fr;
  gap: 16px;
  flex: 1;
  min-height: 520px;
}

.insight-sidebar,
.insight-detail {
  background: var(--admin-surface, #fff);
  border: 1px solid var(--admin-border, #e8dcc8);
  border-radius: 14px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* ===== Sidebar ===== */
.sidebar-head {
  padding: 14px 16px 10px;
  border-bottom: 1px solid var(--admin-border, #e8dcc8);
  background: rgba(201, 169, 110, 0.06);
}

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--admin-surface, #fff);
  border: 1px solid var(--admin-border, #e8dcc8);
  border-radius: 8px;
  padding: 8px 12px;
}

.search-icon {
  font-size: 16px;
  color: var(--admin-muted, #8c7a66);
  flex-shrink: 0;
}

.search-input {
  border: none;
  outline: none;
  font-size: 13px;
  width: 100%;
  background: transparent;
  color: var(--admin-text, #3a2a1a);
}

.search-input::placeholder {
  color: var(--admin-muted, #8c7a66);
}

.sidebar-count {
  font-size: 12px;
  color: var(--admin-muted, #8c7a66);
  margin-top: 8px;
}

.sidebar-list {
  overflow-y: auto;
  flex: 1;
}

.sidebar-empty {
  padding: 40px 16px;
  text-align: center;
  color: var(--admin-muted, #8c7a66);
  font-size: 13px;
}

/* ===== Customer Card ===== */
.customer-card {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 12px 16px;
  border: none;
  border-bottom: 1px solid var(--admin-border, #e8dcc8);
  background: transparent;
  cursor: pointer;
  text-align: left;
  transition: background 0.15s;
}

.customer-card:hover {
  background: rgba(201, 169, 110, 0.08);
}

.customer-card--active {
  background: rgba(201, 169, 110, 0.14);
}

.customer-avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: var(--admin-accent, #c9a96e);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 15px;
  flex-shrink: 0;
}

.customer-info {
  min-width: 0;
  flex: 1;
}

.customer-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--admin-text, #3a2a1a);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.customer-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 3px;
}

.skin-badge {
  display: inline-block;
  padding: 1px 8px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
  background: #f0e6d6;
  color: #6b5430;
}

.skin-badge--DA_DAU { background: #fef3c7; color: #92400e; }
.skin-badge--DA_KHO { background: #e0e7ff; color: #3730a3; }
.skin-badge--DA_HON_HOP { background: #d1fae5; color: #065f46; }
.skin-badge--DA_NHAY_CAM { background: #ffe4e6; color: #9f1239; }
.skin-badge--DA_THUONG { background: #f3f4f6; color: #374151; }

.customer-date {
  font-size: 11px;
  color: var(--admin-muted, #8c7a66);
}

/* ===== Detail Panel ===== */
.detail-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  gap: 12px;
  color: var(--admin-muted, #8c7a66);
  padding: 40px;
}

.empty-icon {
  font-size: 48px;
  opacity: 0.4;
}

.detail-empty p {
  font-size: 14px;
  margin: 0;
}

.detail-section {
  padding: 20px 24px;
  border-bottom: 1px solid var(--admin-border, #e8dcc8);
}

.detail-section:last-child {
  border-bottom: none;
}

.detail-section-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--admin-text, #3a2a1a);
  margin: 0 0 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.product-count {
  font-weight: 400;
  color: var(--admin-muted, #8c7a66);
  font-size: 13px;
}

/* ===== Info Grid ===== */
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.info-label {
  font-size: 11px;
  color: var(--admin-muted, #8c7a66);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-value {
  font-size: 14px;
  font-weight: 500;
  color: var(--admin-text, #3a2a1a);
}

/* ===== Result Card ===== */
.result-card {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.result-badge {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 700;
  white-space: nowrap;
  flex-shrink: 0;
}

.result-desc {
  font-size: 13px;
  color: var(--admin-muted, #8c7a66);
  line-height: 1.6;
  margin: 0;
}

/* ===== Products Grid ===== */
.products-loading,
.products-empty {
  padding: 20px 0;
  text-align: center;
  color: var(--admin-muted, #8c7a66);
  font-size: 13px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 14px;
}

.product-card {
  background: var(--admin-surface, #fff);
  border: 1px solid var(--admin-border, #e8dcc8);
  border-radius: 10px;
  overflow: hidden;
  transition: box-shadow 0.15s;
}

.product-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.product-img-wrap {
  aspect-ratio: 1;
  background: #faf7f2;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-name {
  padding: 8px 10px 2px;
  font-size: 12px;
  font-weight: 600;
  color: var(--admin-text, #3a2a1a);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-price {
  padding: 2px 10px 10px;
  font-size: 13px;
  font-weight: 700;
  color: var(--admin-accent, #c9a96e);
}

/* ===== Responsive ===== */
@media (max-width: 900px) {
  .insight-layout {
    grid-template-columns: 1fr;
  }
  .insight-sidebar {
    max-height: 300px;
  }
}
</style>
