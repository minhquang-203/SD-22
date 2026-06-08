package org.example.templatejava6.product.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.product.entity.SanPham;

import java.time.LocalDateTime;

@Getter
@Setter
public class SanPhamResponse {

    private Integer id;
    private String maSanPham;
    private String ten;
    private String chiSoSpf;
    private String chiSoPa;
    private String loaiChongNang;
    private Boolean khangNuoc;
    private Boolean trangThai;
    private LocalDateTime ngayTao;
    private String tenThuongHieu;
    private String tenDanhMuc;
    private String tenDangSanPham;
    /** URL ảnh chính (thumbnail) — từ bảng anh_san_pham */
    private String anhChinhUrl;

    public SanPhamResponse(SanPham sp) {
        this.id = sp.getId();
        this.maSanPham = sp.getMaSanPham();
        this.ten = sp.getTen();
        this.chiSoSpf = sp.getChiSoSpf();
        this.chiSoPa = sp.getChiSoPa();
        this.loaiChongNang = sp.getLoaiChongNang();
        this.khangNuoc = sp.getKhangNuoc();
        this.trangThai = sp.getTrangThai();
        this.ngayTao = sp.getNgayTao();
        this.tenThuongHieu = sp.getThuongHieu() != null ? sp.getThuongHieu().getTen() : null;
        this.tenDanhMuc = sp.getDanhMuc() != null ? sp.getDanhMuc().getTen() : null;
        this.tenDangSanPham = sp.getDangSanPham() != null ? sp.getDangSanPham().getTen() : null;
    }
}
