-- Thêm cột danh sách sản phẩm gợi ý dạng chuỗi (comma-separated) vào bảng tin_nhan_chat_ai
ALTER TABLE tin_nhan_chat_ai ADD danh_sach_sp_goi_y VARCHAR(255) NULL;
GO
