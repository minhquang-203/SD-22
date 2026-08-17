import request from './request'

/** Lấy toàn bộ kết quả Quiz (Admin) */
export const getQuizHistory = () => request.get('/admin/ket-qua-quiz')

/** Lấy sản phẩm gợi ý theo loại da */
export const getSanPhamGoiY = (idLoaiDa) => request.get(`/admin/ket-qua-quiz/san-pham-goi-y/${idLoaiDa}`)
