import request from "./request";

export function getAllVoucher(page = 1, size = 10) {
  return request.get("/vouchers", {
    params: {
      page,
      size,
    },
  });
}

export function searchVoucher(keyword, timeStatus, loai, page, size) {
  return request.get("/vouchers/search", {
    params: {
      keyword,
      timeStatus,
      loai,
      page,
      size,
    },
  });
}

export function getVoucherById(id) {
  return request.get(`/vouchers/${id}`);
}

export function createVoucher(data) {
  return request.post("/vouchers", data);
}

export function updateVoucher(id, data) {
  return request.put(`/vouchers/${id}`, data);
}

export function deleteVoucher(id) {
  return request.delete(`/vouchers/${id}`);
}
