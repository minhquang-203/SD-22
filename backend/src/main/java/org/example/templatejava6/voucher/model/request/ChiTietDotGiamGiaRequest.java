package org.example.templatejava6.voucher.model.request;

import jakarta.validation.constraints.NotNull;
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

    private Integer idDotGiamGia;

    @NotNull(message = "Chi tiết sản phẩm không được để trống")
    private Integer idChiTietSanPham;

    private BigDecimal giaSauGiam;
}
