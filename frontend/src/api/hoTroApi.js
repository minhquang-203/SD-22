import request from './request'

export const taoHoacLayPhienHoTro = () => request.post('/ho-tro/phien')

export const guiTinHoTroKhach = (payload) => request.post('/ho-tro/tin-nhan', payload)

export const layTinNhanHoTro = (idPhien) => request.get(`/ho-tro/phien/${idPhien}/tin-nhan`)

export const danhSachPhienHoTro = () => request.get('/ho-tro/phien')

export const traLoiHoTro = (idPhien, payload) =>
  request.post(`/ho-tro/phien/${idPhien}/tra-loi`, payload)

export const danhDauDaDocPhien = (idPhien) => request.put(`/ho-tro/phien/${idPhien}/da-doc`)
