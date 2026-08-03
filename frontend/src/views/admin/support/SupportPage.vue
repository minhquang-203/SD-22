<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch, nextTick } from 'vue'
import { Icon } from '@iconify/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import { danhSachPhienHoTro, layTinNhanHoTro, traLoiHoTro, danhDauDaDocPhien } from '@/api/hoTroApi'
import { subscribeAdminHoTroInbox, subscribeAdminHoTroPhien } from '@/composables/useRealtime'

const sessions = ref([])
const selectedId = ref(null)
const messages = ref([])
const draft = ref('')
const loadingList = ref(false)
const loadingChat = ref(false)
const sending = ref(false)
const error = ref('')
const messagesEl = ref(null)

let unsubInbox = null
let unsubPhien = null

const selectedSession = computed(() =>
  sessions.value.find((s) => s.id === selectedId.value) || null,
)

async function loadSessions() {
  loadingList.value = true
  error.value = ''
  try {
    const res = await danhSachPhienHoTro()
    sessions.value = res.data || []
  } catch (err) {
    error.value = String(err)
  } finally {
    loadingList.value = false
  }
}

async function selectSession(id) {
  selectedId.value = id
  loadingChat.value = true
  cleanupPhienSub()
  try {
    const res = await layTinNhanHoTro(id)
    messages.value = res.data || []
    unsubPhien = subscribeAdminHoTroPhien(id, onRealtimePhien)
    try {
      await danhDauDaDocPhien(id)
      clearUnread(id)
    } catch {
      // mở chat vẫn được dù đánh dấu đọc lỗi
    }
    await scrollChat()
  } catch (err) {
    error.value = String(err)
    messages.value = []
  } finally {
    loadingChat.value = false
  }
}

function clearUnread(idPhien) {
  const idx = sessions.value.findIndex((s) => s.id === idPhien)
  if (idx >= 0) {
    sessions.value[idx] = { ...sessions.value[idx], soTinChuaDoc: 0 }
  }
}

function onRealtimeInbox(payload) {
  if (!payload?.idPhien) return
  const idx = sessions.value.findIndex((s) => s.id === payload.idPhien)
  const isKhach = payload.tinNhan?.nguoiGui === 'KHACH'
  const prevUnread = idx >= 0 ? Number(sessions.value[idx].soTinChuaDoc) || 0 : 0
  const patch = {
    id: payload.idPhien,
    tenKhachHang: payload.tenKhachHang || 'Khách',
    tinCuoi: payload.noiDungTomTat || payload.tinNhan?.noiDung || '',
    nguoiGuiCuoi: payload.tinNhan?.nguoiGui,
    capNhatCuoi: payload.tinNhan?.thoiGian || new Date().toISOString(),
    trangThai: 'MO',
    soTinChuaDoc:
      selectedId.value === payload.idPhien
        ? 0
        : isKhach
          ? (idx >= 0 ? prevUnread + 1 : 1)
          : (idx >= 0 ? prevUnread : 0),
  }
  if (idx >= 0) {
    sessions.value[idx] = { ...sessions.value[idx], ...patch }
    const [item] = sessions.value.splice(idx, 1)
    sessions.value.unshift(item)
  } else {
    sessions.value.unshift(patch)
  }
  if (selectedId.value === payload.idPhien && isKhach) {
    void danhDauDaDocPhien(payload.idPhien)
  }
}

function onRealtimePhien(payload) {
  const tin = payload?.tinNhan
  if (!tin?.id) return
  if (messages.value.some((m) => m.id === tin.id)) return
  messages.value.push(tin)
  scrollChat()
  if (tin.nguoiGui === 'KHACH' && selectedId.value) {
    void danhDauDaDocPhien(selectedId.value)
    clearUnread(selectedId.value)
  }
}

async function sendReply() {
  if (!selectedId.value || !draft.value.trim() || sending.value) return
  sending.value = true
  try {
    const res = await traLoiHoTro(selectedId.value, { noiDung: draft.value.trim() })
    const tin = res.data
    if (tin?.id && !messages.value.some((m) => m.id === tin.id)) {
      messages.value.push(tin)
    }
    draft.value = ''
    await scrollChat()
    void loadSessions()
  } catch (err) {
    error.value = String(err)
  } finally {
    sending.value = false
  }
}

