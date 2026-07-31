<script setup>
import { ref, onBeforeUnmount, nextTick, watch } from 'vue'
import request from '@/api/request'
import { formatVND } from '@/utils/formatVND'
import { productImageUrl } from '@/utils/productImage'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import { useAuthModal } from '@/composables/useAuthModal'
import {
  taoHoacLayPhienHoTro,
  guiTinHoTroKhach,
  layTinNhanHoTro,
} from '@/api/hoTroApi'
import { subscribeCustomerHoTroPhien } from '@/composables/useRealtime'


const router = useRouter()
const { isLoggedIn } = useAuth()
const { openAuthModal } = useAuthModal()

const isOpen = ref(false)
const isTyping = ref(false)
const inputMessage = ref('')
const messages = ref([])
const sessionId = ref(null)
const messagesContainer = ref(null)

/** 'AI' | 'NGUOI' */
const chatMode = ref('AI')
const hoTroPhienId = ref(null)
const connectingStaff = ref(false)
const sendingStaff = ref(false)
const showMenu = ref(false)
let unsubscribeHoTro = null

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

async function openChat(mode) {
  showMenu.value = false

  if (mode === 'NGUOI') {
    isOpen.value = true
    await switchToStaff()
    return
  }

  if(chatMode.value === 'NGUOI') {
    cleanupHoTroSub()
    hoTroPhienId.value = null
  }

  chatMode.value = 'AI'
  isOpen.value = true

  if (messages.value.length === 0) {
    messages.value.push({
      nguoiGui: 'AI',
      noiDung: 'Chào bạn! Tôi là Trợ lý AI của SUNOVA. Bạn cần tư vấn kem chống nắng cho loại da nào?',
      thoiGian: new Date(),
    })
  }
}

 const toggleChat = () => {
    isOpen.value = !isOpen.value
    if(!isOpen.value) {
      showMenu.value = false
    }
  }

const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}

function cleanupHoTroSub() {
  if (unsubscribeHoTro) {
    unsubscribeHoTro()
    unsubscribeHoTro = null
  }
}

/** Chỉ push tin có id; bỏ qua nếu id đã có (khử trùng API ↔ realtime). */
function upsertTinHoTro(tin) {
  if (!tin?.id) return false
  if (messages.value.some((m) => m.id === tin.id)) return false
  messages.value.push({
    id: tin.id,
    nguoiGui: tin.nguoiGui,
    noiDung: tin.noiDung,
    thoiGian: tin.thoiGian,
  })
  return true
}

function appendRealtimeTin(payload) {
  if (upsertTinHoTro(payload?.tinNhan)) {
    scrollToBottom()
  }
}

function ensureHoTroSubscribed() {
  if (!hoTroPhienId.value) return
  cleanupHoTroSub()
  unsubscribeHoTro = subscribeCustomerHoTroPhien(hoTroPhienId.value, appendRealtimeTin)
}

async function switchToStaff() {
  if (!isLoggedIn.value) {
    openAuthModal('login')
    return
  }
  connectingStaff.value = true
  try {
    const res = await taoHoacLayPhienHoTro()
    const phien = res.data
    hoTroPhienId.value = phien.id
    chatMode.value = 'NGUOI'
    messages.value = []

    const hist = await layTinNhanHoTro(phien.id)
    messages.value = (hist.data || []).map((t) => ({
      id: t.id,
      nguoiGui: t.nguoiGui,
      noiDung: t.noiDung,
      thoiGian: t.thoiGian,
    }))
    if (messages.value.length === 0) {
      messages.value.push({
        nguoiGui: 'NHAN_VIEN',
        noiDung: 'Bạn đã kết nối với hỗ trợ viên SUNOVA. Hãy mô tả nhu cầu của bạn nhé!',
        thoiGian: new Date(),
      })
    }

    ensureHoTroSubscribed()
    scrollToBottom()
  } catch (err) {
    messages.value.push({
      nguoiGui: 'AI',
      noiDung: typeof err === 'string' ? err : 'Không kết nối được nhân viên. Vui lòng thử lại.',
      thoiGian: new Date(),
    })
  } finally {
    connectingStaff.value = false
  }
}

