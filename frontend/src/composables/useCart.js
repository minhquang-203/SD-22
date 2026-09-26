import { computed, ref } from 'vue'
import {
  addGioHangItem,
  clearGioHang,
  deleteGioHangItem,
  fetchGioHang,
  updateGioHangItem,
} from '@/api/gioHangApi'
import { getCustomerId } from '@/composables/useAuth'
import { GIOI_HAN_MUA_LE } from '@/constants/cartLimits'

const STORAGE_KEY = 'sunova_cart'
/** @deprecated dùng GIOI_HAN_MUA_LE */
export const MAX_PER_LINE = GIOI_HAN_MUA_LE

const items = ref([])
const loading = ref(false)
let currentLoadPromise = null
let loadedCustomerId = null
let loadSeq = 0

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
  return Math.min(stock, GIOI_HAN_MUA_LE)
}

/**
 * Khi chạm max: 'stock' nếu tồn < giới hạn mua lẻ; 'retail' nếu chạm trần mua lẻ.
 */
export function capReasonFor(line) {
  const stock = Math.max(0, Number(line?.soLuongTon) || 0)
  if (stock > 0 && stock < GIOI_HAN_MUA_LE) return 'stock'
  return 'retail'
}

function clampQty(line, qty) {
  return Math.max(1, Math.min(maxQtyFor(line), Number(qty) || 1))
}

function lineSnapshot(line) {
  if (!line) return null
  return {
    idChiTietSanPham: line.idChiTietSanPham,
    idSanPham: line.idSanPham,
    tenSanPham: line.tenSanPham,
    anhUrl: line.anhUrl,
    tenMauSac: line.tenMauSac,
    dungTichMl: line.dungTichMl,
    sku: line.sku,
    soLuongTon: line.soLuongTon,
    soLuongHienTai: line.soLuong,
  }
}

