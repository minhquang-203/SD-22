CREATE DATABASE menz;
GO
USE menz;
GO

-- =============================================
-- BẢNG DANH MỤC CƠ SỞ
-- =============================================

-- tạo bảng danh mục
CREATE TABLE danh_muc (
    id INT IDENTITY PRIMARY KEY,
    ten NVARCHAR(100),
    trang_thai TINYINT
);
GO

-- tạo bảng thương hiệu
CREATE TABLE thuong_hieu (
    id INT IDENTITY PRIMARY KEY,
    ten NVARCHAR(100),
    trang_thai TINYINT
);
GO

-- tạo bảng xuất xứ
CREATE TABLE xuat_xu (
    id INT IDENTITY PRIMARY KEY,
    ten_quoc_gia NVARCHAR(100),
    ghi_chu NVARCHAR(255),
    trang_thai TINYINT
);
GO

-- tạo bảng chất liệu
CREATE TABLE chat_lieu (
    id INT IDENTITY PRIMARY KEY,
    ten NVARCHAR(100),
    trang_thai TINYINT
);
GO

-- tạo bảng màu sắc
CREATE TABLE mau_sac (
    id INT IDENTITY PRIMARY KEY,
    ma NVARCHAR(50),
    ten NVARCHAR(100),
    trang_thai TINYINT
);
GO

-- tạo bảng kích thước
CREATE TABLE kich_thuoc (
    id INT IDENTITY PRIMARY KEY,
    ma NVARCHAR(50),
    ten_kich_thuoc NVARCHAR(50),
    trang_thai TINYINT
);
GO

-- tạo bảng cổ áo
CREATE TABLE co_ao (
    id INT IDENTITY PRIMARY KEY,
    ma NVARCHAR(50),
    ten NVARCHAR(100),
    trang_thai TINYINT
);
GO

-- tạo bảng tay áo
CREATE TABLE tay_ao (
    id INT IDENTITY PRIMARY KEY,
    ma NVARCHAR(50),
    ten NVARCHAR(100),
    trang_thai TINYINT
);
GO

-- =============================================
-- BẢNG SẢN PHẨM
-- =============================================

-- tạo bảng sản phẩm
CREATE TABLE san_pham (
    id INT IDENTITY PRIMARY KEY,
    id_chat_lieu INT,
    id_thuong_hieu INT,
    id_xuat_xu INT,
    id_danh_muc INT,
    ma_san_pham NVARCHAR(50),
    ten_san_pham NVARCHAR(255),
    trang_thai TINYINT,

    CONSTRAINT fk_sp_chat_lieu FOREIGN KEY (id_chat_lieu) REFERENCES chat_lieu(id),
    CONSTRAINT fk_sp_thuong_hieu FOREIGN KEY (id_thuong_hieu) REFERENCES thuong_hieu(id),
    CONSTRAINT fk_sp_xuat_xu FOREIGN KEY (id_xuat_xu) REFERENCES xuat_xu(id),
    CONSTRAINT fk_sp_danh_muc FOREIGN KEY (id_danh_muc) REFERENCES danh_muc(id)
);
GO

-- tạo bảng chi tiết sản phẩm
CREATE TABLE chi_tiet_san_pham (
    id INT IDENTITY PRIMARY KEY,
    id_san_pham INT,
    id_mau_sac INT,
    id_kich_thuoc INT,
    id_co_ao INT,
    id_tay_ao INT,
    ma_san_pham_chi_tiet NVARCHAR(100),
    gia DECIMAL(12,2),
    so_luong INT,
    trong_luong DECIMAL(10,2),
    mo_ta NVARCHAR(255),
    trang_thai TINYINT,

    CONSTRAINT fk_ctsp_sp FOREIGN KEY (id_san_pham) REFERENCES san_pham(id),
    CONSTRAINT fk_ctsp_mau FOREIGN KEY (id_mau_sac) REFERENCES mau_sac(id),
    CONSTRAINT fk_ctsp_size FOREIGN KEY (id_kich_thuoc) REFERENCES kich_thuoc(id),
    CONSTRAINT fk_ctsp_co FOREIGN KEY (id_co_ao) REFERENCES co_ao(id),
    CONSTRAINT fk_ctsp_tay FOREIGN KEY (id_tay_ao) REFERENCES tay_ao(id)
);
GO

