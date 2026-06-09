<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NMenu } from 'naive-ui'
import {
  ADMIN_MENU,
  findExpandedKeys,
  filterMenuByRole,
  getCurrentRole,
} from '@/constants/adminMenu'
import { buildAdminMenuOptions } from '@/utils/adminMenuOptions'

const props = defineProps({
  collapsed: { type: Boolean, default: false },
})

const route = useRoute()
const router = useRouter()
const menuOptions = computed(() => {
  const role = getCurrentRole()
  const filtered = filterMenuByRole(ADMIN_MENU, role)
  return buildAdminMenuOptions(filtered, props.collapsed)
})
const activeKey = computed(() => route.path)
const expandedKeys = ref([])

function expandedKeysEqual(a, b) {
  if (a.length !== b.length) return false
  return a.every((key, i) => key === b[i])
}

function onMenuSelect(key) {
  if (typeof key === 'string' && key.startsWith('/') && key !== route.path) {
    router.push(key)
  }
}

// Chỉ mở nhánh chứa trang hiện tại; không gán lại nếu không đổi (tránh chớp submenu)
watch(
  () => [route.path, props.collapsed, getCurrentRole()],
  ([path]) => {
    const role = getCurrentRole()
    const filtered = filterMenuByRole(ADMIN_MENU, role)
    const next = findExpandedKeys(path, filtered)
    if (!expandedKeysEqual(expandedKeys.value, next)) {
      expandedKeys.value = next
    }
  },
  { immediate: true },
)
</script>

<template>
  <div
    class="admin-sidebar-wrap"
    :class="{ 'admin-sidebar-wrap--collapsed': collapsed }"
  >
    <aside
      class="admin-sidebar-naive"
      :class="{ 'admin-sidebar-naive--collapsed': collapsed }"
    >
      <div
        class="admin-sidebar-naive__brand"
        :class="{ 'admin-sidebar-naive__brand--collapsed': collapsed }"
      >
        <svg
          v-if="collapsed"
          class="admin-sidebar-naive__logo"
          viewBox="0 0 32 32"
          fill="none"
          aria-hidden="true"
        >
          <path d="M16 3L6 8.5V20.5L16 26l10-5.5V8.5L16 3z" fill="#C9A96E" />
          <path d="M16 8l5.5 3v6L16 20l-5.5-3v-6L16 8z" fill="#fff" fill-opacity="0.35" />
        </svg>
        <div v-else class="admin-sidebar-naive__brand-text">
          <div class="admin-sidebar-naive__title">SUNOVA</div>
          <div class="admin-sidebar-naive__tagline">Chống nắng &amp; chăm sóc da</div>
        </div>
      </div>

      <div class="admin-sidebar-naive__menu">
        <NMenu
          v-model:expanded-keys="expandedKeys"
          :value="activeKey"
          @update:value="onMenuSelect"
          :collapsed="collapsed"
          :collapsed-width="64"
          :collapsed-icon-size="20"
          :icon-size="18"
          :indent="20"
          :root-indent="28"
          :options="menuOptions"
          accordion
        />
      </div>
    </aside>
  </div>
</template>
