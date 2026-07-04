package org.example.templatejava6.order.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.product.entity.ChiTietSanPham;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class BienTheBanResponse {

    private Integer idChiTietSanPham;
    private String sku;
    private String tenSanPham;
    private BigDecimal dungTichMl;
    private String tenMauSac;
    private BigDecimal giaBan;
    private BigDecimal giaGoc;
    private BigDecimal phanTramGiam;
    private Boolean dangGiamGia;
    private Integer soLuongTon;
    private LocalDate hanSuDungGanNhat;
    private Integer soNgayConLai;
    private String anhUrl;

    public BienTheBanResponse(ChiTietSanPham cts) {
        this.idChiTietSanPham = cts.getId();
        this.sku = cts.getSku();
        this.tenSanPham = cts.getSanPham() != null ? cts.getSanPham().getTen() : null;
        this.dungTichMl = cts.getDungTichMl();
        this.tenMauSac = cts.getMauSac() != null ? cts.getMauSac().getTen() : null;
        this.giaBan = cts.getGiaBan();
        this.soLuongTon = cts.getSoLuongTon();
    }
}
