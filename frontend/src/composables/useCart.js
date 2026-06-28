import { computed, ref } from 'vue'
import {
  addGioHangItem,
  clearGioHang,
  deleteGioHangItem,
  fetchGioHang,
  updateGioHangItem,
} from '@/api/gioHangApi'
import { getCustomerId } from '@/composables/useAuth'

const STORAGE_KEY = 'sunova_cart'
const MAX_PER_LINE = 10

const items = ref([])
const loading = ref(false)
let currentLoadPromise = null
let loadedCustomerId = null

function normalizeLocalLine(line) {
  return {
    ...line,
    selected: line.selected !== false,
    soLuongTon: Number(line.soLuongTon) || 0,
    soLuong: Number(line.soLuong) || 1,
  }
}

function loadLocal() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    const parsed = raw ? JSON.parse(raw) : []
    items.value = Array.isArray(parsed) ? parsed.map(normalizeLocalLine) : []
  } catch {
    items.value = []
  }
}

function saveLocal() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(items.value))
}

function clearLocal() {
  localStorage.removeItem(STORAGE_KEY)
}

loadLocal()

export function variantLabel(line) {
  const parts = []
  if (line?.tenMauSac) parts.push(line.tenMauSac)
  if (line?.dungTichMl) parts.push(`${line.dungTichMl} ml`)
  return parts.join(' · ')
}

export function maxQtyFor(line) {
  const stock = Math.max(0, Number(line?.soLuongTon) || 0)
  if (stock <= 0) return 1
  return Math.min(stock, MAX_PER_LINE)
}

function clampQty(line, qty) {
  return Math.max(1, Math.min(maxQtyFor(line), Number(qty) || 1))
}

function mergeLine(existing, payload, addQty) {
  existing.tenSanPham = payload.tenSanPham ?? existing.tenSanPham
  existing.tenThuongHieu = payload.tenThuongHieu ?? existing.tenThuongHieu
  existing.giaBan = payload.giaBan ?? existing.giaBan
  existing.giaGoc = payload.giaGoc ?? existing.giaGoc
  existing.anhUrl = payload.anhUrl ?? existing.anhUrl
  existing.tenMauSac = payload.tenMauSac ?? existing.tenMauSac
  existing.dungTichMl = payload.dungTichMl ?? existing.dungTichMl
  existing.soLuongTon = payload.soLuongTon ?? existing.soLuongTon
  existing.idSanPham = payload.idSanPham ?? existing.idSanPham
  existing.sku = payload.sku ?? existing.sku
  const next = existing.soLuong + addQty
  existing.soLuong = clampQty(existing, next)
}

function addLocalItem(payload) {
  const addQty = Math.max(1, Number(payload.soLuong) || 1)
  const existing = items.value.find((l) => l.idChiTietSanPham === payload.idChiTietSanPham)
  if (existing) {
    mergeLine(existing, payload, addQty)
  } else {
    items.value.push({
      ...payload,
      soLuong: clampQty(payload, addQty),
      selected: true,
    })
  }
  saveLocal()
}

function toCartLine(line) {
  return normalizeLocalLine({
    ...line,
    idChiTietGioHang: line.idChiTietGioHang ?? line.id,
    anhUrl: line.anhUrl || line.anhChinhUrl || '',
    giaBan: Number(line.giaBan) || 0,
    giaGoc: line.giaGoc != null ? Number(line.giaGoc) : null,
  })
}

function applyCartResponse(data) {
  const selectedByVariant = new Map(items.value.map((line) => [line.idChiTietSanPham, line.selected]))
  items.value = (data?.items || []).map((line) => {
    const next = toCartLine(line)
    next.selected = selectedByVariant.get(next.idChiTietSanPham) !== false
    return next
  })
  loadedCustomerId = data?.idKhachHang ?? getCustomerId()
  clearLocal()
}

function customerIdOrNull() {
  return Number(getCustomerId()) || null
}

async function refreshCart() {
  const idKhachHang = customerIdOrNull()
  if (!idKhachHang) {
    loadedCustomerId = null
    loadLocal()
    return null
  }
  if (currentLoadPromise && loadedCustomerId === idKhachHang) return currentLoadPromise

  loading.value = true
  currentLoadPromise = fetchGioHang(idKhachHang)
    .then((res) => {
      applyCartResponse(res.data)
      return res.data
    })
    .finally(() => {
      loading.value = false
      currentLoadPromise = null
    })
  return currentLoadPromise
}

async function syncCartAfterLogin() {
  const idKhachHang = customerIdOrNull()
  if (!idKhachHang) return null

  const localItems = [...items.value]
  loading.value = true
  try {
    for (const line of localItems) {
      if (!line.idChiTietSanPham || !line.soLuong) continue
      const res = await addGioHangItem({
        idKhachHang,
        idChiTietSanPham: line.idChiTietSanPham,
        soLuong: line.soLuong,
      })
      applyCartResponse(res.data)
    }
    if (!localItems.length) {
      await refreshCart()
    }
    clearLocal()
    return items.value
  } finally {
    loading.value = false
  }
}

if (typeof window !== 'undefined') {
  window.addEventListener('sunova-customer-auth-changed', (event) => {
    if (event.detail?.loggedIn) {
      void syncCartAfterLogin().catch(() => {
        void refreshCart().catch(() => {})
      })
    } else {
      loadedCustomerId = null
      loadLocal()
    }
  })
}

