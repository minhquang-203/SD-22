import request from '@/api/request'

export function fetchDonCuaToi() {
  return request.get('/hoa-don/cua-toi')
}

export function fetchChiTietDonCuaToi(id) {
  return request.get(`/hoa-don/cua-toi/${id}`)
}

export function huyDonCuaToi(id, payload = {}) {
  return request.post(`/hoa-don/cua-toi/${id}/huy`, payload)
}
