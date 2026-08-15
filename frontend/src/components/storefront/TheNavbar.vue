<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import { useAuth } from '@/composables/useAuth'
import { useAuthModal } from '@/composables/useAuthModal'
import { useCart } from '@/composables/useCart'
import { useCustomerNotifications } from '@/composables/useCustomerNotifications'
import { fetchDanhMucList } from '@/api/storefrontApi'
import { confirm } from '@/composables/useConfirm'

const router = useRouter()
const route = useRoute()
const { isLoggedIn, hoTen, dangXuat } = useAuth()
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
const megaOpen = ref(false)
const userOpen = ref(false)
const notifOpen = ref(false)
const categories = ref([])

const navLinks = [
  { to: '/', label: 'Trang chủ', exact: true },
  { to: '/san-pham', label: 'Kem chống nắng' },
  { to: '/san-pham/khuyen-mai', label: 'Khuyến mãi' },
  { to: '/quiz', label: 'Quiz da' },
  { to: '/blog', label: 'Blog' },
  { to: '/tra-cuu-don', label: 'Tra cứu đơn', requiresAuth: true },
  { to: '/san-pham/goi-y', label: 'Sản phẩm gợi ý' },
]

onMounted(async () => {
  try {
    const res = await fetchDanhMucList()
    categories.value = (res.data || []).filter((d) => d.trangThai !== false)
  } catch {
    categories.value = []
  }
  document.addEventListener('click', onDocClick)
  startNotifPolling()
})

onUnmounted(() => {
  document.removeEventListener('click', onDocClick)
  stopNotifPolling()
})

function onDocClick() {
  userOpen.value = false
  notifOpen.value = false
}

async function toggleNotif(e) {
  e.stopPropagation()
  notifOpen.value = !notifOpen.value
  userOpen.value = false
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
  if (!q) {
    router.push('/san-pham')
    return
  }
  router.push({ path: '/san-pham', query: { q } })
}

function openLogin() {
  openAuthModal('login')
}

function openRegister() {
  openAuthModal('register')
}

function handleNavClick(link, e) {
  if (link.disabled) {
    e.preventDefault()
    return
  }
  if (link.requiresAuth && !isLoggedIn.value) {
    e.preventDefault()
    openAuthModal('login', link.to)
    return
  }
  if (link.requiresAuth) {
    router.push(link.to)
    return
  }
}

function isLinkActive(link) {
  const path = route.path
  const q = route.query

  if (link.exact) return path === '/'

  if (link.label === 'Kem chống nắng') {
    return path === '/san-pham' && !q.noiBat && !q.thuongHieu && !q.danhMuc
  }
  if (link.label === 'Khuyến mãi') {
    return path === '/san-pham/khuyen-mai'
  }
  if (link.label === 'Quiz da') {
    return path === '/quiz'
  }
  if (link.to === '/tra-cuu-don') {
    return path === '/tra-cuu-don' || path.startsWith('/tra-cuu-don/')
  }

  return path === link.to
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
  <header class="sf-header">
    <!-- Tầng trên -->
    <div class="sf-header__top">
      <div class="sf-header__inner">
        <RouterLink to="/" class="sf-navbar__brand">SUN<span>OVA</span></RouterLink>

        <form class="sf-header__search" @submit.prevent="submitSearch">
          <input
            v-model="searchQuery"
            type="search"
            placeholder="Tìm kem chống nắng, thương hiệu..."
            aria-label="Tìm kiếm sản phẩm"
          />
          <button type="submit" aria-label="Tìm kiếm">
            <Icon icon="solar:magnifer-linear" width="20" />
          </button>
        </form>

        <div class="sf-header__actions">
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
            <Icon icon="solar:bag-3-linear" width="22" />
            <span v-if="count > 0" class="sf-cart-badge">{{ count > 99 ? '99+' : count }}</span>
          </RouterLink>

          <template v-if="!isLoggedIn">
            <button type="button" class="sf-header__auth-text" @click="openRegister">Đăng ký</button>
            <span class="sf-header__auth-sep">/</span>
            <button type="button" class="sf-header__auth-text" @click="openLogin">Đăng nhập</button>
            <button type="button" class="sf-nav-icon" title="Đăng nhập" aria-label="Đăng nhập" @click="openLogin">
              <Icon icon="solar:user-circle-linear" width="22" />
            </button>
          </template>
          <div v-else class="sf-user-menu">
            <button type="button" class="sf-user-trigger" @click="toggleUser">
              <Icon icon="solar:user-circle-linear" width="20" />
              <span>{{ hoTen }}</span>
            </button>
            <div v-if="userOpen" class="sf-user-dropdown" @click.stop>
              <RouterLink to="/tai-khoan" @click="userOpen = false">Tài khoản</RouterLink>
              <RouterLink to="/tra-cuu-don" @click="userOpen = false">Tra cứu đơn</RouterLink>
              <button type="button" @click="handleLogout">Đăng xuất</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Tầng dưới -->
    <div class="sf-header__nav">
      <div class="sf-header__inner">
        <div
          class="sf-mega-trigger"
          @mouseenter="megaOpen = true"
          @mouseleave="megaOpen = false"
        >
          <button type="button" class="sf-mega-trigger__btn">
            <Icon icon="solar:hamburger-menu-linear" width="18" />
            Danh mục sản phẩm
          </button>
          <div v-if="megaOpen && categories.length" class="sf-mega-panel">
            <RouterLink
              v-for="cat in categories"
              :key="cat.id"
              :to="`/san-pham?danhMuc=${cat.id}`"
              class="sf-mega-panel__item"
              @click="megaOpen = false"
            >
              {{ cat.ten }}
            </RouterLink>
          </div>
        </div>

        <nav class="sf-header__links">
          <template v-for="link in navLinks" :key="link.label">
            <RouterLink
              v-if="!link.disabled && !link.requiresAuth"
              :to="link.to"
              class="sf-header__link"
              active-class=""
              exact-active-class=""
              :class="{ active: isLinkActive(link) }"
              @click="handleNavClick(link, $event)"
            >
              {{ link.label }}
            </RouterLink>
            <button
              v-else-if="link.disabled"
              type="button"
              class="sf-header__link sf-header__link--muted"
              disabled
              title="Sắp ra mắt"
            >
              {{ link.label }}
            </button>
            <button
              v-else
              type="button"
              class="sf-header__link"
              :class="{ active: isLinkActive(link) }"
              @click="handleNavClick(link, $event)"
            >
              {{ link.label }}
            </button>
          </template>
        </nav>
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
