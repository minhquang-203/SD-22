const ICON_MAP = [
  { keys: ['kem', 'chong', 'nang', 'spf', 'sun'], icon: 'solar:sun-2-linear' },
  { keys: ['serum'], icon: 'solar:waterdrop-linear' },
  { keys: ['duong', 'cream', 'moist'], icon: 'solar:test-tube-linear' },
  { keys: ['trang', 'makeup', 'diem'], icon: 'solar:magic-stick-3-linear' },
  { keys: ['sua', 'rua', 'cleanser'], icon: 'solar:bath-linear' },
  { keys: ['toner'], icon: 'solar:bottle-linear' },
]

export function categoryIcon(name = '', ma = '') {
  const haystack = `${name} ${ma}`.toLowerCase()
  const hit = ICON_MAP.find((item) => item.keys.some((k) => haystack.includes(k)))
  return hit?.icon || 'solar:box-linear'
}
