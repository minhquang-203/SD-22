import request from '@/api/request'

export function traCuuDon(ma, sdt) {
  return request.get('/hoa-don/tra-cuu', { params: { ma, sdt } })
}

export function fetchDonCuaToi() {
  return request.get('/hoa-don/cua-toi')
}

export function fetchChiTietDonCuaToi(id) {
  return request.get(`/hoa-don/cua-toi/${id}`)
}
