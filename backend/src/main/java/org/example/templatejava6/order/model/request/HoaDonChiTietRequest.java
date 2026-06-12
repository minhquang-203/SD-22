package org.example.templatejava6.order.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HoaDonChiTietRequest {

    private Integer id;

    private Integer idHoaDon;

    @NotNull(message = "Chi tiết sản phẩm không được để trống")
    private Integer idChiTietSanPham;

    @NotNull(message = "Số lượng không được để trống")
    @Positive(message = "Số lượng phải lớn hơn 0")
    private Integer soLuong;

    private BigDecimal donGia;
}
