/* ================================================================
   SUNOVA — MIGRATION ĐẦY ĐỦ (gộp toàn bộ V1→V26 + 15 sản phẩm)
   FILE DUY NHẤT — thay thế cho 26 file migration cũ
   ================================================================

   ⚠️⚠️ ĐỌC KỸ TRƯỚC KHI DÙNG ⚠️⚠️

   CÁCH DÙNG ĐÚNG (bắt buộc theo đúng thứ tự):
   1. XÓA HẾT 26 file migration cũ trong thư mục db/migration
      (V1__init.sql ... V26__them_banner_trang_chu.sql).
      CHỈ giữ lại DUY NHẤT file này: V1__init_full.sql
   2. XÓA SẠCH database cũ và tạo lại RỖNG (chạy trong SSMS):
        ALTER DATABASE SUNOVA SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
        DROP DATABASE SUNOVA;
        CREATE DATABASE SUNOVA;
      (đổi 'SUNOVA' thành đúng tên DB của bạn nếu khác)
   3. Chạy lại app Spring Boot → Flyway thấy DB rỗng + 1 file V1
      → chạy đúng 1 lượt từ đầu, tạo toàn bộ bảng + dữ liệu + 15 SP.

   ⚠️ KHÔNG chạy file này khi DB đã có lịch sử Flyway của 26 file cũ
      → sẽ lỗi checksum/missing migration. PHẢI drop DB tạo mới.
   ⚠️ Drop DB = mất sạch dữ liệu hiện có. Backup trước nếu cần giữ.

   Tài khoản CHỦ: nguyenvu20042019@gmail.com (giữ nguyên trong V2).
   ================================================================ */


/* ─────────── [gốc: V1__init.sql] ─────────── */
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

    /* → BẢNG vai_tro: Vai trò/phân quyền: CHU > QUAN_LY > NHAN_VIEN > KHACH_HANG. */
CREATE TABLE vai_tro (
    id          INT IDENTITY(1,1) PRIMARY KEY,
    ma_vai_tro  VARCHAR(20)  NOT NULL UNIQUE,
    ten_vai_tro NVARCHAR(50) NOT NULL,
    mo_ta       NVARCHAR(255)
);
GO

    /* → BẢNG loai_da: Danh mục loại da (dầu, khô, hỗn hợp, nhạy cảm...) — dùng cho quiz & gợi ý. */
CREATE TABLE loai_da (
    id    INT IDENTITY(1,1) PRIMARY KEY,
    ma    VARCHAR(20)  NOT NULL UNIQUE,
    ten   NVARCHAR(50) NOT NULL,
    mo_ta NVARCHAR(255)
);
GO

    /* → BẢNG nhan_vien: Tài khoản nhân viên/quản lý/chủ. Gắn vai_tro. Mật khẩu demo để thô. */
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

    /* → BẢNG khach_hang: Tài khoản khách hàng. Có điểm tích lũy, loại da (nếu đã làm quiz). */
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

    /* → BẢNG dia_chi_khach_hang: Địa chỉ giao hàng của khách (kèm mã tỉnh/huyện/xã GHN). */
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

    /* → BẢNG thuong_hieu: Thương hiệu sản phẩm (Anessa, La Roche-Posay, Vichy...). */
CREATE TABLE thuong_hieu (
    id         INT IDENTITY(1,1) PRIMARY KEY,
    ma         VARCHAR(20)  NOT NULL UNIQUE,
    ten        NVARCHAR(100) NOT NULL,
    xuat_xu    NVARCHAR(50),
    trang_thai BIT DEFAULT 1
);
GO

    /* → BẢNG danh_muc: Danh mục chống nắng: mặt / cơ thể / nâng tông / da mụn / trẻ em. */
CREATE TABLE danh_muc (
    id         INT IDENTITY(1,1) PRIMARY KEY,
    ma         VARCHAR(20)  NOT NULL UNIQUE,
    ten        NVARCHAR(80) NOT NULL,
    mo_ta      NVARCHAR(255),
    trang_thai BIT DEFAULT 1
);
GO

    /* → BẢNG dang_san_pham: Dạng bào chế: sữa / kem / gel / xịt / thỏi. */
CREATE TABLE dang_san_pham (
    id    INT IDENTITY(1,1) PRIMARY KEY,
    ma    VARCHAR(20)  NOT NULL UNIQUE,
    ten   NVARCHAR(50) NOT NULL,
    mo_ta NVARCHAR(255)
);
GO

    /* → BẢNG cong_dung: Danh mục công dụng (kháng nước, cấp ẩm, kiềm dầu...) — nhập tay. */
CREATE TABLE cong_dung (
    id    INT IDENTITY(1,1) PRIMARY KEY,
    ma    VARCHAR(20)  NOT NULL UNIQUE,
    ten   NVARCHAR(50) NOT NULL,
    mo_ta NVARCHAR(255)
);
GO

    /* → BẢNG thanh_phan: Danh mục thành phần (Niacinamide, HA...) — nhập tay từ nhãn. */
CREATE TABLE thanh_phan (
    id    INT IDENTITY(1,1) PRIMARY KEY,
    ma    VARCHAR(30)  NOT NULL UNIQUE,
    ten   NVARCHAR(80) NOT NULL,
    loai  VARCHAR(10),
    mo_ta NVARCHAR(255)
);
GO

    /* → BẢNG mau_sac: Màu sắc biến thể (không màu, tông hồng...). Dùng cho ảnh theo màu. */
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

    /* → BẢNG san_pham: TẦNG 1 - Sản phẩm khái niệm: tên, SPF, PA, loại chống nắng, kháng nước. */
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
    /* → BẢNG san_pham_loai_da: Bảng nối: sản phẩm phù hợp với loại da nào (phục vụ gợi ý cá nhân hóa). */
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
    /* → BẢNG san_pham_cong_dung: Bảng nối: sản phẩm có công dụng gì. */
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
    /* → BẢNG san_pham_thanh_phan: Bảng nối: sản phẩm chứa thành phần gì. */
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
    /* → BẢNG chi_tiet_san_pham: TẦNG 2 - Biến thể (SKU): giá, dung tích, màu, tồn kho (cache). */
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
    /* → BẢNG lo_hang: TẦNG 3 - Lô hàng: số lô, ngày nhập, HẠN SỬ DỤNG, số lượng còn/lỗi. Tồn thật = tổng lô. */
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
     để hiển thị ảnh riêng cho từng màu/dung tích. NULL = ảnh chung sản phẩm.
   - id_mau_sac (V25): NULL = ảnh dùng chung mọi màu; có giá trị = ảnh riêng của màu đó. */
    /* → BẢNG anh_san_pham: Ảnh sản phẩm. Có id_mau_sac (NULL=ảnh chung, có màu=ảnh riêng của màu). */
CREATE TABLE anh_san_pham (
    id                   INT IDENTITY(1,1) PRIMARY KEY,
    id_san_pham          INT NOT NULL,
    id_chi_tiet_san_pham INT NULL,
    id_mau_sac           INT NULL,
    url                  VARCHAR(255) NOT NULL,
    la_anh_chinh         BIT DEFAULT 0,
    thu_tu               INT DEFAULT 0,
    CONSTRAINT fk_anh_sp  FOREIGN KEY (id_san_pham)          REFERENCES san_pham(id),
    CONSTRAINT fk_anh_cts FOREIGN KEY (id_chi_tiet_san_pham) REFERENCES chi_tiet_san_pham(id),
    CONSTRAINT fk_anh_ms  FOREIGN KEY (id_mau_sac)           REFERENCES mau_sac(id)
);
GO

/* ===========================================================================
   4. NHÓM QUIZ XÁC ĐỊNH LOẠI DA
   =========================================================================== */

    /* → BẢNG cau_hoi_quiz: Câu hỏi quiz xác định loại da. */
CREATE TABLE cau_hoi_quiz (
    id         INT IDENTITY(1,1) PRIMARY KEY,
    noi_dung   NVARCHAR(255) NOT NULL,
    thu_tu     INT DEFAULT 0,
    trang_thai BIT DEFAULT 1
);
GO

    /* → BẢNG dap_an_quiz: Đáp án cho từng câu hỏi quiz. */
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

    /* → BẢNG ket_qua_quiz: Kết quả quiz của khách: khách này ra loại da gì, lúc nào. */
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

    /* → BẢNG cau_hoi_thuong_gap: Câu hỏi thường gặp (FAQ) cho chat hỗ trợ. */
CREATE TABLE cau_hoi_thuong_gap (
    id         INT IDENTITY(1,1) PRIMARY KEY,
    cau_hoi    NVARCHAR(255) NOT NULL,
    tra_loi    NVARCHAR(MAX) NOT NULL,
    chu_de     NVARCHAR(50),
    trang_thai BIT DEFAULT 1
);
GO

    /* → BẢNG phien_chat_ai: Phiên chat với AI của khách. */
