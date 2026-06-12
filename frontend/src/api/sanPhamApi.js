import request from './request'

export const getProducts = () => request.get('/san-pham')

export const searchProducts = (keyword) =>
  request.get('/san-pham/tim', { params: { keyword } })

export const getProductDetail = (id) =>
  request.get('/san-pham/detail', { params: { id } })

export const addProduct = (formData) => request.post('/san-pham/add', formData)

export const updateProduct = (id, formData) =>
  request.post(`/san-pham/update/${id}`, formData)

export const updateProductStatus = (id, trangThai) =>
  request.put('/san-pham/trang-thai', null, { params: { id, trangThai } })

export const updateProductNoiBat = (id, noiBat) =>
  request.put('/san-pham/noi-bat', null, { params: { id, noiBat } })

export const addChiTiet = (payload) =>
  request.post('/chi-tiet-san-pham/add', payload)

export const updateChiTiet = (id, payload) =>
  request.put(`/chi-tiet-san-pham/update/${id}`, payload)

export const hideChiTiet = (id) =>
  request.delete('/chi-tiet-san-pham/delete', { params: { id } })
