import { computed, ref } from 'vue'
import { searchHoaDon } from '@/api/hoaDonApi'
import { docTatCaThongBao, docThongBao, getThongBao } from '@/api/thongBaoApi'
import { fetchTraHangList } from '@/api/traHangApi'
import { fetchHoanTienList } from '@/api/hoanTienApi'
import { getSanPhamCanhBaoCount } from '@/api/sanPhamApi'
import { danhSachPhienHoTro } from '@/api/hoTroApi'
import { demYeuCauMuaSoLuongLonMoi } from '@/api/yeuCauMuaSoLuongLonApi'
import { useAdminAuth } from '@/composables/useAdminAuth'
import { toast } from '@/composables/useToast'
import {
  subscribeAdminHoTroInbox,
  subscribeAdminNotifications,
  subscribeAdminOrders,
} from '@/composables/useRealtime'

const MAX_PENDING_ORDERS = 10
const MAX_PENDING_RETURNS = 10
const MAX_PENDING_SUPPORT = 10
const POLL_MS = 60000

/** Các loại đã hiển thị ở mục riêng trong modal chuông — không đưa vào "Khác" */
const EXCLUDED_FROM_OTHER = new Set(['DON_HANG_MOI', 'YEU_CAU_TRA_HANG', 'TIN_HO_TRO_MOI'])

const pendingOrders = ref([])
const pendingOrderCount = ref(0)
const pendingReturns = ref([])
const pendingReturnCount = ref(0)
const pendingRefundCount = ref(0)
const productWarnCount = ref(0)
const pendingSupport = ref([])
const pendingSupportCount = ref(0)
const supportUnreadSessions = ref(0)
const pendingBulkCount = ref(0)
const otherNotifications = ref([])
const unreadOtherCount = ref(0)

/** Badge chuông = đơn chờ + trả hàng + hỗ trợ chưa đọc + thông báo khác */
const notifBadgeCount = computed(
  () =>
    pendingOrderCount.value
    + pendingReturnCount.value
    + pendingSupportCount.value
    + unreadOtherCount.value,
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
  '/admin/products': productWarnCount.value,
  '/admin/support': supportUnreadSessions.value,
  '/admin/yeu-cau-mua-so-luong-lon': pendingBulkCount.value,
}))

/** Dùng buộc NMenu re-render khi số badge đổi */
const badgeVersion = computed(
  () =>
    `${pendingOrderCount.value}-${pendingReturnCount.value}-${pendingRefundCount.value}-${productWarnCount.value}-${pendingSupportCount.value}-${supportUnreadSessions.value}-${pendingBulkCount.value}-${unreadOtherCount.value}`,
)

let pollTimer = null
let subscriberCount = 0
let unsubOrders = null
let unsubNotifications = null
let unsubHoTroInbox = null
let lastToastAt = 0

