-- V2: Thêm vai trò CHỦ CỬA HÀNG + tài khoản chủ. Cấp: CHU > QUAN_LY > NHAN_VIEN.
IF NOT EXISTS (SELECT 1 FROM vai_tro WHERE ma_vai_tro = 'CHU')
    INSERT INTO vai_tro (ma_vai_tro, ten_vai_tro, mo_ta)
    VALUES ('CHU', N'Chủ cửa hàng', N'Cấp cao nhất, quản trị toàn bộ kể cả Quản lý');
GO
IF NOT EXISTS (SELECT 1 FROM nhan_vien WHERE email = 'nguyenvu20042019@gmail.com')
    INSERT INTO nhan_vien
        (id_vai_tro, ma_nhan_vien, ho_ten, email, so_dien_thoai, mat_khau, gioi_tinh, ngay_vao_lam, trang_thai)
    VALUES
        ((SELECT id FROM vai_tro WHERE ma_vai_tro = 'CHU'),
         'CHU01', N'Nguyễn Vũ', 'nguyenvu20042019@gmail.com', '0339589851',
         'nguyenvu20042019@gmail.com', 'Nam', '2026-01-01', 1);
GO