-- tạo bảng hình ảnh
CREATE TABLE hinh_anh (
    id INT IDENTITY PRIMARY KEY,
    ma_anh NVARCHAR(50),
    duong_dan_anh NVARCHAR(255),
    anh_mac_dinh BIT,
    mo_ta NVARCHAR(255),
    trang_thai TINYINT
);
GO

-- tạo bảng liên kết chi tiết sản phẩm - hình ảnh
CREATE TABLE hspct_hinh_anh (
    id INT IDENTITY PRIMARY KEY,
    id_san_pham_chi_tiet INT,
    id_hinh_anh INT,

    CONSTRAINT fk_ha_ctsp FOREIGN KEY (id_san_pham_chi_tiet) REFERENCES chi_tiet_san_pham(id),
    CONSTRAINT fk_ha_anh FOREIGN KEY (id_hinh_anh) REFERENCES hinh_anh(id)
);
GO

-- =============================================
-- BẢNG VAI TRÒ - NHÂN VIÊN - KHÁCH HÀNG
-- =============================================

-- tạo bảng vai trò
CREATE TABLE vai_tro (
    id INT IDENTITY PRIMARY KEY,
    ten NVARCHAR(50),
    mo_ta NVARCHAR(255)
);
GO

-- tạo bảng nhân viên
CREATE TABLE nhan_vien (
    id INT IDENTITY PRIMARY KEY,
    id_vai_tro INT,
    ma_nhan_vien NVARCHAR(50),
    ho_va_ten NVARCHAR(255),
    hinh_anh NVARCHAR(255),
    gioi_tinh NVARCHAR(10),
    ngay_sinh DATE,
    so_dien_thoai NVARCHAR(20),
    can_cuoc_cong_dan NVARCHAR(20),
    email NVARCHAR(100),
    ten_tai_khoan NVARCHAR(50),
    mat_khau NVARCHAR(255),
    chuc_vu NVARCHAR(100),
    trang_thai TINYINT,

    CONSTRAINT fk_nv_vai_tro FOREIGN KEY (id_vai_tro) REFERENCES vai_tro(id)
);
GO

-- tạo bảng khách hàng
CREATE TABLE khach_hang (
    id INT IDENTITY PRIMARY KEY,
    ma_khach_hang NVARCHAR(50),
    ten_tai_khoan NVARCHAR(50),
    mat_khau NVARCHAR(255),
    ten_khach_hang NVARCHAR(255),
    email NVARCHAR(100),
    gioi_tinh NVARCHAR(10),
    sdt NVARCHAR(20),
    ngay_sinh DATE,
    ghi_chu NVARCHAR(255),
    hinh_anh NVARCHAR(255),
    trang_thai TINYINT
);
GO

-- tạo bảng địa chỉ (quản lý nhiều địa chỉ của khách hàng)
CREATE TABLE dia_chi (
    id INT IDENTITY PRIMARY KEY,
    id_khach_hang INT,
    ten_nguoi_nhan NVARCHAR(255),
    sdt_nguoi_nhan NVARCHAR(20),
    tinh_thanh_pho NVARCHAR(100),
    quan_huyen NVARCHAR(100),
    xa_phuong NVARCHAR(100),
    dia_chi_cu_the NVARCHAR(255),
    anh_mac_dinh BIT DEFAULT 0,   -- địa chỉ mặc định
    trang_thai TINYINT,

    CONSTRAINT fk_dc_kh FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id)
);
GO

