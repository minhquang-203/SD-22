/* =====================================================================
   V2: Chống double-order (idempotency key) + tăng tốc khóa xử lý thanh toán.
   - hoa_don.idempotency_key: chặn tạo trùng đơn khi client gửi lại request.
   - Index ma_giao_dich: để lock (PESSIMISTIC_WRITE) ở callback/IPN seek đúng 1 dòng,
     không quét bảng gây chặn các đơn khác.
   Các câu lệnh idempotent để an toàn khi ddl-auto=update đã thêm cột trước.
   ===================================================================== */

IF COL_LENGTH('hoa_don', 'idempotency_key') IS NULL
    ALTER TABLE hoa_don ADD idempotency_key VARCHAR(64) NULL;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'ux_hoa_don_idempotency_key'
      AND object_id = OBJECT_ID('hoa_don')
)
    CREATE UNIQUE INDEX ux_hoa_don_idempotency_key
        ON hoa_don(idempotency_key)
        WHERE idempotency_key IS NOT NULL;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'ix_tthd_ma_giao_dich'
      AND object_id = OBJECT_ID('thanh_toan_hoa_don')
)
    CREATE INDEX ix_tthd_ma_giao_dich
        ON thanh_toan_hoa_don(ma_giao_dich);
GO
