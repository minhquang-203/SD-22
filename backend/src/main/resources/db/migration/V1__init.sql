/* ============================================================================
   SUNOVA - Hệ thống quản lý cửa hàng kem chống nắng
   SQL Server - Schema đầy đủ + dữ liệu mẫu
   ----------------------------------------------------------------------------
   - 3 vai trò: Quản lý, Nhân viên, Khách hàng
   - Bán hàng cả 2 kênh: ONLINE + TẠI QUẦY
   - Đã siết khóa ngoại + CHECK chặt chẽ
   - Bảng ảnh gắn rõ ràng vào sản phẩm (và tùy chọn vào biến thể)
   - Mỗi bảng có ~5 dòng dữ liệu mẫu
   - san_pham.noi_bat: đánh dấu sản phẩm nổi bật (BIT, mặc định 0)
   ============================================================================ */


/* ===========================================================================
   1. NHÓM PHÂN QUYỀN & NGƯỜI DÙNG
   =========================================================================== */

CREATE TABLE vai_tro (
    id          INT IDENTITY(1,1) PRIMARY KEY,
    ma_vai_tro  VARCHAR(20)  NOT NULL UNIQUE,
    ten_vai_tro NVARCHAR(50) NOT NULL,
    mo_ta       NVARCHAR(255)
);
GO

CREATE TABLE loai_da (
    id    INT IDENTITY(1,1) PRIMARY KEY,
    ma    VARCHAR(20)  NOT NULL UNIQUE,
    ten   NVARCHAR(50) NOT NULL,
    mo_ta NVARCHAR(255)
);
GO

CREATE TABLE nhan_vien (
    id            INT IDENTITY(1,1) PRIMARY KEY,
    id_vai_tro    INT NOT NULL,
    ma_nhan_vien  VARCHAR(20)  NOT NULL UNIQUE,
    ho_ten        NVARCHAR(100) NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    so_dien_thoai VARCHAR(15),
    mat_khau      VARCHAR(255) NOT NULL,
    gioi_tinh     VARCHAR(10) DEFAULT 'Khac',
    ngay_vao_lam  DATE,
    trang_thai    BIT DEFAULT 1,
    CONSTRAINT fk_nv_vaitro FOREIGN KEY (id_vai_tro) REFERENCES vai_tro(id)
);
GO

CREATE TABLE khach_hang (
    id            INT IDENTITY(1,1) PRIMARY KEY,
    id_vai_tro    INT NOT NULL,
    ma_khach_hang VARCHAR(20)  NOT NULL UNIQUE,
    ho_ten        NVARCHAR(100) NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    so_dien_thoai VARCHAR(15),
    mat_khau      VARCHAR(255) NOT NULL,
    gioi_tinh     VARCHAR(10) DEFAULT 'Khac',
    ngay_sinh     DATE,
    id_loai_da    INT,
    diem_tich_luy INT DEFAULT 0,
    trang_thai    BIT DEFAULT 1,
    ngay_tao      DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_kh_vaitro FOREIGN KEY (id_vai_tro) REFERENCES vai_tro(id),
    CONSTRAINT fk_kh_loaida FOREIGN KEY (id_loai_da) REFERENCES loai_da(id)
);
GO

CREATE TABLE dia_chi_khach_hang (
    id                INT IDENTITY(1,1) PRIMARY KEY,
    id_khach_hang     INT NOT NULL,
    ho_ten_nguoi_nhan NVARCHAR(100) NOT NULL,
    so_dien_thoai     VARCHAR(15) NOT NULL,
    tinh_thanh        NVARCHAR(50),
    quan_huyen        NVARCHAR(50),
    phuong_xa         NVARCHAR(50),
    dia_chi_chi_tiet  NVARCHAR(255),
    mac_dinh          BIT DEFAULT 0,
    CONSTRAINT fk_dc_kh FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id)
);
GO

/* ===========================================================================
   2. NHÓM DANH MỤC & THUỘC TÍNH SẢN PHẨM
   =========================================================================== */

CREATE TABLE thuong_hieu (
    id         INT IDENTITY(1,1) PRIMARY KEY,
    ma         VARCHAR(20)  NOT NULL UNIQUE,
    ten        NVARCHAR(100) NOT NULL,
    xuat_xu    NVARCHAR(50),
    trang_thai BIT DEFAULT 1
);
GO

CREATE TABLE danh_muc (
    id         INT IDENTITY(1,1) PRIMARY KEY,
    ma         VARCHAR(20)  NOT NULL UNIQUE,
    ten        NVARCHAR(80) NOT NULL,
    mo_ta      NVARCHAR(255),
    trang_thai BIT DEFAULT 1
);
GO

CREATE TABLE dang_san_pham (
    id    INT IDENTITY(1,1) PRIMARY KEY,
    ma    VARCHAR(20)  NOT NULL UNIQUE,
    ten   NVARCHAR(50) NOT NULL,
    mo_ta NVARCHAR(255)
);
GO

CREATE TABLE cong_dung (
    id    INT IDENTITY(1,1) PRIMARY KEY,
    ma    VARCHAR(20)  NOT NULL UNIQUE,
    ten   NVARCHAR(50) NOT NULL,
    mo_ta NVARCHAR(255)
);
GO

CREATE TABLE thanh_phan (
    id    INT IDENTITY(1,1) PRIMARY KEY,
    ma    VARCHAR(30)  NOT NULL UNIQUE,
    ten   NVARCHAR(80) NOT NULL,
    loai  VARCHAR(10),
    mo_ta NVARCHAR(255)
);
GO

CREATE TABLE mau_sac (
    id     INT IDENTITY(1,1) PRIMARY KEY,
    ma     VARCHAR(20)  NOT NULL UNIQUE,
    ten    NVARCHAR(50) NOT NULL,
    ma_hex VARCHAR(7)
);
GO

/* ===========================================================================
   3. NHÓM SẢN PHẨM
   =========================================================================== */

CREATE TABLE san_pham (
    id               INT IDENTITY(1,1) PRIMARY KEY,
    ma_san_pham      VARCHAR(30)  NOT NULL UNIQUE,
    ten              NVARCHAR(200) NOT NULL,
    id_thuong_hieu   INT NOT NULL,
    id_danh_muc      INT NOT NULL,
    id_dang_san_pham INT NOT NULL,
    chi_so_spf       VARCHAR(10),
    chi_so_pa        VARCHAR(6),
    loai_chong_nang  VARCHAR(10),          -- VAT_LY / HOA_HOC / LAI
    khang_nuoc       BIT DEFAULT 0,
    mo_ta            NVARCHAR(MAX),
    trang_thai       BIT DEFAULT 1,
    noi_bat          BIT NOT NULL DEFAULT 0,
    ngay_tao         DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sp_th   FOREIGN KEY (id_thuong_hieu)   REFERENCES thuong_hieu(id),
    CONSTRAINT fk_sp_dm   FOREIGN KEY (id_danh_muc)      REFERENCES danh_muc(id),
    CONSTRAINT fk_sp_dang FOREIGN KEY (id_dang_san_pham) REFERENCES dang_san_pham(id)
);
GO

