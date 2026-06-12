import request from './request'

export const getCustomers = () => request.get('/khach-hang')

export const searchCustomers = (keyword) =>
  request.get('/khach-hang/tim', { params: { keyword } })

export const getCustomerDetail = (id) =>
  request.get('/khach-hang/detail', { params: { id } })

export const updateCustomerStatus = (id, trangThai) =>
  request.put('/khach-hang/trang-thai', null, { params: { id, trangThai } })
