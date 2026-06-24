<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import { useAuthModal } from '@/composables/useAuthModal'

const router = useRouter()
const { dangNhap, dangKy } = useAuth()
const { visible, tab, redirectAfter, closeAuthModal } = useAuthModal()

const loginForm = reactive({ taiKhoan: '', matKhau: '' })
const registerForm = reactive({
  hoTen: '',
  soDienThoai: '',
  email: '',
  matKhau: '',
  dongY: false,
})
const registerErrors = reactive({})
const showLoginPw = ref(false)
const showRegisterPw = ref(false)
const loading = ref(false)
const serverError = ref('')

const brandTitle = computed(() =>
  tab.value === 'login' ? 'Chào mừng trở lại' : 'Tạo tài khoản',
)

watch(visible, (open) => {
  if (open) {
    serverError.value = ''
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
  }
})

function switchTab(next) {
  tab.value = next
  serverError.value = ''
}

function validateRegister() {
  Object.keys(registerErrors).forEach((k) => delete registerErrors[k])
  let ok = true
  if (!registerForm.hoTen.trim()) {
    registerErrors.hoTen = 'Họ tên là bắt buộc'
    ok = false
  }
  if (!registerForm.soDienThoai.trim()) {
    registerErrors.soDienThoai = 'Số điện thoại là bắt buộc'
    ok = false
  }
  if (!registerForm.email.trim()) {
    registerErrors.email = 'Email là bắt buộc'
    ok = false
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(registerForm.email.trim())) {
    registerErrors.email = 'Email không hợp lệ'
    ok = false
  }
  if (!registerForm.matKhau || registerForm.matKhau.length < 6) {
    registerErrors.matKhau = 'Mật khẩu từ 6–32 ký tự'
    ok = false
  } else if (registerForm.matKhau.length > 32) {
    registerErrors.matKhau = 'Mật khẩu tối đa 32 ký tự'
    ok = false
  }
  if (!registerForm.dongY) {
    registerErrors.dongY = 'Vui lòng đồng ý điều khoản'
    ok = false
  }
  return ok
}

