import request from "./request";

export function getAllSale() {
  return request.get("/sale");
}

export function searchSale(keyword, timeStatus, page = 1, size = 10) {
  return request.get("/sale/search", {
    params: {
      keyword,
      timeStatus,
      page,
      size,
    },
  });
}

export function getSaleById(id) {
  return request.get(`/sale/${id}`);
}

export function createSale(data) {
  return request.post("/sale", data);
}

export function updateSale(id, data) {
  return request.put(`/sale/${id}`, data);
}

export function deleteSale(id) {
  return request.delete(`/sale/${id}`);
}

export function getSaleProducts(id) {
  return request.get(`/sale/${id}/products`);
}

export function addSaleProduct(id, data) {
  return request.post(`/sale/${id}/products`, data);
}

export function updateSaleProduct(id, detailId, data) {
  return request.put(`/sale/${id}/products/${detailId}`, data);
}

export function deleteSaleProduct(id, detailId) {
  return request.delete(`/sale/${id}/products/${detailId}`);
}
