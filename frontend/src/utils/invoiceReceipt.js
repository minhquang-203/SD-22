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
    tienGiamGia: line.tienGiamGia ?? line.giamGia ?? null,
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

  const loaiDon = raw.loaiDon || ''
  let hinhThuc = ''
  if (/TAI_QUAY|tai.?quay|pos/i.test(loaiDon)) hinhThuc = 'Tại quầy'
  else if (/ONLINE|online/i.test(loaiDon)) hinhThuc = 'Online'
  else if (loaiDon) hinhThuc = loaiDon

  return {
    id: raw.id,
    maHoaDon: raw.maHoaDon || '',
    ngayTao: raw.ngayTao,
    loaiDon,
    hinhThuc,
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
  const m = /^(\d{4})-(\d{2})-(\d{2})/.exec(s)
  if (m) return `${m[3]}/${m[2]}/${m[1]}`
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return s
  const dd = String(d.getDate()).padStart(2, '0')
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const yyyy = d.getFullYear()
  return `${dd}/${mm}/${yyyy}`
}

const CHU_SO = ['không', 'một', 'hai', 'ba', 'bốn', 'năm', 'sáu', 'bảy', 'tám', 'chín']

function docHangChuc(n, full) {
  const chuc = Math.floor(n / 10)
  const donvi = n % 10
  let s = ''
  if (chuc > 1) {
    s = `${CHU_SO[chuc]} mươi`
    if (donvi === 1) s += ' mốt'
    else if (donvi === 5) s += ' lăm'
    else if (donvi) s += ` ${CHU_SO[donvi]}`
  } else if (chuc === 1) {
    s = 'mười'
    if (donvi === 1) s += ' một'
    else if (donvi === 5) s += ' lăm'
    else if (donvi) s += ` ${CHU_SO[donvi]}`
  } else if (full && donvi) {
    s = `lẻ ${CHU_SO[donvi]}`
  } else if (donvi) {
    s = CHU_SO[donvi]
  }
  return s
}

function docBaSo(n, full) {
  const tram = Math.floor(n / 100)
  const du = n % 100
  let s = ''
  if (tram > 0) {
    s = `${CHU_SO[tram]} trăm`
    if (du) s += ` ${docHangChuc(du, true)}`
  } else {
    s = docHangChuc(du, full)
  }
  return s
}

/** Đọc số tiền VND thành chữ */
export function soTienBangChu(value) {
  let n = Math.round(Number(value) || 0)
  if (n === 0) return 'Không đồng'
  if (n < 0) return `Âm ${soTienBangChu(-n)}`

  const hang = ['', 'nghìn', 'triệu', 'tỷ', 'nghìn tỷ']
  const parts = []
  let i = 0
  let full = false
  while (n > 0 && i < hang.length) {
    const block = n % 1000
    if (block > 0) {
      const chunk = docBaSo(block, full)
      parts.unshift(hang[i] ? `${chunk} ${hang[i]}` : chunk)
    }
    n = Math.floor(n / 1000)
    i += 1
    full = true
  }
  let text = parts.join(' ').replace(/\s+/g, ' ').trim()
  text = text.charAt(0).toUpperCase() + text.slice(1)
  return `${text} đồng`
}