-- =============================================
-- BẢNG GIỏ HÀNG (tách ra 2 bảng)
-- =============================================

-- tạo bảng giỏ hàng
CREATE TABLE gio_hang (
    id INT IDENTITY PRIMARY KEY,
    id_khach_hang INT,
    ngay_tao DATETIME2,
    ngay_cap_nhat DATETIME2,
    trang_thai TINYINT,

    CONSTRAINT fk_gh_kh FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id)
);
GO

-- tạo bảng giỏ hàng chi tiết
CREATE TABLE gio_hang_chi_tiet (
    id INT IDENTITY PRIMARY KEY,
    id_gio_hang INT,
    id_san_pham_chi_tiet INT,
    gia DECIMAL(12,2),
    so_luong INT,
    ghi_chu NVARCHAR(255),
    trang_thai TINYINT,

    CONSTRAINT fk_ghct_gh FOREIGN KEY (id_gio_hang) REFERENCES gio_hang(id),
    CONSTRAINT fk_ghct_ctsp FOREIGN KEY (id_san_pham_chi_tiet) REFERENCES chi_tiet_san_pham(id)
);
GO

-- =============================================
-- BẢNG ĐỖT GIẢM GIÁ & PHIẾU GIẢM GIÁ
-- =============================================

-- tạo bảng đợt giảm giá
CREATE TABLE dot_giam_gia (
    id INT IDENTITY PRIMARY KEY,
    ma_dot_giam_gia NVARCHAR(50),
    ten_dot_giam_gia NVARCHAR(255),
    phan_tram_giam_gia DECIMAL(5,2),
    ngay_bat_dau DATE,
    ngay_ket_thuc DATE,
    ngay_tao DATE,
    ngay_sua DATE,
    mo_ta NVARCHAR(255),
    trang_thai TINYINT
);
GO

-- tạo bảng chi tiết đợt giảm giá
CREATE TABLE chi_tiet_dot_giam_gia (
    id INT IDENTITY PRIMARY KEY,
    id_dot_giam_gia INT,
    id_san_pham_chi_tiet INT,
    gia_truoc_khi_giam DECIMAL(12,2),
    gia_sau_khi_giam DECIMAL(12,2),
    ghi_chu NVARCHAR(255),
    ngay_tao DATE,
    trang_thai TINYINT,

    CONSTRAINT fk_ctdgg_dgg FOREIGN KEY (id_dot_giam_gia) REFERENCES dot_giam_gia(id),
    CONSTRAINT fk_ctdgg_ctsp FOREIGN KEY (id_san_pham_chi_tiet) REFERENCES chi_tiet_san_pham(id)
);
GO

-- tạo bảng phiếu giảm giá (coupon)
CREATE TABLE phieu_giam_gia (
    id INT IDENTITY PRIMARY KEY,
    ma_phieu_giam_gia NVARCHAR(50),
    dieu_kien_giam DECIMAL(12,2),
    ten_phieu NVARCHAR(255),
    loai_phieu NVARCHAR(50),            -- 'phan_tram' hoặc 'so_tien'
    phan_tram_giam_gia DECIMAL(5,2),
    so_tien_giam DECIMAL(12,2),
    giam_toi_da DECIMAL(12,2),
    so_luong_toi_da INT,                -- tổng số lần coupon này có thể dùng
    so_luong_da_dung INT DEFAULT 0,     -- số lần đã được dùng
    ngay_bat_dau DATE,
    ngay_ket_thuc DATE,
    ngay_tao DATE,
    ngay_cap_nhat DATE,
    ghi_chu NVARCHAR(255),
    trang_thai TINYINT
);
GO

