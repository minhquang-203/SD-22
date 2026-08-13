import request from './request'

export function fetchProvinces() {
  return request.get('/shipping/provinces')
}

/** Phường/xã theo tỉnh (địa chỉ 2 cấp GHN v3). */
export function fetchWards(provinceId) {
  return request.get('/shipping/wards', { params: { provinceId } })
}

export function calcShippingFee(payload) {
  return request.post('/shipping/fee', payload)
}

/** Ward id mới GHN v3 thường >= 1000000. */
export function isNewWardCode(wardCode) {
  const n = Number(wardCode)
  return Number.isFinite(n) && n >= 1_000_000
}
