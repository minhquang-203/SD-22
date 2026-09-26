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
        <router-view v-slot="{ Component }">
          <!-- Wrapper 1 element: Transition mode="out-in" không animate được multi-root (vd. TraCuuDon). -->
          <Transition name="sf-fade" mode="out-in">
            <div :key="route.path" class="sf-page">
              <component :is="Component" />
            </div>
          </Transition>
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
