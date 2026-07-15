/* Bảng thong_bao đã tạo ở V1__init.sql.
   V11 chỉ bổ sung các cột cần cho thông báo admin (chuông + điều hướng)
   và nới rộng cột loai để chứa tên enum dài hơn. */

IF COL_LENGTH('thong_bao', 'link') IS NULL
BEGIN
    ALTER TABLE thong_bao ADD link VARCHAR(255) NULL;
END
GO

IF COL_LENGTH('thong_bao', 'id_tham_chieu') IS NULL
BEGIN
    ALTER TABLE thong_bao ADD id_tham_chieu INT NULL;
END
GO

IF COL_LENGTH('thong_bao', 'ma_tham_chieu') IS NULL
BEGIN
    ALTER TABLE thong_bao ADD ma_tham_chieu VARCHAR(30) NULL;
END
GO

IF EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('thong_bao') AND name = 'loai' AND max_length < 30
)
BEGIN
    ALTER TABLE thong_bao ALTER COLUMN loai VARCHAR(30);
END
GO
