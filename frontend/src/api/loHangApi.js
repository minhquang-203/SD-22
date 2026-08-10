import request from './request'

export function getLoHangByChiTiet(idChiTietSanPham) {
  return request.get(`/lo-hang/chi-tiet-san-pham/${idChiTietSanPham}`)
}

/** POS: lô còn hàng, sort HSD tăng dần */
export function getLoHangConHangTheoBienThe(idChiTietSanPham) {
  return request.get(`/lo-hang/theo-bien-the/${idChiTietSanPham}`)
}

export function nhapLoHang(payload) {
  return request.post('/lo-hang', payload)
}

export function capNhatLoHang(id, payload) {
  return request.put(`/lo-hang/${id}`, payload)
}

export function xoaLoHang(id) {
  return request.delete(`/lo-hang/${id}`)
}
