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

/** Tra cứu bằng token bí mật (link trong email). */
export function traCuuDonBangToken(token) {
  return request.get('/hoa-don/tra-cuu', {
    params: { token },
  })
}

/** Tra cứu thủ công bằng mã + email (POST — email không nằm trên URL). */
export function traCuuDonCongKhai(ma, email) {
  return request.post('/hoa-don/tra-cuu', { ma, email })
}
