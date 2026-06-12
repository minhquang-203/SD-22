package org.example.templatejava6.voucher.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.common.entity.PhieuGiamGia;
import org.example.templatejava6.common.enums.LoaiPhieuGiamGia;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PhieuGiamGiaResponse {

    private Integer id;
    private String ma;
    private String ten;
    private LoaiPhieuGiamGia loai;
    private BigDecimal giaTri;
    private BigDecimal giaTriDonToiThieu;
    private BigDecimal giamToiDa;
    private Integer soLuong;
    private LocalDateTime ngayBatDau;
    private LocalDateTime ngayKetThuc;
    private Boolean trangThai;

    public PhieuGiamGiaResponse(PhieuGiamGia pgg) {
        this.id = pgg.getId();
        this.ma = pgg.getMa();
        this.ten = pgg.getTen();
        this.loai = pgg.getLoai();
        this.giaTri = pgg.getGiaTri();
        this.giaTriDonToiThieu = pgg.getGiaTriDonToiThieu();
        this.giamToiDa = pgg.getGiamToiDa();
        this.soLuong = pgg.getSoLuong();
        this.ngayBatDau = pgg.getNgayBatDau();
        this.ngayKetThuc = pgg.getNgayKetThuc();
        this.trangThai = pgg.getTrangThai();
    }
}
