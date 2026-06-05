package org.example.templatejava6.product.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.product.entity.ChiTietSanPham;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ChiTietSanPhamResponse {

    private Integer id;
    private String sku;
    private BigDecimal dungTichMl;
    private BigDecimal giaBan;
    private Integer soLuongTon;
    private LocalDate hanSuDung;
    private Boolean trangThai;
    private String tenMauSac;

    public ChiTietSanPhamResponse(ChiTietSanPham ct) {
        this.id = ct.getId();
        this.sku = ct.getSku();
        this.dungTichMl = ct.getDungTichMl();
        this.giaBan = ct.getGiaBan();
        this.soLuongTon = ct.getSoLuongTon();
        this.hanSuDung = ct.getHanSuDung();
        this.trangThai = ct.getTrangThai();
        this.tenMauSac = ct.getMauSac() != null ? ct.getMauSac().getTen() : null;
    }
}
