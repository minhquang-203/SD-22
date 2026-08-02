/* V25: Gan anh san pham theo mau (id_mau_sac).
   NULL = anh dung chung cho moi mau; co gia tri = anh rieng cua mau do. */

IF COL_LENGTH('anh_san_pham', 'id_mau_sac') IS NULL
BEGIN
    ALTER TABLE anh_san_pham ADD id_mau_sac INT NULL;

    ALTER TABLE anh_san_pham
        ADD CONSTRAINT fk_anh_ms FOREIGN KEY (id_mau_sac) REFERENCES mau_sac(id);
END
GO
