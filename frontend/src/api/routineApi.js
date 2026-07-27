import request from "./request";

// ========== API DÀNH CHO ADMIN ==========

// Lấy toàn bộ danh sách Routine
export function getAllRoutinesAdmin() {
  return request.get("/admin/routines");
}

// Tạo mới Routine
export function createRoutineAdmin(payload) {
  return request.post("/admin/routines", payload);
}

// Cập nhật Routine
export function updateRoutineAdmin(id, payload) {
  return request.put(`/admin/routines/${id}`, payload);
}

// Xóa Routine
export function deleteRoutineAdmin(id) {
  return request.delete(`/admin/routines/${id}`);
}

// ========== API DÀNH CHO KHÁCH HÀNG ==========

// Lấy Routine theo loại da (để hiển thị Combo sau Quiz)
export function getRoutinesByLoaiDa(loaiDaId) {
  return request.get(`/khach/routines/loai-da/${loaiDaId}`);
}