function switchToAi() {
  cleanupHoTroSub()
  chatMode.value = 'AI'
  hoTroPhienId.value = null
  messages.value = [
    {
      nguoiGui: 'AI',
      noiDung: 'Đã quay lại chat AI. Bạn cần tư vấn gì tiếp theo?',
      thoiGian: new Date(),
    },
  ]
}

const sendMessage = async () => {
  if (!inputMessage.value.trim()) return
  if (chatMode.value === 'NGUOI' && sendingStaff.value) return

  const userMsg = inputMessage.value.trim()
  inputMessage.value = ''

  if (chatMode.value === 'NGUOI') {
    sendingStaff.value = true
    try {
      const res = await guiTinHoTroKhach({
        idPhien: hoTroPhienId.value,
        noiDung: userMsg,
      })
      // Không push optimistic — API hoặc realtime (cái nào tới trước), khử trùng theo id
      if (upsertTinHoTro(res.data)) {
        scrollToBottom()
      }
    } catch (err) {
      messages.value.push({
        nguoiGui: 'NHAN_VIEN',
        noiDung: typeof err === 'string' ? err : 'Gửi tin thất bại. Vui lòng thử lại.',
        thoiGian: new Date(),
      })
      scrollToBottom()
    } finally {
      sendingStaff.value = false
    }
    return
  }

  messages.value.push({ nguoiGui: 'KHACH', noiDung: userMsg, thoiGian: new Date() })
  scrollToBottom()

  isTyping.value = true
  try {
    const payload = {
      idPhien: sessionId.value,
      noiDung: userMsg,
    }
    const res = await request.post('/chat/tin-nhan', payload)
    if (res.data) {
      if (!sessionId.value) sessionId.value = res.data.idPhien
      messages.value.push(res.data)
    }
  } catch (err) {
    console.error('Lỗi khi gửi tin nhắn:', err)
    messages.value.push({
      nguoiGui: 'AI',
      noiDung: 'Xin lỗi, hệ thống đang bận. Vui lòng thử lại sau.',
    })
  } finally {
    isTyping.value = false
    scrollToBottom()
  }
}

const viewProduct = (productId) => {
  router.push(`/san-pham/${productId}`)
}

const formatMessage = (text) => {
  if (!text) return ''
  return text.replace(/\n/g, '<br/>')
}

const handleChatClick = (e) => {
  const link = e.target.closest('.ai-link')
  if (link) {
    e.preventDefault()
    const href = link.getAttribute('href')
    if (href) router.push(href)
  }
}

function bubbleClass(nguoiGui) {
  if (nguoiGui === 'KHACH') return 'msg-user'
  if (nguoiGui === 'NHAN_VIEN') return 'msg-staff'
  return 'msg-ai'
}

onBeforeUnmount(() => {
  cleanupHoTroSub()
})

watch(isOpen, (open) => {
  if (!open) {
    showMenu.value = false
    // Đóng khung → huỷ subscribe (tránh chồng handler khi mở lại)
    cleanupHoTroSub()
    return
  }
  // Mở lại khung đang ở chế độ tư vấn viên → subscribe đúng 1 lần
  if (chatMode.value === 'NGUOI' && hoTroPhienId.value) {
    ensureHoTroSubscribed()
  }
})
</script>

