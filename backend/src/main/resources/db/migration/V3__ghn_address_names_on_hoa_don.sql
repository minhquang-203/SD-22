/* =====================================================================
   V3: Lưu tên tỉnh/thành và phường/xã của người nhận trên hóa đơn.
   GHN nhận địa chỉ 2 cấp qua to_province_name + to_ward_name (không có
   quận/huyện), nên tên phải được lưu nguyên vẹn từ lúc đặt hàng thay vì
   cắt lại từ dia_chi_giao (chuỗi này bị giới hạn 255 ký tự và địa chỉ
   cụ thể của khách thường đã chứa dấu phẩy).
   Các câu lệnh idempotent để an toàn khi ddl-auto=update đã thêm cột trước.
   ===================================================================== */

IF COL_LENGTH('hoa_don', 'ghn_province_name') IS NULL
    ALTER TABLE hoa_don ADD ghn_province_name NVARCHAR(100) NULL;
GO

IF COL_LENGTH('hoa_don', 'ghn_ward_name') IS NULL
    ALTER TABLE hoa_don ADD ghn_ward_name NVARCHAR(100) NULL;
GO
