import request from './request'

export function fetchAllProducts() {
  return request.get('/san-pham')
}

export function fetchProductsPage(pageNo, pageSize) {
  return request.get('/san-pham/padding', { params: { pageNo, pageSize } })
}

export function searchProducts(keyword) {
  return request.get('/san-pham/tim', { params: { keyword } })
}

export function fetchProductDetail(id) {
  return request.get('/san-pham/detail', { params: { id } })
}

export function fetchProductReviews(idSanPham) {
  return request.get('/danh-gia/san-pham', { params: { idSanPham } })
}

export function fetchDanhMucList() {
  return request.get('/danh-muc')
}

export function fetchThuongHieuList() {
  return request.get('/thuong-hieu')
}

export function fetchCongDungList() {
  return request.get('/cong-dung')
}

export function fetchThanhPhanList() {
  return request.get('/thanh-phan')
}

export function fetchDangSanPhamList() {
  return request.get('/dang-san-pham')
}