<template>
  <div class="chat-widget-wrapper">
    <div v-if="!isOpen" class="chat-bubble-stack" @mouseenter="showMenu = true" @mouseleave=" showMenu = false">
        <div class="chat-bubble-menu" :class="{'is-visible': showMenu}">
              <button 
                type="button"
                class="chat-menu-item"
                aria-label="Chat với AI"
                @click="openChat('AI')"
              >
              <span class="chat-menu-icon chat-menu-icon--ai">
                      <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                      </svg>
              </span>
              <span class="chat-menu-label">Chat với AI</span>
              </button>

              <button 
                type="button"
                class="chat-menu-item"
                aria-label="Chat với tư vấn viên"
                @click="openChat('NGUOI')"
              >
              <span class="chat-menu-icon chat-menu-icon--staff">
                        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                      </svg>
              </span>
              <span class="chat-menu-label">Chat với tư vấn viên</span>
              </button>
        </div>

        <button type="button" class="chat-bubble-btn" aria-label="Menu chat SUNOVA">
             <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
      <path stroke-linecap="round" stroke-linejoin="round" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" />
            </svg>
        </button>
    </div>

    <div v-else class="chat-window">
      <div class="chat-header">
        <div class="chat-header-info" @click="toggleChat">
          <div class="chat-avatar">
            <img src="@/assets/logo/sunova_mark.png" alt="SUNOVA" class="avatar-img" />
          </div>
          <div>
            <h4>{{ chatMode === 'NGUOI' ? 'Tư vấn viên SUNOVA' : 'SUNOVA AI CHATBOT' }}</h4>
            <span>{{ chatMode === 'NGUOI' ? 'Chat với tư vấn viên' : 'Trực tuyến' }}</span>
          </div>
        </div>
        <button type="button" class="chat-close-btn" @click="toggleChat">&times;</button>
      </div>

      <div class="chat-mode-bar">
        <button
          v-if="chatMode === 'AI'"
          type="button"
          class="chat-mode-btn"
          :disabled="connectingStaff"
          @click="switchToStaff"
        >
          {{ connectingStaff ? 'Đang kết nối...' : 'Gặp nhân viên tư vấn' }}
        </button>
        <button
          v-else
          type="button"
          class="chat-mode-btn chat-mode-btn--ghost"
          @click="switchToAi"
        >
          Quay lại chat AI
        </button>
      </div>

      <div ref="messagesContainer" class="chat-body" @click="handleChatClick">
        <div
          v-for="(msg, idx) in messages"
          :key="msg.id || idx"
          class="chat-message"
          :class="bubbleClass(msg.nguoiGui)"
        >
          <div class="msg-bubble">
            <p v-html="formatMessage(msg.noiDung)"></p>

            <div
              v-if="msg.danhSachSanPhamGoiY && msg.danhSachSanPhamGoiY.length > 0"
              class="chat-product-list"
            >
              <div
                v-for="sp in msg.danhSachSanPhamGoiY"
                :key="sp.id"
                class="chat-product-card"
                @click="viewProduct(sp.id)"
              >
                <img :src="productImageUrl(sp.anhChinhUrl || sp.anhUrl)" alt="Product">
                <div class="chat-product-info">
                  <strong>{{ sp.ten || sp.tenSanPham }}</strong>
                  <span>{{ formatVND(sp.giaMin ?? sp.giaBanNhoNhat) }}</span>
                </div>
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
          type="text"
          :placeholder="chatMode === 'NGUOI' ? 'Nhắn cho nhân viên...' : 'Nhập câu hỏi của bạn...'"
          @keyup.enter="sendMessage"
        />
        <button
          type="button"
          :disabled="!inputMessage.trim() || isTyping || connectingStaff || sendingStaff"
          @click="sendMessage"
        >
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
  z-index: 4000;
  font-family: 'Be Vietnam Pro', system-ui, sans-serif;
}

.chat-bubble-stack {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.chat-bubble-menu {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
  margin-bottom: 12px;
  opacity: 0;
  visibility: hidden;
  transform: translateY(8px);
  pointer-events: none;
  transition: opacity 0.25s ease, transform 0.25s ease, visibility 0.25s ease;
}

.chat-bubble-menu.is-visible {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
  pointer-events: auto ;
}

.chat-menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-direction: row-reverse;
  border: none;
  background: transparent;
  padding: 0;
  cursor:pointer;
}

