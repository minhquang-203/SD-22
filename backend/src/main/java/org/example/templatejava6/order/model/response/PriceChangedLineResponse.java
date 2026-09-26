package org.example.templatejava6.order.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class PriceChangedLineResponse {

    private Integer idChiTietSanPham;
    private String tenSanPham;
    private BigDecimal giaCu;
    private BigDecimal giaMoi;
}
