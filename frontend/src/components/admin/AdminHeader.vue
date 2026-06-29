<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import AppLogo from '@/components/common/AppLogo.vue'
import { useAdminAuth } from '@/composables/useAdminAuth'
import { getRoleLabel } from '@/utils/adminAuth'

const props = defineProps({
  title: { type: String, default: 'SUNOVA Admin' },
  breadcrumb: { type: String, default: '' },
})

const emit = defineEmits(['toggle-sidebar'])

const router = useRouter()
const { hoTen, vaiTro, dangXuat } = useAdminAuth()

const pageLabel = () => (props.title || props.breadcrumb || 'Trang quản trị').toUpperCase()
const roleLabel = computed(() => getRoleLabel(vaiTro.value))
const displayName = computed(() => hoTen.value || 'Quản trị viên')
const avatarLetter = computed(() => {
  const name = displayName.value.trim()
  return name ? name.charAt(0).toUpperCase() : 'A'
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
