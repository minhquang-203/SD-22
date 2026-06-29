const ICON_MAP = [
  { keys: ['kem', 'chong', 'nang', 'spf', 'sun', 'uv'], icon: 'solar:sun-2-linear' },
  { keys: ['serum', 'tinh chat'], icon: 'solar:waterdrop-linear' },
  { keys: ['duong', 'cream', 'moist', 'kem duong'], icon: 'solar:test-tube-linear' },
  { keys: ['trang', 'makeup', 'diem', 'nen'], icon: 'solar:magic-stick-3-linear' },
  { keys: ['sua', 'rua', 'cleanser', 'tay trang'], icon: 'solar:bath-linear' },
  { keys: ['toner', 'nuoc hoa'], icon: 'solar:bottle-linear' },
  { keys: ['tre', 'em', 'baby', 'nhi'], icon: 'solar:emoji-funny-circle-linear' },
  { keys: ['nam', 'men'], icon: 'solar:user-rounded-linear' },
  { keys: ['than', 'mat na', 'mask'], icon: 'solar:mask-happly-linear' },
  { keys: ['body', 'than the'], icon: 'solar:body-linear' },
  { keys: ['moi', 'lip'], icon: 'solar:lipstick-linear' },
  { keys: ['mat', 'eye'], icon: 'solar:eye-linear' },
  { keys: ['phuc hoi', 'repair', 'sau'], icon: 'solar:heart-pulse-linear' },
  { keys: ['chong nhau', 'water'], icon: 'solar:shield-check-linear' },
]

const FALLBACK_ICONS = [
  'solar:sun-2-linear',
  'solar:waterdrop-linear',
  'solar:shield-check-linear',
  'solar:star-fall-linear',
  'solar:leaf-linear',
  'solar:fire-linear',
  'solar:cloud-sun-linear',
  'solar:medal-ribbon-linear',
]

export function categoryIcon(name = '', ma = '', id = 0) {
  const haystack = `${name} ${ma}`.toLowerCase()
  const hit = ICON_MAP.find((item) => item.keys.some((k) => haystack.includes(k)))
  if (hit) return hit.icon
  const idx = Math.abs(Number(id) || haystack.length) % FALLBACK_ICONS.length
  return FALLBACK_ICONS[idx]
}

export function brandDisplayName(ten = '') {
  const words = ten.trim().split(/\s+/).filter(Boolean)
  if (!words.length) return ''
  if (words.length === 1) return words[0].length > 10 ? `${words[0].slice(0, 9)}…` : words[0]
  return words.map((w) => w[0]?.toUpperCase() || '').join('').slice(0, 4)
}
