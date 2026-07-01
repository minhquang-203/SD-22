<script setup>
import 'bootstrap/dist/css/bootstrap.min.css'
import '@/styles/soleil-storefront.css'
import TheNavbar from '@/components/storefront/TheNavbar.vue'
import TheFooter from '@/components/storefront/TheFooter.vue'
import AuthModal from '@/components/storefront/AuthModal.vue'
import CartToast from '@/components/storefront/CartToast.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import ChatWidget from '@/components/storefront/ChatWidget.vue'
</script>

<template>
  <div class="storefront-root storefront-shell">
    <TheNavbar />
    <main class="sf-main">
      <router-view />
    </main>
    <TheFooter />
    <AuthModal />
    <CartToast />
    <ConfirmDialog />
    <ChatWidget />
  </div>
</template>
