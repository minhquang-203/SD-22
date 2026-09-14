/* =====================================================================
   V8: Token tra cứu đơn công khai (khách vãng lai / link email).
   - Không đưa email/mã dễ đoán lên URL; chỉ dùng token ngẫu nhiên.
   - Backfill đơn ONLINE cũ để link mail / tra cứu vẫn hoạt động.
   Câu lệnh idempotent để an toàn khi ddl-auto=update đã thêm cột trước.
   ===================================================================== */

IF COL_LENGTH('hoa_don', 'tracking_token') IS NULL
    ALTER TABLE hoa_don ADD tracking_token VARCHAR(64) NULL;
GO

-- Backfill token cho đơn online chưa có (2× NEWID → ~64 ký tự hex không dấu '-').
UPDATE hoa_don
SET tracking_token = LOWER(REPLACE(CONVERT(varchar(36), NEWID()) + CONVERT(varchar(36), NEWID()), '-', ''))
WHERE tracking_token IS NULL
  AND loai_don = 'ONLINE';
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'UX_hoa_don_tracking_token' AND object_id = OBJECT_ID('hoa_don')
)
BEGIN
    CREATE UNIQUE INDEX UX_hoa_don_tracking_token
        ON hoa_don (tracking_token)
        WHERE tracking_token IS NOT NULL;
END
GO
