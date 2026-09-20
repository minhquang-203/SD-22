<script setup>
import { useRoute } from 'vue-router'
import 'bootstrap/dist/css/bootstrap.min.css'
import '@/styles/soleil-storefront.css'
import TheNavbar from '@/components/storefront/TheNavbar.vue'
import TheFooter from '@/components/storefront/TheFooter.vue'
import AuthModal from '@/components/storefront/AuthModal.vue'
import WelcomeModal from '@/components/storefront/WelcomeModal.vue'
import CartToast from '@/components/storefront/CartToast.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import ChatWidget from '@/components/storefront/ChatWidget.vue'

const route = useRoute()
</script>

<template>
  <div class="storefront-root storefront-shell">
    <TheNavbar />
    <main class="sf-main">
      <router-view v-slot="{ Component }">
        <!-- Wrapper 1 element: Transition mode="out-in" không animate được multi-root (vd. TraCuuDon). -->
        <Transition name="sf-fade" mode="out-in">
          <div :key="route.path" class="sf-page">
            <component :is="Component" />
          </div>
        </Transition>
      </router-view>
    </main>
    <TheFooter />
    <AuthModal />
    <WelcomeModal />
    <CartToast />
    <ConfirmDialog />
    <ChatWidget />
  </div>
</template>
