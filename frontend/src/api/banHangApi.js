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

/** Một dòng trong thanh toán kết hợp POS (Tiền mặt + Chuyển khoản). */
export const buildThanhToanKetHopItem = (item) => ({
  idPhuongThucThanhToan: item.idPhuongThucThanhToan,
  soTien: item.soTien,
  soTienKhachDua: item.soTienKhachDua ?? null,
  maGiaoDich: item.maGiaoDich ?? null,
})

export const taoDonTaiQuay = (payload) =>
  request.post('/ban-hang/tai-quay', payload)

export const tinhGiaTaiQuay = (payload) =>
  request.post('/ban-hang/tinh-gia', payload)

/** Mã giảm giá khả dụng tại quầy (đã loại FREE_SHIP). */
export const fetchPosVouchers = (keyword = '', page = 1, size = 20) =>
  request.get('/ban-hang/vouchers', {
    params: {
      keyword: keyword || undefined,
      page,
      size,
    },
  })

export const kiemTraThanhToanPos = (idHoaDon) =>
  request.get(`/ban-hang/tai-quay/${idHoaDon}/thanh-toan`)

export const huyThanhToanPos = (idHoaDon) =>
  request.post(`/ban-hang/tai-quay/${idHoaDon}/huy-thanh-toan`)

/** Hoàn tất thủ công khi chưa có IPN VNPAY */
export const hoanTatThanhToanPos = (idHoaDon) =>
  request.post(`/ban-hang/tai-quay/${idHoaDon}/hoan-tat-thanh-toan`)

export const giuDon = (payload) => request.post('/ban-hang/cho', payload)

export const dsDonCho = () => request.get('/ban-hang/cho')

export const layDonCho = (id) => request.get(`/ban-hang/cho/${id}`)

export const huyDonCho = (id) => request.delete(`/ban-hang/cho/${id}`)
