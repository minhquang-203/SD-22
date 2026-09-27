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
import { confirm } from '@/composables/useConfirm'
import { toast } from '@/composables/useToast'
import { doiMatKhauToi } from '@/api/nhanVienApi'
import { formatApiError } from '@/utils/apiError'

const props = defineProps({
  title: { type: String, default: 'SUNOVA Admin' },
  breadcrumb: { type: String, default: '' },
  sidebarCollapsed: { type: Boolean, default: false },
})

const emit = defineEmits(['toggle-sidebar'])

const router = useRouter()
const { hoTen, vaiTro, dangXuat } = useAdminAuth()
const {
  pendingOrders,
  pendingOrderCount,
  pendingReturns,
  pendingReturnCount,
  pendingSupport,
  pendingSupportCount,
  otherNotifications,
  unreadOtherCount,
  hasNotifBadge,
  notifBadgeText,
  startPolling,
  stopPolling,
  loadPendingOrders,
  loadPendingReturns,
  loadSupportUnread,
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
const showUserMenu = ref(false)
const loadingNotif = ref(false)

const showChangePw = ref(false)
const changingPw = ref(false)
const showPwCu = ref(false)
const showPwMoi = ref(false)
const showPwLai = ref(false)
const pwForm = ref({ matKhauCu: '', matKhauMoi: '', matKhauLai: '' })
const pwErrors = ref({ matKhauCu: '', matKhauMoi: '', matKhauLai: '' })

const hasBadge = hasNotifBadge
const badgeText = notifBadgeText
const hasContent = computed(
  () =>
    pendingOrders.value.length > 0
    || pendingReturns.value.length > 0
    || pendingSupport.value.length > 0
    || otherNotifications.value.length > 0,
)

function iconForLoai(loai) {
  const map = {
    DON_HANG_MOI: 'icon-park-outline:shopping-bag',
    DON_HANG: 'icon-park-outline:shopping-bag',
    THANH_TOAN_THANH_CONG: 'icon-park-outline:pay-code-one',
    YEU_CAU_TRA_HANG: 'icon-park-outline:return',
    YEU_CAU_HOAN_TIEN: 'icon-park-outline:wallet',
    HOAN_TIEN_HOAN_TAT: 'icon-park-outline:check-one',
    TIN_HO_TRO_MOI: 'icon-park-outline:message',
    KHUYEN_MAI: 'icon-park-outline:ticket',
    UV: 'icon-park-outline:sun',
    HE_THONG: 'icon-park-outline:remind',
  }
  return map[loai] || 'icon-park-outline:remind'
}

function resolveNotifLink(item) {
  if (item?.link) return item.link
  switch (item?.loai) {
    case 'TIN_HO_TRO_MOI':
      return item?.idThamChieu != null || item?.idPhien != null
        ? `/admin/support?phien=${item.idThamChieu ?? item.idPhien}`
        : '/admin/support'
    case 'YEU_CAU_TRA_HANG':
      return '/admin/tra-hang'
    case 'YEU_CAU_HOAN_TIEN':
    case 'HOAN_TIEN_HOAN_TAT':
      return '/admin/hoan-tien'
    case 'DON_HANG_MOI':
    case 'DON_HANG':
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
  showUserMenu.value = false
  showDropdown.value = !showDropdown.value
  if (showDropdown.value) {
    loadingNotif.value = true
    await Promise.all([
      loadPendingOrders(),
      loadPendingReturns(),
      loadSupportUnread(),
      loadOtherNotifications({ updateList: true }),
    ])
    loadingNotif.value = false
  }
}

function toggleUserMenu(event) {
  event.stopPropagation()
  showDropdown.value = false
  showUserMenu.value = !showUserMenu.value
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

function goToAllSupport() {
  showDropdown.value = false
  router.push('/admin/support')
}

async function goToSupport(item) {
  showDropdown.value = false
  if (item?.fromThongBao) {
    await markNotificationRead(item)
  }
  const link = resolveNotifLink(item)
  if (link) router.push(link)
}

function onClickOutside(event) {
  const notifEl = document.getElementById('admin-notif-wrap')
  if (notifEl && !notifEl.contains(event.target)) {
    showDropdown.value = false
  }
  const userEl = document.getElementById('admin-user-wrap')
  if (userEl && !userEl.contains(event.target)) {
    showUserMenu.value = false
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
  showUserMenu.value = false
  const ok = await confirm({
    title: 'Đăng xuất',
    message: 'Bạn có chắc muốn đăng xuất khỏi trang quản trị?',
    confirmText: 'Đăng xuất',
    danger: true,
  })
  if (!ok) return
  dangXuat()
  await router.push('/admin/dang-nhap')
}

function openChangePassword() {
  showUserMenu.value = false
  pwForm.value = { matKhauCu: '', matKhauMoi: '', matKhauLai: '' }
  pwErrors.value = { matKhauCu: '', matKhauMoi: '', matKhauLai: '' }
  showPwCu.value = false
  showPwMoi.value = false
  showPwLai.value = false
  showChangePw.value = true
}

function closeChangePassword() {
  if (changingPw.value) return
  showChangePw.value = false
}

function validatePwField(field) {
  const f = pwForm.value
  const e = { ...pwErrors.value }
  if (field === 'matKhauCu' || field === 'all') {
    e.matKhauCu = f.matKhauCu ? '' : 'Nhập mật khẩu hiện tại'
  }
  if (field === 'matKhauMoi' || field === 'all') {
    if (!f.matKhauMoi) e.matKhauMoi = 'Nhập mật khẩu mới'
    else if (f.matKhauMoi.length < 6) e.matKhauMoi = 'Mật khẩu tối thiểu 6 ký tự'
    else if (f.matKhauCu && f.matKhauMoi === f.matKhauCu) e.matKhauMoi = 'Mật khẩu mới phải khác mật khẩu hiện tại'
    else e.matKhauMoi = ''
  }
  if (field === 'matKhauLai' || field === 'all') {
    if (!f.matKhauLai) e.matKhauLai = 'Nhập lại mật khẩu mới'
    else if (f.matKhauLai !== f.matKhauMoi) e.matKhauLai = 'Mật khẩu nhập lại không khớp'
    else e.matKhauLai = ''
  }
  pwErrors.value = e
  return !e.matKhauCu && !e.matKhauMoi && !e.matKhauLai
}

async function submitChangePassword() {
  if (!validatePwField('all')) {
    toast('Kiểm tra lại thông tin mật khẩu', 'warn')
    return
  }
  changingPw.value = true
  try {
    await doiMatKhauToi({
      matKhauCu: pwForm.value.matKhauCu,
      matKhauMoi: pwForm.value.matKhauMoi,
    })
    showChangePw.value = false
    toast('Đổi mật khẩu thành công. Vui lòng đăng nhập lại.', 'success')
    dangXuat()
    await router.push({
      path: '/admin/dang-nhap',
      query: { passwordChanged: '1' },
    })
  } catch (err) {
    toast(formatApiError(err, 'Không đổi được mật khẩu'), 'error')
  } finally {
    changingPw.value = false
  }
}
</script>

<template>
  <header class="admin-topbar">
    <div class="admin-topbar__left">
      <button
        type="button"
        class="admin-topbar__btn-icon admin-topbar__btn-icon--menu"
        :aria-label="sidebarCollapsed ? 'Hiện menu' : 'Ẩn menu'"
        :aria-expanded="!sidebarCollapsed"
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
              <span v-if="pendingSupportCount > 0" class="admin-notif__header-badge admin-notif__header-badge--support">
                {{ pendingSupportCount }} hỗ trợ
              </span>
              <button
                v-if="unreadOtherCount > 0 || pendingSupportCount > 0"
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

            <!-- 3. Tin hỗ trợ khách hàng -->
            <template v-if="pendingSupport.length">
              <div class="admin-notif__section-row">
                <div class="admin-notif__section">Hỗ trợ khách hàng</div>
                <button
                  type="button"
                  class="admin-notif__view-all"
                  @click="goToAllSupport"
                >
                  Xem tất cả
                </button>
              </div>
              <ul class="admin-notif__list">
                <li
                  v-for="item in pendingSupport"
                  :key="`support-${item.id}`"
                  class="admin-notif__item"
                  :class="{ 'admin-notif__item--unread': !item.daDoc }"
                  @click="goToSupport(item)"
                >
                  <div class="admin-notif__item-icon" data-loai="TIN_HO_TRO_MOI">
                    <Icon icon="icon-park-outline:message" />
                  </div>
                  <div class="admin-notif__item-body">
                    <div class="admin-notif__item-title">
                      <span class="admin-notif__item-code">{{ item.tieuDe || 'Tin hỗ trợ mới' }}</span>
                    </div>
                    <div class="admin-notif__item-desc">
                      {{ item.noiDung || 'Khách vừa nhắn hỗ trợ' }}
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

      <div id="admin-user-wrap" class="admin-user-menu">
        <button
          type="button"
          class="admin-topbar__user hidden md:flex"
          @click="toggleUserMenu"
        >
          <div class="admin-topbar__user-text">
            <span class="admin-topbar__user-name">{{ displayName }}</span>
            <span class="admin-topbar__user-role">{{ roleLabel }}</span>
          </div>
        </button>
        <button
          type="button"
          class="admin-topbar__avatar"
          :class="{ 'admin-topbar__avatar--open': showUserMenu }"
          :title="displayName"
          :aria-expanded="showUserMenu"
          aria-haspopup="menu"
          aria-label="Tài khoản quản trị"
          @click="toggleUserMenu"
        >
          {{ avatarLetter }}
        </button>
        <div v-if="showUserMenu" class="admin-user-dropdown" role="menu" @click.stop>
          <div class="admin-user-dropdown__meta">
            <span class="admin-user-dropdown__name">{{ displayName }}</span>
            <span class="admin-user-dropdown__role">{{ roleLabel }}</span>
          </div>
          <button type="button" class="admin-user-dropdown__item" role="menuitem" @click="openChangePassword">
            <Icon icon="icon-park-outline:lock" />
            Đổi mật khẩu
          </button>
          <button type="button" class="admin-user-dropdown__logout" role="menuitem" @click="handleLogout">
            <Icon icon="icon-park-outline:logout" />
            Đăng xuất
          </button>
        </div>
      </div>
    </div>
  </header>

  <div
    v-if="showChangePw"
    class="admin-pw-modal"
    role="dialog"
    aria-modal="true"
    aria-labelledby="admin-pw-title"
    @click.self="closeChangePassword"
  >
    <div class="admin-pw-modal__panel">
      <div class="admin-pw-modal__head">
        <div>
          <h3 id="admin-pw-title">Đổi mật khẩu</h3>
          <p>Nhập mật khẩu hiện tại và mật khẩu mới (≥ 6 ký tự)</p>
        </div>
        <button
          type="button"
          class="admin-pw-modal__close"
          :disabled="changingPw"
          aria-label="Đóng"
          @click="closeChangePassword"
        >
          <Icon icon="icon-park-outline:close" width="16" />
        </button>
      </div>

      <label class="admin-pw-field">
        <span>Mật khẩu hiện tại</span>
        <div class="admin-pw-wrap">
          <input
            v-model="pwForm.matKhauCu"
            class="admin-pw-input"
            :class="{ 'admin-pw-input--error': pwErrors.matKhauCu }"
            :type="showPwCu ? 'text' : 'password'"
            autocomplete="current-password"
            :disabled="changingPw"
            @blur="validatePwField('matKhauCu')"
            @input="pwErrors.matKhauCu = ''"
          />
          <button type="button" class="admin-pw-toggle" tabindex="-1" @click="showPwCu = !showPwCu">
            <Icon :icon="showPwCu ? 'icon-park-outline:preview-close-one' : 'icon-park-outline:preview-open'" width="16" />
          </button>
        </div>
        <em v-if="pwErrors.matKhauCu" class="admin-pw-error">{{ pwErrors.matKhauCu }}</em>
      </label>

      <label class="admin-pw-field">
        <span>Mật khẩu mới</span>
        <div class="admin-pw-wrap">
          <input
            v-model="pwForm.matKhauMoi"
            class="admin-pw-input"
            :class="{ 'admin-pw-input--error': pwErrors.matKhauMoi }"
            :type="showPwMoi ? 'text' : 'password'"
            autocomplete="new-password"
            :disabled="changingPw"
            @blur="validatePwField('matKhauMoi')"
            @input="pwErrors.matKhauMoi = ''"
          />
          <button type="button" class="admin-pw-toggle" tabindex="-1" @click="showPwMoi = !showPwMoi">
            <Icon :icon="showPwMoi ? 'icon-park-outline:preview-close-one' : 'icon-park-outline:preview-open'" width="16" />
          </button>
        </div>
        <em v-if="pwErrors.matKhauMoi" class="admin-pw-error">{{ pwErrors.matKhauMoi }}</em>
      </label>

      <label class="admin-pw-field">
        <span>Nhập lại mật khẩu mới</span>
        <div class="admin-pw-wrap">
          <input
            v-model="pwForm.matKhauLai"
            class="admin-pw-input"
            :class="{ 'admin-pw-input--error': pwErrors.matKhauLai }"
            :type="showPwLai ? 'text' : 'password'"
            autocomplete="new-password"
            :disabled="changingPw"
            @blur="validatePwField('matKhauLai')"
            @input="pwErrors.matKhauLai = ''"
            @keydown.enter.prevent="submitChangePassword"
          />
          <button type="button" class="admin-pw-toggle" tabindex="-1" @click="showPwLai = !showPwLai">
            <Icon :icon="showPwLai ? 'icon-park-outline:preview-close-one' : 'icon-park-outline:preview-open'" width="16" />
          </button>
        </div>
        <em v-if="pwErrors.matKhauLai" class="admin-pw-error">{{ pwErrors.matKhauLai }}</em>
      </label>

      <div class="admin-pw-modal__actions">
        <button type="button" class="soleil-btn-outline" :disabled="changingPw" @click="closeChangePassword">
          Hủy
        </button>
        <button type="button" class="soleil-btn-primary" :disabled="changingPw" @click="submitChangePassword">
          {{ changingPw ? 'Đang lưu…' : 'Đổi mật khẩu' }}
        </button>
      </div>
    </div>
  </div>
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

.admin-notif__header-badge--support {
  color: #1d4ed8;
  background: rgba(37, 99, 235, 0.12);
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

.admin-notif__item-icon[data-loai='TIN_HO_TRO_MOI'] {
  background: #eff6ff;
  color: #1d4ed8;
}

.admin-notif__item-icon[data-loai='YEU_CAU_HOAN_TIEN'],
.admin-notif__item-icon[data-loai='HOAN_TIEN_HOAN_TAT'] {
  background: #ecfdf5;
  color: #059669;
}

.admin-notif__item-icon[data-loai='DON_HANG_MOI'],
.admin-notif__item-icon[data-loai='DON_HANG'],
.admin-notif__item-icon[data-loai='THANH_TOAN_THANH_CONG'] {
  background: #eff6ff;
  color: #2563eb;
}

.admin-notif__item-icon[data-loai='KHUYEN_MAI'] {
  background: #fef3c7;
  color: #d97706;
}

.admin-notif__item-icon[data-loai='UV'] {
  background: #fff7ed;
  color: #ea580c;
}

.admin-notif__item-icon[data-loai='HE_THONG'] {
  background: #f3f4f6;
  color: #4b5563;
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

.admin-user-menu {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
}

.admin-user-menu :deep(.admin-topbar__user) {
  border: 0;
  border-right: 0;
  margin-right: 0;
  padding-right: 0;
  background: none;
  cursor: pointer;
  font-family: inherit;
}

.admin-user-menu :deep(.admin-topbar__avatar) {
  border: 0;
  padding: 0;
  cursor: pointer;
  font-family: inherit;
  transition: box-shadow 0.15s ease;
}

.admin-user-menu :deep(.admin-topbar__avatar:hover),
.admin-user-menu :deep(.admin-topbar__avatar--open) {
  box-shadow: 0 0 0 2px rgba(166, 124, 61, 0.28);
}

.admin-user-dropdown {
  position: absolute;
  top: calc(100% + 10px);
  right: 0;
  min-width: 220px;
  background: #fff;
  border: 1px solid #eef0f3;
  border-radius: 14px;
  box-shadow: 0 18px 50px rgba(15, 23, 42, 0.22);
  padding: 8px;
  z-index: 1200;
}

.admin-user-dropdown__meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 10px 12px 12px;
  margin-bottom: 4px;
  border-bottom: 1px solid #f1f2f4;
}

.admin-user-dropdown__name {
  font-size: 14px;
  font-weight: 700;
  color: #1f2430;
  word-break: break-word;
}

.admin-user-dropdown__role {
  font-size: 12px;
  font-weight: 600;
  color: var(--bronze, #a67c3d);
}

.admin-user-dropdown__item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  border: none;
  background: none;
  border-radius: 8px;
  padding: 10px 12px;
  font-size: 13px;
  font-weight: 600;
  font-family: inherit;
  color: var(--ink, #1e1510);
  cursor: pointer;
  text-align: left;
}

.admin-user-dropdown__item:hover {
  background: rgba(201, 169, 110, 0.1);
}

.admin-user-dropdown__logout {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  border: none;
  background: none;
  border-radius: 8px;
  padding: 10px 12px;
  font-size: 13px;
  font-weight: 600;
  font-family: inherit;
  color: #9f1239;
  cursor: pointer;
  text-align: left;
}

.admin-user-dropdown__logout:hover {
  background: #fff1f2;
}

.admin-pw-modal {
  position: fixed;
  inset: 0;
  z-index: var(--admin-z-modal, 5000);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  background: rgba(15, 26, 28, 0.45);
}

.admin-pw-modal__panel {
  width: min(420px, 100%);
  background: #fff;
  border-radius: 14px;
  border: 1px solid var(--sand, #ede5d8);
  padding: 1.15rem;
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
  box-shadow: 0 16px 40px rgba(15, 26, 28, 0.18);
}

.admin-pw-modal__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 0.75rem;
}

.admin-pw-modal__head h3 {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--ink, #1e1510);
}

.admin-pw-modal__head p {
  margin: 0.25rem 0 0;
  font-size: 0.8125rem;
  color: rgba(30, 21, 16, 0.55);
}

.admin-pw-modal__close {
  border: 1px solid var(--sand, #ede5d8);
  background: #fff;
  border-radius: 8px;
  width: 36px;
  height: 36px;
  display: grid;
  place-items: center;
  cursor: pointer;
  color: rgba(30, 21, 16, 0.7);
}

.admin-pw-modal__close:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.admin-pw-field {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: #4a3f34;
}

.admin-pw-wrap {
  position: relative;
}

.admin-pw-input {
  width: 100%;
  border: 1px solid #c9b8a4;
  border-radius: 8px;
  padding: 0.6rem 2.5rem 0.6rem 0.75rem;
  font-size: 0.875rem;
  font-weight: 600;
  font-family: inherit;
  text-transform: none;
  letter-spacing: 0;
  color: var(--ink, #1e1510);
  background: #fff;
  outline: none;
  box-sizing: border-box;
}

.admin-pw-input:focus {
  border-color: #8f7349;
  box-shadow: 0 0 0 2px rgba(143, 115, 73, 0.18);
}

.admin-pw-input--error {
  border-color: #c45c3e;
  background: #fff8f5;
}

.admin-pw-toggle {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  border: none;
  background: transparent;
  color: rgba(30, 21, 16, 0.45);
  cursor: pointer;
  padding: 4px;
  display: grid;
  place-items: center;
}

.admin-pw-error {
  font-style: normal;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0;
  text-transform: none;
  color: #a33b1c;
}

.admin-pw-modal__actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 0.25rem;
}
</style>