-- Bảng nối: 1 sản phẩm phù hợp nhiều loại da
CREATE TABLE san_pham_loai_da (
    id          INT IDENTITY(1,1) PRIMARY KEY,
    id_san_pham INT NOT NULL,
    id_loai_da  INT NOT NULL,
    CONSTRAINT fk_spld_sp FOREIGN KEY (id_san_pham) REFERENCES san_pham(id),
    CONSTRAINT fk_spld_ld FOREIGN KEY (id_loai_da)  REFERENCES loai_da(id),
    CONSTRAINT uq_spld UNIQUE (id_san_pham, id_loai_da)
);
GO

-- Bảng nối: 1 sản phẩm có nhiều công dụng
CREATE TABLE san_pham_cong_dung (
    id           INT IDENTITY(1,1) PRIMARY KEY,
    id_san_pham  INT NOT NULL,
    id_cong_dung INT NOT NULL,
    CONSTRAINT fk_spcd_sp FOREIGN KEY (id_san_pham)  REFERENCES san_pham(id),
    CONSTRAINT fk_spcd_cd FOREIGN KEY (id_cong_dung) REFERENCES cong_dung(id),
    CONSTRAINT uq_spcd UNIQUE (id_san_pham, id_cong_dung)
);
GO

-- Bảng nối: 1 sản phẩm có nhiều thành phần
CREATE TABLE san_pham_thanh_phan (
    id            INT IDENTITY(1,1) PRIMARY KEY,
    id_san_pham   INT NOT NULL,
    id_thanh_phan INT NOT NULL,
    CONSTRAINT fk_sptp_sp FOREIGN KEY (id_san_pham)   REFERENCES san_pham(id),
    CONSTRAINT fk_sptp_tp FOREIGN KEY (id_thanh_phan) REFERENCES thanh_phan(id),
    CONSTRAINT uq_sptp UNIQUE (id_san_pham, id_thanh_phan)
);
GO

-- Biến thể: GIÁ + TỒN KHO nằm ở đây
CREATE TABLE chi_tiet_san_pham (
    id           INT IDENTITY(1,1) PRIMARY KEY,
    id_san_pham  INT NOT NULL,
    id_mau_sac   INT,
    sku          VARCHAR(40) NOT NULL UNIQUE,
    dung_tich_ml DECIMAL(6,1),
    gia_ban      DECIMAL(12,0) NOT NULL,
    so_luong_ton INT DEFAULT 0,
    trang_thai   BIT DEFAULT 1,
    CONSTRAINT fk_cts_sp FOREIGN KEY (id_san_pham) REFERENCES san_pham(id),
    CONSTRAINT fk_cts_ms FOREIGN KEY (id_mau_sac)  REFERENCES mau_sac(id),
    CONSTRAINT ck_cts_gia CHECK (gia_ban >= 0),
    CONSTRAINT ck_cts_ton CHECK (so_luong_ton >= 0)
);
GO

-- LÔ HÀNG: mỗi lần nhập 1 biến thể = 1 lô, có HSD + ngày nhập riêng. Tồn kho = tổng so_luong_con các lô.
CREATE TABLE lo_hang (
    id                   INT IDENTITY(1,1) PRIMARY KEY,
    id_chi_tiet_san_pham INT NOT NULL,
    so_lo                VARCHAR(40) NOT NULL,        -- số lô của nhà sản xuất
    ngay_nhap            DATE NOT NULL,
    han_su_dung          DATE,                        -- HSD của riêng lô này
    so_luong_nhap        INT NOT NULL,                -- nhập vào bao nhiêu
    so_luong_con         INT NOT NULL,                -- còn lại (trừ dần khi bán)
    ghi_chu              NVARCHAR(255),
    trang_thai           BIT DEFAULT 1,
    CONSTRAINT fk_lo_cts FOREIGN KEY (id_chi_tiet_san_pham) REFERENCES chi_tiet_san_pham(id),
    CONSTRAINT ck_lo_sln CHECK (so_luong_nhap >= 0),
    CONSTRAINT ck_lo_slc CHECK (so_luong_con >= 0)
);
GO

/* Ảnh sản phẩm — ĐÃ SIẾT CHẶT:
   - Luôn gắn với 1 sản phẩm (id_san_pham, bắt buộc).
   - Có thể gắn thêm 1 biến thể cụ thể (id_chi_tiet_san_pham, tùy chọn)
     để hiển thị ảnh riêng cho từng màu/dung tích. NULL = ảnh chung sản phẩm. */
CREATE TABLE anh_san_pham (
    id                   INT IDENTITY(1,1) PRIMARY KEY,
    id_san_pham          INT NOT NULL,
    id_chi_tiet_san_pham INT NULL,
    url                  VARCHAR(255) NOT NULL,
    la_anh_chinh         BIT DEFAULT 0,
    thu_tu               INT DEFAULT 0,
    CONSTRAINT fk_anh_sp  FOREIGN KEY (id_san_pham)          REFERENCES san_pham(id),
    CONSTRAINT fk_anh_cts FOREIGN KEY (id_chi_tiet_san_pham) REFERENCES chi_tiet_san_pham(id)
);
GO

/* ===========================================================================
   4. NHÓM QUIZ XÁC ĐỊNH LOẠI DA
   =========================================================================== */

CREATE TABLE cau_hoi_quiz (
    id         INT IDENTITY(1,1) PRIMARY KEY,
    noi_dung   NVARCHAR(255) NOT NULL,
    thu_tu     INT DEFAULT 0,
    trang_thai BIT DEFAULT 1
);
GO

CREATE TABLE dap_an_quiz (
    id         INT IDENTITY(1,1) PRIMARY KEY,
    id_cau_hoi INT NOT NULL,
    noi_dung   NVARCHAR(255) NOT NULL,
    id_loai_da INT NOT NULL,
    diem       INT DEFAULT 1,
    icon       NVARCHAR(100) NULL,
    CONSTRAINT fk_da_ch FOREIGN KEY (id_cau_hoi) REFERENCES cau_hoi_quiz(id),
    CONSTRAINT fk_da_ld FOREIGN KEY (id_loai_da) REFERENCES loai_da(id)
);
GO

CREATE TABLE ket_qua_quiz (
    id                 INT IDENTITY(1,1) PRIMARY KEY,
    id_khach_hang      INT NOT NULL,
    id_loai_da_ket_qua INT NOT NULL,
    tong_diem          INT,
    thoi_gian          DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_kq_kh FOREIGN KEY (id_khach_hang)      REFERENCES khach_hang(id),
    CONSTRAINT fk_kq_ld FOREIGN KEY (id_loai_da_ket_qua) REFERENCES loai_da(id)
);
GO

/* ===========================================================================
   5. NHÓM CHAT AI & FAQ
   =========================================================================== */

CREATE TABLE cau_hoi_thuong_gap (
    id         INT IDENTITY(1,1) PRIMARY KEY,
    cau_hoi    NVARCHAR(255) NOT NULL,
    tra_loi    NVARCHAR(MAX) NOT NULL,
    chu_de     NVARCHAR(50),
    trang_thai BIT DEFAULT 1
);
GO

CREATE TABLE phien_chat_ai (
    id                INT IDENTITY(1,1) PRIMARY KEY,
    id_khach_hang     INT,
    tieu_de           NVARCHAR(150),
    thoi_gian_bat_dau DATETIME DEFAULT CURRENT_TIMESTAMP,
    trang_thai        VARCHAR(15) DEFAULT 'DANG_MO',
    CONSTRAINT fk_pc_kh FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id)
);
GO

