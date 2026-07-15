/** Nhãn / class trạng thái trả hàng & hoàn tiền */

export function traHangStatusLabel(trangThai, fallback = '') {
  const map = {
    CHO_DUYET: 'Chờ duyệt',
    DA_DUYET: 'Đã duyệt',
    TU_CHOI: 'Từ chối',
    DANG_HOAN_HANG: 'Đang hoàn hàng',
    DA_NHAN_HANG: 'Đã nhận hàng',
    HOAN_TAT: 'Hoàn tất',
  }
  return map[trangThai] || fallback || trangThai || '—'
}

export function traHangStatusClass(trangThai) {
  const map = {
    CHO_DUYET: 'sf-order-badge--wait',
    DA_DUYET: 'sf-order-badge--ship',
    TU_CHOI: 'sf-order-badge--cancel',
    DANG_HOAN_HANG: 'sf-order-badge--ship',
    DA_NHAN_HANG: 'sf-order-badge--done',
    HOAN_TAT: 'sf-order-badge--done',
  }
  return map[trangThai] || 'sf-order-badge--wait'
}

export function hoanTienStatusLabel(trangThai, fallback = '') {
  const map = {
    CHO_XU_LY: 'Chờ xử lý',
    DA_HOAN: 'Đã hoàn tiền',
    TU_CHOI: 'Từ chối',
  }
  return map[trangThai] || fallback || trangThai || '—'
}

export function hoanTienStatusClass(trangThai) {
  const map = {
    CHO_XU_LY: 'sf-order-badge--wait',
    DA_HOAN: 'sf-order-badge--done',
    TU_CHOI: 'sf-order-badge--cancel',
  }
  return map[trangThai] || 'sf-order-badge--wait'
}

export function loaiHoanTienLabel(loai, fallback = '') {
  const map = {
    HUY_DON: 'Hủy đơn đã thanh toán',
    TRA_HANG: 'Trả hàng sau khi nhận',
  }
  return map[loai] || fallback || loai || '—'
}

/** Admin badge tone */
export function traHangStatusTone(trangThai) {
  if (trangThai === 'HOAN_TAT' || trangThai === 'DA_NHAN_HANG') return 'success'
  if (trangThai === 'TU_CHOI') return 'danger'
  if (trangThai === 'CHO_DUYET') return 'warning'
  if (trangThai === 'DANG_HOAN_HANG' || trangThai === 'DA_DUYET') return 'info'
  return 'neutral'
}

export function hoanTienStatusTone(trangThai) {
  if (trangThai === 'DA_HOAN') return 'success'
  if (trangThai === 'TU_CHOI') return 'danger'
  if (trangThai === 'CHO_XU_LY') return 'warning'
  return 'neutral'
}
