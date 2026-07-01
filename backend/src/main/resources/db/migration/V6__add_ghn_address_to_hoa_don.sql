IF COL_LENGTH('hoa_don', 'ghn_district_id') IS NULL
BEGIN
    ALTER TABLE hoa_don ADD ghn_district_id INT NULL;
END
GO

IF COL_LENGTH('hoa_don', 'ghn_ward_code') IS NULL
BEGIN
    ALTER TABLE hoa_don ADD ghn_ward_code VARCHAR(20) NULL;
END
GO
