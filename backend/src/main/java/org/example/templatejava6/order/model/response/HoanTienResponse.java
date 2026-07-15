package org.example.templatejava6.order.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.order.entity.HoanTien;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class HoanTienResponse {

    private Integer id;
    private Integer idHoaDon;
    private String maHoaDon;
    private Integer idYeuCauTraHang;
    private String loai;
    private String loaiLabel;
    private BigDecimal soTien;
    private String phuongThuc;
    private String trangThai;
    private String trangThaiLabel;
    private String maGiaoDichHoan;
    private String tenNganHang;
    private String soTaiKhoan;
    private String chuTaiKhoan;
    private String ghiChu;
    private String tenNhanVien;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayHoan;

    public HoanTienResponse(HoanTien ht) {
        this.id = ht.getId();
        if (ht.getIdHoaDon() != null) {
            this.idHoaDon = ht.getIdHoaDon().getId();
            this.maHoaDon = ht.getIdHoaDon().getMaHoaDon();
        }
        this.idYeuCauTraHang = ht.getIdYeuCauTraHang() != null ? ht.getIdYeuCauTraHang().getId() : null;
        if (ht.getLoai() != null) {
            this.loai = ht.getLoai().name();
            this.loaiLabel = ht.getLoai().getLabel();
        }
        this.soTien = ht.getSoTien();
        this.phuongThuc = ht.getPhuongThuc();
        if (ht.getTrangThai() != null) {
            this.trangThai = ht.getTrangThai().name();
            this.trangThaiLabel = ht.getTrangThai().getLabel();
        }
        this.maGiaoDichHoan = ht.getMaGiaoDichHoan();
        this.tenNganHang = ht.getTenNganHang();
        this.soTaiKhoan = ht.getSoTaiKhoan();
        this.chuTaiKhoan = ht.getChuTaiKhoan();
        this.ghiChu = ht.getGhiChu();
        this.tenNhanVien = ht.getIdNhanVien() != null ? ht.getIdNhanVien().getHoTen() : null;
        this.ngayTao = ht.getNgayTao();
        this.ngayHoan = ht.getNgayHoan();
    }
}