CREATE TABLE phien_chat_ai (
    id                INT IDENTITY(1,1) PRIMARY KEY,
    id_khach_hang     INT,
    tieu_de           NVARCHAR(150),
    thoi_gian_bat_dau DATETIME DEFAULT CURRENT_TIMESTAMP,
    trang_thai        VARCHAR(15) DEFAULT 'DANG_MO',
    CONSTRAINT fk_pc_kh FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id)
);
GO

    /* → BẢNG tin_nhan_chat_ai: Từng tin nhắn trong phiên chat AI. */
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

    /* → BẢNG du_lieu_uv: Dữ liệu chỉ số UV theo thời gian/địa điểm. */
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

    /* → BẢNG luat_goi_y: Luật gợi ý sản phẩm (theo loại da/UV). */
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

    /* → BẢNG lich_su_hanh_vi: Lịch sử hành vi khách (xem sản phẩm gì) — cho gợi ý. */
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

    /* → BẢNG san_pham_yeu_thich: Danh sách sản phẩm khách yêu thích (wishlist). */
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

    /* → BẢNG routine: Quy trình chăm sóc da gợi ý. */
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

    /* → BẢNG chi_tiet_routine: Các bước trong 1 routine. */
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

    /* → BẢNG danh_gia: Đánh giá sản phẩm của khách (sao + bình luận + lượt thích). */
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

    /* → BẢNG gio_hang: Giỏ hàng của khách. */
CREATE TABLE gio_hang (
    id            INT IDENTITY(1,1) PRIMARY KEY,
    id_khach_hang INT NOT NULL,
    ngay_tao      DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_gh_kh FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id)
);
GO

    /* → BẢNG chi_tiet_gio_hang: Từng sản phẩm trong giỏ. */
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

    /* → BẢNG phuong_thuc_thanh_toan: Danh mục phương thức thanh toán (tiền mặt, CK, VNPay). */
CREATE TABLE phuong_thuc_thanh_toan (
    id         INT IDENTITY(1,1) PRIMARY KEY,
    ma         VARCHAR(20)  NOT NULL UNIQUE,
    ten        NVARCHAR(50) NOT NULL,
    trang_thai BIT DEFAULT 1
);
GO

    /* → BẢNG dot_giam_gia: Đợt giảm giá (áp theo sản phẩm/biến thể). */
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

    /* → BẢNG chi_tiet_dot_giam_gia: Chi tiết đợt giảm giá áp cho biến thể nào. */
CREATE TABLE chi_tiet_dot_giam_gia (
    id                   INT IDENTITY(1,1) PRIMARY KEY,
    id_dot_giam_gia      INT NOT NULL,
    id_chi_tiet_san_pham INT NOT NULL,
    gia_sau_giam         DECIMAL(12,0),
    CONSTRAINT fk_ctdgg_dgg FOREIGN KEY (id_dot_giam_gia)      REFERENCES dot_giam_gia(id),
    CONSTRAINT fk_ctdgg_cts FOREIGN KEY (id_chi_tiet_san_pham) REFERENCES chi_tiet_san_pham(id)
);
GO

    /* → BẢNG phieu_giam_gia: Voucher: phần trăm/tiền mặt, hạn dùng, đơn tối thiểu, giảm tối đa. */
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

    /* → BẢNG hoa_don: Hóa đơn (đơn quầy + đơn online). Kèm mã GHN, địa chỉ giao. */
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

    /* → BẢNG hoa_don_chi_tiet: Từng dòng hàng trên hóa đơn (SKU + số lượng + giá). */
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
    /* → BẢNG thanh_toan_hoa_don: Các dòng thanh toán của 1 đơn (hỗ trợ thanh toán kết hợp). */
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

    /* → BẢNG lich_su_don_hang: Nhật ký thao tác trên đơn (tạo, duyệt, hủy...). */
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

    /* → BẢNG thong_bao: Thông báo gửi tới khách/nhân viên (realtime). */
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

-- ẢNH SẢN PHẨM (gắn sản phẩm; id_mau_sac NULL=ảnh chung, có giá trị=ảnh theo màu)
INSERT INTO anh_san_pham (id_san_pham, id_chi_tiet_san_pham, id_mau_sac, url, la_anh_chinh, thu_tu) VALUES
(1, 1,    NULL, 'https://cdn.sunova.vn/sp001.jpg', 1, 1),
(2, NULL, NULL, 'https://cdn.sunova.vn/sp002.jpg', 1, 1),
(3, NULL, NULL, 'https://cdn.sunova.vn/sp003.jpg', 1, 1),
(4, 4,    NULL, 'https://cdn.sunova.vn/sp004.jpg', 1, 1),
-- SP005 biến thể tông tự nhiên (MS02) → ảnh gắn màu tương ứng
(5, 5,    2,    'https://cdn.sunova.vn/sp005.jpg', 1, 1);
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

/* ─────────── [gốc: V2__them_vai_tro_chu.sql] ─────────── */
-- V2: Thêm vai trò CHỦ CỬA HÀNG + tài khoản chủ. Cấp: CHU > QUAN_LY > NHAN_VIEN.
IF NOT EXISTS (SELECT 1 FROM vai_tro WHERE ma_vai_tro = 'CHU')
    INSERT INTO vai_tro (ma_vai_tro, ten_vai_tro, mo_ta)
    VALUES ('CHU', N'Chủ cửa hàng', N'Cấp cao nhất, quản trị toàn bộ kể cả Quản lý');
GO
IF NOT EXISTS (SELECT 1 FROM nhan_vien WHERE email = 'nguyenvu20042019@gmail.com')
    INSERT INTO nhan_vien
        (id_vai_tro, ma_nhan_vien, ho_ten, email, so_dien_thoai, mat_khau, gioi_tinh, ngay_vao_lam, trang_thai)
    VALUES
        ((SELECT id FROM vai_tro WHERE ma_vai_tro = 'CHU'),
         'CHU01', N'Nguyễn Vũ', 'nguyenvu20042019@gmail.com', '0339589851',
         'nguyenvu20042019@gmail.com', 'Nam', '2026-01-01', 1);
GO


/* ─────────── [gốc: V3__SUNOVA_Review.sql] ─────────── */
-- Nâng cấp bảng Đánh Giá (Shopee-like Review System)
-- Thêm Phản hồi của Shop
ALTER TABLE danh_gia ADD phan_hoi_cua_shop NVARCHAR(500);

-- Thêm Ảnh/Video của khách hàng (Lưu URL)
ALTER TABLE danh_gia ADD hinh_anh_video VARCHAR(1000);

-- Thêm Khóa ngoại Hóa Đơn Chi Tiết để chặn spam review
ALTER TABLE danh_gia ADD id_hoa_don_chi_tiet INT;
ALTER TABLE danh_gia ADD CONSTRAINT fk_dg_hdct FOREIGN KEY (id_hoa_don_chi_tiet) REFERENCES hoa_don_chi_tiet(id);


/* ─────────── [gốc: V4__Them_Luot_Thich_Danh_Gia.sql] ─────────── */
-- Nâng cấp tính năng Hữu Ích cho Đánh Giá
ALTER TABLE danh_gia ADD so_luot_thich INT DEFAULT 0;


/* ─────────── [gốc: V5__add_ghn_order_code_to_hoa_don.sql] ─────────── */
IF COL_LENGTH('hoa_don', 'ma_van_don_ghn') IS NULL
BEGIN
    ALTER TABLE hoa_don ADD ma_van_don_ghn VARCHAR(50) NULL;
END
GO


/* ─────────── [gốc: V6__add_ghn_address_to_hoa_don.sql] ─────────── */
IF COL_LENGTH('hoa_don', 'ghn_district_id') IS NULL
BEGIN
    ALTER TABLE hoa_don ADD ghn_district_id INT NULL;
END
GO

IF COL_LENGTH('hoa_don', 'ghn_ward_code') IS NULL
BEGIN
    ALTER TABLE hoa_don ADD ghn_ward_code VARCHAR(20) NULL;
END
GO


/* ─────────── [gốc: V7__add_is_active_to_voucher_tables.sql] ─────────── */
IF COL_LENGTH('phieu_giam_gia', 'is_active') IS NULL
BEGIN
    ALTER TABLE phieu_giam_gia
        ADD is_active BIT NOT NULL CONSTRAINT df_pgg_is_active DEFAULT 1;
END
GO

IF COL_LENGTH('dot_giam_gia', 'is_active') IS NULL
BEGIN
    ALTER TABLE dot_giam_gia
        ADD is_active BIT NOT NULL CONSTRAINT df_dgg_is_active DEFAULT 1;
END
GO


/* ─────────── [gốc: V8__add_quiz_icons.sql] ─────────── */
UPDATE dap_an_quiz SET icon = 'lucide:droplets' WHERE noi_dung LIKE N'%Bóng dầu diện rộng%'; 
UPDATE dap_an_quiz SET icon = 'lucide:feather' WHERE noi_dung LIKE N'%Khô ráp, có cảm giác căng tức%'; 
UPDATE dap_an_quiz SET icon = 'lucide:split-square-horizontal' WHERE noi_dung LIKE N'%Chỉ đổ dầu vùng chữ T%'; 
UPDATE dap_an_quiz SET icon = 'lucide:smile' WHERE noi_dung LIKE N'%Bề mặt thông thoáng, mềm mịn%'; 
UPDATE dap_an_quiz SET icon = 'lucide:alert-circle' WHERE noi_dung LIKE N'%Dễ bị đỏ ửng, châm chích%'; 