CREATE TABLE tin_nhan_chat_ai (
    id                INT IDENTITY(1,1) PRIMARY KEY,
    id_phien          INT NOT NULL,
    nguoi_gui         VARCHAR(10) NOT NULL,      -- KHACH / AI
    noi_dung          NVARCHAR(MAX) NOT NULL,
    id_san_pham_goi_y INT,
    thoi_gian         DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tn_phien FOREIGN KEY (id_phien)          REFERENCES phien_chat_ai(id),
    CONSTRAINT fk_tn_sp    FOREIGN KEY (id_san_pham_goi_y) REFERENCES san_pham(id)
);
GO

/* ===========================================================================
   6. NHÓM UV / THỜI TIẾT & GỢI Ý
   =========================================================================== */

CREATE TABLE du_lieu_uv (
    id                 INT IDENTITY(1,1) PRIMARY KEY,
    tinh_thanh         NVARCHAR(50) NOT NULL,
    ngay               DATE NOT NULL,
    chi_so_uv          DECIMAL(4,1) NOT NULL,
    muc_do             VARCHAR(15) NOT NULL,     -- THAP / TRUNG_BINH / CAO / RAT_CAO
    spf_khuyen_nghi    VARCHAR(10),
    nguon_du_lieu      VARCHAR(50),
    thoi_gian_cap_nhat DATETIME DEFAULT CURRENT_TIMESTAMP
);
GO

CREATE TABLE luat_goi_y (
    id            INT IDENTITY(1,1) PRIMARY KEY,
    ten_luat      NVARCHAR(100) NOT NULL,
    id_loai_da    INT,
    id_cong_dung  INT,
    uv_tu         DECIMAL(4,1),
    uv_den        DECIMAL(4,1),
    spf_toi_thieu VARCHAR(10),
    do_uu_tien    INT DEFAULT 1,
    trang_thai    BIT DEFAULT 1,
    CONSTRAINT fk_lgy_ld FOREIGN KEY (id_loai_da)   REFERENCES loai_da(id),
    CONSTRAINT fk_lgy_cd FOREIGN KEY (id_cong_dung) REFERENCES cong_dung(id)
);
GO

CREATE TABLE lich_su_hanh_vi (
    id            INT IDENTITY(1,1) PRIMARY KEY,
    id_khach_hang INT NOT NULL,
    id_san_pham   INT,
    loai_hanh_vi  VARCHAR(10) NOT NULL,          -- XEM / THEM_GIO / MUA / QUIZ / YEU_THICH
    thoi_gian     DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_hv_kh FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id),
    CONSTRAINT fk_hv_sp FOREIGN KEY (id_san_pham)   REFERENCES san_pham(id)
);
GO

CREATE TABLE san_pham_yeu_thich (
    id            INT IDENTITY(1,1) PRIMARY KEY,
    id_khach_hang INT NOT NULL,
    id_san_pham   INT NOT NULL,
    ngay_them     DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_yt_kh FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id),
    CONSTRAINT fk_yt_sp FOREIGN KEY (id_san_pham)   REFERENCES san_pham(id),
    CONSTRAINT uq_yt UNIQUE (id_khach_hang, id_san_pham)
);
GO

CREATE TABLE routine (
    id         INT IDENTITY(1,1) PRIMARY KEY,
    ten        NVARCHAR(120) NOT NULL,
    mo_ta      NVARCHAR(255),
    id_loai_da INT,
    tong_gia   DECIMAL(12,0),
    trang_thai BIT DEFAULT 1,
    CONSTRAINT fk_rt_ld FOREIGN KEY (id_loai_da) REFERENCES loai_da(id)
);
GO

CREATE TABLE chi_tiet_routine (
    id          INT IDENTITY(1,1) PRIMARY KEY,
    id_routine  INT NOT NULL,
    id_san_pham INT NOT NULL,
    buoc        INT DEFAULT 1,
    ghi_chu     NVARCHAR(255),
    CONSTRAINT fk_ctrt_rt FOREIGN KEY (id_routine)  REFERENCES routine(id),
    CONSTRAINT fk_ctrt_sp FOREIGN KEY (id_san_pham) REFERENCES san_pham(id)
);
GO

/* ===========================================================================
   7. NHÓM ĐÁNH GIÁ
   =========================================================================== */

CREATE TABLE danh_gia (
    id            INT IDENTITY(1,1) PRIMARY KEY,
    id_khach_hang INT NOT NULL,
    id_san_pham   INT NOT NULL,
    so_sao        TINYINT NOT NULL,
    noi_dung      NVARCHAR(500),
    trang_thai    VARCHAR(15) DEFAULT 'CHO_DUYET',   -- CHO_DUYET / DA_DUYET / TU_CHOI
    ngay_tao      DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dg_kh FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id),
    CONSTRAINT fk_dg_sp FOREIGN KEY (id_san_pham)   REFERENCES san_pham(id),
    CONSTRAINT ck_dg_sao CHECK (so_sao BETWEEN 1 AND 5)
);
GO

/* ===========================================================================
   8. NHÓM GIỎ HÀNG
   =========================================================================== */

CREATE TABLE gio_hang (
    id            INT IDENTITY(1,1) PRIMARY KEY,
    id_khach_hang INT NOT NULL,
    ngay_tao      DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_gh_kh FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id)
);
GO

CREATE TABLE chi_tiet_gio_hang (
    id                   INT IDENTITY(1,1) PRIMARY KEY,
    id_gio_hang          INT NOT NULL,
    id_chi_tiet_san_pham INT NOT NULL,
    so_luong             INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_ctgh_gh  FOREIGN KEY (id_gio_hang)          REFERENCES gio_hang(id),
    CONSTRAINT fk_ctgh_cts FOREIGN KEY (id_chi_tiet_san_pham) REFERENCES chi_tiet_san_pham(id),
    CONSTRAINT ck_ctgh_sl CHECK (so_luong > 0)
);
GO

/* ===========================================================================
   9. NHÓM THANH TOÁN & KHUYẾN MÃI
   =========================================================================== */

CREATE TABLE phuong_thuc_thanh_toan (
    id         INT IDENTITY(1,1) PRIMARY KEY,
    ma         VARCHAR(20)  NOT NULL UNIQUE,
    ten        NVARCHAR(50) NOT NULL,
    trang_thai BIT DEFAULT 1
);
GO

CREATE TABLE dot_giam_gia (
    id             INT IDENTITY(1,1) PRIMARY KEY,
    ma             VARCHAR(30)  NOT NULL UNIQUE,
    ten            NVARCHAR(100) NOT NULL,
    phan_tram_giam DECIMAL(5,2),
    ngay_bat_dau   DATETIME,
    ngay_ket_thuc  DATETIME,
    trang_thai     BIT DEFAULT 1
);
GO

CREATE TABLE chi_tiet_dot_giam_gia (
    id                   INT IDENTITY(1,1) PRIMARY KEY,
    id_dot_giam_gia      INT NOT NULL,
    id_chi_tiet_san_pham INT NOT NULL,
    gia_sau_giam         DECIMAL(12,0),
    CONSTRAINT fk_ctdgg_dgg FOREIGN KEY (id_dot_giam_gia)      REFERENCES dot_giam_gia(id),
    CONSTRAINT fk_ctdgg_cts FOREIGN KEY (id_chi_tiet_san_pham) REFERENCES chi_tiet_san_pham(id)
);
GO