-- tạo bảng chi tiết phiếu giảm giá (gắn coupon với khách hàng)
CREATE TABLE chi_tiet_phieu_giam_gia (
    id INT IDENTITY PRIMARY KEY,
    id_khach_hang INT,
    id_phieu_giam_gia INT,
    ngay_dung DATE,                     -- ngày khách hàng dùng coupon
    trang_thai TINYINT,                 -- 0: chưa dùng, 1: đã dùng

    CONSTRAINT fk_ctpg_kh FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id),
    CONSTRAINT fk_ctpg_pg FOREIGN KEY (id_phieu_giam_gia) REFERENCES phieu_giam_gia(id)
);
GO

-- =============================================
-- BẢNG HÓA ĐƠN
-- =============================================

-- tạo bảng hóa đơn
CREATE TABLE hoa_don (
    id INT IDENTITY PRIMARY KEY,
    id_nhan_vien INT,
    id_khach_hang INT,
    id_phieu_giam_gia INT,              -- FK đã được thêm
    id_dia_chi INT,                     -- địa chỉ giao hàng (tham chiếu bảng dia_chi)
    ma_hoa_don NVARCHAR(50),
    ngay_tao DATETIME2,
    tong_tien DECIMAL(12,2),            -- tổng giá trước giảm giá
    giam_gia DECIMAL(12,2),             -- số tiền giảm giá
    tong_tien_sau_giam DECIMAL(12,2),   -- tổng sau giảm giá
    phi_van_chuyen DECIMAL(12,2),
    tong_hoa_don DECIMAL(12,2),         -- tổng cuối cùng (sau giảm giá + phí vận chuyển)
    ten_khach_hang NVARCHAR(255),
    ngay_dat DATE,
    ngay_giao_du_kien DATE,
    sdt NVARCHAR(20),
    dia_chi NVARCHAR(255),              -- backup địa chỉ giao (text)
    ghi_chu NVARCHAR(255),
    trang_thai TINYINT,

    CONSTRAINT fk_hd_nv FOREIGN KEY (id_nhan_vien) REFERENCES nhan_vien(id),
    CONSTRAINT fk_hd_kh FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id),
    CONSTRAINT fk_hd_pgg FOREIGN KEY (id_phieu_giam_gia) REFERENCES phieu_giam_gia(id),
    CONSTRAINT fk_hd_dc FOREIGN KEY (id_dia_chi) REFERENCES dia_chi(id)
);
GO

-- tạo bảng hóa đơn chi tiết
CREATE TABLE hoa_don_chi_tiet (
    id INT IDENTITY PRIMARY KEY,
    id_hoa_don INT,
    id_san_pham_chi_tiet INT,
    gia DECIMAL(12,2),
    so_luong INT,
    thanh_tien DECIMAL(12,2),
    ghi_chu NVARCHAR(255),
    trang_thai TINYINT,

    CONSTRAINT fk_hdct_hd FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id),
    CONSTRAINT fk_hdct_ctsp FOREIGN KEY (id_san_pham_chi_tiet) REFERENCES chi_tiet_san_pham(id)
);
GO

-- =============================================
-- BẢNG THANH TOÁN
-- =============================================

-- tạo bảng hình thức thanh toán
CREATE TABLE hinh_thuc_thanh_toan (
    id INT IDENTITY PRIMARY KEY,
    ma_hinh_thuc NVARCHAR(50),
    phuong_thuc_thanh_toan NVARCHAR(100),   -- VNPay, Momo, COD, ...
    mo_ta NVARCHAR(255),
    ngay_tao DATE,
    ngay_cap_nhat DATE,
    trang_thai TINYINT
);
GO

-- tạo bảng chi tiết thanh toán
CREATE TABLE chi_tiet_thanh_toan (
    id INT IDENTITY PRIMARY KEY,
    id_hoa_don INT,
    id_hinh_thuc_thanh_toan INT,
    ma_giao_dich NVARCHAR(100),
    so_tien_thanh_toan DECIMAL(12,2),
    ngay_thanh_toan DATETIME2,
    trang_thai_thanh_toan NVARCHAR(50),     -- 'cho_thanh_toan', 'thanh_toan_ok', 'that_bai', ...
    ghi_chu NVARCHAR(255),

    CONSTRAINT fk_cttt_hd FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id),
    CONSTRAINT fk_cttt_httt FOREIGN KEY (id_hinh_thuc_thanh_toan) REFERENCES hinh_thuc_thanh_toan(id)
);
GO

