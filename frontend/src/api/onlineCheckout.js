import request from './request'

export function createOnlineCheckout(payload) {
  return request.post('/online/checkout', payload)
}

export function tinhGiaOnline(payload) {
  return request.post('/online/tinh-gia', payload)
}

export function fetchOnlineOrders() {
  return request.get('/online/orders')
}

export function fetchOnlineOrderDetail(idHoaDon) {
  return request.get(`/online/orders/${idHoaDon}`)
}

export function cancelOnlineOrder(idHoaDon, payload = {}) {
  return request.patch(`/online/orders/${idHoaDon}/cancel`, payload)
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
