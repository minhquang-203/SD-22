/* V24: Tra hang theo lo + phan loai hang TOT/LOI.
   - lo_hang.so_luong_loi: hang loi da ghi nhan (khong nam trong ton ban duoc)
   - chi_tiet_tra_hang_lo: chi tiet nhan hang tra theo tung lo (TOT|LOI) */

IF COL_LENGTH('lo_hang', 'so_luong_loi') IS NULL
BEGIN
    ALTER TABLE lo_hang ADD so_luong_loi INT NOT NULL CONSTRAINT df_lo_hang_so_luong_loi DEFAULT 0;
END
GO

IF OBJECT_ID('chi_tiet_tra_hang_lo', 'U') IS NULL
BEGIN
    CREATE TABLE chi_tiet_tra_hang_lo (
        id                      INT IDENTITY(1,1) PRIMARY KEY,
        id_yeu_cau_tra_hang     INT NOT NULL,
        id_lo_hang              INT NOT NULL,
        so_luong                INT NOT NULL,
        loai                    VARCHAR(10) NOT NULL,
        CONSTRAINT ck_ctthl_so_luong CHECK (so_luong > 0),
        CONSTRAINT ck_ctthl_loai CHECK (loai IN ('TOT', 'LOI')),
        CONSTRAINT fk_ctthl_yeu_cau FOREIGN KEY (id_yeu_cau_tra_hang)
            REFERENCES yeu_cau_tra_hang(id) ON DELETE CASCADE,
        CONSTRAINT fk_ctthl_lo FOREIGN KEY (id_lo_hang)
            REFERENCES lo_hang(id)
    );

    CREATE INDEX ix_ctthl_yeu_cau ON chi_tiet_tra_hang_lo(id_yeu_cau_tra_hang);
    CREATE INDEX ix_ctthl_lo ON chi_tiet_tra_hang_lo(id_lo_hang);
END
GO
