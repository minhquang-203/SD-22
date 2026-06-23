import { computed, ref } from 'vue'
import { dangKyKhach, dangNhapKhach } from '@/api/authApi'

const STORAGE_KEY = 'sunova_customer_auth'

const token = ref(null)
const hoTen = ref('')
const vaiTro = ref('')

function loadFromStorage() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return
    const data = JSON.parse(raw)
    token.value = data.token || null
    hoTen.value = data.hoTen || ''
    vaiTro.value = data.vaiTro || ''
  } catch {
    clearStorage()
  }
}

function persist() {
  if (!token.value) {
    localStorage.removeItem(STORAGE_KEY)
    return
  }
  localStorage.setItem(
    STORAGE_KEY,
    JSON.stringify({
      token: token.value,
      hoTen: hoTen.value,
      vaiTro: vaiTro.value,
    }),
  )
}

function clearStorage() {
  token.value = null
  hoTen.value = ''
  vaiTro.value = ''
  localStorage.removeItem(STORAGE_KEY)
}

function applyAuth(data) {
  token.value = data.token
  hoTen.value = data.hoTen || ''
  vaiTro.value = data.vaiTro || 'KHACH_HANG'
  persist()
}

loadFromStorage()

export function useAuth() {
  const isLoggedIn = computed(() => Boolean(token.value))

  async function dangNhap(taiKhoan, matKhau) {
    const res = await dangNhapKhach({ taiKhoan, matKhau })
    applyAuth(res.data)
    return res.data
  }

  async function dangKy(payload) {
    const res = await dangKyKhach(payload)
    applyAuth(res.data)
    return res.data
  }

  function dangXuat() {
    clearStorage()
  }

  function updateHoTen(name) {
    hoTen.value = name || ''
    persist()
  }

  return {
    token,
    hoTen,
    vaiTro,
    isLoggedIn,
    dangNhap,
    dangKy,
    dangXuat,
    updateHoTen,
  }
}

export function isCustomerLoggedIn() {
  if (token.value) return true
  loadFromStorage()
  return Boolean(token.value)
}

export function getCustomerToken() {
  if (token.value) return token.value
  loadFromStorage()
  return token.value
}
