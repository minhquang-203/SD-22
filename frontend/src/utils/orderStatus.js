/** Nhãn trạng thái đơn — storefront */
export function orderStatusLabel(trangThai, fallback = '') {
  const map = {
    CHO_XAC_NHAN: 'Chờ xác nhận',
    DA_XAC_NHAN: 'Chờ xác nhận',
    DANG_CHUAN_BI: 'Chờ xác nhận',
    DANG_GIAO: 'Đang giao',
    HOAN_THANH: 'Đã giao',
    DA_HUY: 'Đã hủy',
  }
  return map[trangThai] || fallback || trangThai || '—'
}

export function orderStatusClass(trangThai) {
  const map = {
    CHO_XAC_NHAN: 'sf-order-badge--wait',
    DA_XAC_NHAN: 'sf-order-badge--wait',
    DANG_CHUAN_BI: 'sf-order-badge--wait',
    DANG_GIAO: 'sf-order-badge--ship',
    HOAN_THANH: 'sf-order-badge--done',
    DA_HUY: 'sf-order-badge--cancel',
  }
  return map[trangThai] || 'sf-order-badge--wait'
}

export function formatOrderDate(value) {
  if (!value) return '—'
  return new Date(value).toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}