CREATE TABLE phieu_giam_gia (
    id                    INT IDENTITY(1,1) PRIMARY KEY,
    ma                    VARCHAR(30)  NOT NULL UNIQUE,
    ten                   NVARCHAR(100),
    loai                  VARCHAR(10) NOT NULL,     -- PHAN_TRAM / TIEN_MAT
    gia_tri               DECIMAL(12,0) NOT NULL,
    gia_tri_don_toi_thieu DECIMAL(12,0) DEFAULT 0,
    giam_toi_da           DECIMAL(12,0),
    so_luong              INT,
    ngay_bat_dau          DATETIME,
    ngay_ket_thuc         DATETIME,
    trang_thai            BIT DEFAULT 1
);
GO

/* ===========================================================================
   10. NHÓM HÓA ĐƠN / ĐƠN HÀNG  (đã tích hợp người nhận + thanh toán)
   =========================================================================== */

CREATE TABLE hoa_don (
    id                        INT IDENTITY(1,1) PRIMARY KEY,
    ma_hoa_don                VARCHAR(30) NOT NULL UNIQUE,
    id_khach_hang             INT,                    -- NULL = khách lẻ tại quầy
    id_nhan_vien              INT,                    -- nhân viên lập đơn (POS)
    id_phuong_thuc_thanh_toan INT,                    -- phương thức chính
    id_phieu_giam_gia         INT,
    loai_don       VARCHAR(10) DEFAULT 'ONLINE',      -- ONLINE / TAI_QUAY
    trang_thai     VARCHAR(15) DEFAULT 'CHO_XAC_NHAN',
    ten_nguoi_nhan NVARCHAR(100),                     -- cho đơn ONLINE
    sdt_nguoi_nhan VARCHAR(15),                        -- cho đơn ONLINE
    dia_chi_giao   NVARCHAR(255),
    tong_tien      DECIMAL(12,0) DEFAULT 0,
    tien_giam_gia  DECIMAL(12,0) DEFAULT 0,
    phi_van_chuyen DECIMAL(12,0) DEFAULT 0,
    thanh_tien     DECIMAL(12,0) DEFAULT 0,
    ghi_chu        NVARCHAR(255),
    ngay_tao       DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_hd_kh   FOREIGN KEY (id_khach_hang)             REFERENCES khach_hang(id),
    CONSTRAINT fk_hd_nv   FOREIGN KEY (id_nhan_vien)              REFERENCES nhan_vien(id),
    CONSTRAINT fk_hd_pttt FOREIGN KEY (id_phuong_thuc_thanh_toan) REFERENCES phuong_thuc_thanh_toan(id),
    CONSTRAINT fk_hd_pgg  FOREIGN KEY (id_phieu_giam_gia)         REFERENCES phieu_giam_gia(id)
);
GO

CREATE TABLE hoa_don_chi_tiet (
    id                   INT IDENTITY(1,1) PRIMARY KEY,
    id_hoa_don           INT NOT NULL,
    id_chi_tiet_san_pham INT NOT NULL,
    so_luong             INT NOT NULL,
    don_gia              DECIMAL(12,0) NOT NULL,
    thanh_tien           DECIMAL(12,0) NOT NULL,
    CONSTRAINT fk_hdct_hd  FOREIGN KEY (id_hoa_don)           REFERENCES hoa_don(id),
    CONSTRAINT fk_hdct_cts FOREIGN KEY (id_chi_tiet_san_pham) REFERENCES chi_tiet_san_pham(id),
    CONSTRAINT ck_hdct_sl CHECK (so_luong > 0)
);
GO

/* Thanh toán chi tiết: 1 hóa đơn có thể có nhiều lần trả (tiền mặt + chuyển khoản),
   lưu tiền khách đưa / tiền thối (POS) và mã giao dịch (online). */
CREATE TABLE thanh_toan_hoa_don (
    id                        INT IDENTITY(1,1) PRIMARY KEY,
    id_hoa_don                INT NOT NULL,
    id_phuong_thuc_thanh_toan INT NOT NULL,
    so_tien                   DECIMAL(12,0) NOT NULL,
    so_tien_khach_dua         DECIMAL(12,0),
    tien_thua                 DECIMAL(12,0),
    ma_giao_dich              VARCHAR(100),
    trang_thai                VARCHAR(15) DEFAULT 'THANH_CONG',
    thoi_gian                 DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tthd_hd   FOREIGN KEY (id_hoa_don)                REFERENCES hoa_don(id),
    CONSTRAINT fk_tthd_pttt FOREIGN KEY (id_phuong_thuc_thanh_toan) REFERENCES phuong_thuc_thanh_toan(id)
);
GO

CREATE TABLE lich_su_don_hang (
    id           INT IDENTITY(1,1) PRIMARY KEY,
    id_hoa_don   INT NOT NULL,
    trang_thai   VARCHAR(30) NOT NULL,
    ghi_chu      NVARCHAR(255),
    id_nhan_vien INT,
    thoi_gian    DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lsdh_hd FOREIGN KEY (id_hoa_don)   REFERENCES hoa_don(id),
    CONSTRAINT fk_lsdh_nv FOREIGN KEY (id_nhan_vien) REFERENCES nhan_vien(id)
);
GO

/* ===========================================================================
   11. NHÓM THÔNG BÁO
   =========================================================================== */

CREATE TABLE thong_bao (
    id            INT IDENTITY(1,1) PRIMARY KEY,
    tieu_de       NVARCHAR(150) NOT NULL,
    noi_dung      NVARCHAR(500),
    id_khach_hang INT,
    id_nhan_vien  INT,
    loai          VARCHAR(15) DEFAULT 'HE_THONG',   -- DON_HANG / KHUYEN_MAI / UV / HE_THONG
    da_doc        BIT DEFAULT 0,
    thoi_gian     DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tb_kh FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id),
    CONSTRAINT fk_tb_nv FOREIGN KEY (id_nhan_vien)  REFERENCES nhan_vien(id)
);
GO

/* ============================================================================
   ============================  DỮ LIỆU MẪU  =================================
   Ghi chú: mật khẩu để dạng '123456' cho dễ test. Khi tích hợp Spring Security
   nhớ thay bằng chuỗi đã mã hóa BCrypt.
   ============================================================================ */

-- 3 VAI TRÒ
INSERT INTO vai_tro (ma_vai_tro, ten_vai_tro, mo_ta) VALUES
('QUAN_LY',    N'Quản lý',    N'Toàn quyền quản trị hệ thống'),
('NHAN_VIEN',  N'Nhân viên',  N'Bán hàng, xử lý đơn'),
('KHACH_HANG', N'Khách hàng', N'Người mua hàng');
GO

-- LOẠI DA
INSERT INTO loai_da (ma, ten, mo_ta) VALUES
('DA_DAU',      N'Da dầu',       N'Da tiết nhiều dầu, dễ bóng'),
('DA_KHO',      N'Da khô',       N'Da thiếu ẩm, dễ căng'),
('DA_HON_HOP',  N'Da hỗn hợp',   N'Vùng chữ T dầu, hai má khô'),
('DA_THUONG',   N'Da thường',    N'Da cân bằng'),
('DA_NHAY_CAM', N'Da nhạy cảm',  N'Da dễ kích ứng');
GO

