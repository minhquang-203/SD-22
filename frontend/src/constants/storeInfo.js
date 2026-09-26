/**
 * Nguồn duy nhất thông tin cửa hàng SUNOVA (hóa đơn in, footer, liên hệ, B2B…).
 * Chỉnh tại đây — không ghi cứng địa chỉ/hotline ở component.
 */
export const storeInfo = {
  ten: 'SUNOVA – Chống nắng & Chăm sóc da',
  diaChi: 'Tầng 5, 80 Phố Chùa Bộc, Phường Kim Liên, TP. Hà Nội',
  hotline: '1900 6868',
  email: 'cskh@sunova.vn',
  mst: '0109876543',
  website: 'sunova.vn',

  /** Alias giữ tương thích InvoiceReceipt / code cũ */
  brand: 'SUNOVA',
  get tagline() {
    return this.ten
  },
  get address() {
    return this.diaChi
  },
  get taxCode() {
    return this.mst
  },

  lookupBaseUrl: 'https://sunova.vn/tra-cuu',
  fanpageUrl: 'https://www.facebook.com/sunova.official',
  hours: 'Thứ 2 – Thứ 7: 8:00 – 21:00 · Chủ nhật: 9:00 – 18:00',
  returnPolicy: 'Đổi trả trong 7 ngày khi còn nguyên tem và hóa đơn.',
  thankYou: 'Cảm ơn quý khách! Nhớ thoa lại kem chống nắng sau mỗi 2 giờ.',
  legalNote: 'Phiếu thanh toán — không thay thế hóa đơn điện tử (xuất theo yêu cầu).',

  /** Liên hệ tư vấn mua số lượng lớn (B2B) */
  bulkOrder: {
    hotline: '1900 6868',
    hotlineBranch: 'nhánh 2',
    email: 'b2b@sunova.vn',
    hours: '8:00–21:00',
  },
}

export function buildLookupUrl(maHoaDon) {
  if (!maHoaDon) return storeInfo.website
  return `${storeInfo.lookupBaseUrl}?ma=${encodeURIComponent(maHoaDon)}`
}
