<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
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
const menuOptions = computed(() => {
  const role = getCurrentRole()
  const filtered = filterMenuByRole(ADMIN_MENU, role)
  return buildAdminMenuOptions(filtered, props.collapsed)
})
const activeKey = computed(() => route.path)
const expandedKeys = ref([])

// Chỉ mở nhánh chứa trang hiện tại (giống useLayoutMenu + accordion trong FE mẫu)
watch(
  () => [route.path, props.collapsed, getCurrentRole()],
  ([path]) => {
    const role = getCurrentRole()
    const filtered = filterMenuByRole(ADMIN_MENU, role)
    expandedKeys.value = findExpandedKeys(path, filtered)
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
      <div class="admin-sidebar-naive__brand">
        <svg class="admin-sidebar-naive__logo" viewBox="0 0 32 32" fill="none">
          <path d="M16 3L6 8.5V20.5L16 26l10-5.5V8.5L16 3z" fill="#FF8A3D" />
          <path d="M16 8l5.5 3v6L16 20l-5.5-3v-6L16 8z" fill="#fff" fill-opacity="0.35" />
        </svg>
        <span v-if="!collapsed" class="admin-sidebar-naive__title">SUNOVA</span>
      </div>

      <div class="admin-sidebar-naive__menu">
        <NMenu
          v-model:expanded-keys="expandedKeys"
          :value="activeKey"
          :collapsed="collapsed"
          :collapsed-width="64"
          :collapsed-icon-size="20"
          :icon-size="18"
          :indent="26"
          :root-indent="16"
          :options="menuOptions"
          accordion
        />
      </div>
    </aside>
  </div>
</template>
