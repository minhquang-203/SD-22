<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'
import AdminHeader from '@/components/admin/AdminHeader.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import { isAdminLoggedIn, useAdminAuth } from '@/composables/useAdminAuth'

const collapsed = ref(false)
const route = useRoute()

const breadcrumb = computed(() => route.meta.breadcrumb || 'SUNOVA Admin')
const title = computed(() => route.meta.title || 'SUNOVA Admin')

onMounted(async () => {
  if (import.meta.env.DEV && !isAdminLoggedIn()) {
    try {
      await useAdminAuth().dangNhap('an.nv@sunova.vn', '123456')
    } catch {
      // Dev: admin login page chưa có — bỏ qua nếu auto-login thất bại
    }
  }
})
</script>

<template>
  <div class="h-full flex overflow-hidden bg-[var(--admin-bg)]">
    <AdminSidebar :collapsed="collapsed" />
    <div class="flex-1 flex flex-col min-w-0">
      <AdminHeader
        :title="title"
        :breadcrumb="breadcrumb"
        @toggle-sidebar="collapsed = !collapsed"
      />
      <main class="admin-main-content flex-1 overflow-auto">
        <router-view v-slot="{ Component }">
          <transition name="admin-page-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
    <ConfirmDialog />
  </div>
</template>
