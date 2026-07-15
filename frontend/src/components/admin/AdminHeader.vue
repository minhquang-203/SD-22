<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import AppLogo from '@/components/common/AppLogo.vue'
import { useAdminAuth } from '@/composables/useAdminAuth'
import { useAdminBadges } from '@/composables/useAdminBadges'
import { getRoleLabel } from '@/utils/adminAuth'
import { formatCurrency } from '@/utils/format'
import { orderStatusLabel } from '@/utils/orderStatus'

const props = defineProps({
  title: { type: String, default: 'SUNOVA Admin' },
  breadcrumb: { type: String, default: '' },
})

const emit = defineEmits(['toggle-sidebar'])

const router = useRouter()
const { hoTen, vaiTro, dangXuat } = useAdminAuth()
const {
  pendingOrders,
  pendingOrderCount,
  pendingReturns,
  pendingReturnCount,
  otherNotifications,
  unreadOtherCount,
  hasNotifBadge,
  notifBadgeText,
  startPolling,
  stopPolling,
  loadPendingOrders,
  loadPendingReturns,
  loadOtherNotifications,
  markNotificationRead,
  markAllNotificationsRead,
} = useAdminBadges()

const pageLabel = () => (props.title || props.breadcrumb || 'Trang quản trị').toUpperCase()
const roleLabel = computed(() => getRoleLabel(vaiTro.value))
const displayName = computed(() => hoTen.value || 'Quản trị viên')
const avatarLetter = computed(() => {
  const name = displayName.value.trim()
  return name ? name.charAt(0).toUpperCase() : 'A'
})

const showDropdown = ref(false)
const loadingNotif = ref(false)

const hasBadge = hasNotifBadge
const badgeText = notifBadgeText
const hasContent = computed(
  () =>
    pendingOrders.value.length > 0
    || pendingReturns.value.length > 0
    || otherNotifications.value.length > 0,
)

function iconForLoai(loai) {
  const map = {
    DON_HANG_MOI: 'icon-park-outline:shopping-bag',
    THANH_TOAN_THANH_CONG: 'icon-park-outline:pay-code-one',
    YEU_CAU_TRA_HANG: 'icon-park-outline:return',
    YEU_CAU_HOAN_TIEN: 'icon-park-outline:wallet',
    HOAN_TIEN_HOAN_TAT: 'icon-park-outline:check-one',
  }
  return map[loai] || 'icon-park-outline:remind'
}

function resolveNotifLink(item) {
  if (item?.link) return item.link
  switch (item?.loai) {
    case 'YEU_CAU_TRA_HANG':
      return '/admin/tra-hang'
    case 'YEU_CAU_HOAN_TIEN':
    case 'HOAN_TIEN_HOAN_TAT':
      return '/admin/hoan-tien'
    case 'DON_HANG_MOI':
    case 'THANH_TOAN_THANH_CONG':
      return item?.idThamChieu != null
        ? `/admin/hoa-don/chi-tiet/${item.idThamChieu}`
        : '/admin/hoa-don'
    default:
      return '/admin/hoa-don'
  }
}

function customerDisplay(order) {
  return order?.tenKhachHang || 'Khách lẻ'
}

function statusLabel(trangThai) {
  if (trangThai === 'CHO') return 'Chờ tại quầy'
  if (trangThai === 'HOAN_THANH') return 'Hoàn thành'
  return orderStatusLabel(trangThai)
}

async function toggleDropdown() {
  showDropdown.value = !showDropdown.value
  if (showDropdown.value) {
    loadingNotif.value = true
    await Promise.all([
      loadPendingOrders(),
      loadPendingReturns(),
      loadOtherNotifications({ updateList: true }),
    ])
    loadingNotif.value = false
  }
}

function goToOrder(order) {
  showDropdown.value = false
  if (order?.id != null) {
    router.push(`/admin/hoa-don/chi-tiet/${order.id}`)
  }
}

function goToReturn() {
  showDropdown.value = false
  router.push('/admin/tra-hang')
}

