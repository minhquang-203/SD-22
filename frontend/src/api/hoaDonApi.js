import request from './request'

export const getAllHoaDon = () => request.get('/hoa-don')

export const getHoaDonPaging = (pageNo, pageSize) =>
  request.get('/hoa-don/paging', { params: { pageNo, pageSize } })

export const searchHoaDon = (keyword) =>
  request.get('/hoa-don/search', { params: { keyword } })

export const getHoaDonDetail = (id) => request.get(`/hoa-don/${id}`)

export const capNhatTrangThai = (id, payload) =>
  request.patch(`/hoa-don/${id}/status`, payload)

export const tuChoiDon = (id, payload) =>
  request.post(`/hoa-don/${id}/tu-choi`, payload)

export const taoVanDonGhn = (id) => request.post(`/hoa-don/${id}/tao-van-don-ghn`)

export const dongBoGhn = (id) => request.post(`/hoa-don/${id}/dong-bo-ghn`)

export const giaLapWebhookGhn = (id, payload) =>
  request.post(`/hoa-don/${id}/gia-lap-webhook-ghn`, payload)

export const getLichSu = (idHoaDon) =>
  request.get('/lich-su-don-hang/hoa-don', { params: { idHoaDon } })