-- NHÂN VIÊN (1 quản lý + 4 nhân viên)
INSERT INTO nhan_vien (id_vai_tro, ma_nhan_vien, ho_ten, email, so_dien_thoai, mat_khau, gioi_tinh, ngay_vao_lam) VALUES
(1, 'NV01', N'Nguyễn Văn An',   'an.nv@sunova.vn',   '0901000001', '123456', 'Nam', '2024-01-15'),
(2, 'NV02', N'Trần Thị Bình',   'binh.nv@sunova.vn', '0901000002', '123456', N'Nữ', '2024-03-01'),
(2, 'NV03', N'Lê Văn Cường',    'cuong.nv@sunova.vn','0901000003', '123456', 'Nam', '2024-05-20'),
(2, 'NV04', N'Phạm Thị Dung',   'dung.nv@sunova.vn', '0901000004', '123456', N'Nữ', '2024-07-10'),
(2, 'NV05', N'Hoàng Văn Em',    'em.nv@sunova.vn',   '0901000005', '123456', 'Nam', '2025-01-05');
GO

-- KHÁCH HÀNG (vai trò 3)
INSERT INTO khach_hang (id_vai_tro, ma_khach_hang, ho_ten, email, so_dien_thoai, mat_khau, gioi_tinh, ngay_sinh, id_loai_da, diem_tich_luy) VALUES
(3, 'KH01', N'Đỗ Thị Hoa',    'hoa@gmail.com',   '0911000001', '123456', N'Nữ', '1998-04-12', 1, 120),
(3, 'KH02', N'Vũ Văn Khánh',  'khanh@gmail.com', '0911000002', '123456', 'Nam', '1995-09-23', 2, 80),
(3, 'KH03', N'Bùi Thị Lan',   'lan@gmail.com',   '0911000003', '123456', N'Nữ', '2000-12-01', 3, 200),
(3, 'KH04', N'Đặng Văn Minh', 'minh@gmail.com',  '0911000004', '123456', 'Nam', '1992-06-30', 4, 50),
(3, 'KH05', N'Ngô Thị Nga',   'nga@gmail.com',   '0911000005', '123456', N'Nữ', '1999-02-14', 5, 300);
GO

-- ĐỊA CHỈ KHÁCH HÀNG
INSERT INTO dia_chi_khach_hang (id_khach_hang, ho_ten_nguoi_nhan, so_dien_thoai, tinh_thanh, quan_huyen, phuong_xa, dia_chi_chi_tiet, mac_dinh) VALUES
(1, N'Đỗ Thị Hoa',    '0911000001', N'Hà Nội',     N'Cầu Giấy',  N'Dịch Vọng',   N'số 1 Trần Thái Tông', 1),
(2, N'Vũ Văn Khánh',  '0911000002', N'TP.HCM',     N'Quận 1',    N'Bến Nghé',    N'12 Lê Lợi',            1),
(3, N'Bùi Thị Lan',   '0911000003', N'Đà Nẵng',    N'Hải Châu',  N'Thạch Thang', N'45 Bạch Đằng',         1),
(4, N'Đặng Văn Minh', '0911000004', N'Hải Phòng',  N'Lê Chân',   N'An Biên',     N'8 Tô Hiệu',            1),
(5, N'Ngô Thị Nga',   '0911000005', N'Cần Thơ',    N'Ninh Kiều', N'Tân An',      N'90 Nguyễn Trãi',       1);
GO

-- THƯƠNG HIỆU
INSERT INTO thuong_hieu (ma, ten, xuat_xu) VALUES
('TH01', N'Anessa',          N'Nhật Bản'),
('TH02', N'La Roche-Posay',  N'Pháp'),
('TH03', N'Vichy',           N'Pháp'),
('TH04', N'Skin1004',        N'Hàn Quốc'),
('TH05', N'Sunplay',         N'Nhật Bản');
GO

-- DANH MỤC
INSERT INTO danh_muc (ma, ten, mo_ta) VALUES
('DM01', N'Chống nắng cho mặt',    N'Dùng cho vùng da mặt'),
('DM02', N'Chống nắng cơ thể',     N'Dùng cho toàn thân'),
('DM03', N'Chống nắng nâng tông',  N'Vừa chống nắng vừa nâng tông da'),
('DM04', N'Chống nắng cho da mụn', N'Phù hợp da mụn, không gây bít tắc'),
('DM05', N'Chống nắng trẻ em',     N'Dịu nhẹ cho trẻ');
GO

-- DẠNG SẢN PHẨM
INSERT INTO dang_san_pham (ma, ten, mo_ta) VALUES
('DSP01', N'Sữa chống nắng',  N'Kết cấu sữa lỏng nhẹ'),
('DSP02', N'Kem chống nắng',  N'Kết cấu kem'),
('DSP03', N'Gel chống nắng',  N'Kết cấu gel mát'),
('DSP04', N'Xịt chống nắng',  N'Dạng xịt tiện lợi'),
('DSP05', N'Thỏi chống nắng', N'Dạng thỏi lăn');
GO

-- CÔNG DỤNG
INSERT INTO cong_dung (ma, ten, mo_ta) VALUES
('CD01', N'Kiềm dầu',  N'Kiểm soát dầu nhờn'),
('CD02', N'Cấp ẩm',    N'Bổ sung độ ẩm'),
('CD03', N'Nâng tông', N'Làm sáng tông da'),
('CD04', N'Kháng nước',N'Chịu nước, mồ hôi'),
('CD05', N'Làm dịu',   N'Làm dịu da kích ứng');
GO

-- BỔ SUNG CÔNG DỤNG CỐT LÕI
INSERT INTO cong_dung (ma, ten, mo_ta) VALUES
('CD06', N'Chống tia UVA/UVB', N'Bảo vệ da khỏi tia cực tím'),
('CD07', N'Chống lão hóa',     N'Ngăn lão hóa do ánh nắng');
GO

-- THÀNH PHẦN
INSERT INTO thanh_phan (ma, ten, loai, mo_ta) VALUES
('TP01', N'Niacinamide',        'HOAT_CHAT', N'Làm sáng, kiềm dầu'),
('TP02', N'Hyaluronic Acid',    'HOAT_CHAT', N'Cấp ẩm sâu'),
('TP03', N'Zinc Oxide',         'CHONG_NANG',N'Lọc tia UV vật lý'),
('TP04', N'Titanium Dioxide',   'CHONG_NANG',N'Lọc tia UV vật lý'),
('TP05', N'Centella Asiatica',  'HOAT_CHAT', N'Làm dịu, phục hồi');
GO

-- MÀU SẮC
INSERT INTO mau_sac (ma, ten, ma_hex) VALUES
('MS01', N'Không màu (trong suốt)', '#FFFFFF'),
('MS02', N'Tông tự nhiên',          '#F5DEB3'),
('MS03', N'Tông sáng',              '#FFE4C4'),
('MS04', N'Hồng nhẹ',               '#FFD1DC'),
('MS05', N'Be',                     '#F5F5DC');
GO

