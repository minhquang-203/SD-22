<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { Icon } from '@iconify/vue'
import request from '@/api/request'
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

const showReviewModal = ref(false)
const reviewForm = ref({ soSao: 5, noiDung: '', idSanPham: null, idHoaDonChiTiet: null, idKhachHang: null })
const reviewFile = ref(null)

function openReview(line) {
  const user = JSON.parse(localStorage.getItem('user_info') || '{}')
  reviewForm.value = {
    soSao: 5,
    noiDung: '',
    idSanPham: line.idSanPham || line.idChiTietSanPham || 1,
    idHoaDonChiTiet: line.id,
    idKhachHang: user.id || 1
  }
  reviewFile.value = null
  showReviewModal.value = true
}

function handleFileChange(event) {
  reviewFile.value = event.target.files[0]
}

async function submitReview() {
  try {
    const payload = { ...reviewForm.value };
    
    if (reviewFile.value) {
      const base64Str = await new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.readAsDataURL(reviewFile.value);
        reader.onload = () => resolve(reader.result);
        reader.onerror = error => reject(error);
      });
      payload.imageBase64 = base64Str;
    }
    
    await request.post('/danh-gia/add', payload)
    
    alert('Cảm ơn bạn đã đánh giá! Vui lòng chờ admin duyệt.')
    showReviewModal.value = false
  } catch(e) {
    console.error("Lỗi khi thêm đánh giá:", e);
    let errorStr = '';
    if (typeof e === 'string') {
      errorStr = e;
    } else if (e.response?.data?.errors) {
      errorStr = JSON.stringify(e.response.data.errors);
    } else {
      errorStr = e.message;
    }
    alert('Có lỗi xảy ra khi gửi đánh giá: ' + errorStr);
  }
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
            @review="openReview"
          />
        </div>

        <div v-else class="sf-order-empty">
          <div class="sf-order-empty__emoji">📦</div>
          <h3>Không có đơn hàng nào</h3>
          <p>Chưa có đơn hàng phù hợp với bộ lọc bạn chọn.</p>
        </div>
      </section>
    </div>

    <!-- Review Modal -->
    <div class="sf-modal-overlay" v-if="showReviewModal">
      <div class="sf-modal-content">
        <h3>Đánh giá sản phẩm</h3>
        <div class="sf-form-group">
          <label>Chất lượng sản phẩm (Sao):</label>
          <div class="sf-stars-input">
            <Icon 
              v-for="n in 5" :key="n" 
              icon="solar:star-bold" 
              :class="['sf-star', { active: n <= reviewForm.soSao }]"
              @click="reviewForm.soSao = n"
              width="24"
            />
          </div>
        </div>
        <div class="sf-form-group">
          <label>Nhận xét của bạn:</label>
          <textarea v-model="reviewForm.noiDung" placeholder="Hãy chia sẻ nhận xét cho sản phẩm này nhé" rows="3" class="sf-input"></textarea>
        </div>
        <div class="sf-form-group">
          <label>Ảnh/Video đính kèm (Tải lên từ máy):</label>
          <input type="file" accept="image/*,video/*" @change="handleFileChange" class="sf-input" />
        </div>
        <div class="sf-modal-actions">
          <button class="btn-soleil btn-outline" @click="showReviewModal = false">Trở lại</button>
          <button class="btn-soleil btn-primary" @click="submitReview">Hoàn thành</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.sf-modal-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 1000;
  display: flex; align-items: center; justify-content: center; backdrop-filter: blur(2px);
}
.sf-modal-content {
  background: white; padding: 24px; border-radius: 8px; width: 400px; max-width: 90%;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}
.sf-modal-content h3 { margin-top: 0; margin-bottom: 20px; font-size: 18px; text-align: center;}
.sf-form-group { margin-bottom: 16px; }
.sf-form-group label { display: block; font-size: 13px; color: #555; margin-bottom: 8px; font-weight: 500;}
.sf-input { width: 100%; border: 1px solid #ddd; padding: 10px; border-radius: 4px; font-family: inherit; box-sizing: border-box;}
.sf-stars-input { display: flex; gap: 8px; cursor: pointer; justify-content: center; margin-bottom: 10px;}
.sf-star { color: #ccc; transition: 0.2s; }
.sf-star.active { color: #f59e0b; }
.sf-modal-actions { display: flex; justify-content: space-between; margin-top: 24px; gap: 10px;}
.btn-soleil { flex: 1; padding: 10px; border-radius: 4px; border: none; cursor: pointer; font-weight: 600;}
.btn-outline { background: white; border: 1px solid #ddd; color: #555;}
.btn-primary { background: #ee4d2d; color: white; }
</style>
