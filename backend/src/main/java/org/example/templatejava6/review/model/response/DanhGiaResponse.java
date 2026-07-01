package org.example.templatejava6.review.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.review.entity.DanhGia;

import java.time.LocalDateTime;

@Getter
@Setter
public class DanhGiaResponse {

    private Integer id;
    private Integer idKhachHang;
    private Integer idSanPham;
    private Byte soSao;
    private String noiDung;
    private String trangThai;
    private LocalDateTime ngayTao;
    private String tenSanPham;
    private String phanHoiCuaShop;
    private String hinhAnhVideo;
    private Integer idHoaDonChiTiet;
    private Integer soLuotThich;
    private String tenPhanLoaiHang;
    private String tenKhachHang;

    public DanhGiaResponse(DanhGia dg) {
        this.id = dg.getId();
        this.idKhachHang = dg.getIdKhachHang();
        this.tenKhachHang = dg.getKhachHang() != null ? dg.getKhachHang().getHoTen() : ("khachhang_" + dg.getIdKhachHang());
        this.idSanPham = dg.getSanPham() != null ? dg.getSanPham().getId() : null;
        this.soSao = dg.getSoSao();
        this.noiDung = dg.getNoiDung();
        this.trangThai = dg.getTrangThai();
        this.ngayTao = dg.getNgayTao();
        this.tenSanPham = dg.getSanPham() != null ? dg.getSanPham().getTen() : null;
        this.phanHoiCuaShop = dg.getPhanHoiCuaShop();
        this.hinhAnhVideo = dg.getHinhAnhVideo();
        this.soLuotThich = dg.getSoLuotThich() != null ? dg.getSoLuotThich() : 0;
        
        if (dg.getHoaDonChiTiet() != null) {
            this.idHoaDonChiTiet = dg.getHoaDonChiTiet().getId();
            org.example.templatejava6.product.entity.ChiTietSanPham ctsp = dg.getHoaDonChiTiet().getIdChiTietSanPham();
            if (ctsp != null) {
                String ms = ctsp.getMauSac() != null ? ctsp.getMauSac().getTen() : "";
                String dt = ctsp.getDungTichMl() != null ? ctsp.getDungTichMl() + "ml" : "";
                this.tenPhanLoaiHang = (ms + " " + dt).trim();
            }
        }
    }
}
