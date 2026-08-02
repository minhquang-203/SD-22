package org.example.templatejava6.nhaphang.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.nhaphang.entity.ChiTietPhieuNhap;
import org.example.templatejava6.nhaphang.entity.PhieuNhap;
import org.example.templatejava6.product.entity.ChiTietSanPham;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PhieuNhapResponse {

    private Integer id;
    private String maPhieuNhap;
    private Integer idNhaCungCap;
    private String tenNhaCungCap;
    private Integer idNhanVien;
    private String tenNhanVien;
    private String soHoaDonDauVao;
    private LocalDateTime ngayTao;
    private BigDecimal tongTien;
    private BigDecimal giamGia;
    private BigDecimal canTraNcc;
    private String trangThai;
    private String ghiChu;
    private List<DongResponse> chiTiets = new ArrayList<>();

    public PhieuNhapResponse(PhieuNhap p) {
        this.id = p.getId();
        this.maPhieuNhap = p.getMaPhieuNhap();
        if (p.getNhaCungCap() != null) {
            this.idNhaCungCap = p.getNhaCungCap().getId();
            this.tenNhaCungCap = p.getNhaCungCap().getTen();
        }
        if (p.getNhanVien() != null) {
            this.idNhanVien = p.getNhanVien().getId();
            this.tenNhanVien = p.getNhanVien().getHoTen();
        }
        this.soHoaDonDauVao = p.getSoHoaDonDauVao();
        this.ngayTao = p.getNgayTao();
        this.tongTien = p.getTongTien();
        this.giamGia = p.getGiamGia();
        this.canTraNcc = p.getCanTraNcc();
        this.trangThai = p.getTrangThai();
        this.ghiChu = p.getGhiChu();
        if (p.getChiTiets() != null) {
            this.chiTiets = p.getChiTiets().stream().map(DongResponse::new).toList();
        }
    }

    /** Bản danh sách — không cần chi tiết dòng. */
    public static PhieuNhapResponse summary(PhieuNhap p) {
        PhieuNhapResponse res = new PhieuNhapResponse(p);
        res.setChiTiets(List.of());
        return res;
    }

    @Getter
    @Setter
    public static class DongResponse {
        private Integer id;
        private Integer idChiTietSanPham;
        private String sku;
        private String tenSanPham;
        private String tenMauSac;
        private BigDecimal dungTichMl;
        private Integer soLuong;
        private BigDecimal donGia;
        private LocalDate hanSuDung;
        private String soLo;
        private BigDecimal thanhTien;
        private Integer idLoHang;

        public DongResponse(ChiTietPhieuNhap d) {
            this.id = d.getId();
            ChiTietSanPham ct = d.getChiTietSanPham();
            if (ct != null) {
                this.idChiTietSanPham = ct.getId();
                this.sku = ct.getSku();
                if (ct.getSanPham() != null) {
                    this.tenSanPham = ct.getSanPham().getTen();
                }
                if (ct.getMauSac() != null) {
                    this.tenMauSac = ct.getMauSac().getTen();
                }
                this.dungTichMl = ct.getDungTichMl();
            }
            this.soLuong = d.getSoLuong();
            this.donGia = d.getDonGia();
            this.hanSuDung = d.getHanSuDung();
            this.soLo = d.getSoLo();
            this.thanhTien = d.getThanhTien();
            if (d.getLoHang() != null) {
                this.idLoHang = d.getLoHang().getId();
            }
        }
    }
}
