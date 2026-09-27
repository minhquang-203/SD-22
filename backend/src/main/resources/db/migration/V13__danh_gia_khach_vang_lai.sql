/* =====================================================================
   V13: Cho phép id_khach_hang NULL trong danh_gia
   để khách vãng lai (đơn không gắn tài khoản) vẫn gửi được đánh giá.
   ===================================================================== */

IF EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('danh_gia')
      AND name = 'id_khach_hang'
      AND is_nullable = 0
)
BEGIN
    ALTER TABLE danh_gia ALTER COLUMN id_khach_hang INT NULL;
END
GO
