export const LOAI_CHONG_NANG_LABELS = {
  VAT_LY: 'Vật lý',
  HOA_HOC: 'Hóa học',
  LAI: 'Lai (vật lý + hóa học)',
}

export function loaiChongNangLabel(code) {
  if (!code) return '—'
  return LOAI_CHONG_NANG_LABELS[code] || code
}