function sortByNewest(list) {
  return [...list].sort((a, b) => {
    const ta = a.ngayTao || a.thoiGian || a.capNhatCuoi
      ? new Date(a.ngayTao || a.thoiGian || a.capNhatCuoi).getTime()
      : 0
    const tb = b.ngayTao || b.thoiGian || b.capNhatCuoi
      ? new Date(b.ngayTao || b.thoiGian || b.capNhatCuoi).getTime()
      : 0
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
    const res = await searchHoaDon({
      trangThai: 'CHO_XAC_NHAN',
      page: 1,
      size: MAX_PENDING_ORDERS,
    })
    const data = res.data || {}
    const list = asArray(data.content)
    pendingOrderCount.value = Number(data.totalElements) || list.length
    pendingOrders.value = sortByNewest(list).slice(0, MAX_PENDING_ORDERS)
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

async function loadProductWarnings() {
  const { isLoggedIn } = useAdminAuth()
  if (!isLoggedIn.value) return
  try {
    const res = await getSanPhamCanhBaoCount()
    const sapHetHang = Number(res.data?.sapHetHang) || 0
    const canHan = Number(res.data?.canHan) || 0
    productWarnCount.value = sapHetHang + canHan
  } catch {
    // im lặng
  }
}

async function loadSupportUnread() {
  const { isLoggedIn } = useAdminAuth()
  if (!isLoggedIn.value) return
  try {
    const res = await danhSachPhienHoTro()
    const list = asArray(res.data)
    const withUnread = list.filter((s) => Number(s.soTinChuaDoc) > 0)
    supportUnreadSessions.value = withUnread.length
    // Fallback danh sách chuông nếu chưa có ThongBao
    if (!pendingSupport.value.length || pendingSupport.value.every((x) => !x.fromThongBao)) {
      pendingSupport.value = sortByNewest(withUnread)
        .slice(0, MAX_PENDING_SUPPORT)
        .map((s) => ({
          id: `phien-${s.id}`,
          idPhien: s.id,
          tieuDe: 'Tin hỗ trợ mới',
          noiDung: `Khách ${s.tenKhachHang || '—'}${s.soDienThoai ? ` (${s.soDienThoai})` : ''} vừa nhắn hỗ trợ`,
          link: `/admin/support?phien=${s.id}`,
          daDoc: false,
          ngayTao: s.capNhatCuoi || s.ngayTao,
          loai: 'TIN_HO_TRO_MOI',
          fromThongBao: false,
        }))
    }
  } catch {
    // im lặng
  }
}

async function loadOtherNotifications({ updateList = true } = {}) {
  const { isLoggedIn } = useAdminAuth()
  if (!isLoggedIn.value) return
  try {
    const res = await getThongBao()
    const all = asArray(res.data?.danhSach)
    const supportNotifs = all.filter((n) => n.loai === 'TIN_HO_TRO_MOI')
    pendingSupportCount.value = supportNotifs.filter((n) => !n.daDoc).length

    const list = all.filter((n) => !EXCLUDED_FROM_OTHER.has(n.loai))
    unreadOtherCount.value = list.filter((n) => !n.daDoc).length
    if (updateList) {
      otherNotifications.value = list
      // Đồng bộ danh sách chuông hỗ trợ từ ThongBao (ưu tiên tiêu đề/nội dung)
      if (supportNotifs.length) {
        pendingSupport.value = sortByNewest(supportNotifs)
          .slice(0, MAX_PENDING_SUPPORT)
          .map((n) => ({
            id: n.id,
            idPhien: n.idThamChieu,
            tieuDe: n.tieuDe,
            noiDung: n.noiDung,
            link: n.link,
            daDoc: n.daDoc,
            ngayTao: n.thoiGian || n.ngayTao,
            loai: n.loai,
            fromThongBao: true,
          }))
      }
    }
  } catch {
    // im lặng
  }
}

async function loadPendingBulk() {
  const { isLoggedIn } = useAdminAuth()
  if (!isLoggedIn.value) {
    pendingBulkCount.value = 0
    return
  }
  try {
    const res = await demYeuCauMuaSoLuongLonMoi()
    pendingBulkCount.value = Number(res.data?.count) || 0
  } catch {
    pendingBulkCount.value = 0
  }
}

async function refreshBadges() {
  await Promise.all([
    loadPendingOrders(),
    loadPendingReturns(),
    loadPendingRefunds(),
    loadProductWarnings(),
    loadSupportUnread(),
    loadPendingBulk(),
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
  if (unsubOrders || unsubNotifications || unsubHoTroInbox) return
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
  unsubHoTroInbox = subscribeAdminHoTroInbox((event) => {
    refreshBadges()
    const isKhach = String(event?.nguoiGui || event?.tinNhan?.nguoiGui || '').toUpperCase() === 'KHACH'
    if (isKhach) {
      const ten = event?.tenKhachHang || event?.tinNhan?.tenNguoiGui || 'Khách'
      maybeToast(`Khách ${ten} vừa nhắn hỗ trợ`)
    }
  })
}

function unbindRealtime() {
  unsubOrders?.()
  unsubNotifications?.()
  unsubHoTroInbox?.()
  unsubOrders = null
  unsubNotifications = null
  unsubHoTroInbox = null
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
    if (item.loai === 'TIN_HO_TRO_MOI') {
      pendingSupportCount.value = Math.max(0, pendingSupportCount.value - 1)
    } else {
      unreadOtherCount.value = Math.max(0, unreadOtherCount.value - 1)
    }
  } catch {
    // vẫn điều hướng
  }
}

async function markAllNotificationsRead() {
  try {
    await docTatCaThongBao()
    otherNotifications.value = otherNotifications.value.map((n) => ({ ...n, daDoc: true }))
    pendingSupport.value = pendingSupport.value.map((n) => ({ ...n, daDoc: true }))
    unreadOtherCount.value = 0
    pendingSupportCount.value = 0
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
    productWarnCount,
    pendingSupport,
    pendingSupportCount,
    supportUnreadSessions,
    pendingBulkCount,
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
    loadSupportUnread,
    loadOtherNotifications,
    markNotificationRead,
    markAllNotificationsRead,
  }
}
