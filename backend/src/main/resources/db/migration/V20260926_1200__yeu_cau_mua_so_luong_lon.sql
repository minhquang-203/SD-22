-- Yêu cầu mua số lượng lớn (tư vấn B2B / mua sỉ từ storefront)
IF OBJECT_ID(N'dbo.yeu_cau_mua_so_luong_lon', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.yeu_cau_mua_so_luong_lon (
        id                      INT IDENTITY(1,1) PRIMARY KEY,
        id_khach_hang           INT NULL,
        id_chi_tiet_san_pham    INT NOT NULL,
        so_luong                INT NOT NULL,
        ten_cong_ty             NVARCHAR(200) NULL,
        ho_ten                  NVARCHAR(100) NOT NULL,
        so_dien_thoai           NVARCHAR(20) NOT NULL,
        email                   NVARCHAR(100) NOT NULL,
        ghi_chu                 NVARCHAR(1000) NULL,
        nhan_khuyen_mai         BIT NOT NULL CONSTRAINT DF_ycmsll_nhan_km DEFAULT (0),
        trang_thai              NVARCHAR(20) NOT NULL CONSTRAINT DF_ycmsll_tt DEFAULT (N'MOI'),
        ngay_tao                DATETIME2 NOT NULL CONSTRAINT DF_ycmsll_ngay DEFAULT (SYSUTCDATETIME()),
        ghi_chu_noi_bo          NVARCHAR(1000) NULL,
        id_nhan_vien_xu_ly      INT NULL,
        CONSTRAINT FK_ycmsll_khach FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id),
        CONSTRAINT FK_ycmsll_ctsp FOREIGN KEY (id_chi_tiet_san_pham) REFERENCES chi_tiet_san_pham(id),
        CONSTRAINT FK_ycmsll_nv FOREIGN KEY (id_nhan_vien_xu_ly) REFERENCES nhan_vien(id)
    );

    CREATE INDEX IX_ycmsll_ngay_tao ON dbo.yeu_cau_mua_so_luong_lon (ngay_tao DESC);
    CREATE INDEX IX_ycmsll_trang_thai ON dbo.yeu_cau_mua_so_luong_lon (trang_thai);
    CREATE INDEX IX_ycmsll_sdt_ctsp_ngay ON dbo.yeu_cau_mua_so_luong_lon (so_dien_thoai, id_chi_tiet_san_pham, ngay_tao DESC);
END
