package org.example.templatejava6.order.model.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class StorefrontOrderLineResponse {

    private Integer id;
    private Integer idChiTietSanPham;
    private Integer idSanPham;
    private String tenSanPham;
    private String bienThe;
    private Integer soLuong;
    private BigDecimal donGia;
    private BigDecimal thanhTien;
    private String anhUrl;
    private Boolean daDanhGia;
    private String trangThaiDanhGia;
}
