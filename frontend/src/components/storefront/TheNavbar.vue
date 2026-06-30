<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import { useAuth } from '@/composables/useAuth'
import { useAuthModal } from '@/composables/useAuthModal'
import { useCart } from '@/composables/useCart'
import { fetchDanhMucList } from '@/api/storefrontApi'

const router = useRouter()
const route = useRoute()
const { isLoggedIn, hoTen, dangXuat } = useAuth()
const { openAuthModal } = useAuthModal()
const { count } = useCart()

const searchQuery = ref('')
const megaOpen = ref(false)
const userOpen = ref(false)
const categories = ref([])

const navLinks = [
  { to: '/', label: 'Trang chủ', exact: true },
  { to: '/san-pham', label: 'Kem chống nắng' },
  { to: '/san-pham?noiBat=1', label: 'Khuyến mãi' },
  { to: '/san-pham', label: 'Thương hiệu', hash: 'brands' },
  { to: '/quiz', label: 'Quiz da' },
  { to: '#', label: 'Blog', disabled: true },
  { to: '/tra-cuu-don', label: 'Tra cứu đơn', requiresAuth: true },
]

onMounted(async () => {
  try {
    const res = await fetchDanhMucList()
    categories.value = (res.data || []).filter((d) => d.trangThai !== false)
  } catch {
    categories.value = []
  }
  document.addEventListener('click', onDocClick)
})

onUnmounted(() => {
  document.removeEventListener('click', onDocClick)
})

function onDocClick() {
  userOpen.value = false
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

function handleTraCuu() {
  if (!isLoggedIn.value) {
    openAuthModal('login', '/tra-cuu-don')
    return
  }
  router.push('/tra-cuu-don')
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
  if (link.hash === 'brands') {
    e.preventDefault()
    if (route.path === '/') {
      router.replace({ path: '/', hash: '#sf-brands' })
      document.getElementById('sf-brands')?.scrollIntoView({ behavior: 'smooth' })
    } else {
      router.push({ path: '/', hash: '#sf-brands' })
    }
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
    return path === '/san-pham' && String(q.noiBat) === '1'
  }
  if (link.label === 'Thương hiệu') {
    if (path === '/') return route.hash === '#sf-brands'
    return path === '/san-pham' && Boolean(q.thuongHieu)
  }
  if (link.label === 'Quiz da') {
    return path === '/quiz'
  }
  if (link.to === '/tra-cuu-don') {
    return path === '/tra-cuu-don'
  }
  if (link.to === '/don-hang') {
    return path === '/don-hang'
  }

  return path === link.to
}

function handleLogout() {
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
              <RouterLink to="/don-hang" @click="userOpen = false">Đơn hàng</RouterLink>
              <button type="button" @click="handleLogout">Đăng xuất</button>
            </div>
          </div>

          <button type="button" class="sf-nav-icon" title="Tra cứu đơn hàng" aria-label="Tra cứu đơn" @click="handleTraCuu">
            <Icon icon="solar:clipboard-list-linear" width="22" />
          </button>

          <RouterLink to="/gio-hang" class="sf-nav-icon sf-nav-icon--cart" title="Giỏ hàng" aria-label="Giỏ hàng">
            <Icon icon="solar:bag-3-linear" width="22" />
            <span v-if="count > 0" class="sf-cart-badge">{{ count > 99 ? '99+' : count }}</span>
          </RouterLink>
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