async function goToNotification(item) {
  showDropdown.value = false
  await markNotificationRead(item)
  const link = resolveNotifLink(item)
  if (link) router.push(link)
}

async function markAllRead() {
  await markAllNotificationsRead()
}

function goToAllPendingOrders() {
  showDropdown.value = false
  router.push('/admin/hoa-don')
}

function goToAllPendingReturns() {
  showDropdown.value = false
  router.push('/admin/tra-hang')
}

function onClickOutside(event) {
  const el = document.getElementById('admin-notif-wrap')
  if (el && !el.contains(event.target)) {
    showDropdown.value = false
  }
}

function formatTime(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  const diffMs = Date.now() - date.getTime()
  const diffMin = Math.floor(diffMs / 60000)
  if (diffMin < 1) return 'Vừa xong'
  if (diffMin < 60) return `${diffMin} phút trước`
  const diffHour = Math.floor(diffMin / 60)
  if (diffHour < 24) return `${diffHour} giờ trước`
  return date.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

onMounted(() => {
  startPolling()
  document.addEventListener('click', onClickOutside)
})

onBeforeUnmount(() => {
  stopPolling()
  document.removeEventListener('click', onClickOutside)
})

async function handleLogout() {
  dangXuat()
  await router.push('/admin/dang-nhap')
}
</script>

<template>
  <header class="admin-topbar">
    <div class="admin-topbar__left">
      <button
        type="button"
        class="admin-topbar__btn-icon admin-topbar__btn-icon--menu"
        aria-label="Thu gọn menu"
        @click="emit('toggle-sidebar')"
      >
        <Icon icon="icon-park-outline:hamburger-button" />
      </button>
      <AppLogo variant="light" :size="28" class="shrink-0 hidden sm:flex" />
      <div class="admin-topbar__eyebrow">
        {{ pageLabel() }}
      </div>
    </div>

    <div class="admin-topbar__actions">
      <div id="admin-notif-wrap" class="admin-notif">
        <button
          type="button"
          class="admin-notif__btn"
          :class="{ 'admin-notif__btn--active': showDropdown }"
          aria-label="Thông báo"
          @click.stop="toggleDropdown"
        >
          <Icon icon="icon-park-outline:remind" />
          <span v-if="hasBadge" class="admin-notif__dot">{{ badgeText }}</span>
        </button>

        <div v-if="showDropdown" class="admin-notif__panel" @click.stop>
          <div class="admin-notif__header">
            <span>Thông báo</span>
            <div class="admin-notif__header-right">
              <span v-if="pendingOrderCount > 0" class="admin-notif__header-badge">
                {{ pendingOrderCount }} đơn chờ
              </span>
              <span v-if="pendingReturnCount > 0" class="admin-notif__header-badge admin-notif__header-badge--return">
                {{ pendingReturnCount }} trả hàng
              </span>
              <button
                v-if="unreadOtherCount > 0"
                type="button"
                class="admin-notif__mark-all"
                @click="markAllRead"
              >
                Đọc tất cả
              </button>
            </div>
          </div>

          <div v-if="loadingNotif" class="admin-notif__empty">Đang tải...</div>

          <div v-else-if="hasContent" class="admin-notif__scroll">
            <!-- 1. Đơn chờ xác nhận -->
            <template v-if="pendingOrders.length">
              <div class="admin-notif__section-row">
                <div class="admin-notif__section">Đơn chờ xác nhận</div>
                <button
                  type="button"
                  class="admin-notif__view-all"
                  @click="goToAllPendingOrders"
                >
                  Xem tất cả
                </button>
              </div>
              <ul class="admin-notif__list">
                <li
                  v-for="item in pendingOrders"
                  :key="`order-${item.id}`"
                  class="admin-notif__item admin-notif__item--unread"
                  @click="goToOrder(item)"
                >
                  <div class="admin-notif__item-icon" data-loai="DON_HANG_MOI">
                    <Icon icon="icon-park-outline:shopping-bag" />
                  </div>
                  <div class="admin-notif__item-body">
                    <div class="admin-notif__item-title">
                      <span class="admin-notif__item-code">{{ item.maHoaDon }}</span>
                      <span class="admin-notif__item-total">{{ formatCurrency(item.thanhTien) }}</span>
                    </div>
                    <div class="admin-notif__item-desc">
                      {{ customerDisplay(item) }} · {{ statusLabel(item.trangThai) }}
                    </div>
                    <div class="admin-notif__item-time">{{ formatTime(item.ngayTao) }}</div>
                  </div>
                </li>
              </ul>
            </template>

            <!-- 2. Yêu cầu trả hàng chờ duyệt -->
            <template v-if="pendingReturns.length">
              <div class="admin-notif__section-row">
                <div class="admin-notif__section">Yêu cầu trả hàng</div>
                <button
                  type="button"
                  class="admin-notif__view-all"
                  @click="goToAllPendingReturns"
                >
                  Xem tất cả
                </button>
              </div>
              <ul class="admin-notif__list">
                <li
                  v-for="item in pendingReturns"
                  :key="`return-${item.id}`"
                  class="admin-notif__item admin-notif__item--unread"
                  @click="goToReturn(item)"
                >
                  <div class="admin-notif__item-icon" data-loai="YEU_CAU_TRA_HANG">
                    <Icon icon="icon-park-outline:return" />
                  </div>
                  <div class="admin-notif__item-body">
                    <div class="admin-notif__item-title">
                      <span class="admin-notif__item-code">{{ item.maHoaDon || 'Yêu cầu trả hàng' }}</span>
                    </div>
                    <div class="admin-notif__item-desc">
                      {{ customerDisplay(item) }}
                      <template v-if="item.lyDo"> · {{ item.lyDo }}</template>
                    </div>
                    <div class="admin-notif__item-time">{{ formatTime(item.ngayTao) }}</div>
                  </div>
                </li>
              </ul>
            </template>

            <!-- Thông báo khác (hoàn tiền, thanh toán, …) -->
            <template v-if="otherNotifications.length">
              <div class="admin-notif__section">Khác</div>
              <ul class="admin-notif__list">
                <li
                  v-for="item in otherNotifications"
                  :key="`tb-${item.id}`"
                  class="admin-notif__item"
                  :class="{ 'admin-notif__item--unread': !item.daDoc }"
                  @click="goToNotification(item)"
                >
                  <div class="admin-notif__item-icon" :data-loai="item.loai">
                    <Icon :icon="iconForLoai(item.loai)" />
                  </div>
                  <div class="admin-notif__item-body">
                    <div class="admin-notif__item-title">
                      <span class="admin-notif__item-code">{{ item.tieuDe || item.maThamChieu || 'Thông báo' }}</span>
                    </div>
                    <div class="admin-notif__item-desc">
                      {{ item.noiDung || '—' }}
                    </div>
                    <div class="admin-notif__item-time">{{ formatTime(item.ngayTao) }}</div>
                  </div>
                </li>
              </ul>
            </template>
          </div>

          <div v-else class="admin-notif__empty">Không có thông báo mới.</div>
        </div>
      </div>

      <div class="admin-topbar__user hidden md:flex">
        <div class="admin-topbar__user-text">
          <span class="admin-topbar__user-name">{{ displayName }}</span>
          <span class="admin-topbar__user-role">{{ roleLabel }}</span>
        </div>
      </div>
      <button type="button" class="admin-topbar__btn-logout" @click="handleLogout">
        <Icon icon="icon-park-outline:logout" />
        <span class="hidden sm:inline">Đăng xuất</span>
      </button>
      <div class="admin-topbar__avatar" :title="displayName">{{ avatarLetter }}</div>
    </div>
  </header>
</template>

<style scoped>
.admin-notif {
  position: relative;
  display: flex;
  align-items: center;
}

.admin-notif__btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 10px;
  border: 1px solid var(--sand);
  background: transparent;
  color: rgba(30, 21, 16, 0.75);
  font-size: 20px;
  cursor: pointer;
  transition: background 0.15s ease, border-color 0.15s ease, color 0.15s ease;
}

