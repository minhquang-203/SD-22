import {
  PRODUCT_IMAGE_ALLOWED_TYPES,
  PRODUCT_IMAGE_MAX_BYTES,
  PRODUCT_IMAGE_MAX_EDGE,
  PRODUCT_IMAGE_QUALITY,
} from '@/constants/productImages'

/**
 * Kiểm tra MIME / đuôi file ảnh sản phẩm.
 * @param {File} file
 */
export function isAllowedProductImageFile(file) {
  if (!file) return false
  const type = (file.type || '').toLowerCase()
  if (PRODUCT_IMAGE_ALLOWED_TYPES.includes(type)) return true
  const name = String(file.name || '').toLowerCase()
  return /\.(jpe?g|png|webp)$/.test(name)
}

/**
 * Thu nhỏ cạnh dài > maxEdge và nén JPEG/WEBP ~quality.
 * PNG giữ alpha → xuất WEBP nếu hỗ trợ, không thì JPEG.
 * @param {File} file
 * @returns {Promise<File>}
 */
export async function compressProductImage(file) {
  if (!file || !isAllowedProductImageFile(file)) {
    throw new Error('Định dạng ảnh không hợp lệ. Chỉ chấp nhận JPG, JPEG, PNG, WEBP.')
  }

  const bitmap = await loadImageBitmap(file)
  try {
    const { width, height } = bitmap
    const longEdge = Math.max(width, height)
    const scale = longEdge > PRODUCT_IMAGE_MAX_EDGE ? PRODUCT_IMAGE_MAX_EDGE / longEdge : 1
    const tw = Math.max(1, Math.round(width * scale))
    const th = Math.max(1, Math.round(height * scale))

    const canvas = document.createElement('canvas')
    canvas.width = tw
    canvas.height = th
    const ctx = canvas.getContext('2d')
    if (!ctx) throw new Error('Trình duyệt không hỗ trợ xử lý ảnh')
    ctx.drawImage(bitmap, 0, 0, tw, th)

    const srcType = (file.type || '').toLowerCase()
    const preferWebp = srcType === 'image/webp' || srcType === 'image/png'
    const outType = preferWebp && supportsWebpEncode() ? 'image/webp' : 'image/jpeg'
    const blob = await canvasToBlob(canvas, outType, PRODUCT_IMAGE_QUALITY)
    if (!blob) throw new Error('Không thể nén ảnh')

    if (blob.size > PRODUCT_IMAGE_MAX_BYTES) {
      // Thử nén mạnh hơn một lần
      const tighter = await canvasToBlob(canvas, 'image/jpeg', 0.7)
      if (!tighter || tighter.size > PRODUCT_IMAGE_MAX_BYTES) {
        throw new Error(`Ảnh vẫn vượt quá ${PRODUCT_IMAGE_MAX_BYTES / (1024 * 1024)}MB sau khi nén`)
      }
      return new File([tighter], renameExt(file.name, 'jpg'), {
        type: 'image/jpeg',
        lastModified: Date.now(),
      })
    }

    const ext = outType === 'image/webp' ? 'webp' : 'jpg'
    return new File([blob], renameExt(file.name, ext), {
      type: outType,
      lastModified: Date.now(),
    })
  } finally {
    if (typeof bitmap.close === 'function') bitmap.close()
  }
}

function renameExt(name, ext) {
  const base = String(name || 'image').replace(/\.[^.]+$/, '') || 'image'
  const safe = base.replace(/[^\w.-]+/g, '_').slice(0, 80) || 'image'
  return `${safe}.${ext}`
}

function supportsWebpEncode() {
  try {
    const c = document.createElement('canvas')
    c.width = 1
    c.height = 1
    return c.toDataURL('image/webp').startsWith('data:image/webp')
  } catch {
    return false
  }
}

function canvasToBlob(canvas, type, quality) {
  return new Promise((resolve) => {
    canvas.toBlob((b) => resolve(b), type, quality)
  })
}

async function loadImageBitmap(file) {
  if (typeof createImageBitmap === 'function') {
    return createImageBitmap(file)
  }
  const url = URL.createObjectURL(file)
  try {
    const img = await new Promise((resolve, reject) => {
      const el = new Image()
      el.onload = () => resolve(el)
      el.onerror = () => reject(new Error('Không đọc được file ảnh'))
      el.src = url
    })
    return img
  } finally {
    URL.revokeObjectURL(url)
  }
}
