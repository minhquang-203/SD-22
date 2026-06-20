<script setup>
import { Icon } from '@iconify/vue'
import AppLogo from '@/components/common/AppLogo.vue'

const props = defineProps({
  title: { type: String, default: 'SUNOVA Admin' },
  breadcrumb: { type: String, default: '' },
})

const emit = defineEmits(['toggle-sidebar'])

const pageLabel = () => (props.title || props.breadcrumb || 'Trang quản trị').toUpperCase()
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
      <button type="button" class="admin-topbar__btn-icon" aria-label="Thông báo">
        <Icon icon="icon-park-outline:remind" />
      </button>
      <button type="button" class="admin-topbar__btn-icon" aria-label="Trợ giúp">
        <Icon icon="icon-park-outline:help" />
      </button>
      <div class="admin-topbar__avatar" title="Quản trị viên">A</div>
    </div>
  </header>
</template>
