<script setup>
import { computed, onUnmounted, reactive, ref, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import { useAuthModal } from '@/composables/useAuthModal'
import { quenMatKhauKhach, datLaiMatKhauKhach } from '@/api/authApi'

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
const forgotForm = reactive({
  email: '',
  otp: '',
  matKhauMoi: '',
  matKhauXacNhan: '',
})

const registerErrors = reactive({})
const forgotErrors = reactive({})

const authMode = ref('login')
const showLoginPw = ref(false)
const showRegisterPw = ref(false)
const showForgotPw = ref(false)
const showForgotPwConfirm = ref(false)
const loading = ref(false)
const serverError = ref('')
const serverInfo = ref('')
const resendCooldown = ref(0)

let resendTimer = null

const brandTitle = computed(() => {
  if (authMode.value === 'forgot-send') return 'Quên mật khẩu'
  if (authMode.value === 'forgot-reset') return 'Đặt lại mật khẩu'
  return tab.value === 'login' ? 'Chào mừng trở lại' : 'Tạo tài khoản'
})

watch(visible, (open) => {
  if (open) {
    if (authMode.value !== 'forgot-send' && authMode.value !== 'forgot-reset') {
      authMode.value = tab.value === 'register' ? 'register' : 'login'
    }
    serverError.value = ''
    serverInfo.value = ''
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
    resetForgotFlow()
  }
})

onUnmounted(() => {
  if (resendTimer) clearInterval(resendTimer)
})

function resetForgotFlow() {
  authMode.value = tab.value === 'register' ? 'register' : 'login'
  forgotForm.email = ''
  forgotForm.otp = ''
  forgotForm.matKhauMoi = ''
  forgotForm.matKhauXacNhan = ''
  Object.keys(forgotErrors).forEach((k) => delete forgotErrors[k])
  resendCooldown.value = 0
  if (resendTimer) {
    clearInterval(resendTimer)
    resendTimer = null
  }
}

function switchTab(next) {
  tab.value = next
  authMode.value = next
  serverError.value = ''
  serverInfo.value = ''
  Object.keys(forgotErrors).forEach((k) => delete forgotErrors[k])
}

function openForgotPassword() {
  serverError.value = ''
  serverInfo.value = ''
  const tk = loginForm.taiKhoan.trim()
  forgotForm.email = tk.includes('@') ? tk : ''
  authMode.value = 'forgot-send'
}

function backToLogin() {
  authMode.value = 'login'
  tab.value = 'login'
  serverError.value = ''
  serverInfo.value = ''
}

function startResendCooldown(seconds = 30) {
  resendCooldown.value = seconds
  if (resendTimer) clearInterval(resendTimer)
  resendTimer = setInterval(() => {
    if (resendCooldown.value <= 1) {
      resendCooldown.value = 0
      clearInterval(resendTimer)
      resendTimer = null
    } else {
      resendCooldown.value -= 1
    }
  }, 1000)
}

function validateForgotEmail() {
  Object.keys(forgotErrors).forEach((k) => delete forgotErrors[k])
  let ok = true
  if (!forgotForm.email.trim()) {
    forgotErrors.email = 'Email là bắt buộc'
    ok = false
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(forgotForm.email.trim())) {
    forgotErrors.email = 'Email không hợp lệ'
    ok = false
  }
  return ok
}

function validateForgotReset() {
  Object.keys(forgotErrors).forEach((k) => delete forgotErrors[k])
  let ok = true
  if (!/^\d{6}$/.test(forgotForm.otp.trim())) {
    forgotErrors.otp = 'Mã OTP gồm 6 chữ số'
    ok = false
  }
  if (!forgotForm.matKhauMoi || forgotForm.matKhauMoi.length < 6) {
    forgotErrors.matKhauMoi = 'Mật khẩu tối thiểu 6 ký tự'
    ok = false
  }
  if (forgotForm.matKhauMoi !== forgotForm.matKhauXacNhan) {
    forgotErrors.matKhauXacNhan = 'Mật khẩu nhập lại không khớp'
    ok = false
  }
  return ok
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

async function handleSendOtp() {
  serverError.value = ''
  serverInfo.value = ''
  if (!validateForgotEmail()) return
  if (resendCooldown.value > 0) return

  loading.value = true
  try {
    const res = await quenMatKhauKhach({ email: forgotForm.email.trim() })
    serverInfo.value = res.data?.message || 'Nếu email tồn tại, mã xác nhận đã được gửi.'
    authMode.value = 'forgot-reset'
    startResendCooldown(30)
  } catch (error) {
    serverError.value = typeof error === 'string' ? error : 'Không gửi được mã xác nhận'
  } finally {
    loading.value = false
  }
}

async function handleResendOtp() {
  if (resendCooldown.value > 0) return
  await handleSendOtp()
}

async function handleResetPassword() {
  serverError.value = ''
  serverInfo.value = ''
  if (!validateForgotReset()) return

  loading.value = true
  try {
    const res = await datLaiMatKhauKhach({
      email: forgotForm.email.trim(),
      otp: forgotForm.otp.trim(),
      matKhauMoi: forgotForm.matKhauMoi,
    })
    serverInfo.value = res.data?.message || 'Đổi mật khẩu thành công, mời đăng nhập.'
    loginForm.taiKhoan = forgotForm.email.trim()
    loginForm.matKhau = ''
    forgotForm.otp = ''
    forgotForm.matKhauMoi = ''
    forgotForm.matKhauXacNhan = ''
    authMode.value = 'login'
    tab.value = 'login'
  } catch (error) {
    serverError.value = typeof error === 'string' ? error : 'Đặt lại mật khẩu thất bại'
  } finally {
    loading.value = false
  }
}

async function handleLogin() {
  serverError.value = ''
  serverInfo.value = ''
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
  serverInfo.value = ''
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

function onOtpInput(e) {
  forgotForm.otp = String(e.target.value || '').replace(/\D/g, '').slice(0, 6)
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
            <div v-if="authMode === 'login' || authMode === 'register'" class="sf-modal__tabs">
              <button type="button" :class="{ active: authMode === 'login' }" @click="switchTab('login')">
                Đăng nhập
              </button>
              <button type="button" :class="{ active: authMode === 'register' }" @click="switchTab('register')">
                Tạo tài khoản
              </button>
            </div>

            <button
              v-else
              type="button"
              class="sf-auth-back"
              @click="authMode === 'forgot-reset' ? (authMode = 'forgot-send') : backToLogin()"
            >
              <Icon icon="mdi:arrow-left" width="18" />
              Quay lại
            </button>

            <div v-if="serverError" class="sf-auth-alert">{{ serverError }}</div>
            <div v-if="serverInfo" class="sf-auth-info">{{ serverInfo }}</div>

            <form v-if="authMode === 'login'" class="sf-modal__form" @submit.prevent="handleLogin">
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
              <a href="#" class="sf-auth-forgot" @click.prevent="openForgotPassword">Quên mật khẩu?</a>
              <button type="submit" class="sf-modal__submit" :disabled="loading">
                {{ loading ? 'Đang xử lý...' : 'Đăng nhập' }}
              </button>
              <p class="sf-modal__switch">
                Chưa có tài khoản?
                <button type="button" @click="switchTab('register')">Tạo tài khoản</button>
              </p>
            </form>

            <form v-else-if="authMode === 'register'" class="sf-modal__form" @submit.prevent="handleRegister">
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

            <form v-else-if="authMode === 'forgot-send'" class="sf-modal__form" @submit.prevent="handleSendOtp">
              <p class="sf-auth-hint">
                Nhập email đã đăng ký. Chúng tôi sẽ gửi mã OTP 6 số để đặt lại mật khẩu.
              </p>
              <div class="sf-field">
                <label for="forgot-email">Email</label>
                <input
                  id="forgot-email"
                  v-model="forgotForm.email"
                  type="email"
                  class="sf-modal__input"
                  autocomplete="email"
                />
                <span v-if="forgotErrors.email" class="sf-field-error">{{ forgotErrors.email }}</span>
              </div>
              <button type="submit" class="sf-modal__submit" :disabled="loading || resendCooldown > 0">
                {{ loading ? 'Đang gửi...' : resendCooldown > 0 ? `Gửi lại sau ${resendCooldown}s` : 'Gửi mã' }}
              </button>
            </form>

            <form v-else class="sf-modal__form" @submit.prevent="handleResetPassword">
              <p class="sf-auth-hint">
                Nhập mã OTP đã gửi tới <strong>{{ forgotForm.email }}</strong>
              </p>
              <div class="sf-field">
                <label for="forgot-otp">Mã OTP (6 số)</label>
                <input
                  id="forgot-otp"
                  :value="forgotForm.otp"
                  type="text"
                  inputmode="numeric"
                  maxlength="6"
                  class="sf-modal__input sf-modal__input--otp"
                  autocomplete="one-time-code"
                  @input="onOtpInput"
                />
                <span v-if="forgotErrors.otp" class="sf-field-error">{{ forgotErrors.otp }}</span>
              </div>
              <div class="sf-field">
                <label for="forgot-new-pw">Mật khẩu mới</label>
                <div class="sf-password-wrap">
                  <input
                    id="forgot-new-pw"
                    v-model="forgotForm.matKhauMoi"
                    :type="showForgotPw ? 'text' : 'password'"
                    class="sf-modal__input"
                    autocomplete="new-password"
                  />
                  <button
                    type="button"
                    class="sf-modal__eye"
                    :aria-label="showForgotPw ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
                    @click="showForgotPw = !showForgotPw"
                  >
                    <Icon :icon="showForgotPw ? 'mdi:eye-off' : 'mdi:eye'" width="20" />
                  </button>
                </div>
                <span v-if="forgotErrors.matKhauMoi" class="sf-field-error">{{ forgotErrors.matKhauMoi }}</span>
              </div>
              <div class="sf-field">
                <label for="forgot-confirm-pw">Nhập lại mật khẩu</label>
                <div class="sf-password-wrap">
                  <input
                    id="forgot-confirm-pw"
                    v-model="forgotForm.matKhauXacNhan"
                    :type="showForgotPwConfirm ? 'text' : 'password'"
                    class="sf-modal__input"
                    autocomplete="new-password"
                  />
                  <button
                    type="button"
                    class="sf-modal__eye"
                    :aria-label="showForgotPwConfirm ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
                    @click="showForgotPwConfirm = !showForgotPwConfirm"
                  >
                    <Icon :icon="showForgotPwConfirm ? 'mdi:eye-off' : 'mdi:eye'" width="20" />
                  </button>
                </div>
                <span v-if="forgotErrors.matKhauXacNhan" class="sf-field-error">{{ forgotErrors.matKhauXacNhan }}</span>
              </div>
              <button type="submit" class="sf-modal__submit" :disabled="loading">
                {{ loading ? 'Đang xử lý...' : 'Đặt lại mật khẩu' }}
              </button>
              <p class="sf-modal__switch">
                Chưa nhận được mã?
                <button type="button" :disabled="resendCooldown > 0 || loading" @click="handleResendOtp">
                  {{ resendCooldown > 0 ? `Gửi lại sau ${resendCooldown}s` : 'Gửi lại mã' }}
                </button>
              </p>
            </form>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>
