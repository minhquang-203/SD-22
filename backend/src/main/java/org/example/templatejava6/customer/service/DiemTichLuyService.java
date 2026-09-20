package org.example.templatejava6.customer.service;

import org.example.templatejava6.common.entity.KhachHang;
import org.example.templatejava6.customer.repository.KhachHangRepository;
import org.example.templatejava6.order.entity.HoaDon;
import org.example.templatejava6.voucher.service.VoucherKhachHangService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Cộng điểm tích lũy khi đơn hoàn thành. 1.000đ thành tiền = 1 điểm.
 * Chỉ áp dụng khách đã có tài khoản ({@code id_khach_hang}); khách vãng lai bỏ qua.
 */
@Service
public class DiemTichLuyService {

    private static final Logger log = LoggerFactory.getLogger(DiemTichLuyService.class);
    private static final BigDecimal DON_VI_DIEM = BigDecimal.valueOf(1000);

    private final KhachHangRepository khachHangRepository;
    private final VoucherKhachHangService voucherKhachHangService;

    public DiemTichLuyService(KhachHangRepository khachHangRepository,
                              VoucherKhachHangService voucherKhachHangService) {
        this.khachHangRepository = khachHangRepository;
        this.voucherKhachHangService = voucherKhachHangService;
    }

    @Transactional
    public int congDiemTuDonHoanThanh(HoaDon hoaDon) {
        if (hoaDon == null || hoaDon.getIdKhachHang() == null || hoaDon.getIdKhachHang().getId() == null) {
            return 0;
        }
        BigDecimal thanhTien = hoaDon.getThanhTien();
        if (thanhTien == null || thanhTien.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        int diemThem = thanhTien.divide(DON_VI_DIEM, 0, RoundingMode.FLOOR).intValue();
        if (diemThem <= 0) {
            return 0;
        }

        KhachHang khachHang = khachHangRepository.findById(hoaDon.getIdKhachHang().getId()).orElse(null);
        if (khachHang == null) {
            return 0;
        }
        int diemHien = khachHang.getDiemTichLuy() != null ? khachHang.getDiemTichLuy() : 0;
        khachHang.setDiemTichLuy(diemHien + diemThem);
        khachHangRepository.save(khachHang);
        hoaDon.setIdKhachHang(khachHang);
        voucherKhachHangService.tuDongGanKhiCapNhatDiem(khachHang);
        log.info("Cộng {} điểm đơn {} cho khách {} (tổng {})",
                diemThem, hoaDon.getMaHoaDon(), khachHang.getId(), khachHang.getDiemTichLuy());
        return diemThem;
    }
}
