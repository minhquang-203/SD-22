package org.example.templatejava6.voucher.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.voucher.entity.DotGiamGia;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class DotGiamGiaResponse {

    private Integer id;
    private String ma;
    private String ten;
    private BigDecimal phanTramGiam;
    private LocalDateTime ngayBatDau;
    private LocalDateTime ngayKetThuc;
    private Boolean trangThai;

    public DotGiamGiaResponse(DotGiamGia dgg) {
        this.id = dgg.getId();
        this.ma = dgg.getMa();
        this.ten = dgg.getTen();
        this.phanTramGiam = dgg.getPhanTramGiam();
        this.ngayBatDau = dgg.getNgayBatDau();
        this.ngayKetThuc = dgg.getNgayKetThuc();
        this.trangThai = dgg.getTrangThai();
    }
}