UPDATE dap_an_quiz SET icon = 'lucide:circle-dashed' WHERE noi_dung LIKE N'%Kiềm dầu hoàn toàn%'; 
UPDATE dap_an_quiz SET icon = 'lucide:sparkles' WHERE noi_dung LIKE N'%Căng bóng, mọng nước%'; 
UPDATE dap_an_quiz SET icon = 'lucide:eye-off' WHERE noi_dung LIKE N'%Tự nhiên như không bôi gì%'; 
UPDATE dap_an_quiz SET icon = 'lucide:shield-check' WHERE noi_dung LIKE N'%Lớp nền mịn màng, làm dịu da%'; 

UPDATE dap_an_quiz SET icon = 'lucide:monitor' WHERE noi_dung LIKE N'%Làm việc văn phòng%'; 
UPDATE dap_an_quiz SET icon = 'lucide:bike' WHERE noi_dung LIKE N'%Hoạt động ngoài trời%'; 
UPDATE dap_an_quiz SET icon = 'lucide:sun' WHERE noi_dung LIKE N'%Sử dụng hằng ngày%'; 

UPDATE dap_an_quiz SET icon = 'lucide:arrow-up-circle' WHERE noi_dung LIKE N'%Nâng tông trắng hồng%'; 
UPDATE dap_an_quiz SET icon = 'lucide:leaf' WHERE noi_dung LIKE N'%Thành phần lành tính%'; 
UPDATE dap_an_quiz SET icon = 'lucide:shield-alert' WHERE noi_dung LIKE N'%Kiểm soát bã nhờn tối đa%'; 
UPDATE dap_an_quiz SET icon = 'lucide:droplets' WHERE noi_dung LIKE N'%Cấp ẩm sâu, chống oxy hóa%'; 

UPDATE dap_an_quiz SET icon = 'lucide:wind' WHERE noi_dung LIKE N'%Dạng sữa hoặc gel lỏng%'; 
UPDATE dap_an_quiz SET icon = 'lucide:cloud' WHERE noi_dung LIKE N'%Dạng kem đặc mịn màng%'; 
UPDATE dap_an_quiz SET icon = 'lucide:spray-can' WHERE noi_dung LIKE N'%Dạng xịt phun sương%'; 

UPDATE dap_an_quiz SET icon = 'lucide:frown' WHERE noi_dung LIKE N'%Cảm thấy khô căng, hơi rát%'; 
UPDATE dap_an_quiz SET icon = 'lucide:droplet' WHERE noi_dung LIKE N'%Đổ dầu bóng loáng%'; 
UPDATE dap_an_quiz SET icon = 'lucide:split-square-horizontal' WHERE noi_dung LIKE N'%Trán và mũi đổ dầu nhờn%'; 
UPDATE dap_an_quiz SET icon = 'lucide:smile' WHERE noi_dung LIKE N'%Mềm mại, thoải mái%';


/* ─────────── [gốc: V9__fix_quiz_icons.sql] ─────────── */
UPDATE dap_an_quiz SET icon = 'lucide:droplet' WHERE noi_dung LIKE N'%Bóng dầu%'; 
UPDATE dap_an_quiz SET icon = 'lucide:feather' WHERE noi_dung LIKE N'%Khô ráp%'; 
UPDATE dap_an_quiz SET icon = 'lucide:split-square-horizontal' WHERE noi_dung LIKE N'%chữ T%'; 
UPDATE dap_an_quiz SET icon = 'lucide:smile' WHERE noi_dung LIKE N'%Bề mặt thông thoáng%'; 
UPDATE dap_an_quiz SET icon = 'lucide:alert-circle' WHERE noi_dung LIKE N'%đỏ ửng%'; 

UPDATE dap_an_quiz SET icon = 'lucide:droplets' WHERE noi_dung LIKE N'%gel lỏng%'; 

UPDATE dap_an_quiz SET icon = 'lucide:frown' WHERE noi_dung LIKE N'%khô căng%'; 
UPDATE dap_an_quiz SET icon = 'lucide:droplet' WHERE noi_dung LIKE N'%Đổ dầu bóng loáng%'; 
UPDATE dap_an_quiz SET icon = 'lucide:split-square-horizontal' WHERE noi_dung LIKE N'%Trán và mũi%'; 
UPDATE dap_an_quiz SET icon = 'lucide:smile' WHERE noi_dung LIKE N'%Mềm mại, thoải mái%';


/* ─────────── [gốc: V10__add_ghn_ids_to_dia_chi_khach_hang.sql] ─────────── */
ALTER TABLE dia_chi_khach_hang
    ADD province_id INT NULL,
        district_id INT NULL,
        ward_code VARCHAR(20) NULL;
GO


/* ─────────── [gốc: V11__them_thong_bao.sql] ─────────── */
/* Bảng thong_bao đã tạo ở V1__init.sql.
   V11 chỉ bổ sung các cột cần cho thông báo admin (chuông + điều hướng)
   và nới rộng cột loai để chứa tên enum dài hơn. */

IF COL_LENGTH('thong_bao', 'link') IS NULL
BEGIN
    ALTER TABLE thong_bao ADD link VARCHAR(255) NULL;
END
GO

IF COL_LENGTH('thong_bao', 'id_tham_chieu') IS NULL
BEGIN
    ALTER TABLE thong_bao ADD id_tham_chieu INT NULL;
END
GO

IF COL_LENGTH('thong_bao', 'ma_tham_chieu') IS NULL
BEGIN
    ALTER TABLE thong_bao ADD ma_tham_chieu VARCHAR(30) NULL;
END
GO

IF EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('thong_bao') AND name = 'loai' AND max_length < 30
)
BEGIN
    ALTER TABLE thong_bao ALTER COLUMN loai VARCHAR(30);
END
GO


/* ─────────── [gốc: V12__them_tra_hang_hoan_tien.sql] ─────────── */
/* V12: Them tinh nang tra hang (yeu_cau_tra_hang) va hoan tien (hoan_tien).
   - yeu_cau_tra_hang: khach hang gui yeu cau hoan tra sau khi nhan hang, admin duyet.
   - hoan_tien: ban ghi hoan tien (huy don da thanh toan hoac tra hang), admin xac nhan hoan tat. */

IF OBJECT_ID('yeu_cau_tra_hang', 'U') IS NULL
BEGIN
    /* → BẢNG yeu_cau_tra_hang: Yêu cầu trả hàng của khách + lý do + trạng thái. */
    CREATE TABLE yeu_cau_tra_hang (
        id                  INT IDENTITY(1,1) PRIMARY KEY,
        id_hoa_don          INT NOT NULL,
        ly_do               NVARCHAR(255) NULL,
        mo_ta               NVARCHAR(500) NULL,
        trang_thai          VARCHAR(20) NOT NULL DEFAULT 'CHO_DUYET',
        ma_van_don_tra      VARCHAR(50) NULL,
        ghn_district_id     INT NULL,
        ghn_ward_code       VARCHAR(20) NULL,
        dia_chi_tra         NVARCHAR(255) NULL,
        ten_ngan_hang       NVARCHAR(100) NULL,
        so_tai_khoan        VARCHAR(30) NULL,
        chu_tai_khoan       NVARCHAR(100) NULL,
        ghi_chu_admin       NVARCHAR(255) NULL,
        id_nhan_vien_duyet  INT NULL,
        ngay_tao            DATETIME NOT NULL DEFAULT GETDATE(),
        ngay_cap_nhat       DATETIME NULL,
        CONSTRAINT fk_ycth_hoa_don FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id),
        CONSTRAINT fk_ycth_nhan_vien FOREIGN KEY (id_nhan_vien_duyet) REFERENCES nhan_vien(id)
    );
END
GO

IF OBJECT_ID('hoan_tien', 'U') IS NULL
BEGIN
    /* → BẢNG hoan_tien: Giao dịch hoàn tiền cho đơn trả. */
    CREATE TABLE hoan_tien (
        id                  INT IDENTITY(1,1) PRIMARY KEY,
        id_hoa_don          INT NOT NULL,
        id_yeu_cau_tra_hang INT NULL,
        loai                VARCHAR(10) NOT NULL,          -- HUY_DON | TRA_HANG
        so_tien             DECIMAL(12,0) NOT NULL DEFAULT 0,
        phuong_thuc         VARCHAR(20) NULL,              -- VNPAY | CHUYEN_KHOAN | TIEN_MAT
        trang_thai          VARCHAR(15) NOT NULL DEFAULT 'CHO_XU_LY', -- CHO_XU_LY | DA_HOAN | TU_CHOI
        ma_giao_dich_hoan   VARCHAR(100) NULL,
        ten_ngan_hang       NVARCHAR(100) NULL,
        so_tai_khoan        VARCHAR(30) NULL,
        chu_tai_khoan       NVARCHAR(100) NULL,
        ghi_chu             NVARCHAR(255) NULL,
        id_nhan_vien        INT NULL,
        ngay_tao            DATETIME NOT NULL DEFAULT GETDATE(),
        ngay_hoan           DATETIME NULL,
        CONSTRAINT fk_ht_hoa_don FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id),
        CONSTRAINT fk_ht_ycth FOREIGN KEY (id_yeu_cau_tra_hang) REFERENCES yeu_cau_tra_hang(id),
        CONSTRAINT fk_ht_nhan_vien FOREIGN KEY (id_nhan_vien) REFERENCES nhan_vien(id)
    );
