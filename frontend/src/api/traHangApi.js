import request from './request'

/** Khách: tạo yêu cầu trả hàng (multipart: data JSON + files ảnh) */
export function taoYeuCauTraHang(idHoaDon, idKhachHang, payload, files = []) {
  const formData = new FormData()
  formData.append(
    'data',
    new Blob([JSON.stringify(payload)], { type: 'application/json' }),
    'data.json',
  )
  ;(files || []).forEach((file) => {
    if (file) formData.append('files', file)
  })
  return request.post(`/online/orders/${idHoaDon}/tra-hang`, formData, {
    params: { idKhachHang },
  })
}

/** Khách: danh sách yêu cầu trả hàng của tôi */
export function fetchTraHangCuaToi(idKhachHang) {
  return request.get('/online/tra-hang', { params: { idKhachHang } })
}

/** Khách: tạo vận đơn GHN hoàn hàng */
export function taoVanDonTra(id, idKhachHang) {
  return request.post(`/online/tra-hang/${id}/tao-van-don`, null, {
    params: { idKhachHang },
  })
}

/** Admin: danh sách yêu cầu trả hàng */
export function fetchTraHangList(trangThai) {
  return request.get('/tra-hang', {
    params: trangThai ? { trangThai } : {},
  })
}

/** Admin: duyệt yêu cầu */
export function duyetTraHang(id, payload = {}) {
  return request.post(`/tra-hang/${id}/duyet`, payload)
}

/** Admin: từ chối yêu cầu */
export function tuChoiTraHang(id, payload = {}) {
  return request.post(`/tra-hang/${id}/tu-choi`, payload)
}

/** Admin: xác nhận đã nhận hàng hoàn */
export function daNhanHangTraHang(id, payload = {}) {
  return request.post(`/tra-hang/${id}/da-nhan-hang`, payload)
}