-- SẢN PHẨM
INSERT INTO san_pham (ma_san_pham, ten, id_thuong_hieu, id_danh_muc, id_dang_san_pham, chi_so_spf, chi_so_pa, loai_chong_nang, khang_nuoc, mo_ta, noi_bat) VALUES
('SP001', N'Anessa Perfect UV Sunscreen Skin Care Milk', 1, 2, 1, 'SPF50+', 'PA++++', 'HOA_HOC', 1, N'Sữa chống nắng kháng nước nổi tiếng của Anessa', 1),
('SP002', N'La Roche-Posay Anthelios UVMune 400',        2, 1, 2, 'SPF50+', 'PA++++', 'HOA_HOC', 0, N'Kem chống nắng cho da nhạy cảm', 0),
('SP003', N'Skin1004 Madagascar Centella Air-fit Suncream',4, 1, 2, 'SPF50+','PA++++', 'VAT_LY', 0, N'Kem chống nắng chứa rau má, làm dịu da', 0),
('SP004', N'Sunplay Skin Aqua UV Spray',                 5, 2, 4, 'SPF50+', 'PA++++', 'HOA_HOC', 1, N'Xịt chống nắng tiện lợi cho cơ thể', 0),
('SP005', N'Vichy Capital Soleil UV Age Daily',          3, 3, 2, 'SPF50+', 'PA++++', 'HOA_HOC', 0, N'Kem chống nắng nâng tông, chống lão hóa', 0);
GO

-- SẢN PHẨM - LOẠI DA
INSERT INTO san_pham_loai_da (id_san_pham, id_loai_da) VALUES
(1, 1), (2, 5), (3, 5), (4, 1), (5, 4);
GO

-- SẢN PHẨM - CÔNG DỤNG
INSERT INTO san_pham_cong_dung (id_san_pham, id_cong_dung) VALUES
(1, 4), (2, 5), (3, 2), (4, 1), (5, 3);
GO

-- SẢN PHẨM - THÀNH PHẦN
INSERT INTO san_pham_thanh_phan (id_san_pham, id_thanh_phan) VALUES
(1, 3), (2, 4), (3, 5), (4, 1), (5, 2);
GO

-- BIẾN THỂ (CHI TIẾT SẢN PHẨM)
INSERT INTO chi_tiet_san_pham (id_san_pham, id_mau_sac, sku, dung_tich_ml, gia_ban, so_luong_ton) VALUES
(1, 1, 'SP001-MILK-60',  60, 520000, 80),
(2, 1, 'SP002-CREAM-50', 50, 495000, 60),
(3, 1, 'SP003-CREAM-50', 50, 350000, 100),
(4, 1, 'SP004-SPRAY-70', 70, 180000, 120),
(5, 2, 'SP005-CREAM-40', 40, 610000, 45);
GO

-- LÔ HÀNG (seed) — vài biến thể có 2 lô HSD khác nhau để minh họa
INSERT INTO lo_hang (id_chi_tiet_san_pham, so_lo, ngay_nhap, han_su_dung, so_luong_nhap, so_luong_con) VALUES
(1, 'LO001', '2024-11-15', '2026-11-30', 30, 30),
(1, 'LO002', '2025-03-01', '2027-06-30', 50, 50),
(2, 'LO003', '2025-04-01', '2027-03-31', 60, 60),
(3, 'LO004', '2025-05-01', '2027-09-30', 100, 100),
(4, 'LO005', '2025-02-01', '2026-12-31', 80, 80),
(4, 'LO006', '2025-07-01', '2027-08-08', 40, 40),
(5, 'LO007', '2025-05-15', '2027-05-31', 45, 45);
GO

-- ẢNH SẢN PHẨM (gắn sản phẩm; 2 dòng gắn thêm biến thể cụ thể)
INSERT INTO anh_san_pham (id_san_pham, id_chi_tiet_san_pham, url, la_anh_chinh, thu_tu) VALUES
(1, 1,    'https://cdn.sunova.vn/sp001.jpg', 1, 1),
(2, NULL, 'https://cdn.sunova.vn/sp002.jpg', 1, 1),
(3, NULL, 'https://cdn.sunova.vn/sp003.jpg', 1, 1),
(4, 4,    'https://cdn.sunova.vn/sp004.jpg', 1, 1),
(5, NULL, 'https://cdn.sunova.vn/sp005.jpg', 1, 1);
GO

-- CÂU HỎI QUIZ
INSERT INTO cau_hoi_quiz (noi_dung, thu_tu) VALUES
(N'Làn da của bạn thường có biểu hiện gì vào thời điểm giữa ngày hoặc cuối ngày?', 1),
(N'Hiệu ứng bề mặt (Finish) bạn mong muốn nhất sau khi thoa kem chống nắng là gì?', 2),
(N'Bạn dự định sử dụng sản phẩm chống nắng chủ yếu trong môi trường hoặc hoạt động nào?', 3),
(N'Ngoài việc bảo vệ khỏi tia UV, bạn muốn kem chống nắng hỗ trợ thêm vấn đề gì cho da?', 4),
(N'Kết cấu sản phẩm (Format) nào mang lại cảm giác thoải mái nhất cho bạn khi thoa?', 5),
(N'Cảm giác trên da của bạn sau khi rửa mặt khoảng 30 phút (không bôi thêm gì) là như thế nào?', 6);
GO

-- ĐÁP ÁN QUIZ (gắn câu hỏi + loại da + điểm)
INSERT INTO dap_an_quiz (id_cau_hoi, noi_dung, id_loai_da, diem) VALUES
-- Câu 1
(1, N'Bóng dầu diện rộng, đặc biệt là vùng trán, mũi và cằm', 1, 3), 
(1, N'Khô ráp, có cảm giác căng cức nhẹ, đôi khi bong tróc', 2, 3), 
(1, N'Chỉ đổ dầu vùng chữ T (trán, mũi), hai bên má lại khô ráp', 3, 3), 
(1, N'Bề mặt thông thoáng, mềm mịn, không quá dầu hay khô', 4, 3), 
(1, N'Dễ bị đỏ ửng, châm chích hoặc ngứa rát khi đổi mỹ phẩm', 5, 3), 
-- Câu 2
(2, N'Kiềm dầu hoàn toàn, tạo bề mặt lì mịn, mỏng nhẹ (Matte)', 1, 2),
(2, N'Căng bóng, mọng nước, tạo hiệu ứng căng mướt (Glowy)', 2, 2),
(2, N'Tự nhiên như không bôi gì, thoáng da tàng hình (Unseen)', 4, 2),
(2, N'Lớp nền mịn màng, làm dịu da và che mẩn đỏ nhẹ', 5, 2),
-- Câu 3
(3, N'Làm việc văn phòng, đi học, ngồi điều hòa thời gian dài', 2, 1),
(3, N'Hoạt động ngoài trời nhiều, chơi thể thao hoặc đi bơi', 1, 1),
(3, N'Sử dụng hằng ngày như một lớp lót mịn dưới lớp trang điểm', 3, 1),
-- Câu 4
(4, N'Nâng tông trắng hồng tự nhiên, làm sáng vùng da xỉn màu', 4, 1),
(4, N'Thành phần lành tính, phục hồi và làm dịu kích ứng', 5, 2),
(4, N'Kiểm soát bã nhờn tối đa, ngăn ngừa bít tắc sinh mụn', 1, 2),
(4, N'Cấp ẩm sâu, chống oxy hóa và ngăn ngừa lão hóa sớm', 2, 2),
-- Câu 5
(5, N'Dạng sữa hoặc gel lỏng, thấm siêu nhanh và mát da', 1, 1),
(5, N'Dạng kem đặc mịn màng, tạo màng ẩm mượt lâu dài', 2, 1),
(5, N'Dạng xịt phun sương hoặc thỏi lăn tiện lợi dặm lại', 4, 1),
-- Câu 6
(6, N'Cảm thấy khô căng, hơi rát, đôi khi có vảy sừng nhỏ', 2, 3),
(6, N'Đổ dầu bóng loáng trên toàn bộ khuôn mặt', 1, 3),
(6, N'Trán và mũi đổ dầu nhờn, nhưng hai bên má lại khô căng', 3, 3),
(6, N'Mềm mại, thoải mái, không quá dầu cũng không quá khô', 4, 3);
GO

