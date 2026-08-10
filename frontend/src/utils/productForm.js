/**
 * Chuẩn hóa phần SKU: bỏ dấu, viết hoa, khoảng trắng → "-", chỉ giữ A-Z 0-9 và "-".
 */
export function slugSkuPart(text) {
  return String(text ?? '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/đ/g, 'd')
    .replace(/Đ/g, 'D')
    .toUpperCase()
    .replace(/[^A-Z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
}

/**
 * Phần màu trong SKU: viết liền, không dấu, không khoảng trắng.
 * VD: "Hồng nhạt" → HONGNHAT
 */
export function slugSkuColor(text) {
  return slugSkuPart(text).replace(/-/g, '')
}

/**
 * Base SKU theo quy chuẩn: [MÃ_SP]-[DUNG_TÍCH]ML(-[MÀU]).
 * VD: SP006-60ML, SP008-50ML-HONG
 */
export function buildSkuBase(maSanPham, dungTichMl, tenMauSac) {
  const ma = slugSkuPart(maSanPham)
  if (!ma || dungTichMl == null || dungTichMl === '') return ''
  const vol = Number(dungTichMl)
  if (Number.isNaN(vol) || vol <= 0) return ''
  const volText = Number.isInteger(vol) ? String(vol) : String(vol).replace(/\.0$/, '')
  let sku = `${ma}-${volText}ML`
  const mau = slugSkuColor(tenMauSac)
  if (mau) sku += `-${mau}`
  return sku
}

/**
 * Gợi ý SKU; nếu trùng trong danh sách existingSkus thì thêm hậu tố -2, -3, ...
 * @param {string[]} [existingSkus]
 */
export function suggestSku(maSanPham, dungTichMl, tenMauSac, existingSkus = []) {
  const base = buildSkuBase(maSanPham, dungTichMl, tenMauSac)
  if (!base) return ''
  const taken = new Set(
    (existingSkus || [])
      .map((s) => String(s || '').trim().toUpperCase())
      .filter(Boolean),
  )
  let candidate = base
  let n = 2
  while (taken.has(candidate)) {
    candidate = `${base}-${n}`
    n += 1
  }
  return candidate
}

/**
 * Nhãn gợi ý hiển thị (không lưu DB): [Tên SP] - [dung tích]ml(- [màu]).
 * VD: "Anessa Perfect UV Gel - 50ml - Hồng"
 */
export function suggestVariantLabel(tenSanPham, dungTichMl, tenMauSac) {
  const ten = String(tenSanPham || '').trim()
  if (!ten || dungTichMl == null || dungTichMl === '') return ''
  const vol = Number(dungTichMl)
  if (Number.isNaN(vol) || vol <= 0) return ''
  const volText = Number.isInteger(vol) ? String(vol) : String(vol).replace(/\.0$/, '')
  let label = `${ten} - ${volText}ml`
  const mau = String(tenMauSac || '').trim()
  if (mau) label += ` - ${mau}`
  return label
}

/** 4 mức PA cố định trên form */
export const PA_OPTIONS = ['PA+', 'PA++', 'PA+++', 'PA++++']

/** Bóc prefix SPF khỏi giá trị DB (SPF50+ → 50+; 50+ giữ nguyên). */
export function stripSpfPrefix(value) {
  const raw = String(value ?? '').trim()
  if (!raw) return ''
  return raw.replace(/^spf/i, '').trim()
}

/** Chỉ giữ số và dấu + cho phần nhập SPF. */
export function sanitizeSpfSuffix(value) {
  return String(value ?? '').replace(/[^0-9+]/g, '')
}

/**
 * Ghép SPF + phần nhập để lưu DB.
 * Tránh SPFSPF50+ nếu người dùng vẫn dán kèm SPF.
 */
export function composeSpfForSave(suffixOrFull) {
  const cleaned = sanitizeSpfSuffix(stripSpfPrefix(suffixOrFull))
  if (!cleaned) return null
  return `SPF${cleaned}`
}

/** Map PA DB → option dropdown; không khớp thì ''. */
export function normalizePaForSelect(value) {
  const raw = String(value ?? '').trim()
  if (!raw) return ''
  return PA_OPTIONS.includes(raw) ? raw : ''
}

export function createEmptyProductForm() {
  return {
    maSanPham: '',
    ten: '',
    idThuongHieu: null,
    idDanhMuc: null,
    idDangSanPham: null,
    /** Form chỉ giữ phần sau SPF (VD 50+); khi lưu mới ghép SPF */
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
    chiSoSpf: sanitizeSpfSuffix(stripSpfPrefix(detail.chiSoSpf)),
    chiSoPa: normalizePaForSelect(detail.chiSoPa),
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
      idMauSac: img.idMauSac ?? null,
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
    chiSoSpf: composeSpfForSave(form.chiSoSpf),
    chiSoPa: form.chiSoPa || null,
    loaiChongNang: form.loaiChongNang || null,
    khangNuoc: form.khangNuoc ?? false,
    moTa: form.moTa || null,
    chiTiets: (form.chiTiets || []).map((ct) => ({
      ...(ct.id != null ? { id: ct.id } : {}),
      sku: ct.sku?.trim(),
      idMauSac: ct.idMauSac || null,
      dungTichMl: ct.dungTichMl ?? null,
      giaBan: ct.giaBan,
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

/**
 * Validate form SP. Trả về null nếu OK, hoặc object lỗi:
 * { message, fields: { ten, ... }, chiTiets: { [index]: { sku, dungTichMl, giaBan } }, images, warning }
 */
export function validateProductForm(form) {
  const fields = {}
  const chiTiets = {}
  let images = ''
  let warning = ''

  const ten = String(form.ten ?? '').trim()
  if (!ten) fields.ten = 'Tên sản phẩm không được để trống'
  else if (ten.length < 2 || ten.length > 200) fields.ten = 'Tên sản phẩm phải từ 2 đến 200 ký tự'

  if (!form.idThuongHieu) fields.idThuongHieu = 'Vui lòng chọn thương hiệu'
  if (!form.idDanhMuc) fields.idDanhMuc = 'Vui lòng chọn danh mục'
  if (!form.idDangSanPham) fields.idDangSanPham = 'Vui lòng chọn dạng sản phẩm'

  const spfRaw = sanitizeSpfSuffix(form.chiSoSpf)
  if (!spfRaw) fields.chiSoSpf = 'Chỉ số SPF không được để trống'
  else if (/[^0-9+]/.test(spfRaw)) fields.chiSoSpf = 'Chỉ số SPF chỉ gồm số và dấu + (VD: 50+ hoặc 30)'

  if (!form.chiSoPa) fields.chiSoPa = 'Vui lòng chọn chỉ số PA'
  else if (!PA_OPTIONS.includes(form.chiSoPa)) fields.chiSoPa = 'Vui lòng chọn chỉ số PA hợp lệ'

  if (!form.loaiChongNang) fields.loaiChongNang = 'Vui lòng chọn loại chống nắng'
  else if (!['VAT_LY', 'HOA_HOC', 'LAI'].includes(form.loaiChongNang)) {
    fields.loaiChongNang = 'Loại chống nắng không hợp lệ'
  }

  if (!form.idLoaiDas?.length) {
    warning = 'Nên chọn ít nhất 1 loại da để gợi ý cá nhân hóa tốt hơn'
  }

  if (!form.chiTiets?.length) {
    fields.chiTiets = 'Sản phẩm phải có ít nhất 1 biến thể'
  } else {
    const seenSku = new Set()
    form.chiTiets.forEach((ct, index) => {
      const row = {}
      const sku = String(ct.sku ?? '').trim()
      if (!sku) row.sku = 'SKU không được để trống'
      else {
        const key = sku.toUpperCase()
        if (seenSku.has(key)) row.sku = `SKU [${sku}] bị trùng trong danh sách`
        else seenSku.add(key)
      }
      const vol = Number(ct.dungTichMl)
      if (ct.dungTichMl == null || ct.dungTichMl === '' || Number.isNaN(vol) || vol <= 0) {
        row.dungTichMl = 'Dung tích phải là số lớn hơn 0'
      }
      const gia = Number(ct.giaBan)
      if (!ct.giaBan || Number.isNaN(gia) || gia <= 0) {
        row.giaBan = 'Giá bán phải lớn hơn 0'
      }
      if (Object.keys(row).length) chiTiets[index] = row
    })
  }

  const ALLOWED = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp']
  const MAX = 5 * 1024 * 1024
  for (const img of form.anhs || []) {
    if (img.file) {
      const type = (img.file.type || '').toLowerCase()
      if (!ALLOWED.includes(type) && !type.startsWith('image/jpeg')) {
        images = 'Chỉ chấp nhận ảnh JPG, PNG hoặc WEBP'
        break
      }
      if (img.file.size > MAX) {
        images = 'Ảnh vượt quá 5MB'
        break
      }
    }
  }
  if (!images && !(form.anhs || []).length) {
    warning = warning
      ? warning + '. Nên thêm ít nhất 1 ảnh sản phẩm'
      : 'Nên thêm ít nhất 1 ảnh sản phẩm (khuyến nghị)'
  } else if (!images && (form.anhs || []).length && !(form.anhs || []).some((a) => a.laAnhChinh)) {
    warning = warning
      ? warning + '. Nên chọn 1 ảnh chính'
      : 'Nên chọn 1 ảnh chính'
  }

  const hasField = Object.keys(fields).length > 0
  const hasCt = Object.keys(chiTiets).length > 0
  if (!hasField && !hasCt && !images) {
    return warning ? { ok: true, warning } : null
  }

  const firstMsg =
    fields.ten ||
    fields.idThuongHieu ||
    fields.idDanhMuc ||
    fields.idDangSanPham ||
    fields.chiSoSpf ||
    fields.chiSoPa ||
    fields.loaiChongNang ||
    fields.chiTiets ||
    (hasCt ? 'Vui lòng kiểm tra thông tin biến thể' : null) ||
    images ||
    'Vui lòng kiểm tra lại thông tin'

  return { ok: false, message: firstMsg, fields, chiTiets, images, warning }
}

/** @deprecated dùng validateProductForm (trả object). Giữ tương thích nếu chỗ nào còn expect string. */
export function validateProductFormMessage(form) {
  const r = validateProductForm(form)
  if (!r || r.ok) return null
  return r.message
}

function buildAnhPayload(anhs) {
  const files = []
  const payload = anhs.map((img, index) => {
    const idMauSac =
      img.idMauSac == null || img.idMauSac === '' || Number.isNaN(Number(img.idMauSac))
        ? null
        : Number(img.idMauSac)
    if (img.file) {
      const fileIndex = files.length
      files.push(img.file)
      return {
        url: null,
        fileIndex,
        laAnhChinh: img.laAnhChinh ?? false,
        thuTu: img.thuTu ?? index,
        idMauSac,
      }
    }
    return {
      url: img.url?.trim() || null,
      laAnhChinh: img.laAnhChinh ?? false,
      thuTu: img.thuTu ?? index,
      idMauSac,
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
