package org.example.templatejava6.order.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.order.entity.YeuCauTraHang;

import java.time.LocalDateTime;

@Getter
@Setter
public class YeuCauTraHangResponse {

    private Integer id;
    private Integer idHoaDon;
    private String maHoaDon;
    private String tenKhachHang;
    private String lyDo;
    private String moTa;
    private String trangThai;
    private String trangThaiLabel;
    private String maVanDonTra;
    private Integer ghnDistrictId;
    private String ghnWardCode;
    private String diaChiTra;
    private String tenNganHang;
    private String soTaiKhoan;
    private String chuTaiKhoan;
    private String ghiChuAdmin;
    private String phuongThucThanhToan;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;

    public YeuCauTraHangResponse(YeuCauTraHang yc) {
        this.id = yc.getId();
        if (yc.getIdHoaDon() != null) {
            this.idHoaDon = yc.getIdHoaDon().getId();
            this.maHoaDon = yc.getIdHoaDon().getMaHoaDon();
            if (yc.getIdHoaDon().getIdKhachHang() != null) {
                this.tenKhachHang = yc.getIdHoaDon().getIdKhachHang().getHoTen();
            }
            if (yc.getIdHoaDon().getIdPhuongThucThanhToan() != null) {
                this.phuongThucThanhToan = yc.getIdHoaDon().getIdPhuongThucThanhToan().getMa();
            }
        }
        this.lyDo = yc.getLyDo();
        this.moTa = yc.getMoTa();
        if (yc.getTrangThai() != null) {
            this.trangThai = yc.getTrangThai().name();
            this.trangThaiLabel = yc.getTrangThai().getLabel();
        }
        this.maVanDonTra = yc.getMaVanDonTra();
        this.ghnDistrictId = yc.getGhnDistrictId();
        this.ghnWardCode = yc.getGhnWardCode();
        this.diaChiTra = yc.getDiaChiTra();
        this.tenNganHang = yc.getTenNganHang();
        this.soTaiKhoan = yc.getSoTaiKhoan();
        this.chuTaiKhoan = yc.getChuTaiKhoan();
        this.ghiChuAdmin = yc.getGhiChuAdmin();
        this.ngayTao = yc.getNgayTao();
        this.ngayCapNhat = yc.getNgayCapNhat();
    }
}