-- KẾT QUẢ QUIZ
INSERT INTO ket_qua_quiz (id_khach_hang, id_loai_da_ket_qua, tong_diem) VALUES
(1, 1, 8), (2, 2, 7), (3, 3, 6), (4, 4, 5), (5, 5, 9);
GO

-- CÂU HỎI THƯỜNG GẶP
INSERT INTO cau_hoi_thuong_gap (cau_hoi, tra_loi, chu_de) VALUES
(N'Kem chống nắng nên bôi lại sau bao lâu?', N'Nên bôi lại sau mỗi 2-3 giờ khi ở ngoài trời.', N'Sử dụng'),
(N'SPF và PA khác nhau thế nào?',            N'SPF chống tia UVB, PA chống tia UVA.',          N'Kiến thức'),
(N'Da dầu nên chọn loại nào?',               N'Nên chọn dạng gel hoặc sữa có công dụng kiềm dầu.', N'Tư vấn'),
(N'Ở trong nhà có cần chống nắng không?',    N'Có, nếu ngồi gần cửa sổ hoặc dùng thiết bị điện tử.', N'Sử dụng'),
(N'Chính sách đổi trả thế nào?',             N'Đổi trả trong 7 ngày nếu sản phẩm còn nguyên vẹn.', N'Chính sách');
GO

-- PHIÊN CHAT AI
INSERT INTO phien_chat_ai (id_khach_hang, tieu_de, trang_thai) VALUES
(1, N'Tư vấn chống nắng cho da dầu', 'DA_DONG'),
(2, N'Hỏi về chỉ số SPF',            'DA_DONG'),
(3, N'Tư vấn da nhạy cảm',           'DANG_MO'),
(4, N'Chọn sản phẩm nâng tông',      'DANG_MO'),
(5, N'Hỏi về kháng nước',            'DA_DONG');
GO

-- TIN NHẮN CHAT AI
INSERT INTO tin_nhan_chat_ai (id_phien, nguoi_gui, noi_dung, id_san_pham_goi_y) VALUES
(1, 'KHACH', N'Da dầu của em nên dùng loại nào ạ?', NULL),
(1, 'AI',    N'Bạn nên dùng sản phẩm kiềm dầu, ví dụ Anessa Milk.', 1),
(2, 'KHACH', N'SPF50+ nghĩa là gì?', NULL),
(2, 'AI',    N'SPF50+ là khả năng chống tia UVB rất cao.', NULL),
(3, 'KHACH', N'Da em hay kích ứng, nên chọn gì?', 3);
GO

-- DỮ LIỆU UV
INSERT INTO du_lieu_uv (tinh_thanh, ngay, chi_so_uv, muc_do, spf_khuyen_nghi, nguon_du_lieu) VALUES
(N'Hà Nội',  '2026-06-09',  8.5, 'CAO',      'SPF50+', 'OpenWeather'),
(N'TP.HCM',  '2026-06-09', 10.2, 'RAT_CAO',  'SPF50+', 'OpenWeather'),
(N'Đà Nẵng', '2026-06-09',  9.0, 'CAO',      'SPF50+', 'OpenWeather'),
(N'Hải Phòng','2026-06-09', 6.5, 'TRUNG_BINH','SPF30+','OpenWeather'),
(N'Cần Thơ', '2026-06-09', 11.0, 'RAT_CAO',  'SPF50+', 'OpenWeather');
GO

-- LUẬT GỢI Ý
INSERT INTO luat_goi_y (ten_luat, id_loai_da, id_cong_dung, uv_tu, uv_den, spf_toi_thieu, do_uu_tien) VALUES
(N'Da dầu + UV cao',       1, 1, 6.0, 11.0, 'SPF50+', 1),
(N'Da khô cần cấp ẩm',     2, 2, 3.0, 7.0,  'SPF30+', 2),
(N'Da nhạy cảm làm dịu',   5, 5, 3.0, 11.0, 'SPF50+', 1),
(N'Nâng tông hằng ngày',   4, 3, 3.0, 8.0,  'SPF30+', 3),
(N'Hoạt động ngoài trời',  1, 4, 8.0, 11.0, 'SPF50+', 1);
GO

-- LỊCH SỬ HÀNH VI
INSERT INTO lich_su_hanh_vi (id_khach_hang, id_san_pham, loai_hanh_vi) VALUES
(1, 1, 'XEM'),
(1, 1, 'THEM_GIO'),
(2, 2, 'XEM'),
(3, 3, 'MUA'),
(4, 4, 'YEU_THICH');
GO

-- SẢN PHẨM YÊU THÍCH
INSERT INTO san_pham_yeu_thich (id_khach_hang, id_san_pham) VALUES
(1, 1), (1, 2), (2, 3), (3, 4), (4, 5);
GO

-- ROUTINE
INSERT INTO routine (ten, mo_ta, id_loai_da, tong_gia) VALUES
(N'Routine chống nắng da dầu',     N'Combo kiềm dầu cho da dầu',      1, 700000),
(N'Routine da khô cấp ẩm',         N'Combo dưỡng ẩm + chống nắng',    2, 495000),
(N'Routine da nhạy cảm làm dịu',   N'Combo làm dịu cho da nhạy cảm',  5, 350000),
(N'Routine nâng tông hằng ngày',   N'Combo nâng tông tự nhiên',       4, 610000),
(N'Routine hoạt động ngoài trời',  N'Combo kháng nước cho thể thao',  1, 180000);
GO

-- CHI TIẾT ROUTINE
INSERT INTO chi_tiet_routine (id_routine, id_san_pham, buoc, ghi_chu) VALUES
(1, 1, 1, N'Bôi sữa chống nắng buổi sáng'),
(1, 4, 2, N'Xịt lại khi ra ngoài'),
(2, 2, 1, N'Dùng kem chống nắng dịu nhẹ'),
(3, 3, 1, N'Dùng kem có rau má'),
(4, 5, 1, N'Dùng kem nâng tông');
GO

-- ĐÁNH GIÁ
INSERT INTO danh_gia (id_khach_hang, id_san_pham, so_sao, noi_dung, trang_thai) VALUES
(1, 1, 5, N'Rất tốt, không bết dính', 'DA_DUYET'),
(2, 2, 4, N'Dịu nhẹ, hợp da nhạy cảm', 'DA_DUYET'),
(3, 3, 5, N'Thích mùi rau má, làm dịu da', 'DA_DUYET'),
(4, 4, 3, N'Xịt tiện nhưng nhanh hết', 'CHO_DUYET'),
(5, 5, 5, N'Nâng tông tự nhiên, rất đẹp', 'DA_DUYET');
GO

-- GIỎ HÀNG
INSERT INTO gio_hang (id_khach_hang) VALUES
(1), (2), (3), (4), (5);
GO