function mergeLine(existing, payload, addQty) {
  existing.tenSanPham = payload.tenSanPham ?? existing.tenSanPham
  existing.tenThuongHieu = payload.tenThuongHieu ?? existing.tenThuongHieu
  existing.giaBan = payload.giaBan ?? existing.giaBan
  existing.giaGoc = payload.giaGoc ?? existing.giaGoc
  existing.phanTramGiam = payload.phanTramGiam ?? existing.phanTramGiam
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
    phanTramGiam: line.phanTramGiam != null ? Number(line.phanTramGiam) : null,
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

async function refreshCart(options = {}) {
  const force = options === true || Boolean(options?.force)
  const idKhachHang = customerIdOrNull()
  if (!idKhachHang) {
    loadedCustomerId = null
    loadLocal()
    return null
  }
  if (!force && currentLoadPromise && loadedCustomerId === idKhachHang) {
    return currentLoadPromise
  }

  const seq = ++loadSeq
  loading.value = true
  currentLoadPromise = fetchGioHang(idKhachHang)
    .then((res) => {
      // Bỏ qua response cũ nếu đã có refresh mới hơn (tránh ghi đè sau checkout)
      if (seq !== loadSeq) return items.value
      applyCartResponse(res.data)
      return res.data
    })
    .finally(() => {
      if (seq === loadSeq) {
        loading.value = false
        currentLoadPromise = null
      }
    })
  return currentLoadPromise
}

/**
 * Đăng nhập: nếu guest đang có giỏ local thì đẩy từng dòng lên giỏ server, rồi tải lại giỏ server.
 * Chỉ đồng bộ giỏ hàng hiện tại — không đụng đơn hàng.
 */
async function syncCartAfterLogin() {
  const idKhachHang = customerIdOrNull()
  if (!idKhachHang) return null

  let localLines = []
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    const parsed = raw ? JSON.parse(raw) : []
    if (Array.isArray(parsed) && parsed.length) {
      localLines = parsed.map(normalizeLocalLine)
    } else if (items.value.length) {
      localLines = items.value.map((line) => ({ ...line }))
    }
  } catch {
    localLines = items.value.map((line) => ({ ...line }))
  }

  if (localLines.length) {
    for (const line of localLines) {
      const idCtsp = Number(line.idChiTietSanPham)
      const qty = Math.max(1, Number(line.soLuong) || 1)
      if (!Number.isFinite(idCtsp) || idCtsp <= 0) continue
      try {
        await addGioHangItem({
          idKhachHang,
          idChiTietSanPham: idCtsp,
          soLuong: qty,
        })
      } catch {
        // Bỏ qua dòng lỗi (hết hàng / ngừng bán) — vẫn đồng bộ các dòng còn lại
      }
    }
  }

  clearLocal()
  loadedCustomerId = null
  return refreshCart({ force: true })
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
    const addQty = Math.max(1, Number(payload.soLuong) || 1)
    const idKhachHang = customerIdOrNull()
    const existingBefore = items.value.find((l) => l.idChiTietSanPham === payload.idChiTietSanPham)
    const currentQty = existingBefore ? Number(existingBefore.soLuong) || 0 : 0
    const probeLine = {
      ...(existingBefore || payload),
      soLuongTon: payload.soLuongTon ?? existingBefore?.soLuongTon,
    }
    const max = maxQtyFor(probeLine)
    const attempted = currentQty + addQty
    const willCap = attempted > max
    const reason = willCap ? capReasonFor(probeLine) : null

    if (!idKhachHang) {
      addLocalItem(payload)
      const line = items.value.find((l) => l.idChiTietSanPham === payload.idChiTietSanPham)
      return {
        items: items.value,
        capped: willCap,
        capReason: reason,
        line: lineSnapshot(line),
      }
    }

    const res = await addGioHangItem({
      idKhachHang,
      idChiTietSanPham: payload.idChiTietSanPham,
      soLuong: addQty,
    })
    applyCartResponse(res.data)
    const line = items.value.find((l) => l.idChiTietSanPham === payload.idChiTietSanPham)
    // Server chỉ kẹp theo tồn — nếu tồn > 15 mà attempted > 15, tự kẹp lại về 15
    if (line && Number(line.soLuong) > GIOI_HAN_MUA_LE) {
      await setQty(line.idChiTietSanPham, GIOI_HAN_MUA_LE)
    }
    const fresh = items.value.find((l) => l.idChiTietSanPham === payload.idChiTietSanPham)
    return {
      items: items.value,
      capped: willCap || (fresh && attempted > Number(fresh.soLuong)),
      capReason: reason || (willCap ? capReasonFor(fresh || probeLine) : null),
      line: lineSnapshot(fresh),
    }
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

  /**
   * @returns {{ status: 'ok'|'max', capReason: null|'retail'|'stock', line: object|null }}
   */
  async function increaseQty(idChiTietSanPham) {
    const line = items.value.find((l) => l.idChiTietSanPham === idChiTietSanPham)
    if (!line) return { status: 'ok', capReason: null, line: null }
    const max = maxQtyFor(line)
    if (line.soLuong >= max) {
      return { status: 'max', capReason: capReasonFor(line), line: lineSnapshot(line) }
    }
    await setQty(idChiTietSanPham, line.soLuong + 1)
    return { status: 'ok', capReason: null, line: lineSnapshot(line) }
  }

  /**
   * @returns {{ clamped: number, hitMin: boolean, hitMax: boolean, capReason: null|'retail'|'stock', line: object|null }}
   */
  async function setQty(idChiTietSanPham, soLuong) {
    const line = items.value.find((l) => l.idChiTietSanPham === idChiTietSanPham)
    if (!line) return { clamped: 1, hitMin: false, hitMax: false, capReason: null, line: null }
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
    const reason = hitMax ? capReasonFor(line) : null
    const idKhachHang = customerIdOrNull()
    if (!idKhachHang || !line.idChiTietGioHang) {
      line.soLuong = target
      saveLocal()
      return { clamped: target, hitMin, hitMax, capReason: reason, line: lineSnapshot(line) }
    }

    const res = await updateGioHangItem(idKhachHang, line.idChiTietGioHang, target)
    applyCartResponse(res.data)
    const fresh = items.value.find((l) => l.idChiTietSanPham === idChiTietSanPham)
    return {
      clamped: target,
      hitMin,
      hitMax,
      capReason: reason,
      line: lineSnapshot(fresh),
    }
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

  /** Xóa ngay SP đã mua khỏi badge, rồi sync lại từ server. */
  async function syncAfterCheckout(idsChiTietGioHang = []) {
    const idSet = new Set(
      (idsChiTietGioHang || []).map((id) => Number(id)).filter((id) => Number.isFinite(id) && id > 0),
    )
    if (idSet.size) {
      items.value = items.value.filter((line) => !idSet.has(Number(line.idChiTietGioHang)))
      if (!customerIdOrNull()) saveLocal()
    }
    return refreshCart({ force: true })
  }

  /**
   * Khách chưa đăng nhập: giỏ nằm ở localStorage nên xóa theo idChiTietSanPham (biến thể) đã mua.
   */
  async function syncAfterGuestCheckout(idsChiTietSanPham = []) {
    const idSet = new Set(
      (idsChiTietSanPham || []).map((id) => Number(id)).filter((id) => Number.isFinite(id) && id > 0),
    )
    if (idSet.size) {
      items.value = items.value.filter((line) => !idSet.has(Number(line.idChiTietSanPham)))
      saveLocal()
    }
    return refreshCart({ force: true })
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
    syncAfterCheckout,
    syncAfterGuestCheckout,
    syncCartAfterLogin,
    GIOI_HAN_MUA_LE,
  }
}

export { GIOI_HAN_MUA_LE }

export function getCartCount() {
  return items.value.reduce((sum, line) => sum + (line.soLuong || 0), 0)
}
