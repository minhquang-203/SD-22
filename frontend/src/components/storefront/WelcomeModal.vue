<script setup>
import { computed, onUnmounted, ref, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { useAuth } from '@/composables/useAuth'
import { useWelcomeModal } from '@/composables/useWelcomeModal'

const router = useRouter()
const { hoTen } = useAuth()
const { visible, closeWelcomeModal } = useWelcomeModal()

const uvIndex = ref(null)
const uvLoading = ref(false)
const uvFailed = ref(false)

const displayName = computed(() => (hoTen.value || '').trim() || 'bạn')

const uvLine = computed(() => {
  if (uvLoading.value) return 'Chỉ số UV hiện tại: đang cập nhật…'
  if (uvFailed.value || uvIndex.value == null) return 'Chỉ số UV hiện tại: đang cập nhật'
  return `Chỉ số UV hiện tại: ${uvIndex.value}`
})

async function loadUv() {
  uvLoading.value = true
  uvFailed.value = false
  uvIndex.value = null
  try {
    const res = await axios.get('/api/v1/weather/current', {
      params: { city: 'Hà Nội' },
      timeout: 8000,
    })
    const value = res.data?.weather?.uvIndex
    if (value == null || Number.isNaN(Number(value))) {
      uvFailed.value = true
    } else {
      uvIndex.value = Number(value)
    }
  } catch {
    uvFailed.value = true
  } finally {
    uvLoading.value = false
  }
}

watch(visible, (open) => {
  if (open) {
    document.body.style.overflow = 'hidden'
    void loadUv()
  } else {
    document.body.style.overflow = ''
  }
})

onUnmounted(() => {
  document.body.style.overflow = ''
})

function onBackdrop(e) {
  if (e.target === e.currentTarget) closeWelcomeModal()
}

function goQuiz() {
  closeWelcomeModal()
  router.push({ name: 'QuizPlaceholder' })
}

function defer() {
  closeWelcomeModal()
}
</script>

<template>
  <Teleport to="body">
    <div
      v-if="visible"
      class="sf-welcome-backdrop"
      role="presentation"
      @click="onBackdrop"
    >
      <div
        class="sf-welcome-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="sf-welcome-title"
      >
        <button
          type="button"
          class="sf-welcome-modal__close"
          aria-label="Đóng"
          @click="defer"
        >
          <Icon icon="mdi:close" width="22" />
        </button>

        <div class="sf-welcome-modal__icon" aria-hidden="true">
          <Icon icon="solar:sun-2-bold" width="36" />
        </div>

        <h2 id="sf-welcome-title" class="sf-welcome-modal__title">
          Chào mừng {{ displayName }} đã đến với SUNOVA
        </h2>
        <p class="sf-welcome-modal__lead">Chúc một ngày tốt lành!</p>

        <p class="sf-welcome-modal__uv">{{ uvLine }}</p>

        <p class="sf-welcome-modal__ask">
          Bạn có muốn làm quiz để được gợi ý sản phẩm không?
        </p>

        <div class="sf-welcome-modal__actions">
          <button type="button" class="sf-welcome-btn sf-welcome-btn--primary" @click="goQuiz">
            Làm quiz ngay
          </button>
          <button type="button" class="sf-welcome-btn sf-welcome-btn--ghost" @click="defer">
            Để sau
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.sf-welcome-backdrop {
  position: fixed;
  inset: 0;
  z-index: 5100;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(26, 24, 20, 0.45);
  backdrop-filter: blur(2px);
}

.sf-welcome-modal {
  position: relative;
  width: min(440px, 100%);
  padding: 32px 28px 28px;
  border-radius: 16px;
  background: var(--sf-warm-white, #fffdfa);
  border: 1px solid var(--sf-sand, #e8dcc8);
  box-shadow: 0 24px 48px rgba(26, 24, 20, 0.18);
  text-align: center;
  font-family: var(--sf-font-body, 'Be Vietnam Pro', system-ui, sans-serif);
  color: var(--sf-charcoal, #1a1814);
}

.sf-welcome-modal__close {
  position: absolute;
  top: 12px;
  right: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 999px;
  background: transparent;
  color: var(--sf-mid, #5a5248);
  cursor: pointer;
}

.sf-welcome-modal__close:hover {
  background: var(--sf-cream, #f9f5f0);
  color: var(--sf-charcoal, #1a1814);
}

.sf-welcome-modal__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  margin-bottom: 16px;
  border-radius: 50%;
  background: rgba(201, 169, 110, 0.16);
  color: var(--sf-gold-dark, #9e7340);
}

.sf-welcome-modal__title {
  margin: 0 0 8px;
  font-family: var(--sf-font-display, 'Playfair Display', Georgia, serif);
  font-size: 1.45rem;
  font-weight: 600;
  line-height: 1.35;
  color: var(--sf-charcoal, #1a1814);
}

.sf-welcome-modal__lead {
  margin: 0 0 14px;
  font-size: 0.95rem;
  color: var(--sf-gold-dark, #9e7340);
}

.sf-welcome-modal__uv {
  margin: 0 0 12px;
  padding: 10px 12px;
  border-radius: 10px;
  background: var(--sf-cream, #f9f5f0);
  border: 1px solid var(--sf-hairline, #ede5d8);
  font-size: 0.9rem;
  color: var(--sf-mid, #5a5248);
}

.sf-welcome-modal__ask {
  margin: 0 0 22px;
  font-size: 0.95rem;
  line-height: 1.5;
  color: var(--sf-mid, #5a5248);
}

.sf-welcome-modal__actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.sf-welcome-btn {
  width: 100%;
  padding: 12px 16px;
  border-radius: 999px;
  font-size: 0.9rem;
  font-weight: 600;
  letter-spacing: 0.04em;
  cursor: pointer;
  transition: background 0.2s, border-color 0.2s, color 0.2s;
}

.sf-welcome-btn--primary {
  border: 1px solid var(--sf-espresso, #2a201b);
  background: var(--sf-espresso, #2a201b);
  color: #fff;
}

.sf-welcome-btn--primary:hover {
  background: var(--sf-gold-dark, #9e7340);
  border-color: var(--sf-gold-dark, #9e7340);
}

.sf-welcome-btn--ghost {
  border: 1px solid var(--sf-sand, #e8dcc8);
  background: transparent;
  color: var(--sf-mid, #5a5248);
}

.sf-welcome-btn--ghost:hover {
  border-color: var(--sf-gold, #c9a96e);
  color: var(--sf-charcoal, #1a1814);
  background: rgba(201, 169, 110, 0.08);
}
</style>
