<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import AppLogo from '@/components/common/AppLogo.vue'
import { useAdminAuth } from '@/composables/useAdminAuth'
import { getAdminHomePath } from '@/utils/adminAuth'

const router = useRouter()
const route = useRoute()
const { dangNhap } = useAdminAuth()

const form = reactive({ taiKhoan: '', matKhau: '' })
const showPassword = ref(false)
const loading = ref(false)
const serverError = ref('')

const sessionExpired = computed(() => route.query.expired === '1')

async function handleSubmit() {
  serverError.value = ''
  if (!form.taiKhoan.trim() || !form.matKhau) {
    serverError.value = 'Vui lòng nhập đầy đủ thông tin'
    return
  }

  loading.value = true
  try {
    const data = await dangNhap(form.taiKhoan.trim(), form.matKhau)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    const home = getAdminHomePath(data.vaiTro)
    await router.replace(redirect && redirect.startsWith('/admin') ? redirect : home)
  } catch (err) {
    serverError.value = String(err)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="admin-login-page">
    <div class="admin-login-card">
      <div class="admin-login-card__brand">
        <AppLogo variant="dark" :size="48" />
        <p class="admin-login-card__eyebrow">SUNOVA Admin</p>
        <h1 class="admin-login-card__title">Đăng nhập quản trị</h1>
        <p class="admin-login-card__subtitle">
          Dành cho nhân viên và quản lý cửa hàng
        </p>
      </div>

      <p v-if="sessionExpired" class="admin-login-alert admin-login-alert--warn">
        Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.
      </p>

      <form class="admin-login-form" @submit.prevent="handleSubmit">
        <div class="admin-login-field">
          <label for="admin-login-account">Email hoặc số điện thoại</label>
          <input
            id="admin-login-account"
            v-model="form.taiKhoan"
            type="text"
            class="admin-input"
            autocomplete="username"
            placeholder="an.nv@sunova.vn"
          />
        </div>

        <div class="admin-login-field">
          <label for="admin-login-password">Mật khẩu</label>
          <div class="admin-login-password">
            <input
              id="admin-login-password"
              v-model="form.matKhau"
              :type="showPassword ? 'text' : 'password'"
              class="admin-input"
              autocomplete="current-password"
              placeholder="••••••••"
            />
            <button
              type="button"
              class="admin-login-password__toggle"
              :aria-label="showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
              @click="showPassword = !showPassword"
            >
              <Icon :icon="showPassword ? 'icon-park-outline:preview-close-one' : 'icon-park-outline:preview-open'" />
            </button>
          </div>
        </div>

        <p v-if="serverError" class="admin-login-alert admin-login-alert--error">
          {{ serverError }}
        </p>

        <button type="submit" class="soleil-btn-primary admin-login-submit w-full justify-center" :disabled="loading">
          {{ loading ? 'Đang đăng nhập...' : 'Đăng nhập' }}
        </button>
      </form>
    </div>
  </div>
</template>
