/* =============================================
   V20: Tạo bảng Routine Combo + Dữ liệu mẫu
   ============================================= */

-- 1. Bảng chính: Routine Combo
CREATE TABLE routine_combo (
    id              INT IDENTITY(1,1) PRIMARY KEY,
    ten             NVARCHAR(200) NOT NULL,
    mo_ta           NVARCHAR(500),
    id_loai_da      INT NULL,
    trang_thai      BIT DEFAULT 1,
    thu_tu          INT DEFAULT 0,
    ngay_tao        DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT fk_routine_loai_da FOREIGN KEY (id_loai_da) REFERENCES loai_da(id)
);
GO

-- 2. Bảng chi tiết: Sản phẩm trong Routine
CREATE TABLE routine_combo_chi_tiet (
    id              INT IDENTITY(1,1) PRIMARY KEY,
    id_routine      INT NOT NULL,
    id_san_pham     INT NOT NULL,
    thu_tu          INT DEFAULT 0,
    ghi_chu         NVARCHAR(200),
    CONSTRAINT fk_routine_ct_routine FOREIGN KEY (id_routine) REFERENCES routine_combo(id) ON DELETE CASCADE,
    CONSTRAINT fk_routine_ct_san_pham FOREIGN KEY (id_san_pham) REFERENCES san_pham(id)
);
GO

-- 3. Dữ liệu mẫu: Routine phù hợp cho từng loại da
-- Loại da: 1=Da Dầu, 2=Da Khô, 3=Da Hỗn Hợp, 4=Da Thường, 5=Da Nhạy Cảm
-- Sản phẩm: 1=Anessa(HOA_HOC,DaDầu), 2=LaRoche(HOA_HOC,DaNhạyCảm), 3=Skin1004(VAT_LY,DaNhạyCảm), 4=Sunplay(HOA_HOC,DaDầu), 5=Vichy(HOA_HOC,DaThường)

-- Combo 1: Dành cho Da Dầu
INSERT INTO routine_combo (ten, mo_ta, id_loai_da, trang_thai, thu_tu) VALUES
(N'Combo Kiềm Dầu Suốt Ngày', N'Bộ đôi chống nắng kiềm dầu hoàn hảo cho da dầu, giữ da mịn không bóng nhờn cả ngày dài.', 1, 1, 1);
GO
INSERT INTO routine_combo_chi_tiet (id_routine, id_san_pham, thu_tu, ghi_chu) VALUES
(1, 1, 1, N'Chống nắng chính - kháng nước, kiềm dầu'),
(1, 4, 2, N'Xịt chống nắng body tiện lợi khi ra ngoài');
GO

-- Combo 2: Dành cho Da Nhạy Cảm
INSERT INTO routine_combo (ten, mo_ta, id_loai_da, trang_thai, thu_tu) VALUES
(N'Combo An Toàn Cho Da Nhạy Cảm', N'Chỉ sử dụng các sản phẩm dịu nhẹ, không kích ứng, phù hợp cho da nhạy cảm và da sau peel.', 5, 1, 2);
GO
INSERT INTO routine_combo_chi_tiet (id_routine, id_san_pham, thu_tu, ghi_chu) VALUES
(2, 3, 1, N'Kem chống nắng Vật Lý - dịu nhẹ, rau má làm dịu'),
(2, 2, 2, N'Kem chống nắng chuyên biệt cho da nhạy cảm');
GO

-- Combo 3: Dành cho Da Hỗn Hợp
INSERT INTO routine_combo (ten, mo_ta, id_loai_da, trang_thai, thu_tu) VALUES
(N'Combo Cân Bằng Da Hỗn Hợp', N'Kết hợp chống nắng nhẹ dàng cho vùng khô và kiềm dầu cho vùng chữ T.', 3, 1, 3);
GO
INSERT INTO routine_combo_chi_tiet (id_routine, id_san_pham, thu_tu, ghi_chu) VALUES
(3, 5, 1, N'Chống nắng nâng tông, chống lão hóa'),
(3, 3, 2, N'Kem chống nắng dịu nhẹ cho vùng má khô');
GO

-- Combo 4: Dành cho Da Thường
INSERT INTO routine_combo (ten, mo_ta, id_loai_da, trang_thai, thu_tu) VALUES
(N'Combo Bảo Vệ Toàn Diện', N'Da thường có thể dùng hầu hết mọi sản phẩm. Combo này giúp bảo vệ toàn diện cả mặt lẫn body.', 4, 1, 4);
GO
INSERT INTO routine_combo_chi_tiet (id_routine, id_san_pham, thu_tu, ghi_chu) VALUES
(4, 5, 1, N'Chống nắng nâng tông cho mặt'),
(4, 1, 2, N'Sữa chống nắng kháng nước cho body');
GO

-- Combo 5: Dành cho Da Khô
INSERT INTO routine_combo (ten, mo_ta, id_loai_da, trang_thai, thu_tu) VALUES
(N'Combo Cấp Ẩm & Chống Nắng', N'Da khô cần được cấp ẩm sâu kèm chống nắng. Combo này giúp da không bị căng khô khi ra nắng.', 2, 1, 5);
GO
INSERT INTO routine_combo_chi_tiet (id_routine, id_san_pham, thu_tu, ghi_chu) VALUES
(5, 3, 1, N'Kem chống nắng cấp ẩm từ rau má'),
(5, 2, 2, N'Kem chống nắng dưỡng ẩm cho da nhạy cảm/khô');
GO
