<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import request from '@/api/request';

const reviews = ref([]);
const activeTab = ref('ALL');
const showReplyModal = ref(false);
const currentReview = ref(null);
const replyText = ref('');
let intervalId = null;

const fetchReviews = async () => {
  try {
    const res = await request.get('/danh-gia/all');
    // Bắt đúng data trả về từ Backend (phụ thuộc vào interceptor của dự án)
    reviews.value = res.data || res; 
  } catch (err) {
    console.error("Lỗi lấy danh sách đánh giá", err);
  }
};

onMounted(() => {
  fetchReviews();
  intervalId = setInterval(fetchReviews, 10000); // Tự động làm mới mỗi 10 giây
});

onUnmounted(() => {
  if (intervalId) clearInterval(intervalId);
});

const filteredReviews = computed(() => {
  if (activeTab.value === 'ALL') return reviews.value;
  return reviews.value.filter(r => r.trangThai === activeTab.value);
});

const changeStatus = async (id, status) => {
  try {
    await request.put(`/danh-gia/duyet/${id}?trangThai=${status}`);
    fetchReviews();
  } catch (err) {
    console.error("Lỗi cập nhật trạng thái", err);
  }
};

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

const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString('vi-VN');
};

const getStatusLabel = (status) => {
  if (status === 'CHO_DUYET') return { text: 'Chờ duyệt', class: 'badge-warning' };
  if (status === 'DA_DUYET') return { text: 'Đã duyệt', class: 'badge-success' };
  if (status === 'TU_CHOI') return { text: 'Bị ẩn', class: 'badge-danger' };
  return { text: status, class: 'badge-secondary' };
};
</script>

<template>
  <div class="review-dashboard">
    <div class="dash-header">
      <h1 class="page-title">Quản Lý Đánh Giá</h1>
      <p class="subtitle">Kiểm duyệt đánh giá và phản hồi khách hàng</p>
    </div>

    <!-- Tabs Lọc Trạng Thái -->
    <div class="tabs-container">
      <button :class="['tab-btn', { active: activeTab === 'ALL' }]" @click="activeTab = 'ALL'">Tất cả</button>
      <button :class="['tab-btn', { active: activeTab === 'CHO_DUYET' }]" @click="activeTab = 'CHO_DUYET'">Chờ duyệt</button>
      <button :class="['tab-btn', { active: activeTab === 'DA_DUYET' }]" @click="activeTab = 'DA_DUYET'">Đã hiển thị</button>
      <button :class="['tab-btn', { active: activeTab === 'TU_CHOI' }]" @click="activeTab = 'TU_CHOI'">Bị ẩn</button>
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
            <th>Trạng thái</th>
            <th>Phản hồi của Shop</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="filteredReviews.length === 0">
            <td colspan="8" class="text-center">Chưa có dữ liệu đánh giá nào trong mục này</td>
          </tr>
          <tr v-for="review in filteredReviews" :key="review.id">
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
              <span :class="['status-badge', getStatusLabel(review.trangThai).class]">
                {{ getStatusLabel(review.trangThai).text }}
              </span>
            </td>
            <td>
              <div class="shop-reply" v-if="review.phanHoiCuaShop">
                <span class="reply-icon">↪</span> {{ review.phanHoiCuaShop }}
              </div>
              <span v-else class="no-media">Chưa phản hồi</span>
            </td>
            <td>
              <div class="action-buttons">
                <button v-if="review.trangThai !== 'DA_DUYET'" class="btn-approve" @click="changeStatus(review.id, 'DA_DUYET')">Duyệt</button>
                <button v-if="review.trangThai !== 'TU_CHOI'" class="btn-reject" @click="changeStatus(review.id, 'TU_CHOI')">Ẩn</button>
                <button class="btn-reply" @click="openReplyModal(review)">Phản hồi</button>
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

.tabs-container {
  display: flex; gap: 8px; margin-bottom: 20px;
  background: white; padding: 12px; border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}
.tab-btn {
  padding: 8px 16px; border: none; background: transparent;
  color: #64748b; font-weight: 600; border-radius: 8px;
  cursor: pointer; transition: all 0.2s;
}
.tab-btn.active { background: #3b82f6; color: white; }
.tab-btn:hover:not(.active) { background: #f1f5f9; }

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

.status-badge { padding: 6px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; display: inline-block;}
.badge-warning { background: #fef3c7; color: #d97706; }
.badge-success { background: #dcfce3; color: #166534; }
.badge-danger { background: #fee2e2; color: #b91c1c; }
.badge-secondary { background: #f1f5f9; color: #475569; }

.shop-reply { background: #f0fdf4; padding: 10px 12px; border-radius: 8px; font-size: 13px; color: #166534; border-left: 3px solid #22c55e; }
.reply-icon { color: #22c55e; font-weight: bold; margin-right: 4px; }

.action-buttons { display: flex; flex-direction: column; gap: 8px; }
.action-buttons button { padding: 8px 12px; border-radius: 6px; border: none; font-size: 12px; font-weight: 600; cursor: pointer; transition: 0.2s; }
.btn-approve { background: #dcfce3; color: #166534; }
.btn-approve:hover { background: #bbf7d0; }
.btn-reject { background: #fee2e2; color: #b91c1c; }
.btn-reject:hover { background: #fecaca; }
.btn-reply { background: #e0e7ff; color: #4338ca; }
.btn-reply:hover { background: #c7d2fe; }

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
