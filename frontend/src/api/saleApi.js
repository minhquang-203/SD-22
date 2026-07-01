import request from './request'

export function searchSale(keyword, timeStatus, page, size) {
  return request.get('/sale/search', {
    params: { keyword, timeStatus, page, size },
  })
}

export function getSaleById(id) {
  return request.get(`/sale/${id}`)
}

export function createSale(data) {
  return request.post('/sale', data)
}

export function updateSale(id, data) {
  return request.put(`/sale/${id}`, data)
}

export function deleteSale(id) {
  return request.delete(`/sale/${id}`)
}

export function stopSale(id) {
  return request.put(`/sale/${id}/stop`)
}

export function activateSale(id) {
  return request.put(`/sale/${id}/activate`)
}

export function getSaleProducts(id) {
  return request.get(`/sale/${id}/products`)
}

export function addSaleProduct(id, data) {
  return request.post(`/sale/${id}/products`, data)
}

export function deleteSaleProduct(id, detailId) {
  return request.delete(`/sale/${id}/products/${detailId}`)
}
