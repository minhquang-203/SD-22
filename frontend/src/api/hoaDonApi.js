import request from './request'

export const getAllHoaDon = () => request.get('/hoa-don')

export const getHoaDonPaging = (pageNo, pageSize) =>
  request.get('/hoa-don/paging', { params: { pageNo, pageSize } })

export const searchHoaDon = (keyword) =>
  request.get('/hoa-don/search', { params: { keyword } })

export const getHoaDonDetail = (id) => request.get(`/hoa-don/${id}`)

export const capNhatTrangThai = (id, payload) =>
  request.patch(`/hoa-don/${id}/status`, payload)

export const getLichSu = (idHoaDon) =>
  request.get('/lich-su-don-hang/hoa-don', { params: { idHoaDon } })
