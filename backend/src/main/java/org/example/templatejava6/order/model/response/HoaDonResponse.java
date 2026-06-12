package org.example.templatejava6.order.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.common.enums.TrangThaiDonHang;
import org.example.templatejava6.order.entity.HoaDon;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class HoaDonResponse {

    private Integer id;
    private String maHoaDon;
    private String loaiDon;
    private TrangThaiDonHang trangThai;
    private String trangThaiLabel;
    private String diaChiGiao;
    private BigDecimal tongTien;
    private BigDecimal tienGiamGia;
    private BigDecimal phiVanChuyen;
    private BigDecimal thanhTien;
    private String ghiChu;
    private LocalDateTime ngayTao;
    private String tenKhachHang;
    private String tenNhanVien;
    private String tenPhuongThucThanhToan;
    private String maPhieuGiamGia;

    public HoaDonResponse(HoaDon hd) {
        this.id = hd.getId();
        this.maHoaDon = hd.getMaHoaDon();
        this.loaiDon = hd.getLoaiDon();
        this.trangThai = hd.getTrangThai();
        this.trangThaiLabel = hd.getTrangThai() != null ? hd.getTrangThai().getLabel() : null;
        this.diaChiGiao = hd.getDiaChiGiao();
        this.tongTien = hd.getTongTien();
        this.tienGiamGia = hd.getTienGiamGia();
        this.phiVanChuyen = hd.getPhiVanChuyen();
        this.thanhTien = hd.getThanhTien();
        this.ghiChu = hd.getGhiChu();
        this.ngayTao = hd.getNgayTao();
        this.tenKhachHang = hd.getIdKhachHang() != null ? hd.getIdKhachHang().getHoTen() : null;
        this.tenNhanVien = hd.getIdNhanVien() != null ? hd.getIdNhanVien().getHoTen() : null;
        this.tenPhuongThucThanhToan = hd.getIdPhuongThucThanhToan() != null
                ? hd.getIdPhuongThucThanhToan().getTen()
                : null;
        this.maPhieuGiamGia = hd.getIdPhieuGiamGia() != null ? hd.getIdPhieuGiamGia().getMa() : null;
    }
}
