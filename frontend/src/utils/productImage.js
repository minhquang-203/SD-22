const PLACEHOLDER =
  'data:image/svg+xml,' +
  encodeURIComponent(
    `<svg xmlns="http://www.w3.org/2000/svg" width="400" height="500" viewBox="0 0 400 500">
      <rect fill="#f0e9d8" width="400" height="500"/>
      <text x="200" y="255" text-anchor="middle" fill="#c9a96e" font-family="sans-serif" font-size="18">SUNOVA</text>
    </svg>`,
  )

export function productImageUrl(url) {
  if (!url || typeof url !== 'string') return PLACEHOLDER
  const trimmed = url.trim()
  if (!trimmed) return PLACEHOLDER
  if (trimmed.startsWith('http') || trimmed.startsWith('data:')) return trimmed
  if (trimmed.startsWith('/')) return trimmed
  return `/${trimmed}`
}

export { PLACEHOLDER as PRODUCT_IMAGE_PLACEHOLDER }