END
GO


/* ─────────── [gốc: V13__them_provider_giao_dich_hoan.sql] ─────────── */
-- Luu ma giao dich nha cung cap (vd: vnp_TransactionNo) va ngay thanh toan (vnp_PayDate)
-- de goi API hoan tien sau nay.
ALTER TABLE thanh_toan_hoa_don ADD provider_transaction_no VARCHAR(100) NULL;
ALTER TABLE thanh_toan_hoa_don ADD provider_pay_date VARCHAR(20) NULL;

-- Luu raw response nha cung cap khi hoan tien (doi soat).
ALTER TABLE hoan_tien ADD phan_hoi_ncc NVARCHAR(500) NULL;


/* ─────────── [gốc: V14__them_anh_tra_hang.sql] ─────────── */
/* V14: Them bang anh dinh kem yeu cau tra hang (toi thieu 2 anh). */

IF OBJECT_ID('anh_yeu_cau_tra_hang', 'U') IS NULL
BEGIN
    /* → BẢNG anh_yeu_cau_tra_hang: Ảnh minh chứng khi khách yêu cầu trả hàng. */
    CREATE TABLE anh_yeu_cau_tra_hang (
        id                      INT IDENTITY(1,1) PRIMARY KEY,
        id_yeu_cau_tra_hang     INT NOT NULL,
        duong_dan               NVARCHAR(500) NOT NULL,
        ngay_tao                DATETIME NOT NULL DEFAULT GETDATE(),
        CONSTRAINT fk_aycth_yeu_cau FOREIGN KEY (id_yeu_cau_tra_hang)
            REFERENCES yeu_cau_tra_hang(id)
    );
END
GO


/* ─────────── [gốc: V15__them_hoa_don_chi_tiet_lo.sql] ─────────── */
/* V15: Ghi nhan phan bo lo khi tru ton (FEFO) de hoan ton dung lo da xuat. */

IF OBJECT_ID('hoa_don_chi_tiet_lo', 'U') IS NULL
BEGIN
    /* → BẢNG hoa_don_chi_tiet_lo: QUAN TRỌNG: ghi mỗi dòng hóa đơn lấy hàng TỪ LÔ NÀO → truy vết lô khi trả hàng. */
    CREATE TABLE hoa_don_chi_tiet_lo (
        id                      INT IDENTITY(1,1) PRIMARY KEY,
        id_hoa_don_chi_tiet     INT NOT NULL,
        id_lo_hang              INT NOT NULL,
        so_luong                INT NOT NULL,
        CONSTRAINT ck_hdctl_so_luong CHECK (so_luong > 0),
        CONSTRAINT fk_hdctl_hdct FOREIGN KEY (id_hoa_don_chi_tiet)
            REFERENCES hoa_don_chi_tiet(id) ON DELETE CASCADE,
        CONSTRAINT fk_hdctl_lo FOREIGN KEY (id_lo_hang)
            REFERENCES lo_hang(id)
    );

    CREATE INDEX ix_hdctl_hdct ON hoa_don_chi_tiet_lo(id_hoa_don_chi_tiet);
    CREATE INDEX ix_hdctl_lo ON hoa_don_chi_tiet_lo(id_lo_hang);
END
GO


/* ─────────── [gốc: V16__them_ket_qua_quiz.sql] ─────────── */
-- Bảng lưu kết quả làm Quiz của khách hàng
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[ket_qua_quiz]') AND type in (N'U'))
BEGIN
    /* → BẢNG ket_qua_quiz: Kết quả quiz của khách: khách này ra loại da gì, lúc nào. */
    CREATE TABLE ket_qua_quiz (
        id          INT IDENTITY(1,1) PRIMARY KEY,
        id_khach_hang INT NULL,
        id_loai_da_ket_qua INT NOT NULL,
        thoi_gian   DATETIME2 NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_ket_qua_quiz_khach_hang FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id),
        CONSTRAINT FK_ket_qua_quiz_loai_da FOREIGN KEY (id_loai_da_ket_qua) REFERENCES loai_da(id)
    );
END


/* ─────────── [gốc: V17__them_nhieu_san_pham_goi_y_chat.sql] ─────────── */
-- Thêm cột danh sách sản phẩm gợi ý dạng chuỗi (comma-separated) vào bảng tin_nhan_chat_ai
ALTER TABLE tin_nhan_chat_ai ADD danh_sach_sp_goi_y VARCHAR(255) NULL;
GO


/* ─────────── [gốc: V18__them_cau_7_quiz.sql] ─────────── */
-- Thêm câu 7
INSERT INTO cau_hoi_quiz (noi_dung, thu_tu) VALUES
(N'Hiện tại, làn da hoặc cơ thể bạn có đang trong giai đoạn cần chăm sóc đặc biệt không?', 7);
GO

-- Khai báo biến id_cau_7 để lấy ID vừa chèn
DECLARE @id_cau_7 INT = IDENT_CURRENT('cau_hoi_quiz');

-- Thêm đáp án cho câu 7 (Tạm thời gán id_loai_da = 5 là Da Nhạy Cảm, điểm = 0 vì đây là câu hỏi lọc)
INSERT INTO dap_an_quiz (id_cau_hoi, noi_dung, id_loai_da, diem, icon) VALUES
(@id_cau_7, N'Đang mang thai hoặc cho con bú', 5, 0, 'lucide:baby'),
(@id_cau_7, N'Đang trong liệu trình điều trị da chuyên sâu (Peel da, dùng Retinol...)', 5, 0, 'lucide:flask-conical'),
(@id_cau_7, N'Da đang có vết thương hở hoặc mụn viêm sưng tấy', 5, 0, 'lucide:band-aid'),
(@id_cau_7, N'Không, da tôi đang ở trạng thái ổn định', 5, 0, 'lucide:check-circle-2');
GO


/* ─────────── [gốc: V19__nang_cap_luat_quiz.sql] ─────────── */
-- Thêm cột filter_keyword vào bảng dap_an_quiz
-- Cột này dùng cho các đáp án "Lọc cứng" (Ví dụ: VAT_LY, HOA_HOC, LAI)
-- Nếu đáp án có filter_keyword, hệ thống sẽ dùng nó để lọc sản phẩm theo loại chống nắng
ALTER TABLE dap_an_quiz ADD filter_keyword VARCHAR(100) NULL;
GO

-- Cho phép id_loai_da NULL (đáp án dạng Filter thuần túy không cần gắn loại da)
ALTER TABLE dap_an_quiz ALTER COLUMN id_loai_da INT NULL;
GO


/* ─────────── [gốc: V20__them_routine_combo.sql] ─────────── */
/* =============================================
   V20: Tạo bảng Routine Combo + Dữ liệu mẫu
   ============================================= */

-- 1. Bảng chính: Routine Combo
    /* → BẢNG routine_combo: Combo routine theo loại da (gợi ý trọn bộ). */
CREATE TABLE routine_combo (
    id              INT IDENTITY(1,1) PRIMARY KEY,
    ten             NVARCHAR(200) NOT NULL,
    mo_ta           NVARCHAR(500),
    id_loai_da      INT NULL,
    trang_thai      BIT DEFAULT 1,
    thu_tu          INT DEFAULT 0,
    ngay_tao        DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT fk_routine_loai_da FOREIGN KEY (id_loai_da) REFERENCES loai_da(id)
);
GO

-- 2. Bảng chi tiết: Sản phẩm trong Routine
    /* → BẢNG routine_combo_chi_tiet: Các sản phẩm trong 1 combo routine. */
CREATE TABLE routine_combo_chi_tiet (
    id              INT IDENTITY(1,1) PRIMARY KEY,
    id_routine      INT NOT NULL,
    id_san_pham     INT NOT NULL,
    thu_tu          INT DEFAULT 0,
    ghi_chu         NVARCHAR(200),
    CONSTRAINT fk_routine_ct_routine FOREIGN KEY (id_routine) REFERENCES routine_combo(id) ON DELETE CASCADE,
    CONSTRAINT fk_routine_ct_san_pham FOREIGN KEY (id_san_pham) REFERENCES san_pham(id)
);
GO

-- 3. Dữ liệu mẫu: Routine phù hợp cho từng loại da
-- Loại da: 1=Da Dầu, 2=Da Khô, 3=Da Hỗn Hợp, 4=Da Thường, 5=Da Nhạy Cảm
-- Sản phẩm: 1=Anessa(HOA_HOC,DaDầu), 2=LaRoche(HOA_HOC,DaNhạyCảm), 3=Skin1004(VAT_LY,DaNhạyCảm), 4=Sunplay(HOA_HOC,DaDầu), 5=Vichy(HOA_HOC,DaThường)

