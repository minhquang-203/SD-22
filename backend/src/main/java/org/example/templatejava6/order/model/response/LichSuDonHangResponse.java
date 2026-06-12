package org.example.templatejava6.order.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.common.enums.TrangThaiDonHang;
import org.example.templatejava6.order.entity.LichSuDonHang;

import java.time.LocalDateTime;

@Getter
@Setter
public class LichSuDonHangResponse {

    private Integer id;
    private Integer idHoaDon;
    private String maHoaDon;
    private String trangThai;
    private String trangThaiLabel;
    private String ghiChu;
    private Integer idNhanVien;
    private String tenNhanVien;
    private LocalDateTime thoiGian;

    public LichSuDonHangResponse(LichSuDonHang ls) {
        this.id = ls.getId();
        this.idHoaDon = ls.getIdHoaDon() != null ? ls.getIdHoaDon().getId() : null;
        this.maHoaDon = ls.getIdHoaDon() != null ? ls.getIdHoaDon().getMaHoaDon() : null;
        this.trangThai = ls.getTrangThai();
        this.trangThaiLabel = resolveLabel(ls.getTrangThai());
        this.ghiChu = ls.getGhiChu();
        this.idNhanVien = ls.getIdNhanVien() != null ? ls.getIdNhanVien().getId() : null;
        this.tenNhanVien = ls.getIdNhanVien() != null ? ls.getIdNhanVien().getHoTen() : null;
        this.thoiGian = ls.getThoiGian();
    }

    private static String resolveLabel(String ma) {
        if (ma == null) {
            return null;
        }
        try {
            return TrangThaiDonHang.valueOf(ma).getLabel();
        } catch (IllegalArgumentException ignored) {
            return switch (ma) {
                case "TAO_DON" -> "Tạo đơn";
                case "THEM_HANG" -> "Thêm hàng";
                case "AP_MA" -> "Áp mã giảm giá";
                case "THANH_TOAN" -> "Thanh toán";
                default -> ma;
            };
        }
    }
}
