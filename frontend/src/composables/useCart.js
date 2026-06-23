import { computed, ref } from 'vue'

const STORAGE_KEY = 'sunova_cart'
const MAX_PER_LINE = 10

const items = ref([])

function load() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    const parsed = raw ? JSON.parse(raw) : []
    items.value = Array.isArray(parsed)
      ? parsed.map((line) => ({
          ...line,
          selected: line.selected !== false,
          soLuongTon: Number(line.soLuongTon) || 0,
        }))
      : []
  } catch {
    items.value = []
  }
}

function save() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(items.value))
}

load()

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

export function useCart() {
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

  function addItem(payload) {
    const addQty = Math.max(1, payload.soLuong || 1)
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
    save()
  }

  function removeItem(idChiTietSanPham) {
    items.value = items.value.filter((l) => l.idChiTietSanPham !== idChiTietSanPham)
    save()
  }

  function removeSelected() {
    items.value = items.value.filter((l) => !l.selected)
    save()
  }

  function toggleSelect(idChiTietSanPham) {
    const line = items.value.find((l) => l.idChiTietSanPham === idChiTietSanPham)
    if (line) {
      line.selected = !line.selected
      save()
    }
  }

  function setSelectAll(value) {
    items.value.forEach((l) => {
      l.selected = value
    })
    save()
  }

  /** @returns {'ok'|'min'|'max'} */
  function decreaseQty(idChiTietSanPham) {
    const line = items.value.find((l) => l.idChiTietSanPham === idChiTietSanPham)
    if (!line) return 'ok'
    if (line.soLuong <= 1) return 'min'
    line.soLuong -= 1
    save()
    return 'ok'
  }

  /** @returns {'ok'|'max'} */
  function increaseQty(idChiTietSanPham) {
    const line = items.value.find((l) => l.idChiTietSanPham === idChiTietSanPham)
    if (!line) return 'ok'
    const max = maxQtyFor(line)
    if (line.soLuong >= max) return 'max'
    line.soLuong += 1
    save()
    return 'ok'
  }

  /**
   * @returns {{ clamped: number, hitMin: boolean, hitMax: boolean }}
   */
  function setQty(idChiTietSanPham, soLuong) {
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
    line.soLuong = target
    save()
    return { clamped: target, hitMin, hitMax }
  }

  function clearCart() {
    items.value = []
    save()
  }

  return {
    items,
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
  }
}

export function getCartCount() {
  load()
  return items.value.reduce((sum, line) => sum + (line.soLuong || 0), 0)
}
