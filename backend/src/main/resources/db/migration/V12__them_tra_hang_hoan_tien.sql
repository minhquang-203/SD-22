/* V12: Them tinh nang tra hang (yeu_cau_tra_hang) va hoan tien (hoan_tien).
   - yeu_cau_tra_hang: khach hang gui yeu cau hoan tra sau khi nhan hang, admin duyet.
   - hoan_tien: ban ghi hoan tien (huy don da thanh toan hoac tra hang), admin xac nhan hoan tat. */

IF OBJECT_ID('yeu_cau_tra_hang', 'U') IS NULL
BEGIN
    CREATE TABLE yeu_cau_tra_hang (
        id                  INT IDENTITY(1,1) PRIMARY KEY,
        id_hoa_don          INT NOT NULL,
        ly_do               NVARCHAR(255) NULL,
        mo_ta               NVARCHAR(500) NULL,
        trang_thai          VARCHAR(20) NOT NULL DEFAULT 'CHO_DUYET',
        ma_van_don_tra      VARCHAR(50) NULL,
        ghn_district_id     INT NULL,
        ghn_ward_code       VARCHAR(20) NULL,
        dia_chi_tra         NVARCHAR(255) NULL,
        ten_ngan_hang       NVARCHAR(100) NULL,
        so_tai_khoan        VARCHAR(30) NULL,
        chu_tai_khoan       NVARCHAR(100) NULL,
        ghi_chu_admin       NVARCHAR(255) NULL,
        id_nhan_vien_duyet  INT NULL,
        ngay_tao            DATETIME NOT NULL DEFAULT GETDATE(),
        ngay_cap_nhat       DATETIME NULL,
        CONSTRAINT fk_ycth_hoa_don FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id),
        CONSTRAINT fk_ycth_nhan_vien FOREIGN KEY (id_nhan_vien_duyet) REFERENCES nhan_vien(id)
    );
END
GO

IF OBJECT_ID('hoan_tien', 'U') IS NULL
BEGIN
    CREATE TABLE hoan_tien (
        id                  INT IDENTITY(1,1) PRIMARY KEY,
        id_hoa_don          INT NOT NULL,
        id_yeu_cau_tra_hang INT NULL,
        loai                VARCHAR(10) NOT NULL,          -- HUY_DON | TRA_HANG
        so_tien             DECIMAL(12,0) NOT NULL DEFAULT 0,
        phuong_thuc         VARCHAR(20) NULL,              -- VNPAY | CHUYEN_KHOAN | TIEN_MAT
        trang_thai          VARCHAR(15) NOT NULL DEFAULT 'CHO_XU_LY', -- CHO_XU_LY | DA_HOAN | TU_CHOI
        ma_giao_dich_hoan   VARCHAR(100) NULL,
        ten_ngan_hang       NVARCHAR(100) NULL,
        so_tai_khoan        VARCHAR(30) NULL,
        chu_tai_khoan       NVARCHAR(100) NULL,
        ghi_chu             NVARCHAR(255) NULL,
        id_nhan_vien        INT NULL,
        ngay_tao            DATETIME NOT NULL DEFAULT GETDATE(),
        ngay_hoan           DATETIME NULL,
        CONSTRAINT fk_ht_hoa_don FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id),
        CONSTRAINT fk_ht_ycth FOREIGN KEY (id_yeu_cau_tra_hang) REFERENCES yeu_cau_tra_hang(id),
        CONSTRAINT fk_ht_nhan_vien FOREIGN KEY (id_nhan_vien) REFERENCES nhan_vien(id)
    );
END
GO
