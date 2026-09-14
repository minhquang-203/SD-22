/* =====================================================================
   V9: Khoảng điểm tích lũy để tự động gán voucher cá nhân.
   - diem_toi_thieu / diem_toi_da: optional. Nếu có ít nhất 1 giá trị,
     hệ thống tự gán cho khách đủ điểm; admin vẫn có thể gán thủ công
     trong khoảng điểm (tránh sót).
   Idempotent khi ddl-auto=update đã thêm cột trước.
   ===================================================================== */

IF COL_LENGTH('phieu_giam_gia', 'diem_toi_thieu') IS NULL
BEGIN
    ALTER TABLE phieu_giam_gia ADD diem_toi_thieu INT NULL;
END
GO

IF COL_LENGTH('phieu_giam_gia', 'diem_toi_da') IS NULL
BEGIN
    ALTER TABLE phieu_giam_gia ADD diem_toi_da INT NULL;
END
GO
