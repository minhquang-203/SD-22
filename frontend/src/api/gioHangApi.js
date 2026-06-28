import request from './request'

export function fetchGioHang(idKhachHang) {
  return request.get('/gio-hang', { params: { idKhachHang } })
}

export function addGioHangItem(payload) {
  return request.post('/gio-hang/add', payload)
}

export function updateGioHangItem(idKhachHang, idChiTietGioHang, soLuong) {
  return request.put(
    `/gio-hang/update/${idChiTietGioHang}`,
    { soLuong },
    { params: { idKhachHang } },
  )
}

export function deleteGioHangItem(idKhachHang, idChiTietGioHang) {
  return request.delete(`/gio-hang/delete/${idChiTietGioHang}`, { params: { idKhachHang } })
}

export function clearGioHang(idKhachHang) {
  return request.delete('/gio-hang/clear', { params: { idKhachHang } })
}
