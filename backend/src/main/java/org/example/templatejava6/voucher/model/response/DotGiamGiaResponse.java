package org.example.templatejava6.voucher.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.voucher.entity.DotGiamGia;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class DotGiamGiaResponse {

    private Integer id;
    private String ma;
    private String ten;
    private BigDecimal phanTramGiam;
    private LocalDateTime ngayBatDau;
    private LocalDateTime ngayKetThuc;
    private Boolean trangThai;
    private String timeStatus;
    private String timeStatusLabel;

    public DotGiamGiaResponse(DotGiamGia dgg) {
        this.id = dgg.getId();
        this.ma = dgg.getMa();
        this.ten = dgg.getTen();
        this.phanTramGiam = dgg.getPhanTramGiam();
        this.ngayBatDau = dgg.getNgayBatDau();
        this.ngayKetThuc = dgg.getNgayKetThuc();
        this.trangThai = dgg.getTrangThai();
        resolveTimeStatus(dgg);
    }

    private void resolveTimeStatus(DotGiamGia dgg) {
        if (!Boolean.TRUE.equals(dgg.getTrangThai())) {
            this.timeStatus = "INACTIVE";
            this.timeStatusLabel = "Ngừng áp dụng";
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        if (dgg.getNgayBatDau() != null && dgg.getNgayBatDau().isAfter(now)) {
            this.timeStatus = "UPCOMING";
            this.timeStatusLabel = "Sắp diễn ra";
            return;
        }
        if (dgg.getNgayKetThuc() != null && dgg.getNgayKetThuc().isBefore(now)) {
            this.timeStatus = "EXPIRED";
            this.timeStatusLabel = "Đã kết thúc";
            return;
        }
        this.timeStatus = "ACTIVE";
        this.timeStatusLabel = "Đang chạy";
    }
}
