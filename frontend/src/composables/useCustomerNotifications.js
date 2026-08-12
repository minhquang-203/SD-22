import { computed, ref } from 'vue'
import {
  docTatCaThongBaoKhach,
  docThongBaoKhach,
  getThongBaoKhach,
} from '@/api/thongBaoApi'
import { isCustomerLoggedIn } from '@/composables/useAuth'
import { toast } from '@/composables/useToast'
import { subscribeCustomerNotifications } from '@/composables/useRealtime'

const POLL_MS = 60000

const notifications = ref([])
const unreadCount = ref(0)

const hasBadge = computed(() => unreadCount.value > 0)
const badgeText = computed(() => (unreadCount.value > 99 ? '99+' : String(unreadCount.value)))

let pollTimer = null
let subscriberCount = 0
let unsubRealtime = null
let lastToastAt = 0

function asArray(data) {
  return Array.isArray(data) ? data : []
}

function sortByNewest(list) {
  return [...list].sort((a, b) => {
    const ta = a.ngayTao ? new Date(a.ngayTao).getTime() : 0
    const tb = b.ngayTao ? new Date(b.ngayTao).getTime() : 0
    if (tb !== ta) return tb - ta
    return (b.id ?? 0) - (a.id ?? 0)
  })
}

async function loadNotifications() {
  if (!isCustomerLoggedIn()) {
    notifications.value = []
    unreadCount.value = 0
    return
  }
  try {
    const res = await getThongBaoKhach()
    const list = asArray(res.data?.danhSach)
    notifications.value = sortByNewest(list)
    unreadCount.value = Number(res.data?.soChuaDoc) || list.filter((n) => !n.daDoc).length
  } catch {
    // im lặng khi polling lỗi tạm thời
  }
}

function maybeToast(message) {
  if (!message) return
  const now = Date.now()
  if (now - lastToastAt < 1500) return
  lastToastAt = now
  toast(message, 'info')
}

function onRealtimeEvent(event) {
  if (!event) return
  // Chèn thông báo mới lên đầu nếu chưa có, cập nhật số chưa đọc + toast.
  if (event.id != null && !notifications.value.some((n) => n.id === event.id)) {
    notifications.value = [
      {
        id: event.id,
        loai: event.loai,
        tieuDe: event.tieuDe,
        noiDung: event.noiDung,
        link: event.link,
        idThamChieu: event.idThamChieu,
        maThamChieu: event.maThamChieu,
        daDoc: false,
        ngayTao: new Date().toISOString(),
      },
      ...notifications.value,
    ]
    unreadCount.value += 1
  } else {
    loadNotifications()
  }
  maybeToast(event.tieuDe || event.noiDung)
}

function bindRealtime() {
  if (unsubRealtime) return
  unsubRealtime = subscribeCustomerNotifications(onRealtimeEvent)
}

function unbindRealtime() {
  unsubRealtime?.()
  unsubRealtime = null
}

function startPolling() {
  subscriberCount += 1
  if (subscriberCount === 1) {
    loadNotifications()
    bindRealtime()
    pollTimer = setInterval(() => loadNotifications(), POLL_MS)
  }
}

function stopPolling() {
  subscriberCount = Math.max(0, subscriberCount - 1)
  if (subscriberCount === 0) {
    if (pollTimer) {
      clearInterval(pollTimer)
      pollTimer = null
    }
    unbindRealtime()
  }
}

/** Gọi khi trạng thái đăng nhập khách thay đổi để nối lại realtime + tải lại. */
function onAuthChanged() {
  unbindRealtime()
  if (subscriberCount > 0) {
    loadNotifications()
    bindRealtime()
  } else {
    notifications.value = []
    unreadCount.value = 0
  }
}

if (typeof window !== 'undefined') {
  window.addEventListener('sunova-customer-auth-changed', onAuthChanged)
}

async function markRead(item) {
  if (item?.id == null || item.daDoc) return
  try {
    await docThongBaoKhach(item.id)
  } catch {
    // vẫn cập nhật UI
  }
  item.daDoc = true
  unreadCount.value = Math.max(0, unreadCount.value - 1)
}

async function markAllRead() {
  try {
    await docTatCaThongBaoKhach()
  } catch {
    // ignore
  }
  notifications.value = notifications.value.map((n) => ({ ...n, daDoc: true }))
  unreadCount.value = 0
}

export function useCustomerNotifications() {
  return {
    notifications,
    unreadCount,
    hasBadge,
    badgeText,
    startPolling,
    stopPolling,
    loadNotifications,
    markRead,
    markAllRead,
  }
}
