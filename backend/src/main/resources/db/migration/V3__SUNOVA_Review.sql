-- Nâng cấp bảng Đánh Giá (Shopee-like Review System)
-- Thêm Phản hồi của Shop
ALTER TABLE danh_gia ADD phan_hoi_cua_shop NVARCHAR(500);

-- Thêm Ảnh/Video của khách hàng (Lưu URL)
ALTER TABLE danh_gia ADD hinh_anh_video VARCHAR(1000);

-- Thêm Khóa ngoại Hóa Đơn Chi Tiết để chặn spam review
ALTER TABLE danh_gia ADD id_hoa_don_chi_tiet INT;
ALTER TABLE danh_gia ADD CONSTRAINT fk_dg_hdct FOREIGN KEY (id_hoa_don_chi_tiet) REFERENCES hoa_don_chi_tiet(id);
