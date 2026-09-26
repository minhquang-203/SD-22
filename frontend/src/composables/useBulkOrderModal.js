import { ref } from 'vue'
import { GIOI_HAN_MUA_LE } from '@/constants/cartLimits'

const visible = ref(false)
const product = ref(null)

/**
 * @param {object} info
 * @param {number} info.idChiTietSanPham
 * @param {string} info.tenSanPham
 * @param {string} [info.anhUrl]
 * @param {string} [info.tenMauSac]
 * @param {number|string} [info.dungTichMl]
 * @param {string} [info.sku]
 * @param {number} [info.soLuongTon]
 * @param {number} [info.soLuongMongMuon] mặc định 16
 */
export function openBulkOrderModal(info) {
  if (!info?.idChiTietSanPham) return

  const stock = Number(info.soLuongTon)
  // Tồn ≤ giới hạn mua lẻ → không mở popup tư vấn số lượng lớn
  if (Number.isFinite(stock) && stock > 0 && stock <= GIOI_HAN_MUA_LE) return
  if (Number.isFinite(stock) && stock === 0) return

  const minQty = GIOI_HAN_MUA_LE + 1
  let want = Math.max(minQty, Number(info.soLuongMongMuon) || minQty)
  if (Number.isFinite(stock) && stock >= minQty) {
    want = Math.min(want, stock)
  }

  product.value = {
    ...info,
    soLuongTon: Number.isFinite(stock) ? stock : info.soLuongTon,
    soLuongMongMuon: want,
  }
  visible.value = true
}

export function closeBulkOrderModal() {
  visible.value = false
}

export function useBulkOrderModal() {
  return {
    visible,
    product,
    openBulkOrderModal,
    closeBulkOrderModal,
  }
}