-- CHI TIẾT GIỎ HÀNG
INSERT INTO chi_tiet_gio_hang (id_gio_hang, id_chi_tiet_san_pham, so_luong) VALUES
(1, 1, 2),
(1, 2, 1),
(2, 3, 1),
(3, 4, 3),
(4, 5, 1);
GO

-- PHƯƠNG THỨC THANH TOÁN
INSERT INTO phuong_thuc_thanh_toan (ma, ten) VALUES
('TIEN_MAT',     N'Tiền mặt'),
('CHUYEN_KHOAN', N'Chuyển khoản'),
('VNPAY',        N'VNPay'),
('MOMO',         N'Ví Momo'),
('COD',          N'Thanh toán khi nhận hàng');
GO

-- ĐỢT GIẢM GIÁ
INSERT INTO dot_giam_gia (ma, ten, phan_tram_giam, ngay_bat_dau, ngay_ket_thuc) VALUES
('DGG01', N'Hè rực rỡ',       10.00, '2026-06-01', '2026-06-30'),
('DGG02', N'Flash Sale 6.6',  20.00, '2026-06-06', '2026-06-07'),
('DGG03', N'Sinh nhật SUNOVA',15.00, '2026-07-01', '2026-07-15'),
('DGG04', N'Back to School',  12.00, '2026-08-15', '2026-09-05'),
('DGG05', N'Clear stock',     25.00, '2026-06-20', '2026-06-25');
GO

-- CHI TIẾT ĐỢT GIẢM GIÁ
INSERT INTO chi_tiet_dot_giam_gia (id_dot_giam_gia, id_chi_tiet_san_pham, gia_sau_giam) VALUES
(1, 1, 468000),
(1, 2, 445500),
(2, 3, 280000),
(2, 4, 144000),
(3, 5, 518500);
GO

-- PHIẾU GIẢM GIÁ (VOUCHER)
INSERT INTO phieu_giam_gia (ma, ten, loai, gia_tri, gia_tri_don_toi_thieu, giam_toi_da, so_luong, ngay_bat_dau, ngay_ket_thuc) VALUES
('SALE10',   N'Giảm 10% toàn đơn', 'PHAN_TRAM', 10,    200000,  50000,  100, '2026-06-01', '2026-06-30'),
('FREESHIP', N'Miễn phí ship',     'TIEN_MAT',  30000, 0,       30000,  200, '2026-06-01', '2026-12-31'),
('GIAM50K',  N'Giảm 50k',          'TIEN_MAT',  50000, 500000,  50000,  50,  '2026-06-01', '2026-07-31'),
('NEWUSER',  N'Khách mới giảm 15%','PHAN_TRAM', 15,    100000,  100000, 300, '2026-01-01', '2026-12-31'),
('VIP20',    N'VIP giảm 20%',      'PHAN_TRAM', 20,    1000000, 200000, 30,  '2026-06-01', '2026-12-31');
GO

-- HÓA ĐƠN (2 đơn ONLINE, 2 đơn TẠI QUẦY, 1 đơn online chờ xác nhận)
INSERT INTO hoa_don (ma_hoa_don, id_khach_hang, id_nhan_vien, id_phuong_thuc_thanh_toan, id_phieu_giam_gia, loai_don, trang_thai, ten_nguoi_nhan, sdt_nguoi_nhan, dia_chi_giao, tong_tien, tien_giam_gia, phi_van_chuyen, thanh_tien) VALUES
('HD001', 1, NULL, 3, 1,    'ONLINE',   'HOAN_THANH',   N'Đỗ Thị Hoa',   '0911000001', N'số 1 Trần Thái Tông, Cầu Giấy, Hà Nội', 1040000, 104000, 30000, 966000),
('HD002', 2, NULL, 5, NULL, 'ONLINE',   'DANG_GIAO',    N'Vũ Văn Khánh', '0911000002', N'12 Lê Lợi, Quận 1, TP.HCM',              350000,  0,      30000, 380000),
('HD003', NULL, 2, 1, NULL, 'TAI_QUAY', 'HOAN_THANH',   NULL,            NULL,         NULL,                                     180000,  0,      0,     180000),
('HD004', 3, NULL, 3, NULL, 'ONLINE',   'CHO_XAC_NHAN', N'Bùi Thị Lan',  '0911000003', N'45 Bạch Đằng, Hải Châu, Đà Nẵng',        610000,  0,      30000, 640000),
('HD005', NULL, 3, 1, NULL, 'TAI_QUAY', 'HOAN_THANH',   NULL,            NULL,         NULL,                                     495000,  0,      0,     495000);
GO

-- HÓA ĐƠN CHI TIẾT
INSERT INTO hoa_don_chi_tiet (id_hoa_don, id_chi_tiet_san_pham, so_luong, don_gia, thanh_tien) VALUES
(1, 1, 2, 520000, 1040000),
(2, 3, 1, 350000, 350000),
(3, 4, 1, 180000, 180000),
(4, 5, 1, 610000, 610000),
(5, 2, 1, 495000, 495000);
GO

-- THANH TOÁN HÓA ĐƠN (online có mã GD; tại quầy có tiền đưa + tiền thối)
INSERT INTO thanh_toan_hoa_don (id_hoa_don, id_phuong_thuc_thanh_toan, so_tien, so_tien_khach_dua, tien_thua, ma_giao_dich) VALUES
(1, 3, 966000, NULL,   NULL,  'VNP100001'),
(2, 5, 380000, NULL,   NULL,  NULL),
(3, 1, 180000, 200000, 20000, NULL),
(5, 1, 495000, 500000, 5000,  NULL),
(4, 3, 640000, NULL,   NULL,  'VNP100002');
GO

-- LỊCH SỬ ĐƠN HÀNG
INSERT INTO lich_su_don_hang (id_hoa_don, trang_thai, ghi_chu, id_nhan_vien) VALUES
(1, 'CHO_XAC_NHAN', N'Khách đặt online',      NULL),
(1, 'HOAN_THANH',   N'Đã giao thành công',    2),
(2, 'DANG_GIAO',    N'Đang vận chuyển',       2),
(3, 'HOAN_THANH',   N'Bán tại quầy',          2),
(4, 'CHO_XAC_NHAN', N'Chờ nhân viên xác nhận',NULL);
GO

-- THÔNG BÁO
INSERT INTO thong_bao (tieu_de, noi_dung, id_khach_hang, id_nhan_vien, loai) VALUES
(N'Đặt hàng thành công',     N'Đơn HD001 của bạn đã được tạo.',        1, NULL, 'DON_HANG'),
(N'Đơn hàng đang giao',      N'Đơn HD002 đang trên đường giao.',       2, NULL, 'DON_HANG'),
(N'Khuyến mãi hè rực rỡ',    N'Giảm đến 25% nhiều sản phẩm chống nắng.',3, NULL, 'KHUYEN_MAI'),
(N'Cảnh báo UV cao',         N'Chỉ số UV hôm nay rất cao, nhớ chống nắng!', 4, NULL, 'UV'),
(N'Bảo trì hệ thống',        N'Hệ thống bảo trì lúc 23h hôm nay.',     NULL, 2, 'HE_THONG');
GO

PRINT N'>>> Đã tạo xong database SUNOVA với dữ liệu mẫu.';
GO