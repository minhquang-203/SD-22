/** Định dạng tiền VND cho storefront */
export function formatVND(value) {
  const num = Number(value)
  if (Number.isNaN(num)) return '—'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0,
  }).format(num)
}

/** Hiển thị khoảng giá min–max */
export function formatPriceRange(giaMin, giaMax) {
  const min = Number(giaMin)
  const max = Number(giaMax)
  if (Number.isNaN(min) && Number.isNaN(max)) return 'Liên hệ'
  if (!Number.isNaN(min) && !Number.isNaN(max) && min !== max) {
    return `${formatVND(min)} – ${formatVND(max)}`
  }
  return formatVND(!Number.isNaN(min) ? min : max)
}

/** Hiển thị phần trăm giảm giá */
export function formatDiscountPercent(value) {
  const num = Number(value)
  if (Number.isNaN(num) || num <= 0) return ''
  return Number.isInteger(num) ? `-${num}%` : `-${num.toFixed(1)}%`
}
