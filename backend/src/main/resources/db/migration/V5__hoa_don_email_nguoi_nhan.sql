/* =====================================================================
   V5: Lưu email người nhận trên hóa đơn.
   - Cho phép khách chưa đăng nhập (id_khach_hang NULL) nhận email xác nhận
     đơn hàng mà không phụ thuộc vào tài khoản khach_hang.
   - Với khách đã đăng nhập, cột này có thể để trống; mail sẽ fallback về
     khach_hang.email.
   Câu lệnh idempotent để an toàn khi ddl-auto=update đã thêm cột trước.
   ===================================================================== */

IF COL_LENGTH('hoa_don', 'email_nguoi_nhan') IS NULL
    ALTER TABLE hoa_don ADD email_nguoi_nhan VARCHAR(100) NULL;
GO
