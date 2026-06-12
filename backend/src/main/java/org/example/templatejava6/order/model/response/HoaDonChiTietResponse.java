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
        this.soLuong = ct.getSoLuong();
        this.donGia = ct.getDonGia();
        this.thanhTien = ct.getThanhTien();
    }
}
