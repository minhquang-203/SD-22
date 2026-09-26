import request from './request'

export function guiYeuCauMuaSoLuongLon(payload) {
  return request.post('/yeu-cau-mua-so-luong-lon', payload)
}

export function fetchYeuCauMuaSoLuongLon(params = {}) {
  return request.get('/yeu-cau-mua-so-luong-lon', { params })
}

export function fetchYeuCauMuaSoLuongLonDetail(id) {
  return request.get(`/yeu-cau-mua-so-luong-lon/${id}`)
}

export function updateYeuCauMuaSoLuongLon(id, payload) {
  return request.put(`/yeu-cau-mua-so-luong-lon/${id}`, payload)
}

export function demYeuCauMuaSoLuongLonMoi() {
  return request.get('/yeu-cau-mua-so-luong-lon/dem-moi')
}
