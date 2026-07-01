IF COL_LENGTH('phieu_giam_gia', 'is_active') IS NULL
BEGIN
    ALTER TABLE phieu_giam_gia
        ADD is_active BIT NOT NULL CONSTRAINT df_pgg_is_active DEFAULT 1;
END
GO

IF COL_LENGTH('dot_giam_gia', 'is_active') IS NULL
BEGIN
    ALTER TABLE dot_giam_gia
        ADD is_active BIT NOT NULL CONSTRAINT df_dgg_is_active DEFAULT 1;
END
GO
