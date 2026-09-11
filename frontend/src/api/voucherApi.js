import request from "./request";

export function getAllVoucher(page = 1, size = 10) {
  return request.get("/vouchers", {
    params: {
      page,
      size,
    },
  });
}

export function searchVoucher(keyword, timeStatus, loai, page, size, sortBy, direction) {
  return request.get("/vouchers/search", {
    params: {
      keyword,
      timeStatus,
      loai,
      page,
      size,
      sortBy,
      direction,
    },
  });
}

export function getVoucherById(id) {
  return request.get(`/vouchers/${id}`);
}

export function createVoucher(data) {
  return request.post("/vouchers", data);
}

export function fetchNextVoucherMa() {
  return request.get("/vouchers/next-ma");
}

export function updateVoucher(id, data) {
  return request.put(`/vouchers/${id}`, data);
}

export function deleteVoucher(id) {
  return request.delete(`/vouchers/${id}`);
}

export function stopVoucher(id) {
  return request.put(`/vouchers/${id}/stop`);
}

export function activateVoucher(id) {
  return request.put(`/vouchers/${id}/activate`);
}

export function getVoucherStats() {
  return request.get("/vouchers/stats");
}

/* ===================== GÁN VOUCHER CÁ NHÂN (ADMIN) ===================== */

export function getVoucherCustomers(id) {
  return request.get(`/vouchers/${id}/khach-hang`);
}

export function assignVoucherToCustomers(id, idKhachHangs) {
  return request.post(`/vouchers/${id}/khach-hang`, { idKhachHangs });
}

export function unassignVoucherCustomer(id, idKhachHang) {
  return request.delete(`/vouchers/${id}/khach-hang/${idKhachHang}`);
}

/* ===================== VOUCHER CỦA KHÁCH (STOREFRONT) ===================== */

export function getMyPublicVouchers(keyword) {
  return request.get("/khach-hang/toi/voucher/cong-khai", {
    params: { keyword },
  });
}

export function getMyPersonalVouchers() {
  return request.get("/khach-hang/toi/voucher/ca-nhan");
}