.admin-notif__btn:hover,
.admin-notif__btn--active {
  background: rgba(201, 169, 110, 0.06);
  border-color: var(--warm-tan);
  color: var(--bronze);
}

.admin-notif__dot {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  background: #ef4444;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  line-height: 18px;
  text-align: center;
  box-shadow: 0 0 0 2px rgba(0, 0, 0, 0.15);
}

.admin-notif__panel {
  position: absolute;
  top: calc(100% + 10px);
  right: 0;
  width: 380px;
  max-width: 90vw;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 18px 50px rgba(15, 23, 42, 0.25);
  border: 1px solid #eef0f3;
  overflow: hidden;
  z-index: 1200;
}

.admin-notif__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 14px 16px;
  font-weight: 700;
  color: #1f2430;
  border-bottom: 1px solid #f1f2f4;
}

.admin-notif__header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.admin-notif__header-badge {
  font-size: 12px;
  font-weight: 600;
  color: #8a6428;
  background: rgba(196, 149, 84, 0.18);
  padding: 2px 8px;
  border-radius: 999px;
}

.admin-notif__header-badge--return {
  color: #be123c;
  background: rgba(225, 29, 72, 0.12);
}

.admin-notif__mark-all {
  border: none;
  background: transparent;
  color: var(--bronze, #a67c3d);
  font-size: 12px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  padding: 0;
}

.admin-notif__mark-all:hover {
  text-decoration: underline;
}

.admin-notif__scroll {
  max-height: 420px;
  overflow-y: auto;
}

.admin-notif__section-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  background: #fafafa;
  padding-right: 12px;
}

