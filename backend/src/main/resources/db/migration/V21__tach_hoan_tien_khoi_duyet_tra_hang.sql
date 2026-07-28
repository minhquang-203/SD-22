/* V21: Tach hoan tien ra khoi buoc duyet yeu cau tra hang.
   Luong moi: admin duyet -> khach tao van don hoan (chon ca lay hang GHN)
   -> van don hoan hoan thanh (shop nhan hang) -> admin quyet dinh hoan tien hay khong.
   Bo sung cho yeu_cau_tra_hang: ca lay hang GHN, trang thai van don hoan, moc nhan hang. */

IF COL_LENGTH('yeu_cau_tra_hang', 'pick_shift_id') IS NULL
    ALTER TABLE yeu_cau_tra_hang ADD pick_shift_id INT NULL;
GO

IF COL_LENGTH('yeu_cau_tra_hang', 'pick_shift_label') IS NULL
    ALTER TABLE yeu_cau_tra_hang ADD pick_shift_label NVARCHAR(100) NULL;
GO

IF COL_LENGTH('yeu_cau_tra_hang', 'ghn_trang_thai_tra') IS NULL
    ALTER TABLE yeu_cau_tra_hang ADD ghn_trang_thai_tra VARCHAR(50) NULL;
GO

IF COL_LENGTH('yeu_cau_tra_hang', 'ngay_nhan_hang') IS NULL
    ALTER TABLE yeu_cau_tra_hang ADD ngay_nhan_hang DATETIME NULL;
GO
