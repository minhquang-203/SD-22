/* =============================================
   V26: Banner trang chủ (CMS)
   ============================================= */

IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[banner_trang_chu]') AND type in (N'U'))
BEGIN
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
