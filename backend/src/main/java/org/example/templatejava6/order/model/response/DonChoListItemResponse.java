package org.example.templatejava6.order.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.order.entity.HoaDon;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class DonChoListItemResponse {

    private Integer id;
    private String maHoaDon;
    private String tenKhachHang;
    private String soDienThoai;
    private Integer soMatHang;
    private BigDecimal thanhTien;
    private LocalDateTime ngayTao;

    public DonChoListItemResponse(HoaDon hd, int soMatHang) {
        this.id = hd.getId();
        this.maHoaDon = hd.getMaHoaDon();
        if (hd.getIdKhachHang() != null) {
            this.tenKhachHang = hd.getIdKhachHang().getHoTen();
            this.soDienThoai = hd.getIdKhachHang().getSoDienThoai();
        } else {
            this.tenKhachHang = "Khách lẻ";
        }
        this.soMatHang = soMatHang;
        this.thanhTien = hd.getThanhTien();
        this.ngayTao = hd.getNgayTao();
    }
}
