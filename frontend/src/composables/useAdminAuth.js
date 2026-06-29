import { computed, ref } from 'vue'
import { dangNhapNhanVien } from '@/api/authApi'

const STORAGE_KEY = 'sunova_admin_auth'

const token = ref(null)
const nhanVienId = ref(null)
const hoTen = ref('')
const vaiTro = ref('')

function loadFromStorage() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return
    const data = JSON.parse(raw)
    token.value = data.token || null
    nhanVienId.value = data.nhanVienId ?? data.id ?? null
    hoTen.value = data.hoTen || ''
    vaiTro.value = data.vaiTro || ''
    if (data.vaiTro) {
      localStorage.setItem('sunova_admin_role', data.vaiTro)
    }
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
      nhanVienId: nhanVienId.value,
      hoTen: hoTen.value,
      vaiTro: vaiTro.value,
    }),
  )
  if (vaiTro.value) {
    localStorage.setItem('sunova_admin_role', vaiTro.value)
  }
}

function clearStorage() {
  token.value = null
  nhanVienId.value = null
  hoTen.value = ''
  vaiTro.value = ''
  localStorage.removeItem(STORAGE_KEY)
}

function applyAuth(data) {
  token.value = data.token
  nhanVienId.value = data.id ?? null
  hoTen.value = data.hoTen || ''
  vaiTro.value = data.vaiTro || ''
  persist()
}

loadFromStorage()

export function useAdminAuth() {
  const isLoggedIn = computed(() => Boolean(token.value))

  async function dangNhap(taiKhoan, matKhau) {
    const res = await dangNhapNhanVien({ taiKhoan, matKhau })
    applyAuth(res.data)
    return res.data
  }

  function dangXuat() {
    clearStorage()
    localStorage.removeItem('sunova_admin_role')
  }

  return {
    token,
    nhanVienId,
    hoTen,
    vaiTro,
    isLoggedIn,
    dangNhap,
    dangXuat,
  }
}

export function getAdminToken() {
  if (token.value) return token.value
  loadFromStorage()
  return token.value
}

export function isAdminLoggedIn() {
  return Boolean(getAdminToken())
}
