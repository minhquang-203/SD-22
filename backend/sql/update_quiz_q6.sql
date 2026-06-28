-- CHẠY FILE NÀY ĐỂ CẬP NHẬT BỘ CÂU HỎI QUIZ MỚI (6 CÂU) VÀO DATABASE
-- XOÁ DỮ LIỆU QUIZ CŨ
DELETE FROM dap_an_quiz;
DELETE FROM cau_hoi_quiz;

-- RESET LẠI ID VỀ 0 ĐỂ BẮT ĐẦU TỪ 1
DBCC CHECKIDENT ('cau_hoi_quiz', RESEED, 0);
DBCC CHECKIDENT ('dap_an_quiz', RESEED, 0);

-- CÂU HỎI QUIZ MỚI (6 Câu)
INSERT INTO cau_hoi_quiz (noi_dung, thu_tu, trang_thai) VALUES
(N'Yêu cầu quan trọng nhất của bạn đối với kem chống nắng là gì?', 1, 1),
(N'Làn da của bạn thường có biểu hiện gì vào thời điểm giữa ngày hoặc cuối ngày?', 2, 1),
(N'Hiệu ứng bề mặt (Finish) bạn mong muốn nhất sau khi thoa kem chống nắng là gì?', 3, 1),
(N'Bạn dự định sử dụng sản phẩm chống nắng chủ yếu trong môi trường hoặc hoạt động nào?', 4, 1),
(N'Ngoài việc bảo vệ khỏi tia UV, bạn muốn kem chống nắng hỗ trợ thêm vấn đề gì cho da?', 5, 1),
(N'Kết cấu sản phẩm (Format) nào mang lại cảm giác thoải mái nhất cho bạn khi thoa?', 6, 1);
GO

-- ĐÁP ÁN QUIZ MỚI
-- Câu 1 (Bộ Lọc Vật Lý / Hóa Học)
INSERT INTO dap_an_quiz (id_cau_hoi, noi_dung, id_loai_da, diem, icon) VALUES
(1, N'Dịu nhẹ tối đa, an toàn tuyệt đối (Không cay mắt, không gây mụn)', NULL, 0, NULL), 
(1, N'Trong suốt, tàng hình trên da (Bôi như không bôi, không bị trắng bệch)', NULL, 0, NULL),
(1, N'Có khả năng nâng tông làm sáng da (Thay thế được lớp trang điểm)', NULL, 0, NULL);

-- Câu 2
INSERT INTO dap_an_quiz (id_cau_hoi, noi_dung, id_loai_da, diem, icon) VALUES
(2, N'Bóng dầu diện rộng, đặc biệt là vùng trán, mũi và cằm', 1, 3, NULL), 
(2, N'Khô căng, có thể xuất hiện nếp nhăn nhỏ hoặc bong tróc nhẹ', 2, 3, NULL), 
(2, N'Khá cân bằng, chỉ đổ dầu rất nhẹ ở vùng chữ T', 4, 3, NULL), 
(2, N'Da dễ bị ửng đỏ, ngứa hoặc châm chích khi tiếp xúc với môi trường', 3, 3, NULL);

-- Câu 3
INSERT INTO dap_an_quiz (id_cau_hoi, noi_dung, id_loai_da, diem, icon) VALUES
(3, N'Hoàn toàn lì (Matte), ráo mịn và không thấy bóng dầu', 1, 2, NULL), 
(3, N'Căng bóng tự nhiên (Glowy/Dewy), da trông ẩm mượt', 2, 2, NULL), 
(3, N'Tự nhiên như da thật (Natural), không quá lì cũng không quá bóng', 4, 2, NULL);

-- Câu 4
INSERT INTO dap_an_quiz (id_cau_hoi, noi_dung, id_loai_da, diem, icon) VALUES
(4, N'Di chuyển ngoài trời nắng gắt hoặc đi biển, đi bơi', 1, 1, NULL), 
(4, N'Ngồi văn phòng máy lạnh phần lớn thời gian', 2, 1, NULL), 
(4, N'Tham gia thể thao, vận động mạnh đổ nhiều mồ hôi', 1, 1, NULL), 
(4, N'Sinh hoạt hàng ngày, ra ngoài trong thời gian ngắn', 4, 1, NULL);

-- Câu 5
INSERT INTO dap_an_quiz (id_cau_hoi, noi_dung, id_loai_da, diem, icon) VALUES
(5, N'Kiểm soát bã nhờn và ngăn ngừa mụn', 1, 2, NULL), 
(5, N'Bổ sung độ ẩm sâu, ngăn ngừa lão hóa sớm', 2, 2, NULL), 
(5, N'Làm dịu da, phục hồi hàng rào bảo vệ da', 3, 2, NULL), 
(5, N'Chống ô nhiễm, bụi mịn từ môi trường thành thị', 4, 2, NULL);

-- Câu 6
INSERT INTO dap_an_quiz (id_cau_hoi, noi_dung, id_loai_da, diem, icon) VALUES
(6, N'Dạng sữa lỏng (Milk/Fluid) - Thấm nhanh, ráo mặt', 1, 1, NULL), 
(6, N'Dạng kem đặc (Cream) - Giàu độ ẩm, dễ tán', 2, 1, NULL), 
(6, N'Dạng gel trong suốt - Mát lạnh, tan ngay trên da', 1, 1, NULL), 
(6, N'Dạng xịt (Spray) - Nhanh gọn, dễ dặm lại', 4, 1, NULL);
GO
