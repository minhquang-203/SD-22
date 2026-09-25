/**
 * Thông tin cửa hàng in trên hóa đơn bán lẻ (POS / In lại).
 * Chỉnh tại đây khi đổi địa chỉ / hotline / MST.
 */
export const storeInfo = {
  brand: 'SUNOVA',
  tagline: 'SUNOVA – Chống nắng & Chăm sóc da',
  address: '123 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh',
  hotline: '1900 1234',
  taxCode: '0312345678',
  website: 'sunova.vn',
  lookupBaseUrl: 'https://sunova.vn/tra-cuu',
  fanpageUrl: 'https://www.facebook.com/sunova.official',
  returnPolicy: 'Đổi trả trong 7 ngày khi còn nguyên tem & hóa đơn.',
  thankYou: 'Cảm ơn quý khách! Nhớ thoa lại kem chống nắng sau mỗi 2 giờ ☀',
  legalNote: 'Phiếu thanh toán — không thay thế hóa đơn điện tử (xuất theo yêu cầu)',
}

export function buildLookupUrl(maHoaDon) {
  if (!maHoaDon) return storeInfo.website
  return `${storeInfo.lookupBaseUrl}?ma=${encodeURIComponent(maHoaDon)}`
}
