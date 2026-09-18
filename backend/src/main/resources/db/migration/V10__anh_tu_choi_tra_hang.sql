/* =====================================================================
   V10: Phân loại ảnh yêu cầu trả hàng — KHACH (minh chứng) vs TU_CHOI (admin).
   Idempotent khi ddl-auto=update đã thêm cột trước.
   ===================================================================== */

IF COL_LENGTH('anh_yeu_cau_tra_hang', 'loai') IS NULL
BEGIN
    ALTER TABLE anh_yeu_cau_tra_hang
        ADD loai NVARCHAR(20) NOT NULL CONSTRAINT DF_aycth_loai DEFAULT ('KHACH');
END
GO