-- Combo 1: Dành cho Da Dầu
INSERT INTO routine_combo (ten, mo_ta, id_loai_da, trang_thai, thu_tu) VALUES
(N'Combo Kiềm Dầu Suốt Ngày', N'Bộ đôi chống nắng kiềm dầu hoàn hảo cho da dầu, giữ da mịn không bóng nhờn cả ngày dài.', 1, 1, 1);
GO
INSERT INTO routine_combo_chi_tiet (id_routine, id_san_pham, thu_tu, ghi_chu) VALUES
(1, 1, 1, N'Chống nắng chính - kháng nước, kiềm dầu'),
(1, 4, 2, N'Xịt chống nắng body tiện lợi khi ra ngoài');
GO

-- Combo 2: Dành cho Da Nhạy Cảm
INSERT INTO routine_combo (ten, mo_ta, id_loai_da, trang_thai, thu_tu) VALUES
(N'Combo An Toàn Cho Da Nhạy Cảm', N'Chỉ sử dụng các sản phẩm dịu nhẹ, không kích ứng, phù hợp cho da nhạy cảm và da sau peel.', 5, 1, 2);
GO
INSERT INTO routine_combo_chi_tiet (id_routine, id_san_pham, thu_tu, ghi_chu) VALUES
(2, 3, 1, N'Kem chống nắng Vật Lý - dịu nhẹ, rau má làm dịu'),
(2, 2, 2, N'Kem chống nắng chuyên biệt cho da nhạy cảm');
GO

-- Combo 3: Dành cho Da Hỗn Hợp
INSERT INTO routine_combo (ten, mo_ta, id_loai_da, trang_thai, thu_tu) VALUES
(N'Combo Cân Bằng Da Hỗn Hợp', N'Kết hợp chống nắng nhẹ dàng cho vùng khô và kiềm dầu cho vùng chữ T.', 3, 1, 3);
GO
INSERT INTO routine_combo_chi_tiet (id_routine, id_san_pham, thu_tu, ghi_chu) VALUES
(3, 5, 1, N'Chống nắng nâng tông, chống lão hóa'),
(3, 3, 2, N'Kem chống nắng dịu nhẹ cho vùng má khô');
GO

-- Combo 4: Dành cho Da Thường
INSERT INTO routine_combo (ten, mo_ta, id_loai_da, trang_thai, thu_tu) VALUES
(N'Combo Bảo Vệ Toàn Diện', N'Da thường có thể dùng hầu hết mọi sản phẩm. Combo này giúp bảo vệ toàn diện cả mặt lẫn body.', 4, 1, 4);
GO
INSERT INTO routine_combo_chi_tiet (id_routine, id_san_pham, thu_tu, ghi_chu) VALUES
(4, 5, 1, N'Chống nắng nâng tông cho mặt'),
(4, 1, 2, N'Sữa chống nắng kháng nước cho body');
GO

-- Combo 5: Dành cho Da Khô
INSERT INTO routine_combo (ten, mo_ta, id_loai_da, trang_thai, thu_tu) VALUES
(N'Combo Cấp Ẩm & Chống Nắng', N'Da khô cần được cấp ẩm sâu kèm chống nắng. Combo này giúp da không bị căng khô khi ra nắng.', 2, 1, 5);
GO
INSERT INTO routine_combo_chi_tiet (id_routine, id_san_pham, thu_tu, ghi_chu) VALUES
(5, 3, 1, N'Kem chống nắng cấp ẩm từ rau má'),
(5, 2, 2, N'Kem chống nắng dưỡng ẩm cho da nhạy cảm/khô');
GO


/* ─────────── [gốc: V21__tach_hoan_tien_khoi_duyet_tra_hang.sql] ─────────── */
/* V21: Tach hoan tien ra khoi buoc duyet yeu cau tra hang.
   Luong moi: admin duyet -> khach tao van don hoan (chon ca lay hang GHN)
   -> van don hoan hoan thanh (shop nhan hang) -> admin quyet dinh hoan tien hay khong.
   Bo sung cho yeu_cau_tra_hang: ca lay hang GHN, trang thai van don hoan, moc nhan hang. */

IF COL_LENGTH('yeu_cau_tra_hang', 'pick_shift_id') IS NULL
    ALTER TABLE yeu_cau_tra_hang ADD pick_shift_id INT NULL;
GO

IF COL_LENGTH('yeu_cau_tra_hang', 'pick_shift_label') IS NULL
    ALTER TABLE yeu_cau_tra_hang ADD pick_shift_label NVARCHAR(100) NULL;
GO

IF COL_LENGTH('yeu_cau_tra_hang', 'ghn_trang_thai_tra') IS NULL
    ALTER TABLE yeu_cau_tra_hang ADD ghn_trang_thai_tra VARCHAR(50) NULL;
GO

IF COL_LENGTH('yeu_cau_tra_hang', 'ngay_nhan_hang') IS NULL
    ALTER TABLE yeu_cau_tra_hang ADD ngay_nhan_hang DATETIME NULL;
GO


/* ─────────── [gốc: V22__them_anh_hoan_tien.sql] ─────────── */
/* V22: Anh chung tu chuyen khoan khi admin hoan tat hoan tien thu cong (COD / CHUYEN_KHOAN). */

IF OBJECT_ID('anh_hoan_tien', 'U') IS NULL
BEGIN
    /* → BẢNG anh_hoan_tien: Ảnh minh chứng hoàn tiền. */
    CREATE TABLE anh_hoan_tien (
        id              INT IDENTITY(1,1) PRIMARY KEY,
        id_hoan_tien    INT NOT NULL,
        duong_dan       NVARCHAR(500) NOT NULL,
        ngay_tao        DATETIME NOT NULL DEFAULT GETDATE(),
        CONSTRAINT fk_aht_hoan_tien FOREIGN KEY (id_hoan_tien) REFERENCES hoan_tien(id)
    );
END
GO


/* ─────────── [gốc: V23__them_ho_tro_chat.sql] ─────────── */
/* V23: Chat ho tro khach hang (khach <-> nhan vien), shared inbox. */

IF OBJECT_ID('phien_ho_tro', 'U') IS NULL
BEGIN
    /* → BẢNG phien_ho_tro: Phiên chat hỗ trợ khách ↔ nhân viên (shared inbox). */
    CREATE TABLE phien_ho_tro (
        id                  INT IDENTITY(1,1) PRIMARY KEY,
        id_khach_hang       INT NULL,
        trang_thai          VARCHAR(10) NOT NULL CONSTRAINT DF_pht_trang_thai DEFAULT ('MO'),
        nguoi_xu_ly_id      INT NULL,
        ngay_tao            DATETIME NOT NULL CONSTRAINT DF_pht_ngay_tao DEFAULT (GETDATE()),
        cap_nhat_cuoi       DATETIME NOT NULL CONSTRAINT DF_pht_cap_nhat DEFAULT (GETDATE()),
        CONSTRAINT fk_pht_khach FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id),
        CONSTRAINT fk_pht_nv FOREIGN KEY (nguoi_xu_ly_id) REFERENCES nhan_vien(id),
        CONSTRAINT ck_pht_trang_thai CHECK (trang_thai IN ('MO', 'DONG'))
    );
END
GO

IF OBJECT_ID('tin_nhan_ho_tro', 'U') IS NULL
BEGIN
    /* → BẢNG tin_nhan_ho_tro: Từng tin nhắn trong phiên hỗ trợ (KHACH/NHAN_VIEN). */
    CREATE TABLE tin_nhan_ho_tro (
        id              INT IDENTITY(1,1) PRIMARY KEY,
        id_phien        INT NOT NULL,
        nguoi_gui       VARCHAR(15) NOT NULL,
        id_nguoi_gui    INT NULL,
        noi_dung        NVARCHAR(2000) NOT NULL,
        da_doc          BIT NOT NULL CONSTRAINT DF_tnht_da_doc DEFAULT (0),
        thoi_gian       DATETIME NOT NULL CONSTRAINT DF_tnht_thoi_gian DEFAULT (GETDATE()),
        CONSTRAINT fk_tnht_phien FOREIGN KEY (id_phien) REFERENCES phien_ho_tro(id),
        CONSTRAINT ck_tnht_nguoi_gui CHECK (nguoi_gui IN ('KHACH', 'NHAN_VIEN'))
    );

    CREATE INDEX ix_tnht_phien_thoigian ON tin_nhan_ho_tro (id_phien, thoi_gian);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_pht_trang_thai' AND object_id = OBJECT_ID('phien_ho_tro'))
BEGIN
    CREATE INDEX ix_pht_trang_thai ON phien_ho_tro (trang_thai, cap_nhat_cuoi DESC);
END
GO


/* ─────────── [gốc: V24__them_tra_hang_theo_lo.sql] ─────────── */
/* V24: Tra hang theo lo + phan loai hang TOT/LOI.
   - lo_hang.so_luong_loi: hang loi da ghi nhan (khong nam trong ton ban duoc)
   - chi_tiet_tra_hang_lo: chi tiet nhan hang tra theo tung lo (TOT|LOI) */

