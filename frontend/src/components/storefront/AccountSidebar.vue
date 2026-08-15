<script setup>
import { computed } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import { useAuth } from '@/composables/useAuth'
import { confirm } from '@/composables/useConfirm'

const props = defineProps({
  activeSection: { type: String, default: '' },
})

const route = useRoute()
const router = useRouter()
const { dangXuat } = useAuth()

const menuItems = [
  { id: 'info', label: 'Thông tin tài khoản', icon: 'solar:user-circle-linear', to: '/tai-khoan' },
  {
    id: 'addresses',
    label: 'Địa chỉ',
    icon: 'solar:map-point-linear',
    to: { path: '/tai-khoan', query: { section: 'addresses' } },
  },
  { id: 'orders', label: 'Đơn hàng của tôi', icon: 'solar:bag-check-linear', to: '/tra-cuu-don' },
  {
    id: 'quiz',
    label: 'Hồ sơ da (Quiz)',
    icon: 'solar:clipboard-list-linear',
    to: { path: '/tai-khoan', query: { section: 'quiz' } },
  },
  {
    id: 'password',
    label: 'Đổi mật khẩu',
    icon: 'solar:lock-keyhole-linear',
    to: { path: '/tai-khoan', query: { section: 'password' } },
  },
]

const currentId = computed(() => {
  if (route.path.startsWith('/tra-cuu-don')) return 'orders'
  if (props.activeSection) return props.activeSection
  const section = route.query.section
  if (section === 'addresses' || section === 'quiz' || section === 'password') return section
  return 'info'
})

function isActive(item) {
  return currentId.value === item.id
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
  router.push('/')
}
</script>

<template>
  <aside class="sf-account-sidebar">
    <RouterLink
      v-for="item in menuItems"
      :key="item.id"
      :to="item.to"
      class="sf-account-sidebar__item"
      :class="{ active: isActive(item) }"
      active-class=""
      exact-active-class=""
    >
      <Icon v-if="item.icon" :icon="item.icon" width="18" />
      {{ item.label }}
    </RouterLink>
    <button type="button" class="sf-account-sidebar__item sf-account-sidebar__item--logout" @click="handleLogout">
      <Icon icon="solar:logout-2-linear" width="18" />
      Đăng xuất
    </button>
  </aside>
</template>
