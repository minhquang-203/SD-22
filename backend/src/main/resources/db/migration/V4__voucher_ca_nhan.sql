/* =====================================================================
   V4: Voucher cá nhân.
   - Thêm cột pham_vi cho phieu_giam_gia để phân biệt voucher công khai
     (CONG_KHAI, mọi khách thấy) và voucher cá nhân (CA_NHAN, chỉ khách
     được gán mới thấy/dùng được).
   - Bảng khach_hang_phieu_giam_gia lưu quan hệ gán voucher cho khách
     (gán cá nhân hoặc gán hàng loạt theo nhóm đều đổ về bảng này).
   Các câu lệnh idempotent để an toàn khi ddl-auto=update đã thêm trước.
   ===================================================================== */

IF COL_LENGTH('phieu_giam_gia', 'pham_vi') IS NULL
BEGIN
    ALTER TABLE phieu_giam_gia
        ADD pham_vi VARCHAR(10) NOT NULL CONSTRAINT df_pgg_pham_vi DEFAULT 'CONG_KHAI';
END
GO

IF OBJECT_ID('khach_hang_phieu_giam_gia', 'U') IS NULL
BEGIN
    CREATE TABLE khach_hang_phieu_giam_gia (
        id                INT IDENTITY(1,1) PRIMARY KEY,
        id_khach_hang     INT NOT NULL,
        id_phieu_giam_gia INT NOT NULL,
        da_su_dung        BIT DEFAULT 0,
        ngay_gan          DATETIME DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT fk_khpgg_kh  FOREIGN KEY (id_khach_hang)     REFERENCES khach_hang(id),
        CONSTRAINT fk_khpgg_pgg FOREIGN KEY (id_phieu_giam_gia) REFERENCES phieu_giam_gia(id),
        CONSTRAINT uq_khpgg UNIQUE (id_khach_hang, id_phieu_giam_gia)
    );
END
GO