IF COL_LENGTH('lo_hang', 'so_luong_loi') IS NULL
BEGIN
    ALTER TABLE lo_hang ADD so_luong_loi INT NOT NULL CONSTRAINT df_lo_hang_so_luong_loi DEFAULT 0;
END
GO

IF OBJECT_ID('chi_tiet_tra_hang_lo', 'U') IS NULL
BEGIN
    /* → BẢNG chi_tiet_tra_hang_lo: Phân loại hàng trả theo lô: TỐT (hoàn về lô) / LỖI (ghi so_luong_loi). */
    CREATE TABLE chi_tiet_tra_hang_lo (
        id                      INT IDENTITY(1,1) PRIMARY KEY,
        id_yeu_cau_tra_hang     INT NOT NULL,
        id_lo_hang              INT NOT NULL,
        so_luong                INT NOT NULL,
        loai                    VARCHAR(10) NOT NULL,
        CONSTRAINT ck_ctthl_so_luong CHECK (so_luong > 0),
        CONSTRAINT ck_ctthl_loai CHECK (loai IN ('TOT', 'LOI')),
        CONSTRAINT fk_ctthl_yeu_cau FOREIGN KEY (id_yeu_cau_tra_hang)
            REFERENCES yeu_cau_tra_hang(id) ON DELETE CASCADE,
        CONSTRAINT fk_ctthl_lo FOREIGN KEY (id_lo_hang)
            REFERENCES lo_hang(id)
    );

    CREATE INDEX ix_ctthl_yeu_cau ON chi_tiet_tra_hang_lo(id_yeu_cau_tra_hang);
    CREATE INDEX ix_ctthl_lo ON chi_tiet_tra_hang_lo(id_lo_hang);
END
GO


/* ─────────── [gốc: V25__them_id_mau_sac_anh_san_pham.sql] ─────────── */
/* V25: Gan anh san pham theo mau (id_mau_sac).
   NULL = anh dung chung cho moi mau; co gia tri = anh rieng cua mau do.
   (Da nhung vao CREATE anh_san_pham o tren — khoi nay giu idempotent.) */

IF COL_LENGTH('anh_san_pham', 'id_mau_sac') IS NULL
BEGIN
    ALTER TABLE anh_san_pham ADD id_mau_sac INT NULL;

    ALTER TABLE anh_san_pham
        ADD CONSTRAINT fk_anh_ms FOREIGN KEY (id_mau_sac) REFERENCES mau_sac(id);
END
GO


/* ─────────── [gốc: V26__them_banner_trang_chu.sql] ─────────── */
/* V26: Banner trang chủ (CMS) */

IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[banner_trang_chu]') AND type in (N'U'))
BEGIN
    /* → BẢNG banner_trang_chu: Banner CMS trang chủ (tiêu đề, CTA, ảnh, thứ tự). */
    CREATE TABLE banner_trang_chu (
        id              INT IDENTITY(1,1) PRIMARY KEY,
        tieu_de         NVARCHAR(200) NULL,
        tieu_de_chinh   NVARCHAR(300) NOT NULL,
        mo_ta           NVARCHAR(1000) NULL,
        nut_text        NVARCHAR(100) NULL,
        link_url        NVARCHAR(500) NOT NULL,
        anh_url         NVARCHAR(500) NULL,
        thu_tu          INT NOT NULL DEFAULT 0,
        trang_thai      BIT NOT NULL DEFAULT 1,
        ngay_tao        DATETIME2 NOT NULL DEFAULT GETDATE()
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM banner_trang_chu)
BEGIN
    INSERT INTO banner_trang_chu (tieu_de, tieu_de_chinh, mo_ta, nut_text, link_url, anh_url, thu_tu, trang_thai)
    VALUES (
        N'Trắc nghiệm da',
        N'Tìm sản phẩm chống nắng phù hợp với bạn',
        N'Trả lời vài câu hỏi ngắn — hệ thống SUNOVA sẽ phân tích làn da và gợi ý sản phẩm hoàn hảo dành riêng cho bạn.',
        N'Làm Quiz Ngay',
        N'/quiz',
        NULL,
        1,
        1
    );
END
GO


/* ################################################################
   #  DỮ LIỆU FAKE: THÊM 10 SẢN PHẨM (SP006–SP015) → tổng 15 SP    #
   ################################################################ */
IF NOT EXISTS (SELECT 1 FROM thuong_hieu WHERE ma='TH06')
INSERT INTO thuong_hieu (ma, ten, xuat_xu) VALUES
 ('TH06', N'Bioré',   N'Nhật Bản'),
 ('TH07', N'Eucerin', N'Đức'),
 ('TH08', N'EltaMD',  N'Mỹ'),
 ('TH09', N'Cỡ Mềm',  N'Việt Nam'),
 ('TH10', N'Innisfree', N'Hàn Quốc');
GO

/* ---------- 2. THÊM 10 SẢN PHẨM (SP006–SP015) ----------
   Trải đều 5 danh mục: DM01 mặt, DM02 cơ thể, DM03 nâng tông, DM04 da mụn, DM05 trẻ em.
   loai_chong_nang: VAT_LY / HOA_HOC / LAI. */
IF NOT EXISTS (SELECT 1 FROM san_pham WHERE ma_san_pham='SP006')
INSERT INTO san_pham (ma_san_pham, ten, id_thuong_hieu, id_danh_muc, id_dang_san_pham, chi_so_spf, chi_so_pa, loai_chong_nang, khang_nuoc, mo_ta, noi_bat) VALUES
 -- Da mặt
 ('SP006', N'Anessa Perfect UV Sunscreen Skin Care Gel',
   (SELECT id FROM thuong_hieu WHERE ma='TH01'),(SELECT id FROM danh_muc WHERE ma='DM01'),(SELECT id FROM dang_san_pham WHERE ma='DSP03'),
   'SPF50+','PA++++','HOA_HOC',1,N'Gel chống nắng Anessa mỏng nhẹ, thấm nhanh, kháng nước, hợp da dầu.',1),
 ('SP007', N'La Roche-Posay Anthelios UVMune 400',
   (SELECT id FROM thuong_hieu WHERE ma='TH02'),(SELECT id FROM danh_muc WHERE ma='DM01'),(SELECT id FROM dang_san_pham WHERE ma='DSP02'),
   'SPF50+','PA++++','LAI',1,N'Kem chống nắng phổ rộng chống tia UVA dài, hợp da nhạy cảm.',1),
 ('SP008', N'Bioré UV Aqua Rich Watery Essence',
   (SELECT id FROM thuong_hieu WHERE ma='TH06'),(SELECT id FROM danh_muc WHERE ma='DM01'),(SELECT id FROM dang_san_pham WHERE ma='DSP03'),
   'SPF50+','PA++++','HOA_HOC',1,N'Tinh chất chống nắng mọng nước, mỏng nhẹ, giá tốt.',0),
 -- Cơ thể
 ('SP009', N'Sunplay Skin Aqua Clear White',
   (SELECT id FROM thuong_hieu WHERE ma='TH05'),(SELECT id FROM danh_muc WHERE ma='DM02'),(SELECT id FROM dang_san_pham WHERE ma='DSP01'),
   'SPF50+','PA++++','HOA_HOC',1,N'Sữa chống nắng dưỡng trắng, dùng cho mặt và toàn thân.',0),
 ('SP010', N'Vichy Capital Soleil Body Milk',
   (SELECT id FROM thuong_hieu WHERE ma='TH03'),(SELECT id FROM danh_muc WHERE ma='DM02'),(SELECT id FROM dang_san_pham WHERE ma='DSP01'),
   'SPF50+','PA++++','LAI',1,N'Sữa chống nắng cơ thể Vichy, kháng nước, bảo vệ lâu.',0),
 -- Nâng tông
 ('SP011', N'Skin1004 Centella Tone Brightening Sun',
   (SELECT id FROM thuong_hieu WHERE ma='TH04'),(SELECT id FROM danh_muc WHERE ma='DM03'),(SELECT id FROM dang_san_pham WHERE ma='DSP02'),
   'SPF50+','PA++++','LAI',0,N'Kem chống nắng nâng tông rau má, làm sáng tự nhiên.',1),
 ('SP012', N'Innisfree Tone Up No Sebum Sun',
   (SELECT id FROM thuong_hieu WHERE ma='TH10'),(SELECT id FROM danh_muc WHERE ma='DM03'),(SELECT id FROM dang_san_pham WHERE ma='DSP03'),
   'SPF50+','PA++++','LAI',0,N'Chống nắng nâng tông kiềm dầu, cho lớp nền sáng mịn.',0),
 -- Da mụn
 ('SP013', N'La Roche-Posay Anthelios Oil Correct',
   (SELECT id FROM thuong_hieu WHERE ma='TH02'),(SELECT id FROM danh_muc WHERE ma='DM04'),(SELECT id FROM dang_san_pham WHERE ma='DSP03'),
   'SPF50+','PA++++','LAI',1,N'Gel chống nắng cho da dầu mụn, kiềm dầu, không gây bít tắc.',1),
 ('SP014', N'EltaMD UV Clear Broad-Spectrum',
   (SELECT id FROM thuong_hieu WHERE ma='TH08'),(SELECT id FROM danh_muc WHERE ma='DM04'),(SELECT id FROM dang_san_pham WHERE ma='DSP02'),
   'SPF46','PA+++','VAT_LY',0,N'Kem chống nắng cho da mụn nhạy cảm, chứa Niacinamide làm dịu.',1),
 -- Trẻ em
 ('SP015', N'Eucerin Sun Kids Sensitive Protect',
   (SELECT id FROM thuong_hieu WHERE ma='TH07'),(SELECT id FROM danh_muc WHERE ma='DM05'),(SELECT id FROM dang_san_pham WHERE ma='DSP01'),
   'SPF50+','PA++++','VAT_LY',1,N'Sữa chống nắng vật lý dịu nhẹ cho làn da nhạy cảm của trẻ.',0);
