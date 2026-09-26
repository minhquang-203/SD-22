<script setup>
import { onErrorCaptured, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const props = defineProps({
  /** 'storefront' | 'admin' — ảnh hưởng nút về trang chủ */
  variant: { type: String, default: 'storefront' },
})

const route = useRoute()
const router = useRouter()
const hasError = ref(false)
const errorInfo = ref('')

watch(
  () => route.fullPath,
  () => {
    hasError.value = false
    errorInfo.value = ''
  },
)

onErrorCaptured((err) => {
  hasError.value = true
  errorInfo.value = err?.message ? String(err.message) : 'Lỗi không xác định'
  console.error('[ErrorBoundary]', err)
  return false
})

function reloadPage() {
  window.location.reload()
}

function goHome() {
  hasError.value = false
  if (props.variant === 'admin') {
    router.push('/admin/pos')
  } else {
    router.push('/')
  }
}
</script>

<template>
  <div v-if="hasError" class="app-error-boundary" role="alert">
    <div class="app-error-boundary__card">
      <h2 class="app-error-boundary__title">Trang này đang gặp sự cố</h2>
      <p class="app-error-boundary__msg">
        Đã xảy ra lỗi khi hiển thị nội dung. Bạn có thể tải lại trang hoặc quay về trang chủ.
      </p>
      <p v-if="errorInfo" class="app-error-boundary__detail">{{ errorInfo }}</p>
      <div class="app-error-boundary__actions">
        <button type="button" class="app-error-boundary__btn app-error-boundary__btn--ghost" @click="reloadPage">
          Tải lại
        </button>
        <button type="button" class="app-error-boundary__btn app-error-boundary__btn--solid" @click="goHome">
          Về trang chủ
        </button>
      </div>
    </div>
  </div>
  <slot v-else />
</template>

<style scoped>
.app-error-boundary {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 42vh;
  padding: 2rem 1.25rem;
}

.app-error-boundary__card {
  width: 100%;
  max-width: 440px;
  text-align: center;
  padding: 1.75rem 1.5rem;
  border-radius: 16px;
  background: #fffaf4;
  border: 1px solid rgba(158, 115, 64, 0.22);
  color: #3e2c1c;
  box-shadow: 0 12px 40px rgba(36, 26, 18, 0.08);
}

.app-error-boundary__title {
  margin: 0 0 0.5rem;
  font-size: 1.35rem;
  font-weight: 650;
}

.app-error-boundary__msg {
  margin: 0;
  font-size: 0.95rem;
  line-height: 1.5;
  color: #5a5248;
}

.app-error-boundary__detail {
  margin: 0.75rem 0 0;
  font-size: 0.78rem;
  color: #8a7a6a;
  word-break: break-word;
}

.app-error-boundary__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.6rem;
  justify-content: center;
  margin-top: 1.25rem;
}

.app-error-boundary__btn {
  border-radius: 999px;
  padding: 0.55rem 1.15rem;
  font: inherit;
  font-weight: 600;
  cursor: pointer;
  min-height: 40px;
}

.app-error-boundary__btn--ghost {
  border: 1px solid rgba(158, 115, 64, 0.35);
  background: transparent;
  color: #3e2c1c;
}

.app-error-boundary__btn--solid {
  border: 1px solid #3e2c1c;
  background: #3e2c1c;
  color: #fffaf4;
}
</style>
