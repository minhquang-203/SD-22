/* V15: Ghi nhan phan bo lo khi tru ton (FEFO) de hoan ton dung lo da xuat. */

IF OBJECT_ID('hoa_don_chi_tiet_lo', 'U') IS NULL
BEGIN
    CREATE TABLE hoa_don_chi_tiet_lo (
        id                      INT IDENTITY(1,1) PRIMARY KEY,
        id_hoa_don_chi_tiet     INT NOT NULL,
        id_lo_hang              INT NOT NULL,
        so_luong                INT NOT NULL,
        CONSTRAINT ck_hdctl_so_luong CHECK (so_luong > 0),
        CONSTRAINT fk_hdctl_hdct FOREIGN KEY (id_hoa_don_chi_tiet)
            REFERENCES hoa_don_chi_tiet(id) ON DELETE CASCADE,
        CONSTRAINT fk_hdctl_lo FOREIGN KEY (id_lo_hang)
            REFERENCES lo_hang(id)
    );

    CREATE INDEX ix_hdctl_hdct ON hoa_don_chi_tiet_lo(id_hoa_don_chi_tiet);
    CREATE INDEX ix_hdctl_lo ON hoa_don_chi_tiet_lo(id_lo_hang);
END
GO