.chat-menu-item:hover 
.chat-menu-icon {
  transform: scale(1.08);
  box-shadow: 0 6px 18px rgba(185, 147, 90, 0.45);
}

.chat-menu-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background:linear-gradient(135deg, #b9935a 0%, #d4af37 100%) ;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  flex-shrink: 0;
}

.chat-menu-icon--staff {
  background: linear-gradient(135deg, var(--sf-espresso, #2a201b) 0%, #4a3f35 100%);
  box-shadow: 0 4px 12px rgba(42, 32, 27, 0.35);
}

.chat-menu-label {
  white-space: nowrap;
  font-size: 13px;
  font-weight: 600;
  color: var(--sf-espresso, #2a201b);
  background: var(--sf-cream, #f9f5f0);
  border: 1px solid var(--sf-sand, #e8dcc8);
  padding: 6px 12px;
  border-radius: 999px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.09);
  opacity: 0;
  transform: translateX(6px);
  transition: opacity 0.2s ease, transform 0.2s ease;
  pointer-events: none;
}

.chat-menu-item:hover .chat-menu-label {
  opacity: 1;
  transform: translateX(0);
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
.chat-bubble-stack:hover .chat-bubble-btn {
  transform: scale(1.05);
}

.chat-window {
  width: 350px;
  height: 540px;
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
}
.chat-header-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  flex: 1;
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

.chat-mode-bar {
  padding: 8px 12px;
  background: var(--sf-cream, #f9f5f0);
  border-bottom: 1px solid var(--sf-sand, #e8dcc8);
}
.chat-mode-btn {
  width: 100%;
  padding: 8px 12px;
  border-radius: 999px;
  border: 1px solid var(--sf-espresso, #2a201b);
  background: var(--sf-espresso, #2a201b);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}
.chat-mode-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.chat-mode-btn--ghost {
  background: transparent;
  color: var(--sf-mid, #5a5248);
  border-color: var(--sf-sand, #e8dcc8);
}

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
.msg-user { align-self: flex-end; }
.msg-ai, .msg-staff { align-self: flex-start; }

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
}
.msg-staff .msg-bubble {
  background: #fffdfa;
  color: #1a1814;
  border: 1px solid #e8dcc8;
  border-bottom-left-radius: 4px;
}

.msg-time {
  display: block;
  font-size: 10px;
  margin-top: 6px;
  opacity: 0.7;
}

.chat-product-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 10px;
}
.chat-product-card {
  display: flex;
  gap: 8px;
  padding: 8px;
  background: #f9fafb;
  border-radius: 8px;
  cursor: pointer;
  border: 1px solid #e5e7eb;
}
.chat-product-card img {
  width: 48px;
  height: 48px;
  object-fit: cover;
  border-radius: 6px;
}
.chat-product-info {
  display: flex;
  flex-direction: column;
  font-size: 12px;
}
.chat-product-info strong {
  color: #111827;
}
.chat-product-info span {
  color: #b9935a;
  font-weight: 600;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 14px 18px !important;
}
.typing-indicator span {
  width: 6px;
  height: 6px;
  background: #9ca3af;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}
.typing-indicator span:nth-child(1) { animation-delay: -0.32s; }
.typing-indicator span:nth-child(2) { animation-delay: -0.16s; }
@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.chat-footer {
  padding: 12px;
  background: white;
  border-top: 1px solid #e5e7eb;
  display: flex;
  gap: 8px;
}
.chat-footer input {
  flex: 1;
  border: 1px solid #e5e7eb;
  border-radius: 20px;
  padding: 10px 16px;
  outline: none;
  font-size: 14px;
}
.chat-footer input:focus {
  border-color: #b9935a;
}
.chat-footer button {
  background: #b9935a;
  color: white;
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
.chat-footer button:disabled {
  background: #d1d5db;
  cursor: not-allowed;
}
</style>
