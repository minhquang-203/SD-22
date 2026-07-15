import request from './request'

export function createOnlineCheckout(payload) {
  return request.post('/online/checkout', payload)
}

export function tinhGiaOnline(payload) {
  return request.post('/online/tinh-gia', payload)
}

export function fetchOnlineOrders(idKhachHang) {
  return request.get('/online/orders', { params: { idKhachHang } })
}

export function fetchOnlineOrderDetail(idKhachHang, idHoaDon) {
  return request.get(`/online/orders/${idHoaDon}`, { params: { idKhachHang } })
}

export function fetchCheckoutVouchers(keyword = '', page = 1, size = 20) {
  return request.get('/online/vouchers', {
    params: {
      keyword: keyword || undefined,
      page,
      size,
    },
  })
}
