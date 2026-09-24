/* =====================================================================
   V11: Cho phép id_khach_hang NULL trong ket_qua_quiz
   để lưu kết quả làm bài của khách vãng lai (chưa đăng nhập).
   ===================================================================== */

IF EXISTS (
    SELECT 1 FROM sys.columns 
    WHERE object_id = OBJECT_ID('ket_qua_quiz') 
      AND name = 'id_khach_hang' 
      AND is_nullable = 0
)
BEGIN
    ALTER TABLE ket_qua_quiz ALTER COLUMN id_khach_hang INT NULL;
END
GO
