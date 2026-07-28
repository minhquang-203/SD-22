import request from './request'

/** Admin: danh sách hoàn tiền */
export function fetchHoanTienList(trangThai) {
  return request.get('/hoan-tien', {
    params: trangThai ? { trangThai } : {},
  })
}

/**
 * Admin: hoàn tất hoàn tiền.
 * - VNPAY: JSON
 * - COD/chuyển khoản: multipart (mã GD + ghi chú + ảnh chứng từ)
 */
export function hoanTatHoanTien(id, payload = {}, files = []) {
  const isVnpay = String(payload.phuongThuc || '').toUpperCase() === 'VNPAY'
  const { phuongThuc: _pt, ...data } = payload
  if (isVnpay) {
    return request.post(`/hoan-tien/${id}/hoan-tat`, data)
  }
  const formData = new FormData()
  formData.append(
    'data',
    new Blob([JSON.stringify(data)], { type: 'application/json' }),
    'data.json',
  )
  ;(files || []).filter(Boolean).forEach((file) => formData.append('files', file))
  return request.post(`/hoan-tien/${id}/hoan-tat`, formData)
}

/** Admin: từ chối hoàn tiền */
export function tuChoiHoanTien(id, payload = {}) {
  return request.post(`/hoan-tien/${id}/tu-choi`, payload)
}
