<script setup>
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { Icon } from '@iconify/vue'
import OrderCard from '@/components/storefront/OrderCard.vue'
import { fetchChiTietDonCuaToi, fetchDonCuaToi } from '@/api/donHangApi'
import { formatVND } from '@/utils/formatVND'
import { formatOrderDate, orderStatusClass, orderStatusLabel } from '@/utils/orderStatus'

const loading = ref(true)
const orders = ref([])
const selectedId = ref(null)
const detail = ref(null)
const detailLoading = ref(false)
const error = ref('')

onMounted(loadList)

async function loadList() {
  loading.value = true
  error.value = ''
  try {
    const res = await fetchDonCuaToi()
    orders.value = res.data || []
  } catch {
    error.value = 'Không tải được danh sách đơn hàng.'
  } finally {
    loading.value = false
  }
}

async function openDetail(id) {
  if (selectedId.value === id && detail.value) {
    selectedId.value = null
    detail.value = null
    return
  }
  selectedId.value = id
  detailLoading.value = true
  detail.value = null
  try {
    const res = await fetchChiTietDonCuaToi(id)
    detail.value = res.data
  } catch {
    error.value = 'Không tải được chi tiết đơn hàng.'
    selectedId.value = null
  } finally {
    detailLoading.value = false
  }
}

import request from '@/api/request'
const showReviewModal = ref(false)
const reviewForm = ref({ soSao: 5, noiDung: '', idSanPham: null, idHoaDonChiTiet: null, idKhachHang: null })
const reviewFile = ref(null)

function openReview(line) {
  const user = JSON.parse(localStorage.getItem('user_info') || '{}')
  reviewForm.value = {
    soSao: 5,
    noiDung: '',
    idSanPham: line.idSanPham || line.idChiTietSanPham || 1, // fallback if data is corrupted
    idHoaDonChiTiet: line.id, // line.id is idHoaDonChiTiet
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

    console.log("Submitting review payload with base64...");
    
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
      errorStr = e.response?.data?.message || e.message || 'Đã xảy ra lỗi';
    }
    alert('Lỗi: ' + errorStr);
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

      <h1 class="sf-order-page__title">Đơn hàng của tôi</h1>
      <p class="sf-order-page__desc">Lịch sử mua hàng khi bạn đăng nhập tài khoản SUNOVA.</p>

      <p v-if="error" class="sf-order-msg sf-order-msg--err">{{ error }}</p>

      <div v-if="loading" class="sf-order-skeleton" />

      <div v-else-if="!orders.length" class="sf-order-empty">
        <Icon icon="solar:box-linear" width="56" class="sf-order-empty__icon" />
        <p>Bạn chưa có đơn hàng nào.</p>
        <RouterLink to="/san-pham" class="btn-soleil btn-soleil--espresso">
          <span>Tiếp tục mua sắm</span>
        </RouterLink>
      </div>

      <div v-else class="sf-order-history">
        <div class="sf-order-list">
          <button
            v-for="item in orders"
            :key="item.id"
            type="button"
            class="sf-order-list__item"
            :class="{ active: selectedId === item.id }"
            @click="openDetail(item.id)"
          >
            <div class="sf-order-list__main">
              <strong>{{ item.maHoaDon }}</strong>
              <span>{{ formatOrderDate(item.ngayTao) }}</span>
            </div>
            <span
              class="sf-order-badge"
              :class="orderStatusClass(item.trangThai)"
            >
              {{ item.trangThaiLabel || orderStatusLabel(item.trangThai) }}
            </span>
            <strong class="sf-order-list__price">{{ formatVND(item.thanhTien) }}</strong>
          </button>
        </div>

        <div v-if="selectedId" class="sf-order-detail-panel">
          <div v-if="detailLoading" class="sf-order-skeleton sf-order-skeleton--short" />
          <OrderCard v-else-if="detail" :order="detail" @review="openReview" />
        </div>
      </div>
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
