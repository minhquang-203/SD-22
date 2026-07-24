package org.example.templatejava6.order.model.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OnlineTinhGiaResponse {

    private BigDecimal tongTien;
    private BigDecimal tienGiamGia;
    private BigDecimal phiVanChuyen;
    private BigDecimal thanhTien;
    private String maPhieuGiamGia;
}
