package org.example.templatejava6.order.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.order.entity.YeuCauMuaSoLuongLon;
import org.example.templatejava6.product.entity.ChiTietSanPham;

import java.time.LocalDateTime;

@Getter
@Setter
public class YeuCauMuaSoLuongLonResponse {

    private Integer id;
    private Integer idKhachHang;
    private Integer idChiTietSanPham;
    private String tenSanPham;
    private String sku;
    private String bienThe;
    private Integer soLuong;
    private String tenCongTy;
    private String hoTen;
    private String soDienThoai;
    private String email;
    private String ghiChu;
    private Boolean nhanKhuyenMai;
    private String trangThai;
    private LocalDateTime ngayTao;
    private String ghiChuNoiBo;
    private Integer idNhanVienXuLy;
    private String tenNhanVienXuLy;

    public static YeuCauMuaSoLuongLonResponse from(YeuCauMuaSoLuongLon y) {
        YeuCauMuaSoLuongLonResponse r = new YeuCauMuaSoLuongLonResponse();
        r.setId(y.getId());
        if (y.getIdKhachHang() != null) {
            r.setIdKhachHang(y.getIdKhachHang().getId());
        }
        ChiTietSanPham ct = y.getIdChiTietSanPham();
        if (ct != null) {
            r.setIdChiTietSanPham(ct.getId());
            r.setSku(ct.getSku());
            if (ct.getSanPham() != null) {
                r.setTenSanPham(ct.getSanPham().getTen());
            }
            String dt = ct.getDungTichMl() != null
                    ? ct.getDungTichMl().stripTrailingZeros().toPlainString() + "ml"
                    : null;
            String ms = ct.getMauSac() != null ? ct.getMauSac().getTen() : null;
            if (dt != null && ms != null) {
                r.setBienThe(dt + " / " + ms);
            } else if (dt != null) {
                r.setBienThe(dt);
            } else {
                r.setBienThe(ms);
            }
        }
        r.setSoLuong(y.getSoLuong());
        r.setTenCongTy(y.getTenCongTy());
        r.setHoTen(y.getHoTen());
        r.setSoDienThoai(y.getSoDienThoai());
        r.setEmail(y.getEmail());
        r.setGhiChu(y.getGhiChu());
        r.setNhanKhuyenMai(y.getNhanKhuyenMai());
        r.setTrangThai(y.getTrangThai());
        r.setNgayTao(y.getNgayTao());
        r.setGhiChuNoiBo(y.getGhiChuNoiBo());
        if (y.getIdNhanVienXuLy() != null) {
            r.setIdNhanVienXuLy(y.getIdNhanVienXuLy().getId());
            r.setTenNhanVienXuLy(y.getIdNhanVienXuLy().getHoTen());
        }
        return r;
    }
}
