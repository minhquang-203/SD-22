-- Bảng lưu kết quả làm Quiz của khách hàng
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[ket_qua_quiz]') AND type in (N'U'))
BEGIN
    CREATE TABLE ket_qua_quiz (
        id          INT IDENTITY(1,1) PRIMARY KEY,
        id_khach_hang INT NULL,
        id_loai_da_ket_qua INT NOT NULL,
        thoi_gian   DATETIME2 NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_ket_qua_quiz_khach_hang FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id),
        CONSTRAINT FK_ket_qua_quiz_loai_da FOREIGN KEY (id_loai_da_ket_qua) REFERENCES loai_da(id)
    );
END
