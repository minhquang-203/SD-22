import request from './request'

// —— Storefront: khách đang đăng nhập ——
export function fetchKhachToi() {
  return request.get('/khach-hang/toi')
}

export function updateKhachToi(payload) {
  return request.put('/khach-hang/toi', payload)
}

export function doiMatKhauToi(payload) {
  return request.put('/khach-hang/toi/doi-mat-khau', payload)
}

// —— Admin: quản lý khách hàng ——
export function getCustomers() {
  return request.get('/khach-hang')
}

export function searchCustomers(keyword) {
  return request.get('/khach-hang/tim', { params: { keyword } })
}

export function getCustomerDetail(id) {
  return request.get('/khach-hang/detail', { params: { id } })
}

export function updateCustomerStatus(id, trangThai) {
  return request.put('/khach-hang/trang-thai', null, { params: { id, trangThai } })
}
