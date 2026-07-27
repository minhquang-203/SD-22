-- Thêm cột filter_keyword vào bảng dap_an_quiz
-- Cột này dùng cho các đáp án "Lọc cứng" (Ví dụ: VAT_LY, HOA_HOC, LAI)
-- Nếu đáp án có filter_keyword, hệ thống sẽ dùng nó để lọc sản phẩm theo loại chống nắng
ALTER TABLE dap_an_quiz ADD filter_keyword VARCHAR(100) NULL;
GO

-- Cho phép id_loai_da NULL (đáp án dạng Filter thuần túy không cần gắn loại da)
ALTER TABLE dap_an_quiz ALTER COLUMN id_loai_da INT NULL;
GO
