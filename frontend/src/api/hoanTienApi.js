import request from './request'

/** Admin: danh sách hoàn tiền */
export function fetchHoanTienList(trangThai) {
  return request.get('/hoan-tien', {
    params: trangThai ? { trangThai } : {},
  })
}

/** Admin: hoàn tất hoàn tiền */
export function hoanTatHoanTien(id, payload = {}) {
  return request.post(`/hoan-tien/${id}/hoan-tat`, payload)
}

/** Admin: từ chối hoàn tiền */
export function tuChoiHoanTien(id, payload = {}) {
  return request.post(`/hoan-tien/${id}/tu-choi`, payload)
}
