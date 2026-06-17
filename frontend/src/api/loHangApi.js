import request from './request'

export function getLoHangByChiTiet(idChiTietSanPham) {
  return request.get(`/lo-hang/chi-tiet-san-pham/${idChiTietSanPham}`)
}

export function nhapLoHang(payload) {
  return request.post('/lo-hang', payload)
}
