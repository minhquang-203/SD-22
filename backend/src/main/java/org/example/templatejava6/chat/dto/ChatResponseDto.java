package org.example.templatejava6.chat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.product.model.response.SanPhamResponse;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ChatResponseDto {
    private Integer idTinNhan;
    private Integer idPhien;
    private String nguoiGui;
    private String noiDung;
    private SanPhamResponse sanPhamGoiY; // Giữ lại dự phòng
    private java.util.List<SanPhamResponse> danhSachSanPhamGoiY; // Danh sách nhiều sản phẩm
    private LocalDateTime thoiGian;
}
