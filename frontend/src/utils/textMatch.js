/** Chuẩn hóa chuỗi để so khớp tìm kiếm: bỏ dấu, không phân biệt hoa/thường */
export function normalizeSearchText(text) {
  return String(text ?? '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/đ/g, 'd')
    .replace(/Đ/g, 'D')
    .toLowerCase()
    .trim()
}

/** Kiểm tra label có khớp keyword (substring, không dấu) */
export function matchesSearch(label, keyword) {
  const q = normalizeSearchText(keyword)
  if (!q) return true
  return normalizeSearchText(label).includes(q)
}
