<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import request from '@/api/request';

const reviews = ref([]);
const showReplyModal = ref(false);
const currentReview = ref(null);
const replyText = ref('');
let intervalId = null;

const fetchReviews = async () => {
  try {
    const res = await request.get('/danh-gia/all');
    reviews.value = res.data || res;
  } catch (err) {
    console.error("Lỗi lấy danh sách đánh giá", err);
  }
};

onMounted(() => {
  fetchReviews();
  intervalId = setInterval(fetchReviews, 10000);
});

onUnmounted(() => {
  if (intervalId) clearInterval(intervalId);
});

const openReplyModal = (review) => {
  currentReview.value = review;
  replyText.value = review.phanHoiCuaShop || '';
  showReplyModal.value = true;
};

const submitReply = async () => {
  try {
    await request.put(`/danh-gia/phan-hoi/${currentReview.value.id}`, replyText.value, {
      headers: { 'Content-Type': 'text/plain' }
    });
    showReplyModal.value = false;
    fetchReviews();
  } catch (err) {
    console.error("Lỗi gửi phản hồi", err);
  }
};

const deleteReview = async (review) => {
  const ok = window.confirm(`Xóa đánh giá của ${review.tenKhachHang || ('#' + review.idKhachHang)}? Đánh giá sẽ biến mất khỏi trang sản phẩm.`);
  if (!ok) return;
  try {
    await request.delete(`/danh-gia/${review.id}`);
    fetchReviews();
  } catch (err) {
    console.error("Lỗi xóa đánh giá", err);
    window.alert(err.response?.data?.message || 'Không xóa được đánh giá.');
  }
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString('vi-VN');
};
</script>

