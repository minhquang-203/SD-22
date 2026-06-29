import request from './request'

/** Danh sách đầy đủ — màn quản lý nhân viên (chỉ QUAN_LY) */
export function getNhanVienDanhSach() {
  return request.get('/nhan-vien')
}

export function getNhanVienDetail(id) {
  return request.get(`/nhan-vien/${id}`)
}

export function createNhanVien(payload) {
  return request.post('/nhan-vien', payload)
}

export function updateNhanVien(id, payload) {
  return request.put(`/nhan-vien/${id}`, payload)
}

export function updateNhanVienStatus(id, trangThai) {
  return request.patch(`/nhan-vien/${id}/trang-thai`, { trangThai })
}

export function datLaiMatKhauNhanVien(id, payload) {
  return request.put(`/nhan-vien/${id}/dat-lai-mat-khau`, payload)
}
