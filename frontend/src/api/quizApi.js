import request from "./request";


// API DÀNH CHO KHÁCH HÀNG (STOREFRONT)


// Lấy danh sách câu hỏi để khách làm bài test
export function getQuizQuestions() {
  return request.get("/khach/quiz");
}

// API DÀNH CHO ADMIN (QUẢN TRỊ VIÊN)

// Lấy toàn bộ danh sách câu hỏi
export function getAllQuizzesAdmin() {
  return request.get("/admin/quizzes");
}

// Thêm mới 1 câu hỏi
export function createQuizAdmin(payload) {
  return request.post("/admin/quizzes", payload);
}

// Cập nhật câu hỏi
export function updateQuizAdmin(id, payload) {
  return request.put(`/admin/quizzes/${id}`, payload);
}

// Xóa câu hỏi
export function deleteQuizAdmin(id) {
  return request.delete(`/admin/quizzes/${id}`);
}
