/**
 * Chuẩn hóa dữ liệu hóa đơn từ POS (BanHangHoaDonResponse)
 * hoặc chi tiết admin (HoaDonDetailResponse) cho InvoiceReceipt.
 */
export function normalizeInvoice(raw) {
  if (!raw) return null

  const items = (raw.items || raw.chiTiets || []).map((line) => ({
    id: line.id,
    sku: line.sku || '',
    tenSanPham: line.tenSanPham || 'Sản phẩm',
    bienThe: line.bienThe || '',
    soLuong: line.soLuong ?? 0,
    donGia: line.donGia ?? 0,
    thanhTien: line.thanhTien ?? 0,
    loHangs: (line.loHangs || []).map((lo) => ({
      soLo: lo.soLo || lo.maLo || '',
      hanSuDung: lo.hanSuDung || null,
      soLuong: lo.soLuongDaBan ?? lo.soLuong ?? null,
    })),
  }))

  let danhSachThanhToan = raw.danhSachThanhToan || []
  if ((!danhSachThanhToan || !danhSachThanhToan.length) && raw.tenPhuongThucThanhToan) {
    danhSachThanhToan = [
      {
        tenPhuongThucThanhToan: raw.tenPhuongThucThanhToan,
        maPhuongThucThanhToan: raw.maPhuongThucThanhToan || '',
        soTien: raw.thanhTien,
        maGiaoDich: raw.maGiaoDich || null,
        soTienKhachDua: raw.soTienKhachDua,
        tienThua: raw.tienThua,
      },
    ]
  }

  const idKhachHang = raw.idKhachHang ?? null
  let diemCong = raw.diemCong
  if (diemCong == null && idKhachHang != null && Number(raw.thanhTien) > 0) {
    diemCong = Math.floor(Number(raw.thanhTien) / 1000)
  }

  return {
    id: raw.id,
    maHoaDon: raw.maHoaDon || '',
    ngayTao: raw.ngayTao,
    tenNhanVien: raw.tenNhanVien || '',
    tenKhachHang: raw.tenKhachHang || 'Khách lẻ',
    soDienThoaiKhachHang: raw.soDienThoaiKhachHang || '',
    idKhachHang,
    items,
    tongTien: raw.tongTien ?? 0,
    tienGiamGia: raw.tienGiamGia ?? 0,
    maPhieuGiamGia: raw.maPhieuGiamGia || '',
    thanhTien: raw.thanhTien ?? 0,
    soTienKhachDua: raw.soTienKhachDua,
    tienThua: raw.tienThua,
    tenPhuongThucThanhToan: raw.tenPhuongThucThanhToan || '',
    danhSachThanhToan,
    diemCong: diemCong && diemCong > 0 ? diemCong : null,
    diemTichLuySau: raw.diemTichLuySau ?? null,
    maGiaoDich: raw.maGiaoDich || null,
  }
}

/** Định dạng tiền hóa đơn: 1.250.000đ */
export function formatReceiptMoney(value) {
  if (value == null || value === '') return ''
  const num = Math.round(Number(value))
  if (Number.isNaN(num)) return ''
  return `${num.toLocaleString('vi-VN')}đ`
}

export function formatReceiptDateTime(value) {
  if (!value) return ''
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  const dd = String(d.getDate()).padStart(2, '0')
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const yyyy = d.getFullYear()
  const hh = String(d.getHours()).padStart(2, '0')
  const mi = String(d.getMinutes()).padStart(2, '0')
  return `${dd}/${mm}/${yyyy} ${hh}:${mi}`
}

export function formatReceiptDate(value) {
  if (!value) return ''
  const s = String(value)
  // LocalDate: yyyy-mm-dd
  const m = /^(\d{4})-(\d{2})-(\d{2})/.exec(s)
  if (m) return `${m[3]}/${m[2]}/${m[1]}`
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return s
  const dd = String(d.getDate()).padStart(2, '0')
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const yyyy = d.getFullYear()
  return `${dd}/${mm}/${yyyy}`
}
