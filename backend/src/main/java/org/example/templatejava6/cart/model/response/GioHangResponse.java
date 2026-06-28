package org.example.templatejava6.cart.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.cart.entity.GioHang;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class GioHangResponse {

    private Integer id;
    private Integer idKhachHang;
    private LocalDateTime ngayTao;
    private List<ChiTietGioHangResponse> items;
    private Integer tongSoLuong;
    private BigDecimal tongTien;

    public GioHangResponse(GioHang gioHang, List<ChiTietGioHangResponse> items) {
        this.id = gioHang.getId();
        this.idKhachHang = gioHang.getKhachHang() != null ? gioHang.getKhachHang().getId() : null;
        this.ngayTao = gioHang.getNgayTao();
        this.items = items;
        this.tongSoLuong = items.stream()
                .map(ChiTietGioHangResponse::getSoLuong)
                .filter(soLuong -> soLuong != null)
                .reduce(0, Integer::sum);
        this.tongTien = items.stream()
                .map(ChiTietGioHangResponse::getThanhTien)
                .filter(thanhTien -> thanhTien != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
