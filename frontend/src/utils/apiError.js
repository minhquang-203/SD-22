/** Chuyển lỗi API thành câu dễ đọc — không hiện SQL thô */
export function formatApiError(dataOrMessage) {
  if (!dataOrMessage) return 'Có lỗi xảy ra. Vui lòng thử lại.'

  if (typeof dataOrMessage === 'string') {
    return humanizeMessage(dataOrMessage)
  }

  if (dataOrMessage.message) {
    return humanizeMessage(String(dataOrMessage.message))
  }

  if (dataOrMessage.errors && typeof dataOrMessage.errors === 'object') {
    return Object.values(dataOrMessage.errors).join(' · ')
  }

  return 'Có lỗi xảy ra. Vui lòng thử lại.'
}

function humanizeMessage(text) {
  const raw = String(text).trim()
  const lower = raw.toLowerCase()

  // Giu nguyen thong bao loi GHN (vd: "Loi GHN: 401 ... unauthorized") de khong bi
  // map nham thanh "phien dang nhap het han".
  if (lower.includes('ghn')) {
    return raw
  }

  const dupMatch = raw.match(/duplicate key value is \(([^)]+)\)/i)
  if (dupMatch) {
    return `Mã "${dupMatch[1]}" đã tồn tại. Vui lòng dùng mã khác.`
  }

  if (lower.includes('unique key') || lower.includes('duplicate key') || lower.includes('đã tồn tại')) {
    return raw.includes('SQL') ? 'Mã đã tồn tại. Vui lòng kiểm tra lại.' : raw
  }

  if (lower.includes('forbidden') || lower === 'access denied') {
    return 'Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại'
  }

  if (lower.includes('unauthorized')) {
    return 'Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại'
  }

  if (raw.includes('insert into') || raw.includes('SQL [')) {
    return 'Không thể lưu dữ liệu. Vui lòng kiểm tra lại thông tin.'
  }

  return raw
}
