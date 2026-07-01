package org.example.templatejava6.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRequestDto {
    private Integer idKhachHang; // Có thể null nếu khách chưa đăng nhập
    private Integer idPhien;     // Bắt buộc nếu đang chat tiếp, null nếu mở phiên mới
    private String noiDung;
}
