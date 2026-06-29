import request from './request'

export const getSanPhamBan = (keyword = '', page = 0) =>
  request.get('/ban-hang/san-pham', { params: { keyword, page } })

/** @deprecated dùng getSanPhamBan */
export const timSanPham = (keyword) =>
  request.get('/ban-hang/san-pham/tim', { params: { keyword } })

export const getPhuongThuc = () => request.get('/phuong-thuc-thanh-toan')

export const timKhachTheoSdt = (sdt) =>
  request.get('/khach-hang/theo-sdt', { params: { sdt } })

export const taoKhachNhanh = (payload) =>
  request.post('/khach-hang/tao-nhanh', payload)

export const taoDonTaiQuay = (payload) =>
  request.post('/ban-hang/tai-quay', payload)

export const giuDon = (payload) => request.post('/ban-hang/cho', payload)

export const dsDonCho = () => request.get('/ban-hang/cho')

export const layDonCho = (id) => request.get(`/ban-hang/cho/${id}`)

export const huyDonCho = (id) => request.delete(`/ban-hang/cho/${id}`)
