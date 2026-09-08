<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import { useAuth } from '@/composables/useAuth'
import { useAuthModal } from '@/composables/useAuthModal'
import { useCart } from '@/composables/useCart'
import { useCustomerNotifications } from '@/composables/useCustomerNotifications'
import { confirm } from '@/composables/useConfirm'

const router = useRouter()
const route = useRoute()
const { isLoggedIn, hoTen, dangXuat } = useAuth()

const lastName = computed(() => {
  const parts = String(hoTen.value || '').trim().split(/\s+/).filter(Boolean)
  return parts[parts.length - 1] || 'Bạn'
})

const avatarLetter = computed(() => lastName.value.charAt(0).toUpperCase())
const { openAuthModal } = useAuthModal()
const { count } = useCart()
const {
  notifications,
  hasBadge: hasNotifBadge,
  badgeText: notifBadgeText,
  startPolling: startNotifPolling,
  stopPolling: stopNotifPolling,
  loadNotifications,
  markRead: markNotifRead,
  markAllRead: markAllNotifRead,
} = useCustomerNotifications()

const searchQuery = ref('')
const searchOpen = ref(false)
const searchInput = ref(null)
const navEl = ref(null)
const searchBarPx = ref(0)
const megaOpen = ref(false)
const userOpen = ref(false)
const notifOpen = ref(false)
let megaTimer

const navLinks = [
  { to: '/', label: 'Trang chủ', exact: true },
  { to: '/san-pham', label: 'Sản phẩm' },
  { to: '/quiz', label: 'Quiz da' },
]

const extraLinks = [
  { to: '/san-pham/khuyen-mai', label: 'Khuyến mãi' },
  { to: '/blog', label: 'Blog' },
  { to: '/san-pham/goi-y', label: 'Sản phẩm gợi ý' },
]

onMounted(() => {
  document.addEventListener('click', onDocClick)
  startNotifPolling()
})

onUnmounted(() => {
  document.removeEventListener('click', onDocClick)
  stopNotifPolling()
  clearTimeout(megaTimer)
})

function openMega() {
  clearTimeout(megaTimer)
  megaOpen.value = true
}

function closeMega() {
  clearTimeout(megaTimer)
  megaTimer = setTimeout(() => {
    megaOpen.value = false
  }, 180)
}

function toggleMega(e) {
  e.stopPropagation()
  clearTimeout(megaTimer)
  megaOpen.value = !megaOpen.value
}

function onDocClick() {
  userOpen.value = false
  notifOpen.value = false
  searchOpen.value = false
  clearTimeout(megaTimer)
  megaOpen.value = false
}

async function toggleNotif(e) {
  e.stopPropagation()
  notifOpen.value = !notifOpen.value
  userOpen.value = false
  searchOpen.value = false
  megaOpen.value = false
  if (notifOpen.value) {
    await loadNotifications()
  }
}

async function goToNotif(item) {
  notifOpen.value = false
  await markNotifRead(item)
  router.push(item?.link || '/tra-cuu-don')
}

async function markAllNotifications() {
  await markAllNotifRead()
}

function iconForNotifLoai(loai) {
  const map = {
    DON_HANG_CAP_NHAT: 'solar:box-linear',
    DON_HANG: 'solar:box-linear',
    TRA_HANG_DUOC_DUYET: 'solar:undo-left-round-linear',
    TRA_HANG_BI_TU_CHOI: 'solar:close-circle-linear',
    HOAN_TIEN_THANH_CONG: 'solar:wallet-money-linear',
    HOAN_TIEN_BI_TU_CHOI: 'solar:close-circle-linear',
    KHUYEN_MAI: 'solar:ticket-sale-linear',
    UV: 'solar:sun-linear',
    HE_THONG: 'solar:bell-linear',
  }
  return map[loai] || 'solar:bell-linear'
}

