<script setup>
import { ref, onMounted, nextTick } from 'vue';
import request from '@/api/request';
import { formatVND } from '@/utils/formatVND';
import { productImageUrl } from '@/utils/productImage';
import { useRouter } from 'vue-router';

const router = useRouter();

const isOpen = ref(false);
const isTyping = ref(false);
const inputMessage = ref('');
const messages = ref([]);
const sessionId = ref(null);
const messagesContainer = ref(null);

const scrollToBottom = async () => {
  await nextTick();
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
  }
};

const toggleChat = () => {
  isOpen.value = !isOpen.value;
  if (isOpen.value && messages.value.length === 0) {
    // Nếu chưa có tin nhắn nào, gửi lời chào đầu tiên
    messages.value.push({
      nguoiGui: 'AI',
      noiDung: 'Chào bạn! Tôi là Trợ lý AI của SUNOVA. Bạn cần tư vấn kem chống nắng cho loại da nào?',
      thoiGian: new Date()
    });
  }
};

const formatTime = (dateStr) => {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`;
};

const sendMessage = async () => {
  if (!inputMessage.value.trim()) return;

  const userMsg = inputMessage.value;
  messages.value.push({ nguoiGui: 'KHACH', noiDung: userMsg, thoiGian: new Date() });
  inputMessage.value = '';
  scrollToBottom();

  isTyping.value = true;

  try {
    const payload = {
      idPhien: sessionId.value,
      noiDung: userMsg
    };
    
    // Nếu đã đăng nhập, có thể truyền thêm idKhachHang vào payload
    // const user = JSON.parse(localStorage.getItem('user'));
    // if (user) payload.idKhachHang = user.id;

    const res = await request.post('/chat/tin-nhan', payload);
    
    if (res.data) {
      if (!sessionId.value) sessionId.value = res.data.idPhien;
      messages.value.push(res.data);
    }
  } catch (err) {
    console.error("Lỗi khi gửi tin nhắn:", err);
    messages.value.push({ nguoiGui: 'AI', noiDung: 'Xin lỗi, hệ thống đang bận. Vui lòng thử lại sau.' });
  } finally {
    isTyping.value = false;
    scrollToBottom();
  }
};

const viewProduct = (productId) => {
  router.push(`/san-pham/${productId}`);
};

</script>

<template>
  <div class="chat-widget-wrapper">
    <!-- Nút bong bóng chat -->
    <button v-if="!isOpen" class="chat-bubble-btn" @click="toggleChat">
      <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" />
      </svg>
    </button>

    <!-- Cửa sổ Chat -->
    <div v-else class="chat-window">
      <div class="chat-header" @click="toggleChat">
        <div class="chat-header-info">
          <div class="chat-avatar">
            <img src="@/assets/logo/sunova_mark.png" alt="SUNOVA" class="avatar-img" />
          </div>
          <div>
            <h4>SUNOVA AI CHATBOT</h4>
            <span>Trực tuyến</span>
          </div>
        </div>
        <button class="chat-close-btn">&times;</button>
      </div>

      <div class="chat-body" ref="messagesContainer">
        <div 
          v-for="(msg, idx) in messages" 
          :key="idx" 
          class="chat-message"
          :class="msg.nguoiGui === 'KHACH' ? 'msg-user' : 'msg-ai'"
        >
          <div class="msg-bubble">
            <p>{{ msg.noiDung }}</p>
            
            <!-- Thẻ sản phẩm gợi ý -->
            <div v-if="msg.sanPhamGoiY" class="chat-product-card" @click="viewProduct(msg.sanPhamGoiY.id)">
              <img :src="productImageUrl(msg.sanPhamGoiY.anhUrl)" alt="Product">
              <div class="chat-product-info">
                <strong>{{ msg.sanPhamGoiY.tenSanPham }}</strong>
                <span>{{ formatVND(msg.sanPhamGoiY.giaBanNhoNhat) }}</span>
              </div>
            </div>

            <span class="msg-time">{{ formatTime(msg.thoiGian) }}</span>
          </div>
        </div>

        <div v-if="isTyping" class="chat-message msg-ai">
          <div class="msg-bubble typing-indicator">
            <span></span><span></span><span></span>
          </div>
        </div>
      </div>

      <div class="chat-footer">
        <input 
          v-model="inputMessage" 
          @keyup.enter="sendMessage" 
          type="text" 
          placeholder="Nhập câu hỏi của bạn..." 
        />
        <button @click="sendMessage" :disabled="!inputMessage.trim() || isTyping">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chat-widget-wrapper {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 9999;
  font-family: 'Inter', sans-serif;
}

.chat-bubble-btn {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #b9935a 0%, #d4af37 100%);
  color: white;
  border: none;
  box-shadow: 0 4px 15px rgba(185, 147, 90, 0.4);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s ease;
}
.chat-bubble-btn:hover {
  transform: scale(1.1);
}

.chat-window {
  width: 350px;
  height: 500px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #f3f4f6;
}

.chat-header {
  background: linear-gradient(135deg, #1f2937 0%, #374151 100%);
  color: white;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
}
.chat-header-info {
  display: flex;
  align-items: center;
  gap: 12px;
}
.chat-avatar {
  font-size: 24px;
  background: rgba(255, 255, 255, 0.2);
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.chat-header-info h4 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
}
.chat-header-info span {
  font-size: 12px;
  color: #4ade80;
}
.chat-close-btn {
  background: none;
  border: none;
  color: white;
  font-size: 24px;
  cursor: pointer;
  opacity: 0.7;
}
.chat-close-btn:hover { opacity: 1; }

.chat-body {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  background: #f9fafb;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chat-message {
  display: flex;
  max-width: 85%;
}
.msg-user {
  align-self: flex-end;
}
.msg-ai {
  align-self: flex-start;
}

.msg-bubble {
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.5;
  position: relative;
}
.msg-user .msg-bubble {
  background: #b9935a;
  color: white;
  border-bottom-right-radius: 4px;
}
.msg-ai .msg-bubble {
  background: white;
  color: #1f2937;
  border: 1px solid #e5e7eb;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}
.msg-bubble p {
  margin: 0;
}
.msg-time {
  font-size: 10px;
  opacity: 0.7;
  display: block;
  text-align: right;
  margin-top: 4px;
}

.chat-product-card {
  margin-top: 8px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 8px;
  display: flex;
  gap: 12px;
  cursor: pointer;
  transition: border-color 0.2s;
}
.chat-product-card:hover {
  border-color: #b9935a;
}
.chat-product-card img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 6px;
}
.chat-product-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.chat-product-info strong {
  font-size: 13px;
  color: #1f2937;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.chat-product-info span {
  font-size: 13px;
  color: #b9935a;
  font-weight: 600;
  margin-top: 4px;
}

.chat-footer {
  padding: 16px;
  background: white;
  border-top: 1px solid #f3f4f6;
  display: flex;
  gap: 8px;
}
.chat-footer input {
  flex: 1;
  padding: 10px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 20px;
  outline: none;
  font-size: 14px;
  transition: border-color 0.2s;
}
.chat-footer input:focus {
  border-color: #b9935a;
}
.chat-footer button {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #b9935a;
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.chat-footer button:disabled {
  background: #d1d5db;
  cursor: not-allowed;
}

.typing-indicator span {
  display: inline-block;
  width: 6px;
  height: 6px;
  background: #9ca3af;
  border-radius: 50%;
  margin: 0 2px;
  animation: bounce 1.4s infinite ease-in-out both;
}
.typing-indicator span:nth-child(1) { animation-delay: -0.32s; }
.typing-indicator span:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}
</style>