GO

/* ---------- 3. BIẾN THỂ (chi_tiet_san_pham) cho SP006–SP015 ----------
   Đa số MS01 (không màu). SP011/SP012 nâng tông có thêm biến thể MS02/MS03 để demo ảnh theo màu (V25).
   so_luong_ton để 0, sẽ đồng bộ từ lô ở mục 5. */
IF NOT EXISTS (SELECT 1 FROM chi_tiet_san_pham WHERE sku='SP006-GEL-60')
INSERT INTO chi_tiet_san_pham (id_san_pham, id_mau_sac, sku, dung_tich_ml, gia_ban, so_luong_ton) VALUES
 ((SELECT id FROM san_pham WHERE ma_san_pham='SP006'),(SELECT id FROM mau_sac WHERE ma='MS01'),'SP006-GEL-60',   60, 550000,0),
 ((SELECT id FROM san_pham WHERE ma_san_pham='SP007'),(SELECT id FROM mau_sac WHERE ma='MS01'),'SP007-CREAM-50', 50, 495000,0),
 ((SELECT id FROM san_pham WHERE ma_san_pham='SP008'),(SELECT id FROM mau_sac WHERE ma='MS01'),'SP008-ESS-50',   50, 225000,0),
 ((SELECT id FROM san_pham WHERE ma_san_pham='SP009'),(SELECT id FROM mau_sac WHERE ma='MS01'),'SP009-MILK-70',  70, 185000,0),
 ((SELECT id FROM san_pham WHERE ma_san_pham='SP010'),(SELECT id FROM mau_sac WHERE ma='MS01'),'SP010-MILK-150',150, 395000,0),
 ((SELECT id FROM san_pham WHERE ma_san_pham='SP011'),(SELECT id FROM mau_sac WHERE ma='MS01'),'SP011-CREAM-50', 50, 360000,0),
 ((SELECT id FROM san_pham WHERE ma_san_pham='SP011'),(SELECT id FROM mau_sac WHERE ma='MS02'),'SP011-CREAM-50-TN',50, 360000,0),
 ((SELECT id FROM san_pham WHERE ma_san_pham='SP012'),(SELECT id FROM mau_sac WHERE ma='MS01'),'SP012-GEL-50',   50, 310000,0),
 ((SELECT id FROM san_pham WHERE ma_san_pham='SP012'),(SELECT id FROM mau_sac WHERE ma='MS03'),'SP012-GEL-50-SANG',50, 310000,0),
 ((SELECT id FROM san_pham WHERE ma_san_pham='SP013'),(SELECT id FROM mau_sac WHERE ma='MS01'),'SP013-GEL-50',   50, 460000,0),
 ((SELECT id FROM san_pham WHERE ma_san_pham='SP014'),(SELECT id FROM mau_sac WHERE ma='MS01'),'SP014-CREAM-48', 48, 720000,0),
 ((SELECT id FROM san_pham WHERE ma_san_pham='SP015'),(SELECT id FROM mau_sac WHERE ma='MS01'),'SP015-MILK-75',  75, 340000,0);
GO

/* ---------- 4. LÔ HÀNG cho các biến thể mới ----------
   Có sẵn: lô cận hạn (SP008) + lô tồn thấp ≤10 (SP012) để demo cảnh báo;
   SP006 có 2 lô khác hạn để demo FEFO. */
IF NOT EXISTS (SELECT 1 FROM lo_hang WHERE so_lo='LO0601')
INSERT INTO lo_hang (id_chi_tiet_san_pham, so_lo, ngay_nhap, han_su_dung, so_luong_nhap, so_luong_con) VALUES
 ((SELECT id FROM chi_tiet_san_pham WHERE sku='SP006-GEL-60'),   'LO0601','2026-03-01','2026-11-30',50,50),
 ((SELECT id FROM chi_tiet_san_pham WHERE sku='SP006-GEL-60'),   'LO0602','2026-06-01','2027-06-30',40,40),
 ((SELECT id FROM chi_tiet_san_pham WHERE sku='SP007-CREAM-50'), 'LO0701','2026-05-01','2027-05-31',60,60),
 ((SELECT id FROM chi_tiet_san_pham WHERE sku='SP008-ESS-50'),   'LO0801','2026-02-01','2026-10-31',30,30),
 ((SELECT id FROM chi_tiet_san_pham WHERE sku='SP009-MILK-70'),  'LO0901','2026-04-01','2027-04-30',70,70),
 ((SELECT id FROM chi_tiet_san_pham WHERE sku='SP010-MILK-150'), 'LO1001','2026-04-10','2027-06-30',40,40),
 ((SELECT id FROM chi_tiet_san_pham WHERE sku='SP011-CREAM-50'), 'LO1101','2026-05-15','2027-05-31',55,55),
 ((SELECT id FROM chi_tiet_san_pham WHERE sku='SP011-CREAM-50-TN'),'LO1102','2026-05-15','2027-05-31',40,40),
 ((SELECT id FROM chi_tiet_san_pham WHERE sku='SP012-GEL-50'),   'LO1201','2026-06-01','2027-07-31', 8, 8),
 ((SELECT id FROM chi_tiet_san_pham WHERE sku='SP012-GEL-50-SANG'),'LO1202','2026-06-01','2027-07-31',25,25),
 ((SELECT id FROM chi_tiet_san_pham WHERE sku='SP013-GEL-50'),   'LO1301','2026-05-20','2027-05-31',65,65),
 ((SELECT id FROM chi_tiet_san_pham WHERE sku='SP014-CREAM-48'), 'LO1401','2026-04-25','2027-04-30',35,35),
 ((SELECT id FROM chi_tiet_san_pham WHERE sku='SP015-MILK-75'),  'LO1501','2026-06-05','2027-08-31',50,50);
GO

/* ---------- 5. ĐỒNG BỘ tồn kho cache = tổng số còn của các lô ---------- */
UPDATE ct SET ct.so_luong_ton = ISNULL(l.tong,0)
FROM chi_tiet_san_pham ct
LEFT JOIN (SELECT id_chi_tiet_san_pham, SUM(so_luong_con) tong FROM lo_hang GROUP BY id_chi_tiet_san_pham) l
  ON l.id_chi_tiet_san_pham = ct.id;
GO

/* ---------- 6. ẢNH cho SP006–SP015 (+ ảnh theo màu cho SP011/SP012) ---------- */
IF NOT EXISTS (
    SELECT 1 FROM anh_san_pham a
    JOIN san_pham sp ON sp.id = a.id_san_pham
    WHERE sp.ma_san_pham = 'SP006'
)
INSERT INTO anh_san_pham (id_san_pham, id_chi_tiet_san_pham, id_mau_sac, url, la_anh_chinh, thu_tu) VALUES
 ((SELECT id FROM san_pham WHERE ma_san_pham='SP006'), NULL, NULL, 'https://cdn.sunova.vn/sp006.jpg', 1, 1),
 ((SELECT id FROM san_pham WHERE ma_san_pham='SP007'), NULL, NULL, 'https://cdn.sunova.vn/sp007.jpg', 1, 1),
 ((SELECT id FROM san_pham WHERE ma_san_pham='SP008'), NULL, NULL, 'https://cdn.sunova.vn/sp008.jpg', 1, 1),
 ((SELECT id FROM san_pham WHERE ma_san_pham='SP009'), NULL, NULL, 'https://cdn.sunova.vn/sp009.jpg', 1, 1),
 ((SELECT id FROM san_pham WHERE ma_san_pham='SP010'), NULL, NULL, 'https://cdn.sunova.vn/sp010.jpg', 1, 1),
 -- SP011: ảnh chung + ảnh riêng tông tự nhiên (MS02)
 ((SELECT id FROM san_pham WHERE ma_san_pham='SP011'), NULL, NULL, 'https://cdn.sunova.vn/sp011.jpg', 1, 1),
 ((SELECT id FROM san_pham WHERE ma_san_pham='SP011'),
  (SELECT id FROM chi_tiet_san_pham WHERE sku='SP011-CREAM-50-TN'),
  (SELECT id FROM mau_sac WHERE ma='MS02'),
  'https://cdn.sunova.vn/sp011-tone-tn.jpg', 0, 2),
 -- SP012: ảnh chung + ảnh riêng tông sáng (MS03)
 ((SELECT id FROM san_pham WHERE ma_san_pham='SP012'), NULL, NULL, 'https://cdn.sunova.vn/sp012.jpg', 1, 1),
 ((SELECT id FROM san_pham WHERE ma_san_pham='SP012'),
  (SELECT id FROM chi_tiet_san_pham WHERE sku='SP012-GEL-50-SANG'),
  (SELECT id FROM mau_sac WHERE ma='MS03'),
  'https://cdn.sunova.vn/sp012-tone-sang.jpg', 0, 2),
 ((SELECT id FROM san_pham WHERE ma_san_pham='SP013'), NULL, NULL, 'https://cdn.sunova.vn/sp013.jpg', 1, 1),
 ((SELECT id FROM san_pham WHERE ma_san_pham='SP014'), NULL, NULL, 'https://cdn.sunova.vn/sp014.jpg', 1, 1),
 ((SELECT id FROM san_pham WHERE ma_san_pham='SP015'), NULL, NULL, 'https://cdn.sunova.vn/sp015.jpg', 1, 1);
