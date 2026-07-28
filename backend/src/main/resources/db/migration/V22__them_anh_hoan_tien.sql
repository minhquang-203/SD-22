/* V22: Anh chung tu chuyen khoan khi admin hoan tat hoan tien thu cong (COD / CHUYEN_KHOAN). */

IF OBJECT_ID('anh_hoan_tien', 'U') IS NULL
BEGIN
    CREATE TABLE anh_hoan_tien (
        id              INT IDENTITY(1,1) PRIMARY KEY,
        id_hoan_tien    INT NOT NULL,
        duong_dan       NVARCHAR(500) NOT NULL,
        ngay_tao        DATETIME NOT NULL DEFAULT GETDATE(),
        CONSTRAINT fk_aht_hoan_tien FOREIGN KEY (id_hoan_tien) REFERENCES hoan_tien(id)
    );
END
GO
