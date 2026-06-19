package org.example.templatejava6.voucher.model.request;

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
public class ChiTietDotGiamGiaRequest {

    @NotNull(message = "Đợt giảm giá không được để trống")
    private Integer idDotGiamGia;

    @NotNull(message = "Chi tiết sản phẩm không được để trống")
    private Integer idChiTietSanPham;

    @Positive(message = "Giá sau giảm phải lớn hơn 0")
    private BigDecimal giaSauGiam;
}