GO

/* ---------- 7. GÁN LOẠI DA / CÔNG DỤNG cho SP mới (gợi ý + bộ lọc) ---------- */
IF NOT EXISTS (
    SELECT 1 FROM san_pham_loai_da sld
    JOIN san_pham sp ON sp.id = sld.id_san_pham
    WHERE sp.ma_san_pham = 'SP006'
)
INSERT INTO san_pham_loai_da (id_san_pham, id_loai_da)
SELECT sp.id, ld.id
FROM (VALUES
    ('SP006', 'DA_DAU'),
    ('SP007', 'DA_NHAY_CAM'),
    ('SP008', 'DA_HON_HOP'),
    ('SP009', 'DA_THUONG'),
    ('SP010', 'DA_THUONG'),
    ('SP011', 'DA_KHO'),
    ('SP012', 'DA_DAU'),
    ('SP013', 'DA_DAU'),
    ('SP014', 'DA_NHAY_CAM'),
    ('SP015', 'DA_NHAY_CAM')
) v(ma_sp, ma_da)
JOIN san_pham sp ON sp.ma_san_pham = v.ma_sp
JOIN loai_da ld ON ld.ma = v.ma_da;
GO

IF NOT EXISTS (
    SELECT 1 FROM san_pham_cong_dung scd
    JOIN san_pham sp ON sp.id = scd.id_san_pham
    WHERE sp.ma_san_pham = 'SP006'
)
INSERT INTO san_pham_cong_dung (id_san_pham, id_cong_dung)
SELECT sp.id, cd.id
FROM (VALUES
    ('SP006', 'CD04'),
    ('SP007', 'CD05'),
    ('SP008', 'CD02'),
    ('SP009', 'CD03'),
    ('SP010', 'CD04'),
    ('SP011', 'CD03'),
    ('SP012', 'CD01'),
    ('SP013', 'CD01'),
    ('SP014', 'CD05'),
    ('SP015', 'CD05')
) v(ma_sp, ma_cd)
JOIN san_pham sp ON sp.ma_san_pham = v.ma_sp
JOIN cong_dung cd ON cd.ma = v.ma_cd;
GO

/* ═══════════════ HẾT — schema V1→V26 + tổng 15 sản phẩm ═══════════════ */

/* ################################################################
   #  CHỨC NĂNG NHẬP HÀNG (Phiếu nhập + Nhà cung cấp)             #
   #  1 PHIẾU NHẬP chứa nhiều spct; khi Hoàn thành → mỗi dòng     #
   #  sinh 1 LÔ cho biến thể tương ứng (đúng chuẩn kho).          #
   #  Chỉ Admin/Quản lý dùng. Tham chiếu chi_tiet_san_pham,       #
   #  nhan_vien (đã tạo ở phần trên).                             #
   ################################################################ */

/* → BẢNG nha_cung_cap: nhà cung cấp hàng nhập (tên, liên hệ). */
CREATE TABLE nha_cung_cap (
    id            INT IDENTITY(1,1) PRIMARY KEY,
    ma            VARCHAR(20)  NOT NULL UNIQUE,      -- NCC0001, NCC0002... (tự sinh)
    ten           NVARCHAR(200) NOT NULL,
    so_dien_thoai VARCHAR(20),
    email         VARCHAR(100),
    dia_chi       NVARCHAR(255),
    ghi_chu       NVARCHAR(255),
    trang_thai    BIT DEFAULT 1                      -- 1=đang dùng, 0=ẩn (xóa mềm)
);
GO

/* → BẢNG phieu_nhap: mỗi phiếu = 1 lần nhập, 1 nhà cung cấp, nhiều mặt hàng.
     trang_thai: PHIEU_TAM (lưu tạm, chưa cộng kho) / DA_NHAP (đã cộng kho, đã sinh lô) / DA_HUY. */
CREATE TABLE phieu_nhap (
    id                   INT IDENTITY(1,1) PRIMARY KEY,
    ma_phieu_nhap        VARCHAR(20) NOT NULL UNIQUE,   -- PN000001... (tự sinh)
    id_nha_cung_cap      INT NULL,                      -- có thể null khi mới lưu tạm
    id_nhan_vien         INT NOT NULL,                  -- người tạo phiếu (lấy từ JWT)
    so_hoa_don_dau_vao   VARCHAR(50),                   -- số hóa đơn nhà cung cấp (nếu có)
    ngay_tao             DATETIME2 DEFAULT SYSDATETIME(),
    tong_tien            DECIMAL(18,2) DEFAULT 0,        -- tổng thành tiền các dòng
    giam_gia             DECIMAL(18,2) DEFAULT 0,
    can_tra_ncc          DECIMAL(18,2) DEFAULT 0,        -- còn phải trả nhà cung cấp
    trang_thai           VARCHAR(20) NOT NULL DEFAULT 'PHIEU_TAM',
    ghi_chu              NVARCHAR(500),
    CONSTRAINT fk_pn_ncc FOREIGN KEY (id_nha_cung_cap) REFERENCES nha_cung_cap(id),
    CONSTRAINT fk_pn_nv  FOREIGN KEY (id_nhan_vien)    REFERENCES nhan_vien(id),
    CONSTRAINT ck_pn_trang_thai CHECK (trang_thai IN ('PHIEU_TAM','DA_NHAP','DA_HUY'))
);
GO

/* → BẢNG chi_tiet_phieu_nhap: mỗi dòng = 1 biến thể (spct) + số lượng + đơn giá + HSD.
     Khi phiếu Hoàn thành, mỗi dòng sinh 1 lô (id_lo_hang lưu lại để truy vết). */
CREATE TABLE chi_tiet_phieu_nhap (
    id                    INT IDENTITY(1,1) PRIMARY KEY,
    id_phieu_nhap         INT NOT NULL,
    id_chi_tiet_san_pham  INT NOT NULL,                 -- biến thể (SKU) được nhập
    so_luong              INT NOT NULL,
    don_gia               DECIMAL(18,2) NOT NULL DEFAULT 0,  -- giá nhập
    han_su_dung           DATE,                          -- HSD của lô sẽ sinh (nhập tay)
    so_lo                 VARCHAR(40),                   -- số lô (tự sinh nếu để trống)
    thanh_tien            DECIMAL(18,2) DEFAULT 0,
    id_lo_hang            INT NULL,                      -- lô sinh ra khi hoàn thành (truy vết)
    CONSTRAINT fk_ctpn_pn  FOREIGN KEY (id_phieu_nhap)        REFERENCES phieu_nhap(id) ON DELETE CASCADE,
    CONSTRAINT fk_ctpn_cts FOREIGN KEY (id_chi_tiet_san_pham) REFERENCES chi_tiet_san_pham(id),
    CONSTRAINT fk_ctpn_lo  FOREIGN KEY (id_lo_hang)           REFERENCES lo_hang(id),
    CONSTRAINT ck_ctpn_sl  CHECK (so_luong > 0)
);
CREATE INDEX ix_ctpn_phieu ON chi_tiet_phieu_nhap(id_phieu_nhap);
CREATE INDEX ix_ctpn_cts   ON chi_tiet_phieu_nhap(id_chi_tiet_san_pham);
GO

/* → SEED nhà cung cấp mẫu (để demo có sẵn NCC chọn) */
INSERT INTO nha_cung_cap (ma, ten, so_dien_thoai, email, dia_chi) VALUES
 ('NCC0001', N'Công ty TNHH Mỹ phẩm Hoàng Gia', '0281234501', 'hoanggia@ncc.vn',  N'Quận 1, TP.HCM'),
 ('NCC0002', N'Nhà phân phối Anessa Việt Nam',   '0281234502', 'anessa@ncc.vn',    N'Quận 3, TP.HCM'),
 ('NCC0003', N'Đại lý L''Oréal Việt Nam',        '0241234503', 'loreal@ncc.vn',    N'Cầu Giấy, Hà Nội'),
 ('NCC0004', N'Công ty Dược mỹ phẩm Hồng Phúc',  '0241234504', 'hongphuc@ncc.vn',  N'Đống Đa, Hà Nội');
GO

/* ═══════════════ HẾT — chức năng nhập hàng ═══════════════ */
