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
