package org.example.templatejava6.order.model.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class StorefrontOrderLineResponse {

    private String tenSanPham;
    private String bienThe;
    private Integer soLuong;
    private BigDecimal donGia;
    private BigDecimal thanhTien;
    private String anhUrl;
}
