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

    public DanhGiaResponse(DanhGia dg) {
        this.id = dg.getId();
        this.idKhachHang = dg.getIdKhachHang();
        this.idSanPham = dg.getSanPham() != null ? dg.getSanPham().getId() : null;
        this.soSao = dg.getSoSao();
        this.noiDung = dg.getNoiDung();
        this.trangThai = dg.getTrangThai();
        this.ngayTao = dg.getNgayTao();
        this.tenSanPham = dg.getSanPham() != null ? dg.getSanPham().getTen() : null;
    }
}
