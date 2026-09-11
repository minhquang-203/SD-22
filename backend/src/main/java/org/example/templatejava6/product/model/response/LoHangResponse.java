package org.example.templatejava6.product.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.product.entity.LoHang;

import java.time.LocalDate;

@Getter
@Setter
public class LoHangResponse {

    private Integer id;
    private Integer idChiTietSanPham;
    private String soLo;
    private LocalDate ngayNhap;
    private LocalDate hanSuDung;
    private Integer soLuongNhap;
    private Integer soLuongCon;
    private Integer soLuongLoi;
    private String ghiChu;
    private Boolean trangThai;
    private Boolean sapHetHan;

    public LoHangResponse(LoHang lo) {
        this.id = lo.getId();
        this.idChiTietSanPham = lo.getChiTietSanPham() != null ? lo.getChiTietSanPham().getId() : null;
        this.soLo = lo.getSoLo();
        this.ngayNhap = lo.getNgayNhap();
        this.hanSuDung = lo.getHanSuDung();
        this.soLuongNhap = lo.getSoLuongNhap();
        this.soLuongCon = lo.getSoLuongCon();
        this.soLuongLoi = lo.getSoLuongLoi() != null ? lo.getSoLuongLoi() : 0;
        this.ghiChu = lo.getGhiChu();
        this.trangThai = lo.getTrangThai();
        this.sapHetHan = isSapHetHan(lo.getHanSuDung());
    }

    public static boolean isSapHetHan(LocalDate hanSuDung) {
        if (hanSuDung == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        return !hanSuDung.isBefore(today) && hanSuDung.isBefore(today.plusMonths(6));
    }
}
