import request from './request'

function appendReturnFormData(payload, files = []) {
  const formData = new FormData()
  formData.append(
    'data',
    new Blob([JSON.stringify(payload)], { type: 'application/json' }),
    'data.json',
  )
  ;(files || []).forEach((file) => {
    if (file) formData.append('files', file)
  })
  return formData
}

function guestReturnPath(token) {
  return `/hoa-don/tra-cuu/${encodeURIComponent(token)}`
}

/** Khách: tạo yêu cầu trả hàng (multipart: data JSON + files ảnh). idKhachHang lấy từ JWT phía BE. */
export function taoYeuCauTraHang(idHoaDon, _idKhachHang, payload, files = []) {
  return request.post(`/online/orders/${idHoaDon}/tra-hang`, appendReturnFormData(payload, files))
}

/** Khách vãng lai: tạo yêu cầu trả hàng bằng tracking token */
export function taoYeuCauTraHangCongKhai(token, idHoaDon, payload, files = []) {
  return request.post(
    `${guestReturnPath(token)}/orders/${idHoaDon}/tra-hang`,
    appendReturnFormData(payload, files),
  )
}

/** Khách: danh sách yêu cầu trả hàng của tôi */
export function fetchTraHangCuaToi(_idKhachHang) {
  return request.get('/online/tra-hang')
}

/** Khách: chi tiết một yêu cầu trả hàng */
export function fetchChiTietTraHangCuaToi(id, _idKhachHang) {
  return request.get(`/online/tra-hang/${id}`)
}

/** Khách vãng lai: chi tiết yêu cầu trả hàng bằng tracking token */
export function fetchChiTietTraHangCongKhai(token, id) {
  return request.get(`${guestReturnPath(token)}/tra-hang/${id}`)
}

/** Khách: danh sách ca lấy hàng GHN để chọn thời điểm shipper đến lấy hàng trả */
export function fetchCaLayHang(trackingToken) {
  if (trackingToken) {
    return request.get(`${guestReturnPath(trackingToken)}/tra-hang/ca-lay-hang`)
  }
  return request.get('/online/tra-hang/ca-lay-hang')
}

/** Khách: tạo vận đơn GHN hoàn hàng kèm ca lấy hàng đã chọn */
export function taoVanDonTra(id, _idKhachHang, pickShiftId = null) {
  return request.post(`/online/tra-hang/${id}/tao-van-don`, { pickShiftId })
}

/** Khách vãng lai: tạo vận đơn GHN hoàn hàng bằng tracking token */
export function taoVanDonTraCongKhai(token, id, pickShiftId = null) {
  return request.post(`${guestReturnPath(token)}/tra-hang/${id}/tao-van-don`, { pickShiftId })
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

/** Admin: từ chối yêu cầu (multipart: data JSON + files ảnh) */
export function tuChoiTraHang(id, payload = {}, files = []) {
  return request.post(`/tra-hang/${id}/tu-choi`, appendReturnFormData(payload, files))
}

/** Admin: danh sách lô đơn đã lấy (để chọn khi nhận hàng trả) */
export function fetchLoHangTraHang(id) {
  return request.get(`/tra-hang/${id}/lo-hang`)
}

/** Admin: xác nhận đã nhận hàng hoàn (kèm phân bổ lô TOT/LOI) */
export function daNhanHangTraHang(id, payload = {}) {
  return request.post(`/tra-hang/${id}/da-nhan-hang`, payload)
}

/** Admin: đồng bộ trạng thái vận đơn hoàn từ GHN */
export function dongBoVanDonTra(id, payload = {}) {
  return request.post(`/tra-hang/${id}/dong-bo-ghn`, payload)
}
