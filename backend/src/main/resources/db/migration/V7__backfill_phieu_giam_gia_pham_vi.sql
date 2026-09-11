/* Seed cũ (V1) có phiếu trước khi thêm cột pham_vi → giá trị NULL.
   Trang voucher tài khoản chỉ lấy CONG_KHAI nên phiếu NULL bị ẩn dù đang hoạt động. */
UPDATE phieu_giam_gia
SET pham_vi = 'CONG_KHAI'
WHERE pham_vi IS NULL;
GO
