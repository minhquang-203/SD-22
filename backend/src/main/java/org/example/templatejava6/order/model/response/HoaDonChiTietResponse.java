package org.example.templatejava6.order.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.order.entity.HoaDonChiTiet;

import java.math.BigDecimal;

@Getter
@Setter
public class HoaDonChiTietResponse {

    private Integer id;
    private Integer idHoaDon;
    private Integer idChiTietSanPham;
    private String sku;
    private String tenSanPham;
    private String bienThe;
    private Integer soLuong;
    private BigDecimal donGia;
    private BigDecimal thanhTien;

    public HoaDonChiTietResponse(HoaDonChiTiet ct) {
        this.id = ct.getId();
        this.idHoaDon = ct.getIdHoaDon() != null ? ct.getIdHoaDon().getId() : null;
        this.idChiTietSanPham = ct.getIdChiTietSanPham() != null ? ct.getIdChiTietSanPham().getId() : null;
        this.sku = ct.getIdChiTietSanPham() != null ? ct.getIdChiTietSanPham().getSku() : null;
        this.tenSanPham = ct.getIdChiTietSanPham() != null
                && ct.getIdChiTietSanPham().getSanPham() != null
                ? ct.getIdChiTietSanPham().getSanPham().getTen()
                : null;
        if (ct.getIdChiTietSanPham() != null) {
            var cts = ct.getIdChiTietSanPham();
            String dt = cts.getDungTichMl() != null
                    ? cts.getDungTichMl().stripTrailingZeros().toPlainString() + "ml" : null;
            String ms = cts.getMauSac() != null ? cts.getMauSac().getTen() : null;
            if (dt != null && ms != null) {
                this.bienThe = dt + " / " + ms;
            } else if (dt != null) {
                this.bienThe = dt;
            } else {
                this.bienThe = ms;
            }
        }
        this.soLuong = ct.getSoLuong();
        this.donGia = ct.getDonGia();
        this.thanhTien = ct.getThanhTien();
    }
}