function formatTime(v) {
  if (!v) return ''
  const d = new Date(v)
  return d.toLocaleString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit',
    day: '2-digit',
    month: '2-digit',
  })
}

async function scrollChat() {
  await nextTick()
  if (messagesEl.value) {
    messagesEl.value.scrollTop = messagesEl.value.scrollHeight
  }
}

function cleanupPhienSub() {
  if (unsubPhien) {
    unsubPhien()
    unsubPhien = null
  }
}

onMounted(async () => {
  await loadSessions()
  unsubInbox = subscribeAdminHoTroInbox(onRealtimeInbox)
})

onBeforeUnmount(() => {
  if (unsubInbox) unsubInbox()
  cleanupPhienSub()
})

watch(selectedId, () => {
  error.value = ''
})
</script>

<template>
  <div class="support-page">
    <PageHeader
      title="Hỗ trợ khách hàng"
      subtitle="Shared inbox — mọi nhân viên đều thấy và trả lời khách đang chat"
    />

    <p v-if="error" class="support-error">{{ error }}</p>

    <div class="support-layout">
      <aside class="support-inbox">
        <div class="support-inbox__head">
          <span>Phiên đang mở</span>
          <button type="button" class="admin-icon-btn" title="Tải lại" @click="loadSessions">
            <Icon icon="mdi:refresh" width="18" />
          </button>
        </div>

        <div v-if="loadingList" class="support-empty">Đang tải...</div>
        <div v-else-if="sessions.length === 0" class="support-empty">Chưa có phiên chat</div>

        <button
          v-for="s in sessions"
          :key="s.id"
          type="button"
          class="support-session"
          :class="{ 'support-session--active': selectedId === s.id }"
          @click="selectSession(s.id)"
        >
          <div class="support-session__top">
            <strong class="support-session__name">
              <span
                v-if="(s.soTinChuaDoc || 0) > 0"
                class="support-unread-dot"
                :title="`${s.soTinChuaDoc} tin chưa đọc`"
              />
              {{ s.tenKhachHang || 'Khách' }}
              <span v-if="(s.soTinChuaDoc || 0) > 0" class="support-unread-count">
                {{ s.soTinChuaDoc }}
              </span>
            </strong>
            <span>{{ formatTime(s.capNhatCuoi) }}</span>
          </div>
          <p class="support-session__preview">
            <template v-if="s.nguoiGuiCuoi === 'NHAN_VIEN'">Bạn: </template>
            {{ s.tinCuoi || '—' }}
          </p>
        </button>
      </aside>

      <section class="support-chat">
        <div v-if="!selectedId" class="support-empty support-empty--center">
          Chọn một phiên bên trái để trả lời
        </div>

        <template v-else>
          <div class="support-chat__head">
            <div>
              <h2>{{ selectedSession?.tenKhachHang || 'Khách' }}</h2>
              <p v-if="selectedSession?.tenNguoiXuLy">
                Đang xử lý: {{ selectedSession.tenNguoiXuLy }}
              </p>
              <p v-else class="support-muted">Chưa có nhân viên nhận — ai rảnh vào trả lời</p>
            </div>
            <span class="support-badge">#{{ selectedId }}</span>
          </div>

          <div ref="messagesEl" class="support-chat__body">
            <div v-if="loadingChat" class="support-empty">Đang tải tin nhắn...</div>
            <div
              v-for="m in messages"
              :key="m.id"
              class="support-msg"
              :class="m.nguoiGui === 'NHAN_VIEN' ? 'support-msg--staff' : 'support-msg--customer'"
            >
              <div class="support-msg__bubble">
                <p>{{ m.noiDung }}</p>
                <span>
                  {{ formatTime(m.thoiGian) }} ·
                  {{
                    m.nguoiGui === 'NHAN_VIEN'
                      ? (m.tenNguoiGui ? `NV ${m.tenNguoiGui}` : 'NV')
                      : 'KH'
                  }}
                </span>
              </div>
            </div>
          </div>

          <form class="support-chat__footer" @submit.prevent="sendReply">
            <input
              v-model="draft"
              type="text"
              class="admin-input"
              placeholder="Nhập nội dung trả lời..."
              :disabled="sending"
            />
            <button
              type="submit"
              class="soleil-btn-primary"
              :disabled="sending || !draft.trim()"
            >
              {{ sending ? 'Đang gửi...' : 'Gửi' }}
            </button>
          </form>
        </template>
      </section>
    </div>
  </div>
