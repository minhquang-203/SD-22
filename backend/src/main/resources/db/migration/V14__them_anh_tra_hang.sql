/* V14: Them bang anh dinh kem yeu cau tra hang (toi thieu 2 anh). */

IF OBJECT_ID('anh_yeu_cau_tra_hang', 'U') IS NULL
BEGIN
    CREATE TABLE anh_yeu_cau_tra_hang (
        id                      INT IDENTITY(1,1) PRIMARY KEY,
        id_yeu_cau_tra_hang     INT NOT NULL,
        duong_dan               NVARCHAR(500) NOT NULL,
        ngay_tao                DATETIME NOT NULL DEFAULT GETDATE(),
        CONSTRAINT fk_aycth_yeu_cau FOREIGN KEY (id_yeu_cau_tra_hang)
            REFERENCES yeu_cau_tra_hang(id)
    );
END
GO
