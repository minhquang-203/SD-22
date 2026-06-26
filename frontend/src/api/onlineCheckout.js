import request from './request'

export function createOnlineCheckout(payload) {
  return request.post('/online/checkout', payload)
}

export function fetchOnlineOrders(idKhachHang) {
  return request.get('/online/orders', { params: { idKhachHang } })
}

export function fetchOnlineOrderDetail(idKhachHang, idHoaDon) {
  return request.get(`/online/orders/${idHoaDon}`, { params: { idKhachHang } })
}
