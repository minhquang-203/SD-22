-- ==========================================================================
-- V15: Bổ sung ảnh chính cho SP17 (EltaMD Daily) và SP20 (Beauty of Joseon)
-- ==========================================================================

-- 1. Bổ sung ảnh cho SP17 nếu chưa có
IF NOT EXISTS (SELECT 1 FROM anh_san_pham WHERE id_san_pham = 17 AND la_anh_chinh = 1)
BEGIN
    INSERT INTO anh_san_pham (id_san_pham, id_chi_tiet_san_pham, id_mau_sac, url, la_anh_chinh, thu_tu)
    VALUES (17, NULL, NULL, N'/uploads/products/7301c795-839c-4cd1-8d05-464674c574ab_etla.jpg', 1, 1);
END;

-- 2. Bổ sung ảnh cho SP20 nếu chưa có
IF NOT EXISTS (SELECT 1 FROM anh_san_pham WHERE id_san_pham = 20 AND la_anh_chinh = 1)
BEGIN
    INSERT INTO anh_san_pham (id_san_pham, id_chi_tiet_san_pham, id_mau_sac, url, la_anh_chinh, thu_tu)
    VALUES (20, NULL, NULL, N'/uploads/products/beauty_of_joseon_rice.jpg', 1, 1);
END;
-- 3. Đồng bộ sản phẩm chân ái (SP15 Innisfree) vào Bước 1 của Combo Kiềm Dầu
UPDATE routine_combo_chi_tiet 
SET id_san_pham = 15, ghi_chu = N'Chống nắng chính buổi sáng (Chân ái)'
WHERE id_routine = 1 AND thu_tu = 1;

-- 4. Đồng bộ sản phẩm chân ái (SP20 Beauty of Joseon) vào Bước 1 của Combo Da Thường
UPDATE routine_combo 
SET ten = N'Combo Bảo Vệ Toàn Diện Da Thường', 
    mo_ta = N'Bộ đôi bảo vệ da hằng ngày: dưỡng ẩm tự nhiên buổi sáng, bảo vệ ngoài trời tăng cường.' 
WHERE id = 4;

UPDATE routine_combo_chi_tiet 
SET id_san_pham = 20, ghi_chu = N'Chống nắng chính hằng ngày (Chân ái)' 
WHERE id_routine = 4 AND thu_tu = 1;
GO