.admin-notif__section {
  padding: 8px 16px 4px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: #9aa0aa;
  background: #fafafa;
}

.admin-notif__section-row .admin-notif__section {
  flex: 1;
  padding-bottom: 8px;
}

.admin-notif__view-all {
  border: none;
  background: transparent;
  color: var(--bronze, #a67c3d);
  font-size: 12px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  padding: 0;
  white-space: nowrap;
}

.admin-notif__view-all:hover {
  text-decoration: underline;
}

.admin-notif__list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.admin-notif__item {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid #f5f6f8;
  transition: background 0.12s ease;
}

.admin-notif__item:hover {
  background: #f9fafb;
}

.admin-notif__item--unread {
  background: #fff7ed;
}

.admin-notif__item--unread:hover {
  background: #ffedd5;
}

.admin-notif__item-icon {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fef3c7;
  color: #f97316;
  font-size: 18px;
}

.admin-notif__item-icon[data-loai='YEU_CAU_TRA_HANG'] {
  background: #fff1f2;
  color: #e11d48;
}

.admin-notif__item-icon[data-loai='YEU_CAU_HOAN_TIEN'],
.admin-notif__item-icon[data-loai='HOAN_TIEN_HOAN_TAT'] {
  background: #ecfdf5;
  color: #059669;
}

.admin-notif__item-icon[data-loai='DON_HANG_MOI'],
.admin-notif__item-icon[data-loai='THANH_TOAN_THANH_CONG'] {
  background: #eff6ff;
  color: #2563eb;
}

.admin-notif__item-body {
  min-width: 0;
  flex: 1;
}

.admin-notif__item-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  font-weight: 600;
  color: #1f2430;
  font-size: 14px;
}

.admin-notif__item-code {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.admin-notif__item-total {
  flex-shrink: 0;
  color: #a67c3d;
  font-weight: 700;
}

.admin-notif__item-desc {
  color: #4b5563;
  font-size: 13px;
  margin-top: 2px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.admin-notif__item-time {
  color: #9aa0aa;
  font-size: 12px;
  margin-top: 4px;
}

.admin-notif__empty {
  padding: 28px 16px;
  text-align: center;
  color: #9aa0aa;
  font-size: 14px;
}
</style>