</template>

<style scoped>
.support-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: calc(100vh - 140px);
}

.support-error {
  margin: 0;
  padding: 10px 14px;
  border-radius: 10px;
  background: rgba(220, 80, 60, 0.08);
  color: var(--coral, #c45c4a);
  border: 1px solid rgba(220, 80, 60, 0.2);
}

.support-layout {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 16px;
  flex: 1;
  min-height: 520px;
}

.support-inbox,
.support-chat {
  background: var(--admin-surface, #fff);
  border: 1px solid var(--admin-border, #e8dcc8);
  border-radius: 14px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.support-inbox__head,
.support-chat__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  border-bottom: 1px solid var(--admin-border, #e8dcc8);
  background: rgba(201, 169, 110, 0.06);
}

.support-inbox__head span,
.support-chat__head h2 {
  margin: 0;
  font-weight: 600;
  color: var(--admin-text, var(--ink, #1a1814));
}

.support-chat__head h2 {
  font-size: 1.05rem;
}

.support-chat__head p {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--admin-muted, #8a8278);
}

.support-muted {
  color: var(--admin-muted, #8a8278) !important;
}

.support-badge {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(201, 169, 110, 0.15);
  color: var(--warm-tan, #c9a96e);
}

.support-session {
  width: 100%;
  text-align: left;
  padding: 12px 16px;
  border: none;
  border-bottom: 1px solid var(--admin-border, #e8dcc8);
  background: transparent;
  cursor: pointer;
}

.support-session:hover {
  background: rgba(201, 169, 110, 0.08);
}

.support-session--active {
  background: rgba(201, 169, 110, 0.14);
}

.support-session__top {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  font-size: 13px;
}

.support-session__name {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.support-unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--sage, #7a8c6e);
  flex-shrink: 0;
  box-shadow: 0 0 0 2px rgba(122, 140, 110, 0.25);
}

.support-unread-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  color: #fff;
  background: var(--sage, #7a8c6e);
}

.support-session__top > span {
  color: var(--admin-muted, #8a8278);
  font-size: 11px;
  white-space: nowrap;
}

.support-session__preview {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--admin-muted, #8a8278);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.support-inbox {
  max-height: 70vh;
  overflow: auto;
}

.support-chat__body {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  background: var(--admin-bg, #f7f3ec);
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 360px;
  max-height: 55vh;
}

.support-msg {
  display: flex;
  max-width: 78%;
}

.support-msg--customer {
  align-self: flex-start;
}

.support-msg--staff {
  align-self: flex-end;
}

.support-msg__bubble {
  padding: 10px 14px;
  border-radius: 14px;
  border: 1px solid var(--admin-border, #e8dcc8);
  background: #fff;
}

.support-msg--staff .support-msg__bubble {
  background: rgba(201, 169, 110, 0.18);
  border-color: rgba(201, 169, 110, 0.35);
}

.support-msg__bubble p {
  margin: 0;
  font-size: 14px;
  line-height: 1.45;
  color: var(--admin-text, #1a1814);
  white-space: pre-wrap;
}

.support-msg__bubble span {
  display: block;
  margin-top: 6px;
  font-size: 11px;
  color: var(--admin-muted, #8a8278);
}

.support-chat__footer {
  display: flex;
  gap: 10px;
  padding: 12px 16px;
  border-top: 1px solid var(--admin-border, #e8dcc8);
}

.support-chat__footer .admin-input {
  flex: 1;
}

.support-empty {
  padding: 20px;
  color: var(--admin-muted, #8a8278);
  font-size: 13px;
}

.support-empty--center {
  margin: auto;
  text-align: center;
}

@media (max-width: 900px) {
  .support-layout {
    grid-template-columns: 1fr;
  }
}
</style>
