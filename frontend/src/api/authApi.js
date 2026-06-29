import request from './request'

export function dangKyKhach(payload) {
  return request.post('/auth/khach/dang-ky', payload)
}

export function dangNhapKhach(payload) {
  return request.post('/auth/khach/dang-nhap', payload)
}

export function quenMatKhauKhach(payload) {
  return request.post('/auth/khach/quen-mat-khau', payload)
}

export function datLaiMatKhauKhach(payload) {
  return request.post('/auth/khach/dat-lai-mat-khau', payload)
}

export function dangNhapNhanVien(payload) {
  return request.post('/auth/nhan-vien/dang-nhap', payload)
}
