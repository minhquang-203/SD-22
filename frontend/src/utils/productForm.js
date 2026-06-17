/** Gợi ý SKU: {MÃ_SP}-{DUNG_TÍCH} — chỉ điền khi ô SKU đang trống */
export function suggestSku(maSanPham, dungTichMl) {
  const ma = String(maSanPham || '').trim()
  if (!ma || dungTichMl == null || dungTichMl === '') return ''
  const vol = Number(dungTichMl)
  if (Number.isNaN(vol)) return ''
  const volText = Number.isInteger(vol) ? String(vol) : String(vol)
  return `${ma}-${volText}`
}

export function createEmptyProductForm() {
  return {
    maSanPham: '',
    ten: '',
    idThuongHieu: null,
    idDanhMuc: null,
    idDangSanPham: null,
    chiSoSpf: '',
    chiSoPa: '',
    loaiChongNang: '',
    khangNuoc: false,
    moTa: '',
    chiTiets: [],
    anhs: [],
    idLoaiDas: [],
    idCongDungs: [],
    idThanhPhans: [],
  }
}

export function detailToForm(detail, mauSacOptions = []) {
  const findMauSacId = (tenMauSac) => {
    if (!tenMauSac) return null
    const found = mauSacOptions.find((m) => m.ten === tenMauSac)
    return found?.id ?? null
  }

  return {
    maSanPham: detail.maSanPham || '',
    ten: detail.ten || '',
    idThuongHieu: detail.idThuongHieu ?? null,
    idDanhMuc: detail.idDanhMuc ?? null,
    idDangSanPham: detail.idDangSanPham ?? null,
    chiSoSpf: detail.chiSoSpf || '',
    chiSoPa: detail.chiSoPa || '',
    loaiChongNang: detail.loaiChongNang || '',
    khangNuoc: detail.khangNuoc ?? false,
    moTa: detail.moTa || '',
    chiTiets: (detail.chiTiets || []).map((ct) => ({
      id: ct.id,
      sku: ct.sku || '',
      idMauSac: findMauSacId(ct.tenMauSac),
      dungTichMl: ct.dungTichMl ?? null,
      giaBan: ct.giaBan ?? null,
    })),
    anhs: (detail.anhs || []).map((img) => ({
      url: img.url || '',
      file: null,
      previewUrl: null,
      laAnhChinh: img.laAnhChinh ?? false,
      thuTu: img.thuTu ?? 0,
    })),
    idLoaiDas: detail.idLoaiDas || [],
    idCongDungs: detail.idCongDungs || [],
    idThanhPhans: detail.idThanhPhans || [],
  }
}

export function resolveProductImageUrl(url) {
  if (!url) return ''
  if (url.startsWith('http') || url.startsWith('blob:') || url.startsWith('data:')) return url
  return url.startsWith('/') ? url : `/${url}`
}

function buildProductData(form) {
  const { anhs } = buildAnhPayload(form.anhs || [])
  return {
    maSanPham: form.maSanPham?.trim(),
    ten: form.ten?.trim(),
    idThuongHieu: form.idThuongHieu,
    idDanhMuc: form.idDanhMuc,
    idDangSanPham: form.idDangSanPham,
    chiSoSpf: form.chiSoSpf || null,
    chiSoPa: form.chiSoPa || null,
    loaiChongNang: form.loaiChongNang || null,
    khangNuoc: form.khangNuoc ?? false,
    moTa: form.moTa || null,
    chiTiets: (form.chiTiets || []).map((ct) => ({
      sku: ct.sku?.trim(),
      idMauSac: ct.idMauSac || null,
      dungTichMl: ct.dungTichMl ?? null,
      giaBan: ct.giaBan,
      soLuongTon: 0,
    })),
    anhs,
    idLoaiDas: form.idLoaiDas || [],
    idCongDungs: form.idCongDungs || [],
    idThanhPhans: form.idThanhPhans || [],
  }
}

export function formToPayload(form) {
  return buildProductData(form)
}

export function validateProductForm(form) {
  if (!form.maSanPham?.trim()) return 'Mã sản phẩm không được để trống'
  if (!form.ten?.trim()) return 'Tên sản phẩm không được để trống'
  if (!form.idThuongHieu) return 'Vui lòng chọn thương hiệu'
  if (!form.idDanhMuc) return 'Vui lòng chọn danh mục'
  if (!form.idDangSanPham) return 'Vui lòng chọn dạng sản phẩm'
  if (!form.chiTiets?.length) return 'Sản phẩm phải có ít nhất 1 biến thể (SKU)'
  for (const ct of form.chiTiets) {
    if (!ct.sku?.trim()) return 'SKU không được để trống'
    if (!ct.giaBan || Number(ct.giaBan) <= 0) return 'Giá bán phải lớn hơn 0'
  }
  return null
}

function buildAnhPayload(anhs) {
  const files = []
  const payload = anhs.map((img, index) => {
    if (img.file) {
      const fileIndex = files.length
      files.push(img.file)
      return {
        url: null,
        fileIndex,
        laAnhChinh: img.laAnhChinh ?? false,
        thuTu: img.thuTu ?? index,
      }
    }
    return {
      url: img.url?.trim() || null,
      laAnhChinh: img.laAnhChinh ?? false,
      thuTu: img.thuTu ?? index,
    }
  })
  return { anhs: payload, files }
}

/** Chuyển form sản phẩm sang FormData (multipart) gửi kèm file ảnh */
export function formToFormData(form) {
  const { files } = buildAnhPayload(form.anhs || [])
  const data = buildProductData(form)

  const formData = new FormData()
  formData.append(
    'data',
    new Blob([JSON.stringify(data)], { type: 'application/json' }),
    'data.json',
  )
  files.forEach((file) => formData.append('files', file))
  return formData
}