async function handleLogin() {
  serverError.value = ''
  if (!loginForm.taiKhoan.trim() || !loginForm.matKhau) {
    serverError.value = 'Vui lòng nhập đầy đủ thông tin'
    return
  }
  loading.value = true
  try {
    await dangNhap(loginForm.taiKhoan.trim(), loginForm.matKhau)
    const dest = redirectAfter.value || '/'
    closeAuthModal()
    if (dest && dest !== router.currentRoute.value.fullPath) {
      router.push(dest)
    }
  } catch (error) {
    serverError.value = typeof error === 'string' ? error : 'Đăng nhập thất bại'
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  serverError.value = ''
  if (!validateRegister()) return
  loading.value = true
  try {
    await dangKy({
      hoTen: registerForm.hoTen.trim(),
      email: registerForm.email.trim(),
      soDienThoai: registerForm.soDienThoai.trim(),
      matKhau: registerForm.matKhau,
    })
    const dest = redirectAfter.value || '/'
    closeAuthModal()
    if (dest && dest !== router.currentRoute.value.fullPath) {
      router.push(dest)
    }
  } catch (error) {
    serverError.value = typeof error === 'string' ? error : 'Đăng ký thất bại'
  } finally {
    loading.value = false
  }
}

function onBackdrop(e) {
  if (e.target === e.currentTarget) closeAuthModal()
}
</script>

<template>
  <Teleport to="body">
    <div v-if="visible" class="sf-modal-backdrop" @click="onBackdrop">
      <div class="sf-modal" role="dialog" aria-modal="true" aria-labelledby="sf-auth-title">
        <button type="button" class="sf-modal__close" aria-label="Đóng" @click="closeAuthModal">
          <Icon icon="mdi:close" width="22" />
        </button>

        <div class="sf-modal__layout">
          <aside class="sf-modal__aside">
            <span class="sf-modal__logo">SUN<span>OVA</span></span>

            <div class="sf-modal__sun-ring" aria-hidden="true">
              <Icon icon="solar:sun-2-linear" width="36" />
            </div>

            <h2 id="sf-auth-title" class="sf-modal__aside-title">{{ brandTitle }}</h2>
            <p class="sf-modal__aside-slogan">Chống nắng tinh tế cho làn da Việt</p>

            <p class="sf-modal__aside-eyebrow">SOLEIL SKINCARE</p>
          </aside>

          <div class="sf-modal__main">
            <div class="sf-modal__tabs">
              <button type="button" :class="{ active: tab === 'login' }" @click="switchTab('login')">
                Đăng nhập
              </button>
              <button type="button" :class="{ active: tab === 'register' }" @click="switchTab('register')">
                Tạo tài khoản
              </button>
            </div>

            <div v-if="serverError" class="sf-auth-alert">{{ serverError }}</div>

            <form v-if="tab === 'login'" class="sf-modal__form" @submit.prevent="handleLogin">
              <div class="sf-field">
                <label for="modal-taiKhoan">Email hoặc số điện thoại</label>
                <input
                  id="modal-taiKhoan"
                  v-model="loginForm.taiKhoan"
                  type="text"
                  class="sf-modal__input"
                  autocomplete="username"
                />
              </div>
              <div class="sf-field">
                <label for="modal-matKhau">Mật khẩu</label>
                <div class="sf-password-wrap">
                  <input
                    id="modal-matKhau"
                    v-model="loginForm.matKhau"
                    :type="showLoginPw ? 'text' : 'password'"
                    class="sf-modal__input"
                    autocomplete="current-password"
                  />
                  <button
                    type="button"
                    class="sf-modal__eye"
                    :aria-label="showLoginPw ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
                    @click="showLoginPw = !showLoginPw"
                  >
                    <Icon :icon="showLoginPw ? 'mdi:eye-off' : 'mdi:eye'" width="20" />
                  </button>
                </div>
              </div>
              <a href="#" class="sf-auth-forgot" @click.prevent>Quên mật khẩu?</a>
              <button type="submit" class="sf-modal__submit" :disabled="loading">
                {{ loading ? 'Đang xử lý...' : 'Đăng nhập' }}
              </button>
              <p class="sf-modal__switch">
                Chưa có tài khoản?
                <button type="button" @click="switchTab('register')">Tạo tài khoản</button>
              </p>
            </form>

            <form v-else class="sf-modal__form" @submit.prevent="handleRegister">
              <div class="sf-field">
                <label for="modal-hoTen">Họ tên</label>
                <input id="modal-hoTen" v-model="registerForm.hoTen" type="text" class="sf-modal__input" autocomplete="name" />
                <span v-if="registerErrors.hoTen" class="sf-field-error">{{ registerErrors.hoTen }}</span>
              </div>
              <div class="sf-field">
                <label for="modal-sdt">Số điện thoại</label>
                <input id="modal-sdt" v-model="registerForm.soDienThoai" type="tel" class="sf-modal__input" autocomplete="tel" />
                <span v-if="registerErrors.soDienThoai" class="sf-field-error">{{ registerErrors.soDienThoai }}</span>
              </div>
              <div class="sf-field">
                <label for="modal-email">Email</label>
                <input id="modal-email" v-model="registerForm.email" type="email" class="sf-modal__input" autocomplete="email" />
                <span v-if="registerErrors.email" class="sf-field-error">{{ registerErrors.email }}</span>
              </div>
              <div class="sf-field">
                <label for="modal-reg-pw">Mật khẩu</label>
                <div class="sf-password-wrap">
                  <input
                    id="modal-reg-pw"
                    v-model="registerForm.matKhau"
                    :type="showRegisterPw ? 'text' : 'password'"
                    class="sf-modal__input"
                    autocomplete="new-password"
                  />
                  <button
                    type="button"
                    class="sf-modal__eye"
                    :aria-label="showRegisterPw ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
                    @click="showRegisterPw = !showRegisterPw"
                  >
                    <Icon :icon="showRegisterPw ? 'mdi:eye-off' : 'mdi:eye'" width="20" />
                  </button>
                </div>
                <span v-if="registerErrors.matKhau" class="sf-field-error">{{ registerErrors.matKhau }}</span>
              </div>
              <label class="sf-filter-check sf-modal__terms">
                <input v-model="registerForm.dongY" type="checkbox" />
                Tôi đồng ý với điều khoản sử dụng của SUNOVA
              </label>
              <span v-if="registerErrors.dongY" class="sf-field-error">{{ registerErrors.dongY }}</span>
              <button type="submit" class="sf-modal__submit" :disabled="loading">
                {{ loading ? 'Đang xử lý...' : 'Đăng ký' }}
              </button>
              <p class="sf-modal__switch">
                Đã có tài khoản?
                <button type="button" @click="switchTab('login')">Đăng nhập</button>
              </p>
            </form>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>
