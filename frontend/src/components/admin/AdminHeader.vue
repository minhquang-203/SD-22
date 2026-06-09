<script setup>
import { Icon } from '@iconify/vue'

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
      <div class="admin-topbar__eyebrow">
        SUNOVA ADMIN — {{ pageLabel() }}
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
