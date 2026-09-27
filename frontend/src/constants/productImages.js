/** Giới hạn ảnh sản phẩm — giữ đồng bộ với ProductImageLimits (BE). */
export const PRODUCT_IMAGE_MAX_COUNT = 10
export const PRODUCT_IMAGE_MAX_BYTES = 15 * 1024 * 1024
export const PRODUCT_IMAGE_MAX_EDGE = 1600
export const PRODUCT_IMAGE_QUALITY = 0.85
export const PRODUCT_IMAGE_ALLOWED_TYPES = [
  'image/jpeg',
  'image/jpg',
  'image/png',
  'image/webp',
]
export const PRODUCT_IMAGE_ACCEPT = 'image/jpeg,image/jpg,image/png,image/webp,.jpg,.jpeg,.png,.webp'
