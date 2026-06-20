export function formatCurrency(value) {
  const num = Number(value || 0)
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(num)
}

export function formatDate(value) {
  if (!value) return '—'
  return new Date(value).toLocaleDateString('vi-VN')
}

/** Hiển thị HSD dạng mm/yyyy (POS, lô hàng) */
export function formatMonthYear(value) {
  if (!value) return ''
  const parts = String(value).split('-')
  if (parts.length >= 2) {
    return `${parts[1].padStart(2, '0')}/${parts[0]}`
  }
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return ''
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  return `${mm}/${d.getFullYear()}`
}
