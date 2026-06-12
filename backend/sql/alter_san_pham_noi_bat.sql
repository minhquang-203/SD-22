-- Nâng cấp DB SUNOVA đang chạy (đã tạo trước khi có cột noi_bat)
-- Chạy: sqlcmd -S localhost -U sa -P <pass> -i alter_san_pham_noi_bat.sql -f 65001

USE SUNOVA;
GO

IF COL_LENGTH('san_pham', 'noi_bat') IS NULL
BEGIN
    ALTER TABLE san_pham ADD noi_bat BIT NOT NULL CONSTRAINT DF_san_pham_noi_bat DEFAULT 0;
    PRINT N'Đã thêm cột san_pham.noi_bat';
END
ELSE
BEGIN
    PRINT N'Cột san_pham.noi_bat đã tồn tại — bỏ qua';
END
GO

-- Gợi ý: đánh dấu SP001 nổi bật để test admin
UPDATE san_pham SET noi_bat = 1 WHERE ma_san_pham = 'SP001' AND noi_bat = 0;
GO