function formatNotifTime(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  const diffMin = Math.floor((Date.now() - date.getTime()) / 60000)
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


function submitSearch() {
  const q = searchQuery.value.trim()
  searchOpen.value = false
  if (!q) {
    router.push('/san-pham')
    return
  }
  router.push({ path: '/san-pham', query: { q } })
}

function openLogin() {
  openAuthModal('login')
}

function isLinkActive(link) {
  const path = route.path
  if (link.exact) return path === '/'
  if (link.to === '/san-pham') {
    return path === '/san-pham' || (path.startsWith('/san-pham/') && !extraLinks.some((x) => path.startsWith(x.to)))
  }
  return path === link.to || path.startsWith(`${link.to}/`)
}

function isExtraActive() {
  return extraLinks.some((link) => isLinkActive(link))
}

async function toggleSearch(e) {
  e.stopPropagation()
  if (!searchOpen.value) {
    const w = navEl.value?.getBoundingClientRect().width || 0
    const max = Math.max(280, window.innerWidth - 48)
    searchBarPx.value = Math.min(Math.round(w * 1.5), max)
  }
  searchOpen.value = !searchOpen.value
  userOpen.value = false
  notifOpen.value = false
  megaOpen.value = false
  if (searchOpen.value) {
    await nextTick()
    searchInput.value?.focus()
  }
}

async function handleLogout() {
  const ok = await confirm({
    title: 'Đăng xuất',
    message: 'Bạn có chắc muốn đăng xuất?',
    confirmText: 'Đăng xuất',
    danger: true,
  })
  if (!ok) return
  dangXuat()
  userOpen.value = false
  router.push('/')
}

function toggleUser(e) {
  e.stopPropagation()
  userOpen.value = !userOpen.value
}
</script>

<template>
  <header class="sf-header" :class="{ 'is-searching': searchOpen }">
    <div class="sf-header__inner">
      <RouterLink to="/" class="sf-navbar__brand">
        <img src="@/assets/logo/sunova_mark.png" alt="SUNOVA Logo" class="sf-navbar__logo-img" />
        <span class="sf-navbar__brand-text">SUN<span>OVA</span></span>
      </RouterLink>

      <div class="sf-header__center">
      <form
        v-show="searchOpen"
        class="sf-header__search-bar"
        :style="searchBarPx ? { width: `${searchBarPx}px` } : undefined"
        @click.stop
        @submit.prevent="submitSearch"
      >
        <input
          ref="searchInput"
          v-model="searchQuery"
          type="search"
          placeholder="Tìm kem chống nắng, thương hiệu..."
          aria-label="Tìm kiếm sản phẩm"
        />
        <button type="submit" class="sf-header__search-go">Tìm</button>
        <button type="button" class="sf-header__search-close" aria-label="Đóng tìm kiếm" @click="searchOpen = false">
          <Icon icon="solar:close-linear" width="18" />
        </button>
      </form>

      <nav v-show="!searchOpen" ref="navEl" class="sf-header__links" aria-label="Chính">
        <RouterLink
          v-for="link in navLinks"
          :key="link.label"
          :to="link.to"
          class="sf-header__link"
          active-class=""
          exact-active-class=""
          :class="{ active: isLinkActive(link) }"
        >
          {{ link.label }}
        </RouterLink>

        <div
          class="sf-nav-drop"
          @mouseenter="openMega"
          @mouseleave="closeMega"
        >
          <button
            type="button"
            class="sf-header__link sf-nav-drop__btn"
            :class="{ active: isExtraActive() || megaOpen }"
            @click="toggleMega"
          >
            Danh mục
            <span class="sf-nav-drop__chev" aria-hidden="true">▾</span>
          </button>
          <div v-show="megaOpen" class="sf-nav-drop__panel" @click.stop>
            <RouterLink
              v-for="link in extraLinks"
              :key="link.to"
              :to="link.to"
              class="sf-nav-drop__item"
              :class="{ active: isLinkActive(link) }"
              @click="megaOpen = false"
            >
              {{ link.label }}
            </RouterLink>
          </div>
        </div>
      </nav>
      </div>

      <div class="sf-header__actions">
        <button
          type="button"
          class="sf-nav-icon"
          :class="{ 'is-on': searchOpen }"
          title="Tìm kiếm"
          aria-label="Tìm kiếm"
          @click.stop="toggleSearch"
        >
          <Icon icon="solar:magnifer-linear" width="20" />
        </button>

        <div v-if="isLoggedIn" class="sf-bell" @click.stop>
          <button
            type="button"
            class="sf-nav-icon sf-bell__btn"
            :class="{ 'sf-bell__btn--active': notifOpen }"
            title="Thông báo"
            aria-label="Thông báo"
            @click="toggleNotif"
          >
            <Icon icon="solar:bell-linear" width="22" />
            <span v-if="hasNotifBadge" class="sf-cart-badge sf-bell__badge">{{ notifBadgeText }}</span>
          </button>

          <div v-if="notifOpen" class="sf-bell__panel">
            <div class="sf-bell__header">
              <span>Thông báo</span>
              <button
                v-if="notifications.length"
                type="button"
                class="sf-bell__mark-all"
                @click="markAllNotifications"
              >
                Đọc tất cả
              </button>
            </div>

            <ul v-if="notifications.length" class="sf-bell__list">
              <li
                v-for="item in notifications"
                :key="item.id"
                class="sf-bell__item"
                :class="{ 'sf-bell__item--unread': !item.daDoc }"
                @click="goToNotif(item)"
              >
                <div class="sf-bell__item-icon" :data-loai="item.loai">
                  <Icon :icon="iconForNotifLoai(item.loai)" width="18" />
                </div>
                <div class="sf-bell__item-body">
                  <div class="sf-bell__item-title">{{ item.tieuDe || 'Thông báo' }}</div>
                  <div class="sf-bell__item-desc">{{ item.noiDung || '—' }}</div>
                  <div class="sf-bell__item-time">{{ formatNotifTime(item.ngayTao) }}</div>
                </div>
                <span v-if="!item.daDoc" class="sf-bell__item-dot" />
              </li>
            </ul>

            <div v-else class="sf-bell__empty">Chưa có thông báo nào.</div>
          </div>
        </div>

        <RouterLink to="/gio-hang" class="sf-nav-icon sf-nav-icon--cart" title="Giỏ hàng" aria-label="Giỏ hàng">
          <Icon icon="solar:cart-large-2-linear" width="20" />
          <span v-if="count > 0" class="sf-cart-badge">{{ count > 99 ? '99+' : count }}</span>
        </RouterLink>

        <button
          v-if="!isLoggedIn"
          type="button"
          class="sf-header__cta"
          @click="openLogin"
        >
          Đăng nhập
        </button>
        <div v-else class="sf-user-menu">
          <button type="button" class="sf-header__user" :title="hoTen" @click="toggleUser">
            <span class="sf-header__avatar" aria-hidden="true">{{ avatarLetter }}</span>
          </button>
          <div v-if="userOpen" class="sf-user-dropdown" @click.stop>
            <div class="sf-user-dropdown__name">{{ hoTen }}</div>
            <RouterLink to="/tai-khoan" @click="userOpen = false">Tài khoản</RouterLink>
            <RouterLink to="/tra-cuu-don" @click="userOpen = false">Tra cứu đơn</RouterLink>
            <button type="button" @click="handleLogout">Đăng xuất</button>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<style scoped>
.sf-bell {
  position: relative;
  display: flex;
  align-items: center;
}

.sf-bell__btn {
  position: relative;
}

.sf-bell__btn--active {
  color: var(--espresso, #4a2f1b);
}

.sf-bell__badge {
  top: -4px;
  right: -4px;
}

.sf-bell__panel {
  position: absolute;
  top: calc(100% + 12px);
  right: 0;
  width: 360px;
  max-width: 90vw;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 18px 50px rgba(31, 21, 12, 0.22);
  border: 1px solid #efe7dc;
  overflow: hidden;
  z-index: 1300;
}

.sf-bell__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 13px 16px;
  font-weight: 700;
  color: #2a1d12;
  border-bottom: 1px solid #f3ede4;
}

.sf-bell__mark-all {
  border: none;
  background: transparent;
  color: #a6763d;
  font-size: 12px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  padding: 0;
}

.sf-bell__mark-all:hover {
  text-decoration: underline;
}

.sf-bell__list {
  list-style: none;
  margin: 0;
  padding: 0;
  max-height: 420px;
  overflow-y: auto;
}

.sf-bell__item {
  position: relative;
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid #f6f1ea;
  transition: background 0.12s ease;
}

.sf-bell__item:hover {
  background: #faf6f0;
}

.sf-bell__item--unread {
  background: #fdf6ec;
}

.sf-bell__item--unread:hover {
  background: #f9edda;
}

.sf-bell__item-icon {
  flex-shrink: 0;
  width: 34px;
  height: 34px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #eef4ff;
  color: #2563eb;
}

.sf-bell__item-icon[data-loai='TRA_HANG_DUOC_DUYET'] {
  background: #ecfdf5;
  color: #059669;
}

.sf-bell__item-icon[data-loai='HOAN_TIEN_THANH_CONG'] {
  background: #ecfdf5;
  color: #047857;
}

.sf-bell__item-icon[data-loai='TRA_HANG_BI_TU_CHOI'],
.sf-bell__item-icon[data-loai='HOAN_TIEN_BI_TU_CHOI'] {
  background: #fff1f2;
  color: #e11d48;
}

.sf-bell__item-icon[data-loai='DON_HANG'] {
  background: #eef4ff;
  color: #2563eb;
}

.sf-bell__item-icon[data-loai='KHUYEN_MAI'] {
  background: #fef3c7;
  color: #d97706;
}

.sf-bell__item-icon[data-loai='UV'] {
  background: #fff7ed;
  color: #ea580c;
}

.sf-bell__item-icon[data-loai='HE_THONG'] {
  background: #f3f4f6;
  color: #4b5563;
}

.sf-bell__item-body {
  min-width: 0;
  flex: 1;
}

.sf-bell__item-title {
  font-weight: 600;
  color: #2a1d12;
  font-size: 14px;
}

.sf-bell__item-desc {
  color: #6b5b4c;
  font-size: 13px;
  margin-top: 2px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.sf-bell__item-time {
  color: #a99a89;
  font-size: 12px;
  margin-top: 4px;
}

.sf-bell__item-dot {
  flex-shrink: 0;
  align-self: center;
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: #e11d48;
}

.sf-bell__empty {
  padding: 28px 16px;
  text-align: center;
  color: #a99a89;
  font-size: 14px;
}
</style>
