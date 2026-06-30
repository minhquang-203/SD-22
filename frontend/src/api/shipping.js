import request from './request'

export function fetchProvinces() {
  return request.get('/shipping/provinces')
}

export function fetchDistricts(provinceId) {
  return request.get('/shipping/districts', { params: { provinceId } })
}

export function fetchWards(districtId) {
  return request.get('/shipping/wards', { params: { districtId } })
}

export function calcShippingFee(payload) {
  return request.post('/shipping/fee', payload)
}