-- =============================================
-- BẢNG GIA0 HÀNG (mới thêm)
-- =============================================

-- tạo bảng đơn vận chuyển
CREATE TABLE don_van_chuyen (
    id INT IDENTITY PRIMARY KEY,
    id_hoa_don INT,
    ma_don_van_chuyen NVARCHAR(100),        -- mã theo dõi từ shipper
    ten_nguoi_giao NVARCHAR(255),
    sdt_nguoi_giao NVARCHAR(20),
    ten_nguoi_nhan NVARCHAR(255),
    sdt_nguoi_nhan NVARCHAR(20),
    dia_chi_giao NVARCHAR(255),
    ngay_giao_thực_te DATE,                 -- ngày giao thực tế
    ngay_giao_du_kien DATE,
    trang_thai TINYINT,                     -- 0: chưa gửi, 1: đang giao, 2: đã giao, 3: thất lạc, 4: hoàn trả
    ghi_chu NVARCHAR(255),

    CONSTRAINT fk_dvc_hd FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id)
);
GO

-- =============================================
-- BẢNG LỨ SỬ HÓA ĐƠN (sửa: thêm timestamp)
-- =============================================

-- tạo bảng lịch sử hóa đơn
CREATE TABLE lich_su_hoa_don (
    id INT IDENTITY PRIMARY KEY,
    id_hoa_don INT,
    ma_lich_su NVARCHAR(50),
    noi_dung_thay_doi NVARCHAR(255),
    nguoi_thuc_hien NVARCHAR(255),
    ngay_thay_doi DATETIME2,               -- timestamp của thay đổi
    ghi_chu NVARCHAR(255),
    trang_thai TINYINT,

    CONSTRAINT fk_ls_hd FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id)
);
GO

-- =============================================
-- BẢNG ĐÁNH GIÁ SẢN PHẨM (mới thêm)
-- =============================================

-- tạo bảng đánh giá sản phẩm
CREATE TABLE danh_gia (
    id INT IDENTITY PRIMARY KEY,
    id_khach_hang INT,
    id_san_pham INT,                        -- đánh giá ở cấp sản phẩm chính
    id_hoa_don INT,                         -- xác minh đã mua
    diem_danh_gia TINYINT,                  -- 1 - 5
    tieu_de NVARCHAR(255),
    noi_dung NVARCHAR(MAX),
    hinh_anh NVARCHAR(MAX),                 -- đường dẫn các ảnh đánh giá (JSON array hoặc comma-separated)
    ngay_tao DATETIME2,
    ngay_cap_nhat DATETIME2,
    trang_thai TINYINT,                     -- 0: chờ duyệt, 1: đã duyệt, 2: ẩn

    CONSTRAINT fk_dg_kh FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id),
    CONSTRAINT fk_dg_sp FOREIGN KEY (id_san_pham) REFERENCES san_pham(id),
    CONSTRAINT fk_dg_hd FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id)
);
GO

-- =============================================
-- BẢNG THÔNG BÁO (mới thêm)
-- =============================================

-- tạo bảng thông báo
CREATE TABLE thong_bao (
    id INT IDENTITY PRIMARY KEY,
    id_khach_hang INT,
    loai_thong_bao NVARCHAR(50),            -- 'dat_hang', 'giao_hang', 'coupon', 'khuyen_mai', ...
    tieu_de NVARCHAR(255),
    noi_dung NVARCHAR(MAX),
    da_doc BIT DEFAULT 0,                   -- đã đọc chưa
    ngay_tao DATETIME2,
    trang_thai TINYINT,

    CONSTRAINT fk_tb_kh FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id)
);
GO