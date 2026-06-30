IF COL_LENGTH('hoa_don', 'ma_van_don_ghn') IS NULL
BEGIN
    ALTER TABLE hoa_don ADD ma_van_don_ghn VARCHAR(50) NULL;
END
GO
