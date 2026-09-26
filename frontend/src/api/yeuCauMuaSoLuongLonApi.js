import request from './request'

/** Gửi yêu cầu mua số lượng lớn (công khai) — hệ thống gửi email cửa hàng. */
export function guiYeuCauMuaSoLuongLon(payload) {
  return request.post('/yeu-cau-mua-so-luong-lon', payload)
}
