package org.example.templatejava6.order.model.response;

import lombok.Builder;
import lombok.Getter;
import org.example.templatejava6.common.enums.TrangThaiDonHang;
import org.example.templatejava6.order.entity.HoaDon;
import org.example.templatejava6.payment.model.response.TaoThanhToanResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class OnlineCheckoutResponse {

    private Integer idHoaDon;
    private String maHoaDon;
    private String loaiDon;
    private TrangThaiDonHang trangThai;
    private String trangThaiLabel;
    private String maPhuongThucThanhToan;
    private String tenPhuongThucThanhToan;
    private String maPhieuGiamGia;
    private BigDecimal tongTien;
    private BigDecimal tienGiamGia;
    private BigDecimal phiVanChuyen;
    private BigDecimal thanhTien;
    private String paymentUrl;
    private String transactionRef;
    private LocalDateTime ngayTao;

    public static OnlineCheckoutResponse from(HoaDon hoaDon, TaoThanhToanResponse payment) {
        return OnlineCheckoutResponse.builder()
                .idHoaDon(hoaDon.getId())
                .maHoaDon(hoaDon.getMaHoaDon())
                .loaiDon(hoaDon.getLoaiDon())
                .trangThai(hoaDon.getTrangThai())
                .trangThaiLabel(hoaDon.getTrangThai() != null ? hoaDon.getTrangThai().getLabel() : null)
                .maPhuongThucThanhToan(hoaDon.getIdPhuongThucThanhToan() != null
                        ? hoaDon.getIdPhuongThucThanhToan().getMa()
                        : null)
                .tenPhuongThucThanhToan(hoaDon.getIdPhuongThucThanhToan() != null
                        ? hoaDon.getIdPhuongThucThanhToan().getTen()
                        : null)
                .maPhieuGiamGia(hoaDon.getIdPhieuGiamGia() != null ? hoaDon.getIdPhieuGiamGia().getMa() : null)
                .tongTien(hoaDon.getTongTien())
                .tienGiamGia(hoaDon.getTienGiamGia())
                .phiVanChuyen(hoaDon.getPhiVanChuyen())
                .thanhTien(hoaDon.getThanhTien())
                .paymentUrl(payment != null ? payment.getPaymentUrl() : null)
                .transactionRef(payment != null ? payment.getTransactionRef() : null)
                .ngayTao(hoaDon.getNgayTao())
                .build();
    }
}
