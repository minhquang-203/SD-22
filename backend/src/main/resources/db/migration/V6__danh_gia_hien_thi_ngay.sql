/* =====================================================================
   V6: Đánh giá hiển thị ngay, không chờ admin duyệt.
   - Đổi default trang_thai từ CHO_DUYET sang DA_DUYET.
   - Chuyển các đánh giá đang chờ / bị ẩn sang đã hiển thị.
   ===================================================================== */

DECLARE @constraintName NVARCHAR(256);
SELECT @constraintName = dc.name
FROM sys.default_constraints dc
INNER JOIN sys.columns c ON c.default_object_id = dc.object_id
WHERE dc.parent_object_id = OBJECT_ID(N'dbo.danh_gia')
  AND c.name = N'trang_thai';

IF @constraintName IS NOT NULL
    EXEC(N'ALTER TABLE danh_gia DROP CONSTRAINT [' + @constraintName + N']');
GO

ALTER TABLE danh_gia ADD CONSTRAINT df_danh_gia_trang_thai DEFAULT 'DA_DUYET' FOR trang_thai;
GO

UPDATE danh_gia
SET trang_thai = 'DA_DUYET'
WHERE trang_thai IN ('CHO_DUYET', 'TU_CHOI')
   OR trang_thai IS NULL;
GO
