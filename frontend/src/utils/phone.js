/** Số di động Việt Nam: 10 chữ số, bắt đầu 0, đầu số 03/05/07/08/09 */
export const PHONE_VN_REGEX = /^0(3|5|7|8|9)\d{8}$/

export function isValidVietnamesePhone(value) {
  const digits = String(value || '').replace(/\D/g, '')
  return PHONE_VN_REGEX.test(digits)
}

export function normalizePhoneDigits(value, maxLength = 10) {
  return String(value || '').replace(/\D/g, '').slice(0, maxLength)
}

export function getPhoneValidationError(value) {
  const digits = normalizePhoneDigits(value)
  if (!digits) return 'Vui lòng nhập số điện thoại'
  if (!PHONE_VN_REGEX.test(digits)) {
    return 'Số điện thoại không hợp lệ (10 chữ số, bắt đầu bằng 0)'
  }
  return ''
}
