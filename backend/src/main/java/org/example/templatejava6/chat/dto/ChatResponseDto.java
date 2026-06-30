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
    private SanPhamResponse sanPhamGoiY; // DTO trả về thông tin thẻ sản phẩm nếu có
    private LocalDateTime thoiGian;
}
