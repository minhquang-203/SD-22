/**
 * Chọn gallery ảnh theo màu SKU.
 *
 * Khái niệm tách biệt:
 * - main: laAnhChinh (chỉ ảnh đại diện mặc định của PRODUCT)
 * - color: idMauSac === colorId của SKU đang chọn
 * - common: idMauSac == null ("Dùng chung")
 *
 * Đã chọn SKU có màu: color → common → main → first
 * Chưa chọn màu: main → common → first
 */

function toId(value) {
  if (value == null || value === '') return null
  const n = Number(value)
  return Number.isFinite(n) ? n : null
}

function sortByThuTu(list) {
  return [...list].sort((a, b) => (a.thuTu ?? 0) - (b.thuTu ?? 0) || (a.id ?? 0) - (b.id ?? 0))
}

function sameColor(img, colorId) {
  const imgColor = toId(img?.idMauSac)
  return imgColor != null && colorId != null && imgColor === colorId
}

function isCommon(img) {
  return toId(img?.idMauSac) == null
}

function isMain(img) {
  return img?.laAnhChinh === true || img?.laAnhChinh === 1 || img?.laAnhChinh === 'true'
}

/**
 * @param {Array} anhs - product.anhs từ API
 * @param {number|string|null|undefined} colorId - idMauSac của SKU đang chọn; null = chưa chọn màu
 * @returns {Array} danh sách object ảnh (giữ nguyên phần tử gốc), đã sắp xếp
 */
export function resolveGalleryAnhs(anhs, colorId) {
  const all = (anhs || []).filter((a) => a && a.url)
  if (!all.length) return []

  const cid = toId(colorId)

  if (cid != null) {
    const byColor = sortByThuTu(all.filter((a) => sameColor(a, cid)))
    if (byColor.length) return byColor

    const common = sortByThuTu(all.filter(isCommon))
    if (common.length) return common

    const main = sortByThuTu(all.filter(isMain))
    if (main.length) return main

    return [sortByThuTu(all)[0]]
  }

  const main = sortByThuTu(all.filter(isMain))
  if (main.length) return main

  const common = sortByThuTu(all.filter(isCommon))
  if (common.length) return common

  return [sortByThuTu(all)[0]]
}

/** @returns {string[]} URL ảnh cho gallery */
export function resolveGalleryUrls(anhs, colorId, fallbackAnhChinhUrl) {
  const list = resolveGalleryAnhs(anhs, colorId)
  const urls = list.map((a) => a.url).filter(Boolean)
  if (!urls.length && fallbackAnhChinhUrl) urls.push(fallbackAnhChinhUrl)
  return urls
}
