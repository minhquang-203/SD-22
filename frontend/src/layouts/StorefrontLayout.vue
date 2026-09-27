<script setup>
import { useRoute } from 'vue-router'
import 'bootstrap/dist/css/bootstrap.min.css'
import '@/styles/soleil-storefront.css'
import TheNavbar from '@/components/storefront/TheNavbar.vue'
import TheFooter from '@/components/storefront/TheFooter.vue'
import AuthModal from '@/components/storefront/AuthModal.vue'
import WelcomeModal from '@/components/storefront/WelcomeModal.vue'
import CartToast from '@/components/storefront/CartToast.vue'
import BulkOrderModal from '@/components/storefront/BulkOrderModal.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import ChatWidget from '@/components/storefront/ChatWidget.vue'
import ErrorBoundary from '@/components/ui/ErrorBoundary.vue'

const route = useRoute()
</script>

<template>
  <div class="storefront-root storefront-shell">
    <TheNavbar />
    <main class="sf-main">
      <ErrorBoundary variant="storefront">
        <!-- Không dùng Transition: mode out-in dễ kẹt trang cũ khi soft-nav. -->
        <router-view v-slot="{ Component }">
          <div :key="route.fullPath" class="sf-page">
            <component :is="Component" />
          </div>
        </router-view>
      </ErrorBoundary>
    </main>
    <TheFooter />
    <AuthModal />
    <WelcomeModal />
    <CartToast />
    <BulkOrderModal />
    <ConfirmDialog />
    <ChatWidget />
  </div>
</template>
