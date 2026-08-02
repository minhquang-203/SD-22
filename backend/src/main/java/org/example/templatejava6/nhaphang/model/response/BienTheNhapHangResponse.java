package org.example.templatejava6.nhaphang.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.product.entity.ChiTietSanPham;

import java.math.BigDecimal;

@Getter
@Setter
public class BienTheNhapHangResponse {

    private Integer idChiTietSanPham;
    private String sku;
    private String tenSanPham;
    private String tenMauSac;
    private BigDecimal dungTichMl;
    private BigDecimal giaBan;
    private Integer soLuongTon;

    public BienTheNhapHangResponse(ChiTietSanPham ct) {
        this.idChiTietSanPham = ct.getId();
        this.sku = ct.getSku();
        if (ct.getSanPham() != null) {
            this.tenSanPham = ct.getSanPham().getTen();
        }
        if (ct.getMauSac() != null) {
            this.tenMauSac = ct.getMauSac().getTen();
        }
        this.dungTichMl = ct.getDungTichMl();
        this.giaBan = ct.getGiaBan();
        this.soLuongTon = ct.getSoLuongTon();
    }
}
