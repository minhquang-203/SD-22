import request from './request'

export const getNhaCungCapList = (q, activeOnly = true) =>
  request.get('/nha-cung-cap', {
    params: {
      ...(q ? { q } : {}),
      activeOnly,
    },
  })

export const getNhaCungCapDetail = (id) => request.get(`/nha-cung-cap/${id}`)

export const createNhaCungCap = (payload) => request.post('/nha-cung-cap', payload)

export const updateNhaCungCap = (id, payload) => request.put(`/nha-cung-cap/${id}`, payload)

export const deleteNhaCungCap = (id) => request.delete(`/nha-cung-cap/${id}`)

export const timBienTheNhapHang = (keyword = '', page = 0, size = 20) =>
  request.get('/nhap-hang/tim-bien-the', { params: { keyword, page, size } })

export const getPhieuNhapList = (params) => request.get('/nhap-hang', { params })

export const getPhieuNhapDetail = (id) => request.get(`/nhap-hang/${id}`)

export const luuTamPhieuNhap = (payload) => request.post('/nhap-hang/luu-tam', payload)

export const updatePhieuNhap = (id, payload) => request.put(`/nhap-hang/${id}`, payload)

export const hoanThanhPhieuNhap = (id) => request.post(`/nhap-hang/${id}/hoan-thanh`)

export const huyPhieuNhap = (id) => request.post(`/nhap-hang/${id}/huy`)
