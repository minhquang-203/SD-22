import request from './request'

export function fetchAllProducts(options = {}) {
  const params = {}
  if (options.excludeKhuyenMai) params.excludeKhuyenMai = true
  return request.get('/san-pham', { params })
}

export function fetchSaleProducts() {
  return request.get('/san-pham/khuyen-mai')
}

export function fetchProductsPage(pageNo, pageSize) {
  return request.get('/san-pham/padding', { params: { pageNo, pageSize } })
}

export function searchProducts(keyword, options = {}) {
  const params = { keyword }
  if (options.excludeKhuyenMai) params.excludeKhuyenMai = true
  return request.get('/san-pham/tim', { params })
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
