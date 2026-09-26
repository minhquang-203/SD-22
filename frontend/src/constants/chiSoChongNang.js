/**
 * Chuẩn chỉ số chống nắng.
 * Định dạng lưu DB không đổi: "SPF46", "SPF50+", "PA+++".
 */

/**
 * Danh sách SPF chọn trên form admin (label hiển thị → value gửi backend).
 * Thứ tự tăng dần theo yêu cầu thị trường.
 */
export const SPF_OPTIONS = [
  { label: 'SPF 15', value: 'SPF15' },
  { label: 'SPF 20', value: 'SPF20' },
  { label: 'SPF 25', value: 'SPF25' },
  { label: 'SPF 30', value: 'SPF30' },
  { label: 'SPF 35', value: 'SPF35' },
  { label: 'SPF 40', value: 'SPF40' },
  { label: 'SPF 45', value: 'SPF45' },
  { label: 'SPF 46', value: 'SPF46' },
  { label: 'SPF 50', value: 'SPF50' },
  { label: 'SPF 50+', value: 'SPF50+' },
  { label: 'SPF 60', value: 'SPF60' },
  { label: 'SPF 70', value: 'SPF70' },
  { label: 'SPF 100', value: 'SPF100' },
]

/** @deprecated giữ tương thích bộ lọc cũ */
export const SPF_CHUAN = [
  { value: 'SPF6', suffix: '6', label: 'SPF 6' },
  { value: 'SPF10', suffix: '10', label: 'SPF 10' },
  { value: 'SPF15', suffix: '15', label: 'SPF 15' },
  { value: 'SPF20', suffix: '20', label: 'SPF 20' },
  { value: 'SPF25', suffix: '25', label: 'SPF 25' },
  { value: 'SPF30', suffix: '30', label: 'SPF 30' },
  { value: 'SPF50', suffix: '50', label: 'SPF 50' },
  { value: 'SPF50+', suffix: '50+', label: 'SPF 50+' },
]

export const SPF_KHAC = '__KHAC__'

/**
 * 4 mức bảo vệ — dùng cho bộ lọc khách.
 * Rất cao: có dấu + hoặc số > 50 (60, 70, 100…).
 * Cao: 30–50 (kể cả số lẻ như 46).
 */
export const MUC_BAO_VE = [
  {
    key: 'rat_cao',
    label: 'Rất cao (SPF 50+)',
    shortLabel: 'Rất cao',
  },
  {
    key: 'cao',
    label: 'Cao (SPF 30–50)',
    shortLabel: 'Cao',
  },
  {
    key: 'trung_binh',
    label: 'Trung bình (SPF 15–25)',
    shortLabel: 'Trung bình',
  },
  {
    key: 'thap',
    label: 'Thấp (SPF 6–10)',
    shortLabel: 'Thấp',
  },
]

/** 4 mức PA — thứ tự hiển thị lọc: cao → thấp. */
export const PA_CHUAN = ['PA++++', 'PA+++', 'PA++', 'PA+']

/** PA options cho form admin (thấp → cao). */
export const PA_OPTIONS = ['PA+', 'PA++', 'PA+++', 'PA++++']

function stripSpfPrefix(raw) {
  return String(raw ?? '')
    .trim()
    .replace(/^spf\s*/i, '')
    .trim()
}

function stripPaPrefix(raw) {
  return String(raw ?? '')
    .trim()
    .replace(/^pa\s*/i, '')
    .trim()
}

/**
 * @returns {{ num: number, hasPlus: boolean, suffix: string } | null}
 */
export function parseSpfParts(raw) {
  const suffix = stripSpfPrefix(raw)
  if (!suffix) return null
  const m = suffix.match(/^(\d+)\s*(\+)?$/)
  if (!m) return null
  const num = Number(m[1])
  if (!Number.isFinite(num) || num <= 0) return null
  return { num, hasPlus: Boolean(m[2]), suffix: `${num}${m[2] ? '+' : ''}` }
}

/**
 * Phân loại mức bảo vệ từ giá trị DB hoặc suffix.
 * @returns {'rat_cao'|'cao'|'trung_binh'|'thap'|null}
 */
export function phanLoaiSpf(giaTri) {
  const p = parseSpfParts(giaTri)
  if (!p) return null
  if (p.hasPlus || p.num > 50) return 'rat_cao'
  if (p.num >= 30 && p.num <= 50) return 'cao'
  if (p.num >= 15 && p.num <= 25) return 'trung_binh'
  if (p.num >= 6 && p.num <= 10) return 'thap'
  if (p.num < 15) return 'thap'
  if (p.num < 30) return 'trung_binh'
  return null
}

export function tenMucBaoVe(giaTri, { short = false } = {}) {
  const key = phanLoaiSpf(giaTri)
  if (!key) return ''
  const muc = MUC_BAO_VE.find((m) => m.key === key)
  if (!muc) return ''
  return short ? muc.shortLabel : muc.label
}

/**
 * @param {unknown} v — "SPF46", "spf 46", "46", "SPF50+"
 * @returns {string} "SPF 46", "SPF 50+"
 */
export function formatSpf(v) {
  const body = stripSpfPrefix(v)
  if (!body) return ''
  return `SPF ${body}`
}

/**
 * @param {unknown} v — "PA+++", "+++", "pa ++++"
 * @returns {string} "PA+++", "PA++++"
 */
export function formatPa(v) {
  const body = stripPaPrefix(v)
  if (!body) return ''
  return `PA${body}`
}

/** Badge thẻ SP: "SPF 50+ · PA++++" */
export function formatSpfPaBadge(spf, pa) {
  const parts = []
  const s = formatSpf(spf)
  const p = formatPa(pa)
  if (s) parts.push(s)
  if (p) parts.push(p)
  return parts.join(' · ')
}

/** Chi tiết SP: "SPF 50+ (Rất cao) · PA++++" */
export function formatSpfPaDetail(spf, pa) {
  const parts = []
  const s = formatSpf(spf)
  if (s) {
    const muc = tenMucBaoVe(spf, { short: true })
    parts.push(muc ? `${s} (${muc})` : s)
  }
  const p = formatPa(pa)
  if (p) parts.push(p)
  return parts.join(' · ')
}

/** Suffix form ("46", "50+") có nằm trong SPF_CHUAN không. */
export function isSpfChuanSuffix(suffix) {
  const s = String(suffix ?? '').trim()
  return SPF_CHUAN.some((o) => o.suffix === s)
}

/** Ghép số + tùy chọn dấu + → suffix ("46", "46+"). */
export function composeSpfSuffix(num, hasPlus) {
  const n = Number(num)
  if (!Number.isFinite(n) || n < 6 || n > 100) return ''
  const int = Math.round(n)
  return `${int}${hasPlus ? '+' : ''}`
}

/** Chuẩn hóa giá trị DB → option form SPF (không khớp → ''). */
export function normalizeSpfForSelect(value) {
  const body = stripSpfPrefix(value)
  if (!body) return ''
  const composed = `SPF${body}`
  return SPF_OPTIONS.some((o) => o.value === composed) ? composed : ''
}
