/* =====================================================================
   V12: Bảng uv_configs (cấu hình ngưỡng UV / điểm thưởng).
   Entity UvConfig tồn tại từ trước; bảng từng được tạo ngoài Flyway
   (ddl-auto=update). Cần migration sau reset DB sạch + ddl-auto=validate.
   ===================================================================== */

IF OBJECT_ID('uv_configs', 'U') IS NULL
BEGIN
    CREATE TABLE uv_configs (
        id                     BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        uv_high_threshold      FLOAT NOT NULL CONSTRAINT DF_uv_configs_high DEFAULT (6.0),
        uv_extreme_threshold   FLOAT NOT NULL CONSTRAINT DF_uv_configs_extreme DEFAULT (8.0),
        bonus_points_high      INT NOT NULL CONSTRAINT DF_uv_configs_bonus_high DEFAULT (20),
        bonus_points_extreme   INT NOT NULL CONSTRAINT DF_uv_configs_bonus_extreme DEFAULT (50),
        enable_realtime_alert  BIT NOT NULL CONSTRAINT DF_uv_configs_alert DEFAULT (1)
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM uv_configs WHERE id = 1)
BEGIN
    SET IDENTITY_INSERT uv_configs ON;
    INSERT INTO uv_configs (id, uv_high_threshold, uv_extreme_threshold, bonus_points_high, bonus_points_extreme, enable_realtime_alert)
    VALUES (1, 6.0, 8.0, 20, 50, 1);
    SET IDENTITY_INSERT uv_configs OFF;
END
GO
