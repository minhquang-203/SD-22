import { computed, ref } from 'vue'
import { getAllHoaDon } from '@/api/hoaDonApi'
import { docTatCaThongBao, docThongBao, getThongBao } from '@/api/thongBaoApi'
import { fetchTraHangList } from '@/api/traHangApi'
import { fetchHoanTienList } from '@/api/hoanTienApi'
import { useAdminAuth } from '@/composables/useAdminAuth'
import { toast } from '@/composables/useToast'
import {
  subscribeAdminNotifications,
  subscribeAdminOrders,
} from '@/composables/useRealtime'

const MAX_PENDING_ORDERS = 10
const MAX_PENDING_RETURNS = 10
const POLL_MS = 60000

/** Các loại đã hiển thị ở mục riêng trong modal chuông — không đưa vào "Khác" */
const EXCLUDED_FROM_OTHER = new Set(['DON_HANG_MOI', 'YEU_CAU_TRA_HANG'])

const pendingOrders = ref([])
const pendingOrderCount = ref(0)
const pendingReturns = ref([])
const pendingReturnCount = ref(0)
const pendingRefundCount = ref(0)
const otherNotifications = ref([])
const unreadOtherCount = ref(0)

/** Badge chuông = đơn chờ + trả hàng chờ duyệt + thông báo khác chưa đọc */
const notifBadgeCount = computed(
  () => pendingOrderCount.value + pendingReturnCount.value + unreadOtherCount.value,
)

/** Badge mục Hóa đơn — chỉ đếm đơn chờ xác nhận */
const hoaDonSidebarBadge = computed(() => pendingOrderCount.value)

const hasNotifBadge = computed(() => notifBadgeCount.value > 0)
const notifBadgeText = computed(() =>
  notifBadgeCount.value > 99 ? '99+' : String(notifBadgeCount.value),
)

const sidebarBadgeByPath = computed(() => ({
  '/admin/hoa-don': hoaDonSidebarBadge.value,
  '/admin/tra-hang': pendingReturnCount.value,
  '/admin/hoan-tien': pendingRefundCount.value,
}))

/** Dùng buộc NMenu re-render khi số badge đổi */
const badgeVersion = computed(
  () =>
    `${pendingOrderCount.value}-${pendingReturnCount.value}-${pendingRefundCount.value}-${unreadOtherCount.value}`,
)

let pollTimer = null
let subscriberCount = 0
let unsubOrders = null
let unsubNotifications = null
let lastToastAt = 0

function sortByNewest(list) {
  return [...list].sort((a, b) => {
    const ta = a.ngayTao ? new Date(a.ngayTao).getTime() : 0
    const tb = b.ngayTao ? new Date(b.ngayTao).getTime() : 0
    if (tb !== ta) return tb - ta
    return (b.id ?? 0) - (a.id ?? 0)
  })
}

function formatBadge(count) {
  if (!count || count <= 0) return ''
  return count > 99 ? '99+' : String(count)
}

function asArray(data) {
  return Array.isArray(data) ? data : []
}

async function loadPendingOrders() {
  const { isLoggedIn } = useAdminAuth()
  if (!isLoggedIn.value) return
  try {
    const res = await getAllHoaDon()
    const all = asArray(res.data)
    const choXacNhan = all.filter((o) => o.trangThai === 'CHO_XAC_NHAN')
    pendingOrderCount.value = choXacNhan.length
    pendingOrders.value = sortByNewest(choXacNhan).slice(0, MAX_PENDING_ORDERS)
  } catch {
    // im lặng khi polling lỗi tạm thời
  }
}

async function loadPendingReturns() {
  const { isLoggedIn } = useAdminAuth()
  if (!isLoggedIn.value) return
  try {
    const res = await fetchTraHangList('CHO_DUYET')
    const list = asArray(res.data)
    pendingReturnCount.value = list.length
    pendingReturns.value = sortByNewest(list).slice(0, MAX_PENDING_RETURNS)
  } catch {
    // im lặng
  }
}

async function loadPendingRefunds() {
  const { isLoggedIn } = useAdminAuth()
  if (!isLoggedIn.value) return
  try {
    const res = await fetchHoanTienList('CHO_XU_LY')
    pendingRefundCount.value = asArray(res.data).length
  } catch {
    // im lặng
  }
}

async function loadOtherNotifications({ updateList = true } = {}) {
  const { isLoggedIn } = useAdminAuth()
  if (!isLoggedIn.value) return
  try {
    const res = await getThongBao()
    const list = asArray(res.data?.danhSach).filter((n) => !EXCLUDED_FROM_OTHER.has(n.loai))
    unreadOtherCount.value = list.filter((n) => !n.daDoc).length
    if (updateList) {
      otherNotifications.value = list
    }
  } catch {
    // im lặng
  }
}

async function refreshBadges() {
  await Promise.all([
    loadPendingOrders(),
    loadPendingReturns(),
    loadPendingRefunds(),
    loadOtherNotifications({ updateList: true }),
  ])
}

function maybeToast(message) {
  const now = Date.now()
  if (now - lastToastAt < 1500) return
  lastToastAt = now
  toast(message, 'info')
}

function bindRealtime() {
  if (unsubOrders || unsubNotifications) return
  unsubOrders = subscribeAdminOrders((event) => {
    refreshBadges()
    if (event?.type === 'ORDER_CREATED') {
      maybeToast(event.message || `Đơn mới: ${event.maHoaDon || ''}`)
    }
    window.dispatchEvent(new CustomEvent('sunova-admin-order-realtime', { detail: event }))
  })
  unsubNotifications = subscribeAdminNotifications((event) => {
    refreshBadges()
    if (event?.tieuDe) {
      maybeToast(event.tieuDe)
    }
  })
}

function unbindRealtime() {
  unsubOrders?.()
  unsubNotifications?.()
  unsubOrders = null
  unsubNotifications = null
}

function startPolling() {
  subscriberCount += 1
  if (subscriberCount === 1) {
    refreshBadges()
    bindRealtime()
    // fallback nhẹ khi WS tạm mất kết nối
    pollTimer = setInterval(() => refreshBadges(), POLL_MS)
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

async function markNotificationRead(item) {
  if (item?.id == null || item.daDoc) return
  try {
    await docThongBao(item.id)
    item.daDoc = true
    unreadOtherCount.value = Math.max(0, unreadOtherCount.value - 1)
  } catch {
    // vẫn điều hướng
  }
}

async function markAllNotificationsRead() {
  try {
    await docTatCaThongBao()
    otherNotifications.value = otherNotifications.value.map((n) => ({ ...n, daDoc: true }))
    unreadOtherCount.value = 0
  } catch {
    // ignore
  }
}

export function useAdminBadges() {
  return {
    pendingOrders,
    pendingOrderCount,
    pendingReturns,
    pendingReturnCount,
    pendingRefundCount,
    otherNotifications,
    unreadOtherCount,
    notifBadgeCount,
    hoaDonSidebarBadge,
    hasNotifBadge,
    notifBadgeText,
    sidebarBadgeByPath,
    badgeVersion,
    formatBadge,
    refreshBadges,
    startPolling,
    stopPolling,
    loadPendingOrders,
    loadPendingReturns,
    loadPendingRefunds,
    loadOtherNotifications,
    markNotificationRead,
    markAllNotificationsRead,
  }
}
