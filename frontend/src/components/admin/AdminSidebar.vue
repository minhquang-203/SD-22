<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NMenu } from 'naive-ui'
import {
  ADMIN_MENU,
  findExpandedKeys,
  filterMenuByRole,
} from '@/constants/adminMenu'
import { buildAdminMenuOptions } from '@/utils/adminMenuOptions'
import { useAdminAuth } from '@/composables/useAdminAuth'
import { useAdminBadges } from '@/composables/useAdminBadges'
import AppLogo from '@/components/common/AppLogo.vue'

const props = defineProps({
  collapsed: { type: Boolean, default: false },
})

const route = useRoute()
const router = useRouter()
const { vaiTro } = useAdminAuth()
const { startPolling, stopPolling, refreshBadges } = useAdminBadges()

const menuOptions = computed(() => {
  const filtered = filterMenuByRole(ADMIN_MENU, vaiTro.value)
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

watch(
  () => [route.path, props.collapsed, vaiTro.value],
  ([path]) => {
    const filtered = filterMenuByRole(ADMIN_MENU, vaiTro.value)
    const next = findExpandedKeys(path, filtered)
    if (!expandedKeysEqual(expandedKeys.value, next)) {
      expandedKeys.value = next
    }
  },
  { immediate: true },
)

watch(
  () => route.path,
  (path, prev) => {
    if (path?.startsWith('/admin') && path !== prev) {
      refreshBadges()
    }
  },
)

onMounted(() => {
  startPolling()
})

onBeforeUnmount(() => {
  stopPolling()
})
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
        <AppLogo variant="dark" :size="40" :collapsed="collapsed" class="max-w-full" />
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