<template>
  <div class="review-dashboard">
    <div class="dash-header">
      <h1 class="page-title">Quản Lý Đánh Giá</h1>
      <p class="subtitle">Đánh giá hiển thị ngay trên trang sản phẩm. Admin chỉ phản hồi hoặc xóa.</p>
    </div>

    <!-- Bảng Danh Sách Đánh Giá -->
    <div class="table-container">
      <table class="review-table">
        <thead>
          <tr>
            <th>Ngày tạo</th>
            <th>Sản phẩm</th>
            <th>Khách hàng</th>
            <th>Đánh giá</th>
            <th>Hình ảnh / Video</th>
            <th>Phản hồi của Shop</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="reviews.length === 0">
            <td colspan="7" class="text-center">Chưa có đánh giá nào</td>
          </tr>
          <tr v-for="review in reviews" :key="review.id">
            <td>{{ formatDate(review.ngayTao) }}</td>
            <td class="product-name">{{ review.tenSanPham }}</td>
            <td><strong>{{ review.tenKhachHang || ('#' + review.idKhachHang) }}</strong></td>
            <td>
              <div class="stars">
                <span v-for="n in 5" :key="n" :class="['star', { 'filled': n <= review.soSao }]">★</span>
              </div>
              <p class="review-content">{{ review.noiDung }}</p>
            </td>
            <td>
              <div class="media-container" v-if="review.hinhAnhVideo">
                <img :src="review.hinhAnhVideo" class="review-thumbnail" alt="Hình ảnh khách đăng" onerror="this.style.display='none'">
              </div>
              <span v-else class="no-media">Không có ảnh</span>
            </td>
            <td>
              <div class="shop-reply" v-if="review.phanHoiCuaShop">
                <span class="reply-icon">↪</span> {{ review.phanHoiCuaShop }}
              </div>
              <span v-else class="no-media">Chưa phản hồi</span>
            </td>
            <td>
              <div class="action-buttons">
                <button class="btn-reply" @click="openReplyModal(review)">Phản hồi</button>
                <button class="btn-delete" @click="deleteReview(review)">Xóa</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Popup Phản Hồi -->
    <div class="modal-overlay" v-if="showReplyModal">
      <div class="reply-modal">
        <div class="modal-header">
          <h3>Phản hồi đánh giá</h3>
          <button class="close-btn" @click="showReplyModal = false">×</button>
        </div>
        <div class="modal-body">
          <p class="review-quote">"{{ currentReview?.noiDung }}"</p>
          <textarea v-model="replyText" class="reply-input" placeholder="Nhập câu trả lời của bạn để giải quyết khiếu nại hoặc cảm ơn khách hàng..." rows="4"></textarea>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="showReplyModal = false">Hủy</button>
          <button class="btn-submit" @click="submitReply">Gửi Phản Hồi</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.review-dashboard {
  padding: 24px;
  background-color: #f8fafc;
  min-height: 100vh;
  font-family: 'Inter', sans-serif;
}
.dash-header { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 700; color: #1e293b; margin: 0 0 8px 0; }
.subtitle { font-size: 14px; color: #64748b; margin: 0; }

.table-container {
  background: white; border-radius: 12px; overflow: hidden;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);
}
.review-table { width: 100%; border-collapse: collapse; }
.review-table th { background: #f8fafc; padding: 16px; text-align: left; font-size: 13px; color: #64748b; font-weight: 600; border-bottom: 1px solid #e2e8f0; }
.review-table td { padding: 16px; border-bottom: 1px solid #e2e8f0; vertical-align: top; font-size: 14px; color: #334155; }

.product-name { font-weight: 600; max-width: 200px; }
.stars { color: #cbd5e1; font-size: 18px; margin-bottom: 4px; letter-spacing: 2px;}
.star.filled { color: #f59e0b; }
.review-content { margin: 0; line-height: 1.5; font-size: 13px; font-style: italic;}

.review-thumbnail { width: 70px; height: 70px; object-fit: cover; border-radius: 8px; border: 1px solid #e2e8f0; transition: transform 0.2s;}
.review-thumbnail:hover { transform: scale(1.5); cursor: pointer; z-index: 10; position: relative;}
.no-media { color: #94a3b8; font-style: italic; font-size: 12px; }

.shop-reply { background: #f0fdf4; padding: 10px 12px; border-radius: 8px; font-size: 13px; color: #166534; border-left: 3px solid #22c55e; }
.reply-icon { color: #22c55e; font-weight: bold; margin-right: 4px; }

.action-buttons { display: flex; flex-direction: column; gap: 8px; }
.action-buttons button { padding: 8px 12px; border-radius: 6px; border: none; font-size: 12px; font-weight: 600; cursor: pointer; transition: 0.2s; }
.btn-reply { background: #e0e7ff; color: #4338ca; }
.btn-reply:hover { background: #c7d2fe; }
.btn-delete { background: #fee2e2; color: #b91c1c; }
.btn-delete:hover { background: #fecaca; }

/* Modal Styles */
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; z-index: 1000; backdrop-filter: blur(2px);}
.reply-modal { background: white; width: 500px; border-radius: 12px; box-shadow: 0 20px 25px -5px rgba(0,0,0,0.1); overflow: hidden; animation: slideDown 0.3s ease-out;}
@keyframes slideDown { from { opacity: 0; transform: translateY(-20px); } to { opacity: 1; transform: translateY(0); } }
.modal-header { padding: 16px 24px; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: center; }
.modal-header h3 { margin: 0; font-size: 18px; color: #1e293b; font-weight: 700;}
.close-btn { background: none; border: none; font-size: 28px; cursor: pointer; color: #94a3b8; line-height: 1;}
.close-btn:hover { color: #0f172a; }
.modal-body { padding: 24px; }
.review-quote { background: #f8fafc; padding: 12px 16px; border-radius: 8px; font-style: italic; color: #475569; margin: 0 0 16px 0; font-size: 14px; border-left: 4px solid #cbd5e1; }
.reply-input { width: 100%; border: 1px solid #cbd5e1; border-radius: 8px; padding: 12px; font-family: 'Inter', sans-serif; font-size: 14px; resize: vertical; box-sizing: border-box; }
.reply-input:focus { outline: none; border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }
.modal-footer { padding: 16px 24px; border-top: 1px solid #e2e8f0; display: flex; justify-content: flex-end; gap: 12px; background: #f8fafc; }
.btn-cancel { padding: 8px 16px; border: 1px solid #cbd5e1; background: white; color: #475569; border-radius: 6px; font-weight: 600; cursor: pointer; transition: 0.2s;}
.btn-cancel:hover { background: #f1f5f9; }
.btn-submit { padding: 8px 16px; border: none; background: #3b82f6; color: white; border-radius: 6px; font-weight: 600; cursor: pointer; transition: 0.2s;}
.btn-submit:hover { background: #2563eb; }
.text-center { text-align: center; color: #64748b; padding: 40px !important; font-style: italic;}
</style>
