<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  /** 'storefront' | 'admin' */
  variant: { type: String, default: 'storefront' },
})

const router = useRouter()
const isAdmin = computed(() => props.variant === 'admin')

function goHome() {
  router.push(isAdmin.value ? '/admin/pos' : '/')
}
</script>

<template>
  <div class="app-not-found" :class="{ 'app-not-found--admin': isAdmin }">
    <p class="app-not-found__code">404</p>
    <h1 class="app-not-found__title">Không tìm thấy trang</h1>
    <p class="app-not-found__msg">
      Đường dẫn không tồn tại hoặc đã bị di chuyển. Vui lòng kiểm tra lại hoặc quay về trang chủ.
    </p>
    <button type="button" class="app-not-found__btn" @click="goHome">
      {{ isAdmin ? 'Về bán hàng tại quầy' : 'Về trang chủ' }}
    </button>
  </div>
</template>

<style scoped>
.app-not-found {
  max-width: 480px;
  margin: 4rem auto;
  padding: 2rem 1.25rem;
  text-align: center;
  color: #3e2c1c;
}

.app-not-found--admin {
  margin: 3rem auto;
}

.app-not-found__code {
  margin: 0;
  font-size: 3rem;
  font-weight: 800;
  letter-spacing: 0.06em;
  color: #9e7340;
}

.app-not-found__title {
  margin: 0.35rem 0 0.5rem;
  font-size: 1.45rem;
}

.app-not-found__msg {
  margin: 0;
  color: #5a5248;
  line-height: 1.5;
  font-size: 0.95rem;
}

.app-not-found__btn {
  margin-top: 1.25rem;
  border-radius: 999px;
  padding: 0.6rem 1.25rem;
  border: 1px solid #3e2c1c;
  background: #3e2c1c;
  color: #fffaf4;
  font: inherit;
  font-weight: 600;
  cursor: pointer;
}
</style>
