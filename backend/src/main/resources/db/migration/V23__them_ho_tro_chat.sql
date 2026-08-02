/* V23: Chat ho tro khach hang (khach <-> nhan vien), shared inbox. */

IF OBJECT_ID('phien_ho_tro', 'U') IS NULL
BEGIN
    CREATE TABLE phien_ho_tro (
        id                  INT IDENTITY(1,1) PRIMARY KEY,
        id_khach_hang       INT NULL,
        trang_thai          VARCHAR(10) NOT NULL CONSTRAINT DF_pht_trang_thai DEFAULT ('MO'),
        nguoi_xu_ly_id      INT NULL,
        ngay_tao            DATETIME NOT NULL CONSTRAINT DF_pht_ngay_tao DEFAULT (GETDATE()),
        cap_nhat_cuoi       DATETIME NOT NULL CONSTRAINT DF_pht_cap_nhat DEFAULT (GETDATE()),
        CONSTRAINT fk_pht_khach FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id),
        CONSTRAINT fk_pht_nv FOREIGN KEY (nguoi_xu_ly_id) REFERENCES nhan_vien(id),
        CONSTRAINT ck_pht_trang_thai CHECK (trang_thai IN ('MO', 'DONG'))
    );
END
GO

IF OBJECT_ID('tin_nhan_ho_tro', 'U') IS NULL
BEGIN
    CREATE TABLE tin_nhan_ho_tro (
        id              INT IDENTITY(1,1) PRIMARY KEY,
        id_phien        INT NOT NULL,
        nguoi_gui       VARCHAR(15) NOT NULL,
        id_nguoi_gui    INT NULL,
        noi_dung        NVARCHAR(2000) NOT NULL,
        da_doc          BIT NOT NULL CONSTRAINT DF_tnht_da_doc DEFAULT (0),
        thoi_gian       DATETIME NOT NULL CONSTRAINT DF_tnht_thoi_gian DEFAULT (GETDATE()),
        CONSTRAINT fk_tnht_phien FOREIGN KEY (id_phien) REFERENCES phien_ho_tro(id),
        CONSTRAINT ck_tnht_nguoi_gui CHECK (nguoi_gui IN ('KHACH', 'NHAN_VIEN'))
    );

    CREATE INDEX ix_tnht_phien_thoigian ON tin_nhan_ho_tro (id_phien, thoi_gian);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_pht_trang_thai' AND object_id = OBJECT_ID('phien_ho_tro'))
BEGIN
    CREATE INDEX ix_pht_trang_thai ON phien_ho_tro (trang_thai, cap_nhat_cuoi DESC);
END
GO
