package org.example.templatejava6.order.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** Đơn giá khách đang thấy trên màn hình cho một biến thể (dùng đối chiếu PRICE_CHANGED). */
@Getter
@Setter
public class DongGiaKhachThayRequest {

    @NotNull(message = "Thiếu idChiTietSanPham")
    private Integer idChiTietSanPham;

    @NotNull(message = "Thiếu giá khách đang thấy")
    private BigDecimal giaKhachThay;
}