export function useCart() {
  const idKhachHang = customerIdOrNull()
  if (idKhachHang && loadedCustomerId !== idKhachHang) {
    void refreshCart()
  }

  const count = computed(() =>
    items.value.reduce((sum, line) => sum + (line.soLuong || 0), 0),
  )

  const total = computed(() =>
    items.value.reduce((sum, line) => sum + (line.giaBan || 0) * (line.soLuong || 0), 0),
  )

  const selectedItems = computed(() => items.value.filter((l) => l.selected))

  const selectedCount = computed(() =>
    selectedItems.value.reduce((sum, line) => sum + (line.soLuong || 0), 0),
  )

  const selectedSubtotal = computed(() =>
    selectedItems.value.reduce((sum, line) => sum + (line.giaBan || 0) * (line.soLuong || 0), 0),
  )

  const selectedSavings = computed(() =>
    selectedItems.value.reduce((sum, line) => {
      const goc = Number(line.giaGoc)
      const ban = Number(line.giaBan)
      if (!goc || goc <= ban) return sum
      return sum + (goc - ban) * (line.soLuong || 0)
    }, 0),
  )

  const allSelected = computed(
    () => items.value.length > 0 && items.value.every((l) => l.selected),
  )

  async function addItem(payload) {
    const idKhachHang = customerIdOrNull()
    if (!idKhachHang) {
      addLocalItem(payload)
      return items.value
    }

    const res = await addGioHangItem({
      idKhachHang,
      idChiTietSanPham: payload.idChiTietSanPham,
      soLuong: Math.max(1, Number(payload.soLuong) || 1),
    })
    applyCartResponse(res.data)
    return res.data
  }

  async function removeItem(idChiTietSanPham) {
    const idKhachHang = customerIdOrNull()
    const line = items.value.find((l) => l.idChiTietSanPham === idChiTietSanPham)
    if (!idKhachHang || !line?.idChiTietGioHang) {
      items.value = items.value.filter((l) => l.idChiTietSanPham !== idChiTietSanPham)
      saveLocal()
      return items.value
    }

    const res = await deleteGioHangItem(idKhachHang, line.idChiTietGioHang)
    applyCartResponse(res.data)
    return res.data
  }

  async function removeSelected() {
    const selected = items.value.filter((l) => l.selected)
    const idKhachHang = customerIdOrNull()
    if (!idKhachHang) {
      items.value = items.value.filter((l) => !l.selected)
      saveLocal()
      return items.value
    }

    if (selected.length === items.value.length) {
      return clearCart()
    }

    let lastResponse = null
    for (const line of selected) {
      if (!line.idChiTietGioHang) continue
      const res = await deleteGioHangItem(idKhachHang, line.idChiTietGioHang)
      lastResponse = res.data
      applyCartResponse(res.data)
    }
    return lastResponse
  }

  function toggleSelect(idChiTietSanPham) {
    const line = items.value.find((l) => l.idChiTietSanPham === idChiTietSanPham)
    if (line) {
      line.selected = !line.selected
      if (!customerIdOrNull()) saveLocal()
    }
  }

  function setSelectAll(value) {
    items.value.forEach((l) => {
      l.selected = value
    })
    if (!customerIdOrNull()) saveLocal()
  }

  /** @returns {'ok'|'min'|'max'} */
  async function decreaseQty(idChiTietSanPham) {
    const line = items.value.find((l) => l.idChiTietSanPham === idChiTietSanPham)
    if (!line) return 'ok'
    if (line.soLuong <= 1) return 'min'
    return setQty(idChiTietSanPham, line.soLuong - 1).then(() => 'ok')
  }

  /** @returns {'ok'|'max'} */
  async function increaseQty(idChiTietSanPham) {
    const line = items.value.find((l) => l.idChiTietSanPham === idChiTietSanPham)
    if (!line) return 'ok'
    const max = maxQtyFor(line)
    if (line.soLuong >= max) return 'max'
    return setQty(idChiTietSanPham, line.soLuong + 1).then(() => 'ok')
  }

  /**
   * @returns {{ clamped: number, hitMin: boolean, hitMax: boolean }}
   */
  async function setQty(idChiTietSanPham, soLuong) {
    const line = items.value.find((l) => l.idChiTietSanPham === idChiTietSanPham)
    if (!line) return { clamped: 1, hitMin: false, hitMax: false }
    const raw = Number(soLuong)
    const max = maxQtyFor(line)
    let hitMin = false
    let hitMax = false
    let target = raw
    if (Number.isNaN(raw) || raw < 1) {
      target = 1
      hitMin = true
    } else if (raw > max) {
      target = max
      hitMax = true
    }
    const idKhachHang = customerIdOrNull()
    if (!idKhachHang || !line.idChiTietGioHang) {
      line.soLuong = target
      saveLocal()
      return { clamped: target, hitMin, hitMax }
    }

    const res = await updateGioHangItem(idKhachHang, line.idChiTietGioHang, target)
    applyCartResponse(res.data)
    return { clamped: target, hitMin, hitMax }
  }

  async function clearCart() {
    const idKhachHang = customerIdOrNull()
    if (!idKhachHang) {
      items.value = []
      saveLocal()
      return items.value
    }

    const res = await clearGioHang(idKhachHang)
    applyCartResponse(res.data)
    return res.data
  }

  return {
    items,
    loading,
    count,
    total,
    selectedItems,
    selectedCount,
    selectedSubtotal,
    selectedSavings,
    allSelected,
    addItem,
    removeItem,
    removeSelected,
    toggleSelect,
    setSelectAll,
    decreaseQty,
    increaseQty,
    setQty,
    clearCart,
    refreshCart,
    syncCartAfterLogin,
  }
}

export function getCartCount() {
  return items.value.reduce((sum, line) => sum + (line.soLuong || 0), 0)
}
